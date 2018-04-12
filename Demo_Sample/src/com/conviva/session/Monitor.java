// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import com.conviva.api.Client;
import com.conviva.utils.Lang;
import com.conviva.api.ConvivaException;
import com.conviva.api.system.IGraphicalInterface;
import com.conviva.internal.StreamerError;
import com.conviva.api.ContentMetadata;
import com.conviva.api.Client.AdPlayer;
import com.conviva.api.Client.AdPosition;
import com.conviva.api.Client.AdStream;
import com.conviva.api.SystemFactory;
import com.conviva.api.player.PlayerStateManager;
import com.conviva.platforms.android.AndroidNetworkUtils;
import com.conviva.protocol.Protocol;
import com.conviva.utils.ExceptionCatcher;
import com.conviva.utils.Logger;
import com.conviva.utils.Time;

/**
 * This class polls the streamer via a proxy, maintains playing state.
 */
public class Monitor implements IMonitorNotifier {

    /**
     * InternalPlayerState enum defines the player states internally used by Conviva.
     */
    public static enum InternalPlayerState {
        STOPPED,
        PLAYING,
        BUFFERING,
        PAUSED,
        UNKNOWN,
        NOT_MONITORED,
    };

	private Logger _logger;
	
    private int _sessionId = 0; // Used to identify the session in logs
    // @owner
    private PlayerStateManager _playerStateManager = null;
    private EventQueue _eventQueue = null;
    private ContentMetadata _contentMetadata = null;
    private SystemFactory _systemFactory = null;
    private ExceptionCatcher _exceptionCatcher = null;
    private Time _time = null;
    private double _startTimeMs = 0;		
    private boolean _hasJoined = false;		
    private boolean _pauseJoin = false;		
    private boolean _preloading = false;
    private boolean _ignorePlayerState = false;
    private InternalPlayerState _pooledPlayerState = InternalPlayerState.NOT_MONITORED;
    private boolean _ignoreBitrateAndResource = false;
    private boolean _ignoreEncodedFrameRateAndDuration = false;
    private boolean _ignoreError = false;

    private boolean _adPlaying = false;
    private AdStream _adStream = null;
    private AdPlayer _adPlayer = null;
    //Proxy state
    private InternalPlayerState _playerState = InternalPlayerState.NOT_MONITORED;
    private int _bitrateKbps = -1;
    private int _sessionFlags = Protocol.CAPABILITY_VIDEO +
    							Protocol.CAPABILITY_QUALITY_METRICS +
    							Protocol.CAPABILITY_BITRATE_METRICS;
    
    /// @brief The duration of the content, in seconds. This is useful for
    /// communicating content duration when it becomes known after the start
    /// of streaming.
    public final static String METADATA_DURATION = "duration";
    /// The encoded frame rate of the content, in frames per second.
    public final static String METADATA_ENCODED_FRAMERATE = "framerate";
    /// The streamer is polled every 200ms
    public static final int POLL_STREAMER_INTERVAL_MS = 200;
    /// Poll samples are kept for 1 second (5 samples)
    public static final int POLL_STREAMER_WINDOW_SIZE_MS = 1000;


    //private int _encodedFps = -1;
    //private int _contentLenSec = -1;

    private int _videoWidth = 0;
    private int _videoHeight = 0;

    private String _CDNServerIP = null;

    private IGraphicalInterface _graphicalInterface;
    private String _adID = null;
    private int _atiStatus = -999;
    private final Object mObj = new Object();

    private boolean _autoDurationUpdate = true;
    private boolean _autoFrameRateUpdate = true;
    private String _connectionType = null;
    private String _linkEncryption = null;

    // SSID code is commented as this is a PII item.
    //private String _ssID = null;

    // Number of times the framerate has been checked while in playing state.
    private int _playingFpsObservationCount = 0;
    // Total of all playing FPS observations
    private long _playingFpsTotal = 0;
    private String _oldAssetName = null;
    private String  oldResource = null;

    /// @brief Constructor for Monitor.
    ///
    /// Typically this object is owned by the Session which creates, starts and
    /// cleans up the Monitor object
    public Monitor(int sessionId,
    				EventQueue eventQueue,
    				ContentMetadata contentMetadata,
    				SystemFactory systemFactory)
    {
        _sessionId = sessionId;
        _eventQueue = eventQueue;
        _contentMetadata = contentMetadata;
        _systemFactory = systemFactory;

        _logger = _systemFactory.buildLogger();
        _logger.setModuleName("Monitor");
        _logger.setSessionId(_sessionId);
        _exceptionCatcher = _systemFactory.buildExceptionCatcher();
        _time = _systemFactory.buildTime();
        _systemFactory.buildTimer();
        _graphicalInterface = _systemFactory.buildGraphicalInterface();

        if(_contentMetadata.duration > 0)  {
             //Duration provided by customer so disable the automatic detection of duration
             // (through onMetadata())
             _autoDurationUpdate = false;
        }

        if(_contentMetadata.encodedFrameRate > 0){
            //Encoded framerate set by customer during the start of session. Hence
            // this flag should be set to false so that proxy does not override the value
            // through onMetadata()
            _autoFrameRateUpdate = false;
        }
    }

    /// @brief Start polling the streamer
    ///
    /// \param nowMs This time is set as the start time and events are offset
    /// from this. Should be in epoch format.
    public void start(double nowMs)  {
    	_logger.info("monitor starts");

        _startTimeMs = nowMs;
    }

    public void setDefaultBitrateAndResource() {
    	if (_contentMetadata != null) {
            if (_contentMetadata.defaultBitrateKbps > 0 && _bitrateKbps < 0) {
            	setBitrateKbps(_contentMetadata.defaultBitrateKbps);
            }
            if (_contentMetadata.defaultResource != null) {
            	setResource(_contentMetadata.defaultResource);
            } 
    	}
    }
    
    public void setBitrateKbps(int newBitrateKbps) {
        _logger.debug("setBitrateKbps()");

        if (_ignoreBitrateAndResource) {
            _logger.info("setBitrateKbps(): ignored");
            return;
        }

        int oldBitrateKbps = _bitrateKbps;
        if (oldBitrateKbps != newBitrateKbps && newBitrateKbps >= -1) {
            _logger.info("Change bitrate from " + oldBitrateKbps + " to " + newBitrateKbps);
            enqueueBitrateChangeEvent(oldBitrateKbps, newBitrateKbps);
            _bitrateKbps = newBitrateKbps;
        }
    }

    @Override
    public void setVideoWidth(int videoWidth) {
        _logger.debug("setVideoWidth()");

        int oldVideoWidth = _videoWidth;
        if (oldVideoWidth != videoWidth && videoWidth >= -1) {
            _logger.info("Change videoWidth from " + oldVideoWidth + " to " + videoWidth);
            enqueueVideoWidthChangeEvent(oldVideoWidth, videoWidth);
            _videoWidth = videoWidth;
        }
    }


    @Override
    public void setVideoHeight(int videoHeight) {
        _logger.debug("setVideoHeight()");

        int oldVideoHeight = _videoHeight;
        if (oldVideoHeight != videoHeight && videoHeight >= -1) {
            _logger.info("Change videoHeight from " + oldVideoHeight + " to " + videoHeight);
            enqueueVideoHeightChangeEvent(oldVideoHeight, videoHeight);
            _videoHeight = videoHeight;
        }
    }

    @Override
    public void setCDNServerIP(String newCDNServerIp) {
        _logger.debug("setCDNServerIP()");

        String oldCDNServerIp = _CDNServerIP;
        if (oldCDNServerIp == null) {
            oldCDNServerIp = "";
        }
        if (newCDNServerIp != null && (!oldCDNServerIp.equals(newCDNServerIp))) {
            _logger.info("Change CDN Server IP from " + oldCDNServerIp + " to " + newCDNServerIp);
            enqueueCDNServerIPChangeEvent(oldCDNServerIp, newCDNServerIp);
            _CDNServerIP = newCDNServerIp;
        }
    }

    private void setResource(String newResource) {
        _logger.debug("setResource()");

        if (_ignoreBitrateAndResource) {
            _logger.info("setResource(): ignored");
            return;
        }


        if (newResource != null && !newResource.equals(oldResource) ) {
            _logger.info("Change resource from " + oldResource + " to " + newResource);
            enqueueResourceChangeEvent(oldResource, newResource);
            _contentMetadata.defaultResource = newResource;
            oldResource = _contentMetadata.defaultResource;
        }
    };
    
    
    /// Attach a player to the monitor and resume monitoring if suspended
    public void attachPlayer(PlayerStateManager playerStateManager)  {
    	
    	_logger.info("attachPlayer()");
        
    	if (_playerStateManager != null){
    		_logger.error("Monitor.attachPlayer(): detach current PlayerStateManager first");
            return;
    	}
        
    	if (playerStateManager.setMonitoringNotifier(this, _sessionId)) {
        	_playerStateManager = playerStateManager;
	    } else {
	        _logger.error("attachPlayer(): instance of PlayerStateManager is already attached to a session");
	    }

    }
    

    /// Pause monitoring such that it can be restarted later and detach from current player
    public void detachPlayer() throws ConvivaException {
    	_logger.info("detachPlayer()");
        synchronized (mObj) {
            if(_playerStateManager != null) {
                _exceptionCatcher.runProtected(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        _playerStateManager.removeMonitoringNotifier();
                        setPlayerState(InternalPlayerState.NOT_MONITORED);
                        _playerStateManager = null;
                        return null;
                    }
                }, "detachPlayer");
            }
        }
    }
    
    /// Attach a player to the monitor for preloading purposes
    public void contentPreload() {
    	_logger.info("contentPreload()");
    	
        if (_preloading) { // Shouldn't happen twice in a row.
            _logger.debug("contentPreload(): called twice, ignoring"); // warning?
            return;
        }
        _preloading = true;
        _ignorePlayerState = true;
    }
    
    /// Attach a player to the monitor for preloading purposes
    public void contentStart() {
        _logger.debug("contentStart()");
        if (!_preloading) { // Shouldn't happen without contentPreload being called.
            _logger.warning("contentStart(): called without contentPreload, ignoring");
            return;
        }
        _preloading = false;
        if (!_adPlaying) {
            _ignorePlayerState = false;
            setPlayerState(_pooledPlayerState);
        }    	
    }

    
    public void adStart(AdStream adStream,
			AdPlayer adPlayer,
			AdPosition adPosition) {
    	_logger.debug("adStart(): adStream= " + adStream + " adPlayer= " + adPlayer + " adPosition= " + adPosition);
    	if (_adPlaying) {
    		_logger.warning("adStart(): Multiple adStart calls, ignoring");
    		return;
    	}
    	_adPlaying = true;
    	_adStream = adStream;
    	_adPlayer = adPlayer;
    	if (!_hasJoined) {
            togglePauseJoin(true);
        }
    	
        if (_adStream == Client.AdStream.CONTENT ||
                _adPlayer == Client.AdPlayer.SEPARATE) {
                // If in-stream ads, get errors and bitrate, ignore player state
                // If out-of-stream ads in different player, same thing
                if (!_playerState.equals(InternalPlayerState.NOT_MONITORED)) {
                    _pooledPlayerState = _playerState;
                }
                setPlayerState(InternalPlayerState.NOT_MONITORED);
                _ignorePlayerState = true;
            } else /* istanbul ignore else */ if (_adStream == Client.AdStream.SEPARATE &&
                _adPlayer == Client.AdPlayer.CONTENT) {
                // If out-of-stream ads in same player, ignore everything until adEnd
                if (!_playerState.equals(InternalPlayerState.NOT_MONITORED)) {
                    _pooledPlayerState = _playerState;
                }
                setPlayerState(InternalPlayerState.NOT_MONITORED);
                _ignorePlayerState = true;
                _ignoreBitrateAndResource = true;
                _ignoreEncodedFrameRateAndDuration = true;
                _ignoreError = true;
            } else {} // should never happen
    }

    public void adEnd() {
    	_logger.info("adEnd()");
    	if (!_adPlaying) {
    		_logger.info("adEnd(): called before adStart, ignoring");
    		return;
    	}
    	
        if (!_hasJoined) {
            togglePauseJoin(false);
        }

        if (_adStream == Client.AdStream.CONTENT ||
            _adPlayer == Client.AdPlayer.SEPARATE) {
            // If in-stream ads, get errors and bitrate, ignore player state
            // If out-of-stream ads in different player, same thing
            if (!_preloading) {
                _ignorePlayerState = false;
                setPlayerState(_pooledPlayerState);
            }
        } else /* istanbul ignore else */ if (_adStream == Client.AdStream.SEPARATE &&
            _adPlayer == Client.AdPlayer.CONTENT) {
            // If out-of-stream ads in same player, ignore everything until adEnd
            _ignoreBitrateAndResource = false;
            _ignoreEncodedFrameRateAndDuration = false;
            _ignoreError = false;
            if (!_preloading) {
                // console.log("2-1");
                _ignorePlayerState = false;
                setPlayerState(_pooledPlayerState);
            }
        } else {} // should never happen

        _adPlaying = false;
        _adStream = null;
        _adPlayer = null;
    }

    private void togglePauseJoin(boolean paused) {
    	_logger.info("TogglePauseJoin()");
        if (_pauseJoin == paused) {
        	_logger.info("TogglePauseJoin(): same value ignoring");
        	return;
        }
        enqueueStateChange("pj", Boolean.valueOf(_pauseJoin), Boolean.valueOf(paused));
        _pauseJoin = paused;        
	}

    
    /// @brief Set state of streamer
    ///
    /// Must be one of the int constants in class PlayerStates
    @Override
    public void setPlayerState(final InternalPlayerState newState) {
        if (_playerState.equals(newState)) {
            return;
        }
        
        if (_playerState.equals(InternalPlayerState.NOT_MONITORED) &&
        		!newState.equals(InternalPlayerState.NOT_MONITORED)) {
        	_pooledPlayerState = newState;
        }

        if (_ignorePlayerState) {
            _logger.debug("OnPlayerStateChange(): " + newState + " (pooled, " + (_adPlaying ? "ad playing" : "preloading") + ")" );
            return;
        }        
        _logger.debug("OnPlayerStateChange(): " + newState);
        
        // Do we have a join?
        if (!_hasJoined && newState.equals(InternalPlayerState.PLAYING)) {
        	_hasJoined = true;
        	togglePauseJoin(false);
        }
        enqueueStateChange("ps", Protocol.convertPlayerState(_playerState), Protocol.convertPlayerState(newState));
        _logger.info("SetPlayerState(): changing player state from " + _playerState + " to " + newState);
        _playerState = newState;
    }

    // Reports seek start via CwsSeekEvent.
    @Override
    public void onSeekStart(int seekToPos) {
        Map<String, Object> data = new HashMap<String, Object>();
        synchronized (mObj) {
            if (_playerStateManager != null) {
                data.put("bl", _playerStateManager.getBufferLength());
                data.put("pht", _playerStateManager.getPHT());
            }
        }
        data.put("act","pss");
        data.put("skto", seekToPos);
        enqueueEvent("CwsSeekEvent", data);
    }

    // Reports seek end via CwsSeekEvent.
    @Override
    public void onSeekEnd() {
        Map<String, Object> data = new HashMap<String, Object>();
        synchronized (mObj) {
            if (_playerStateManager != null) {
                data.put("pht", _playerStateManager.getPHT());
                data.put("bl", _playerStateManager.getBufferLength());
            }
        }
        data.put("act","pse");
        enqueueEvent("CwsSeekEvent", data);
    }

    // Reports Seek User Down via CwsSeekEvent
    @Override
    public void onSeekButtonDown() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("act","bd");
        synchronized (mObj) {
            if (_playerStateManager != null) {
                data.put("pht", _playerStateManager.getPHT());
                data.put("bl", _playerStateManager.getBufferLength());
            }
        }
        enqueueEvent("CwsSeekEvent", data);
    }

    // Reports Seek User Up via CwsSeekEvent
    @Override
    public void onSeekButtonUp() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("act","bu");
        synchronized (mObj) {
            if (_playerStateManager != null) {
                data.put("pht", _playerStateManager.getPHT());
                data.put("bl", _playerStateManager.getBufferLength());
            }
        }
        enqueueEvent("CwsSeekEvent", data);
    }

    /// Reports an error and changes via the CwsErrorEvent
    @Override
    public void onError(final StreamerError e) {
        if (e.getErrorCode() == null || e.getErrorCode().isEmpty()) {
            _logger.error("OnError(): invalid error message string: " + e.getErrorCode());
            return;
    	}
    	if (e.getSeverity() == null) {
            _logger.error("OnError(): invalid error message severity");
            return;
    	}
    	if (_ignoreError) {
    		_logger.info("monitor.onError(): ignored");
    		return;
    	}
    	_logger.info("Enqueue CwsErrorEvent");

        boolean isFatal; 
        if (e.getSeverity() == Client.ErrorSeverity.FATAL)
        	isFatal = true;
        else
        	isFatal = false;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ft", isFatal);
        data.put("err", e.getErrorCode().toString());
        synchronized (mObj) {
            if (_playerStateManager != null) {
                data.put("bl", _playerStateManager.getBufferLength());
                data.put("pht", _playerStateManager.getPHT());
            }
        }
        enqueueEvent("CwsErrorEvent", data);
    }

    /// @brief Parse the string and return an integer.
    /// \note defaultResult is returned on exception
    public int parseInt(String decimalInt, int defaultResult) {
        int result = defaultResult;
        try {
            result = Integer.parseInt(decimalInt);
        } catch (Exception e) {
        }
        return result;
    }
    
    /// Retrieve Metadata from Dictionary and send via subsequent heartbeats
    @Override
    public void onMetadata(final Map<String, String> metadata) {
        try {
            Map<String, String> metadataDict = metadata;

            if (metadataDict.containsKey(METADATA_ENCODED_FRAMERATE) && _autoFrameRateUpdate) {
                int encodedFpsNumber = parseInt(metadataDict.get(METADATA_ENCODED_FRAMERATE), -1);

                if (encodedFpsNumber > 0 && !_ignoreEncodedFrameRateAndDuration) {

                    if(encodedFpsNumber != _contentMetadata.encodedFrameRate )
                        enqueueFramerateChangeEvent(_contentMetadata.encodedFrameRate, encodedFpsNumber);

                    _contentMetadata.encodedFrameRate = encodedFpsNumber;
            	}
            }
            if (metadataDict.containsKey(METADATA_DURATION) && _autoDurationUpdate ) {

                int contentLengthSec = parseInt(metadataDict.get(METADATA_DURATION), -1);

                if (contentLengthSec > 0 && !_ignoreEncodedFrameRateAndDuration) {

                    if(contentLengthSec != _contentMetadata.duration && contentLengthSec > 0 )
                        enqueueDurationChangeEvent(_contentMetadata.duration, contentLengthSec);

                    _contentMetadata.duration = contentLengthSec;
                }
            }
        }
        catch (Exception e) {
        	_logger.error("monitor.OnMetadata() error: " + e.toString());
        }
    }

    // Commenting ADID related code.
    /*public void fetchAdid() {
        if (!AndroidSystemUtils.checkMainThread()) {
            int _newAtiStatus = AndroidSystemUtils.getATIStatus();
            if (_atiStatus != -999 && _atiStatus != _newAtiStatus) {
                enqueueAtiStatusChangeEvent(_atiStatus, _newAtiStatus);
            }
            _atiStatus = _newAtiStatus;
            if (_newAtiStatus == AndroidSystemUtils.ATI_STATUS.ADID_ENABLED.getValue()) {
                String _newAdID = AndroidSystemUtils.getAdID();
                if (_newAdID != null) {
                    if (_adID != null && !(_adID.equals(_newAdID))) {
                        enqueueADIdChangeEvent(_adID, _newAdID);
                    }
                    _adID = _newAdID;
                }
            } else {
                _adID = null;
            }
        }
    }*/

	public void getNetworkMetrics(){

		String newConnectionType = AndroidNetworkUtils.getConnectionType();
        if(newConnectionType!= null && !newConnectionType.equals(_connectionType)){
            enqueueConnectionTypeChangeEvent(_connectionType,newConnectionType);
            _connectionType = newConnectionType;
        }
 
        String newLinkEncryption = AndroidNetworkUtils.getLinkEncryption();
        if(newLinkEncryption!= null && !newLinkEncryption.equals(_linkEncryption)){
            enqueueLinkEncryptionChangeEvent(_linkEncryption,newLinkEncryption);
            _linkEncryption = newLinkEncryption;
        }

        // SSID code is commented as this is a PII item.
        /*String newSSID = AndroidNetworkUtils.getSSID();
        if(newSSID != null && !newSSID.equals(_ssID)) {
            enqueueSSIDChangeEvent(_ssID,newSSID);
        }
        _ssID = newSSID;*/

    }

    public void enqueueDataSamples(HashMap<String, Object> dataSamples) {
        if ((_graphicalInterface != null && (_graphicalInterface.inSleepingMode() || _graphicalInterface.isDataSaverEnabled() || !_graphicalInterface.isVisible()))) {
            return;
        }
        _logger.debug("enqueueDataSamplesEvent()");
        enqueueEvent("CwsDataSamplesEvent", dataSamples);
    }

    /// Update heartbeat with current metrics
    public void updateHeartbeat(Map<String, Object> heartbeat) {

        heartbeat.put("ps", Protocol.convertPlayerState(_playerState));
        heartbeat.put("pj", _pauseJoin);
        heartbeat.put("sf", _sessionFlags);

        // Commenting ADID related code.
        /*heartbeat.put("atistatus", _atiStatus);
        if (_atiStatus == AndroidSystemUtils.ATI_STATUS.ADID_ENABLED.getValue()) {
            if (_adID != null) {
                heartbeat.put("ati", _adID);
            }
        }*/

        synchronized (mObj) {
        if (_playerStateManager != null) {

            HashMap<String, Object> dataSamples = new HashMap<String, Object>();
            dataSamples.put("pht", _playerStateManager.getPHT());
            dataSamples.put("bl", _playerStateManager.getBufferLength());
            dataSamples.put("ss", AndroidNetworkUtils.getSignalStrength());

            enqueueDataSamples(dataSamples);

            heartbeat.put("pht", _playerStateManager.getPHT());
            heartbeat.put("bl", _playerStateManager.getBufferLength());

            heartbeat.put("ss", AndroidNetworkUtils.getSignalStrength());
            // Add implementation information for player interface module
            String moduleName = _playerStateManager.getModuleName();
            String moduleVersion = _playerStateManager.getModuleVersion();

            Map<String, String> componentConfig = new HashMap<String, String>();
            if (moduleName != null) {
                componentConfig.put("mn", moduleName);
            }
            if (moduleVersion != null) {
                componentConfig.put("mv", moduleVersion);
            }
            if (componentConfig.size() > 0) {
                heartbeat.put("cc", componentConfig);
            }


            Map<String, String> pm = (Map<String, String>) heartbeat.get("pm");
            boolean pm_updated = false;
            if (pm != null) {
                if (pm.get("fw") == null) {
                    class MyCallable implements Callable<Void> {
                        String playerType = null;

                        @Override
                        public Void call() throws Exception {
                            playerType = _playerStateManager.getPlayerType();
                            return null;
                        }

                        public String getPlayerType() {
                            return playerType;
                        }
                    }
                    MyCallable myCallable = new MyCallable();
                    try {
                        _exceptionCatcher.runProtected(myCallable, "updateHeartbeat.getPlayerType");
                    } catch (Exception e) {
                        _logger.error("Exception in updateHeartbeat.getPlayerType: " + e.toString());
                        e.printStackTrace();
                    }
                    if (myCallable.getPlayerType() != null) {
                        pm.put("fw", myCallable.getPlayerType());
                        pm_updated = true;
                    }
                }
                if (pm.get("fwv") == null) {
                    class MyCallable implements Callable<Void> {
                        String playerVersion = null;

                        @Override
                        public Void call() throws Exception {
                            playerVersion = _playerStateManager.getPlayerVersion();
                            return null;
                        }

                        public String getPlayerVersion() {
                            return playerVersion;
                        }
                    }
                    MyCallable myCallable = new MyCallable();
                    try {
                        _exceptionCatcher.runProtected(myCallable, "updateHeartbeat.getPlayerVersion");
                    } catch (Exception e) {
                        _logger.error("Exception in updateHeartbeat.getPlayerVersion: " + e.toString());
                        e.printStackTrace();
                    }
                    if (myCallable.getPlayerVersion() != null) {
                        pm.put("fwv", myCallable.getPlayerVersion());
                        pm_updated = true;
                    }
                }
                if (pm_updated) {
                    heartbeat.put("pm", pm);
                }
            }
        } else {
            HashMap<String, Object> dataSamples = new HashMap<String, Object>();
            dataSamples.put("pht", -1);
            dataSamples.put("bl", -1);
            dataSamples.put("ss", AndroidNetworkUtils.getSignalStrength());

            enqueueDataSamples(dataSamples);
            }
        }

        int averageFps = getAverageFrameRate();
        if (averageFps >= 0) {
            heartbeat.put("afps", averageFps);
        }
        if(_playingFpsObservationCount > 0 && _playingFpsTotal > 0) {
            heartbeat.put("rfpscnt", _playingFpsObservationCount);
            heartbeat.put("rfpstot", _playingFpsTotal);
        }

        Map<String, String> sanitizedCustomMetadata = new HashMap<String, String>();
        if (_contentMetadata != null) {
            Iterator<String> iterator = _contentMetadata.custom.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String value = _contentMetadata.custom.get(key);
                if (key != null && value != null)
                    if (value.length() != 0)
                        sanitizedCustomMetadata.put(key, value);
            }
        }

        if (sanitizedCustomMetadata != null && sanitizedCustomMetadata.size() != 0)
            heartbeat.put("tags", sanitizedCustomMetadata);

        if (_contentMetadata != null &&_contentMetadata.viewerId != null) {
            heartbeat.put("vid", _contentMetadata.viewerId);
        }

        if (_contentMetadata != null) {
            heartbeat.put("an", _contentMetadata.assetName);
            if(_contentMetadata.assetName != null && !_contentMetadata.assetName.equals(_oldAssetName)) {
                enqueueAssetNameChangeEvent(_oldAssetName, _contentMetadata.assetName);
                _oldAssetName = _contentMetadata.assetName;
            }
        }

        if(_contentMetadata != null && !ContentMetadata.StreamType.UNKNOWN.equals(_contentMetadata.streamType)){
            heartbeat.put("lv", ContentMetadata.StreamType.LIVE.equals(_contentMetadata.streamType));
        }

        if (_contentMetadata != null && _contentMetadata.applicationName != null) {
            heartbeat.put("pn", _contentMetadata.applicationName);
        }

        if (_contentMetadata != null && _contentMetadata.streamUrl != null) {
            heartbeat.put("url", _contentMetadata.streamUrl);
        }
        if (_contentMetadata != null && _contentMetadata.defaultResource != null)
        	heartbeat.put("rs", _contentMetadata.defaultResource);

        if (_contentMetadata != null && _contentMetadata.duration > 0) {
            heartbeat.put("cl", (int) (_contentMetadata.duration));
        }
        if (_contentMetadata != null && _contentMetadata.encodedFrameRate > 0) {
            heartbeat.put("efps", _contentMetadata.encodedFrameRate);
        }

        if (_bitrateKbps > 0) {
            heartbeat.put("br", _bitrateKbps); // Not deprecated
        }

        if (_CDNServerIP != null) {
            heartbeat.put("csi", _CDNServerIP);
        }
        if (_videoWidth >= 0 && _videoHeight >= 0) {
            heartbeat.put("w", _videoWidth);
            heartbeat.put("h", _videoHeight);
        }
        if(_connectionType != null){
            heartbeat.put("ct", _connectionType);
        }
        if(_linkEncryption != null){
            heartbeat.put("le", _linkEncryption);
        }

        // SSID code is commented as this is a PII item.
        /*if(_ssID != null){
           heartbeat.put("ssid", _ssID);
        }*/

    }
    
    /// Stops monitoring and resets internal state
    public void cleanup() {
        _logger.info("cleanup()");
        synchronized (mObj) {
            if (_playerStateManager != null) {
                try {
                    detachPlayer();
                } catch (Exception e) {
                    _logger.error("Exception in cleanup: " + e.toString());
                    e.printStackTrace();
                }
            }
        }
        _eventQueue = null;
        _contentMetadata = null;
    }

    public int getSessionTime() {
    	return (int) (_time.current() - _startTimeMs);
    }
    
    private void enqueueEvent(String type, Map<String, Object> eventData) {
        if (_eventQueue != null) {
            _eventQueue.enqueueEvent(type, eventData,
                   (int)getSessionTime());
        }
    }

    private void enqueueStateChange(String key, Object oldState, Object newState) {

        HashMap<String, Object> eventData = new HashMap<String, Object>();

        // If old state is not available, ignore
        if (oldState != null) {
            HashMap<String, Object> oldStateMap = new HashMap<String, Object>();
            oldStateMap.put(key, oldState);
            eventData.put("old", oldStateMap);
        }
        HashMap<String, Object> newStateMap = new HashMap<String, Object>();
        newStateMap.put(key, newState);
        eventData.put("new", newStateMap);

        synchronized (mObj) {
            if (_playerStateManager != null) {
                eventData.put("bl", _playerStateManager.getBufferLength());
                eventData.put("pht", _playerStateManager.getPHT());
            }
        }
        // Enqueue event
        enqueueEvent("CwsStateChangeEvent", eventData);
    }

    private void enqueueMetadataChangeEvent(Map<String, Object> oldMetadata, Map<String, Object> newMetadata) {
        Map<String, Object> _oldMetadata = new HashMap<String, Object>();
        _oldMetadata.putAll(oldMetadata);
        Map<String, Object> _newMetadata = new HashMap<String, Object>();
        _newMetadata.putAll(newMetadata);

        HashMap<String, Object> eventData = new HashMap<String, Object>();

        if(!_oldMetadata.isEmpty())
            eventData.put("old", _oldMetadata);
        eventData.put("new", _newMetadata);
        synchronized (mObj) {
            if (_playerStateManager != null) {
                eventData.put("bl", _playerStateManager.getBufferLength());
                eventData.put("pht", _playerStateManager.getPHT());
            }
        }

        enqueueEvent("CwsStateChangeEvent", eventData);
    }

    private void enqueueBitrateChangeEvent(int oldBitrateKbps, int newBitrateKbps) {
        Integer oldValue = null;
        if (oldBitrateKbps > 0) {
            oldValue = oldBitrateKbps;
        }
        enqueueStateChange("br", oldValue, newBitrateKbps);
    }

    private void enqueueVideoWidthChangeEvent(int oldVideoWidth, int newVideoWidth) {

        enqueueStateChange("w", oldVideoWidth, newVideoWidth);
    }

    private void enqueueVideoHeightChangeEvent(int oldVideoHeight, int newVideoHeight) {

        enqueueStateChange("h", oldVideoHeight, newVideoHeight);
    }

    private void enqueueCDNServerIPChangeEvent(String oldIP, String newIP) {

        enqueueStateChange("csi", oldIP, newIP);
    }

    private void enqueueConnectionTypeChangeEvent(String oldValue, String newValue) {

        enqueueStateChange("ct", oldValue, newValue);
    }

	 private void enqueueLinkEncryptionChangeEvent(String oldValue, String newValue) {

        enqueueStateChange("le", oldValue, newValue);
    }

    // SSID code is commented as this is a PII item.
    /*private void enqueueSSIDChangeEvent(String oldValue, String newValue) {
        enqueueStateChange("ssid", oldValue, newValue);
    }*/

    private void enqueueResourceChangeEvent(String oldResource, String newResource) {
        enqueueStateChange("rs", oldResource, newResource);
    }

    private void enqueueADIdChangeEvent(String oldAdID, String newAdID) {
        enqueueStateChange("ati", oldAdID, newAdID);
    }

    private void enqueueAtiStatusChangeEvent(int oldAdID, int newAdID) {
        enqueueStateChange("atistatus", oldAdID, newAdID);
    }

    private void enqueueDurationChangeEvent(int oldDuration, int newDuration) {
        enqueueStateChange("cl", oldDuration, newDuration);
    }

    private void enqueueFramerateChangeEvent(int oldFps, int newFps) {
        Integer oldValue = null;
        if (oldFps > 0) {
            oldValue = oldFps;
        }
        enqueueStateChange("efps", oldValue, newFps);
    }

    private void enqueueAssetNameChangeEvent(String oldAssetName, String newAssetName) {
        enqueueStateChange("an", oldAssetName, newAssetName);
    }

    @Override
    public void release() throws ConvivaException {
        detachPlayer();
        _graphicalInterface = null;
    }

    @Override
    public void onRenderedFramerateUpdate(int renderedFps) {
        if (renderedFps > 0 && _playerState.equals(InternalPlayerState.PLAYING)) {
            synchronized (mObj) {
                _playingFpsTotal += renderedFps;
                _playingFpsObservationCount++;
            }
        }
    }

    /**
     * Reports the average of rendered frame rate for the related video player.
     * @return averageFps - Average of Framerate over the session lifetime.
     */
    private int getAverageFrameRate() {
        if (_playingFpsTotal > 0 && _playingFpsObservationCount > 0) {
            return (int) _playingFpsTotal / _playingFpsObservationCount;
        } else {
            synchronized (mObj) {
                if (_playerStateManager != null) {
                    //this condition will execute only once when player frame rate is available and PSM misses out due to race condition
                    _playingFpsTotal += _playerStateManager.getPlayerFramerate();
                    _playingFpsObservationCount++;
                    if (_playingFpsTotal > 0 && _playingFpsObservationCount > 0) {
                        return (int) _playingFpsTotal / _playingFpsObservationCount;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public void onContentMetadataUpdate(ContentMetadata contentMetadata) {
        updateContentMetadata(contentMetadata);
    }

    private void updateContentMetadata(ContentMetadata newContentMetadata) {
        if (newContentMetadata == null) {
            _logger.warning("mergeContentMetadata(): null ContentMetadata");
            return;
        }

        Map<String, Object> _oldMetadata = new HashMap<String, Object>();
        Map<String, Object> _newMetadata = new HashMap<String, Object>();

        if (_contentMetadata == null) {
            _contentMetadata = new ContentMetadata();
        }

        if(Lang.isValidString(newContentMetadata.assetName) && !newContentMetadata.assetName.equals(_contentMetadata.assetName)) {
            if(_contentMetadata.assetName != null) {
                _oldMetadata.put("an", _contentMetadata.assetName);
            }
            _newMetadata.put("an", newContentMetadata.assetName);
            _contentMetadata.assetName = newContentMetadata.assetName;
            _oldAssetName = newContentMetadata.assetName;

        }

        Map<String, String> _oldStrMetadata = new HashMap<String, String>();
        Map<String, String> _newStrMetadata = new HashMap<String, String>();

        if(Lang.isValidString(newContentMetadata.applicationName) && !newContentMetadata.applicationName.equals(_contentMetadata.applicationName)) {
            if(_contentMetadata.applicationName != null) {
                _oldStrMetadata.put("pn", _contentMetadata.applicationName);
            }
            _newStrMetadata.put("pn", newContentMetadata.applicationName);
            _contentMetadata.applicationName = newContentMetadata.applicationName;
        }

        if(Lang.isValidString(newContentMetadata.viewerId) && !newContentMetadata.viewerId.equals(_contentMetadata.viewerId)) {
            if(_contentMetadata.viewerId != null) {
                _oldStrMetadata.put("vid", _contentMetadata.viewerId);
            }
            _newStrMetadata.put("vid", newContentMetadata.viewerId);
            _contentMetadata.viewerId = newContentMetadata.viewerId;
        }

        if(!_newStrMetadata.isEmpty()) {
            if(!_oldStrMetadata.isEmpty())
                _oldMetadata.put("strmetadata", _oldStrMetadata);
            _newMetadata.put("strmetadata", _newStrMetadata);
        } else {
            _oldStrMetadata = null;
            _newStrMetadata = null;
        }

        if(Lang.isValidString(newContentMetadata.streamUrl) && !newContentMetadata.streamUrl.equals(_contentMetadata.streamUrl)) {
            if(_contentMetadata.streamUrl != null) {
                _oldMetadata.put("url", _contentMetadata.streamUrl);
            }
            _newMetadata.put("url", newContentMetadata.streamUrl);
            _contentMetadata.streamUrl = newContentMetadata.streamUrl;
        }

        if(Lang.isValidString(newContentMetadata.defaultResource) && !newContentMetadata.defaultResource.equals(_contentMetadata.defaultResource)) {
            if(_contentMetadata.defaultResource != null) {
                _oldMetadata.put("rs", _contentMetadata.defaultResource);
            }
            _newMetadata.put("rs", newContentMetadata.defaultResource);
            _contentMetadata.defaultResource = newContentMetadata.defaultResource;
        }

        if(newContentMetadata.duration > 0 && newContentMetadata.duration != _contentMetadata.duration) {
            if(_contentMetadata.duration > 0) {
                _oldMetadata.put("cl", _contentMetadata.duration);
            }
            _newMetadata.put("cl", newContentMetadata.duration);
            _contentMetadata.duration = newContentMetadata.duration;

           //Customer given Duration value so auto-detection should be disabled.
           _autoDurationUpdate = false;
        }

        if(newContentMetadata.encodedFrameRate > 0  && _contentMetadata.encodedFrameRate != newContentMetadata.encodedFrameRate) {
            if(_contentMetadata.encodedFrameRate > 0)
                _oldMetadata.put("efps", _contentMetadata.encodedFrameRate);
            _newMetadata.put("efps", newContentMetadata.encodedFrameRate);

            _contentMetadata.encodedFrameRate = newContentMetadata.encodedFrameRate;

            //Customer given framerate value so auto-detection should be disabled.
            _autoFrameRateUpdate = false;
        }

        if (newContentMetadata.streamType != null && !ContentMetadata.StreamType.UNKNOWN.equals(newContentMetadata.streamType) && !newContentMetadata.streamType.equals(_contentMetadata.streamType)) {
            if (_contentMetadata.streamType != null && !ContentMetadata.StreamType.UNKNOWN.equals(_contentMetadata.streamType)) {
                boolean oldStreamType = ContentMetadata.StreamType.LIVE.equals(_contentMetadata.streamType);
                _oldMetadata.put("lv", oldStreamType);
            }
            boolean newStreamType = ContentMetadata.StreamType.LIVE.equals(newContentMetadata.streamType);
            _newMetadata.put("lv", newStreamType);
            _contentMetadata.streamType = newContentMetadata.streamType;
        }


        if(_contentMetadata.custom == null) {
            _contentMetadata.custom = new HashMap<String, String>();
        }

        if (newContentMetadata.custom != null &&  !newContentMetadata.custom.isEmpty()) {

            Map<String, String> _newCustom = new HashMap<String, String>();
            Map<String, String> _oldCustom = new HashMap<String, String>();

            for (Map.Entry<String, String> entry : newContentMetadata.custom.entrySet()) {

                if(Lang.isValidString(entry.getKey()) && Lang.isValidString(entry.getValue())) {
                    if (_contentMetadata.custom.containsKey(entry.getKey())) {
                        if(!_contentMetadata.custom.get(entry.getKey()).equals(entry.getValue())) {
                            _newCustom.put(entry.getKey(), entry.getValue());
                            _oldCustom.put(entry.getKey(), _contentMetadata.custom.get(entry.getKey()));
                        }
                    } else {
                        _newCustom.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            if(!_newCustom.isEmpty()) {
                if (_oldCustom != null  && !_oldCustom.isEmpty()) {
                    _oldMetadata.put("tags",_oldCustom);
                }
                _newMetadata.put("tags", _newCustom);
                _contentMetadata.custom.putAll(_newCustom);
            }
        }

        if(! _newMetadata.isEmpty()) {
            enqueueMetadataChangeEvent(_oldMetadata, _newMetadata);
        }
    }
}

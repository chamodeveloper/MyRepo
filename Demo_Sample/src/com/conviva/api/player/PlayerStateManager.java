/**
 * 
 */
package com.conviva.api.player;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.conviva.api.Client;
import com.conviva.api.ContentMetadata;
import com.conviva.api.ConvivaException;
import com.conviva.api.SystemFactory;
import com.conviva.api.SystemSettings;
import com.conviva.internal.StreamerError;
import com.conviva.session.IMonitorNotifier;
import com.conviva.session.Monitor;
import com.conviva.session.Monitor.InternalPlayerState;
import com.conviva.utils.ExceptionCatcher;
import com.conviva.utils.Logger;
import com.conviva.utils.Sanitize;

/**
 * Helper class to store and relay pertinent video player data to Conviva.<br>
 * Lifecycle is tied to an instance of the video player.
 * See tutorial 2-collecting-video-playback-data.
 */
public class PlayerStateManager implements IModuleVersion {
	private Logger _logger;
	
	
	/**
	 * Possible player states during video playback.
	 */
	public static enum PlayerState {
	    /** Report this state when you can confirm that the player is currently inactive/idle. */
	    STOPPED,
	    /** Report this state when you can confirm that the player is actively rendering video content for the viewer.<br>
	        This should never be reported for unavailable/blocked content. */
	    PLAYING,
	    /** Report this state when you can confirm that the player is stalled due to lack of video data in the buffer.<br>
	        It may show a spinner, or it may simply present a freeze frame. */
	    BUFFERING,
	    /** Report this state when you can confirm that the player is paused, generally upon viewer request. */
	    PAUSED,
	    /** Report this state when no other recognized Conviva player states apply. */
	    UNKNOWN
	};

    
    private SystemFactory _systemFactory;
    private ExceptionCatcher _exceptionCatcher;
	private IMonitorNotifier _monitorNotifier = null;    
    // Private state values
	private int _bitrateKbps = -2;
    private int _videoWidth = -1;
    private int _videoHeight = -1;
    private String _CDNServerIP = null;
    private PlayerState _playerState = PlayerStateManager.PlayerState.UNKNOWN;

    // The last metadata received, or null if none
    private Map<String, String> _currentMetadata = new HashMap<String, String>();

    private int _renderedFrameRate = -1;
    private int _encodedFrameRate = -1;
    private int _duration = -1;
    private String _playerVersion = null;
    private String _playerType = null;

    // The last error encountered, or null if none
    private StreamerError _lastError = null;

    // If we are told about errors, before we have a notifier from the monitor, we store them
    private ArrayList<StreamerError> _pendingErrors = new ArrayList<StreamerError>();


    private String _moduleName = null;
    private String _moduleVersion = null;

    private IClientMeasureInterface _IClientMeasureInterface = null;

    public PlayerStateManager(SystemFactory systemFactory) {

        if(systemFactory == null) {
            Log.e("CONVIVA : ","SystemFactory is null");
            return;
        }
    	_systemFactory = systemFactory;
        _logger = _systemFactory.buildLogger();
        _logger.setModuleName("PlayerStateManager");
        _exceptionCatcher = _systemFactory.buildExceptionCatcher();
    }
    
    
    /**
     * Release this PlayerStateManager instance.<br>
     * Call when you no longer need to collect data from the related video player.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void release() throws ConvivaException {
    	_exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
            	if (_monitorNotifier != null) {
                	_monitorNotifier.release();
                	removeMonitoringNotifier();
            	}
            	return null;
            }
        },"PlayerStateManager.release");
    }
    
    /**
     * Returns the latest rendered frame rate collected by this PlayerStateManager instance.
     * @return Latest rendered frame rate collected. -1 if not available.
     */
    public int getRenderedFrameRate() {
        return _renderedFrameRate;
    }

    /**
     * Reports the current rendered frame rate for the related video player.
     * @param renderedFrameRate New rendered frame rate for the related video player. 
     */
    public void setRenderedFrameRate(final int renderedFrameRate)
    {
        _renderedFrameRate = Sanitize.Integer(renderedFrameRate, -1, Integer.MAX_VALUE, -1);
        if(_monitorNotifier != null) {
            _monitorNotifier.onRenderedFramerateUpdate(_renderedFrameRate);
        }
    }
    
    /**
     * Returns the latest encoded frame rate collected by the PlayerInterface instance.
     * @return Latest encoded frame rate collected. -1 if not available.
     */
    public int getEncodedFrameRate() {
        return _encodedFrameRate;
    }

    /** 
     * This API is depricated. Use updateContentMetadata(final ContentMetadata _contentMetadata) API in {@link PlayerStateManager} to update encodedFrameRate.
     * <br>Reports the encoded frame rate for the video stream played by the related video player.
     * If you know the encoded frame rate of the video stream in advance, consider setting ContentMetadata.encodedFrameRate instead.
     * @param encodedFrameRate The encoded frame rate of the video stream.
     * @throws ConvivaException When Conviva internal exception happens.
     */
	 //TODO: As of now these deprecated API's will be used in all our drop in library as well.
	 //In future when we plan to remove these API's completely, we need to rename these API's if required and keep the functionalities intact.
    @Deprecated
    public void setEncodedFrameRate(final int encodedFrameRate) throws ConvivaException {
    	_exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws ConvivaException {
                _encodedFrameRate = encodedFrameRate;
                if (_encodedFrameRate < -1)
                	_encodedFrameRate = -1;
                Map<String, String> metaData = new HashMap<String, String>();
                metaData.put(Monitor.METADATA_ENCODED_FRAMERATE, String.valueOf(encodedFrameRate));
                setMetadata(metaData);
                return null;
            }
        },"PlayerStateManager.setEncodedFrameRate");
    	
    }

    /**
     * Returns the latest duration collected by this PlayerStateManager instance.
     * @return Latest duration collected. -1 if not available.
     */
    public int getDuration() {
        return _duration;
    }

    /** 
     * This API is deprecated. Use updateContentMetadata(final ContentMetadata _contentMetadata) API in {@link PlayerStateManager} to update duration <br>
     * <br> Reports the duration of the video stream played by the related video player.
	 * If you know the duration of the video stream in advance, consider setting ContentMetadata.duration instead.
     * @param duration Stream duration.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    //TODO: As of now these deprecated API's will be used in all our drop in library as well.
    //In future when we plan to remove these API's completely, we need to rename these API's if required and keep the functionality intact.
    @Deprecated
    public void setDuration(final int duration) throws ConvivaException {
    	_exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                _duration = duration;
                if (_duration < -1)
                	_duration = -1;
                Map<String, String> metaData = new HashMap<String, String>();
                metaData.put(Monitor.METADATA_DURATION, String.valueOf(duration));
                setMetadata(metaData);
                return null;
            }
        },"PlayerStateManager.setDuration");
    }

    /** 
     * Returns the last player type collected by this PlayerStateManager instance.
     * @return Last player type collected. null if not available.
     */
    public String getPlayerType() {
        return _playerType;
    }

    /** 
     * Reports the type of player monitored via the PlayerInterface instance.
     * @param plType Type of the player.
     */
    public void setPlayerType(String plType) {
        _playerType = plType;
    }

    /** 
     * Returns the last player version collected by this PlayerStateManager instance.
     * @return Last player version collected. null if not available.
     */
    public String getPlayerVersion()  {
        return _playerVersion;
    }

    /** 
     *  Reports the version of the related video player
     * @param plVer Version of the player.
     */
    public void setPlayerVersion(String plVer) {
        _playerVersion = plVer;
    }

    /*
     * Internal: Do not use
	 * @param monitor Internal: Do not use.
	 * @param sessionId Internal: Do not use.
     * @return Internal: Do not use.
     */
    public boolean setMonitoringNotifier(IMonitorNotifier monitor, int sessionId) {
    	if (_monitorNotifier != null)
    		return false;
        _monitorNotifier = monitor;
        if (_logger != null) {
        	_logger.setSessionId(sessionId);
        }
        pushCurrentState();
        return true;
    }

    
    /*
     * Internal: Do not use
     */
    public void removeMonitoringNotifier() {
        _monitorNotifier = null;
        if (_logger != null) {
        	_logger.setSessionId(-1);
        }
    }

    private void pushCurrentState() {
        if (_monitorNotifier == null) return;

        try {
            setPlayerState(getPlayerState());
        } catch (ConvivaException e) {
            log("Error set current player state " + e.getMessage(), SystemSettings.LogLevel.ERROR);
        }
        try {
            setBitrateKbps(getBitrateKbps());
        }  catch (ConvivaException e) {
            log("Error set current bitrate " + e.getMessage(), SystemSettings.LogLevel.ERROR);
        }
        setMetadata(getMetadata());

        // ## that shouldn't be necessary in the SDK paradigm
        // Now pass on all the pending errors, in the order in which we got them. This includes _currentError
        for (int i = 0; i < _pendingErrors.size(); i++) {
        	StreamerError strErr = _pendingErrors.get(i);
        	setError(strErr);
        }
        _pendingErrors.clear();
    }

    /** 
     * Returns the current state for the video player monitored by the PlayerInterface instance.
     * @return Current state of the video player under monitoring.
     */
    public PlayerState getPlayerState()  {
        return _playerState;
    }

    /** 
     * Reports the new player state of the related video player.
     * @param newState New player state for the video player under monitoring.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void setPlayerState(final PlayerState newState) throws ConvivaException {
    	_exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws ConvivaException {
            	if (PlayerStateManager.isValidPlayerState(newState)) {
                    if (_monitorNotifier != null) {
                    	_monitorNotifier.setPlayerState(convertToInternalPlayerState(newState));
                    }
                    _playerState = newState;
            	} else {
            		log("PlayerStateManager.SetPlayerState(): invalid state: " + newState, SystemSettings.LogLevel.ERROR);
            	}
                return null;
            }
        },"PlayerStateManager.setPlayerState");
    }

    /** 
     * Returns the last bitrate collected from the related video player.
     * @return Current known bitrate of the video player under monitoring. -1 if not available.
     */
    public int getBitrateKbps() {
        return _bitrateKbps;
    }

    /**
     * Returns the last video width collected from the related video player.
     * @return Current known video width  of the video player under monitoring. -1 if not available.
     */
    public int getVideoWidth() {
        return _videoWidth;
    }

    /**
     * Returns the last video height collected from the related video player.
     * @return Current known video height of the video player under monitoring. -1 if not available.
     */
    public int getVideoHeight() {
        return _videoHeight;
    }

    /**
     * Returns the last CDNServerIP collected from the related video player.
     * @return Current known CDNServerIP of the video player under monitoring. null if not available.
     */
    public String getCDNServerIP() {
        return _CDNServerIP;
    }

    /** 
     * Reports the new bitrate of the video stream played by the related video player.
     * We recommend reporting manifest/nominal bitrates.
     * For protocols like Smooth Streaming, you may have to sum audio and video bitrates to get the total bitrate.
     * @param newBitrateKbps New player state for the video player under monitoring.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void setBitrateKbps(final int newBitrateKbps) throws ConvivaException {
    	_exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call(){
            	int sanitizedBitrateKbps = newBitrateKbps;
            	if (sanitizedBitrateKbps >= -1) {
                    if (_monitorNotifier != null) {
                    	_monitorNotifier.setBitrateKbps(sanitizedBitrateKbps);
                    }
            		_bitrateKbps = sanitizedBitrateKbps;
            	}
                return null;
            }
        },"PlayerStateManager.setBitrateKbps");
    }

    /**
     * Reports the Width of the video stream played by the related video player.
     * @param newVideoWidth New width of the video under monitoring.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void setVideoWidth(final int newVideoWidth) throws ConvivaException {
        _exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                _videoWidth = newVideoWidth;
                if (_monitorNotifier != null) {
                    _monitorNotifier.setVideoWidth(newVideoWidth);
                }
                return null;
            }
        }, "PlayerStateManager.setVideoWidth");
    }

    /**
     * Reports the Height of the video stream played by the related video player.
     * @param newVideoHeight New height of the video under monitoring.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void setVideoHeight(final int newVideoHeight) throws ConvivaException {
        _exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                _videoHeight = newVideoHeight;
                if (_monitorNotifier != null) {
                    _monitorNotifier.setVideoHeight(newVideoHeight);
                }
                return null;
            }
        }, "PlayerStateManager.setVideoWidth");
    }

    /**
     * Reports the CDN of the video stream played by the related video player.
     * @param newCDNServerIP New CDN server IP of the video under monitoring.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void setCDNServerIP(final String newCDNServerIP) throws ConvivaException {
        _exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (newCDNServerIP == null) {
                    return null;
                }
                _CDNServerIP = newCDNServerIP;
                if (_monitorNotifier != null) {
                    _monitorNotifier.setCDNServerIP(_CDNServerIP);
                }
                return null;
            }
        }, "PlayerStateManager.setVideoWidth");
    }

    // private
    private void setError(StreamerError error)  {
    	_lastError = error;
        if (_monitorNotifier != null) {
        	_monitorNotifier.onError(_lastError);
        } else {
            _pendingErrors.add(_lastError);
        }
    }

    /** 
     * Reports an error while playing a video stream in the related video player.<br>
     * These include networking errors, download errors, parsing errors, decoding/decrypting errors, DRM errors.<br>
     * For best use, the error message should not include variables like user IDs and memory addresses.
     * @param errMessage The error message or error code for this error.
     * @param severity The severity of this error. See enum in @Client.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void sendError(final String errMessage, final Client.ErrorSeverity severity) throws ConvivaException  {
    	_exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
            	StreamerError error = new StreamerError(errMessage, severity);    	
                setError(error);
                return null;
            }
        },"PlayerStateManager.sendError");
    }
    
    /** 
     * Discard the video quality data contained in this PlayerStateManager instance.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void reset() throws ConvivaException {
    	_exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call()  {
                _bitrateKbps = -1;
                _playerState = PlayerStateManager.PlayerState.UNKNOWN;
                _currentMetadata = new HashMap<String, String>();
                _renderedFrameRate = -1;
                _encodedFrameRate = -1;
                _duration = -1;
                _playerVersion = null;
                _playerType = null;
                _lastError = null;
                _pendingErrors = new ArrayList<StreamerError>();
                return null;
            }
        },"PlayerStateManager.reset");
    }

    /**
     * Reports seek start while playing a video stream in the related video player.
     * @param seekToPos new position that seek is trying to get to. This is the targeted play head time in milliseconds
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void setPlayerSeekStart(final int seekToPos) throws ConvivaException  {
        _exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws ConvivaException {
                if(_monitorNotifier != null) {
                    _monitorNotifier.onSeekStart(seekToPos);
                }
                return null;
            }
        },"PlayerStateManager.sendSeekStart");
    }

    /**
     * Reports seek end while playing a video stream in the related video player.<br>
     *
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void setPlayerSeekEnd() throws ConvivaException  {
        _exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws ConvivaException {
                if(_monitorNotifier != null) {
                    _monitorNotifier.onSeekEnd();
                }
                return null;
            }
        },"PlayerStateManager.sendSeekEnd");
    }

    /**
     * Reports seek button up while playing a video stream in the related video player.
     *
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void setUserSeekButtonUp() throws ConvivaException  {
        _exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws ConvivaException {
                if(_monitorNotifier != null) {
                    _monitorNotifier.onSeekButtonUp();
                }
                return null;
            }
        },"PlayerStateManager.setSeekButtonUp");
    }

    /**
     * Reports seek button down while playing a video stream in the related video player.
     *
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void setUserSeekButtonDown() throws ConvivaException  {
        _exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws ConvivaException {
                if(_monitorNotifier != null) {
                    _monitorNotifier.onSeekButtonDown();
                }
                return null;
            }
        },"PlayerStateManager.setSeekButtonDown");
    }

    private Map<String, String> getMetadata() {
        return _currentMetadata;
    }

    private void setMetadata(Map<String, String> metadata) {
    	for (Map.Entry<String, String> entry : metadata.entrySet()) {
    		_currentMetadata.put(entry.getKey(), entry.getValue());
    	}    	
        if (_monitorNotifier == null) return;
    	_monitorNotifier.onMetadata(_currentMetadata);
    }

    private void log(String message, SystemSettings.LogLevel logLevel) {
    	if (_logger != null) {
    		_logger.log(message, logLevel);
    	}
    }

    private static boolean isValidPlayerState(PlayerState newState) {
        if (newState == PlayerState.STOPPED||
            newState == PlayerState.PLAYING ||
            newState == PlayerState.BUFFERING ||
            newState == PlayerState.PAUSED ||
            newState == PlayerState.UNKNOWN)
            return true;
        return false;
    };

    private static InternalPlayerState convertToInternalPlayerState(PlayerState state) {
        switch (state) {
            case STOPPED:
                return InternalPlayerState.STOPPED;
            case PLAYING:
                return InternalPlayerState.PLAYING;
            case BUFFERING:
                return InternalPlayerState.BUFFERING;
            case PAUSED:
                return InternalPlayerState.PAUSED;
            case UNKNOWN:
            default:
                return InternalPlayerState.UNKNOWN;
        }
    }

    /**
     * Return the name of a client player interface.
     *
     * @return Module version.  A short string.  <code>null </code> if not available.
     */
    public String getModuleName() {
        return _moduleName;
    }

    /**
     * Return the version of a  client player interface.
     *
     * @return Module version.  A short string.  <code>null </code> if not available.
     */
    public String getModuleVersion() {
        return _moduleVersion;
    }


    /**
     * Set the name / version of a client module implementation.
     * @param name Module name. A  String.
	 * @param version Module version in String.
     */
    public void setModuleNameAndVersion(String name, String version) {
        _moduleName = name;
        _moduleVersion = version;
    }

    /**
	 * Returns of the play head time.
     * @return The pht of player. -1 if unknown.
     *
     */
    public long getPHT() {
        if (_IClientMeasureInterface != null) {
            return _IClientMeasureInterface.getPHT();
        } else {
            return -1;
        }
    }

    /**
     * Returns the buffer length.
     * @return The Buffer length of player. -1 if unknown.
     */
    public int getBufferLength() {
        if (_IClientMeasureInterface != null) {
            return _IClientMeasureInterface.getBufferLength();
        } else {
            return -1;
        }
    }

    /**
     * Returns of the signal strength of the device.
     * @return The signal strength of the device. -1.0 if unknown.
     */
    public double getSignalStrength() {
        if (_IClientMeasureInterface != null) {
            return _IClientMeasureInterface.getSignalStrength();
        } else {
            return -1.0;
        }
    }

    public void setClientMeasureInterface(IClientMeasureInterface IClientMeasureInterface) {
        _IClientMeasureInterface = IClientMeasureInterface;
    }

    public void removeClientMeasureInterface() {
        _IClientMeasureInterface = null;
    }

    public void cleanup() {
        removeClientMeasureInterface();
    }

    /**
     * Update content metadata for an existing monitoring session. <br><br>
     *
     * For e.g.
     *
     * ContentMetadata _contentMetadata = new ContentMetadata(); <br><br>
     * _contentMetadata.assetName = "assetName";                            // assetName should not be null <br>
     * _contentMetadata.custom = new HashMap&lt;String, String&gt;(custom);       // custom should contain the full list of tags including tags
     *                                                                      which have already been set.<br>
     * _contentMetadata.defaultResource = "defaultReportingResource";       // defaultReportingResource should not be null <br>
     * _contentMetadata.viewerId = "viewerId";                              // viewerId should not be null <br>
     * _contentMetadata.applicationName = "playerName";                     // applicationName should not be null <br>
     * _contentMetadata.streamUrl = "streamUrl";                            // streamUrl should not be null <br>
     * _contentMetadata.streamType = ContentMetadata.StreamType.LIVE        // streamType should be LIVE/VOD <br>
     * _contentMetadata.duration = 300                                      // duration of the video content in seconds. duration should be greater than 0.  <br>
     * _contentMetadata.encodedFrameRate = 30;                              // encodedFrameRate is measured in frames per second. encodedFrameRate should be greater than 0.  <br>
     * <br>
     * psm.updateContentMetadata(convivaMetada); // psm is an instance of PlayerStateManager <br>
     * @param _contentMetadata New content metadata for the monitoring session.
	 * @throws ConvivaException When Conviva internal exception happens.
     */
    public void updateContentMetadata(final ContentMetadata _contentMetadata) throws ConvivaException {
        _exceptionCatcher.runProtected(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (_monitorNotifier != null) {
                    _monitorNotifier.onContentMetadataUpdate(_contentMetadata);
                    // Encoded framerate & duration are cached locally in PlayerStateManager and updated via setDuration & setEncodedFramerate API
                    // These values are cached in _currentMetadata. So whenever there is any update in contentMetadata, need to update _currentMetadata as well.
                    if(_contentMetadata != null && _contentMetadata.duration > 0) {
                        _currentMetadata.put(Monitor.METADATA_DURATION, String.valueOf(_contentMetadata.duration));
                    }
                    if(_contentMetadata != null && _contentMetadata.encodedFrameRate > 0) {
                        _currentMetadata.put(Monitor.METADATA_ENCODED_FRAMERATE, String.valueOf(_contentMetadata.encodedFrameRate));
                    }
                }

                return null;
            }
        }, "PlayerStateManager.onContentMetadataUpdate");
    }

    /**
     * Returns of the frame rate of player.
     *
     * @return The frame rate of player. -1.0 if unknown.
     */
    public int getPlayerFramerate() {
        int value = -1;
        if (_IClientMeasureInterface != null) {
            try {
                Method method = IClientMeasureInterface.class.getDeclaredMethod("getFrameRate", null);
                value = ((Integer) method.invoke(_IClientMeasureInterface, null)).intValue();
                return value;
            } catch (NoSuchMethodException e) {
                log("Exception " + e.toString(), SystemSettings.LogLevel.DEBUG);
            } catch (IllegalAccessException e) {
                log("Exception " + e.toString(), SystemSettings.LogLevel.DEBUG);
            } catch (InvocationTargetException e) {
                log("Exception " + e.toString(), SystemSettings.LogLevel.DEBUG);
            } finally {
                value = -1;
            }
        }
        return value;
    }

    public void sendLogMessage(String message, SystemSettings.LogLevel logLevel, IPlayerInterface senderObj) {
        if (senderObj != null) {
            log(message, logLevel);
        }
    }
}

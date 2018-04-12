// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.conviva.internal.StreamerError;
import com.conviva.api.ConvivaException;
import com.conviva.api.Client;
import com.conviva.api.ClientSettings;
import com.conviva.api.ContentMetadata;
import com.conviva.api.Client.AdPlayer;
import com.conviva.api.Client.AdPosition;
import com.conviva.api.Client.AdStream;
import com.conviva.api.SystemFactory;
import com.conviva.api.SystemSettings;
import com.conviva.api.player.PlayerStateManager;
import com.conviva.api.system.ICallbackInterface;
import com.conviva.api.system.ICancelTimer;
import com.conviva.api.system.IGraphicalInterface;
import com.conviva.json.IJsonInterface;
import com.conviva.protocol.Protocol;
import com.conviva.utils.CallableWithParameters;
import com.conviva.utils.HttpClient;
import com.conviva.utils.Logger;
import com.conviva.utils.Config;
import com.conviva.utils.SystemMetadata;
import com.conviva.utils.Time;
import com.conviva.utils.Timer;
import com.conviva.session.SessionFactory.SessionType;

/**
 *  Manages a session.
 */
public class Session {
    private ContentMetadata _contentMetadata = null;
    // A copy of tags from _sessionMetadata, in native dictionary form for
    // faster encoding.
    private int _sessionId = 0;
    private EventQueue _eventQueue;
    private Monitor _monitor; 
    private Client _client;
    private ClientSettings _clientSettings;
    private Config _clientConfig;
    private SystemFactory _systemFactory;
    private Protocol _protocol;
    
    private Time _time;
    private Timer _timer;
    private IJsonInterface _jsonInterface;
    private Logger _logger;
    private HttpClient _httpClient;
    private SystemMetadata _systemMetadata;
    private IGraphicalInterface _graphicalInterface;
    private double _startTimeMs = 0;
    private int _heartbeatSequenceNumber = 0;
    private ICancelTimer _hbTimer = null;
    private boolean _cleanedUp = false;
    private String _clv = Client.version;
    private SessionType _sessionType = SessionType.GLOBAL;

    // test CWS 2.1+/2.2 usage
    private boolean _enableCWS22 = true;
    
    public Session(int sessionId, EventQueue eventQueue, ContentMetadata contentMetadata, Monitor monitor, 
                Client client, ClientSettings clientSettings, Config clientConfig, SystemFactory systemFactory, SessionType sessionType)
             /* throws Exception */ {

    	_sessionId = sessionId;
    	_eventQueue = eventQueue;
    	_contentMetadata = contentMetadata;
    	_monitor = monitor;
    	_client = client;
    	_clientSettings = clientSettings;
    	_clientConfig = clientConfig;
    	_systemFactory = systemFactory;
    	
        _time = _systemFactory.buildTime();
        _timer = _systemFactory.buildTimer();
        _jsonInterface = _systemFactory.buildJsonInterface();
        _systemFactory.buildExceptionCatcher();
        _logger = _systemFactory.buildLogger();
        _logger.setModuleName("Session");
        _logger.setSessionId(_sessionId);
        _httpClient = _systemFactory.buildHttpClient();
        _systemMetadata = _systemFactory.buildSystemMetadata();
        _protocol = _systemFactory.buildProtocol();
        _graphicalInterface = _systemFactory.buildGraphicalInterface();
        _systemFactory.buildCallbackWithTimeout();
        _systemFactory.getSettings();
        _sessionType = sessionType;

        
        if(_contentMetadata!=null && _contentMetadata.custom == null) {
            _contentMetadata.custom = new HashMap<String, String>();
        }
        
    }

    public void start() {
        if (isVideoSession()) {
            if (_contentMetadata != null && _contentMetadata.assetName != null)
                _logger.info("Session.start(): assetName=" + _contentMetadata.assetName);
        }
        // only start generating Session events after this line
        _startTimeMs = _time.current();
        if (!isGlobalSession()) {
            // only start generating Monitor events after this line
            _monitor.start(_startTimeMs);
            _monitor.setDefaultBitrateAndResource();
        }
        _heartbeatSequenceNumber = 0;

        if (_clientConfig.isReady()) {
            // Local config is already available, we can send heartbeat immediately
            sendHeartbeat();
            createHBTimer();
        } else {
            // Local config hasn't been loaded yet, wait until it is
        	class ConfigLoaded implements CallableWithParameters.With0 {

				@Override
				public void exec() {
	                sendHeartbeat();
	                createHBTimer();
				}
        	}
        	ConfigLoaded configLoaded = new ConfigLoaded();
            _clientConfig.register(configLoaded);
        }
    }

    public void cleanup() {
        _logger.info("Session.cleanup()" + sessionTypeTag());
        if (_hbTimer != null) {
        	_hbTimer.cancel();
        	_hbTimer = null;
        }
        _logger.debug("Schedule the last hb before session cleanup" + sessionTypeTag());

        if (!isGlobalSession()) {
            enqueueSessionEndEvent();
        }
        sendHeartbeat();
        
        cleanupAll();
    }

    public void cleanupAll() {
        _cleanedUp = true;
        if (!isGlobalSession()) {
            _monitor.cleanup();
            _monitor = null;
        }
        if (_eventQueue != null) {
            //_eventQueue.flush();
            _eventQueue = null;
        } else {
            // should never happen
        }
        _contentMetadata = null;
        _clientSettings = null;
        _systemFactory = null;
        _time = null;
        _timer = null;
        _jsonInterface = null;
        _logger = null;
        //_cleanedUp = true;
    	
    }

    public int getSessionTime() {
    	return (int) (_time.current() - _startTimeMs);
    }
    
    /// @brief Enqueue session end event to be sent via next heartbeat
    public void enqueueSessionEndEvent() {
        _logger.info("cws.sendSessionEndEvent()");
        Map<String, Object> eventData = new HashMap<String, Object>();
        _eventQueue.enqueueEvent("CwsSessionEndEvent", eventData,
                getSessionTime());
    }
    
    /// @brief Notify that ad has started
    public void adStart(AdStream adStream,
			AdPlayer adPlayer,
			AdPosition adPosition) {
        _monitor.adStart(adStream, adPlayer, adPosition);
    }

    /// @brief Notify that ad has ended
    public void adEnd() {    	// Client will only call this on video sessions, we always have a Monitor.
        _monitor.adEnd();
    }

    /// Pause monitoring and detach player
    public void detachPlayer() throws ConvivaException {
    	_monitor.detachPlayer();
    }
    
    /// Resume monitoring if paused and attach new streamer
    public void attachPlayer(PlayerStateManager playerStateManager) throws ConvivaException {
    	_monitor.attachPlayer(playerStateManager);
    }

    /// Content Preloading
    public void contentPreload() throws ConvivaException {
    	_monitor.contentPreload();
    }
    
    /// Content Start
    public void contentStart() throws ConvivaException {    	// Client will only call this on video sessions, we always have a Monitor.
    	_monitor.contentStart();
    }

    /// @brief Report error specific to this session
    public void reportError(String errorMsg, Client.ErrorSeverity errorSeverity) {
        _logger.info("reportError(): " + errorMsg);

        _monitor.onError(new StreamerError(errorMsg, errorSeverity));
    }

    /// @brief Enqueue custom event to be sent via next heartbeat
    public void sendCustomEvent(String name, Map<String, Object> eventData) {
        _logger.info("Session.sendEvent(): eventName=" + name + sessionTypeTag());
        
        HashMap<String, Object> eventMap = new HashMap<String, Object>();
        eventMap.put("name", name);
        eventMap.put("attr", eventData);
        _eventQueue.enqueueEvent("CwsCustomEvent", eventMap,
                getSessionTime());
    }

    private void sendHeartbeat() {
    	if (_cleanedUp)
    		return;
    	
        boolean urgent = false;

        // If we have events, send the HB regardless of visibility
        if (_eventQueue.size() > 0) {
            urgent = true;
        } else if (_monitor == null) {
            // This is a global session with no events, skip sending HB
            return;
        }

        // Unless heartbeat is urgent, do not send when sleeping
        if (!urgent && (_graphicalInterface.inSleepingMode() || !_graphicalInterface.isVisible()) || _graphicalInterface.isDataSaverEnabled()) {
            _logger.info("Do not send out heartbeat: player is sleeping or not visible");
            return;
        }
        // Commenting ADID related code.
        /*if (_monitor != null) {
            _monitor.fetchAdid();
        }*/
        if(_monitor != null){
            _monitor.getNetworkMetrics();
		}

        Map<String, Object> heartbeat = makeHeartbeat();
        if (heartbeat != null) {
            encodeAndPostHeartbeat(heartbeat);
        }
    }
    
   

    private Map<String, Object> makeHeartbeat() {

        List<Map<String, Object>> events = _eventQueue.flushEvents();
        Map<String, Object> heartbeat = new HashMap<String, Object>();
        Map<String, String> sanitizedCustomMetadata = null;


        heartbeat.put("t", "CwsSessionHb");
        heartbeat.put("evs", events);
        heartbeat.put("cid", _clientSettings.customerKey);
        heartbeat.put("clid", _clientConfig.get("clientId"));
        heartbeat.put("sid", _sessionId);
        heartbeat.put("seq", _heartbeatSequenceNumber);
        heartbeat.put("pver", Protocol.version);
        heartbeat.put("clv", _clv);
        heartbeat.put("iid", _client.getId());
        heartbeat.put("sdk", true);

        if(SessionType.AD.equals(_sessionType)) {
            heartbeat.put("ad", true);
        }

        //Map<String, String> platformMetadata = getSystemMetadataSchema();
        Map<String, String> platformMetadata;
		try {
			platformMetadata = _protocol.buildPlatformMetadata(_systemMetadata.get());
	        if (platformMetadata != null){
	            heartbeat.put("pm", platformMetadata);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
        if (_monitor != null) {
            _monitor.updateHeartbeat(heartbeat);

        } else {
            // Global session, use 0 as session flag
            heartbeat.put("sf", Protocol.CAPABILITY_GLOBAL);
        }
        if ((Boolean) _clientConfig.get("sendLogs")) {
        	List<String> lb = _systemFactory.getLogBuffer();
            heartbeat.put("lg", lb);
        }
        // put the timestamps last
        double currentTimeMs = _time.current();
        heartbeat.put("st", (int) (currentTimeMs - _startTimeMs));
        heartbeat.put("sst", _startTimeMs);
        // precision caps field. set 0 for insights.
        heartbeat.put("caps", 0);
        _heartbeatSequenceNumber++;
        return heartbeat;
    }
    
    public boolean isGlobalSession() {
    	return _monitor == null;
    }
    
    public boolean isVideoSession() {
        return SessionFactory.SessionType.VIDEO.equals(_sessionType);
    }
    
    public String sessionTypeTag() {
    	if (isGlobalSession()) {
    		return "(global session)";
    	}
    	return "";
    }
    

    private void encodeAndPostHeartbeat(final Map<String, Object> heartbeat) {
        String jsonString = _jsonInterface.encode(heartbeat);
        if (jsonString != null) {
            try {
                postHeartbeat(jsonString);
            } catch (Exception e) {
                _logger.error("JSON post error: " + e.toString());
            }
        }
    }

    private void postHeartbeat(String jsonHeartbeat) {
        String url = _clientSettings.gatewayUrl + Protocol.gatewayPath;
        String contentType = "application/json";
        _logger.info("Send HB[" +  (_heartbeatSequenceNumber-1) + "]" + sessionTypeTag());

        // For debugging only. We do not want to log to logBuffer because if 'sendLogs' is true,
        // then each send log will have previous logbuffer and exponentially increase.
        //_logger.log("heartbeat to be sent: " + jsonHeartbeat, SystemSettings.LogLevel.DEBUG);

        _httpClient.request("POST", url, jsonHeartbeat, contentType,
                new ICallbackInterface() {
					@Override
					public void done(boolean succeeded, String data) {
				        //_logger.consoleLog("heartbeat response: " + data, SystemSettings.LogLevel.DEBUG);
                        try {
                            onHeartbeatResponse(succeeded, data);
                        }catch(NullPointerException e){
                            e.printStackTrace();
                        }catch (Exception e){
                           e.printStackTrace();
                        }
					}
                });
    }

    //class HBTimerTask extends TimerTask {
	//	@Override
	//	public void run() {
    //        createHBTimer();
	//	}
    //}
    
    private void onHeartbeatResponse(Boolean success, final String jsonResponse) {
        String seqNumStr = "-1";
    	if (_cleanedUp)
    		return;

        if (!success && _logger != null) {
            _logger.error("received no response (or a bad response) to heartbeat POST request.");
            // uncomment following during testing if you want to see what's the reason for heartbeat failure
            //_logger.debug("onHeartbeatResponse: data = " + jsonResponse);
            return;
        }

        // Do not log heartbearts requests nor heartbeat responses
        // PII/security concern, also huge overhead
        // _logger.debug("onHeartbeatResponse: data = " + jsonResponse);

        Map<String, Object> decodedResponse = _jsonInterface.decode(jsonResponse);
        if (decodedResponse == null) {
            _logger.warning("JSON: Received null decoded response");
            return;
        }

        if (decodedResponse.containsKey("seq")) {
        	seqNumStr = decodedResponse.get("seq").toString();
        }
    	_logger.debug("onHeartbeatResponse(): received valid response for HB[" + seqNumStr + "]");

        // Unconditionally replace client id from server
        if (decodedResponse.containsKey("clid")) {
        	String newClientId = decodedResponse.get("clid").toString();
            if (!newClientId.equals(_clientConfig.get("clientId"))) {
                _logger.debug("onHeartbeatResponse(): setting the client id to " + newClientId + " (from server)");
                _clientConfig.set("clientId", newClientId);
                _clientConfig.save();
            }        	
        }

        if (decodedResponse.containsKey("err")) {
            String errorMessage = (String)(decodedResponse.get("err"));
            if (!errorMessage.equals(Protocol.BACKEND_RESPONSE_NO_ERRORS)) {
                _logger.error("onHeartbeatResponse(): error posting heartbeat: " + errorMessage);
            }
        }

        if(decodedResponse.containsKey("cfg")) {

            Map<String, Object> cfgResponse = (Map<String, Object>) decodedResponse.get("cfg");
            if(cfgResponse == null){return;}
            boolean newSendLogs = (cfgResponse.containsKey("slg") &&
                    (Boolean) (cfgResponse.get("slg")));
            if (newSendLogs != (Boolean) _clientConfig.get("sendLogs")) {
                _logger.info("Turning " + (newSendLogs ? "on" : "off") + " sending of logs");
                _clientConfig.set("sendLogs", newSendLogs);
            }
            if (cfgResponse.containsKey("hbi")) {
                long heartbeatIntervalSec =
                        (Long) cfgResponse.get("hbi");
                if (_clientSettings.heartbeatInterval != heartbeatIntervalSec) {
                    _logger.info("Received hbIntervalMs from server " +
                            heartbeatIntervalSec);
                    _clientSettings.heartbeatInterval = (int) heartbeatIntervalSec;
                    // Since we just got a HB response, we want to schedule the timer
                    // after 'heartbeatIntervalMs'
                    createHBTimer();
                    //new Timer().schedule(new HBTimerTask(), _settings.heartbeatIntervalMs);;
                }
            }
            if (cfgResponse.containsKey("gw")) {
                String gatewayUrl = (String) (cfgResponse.get("gw"));
                if (!_clientSettings.gatewayUrl.equals(gatewayUrl)) {
                    _logger.info("Received gatewayUrl from server " + gatewayUrl);
                    _clientSettings.gatewayUrl = gatewayUrl;
                }
            }
        }
    }
    
    private void createHBTimer() {
    	if (_hbTimer != null) {
    		_hbTimer.cancel();
    		_hbTimer = null;
    	}
    	        
        _hbTimer = _timer.createRecurring(new Runnable() {
            @Override
            public void run() {
            	sendHeartbeat();
            }
        }, _clientSettings.heartbeatInterval * 1000, "sendHeartbeat");
    }

    public void updateContentMetadata(ContentMetadata contentMetadata) {
        if(_monitor != null ) {
            _monitor.onContentMetadataUpdate(contentMetadata);
        }
    }

    ContentMetadata getContentMetadata() {
        return _contentMetadata;
    }
}

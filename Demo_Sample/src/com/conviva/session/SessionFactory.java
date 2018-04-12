// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.session;

import com.conviva.api.Client;
import com.conviva.api.ClientSettings;
import com.conviva.api.ContentMetadata;
import com.conviva.api.SystemFactory;
import com.conviva.utils.Config;
import com.conviva.utils.Lang;
import com.conviva.utils.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A factory for Session objects.
 */
public class SessionFactory {

    /**
     * The position of video advertisement relative to content.
     */
    public static enum SessionType {
        /** Session of type AD is intended to monitor Ad content. */
        AD,
        /** Session of type VIDEO is intended to monitor main video content. */
        VIDEO,
        /** Session of type GLOBAL is not intended for global events. */
        GLOBAL
    }

	private Client _client;
	private ClientSettings _clientSettings;
	private Config _clientConfig; 
	private SystemFactory _systemFactory;
	private Logger _logger;
	
    private int _nextSessionKey = 0; // should be positive
    private Map<Integer, Session> _sessionsByKey = null;
    private Map<Integer, Integer> _internalSessionIdByKey = null;

    public SessionFactory(Client client, ClientSettings clientSettings,
    		Config clientConfig, SystemFactory systemFactory ) {
    	_client = client;
    	_clientSettings = clientSettings;
    	_clientConfig = clientConfig;
    	_systemFactory = systemFactory;
    	
    	_logger = _systemFactory.buildLogger();
    	_logger.setModuleName("SessionFactory");
    	
        _nextSessionKey= 0;
        _sessionsByKey = new HashMap<Integer, Session>();
        _internalSessionIdByKey = new HashMap<Integer, Integer>();
    }

    /// @brief Cleanup all the sessions owned by the factory
    public void cleanup() {
        if (_sessionsByKey != null) {
        	for(Iterator<Map.Entry<Integer, Session>> it = _sessionsByKey.entrySet().iterator(); it.hasNext(); ) {
        		Map.Entry<Integer, Session> entry = it.next();
        		
                cleanupSession(entry.getKey(), false);  // When deleting from map need to use it.remove, otherwise exception
        		
        		it.remove();
            }
        	/*
            for(Map.Entry<Integer, Session> entry : _sessionsById.entrySet()) {
                cleanupSession(entry.getKey());
            }
            */
        }
        _sessionsByKey = null;
        _internalSessionIdByKey = null;
        _nextSessionKey = 0;
        _logger = null;
    }

    // Generate a new session id
    private int newSessionKey() {
        int sessionKey = _nextSessionKey;
        _nextSessionKey++;
        return sessionKey;
    }

    public int makeAdSession( int contentSessionKey, ContentMetadata adMetadata) {

        if(contentSessionKey == Client.NO_SESSION_KEY)
            return Client.NO_SESSION_KEY;

        Session contentSession = getSession(contentSessionKey);
        ContentMetadata internalAdMetadata = new ContentMetadata(adMetadata); // To make sure we create a copy as we need to add content sessionKey

        if (contentSession != null) {
            ContentMetadata contentMetadata = contentSession.getContentMetadata();

            if (internalAdMetadata.custom == null) {
                internalAdMetadata.custom = new HashMap<String, String>();
            }
            internalAdMetadata.custom.put("c3.csid", String.valueOf(_internalSessionIdByKey.get(contentSessionKey)));

            if((!Lang.isValidString(internalAdMetadata.applicationName)) && contentMetadata != null && Lang.isValidString(contentMetadata.applicationName)) {
                internalAdMetadata.applicationName = contentMetadata.applicationName;
            }

            if((!Lang.isValidString(internalAdMetadata.viewerId)) && contentMetadata != null && Lang.isValidString(contentMetadata.viewerId)) {
                internalAdMetadata.viewerId = contentMetadata.viewerId;
            }

            return makeSession(internalAdMetadata, SessionType.AD);
        }

        return Client.NO_SESSION_KEY;
    }

    public int makeVideoSession(ContentMetadata contentMetadata) /* throws Exception */{
        return makeSession(contentMetadata, SessionType.VIDEO);
    }

    public int makeGlobalSession(ContentMetadata contentMetadata) /* throws Exception */ {
        return makeSession(contentMetadata, SessionType.GLOBAL);
    }
    
    private Session buildVideoSession(int sid, EventQueue eventQueue, ContentMetadata contentMetadata, Monitor monitor, SessionType sessionType) /* throws Exception*/ {
        return new Session(sid, eventQueue, contentMetadata, monitor, _client, _clientSettings, _clientConfig, _systemFactory, sessionType);
    }
    
    private EventQueue buildEventQueue() {
    	return new EventQueue();
    }
    
    public int generateSessionId() {
    	return com.conviva.utils.Random.integer32();
    }
    
    private Monitor buildMonitor(int sid, EventQueue eventQueue, ContentMetadata contentMetadata)/* throws Exception */ {
    	return new Monitor(sid, eventQueue, contentMetadata, _systemFactory);
    }
    
    private int makeSession(ContentMetadata contentMetadata, SessionType sessionType) /* throws Exception */ {
        Session session = null;
    	int internalSessionId = generateSessionId();
    	EventQueue eventQueue = buildEventQueue();

        if(SessionType.AD.equals(sessionType)) {
            Monitor monitor = buildMonitor(internalSessionId, eventQueue, contentMetadata); // As the contentmetadata for ad is an internal copy.
            session = buildVideoSession(internalSessionId, eventQueue, contentMetadata, monitor, sessionType);
        } else {
            ContentMetadata internalContentMetadata = new ContentMetadata(contentMetadata);
            if(SessionType.GLOBAL.equals(sessionType)) {
                session = buildVideoSession(internalSessionId, eventQueue, internalContentMetadata, null, sessionType); // No monitor for global session.
            } else {
                Monitor monitor = buildMonitor(internalSessionId, eventQueue, internalContentMetadata);
                session = buildVideoSession(internalSessionId, eventQueue, internalContentMetadata, monitor, sessionType);
            }
        }

        int externalSessionId = newSessionKey();
        addSession(externalSessionId, session);
        addInternalSessionKey(externalSessionId, internalSessionId);

        session.start();

        return externalSessionId;
    }

    /// @brief Return a session that is associated with session id
    public Session getSession(int sessionKey) {
    	Session s = _sessionsByKey.get(sessionKey);
    	if (s != null)
    		return s;
        _logger.error("Client: invalid sessionId. Did you cleanup that session previously?");
    	return s;
    }

    public Session getVideoSession(int sessionKey) {
    	Session s = _sessionsByKey.get(sessionKey);
    	if (s != null) {
            if (!s.isGlobalSession())
    			return s;
    	}
        _logger.error("Client: invalid sessionId. Did you cleanup that session previously?");
        return null;
    }

    private void addSession(int sessionKey, Session session) {
        _sessionsByKey.put(sessionKey, session);
    }

    private void addInternalSessionKey(int sessionKey, int internalSessionKey) {
        _internalSessionIdByKey.put(sessionKey, internalSessionKey);
    }
    
    private void removeSession(int sessionKey) {
        _internalSessionIdByKey.remove(sessionKey);
        _sessionsByKey.remove(sessionKey);
    }
    
    /// @brief Cleanup a single session
    public void cleanupSession(int sessionKey, boolean deleteFromMap) {
        Session session = _sessionsByKey.get(sessionKey);
        if (session != null) {
            if (deleteFromMap) {
                _sessionsByKey.remove(sessionKey);
                _internalSessionIdByKey.remove(sessionKey);
            }
            _logger.info("session id(" + sessionKey + ") is cleaned up and removed from sessionFactory");
            session.cleanup();
        }
    }
}

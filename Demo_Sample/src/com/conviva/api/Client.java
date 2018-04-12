// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

import com.conviva.api.player.PlayerStateManager;
import com.conviva.session.Session;
import com.conviva.session.SessionFactory;
import com.conviva.utils.Config;
import com.conviva.utils.ExceptionCatcher;
import com.conviva.utils.Logger;
import com.conviva.utils.Random;

/**
 * The Client class is responsible for managing SDK life cycle and video
 * sessions. In an application, a single instance of Client is maintained and is
 * used to manage, create PlayerStateManager, sessions and perform other
 * important tasks.
 */
public class Client {
    /**
     * The severity of errors reported to Conviva.
     */
    public static enum ErrorSeverity {
	    /** The error could prevent playback altogether. */
		FATAL, 
	    /** The error should not affect playback. */
		WARNING 
	}

	/**
	 * Video stream that contains video advertisement.
	 */	
	public static enum AdStream {
		/** The ad is embedded inside the video content stream. */
		CONTENT,
		/** The ad is played from a separate video stream. */
		SEPARATE
	}

	/**
	 * The video player in charge of rendering video advertisement.
	 */	
	public static enum AdPlayer {
		/** Ads and content are played using the same video player instance. */
		CONTENT,
		/** Ads and content  are played using separate video player instances. */
		SEPARATE
	}
	
	/**
	 * The position of video advertisement relative to content.
	 */	
	public static enum AdPosition {
	    /** The ad is a preroll, kicking in before content. */
		PREROLL,
		/** The ad is a midroll, kicking in during content. */
		MIDROLL,
		 /** The ad is a postroll, kicking in after content. */
		POSTROLL
	}

	/**
	 * A default sessionKey to use for the Client.sendCustomEvent API if you do not yet have a Conviva session.
	 */
	public final static int NO_SESSION_KEY = -2;

	/**
	 * Possible device types reported to Conviva.
	 */
	public enum DeviceType {

		DESKTOP {
		    @Override
		    public String toString() {
		      return "DESKTOP";
		    }
		  },
		  CONSOLE {
		    @Override
		    public String toString() {
		      return "Console";
		    }
		  },
		  SETTOP {
		    @Override
		    public String toString() {
		      return "Settop";
		    }
		  },
		  MOBILE {
			    @Override
			    public String toString() {
			      return "Mobile";
			    }
			  },
		  TABLET {
			    @Override
			    public String toString() {
			      return "Tablet";
			    }
			  },
		  SMARTTV {
			    @Override
			    public String toString() {
			      return "SmartTV";
			    }
			  },
		  UNKNOWN {
			    @Override
			    public String toString() {
			      return "Unknown";
			    }
			  }
	}	

	
    private Logger _logger = null;
    private SessionFactory _sessionFactory;
    private SystemFactory _systemFactory;
    private int _globalSessionKey = -1;
    private ClientSettings _settings = null;
    private ExceptionCatcher _exceptionCatcher = null;
    private boolean _released = false;
    
    private Config _config = null;
    private int _id = -1;
    
    /**
     * The current version of the Conviva library.
     */
    // the next line will be modified by set_versions.pl
    public static final String version = "2.140.0.35590";

    /**
     * Whether the Client successfully initialized or not.
     */
    private volatile boolean initialized = false;
    private boolean defaultGatewayURLError = false;

    /**
     * Client
	 * Most applications will only need one Client, created during application initialization and released during application shutdown.
	 * See tutorial 3-integrating-with-the-video-application
     * @param clientSettings An instance of Settings representing Conviva settings to be used.
     * @param systemFactory Factory to use for all system information and utility needs.
     */
    
    public Client(final ClientSettings clientSettings, SystemFactory systemFactory) {
        if (!clientSettings.isInitialized()) {
            return;
        }

        // This check should be done before sanitizing ClientSettings.
        try {
            if(((new URL(ClientSettings.defaultProductionGatewayUrl).getHost()).equals(new URL(clientSettings.gatewayUrl).getHost())))
                defaultGatewayURLError = true;      // As logger is not available, wait for the logger to be available and then print this error.
        } catch (MalformedURLException e1) {

        }

        _settings = new ClientSettings(clientSettings);
        
        this._systemFactory = systemFactory;
        this._systemFactory.configure("SDK", _settings);

        this._exceptionCatcher = this._systemFactory.buildExceptionCatcher();

        class MyCallable implements Callable<Void> {
        	Client _client;
        	public MyCallable(Client client) {
        		_client = client;
        	}
			@Override
			public Void call() throws Exception {
                _logger = _systemFactory.buildLogger();
                _logger.setModuleName("Client");

                // Not logging customerKey anymore, security/privacy concerns
                _logger.info("init(): url=" + _settings.gatewayUrl);
                if(defaultGatewayURLError) {
                    _logger.error("Gateway URL should not be set to https://cws.conviva.com or http://cws.conviva.com, therefore this call is ignored");
                    defaultGatewayURLError = false;
                }

                _id = Random.integer32();

                _config = _systemFactory.buildConfig(_client);
                
                _config.load();

                _sessionFactory = _systemFactory.buildSessionFactory(_client, _settings, _config);

                _logger.info("init(): done.");    	
				return null;
			}
        }

        try {
            _exceptionCatcher.runProtected(new MyCallable(this), "Client.init");
            initialized = true;
        } catch (Exception e) {
            initialized = false;
            //clean up resourced when initalization failed.
            _systemFactory = null;
            _exceptionCatcher = null;
            if(_sessionFactory != null) {
                _sessionFactory.cleanup();
            }
            _sessionFactory = null;
        }
    }

    /**
     * Returns true if the client has been initialized.
     * @return If Client has been successfully initialized and not released.
     */
    public boolean isInitialized() {
        return initialized && !this._released;
    }
    /**
     * Unloads the Conviva client.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void release() throws ConvivaException {
        // Since this can be called when initialization fails, we have to be careful when cleaning up.
        // All references could be incorrect or incomplete.
        // console.log("CLEANUP!");
        if (this._released) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        // Nothing to do if the Client is not initalized at all
        if (!isInitialized()) return;

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
		        _logger.info("release()");
		        _sessionFactory.cleanup(); // Cleans up all sessions, too.
		        _sessionFactory = null;
		        _globalSessionKey  = -1;
		        _logger = null;
		        _id = -1; // # ugly?
		        _exceptionCatcher = null;
		        _settings = null;
		        _systemFactory = null;
		        _released = true;
				return null;
			}
        }
    	_exceptionCatcher.runProtected(new MyCallable(), "Client.release");
    }

    /** 
     * Creates a Conviva monitoring session.
     * Use when the viewer requests playback for video content.<br>
     * Unless your application can display multiple videos concurrently, you should only ever have one active monitoring session.
     * See tutorial 3-integrating-with-the-video-application
     * @param contentMetadata An instance of ContentMetadata containing the content metadata for this session.
     * @return Opaque identifier for the newly created session. Will be Client.NO_SESSION_KEY if session creation failed
     * @throws ConvivaException When Conviva internal exception happens during session creations.
     */
    public int createSession(final ContentMetadata contentMetadata) throws ConvivaException
    {
        int sid = Client.NO_SESSION_KEY;
        
        if (!isInitialized()) return sid; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
        	int id = Client.NO_SESSION_KEY;
			@Override
			public Void call() throws Exception {
                id = _sessionFactory.makeVideoSession(contentMetadata); // "this" bind
				return null;
			}
			
			public int getSessionId() {
				return id;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.createSession");
    	return myCallable.getSessionId();
    }

    /**
     * Creates a Conviva Ad monitoring session.<br>
     * Use when an ad playback starts for a particular content session.
     * @param contentSessionKey An identifier of the content session to which ad session belongs.
     * @param adMetadata An instance of ContentMetadata containing the ad metadata for this session.
     * @return Opaque identifier for the newly created ad session. Will be Client.NO_SESSION_KEY if session creation failed
     * @throws ConvivaException When Conviva internal exception happens during session creations.
     */
    public int createAdSession(final int contentSessionKey, final ContentMetadata adMetadata) throws ConvivaException
    {
        int sid = Client.NO_SESSION_KEY;

        if (!isInitialized()) return sid; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
            int id = Client.NO_SESSION_KEY;
            @Override
            public Void call() throws Exception {
                id = _sessionFactory.makeAdSession(contentSessionKey, adMetadata);
                return null;
            }

            public int getSessionId() {
                return id;
            }
        }

        MyCallable myCallable = new MyCallable();
        _exceptionCatcher.runProtected(myCallable, "Client.createAdSession");
        return myCallable.getSessionId();
    }
    
    
    /** 
     * Reports an error for this monitoring session.
     * @param sessionKey The identifier for the monitoring session.
     * @param errorMsg The error message to be reported.
     * @param errorSeverity The severity of the error.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void reportError(final int sessionKey, final String errorMsg,
                                   final Client.ErrorSeverity errorSeverity) throws ConvivaException {
        if (!isInitialized()) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
                Session session = _sessionFactory.getVideoSession(sessionKey); // "this" bind
                if (session != null) {
                    session.reportError(errorMsg, errorSeverity);
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.reportError");
    }
    
    /** 
     * This API is deprecated. Use updateContentMetadata(final ContentMetadata _contentMetadata) API in {@link PlayerStateManager} to update ContentMetadata <br>
     * <br> Update missing content metadata for an existing monitoring session.
     * @param sessionKey The identifier for the monitoring session.
     * @param contentMetadata New content metadata for the monitoring session.
	 * @throws ConvivaException When Conviva internal exception happens.
     */
    //TODO: As of now these deprecated API's will be used in all our drop in library as well.
    //In future when we plan to remove these API's completely, we need to rename these API's if required and keep the functionalities intact.
    @Deprecated
    public void updateContentMetadata(final int sessionKey, final ContentMetadata contentMetadata) throws ConvivaException
    {
        if (!isInitialized()) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
            @Override
            public Void call() throws Exception {
                Session session = _sessionFactory.getVideoSession(sessionKey); // "this" bind
                if (session != null) {
                    session.updateContentMetadata(contentMetadata);
                }
                return null;
            }
        }

        MyCallable myCallable = new MyCallable();
        _exceptionCatcher.runProtected(myCallable, "Client.updateContentMetadata");
    }
    
    
    /** 
     * Detach the video player from the monitoring session.
     * Use when video player currently attached is no longer relevant for the current session.
     * See tutorial 2-collecting-video-playback-data
     * See tutorial 3-integrating-with-the-video-application
     * @param sessionKey The identifier for the monitoring session.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void detachPlayer(final int sessionKey) throws ConvivaException {
        if (!isInitialized()) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
                Session session = _sessionFactory.getVideoSession(sessionKey); // "this" bind
                if (session != null) {
                    session.detachPlayer();
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.detachPlayer");
    }
    
    /** 
     * Attach a video player to the monitoring session.
     * Use when a video player becomes relevant for the current session.
     * See tutorial 2-collecting-video-playback-data
     * See tutorial 3-integrating-with-the-video-application
     * @param sessionKey The identifier for the monitoring session.
     * @param playerStateManager Instance of PlayerStateManager currently gathering data from a video player.
	 * @throws ConvivaException When Conviva internal exception happens.
     */
    public void attachPlayer(final int sessionKey, final PlayerStateManager playerStateManager) 
            throws ConvivaException {
        if (!isInitialized()) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        if (playerStateManager == null) {
            _logger.error("attachPlayer(): expecting an instance of PlayerStateManager for playerStateManager parameter");
            return;
        }        
        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
                Session session = _sessionFactory.getVideoSession(sessionKey); // "this" bind
                if (session != null) {
                    session.attachPlayer(playerStateManager);
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.attachPlayer");
    }
    
    /** 
     * Notification that we will be preloading content with the video player.<br>
     * Use when a video player is loading content but is not yet displaying video for the viewer.
     * @param sessionKey The identifier for the monitoring session.
	 * @throws ConvivaException When Conviva internal exception happens.
     */    
    public void contentPreload(final int sessionKey) 
        throws ConvivaException {
        if (!isInitialized()) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
                Session session = _sessionFactory.getVideoSession(sessionKey); // "this" bind
                if (session != null) {
                    session.contentPreload();
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.contentPreload");
    }


    /**
     * Notification that the attached video player is no longer preloading, and has started displaying video for the viewer.
     * Only usable if preceded by contentPreload.
     * @param sessionKey The identifier for the monitoring session.
	 * @throws ConvivaException When Conviva internal exception happens.
     */
    public void contentStart(final int sessionKey) 
            throws ConvivaException {
        if (!isInitialized()) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
                Session session = _sessionFactory.getVideoSession(sessionKey); // "this" bind
                if (session != null) {
                    session.contentStart();
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.contentStart");
    }
    
    
    /** 
     * Send a custom event to Conviva.
     * @param sessionKey The identifier for the monitoring session. Use Client.NO_SESSION_KEY if you do not yet have a monitoring session.
     * @param eventName Name of the custom event.
     * @param attributes A dictionary of key value pair associated with the event
	 * @throws ConvivaException When Conviva internal exception happens.
     */    
    public void sendCustomEvent(final int sessionKey, final String eventName, final Map<String, Object> attributes) throws ConvivaException{
    	
        if (!isInitialized()) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
				int sKey = sessionKey;
                if (sKey == Client.NO_SESSION_KEY) { // custom event is not tied to a particular video session
                    // Have we created the global session already ? 
                    if (_globalSessionKey < 0) { // "this" bind
                        ContentMetadata sm = new ContentMetadata(); // ## delete that & simply make global sessions not require metadata?
                        _globalSessionKey = _sessionFactory.makeGlobalSession(sm); // "this" bind
                    }
                    sKey = _globalSessionKey; // "this" bind
                }
                Session session = _sessionFactory.getSession(sKey); // "this" bind
                if (session != null) {
                    session.sendCustomEvent(eventName, attributes);
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.sendCustomEvent");
    }

    
    /** 
     * Notify Conviva that an ad is about to start for the monitoring session.
     * @param sessionKey The identifier for the monitoring session.
     * @param adStream Whether the ad is embedded inside the content stream.
     * @param adPlayer Whether the ad is played by the same player as the original video content.
     * @param adPosition Whether the ad is a preroll, midroll or postroll.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void adStart(final int sessionKey, 
    							final AdStream adStream,
    							final AdPlayer adPlayer,
    							final AdPosition adPosition) throws ConvivaException{
        if (!isInitialized()) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
                Session session = _sessionFactory.getVideoSession(sessionKey); // "this" bind
                if (session != null) {
                    session.adStart(adStream, adPlayer, adPosition);
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.adStart");
    }

    /** 
     * Notify Conviva that an ad has ended for the monitoring session.
     * @param sessionKey The identifier for the monitoring session.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void adEnd(final int sessionKey) throws ConvivaException{
        if (!isInitialized()) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
                Session session = _sessionFactory.getVideoSession(sessionKey); // "this" bind
                if (session != null) {
                    session.adEnd();
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.adEnd");
    }
    
    /** 
     * Terminates a monitoring session.
     * Use when playback for video content ends, fails or is cancelled by the viewer.
     * See tutorial 3-integrating-with-the-video-application
     * @param sessionKey The identifier for the monitoring session.
     * @throws ConvivaException When Conviva internal exception happens.
     */
    public void cleanupSession(final int sessionKey) throws ConvivaException{
        if (!isInitialized() ) return; // everything has been cleaned up already, just do nothing to prevent exceptions

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
                Session session = _sessionFactory.getVideoSession(sessionKey); // "this" bind
                if (session != null) { // don't allow cleaning up global session
                    _sessionFactory.cleanupSession(sessionKey, true); // "this" bind
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.cleanupSession");
    }

    
    
    /** 
     * Provides a PlayerStateManager instance.
     * See tutorial 2-collecting-video-playback-data
     * @return An instance of PlayerStateManager.
     * @throws ConvivaException When Conviva Client has been released.
     */
    public PlayerStateManager getPlayerStateManager() throws ConvivaException {
        if (!isInitialized()) throw new ConvivaException("This instance of Conviva.Client is not active."); // everything has been cleaned up already, just do nothing to prevent exceptions
        // The main purpose of providing this API is to control dependency injection into PlayerStateManagers.
        return new PlayerStateManager(this._systemFactory);
    }

    /** 
     * Properly frees a PlayerStateManager instance.
     * See tutorial 2-collecting-video-playback-data
     * @param playerStateManager An instance of PlayerStateManager to be free up.
     * @throws ConvivaException When Conviva Client is not initialized.
     */
    public void releasePlayerStateManager(final PlayerStateManager playerStateManager)throws ConvivaException {
        if (!isInitialized()) throw new ConvivaException("This instance of Conviva.Client is not active."); // everything has been cleaned up already, just do nothing to prevent exceptions
        // The main purpose of providing this API is to force release of PlayerStateManagers through our code
        // so we can manage the SystemFactory dependency properly.
        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
                if (playerStateManager instanceof PlayerStateManager) {
                    playerStateManager.release();
                }
				return null;
			}
        }
        
        MyCallable myCallable = new MyCallable();
    	_exceptionCatcher.runProtected(myCallable, "Client.releasePlayerStateManager");
    }
    
    /**
     * Returns a ClientSettings object.
     * @return a copy of the current ClientSettings for this Client.
     */
    public ClientSettings getSettings() {
        if (!isInitialized()) return null; // everything has been cleaned up already, just do nothing to prevent exceptions
        return new ClientSettings(this._settings);
    }
    
    /**
     * Returns an id for the current client instance.
     * @return an identifier for this Client.
     */
    public int getId() {
        return this._id;
    }
}

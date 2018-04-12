
package com.conviva.api.system;


/**
 * Callback Interface used by SystemInterface implementation to notify Conviva.
 */
public interface ICallbackInterface {

    /** 
     * Processing status
     * All HTTP responses with status codes in the 200s should be considered successful.
     * @param succeeded Indicates whether operation was performed successfully
     * @param data Operation data when successful, otherwise error message.
     */
	public void done(boolean succeeded, String data); 
}

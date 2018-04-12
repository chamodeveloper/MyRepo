package com.conviva.api.system;

/**
 * IStorageInterface - The underlying storage technology used must be reliable and data must be persisted for as long as possible.
 */

public interface IStorageInterface {
    /** 
     * Save data on local storage.
     * Will be called when first calling Client.createSession on a new device.
     * @param storageSpace Namespace to store local data into.
     * @param storageKey Key to store local data into.
     * @param data Data to be save.
     * @param callback Callback to call when done.
     * If the query was successful, it must be true even if the returned data was null/empty.
     * If storage data is not persisted or persistence is flaky for your device, please contact Conviva for more information.
     */
	public void saveData(String storageSpace, String storageKey, String data, ICallbackInterface callback);
	
    /** 
     * Load data from local storage.
     * Will be called when creating a Client.
     * @param storageSpace Namespace to load local data from.
     * @param storageKey Key to load local data from.
     * @param callback Callback to call when done.
     *        If the query was successful, it must be true even if the returned data was null/empty.
     *        Returned data can be still null if Conviva data was never saved before.
     */
	public void loadData(String storageSpace, String storageKey, ICallbackInterface callback);

    /** 
     * Delete data from local storage.
     * @param storageSpace Namespace to delete local data from.
     * @param storageKey Key to delete local data from.
     * @param callback to call when done.
     */
	public void deleteData(String storageSpace, String storageKey, ICallbackInterface callback);

    /** 
     * Notification that this StorageInterface is no longer needed.
     */
    public void release();
}

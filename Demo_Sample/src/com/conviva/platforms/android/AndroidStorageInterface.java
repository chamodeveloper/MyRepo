package com.conviva.platforms.android;

import com.conviva.api.Client;
import com.conviva.api.system.ICallbackInterface;
import com.conviva.api.system.IStorageInterface;
import com.conviva.api.system.SystemInterface;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Conviva provided helper class which implements {@link IStorageInterface} required
 * methods. The application can implement its own {@link IStorageInterface} conforming
 * class for creating {@link SystemInterface} while creating a {@link Client}.
 */
public class AndroidStorageInterface implements IStorageInterface {
	private Context _context = null;

	public AndroidStorageInterface(Context context) {
		_context = context;
	}
	
	@Override
	public void saveData(String storageSpace, String storageKey, String data,
			ICallbackInterface callback) {
		SharedPreferences prefs = _context.getSharedPreferences(storageSpace, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(storageKey, data);
		if (editor.commit())
			callback.done(true, data);
		else
			callback.done(false, "Failed to write data");
	}

	@Override
	public void loadData(String storageSpace, String storageKey,
			ICallbackInterface callback) {
		String data = null;
		SharedPreferences prefs = _context.getSharedPreferences(storageSpace, Context.MODE_PRIVATE);
		try {
			data = prefs.getString(storageKey, null);
		} catch (Exception e) {
			callback.done(false, e.toString());
			return;
		}
		
		// data==null is not an error condition since this will happen 1st time.
		//if (data == null)
		//	callback.done(false, "Data not found");
		//else 
			callback.done(true, data);
	}

	@Override
	public void deleteData(String storageSpace, String storageKey,
			ICallbackInterface callback) {
		SharedPreferences prefs = _context.getSharedPreferences(storageSpace, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(storageKey);
		if (editor.commit())
			callback.done(true, null);
		else
			callback.done(false, "Failed to delete data");
	}

	@Override
	public void release() {
		// nothing to release
	}
}

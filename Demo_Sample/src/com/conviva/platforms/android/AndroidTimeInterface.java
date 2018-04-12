package com.conviva.platforms.android;

import java.util.Date;

import com.conviva.api.Client;
import com.conviva.api.system.ITimeInterface;
import com.conviva.api.system.SystemInterface;

/**
 * Conviva provided helper class which implements {@link ITimeInterface} required
 * methods. The application can implement its own {@link ITimeInterface} conforming
 * class for creating {@link SystemInterface} while creating a {@link Client}.
 */
public class AndroidTimeInterface implements ITimeInterface {

	@Override
	public double getEpochTimeMs() {
		return (new Date()).getTime();
	}

	@Override
	public void release() {
		// Nothing to do
	}

}

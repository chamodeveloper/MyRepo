package com.conviva.utils;

/**
 * Conviva Random util class.
 */
public class Random {
	public static int integer32() {
		return Math.abs(new java.security.SecureRandom().nextInt());
	}
}

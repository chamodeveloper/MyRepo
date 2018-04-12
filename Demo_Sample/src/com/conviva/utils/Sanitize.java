package com.conviva.utils;

/**
 * Conviva util class to check and convert int to a valid int if required.
 */
public class Sanitize {
	public static int Integer(int number, int minNumber, int maxNumber, int defaultNumber) {
	    if (number == defaultNumber) return defaultNumber;
	    return Sanitize.EnforceBoundaries(number, minNumber, maxNumber);	}
	
	private static int EnforceBoundaries(int number, int minNumber, int maxNumber) {
	    if (number > maxNumber) {
	        number = maxNumber;
	    } else if (number < minNumber) {
	        number = minNumber;
	    }
	    return number;		
	}
}

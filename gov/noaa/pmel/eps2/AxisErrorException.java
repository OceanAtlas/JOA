package gov.noaa.pmel.eps2;

import java.lang.*;

/**
 * <code>AxisErrorException</code>
 * Axis does not exist.
 * Attempt to set a time axis to a non-time axis
 * Couldn't get a time axis as requested
 * Error getting a value out of an axis's multiarray
 *
 * @author oz
 * @version 1.0
 */
 
public class AxisErrorException extends Exception {
	public AxisErrorException() {
		super();
	}
	
	public AxisErrorException(String s) {
		super(s);
	}
}
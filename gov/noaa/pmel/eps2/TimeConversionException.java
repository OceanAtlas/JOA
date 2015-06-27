package gov.noaa.pmel.eps2;

import java.lang.*;

/**
 * <code>TimeConversionException</code>
 * An error occurred try to convert to/from EPIC Time units
 *
 * @author oz
 * @version 1.0
 */
 
public class TimeConversionException extends Exception {
	public TimeConversionException() {
		super();
	}
	
	public TimeConversionException(String s) {
		super(s);
	}
}
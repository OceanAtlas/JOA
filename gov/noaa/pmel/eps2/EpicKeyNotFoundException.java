package gov.noaa.pmel.eps2;

import java.lang.*;

/**
 * <code>EpicKeyNotFoundException</code> 
 * The specified EPIC Key was not found in the EPIC Key database
 *
 * @author oz
 * @version 1.0
 */
public class EpicKeyNotFoundException extends Exception {
	public EpicKeyNotFoundException() {
		super();
	}
	
	public EpicKeyNotFoundException(String s) {
		super(s);
	}
}

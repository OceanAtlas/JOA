package gov.noaa.pmel.eps2;

import java.lang.*;

/**
 * <code>EPSVariableException</code> 
 * An error occured operating on an EPSVariable
 *
 * @author oz
 * @version 1.0
 */
public class EPSVariableException extends Exception {
	public EPSVariableException() {
		super();
	}
	
	public EPSVariableException(String s) {
		super(s);
	}
}

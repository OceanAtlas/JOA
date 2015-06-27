package gov.noaa.pmel.eps2;

import java.lang.*;

/**
 * <code>EPSVariableException</code> 
 * EPSVariable does not exist in the database.
 *
 * @author oz
 * @version 1.0
 */
public class EPSVarDoesNotExistExcept extends Exception {
	public EPSVarDoesNotExistExcept() {
		super();
	}
	
	public EPSVarDoesNotExistExcept(String s) {
		super(s);
	}
}

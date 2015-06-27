/*
 * $Id: NeutralSurfaceErrorException.java,v 1.2 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

@SuppressWarnings("serial")
public class NeutralSurfaceErrorException extends Exception {
	String mErrStr;
	public NeutralSurfaceErrorException() {
		super();
	}
	
	public NeutralSurfaceErrorException(String s) {
		super(s);
		mErrStr = new String(s);
	}
	
	public String getErrString() {
		return mErrStr;
	}
}
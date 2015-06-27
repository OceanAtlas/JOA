/*
 * $Id: PreferencesErrorException.java,v 1.2 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

@SuppressWarnings("serial")
public class PreferencesErrorException extends Exception {
	int mLineNumberOfError;
			
	public PreferencesErrorException() {
		super();
	}
	
	public PreferencesErrorException(String s) {
		super(s);
	}
}
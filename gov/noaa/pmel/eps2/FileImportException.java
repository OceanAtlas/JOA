package gov.noaa.pmel.eps2;

import java.lang.*;

/**
 * <code>FileImportException</code> 
 * An error occurred trying to import a file into an EPSDbase.
 *
 * @author oz
 * @version 1.0
 */

public class FileImportException extends Exception {
	int mLineNumberOfError;
	String mErrorType;
			
	public FileImportException() {
		super();
	}
	
	public FileImportException(String s) {
		super(s);
	}
		
	public void setErrorLine(int errLine) {
		mLineNumberOfError = errLine;
	}
		
	public int getErrorLine() {
		return mLineNumberOfError;
	}
		
	public void setErrorType(String type) {
		mErrorType = type;
	}
		
	public String getErrorType() {
		return mErrorType;
	}
}
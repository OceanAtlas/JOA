/*
 * $Id: FileImportException.java,v 1.2 2005/06/17 18:03:35 oz Exp $
 *
 */

package javaoceanatlas.io;

@SuppressWarnings("serial")
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
	
	public void setProgressWindow() {
		
	}
}
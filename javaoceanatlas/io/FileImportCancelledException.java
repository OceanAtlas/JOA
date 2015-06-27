/*
 * $Id: FileImportCancelledException.java,v 1.2 2005/06/17 18:03:35 oz Exp $
 *
 */

package javaoceanatlas.io;

import java.lang.*;

public class FileImportCancelledException extends Exception {
	public FileImportCancelledException() {
		super();
	}
	
	public FileImportCancelledException(String s) {
		super(s);
	}
}
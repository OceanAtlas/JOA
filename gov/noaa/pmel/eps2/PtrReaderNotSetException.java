package gov.noaa.pmel.eps2;
import java.lang.*;

/**
 * A file reader has not been specified for the pointer database.
 *
 * @author oz
 * @version 1.0
 */
 
public class PtrReaderNotSetException extends Exception {
	public PtrReaderNotSetException() {
		this("A file reader has not been specified for the pointer database");
	}
	
	public PtrReaderNotSetException(String s) {
		super(s);
	}
}

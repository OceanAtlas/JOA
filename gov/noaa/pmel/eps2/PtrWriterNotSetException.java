package gov.noaa.pmel.eps2;
import java.lang.*;

/**
 * A file writer has not been specified for the pointer database.
 *
 * @author oz
 * @version 1.0
 */
 
public class PtrWriterNotSetException extends Exception {
	public PtrWriterNotSetException() {
		this("A file writer has not been specified for the pointer database");
	}
	
	public PtrWriterNotSetException(String s) {
		super(s);
	}
}

package gov.noaa.pmel.eps2;
import java.lang.*;
/**
 * The specified File type is unknown
 * It's not a pointer file or a section file
 *
 *
 * @author oz
 * @version 1.0
 */
 
public class UnknownFileFormatException extends Exception {
	public UnknownFileFormatException() {
		super();
	}
	
	public UnknownFileFormatException(String s) {
		super(s);
	}
}

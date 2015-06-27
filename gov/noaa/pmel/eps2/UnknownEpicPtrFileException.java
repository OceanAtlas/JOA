package gov.noaa.pmel.eps2;
import java.lang.*;
/**
 * The specified EpicPtr File type is unknown
 *
 *
 * @author oz
 * @version 1.0
 */
 
public class UnknownEpicPtrFileException extends Exception {
	public UnknownEpicPtrFileException() {
		super();
	}
	
	public UnknownEpicPtrFileException(String s) {
		super(s);
	}
}

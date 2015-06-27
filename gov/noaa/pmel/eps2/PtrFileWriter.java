package gov.noaa.pmel.eps2;
import java.util.*;
import java.io.*;
/**
 * <code>PtrFileWriter</code> defines the write method for format dependent writer classes.
 *
 * @see EpicPtrWriter, XMLPtrWriter
 *
 * @author oz
 * @version 1.0
 */
 
public interface PtrFileWriter {
	/*
	* Write a pointer file.
	*
	* @exception IOException An IO error occurred parsing the pointer file
	*/
	public void write(ArrayList thePtrs) throws IOException;
	public void write(ArrayList thePtrs, PointerFileAttributes attributes) throws IOException;
}

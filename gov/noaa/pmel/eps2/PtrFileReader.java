package gov.noaa.pmel.eps2;

import gov.noaa.pmel.util.GeoDate;
import java.util.*;
import java.io.*;
/**
 * <code>PtrFileReader</code> defines the parse method for file dependent reader classes.
 *
 * @see ProfilePtrReader, TimeSeriesReader
 *
 * @author oz
 * @version 1.0
 */
 
public interface PtrFileReader {
	/*
	* Parse a pointer file.
	*
	* @returns ArrayList of pointer file entries
	*
	* @exception IOException An IO error occurred parsing the pointer file
	*/
	public ArrayList parse() throws IOException;
	public boolean isArgo();
}

package gov.noaa.pmel.eps2;

import ucar.multiarray.*;
import java.io.*;

/**
 * <code>EPSFileReader</code> defines methods for a generic file reader.
 *
 * @see EpicPtrFactory
 *
 * @author oz
 * @version 1.0
 */
public interface EPSFileReader {
	/**
	 * Parse a datafile (fill in a dbase object).
	 *
	 */
	public void parse() throws Exception;
	
	/**
	 * Get variable data from file .
	 *
	 * @param varname Get a multiarray for the specified variable
   	 * @param lci coordinates to check range of request against valid range
   	 * @param uci coordinates to check range of request against valid range
   	 * @param dim axis dimension to check against valid dimension
   	 *
	 * @exception EPSVarDoesNotExistExcept Specified variable does not exist in file.
	 * @exception IOException IO Error occured getting variable
	 */
	public MultiArray getvar(String varname, int[] lci, int[] uci, int[] dims) throws EPSVarDoesNotExistExcept, IOException;
	
	/**
	 * Get variable data using user coordinates from file.
	 *
	 * @param varname Get a multiarray for the specified variable
	 *
	 * @exception EPSVarDoesNotExistExcept Specified variable does not exist in file.
	 * @exception IOException IO Error occured getting variable
	 */
	public MultiArray getvar(String varname) throws EPSVarDoesNotExistExcept,IOException;

}


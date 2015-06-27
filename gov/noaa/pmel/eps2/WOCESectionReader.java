package gov.noaa.pmel.eps2;

import java.io.*;
import ucar.multiarray.*;

/**
 * <code>WOCESectionReader</code> 
 * Concrete implementation of the EPSFileReader interface to read, parse, and save a WOCE Section file
 *
 * @author oz
 * @version 1.0
 *
 * @see EPSFileReader
 */
public class WOCESectionReader implements EPSFileReader, EPSConstants {
	/**
	* ID count (< 0 if variable not found in EPIC Key database
	*/
	protected int mIDCount = -1;
	/**
	* Dbase that this file reader is initializing
	*/
	protected Dbase mOwnerDBase;
	/**
	* File to read
	*/
	protected File mFile;
	
  	/**
   	* Construct a new <code>WOCESectionReader</code> with a Dbase amd file.
   	*
   	* @param dname Dbase that this reader will fill in
   	* @param inFile Source section data file
   	*
 	* @see Dbase
   	*/
	public WOCESectionReader(Dbase dname, File inFile) {
		mOwnerDBase = dname;
		mFile = inFile;
	}
	
	// concrete implementations of the io routines
  	/**
   	* Parse the section file and fill in the Dbase.
   	*
   	* @return Success code
   	*/
	public void parse() throws Exception {
	}
	
  	/**
   	* Get variable data from section file.
   	*
   	* @param inName Name of variable to get data for
   	*
   	* @return Multiarray of data
   	*
   	* @exception EPSVariableDoesNotExistException Variable not found in the owner database
   	* @exception IOException An IO error occurred getting the data 
   	*/
	public MultiArray getvar(String inName) throws EPSVarDoesNotExistExcept, IOException {
		return null;
	}
	public MultiArray getvar(String inName, int[] lci, int[] uci, int[] dims) throws EPSVarDoesNotExistExcept, IOException {
		return null;
	}
	
}
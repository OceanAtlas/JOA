package gov.noaa.pmel.eps2;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import gov.noaa.pmel.util.*;
/**
 * <code>TimeSeriesPtrReader</code> a reader class for EPIC time series ptr files
 *
 * @see EpicPtrFactory
 *
 * @author oz
 * @version 1.0
 */
 
public class TimeSeriesPtrReader implements PtrFileReader, EPSConstants {
    /**
     * Epic ptr file to read
     */
	protected File mFile;
	
    /**
     * keys to use when sorting the file in place
     */
	protected int[] mSortKeys = new int[4];
		
    /**
     * Construct a new <code>TimeSeriesPtrReader</code>.
     */
	public TimeSeriesPtrReader(File inFile, int[] inKeys) {
		mFile = inFile;
		if (inKeys != null)
			setSortKeys(inKeys);
	}
	
    /**
     * set the sort keys
     */
	public void setSortKeys(int[] inKeys) {
		for (int i=0; i<4; i++)
			mSortKeys[i] = inKeys[i];
	}
	
    /**
     * Parse the ptr file and create a collection of EpicPtrs
     *
     * @exception IOException An IO error occured parsing the pointer file
     */
	public ArrayList parse() throws IOException {
		String dataType = new String();
		String database = new String();
		String fileName;
		ArrayList paramList = null;
		String path;
		double lat;
		double lon;
		GeoDate stDate=null, endDate=null;
		double z, del;
		ArrayList filePtrs = new ArrayList();
		
	    try {
			FileReader fr = new FileReader(mFile);
		    LineNumberReader in = new LineNumberReader(fr, 10000);
			while (true) {
				String inLine = in.readLine();
				if (inLine == null) 
					break;
				
				if (inLine.length() == 0)
					continue;
					
				// look for the line with DAT and DATABASE in it
 				if (inLine.toUpperCase().indexOf("DAT") >= 0 && inLine.toUpperCase().indexOf("DATABASE") >= 0) {
 					// skip the next line with the column format string
 					inLine = in.readLine();
 					
 					// get the next line with values
 					inLine = in.readLine();
					String oldDelim = EPSProperties.SDELIMITER;
					EPSProperties.SDELIMITER = EPSProperties.SSPACE_DELIMITER;
						dataType = EPS_Util.getItem(inLine, 1);
						database = EPS_Util.getItem(inLine, 2);
					EPSProperties.SDELIMITER = oldDelim;
				}
				
				// look for line with "LATITUDE" in it--these are the column headers;
 				if (inLine.toUpperCase().indexOf("LATITUDE") >= 0) {
 					// skip the next line with the column format string
 					inLine = in.readLine();
 					
 					// determine whether the pointer has a parameter or variable list
 					boolean hasParams = false;
 					if (inLine.toUpperCase().indexOf("PARAM") >= 0 || inLine.toUpperCase().indexOf("VAR") >= 0)
 						hasParams = true;

					// loop on the details records
					String[] objs = {new String(), new String(), new String(), new String(), new String(), new String(), 
									 new String(), new String(), new String(), new String(), new String(), new String()};
								
					String oldDelim = EPSProperties.SDELIMITER;
					EPSProperties.SDELIMITER = EPSProperties.SSPACE_DELIMITER;
					
 					while (true) {
	 					// get the next line with values
	 					inLine = in.readLine();
	 					
	 					if (inLine == null)
	 						break;
	 						
	 					// get the individual items. Using EPS_Util.getItem because MessageFormat seems to allow only 10 items
 						int numCols = 12;
						objs[0] = EPS_Util.getItem(inLine, 1);
						objs[1] = EPS_Util.getItem(inLine, 2);
						objs[2] = EPS_Util.getItem(inLine, 3);
						objs[3] = EPS_Util.getItem(inLine, 4);
						objs[4] = EPS_Util.getItem(inLine, 5);
						objs[5] = EPS_Util.getItem(inLine, 6);
						objs[6] = EPS_Util.getItem(inLine, 7);
						objs[7] = EPS_Util.getItem(inLine, 8);
						objs[8] = EPS_Util.getItem(inLine, 9);
						objs[9] = EPS_Util.getItem(inLine, 10);
						if (hasParams) {
							numCols = 13;
							objs[10] = EPS_Util.getItem(inLine, 11);	// parameter list
							objs[11] = EPS_Util.getItem(inLine, 12);	// filename
							objs[12] = EPS_Util.getItem(inLine, 13);	// path
						}
						else {
							objs[10] = EPS_Util.getItem(inLine, 11);	// filename
							objs[11] = EPS_Util.getItem(inLine, 12);	// path
						}
						
						try {
							fileName = objs[10];
							path = objs[11];
							
							// put together the latitude
							Object[] objs2 = {new Double(0), new String()};
							MessageFormat msgf = new MessageFormat("{0,number}{1}");
							try {
								objs2 = msgf.parse(objs[1]);
								double deg = Double.valueOf((String)objs[0]).doubleValue();
								double min = Double.valueOf(objs2[0].toString()).doubleValue(); 
								String hemis = (String)objs2[1];
								lat = deg + min/60;
								if (hemis.equals("S"))
									lat = -lat;
							}
							catch (ParseException ex) {
								throw ex;
							}
							
							// put together the longitude
							msgf = new MessageFormat("{0,number}{1}");
							try {
								objs2 = msgf.parse(objs[3]);
								double deg = Double.valueOf((String)objs[2]).doubleValue();
								double min = Double.valueOf(objs2[0].toString()).doubleValue(); 
								String hemis = (String)objs2[1];
								lon = deg + min/60;
								if (hemis.equals("W"))
									lon = -lon;
							}
							catch (ParseException ex) {
								throw ex;
							}
							
							// depth
							z = Double.valueOf((String)objs[4]).doubleValue();
							
							try {
								// put together the start geodate (input is of the form 1976-11-01 1945)
								stDate = new GeoDate((String)objs[5] + " " + (String)objs[6], "yyyy-MM-dd HHmm");
								
								// put together the end geodate (input is of the form 1976-11-01 1945)
								endDate = new GeoDate((String)objs[7] + " " + (String)objs[8], "yyyy-MM-dd HHmm");
							}
							catch (Exception ex) {}
							
							// delta
							del = Double.valueOf((String)objs[9]).doubleValue();
							
							if (hasParams) {
								// paramList
								String paramListStr = (String)objs[10];
								
								// can this be turned into an array? TODO
								
								// filename
								fileName = (String)objs[11];
								
								// path
								path = (String)objs[12];
							}
							else {
								// filename
								fileName = (String)objs[10];
								
								// path
								path = (String)objs[11];
							}
														
							// create a pointer object and add to Vector
							EpicPtr eptsp = new EpicPtr(EPSConstants.NETCDFFORMAT, database, dataType, z, lat, lon, 
			                  stDate, endDate, del, paramList, fileName, path);
							filePtrs.add(eptsp);
							
						}
						catch (ParseException pe) {
						}
					}
					EPSProperties.SDELIMITER = oldDelim;
				}
			}
		}
		catch (IOException ex) {
			throw ex;
		}
		return filePtrs;
	}
	
	public boolean isArgo() {
		return false;
	}
}

package gov.noaa.pmel.eps2;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import gov.noaa.pmel.util.*;
/**
 * <code>ProfilePtrReader</code> a reader class for EPIC profile ptr files.
 *
 * @see EpicPtrFactory
 * @see PtrFileReader
 *
 * @author oz
 * @version 1.0
 */
 
public class ProfilePtrReader implements PtrFileReader, EPSConstants {
    /**
     * Epic ptr file to read.
     */
	protected File mFile;
	
    /**
     * Keys to use when sorting the file in place.
     */
	protected int[] mSortKeys = new int[4];
	
    /**
     * Flag indicates that Argo was found in the DATABASE field.
     */
	protected boolean mIsArgo = false;
	protected boolean mIgnoreArgo = true;
	
	private boolean DEBUG = false;
		
    /**
     * Construct a new <code>ProfilePtrReader</code> with a file reference and sortkeys.
     *
     * @param inFile Pointer file
     * @param inKeys Sort key array
     */
	public ProfilePtrReader(File inFile, int[] inKeys, boolean ignoreArgoFlag) {
		mFile = inFile;
		if (inKeys != null)
			setSortKeys(inKeys);
		mIgnoreArgo = ignoreArgoFlag;
	};
		
    /**
     * Construct a new <code>ProfilePtrReader</code> with a file reference and sortkeys.
     *
     * @param inFile Pointer file
     * @param inKeys Sort key array
     */
	public ProfilePtrReader(File inFile, int[] inKeys) {
		this(inFile, inKeys, true);
	}
	
    /**
     * Set the sort keys.
     *
     * @param inKeys Array of sort keys
     */
	public void setSortKeys(int[] inKeys) {
		for (int i=0; i<4; i++)
			mSortKeys[i] = inKeys[i];
	}
	
    /**
     * Parse the ptr file and create a collection of EpicPtrs.
     *
     * @exception IOException An IO error occurred reading the ptr file
     */
	public ArrayList parse() throws IOException {
		String dataType = new String();
		String database = new String();
		String cruise, prevCruise = "";
		String cast;
		String fileName;
		ArrayList paramList = null;
		String path;
		double lat = 0.0, prevLat = 0.0;
		double lon = 0.0, prevLon = 0.0;
		GeoDate date = null;
		GeoDate prevDate = null;
		double zi = 0.0, zf = 0.0;
		double prevzi = 0.0, prevzf = 0.0;
		String[] objs = {new String(), new String(), new String(), new String(), new String(), new String(), 
						 new String(), new String(), new String(), new String(), new String(), new String()};
 		int numCols = 9;
		int cols[] = new int[numCols];
		int widths[] = new int[numCols];
		boolean hasParams = false;
		int ptrCnt = 0;
		boolean simplePtrFile = true;
		ArrayList filePtrs = new ArrayList(100);
		boolean datFound = false;
		boolean cruiseFound = false;
		int currTrackStnCnt = 0;
		String ocean = "na";
		String dataCtr = "na";
		String dataMode = "na";
		String numLevels = "na";
		String timeQC = "na";
		String posQC = "na";
		String argoorgtspp = "Argo";
		String sparamList = "na";
		
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
					simplePtrFile = false;
 					// skip the next line with the column format string
 					inLine = in.readLine();
 					
 					// get the next line with values
 					inLine = in.readLine();
					String oldDelim = EPSProperties.SDELIMITER;
					EPSProperties.SDELIMITER = EPSProperties.SSPACE_DELIMITER;
					dataType = EPS_Util.getItem(inLine, 1);
					database = EPS_Util.getItem(inLine, 2);
					EPSProperties.SDELIMITER = oldDelim;
					datFound = true;
					if (database.equalsIgnoreCase("ARGO"))
						mIsArgo = true;
					continue;
				}
				
				// look for line with "CRUISE" in it--these are the column headers;
 				if (inLine.toUpperCase().indexOf("CRUISE") >= 0) {
 					numCols = 9;
					simplePtrFile = false;
 					// get the next line with the column format string
 					inLine = in.readLine();
 					
 					// determine whether the pointer has a parameter or variable list
 					hasParams = false;
 					if (inLine.toUpperCase().indexOf("PARAM") >= 0 || inLine.toUpperCase().indexOf("VAR") >= 0)
 						hasParams = true;
								
					String oldDelim = EPSProperties.SDELIMITER;
					EPSProperties.SDELIMITER = EPSProperties.SSPACE_DELIMITER;
					
 					// get the length of the columns
					objs[0] = EPS_Util.getItem(inLine, 1);	// cruise
					objs[1] = EPS_Util.getItem(inLine, 2);	// cast
					objs[2] = EPS_Util.getItem(inLine, 3);	// latitude
					objs[3] = EPS_Util.getItem(inLine, 4);	// longitude
					objs[4] = EPS_Util.getItem(inLine, 5);	// date
					objs[5] = EPS_Util.getItem(inLine, 6);	// zi
					objs[6] = EPS_Util.getItem(inLine, 7);	// zf
					if (hasParams) {
						numCols = 10;
						objs[7] = EPS_Util.getItem(inLine, 8);	// parameter list
						objs[8] = EPS_Util.getItem(inLine, 9);	// filename
						objs[9] = EPS_Util.getItem(inLine, 10);	// path
					}
					else {
						objs[7] = EPS_Util.getItem(inLine, 8);	// filename
						objs[8] = EPS_Util.getItem(inLine, 9);	// path
					}		
					
					cols = null;
					widths = null;
					cols = new int[numCols];
					widths = new int[numCols];

					for (int i=0; i<numCols; i++) {
						widths[i] = ((String)objs[i]).length();
						if (i == 0)
							cols[i] = 0;
						else
							cols[i] = cols[i-1] + widths[i-1] + 1;
					}
					EPSProperties.SDELIMITER = oldDelim;
					cruiseFound = true;
					continue;
				}
 					
 				if (!datFound || !cruiseFound)
 					continue;
 					
 				boolean pathIsRelative = false;

				// get the individual detail items.
				for (int i=0; i<numCols; i++) {
					if (i < numCols - 1)
						objs[i] = inLine.substring(cols[i], cols[i] + widths[i]);
					else
						// always read to the end of the line for path names
						objs[i] = inLine.substring(cols[i],  cols[i] + (inLine.length() - cols[i]));
				}
					
				try {
					cruise = (String)objs[0];
					cast = (String)objs[1];
					EPSProperties.SDELIMITER = EPSProperties.SSPACE_DELIMITER;

					// put together the latitude
					// this is kinda kludgy but I couldn't parse the string w/ MessageFormat w/o breaking up the
					// strings into degs and mins components. The conversion to degs always threw even when it seemd to have a 
					// valid string
					Object[] objs2 = {new Double(0), new String()};
					MessageFormat msgf = new MessageFormat("{0,number}{1}");
					try {
						String degStr = EPS_Util.getItem(objs[2], 1);
						String minStr = EPS_Util.getItem(objs[2], 2);
						degStr = EPS_Util.trimPreceedingWhiteSpace(degStr);
						objs2 = msgf.parse(minStr);
						double deg = Integer.valueOf(degStr).intValue(); 
						double min = 0;
						String hemis = null;
						try {
							min = Double.valueOf(objs2[0].toString()).doubleValue();
							hemis = (String)objs2[1];
						}
						catch (Exception ex) {
							String sMin = minStr.substring(0, minStr.length() - 1);
							hemis = minStr.substring(minStr.length() - 1, minStr.length());
							min = Double.valueOf(sMin).doubleValue();
						}
						lat = deg + min/60;
						if (hemis.equals("S"))
							lat = -lat;
					}
					catch (ParseException ex) {
						throw ex;
					}
					
					// put together the longitude
					try {
						String degStr = EPS_Util.getItem(objs[3], 1);
						String minStr = EPS_Util.getItem(objs[3], 2);
						degStr = EPS_Util.trimPreceedingWhiteSpace(degStr);
						objs2 = msgf.parse(minStr);
						double deg = Integer.valueOf(degStr).intValue(); 
						double min = 0;
						String hemis = null;
						try {
							min = Double.valueOf(objs2[0].toString()).doubleValue();
							hemis = (String)objs2[1];
						}
						catch (Exception ex) {
							String sMin = minStr.substring(0, minStr.length() - 1);
							hemis = minStr.substring(minStr.length() - 1, minStr.length());
							min = Double.valueOf(sMin).doubleValue();
						}
						hemis = (String)objs2[1];
						lon = deg + min/60;
						if (hemis.equals("W"))
							lon = -lon;
					}
					catch (ParseException ex) {
						throw ex;
					}
					
					// put together the geodate (input is of the form 1976-11-01 1945)
					try {
						date = new GeoDate((String)objs[4], "yyyy-MM-dd HHmm");
					}
					catch (Exception ex) {}
					
					try {
						// min depth
						zi = Double.valueOf((String)objs[5]).doubleValue();
					}
					catch (Exception ez1) {
						zi = -99;
					}
					
					try {
						// max depth
						zf = Double.valueOf((String)objs[6]).doubleValue();
					}
					catch (Exception ez2) {
						zf = -99;
					}
					
					if (hasParams) {
						// paramList
						String paramListStr = (String)objs[7];
						
						// filename
						fileName = (String)objs[8];
						
						// path
						path = (String)objs[9];
					}
					else {
						// filename
						fileName = (String)objs[7];
						
						// path
						path = (String)objs[8];
					}
						
					if (path.startsWith("./"))
						pathIsRelative = true;
					else if (path.startsWith("\\"))
						pathIsRelative = true;
						
					// the format of these files is assumed to be netCDF
					// create a pointer object and add to Vector
					
					if (!mIsArgo || mIgnoreArgo) {
						EpicPtr eppf = new EpicPtr(EPSConstants.NETCDFFORMAT, database, dataType, cruise, cast, lat, lon, 
		                  date, zi, zf, paramList, fileName, path, null);
		                 eppf.setIsRelativePath(pathIsRelative);
		                 
						filePtrs.add(eppf);
					}
					else {		
						if (!cruise.equalsIgnoreCase(prevCruise)) {
							if (currTrackStnCnt == 0) {
								// first record of a first track
								currTrackStnCnt++;
								prevLat = lat;
								prevLon = lon;
								prevzi = zi;
								prevzf = zf;
								prevDate = date;
								prevCruise = cruise;
								
								// write a pointer for a stn that is just a point
								EpicPtr eppf = new EpicPtr(
									EPSConstants.PROFILEPTRS, 
									argoorgtspp, 
									"CTD", 
									cruise, 
									cast, 
									lat, 
									lat,
									lon,
									lon,
				                  	date, 
				                  	date, 
				                  	zi, 
				                  	zf,
				                  	fileName,
				                  	ocean,
				                  	posQC,
				                  	timeQC,
				                  	dataCtr,
				                  	dataMode,
				                  	numLevels,
				                  	"na",
				                  	sparamList, 
				                  	path);
								if (DEBUG) 
									System.out.println("start " + eppf);
		                 		eppf.setIsRelativePath(pathIsRelative);
								filePtrs.add(eppf);
								ptrCnt++;
								continue;
							}
							else {
								// first record of new track
								EpicPtr eppf = new EpicPtr(
									EPSConstants.PROFILEPTRS, 
									argoorgtspp, 
									"CTD", 
									cruise, 
									cast, 
									lat, 
									lat,
									lon,
									lon,
				                  	date, 
				                  	date, 
				                  	zi, 
				                  	zf,
				                  	fileName,
				                  	ocean,
				                  	posQC,
				                  	timeQC,
				                  	dataCtr,
				                  	dataMode,
				                  	numLevels,
				                  	"na",
				                  	sparamList, 
				                  	path);
								if (DEBUG) 
									System.out.println("start " + eppf);
		                 		eppf.setIsRelativePath(pathIsRelative);
								filePtrs.add(eppf);
								ptrCnt++;
								currTrackStnCnt = 1;
								prevLat = lat;
								prevLon = lon;
								prevzi = zi;
								prevzf = zf;
								prevDate = date;
								prevCruise = cruise;
							}
						}
						else {
							// part of an existing track
							EpicPtr eppf = new EpicPtr(
								EPSConstants.PROFILEPTRS, 
								argoorgtspp, 
								"CTD", 
								cruise, 
								cast, 
								lat, 
								prevLat,
								lon,
								prevLon,
			                  	date, 
			                  	prevDate, 
			                  	zi, 
			                  	zf,
			                  	fileName,
			                  	ocean,
			                  	posQC,
			                  	timeQC,
			                  	dataCtr,
			                  	dataMode,
			                  	numLevels,
			                  	"na",
			                  	sparamList, 
			                  	path);
							filePtrs.add(eppf);
		                 	eppf.setIsRelativePath(pathIsRelative);
							if (DEBUG) 
								System.out.println("mid " + eppf);
							ptrCnt++;
							currTrackStnCnt++;
							prevLat = lat;
							prevLon = lon;
							prevzi = zi;
							prevzf = zf;
							prevDate = date;
							prevCruise = cruise;
						}
					}
					ptrCnt++;
				}
				catch (ParseException pe) {}
			}
			
			in.close();
						
			if (simplePtrFile) {
				fr = new FileReader(mFile);
			    in = new LineNumberReader(fr, 10000);
				// ptr file is just a list of pathnames
				// loop on the details records
				while (true) {
 					// get the next line with values
 					String inLine = in.readLine();		

 					if (inLine == null)
 						break;
 						
 					// get the path and file name
 					File filePath = new File(inLine);
 					String filename = filePath.getName();
 					String fpath = filePath.getParent();
 						
					// the format of these files is assumed to be netCDF
					// create a pointer object and add to Vector
					EpicPtr eppf = new EpicPtr(EPSConstants.NETCDFFORMAT, "unk", "UNK", "UNK", "UNK", -99, -99, 
	                  new GeoDate(),  -99,  -99, paramList, filename, fpath, null);
					filePtrs.add(eppf);
						
				}  //while
			}
		}
		catch (IOException ex) {
			System.out.println("throwing here");
			throw ex;
		}
		return filePtrs;
	}
	
	public boolean isArgo() {
		return mIsArgo;
	}
}

package gov.noaa.pmel.eps2;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import gov.noaa.pmel.util.*;
/**
 * <code>GTSPPInvFileReader</code> a reader class for an GTSPP inventory file.
 *
 * @see EpicPtrFactory
 * @see PtrFileReader
 *
 * @author oz
 * @version 1.0
 */
 
public class GTSPPInvFileReader implements PtrFileReader, EPSConstants {
    /**
     * Epic ptr file to read.
     */
	protected File mFile;
	private boolean DEBUG = false;
	private boolean DEBUG2 = false;
	private boolean DEBUG3 = false;
	private String argoorgtspp = "NODC GTSPP";
	
    /**
     * Keys to use when sorting the file in place.
     */
	protected int[] mSortKeys = new int[4];
		
    /**
     * Construct a new <code>GTSPPInvFileReader</code> with a file reference and sortkeys.
     *
     * @param inFile Pointer file
     * @param inKeys Sort key array
     */
	public GTSPPInvFileReader(File inFile, int[] inKeys) {
		mFile = inFile;
		if (inKeys != null)
			setSortKeys(inKeys);
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
		String cruise = "";
		String file = null;
		String ocean = null;
		String cast;
		String fileName;
		String paramList;
		String path;
		double lat = 0.0;
		double lon = 0.0;
		GeoDate date = null;
		GeoDate prevDate = null;
		double zi = 0.0, zf = 0.0;
		double prevzi = 0.0;
		String[] objs = {new String(), new String(), new String(), new String(), new String(), new String(), new String(),
						 new String(), new String(), new String(), new String(), new String(), new String(), new String(), 
						 new String(), new String(), new String(), new String(), new String(), new String(), new String()};
		int ptrCnt = 0;
		ArrayList filePtrs = new ArrayList(100);
		EPSProperties.SDELIMITER = EPSProperties.SCOMMA_DELIMITER;
		int currTrackStnCnt = 0;
		
		double minLat = 90;
		double maxLat = -90;
		double minLon = 180;
		double maxLon = -180;
		String prevLine = null;
		String inLine = null;
		int offset = 0;
		String URL = null;
		String prevURL = null;
		String dataCtr = null;
		String dataMode = null;
		String numLevels = null;
		String timeQC = null;
		String posQC = null;
		//System.out.println("in GTSPP Parse");
	    try {
			FileReader fr = new FileReader(mFile);
		    LineNumberReader in = new LineNumberReader(fr, 10000);
		    
		    // skip the first line
		    inLine = in.readLine();
		    
			while (true) {
				inLine = in.readLine();
				
				if (DEBUG) 
					System.out.println("inLine = " + inLine);
				
				if (inLine == null)
					// all done
					break;
				
				if (inLine.length() == 0)
					continue;
					
				/* GTSPP fields
					callSign
					data_URL
					file
					ocean
					date
					time
					time_qc
					latitude
					longitude
					position_qc
					data_center
					data_mode
					num_of_levels
					min_D_P
					max_D_P
					num_of_param
					param1
					param2
					param3
					param4
					param5 */
				
 				// get the details
 				objs[0] = EPS_Util.getItem(inLine, 1);		// cruise
 				URL = EPS_Util.getItem(inLine, 2);			// URL
				objs[1] = EPS_Util.getItem(inLine, 3);		// file
				objs[2] = EPS_Util.getItem(inLine, 4);		// ocean
				objs[3] = EPS_Util.getItem(inLine, 5);		// date
				objs[4] = EPS_Util.getItem(inLine, 6);		// time
				objs[5] = EPS_Util.getItem(inLine, 7);		// time_qc
				objs[6] = EPS_Util.getItem(inLine, 8);		// latitude
				objs[7] = EPS_Util.getItem(inLine, 9);		// longitude
				objs[8] = EPS_Util.getItem(inLine, 10);		// position_qc
				objs[9] = EPS_Util.getItem(inLine, 11);		// data_center
				objs[10] = EPS_Util.getItem(inLine, 12);	// data_mode
				objs[11] = EPS_Util.getItem(inLine, 13);	// num_of_levels
				objs[12] = EPS_Util.getItem(inLine, 14);	// min_D_P
				objs[13] = EPS_Util.getItem(inLine, 15);	// max_D_P
				objs[14] = EPS_Util.getItem(inLine, 16);	// num_of_param
				
				if (DEBUG) {
					for (int i=0; i<15; i++) {
						System.out.println(i + " " + (String)objs[i] + "\t");
					}
				}
				
				int numParams = Integer.valueOf(objs[14]).intValue();
				
				paramList = "";
				for (int i=0; i<numParams; i++) {
					// concat paramList
					paramList += (String)EPS_Util.getItem(inLine, 17 + i);
					if (i < numParams - 1)
						paramList += ",";
				}
					
				try {
					cruise = (String)objs[0];
					file = (String)objs[1];
					ocean = (String)objs[2];
					
					// put together the geodate (input is of the form 1976-11-01 1945)
					try {
						String dateTime = (String)objs[3] + " " + (String)objs[4];
						if (DEBUG) 
							System.out.println("dateTime = " + dateTime);
						date = new GeoDate(dateTime, "yyyy-MM-dd HH:mm");
					}
					catch (Exception ex) {
						System.out.println("throw #1");
						ex.printStackTrace();
					}
					timeQC  = (String)objs[5];
					int itimeQC = Integer.valueOf(timeQC).intValue();
					
					lat = Double.valueOf((String)objs[6]).doubleValue();
					lon = Double.valueOf((String)objs[7]).doubleValue();
					
					if (DEBUG) 
						System.out.println(cruise + " lat = " + lat + " lon = " + lon);
						
					posQC  = (String)objs[8];
					int iposQC = Integer.valueOf(posQC).intValue();
					
					// test QC flags
					if (itimeQC != 1 || iposQC != 1) {
						if (DEBUG3)
							System.out.println(inLine);
						continue;
					}
					
					// auxillary fields
					dataCtr = (String)objs[9];
					dataMode = (String)objs[10];
					numLevels = (String)objs[11];
					
					// min depth
					zi = Double.valueOf((String)objs[12]).doubleValue();
					
					// max depth
					zf = Double.valueOf((String)objs[13]).doubleValue();
					
				}
				catch (Exception ex) {
					System.out.println("throw #2");
					ex.printStackTrace();
				}
				
				// write a pointer for a stn that is just a point
				cast = String.valueOf(++currTrackStnCnt);
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
                  	file,
                  	ocean,
                  	posQC,
                  	timeQC,
                  	dataCtr,
                  	dataMode,
                  	numLevels,
                  	(String)objs[14],
                  	paramList, 
                  	URL);
				if (DEBUG2) 
					System.out.println("start " + eppf);
				filePtrs.add(eppf);
				ptrCnt++;
				continue;
			}
			
			// close the input file
			in.close();
		}
		catch (IOException ex) {
			System.out.println("throwing in GTSPP Inventory Reader");
			throw ex;
		}
		return filePtrs;
	}
	
	public boolean isArgo() {
		return false;
	}
}

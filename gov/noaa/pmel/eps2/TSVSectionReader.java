package gov.noaa.pmel.eps2;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.awt.*;
import java.util.*;
import ucar.netcdf.*;
import ucar.multiarray.*;
import gov.noaa.pmel.util.*;

/**
 * <code>TSVSectionReader</code> 
 * Concrete implementation of the EPSFileReader interface to read, parse, and save a 
 * tab-separated (spreadsheet) format data file.
 *
 * @author oz
 * @version 1.0
 *
 * @see EPSFileReader
 */
public class TSVSectionReader implements EPSFileReader, EPSConstants {
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
	protected File mFile;;
	/**
	* Optional string for progress string
	*/
	protected String mProgressStr = "Reading Spreadsheet(TSV) Data...";
	
  	/**
   	* Construct a new <code>TSVSectionReader</code> with a Dbase amd file.
   	*
   	* @param dname Dbase that this reader will fill in
   	* @param inFile Source section data file
   	*
 	* @see Dbase
   	*/
	public TSVSectionReader(gov.noaa.pmel.eps2.Dbase dname, File inFile, EpicPtr ep) {
		mOwnerDBase = dname;
		mFile = inFile;
		mOwnerDBase.setEpicPtr(ep);
		if (ep.getProgressStr() != null)
			mProgressStr = new String(ep.getProgressStr());
	}
	
  	/**
   	* Construct a new <code>TSVSectionReader</code> with a Dbase amd file and a prompt string.
   	*
   	* @param dname Dbase that this reader will fill in
   	* @param inFile Source section data file
   	*
 	* @see Dbase
   	*/
	public TSVSectionReader(Dbase dname, File inFile, EpicPtr ep, String progress) {
		mOwnerDBase = dname;
		mFile = inFile;
		mProgressStr = progress;
		mOwnerDBase.setEpicPtr(ep);
	}
	
	// concrete implementations of the io routines
  	/**
   	* Parse the section file and fill in the Dbase.
   	*
   	* @return Success code
   	*/
	public void parse() throws Exception {
		JOAParameter tempProperties[] = new JOAParameter[100];
		boolean[] hasQC = new boolean[100];
		String inLine = new String();
		LineNumberReader in = null;
		long bytesInFile = mFile.length();
		long bytesRead = 0;
		int presPos = 0;
		
		EPSProgressDialog mProgress = new EPSProgressDialog(new Frame(), mProgressStr, Color.blue);
		mProgress.setVisible(true);	
		
    	// Get an epic key database specific to JOA
    	EPIC_Key_DB mEpicKeyDB = new EPIC_Key_DB("joa_epic.key");
    	
    	// Get an epic key database specific to JOA
    	EPIC_Key_DB mOrigEpicKeyDB = new EPIC_Key_DB("epic.key");
    	
    	// create a vector for temporary storage of the dbases
    	Vector dBases = new Vector(100);
		
		try {
		    in = new LineNumberReader(new FileReader(mFile), 10000);
		    
	    	// create a new open file object
	    	JOADataFile of = new JOADataFile("Untitled");
	    	
		    // read the column header line
		    inLine = in.readLine();
		    bytesRead += inLine.length();
		    
		    // convert string to uppercase
		    String inLineLC = new String(inLine);
		    inLine = inLine.toUpperCase();
		    
		    // find the columns of important stuff
		    EPSProperties.SDELIMITER = STAB_DELIMITER;
		    String[] secStrings = {"SEC", "SE", "WHP-ID", "PROJECT", "ID", "NAME", "EXP"};
		    int[] secStrictness = {STARTSWITH, MATCHES, MATCHES, MATCHES, MATCHES, MATCHES, MATCHES};
		    int secNamePos = EPS_Util.getItemNumber(inLine, secStrings, secStrictness);
		    
		    String[] shipStrings = {"SHIP", "SHP", "SH", "VESSEL", "SHIP"};
		    int[] shipStrictness = {MATCHES, MATCHES, MATCHES, MATCHES, STARTSWITH};
		    int shipPos = EPS_Util.getItemNumber(inLine, shipStrings, shipStrictness);
		    
		    String[] stnStrings = {"STA", "STATION", "ST", "STN",};
		    int [] stnStrictness = {MATCHES, MATCHES, MATCHES, STARTSWITH};
		    int stnNumPos = EPS_Util.getItemNumber(inLine, stnStrings, stnStrictness);
		    
		    String[] castStrings = {"CA", "CAST", "CST"};
		    int [] castStrictness = {MATCHES, STARTSWITH, STARTSWITH};
		    int castNumPos = EPS_Util.getItemNumber(inLine, castStrings, castStrictness);
		    
		    String[] latStrings = {"LAT", "LA"};
		    int [] latStrictness = {STARTSWITH, MATCHES};
		    int latPos = EPS_Util.getItemNumber(inLine, latStrings, castStrictness);
		    
		    String[] lonStrings = {"LON", "LO"};
		    int [] lonStrictness = {STARTSWITH, MATCHES};
		    int lonPos = EPS_Util.getItemNumber(inLine, lonStrings, lonStrictness);
		    
		    String[] dateStrings = {"DA", "DAT", "DATE", "DATE"};
		    int [] dateStrictness = {MATCHES, MATCHES, CONTAINS, MATCHES};
		    int datePos = EPS_Util.getItemNumber(inLine, dateStrings, dateStrictness);
		    
		    String[] bottStrings = {"BOTTOM", "WATER DEPTH", "ZBOT"};
		    int [] bottStrictness = {STARTSWITH, MATCHES, MATCHES};
		    int bottomPos = EPS_Util.getItemNumber(inLine, bottStrings, bottStrictness);

		    String[] presStrings = {"PRES", "P", "PR", "CTDP"};
		    int [] presStrictness = {STARTSWITH, MATCHES, MATCHES, MATCHES};
		    int paramStartPos = EPS_Util.getItemNumber(inLine, presStrings, presStrictness);
		    
		    // test for missing required stuff
		    if ((stnNumPos == -1 && castNumPos == -1) || latPos == -1 || lonPos == -1 || paramStartPos == -1) {
		    	// throw an exception
				FileImportException fie = new FileImportException();
				throw(fie);
		    }
 	
		    // get the individual parameters
		    int ic = paramStartPos;
		    int numVars = 0;
		    while (true) {
		    	hasQC[numVars] = false;
		    	String param = EPS_Util.getItem(inLineLC, ic);
		    	if (param == null || param.length() == 0) {
		    		break;
		    	}

				// test if this is a QC Flag
				if (param.indexOf("FLAG") >= 0) {
					// Quality Code
					hasQC[numVars-1] = true;
				}
				else {
					// Parameter
			    	// break this into param name and units
			    	// store the current delimiter
			    	String oldDelim = EPSProperties.SDELIMITER;
			    	
			    	String units = new String("");
			    	if (param.indexOf(' ') >= 0) {
			    		// number of items
			    		EPSProperties.SDELIMITER = SSPACE_DELIMITER;
			    		int numItems = EPS_Util.numItems(param) + 1;
			    		if (numItems > 2) {
			    			int start = param.indexOf(' ');
			    			int send = param.length();
			    			units = param.substring(start);
				    	}
				    	else {
			    			units = EPS_Util.getItem(param, 2);
			    		}
			    	}
			    	
			    	if (units != null) {
			    		if (units.endsWith(" "))
			    			units = units.substring(0, units.length()-1);
			    		
			    		// param is always first item
			    		param = EPS_Util.getItem(param, 1);
			    		
			    		// trim any preceeding blanks from units
			    		while (units.startsWith(" ")) {
			    			String test = new String(units);
			    			units = test.substring(1, test.length());
			    		}
			    	}
			    	else
			    		units = new String(" ");
			    	
			    	EPSProperties.SDELIMITER = oldDelim;
			    	
					// convert varnames to UC 
					param.toUpperCase();
					
					// create new property
					boolean reverse = false;
					if (param.endsWith(":R") || param.startsWith("PRES")) {
						reverse = true;
						param = param.substring(0, 4);
					}
					else
						reverse = false;
					
					tempProperties[numVars] = new JOAParameter(param, units);
					tempProperties[numVars].mReverseY = reverse;
					if (param.equalsIgnoreCase("PRES")) {
						presPos = numVars;
					}
		    		numVars++;
				}
		    	ic++;
		    }
		    
		    // main loop
			int bc = 0;						// number of bottles read
			int s = 0;						// number of sections read
			String oldSecDescrip = new String("");
			String oldStn = new String();
			String stnNum;
			int castNum = 1;
			double myLat = 0.0;
			double myLon = 0.0;
			int mTotalStations = 0;
			boolean newSection = false;
			short[] sarray = new short[1];
			String sectionDescrip = null;
			double bottomdbar = MISSINGVALUE;
	    	int year = MISSINGVALUE;
	    	int month = MISSINGVALUE;
	    	int day = MISSINGVALUE;
	    	int hour = MISSINGVALUE;
	    	double min = MISSINGVALUE;
			String shipCode = null;
			
			// create an array of arrays to store the data
			double[][] va = new double[numVars][500];
			short[][] qc = new short[numVars][500];
		    
		    // skip to data and intialize stn boundaries
		    while (true) {
				inLine = in.readLine();
		    	bytesRead += inLine.length();
				if (inLine.length() > 0) 
					break;
					
		    }
		    
		    // initialize old station from station number if
		    // it exists in the file--otherwise use the cast number
		    if (stnNumPos != -1)
				oldStn = EPS_Util.getItem(inLine, stnNumPos);
			else
				oldStn = EPS_Util.getItem(inLine, castNumPos);
			
			boolean eof = false;
			boolean first = true;
			while (!eof) {
				mProgress.setPercentComplete(100.0 * ((double)bytesRead/(double)bytesInFile));
				
		    	// need to read all the bottle data for this stn
		    	while (true) {
			    	// read the station number
				    // it exists in the file--otherwise use the cast number
				    if (stnNumPos != -1)
						stnNum = EPS_Util.getItem(inLine, stnNumPos);
					else
						stnNum = EPS_Util.getItem(inLine, castNumPos);
			    	
		    		if (stnNum == null || stnNum.length() == 0) {
		    			eof = true;
						newSection = true;
		    			break;
		    		}
		    		else if (!stnNum.equalsIgnoreCase(oldStn)) {
		    			newSection = true;
		    			break;
		    		}
		    		
		    		bytesRead += inLine.length();
					// section name
					if (secNamePos != -1)
						sectionDescrip = EPS_Util.getItem(inLine, secNamePos);
					else
						sectionDescrip = new String("Untitled");
						
					// ship code
					shipCode = null;
					if (shipPos != -1)
						shipCode = EPS_Util.getItem(inLine, shipPos);
					else
						shipCode = new String("  ");
			    	
			    	// cast number
			    	if (castNumPos != -1)
			    		castNum = EPS_Util.getIntItem(inLine, castNumPos);
			    	else
			    		castNum = 1;
			    	
			    	// lat
			    	myLat = EPS_Util.getDoubleItem(inLine, latPos);
			    	
			    	// lon
			    	myLon = EPS_Util.getDoubleItem(inLine, lonPos);
			    			    	
			    	// date
			    	year = MISSINGVALUE;
			    	month = MISSINGVALUE;
			    	day = MISSINGVALUE;
			    	hour = MISSINGVALUE;
			    	min = MISSINGVALUE;
			    	if (datePos != -1) {
				    	String sDate = EPS_Util.getItem(inLine, datePos);
				    	String sTime = null;
				    	 
				    	// parse the date
				    	// store the current delimiter
				    	String oldDelim = EPSProperties.SDELIMITER;
				    	
				    	// isolate the date if time is present
				    	if (sDate.indexOf(' ') >= 0) {
				    		EPSProperties.SDELIMITER = SSPACE_DELIMITER;
				    		sTime = EPS_Util.getItem(sDate, 2);
				    		sDate = EPS_Util.getItem(sDate, 1);
				    	}
				    		
				    	// get the date delimiter
				    	if (sDate.indexOf('/') >= 0)
				    		EPSProperties.SDELIMITER = SSLASH_DELIMITER;
				    	else if (sDate.indexOf('-') >= 0)
				    		EPSProperties.SDELIMITER = SHYPHEN_DELIMITER;
				    	
				    	// get the day
				    	String sDay = EPS_Util.getItem(sDate, 1);
				    	day = Integer.valueOf(sDay).intValue();
				    		
				    	// get the month
				    	String sMonth = EPS_Util.getItem(sDate, 2);
				    	month = Integer.valueOf(sMonth).intValue();
				    	
				    	// get the year
				    	String sYear = EPS_Util.getItem(sDate, 3);
				    	year = Integer.valueOf(sYear).intValue();
				    	
				    	// test for a time string
				    	if (sTime != null) {
					    	// parse the time
					    	EPSProperties.SDELIMITER = SCOLON_DELIMITER;
					    	String sHour = EPS_Util.getItem(sTime, 1);
					    	String sMin = EPS_Util.getItem(sTime, 2);
					    	hour = Integer.valueOf(sHour).intValue();
					    	min = Double.valueOf(sMin).doubleValue();
					    }
					    else {
					    	hour = 0;
					    	min = 0;
					    }
				    	
				    	// restore the delimiter
				    	EPSProperties.SDELIMITER = oldDelim;
				    }
			    	
					// read bottom
					bottomdbar = MISSINGVALUE;
					if (bottomPos != -1)
					 	bottomdbar = EPS_Util.getDoubleItem(inLine, bottomPos);
					 	
					int p = 0;
					for (int v=0; v<numVars; v++) {
						double varVal = EPS_Util.getDoubleItem(inLine, paramStartPos + p);
	
						// store the value in the multidimensional array
						va[v][bc] = varVal;
						if (varVal != EPS_Util.MISSINGVALUE) {
							va[v][bc] = varVal;
						}
						else
							va[v][bc] = EPS_Util.MISSINGVALUE;
							
						if (hasQC[v]) {
							// read a QC
							short qcVal = EPS_Util.getShortItem(inLine, paramStartPos + p + 1);
							if (qcVal != EPS_Util.MISSINGVALUE) {
								qc[v][bc] = qcVal;
							}
							else
								qc[v][bc] = EPS_Util.MISSINGVALUE;
							p += 2;
						}
						else
							p++;
					} // for v
					bc++;

					// read a new line
					inLine = in.readLine();
					//System.out.println(inLine);
					if (inLine == null) {
						eof = true;
						newSection = true;
						break;
					}
					else {
					}
				}
		    	
		    	if (newSection) {
					// new Station found--make a new dBase
					mTotalStations++;
					
				    // make a DBase object
				    Dbase db = new Dbase();

				    // add the global attributes
				    db.addEPSAttribute("CRUISE", EPSConstants.EPCHAR, sectionDescrip.length(), sectionDescrip);
				    db.addEPSAttribute("CAST", EPSConstants.EPCHAR, oldStn.length(), oldStn);
				    sarray[0] = (short)castNum;
				    db.addEPSAttribute("CAST_NUMBER", EPSConstants.EPSHORT, 1, sarray);
				    sarray[0] = (short)bottomdbar;
				    db.addEPSAttribute("WATER_DEPTH", EPSConstants.EPSHORT, 1, sarray);
				    db.addEPSAttribute("DATA_ORIGIN", EPSConstants.EPCHAR, shipCode.length(), shipCode);
				    String dType = "TSV BOTTLE";
				    db.addEPSAttribute("DATA_TYPE", EPSConstants.EPCHAR, dType.length(), dType);
				    db.setDataType("TSV BOTTLE");
				    				    
				    // make the axes and axes variable
				    // create the axes time = 0, depth = 1, lat = 2, lon = 3
				    Axis timeAxis = new Axis();
				    Axis zAxis = new Axis();
				    Axis latAxis = new Axis();
				    Axis lonAxis = new Axis();
				    
				    // time axis
				    timeAxis.setName("time");
				    timeAxis.setTime(true);
				    timeAxis.setUnlimited(false);
				    timeAxis.setAxisType(EPSConstants.EPTAXIS);
					timeAxis.setLen(1);
					
					double mins = 0;
					if (min != EPS_Util.MISSINGVALUE)
						mins = min;
					
					try {
						// make the time axis units	
						String date = "days since ";
						int imin = (int)min;
						double fmin = min - imin;
						int secs = (int)(fmin * 60.0);
						double fsec = (fmin * 60.0) - secs;
						int msec = (int)(fsec * 1000.0);
						String fs = String.valueOf(fsec);
						fs = fs.substring(fs.indexOf(".")+1, fs.length()).trim();
						int f = 0;
						if (fs != null && fs.length() > 0)
							f = Integer.valueOf(fs).intValue();
					    GeoDate[] ta = {new GeoDate(month, day, year, hour, imin, secs, msec)};
						
	    		    	//sprintf(time_string,"%04d-%02d-%02d %02d:%02d:%02d.%03d",yr,mon,day,hr,min,sec,f); 
						String frmt = new String("{0,number,####}-{1,number,00}-{2,number,00} {3,number,00}:{4,number,00}:{5,number,00}.{6,number,000}"); 
						MessageFormat msgf = new MessageFormat(frmt);
						
						Object[] objs = {new Integer(year), new Integer(month), new Integer(day), new Integer(hour),
										 new Integer(imin), new Integer(secs), new Integer(f)};
						StringBuffer out = new StringBuffer();
						msgf.format(objs, out, null);
						String time_string = new String(out);
						date = date + time_string;
					    timeAxis.addAttribute(0, "units", EPCHAR, date.length(), date);
					    timeAxis.setUnits(date);
					    MultiArray tma = new ArrayMultiArray(ta);
					    timeAxis.setData(tma);
					    db.setAxis(timeAxis);
					    
					    // add the time axes variable
					    EPSVariable var = new EPSVariable();
					    var.setOname("time");
					    var.setDtype(EPDOUBLE);
					    var.setVclass(Double.TYPE);
					    var.addAttribute(0, "units", EPCHAR, date.length(), date);
					    var.setUnits(date);
					    double[] vta = {0.0};
					    MultiArray vtma = new ArrayMultiArray(vta);
					    try {
					    	var.setData(vtma);
					    }
					    catch (Exception ex) {}
					    db.addEPSVariable(var);
					}
					catch (Exception ex) {
						// no time or error in time 
					    timeAxis.addAttribute(0, "units", EPCHAR, 23, "days since 00-0-0 00:00");
					    timeAxis.setUnits("days since 01-01-01 01:01");
					    GeoDate[] ta = {new GeoDate(1, 1, 1, 1, 1, 1, 1)};
					    MultiArray tma = new ArrayMultiArray(ta);
					    timeAxis.setData(tma);
					    db.setAxis(timeAxis);
					    
					    // add the time axes variable
					    EPSVariable var = new EPSVariable();
					    var.setOname("time");
					    var.setDtype(EPDOUBLE);
					    var.setVclass(Double.TYPE);
					    var.addAttribute(0, "units", EPCHAR, 23, "days since 00-0-0 00:00");
					    var.setUnits("days since 00-0-0 00:00");
					    double[] vta = {0.0};
					    MultiArray vtma = new ArrayMultiArray(vta);
					    try {
					    	var.setData(vtma);
					    }
					    catch (Exception exx) {}
					    db.addEPSVariable(var);
					}
				    
				    // z axis
				    zAxis.setName("depth");
				    zAxis.setTime(false);
				    zAxis.setUnlimited(false);
					zAxis.setLen(bc);
				    zAxis.setAxisType(EPZAXIS);
				    zAxis.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.1");
				    zAxis.addAttribute(1, "units", EPCHAR, 4, "dbar");
				    zAxis.setUnits("dbar");
				    zAxis.setFrmt("f10.1");
				    //zAxis.addAttribute(2, "type", EPCHAR, 0, "");
				    sarray[0] = 1;
				    zAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
				    double[] za = new double[bc];
				    for (int b=0; b<bc; b++) {
		    			za[b] = va[presPos][b];
		    		}
				    MultiArray zma = new ArrayMultiArray(za);
				    zAxis.setData(zma);
				    db.setAxis(zAxis);
				    				    
				    // add the z axes variables
				    EPSVariable var = new EPSVariable();
				    var.setOname("depth");
				    var.setDtype(EPDOUBLE);
				    var.setVclass(Double.TYPE);
				    var.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.1");
				    var.addAttribute(1, "units", EPCHAR, 4, "dbar");
				    var.setUnits("dbar");
				    var.setFrmt("f10.1");
				    //var.addAttribute(2, "type", EPCHAR, 0, "");
				    sarray[0] = 1;
				    var.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
				    MultiArray zvma = new ArrayMultiArray(za);
				    try {
				    	var.setData(zvma);
				    }
				    catch (Exception ex) {}
				    db.addEPSVariable(var);
				    
				    // lat axis
				    latAxis.setName("latitude");
				    latAxis.setTime(false);
				    latAxis.setUnlimited(false);
					latAxis.setLen(1);
				    latAxis.setAxisType(EPYAXIS);
				    latAxis.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.4");
				    latAxis.addAttribute(1, "units", EPCHAR, 7, "degrees");
				    latAxis.setUnits("degrees");
				    latAxis.setFrmt("f10.4");
				    //latAxis.addAttribute(2, "type", EPCHAR, 0, "");
				    sarray[0] = 500;
				    latAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
				    double[] la = {myLat};
				    MultiArray lma = new ArrayMultiArray(la);
				    latAxis.setData(lma);
				    db.setAxis(latAxis);
				    
				    // add the y axes variable
				    var = new EPSVariable();
				    var.setOname("latitude");
				    var.setDtype(EPDOUBLE);
				    var.setVclass(Double.TYPE);
				    var.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.4");
				    var.addAttribute(1, "units", EPCHAR, 7, "degrees");
				    var.setUnits("degrees");
				    var.setFrmt("f10.4");
				    //var.addAttribute(2, "type", EPCHAR, 0, "");
				    sarray[0] = 500;
				    var.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
				    MultiArray yvma = new ArrayMultiArray(la);
				    try {
				    	var.setData(yvma);
				    }
				    catch (Exception ex) {ex.printStackTrace();}
				    db.addEPSVariable(var);
				    
				    // lon axis
				    lonAxis.setName("longitude");
				    lonAxis.setTime(false);
				    lonAxis.setUnlimited(false);
					lonAxis.setLen(1);
				    lonAxis.setAxisType(EPXAXIS);
				    lonAxis.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.4");
				    lonAxis.addAttribute(1, "units", EPCHAR, 7, "degrees");
				    lonAxis.setUnits("degrees");
				    lonAxis.setFrmt("f10.4");
				    //lonAxis.addAttribute(2, "type", EPCHAR, 0, "");
				    sarray[0] = 502;
				    lonAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
				    double[] lla = {myLon};
				    lma = new ArrayMultiArray(lla);
				    lonAxis.setData(lma);
				    db.setAxis(lonAxis);
				    
				    // add the x axes variable
				    var = new EPSVariable();
				    var.setOname("longitude");
				    var.setDtype(EPDOUBLE);
				    var.setVclass(Double.TYPE);
				    var.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.4");
				    var.addAttribute(1, "units", EPCHAR, 7, "degrees");
				    var.setUnits("degrees");
				    var.setFrmt("f10.4");
				    //var.addAttribute(2, "type", EPCHAR, 0, "");
				    sarray[0] = 502;
				    var.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
				    MultiArray xvma = new ArrayMultiArray(lla);
				    try {
				    	var.setData(xvma);
				    }
				    catch (Exception ex) {ex.printStackTrace();}
				    
				    db.addEPSVariable(var);
				    
				    // make the measured variables and add the data
				    for (int v=0; v<numVars; v++) { 
						//if (presPos == v)
						//	continue;
    				
	    				// create an array of measured EPSVariables for this database
	    				EPSVariable epsVar = new EPSVariable();
	    				
	    				// initialize the new EPSVariables
			    		// look this variable up in JOA EPIC_Key. find matching entry in original EPIC Key
			    		String oname = tempProperties[v].mVarLabel;
			    		String sname = null;
			    		String lname = null;
			    		String gname = null;
			    		String units = null;
			    		String ffrmt = null;
			    		int keyID = -99;
			    		int type = -99;
			    		try {
				    		keyID = mEpicKeyDB.findKey(tempProperties[v].mVarLabel);
				    		Key key = mOrigEpicKeyDB.findKey(keyID);
				    		gname = key.getGname();
				    		sname = key.getSname();
				    		lname = key.getLname();
				    		units = key.getUnits();
				    		ffrmt = key.getFrmt();
				    		type = key.getType();
				    	}
				    	catch (Exception e) {
				    		lname = tempProperties[v].mVarLabel;
				    		gname = tempProperties[v].mVarLabel;
				    		sname = tempProperties[v].mVarLabel;
				    		units = tempProperties[v].mUnits;
				    	}
			    		
			    		// make a new variable
					    epsVar = new EPSVariable();
					    
					    epsVar.setOname(oname);
					    epsVar.setSname(sname);
					    epsVar.setLname(lname);
					    epsVar.setGname(gname);
					    epsVar.setDtype(EPDOUBLE);
					    epsVar.setVclass(Double.TYPE);
					    int numAttributes = 0;
					    if (ffrmt != null) {
					    	epsVar.addAttribute(numAttributes++, "FORTRAN_format", EPCHAR, ffrmt.length(), ffrmt);
				    		epsVar.setFrmt(ffrmt);
					    }
					    if (units != null && units.length() > 0) {
					    	epsVar.addAttribute(numAttributes++, "units", EPCHAR, units.length(), units);
				    		epsVar.setUnits(units);
					    }
					    if (keyID >= 0)	{
					    	sarray[0] = (short)type;
					    	//epsVar.addAttribute(numAttributes++, "type", EPSHORT, 1, sarray);
					    }
					    if (keyID >= 0) {
					    	sarray[0] = (short)keyID;
					    	epsVar.addAttribute(numAttributes++, "epic_code", EPSHORT, 1, sarray);
					    }
					    
					    // connect variable to axis
					    epsVar.setDimorder(0, 0);
					    epsVar.setDimorder(1, 1);
					    epsVar.setDimorder(2, 2);
					    epsVar.setDimorder(3, 3);
					    epsVar.setT(timeAxis);
					    epsVar.setZ(zAxis);
					    epsVar.setY(latAxis);
					    epsVar.setX(lonAxis);
					    							
					    if (hasQC[v]) {
						    // add the quality code attribute
						    String qcVar = oname + "_QC";
						    epsVar.addAttribute(numAttributes++, "OBS_QC_VARIABLE", EPCHAR, qcVar.length(), qcVar);
						}
					    
					    // set the data 
					    // create storage for the measured variables
						double[][][][] vaa = new double[1][bc][1][1];
					    for (int b=0; b<bc; b++) {
		    				vaa[0][b][0][0] = va[v][b];
		    			}
					    MultiArray mdma = new ArrayMultiArray(vaa);
					    try {
					    	epsVar.setData(mdma);
					    }
					    catch (Exception ex) {System.out.println("throwing");}
					    
					    // add the variable to the database
				    	db.addEPSVariable(epsVar);
				    	
				    	if (hasQC[v]) {
					    	// create the quality code variable
					    	EPSVariable epsQCVar = new EPSVariable();
						    epsQCVar.setOname(oname + "_QC");
						    epsQCVar.setSname(sname + "_QC");
						    epsQCVar.setLname(sname + "_QC");
						    epsQCVar.setGname(sname + "_QC");
						    epsQCVar.setDtype(EPSHORT);
						    epsQCVar.setVclass(Short.TYPE);
						    
						    // connect variable to axis
						    epsQCVar.setDimorder(0, 0);
						    epsQCVar.setDimorder(1, 1);
						    epsQCVar.setDimorder(2, 2);
						    epsQCVar.setDimorder(3, 3);
						    epsQCVar.setT(timeAxis);
						    epsQCVar.setZ(zAxis);
						    epsQCVar.setY(latAxis);
						    epsQCVar.setX(lonAxis);
						    
						    // set the data 
						    // create storage for the qc variables
						    short[][][][] qcaa = new short[1][bc][1][1];
						    for (int b=0; b<bc; b++) {
			    				qcaa[0][b][0][0] = qc[v][b];
			    			}
						    MultiArray qcma = new ArrayMultiArray(qcaa);
						    try {
						    	epsQCVar.setData(qcma);
						    }
						    catch (Exception ex) {System.out.println("throwing");}
						    
						    // add the qc variable to the database
					    	db.addEPSVariable(epsQCVar);
				    	}
    				} //for v
				    
				    // add to temporary collection
				    dBases.addElement(db);
				    
				    // reset the bottle count
					bc = 0;
					newSection = false;
		    		oldStn = stnNum;
				}
			}  // while

			// add the dBase collection to the owner dbase
			mOwnerDBase.createSubEntries(mTotalStations, mFile.getName());
			for (int d=0; d<mTotalStations; d++) {
				Dbase db = (Dbase)dBases.elementAt(d);
				mOwnerDBase.addSubEntry(db);
			}
			mProgress.setPercentComplete(100.0);
			mProgress.dispose();
			in.close();
		}
		catch (Exception ex) {
		ex.printStackTrace();
			mProgress.dispose();
			FileImportException fie = new FileImportException();
			fie.setErrorLine(in.getLineNumber() - 1);
			throw(fie);
		}
	}
	
  	/**
   	* Get variable data from section file.
   	*
   	* @param inName Name of variable to get data for
   	*
   	* @return Multiarray of data
   	*
   	* @exception EPSVarDoesNotExistExcept Variable not found in the owner database
   	* @exception IOException An IO error occurred getting the data 
   	*/
	public MultiArray getvar(String inName) throws EPSVarDoesNotExistExcept, IOException {
		return null;
	}
	public MultiArray getvar(String inName, int[] lci, int[] uci, int[] dims) throws EPSVarDoesNotExistExcept, IOException {
		return null;
	}
	
}
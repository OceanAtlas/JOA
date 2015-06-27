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
 * <code>SD2SectionReader</code> 
 * Concrete implementation of the EPSFileReader interface to read, parse, and save an NODC SD2 Section file
 *
 * @author oz
 * @version 1.0
 *
 * @see EPSFileReader
 */
public class SD2SectionReader implements EPSFileReader, EPSConstants {
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
	* Optional string for progress string
	*/
	protected String mProgressStr = "Reading SD2 Data...";
	
  	/**
   	* Construct a new <code>SD2SectionReader</code> with a Dbase amd file.
   	*
   	* @param dname Dbase that this reader will fill in
   	* @param inFile Source section data file
   	*
 	* @see Dbase
   	*/
   	
	public SD2SectionReader(Dbase dname, File inFile, EpicPtr ep) {
		mOwnerDBase = dname;
		mFile = inFile;
		mOwnerDBase.setEpicPtr(ep);
		if (ep.getProgressStr() != null)
			mProgressStr = new String(ep.getProgressStr());
	}
	
  	/**
   	* Construct a new <code>SD2SectionReader</code> with a Dbase amd file and a prompt string.
   	*
   	* @param dname Dbase that this reader will fill in
   	* @param inFile Source section data file
   	*
 	* @see Dbase
   	*/
	public SD2SectionReader(Dbase dname, File inFile, EpicPtr ep, String progress) {
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
		String inLine = new String();
		LineNumberReader in = null;
		long bytesRead = 0;
		int[] ns = new int[2500];
		long bytesInFile = mFile.length();
		
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
	    	
	    	// first pass on the data file to get number of actual bottles per cast 
			int sc = -1;
			do {
				//read a line
				inLine = in.readLine();
				if (inLine == null) 
					break;
				bytesRead += inLine.length();
					
				if (inLine.length() == 0)
					continue;
				
				// get the record type 
				String testStr = inLine.substring(79, 80);
				int recordType = Integer.valueOf(testStr.trim()).intValue();
				
				if (recordType == 1) {
					// read the second station header
					do {
						inLine = in.readLine();
						bytesRead += inLine.length();
					}
					while (inLine.length() == 0 && bytesRead < bytesInFile);
					
					if (inLine == null) 
						break;

					testStr = inLine.substring(79, 80);
					recordType = Integer.valueOf(testStr.trim()).intValue();
					
					if (recordType != 2) {
						// an error has occurred 
						throw new FileImportException();
					}
					sc++;
					ns[sc] = 0;	
				}
				else if (recordType == 3 || recordType == 4)
					ns[sc]++;
					
			}
			while (bytesRead < bytesInFile && bytesRead < bytesInFile);
			in.close();
			in = null;
		    in = new LineNumberReader(new FileReader(mFile), 10000);

		    // main loop
			String stnNum;
			int castNum;
			double myLat = 0.0;
			double myLon = 0.0;
			int mTotalStations = 0;
			short[] sarray = new short[1];
			String sectionDescrip = mFile.getName();
			double bottomdbar = MISSINGVALUE;
	    	int year = MISSINGVALUE;
	    	int month = MISSINGVALUE;
	    	int day = MISSINGVALUE;
	    	int hour = MISSINGVALUE;
	    	double min = MISSINGVALUE;
			String shipCode = null;
			int nodcStnNum = 0;
			bytesRead = 0;
						
			tempProperties[0] = new JOAParameter("PRES");
			tempProperties[1] = new JOAParameter("TEMP");
			tempProperties[2] = new JOAParameter("SALT");
			tempProperties[3] = new JOAParameter("O2");
			tempProperties[4] = new JOAParameter("PO4");
			tempProperties[5] = new JOAParameter("SIO3");
			tempProperties[6] = new JOAParameter("NO2");
			tempProperties[7] = new JOAParameter("NO3");
			double pres = 0.0;
			double temp = 0.0;
			double salt = 0.0;
			double o2 = 0.0;
			double po4 = 0.0;
			double sio3 = 0.0;
			double no2 = 0.0;
			double no3 = 0.0;
			sc = 0;
			
			// create an array of arrays to store the data
			double[][] va = new double[8][500];
			
			do {
				mProgress.setPercentComplete(100.0 * ((double)bytesRead/(double)bytesInFile));
				// read master record 1--every record is considered a new section--store the section information
				do {
					inLine = in.readLine();
					if (inLine == null) 
						break;
					bytesRead += inLine.length();
				}
				while (inLine.length() == 0 && bytesRead < bytesInFile);
			
				if (inLine == null)  {
					break;
				}
					
				mTotalStations++;
				
				// get the nodc consecutive station number
				String testStr = inLine.substring(9, 13);
				
				try {
					nodcStnNum = Integer.valueOf(testStr.trim()).intValue();
				}
				catch (NumberFormatException ex) {
					nodcStnNum = 0;
				}
				
				shipCode = inLine.substring(53, 55);
				
				// read lat and lon 
				char hem = inLine.charAt(26);
				
				testStr = inLine.substring(27, 29);
				double degs =  Double.valueOf(testStr.trim()).doubleValue();
				
				testStr = inLine.substring(29, 31);
				double mins =  Double.valueOf(testStr.trim()).doubleValue();

				testStr = inLine.substring(31, 32);
				double tenths =  Double.valueOf(testStr.trim()).doubleValue();
				
				myLat = degs + (mins + (tenths/10))/60;
				myLat = hem == 'S' ? -myLat : myLat;
				
				hem = inLine.charAt(32);
				testStr = inLine.substring(33, 36);
				degs =  Double.valueOf(testStr.trim()).doubleValue();
				
				testStr = inLine.substring(36, 38);
				mins =  Double.valueOf(testStr.trim()).doubleValue();

				testStr = inLine.substring(38, 39);
				tenths = Double.valueOf(testStr.trim()).doubleValue();

				myLon = degs + (mins + (tenths/10))/60;
				myLon = hem == 'W' ? -myLon : myLon;
												
				// read the date 
				testStr = inLine.substring(40, 42);
				try {
					year = 1900 + Integer.valueOf(testStr.trim()).intValue();
				}
				catch (NumberFormatException ex) {
					System.out.println("err in year " + testStr);
				}
				
				testStr = inLine.substring(42, 44);
				try {
					month = Integer.valueOf(testStr.trim()).intValue();
				}
				catch (NumberFormatException ex) {
					System.out.println("err in month " + testStr);
				}
				
				testStr = inLine.substring(44, 46);
				try {
					day = Integer.valueOf(testStr.trim()).intValue();
				}
				catch (NumberFormatException ex) {
					System.out.println("err in day " + testStr);
				}

				testStr = inLine.substring(46, 49);
				double fhr = Double.valueOf(testStr.trim()).doubleValue()/10.0;
				hour = (int)Math.floor(fhr);
				double rmn = fhr - (double)hour;
				double flmin = rmn * 60;
				int imin = (int)flmin;
				double frmin = flmin - imin;
				int secs = (int)(frmin * 60.0);
				double fsec = (frmin * 60.0) - secs;
				int msec = (int)(fsec * 1000.0);
				String fs = String.valueOf(fsec);
				fs = fs.substring(fs.indexOf(".")+1, fs.length()).trim();
				int f = 0;
				try {
					if (fs != null && fs.length() > 0)
						f = Integer.valueOf(fs).intValue();
				}
				catch (NumberFormatException ex) {
				}
				
				// read the depth
				double zmax = EPSConstants.MISSINGVALUE;
				double z = EPSConstants.MISSINGVALUE;
				testStr = inLine.substring(55, 60);
				if (testStr.length() > 0) {
					zmax = Double.valueOf(testStr.trim()).doubleValue();
					zmax = EPS_Util.zToPres(zmax);
				}
				else 
					zmax = EPSConstants.MISSINGVALUE;			
								
				// read master record 2
				do {
					inLine = in.readLine();
					if (inLine == null)  {
						break;
					}
					bytesRead += inLine.length();
				}
				while (inLine.length() == 0 && bytesRead < bytesInFile);
			
				if (inLine == null) {
					break;
				}
				
				// read the station number 
				testStr = inLine.substring(17, 26);
				
				stnNum = testStr.trim();
				
				// test to see if missing and if so use the nodc stn number
				if (stnNum.length() == 0)
					stnNum = String.valueOf(nodcStnNum);
				
				// use the number of obs from first pass 
				int numObs = ns[sc++];
				
				// read the detail records
				for (int i=0; i<numObs; i++) {
					do {
						inLine = in.readLine();
						if (inLine == null)  {
							break;
						}
						bytesRead += inLine.length();
					}
					while (inLine.length() == 0 && bytesRead < bytesInFile);
				
					if (inLine == null) {
						break;
					}
					
					// make sure this is not the last detail record
					testStr = inLine.substring(78, 79);
					int nextRecType = 0;
					try {
						nextRecType = Integer.valueOf(testStr.trim()).intValue();
					}
					catch (NumberFormatException ex) {
						nextRecType = 0;
					}
					
					if (nextRecType == 1 && i+1 != numObs) {
						// premature end of detail
						break;
					}
							
					// depth
					testStr = inLine.substring(0, 5);
					try {
						z = Double.valueOf(testStr.trim()).doubleValue();
					}
					catch (NumberFormatException ex) {
						z = 0;
					}
					
					pres = EPS_Util.zToPres(z);
					
					// temperature
					testStr = inLine.substring(7, 12);
					try {
						temp = Double.valueOf(testStr.trim()).doubleValue();
						testStr = inLine.substring(12, 13);
						int prec = Integer.valueOf(testStr.trim()).intValue();
						if (prec != 9)
							temp = temp/(Math.pow(10, (double)prec));
						else
							temp = EPSConstants.MISSINGVALUE;
					}
					catch (NumberFormatException ex) {
						temp = EPSConstants.MISSINGVALUE;
					}
					
					// salinity
					testStr = inLine.substring(14, 19);
					try {
						salt = Double.valueOf(testStr.trim()).doubleValue();
						testStr = inLine.substring(19, 20);
						int prec = Integer.valueOf(testStr.trim()).intValue();
						if (prec != 9)
							salt = salt/(Math.pow(10, (double)prec));
						else
							salt = EPSConstants.MISSINGVALUE;
					}
					catch (NumberFormatException ex) {
						salt = EPSConstants.MISSINGVALUE;
					}
										
					// oxygen
					testStr = inLine.substring(32, 36);
					try {
						o2 = Double.valueOf(testStr.trim()).doubleValue();
						testStr = inLine.substring(36, 37);
						int prec = Integer.valueOf(testStr.trim()).intValue();
						if (prec != 9)
							o2 = o2/(Math.pow(10, (double)prec));
						else
							o2 = EPSConstants.MISSINGVALUE;
					}
					catch (NumberFormatException ex) {
						o2 = EPSConstants.MISSINGVALUE;
					}
										
					// phosphate
					testStr = inLine.substring(48, 52);
					try {
						po4 = Double.valueOf(testStr.trim()).doubleValue();
						testStr = inLine.substring(52, 53);
						int prec = Integer.valueOf(testStr.trim()).intValue();
						if (prec != 9)
							po4 = po4/(Math.pow(10, (double)prec));
						else
							po4 = EPSConstants.MISSINGVALUE;
					}
					catch (NumberFormatException ex) {
						po4 = EPSConstants.MISSINGVALUE;
					}
										
					// silicate
					testStr = inLine.substring(58, 62);
					try {
						sio3 = Double.valueOf(testStr.trim()).doubleValue();
						testStr = inLine.substring(62, 63);
						int prec = Integer.valueOf(testStr.trim()).intValue();
						if (prec != 9)
							sio3 = sio3/(Math.pow(10, (double)prec));
						else
							sio3 = EPSConstants.MISSINGVALUE;
					}
					catch (NumberFormatException ex) {
						sio3 = EPSConstants.MISSINGVALUE;
					}
					
					// nitrite
					testStr = inLine.substring(63, 66);
					try {
						no2 = Double.valueOf(testStr.trim()).doubleValue();;
						testStr = inLine.substring(66, 67);
						int prec = Integer.valueOf(testStr.trim()).intValue();
						if (prec != 9)
							no2 = no2/(Math.pow(10, (double)prec));
						else
							no2 = EPSConstants.MISSINGVALUE;
					}
					catch (NumberFormatException ex) {
						no2 = EPSConstants.MISSINGVALUE;
					}
					
					// nitrate
					testStr = inLine.substring(67, 70);
					try {
						no3 = Double.valueOf(testStr.trim()).doubleValue();
						testStr = inLine.substring(70, 71);
						int prec = Integer.valueOf(testStr.trim()).intValue();
						if (prec != 9)
							no3 = no3/(Math.pow(10, (double)prec));
						else
							no3 = EPSConstants.MISSINGVALUE;
					}
					catch (NumberFormatException ex) {
						no3 = EPSConstants.MISSINGVALUE;
					}
										
					castNum = 0;
					testStr = inLine.substring(47, 48);
					try {
						castNum = Integer.valueOf(testStr.trim()).intValue();
					}
					catch (NumberFormatException ex) {
						castNum = EPSConstants.MISSINGVALUE;
					}
					
					// add these to the bottle
					va[0][i] = pres;
					va[1][i] = temp;
					va[2][i] = salt;
					va[3][i] = o2;
					va[4][i] = po4;
					va[5][i] = sio3;
					va[6][i] = no2;
					va[7][i] = no3;
				}
				
			    // make a DBase object
			    Dbase db = new Dbase();

			    // add the global attributes
			    db.addEPSAttribute("CRUISE", EPSConstants.EPCHAR, sectionDescrip.length(), sectionDescrip);
			    db.addEPSAttribute("CAST", EPSConstants.EPCHAR, stnNum.length(), stnNum);
			    sarray[0] = (short)1;
			    db.addEPSAttribute("CAST_NUMBER", EPSConstants.EPSHORT, 1, sarray);
			    sarray[0] = (short)zmax;
			    db.addEPSAttribute("WATER_DEPTH", EPSConstants.EPSHORT, 1, sarray);
			    db.addEPSAttribute("DATA_ORIGIN", EPSConstants.EPCHAR, shipCode.length(), shipCode);
			    String dType = "SD2 BOTTLE";
			    db.addEPSAttribute("DATA_TYPE", EPSConstants.EPCHAR, dType.length(), dType);
			    db.setDataType("SD2 BOTTLE");
			    				    
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
				
				mins = 0;
				if (min != EPS_Util.MISSINGVALUE)
					mins = min;
				
				// make the time axis units	
				String date = "days since ";
				
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
			    GeoDate[] ta = {new GeoDate(month, day, year, hour, imin, secs, msec)};
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
			    
			    // z axis
			    zAxis.setName("depth");
			    zAxis.setTime(false);
			    zAxis.setUnlimited(false);
				zAxis.setLen(numObs);
			    zAxis.setAxisType(EPZAXIS);
			    zAxis.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.1");
			    zAxis.addAttribute(1, "units", EPCHAR, 4, "dbar");
			    zAxis.setUnits("dbar");
			    zAxis.setFrmt("f10.1");
			    //zAxis.addAttribute(2, "type", EPCHAR, 0, "");
			    sarray[0] = 1;
			    zAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
			    double[] za = new double[numObs];
			    for (int b=0; b<numObs; b++) {
	    			za[b] = va[0][b];
	    		}
			    MultiArray zma = new ArrayMultiArray(za);
			    zAxis.setData(zma);
			    db.setAxis(zAxis);
			    				    
			    // add the z axes variables
			    var = new EPSVariable();
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
			    catch (Exception ex) {}
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
			    catch (Exception ex) {}
			    
			    db.addEPSVariable(var);
			    
			    // make the measured variables and add the data
			    for (int v=0; v<8; v++) { 
					//if (v == 0)
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
				    
				    // add the quality code attribute
				    String qcVar = oname + "_QC";
				    epsVar.addAttribute(numAttributes++, "OBS_QC_VARIABLE", EPCHAR, qcVar.length(), qcVar);
				    
				    // connect variable to axis
				    epsVar.setDimorder(0, 0);
				    epsVar.setDimorder(1, 1);
				    epsVar.setDimorder(2, 2);
				    epsVar.setDimorder(3, 3);
				    epsVar.setT(timeAxis);
				    epsVar.setZ(zAxis);
				    epsVar.setY(latAxis);
				    epsVar.setX(lonAxis);
				    
				    // set the data 
				    // create storage for the measured variables
					double[][][][] vaa = new double[1][numObs][1][1];
				    for (int b=0; b<numObs; b++) {
	    				vaa[0][b][0][0] = va[v][b];
	    			}
				    MultiArray mdma = new ArrayMultiArray(vaa);
				    try {
				    	epsVar.setData(mdma);
				    }
				    catch (Exception ex) {System.out.println("throwing");}
				    
				    // add the variable to the database
			    	db.addEPSVariable(epsVar);
				} //for v
			    
			    // add to temporary collection
			    dBases.addElement(db);
			}
			while (bytesRead < bytesInFile);
			
			// add the dBase collection to the owner dbase
			mOwnerDBase.createSubEntries(mTotalStations, mFile.getName());
			for (int d=0; d<mTotalStations; d++) {
				Dbase db = (Dbase)dBases.elementAt(d);
				mOwnerDBase.addSubEntry(db);
			}
			mProgress.setPercentComplete(100.0);
			mProgress.dispose();
		}
		catch (Exception ex) {
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
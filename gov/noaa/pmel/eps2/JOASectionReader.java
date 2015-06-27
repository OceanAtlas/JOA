package gov.noaa.pmel.eps2;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.awt.*;
import java.util.*;
import java.beans.*;
import ucar.netcdf.*;
import ucar.multiarray.*;
import gov.noaa.pmel.util.*;
//import gov.noaa.noaaserver.sgt.*;

/**
 * <code>JOASectionReader</code> 
 * Concrete implementation of the EPSFileReader interface to read, parse, and save a JOA Section file
 *
 * @author oz
 * @version 1.0
 *
 * @see EPSFileReader
 */
public class JOASectionReader implements EPSFileReader, EPSConstants {
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
	protected String mProgressStr = "Reading JOA Binary Data...";
	
  	/**
   	* Construct a new <code>JOASectionReader</code> with a Dbase amd file.
   	*
   	* @param dname Dbase that this reader will fill in
   	* @param inFile Source section data file
   	*
 	* @see Dbase
   	*/
	public JOASectionReader(Dbase dname, File inFile, EpicPtr ep) {
		mOwnerDBase = dname;
		mFile = inFile;
		mOwnerDBase.setEpicPtr(ep);
		if (ep.getProgressStr() != null)
			mProgressStr = new String(ep.getProgressStr());
	}
	
  	/**
   	* Construct a new <code>JOASectionReader</code> with a Dbase amd file and a prompt string.
   	*
   	* @param dname Dbase that this reader will fill in
   	* @param inFile Source section data file
   	*
 	* @see Dbase
   	*/
	public JOASectionReader(Dbase dname, File inFile, EpicPtr ep, String progress) {
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
		FileInputStream in = null;
		DataInputStream inData = null;
		short inShort = 0;
		long bytesRead = 0;
		long bytesInFile = mFile.length();
		short[] sarray = new short[1];
		int mTotalStns = 0;
		
		EPSProgressDialog mProgress = new EPSProgressDialog(new Frame(), mProgressStr, Color.blue);
		mProgress.setVisible(true);	
		
    	// Get an epic key database specific to JOA
    	EPIC_Key_DB mEpicKeyDB = new EPIC_Key_DB("joa_epic.key");
    	
    	// Get an epic key database specific to JOA
    	EPIC_Key_DB mOrigEpicKeyDB = new EPIC_Key_DB("epic.key");
    	
    	// create a vector for temporary storage of the dbases
    	Vector dBases = new Vector(100);
    	
    	try {
		    in = new FileInputStream(mFile);
			BufferedInputStream bis = new BufferedInputStream(in, 1000000);
		    inData = new DataInputStream(bis);
		    
	    	// read version
	    	short vers = inData.readShort();
	    	bytesRead += 2;
	    	
	    	if (vers < 2) {
				FileImportException fiex = new FileImportException();
				String errStr = "Invalid version for a JOA binary file";
				fiex.setErrorType(errStr);
				throw fiex;
	    	}
		    
	    	// read number of bytes in file description string
	    	inShort = inData.readShort();
	    	bytesRead += 2;
	    	
	    	// read the file description String
	    	byte buf[] = new byte[inShort];
	    	inData.read(buf, 0, inShort);
	    	String fileDescrip = new String(buf);
	    	bytesRead += inShort;
	    	
	    	// create a new open file object
	    	JOADataFile of = new JOADataFile(fileDescrip);
	    		    	
	    	// read the number of sections
	    	int numSections = inData.readShort();
	    	bytesRead += 2;
	    	
	    	// read each section
	    	JOASection sech;
			int ord = 0; 
	    	for (int s=0; s<numSections; s++) {	
				mProgress.setPercentComplete(100.0 * ((double)bytesRead/(double)bytesInFile));

	    		// read the section header
	    		inShort = inData.readShort();
	    		bytesRead += 2;
		    	byte buf1[] = new byte[inShort];
		    	inData.read(buf1, 0, inShort);
		    	String sectionDescrip = new String(buf1);
	    		bytesRead += inShort;
		    	
		    	// read the ship code
		    	byte bufsc[] = new byte[2];
		    	bufsc[0] = inData.readByte();
		    	bufsc[1] = inData.readByte();
		    	String shipCode = new String(bufsc);
	    		bytesRead += 2;
		    	
		    	// read num casts
	    		int numCasts = inData.readShort();
	    		bytesRead += 2;
		    	
		    	// read num parameters
	    		int numVars = inData.readShort();
	    		bytesRead += 2;
	    		
	    		// quality code
	    		int qcStd = 1;
	    		if (vers == 4) {
		    		qcStd = inData.readShort();
		    		bytesRead += 2;
	    		}
	    		
	    		// create a new section
	    		of.mNumSections++;
	    		sech = new JOASection(of.mNumSections, sectionDescrip, shipCode, numCasts, numVars);
	    		
	    		if (vers == 2) {
		    		// read the properties
			    	byte bufv[] = new byte[4];
					for (int p=0; p<numVars; p++) {
						// parameter name
				    	inData.read(bufv, 0, 4);
				    	String tempVar = new String(bufv);
		    			bytesRead += 4;
		    			
						// units
			    		inShort = inData.readShort();
			    		bytesRead += 2;
			    		
			    		String units = null;
			    		if (inShort > 0) {
					    	byte buf11[] = new byte[inShort];
					    	inData.read(buf11, 0, inShort);
					    	units = new String(buf11);
		    				bytesRead += inShort;
					    }
						
						// convert varnames to UC 
						tempVar.toUpperCase();
						
						// create new property
						tempProperties[p] = new JOAParameter(tempVar, units);
						
						// add this property to the section property list 
						if (tempProperties[p].mUnits == null) {
							tempProperties[p].mUnits = EPS_Util.paramNameToJOAUnits(false, tempProperties[0].mVarLabel);
						} 
						
						if (tempProperties[p].mUnits == null) {
							tempProperties[p].mUnits = new String("na");
						}
						tempProperties[p].mCastOrObs = EPSConstants.ALL_OBS;

						// read the actual scale
						int actScale = inData.readShort();
		    			bytesRead += 2;
		    			
						if (actScale != 0)
							tempProperties[p].mActScale = 1.0/(double)actScale;
						else
							tempProperties[p].mActScale = 1.0;
							
						// read the actual origin
						int actOrigin = inData.readShort();
						tempProperties[p].mActOrigin = (double)actOrigin * tempProperties[p].mActScale;

						// read reverse y
						int reverseY = inData.readShort();
		    			bytesRead += 2;
		    			
						if (reverseY == 0)
							tempProperties[p].mReverseY = false;
						else
							tempProperties[p].mReverseY = true;
						
						tempProperties[p].mWasCalculated = false;
					}
				}
				else {
		    		// read the properties
				    String tempVar = null;
					for (int p=0; p<numVars; p++) {
						// length of parameter name
			    		inShort = inData.readShort();
			    		bytesRead += 2;
			    		
			    		// parameter name
			    		if (inShort > 0) {
					    	byte buf13[] = new byte[inShort];
					    	inData.read(buf13, 0, inShort);
					    	tempVar = new String(buf13);
		    				bytesRead += inShort;
					    }
			    		
						// units
			    		inShort = inData.readShort();
			    		bytesRead += 2;
			    		
			    		String units = null;
			    		if (inShort > 0) {
					    	byte buf11[] = new byte[inShort];
					    	inData.read(buf11, 0, inShort);
					    	units = new String(buf11);
		    				bytesRead += inShort;
					    }
						
						// convert varnames to UC 
						tempVar = tempVar.toUpperCase();
						
						// create new property
						tempProperties[p] = new JOAParameter(tempVar, units);
						
						// add this property to the section property list 
						if (tempProperties[p].mUnits == null) {
							tempProperties[p].mUnits = EPS_Util.paramNameToJOAUnits(false, tempProperties[0].mVarLabel);
						} 
						
						if (tempProperties[p].mUnits == null) {
							tempProperties[p].mUnits = new String("na");
						}
						tempProperties[p].mCastOrObs = EPSConstants.ALL_OBS;
						
						// read the actual scale
						int actScale = inData.readShort();
		    			bytesRead += 2;
						if (actScale != 0)
							tempProperties[p].mActScale = 1.0/(double)actScale;
						else
							tempProperties[p].mActScale = 1.0;
							
						// read the actual origin
						int actOrigin = inData.readShort();
						tempProperties[p].mActOrigin = (double)actOrigin * tempProperties[p].mActScale;
						    
						// read reverse y
						int reverseY = inData.readShort();
		    			bytesRead += 2;
						if (reverseY == 0)
							tempProperties[p].mReverseY = false;
						else
							tempProperties[p].mReverseY = true;
						
						tempProperties[p].mWasCalculated = false;
					}
				}
				
				// read the cast headers
				for (int c=0; c<numCasts; c++) {
					// read station number
		    		inShort = inData.readShort();
		    		bytesRead += 2;
			    	byte bufx[] = new byte[inShort];
			    	inData.read(bufx, 0, inShort);
			    	String stnNum = new String(bufx);
	    			bytesRead += inShort;
					
					// read cast number
					int castNum = inData.readShort();
	    			bytesRead += 2;
					
					double myLat = 0.0;
					double myLon = 0.0;
					if (vers == 2) {
						// read latitude/lon
						int lat = inData.readInt();
		    			bytesRead += 4;
						
						int lon = inData.readInt();
		    			bytesRead += 4;
						
						myLat = lat * 0.001;
						myLon = lon * 0.001;
					}
					else if (vers > 2) {
						// read latitude/lon
						myLat = inData.readDouble();
		    			bytesRead += 8;
						
						myLon = inData.readDouble();
		    			bytesRead += 4;
					}
										
					// read number of bottles
					int numBottles = inData.readShort();
	    			bytesRead += 2;
					
					// read the date
					int year = inData.readInt(); 
	    			bytesRead += 4;
					int month = inData.readInt();
	    			bytesRead += 4;
					int day = inData.readInt();
	    			bytesRead += 4;
					int hour = inData.readInt();
	    			bytesRead += 4;
					double min = inData.readDouble();
	    			bytesRead += 8;
					
					// read bottom
					int bottomdbar = inData.readInt();

					// read station quality
					int stnQual = inData.readShort();
	    			bytesRead += 2;
				    
					ord++; 
					JOAStation sh = new JOAStation(ord, shipCode, stnNum, castNum, myLat, 
						myLon, numBottles, year, month, day, hour, min, bottomdbar, stnQual);
					sech.mStations.addElement(sh);
					sh.setType("JOA BOTTLE");
						
				    // make a DBase object
				    Dbase db = new Dbase();

				    // add the global attributes
				    db.addEPSAttribute("CRUISE", EPCHAR, sech.mSectionDescription.length(), sech.mSectionDescription);
				    db.addEPSAttribute("CAST", EPCHAR, sh.mStnNum.length(), sh.mStnNum);
				    sarray[0] = (short)sh.mBottomDepthInDBARS;
				    db.addEPSAttribute("WATER_DEPTH", EPSHORT, 1, sarray);
				    db.addEPSAttribute("DATA_ORIGIN", EPCHAR, sech.mShipCode.length(), sech.mShipCode);
				    String dType = sh.getType();
				    if (dType == null)
				    	dType = "UNKN";
				    db.addEPSAttribute("DATA_TYPE", EPCHAR, dType.length(), dType);
			    	sarray[0] = (short)stnQual;
			    	db.addEPSAttribute("STN_QUALITY", EPSHORT, 1, sarray);
				    db.setDataType("JOA BOTTLE");
				    
				    // add to temporary collection
				    dBases.addElement(db);
 				}
 				
    			int start = mTotalStns;
    			int end = sech.mStations.size()+mTotalStns;
    			for (int sc=start; sc<end; sc++) {	
					mProgress.setPercentComplete(100.0 * ((double)bytesRead/(double)bytesInFile));
						
    				// get a dBase
    				Dbase db = (Dbase)dBases.elementAt(sc);
    				
    				// get a station
    				JOAStation sh = (JOAStation)sech.mStations.elementAt(sc-start);
						
					// create an array of arrays to store the data
					double[][] va = new double[sech.mNumVars][sh.mNumBottles];
					int[][] qc = new int[sech.mNumVars][sh.mNumBottles];
					short[] bqc = new short[sh.mNumBottles];
					int presPos = 0;
    				
    				// read the bottles
    				for (int b=0; b<sh.mNumBottles; b++) {
				    	// read the bottle quality code
				    	bqc[b] = inData.readShort();
	    				bytesRead += 2;
	    				
    					for (int v=0; v<sech.mNumVars; v++) {
    						// store the position of the PRES variable
    						if (tempProperties[v].mVarLabel.equals("PRES"))
    							presPos = v;
    							
    						// get the measured parameter
    						double dVarVal = EPSConstants.MISSINGVALUE;
    						try {
    							dVarVal = inData.readDouble();
    						} 
    						catch (IOException e) {
								FileImportException fiex = new FileImportException();
								String errStr = "Error reading the parameter data. " +
									"\n" + "Bottle #" + b + " Parameter #" + v;
								fiex.setErrorType(errStr);
								throw fiex;    						}
	    					bytesRead += 8;

							// store the value in the multidimensional array
							va[v][b] = dVarVal;
							
							// get the quality flag
							short flag = (short)EPSConstants.MISSINGVALUE;
							
							try {
    							flag = inData.readShort();
    						} 
    						catch (IOException e) {
								FileImportException fiex = new FileImportException();
								String errStr = "Error reading the parameter quality code. " +
									"\n" + "Bottle #" + b + " Parameter #" + v;
								fiex.setErrorType(errStr);
								throw fiex;
    						}
    						bytesRead += 2;
    						qc[v][b] = flag;
    						
						} // for v
    				} // for b
    				
    				// add the bottle quality codes as an attribute
			    	db.addEPSAttribute("BOTTLE_QUALITY_CODES", EPSHORT, sh.mNumBottles, bqc);
					
				    // create the axes time = 0, depth = 1, lat = 2, lon = 3
				    Axis timeAxis = new Axis();
				    Axis zAxis = new Axis();
				    Axis latAxis = new Axis();
				    Axis lonAxis = new Axis();
				    
				    // time axis
				    timeAxis.setName("time");
				    timeAxis.setTime(true);
				    timeAxis.setUnlimited(false);
				    timeAxis.setAxisType(EPTAXIS);
					timeAxis.setLen(1);
					
					int hour = 0;
					if (sh.mHour != EPSConstants.MISSINGVALUE)
						hour = sh.mHour;
						
					double mins = 0;
					if (sh.mMinute != EPSConstants.MISSINGVALUE)
						mins = sh.mMinute;
					
					// make the time axis units	
					String date = "days since ";
					int min = (int)mins;
					double fmin = mins - min;
					int secs = (int)(fmin * 60.0);
					double fsec = (fmin * 60.0) - secs;
					int msec = (int)(fsec * 1000.0);
					String fs = String.valueOf(fsec);
					fs = fs.substring(fs.indexOf(".")+1, fs.length()).trim();
					int f = 0;
					if (fs != null && fs.length() > 0)
						f = Integer.valueOf(fs).intValue();
					
    		    	//sprintf(time_string,"%04d-%02d-%02d %02d:%02d:%02d.%03d",yr,mon,day,hr,min,sec,f); 
					String frmt = new String("{0,number,####}-{1,number,00}-{2,number,00} {3,number,00}:{4,number,00}:{5,number,00}.{6,number,000}"); 
					MessageFormat msgf = new MessageFormat(frmt);
					
					Object[] objs = {new Integer(sh.mYear), new Integer(sh.mMonth), new Integer(sh.mDay), new Integer(hour),
									 new Integer(min), new Integer(secs), new Integer(f)};
					StringBuffer out = new StringBuffer();
					msgf.format(objs, out, null);
					String time_string = new String(out);
					date = date + time_string;
				    timeAxis.addAttribute(0, "units", EPCHAR, date.length(), date);
				    timeAxis.setUnits(date);
				    GeoDate gd = null;
				    try {
				    	gd = new GeoDate(sh.mMonth, sh.mDay, sh.mYear, hour, min, secs, msec);
				    	GeoDate[] ta = {gd};
					    MultiArray tma = new ArrayMultiArray(ta);
					    
					    timeAxis.setData(tma);
					    db.setAxis(timeAxis);
				    }
				    catch (Exception ex) {
				    	GeoDate[] ta = {new GeoDate()};
					    MultiArray tma = new ArrayMultiArray(ta);
					    
					    timeAxis.setData(tma);
					    db.setAxis(timeAxis);
				    }
				    
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
					zAxis.setLen(sh.mNumBottles);
				    zAxis.setAxisType(EPZAXIS);
				    zAxis.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.1");
				    zAxis.addAttribute(1, "units", EPCHAR, 4, "dbar");
				    zAxis.setUnits("dbar");
				    zAxis.setFrmt("f10.1");
				    //zAxis.addAttribute(2, "type", EPCHAR, 0, "");
				    sarray[0] = 1;
				    zAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
				    double[] za = new double[sh.mNumBottles];
				    for (int b=0; b<sh.mNumBottles; b++) {
		    			za[b] = va[presPos][b];
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
				    double lat = sh.mLat;
				    double[] la = {lat};
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
				    double lon = sh.mLon;
				    double[] lla = {lon};
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
				    					
				    // add the measured variables
					for (int v=0; v<sech.mNumVars; v++) { 
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
						double[][][][] vaa = new double[1][sh.mNumBottles][1][1];
					    for (int b=0; b<sh.mNumBottles; b++) {
		    				vaa[0][b][0][0] = va[v][b];
		    			}
					    MultiArray mdma = new ArrayMultiArray(vaa);
					    try {
					    	epsVar.setData(mdma);
					    }
					    catch (Exception ex) {System.out.println("throwing");}
					    
					    // add the variable to the database
				    	db.addEPSVariable(epsVar);
				    	
				    	// create the quality code variable
				    	epsVar = new EPSVariable();
					    epsVar.setOname(oname + "_QC");
					    epsVar.setSname(sname + "_QC");
					    epsVar.setLname(sname + "_QC");
					    epsVar.setGname(sname + "_QC");
					    epsVar.setDtype(EPSHORT);
					    epsVar.setVclass(Short.TYPE);
					    
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
					    // create storage for the qc variables
						short[][][][] qcaa = new short[1][sh.mNumBottles][1][1];
					    for (int b=0; b<sh.mNumBottles; b++) {
		    				qcaa[0][b][0][0] = (short)qc[v][b];
		    			}
					    MultiArray qcma = new ArrayMultiArray(qcaa);
					    try {
					    	epsVar.setData(qcma);
					    }
					    catch (Exception ex) {System.out.println("throwing");}
					    
					    // add the qc variable to the database
				    	db.addEPSVariable(epsVar);
    				} //for v
    			}
	    		mTotalStns += numCasts;
    		} // for sc

			// read the file comments
	    	StringBuffer sb = null;
	    	while (true) {
	    		try {
	    			// read continuation line
			    	inShort = inData.readShort();
	    			bytesRead += 2;
			    	
			    	if (inShort == 1) {
				    	// read number of bytes in file description string
				    	inShort = inData.readShort();
	    				bytesRead += 2;
				    	
				    	// read the file description String
				    	byte buf2[] = new byte[inShort];
				    	inData.read(buf2, 0, inShort);
				    	String commentLine = new String(buf2);
	    				bytesRead += inShort;
				    	
				    	if (sb == null)
				    		sb = new StringBuffer(commentLine);
				    	else
				    		sb.append(commentLine);
				    }
				}
				catch (IOException e) { 
					break; 
				}	
			} // while true
			
			if (sb != null) {
				// make attributes for the comments
				mOwnerDBase.setDataComment(new String(sb));
			}
			
			// make a sub database in the dbase
			mOwnerDBase.createSubEntries(mTotalStns, mFile.getName());
			for (int d=0; d<mTotalStns; d++) {
				Dbase db = (Dbase)dBases.elementAt(d);
				mOwnerDBase.addSubEntry(db);
			}
			mProgress.setPercentComplete(100.0);
			mProgress.setVisible(false);	
			mProgress.dispose();
			in.close();
    	}
	    catch (IOException e) { 
			mProgress.setVisible(false);	
			mProgress.dispose();
	    	throw e;
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
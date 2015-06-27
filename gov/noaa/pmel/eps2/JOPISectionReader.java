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
public class JOPISectionReader implements EPSFileReader, EPSConstants {
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
	protected String mProgressStr = "Reading JOPI Data...";
	
  	/**
   	* Construct a new <code>JOPISectionReader</code> with a Dbase amd file.
   	*
   	* @param dname Dbase that this reader will fill in
   	* @param inFile Source section data file
   	*
 	* @see Dbase
   	*/
   	
	public JOPISectionReader(Dbase dname, File inFile, EpicPtr ep) {
		mOwnerDBase = dname;
		mFile = inFile;
		mOwnerDBase.setEpicPtr(ep);
		if (ep.getProgressStr() != null)
			mProgressStr = new String(ep.getProgressStr());
	}
	
  	/**
   	* Construct a new <code>JOPISectionReader</code> with a Dbase amd file and a prompt string.
   	*
   	* @param dname Dbase that this reader will fill in
   	* @param inFile Source section data file
   	*
 	* @see Dbase
   	*/
	public JOPISectionReader(Dbase dname, File inFile, EpicPtr ep, String progress) {
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
    	
    	EPSProperties.SDELIMITER = EPSConstants.STAB_DELIMITER;
    	EPSProperties.DOUBLEDELIM = EPSProperties.SDELIMITER + EPSProperties.SDELIMITER;
    	int sc = 1;
    	// test the delimiter
		try {
		    in = new LineNumberReader(new FileReader(mFile), 10000);
	    	
	    	// first pass get the delimiter
			inLine = in.readLine();
			bytesRead += inLine.length();
				
			if (inLine.indexOf(',') > 0) {
				EPSProperties.SDELIMITER = EPSConstants.SCOMMA_DELIMITER;
    			EPSProperties.DOUBLEDELIM = EPSProperties.SDELIMITER + EPSProperties.SDELIMITER;
    		}
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
	    	int min = MISSINGVALUE;
			String shipCode = null;
			int nodcStnNum = 0;
			bytesRead = 0;
			
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
				
				// assign a consecutive station number
				stnNum = String.valueOf(sc++);
				
				// read lat and lon 
				
				// lat degrees
				String itemStr = EPS_Util.getItem(inLine, 1);
				char hem = itemStr.charAt(6);
				
				String testStr = itemStr.substring(0, 2);
				double degs =  Double.valueOf(testStr.trim()).doubleValue();
				
				testStr = itemStr.substring(2, 4);
				double mins =  Double.valueOf(testStr.trim()).doubleValue();

				testStr = itemStr.substring(4, 6);
				double secs =  Double.valueOf(testStr.trim()).doubleValue();
				
				myLat = degs + (mins + (secs/60))/60.0;
				myLat = hem == 'S' ? -myLat : myLat;
				
				// lon
				itemStr = EPS_Util.getItem(inLine, 2); 
				hem = itemStr.charAt(7);
				
				testStr = itemStr.substring(0, 3);
				degs =  Double.valueOf(testStr.trim()).doubleValue();
				
				testStr = itemStr.substring(3, 5);
				mins =  Double.valueOf(testStr.trim()).doubleValue();

				testStr = itemStr.substring(5, 7);
				secs = Double.valueOf(testStr.trim()).doubleValue();

				myLon = degs + (mins + (secs/60))/60.0;
				myLon = hem == 'W' ? -myLon : myLon;
												
				// read the date 
				itemStr = EPS_Util.getItem(inLine, 3);
				testStr = itemStr.substring(0, 4);
				try {
					year = Integer.valueOf(testStr.trim()).intValue();
				}
				catch (NumberFormatException ex) {
					System.out.println("err in year " + testStr);
				}
				
				testStr = itemStr.substring(4, 6);
				try {
					month = Integer.valueOf(testStr.trim()).intValue();
				}
				catch (NumberFormatException ex) {
					System.out.println("err in month " + testStr);
				}
				
				testStr = itemStr.substring(6, 8);
				try {
					day = Integer.valueOf(testStr.trim()).intValue();
				}
				catch (NumberFormatException ex) {
					System.out.println("err in day " + testStr);
				}
				
				// get the time
				itemStr = EPS_Util.getItem(inLine, 4);
				testStr = itemStr.substring(0, 2);
				try {
					hour = Integer.valueOf(testStr.trim()).intValue();
				}
				catch (NumberFormatException ex) {
					System.out.println("err in hour " + testStr);
				}
				
				testStr = itemStr.substring(2, 4);
				try {
					min = Integer.valueOf(testStr.trim()).intValue();
				}
				catch (NumberFormatException ex) {
					System.out.println("err in month " + testStr);
				}
				
				// read the number of details lines
				itemStr = EPS_Util.getItem(inLine, 6);
				int numDetailLines = Integer.valueOf(itemStr.trim()).intValue();
				
				// read the depth
				itemStr = EPS_Util.getItem(inLine, 7);
				double zmax = EPSConstants.MISSINGVALUE;
				double z = EPSConstants.MISSINGVALUE;
				if (itemStr.length() > 0) {
					zmax = Double.valueOf(itemStr.trim()).doubleValue();
					zmax = EPS_Util.zToPres(zmax);
				}
				else 
					zmax = EPSConstants.MISSINGVALUE;			
								
				// read master record 2: -it has the number and names of the parameter
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
				
				// read the number of parameters 
				itemStr = EPS_Util.getItem(inLine, 1);
				int numParams = Integer.valueOf(itemStr.trim()).intValue();
				
				boolean convertZtoPres = false;
				
				// read the parameters into temp properties
				String varLabel = null;
				int pPos = 2;
				for (int i=1; i<=numParams; i++) {
					itemStr = EPS_Util.getItem(inLine, i + 1);
					if (itemStr.equalsIgnoreCase("depth")) {
						varLabel = "PRES";
						convertZtoPres = true;
						pPos = i + 1;
					}
					varLabel = itemStr;
					tempProperties[i-1] = new JOAParameter(varLabel);
				}
						
				// create an array of arrays to store the data
				double[][] va = new double[numParams][numDetailLines];
				
				// read the detail records
				for (int i=0; i<numDetailLines; i++) {
					do {
						inLine = in.readLine();
						if (inLine == null) 
							break;
						bytesRead += inLine.length();
					}
					while (inLine.length() == 0 && bytesRead < bytesInFile);
				
					if (inLine == null) {
						break;
					}
					inLine = EPS_Util.expandNullItems(inLine);
							
					double val = EPSConstants.MISSINGVALUE;
					for (int p=1; p<=numParams; p++) {			
						testStr = EPS_Util.getItem(inLine, p + 1);
						try {
							val = Double.valueOf(testStr.trim()).doubleValue();
							if (p + 1 == pPos && convertZtoPres)
								val = EPS_Util.zToPres(val);
						}
						catch (Exception ex) {
							val = EPSConstants.MISSINGVALUE;
						}
						va[p-1][i] = val;
					}
				}
				
			    // make a DBase object
			    Dbase db = new Dbase();

			    // add the global attributes
			    db.addEPSAttribute("CRUISE", EPSConstants.EPCHAR, sectionDescrip.length(), sectionDescrip);
			    db.addEPSAttribute("CAST", EPSConstants.EPCHAR, stnNum.length(), stnNum);
			    sarray[0] = (short)1;
			    db.addEPSAttribute("CAST_NUMBER", EPSConstants.EPSHORT, 1, sarray);
			    db.addEPSAttribute("DATA_ORIGIN", EPSConstants.EPCHAR, 9, "NODC/JOPI");
			    String dType = "JOPI BOTTLE";
			    db.addEPSAttribute("DATA_TYPE", EPSConstants.EPCHAR, dType.length(), dType);
			    db.setDataType("JOPI BOTTLE");
			    				    
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
								 new Integer(min), new Integer(0), new Integer(0)};
				StringBuffer out = new StringBuffer();
				msgf.format(objs, out, null);
				String time_string = new String(out);
				date = date + time_string;
			    timeAxis.addAttribute(0, "units", EPCHAR, date.length(), date);
			    timeAxis.setUnits(date);
			    GeoDate[] ta = new GeoDate[1];
			    try {
			    	ta[0] = new GeoDate(month, day, year, hour, min, 0, 0);
			    }
			    catch (Exception ex) {
			    	ta[0] = new GeoDate();
			    }
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
				zAxis.setLen(numDetailLines);
			    zAxis.setAxisType(EPZAXIS);
			    zAxis.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.1");
			    zAxis.addAttribute(1, "units", EPCHAR, 4, "dbar");
			    zAxis.setUnits("dbar");
			    zAxis.setFrmt("f10.1");
			    //zAxis.addAttribute(2, "type", EPCHAR, 0, "");
			    sarray[0] = 1;
			    zAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
			    double[] za = new double[numDetailLines];
			    for (int b=0; b<numDetailLines; b++) {
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
			    for (int v=1; v<numParams; v++) { 				
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
				    
				    // set the data 
				    // create storage for the measured variables
					double[][][][] vaa = new double[1][numDetailLines][1][1];
				    for (int b=0; b<numDetailLines; b++) {
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
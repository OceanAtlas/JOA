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
 * <code>WOCESectionReader</code> Concrete implementation of the EPSFileReader
 * interface to read, parse, and save a WOCE Section file
 * 
 * @author oz
 * @version 1.0
 * 
 * @see EPSFileReader
 */
public class WOCECTDProfileReader implements EPSFileReader, EPSConstants {
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
	protected String mProgressStr = "Reading WOCE CTD Data...";

	/**
	 * Construct a new <code>WOCEBottleSectionReader</code> with a Dbase and
	 * file.
	 * 
	 * @param dname
	 *          Dbase that this reader will fill in
	 * @param inFile
	 *          Source section data file
	 * 
	 * @see Dbase
	 */
	public WOCECTDProfileReader(Dbase dname, File inFile, EpicPtr ep) {
		mOwnerDBase = dname;
		mFile = inFile;
		mOwnerDBase.setEpicPtr(ep);
		if (ep.getProgressStr() != null)
			mProgressStr = new String(ep.getProgressStr());
	}

	/**
	 * Construct a new <code>WOCEBottleSectionReader</code> with a Dbase and
	 * file and prompt string.
	 * 
	 * @param dname
	 *          Dbase that this reader will fill in
	 * @param inFile
	 *          Source section data file
	 * 
	 * @see Dbase
	 */
	public WOCECTDProfileReader(Dbase dname, File inFile, EpicPtr ep, String progress) {
		mOwnerDBase = dname;
		mFile = inFile;
		mProgressStr = null;
		mProgressStr = progress;
		mOwnerDBase.setEpicPtr(ep);
	}

	// concrete implementations of the io routines
	/**
	 * Parse the section file and fill in the Dbase.
	 * 
	 * @exception Exception
	 */
	public void parse() throws Exception {
		JOAParameter tempProperties[] = new JOAParameter[100];
		int[] paramPositions = new int[100];
		int[] qbPositions = new int[100];
		for (int i = 0; i < 100; i++) {
			qbPositions[i] = MISSINGVALUE;
		}
		String inLine = new String();
		LineNumberReader in = null;
		long bytesRead = 0;
		long bytesInFile = mFile.length();
		int presPos = 0;
		String qbStd = null;

		EPSProgressDialog mProgress = new EPSProgressDialog(new Frame(), mProgressStr, Color.blue);
		mProgress.setVisible(true);

		// Get an epic key database specific to JOA
		EPIC_Key_DB mEpicKeyDB = new EPIC_Key_DB("joa_epic.key");

		// Get an epic key database specific to JOA
		EPIC_Key_DB mOrigEpicKeyDB = new EPIC_Key_DB("epic.key");

		try {
			in = new LineNumberReader(new FileReader(mFile), 500000);

			// create a new open file object
			JOADataFile of = new JOADataFile("Untitled");

			// read to the column header line
			while (true) {
				inLine = in.readLine();
				bytesRead += inLine.length();
				if (inLine.startsWith("#")) {
					mOwnerDBase.addComment(inLine);
				}
				else
					break;
			}

			// should have the first significant line
			if (!inLine.startsWith("CTD")) {
				// not a CTD file
				FileImportException fie = new FileImportException();
				throw (fie);
			}
			else {
				// read to next noncommented line
				while (true) {
					inLine = in.readLine();
					bytesRead += inLine.length();
					if (inLine.startsWith("#")) {
						mOwnerDBase.addComment(inLine);
					}
					else
						break;
				}
			}

			// get to here when we found the header lines
			// convert string to uppercase
			String inLineLC = new String(inLine);
			inLine = inLine.toUpperCase();

			// first real line is number of headers to read
			int numHeaders = 0;
			EPSProperties.SDELIMITER = SEQUAL_DELIMITER;
			if (EPS_Util.getItem(inLine, 1).trim().equalsIgnoreCase("NUMBER_HEADERS"))
				numHeaders = EPS_Util.getIntItem(inLine, 2);
			else {
				FileImportException fie = new FileImportException();
				throw (fie);
			}

			String[] expoStrings = { "EXPOCODE" };
			int[] expoStrictness = { CONTAINS };
			String[] secStrings = { "SECTION_ID", "SECT", "SEC", "SE", "WHP-ID", "PROJECT", "ID", "NAME" };
			int[] secStrictness = { CONTAINS, CONTAINS, STARTSWITH, CONTAINS, CONTAINS, CONTAINS, CONTAINS, CONTAINS, };
			String[] stnStrings = { "STNNBR", "STA", "STATION", "ST", "STN", };
			int[] stnStrictness = { CONTAINS, CONTAINS, CONTAINS, STARTSWITH, CONTAINS };
			String[] castStrings = { "CAST", "CASTNO", "CA", "CST" };
			int[] castStrictness = { STARTSWITH, CONTAINS, CONTAINS, STARTSWITH };
			String[] latStrings = { "LAT", "LA" };
			int[] latStrictness = { STARTSWITH, CONTAINS };
			String[] lonStrings = { "LON", "LO" };
			int[] lonStrictness = { STARTSWITH, CONTAINS };
			String[] timeStrings = { "TIME", "TIM" };
			int[] timeStrictness = { CONTAINS, STARTSWITH };
			String[] dateStrings = { "DATE", "DAT", "DAT" };
			int[] dateStrictness = { CONTAINS, CONTAINS, CONTAINS };
			String[] bottStrings = { "BOTTOM", "DEPTH", "WATER DEPTH", "ZBOT" };
			int[] bottStrictness = { STARTSWITH, STARTSWITH, CONTAINS, CONTAINS };
			int expoPos = -1;
			int secNamePos = -1;
			int stnNumPos = -1;
			int castNumPos = -1;
			int latPos = -1;
			int lonPos = -1;
			int timePos = -1;
			int datePos = -1;
			int bottomPos = -1;
			String stnNum = "";
			String secName = "";
			double bottomdbar = MISSINGVALUE;
			int year = MISSINGVALUE;
			int month = MISSINGVALUE;
			int day = MISSINGVALUE;
			int hour = MISSINGVALUE;
			double min = MISSINGVALUE;
			double myLat = 0.0;
			double myLon = 0.0;
			String sectionDescrip = new String("Untitled");
			int castNum = 1;
			String sDate = "";
			String sTime = "";
			for (int nl = 0; nl < numHeaders - 1; nl++) {
				while (true) {
					inLine = in.readLine();
					bytesRead += inLine.length();
					if (inLine.startsWith("#")) {
						mOwnerDBase.addComment(inLine);
					}
					else
						break;
				}
				// find the columns of important stuff
				if (expoPos == -1 && EPS_Util.isItem(1, inLine, expoStrings, expoStrictness)) {
					expoPos = 1;
					sectionDescrip = EPS_Util.getItem(inLine, 2);
				}
				else if (secNamePos == -1 && EPS_Util.isItem(1, inLine, secStrings, secStrictness)) {
					secNamePos = 1;
					secName = EPS_Util.getItem(inLine, 2);
				}
				else if (stnNumPos == -1 && EPS_Util.isItem(1, inLine, stnStrings, stnStrictness)) {
					stnNumPos = 1;
					stnNum = EPS_Util.getItem(inLine, 2);
				}
				else if (castNumPos == -1 && EPS_Util.isItem(1, inLine, castStrings, castStrictness)) {
					castNumPos = 1;
					castNum = EPS_Util.getIntItem(inLine, 2);
				}
				else if (latPos == -1 && EPS_Util.isItem(1, inLine, latStrings, latStrictness)) {
					latPos = 1;
					myLat = EPS_Util.getDoubleItem(inLine, 2);
				}
				else if (lonPos == -1 && EPS_Util.isItem(1, inLine, lonStrings, lonStrictness)) {
					lonPos = 1;
					myLon = EPS_Util.getDoubleItem(inLine, 2);
				}
				else if (timePos == -1 && EPS_Util.isItem(1, inLine, timeStrings, timeStrictness)) {
					timePos = 1;
					sTime = EPS_Util.getItem(inLine, 2);
					while (sTime.length() < 4)
						sTime = "0" + sTime;
				}
				else if (datePos == -1 && EPS_Util.isItem(1, inLine, dateStrings, dateStrictness)) {
					datePos = 1;
					sDate = EPS_Util.getItem(inLine, 2);
				}
				else if (bottomPos == -1 && EPS_Util.isItem(1, inLine, bottStrings, bottStrictness)) {
					bottomPos = 1;
					bottomdbar = EPS_Util.getDoubleItem(inLine, 2);
				}
			}

			// compute the date/time str
			String frmtStr = null;
			String dateTimeStr = null;
			if (timePos != -1) {
				frmtStr = "yyyyMMdd HHmm";
				dateTimeStr = sDate.trim() + " " + sTime.trim();
			}
			else {
				frmtStr = "yyyyMMdd";
				dateTimeStr = sDate;
			}

			// parse the date
			DateFormat df = new SimpleDateFormat(frmtStr);
			Date ddate = df.parse(dateTimeStr);
			Calendar cal = new GregorianCalendar();
			cal.setTime(ddate);
			day = cal.get(Calendar.DAY_OF_MONTH);
			month = cal.get(Calendar.MONTH) + 1;
			year = cal.get(Calendar.YEAR);
			hour = cal.get(Calendar.HOUR_OF_DAY);
			min = cal.get(Calendar.MINUTE);

			// skip any comments lines to look for the parameter columns
			while (true) {
				inLine = in.readLine();
				bytesRead += inLine.length();
				if (inLine.startsWith("#")) {
					mOwnerDBase.addComment(inLine);
				}
				else
					break;
			}

			EPSProperties.SDELIMITER = SCOMMA_DELIMITER;
			EPSProperties.DOUBLEDELIM = EPSProperties.SDELIMITER + EPSProperties.SDELIMITER;
			String[] presStrings = { "CTDPRS", "PRES", "P", "PR", "CTDP" };
			int[] presStrictness = { CONTAINS, STARTSWITH, CONTAINS, CONTAINS, CONTAINS };
			int paramStartPos = EPS_Util.getItemNumber(inLine, presStrings, presStrictness);

			// test for missing required stuff
			if (stnNumPos == -1 || latPos == -1 || lonPos == -1 || paramStartPos == -1) {
				// throw an exception
				FileImportException fie = new FileImportException();
				throw (fie);
			}

			// get the individual parameters
			int ic = paramStartPos;
			int numVars = 0;
			while (true) {
				String param = EPS_Util.getItem(inLine, ic);
				if (param == null || param.length() == 0)
					break;

				// convert varnames to UC
				param.toUpperCase();

				// create new property
				boolean reverse = false;
				if (param.startsWith("PRES") || param.startsWith("CTDP") || param.equals("P") || param.equals("PR")) {
					presPos = numVars;
					reverse = true;
				}
				else
					reverse = false;

				tempProperties[numVars] = new JOAParameter(param, "");

				paramPositions[numVars] = ic;
				ic++;

				// look for the qb in the next item
				String qbStr = EPS_Util.getItem(inLine, ic);
				if (qbStr != null) {
					if (qbStr.equalsIgnoreCase(param + "_FLAG_W")) {
						if (qbStd == null)
							qbStd = new String("WOCE");
						qbPositions[numVars] = ic;
						ic++;
					}
					else if (qbStr.equalsIgnoreCase(param + "_FLAG_I")) {
						if (qbStd == null)
							qbStd = new String("IGOSS");
						qbPositions[numVars] = ic;
						ic++;
					}
				}
				numVars++;
			}

			// get the units in the next non-comment line
			while (true) {
				inLine = in.readLine();
				bytesRead += inLine.length();
				if (inLine.startsWith("#")) {
					mOwnerDBase.addComment(inLine);
				}
				else
					break;
			}

			// expand any null items
			inLine = EPS_Util.expandNullItems(inLine);

			for (int i = 0; i < numVars; i++) {
				String units = EPS_Util.getItem(inLine, paramPositions[i]);

				if (units == null || units.length() == 0)
					continue;
				else
					tempProperties[i].mUnits = new String(units);
			}

			// data loop
			int bc = 0; // number of bottles read
			short[] sarray = new short[1];

			// create an array of arrays to store the data and associated quality
			// bytes
			// limit of 500 observations per station
			double[][] va = new double[numVars][9000];

			// skip to data and intialize stn boundaries
			while (true) {
				inLine = in.readLine();
				bytesRead += inLine.length();
				if (inLine.startsWith("#")) {
					mOwnerDBase.addComment(inLine);
				}
				else
					break;
			}

			inLine = EPS_Util.expandNullItems(inLine);

			// need to read all the bottle data for this stn
			short[][] pqb = new short[numVars][9000];
			while (true) {
				mProgress.setPercentComplete(100.0 * ((double) bytesRead / (double) bytesInFile));
				for (int v = 0; v < numVars; v++) {
					double varVal = 0;
					try {
						varVal = EPS_Util.getDoubleItem(inLine, paramPositions[v]);
					}
					catch (Exception ex) {
						varVal = MISSINGVALUE;
					}

					// store the value in the multidimensional array
					if (varVal != MISSINGVALUE && varVal != -9.0 && varVal != -999.0) {
						va[v][bc] = varVal;
					}
					else
						va[v][bc] = MISSINGVALUE;

					// get the quality bytes (if present)
					short qbVal = MISSINGVALUE;
					try {
						if (qbPositions[v] != MISSINGVALUE)
							qbVal = EPS_Util.getShortItem(inLine, qbPositions[v]);
					}
					catch (Exception ex) {
					}
					pqb[v][bc] = qbVal;

				} // for v!

				// increment the bottle counter
				bc++;

				// read a new line
				inLine = in.readLine();
				bytesRead += inLine.length();
				if (inLine == null || inLine.startsWith("END_DATA")) {
					break;
				}

				inLine = EPS_Util.expandNullItems(inLine);
			}
			// add the global attributes
			if (sectionDescrip != null)
				mOwnerDBase.addEPSAttribute("SECTION_DESCRIPTION", EPSConstants.EPCHAR, sectionDescrip.trim().length(), sectionDescrip.trim());
			if (secName != null)
				mOwnerDBase.addEPSAttribute(EPCRUISE, EPSConstants.EPCHAR, secName.trim().length(), secName.trim());
			if (stnNum != null)
				mOwnerDBase.addEPSAttribute(EPCAST, EPSConstants.EPCHAR, stnNum.trim().length(), stnNum.trim());
			sarray[0] = (short) castNum;
			mOwnerDBase.addEPSAttribute(EPCASTNUM, EPSConstants.EPSHORT, 1, sarray);
			sarray[0] = (short) bottomdbar;
			mOwnerDBase.addEPSAttribute("WATER_DEPTH", EPSConstants.EPSHORT, 1, sarray);
			String dType = "WOCE CTD";
			mOwnerDBase.addEPSAttribute("DATA_TYPE", EPSConstants.EPCHAR, dType.length(), dType);
			if (qbStd != null)
				mOwnerDBase.addEPSAttribute("QUALITY_CODE_STD", EPSConstants.EPCHAR, qbStd.length(), qbStd);
			mOwnerDBase.setDataType("WOCE CTD");

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

			// make the time axis units
			String date = "days since ";
			int imin = (int) min;
			double fmin = min - imin;
			int secs = (int) (fmin * 60.0);
			double fsec = (fmin * 60.0) - secs;
			int msec = (int) (fsec * 1000.0);
			String fs = String.valueOf(fsec);
			fs = fs.substring(fs.indexOf(".") + 1, fs.length()).trim();
			int f = 0;
			if (fs != null && fs.length() > 0)
				f = Integer.valueOf(fs).intValue();

			// sprintf(time_string,"%04d-%02d-%02d
			// %02d:%02d:%02d.%03d",yr,mon,day,hr,min,sec,f);
			String frmt = new String(
			    "{0,number,####}-{1,number,00}-{2,number,00} {3,number,00}:{4,number,00}:{5,number,00}.{6,number,000}");
			MessageFormat msgf = new MessageFormat(frmt);

			Object[] objs = { new Integer(year), new Integer(month), new Integer(day), new Integer(hour), new Integer(imin),
			    new Integer(secs), new Integer(f) };
			StringBuffer out = new StringBuffer();
			msgf.format(objs, out, null);
			String time_string = new String(out);
			date = date + time_string;
			timeAxis.addAttribute(0, "units", EPCHAR, date.length(), date);
			timeAxis.setUnits(date);
			GeoDate[] ta = { new GeoDate(month, day, year, hour, imin, secs, msec) };
			MultiArray tma = new ArrayMultiArray(ta);
			timeAxis.setData(tma);
			mOwnerDBase.setAxis(timeAxis);

			// add the time axes variable
			EPSVariable var = new EPSVariable();
			var.setOname("time");
			var.setDtype(EPDOUBLE);
			var.setVclass(Double.TYPE);
			var.addAttribute(0, "units", EPCHAR, date.length(), date);
			var.setUnits(date);
			double[] vta = { 0.0 };
			MultiArray vtma = new ArrayMultiArray(vta);
			try {
				var.setData(vtma);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			mOwnerDBase.addEPSVariable(var);

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
			// zAxis.addAttribute(2, "type", EPCHAR, 0, "");
			sarray[0] = 1;
			zAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
			double[] za = new double[bc];
			for (int b = 0; b < bc; b++) {
				za[b] = va[presPos][b];
			}
			MultiArray zma = new ArrayMultiArray(za);
			zAxis.setData(zma);
			mOwnerDBase.setAxis(zAxis);

			// add the z axes variables
			var = new EPSVariable();
			var.setOname("depth");
			var.setDtype(EPDOUBLE);
			var.setVclass(Double.TYPE);
			var.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.1");
			var.addAttribute(1, "units", EPCHAR, 4, "dbar");
			var.setUnits("dbar");
			var.setFrmt("f10.1");
			// var.addAttribute(2, "type", EPCHAR, 0, "");
			sarray[0] = 1;
			var.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
			MultiArray zvma = new ArrayMultiArray(za);
			try {
				var.setData(zvma);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			mOwnerDBase.addEPSVariable(var);

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
			// latAxis.addAttribute(2, "type", EPCHAR, 0, "");
			sarray[0] = 500;
			latAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
			double[] la = { myLat };
			MultiArray lma = new ArrayMultiArray(la);
			latAxis.setData(lma);
			mOwnerDBase.setAxis(latAxis);

			// add the y axes variable
			var = new EPSVariable();
			var.setOname("latitude");
			var.setDtype(EPDOUBLE);
			var.setVclass(Double.TYPE);
			var.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.4");
			var.addAttribute(1, "units", EPCHAR, 7, "degrees");
			var.setUnits("degrees");
			var.setFrmt("f10.4");
			// var.addAttribute(2, "type", EPCHAR, 0, "");
			sarray[0] = 500;
			var.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
			MultiArray yvma = new ArrayMultiArray(la);
			try {
				var.setData(yvma);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			mOwnerDBase.addEPSVariable(var);

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
			// lonAxis.addAttribute(2, "type", EPCHAR, 0, "");
			sarray[0] = 502;
			lonAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
			double[] lla = { myLon };
			lma = new ArrayMultiArray(lla);
			lonAxis.setData(lma);
			mOwnerDBase.setAxis(lonAxis);

			// add the x axes variable
			var = new EPSVariable();
			var.setOname("longitude");
			var.setDtype(EPDOUBLE);
			var.setVclass(Double.TYPE);
			var.addAttribute(0, "FORTRAN_format", EPCHAR, 5, "f10.4");
			var.addAttribute(1, "units", EPCHAR, 7, "degrees");
			var.setUnits("degrees");
			var.setFrmt("f10.4");
			// var.addAttribute(2, "type", EPCHAR, 0, "");
			sarray[0] = 502;
			var.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
			MultiArray xvma = new ArrayMultiArray(lla);
			try {
				var.setData(xvma);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}

			mOwnerDBase.addEPSVariable(var);

			// make the measured variables and add the data
			for (int v = 0; v < numVars; v++) {
				// if (presPos == v)
				// continue;

				// create an array of measured EPSVariables for this database
				EPSVariable epsVar = new EPSVariable();

				// initialize the new EPSVariables
				// look this variable up in JOA EPIC_Key. find matching entry in
				// original EPIC Key
				String oname = tempProperties[v].mVarLabel;
				String sname = null;
				String lname = null;
				String gname = null;
				String units = tempProperties[v].mUnits;
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
				if (keyID >= 0) {
					sarray[0] = (short) type;
					// epsVar.addAttribute(numAttributes++, "type", EPSHORT, 1, sarray);
				}
				if (keyID >= 0) {
					sarray[0] = (short) keyID;
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

				if (qbPositions[v] != MISSINGVALUE) {
					// add the quality code attribute
					String qcVar = oname + "_QC";
					epsVar.addAttribute(numAttributes++, "OBS_QC_VARIABLE", EPCHAR, qcVar.length(), qcVar);
				}

				// set the data
				// create storage for the measured variables
				double[][][][] vaa = new double[1][bc][1][1];
				for (int b = 0; b < bc; b++) {
					vaa[0][b][0][0] = va[v][b];
				}
				MultiArray mdma = new ArrayMultiArray(vaa);
				try {
					epsVar.setData(mdma);
				}
				catch (Exception ex) {
					System.out.println("throwing");
				}

				// add the variable to the database
				mOwnerDBase.addEPSVariable(epsVar);

				if (qbPositions[v] != MISSINGVALUE) {
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
					for (int b = 0; b < bc; b++) {
						qcaa[0][b][0][0] = MISSINGVALUE;
						if (qbPositions[v] != MISSINGVALUE) {
							qcaa[0][b][0][0] = pqb[v][b];
						}
					}
					MultiArray qcma = new ArrayMultiArray(qcaa);
					try {
						epsQCVar.setData(qcma);
					}
					catch (Exception ex) {
						System.out.println("throwing");
					}

					// add the qc variable to the database
					mOwnerDBase.addEPSVariable(epsQCVar);
				}
			}
			// for v
			mProgress.setPercentComplete(100.0);
			mProgress.dispose();
			in.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			mProgress.dispose();
			FileImportException fie = new FileImportException();
			fie.setErrorLine(in.getLineNumber() - 1);
			throw (fie);
		}
	}

	/**
	 * Get variable data from section file.
	 * 
	 * @param inName
	 *          Name of variable to get data for
	 * 
	 * @return Multiarray of data
	 * 
	 * @exception EPSVarDoesNotExistExcept
	 *              Variable not found in the owner database
	 * @exception IOException
	 *              An IO error occurred getting the data
	 */
	public MultiArray getvar(String inName) throws EPSVarDoesNotExistExcept, IOException {
		return null;
	}

	public MultiArray getvar(String inName, int[] lci, int[] uci, int[] dims) throws EPSVarDoesNotExistExcept,
	    IOException {
		return null;
	}

}
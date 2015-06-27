package gov.noaa.pmel.eps2;

import java.io.*;
import java.text.*;
import java.awt.*;
import java.util.*;
import javaoceanatlas.utility.JOAFormulas;
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
public class WOCEBottleSectionReader implements EPSFileReader, EPSConstants {
	public static final int DAYS = 1;
	protected boolean mConvertTemps;
	protected boolean mConvMassToVol;
	protected boolean mReplEQ3;
	protected boolean mReplEQ4;
	protected boolean mReplEQ7;
	protected boolean mReplEQ8;
	protected boolean mReplAllEQ4;
	protected boolean mReplGasEQ4and3;
	private boolean DEBUG = false;

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
	protected String mProgressStr = "Reading WOCE Hydro Data...";

	/**
	 * Construct a new <code>WOCEBottleSectionReader</code> with a Dbase amd
	 * file.
	 * 
	 * @param dname
	 *          Dbase that this reader will fill in
	 * @param inFile
	 *          Source section data file
	 * 
	 * @see Dbase
	 */
	public WOCEBottleSectionReader(Dbase dname, File inFile, EpicPtr ep) {
		mOwnerDBase = dname;
		mFile = inFile;
		mOwnerDBase.setEpicPtr(ep);
		if (ep.getProgressStr() != null)
			mProgressStr = new String(ep.getProgressStr());
	}

	/**
	 * Construct a new <code>WOCEBottleSectionReader</code> with a Dbase and
	 * file and a prompt string.
	 * 
	 * @param dname
	 *          Dbase that this reader will fill in
	 * @param inFile
	 *          Source section data file
	 * 
	 * @see Dbase
	 */
	public WOCEBottleSectionReader(Dbase dname, File inFile, EpicPtr ep, String progress) {
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
		int[] paramPositions = new int[100];
		int[] qbPositions = new int[100];
		for (int i = 0; i < 100; i++) {
			qbPositions[i] = MISSINGVALUE;
		}
		String inLine = new String();
		LineNumberReader in = null;
		long bytesRead = 0;
		long bytesInFile = mFile.length();
		int presPos = -99;
		boolean hasMeasuredPres = false;
		String qbStd = null;
		int currLine = 0;

		EPSProgressDialog mProgress = new EPSProgressDialog(new Frame(), mProgressStr, Color.blue);
		mProgress.setVisible(true);

		// Get an epic key database specific to JOA
		EPIC_Key_DB mEpicKeyDB = new EPIC_Key_DB("joa_epic.key");

		// Get an epic key database specific to JOA
		EPIC_Key_DB mOrigEpicKeyDB = new EPIC_Key_DB("epic.key");

		in = new LineNumberReader(new FileReader(mFile), 500000);

		// create a new open file object
		// JOADataFile of = new JOADataFile("Untitled");

		// create a vector for temporary storage of the dbases
		Vector dBases = new Vector(100);

		// read to the column header line
		while (true) {
			inLine = in.readLine();

			if (DEBUG) {
				System.out.println(currLine + " " + inLine);
			}
			currLine++;
			bytesRead += inLine.length();
			currLine++;
			if (inLine.startsWith("#")) {
				mOwnerDBase.addComment(inLine);
			}
			else if (inLine.length() > 0)
				break;
		}

		// should have the first significant line
		if (!inLine.startsWith("BOT")) {
			// not a bottle file
			FileImportException fie = new FileImportException();
			fie.setErrorLine(1);
			fie.setErrorType("File doesn't start with BOT");
			throw (fie);
		}
		else {
			mOwnerDBase.addComment(inLine);

			// read to next noncommented line
			while (true) {
				inLine = in.readLine();

				if (DEBUG) {
					System.out.println(currLine + " " + inLine);
				}
				currLine++;
				bytesRead += inLine.length();
				if (inLine.startsWith("#")) {
					mOwnerDBase.addComment(inLine);
				}
				else if (inLine.length() > 0)
					break;
			}
		}

		// get to here when we found the header lines
		// convert string to uppercase
		String inLineLC = new String(inLine);
		inLine = inLine.toUpperCase();

		// find the columns of important stuff
		EPSProperties.SDELIMITER = SCOMMA_DELIMITER;
		EPSProperties.DOUBLEDELIM = EPSProperties.SDELIMITER + EPSProperties.SDELIMITER;

		String[] expoStrings = { "EXPOCODE", "EXPO" };
		int[] expoStrictness = { MATCHES, STARTSWITH };
		int expoCodePos = EPS_Util.getItemNumber(inLine, expoStrings, expoStrictness);

		String[] secStrings = { "SECT_ID", "SEC", "SE", "WHP-ID", "PROJECT", "ID", "NAME" };
		int[] secStrictness = { MATCHES, STARTSWITH, MATCHES, MATCHES, MATCHES, MATCHES, MATCHES };
		int secNamePos = EPS_Util.getItemNumber(inLine, secStrings, secStrictness);

		String[] shipStrings = { "SHIP", "SHP", "SH", "VESSEL", "SHIP" };
		int[] shipStrictness = { MATCHES, MATCHES, MATCHES, MATCHES, STARTSWITH };
		int shipPos = EPS_Util.getItemNumber(inLine, shipStrings, shipStrictness);

		String[] stnStrings = { "STNNBR", "STA", "STATION", "ST", "STN", };
		int[] stnStrictness = { MATCHES, MATCHES, MATCHES, STARTSWITH, STARTSWITH };
		int stnNumPos = EPS_Util.getItemNumber(inLine, stnStrings, stnStrictness);

		String[] castStrings = { "CASTNO", "CA", "CAST", "CST" };
		int[] castStrictness = { MATCHES, MATCHES, STARTSWITH, STARTSWITH };
		int castNumPos = EPS_Util.getItemNumber(inLine, castStrings, castStrictness);

		String[] sampStrings = { "SAMPNO" };
		int[] sampStrictness = { MATCHES };
		int sampPos = EPS_Util.getItemNumber(inLine, sampStrings, sampStrictness);

		String[] latStrings = { "LAT", "LA" };
		int[] latStrictness = { STARTSWITH, MATCHES };
		int latPos = EPS_Util.getItemNumber(inLine, latStrings, latStrictness);

		String[] lonStrings = { "LON", "LO" };
		int[] lonStrictness = { STARTSWITH, MATCHES };
		int lonPos = EPS_Util.getItemNumber(inLine, lonStrings, lonStrictness);

		String[] timeStrings = { "TIME", "TIM" };
		int[] timeStrictness = { MATCHES, STARTSWITH };
		int timePos = EPS_Util.getItemNumber(inLine, timeStrings, timeStrictness);

		String[] dateStrings = { "DA", "DAT", "DATE", "DATE" };
		int[] dateStrictness = { MATCHES, MATCHES, CONTAINS, MATCHES };
		int datePos = EPS_Util.getItemNumber(inLine, dateStrings, dateStrictness);

		String[] bottStrings = { "DEPTH", "BOTTOM", "WATER DEPTH", "ZBOT" };
		int[] bottStrictness = { MATCHES, STARTSWITH, MATCHES, MATCHES };
		int bottomPos = EPS_Util.getItemNumber(inLine, bottStrings, bottStrictness);

		String[] botNumStrings = { "BTLNBR", "BOT", "BTL", "SER#" };
		int[] botNumStrictness = { MATCHES, STARTSWITH, MATCHES, MATCHES };
		int botNumPos = EPS_Util.getItemNumber(inLine, botNumStrings, botNumStrictness);

		String[] rawCTDStrings = { "CTDRAW" };
		int[] rawCTDStrictness = { MATCHES };
		int rawCTDPos = EPS_Util.getItemNumber(inLine, rawCTDStrings, rawCTDStrictness);

		int botNumQBPos = -1;
		if (botNumPos >= 0) {
			// look for the quality flags for bottle
			String bnStr = EPS_Util.getItem(inLine, botNumPos);
			String qbStr = EPS_Util.getItem(inLine, botNumPos + 1);
			if (qbStr != null) {
				if (qbStr.indexOf("_FLAG") > 0) {
					botNumQBPos = botNumPos + 1;
					if (qbStr.indexOf("_W") > 0)
						qbStd = "WOCE";
					else if (qbStr.indexOf("_I") > 0)
						qbStd = "IGOSS";
				}
			}
		}

		String[] presStrings = { "CTDPRS", "PRES", "P", "PR", "CTDP", "REVPRS" };
		int[] presStrictness = { MATCHES, STARTSWITH, MATCHES, MATCHES, MATCHES, MATCHES };
		int paramStartPos = EPS_Util.getItemNumber(inLine, presStrings, presStrictness);

		if (DEBUG) {
			System.out.println("stnNumPos = " + stnNumPos);
			System.out.println("latPos = " + latPos);
			System.out.println("lonPos = " + lonPos);
			System.out.println("paramStartPos = " + paramStartPos);
		}

		// test for missing required stuff
		if (stnNumPos == -1 || latPos == -1 || lonPos == -1 || paramStartPos == -1) {
			// throw an exception
			System.out.println("stnNumPos = " + stnNumPos);
			System.out.println("latPos = " + latPos);
			System.out.println("lonPos = " + lonPos);
			System.out.println("paramStartPos = " + paramStartPos);
			FileImportException fie = new FileImportException();
			fie.setErrorLine(currLine);
			String msg = new String("Station missing required info: ");
			if (stnNumPos == -1)
				msg += "Station Number ";
			if (latPos == -1)
				msg += "Latitude ";
			if (lonPos == -1)
				msg += "Longitude ";
			if (paramStartPos == -1)
				msg += "Pressure ";
			fie.setErrorType("msg");
			throw (fie);
		}

		// get the individual parameters
		int ic = paramStartPos;
		int numVars = 0;
		try {
			while (true) {
				String param = EPS_Util.getItem(inLineLC, ic);

				if (DEBUG) {
					System.out.println(" param = " + param);
				}
				if (param == null || param.length() == 0)
					break;

				// convert varnames to UC
				param.toUpperCase();

				// create new property
				boolean reverse = false;
				boolean isPres = param.startsWith("PRES") || param.startsWith("CTDP") || param.equals("P") || param.equals("PR");
				boolean isRevPres = param.equals("REVPRS");
				
				if (isPres || isRevPres) {
					if (isPres && !hasMeasuredPres) {
						presPos = numVars;
						hasMeasuredPres = true;
					}
					else if (isRevPres && !hasMeasuredPres) {
						presPos = numVars;
					}
					reverse = true;
					param = param.substring(0, 4);
				}
				else
					reverse = false;

				tempProperties[numVars] = new JOAParameter(param, "");
				paramPositions[numVars] = ic++;

				// look for the qb in the next item
				String qbStr = null;
				qbStr = EPS_Util.getItem(inLine, ic);
				if (qbStr != null && qbStr.indexOf("_FLAG") >= 0) {
					// found qc field
					qbPositions[numVars] = ic;
					ic++;
					
					// set QC standard of necessary
					if (qbStd == null && qbStr.indexOf("_FLAG") > 0) {
						if (qbStr.indexOf("_W") > 0)
							qbStd = "WOCE";
						else if (qbStr.indexOf("_I") > 0)
							qbStd = "IGOSS";
					}
				}
				numVars++;
			}
		}
		catch (Exception ex) {
			FileImportException fie = new FileImportException();
			fie.setErrorLine(currLine);
			fie.setErrorType("An error occurred parsing the parameters");
			throw (fie);
		}

		// get the units in the next non-comment line
		while (true) {
			inLine = in.readLine();
			if (DEBUG) {
				System.out.println(currLine + " " + inLine);
			}

			currLine++;
			bytesRead += inLine.length();
			if (inLine.startsWith("#")) {
				mOwnerDBase.addComment(inLine);
			}
			else if (inLine.length() > 0)
				break;
		}

		try {
			// expand any null items
			inLine = EPS_Util.expandNullItems(inLine);

			for (int i = 0; i < numVars; i++) {
				String units = EPS_Util.getItem(inLine, paramPositions[i]);
				if (DEBUG) {
					System.out.println(" units = " + units);
				}

				if (units == null || units.length() == 0)
					continue;
				else
					tempProperties[i].mUnits = new String(units);
			}
		}
		catch (Exception ex) {
			FileImportException fie = new FileImportException();
			fie.setErrorLine(currLine);
			fie.setErrorType("An error occurred parsing the units");
			throw (fie);
		}

		// data loop
		int bc = 0; // number of bottles read
		int s = 0; // number of sections read
		String oldSecDescrip = new String("");
		String oldStn = new String();
		String stnNum = "";
		int castNum = 0, oldCast = 0;
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
		String expoCode = null;
		String sampNo;
		boolean capturedPrecisions = false;

		// create an array of arrays to store the data and associated quality bytes
		// limit of 500 observations per station
		double[][] va = new double[numVars][500];
		short[] bqc = new short[500];
		int[] samps = new int[500];
		int[] botnums = new int[500];
		float[] rawCTDs = new float[500];

		// skip to data and intialize stn boundaries
		while (true) {
			inLine = in.readLine();

			if (DEBUG) {
				System.out.println(currLine + " " + inLine);
			}
			currLine++;
			bytesRead += inLine.length();
			if (inLine.startsWith("#")) {
				mOwnerDBase.addComment(inLine);
			}
			else if (inLine.length() > 0)
				break;
		}

		// expand any null items
		inLine = EPS_Util.expandNullItems(inLine);

		oldStn = EPS_Util.getItem(inLine, stnNumPos);
		if (castNumPos != -1)
			oldCast = EPS_Util.getIntItem(inLine, castNumPos);
		
		boolean eof = false;
		boolean first = true;
		while (!eof) {
			mProgress.setPercentComplete(100.0 * ((double) bytesRead / (double) bytesInFile));

			// read the expocode
			if (expoCodePos != -1) {
				expoCode = EPS_Util.getItem(inLine, expoCodePos);
			}

			// section name
			try {
				if (secNamePos != -1)
					sectionDescrip = EPS_Util.getItem(inLine, secNamePos);
				else
					sectionDescrip = new String("Untitled");
			}
			catch (Exception ex) {
				if (DEBUG) {
					System.out.println(currLine + " " + "An error occurred parsing the section description");
				}

				FileImportException fie = new FileImportException();
				fie.setErrorLine(currLine);
				fie.setErrorType("An error occurred parsing the section description");
				throw (fie);
			}

			// ship code
			try {
				shipCode = null;
				if (shipPos != -1)
					shipCode = EPS_Util.getItem(inLine, shipPos);
				else
					shipCode = new String("  ");
			}
			catch (Exception ex) {
				if (DEBUG) {
					System.out.println(currLine + " " + "An error occurred parsing the ship code");
				}
				FileImportException fie = new FileImportException();
				fie.setErrorLine(currLine);
				fie.setErrorType("An error occurred parsing the ship code");
				throw (fie);
			}

			// lat
			try {
				myLat = EPS_Util.getDoubleItem(inLine, latPos);
			}
			catch (Exception ex) {
				if (DEBUG) {
					System.out.println(currLine + " " + "An error occurred parsing the latitude");
				}
				FileImportException fie = new FileImportException();
				fie.setErrorLine(currLine);
				fie.setErrorType("An error occurred parsing the latitude");
				throw (fie);
			}

			// lon
			try {
				myLon = EPS_Util.getDoubleItem(inLine, lonPos);
			}
			catch (Exception ex) {
				if (DEBUG) {
					System.out.println(currLine + " " + "An error occurred parsing longitude");
				}
				FileImportException fie = new FileImportException();
				fie.setErrorLine(currLine);
				fie.setErrorType("An error occurred parsing the longitude");
				throw (fie);
			}

			// date
			boolean timeIsMissing = false;
			boolean dateIsMissing = false;
			try {
				year = MISSINGVALUE;
				month = MISSINGVALUE;
				day = MISSINGVALUE;
				hour = MISSINGVALUE;
				min = MISSINGVALUE;
				if (datePos != -1) {
					String sDate = EPS_Util.getItem(inLine, datePos);
					String sTime = "";
					String frmtStr = null;
					String dateTimeStr = null;

					if (timePos != -1) {
						frmtStr = "yyyyMMdd HHmm";
						sTime = EPS_Util.getItem(inLine, timePos);
						sTime = sTime.trim();
						while (sTime.length() < 4)
							sTime = "0" + sTime;
						if (sTime.indexOf(':') > 0) {
							// funny time with colon delimiters
							frmtStr = "yyyyMMdd HH:mm:ss";
						}
						dateTimeStr = sDate.trim() + " " + sTime.trim();
					}
					else {
						frmtStr = "yyyyMMdd";
						dateTimeStr = sDate;
					}

					// parse the date
					DateFormat df = new SimpleDateFormat(frmtStr);
					Date date = df.parse(dateTimeStr);
					Calendar cal = new GregorianCalendar();
					cal.setTime(date);
					day = cal.get(Calendar.DAY_OF_MONTH);
					month = cal.get(Calendar.MONTH) + 1;
					year = cal.get(Calendar.YEAR);
					hour = cal.get(Calendar.HOUR_OF_DAY);
					min = cal.get(Calendar.MINUTE);
				}
				else {
					dateIsMissing = true;
					if (timePos != -1) {
						
					}
					else {
						timeIsMissing = true;
					}
				}
			}
			catch (Exception ex) {
				timeIsMissing = true;
				dateIsMissing = true;
				// silent
//				if (DEBUG) {
//					System.out.println(currLine + " " + "An error occurred parsing the date");
//				}
//				FileImportException fie = new FileImportException();
//				ex.printStackTrace();
//				fie.setErrorLine(currLine);
//				fie.setErrorType("An error occurred parsing the date");
//				throw (fie);
			}

			// read bottom
			try {
				bottomdbar = MISSINGVALUE;
				if (bottomPos != -1)
					bottomdbar = EPS_Util.getDoubleItem(inLine, bottomPos);
			}
			catch (Exception ex) {
				if (DEBUG) {
					System.out.println(currLine + " " + "An error occurred parsing the bottom");
				}
				FileImportException fie = new FileImportException();
				fie.setErrorLine(currLine);
				fie.setErrorType("An error occurred parsing the bottom");
				throw (fie);
			}

			// cast number
			try {
				if (castNumPos != -1)
					castNum = EPS_Util.getIntItem(inLine, castNumPos);
				else
					castNum = 1;
			}
			catch (Exception ex) {
				if (DEBUG) {
					System.out.println(currLine + " " + "An error occurred parsing the cast number");
				}
				FileImportException fie = new FileImportException();
				fie.setErrorLine(currLine);
				fie.setErrorType("An error occurred parsing the cast number");
				throw (fie);
			}

			// need to read all the bottle data for this stn
			short[][] pqb = new short[numVars][100];
			try {
				while (true) {
					try {
						// read the sampno
						if (sampPos != -1) {
							samps[bc] = EPS_Util.getIntItem(inLine, sampPos);
						}
					}
					catch (Exception e1) {
						if (DEBUG) {
							System.out.println(currLine + " " + "An error occurred parsing the sample number");
						}
						FileImportException fie = new FileImportException();
						fie.setErrorLine(currLine);
						fie.setErrorType("An error occurred parsing the sample number");
						throw (fie);
					}

					try {
						// read the rawCTD
						if (rawCTDPos != -1) {
							rawCTDs[bc] = (float)EPS_Util.getDoubleItem(inLine, rawCTDPos);
						}
					}
					catch (Exception e1) {
						if (DEBUG) {
							System.out.println(currLine + " " + "An error occurred parsing the raw CTD value");
						}
						FileImportException fie = new FileImportException();
						fie.setErrorLine(currLine);
						fie.setErrorType("An error occurred parsing the raw CTD value");
						throw (fie);
					}

					// read the bottle number
					try {
						if (botNumPos != -1) {
							botnums[bc] = EPS_Util.getIntItem(inLine, botNumPos);
						}
					}
					catch (Exception e1) {
						if (DEBUG) {
							System.out.println(currLine + " " + "An error occurred parsing the bottle number");
						}
						FileImportException fie = new FileImportException();
						fie.setErrorLine(currLine);
						fie.setErrorType("An error occurred parsing the bottle number");
						throw (fie);
					}

					// read the station number
					try {
						stnNum = EPS_Util.getItem(inLine, stnNumPos);
					}
					catch (Exception e1) {
						if (DEBUG) {
							System.out.println(currLine + " " + "An error occurred parsing the station number");
						}
						FileImportException fie = new FileImportException();
						fie.setErrorLine(currLine);
						fie.setErrorType("An error occurred parsing the station number");
						throw (fie);
					}

					try {
						if (!stnNum.equalsIgnoreCase(oldStn)) {
							newSection = true;
							if (castNumPos != -1)
								castNum = EPS_Util.getIntItem(inLine, castNumPos);
							else
								castNum = 1;
							// oldCast = castNum;
							break;
						}
					}
					catch (Exception e1) {
						if (DEBUG) {
							System.out.println(currLine + " " + "An error occurred parsing the old cast number");
						}
						FileImportException fie = new FileImportException();
						fie.setErrorLine(currLine);
						fie.setErrorType("An error occurred parsing old cast number");
						throw (fie);
					}

					try {
						// cast number
						if (castNumPos != -1) {
							castNum = EPS_Util.getIntItem(inLine, castNumPos);
							if (castNum != oldCast) {
								newSection = true;
								break;
							}
						}
						else
							castNum = 1;
					}
					catch (Exception e1) {
						if (DEBUG) {
							System.out.println(currLine + " " + "An error occurred parsing the cast number");
						}
						FileImportException fie = new FileImportException();
						fie.setErrorLine(currLine);
						fie.setErrorType("An error occurred parsing the cast number");
						throw (fie);
					}

					for (int v = 0; v < numVars; v++) {
						double varVal = 0;
						try {
							varVal = EPS_Util.getDoubleItem(inLine, paramPositions[v]);
						}
						catch (Exception ex) {
							varVal = MISSINGVALUE;
						}
						
						String strVal = EPS_Util.getItem(inLine, paramPositions[v]);
						String[] tokens = strVal.split("[.]");
						
						// precision can be captured from missing values so only need to do this once
						if (!capturedPrecisions) {
							if (tokens.length > 1) {
								int prec = tokens[1].length();
								tempProperties[v].setDisplayPrecision(prec);
							}
						}
						
						int sigdig;
						if (varVal != MISSINGVALUE && varVal != -9.0 && varVal != -999.0) {
							sigdig = JOAFormulas.getSignificantDigits(strVal.trim());
							tempProperties[v].setSignificantDigits(sigdig);
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
					capturedPrecisions = true;

					// get the bottle quality codes if necessary
					if (botNumQBPos != -1) {
						try {
							// has quality code
							bqc[bc] = EPS_Util.getShortItem(inLine, botNumQBPos);
						}
						catch (Exception ex) {
							if (DEBUG) {
								System.out.println(currLine + " " + "An error occurred parsing the quality code");
							}
							FileImportException fie = new FileImportException();
							fie.setErrorLine(currLine);
							fie.setErrorType("An error occurred getting the bottle quality code");
							throw (fie);
						}
					}

					// get the bottle number as a parameter

					// increment the bottle counter
					bc++;

					// read a new line
					inLine = in.readLine();
					currLine++;
					bytesRead += inLine.length();
					if (inLine == null || inLine.startsWith("END_DATA")) {
						eof = true;
						newSection = true;
						break;
					}
					try {
						// expand any null items
						inLine = EPS_Util.expandNullItems(inLine);
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			catch (Exception e1) {
				if (DEBUG) {
					System.out.println("An error occurred parsing a data record on line " + currLine);
					System.out.println(inLine);
				}
				FileImportException fie = new FileImportException();
				fie.setErrorLine(currLine);
				fie.setErrorType("An error occurred parsing the data at line at " + currLine);
				throw (fie);
			}

			if (newSection) {
				// new Station found--make a new dBase
				mTotalStations++;
				// make a DBase object
				Dbase db = new Dbase();
				// add the global attributes
				if (sectionDescrip != null)
					db.addEPSAttribute("CRUISE", EPSConstants.EPCHAR, sectionDescrip.length(), sectionDescrip);
				db.addEPSAttribute("CAST", EPSConstants.EPCHAR, oldStn.length(), oldStn);
				sarray[0] = (short) oldCast;
				db.addEPSAttribute("CAST_NUMBER", EPSConstants.EPSHORT, 1, sarray);
				sarray[0] = (short) bottomdbar;
				db.addEPSAttribute("WATER_DEPTH", EPSConstants.EPSHORT, 1, sarray);
				if (shipCode != null)
					db.addEPSAttribute("DATA_ORIGIN", EPSConstants.EPCHAR, shipCode.length(), shipCode);
				String dType = "WOCE BOTTLE";
				db.addEPSAttribute("DATA_TYPE", EPSConstants.EPCHAR, dType.length(), dType);
				if (qbStd != null)
					db.addEPSAttribute("QUALITY_CODE_STD", EPSConstants.EPCHAR, qbStd.length(), qbStd);

				if (expoCode != null)
					db.addEPSAttribute("EXPOCODE", EPSConstants.EPCHAR, expoCode.length(), expoCode);

				db.setDataType("WOCE BOTTLE");

				if (botNumQBPos != -1)
					db.addEPSAttribute("BOTTLE_QUALITY_CODES", EPSHORT, bc, bqc);

				if (sampPos != -1)
					db.addEPSAttribute("SAMP_NOS", EPINT, bc, samps);
				
				if (rawCTDPos != -1)
					db.addEPSAttribute("RAW_CTD", EPREAL, bc, rawCTDs);

				if (botNumPos != -1)
					db.addEPSAttribute("BTLNBR", EPINT, bc, botnums);

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

				Object[] objs = { new Integer(year), new Integer(month), new Integer(day), new Integer(hour),
				    new Integer(imin), new Integer(secs), new Integer(f) };
				StringBuffer out = new StringBuffer();
				msgf.format(objs, out, null);
				String time_string = new String(out);
				date = date + time_string;
				timeAxis.addAttribute(0, "units", EPCHAR, date.length(), date);
				timeAxis.setUnits(date);
				GeoDate[] ta = new GeoDate[1];
				if (!timeIsMissing) {
					ta[0] = new GeoDate(month, day, year, hour, imin, secs, msec);
				}
				else {
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
				double[] vta = { 0.0 };
				MultiArray vtma = new ArrayMultiArray(vta);
				try {
					var.setData(vtma);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				db.addEPSVariable(var);

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
				// latAxis.addAttribute(2, "type", EPCHAR, 0, "");
				sarray[0] = 500;
				latAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
				double[] la = { myLat };
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
				// lonAxis.addAttribute(2, "type", EPCHAR, 0, "");
				sarray[0] = 502;
				lonAxis.addAttribute(2, "epic_code", EPSHORT, 1, sarray);
				double[] lla = { myLon };
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

				db.addEPSVariable(var);

				// make the measured variables and add the data
				for (int v = 0; v < numVars; v++) {
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
						String keyUnits = key.getUnits();
						String fileUnits = tempProperties[v].mUnits;
						
						// file units always trump canonical units
						if (!fileUnits.equalsIgnoreCase(keyUnits)) {
							units = fileUnits;
						}
						else {
							units = keyUnits;
						}
						ffrmt = key.getFrmt();
						type = key.getType();
					}
					catch (Exception e) {
						lname = tempProperties[v].mVarLabel;
						gname = tempProperties[v].mVarLabel;
						sname = tempProperties[v].mVarLabel;
						units = tempProperties[v].mUnits;
					}
					
					int prec = tempProperties[v].getDisplayPrecision();
					int sigdig = tempProperties[v].getSignificantDigits();

					// make a new variable
					epsVar = new EPSVariable();

					epsVar.setOname(oname);
					epsVar.setDisplayPrecision(prec);
					epsVar.setSignificantDigits(sigdig);
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
						ex.printStackTrace();
					}

					// add the variable to the database
					db.addEPSVariable(epsVar);

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
							ex.printStackTrace();
						}

						// add the qc variable to the database
						db.addEPSVariable(epsQCVar);
					}
				} // for v

				// add to temporary collection
				dBases.addElement(db);

				// reset the bottle count
				bc = 0;
				newSection = false;
				oldStn = stnNum;
				oldCast = castNum;
			} // if new section
			
			// reconcile the significant digits in tempProperties with the db 
//			foobar = 34;
//			for (int v = 0; v < numVars; v++) {
//				int sigdig = tempProperties[v].getSignificantDigits();
//				db.
//			}
		} // while !eof
		in.close();

		// make a sub database in the dbase
		mOwnerDBase.createSubEntries(dBases.size(), mFile.getName());
		for (int d = 0; d < dBases.size(); d++) {
			Dbase db = (Dbase) dBases.elementAt(d);
			try {
				for (int v=0; v<tempProperties.length; v++) {
					EPSVariable epv = db.getEPSVariable(tempProperties[v].mVarLabel);
					if (epv != null) {
						epv.setSignificantDigits(tempProperties[v].getSignificantDigits());
					}
				}
			}
			catch (Exception ee) {
				// silent
			}
			mOwnerDBase.addSubEntry(db);
		}

		mProgress.setPercentComplete(100.0);
		mProgress.dispose();
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
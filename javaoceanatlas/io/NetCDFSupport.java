/*
 * $Id: NetCDFSupport.java,v 1.14 2005/09/07 18:42:47 oz Exp $
 *
 */

package javaoceanatlas.io;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.text.*;
import gov.noaa.pmel.util.*;
import gov.noaa.pmel.eps2.*;
import ucar.multiarray.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.io.*;
import javaoceanatlas.ui.*;
import com.apple.mrj.*;

public class NetCDFSupport {
	public static void writeNetCDF_XML(File outFile, FileViewer fv) throws Exception {
		ProgressDialog progress = new ProgressDialog(fv, "Writing netCDF Profile Data...", Color.blue, Color.white);
		progress.setVisible(true);

		// outfile is the pointer file
		String dir = outFile.getParent();
		String ptrFileName = outFile.getName();

		// instantiate a pointer file object
		EpicPtrs ptrDB = null;
		ArrayList filePtrs = new ArrayList();
		XMLPtrFileWriter mXMLWriter = null;

		// objects needed for XML output
		String startPath = new String(outFile.getParent());
		PointerFileAttributes xmlattributes = fv.getXMLAttributes();

		xmlattributes.setPath("file:" + "/" + "/" + startPath);

		// add attributes and comments here
		xmlattributes.addAttribute("JOA Version", "4.0.1");
		xmlattributes.addComment("A dummy comment for testing XML Writing");
		xmlattributes.setMissingValue(JOAConstants.MISSINGVALUE);
		xmlattributes.setCreationDate("Today");

		// set the writer class for the pointer file
		boolean isXML = false;
		String lcName = ptrFileName.toLowerCase();
		if (lcName.indexOf(".xml") > 0) {
			ptrDB = new EpicPtrs(outFile, true);
			mXMLWriter = new XMLPtrFileWriter(outFile);
			ptrDB.setWriter(mXMLWriter);
			isXML = true;
		}
		else {
			ptrDB = new EpicPtrs(outFile);
			ptrDB.setWriter(new EpicPtrFileWriter(outFile));
		}

		// Get an epic key database specific to JOA
		EPIC_Key_DB mEpicKeyDB = new EPIC_Key_DB("epic.key");

		// Get an epic key database specific to JOA
		EPIC_Key_DB mOrigEpicKeyDB = new EPIC_Key_DB("epic.key");

		// check whether there is any quality code information in the data
		// collection
		boolean isStnQual = false;
		boolean isBottleQual = false;
		boolean isObsQual = false;
		for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = of.getSection(sec);

				if (sech.mNumCasts == 0) {
					continue;
				}

				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					if (sh.mVarFlag != JOAConstants.MISSINGVALUE) {
						isStnQual = true;
					}

					for (int b = 0; b < sh.mNumBottles; b++) {
						Bottle bh = (Bottle) sh.mBottles.elementAt(b);

						if (bh.mQualityFlag != JOAConstants.MISSINGVALUE) {
							isBottleQual = true;
						}

						for (int i = 0; i < fv.gNumProperties && !isObsQual; i++) {
							// if
							// (fv.mAllProperties[i].getVarLabel().equalsIgnoreCase("PRES"))
							// continue;
							int vPos = sech.getVarPos(fv.mAllProperties[i].getVarLabel(), false);
							if (vPos == -1) {
								continue;
							}
							if (bh.mQualityFlags[i] != JOAConstants.MISSINGVALUE) {
								isObsQual = true;
								break;
							}
						}
					}
				}
			}
		}

		// loop on the casts: stations become individual files
		int s = 0;
		int totalStnsToWrite = JOAFormulas.getNumberVisStns(fv);

		int lastGoodMonth = 1, lastGoodDay = 1;
		short[] bqc = new short[500];

		// get the units of the index variable
		String zUnits = "na";
		int pPos = fv.getPRESPropertyPos();
		if (pPos >= 0) {
			zUnits = new String(fv.mAllProperties[pPos].getUnits());
		}

		for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = of.getSection(sec);

				if (sech.mNumCasts == 0) {
					continue;
				}

				// get the measured variables for this section: could be
				// a subset of the total number of vars. These variables are written to
				// the
				// ExportFileSet
				ArrayList exportVars = new ArrayList();
				for (int i = 0; i < sech.mNumProperties; i++) {
					ExportVariable expvar = new ExportVariable(sech.getParam(i), sech.getParamUnits(i), "JOA");
					exportVars.add(expvar);

					// TODO: Add attributes and comments if they exist
				}

				ArrayList stns = new ArrayList();
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					progress.setPercentComplete(100.0 * ((double) s / (double) totalStnsToWrite));

					// make the filename from the section description
					String outFileName = sech.mSectionDescription + "_" + sh.mStnNum + ".nc";

					// make a DBase object
					Dbase db = new Dbase();

					// add the global attributes
					short[] sarray = new short[1];

					// db.addEPSAttribute("CREATION_DATE", EPCHAR, 8, "Today");
					db
					    .addEPSAttribute("CRUISE", EPSConstants.EPCHAR, sech.mSectionDescription.length(),
					        sech.mSectionDescription);
					db.addEPSAttribute("CAST", EPSConstants.EPCHAR, sh.mStnNum.length(), sh.mStnNum);
					sarray[0] = (short) sh.mBottomDepthInDBARS;
					db.addEPSAttribute("WATER_DEPTH", EPSConstants.EPSHORT, 1, sarray);
					db.addEPSAttribute("DATA_ORIGIN", EPSConstants.EPCHAR, sech.getShipCode().length(), sech.getShipCode());
					String dType = sh.getType();
					String lex = "JOA";
					if (dType == null) {
						dType = "UNKN";
						lex = "JOA";
					}
					else {
						if (dType.indexOf("JOA") >= 0) {
							lex = "JOA";
						}
						else if (dType.indexOf("WOCE") >= 0) {
							lex = "WOCE";
						}
					}
					if (isStnQual || isBottleQual) {
						if (fv.getQCStd() == JOAConstants.IGOSS_QC_STD) {
							db.addEPSAttribute("QUALITY_CODE_STD", EPSConstants.EPCHAR, 5, "IGOSS");
						}
						else if (fv.getQCStd() == JOAConstants.WOCE_QC_STD) {
							db.addEPSAttribute("QUALITY_CODE_STD", EPSConstants.EPCHAR, 5, "WOCE");
						}
					}
					db.addEPSAttribute("DATA_TYPE", EPSConstants.EPCHAR, dType.length(), dType);
					db.addEPSAttribute("LEXICON", EPSConstants.EPCHAR, lex.length(), lex);
					if (isStnQual) {
						// write a station quality attribute
						sarray[0] = (short) sh.mVarFlag;
						db.addEPSAttribute("STN_QUALITY", EPSConstants.EPSHORT, 1, sarray);
					}
					if (isBottleQual) {
						// write a bottle quality attribute
						// bqc = null;
						// bqc = new short[sh.mNumBottles];
						for (int b = 0; b < sh.mNumBottles; b++) {
							// read the bottle quality code
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);
							bqc[b] = bh.mQualityFlag;
						}
						EPSAttribute epa = db.addEPSAttribute("BOTTLE_QUALITY_CODES", EPSConstants.EPSHORT, 36, bqc);
					}

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

					int hour = 0;
					if (sh.mHour != JOAConstants.MISSINGVALUE) {
						hour = sh.mHour;
					}

					double mins = 0;
					if (sh.mMinute != JOAConstants.MISSINGVALUE) {
						mins = sh.mMinute;
					}

					// make the time axis units
					String date = "days since ";
					int min = (int) mins;
					double fmin = mins - min;
					int secs = (int) (fmin * 60.0);
					double fsec = (fmin * 60.0) - secs;
					int msec = (int) (fsec * 1000.0);
					String fs = String.valueOf(fsec);
					fs = fs.substring(fs.indexOf(".") + 1, fs.length()).trim();
					int f = 0;
					if (fs != null && fs.length() > 0 && fs.indexOf('E') < 0) {
						f = Integer.valueOf(fs).intValue();
					}

					// sprintf(time_string,"%04d-%02d-%02d
					// %02d:%02d:%02d.%03d",yr,mon,day,hr,min,sec,f);
					String frmt = new String(
					    "{0,number,####}-{1,number,00}-{2,number,00} {3,number,00}:{4,number,00}:{5,number,00}.{6,number,000}");
					MessageFormat msgf = new MessageFormat(frmt);

					Object[] objs = { new Integer(sh.mYear), new Integer(sh.mMonth), new Integer(sh.mDay), new Integer(hour),
					    new Integer(min), new Integer(secs), new Integer(f) };
					StringBuffer out = new StringBuffer();
					msgf.format(objs, out, null);
					String time_string = new String(out);
					date = date + time_string;
					timeAxis.addAttribute(0, "units", EPSConstants.EPCHAR, date.length(), date);
					timeAxis.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
					double[] ta = { 0.0 };
					MultiArray tma = new ArrayMultiArray(ta);
					timeAxis.setData(tma);
					db.setAxis(timeAxis);

					// add the time axes variable
					EPSVariable var = new EPSVariable();
					var.setOname("time");
					var.setDtype(EPSConstants.EPDOUBLE);
					var.setVclass(Double.TYPE);
					var.addAttribute(0, "units", EPSConstants.EPCHAR, date.length(), date);
					var.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
					double[] vta = { 0.0 };
					MultiArray vtma = new ArrayMultiArray(vta);
					try {
						var.setData(vtma);
					}
					catch (Exception ex) {
						throw ex;
					}
					db.addEPSVariable(var);

					// z axis
					zAxis.setName("depth");
					zAxis.setTime(false);
					zAxis.setUnlimited(false);
					zAxis.setLen(sh.mNumBottles);
					zAxis.setAxisType(EPSConstants.EPZAXIS);
					zAxis.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.1");
					zAxis.addAttribute(1, "units", EPSConstants.EPCHAR, 2, "db");

					if (sh.mCastIsEvenlySpaced) {
						zAxis.addAttribute(2, "type", EPSConstants.EPCHAR, 4, "EVEN");
					}
					else {
						zAxis.addAttribute(2, "type", EPSConstants.EPCHAR, 6, "UNEVEN");
					}
					sarray[0] = 1;
					zAxis.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					int xPos = sech.getPRESVarPos();
					double[] za = new double[sh.mNumBottles];
					for (int b = 0; b < sh.mNumBottles; b++) {
						Bottle bh = (Bottle) sh.mBottles.elementAt(b);
						za[b] = bh.mDValues[xPos];
					}
					MultiArray zma = new ArrayMultiArray(za);
					zAxis.setData(zma);
					db.setAxis(zAxis);

					// add the z axes variable
					var = new EPSVariable();
					var.setOname("depth");
					var.setDtype(EPSConstants.EPDOUBLE);
					var.setVclass(Double.TYPE);
					var.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.1");
					var.addAttribute(1, "units", EPSConstants.EPCHAR, 4, "dbar");
					if (sh.mCastIsEvenlySpaced) {
						var.addAttribute(2, "type", EPSConstants.EPCHAR, 4, "EVEN");
					}
					else {
						var.addAttribute(2, "type", EPSConstants.EPCHAR, 6, "UNEVEN");
					}
					sarray[0] = 1;
					var.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);

					MultiArray zvma = new ArrayMultiArray(za);
					try {
						var.setData(zvma);
					}
					catch (Exception ex) {
						throw ex;
					}
					db.addEPSVariable(var);

					// lat axis
					latAxis.setName("latitude");
					latAxis.setTime(false);
					latAxis.setUnlimited(false);
					latAxis.setLen(1);
					latAxis.setAxisType(EPSConstants.EPYAXIS);
					latAxis.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");
					latAxis.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees");
					latAxis.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");
					sarray[0] = 500;
					latAxis.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					double lat = sh.mLat;
					double[] la = { lat };
					MultiArray lma = new ArrayMultiArray(la);
					latAxis.setData(lma);
					db.setAxis(latAxis);

					// add the y axes variable
					var = new EPSVariable();
					var.setOname("latitude");
					var.setDtype(EPSConstants.EPDOUBLE);
					var.setVclass(Double.TYPE);
					var.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");
					var.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees");
					var.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");
					sarray[0] = 500;
					var.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					MultiArray yvma = new ArrayMultiArray(la);
					try {
						var.setData(yvma);
					}
					catch (Exception ex) {
						throw ex;
					}
					db.addEPSVariable(var);

					// lon axis
					lonAxis.setName("longitude");
					lonAxis.setTime(false);
					lonAxis.setUnlimited(false);
					lonAxis.setLen(1);
					lonAxis.setAxisType(EPSConstants.EPXAXIS);
					lonAxis.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");
					lonAxis.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees");
					lonAxis.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");
					sarray[0] = 502;
					lonAxis.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					double lon = sh.mLon;
					double[] lla = { lon };
					lma = new ArrayMultiArray(lla);
					lonAxis.setData(lma);
					db.setAxis(lonAxis);

					// add the x axes variable
					var = new EPSVariable();
					var.setOname("longitude");
					var.setDtype(EPSConstants.EPDOUBLE);
					var.setVclass(Double.TYPE);
					var.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");
					var.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees");
					var.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");
					sarray[0] = 502;
					var.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					MultiArray xvma = new ArrayMultiArray(lla);
					try {
						var.setData(xvma);
					}
					catch (Exception ex) {
						throw ex;
					}

					db.addEPSVariable(var);

					// make a GeoDate object
					GeoDate theDate = null;
					int month, day;
					try {
						// make sure we get a valid GeoDate
						if (sh.mMonthIsBad || (sh.mMonth <= 0 || sh.mMonth > 12)) {
							month = lastGoodMonth;
						}
						else {
							month = sh.mMonth;
							lastGoodMonth = month;
						}
						if (sh.mDayIsBad || (sh.mDay <= 0 || sh.mDay > 31)) {
							day = lastGoodDay;
						}
						else {
							day = sh.mDay;
							lastGoodDay = day;
						}

						theDate = new GeoDate(month, day, sh.mYear, hour, min, secs, msec);
					}
					catch (IllegalTimeValue ex) {
						System.out.println("mMonth = " + sh.mMonth);
						System.out.println("mDay = " + sh.mDay);
						System.out.println("mYear = " + sh.mYear);
						System.out.println("hour = " + hour);
						System.out.println("min = " + min);
						System.out.println("secs = " + secs);
						System.out.println("msec = " + msec);
						System.out.println("couldn't create a geodate");
					}

					dType = sh.getType();
					if (dType == null) {
						dType = "UNKN";
					}

					// create the station calc arraylist
					ArrayList stnVars = null;
					if (sech.getNumStnVars() > 0) {
						stnVars = new ArrayList();
						for (int sv = 0; sv < sech.getNumStnVars(); sv++) {
							boolean missing = sh.getStnValue(sv) != JOAConstants.MISSINGVALUE;
							StationCalculation sc = new StationCalculation(sech.getStnVar(sv), sech.getStnVarUnits(sv), sech
							    .getStnVarMethod(sv), "JOA", sh.getStnValue(sv), missing);
							stnVars.add(sc);
						}
					}
					ExportLatitude elat = new ExportLatitude(sh.mLat, "point");
					ExportLongitude elon = new ExportLongitude(sh.mLon, "point");
					ExportVertical zmin = new ExportVertical(za[0], "top", zUnits);
					ExportVertical zmax = new ExportVertical(za[sh.mNumBottles - 1], "bottom", zUnits);
					ArrayList verts = new ArrayList();
					verts.add(zmin);
					verts.add(zmax);
					ExportDate edate = new ExportDate(theDate, "point");

					ExportStation epPtr = new ExportStation(EPSConstants.NETCDFFORMAT, "JOA Export", dType,
					    sech.mSectionDescription, sh.mStnNum, new Integer(sh.mCastNum).toString(), elat, elon, edate, verts,
					    null, outFileName, dir, stnVars, (double) sh.mBottomDepthInDBARS);
					stns.add(epPtr);

					// add the measured variables;
					for (int i = 0; i < fv.gNumProperties; i++) {
						// if (fv.mAllProperties[i].getVarLabel().equalsIgnoreCase("PRES"))
						// continue;
						int vPos = sech.getVarPos(fv.mAllProperties[i].getVarLabel(), false);

						if (sech.mNumCasts == 0 || vPos == -1) {
							continue;
						}

						double[][][][] va = new double[1][sh.mNumBottles][1][1];
						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);
							va[0][b][0][0] = bh.mDValues[vPos];
							// System.out.println("va =" + b + " " + va[0][b][0][0]);
						}

						// look this variable up in JOA EPIC_Key. find matching entry in
						// original EPIC Key
						String oname = fv.mAllProperties[i].getVarLabel();
						String sname = null;
						String lname = null;
						String gname = null;
						String units = null;
						String ffrmt = null;
						int keyID = -99;
						int type = -99;
						try {
							keyID = mEpicKeyDB.findKeyIDByCode(fv.mAllProperties[i].getVarLabel());
							Key key = mOrigEpicKeyDB.findKey(keyID);
							gname = key.getGname();
							sname = key.getSname();
							lname = key.getLname();
							units = key.getUnits();
							ffrmt = key.getFrmt();
							type = key.getType();
						}
						catch (Exception e) {
							lname = fv.mAllProperties[i].getVarLabel();
							gname = fv.mAllProperties[i].getVarLabel();
							sname = fv.mAllProperties[i].getVarLabel();
							units = fv.mAllProperties[i].getUnits();
						}

						// make a new variable
						var = new EPSVariable();

						var.setOname(oname);
						var.setSname(sname);
						var.setLname(lname);
						var.setGname(gname);
						var.setDtype(EPSConstants.EPDOUBLE);
						var.setVclass(Double.TYPE);
						int numAttributes = 0;
						if (ffrmt != null) {
							var.addAttribute(numAttributes++, "FORTRAN_format", EPSConstants.EPCHAR, ffrmt.length(), ffrmt);
						}
						if (units != null && units.length() > 0) {
							var.addAttribute(numAttributes++, "units", EPSConstants.EPCHAR, units.length(), units);
						}
						if (keyID >= 0) {
							sarray[0] = (short) type;
							// var.addAttribute(numAttributes++, "type", EPSConstants.EPSHORT,
							// 1, sarray);
						}
						if (keyID >= 0) {
							sarray[0] = (short) keyID;
							var.addAttribute(numAttributes++, "epic_code", EPSConstants.EPSHORT, 1, sarray);
						}

						// connect variable to axis
						boolean[] dimUsed = { true, true, true, true };
						var.setDimorder(0, 0);
						var.setDimorder(1, 1);
						var.setDimorder(2, 2);
						var.setDimorder(3, 3);
						var.setT(timeAxis);
						var.setZ(zAxis);
						var.setY(latAxis);
						var.setX(lonAxis);

						// store the data
						MultiArray mdma = new ArrayMultiArray(va);
						try {
							var.setData(mdma);
						}
						catch (Exception ex) {
							throw ex;
						}

						if (isObsQual) {
							// make an attribute to point to the quality variable
							String qcVar = oname + "_QC";
							var.addAttribute(numAttributes++, "OBS_QC_VARIABLE", EPSConstants.EPCHAR, qcVar.length(), qcVar);

							// add the variable to the database
							db.addEPSVariable(var);

							// now add the quality code variable
							EPSVariable epsVar = new EPSVariable();
							epsVar.setOname(oname + "_QC");
							epsVar.setSname(sname + "_QC");
							epsVar.setLname(sname + "_QC");
							epsVar.setGname(sname + "_QC");
							epsVar.setDtype(EPSConstants.EPSHORT);
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
							for (int b = 0; b < sh.mNumBottles; b++) {
								Bottle bh = (Bottle) sh.mBottles.elementAt(b);
								qcaa[0][b][0][0] = (short) bh.mQualityFlags[vPos];
								// System.out.println("qcaa =" + b + " " + qcaa[0][b][0][0]);
							}
							MultiArray qcma = new ArrayMultiArray(qcaa);
							try {
								epsVar.setData(qcma);
							}
							catch (Exception ex) {
								throw ex;
							}

							// add the qc variable to the database
							db.addEPSVariable(epsVar);
						}
						else {
							// add the variable to the database
							db.addEPSVariable(var);
						}
					}

					// write the output file
					try {
						db.writeNetCDF(new File(dir, outFileName));
					}
					catch (Exception ex) {
						ex.printStackTrace();
						System.out.println("an error occurred writing a netCDF file");
					}
					s++;
				}

				// add these stations to a new ExportFileSet
				// TODO: the exportVars should be present only if they are different
				// from the global variables
				ExportFileSet expFS = new ExportFileSet(sech.getID(), null, exportVars, stns);

				// TODO: add attributes and comments to the
				filePtrs.add(expFS);
			}
		}

		// write the pointer file
		try {
			if (isXML) {
				ptrDB.setXMLAttributes(xmlattributes);
			}
			ptrDB.setData(filePtrs);
			ptrDB.writePtrs();

			// type the file if on the Mac
			if (JOAConstants.ISMAC) {
				try {
					MRJFileUtils.setFileTypeAndCreator(outFile, new MRJOSType("TEXT"), new MRJOSType("JOAA"));
				}
				catch (IOException ex) {
					System.out.println("couldn't type a Mac file");
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		progress.setPercentComplete(100.0);
		progress.dispose();
	}

	public static void writeNetCDF_EPIC(File outFile, FileViewer fv) throws Exception {
		ProgressDialog progress = new ProgressDialog(fv, "Writing netCDF Profile Data...", Color.blue, Color.white);
		progress.setVisible(true);

		// outfile is the pointer file
		String dir = outFile.getParent();
		String ptrFileName = outFile.getName();

		// instantiate a pointer file object
		EpicPtrs ptrDB = new EpicPtrs(outFile);
		ArrayList filePtrs = new ArrayList();

		// set the writer class for the pointer file
		String lcName = ptrFileName.toLowerCase();
		if (lcName.indexOf(".xml") > 0) {
			ptrDB.setWriter(new XMLPtrFileWriter(outFile));
		}
		else {
			ptrDB.setWriter(new EpicPtrFileWriter(outFile));
		}

		// Get an epic key database specific to JOA
		EPIC_Key_DB mEpicKeyDB = new EPIC_Key_DB("joa_epic.key");

		// Get an epic key database specific to JOA
		EPIC_Key_DB mOrigEpicKeyDB = new EPIC_Key_DB("epic.key");

		// check whether there is any quality code information in the data
		// collection
		boolean isStnQual = false;
		boolean isBottleQual = false;
		boolean isObsQual = false;
		for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = of.getSection(sec);

				if (sech.mNumCasts == 0) {
					continue;
				}

				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					if (sh.mVarFlag != JOAConstants.MISSINGVALUE) {
						isStnQual = true;
					}

					for (int b = 0; b < sh.mNumBottles; b++) {
						Bottle bh = (Bottle) sh.mBottles.elementAt(b);

						if (bh.mQualityFlag != JOAConstants.MISSINGVALUE) {
							isBottleQual = true;
						}

						for (int i = 0; i < fv.gNumProperties && !isObsQual; i++) {
							// if
							// (fv.mAllProperties[i].getVarLabel().equalsIgnoreCase("PRES"))
							// continue;
							int vPos = sech.getVarPos(fv.mAllProperties[i].getVarLabel(), false);
							if (vPos == -1) {
								continue;
							}
							if (bh.mQualityFlags[i] != JOAConstants.MISSINGVALUE) {
								isObsQual = true;
								break;
							}
						}
					}
				}
			}
		}

		// loop on the casts: stations become individual files
		int s = 0;
		int totalStnsToWrite = JOAFormulas.getNumberVisStns(fv);

		int lastGoodMonth = 1, lastGoodDay = 1;
		short[] bqc = new short[500];

		for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = of.getSection(sec);

				if (sech.mNumCasts == 0) {
					continue;
				}
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					progress.setPercentComplete(100.0 * ((double) s / (double) totalStnsToWrite));

					// make the filename from the section description
					int cast = sh.mCastNum;
					String castStr = "";
					if (cast >= 0) {
						castStr = "_" + String.valueOf(cast);
					}
					String outFileName = sech.mSectionDescription + "_" + sh.mStnNum + castStr + ".nc";

					// make a DBase object
					Dbase db = new Dbase();

					// add the global attributes
					short[] sarray = new short[1];

					// db.addEPSAttribute("CREATION_DATE", EPCHAR, 8, "Today");
					db
					    .addEPSAttribute("CRUISE", EPSConstants.EPCHAR, sech.mSectionDescription.length(),
					        sech.mSectionDescription);
					db.addEPSAttribute("CAST", EPSConstants.EPCHAR, sh.mStnNum.length(), sh.mStnNum);
					sarray[0] = (short) sh.mBottomDepthInDBARS;
					db.addEPSAttribute("WATER_DEPTH", EPSConstants.EPSHORT, 1, sarray);
					db.addEPSAttribute("DATA_ORIGIN", EPSConstants.EPCHAR, sech.getShipCode().length(), sech.getShipCode());
					String dType = sh.getType();
					if (dType == null) {
						dType = "UNKN";
					}
					if (isStnQual || isBottleQual) {
						if (fv.getQCStd() == JOAConstants.IGOSS_QC_STD) {
							db.addEPSAttribute("QUALITY_CODE_STD", EPSConstants.EPCHAR, 5, "IGOSS");
						}
						else if (fv.getQCStd() == JOAConstants.WOCE_QC_STD) {
							db.addEPSAttribute("QUALITY_CODE_STD", EPSConstants.EPCHAR, 5, "WOCE");
						}
					}
					db.addEPSAttribute("DATA_TYPE", EPSConstants.EPCHAR, dType.length(), dType);
					if (isStnQual) {
						// write a station quality attribute
						sarray[0] = (short) sh.mVarFlag;
						db.addEPSAttribute("STN_QUALITY", EPSConstants.EPSHORT, 1, sarray);
					}
					if (isBottleQual) {
						// write a bottle quality attribute
						// bqc = null;
						// bqc = new short[sh.mNumBottles];
						for (int b = 0; b < sh.mNumBottles; b++) {
							// read the bottle quality code
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);
							bqc[b] = bh.mQualityFlag;
						}
						EPSAttribute epa = db.addEPSAttribute("BOTTLE_QUALITY_CODES", EPSConstants.EPSHORT, 36, bqc);
					}

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

					int hour = 0;
					if (sh.mHour != JOAConstants.MISSINGVALUE) {
						hour = sh.mHour;
					}

					double mins = 0;
					if (sh.mMinute != JOAConstants.MISSINGVALUE) {
						mins = sh.mMinute;
					}

					// make the time axis units
					String date = "days since ";
					int min = (int) mins;
					double fmin = mins - min;
					int secs = (int) (fmin * 60.0);
					double fsec = (fmin * 60.0) - secs;
					int msec = (int) (fsec * 1000.0);
					String fs = String.valueOf(fsec);
					fs = fs.substring(fs.indexOf(".") + 1, fs.length()).trim();
					int f = 0;
					try {
						if (fs != null && fs.length() > 0) {
							f = Integer.valueOf(fs).intValue();
						}
					}
					catch (Exception ex) {
						// silent
					}

					// sprintf(time_string,"%04d-%02d-%02d
					// %02d:%02d:%02d.%03d",yr,mon,day,hr,min,sec,f);
					String frmt = new String(
					    "{0,number,####}-{1,number,00}-{2,number,00} {3,number,00}:{4,number,00}:{5,number,00}.{6,number,000}");
					MessageFormat msgf = new MessageFormat(frmt);

					Object[] objs = { new Integer(sh.mYear), new Integer(sh.mMonth), new Integer(sh.mDay), new Integer(hour),
					    new Integer(min), new Integer(secs), new Integer(f) };
					StringBuffer out = new StringBuffer();
					msgf.format(objs, out, null);
					String time_string = new String(out);
					date = date + time_string;
					timeAxis.addAttribute(0, "units", EPSConstants.EPCHAR, date.length(), date);
					timeAxis.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
					double[] ta = { 0.0 };
					MultiArray tma = new ArrayMultiArray(ta);
					timeAxis.setData(tma);
					db.setAxis(timeAxis);

					// add the time axes variable
					EPSVariable var = new EPSVariable();
					var.setOname("time");
					var.setDtype(EPSConstants.EPDOUBLE);
					var.setVclass(Double.TYPE);
					var.addAttribute(0, "units", EPSConstants.EPCHAR, date.length(), date);
					var.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
					double[] vta = { 0.0 };
					MultiArray vtma = new ArrayMultiArray(vta);
					try {
						var.setData(vtma);
					}
					catch (Exception ex) {
						throw ex;
					}
					db.addEPSVariable(var);

					// z axis
					zAxis.setName("depth");
					zAxis.setTime(false);
					zAxis.setUnlimited(false);
					zAxis.setLen(sh.mNumBottles);
					zAxis.setAxisType(EPSConstants.EPZAXIS);
					zAxis.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.1");
					zAxis.addAttribute(1, "units", EPSConstants.EPCHAR, 2, "db");

					if (sh.mCastIsEvenlySpaced) {
						zAxis.addAttribute(2, "type", EPSConstants.EPCHAR, 4, "EVEN");
					}
					else {
						zAxis.addAttribute(2, "type", EPSConstants.EPCHAR, 6, "UNEVEN");
					}
					sarray[0] = 1;
					zAxis.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					int xPos = sech.getPRESVarPos();
					double[] za = new double[sh.mNumBottles];
					for (int b = 0; b < sh.mNumBottles; b++) {
						Bottle bh = (Bottle) sh.mBottles.elementAt(b);
						za[b] = bh.mDValues[xPos];
					}
					MultiArray zma = new ArrayMultiArray(za);
					zAxis.setData(zma);
					db.setAxis(zAxis);

					// add the z axes variable
					var = new EPSVariable();
					var.setOname("depth");
					var.setDtype(EPSConstants.EPDOUBLE);
					var.setVclass(Double.TYPE);
					var.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.1");
					var.addAttribute(1, "units", EPSConstants.EPCHAR, 4, "dbar");
					if (sh.mCastIsEvenlySpaced) {
						var.addAttribute(2, "type", EPSConstants.EPCHAR, 4, "EVEN");
					}
					else {
						var.addAttribute(2, "type", EPSConstants.EPCHAR, 6, "UNEVEN");
					}
					sarray[0] = 1;
					var.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);

					MultiArray zvma = new ArrayMultiArray(za);
					try {
						var.setData(zvma);
					}
					catch (Exception ex) {
						throw ex;
					}
					db.addEPSVariable(var);

					// lat axis
					latAxis.setName("latitude");
					latAxis.setTime(false);
					latAxis.setUnlimited(false);
					latAxis.setLen(1);
					latAxis.setAxisType(EPSConstants.EPYAXIS);
					latAxis.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");
					latAxis.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees");
					latAxis.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");
					sarray[0] = 500;
					latAxis.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					double lat = sh.mLat;
					double[] la = { lat };
					MultiArray lma = new ArrayMultiArray(la);
					latAxis.setData(lma);
					db.setAxis(latAxis);

					// add the y axes variable
					var = new EPSVariable();
					var.setOname("latitude");
					var.setDtype(EPSConstants.EPDOUBLE);
					var.setVclass(Double.TYPE);
					var.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");
					var.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees");
					var.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");
					sarray[0] = 500;
					var.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					MultiArray yvma = new ArrayMultiArray(la);
					try {
						var.setData(yvma);
					}
					catch (Exception ex) {
						throw ex;
					}
					db.addEPSVariable(var);

					// lon axis
					lonAxis.setName("longitude");
					lonAxis.setTime(false);
					lonAxis.setUnlimited(false);
					lonAxis.setLen(1);
					lonAxis.setAxisType(EPSConstants.EPXAXIS);
					lonAxis.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");
					lonAxis.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees");
					lonAxis.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");
					sarray[0] = 502;
					lonAxis.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					double lon = sh.mLon;
					double[] lla = { lon };
					lma = new ArrayMultiArray(lla);
					lonAxis.setData(lma);
					db.setAxis(lonAxis);

					// add the x axes variable
					var = new EPSVariable();
					var.setOname("longitude");
					var.setDtype(EPSConstants.EPDOUBLE);
					var.setVclass(Double.TYPE);
					var.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");
					var.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees");
					var.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");
					sarray[0] = 502;
					var.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);
					MultiArray xvma = new ArrayMultiArray(lla);
					try {
						var.setData(xvma);
					}
					catch (Exception ex) {
						throw ex;
					}

					db.addEPSVariable(var);

					// make a GeoDate object
					GeoDate theDate = null;
					int month, day;
					try {
						// make sure we get a valid GeoDate
						if (sh.mMonthIsBad || (sh.mMonth <= 0 || sh.mMonth > 12)) {
							month = lastGoodMonth;
						}
						else {
							month = sh.mMonth;
							lastGoodMonth = month;
						}
						if (sh.mDayIsBad || (sh.mDay <= 0 || sh.mDay > 31)) {
							day = lastGoodDay;
						}
						else {
							day = sh.mDay;
							lastGoodDay = day;
						}

						theDate = new GeoDate(month, day, sh.mYear, hour, min, secs, msec);
					}
					catch (IllegalTimeValue ex) {
						System.out.println("mMonth = " + sh.mMonth);
						System.out.println("mDay = " + sh.mDay);
						System.out.println("mYear = " + sh.mYear);
						System.out.println("hour = " + hour);
						System.out.println("min = " + min);
						System.out.println("secs = " + secs);
						System.out.println("msec = " + msec);
						System.out.println("couldn't create a geodate");
					}

					dType = sh.getType();
					if (dType == null) {
						dType = "UNKN";
					}
					EpicPtr epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "JOA Export", dType, sech.mSectionDescription,
					    sh.mStnNum, sh.mLat, sh.mLon, theDate, za[0], za[sh.mNumBottles - 1], null, outFileName, dir, null);
					filePtrs.add(epPtr);

					// add the measured variables;
					for (int i = 0; i < fv.gNumProperties; i++) {
						// if (fv.mAllProperties[i].getVarLabel().equalsIgnoreCase("PRES"))
						// continue;
						int vPos = sech.getVarPos(fv.mAllProperties[i].getVarLabel(), false);

						if (sech.mNumCasts == 0 || vPos == -1) {
							continue;
						}

						double[][][][] va = new double[1][sh.mNumBottles][1][1];
						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);
							va[0][b][0][0] = bh.mDValues[vPos];
							// System.out.println("va =" + b + " " + va[0][b][0][0]);
						}

						// look this variable up in JOA EPIC_Key. find matching entry in
						// original EPIC Key
						String oname = fv.mAllProperties[i].getVarLabel();
						String sname = null;
						String lname = null;
						String gname = null;
						String units = null;
						String ffrmt = null;
						int keyID = -99;
						int type = -99;
						try {
							keyID = mEpicKeyDB.findKeyIDByCode(fv.mAllProperties[i].getVarLabel());
							Key key = mOrigEpicKeyDB.findKey(keyID);
							gname = key.getGname();
							sname = key.getSname();
							lname = key.getLname();
							units = key.getUnits();
							ffrmt = key.getFrmt();
							type = key.getType();
						}
						catch (Exception e) {
							lname = fv.mAllProperties[i].getVarLabel();
							gname = fv.mAllProperties[i].getVarLabel();
							sname = fv.mAllProperties[i].getVarLabel();
							units = fv.mAllProperties[i].getUnits();
						}

						// make a new variable
						var = new EPSVariable();

						var.setOname(oname);
						var.setSname(sname);
						var.setLname(lname);
						var.setGname(gname);
						var.setDtype(EPSConstants.EPDOUBLE);
						var.setVclass(Double.TYPE);
						int numAttributes = 0;
						if (ffrmt != null) {
							var.addAttribute(numAttributes++, "FORTRAN_format", EPSConstants.EPCHAR, ffrmt.length(), ffrmt);
						}
						if (units != null && units.length() > 0) {
							var.addAttribute(numAttributes++, "units", EPSConstants.EPCHAR, units.length(), units);
						}
						if (keyID >= 0) {
							sarray[0] = (short) type;
							// var.addAttribute(numAttributes++, "type", EPSConstants.EPSHORT,
							// 1, sarray);
						}
						if (keyID >= 0) {
							sarray[0] = (short) keyID;
							var.addAttribute(numAttributes++, "epic_code", EPSConstants.EPSHORT, 1, sarray);
						}

						// connect variable to axis
						boolean[] dimUsed = { true, true, true, true };
						var.setDimorder(0, 0);
						var.setDimorder(1, 1);
						var.setDimorder(2, 2);
						var.setDimorder(3, 3);
						var.setT(timeAxis);
						var.setZ(zAxis);
						var.setY(latAxis);
						var.setX(lonAxis);

						// store the data
						MultiArray mdma = new ArrayMultiArray(va);
						try {
							var.setData(mdma);
						}
						catch (Exception ex) {
							throw ex;
						}

						if (isObsQual) {
							// make an attribute to point to the quality variable
							String qcVar = oname + "_QC";
							var.addAttribute(numAttributes++, "OBS_QC_VARIABLE", EPSConstants.EPCHAR, qcVar.length(), qcVar);

							// add the variable to the database
							db.addEPSVariable(var);

							// now add the quality code variable
							EPSVariable epsVar = new EPSVariable();
							epsVar.setOname(oname + "_QC");
							epsVar.setSname(sname + "_QC");
							epsVar.setLname(sname + "_QC");
							epsVar.setGname(sname + "_QC");
							epsVar.setDtype(EPSConstants.EPSHORT);
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
							for (int b = 0; b < sh.mNumBottles; b++) {
								Bottle bh = (Bottle) sh.mBottles.elementAt(b);
								qcaa[0][b][0][0] = (short) bh.mQualityFlags[vPos];
								// System.out.println("qcaa =" + b + " " + qcaa[0][b][0][0]);
							}
							MultiArray qcma = new ArrayMultiArray(qcaa);
							try {
								epsVar.setData(qcma);
							}
							catch (Exception ex) {
								throw ex;
							}

							// add the qc variable to the database
							db.addEPSVariable(epsVar);
						}
						else {
							// add the variable to the database
							db.addEPSVariable(var);
						}
					}

					// write the output file
					try {
						db.writeNetCDF(new File(dir, outFileName));
					}
					catch (Exception ex) {
						ex.printStackTrace();
						System.out.println("an error occurred writing a netCDF file");
					}
					s++;
				}
			}
		}

		// write the pointer file
		try {
			ptrDB.setData(filePtrs);
			ptrDB.writePtrs();

			// type the file if on the Mac
			if (JOAConstants.ISMAC) {
				try {
					MRJFileUtils.setFileTypeAndCreator(outFile, new MRJOSType("TEXT"), new MRJOSType("JOAA"));
				}
				catch (IOException ex) {
					System.out.println("couldn't type a Mac file");
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		progress.setPercentComplete(100.0);
		progress.dispose();
	}
}

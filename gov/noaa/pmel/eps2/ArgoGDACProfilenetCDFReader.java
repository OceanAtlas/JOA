/**
 * 
 */
package gov.noaa.pmel.eps2;

import gov.noaa.pmel.util.GeoDate;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import javaoceanatlas.resources.JOAConstants;
import ucar.multiarray.ArrayMultiArray;
import ucar.multiarray.MultiArray;
import ucar.netcdf.Attribute;
import ucar.netcdf.AttributeIterator;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;

/**
 * @author oz
 *
 */
public class ArgoGDACProfilenetCDFReader implements EPSFileReader, EPSConstants {
	/**
	 * ID count (< 0 if variable not found in EPIC Key database
	 */
	protected int mIDCount = -1;
	/**
	 * Dbase that this file reader is initializing
	 */
	protected Dbase mOwnerDBase;
	/**
	 * netCDF file being read by this reader
	 */
	protected EPSNetCDFFile mNCFile;
	/**
	 * The EPIC Key database
	 */
	protected EPIC_Key_DB mEpicKeyDB = null;
	/**
	 * Optional string for progress string
	 */
	protected String mProgressStr = "Reading GDAC Argo netCDF Data...";

	private static boolean DEBUG = false;

	/**
	 * Construct a new <code>netCDFReader</code> with a Dbase amd netCDF file.
	 * 
	 * @param dname
	 *          Dbase that this reader will fill in
	 * @param ncf
	 *          Source netCDF file
	 * 
	 * @see Dbase
	 * @see EPSNetCDFFile

	/**
	 * @param db
	 * @param nc
	 * @param ep
	 */
	public ArgoGDACProfilenetCDFReader(Dbase db, EPSNetCDFFile nc, EpicPtr ep) {
		mOwnerDBase = db;
		mNCFile = nc;
		mOwnerDBase.setEpicPtr(ep);
		if (ep != null && ep.getProgressStr() != null)
			mProgressStr = new String(ep.getProgressStr());
	}

	/* (non-Javadoc)
	 * @see gov.noaa.pmel.eps2.EPSFileReader#getvar(java.lang.String, int[], int[], int[])
	 */
	public MultiArray getvar(String varname, int[] lci, int[] uci, int[] dims) throws EPSVarDoesNotExistExcept,
	IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.noaa.pmel.eps2.EPSFileReader#getvar(java.lang.String)
	 */
	public MultiArray getvar(String varname) throws EPSVarDoesNotExistExcept, IOException {
		return null;
	}
	
	public String translateArgoParamNames(String inName) {
		String suffix = "";
		if (inName.toLowerCase().indexOf("2") > 0) {
			suffix  += "_2";
		}
		if (inName.toLowerCase().indexOf("qc") > 0) {
			suffix  += "_qc";
		}
		if (inName.toLowerCase().indexOf("pres") >= 0) {
				return "pressure" + suffix;
		}
		else if (inName.toLowerCase().indexOf("psal") >= 0) {
			return "salinity" + suffix;
		}
		else if (inName.toLowerCase().indexOf("temp_doxy") >= 0) {
			return "oxygen temperature" + suffix;
		}
		else if (inName.toLowerCase().indexOf("frequency_doxy") >= 0) {
			return "oxygen frequency" + suffix;
		}
		else if (inName.toLowerCase().indexOf("temp") >= 0) {
			return "temperature" + suffix;
		}
		else if (inName.toLowerCase().indexOf("bphase_doxy") >= 0) {
			return "bphase oxygen" + suffix;
		}
		else if (inName.toLowerCase().indexOf("doxy") >= 0) {
			return "oxygen" + suffix;
		}
		return inName;
	}
	
	public boolean isCanonicalArgoVariable(String inVarStr) {
		if (inVarStr.toLowerCase().indexOf("profile") >= 0 || inVarStr.toLowerCase().indexOf("history") >= 0) {
			return false;
		}
		
		if (inVarStr.toLowerCase().indexOf("pres") >= 0) {
			return true;
		}
		else if (inVarStr.toLowerCase().indexOf("temp") >= 0) {
			return true;
		}
		else if (inVarStr.toLowerCase().indexOf("psal") >= 0) {
			return true;
		}
		else if (inVarStr.toLowerCase().indexOf("doxy") >= 0) {
			return true;
		}
		return false;
	}
	
	public boolean isAdjustedVariable(String inVarStr) {
		if (inVarStr.toLowerCase().indexOf("adjust") >= 0) {
			return true;
		}
		return false;
	}
	
	public boolean isQCVariable(String inVarStr) {
		if (inVarStr.toLowerCase().indexOf("qc") >= 0) {
			return true;
		}
		return false;
	}
	
	public boolean isErrorVariable(String inVarStr) {
		if (inVarStr.toLowerCase().indexOf("error") >= 0) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see gov.noaa.pmel.eps2.EPSFileReader#parse()
	 */
	public void parse() throws Exception {
		Dimension nProfsDim = null;
		Dimension nLevelsDim = null;
		Dimension nParamsDim = null;
		Dimension nDateTimeDim = null;

		try {
			// count up the number of variable
			mEpicKeyDB = new EPIC_Key_DB("epic.key");

			// the array gets filled with ids not the length of the dimension
			DimensionIterator di = mNCFile.getDimensions().iterator();
			while (di.hasNext()) {
				Dimension d = di.next();
				if (d.getName().equalsIgnoreCase("n_prof")) {
					nProfsDim = d;
				}
				else if (d.getName().equalsIgnoreCase("n_levels")) {
					nLevelsDim = d;
				}
				else if (d.getName().equalsIgnoreCase("n_param")) {
					nParamsDim = d;
				}
				else if (d.getName().equalsIgnoreCase("date_time")) {
					nDateTimeDim = d;
				}
			}

			int numProfiles = nProfsDim.getLength();
			int numLevels = nLevelsDim.getLength();
			int numParams = nParamsDim.getLength();
			int numDateTime = nDateTimeDim.getLength();

			int[] zVarPos = new int[numProfiles];
			double[][][] pData = new double[numParams][numProfiles][numLevels];
			Character pQCData = new Character('a');
			int[][][] pIntQCData = new int[numParams][numProfiles][numLevels];
			double[] lons = new double[numProfiles];
			double[] lats = new double[numProfiles];
			double[] julDays = new double[numProfiles];
			GeoDate[] measureDate = new GeoDate[numProfiles];
			String[] params = new String[numParams];
			String[] paramsUsed = new String[numParams];
			String[] qcParams = new String[numParams]; //param_QC
			String[] qcParamsUsed = new String[numParams]; //param_QC
			String[] adjustedParams = new String[numParams]; //param_Adjusted
			String[] qcAdjustedParams = new String[numParams]; //param_Adjusted_QC
			GeoDate refGeoDate = null;
			int[] cycleNums =new int[numProfiles];
			String[] units = new String[numParams];
			String[] mCruiseIDs = new String[numProfiles];

			// look for the axis variable
			VariableIterator vi = mNCFile.iterator();
			int nDataVars = -1;
			while (vi.hasNext()) {
				Variable v = vi.next();
				String vName = v.getName();
				
				// isolate observed variables by using metadata clues
				if (isCanonicalArgoVariable(vName)) {
					// should be a measured variable
					if (!isQCVariable(vName) && !isErrorVariable(vName)) {
						//either "raw" variable or adjusted variable
						if (!isAdjustedVariable(vName)) {
							// raw variable
							params[++nDataVars] = vName;
						}
						else {
							// adjusted variable
							adjustedParams[nDataVars] = vName;
						}
					}
					else if (isQCVariable(vName)) { 
						if (!isAdjustedVariable(vName)) {
							// raw qc variable
							qcParams[nDataVars] = vName;
						}
						else {
							// adjusted qc variable
							qcAdjustedParams[nDataVars] = vName;
						}	
					}
				}
				
				if (v.getName().equalsIgnoreCase("platform_number")) {
					// this becomes the cruise ID
					int[] origin = new int[v.getRank()];
					int[] extent = v.getLengths();
					MultiArray tMa = (MultiArray) v.copyout(origin, extent);
					Object array = tMa.toArray();
					for (int i=0; i<numProfiles; i++) {
						String platStr = new String();
						int offset = i * 8;
						for (int c=offset; c<offset+8; c++) {
							char pc = ((char[])array)[c];
							platStr += pc;
						}
						mCruiseIDs[i] = platStr;
					}
				}
				
				if (v.getName().equalsIgnoreCase("reference_date_time")) {
					// array of char
					int[] origin = new int[v.getRank()];
					int[] extent = v.getLengths();
					MultiArray tMa = (MultiArray) v.copyout(origin, extent);
					Object array = tMa.toArray();
					String paramStr = new String();
					for (int c=0; c<numDateTime; c++) {
						char pc = ((char[])array)[c];
						paramStr += pc;
					}
					String refDate = paramStr.trim();

					Calendar cal_ = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
					DateFormat df = new SimpleDateFormat("yyyyMMDDhhmmss");
					df.setCalendar(cal_);
					Date dte = df.parse(refDate, new ParsePosition(0));
					refGeoDate = new GeoDate(dte.getTime());
				}

				if (v.getName().equalsIgnoreCase("latitude")) {
					// array of doubles
					int[] origin = new int[v.getRank()];
					int[] extent = v.getLengths();
					MultiArray tMa = (MultiArray) v.copyout(origin, extent);
					for (int i=0; i<numProfiles; i++) {
						lats[i] = tMa.getDouble(new int[] { i });
					}	
				}

				if (v.getName().equalsIgnoreCase("longitude")) {
					// array of doubles
					int[] origin = new int[v.getRank()];
					int[] extent = v.getLengths();
					MultiArray tMa = (MultiArray) v.copyout(origin, extent);
					for (int i=0; i<numProfiles; i++) {
						lons[i] = tMa.getDouble(new int[] { i });
					}
				}

				if (v.getName().equalsIgnoreCase("cycle_number")) {
					// cycle number is the same for all profiles
					int[] origin = new int[v.getRank()];
					int[] extent = v.getLengths();
					MultiArray tMa = (MultiArray) v.copyout(origin, extent);
					for (int i=0; i<numProfiles; i++) {
						cycleNums[i] = tMa.getInt(new int[] { i });
					}
				}

				if (v.getName().equalsIgnoreCase("juld")) {
					// array of doubles
					int[] origin = new int[v.getRank()];
					int[] extent = v.getLengths();
					MultiArray tMa = (MultiArray) v.copyout(origin, extent);
					for (int i=0; i<numProfiles; i++) {
						julDays[i] = tMa.getDouble(new int[] { i });
					}	
				}

//				if (v.getName().equalsIgnoreCase("station_parameters")) {
//					Attribute missingAttr = v.getAttribute("_FillValue");
//					String sFillValue = "";
//					if (missingAttr != null) {
//						sFillValue = missingAttr.getStringValue();
//					}
//					int[] origin = new int[v.getRank()];
//					int[] extent = v.getLengths();
//					MultiArray tMa = (MultiArray) v.copyout(origin, extent);
//					// this is 16 char string for each profile and parameter
//					Object array = tMa.toArray();
//					for (int i=0; i<numProfiles; i++) {
////						System.out.print(i + " ");
//						int offset = i * numParams * 16;
//						for (int j=0; j<numParams; j++) {
//							int paramStartPos = offset + j * 16;
//							String paramStr = new String();
//							for (int c=paramStartPos; c<paramStartPos+16; c++) {
//								char pc = ((char[])array)[c];
//								paramStr += pc;
//							}
//							if (params[j] == null) {
//								params[j] = paramStr.trim();
//							}
////							System.out.print(j + ":  *" + paramStr + "* ");
//
//							if (params[j].length() > 0) {
//								// create the possible qc and adjusted parameters
//								qcParams[j] = params[j] + "_QC"; //param_QC
//								adjustedParams[j] = params[j] + "_Adjusted"; //param_Adjusted
//								qcAdjustedParams[j] =  params[j] + "_Adjusted_QC"; //param_Adjusted_QC
//							}
//						}
////						System.out.println();
//					}
//				}
			}

			// parse all the observed parameters
			for (int iParam = 0; iParam < numParams; iParam++) {
				String currVar = adjustedParams[iParam];
				if (currVar == null || currVar.length() == 0) {
					continue;
				}
				Variable v = null;
				try {
				v = mNCFile.getVariable(currVar);
				if (v == null) {
					currVar = params[iParam];
					v = mNCFile.getVariable(currVar);
				}
				paramsUsed[iParam] = translateArgoParamNames(currVar);
				}
				catch (Exception ee) {
//					ee.printStackTrace();
				}
				
				if (v != null) {
					double fillValue = 99999.0;
					Attribute missingAttr = v.getAttribute("_FillValue");
					if (missingAttr != null) {
						fillValue = missingAttr.getNumericValue().doubleValue();
					}
					
					Attribute unitsAttr = v.getAttribute("units");
					String unitStr = "";
					if (unitsAttr != null) {
						unitStr = unitsAttr.getStringValue();
					}
					units[iParam] = unitStr;

					int[] origin = new int[v.getRank()];
					int[] extent = v.getLengths();
					MultiArray tMa = (MultiArray)v.copyout(origin, extent);
					Object array = tMa.toArray();
					for (int iProf=0; iProf<numProfiles; iProf++) {
						if (currVar.indexOf("pres") >= 0) {
							zVarPos[iProf] = iParam;
						}
						int profOffset = iProf * numLevels;
						for (int jj = 0; jj < numLevels; jj++) {
							float fval = ((float[])array)[profOffset + jj];
							pData[iParam][iProf][jj] = fval;
							if (fval == fillValue) {
								pData[iParam][iProf][jj] = JOAConstants.MISSINGVALUE;
							}
						}
					}
				}

				String currQCVar = qcAdjustedParams[iParam];
				v = mNCFile.getVariable(currQCVar);
				
				if (v == null) {
					currQCVar = qcParams[iParam];
					v = mNCFile.getVariable(currQCVar);
				}
				qcParamsUsed[iParam] = translateArgoParamNames(currQCVar);
				
				if (v != null) {
					String fillValue = " ";
					Attribute missingAttr = v.getAttribute("_FillValue");
					if (missingAttr != null) {
						fillValue = missingAttr.getStringValue();
					}
					int[] origin = new int[v.getRank()];
					int[] extent = v.getLengths();
					MultiArray tMa = (MultiArray)v.copyout(origin, extent);
					Object array = tMa.toArray();
					for (int iProf=0; iProf<numProfiles; iProf++) {
						int profOffset = iProf * numLevels;
						for (int jj = 0; jj < numLevels; jj++) {
							pQCData = new Character(((char[])array)[profOffset + jj]);
							if (pQCData.equals(' ')) {
								pIntQCData[iParam][iProf][jj] = 9;
							}
							else {
								try {
									pIntQCData[iParam][iProf][jj] = Integer.valueOf(pQCData.toString());
								}
								catch (Exception ex) {
									pIntQCData[iParam][iProf][jj] = 9;
									System.out.println("NFE " + pQCData.toString());
								}
							}
						}
					}
				}

			}

			// create the real date
			mOwnerDBase.setDataType("CTD");
			mOwnerDBase.createSubEntries(numProfiles, "foobar");
			for (int i=0;i<numProfiles; i++) {
				measureDate[i] = new GeoDate(refGeoDate.getTime());
				measureDate[i].increment(julDays[i], GeoDate.DAYS);
				//				System.out.println(measureDate[i].toString());
			}

			int castNum = 1;
			double oldLon = -99;
			double oldLat = -99;
			for (int i=0; i<numProfiles; i++) {
				Dbase db = new Dbase();
				db.setDataType("CTD");

				// create the axes
				Axis xAxis = new Axis();
				Axis yAxis = new Axis();
				Axis zAxis = new Axis();
				Axis tAxis = new Axis();

				xAxis.setDimension(true);
				xAxis.setName("lon");
				xAxis.setUnits("Degrees_E");
				xAxis.setTime(false);
				xAxis.setAxisType(EPXAXIS);
				xAxis.setLen(1);
				double[] lonarray = {lons[i]};
				MultiArray lonma = new ArrayMultiArray(lonarray);
				try {
					xAxis.setData(lonma);
				}
				catch (Exception ex) {}

				yAxis.setDimension(true);
				yAxis.setName("lat");
				yAxis.setUnits("Degrees_N");
				yAxis.setTime(false);
				yAxis.setAxisType(EPYAXIS);
				yAxis.setLen(1);
				double[] latarray = {lats[i]};
				MultiArray latma = new ArrayMultiArray(latarray);
				try {
					yAxis.setData(latma);
				}
				catch (Exception ex) {}
				
				if (lons[i] == oldLon && oldLat == lats[i]) {
					castNum++;
				}
				else {
					castNum = 1;
					oldLat = lats[i];
					oldLon = lons[i];
				}

				zAxis.setDimension(true);
				zAxis.setName("pres");
				zAxis.setUnits("dbar");
				zAxis.setTime(false);
				zAxis.setAxisType(EPZAXIS);
				zAxis.setLen(numLevels);
				int zPos = zVarPos[i];
				double[] zVals = new double[numLevels];
				for (int l=0; l<numLevels; l++) {
					zVals[l] = pData[zPos][i][l];
				}
				MultiArray zma = new ArrayMultiArray(zVals);
				try {
					zAxis.setData(zma);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}

				tAxis.setDimension(true);
				tAxis.setName("time");
				tAxis.setUnits("minutes since 1980-01-01 00:00:00");
				tAxis.setTime(true);
				tAxis.setAxisType(EPTAXIS);
				tAxis.setLen(1);
				long[] tarray = {measureDate[i].getTime()};
				MultiArray tma = new ArrayMultiArray(tarray);
				try {
					tAxis.setData(tma);
				}
				catch (Exception ex) {}

				db.setAxis(xAxis);
				db.setAxis(yAxis);
				db.setAxis(zAxis);
				db.setAxis(tAxis);
				
				// now add the variables 
				// instantiate an EPS Variable
				for (int p=0; p<numParams; p++) {
					// make variable for observed data
					EPSVariable var = new EPSVariable();
					var.setDtype(EPDOUBLE);
					var.setID(0);	//no EPIC code
					var.setOname(paramsUsed[p]);
					var.setSname(paramsUsed[p]);
					var.setLname(paramsUsed[p]);
					var.setGname(paramsUsed[p]);
					var.setUnits(units[p]);
					Axis[] varaxis = new Axis[4];
					boolean[] dim_used = new boolean[4];
					varaxis[0] = tAxis;
					varaxis[1] = zAxis;
					varaxis[2] = yAxis;
					varaxis[3] = xAxis;
					dim_used[0] = true;
					dim_used[1] = true;
					dim_used[2] = true;
					dim_used[3] = true;
					var.setDimorder(0, 0);
					var.setDimorder(1, 1);
					var.setDimorder(2, 2);
					var.setDimorder(3, 3);
					var.setT(varaxis[0]);
					var.setZ(varaxis[1]);
					var.setY(varaxis[2]);
					var.setX(varaxis[3]);

					// set the data
					float[][][][] presaa = new float[1][numLevels][1][1];
						for (int b = 0; b < numLevels; b++) {
							presaa[0][b][0][0] = (float)pData[p][i][b];
						}
					
					MultiArray presma = new ArrayMultiArray(presaa);
					try {
						var.setData(presma);
					}
					catch (Exception ex) {
						throw ex;
					}

					EPSVariable qcvar = new EPSVariable();

					String qcVar = var.getName() + "_QC";
					qcvar.setOname(qcVar);
					qcvar.setSname(qcVar);
					qcvar.setLname(qcVar);
					qcvar.setGname(qcVar);
					qcvar.setDtype(EPSConstants.EPSHORT);
					qcvar.setVclass(Short.TYPE);

					qcvar.setDimorder(0, 0);
					qcvar.setDimorder(1, 1);
					qcvar.setDimorder(2, 2);
					qcvar.setDimorder(3, 3);
					qcvar.setT(var.getT());
					qcvar.setZ(var.getZ());
					qcvar.setY(var.getY());
					qcvar.setX(var.getX());

					// set the data
					// create storage for the qc variables
					int[][][][] qcaa = new int[1][numLevels][1][1];
					for (int b = 0; b < numLevels; b++) {
						qcaa[0][b][0][0] = pIntQCData[p][i][b];
					}
					MultiArray qcma = new ArrayMultiArray(qcaa);
					try {
						qcvar.setData(qcma);
					}
					catch (Exception ex) {
						throw ex;
					}
					
					// make the matching QC Var
					var.addAttribute(var.getAttnum() + 1, "OBS_QC_VARIABLE", EPSConstants.EPCHAR, qcVar.length(), qcVar);
					db.addEPSVariable(var);
					db.addEPSVariable(qcvar);
				}
				

				String valc = "IGOSS";
				db.addEPSAttribute("QUALITY_CODE_STD", EPCHAR, valc.length(), (Object) valc);
				String stn = String.valueOf(cycleNums[i]);
				String cast = String.valueOf(castNum);
				db.addEPSAttribute(EPCRUISE, EPCHAR, mCruiseIDs[i].length(), (Object) mCruiseIDs[i]); //platform number
				db.addEPSAttribute("STATION_NUMBER", EPCHAR, stn.length(), (Object) stn); //cycle number
				db.addEPSAttribute(EPCASTNUM, EPCHAR, cast.length(), (Object) cast);  //cycle number
				mOwnerDBase.addSubEntry(db);
			}

			mOwnerDBase.setDataType("CTD");
			String valc = "IGOSS";
			mOwnerDBase.addEPSAttribute("QUALITY_CODE_STD", EPCHAR, valc.length(), (Object) valc);

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}

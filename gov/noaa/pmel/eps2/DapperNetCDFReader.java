package gov.noaa.pmel.eps2;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import ucar.nc2.*;
import ucar.ma2.*;
import ucar.multiarray.*;
import gov.noaa.pmel.util.*;
import gov.noaa.pmel.eps2.dapper.*;

/**
 * <code>DapperNetCDFReader</code> Parse a netCDF file that comes from the
 * Dapper Server
 * 
 * @author oz
 * @version 1.0
 * 
 * @see EPSFileReader
 */
public class DapperNetCDFReader implements EPSConstants {
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
	protected DapperNcFile mNCFile;
	/**
	 * The EPIC Key database
	 */
	protected EPIC_Key_DB mEpicKeyDB = null;
	/**
	 * Optional string for progress string
	 */
	protected String mProgressStr = "Reading netCDF Data...";
	private boolean DEBUG = false;
	private boolean DEBUG2 = false;

	/**
	 * Construct a new <code>DapperNetCDFReader</code> with a Dbase, Dapper netCDF
	 * file, and an EPIC pointer.
	 * 
	 * @param dname
	 *          Dbase that this reader will fill in
	 * @param ncf
	 *          Source Dapper netCDF file
	 * @param ep
	 *          EPIC pointer contains metadata for the Dapper netCDF file
	 * 
	 * @see Dbase
	 * @see DapperNcFile
	 */
	public DapperNetCDFReader(Dbase dname, DapperNcFile ncf, EpicPtr ep) {
		mOwnerDBase = dname;
		mNCFile = ncf;
		mOwnerDBase.setEpicPtr(ep);
		if (ep != null && ep.getProgressStr() != null)
			mProgressStr = new String(ep.getProgressStr());
	}

	/**
	 * Construct a new <code>DapperNetCDFReader</code> with a Dbase, Dapper netCDF
	 * file, EPIC pointer, and a progress string.
	 * 
	 * @param dname
	 *          Dbase that this reader will fill in
	 * @param ncf
	 *          Source Dapper netCDF file
	 * @param ep
	 *          EPIC pointer contains metadata for the Dapper netCDF file
	 * @param progress
	 *          String to display in progress window
	 * 
	 * @see Dbase
	 * @see DapperNcFile
	 */
	public DapperNetCDFReader(Dbase dname, DapperNcFile ncf, EpicPtr ep, String progress) {
		mOwnerDBase = dname;
		mNCFile = ncf;
		mProgressStr = progress;
		mOwnerDBase.setEpicPtr(ep);
	}

	// concrete implementations of the io routines
	/**
	 * Parse the netCDF file and fill in the Dbase.
	 * 
	 */
	public void parse() throws Exception {
		try {
			mEpicKeyDB = new EPIC_Key_DB("epic.key");
			Iterator vi = mNCFile.getVariableIterator();
			int nvars = 0;
			while (vi.hasNext()) {
				nvars++;
				Variable v = (Variable)vi.next();
			}
			if (DEBUG)
				System.out.println("nvars = " + nvars);
			int ngatts = 0;
			// get the global attributes
			Iterator ai = mNCFile.getGlobalAttributeIterator();
			int[] iarray; // nclong
			short[] sarray;
			float[] rarray;
			double[] darray;
			mOwnerDBase.setDataType("UNK");
			String datatype = null;
			String datasubtype = null;
			String insttype = null;
			// iterate through the global attributes for attributes of interest
			while (ai.hasNext()) {
				ngatts++;
				// get the name
				Attribute at = (Attribute)ai.next();
				String name = at.getName();
				if (DEBUG)
					System.out.println("attribute = " + name);
				if (name.equalsIgnoreCase("DATA_TYPE")) {
					datatype = at.getStringValue();
				}
				if (name.equalsIgnoreCase("DATA_SUBTYPE")) {
					datasubtype = at.getStringValue();
					if (datasubtype != null) {
						if (datasubtype.length() == 0)
							datasubtype = null;
						else if (datasubtype.equals(" "))
							datasubtype = null;
					}
				}
				if (name.equalsIgnoreCase("INST_TYPE")) {
					insttype = at.getStringValue();
					if (insttype != null) {
						if (insttype.length() == 0)
							insttype = null;
						else if (insttype.equals(" "))
							insttype = null;
						else if (insttype.toUpperCase().indexOf("XBT") >= 0)
							insttype = "XBT";
						else if (insttype.toUpperCase().indexOf("CTD") >= 0)
							insttype = "CTD";
						else
							insttype = null;
					}
				}
				// get the type
				String type = at.getClass().getName();
				if (DEBUG)
					System.out.println("attribute type = " + type);
				// get the length
				int atlen = at.getLength();
				// install epic attributes
				if (type.equalsIgnoreCase("char")) {
					String valc = at.getStringValue();
					mOwnerDBase.addEPSAttribute(name, EPCHAR, atlen, (Object)valc);
				}
				else if (type.equalsIgnoreCase("short")) {
					// array of shorts
					sarray = new short[atlen];
					for (int i = 0; i < atlen; i++) {
						sarray[i] = ((Short)at.getNumericValue(i)).shortValue();
					}
					mOwnerDBase.addEPSAttribute(name, EPSHORT, atlen, (Object)sarray);
				}
				else if (type.equalsIgnoreCase("int")) {
					// array of ints
					iarray = new int[atlen];
					for (int i = 0; i < atlen; i++) {
						iarray[i] = ((Integer)at.getNumericValue(i)).intValue();
					}
					mOwnerDBase.addEPSAttribute(name, EPINT, atlen, (Object)iarray);
				}
				else if (type.equalsIgnoreCase("long")) {
					// array of longs
					iarray = new int[atlen];
					for (int i = 0; i < atlen; i++) {
						iarray[i] = ((Integer)at.getNumericValue(i)).intValue();
					}
					mOwnerDBase.addEPSAttribute(name, EPINT, atlen, (Object)iarray);
				}
				else if (type.equalsIgnoreCase("float")) {
					// array of floats
					rarray = new float[atlen];
					for (int i = 0; i < atlen; i++) {
						rarray[i] = ((Float)at.getNumericValue(i)).floatValue();
					}
					mOwnerDBase.addEPSAttribute(name, EPREAL, atlen, (Object)rarray);
				}
				else if (type.equalsIgnoreCase("double")) {
					// array of doubles
					darray = new double[atlen];
					for (int i = 0; i < atlen; i++) {
						darray[i] = ((Double)at.getNumericValue(i)).doubleValue();
					}
					mOwnerDBase.addEPSAttribute(name, EPDOUBLE, atlen, (Object)darray);
				}
			}
			if (DEBUG)
				System.out.println("ngatts = " + ngatts);
			if (datatype != null && datasubtype == null)
				mOwnerDBase.setDataType(datatype);
			else if (datasubtype != null)
				mOwnerDBase.setDataType(datasubtype);
			else if (insttype != null)
				mOwnerDBase.setDataType(insttype);
			String valc = null;
			// get the axes by iterating through the dimensions
			Axis[] axlist = new Axis[MAX_NC_DIMS];
			Iterator di = mNCFile.getDimensionIterator();
			int i = 0;
			while (di.hasNext()) {
				Dimension d = (Dimension)di.next();
				int axlen = d.getLength();
				String name = d.getName();
				if (DEBUG)
					System.out.println("Dimension = " + name);
				// build the axis entry
				axlist[i] = new Axis();
				axlist[i].setDimension(true);
				axlist[i].setName(name);
				if (d.isUnlimited()) {
					axlist[i].setUnlimited(true);
				}
				else
					axlist[i].setUnlimited(false);
				// get variable associated with axis
				int varid = mNCFile.getVarID(name);
				if (DEBUG)
					System.out.println("varid = " + varid);
				if (varid == -1) {
					// case when dim is not a var
					axlist[i].setTime(false);
					i++;
					// axlist[i].setAxisType(EPNONEAXIS);
					continue;
				}
				// get standard axis attributes and load into database
				int dtype = mNCFile.getVariableDataType(name);
				if (DEBUG)
					System.out.println("dtype = " + dtype);
				int natts = mNCFile.getVariableAtrributeCount(name);
				if (DEBUG)
					System.out.println("natts = " + natts);
				// get an attribute for varid called "FORTRAN_format"
				String attVal = null;
				if (mNCFile.isAttributeInVariable(name, "FORTRAN_format")) {
					attVal = mNCFile.getVariableAttributeName(name, "FORTRAN_format");
					axlist[i].setFrmt(attVal);
				}
				else {
					attVal = new String("");
					axlist[i].setFrmt(attVal);
				}
				if (DEBUG)
					System.out.println("ff attVal = " + attVal);
				// get an attribute for varid called "units"
				attVal = null;
				if (mNCFile.isAttributeInVariable(name, "units")) {
					attVal = mNCFile.getVariableAttributeName(name, "units");
					axlist[i].setUnits(attVal);
				}
				else {
					attVal = new String("");
					axlist[i].setUnits(attVal);
				}
				if (DEBUG)
					System.out.println("units attVal = " + attVal);
				// get an attribute for varid called "type"
				attVal = null;
				if (mNCFile.isAttributeInVariable(name, "type")) {
					attVal = mNCFile.getVariableAttributeName(name, "type");
					axlist[i].setType(attVal);
				}
				else {
					// check Ferret axis type
					attVal = null;
					if (mNCFile.isAttributeInVariable(name, "point_spacing")) {
						attVal = mNCFile.getVariableAttributeName(name, "units");
						axlist[i].setType(attVal);
					}
					else {
						attVal = new String("");
						axlist[i].setType(attVal);
					}
				}
				if (DEBUG)
					System.out.println("type attVal = " + attVal);
				if (axlist[i].getType().startsWith("e") || axlist[i].getType().startsWith("E"))
					axlist[i].setType(EPEVEN);
				// get epic_code attribute if it exists
				attVal = null;
				if (mNCFile.isAttributeInVariable(name, "epic_code")) {
					attVal = mNCFile.getVariableAttributeName(name, "epic_code");
					axlist[i].setID(Integer.valueOf(attVal).intValue());
				}
				else {
					axlist[i].setID(0);
				}
				if (DEBUG)
					System.out.println("epic_code attVal = " + attVal);
				// load the axis attributes
				for (int j = 0; j < natts; j++) {
					Attribute att = mNCFile.getVariableAttributeByIndex(name, j);
					if (att == null)
						continue;
					String attname = mNCFile.getVariableAttributeName(name, j);
					String attype = mNCFile.getVariableAttributeClass(name, attname);
					int atatlen = mNCFile.getVariableAttributeLength(name, attname);
					if (DEBUG)
						System.out.println(j + " attribute name = " + attname + " attribute type = " + attype);
					if (attype.equalsIgnoreCase("char")) {
						valc = att.getStringValue();
						axlist[i].addAttribute(axlist[i].getAttnum() + 1, attname, EPCHAR, atatlen, (Object)valc);
					}
					else if (attype.equalsIgnoreCase("short")) {
						// array of shorts
						sarray = new short[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							sarray[ii] = ((Short)att.getNumericValue(ii)).shortValue();
						}
						axlist[i].addAttribute(axlist[i].getAttnum() + 1, attname, EPSHORT, atatlen, (Object)sarray);
					}
					else if (attype.equalsIgnoreCase("int")) {
						// array of ints
						iarray = new int[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							iarray[ii] = ((Integer)att.getNumericValue(ii)).intValue();
						}
						axlist[i].addAttribute(axlist[i].getAttnum() + 1, attname, EPINT, atatlen, (Object)iarray);
					}
					else if (attype.equalsIgnoreCase("long")) {
						// array of longs
						iarray = new int[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							iarray[ii] = ((Integer)att.getNumericValue(ii)).intValue();
						}
						axlist[i].addAttribute(axlist[i].getAttnum() + 1, attname, EPINT, atatlen, (Object)iarray);
					}
					else if (attype.equalsIgnoreCase("float")) {
						// array of floats
						rarray = new float[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							rarray[ii] = ((Float)att.getNumericValue(ii)).floatValue();
						}
						axlist[i].addAttribute(axlist[i].getAttnum() + 1, attname, EPREAL, atatlen, (Object)rarray);
					}
					else if (attype.equalsIgnoreCase("double")) {
						// array of doubles
						darray = new double[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							darray[ii] = ((Double)att.getNumericValue(ii)).doubleValue();
						}
						axlist[i].addAttribute(axlist[i].getAttnum() + 1, attname, EPDOUBLE, atatlen, (Object)darray);
					}
				}
				// check axis direction: time or space
				axlist[i].setTime(false);
				axlist[i].setAxisType(EPNONEAXIS);
				axlist[i].setLen(axlen);
				long[] t_orig = new long[2];
				if (axlist[i].getID() > 0) {
					// axis id exists
					if (axlist[i].getID() == 624) { // 624 is Epic Time System
						axlist[i].setTime(true);
						axlist[i].setAxisType(EPTIME);
						axlist[i].setUnits("True Julian Day");
						if (DEBUG)
							System.out.println(" axis type = 624");
					}
					else if (axlist[i].getID() == 625) {
						axlist[i].setTime(false);
						if (dtype == NC_LONG)
							axlist[i].setAxisType(EPINTT);
						else
							axlist[i].setAxisType(EPREALT);
						axlist[i].setUnits("seconds");
						String t_units = new String("seconds");
						t_orig[0] = t_orig[1] = 0;
						if (DEBUG)
							System.out.println(" axis type = 625");
					}
					else if (axlist[i].getID() >= 1 && axlist[i].getID() <= 9) {
						axlist[i].setTime(false);
						axlist[i].setAxisType(EPZAXIS);
						if (DEBUG)
							System.out.println(" axis type = EPZAXIS");
					}
					else if (axlist[i].getID() == 500) {
						axlist[i].setTime(false);
						axlist[i].setAxisType(EPYAXIS);
						if (DEBUG)
							System.out.println(" axis type = EPYAXIS");
					}
					else if (axlist[i].getID() == 501 || axlist[i].getID() == 502) {
						axlist[i].setTime(false);
						axlist[i].setAxisType(EPXAXIS);
						if (DEBUG)
							System.out.println(" axis type = EPXAXIS");
					}
				}
				// If epic_code is not used, look for EPTIME (DOUBLE LONG) by checking
				// the units.
				if (axlist[i].getAxisType() == EPNONEAXIS) {
					if (axlist[i].getUnits().length() > 0) { // not null or 0 length
						String time2_name = axlist[i].getName() + "2";
						if (axlist[i].getUnits().equalsIgnoreCase("True Julian Day")) {
							int var2_id = mNCFile.getVarID(time2_name);
							if (mNCFile.isAttributeInVariable(time2_name, "units")) {
								String valc1 = mNCFile.getVariableAttributeName(time2_name, "units");
								if (valc1.equalsIgnoreCase("msec since 0:00 GMT")) {
									axlist[i].setTime(true);
									axlist[i].setAxisType(EPTIME);
									if (DEBUG)
										System.out.println(" axis type = EPTIME");
								}
							}
						}
					}
				}
				if (axlist[i].getAxisType() == EPNONEAXIS) {
					if (axlist[i].getUnits().length() > 0) { // not null or 0 length
						if (axlist[i].getUnits().indexOf("msec since") >= 0 && axlist[i].getUnits().indexOf("1970") >= 0) {
							axlist[i].setTime(true);
							axlist[i].setAxisType(CANONICALTIME);
							try {
								Variable timeVar = mNCFile.getVariable(axlist[i].getName());
								int rank = timeVar.getRank();
								int[] origin = new int[rank];
								int[] extent = timeVar.getShape();
								ucar.ma2.Array tMA = timeVar.read(origin, extent);
								GeoDate[] gt = new GeoDate[extent[0]];
								for (int t = 0; t < extent[0]; t++) {
									long tim;
									try {
										tim = tMA.getLong(new Index1D(new int[] { i }));
									}
									catch (Exception ex) {
										throw new TimeConversionException("Error getting a double from the multiarray");
									}
									gt[t] = new GeoDate(tim);
								}
								ucar.multiarray.MultiArray tma = new ucar.multiarray.ArrayMultiArray(gt);
								axlist[i].setData(tma);
							}
							catch (Exception pe) {
								System.out.println("parse error");
							}
							if (DEBUG) {
								System.out.println(" axis type = SIMPLETIME because of units check");
							}
						}
					}
				}
				// check FERRET format time by looking for "time_origin" attribute
				if (axlist[i].getAxisType() == EPNONEAXIS) {
					if (DEBUG)
						System.out.println(" checking Ferret format time");
					String t_base = null;
					String t_units = null;
					String t_base2 = null;
					if (mNCFile.isAttributeInVariable(name, "time_origin")) {
						t_base = mNCFile.getVariableAttributeName(name, "time_origin");
						t_units = new String(axlist[i].getUnits());
						t_units.toLowerCase(); // convert all to lower case
						String temp_str = new String(t_units + " time_origin " + t_base);
						axlist[i].setUnits(temp_str);
					}
					else if (axlist[i].getUnits().indexOf("time_origin") >= 0) {
						// sscanf(axlist[i]->units,"%s time_origin %s %s",t_units,t_base,t_base2);
						Object[] objs = { new String(), new String(), new String() };
						MessageFormat msgf = new MessageFormat("{0} time_origin {1} {2}");
						try {
							objs = msgf.parse(axlist[i].getUnits());
							t_units = (String)objs[0];
							t_base = (String)objs[1];
							t_base2 = (String)objs[2];
						}
						catch (ParseException pe) {
							System.out.println("parse error");
						}
						t_base = t_base + " " + t_base2;
						t_units.toLowerCase(); // convert all to lower case
						t_base = t_base + " " + t_base2;
					}
					if (t_base != null) {
						// it is FERRET format
						t_orig = EPS_Util.stringToEpicTime("DD-MMM-YYYY hh:mm:ss", EPS_Util.FERRET_time_str(t_base));
						if (dtype == NC_LONG) {
							axlist[i].setTime(false);
							axlist[i].setAxisType(EPINTT);
						}
						else {
							axlist[i].setTime(false);
							axlist[i].setAxisType(EPREALT);
						}
					}
				}
				if (axlist[i].getAxisType() == EPNONEAXIS) {
					axlist[i].setTime(false);
					axlist[i].setAxisType(EPS_Util.getAxisDirection(axlist[i].getName(), axlist[i].getUnits()));
					if (DEBUG)
						System.out.println(" EPNONEAXIS");
				}
				// check other kinds of time axis, either int or real
				if (axlist[i].getAxisType() == EPTAXIS) {
					if (DEBUG)
						System.out.println(" EPTAXIS");
					String t_units = null;
					String t_base = null;
					String t_base2 = null;
					if (EPS_Util.isNetcdfTimeUnits(axlist[i].getUnits()) > 0) {
						if (axlist[i].getUnits().indexOf("since") >= 0) { // udunits time
																															// format
							if (axlist[i].getUnits().indexOf("(") >= 0 && axlist[i].getUnits().indexOf(")") >= 0) {
								Object[] objs = { new String(), new String(), new String() };
								MessageFormat msgf = new MessageFormat("{0} since ({1} {2})");
								try {
									objs = msgf.parse(axlist[i].getUnits());
									t_units = (String)objs[0];
									t_base = (String)objs[1];
									t_base2 = (String)objs[2];
								}
								catch (ParseException pe) {
									System.out.println("parse error");
								}
							}
							else {
								Object[] objs = { new String(), new String(), new String() };
								MessageFormat msgf = new MessageFormat("{0} since {1} {2}");
								try {
									objs = msgf.parse(axlist[i].getUnits());
									t_units = (String)objs[0];
									t_base = (String)objs[1];
									t_base2 = (String)objs[2];
								}
								catch (ParseException pe) {
									System.out.println("parse error");
								}
							}
							t_base = t_base + " " + t_base2;
						}
						else {
							// sscanf(axlist[i].getUnits(),"%s",t_units);
							Object[] objs = { new String() };
							MessageFormat msgf = new MessageFormat("{0}");
							try {
								objs = msgf.parse(axlist[i].getUnits());
								t_units = (String)objs[0];
							}
							catch (ParseException pe) {
							}
						}
					}
					if (t_base != null)
						t_orig = EPS_Util.stringToEpicTime("YYYY-MM-DD hh:mm:ss.fff", EPS_Util.convertNetCDFTimeString(t_base));
					dtype = mNCFile.getVariableDataType(name);
					if (dtype == NC_LONG) {
						axlist[i].setTime(false);
						axlist[i].setAxisType(EPINTT);
					}
					else {
						axlist[i].setTime(false);
						axlist[i].setAxisType(EPREALT);
					}
				}
				// Load axis value
				if (axlist[i].getAxisType() == EPNONEAXIS) {
					axlist[i].setAxisType(EPSPACE);
					if (DEBUG)
						System.out.println(" EPSPACE");
				}
				if (axlist[i].getAxisType() == EPTIME) {
					if (DEBUG)
						System.out.println(" EPTIME");
					// Double Integer EPIC Time
					Variable timeVar = mNCFile.getVariable(axlist[i].getName());
					int[] origin = new int[timeVar.getRank()];
					int[] extent = timeVar.getShape();
					ucar.ma2.Array tMa = timeVar.read(origin, extent);
					timeVar = mNCFile.getVariable(axlist[i].getName() + "2");
					origin = new int[timeVar.getRank()];
					extent = timeVar.getShape();
					ucar.ma2.Array t2Ma = timeVar.read(origin, extent);
					// store as a GeoDate array
					// array optimization needed
					GeoDate[] gt = new GeoDate[extent[0]];
					for (int t = 0; t < extent[0]; t++) {
						gt[t] = new GeoDate(tMa.getInt(new Index1D(new int[] { t })), t2Ma.getInt(new Index1D(new int[] { t })));
					}
					ucar.multiarray.MultiArray tma = new ucar.multiarray.ArrayMultiArray(gt);
					axlist[i].setData(tma);
				}
				else if (axlist[i].getAxisType() == EPINTT) {
					if (DEBUG)
						System.out.println(" EPINTT");
					// Single Integer Time.
					String t_units = new String(axlist[i].getUnits());
					Variable timeVar = mNCFile.getVariable(axlist[i].getName());
					int rank = timeVar.getRank();
					int[] origin = new int[rank];
					int[] extent = timeVar.getShape();
					ucar.ma2.Array tMA = timeVar.read(origin, extent);
					long[] eptime = EPS_Util.tArrayToEpicTime(tMA, extent[timeVar.getRank() - 1], t_units, t_orig);
					axlist[i].setTime(true);
					axlist[i].setAxisType(EPTIME);
					// store as geodate array
					GeoDate[] gt = new GeoDate[extent[0]];
					for (int t = 0; t < extent[0]; t++) {
						gt[t] = new GeoDate((int)eptime[2 * t], (int)eptime[2 * t + 1]);
					}
					ucar.multiarray.MultiArray tma = new ucar.multiarray.ArrayMultiArray(gt);
					axlist[i].setData(tma);
				}
				else if (axlist[i].getAxisType() == EPREALT) {
					if (DEBUG)
						System.out.println(" EPREALT");
					// Single Real Time
					String t_units = new String(axlist[i].getUnits());
					Variable timeVar = mNCFile.getVariable(axlist[i].getName());
					int[] origin = new int[timeVar.getRank()];
					int[] extent = timeVar.getShape();
					ucar.ma2.Array tMA = timeVar.read(origin, extent);
					long[] eptime = EPS_Util.tArrayToEpicTime(tMA, extent[timeVar.getRank() - 1], t_units, t_orig);
					axlist[i].setTime(true);
					axlist[i].setAxisType(EPREALT);
					// store as geodate array
					GeoDate[] gt = new GeoDate[extent[0]];
					for (int t = 0; t < extent[0]; t++) {
						gt[t] = new GeoDate((int)eptime[2 * t], (int)eptime[2 * t + 1]);
					}
					ucar.multiarray.MultiArray tma = new ucar.multiarray.ArrayMultiArray(gt);
					axlist[i].setData(tma);
				}
				else if (axlist[i].getAxisType() == EPXAXIS || axlist[i].getAxisType() == EPYAXIS
				    || axlist[i].getAxisType() == EPZAXIS || axlist[i].getAxisType() == EPSPACE) {
					if (DEBUG)
						System.out.println(" EPXAXIS, EPYAXIS, EPZAXIS, EPSPACE");
					// Geographical Axis
					Variable geogVar = mNCFile.getVariable(axlist[i].getName());
					int[] origin = new int[geogVar.getRank()];
					int[] extent = geogVar.getShape();
					ucar.ma2.Array gMA = geogVar.read(origin, extent);
					Object rv = gMA.copyTo1DJavaArray();
					double[] vals = null;
					if (rv instanceof float[]) {
						float[] fa = (float[])rv;
						vals = new double[fa.length];
						for (int vv = 0; vv < fa.length; vv++)
							vals[vv] = (double)fa[vv];
					}
					else if (rv instanceof int[]) {
						int[] ia = (int[])rv;
						vals = new double[ia.length];
						for (int vv = 0; vv < ia.length; vv++)
							vals[vv] = (double)ia[vv];
					}
					else if (rv instanceof long[]) {
						long[] la = (long[])rv;
						vals = new double[la.length];
						for (int vv = 0; vv < la.length; vv++)
							vals[vv] = (double)la[vv];
					}
					else if (rv instanceof double[]) {
						double[] da = (double[])rv;
						vals = new double[da.length];
						for (int vv = 0; vv < da.length; vv++)
							vals[vv] = da[vv];
					}
					else if (rv instanceof short[]) {
						short[] sa = (short[])rv;
						vals = new double[sa.length];
						for (int vv = 0; vv < sa.length; vv++)
							vals[vv] = sa[vv];
					}
					ucar.multiarray.MultiArray gMa = new ucar.multiarray.ArrayMultiArray(vals);
					axlist[i].setData(gMa);
				}
				// Install this axis in dbase
				mOwnerDBase.setAxis(axlist[i]);
				i++;
			} // while di
			// get variables that aren't dimensions
			vi = mNCFile.getVariableIterator();
			while (vi.hasNext()) {
				Variable v = (Variable)vi.next();
				String vName = v.getName();
				if (DEBUG2)
					System.out.println(" vName = " + vName);
				int nvardim = mNCFile.getVariableRank(vName);
				int[] vardims = mNCFile.getVariableDims(vName);
				int natts = mNCFile.getVariableAtrributeCount(vName);
				int dtype = mNCFile.getVariableDataType(vName);
				Class clss = mNCFile.getVariableClass(vName);
				// is it an axis variable?
				if (nvardim == 1) {
					if (DEBUG2)
						System.out.println(" is an axis variable = ");
					String time2_name = "time2";// axlist[vardims[0]].getName() + "2";
					if (axlist[vardims[0]].getAxisType() == EPTIME && axlist[vardims[0]].isTimeAxis()
					    && vName.equalsIgnoreCase(axlist[vardims[0]].getName())) {
						// got a file in EPIC time format: make a new time axis variable
						EPSVariable var = new EPSVariable();
						var.setOname(vName);
						var.setDtype(EPDOUBLE);
						// store the class
						var.setVclass(Double.TYPE);
						// add the var to the database
						mOwnerDBase.addEPSVariable(var);
						// get a GeoDate
						ucar.multiarray.MultiArray tma = axlist[vardims[0]].getData();
						int[] lens = tma.getLengths();
						GeoDate gd0 = null;
						try {
							gd0 = (GeoDate)tma.get(new int[] { 0 });
						}
						catch (Exception ex) {
						}
						String dateStr = gd0.toString();
						// make a units attribute
						String attname = "units";
						valc = "days since " + dateStr;
						int atatlen = valc.length();
						var.addAttribute(var.getAttnum() + 1, attname, EPCHAR, atatlen, (Object)valc);
						// make a type attribute
						attname = "type";
						valc = "EVEN";
						atatlen = valc.length();
						var.addAttribute(var.getAttnum() + 1, attname, EPCHAR, atatlen, (Object)valc);
						// store the time axis data in the EPSVariable multiarray
						// array optimization
						if (lens[0] > 1) {
							double[] timeData = new double[lens[0]];
							for (int ii = 0; ii < lens[0]; ii++) {
								GeoDate gd = (GeoDate)tma.get(new int[] { ii });
								// compute the difference between this date and start geodate in
								// days
								GeoDate diff = gd.subtract(gd0);
								timeData[ii] = (double)diff.getTime() / (double)GeoDate.MSECS_IN_DAY;
							}
							ucar.multiarray.MultiArray tima = new ucar.multiarray.ArrayMultiArray(timeData);
							var.setData(tima);
						}
						else {
							double[] timeData = { 0.00 };
							ucar.multiarray.MultiArray tima = new ucar.multiarray.ArrayMultiArray(timeData);
							var.setData(tima);
						}
						continue;
					}
					if (vName.equalsIgnoreCase(time2_name)) {
						if (DEBUG)
							System.out.println(" is time2_name = ");
						continue;
					}
					if (axlist[vardims[0]].getAxisType() == EPNONEAXIS) {
						if (DEBUG)
							System.out.println(" is EPNONEAXIS ");
						continue;
					}
				}
				// instantiate an EPS Variable
				EPSVariable var = new EPSVariable();
				if (DEBUG)
					System.out.println(" creating EPSVariable ");
				// store the class
				var.setVclass(clss);
				// find the data type
				switch (dtype) {
					case NC_BYTE:
						var.setDtype(EPBYTE);
						break;
					case NC_CHAR:
						var.setDtype(EPCHAR);
						break;
					case NC_SHORT:
						var.setDtype(EPSHORT);
						break;
					case NC_LONG:
						var.setDtype(EPINT);
						break;
					case NC_FLOAT:
						var.setDtype(EPREAL);
						break;
					case NC_DOUBLE:
						var.setDtype(EPDOUBLE);
						break;
					default:
						System.out.println("setnext: illegal netCDF data type");
				}
				// load the variable attributes
				for (int j = 0; j < natts; j++) {
					Attribute att = mNCFile.getVariableAttributeByIndex(vName, j);
					if (att == null)
						continue;
					String attname = mNCFile.getVariableAttributeName(vName, j);
					if (DEBUG)
						System.out.println(" adding attribute = " + attname);
					String attype = mNCFile.getVariableAttributeClass(vName, attname);
					int atatlen = mNCFile.getVariableAttributeLength(vName, attname);
					if (attype.equalsIgnoreCase("char")) {
						valc = att.getStringValue();
						var.addAttribute(var.getAttnum() + 1, attname, EPCHAR, atatlen, (Object)valc);
					}
					else if (attype.equalsIgnoreCase("short")) {
						// array of shorts
						sarray = new short[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							sarray[ii] = ((Short)att.getNumericValue(ii)).shortValue();
						}
						var.addAttribute(var.getAttnum() + 1, attname, EPSHORT, atatlen, (Object)sarray);
					}
					else if (attype.equalsIgnoreCase("int")) {
						// array of ints
						iarray = new int[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							iarray[ii] = ((Integer)att.getNumericValue(ii)).intValue();
						}
						var.addAttribute(var.getAttnum() + 1, attname, EPINT, atatlen, (Object)iarray);
					}
					else if (attype.equalsIgnoreCase("long")) {
						// array of longs
						iarray = new int[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							iarray[ii] = ((Integer)att.getNumericValue(ii)).intValue();
						}
						var.addAttribute(var.getAttnum() + 1, attname, EPINT, atatlen, (Object)iarray);
					}
					else if (attype.equalsIgnoreCase("float")) {
						// array of floats
						rarray = new float[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							rarray[ii] = ((Float)att.getNumericValue(ii)).floatValue();
						}
						var.addAttribute(var.getAttnum() + 1, attname, EPREAL, atatlen, (Object)rarray);
					}
					else if (attype.equalsIgnoreCase("double")) {
						// array of doubles
						darray = new double[atatlen];
						for (int ii = 0; ii < atatlen; ii++) {
							darray[ii] = ((Double)att.getNumericValue(ii)).doubleValue();
						}
						var.addAttribute(var.getAttnum() + 1, attname, EPDOUBLE, atatlen, (Object)darray);
					}
				}
				// get EPIC key code
				if (mNCFile.isAttributeInVariable(vName, "epic_code")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "epic_code");
					var.setID(Integer.valueOf(attVal).intValue());
					if (DEBUG)
						System.out.println(" adding epic_code = " + attVal);
				}
				else
					var.setID(0);
				/*
				 * get information from netCDF file. Information in the data file will
				 * be used. If var->id > 0, but information is not found in the data
				 * file, then key file information will be used.
				 */
				Key epKey = null;
				if (var.getID() > 0) {
					try {
						epKey = mEpicKeyDB.findKey(var.getID());
					}
					catch (Exception ex) {
					}
				}
				// set the original name
				var.setOname(vName);
				if (DEBUG)
					System.out.println(" set oname = " + vName);
				// get sname, i.e. "name" attribute of the variable
				if (mNCFile.isAttributeInVariable(vName, "name")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "name");
					var.setSname(attVal);
					if (DEBUG)
						System.out.println(" setSname = " + attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null) {
						var.setSname(epKey.getSname());
						if (DEBUG)
							System.out.println(" setSname = " + epKey.getSname());
					}
					else {
						var.setSname(vName);
						if (DEBUG)
							System.out.println(" setSname = " + vName);
					}
				}
				// get the long name
				if (mNCFile.isAttributeInVariable(vName, "long_name")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "long_name");
					var.setLname(attVal);
					if (DEBUG)
						System.out.println(" setLname = " + attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null) {
						var.setLname(epKey.getLname());
						if (DEBUG)
							System.out.println(" setLname = " + epKey.getLname());
					}
					else {
						var.setLname(vName);
						if (DEBUG)
							System.out.println(" setLname = " + vName);
					}
				}
				// get the generic name
				if (mNCFile.isAttributeInVariable(vName, "generic_name")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "generic_name");
					var.setGname(attVal);
					if (DEBUG)
						System.out.println(" setGname = " + attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null) {
						var.setGname(epKey.getGname());
						if (DEBUG)
							System.out.println(" setGname = " + epKey.getGname());
					}
					else {
						var.setGname(vName);
						if (DEBUG)
							System.out.println(" setGname = " + vName);
					}
				}
				// get the FORTRAN format
				if (mNCFile.isAttributeInVariable(vName, "FORTRAN_format")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "FORTRAN_format");
					var.setFrmt(attVal);
					if (DEBUG)
						System.out.println(" setFrmt = " + attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null) {
						var.setFrmt(epKey.getFrmt());
						if (DEBUG)
							System.out.println(" setFrmt = " + epKey.getFrmt());
					}
					else {
						var.setFrmt(vName);
						if (DEBUG)
							System.out.println(" setFrmt = " + vName);
					}
				}
				// get the units
				if (mNCFile.isAttributeInVariable(vName, "units")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "units");
					var.setUnits(attVal);
					if (DEBUG)
						System.out.println(" setUnits = " + attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null) {
						var.setUnits(epKey.getUnits());
						if (DEBUG)
							System.out.println(" setUnits = " + epKey.getUnits());
					}
					else {
						var.setUnits(vName);
						if (DEBUG)
							System.out.println(" setUnits = " + vName);
					}
				}
				// get information from epic.key file, check it against the variable
				// in the data file.
				if (var.getID() > 0) {
					try {
						if ((epKey = mEpicKeyDB.findKey(var.getID())) != null) {
							String sv = var.getSname().trim();
							String se = epKey.getSname().trim();
							String lv = var.getLname().trim();
							String le = epKey.getLname().trim();
							String gv = var.getGname().trim();
							String ge = epKey.getGname().trim();
							String fv = var.getFrmt().trim();
							String fe = epKey.getFrmt().trim();
							String uv = var.getUnits().trim();
							String ue = epKey.getUnits().trim();
							boolean c1 = sv.equalsIgnoreCase(se);
							boolean c2 = lv.equalsIgnoreCase(le);
							boolean c3 = gv.equalsIgnoreCase(ge);
							boolean c4 = fv.equalsIgnoreCase(fe);
							boolean c5 = uv.equalsIgnoreCase(ue);
							if (!c1 || !c2 || !c3 || !c4 || !c5) {
								;// System.out.println("setnext: attributes of variable code " +
								 // var.getSname() +
								// " are different from these defined in EPIC key file");
								/*
								 * if (!c1) System.out.println("short names don't match " + sv +
								 * "<>" + se); if (!c2)
								 * System.out.println("long names don't match " + lv + "<>" +
								 * le); if (!c3) System.out.println("generic names don't match "
								 * + gv + "<>" + ge); if (!c4)
								 * System.out.println("formats don't match " + fv + " <> " +
								 * fe); if (!c5) System.out.println("units don't match " + uv +
								 * " <> " + ue);
								 */
							}
							else
								;// System.out.println("setnext: attributes of variable code " +
								 // var.getSname() +
							// " are the same as those defined in EPIC key file");
						}
					}
					catch (Exception ex) {
					}
				}
				if (var.getID() < 0) {
					// store as new key
					Key newKey = new Key();
					newKey.setID(var.getID());
					newKey.setSname(var.getSname());
					newKey.setLname(var.getLname());
					newKey.setGname(var.getGname());
					newKey.setFrmt(var.getFrmt());
					newKey.setUnits(var.getUnits());
					newKey.setType(var.getDtype());
					// add to list
					mEpicKeyDB.installKey(newKey);
				}
				// connect variable to axes
				// var->dim_order[i] = 0,1,2,3 means t,z,y,x axis is at the ith location
				// in input var
				// dim_used[0,1,2,3] = true means, t,z,y,x has appeared once in the
				// variable.
				Axis[] varaxis = new Axis[4];
				boolean[] dim_used = new boolean[4];
				// initialize things
				for (int j = 0; j < 4; j++) {
					varaxis[j] = null;
					var.setLci(j, 1);
					var.setUci(j, 1);
					var.setDimorder(j, -1); // means axis is missing
					dim_used[j] = false;
				}
				int zAxisIndex = 0;
				for (int j = 0; j < nvardim; j++) {
					var.setDimorder(j, -2); // means did know the type of the axis
					switch (axlist[vardims[j]].getAxisType()) {
						case EPTAXIS:
						case EPTIME:
							if (!dim_used[0]) {
								varaxis[0] = axlist[vardims[j]];
								var.setDimorder(j, 0);
								dim_used[0] = true;
							}
							break;
						case EPZAXIS:
							if (!dim_used[1]) {
								varaxis[1] = axlist[vardims[j]];
								var.setDimorder(j, 1);
								dim_used[1] = true;
								zAxisIndex = j;
							}
							break;
						case EPYAXIS:
							if (!dim_used[2]) {
								varaxis[2] = axlist[vardims[j]];
								var.setDimorder(j, 2);
								dim_used[2] = true;
							}
							break;
						case EPXAXIS:
							if (!dim_used[3]) {
								varaxis[3] = axlist[vardims[j]];
								var.setDimorder(j, 3);
								dim_used[3] = true;
							}
							break;
						default:
							break;
					}
				}
				// Check and fill in missing axis or axes not know as t,z,y,x
				for (int k = 0; k < 4; k++) {
					if (varaxis[k] == null) {
						boolean found = false;
						int j = 0;
						while (!found && j < nvardim) { // to find unassigned axis of input
																						// variable
							if (var.getDimorder(j) == -2)
								found = true;
							else
								j++;
						}
						if (found) {
							var.setDimorder(j, k);
							varaxis[k] = axlist[vardims[j]];
						}
						else {
							varaxis[k] = null;
							// fill in dummy axis
							varaxis[k] = new Axis();
							varaxis[k].setLen(1);
							varaxis[k].setName(new String(dummyName[k]));
							varaxis[k].setFrmt(new String(""));
							varaxis[k].setUnits(new String(""));
							varaxis[k].setType(new String(""));
							if (k == 0) {
								varaxis[k].setTime(false);
								varaxis[k].setAxisType(EPTDUMMY);
								GeoDate[] fmad = { new GeoDate() };
								ArrayMultiArray ma = new ArrayMultiArray(fmad);
								varaxis[k].setData(ma);
							}
							else {
								double[] fmad = { 0.00 };
								ArrayMultiArray ma = new ArrayMultiArray(fmad);
								varaxis[k].setData(ma);
								varaxis[k].setTime(false);
								if (k == 1)
									varaxis[k].setAxisType(EPZDUMMY);
								if (k == 2)
									varaxis[k].setAxisType(EPYDUMMY);
								if (k == 3)
									varaxis[k].setAxisType(EPXDUMMY);
							}
						}
					}
					var.setUci(3 - k, varaxis[k].getLen());
				}
				var.setT(varaxis[0]);
				var.setZ(varaxis[1]);
				var.setY(varaxis[2]);
				var.setX(varaxis[3]);
				
				// store in a multiarray
				int[] origin = new int[v.getRank()];
				int[] extent = v.getShape();
				ucar.ma2.Array tMa = v.read(origin, extent);
				Object rv = tMa.copyTo1DJavaArray();
				double[] vals = null;
				if (rv instanceof float[]) {
					float[] fa = (float[])rv;
					vals = new double[fa.length];
					for (int vv = 0; vv < fa.length; vv++) {
						vals[vv] = (double)fa[vv];
						if (DEBUG) {
							if (vv < 10)
								System.out.println(vv + " float val = " + vals[vv]);
						}
					}
				}
				else if (rv instanceof int[]) {
					int[] ia = (int[])rv;
					vals = new double[ia.length];
					for (int vv = 0; vv < ia.length; vv++) {
						vals[vv] = (double)ia[vv];
					}
				}
				else if (rv instanceof long[]) {
					long[] la = (long[])rv;
					vals = new double[la.length];
					for (int vv = 0; vv < la.length; vv++)
						vals[vv] = (double)la[vv];
				}
				else if (rv instanceof double[]) {
					double[] da = (double[])rv;
					vals = new double[da.length];
					for (int vv = 0; vv < da.length; vv++)
						vals[vv] = da[vv];
				}
				else if (rv instanceof short[]) {
					short[] sa = (short[])rv;
					vals = new double[sa.length];
					for (int vv = 0; vv < sa.length; vv++)
						vals[vv] = sa[vv];
				}
				ucar.multiarray.MultiArray gMa = new ucar.multiarray.ArrayMultiArray(vals);
				var.setData(gMa);
				mOwnerDBase.addEPSVariable(var);
			} // while vi
		}
		catch (IOException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
}
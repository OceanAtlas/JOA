package gov.noaa.pmel.eps2;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import ucar.netcdf.*;
import ucar.multiarray.*;
import gov.noaa.pmel.util.*;

/**
 * <code>netCDFReader</code> Concrete implementation of the EPSFileReader
 * interface to read, parse, and save a netCDF file
 * 
 * @author oz
 * @version 1.0
 * 
 * @see EPSFileReader
 */
public class netCDFReader implements EPSFileReader, EPSConstants {
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
	protected String mProgressStr = "Reading netCDF Data...";
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
	 */
	public netCDFReader(Dbase dname, EPSNetCDFFile ncf, EpicPtr ep) {
		mOwnerDBase = dname;
		mNCFile = ncf;
		mOwnerDBase.setEpicPtr(ep);
		if (ep != null && ep.getProgressStr() != null)
			mProgressStr = new String(ep.getProgressStr());
	}

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
	 */
	public netCDFReader(Dbase dname, EPSNetCDFFile ncf, EpicPtr ep, String progress) {
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
		boolean isArgoNetCDF = false;
		boolean isUOTNetCDF = false;
		boolean isWOCENetCDF = false;
		try {
			mEpicKeyDB = new EPIC_Key_DB("epic.key");
			VariableIterator vi = mNCFile.iterator();
			int nvars = 0;
			while (vi.hasNext()) {
				nvars++;
				Variable v = vi.next();
			}
			int ngatts = mNCFile.getAttributes().size();
			// get the global attributes
			AttributeIterator ai = mNCFile.getAttributes().iterator();
			int[] iarray; // nclong
			short[] sarray;
			float[] rarray;
			double[] darray;
			mOwnerDBase.setDataType("UNK");
			String datatype = null;
			String datasubtype = null;
			String insttype = null;
			while (ai.hasNext()) {
				// get the name
				Attribute at = ai.next();
				String name = at.getName();
				if (name.equalsIgnoreCase("conventions")) {
					String conventions = at.getStringValue().toLowerCase().trim();
					if (conventions.equalsIgnoreCase("coards/woce")) {
						isWOCENetCDF = true;
					}
				}
				if (name.equalsIgnoreCase("title")) {
					String title = at.getStringValue().toLowerCase().trim();
					if (title.indexOf("us nodc argo") >= 0) {
						isArgoNetCDF = true;
						// add an attribute to specify the qc std
						String valc = "IGOSS";
						mOwnerDBase.addEPSAttribute("QUALITY_CODE_STD", EPCHAR, valc.length(), (Object)valc);
						datatype = "Argo CTD";
					}
					else if (title.indexOf("global temperature-salinity profile") >= 0) {
						isUOTNetCDF = true;
						// add an attribute to specify the qc std
						String valc = "IGOSS";
						mOwnerDBase.addEPSAttribute("QUALITY_CODE_STD", EPCHAR, valc.length(), (Object)valc);
					}
				}
				if (name.equalsIgnoreCase("project_name")) {
					String proj = at.getStringValue().toLowerCase().trim();
					if (proj.indexOf("argo") >= 0) {
						isArgoNetCDF = true;
						// add an attribute to specify the qc std
						String valc = "IGOSS";
						mOwnerDBase.addEPSAttribute("QUALITY_CODE_STD", EPCHAR, valc.length(), (Object)valc);
					}
				}
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
						if (insttype.toUpperCase().indexOf("XBT") >= 0)
							insttype = "XBT";
						else if (insttype.toUpperCase().indexOf("CTD") >= 0)
							insttype = "CTD";
						else
							insttype = null;
					}
				}
				// get the type
				String type = at.getComponentType().getName();
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
			if (datatype != null && datasubtype == null)
				mOwnerDBase.setDataType(datatype);
			else if (datasubtype != null)
				mOwnerDBase.setDataType(datasubtype);
			else if (insttype != null)
				mOwnerDBase.setDataType(insttype);
			String valc = null;
			
			// get the axes
			Axis[] axlist = new Axis[MAX_NC_DIMS];
			Axis xAxis = null;
			Axis yAxis = null;
			Axis zAxis = null;
			Axis tAxis = null;
			DimensionIterator di = mNCFile.getDimensions().iterator();
			int i = 0;
			while (di.hasNext()) {
				Dimension d = di.next();
				int axlen = d.getLength();
				String name = d.getName();
				if (DEBUG)
					System.out.println("dimension #" + i + " = " + name);
				// build the axis entry
				axlist[i] = new Axis();
				axlist[i].setDimension(true);
				axlist[i].setName(name);
				if (d instanceof UnlimitedDimension) {
					axlist[i].setUnlimited(true);
				}
				else
					axlist[i].setUnlimited(false);
				// get variable associated with axis
				int varid = mNCFile.getVarID(name);
				if (varid == -1) {
					// case when dim is not a var
					axlist[i].setTime(false);
					i++;
					// axlist[i].setAxisType(EPNONEAXIS);
					continue;
				}
				// get standard axis attributes and load into database
				int dtype = mNCFile.getVariableDataType(name);
				int natts = mNCFile.getVariableAtrributeCount(name);
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
				if (axlist[i].getType().startsWith("e") || axlist[i].getType().startsWith("E"))
					axlist[i].setType(EPEVEN);
				// get epic_code attribute
				attVal = null;
				if (mNCFile.isAttributeInVariable(name, "epic_code")) {
					attVal = mNCFile.getVariableAttributeName(name, "epic_code");
					axlist[i].setID(Integer.valueOf(attVal).intValue());
				}
				else {
					axlist[i].setID(0);
				}
				// load the axis attributes why is there 34 attributes?
				for (int j = 0; j < natts; j++) {
					Attribute att = mNCFile.getVariableAttributeByIndex(name, j);
					if (att == null)
						continue;
					String attname = mNCFile.getVariableAttributeName(name, j);
					String attype = mNCFile.getVariableAttributeClass(name, attname);
					int atatlen = mNCFile.getVariableAttributeLength(name, attname);
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
					if (axlist[i].getID() == 624) { // what's 624? Epic Time System?
						axlist[i].setTime(true);
						axlist[i].setAxisType(EPTIME);
						axlist[i].setUnits("True Julian Day");
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
						tAxis = axlist[i];
					}
					else if (axlist[i].getID() >= 1 && axlist[i].getID() <= 9) {
						axlist[i].setTime(false);
						axlist[i].setAxisType(EPZAXIS);
						if (zAxis == null) {
							zAxis = axlist[i];
							if (DEBUG)
								System.out.println(" #1 zAxis = " + zAxis);
						}
					}
					else if (axlist[i].getID() == 500) {
						axlist[i].setTime(false);
						axlist[i].setAxisType(EPYAXIS);
						yAxis = axlist[i];
					}
					else if (axlist[i].getID() == 501 || axlist[i].getID() == 502) {
						axlist[i].setTime(false);
						axlist[i].setAxisType(EPXAXIS);
						xAxis = axlist[i];
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
									;
									tAxis = axlist[i];
								}
							}
						}
					}
				}
				// check FERRET format time by looking for "time_origin" attribute
				if (axlist[i].getAxisType() == EPNONEAXIS) {
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
						tAxis = axlist[i];
					}
				}
				if (axlist[i].getAxisType() == EPNONEAXIS) {
					axlist[i].setTime(false);
					axlist[i].setAxisType(EPS_Util.getAxisDirection(axlist[i].getName(), axlist[i].getUnits()));
				}
				
				// check other kinds of time axis, either int or real
				if (axlist[i].getAxisType() == EPTAXIS) {
					String t_units = null;
					String t_base = null;
					String t_base2 = null;
					if (EPS_Util.isNetcdfTimeUnits(axlist[i].getUnits()) > 0) {
						if (axlist[i].getUnits().indexOf("since") >= 0) { // udunits time
							// format
							if (axlist[i].getUnits().indexOf("(") >= 0 && axlist[i].getUnits().indexOf(")") >= 0) {
								// sscanf(axlist[i].getUnits(),"%s since (%s %s)
								// ,t_units,t_base,t_base2);
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
								// sscanf(axlist[i].getUnits(),"%s since %s %s",t_units,t_base,t_base2);
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
								pe.printStackTrace();
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
					tAxis = axlist[i];
				}
				// Load axis value
				if (axlist[i].getAxisType() == EPNONEAXIS)
					axlist[i].setAxisType(EPSPACE);
				if (axlist[i].getAxisType() == EPTIME) {
					// Double Integer EPIC Time
					Variable timeVar = mNCFile.get(axlist[i].getName());
					int[] origin = new int[timeVar.getRank()];
					int[] extent = timeVar.getLengths();
					MultiArray tMa = (MultiArray)timeVar.copyout(origin, extent);
					timeVar = mNCFile.get(axlist[i].getName() + "2");
					origin = new int[timeVar.getRank()];
					extent = timeVar.getLengths();
					MultiArray t2Ma = (MultiArray)timeVar.copyout(origin, extent);
					// store as a GeoDate array
					// array optimization
					GeoDate[] gt = new GeoDate[extent[0]];
					for (int t = 0; t < extent[0]; t++) {
						gt[t] = new GeoDate(tMa.getInt(new int[] { t }), t2Ma.getInt(new int[] { t }));
					}
					MultiArray tma = new ArrayMultiArray(gt);
					axlist[i].setData(tma);
					tAxis = axlist[i];
				}
				else if (axlist[i].getAxisType() == EPINTT) {
					// Single Integer Time.
					String t_units = new String(axlist[i].getUnits());
					Variable timeVar = mNCFile.get(axlist[i].getName());
					int rank = timeVar.getRank();
					int[] origin = new int[rank];
					int[] extent = timeVar.getLengths();
					MultiArray tMA = (MultiArray)timeVar.copyout(origin, extent);
					long[] eptime = EPS_Util.tArrayToEpicTime(tMA, extent[timeVar.getRank() - 1], t_units, t_orig);
					axlist[i].setTime(true);
					axlist[i].setAxisType(EPTIME);
					// store as geodate array
					GeoDate[] gt = new GeoDate[extent[0]];
					for (int t = 0; t < extent[0]; t++) {
						gt[t] = new GeoDate((int)eptime[2 * t], (int)eptime[2 * t + 1]);
					}
					MultiArray tma = new ArrayMultiArray(gt);
					axlist[i].setData(tma);
					tAxis = axlist[i];
				}
				else if (axlist[i].getAxisType() == EPREALT) {
					// Single Real Time
					String t_units = new String(axlist[i].getUnits());
					Variable timeVar = mNCFile.get(axlist[i].getName());
					int[] origin = new int[timeVar.getRank()];
					int[] extent = timeVar.getLengths();
					MultiArray tMA = (MultiArray)timeVar.copyout(origin, extent);
					boolean err = false;
					long[] eptime = null;
					try {
						eptime = EPS_Util.tArrayToEpicTime(tMA, extent[timeVar.getRank() - 1], t_units, t_orig);
					}
					catch (Exception ex) {
						err = true;
					}
					axlist[i].setTime(true);
					axlist[i].setAxisType(EPREALT);
					// store as geodate array
					GeoDate[] gt = new GeoDate[extent[0]];
					for (int t = 0; t < extent[0]; t++) {
						if (!err)
							gt[t] = new GeoDate((int)eptime[2 * t], (int)eptime[2 * t + 1]);
						else
							gt[t] = new GeoDate();
					}
					MultiArray tma = new ArrayMultiArray(gt);
					axlist[i].setData(tma);
					tAxis = axlist[i];
				}
				else if (axlist[i].getAxisType() == EPXAXIS || axlist[i].getAxisType() == EPYAXIS
				    || axlist[i].getAxisType() == EPZAXIS || axlist[i].getAxisType() == EPSPACE) {
					// Geographical Axis
					Variable geogVar = mNCFile.get(axlist[i].getName());
					int[] origin = new int[geogVar.getRank()];
					int[] extent = geogVar.getLengths();
					MultiArray gMa = (MultiArray)geogVar.copyout(origin, extent);
					axlist[i].setData(gMa);
					if (axlist[i].getAxisType() == EPXAXIS)
						xAxis = axlist[i];
					else if (axlist[i].getAxisType() == EPYAXIS)
						yAxis = axlist[i];
					else if (axlist[i].getAxisType() == EPZAXIS) {
						if (zAxis == null) {
							zAxis = axlist[i];
							if (DEBUG)
								System.out.println(" #2 zAxis = " + zAxis);
						}
					}
					else if (axlist[i].getAxisType() == EPSPACE) {
						if (zAxis == null) {
							zAxis = axlist[i];
							if (DEBUG)
								System.out.println(" #3 zAxis = " + zAxis);
						}
					}
				}
				// Install this axis
				mOwnerDBase.setAxis(axlist[i]);
				i++;
			} // while di
			
			// store variables that aren't dimensions
			short[] tempQCs = null;
			short[] saltQCs = null;
			short[] presQCs = null;
			EPSVariable pressVar = null;
			vi = mNCFile.iterator();
			while (vi.hasNext()) {
				Variable v = vi.next();
				String vName = v.getName();
				int nvardim = mNCFile.getVariableRank(vName);
				int[] vardims = mNCFile.getVariableDims(vName);
				int natts = mNCFile.getVariableAtrributeCount(vName);
				int dtype = mNCFile.getVariableDataType(vName);
				Class clss = mNCFile.getVariableClass(vName);
				if (DEBUG)
					System.out.println(vName + "\t" + dtype + "\t" + clss + "\t" + nvardim);
				// special case of UOT single profile netCDF
				if (isUOTNetCDF) {
					if (vName.equalsIgnoreCase("cruise_id")) {
						// map this to a CRUISE global attribute
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray tMa = (MultiArray)v.copyout(origin, extent);
						char[] fn = EPS_Util.get1DCharArray(tMa, EPS_Util.getMeasuredDim(tMa));
						String cruise = new String(fn);
						mOwnerDBase.addEPSAttribute(EPCRUISE, EPCHAR, cruise.length(), (Object)cruise);
						continue;
					}
					else if (vName.equalsIgnoreCase("station_id")) {
						// map this to a Station global attribute
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray tMa = (MultiArray)v.copyout(origin, extent);
						int[] fn = EPS_Util.get1DIntArray(tMa, EPS_Util.getMeasuredDim(tMa));
						String stn = String.valueOf(fn[0]);
						mOwnerDBase.addEPSAttribute("STATION_NUMBER", EPCHAR, stn.length(), (Object)stn);
						continue;
					}
					else if (vName.indexOf("DEPH_qparm") >= 0) {
						// make a qc variable for pressure qc codes
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray qcMa = (MultiArray)v.copyout(origin, extent);
						char[] qcs = EPS_Util.get1DCharArray(qcMa, 0);
						String qcStr = new String(qcs).trim();
						presQCs = new short[qcStr.length()];
						int cnt = 0;
						for (int q = 0; q < qcStr.length(); q++) {
							try {
								presQCs[q] = Short.valueOf(qcStr.substring(q, q + 1)).shortValue();
								cnt++;
							}
							catch (Exception ex) {
								presQCs[q] = 9;
							}
						}
						if (DEBUG)
							System.out.println("found QC for UOT Pressure variable, numvals = " + cnt);
						continue;
					}
					else if (vName.indexOf("TEMP_qparm") >= 0) {
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray qcMa = (MultiArray)v.copyout(origin, extent);
						char[] qcs = EPS_Util.get1DCharArray(qcMa, 0);
						String qcStr = new String(qcs).trim();
						tempQCs = new short[qcStr.length()];
						for (int q = 0; q < qcStr.length(); q++) {
							try {
								tempQCs[q] = Short.valueOf(qcStr.substring(q, q + 1)).shortValue();
							}
							catch (Exception ex) {
								tempQCs[q] = 9;
							}
						}
						continue;
					}
					else if (vName.indexOf("PSAL_qparm") >= 0) {
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray qcMa = (MultiArray)v.copyout(origin, extent);
						char[] qcs = EPS_Util.get1DCharArray(qcMa, 0);
						String qcStr = new String(qcs).trim();
						saltQCs = new short[qcStr.length()];
						for (int q = 0; q < qcStr.length(); q++) {
							try {
								saltQCs[q] = Short.valueOf(qcStr.substring(q, q + 1)).shortValue();
							}
							catch (Exception ex) {
								saltQCs[q] = 9;
							}
						}
						continue;
					}
					else if (dtype == 2 && nvardim == 1) {
						// turn a character attribute into a global attribute
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray tMa = (MultiArray)v.copyout(origin, extent);
						char[] ca = EPS_Util.get1DCharArray(tMa, EPS_Util.getMeasuredDim(tMa));
						String sval = new String(ca);
						mOwnerDBase.addEPSAttribute(vName, EPCHAR, sval.length(), (Object)sval);
						continue;
					}
					else if (nvardim < 4)
						continue;
				}
				
				// special case of argo single profile netCDF
				if (isArgoNetCDF) {
					if (vName.equalsIgnoreCase("float_number")) {
						// map this to a CRUISE global attribute
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray tMa = (MultiArray)v.copyout(origin, extent);
						char[] fn = EPS_Util.get1DCharArray(tMa, EPS_Util.getMeasuredDim(tMa));
						String cruise = new String(fn);
						mOwnerDBase.addEPSAttribute(EPCRUISE, EPCHAR, cruise.length(), (Object)cruise);
						continue;
					}
					else if (vName.equalsIgnoreCase("cycle_number")) {
						// map this to a CAST global attribute
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray tMa = (MultiArray)v.copyout(origin, extent);
						short[] fn = EPS_Util.get1DShortArray(tMa, EPS_Util.getMeasuredDim(tMa));
						String stn = String.valueOf(fn[0]);
						mOwnerDBase.addEPSAttribute("STATION_NUMBER", EPCHAR, stn.length(), (Object)stn);
						continue;
					}
					else if (vName.equalsIgnoreCase("pressure_qc") || vName.equalsIgnoreCase("pres_qc")) {
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray qcMa = (MultiArray)v.copyout(origin, extent);
						char[] qcs = EPS_Util.get1DCharArray(qcMa, 0);
						String qcStr = new String(qcs).trim();
						presQCs = new short[qcStr.length()];
						int cnt = 0;
						for (int q = 0; q < qcStr.length(); q++) {
							try {
								presQCs[q] = Short.valueOf(qcStr.substring(q, q + 1)).shortValue();
								cnt++;
							}
							catch (Exception ex) {
								presQCs[q] = 9;
							}
						}
						continue;
					}
					else if (vName.indexOf("ARGOPRS_FLAG") >= 0) {
						if (DEBUG)
							System.out.println("found QC for Argo Pressure variable, numvals = ");
					}
					else if (vName.equalsIgnoreCase("temperature_qc")) {
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray qcMa = (MultiArray)v.copyout(origin, extent);
						char[] qcs = EPS_Util.get1DCharArray(qcMa, 0);
						String qcStr = new String(qcs).trim();
						tempQCs = new short[qcStr.length()];
						for (int q = 0; q < qcStr.length(); q++) {
							try {
								tempQCs[q] = Short.valueOf(qcStr.substring(q, q + 1)).shortValue();
							}
							catch (Exception ex) {
								tempQCs[q] = 9;
							}
						}
						continue;
					}
					else if (vName.indexOf("ARGOTMP_FLAG") >= 0) {
						if (DEBUG)
							System.out.println("found QC for Argo temp variable, numvals = ");
					}
					else if (vName.equalsIgnoreCase("salinity_qc")) {
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray qcMa = (MultiArray)v.copyout(origin, extent);
						char[] qcs = EPS_Util.get1DCharArray(qcMa, 0);
						String qcStr = new String(qcs).trim();
						saltQCs = new short[qcStr.length()];
						for (int q = 0; q < qcStr.length(); q++) {
							try {
								saltQCs[q] = Short.valueOf(qcStr.substring(q, q + 1)).shortValue();
							}
							catch (Exception ex) {
								saltQCs[q] = 9;
							}
						}
						continue;
					}
					else if (vName.indexOf("ARGOSAL_FLAG") >= 0) {
						if (DEBUG)
							System.out.println("found QC for Argo salt variable, numvals = ");
					}
					else if (dtype == 2 && nvardim == 1) {
						// turn a character attribute into a global attribute
						int[] origin = new int[v.getRank()];
						int[] extent = v.getLengths();
						MultiArray tMa = (MultiArray)v.copyout(origin, extent);
						char[] ca = EPS_Util.get1DCharArray(tMa, EPS_Util.getMeasuredDim(tMa));
						String sval = new String(ca);
						mOwnerDBase.addEPSAttribute(vName, EPCHAR, sval.length(), (Object)sval);
						continue;
					}
					else if (nvardim < 4)
						continue;
				}
				// is it an axis variable?
				if (nvardim == 1) {
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
						MultiArray tma = axlist[vardims[0]].getData();
						int[] lens = tma.getLengths();
						GeoDate gd0 = null;
						try {
							gd0 = (GeoDate)tma.get(new int[] { 0 });
						}
						catch (Exception ex) {
							ex.printStackTrace();
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
							MultiArray tima = new ArrayMultiArray(timeData);
							var.setData(tima);
						}
						else {
							double[] timeData = { 0.00 };
							MultiArray tima = new ArrayMultiArray(timeData);
							var.setData(tima);
						}
						continue;
					}
					if (vName.equalsIgnoreCase(time2_name)) {
						continue;
					}
					if (axlist[vardims[0]].getAxisType() == EPNONEAXIS) {
						if (DEBUG)
							System.out.println("EPNONEAXIS");
						continue;
					}
					if (dtype == 2) {
						// convert this variable to a global attribute
					}
				}
				// instantiate an EPS Variable
				EPSVariable var = new EPSVariable();
				boolean mapMissing = false;
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
				double fileMissingValue = -99;
				for (int j = 0; j < natts; j++) {
					Attribute att = mNCFile.getVariableAttributeByIndex(vName, j);
					if (att == null)
						continue;
					String attname = mNCFile.getVariableAttributeName(vName, j).trim();
					String attype = mNCFile.getVariableAttributeClass(vName, attname);
					int atatlen = mNCFile.getVariableAttributeLength(vName, attname);
					if (attname.toLowerCase().indexOf("_fillvalue") >= 0) {
						mapMissing = true;
						if (attype.equalsIgnoreCase("char")) {
							String fvals = att.getStringValue();
							fileMissingValue = Double.valueOf(fvals).doubleValue();
						}
						else if (attype.equalsIgnoreCase("short")) {
							// array of shorts
							short sval = ((Short)att.getNumericValue()).shortValue();
							fileMissingValue = (double)sval;
						}
						else if (attype.equalsIgnoreCase("int")) {
							// array of ints
							int ival = ((Integer)att.getNumericValue()).intValue();
							fileMissingValue = (double)ival;
						}
						else if (attype.equalsIgnoreCase("long")) {
							// array of longs
							long lval = ((Integer)att.getNumericValue()).intValue();
							fileMissingValue = (double)lval;
						}
						else if (attype.equalsIgnoreCase("float")) {
							// array of floats
							float fval = ((Float)att.getNumericValue()).floatValue();
							fileMissingValue = (double)fval;
						}
						else if (attype.equalsIgnoreCase("double")) {
							fileMissingValue = ((Double)att.getNumericValue()).doubleValue();
						}
						mapMissing = fileMissingValue != MISSINGVALUE;
					}
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
				// get sname, i.e. "name" attribute of the variable
				if (mNCFile.isAttributeInVariable(vName, "name")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "name");
					var.setSname(attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null)
						var.setSname(epKey.getSname());
					else
						var.setSname(vName);
				}
				// get the long name
				if (mNCFile.isAttributeInVariable(vName, "long_name")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "long_name");
					var.setLname(attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null)
						var.setLname(epKey.getLname());
					else
						var.setLname(vName);
				}
				// get the generic name
				if (mNCFile.isAttributeInVariable(vName, "generic_name")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "generic_name");
					var.setGname(attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null)
						var.setGname(epKey.getGname());
					else
						var.setGname(vName);
				}
				// get the FORTRAN format
				if (mNCFile.isAttributeInVariable(vName, "FORTRAN_format")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "FORTRAN_format");
					var.setFrmt(attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null)
						var.setFrmt(epKey.getFrmt());
					else
						var.setFrmt(vName);
				}
				// get the units
				if (mNCFile.isAttributeInVariable(vName, "units")) {
					String attVal = mNCFile.getVariableAttributeName(vName, "units");
					var.setUnits(attVal);
				}
				else {
					if (var.getID() > 0 && epKey != null)
						var.setUnits(epKey.getUnits());
					else
						var.setUnits(vName);
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
				// dim_used[0,1,2,3] = 1 means, t,z,y,x has appeared once in the
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
				
				// store a multiarray
				int[] origin = new int[v.getRank()];
				int[] extent = v.getLengths();
				MultiArray tMa = (MultiArray)v.copyout(origin, extent);
				
				if (isWOCENetCDF) {
					double[] varray = EPS_Util.get1DDoubleArray(tMa, EPS_Util.getMeasuredDim(tMa));
					// replace the data in variables multiarray with this new data
					double[][][][] newVals = new double[1][varray.length][1][1];
					for (int b = 0; b < varray.length; b++) {
						if (DEBUG)
							System.out.println(v.getName() + " b = " + b + " val = " + varray[b]);
						newVals[0][b][0][0] = varray[b];
					}
					tMa = new ArrayMultiArray(newVals);
					try {
						var.setData(tMa);
					}
					catch (Exception ex) {
						throw ex;
					}
				}
				
				if (mapMissing && (isArgoNetCDF || isUOTNetCDF)) {
					double[] varray = null;
					try {
						varray = EPS_Util.get1DDoubleArray(tMa, EPS_Util.getMeasuredDim(tMa));
						for (int ii = 0; ii < varray.length; ii++) {
							double val = varray[ii];
							if (val == fileMissingValue)
								varray[ii] = MISSINGVALUE;
						}
						
						// replace the data in variables multiarray with this new data
						double[][][][] newVals = new double[1][varray.length][1][1];
						for (int b = 0; b < varray.length; b++) {
							if (DEBUG)
								System.out.println(v.getName() + "b = " + b + " val = " + varray[b]);
							newVals[0][b][0][0] = varray[b];
						}
						tMa = new ArrayMultiArray(newVals);
						try {
							var.setData(tMa);
						}
						catch (Exception ex) {
							throw ex;
						}
					}
					catch (Exception ex) {
						// missing value mapping failed
					}
				}
				else
					var.setData(tMa);
				mOwnerDBase.addEPSVariable(var);
			} // while vi
			
			if (isUOTNetCDF && presQCs != null) {
				// first make a real pressure variable from the z axis
				if (DEBUG)
					System.out.println("zAxis = " + zAxis.getName());
				MultiArray tMa = zAxis.getData();
				double[] pres = EPS_Util.get1DDoubleArray(tMa, EPS_Util.getMeasuredDim(tMa));
				pressVar = new EPSVariable();
				pressVar.setOname(zAxis.getName());
				pressVar.setSname(zAxis.getName());
				pressVar.setLname(zAxis.getName());
				pressVar.setGname(zAxis.getName());
				pressVar.setDtype(EPSConstants.EPDOUBLE);
				pressVar.setVclass(Double.TYPE);
				pressVar.setUnits(zAxis.getUnits());
				if (tAxis != null && zAxis != null && yAxis != null && xAxis != null) {
					boolean[] dimUsed = { true, true, true, true };
					pressVar.setDimorder(0, 0);
					pressVar.setDimorder(1, 1);
					pressVar.setDimorder(2, 2);
					pressVar.setDimorder(3, 3);
					pressVar.setT(tAxis);
					pressVar.setZ(zAxis);
					pressVar.setY(yAxis);
					pressVar.setX(xAxis);
					// set the data
					float[][][][] presaa = new float[1][pres.length][1][1];
					for (int b = 0; b < pres.length; b++) {
						presaa[0][b][0][0] = (float)pres[b];
					}
					MultiArray presma = new ArrayMultiArray(presaa);
					try {
						pressVar.setData(presma);
					}
					catch (Exception ex) {
						throw ex;
					}
					if (DEBUG)
						System.out.println("UOT added a new presVar");
					mOwnerDBase.addEPSVariable(pressVar);
					// make a new variable for the pressure qc values
					EPSVariable qcvar = new EPSVariable();
					String qcVar = pressVar.getName() + "_QC";
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
					qcvar.setT(pressVar.getT());
					qcvar.setZ(pressVar.getZ());
					qcvar.setY(pressVar.getY());
					qcvar.setX(pressVar.getX());
					// set the data
					// create storage for the qc variables
					short[][][][] qcaa = new short[1][tempQCs.length][1][1];
					for (int b = 0; b < tempQCs.length; b++) {
						qcaa[0][b][0][0] = (short)tempQCs[b];
						// System.out.println("qcaa =" + b + " " + qcaa[0][b][0][0]);
					}
					MultiArray qcma = new ArrayMultiArray(qcaa);
					try {
						qcvar.setData(qcma);
					}
					catch (Exception ex) {
						throw ex;
					}
					mOwnerDBase.addEPSVariable(qcvar);
					// add an attribute to current variable that points to this new qc
					pressVar
					    .addAttribute(pressVar.getAttnum() + 1, "OBS_QC_VARIABLE", EPSConstants.EPCHAR, qcVar.length(), qcVar);
				}
			}
			if (isArgoNetCDF && presQCs != null) {
				// first make a real pressure variable from the z axis
				MultiArray tMa = zAxis.getData();
				double[] pres = EPS_Util.get1DDoubleArray(tMa, EPS_Util.getMeasuredDim(tMa));
				pressVar = new EPSVariable();
				pressVar.setOname(zAxis.getName());
				pressVar.setSname(zAxis.getName());
				pressVar.setLname(zAxis.getName());
				pressVar.setGname(zAxis.getName());
				pressVar.setDtype(EPSConstants.EPDOUBLE);
				pressVar.setVclass(Double.TYPE);
				pressVar.setUnits(zAxis.getUnits());
				if (tAxis != null && zAxis != null && yAxis != null && xAxis != null) {
					boolean[] dimUsed = { true, true, true, true };
					pressVar.setDimorder(0, 0);
					pressVar.setDimorder(1, 1);
					pressVar.setDimorder(2, 2);
					pressVar.setDimorder(3, 3);
					pressVar.setT(tAxis);
					pressVar.setZ(zAxis);
					pressVar.setY(yAxis);
					pressVar.setX(xAxis);
					// set the data
					float[][][][] presaa = new float[1][pres.length][1][1];
					for (int b = 0; b < pres.length; b++) {
						presaa[0][b][0][0] = (float)pres[b];
						if (DEBUG)
							System.out.println("b = " + b + " pres = " + pres[b]);
					}
					MultiArray presma = new ArrayMultiArray(presaa);
					try {
						pressVar.setData(presma);
					}
					catch (Exception ex) {
						throw ex;
					}
					// make a new variable for the pressure qc values
					EPSVariable qcvar = new EPSVariable();
					String qcVar = pressVar.getName() + "_QC";
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
					qcvar.setT(pressVar.getT());
					qcvar.setZ(pressVar.getZ());
					qcvar.setY(pressVar.getY());
					qcvar.setX(pressVar.getX());
					// set the data
					// create storage for the qc variables
					short[][][][] qcaa = new short[1][tempQCs.length][1][1];
					for (int b = 0; b < tempQCs.length; b++) {
						qcaa[0][b][0][0] = (short)tempQCs[b];
						// System.out.println("qcaa =" + b + " " + qcaa[0][b][0][0]);
					}
					MultiArray qcma = new ArrayMultiArray(qcaa);
					try {
						qcvar.setData(qcma);
					}
					catch (Exception ex) {
						throw ex;
					}
					mOwnerDBase.addEPSVariable(qcvar);
					// add an attribute to current variable that points to this new qc
					pressVar
					    .addAttribute(pressVar.getAttnum() + 1, "OBS_QC_VARIABLE", EPSConstants.EPCHAR, qcVar.length(), qcVar);
					if (DEBUG)
						System.out.println("Argo added a new presVar");
					mOwnerDBase.addEPSVariable(pressVar);
				}
			}
			
			if ((isArgoNetCDF || isUOTNetCDF) && tempQCs != null) {
				// make a new variable for the qc values
				EPSVariable oldVar = mOwnerDBase.getEPSVariableByFuzzyName("temperature");
				if (oldVar != null) {
					EPSVariable qcvar = new EPSVariable();
					String qcVar = oldVar.getName() + "_QC";
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
					qcvar.setT(oldVar.getT());
					qcvar.setZ(oldVar.getZ());
					qcvar.setY(oldVar.getY());
					qcvar.setX(oldVar.getX());
					// set the data
					// create storage for the qc variables
					short[][][][] qcaa = new short[1][tempQCs.length][1][1];
					for (int b = 0; b < tempQCs.length; b++) {
						qcaa[0][b][0][0] = (short)tempQCs[b];
						// System.out.println("qcaa =" + b + " " + qcaa[0][b][0][0]);
					}
					MultiArray qcma = new ArrayMultiArray(qcaa);
					try {
						qcvar.setData(qcma);
					}
					catch (Exception ex) {
						throw ex;
					}
					mOwnerDBase.addEPSVariable(qcvar);
					// add an attribute to current variable that points to this new qc
					oldVar.addAttribute(oldVar.getAttnum() + 1, "OBS_QC_VARIABLE", EPSConstants.EPCHAR, qcVar.length(), qcVar);
				}
			}
			
			if ((isArgoNetCDF || isUOTNetCDF) && saltQCs != null) {
				// make a new variable for the qc values
				EPSVariable oldVar = mOwnerDBase.getEPSVariableByFuzzyName("salinity");
				if (oldVar != null) {
					EPSVariable qcvar = new EPSVariable();
					String qcVar = oldVar.getName() + "_QC";
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
					qcvar.setT(oldVar.getT());
					qcvar.setZ(oldVar.getZ());
					qcvar.setY(oldVar.getY());
					qcvar.setX(oldVar.getX());
					// set the data
					// create storage for the qc variables
					short[][][][] qcaa = new short[1][saltQCs.length][1][1];
					for (int b = 0; b < saltQCs.length; b++) {
						qcaa[0][b][0][0] = (short)saltQCs[b];
						// System.out.println("qcaa =" + b + " " + qcaa[0][b][0][0]);
					}
					MultiArray qcma = new ArrayMultiArray(qcaa);
					try {
						qcvar.setData(qcma);
					}
					catch (Exception ex) {
						throw ex;
					}
					mOwnerDBase.addEPSVariable(qcvar);
					// add an attribute to current variable that points to this new qc
					oldVar.addAttribute(oldVar.getAttnum() + 1, "OBS_QC_VARIABLE", EPSConstants.EPCHAR, qcVar.length(), qcVar);
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * Get variable data from netCDF file.
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
		// get variable data from netCDF file
		EPSVariable var;
		if ((var = mOwnerDBase.getEPSVariable(inName)) == null) { throw new EPSVarDoesNotExistExcept(
		    "getvar: Variable not found (var code = (" + inName + ")"); }
		// get data from the netCDF file
		/*
		 * int var_id; String name = null; if (var.getID() < 0) { var_id =
		 * mNCFile.getVarID(name); name = var.getSname(); } else {
		 * //sprintf(name,"%s_%d\0", var.getSname(), var.getID()); String frmt = new
		 * String("{0}_{1,number}"); MessageFormat msgf = new MessageFormat(frmt);
		 * Object[] objs = {new String(var.getSname()), new Integer(var.getID())};
		 * StringBuffer out = new StringBuffer(); msgf.format(objs, out, null); name
		 * = new String(out); var_id = mNCFile.getVarID(name); }
		 */
		Variable ncVar = mNCFile.get(inName);
		int[] origin = new int[ncVar.getRank()];
		int[] extent = ncVar.getLengths();
		try {
			MultiArray tMa = (MultiArray)ncVar.copyout(origin, extent);
			return tMa;
		}
		catch (IOException ex) {
			System.out.println("throwing");
			throw ex;
		}
	}

	/**
	 * Get variable data from netCDF file.
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
	public MultiArray getvar(String inName, int[] lci, int[] uci, int[] dims) throws EPSVarDoesNotExistExcept,
	    IOException {
		// get variable data from netCDF file
		EPSVariable var;
		if ((var = mOwnerDBase.getEPSVariable(inName)) == null) { throw new EPSVarDoesNotExistExcept(
		    "getvar: Variable not found (var code = (" + inName + ")"); }
		// check range of request against stored data
		for (int i = 0; i < 4; i++) {
			if ((lci[i] < var.getLci(i)) || (lci[i] > var.getUci(i)) || (uci[i] > var.getUci(i)) || (uci[i] < var.getLci(i))) {
				String EpErrorMessage = "getvar: requested data range outside available data (var code = " + inName + ")";
				// eps_set_error(EpErrorMessage);
				return null;
			}
		}
		// check that array dimensions are great enough
		for (int i = 0; i < 4; i++) {
			if ((uci[i] - lci[i] + 1) > dims[i]) {
				String EpErrorMessage = "getvar: requested data exceeds array storage (var code = " + inName + ")";
				// eps_set_error(EpErrorMessage);
				return null;
			}
		}
		// Check the number of dimension of that variable in netCDf file
		int ndim = 4;
		int sizei = 1;
		int[] dimi = new int[4];
		int[] tmp_dim = new int[4];
		for (int i = 0; i < 4; i++) {
			if (var.getDimorder(i) < 0)
				ndim--;
			dimi[i] = uci[i] - lci[i] + 1;
			tmp_dim[3 - i] = dimi[i];
			sizei = sizei * dimi[i];
		}
		// get data from the netCDF file
		int var_id;
		String name = null;
		if (var.getID() < 0) {
			var_id = mNCFile.getVarID(name);
			name = var.getSname();
		}
		else {
			// sprintf(name,"%s_%d\0", var.getSname(), var.getID());
			String frmt = new String("{0}_{1,number}");
			MessageFormat msgf = new MessageFormat(frmt);
			Object[] objs = { new String(var.getSname()), new Integer(var.getID()) };
			StringBuffer out = new StringBuffer();
			msgf.format(objs, out, null);
			name = new String(out);
			var_id = mNCFile.getVarID(name);
		}
		long[] start = new long[ndim];
		long[] count = new long[ndim];
		for (int i = 0; i < ndim; i++) {
			start[i] = lci[3 - var.getDimorder(i)] - var.getLci(3 - var.getDimorder(i));
			count[i] = uci[3 - var.getDimorder(i)] - lci[3 - var.getDimorder(i)] + 1;
		}
		// check the order of dimension
		boolean right_order = true;
		for (int i = 0; i < ndim - 1 && right_order; i++)
			if (var.getDimorder(i) > var.getDimorder(i + 1))
				right_order = false;
		boolean whole_size = false;
		if ((dimi[0] == dims[0]) && (dimi[1] == dims[1]) && (dimi[2] == dims[2]) && (dimi[3] == dims[3]))
			whole_size = true;
		if (right_order) {
			// in right order
			if (whole_size) {
				// the whole size
				// getVariableget(db->fileid, var_id, start, count, array);
				Variable getVariable = mNCFile.get(name);
				int[] origin = new int[getVariable.getRank()];
				int[] extent = getVariable.getLengths();
				// MultiArray tMa = (MultiArray)getVariable.copyout(origin, extent);
			}
			else {
				;/*
					 * inarr = (epvoid *)eps_malloc(wsize*sizei);
					 * getVariableget(db->fileid, var_id, start, count, inarr);
					 * eps_copy_in_array(inarr,dimi, array,dims, wsize); eps_free((char
					 * **)&inarr);
					 */
			}
		}
		else {
			// dimension order must be swapped
			;/*
				 * tmparr = (epvoid *)eps_malloc(wsize*sizei);
				 * getVariableget(db->fileid, var_id, start, count, tmparr); if
				 * (whole_size) eps_swap_array(tmparr, array, tmp_dim,
				 * var->dim_order,wsize,1); else { inarr = (epvoid
				 * *)eps_malloc(wsize*sizei); eps_swap_array(tmparr, inarr, tmp_dim,
				 * var->dim_order,wsize,1); eps_copy_in_array(inarr,dimi,
				 * array,dims,wsize); eps_free((char **)&inarr); } eps_free((char
				 * **)&tmparr);
				 */
		}
		return null;
	}
}
package gov.noaa.pmel.eps2;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*; //import ucar.netcdf.*;
import ucar.multiarray.*;
import gov.noaa.pmel.util.*;
import ucar.nc2.*;
import ucar.nc2.dods.DODSNetcdfFile;
import dods.dap.DODSException;
import java.net.MalformedURLException;
import ucar.ma2.Array;

/**
 * <code>netCDFReader</code> Concrete implementation of the EPSFileReader
 * interface to read, parse, and save a netCDF file
 * 
 * @author oz
 * @version 1.0
 * 
 * @see EPSFileReader
 */
public class DODSnetCDFReader implements EPSFileReader, EPSConstants {
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
	protected String mURL;
	protected DODSNcFile ncFile = null;

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
	public DODSnetCDFReader(Dbase dname, String URL, EpicPtr ep) {
		mOwnerDBase = dname;
		mURL = URL;
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
	public DODSnetCDFReader(Dbase dname, String URL, EpicPtr ep, String progress) {
		mOwnerDBase = dname;
		mURL = URL;
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
			ncFile = new DODSNcFile(mURL);
		}
		catch (dods.dap.DODSException e) {
			return;
		}
		catch (MalformedURLException e) {
			return;
		}
		catch (IOException e) {
			return;
		}
		// Get an epic key database specific to JOA
		EPIC_Key_DB mEpicKeyDB = new EPIC_Key_DB("joa_epic.key");
		// Get an epic key database specific to JOA
		EPIC_Key_DB mOrigEpicKeyDB = new EPIC_Key_DB("epic.key");
		// create a vector for temporary storage of the dbases
		Vector dBases = new Vector(100);
		// try to get the global attributes
		Iterator itor = ncFile.getGlobalAttributeIterator();
		int[] iarray;
		short[] sarray;
		float[] rarray;
		double[] darray;
		mOwnerDBase.setDataType("UNK");
		String datatype = "DODS";
		String datasubtype = "GRID";
		String insttype = "UNK";
		while (itor.hasNext()) {
			// get the name
			Attribute at = (ucar.nc2.Attribute)itor.next();
			String name = at.getName();
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
			String type = at.getValueType().getName();
			// get the length
			int atlen = at.getLength();
			// install global attributes
			if (type.equalsIgnoreCase("char") || type.equalsIgnoreCase("java.lang.String")) {
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
		// get the axes
		Axis[] axlist = new Axis[MAX_NC_DIMS];
		// Iterator di = ncFile.getDimensionVariables();
		Iterator di = ncFile.getDimensionIterator();
		int i = 0;
		while (di.hasNext()) {
			ucar.nc2.Dimension dim = (ucar.nc2.Dimension)di.next();
			// Variable var = (Variable)di.next();
			int axlen = dim.getLength();
			// get variable associated with axis
			Variable var = dim.getCoordinateVariable();
			Class c = var.getClass();
			// String varType = c.getName();
			// System.out.println("varType = " + varType);
			String name = var.getName();
			// build the axis entry
			axlist[i] = new Axis();
			axlist[i].setDimension(true);
			axlist[i].setName(name);
			axlist[i].setUnits(new String(""));
			axlist[i].setType(EPEVEN);
			if (name.equalsIgnoreCase("time"))
				axlist[i].setTime(true);
			else
				axlist[i].setTime(false);
			axlist[i].setAxisType(EPNONEAXIS);
			axlist[i].setLen(axlen);
			if (dim.isUnlimited()) {
				axlist[i].setUnlimited(true);
			}
			else
				axlist[i].setUnlimited(false);
			// get attributes for dimension
			Iterator ai = var.getAttributeIterator();
			while (ai.hasNext()) {
				Attribute at = (ucar.nc2.Attribute)ai.next();
				String attVal = at.getStringValue();
				name = at.getName();
				String attype = at.getValueType().getName();
				int atatlen = at.getLength();
				// set the units
				if (name.equalsIgnoreCase("units")) {
					axlist[i].setUnits(attVal);
				}
				if (attype.equalsIgnoreCase("java.lang.String")) {
					axlist[i].addAttribute(axlist[i].getAttnum() + 1, name, EPCHAR, atatlen, (Object)attVal);
				}
			}
			// check axis direction: time or space
			int axisDirec = EPS_Util.getAxisDirection(axlist[i].getName(), axlist[i].getUnits());
			axlist[i].setAxisType(axisDirec);
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
						}
					}
					long[] t_orig = new long[2];
					if (t_base != null) {
						String cTime = EPS_Util.convertNetCDFTimeString(t_base);
						// System.out.println("cTime = " + cTime);
						t_orig = EPS_Util.stringToEpicTime("YYYY-MM-DD hh:mm:ss.fff", cTime);
						// get the values for the time axis an put into a multiarray
						int[] origin = new int[var.getRank()];
						int[] extent = var.getShape();
						Object anArray = null;
						try {
							Array arr = var.read(origin, extent);
							anArray = arr.copyTo1DJavaArray();
							// double[] tmp = (double[])anArray;
							ArrayMultiArray tMA = new ArrayMultiArray(anArray);
							long[] eptime = EPS_Util.tArrayToEpicTime(tMA, extent[var.getRank() - 1], t_units, t_orig);
							axlist[i].setTime(true);
							axlist[i].setAxisType(EPTIME);
							// store as geodate array
							GeoDate[] gt = new GeoDate[extent[0]];
							for (int t = 0; t < extent[0]; t++) {
								gt[t] = new GeoDate((int)eptime[2 * t], (int)eptime[2 * t + 1]);
							}
							MultiArray tma = new ArrayMultiArray(gt);
							axlist[i].setData(tma);
						}
						catch (IOException e) {
							System.out.println(e);
						}
					}
					else {
						// failed trying to parse a udunit time
					}
				}
				else {
					// fail if time not in udunits
				}
			}
			else if (axlist[i].getAxisType() == EPXAXIS || axlist[i].getAxisType() == EPYAXIS
			    || axlist[i].getAxisType() == EPZAXIS || axlist[i].getAxisType() == EPSPACE) {
				// Geographical Axis
				int[] origin = new int[var.getRank()];
				int[] extent = var.getShape();
				Object anArray = null;
				try {
					Array arr = var.read(origin, extent);
					anArray = arr.copyTo1DJavaArray();
					// double[] tmp = (double[])anArray;
					ArrayMultiArray gMa = new ArrayMultiArray(anArray);
					axlist[i].setData(gMa);
				}
				catch (IOException e) {
					System.out.println(e);
				}
			}
			// Install this axis
			mOwnerDBase.setAxis(axlist[i]);
			i++;
		}
		// get the measured variables
		Iterator vi = ncFile.getNonDimensionVariables();
		i = 0;
		while (vi.hasNext()) {
			Variable mvar = (Variable)vi.next();
			String name = mvar.getName();
			Class clss = mvar.getElementType();
			int nvardim = mvar.getRank();
			// int[] vardims = mvar.getShape();
			int[] vardims = getVariableDims(mvar);
			int dtype = -1;
			if (mvar != null) {
				String type = clss.getName();
				if (type.equalsIgnoreCase("char")) {
					dtype = EPSConstants.NC_CHAR;
				}
				else if (type.equalsIgnoreCase("short")) {
					dtype = EPSConstants.NC_SHORT;
				}
				else if (type.equalsIgnoreCase("int")) {
					dtype = EPSConstants.NC_LONG;
				}
				else if (type.equalsIgnoreCase("long")) {
					dtype = EPSConstants.NC_LONG;
				}
				else if (type.equalsIgnoreCase("float")) {
					dtype = EPSConstants.NC_FLOAT;
				}
				else if (type.equalsIgnoreCase("double")) {
					dtype = EPSConstants.NC_DOUBLE;
				}
			}
			// System.out.println("var = " + name);
			// instantiate an EPS Variable
			EPSVariable evar = new EPSVariable();
			// store the class
			evar.setVclass(clss);
			// find the data type
			switch (dtype) {
				case NC_BYTE:
					evar.setDtype(EPBYTE);
					break;
				case NC_CHAR:
					evar.setDtype(EPCHAR);
					break;
				case NC_SHORT:
					evar.setDtype(EPSHORT);
					break;
				case NC_LONG:
					evar.setDtype(EPINT);
					break;
				case NC_FLOAT:
					evar.setDtype(EPREAL);
					break;
				case NC_DOUBLE:
					evar.setDtype(EPDOUBLE);
					break;
				default:
					System.out.println("setnext: illegal netCDF data type");
			}
			// get attributes for variable
			Iterator ai = mvar.getAttributeIterator();
			while (ai.hasNext()) {
				Attribute at = (ucar.nc2.Attribute)ai.next();
				String attname = at.getName();
				// get the type
				String type = at.getValueType().getName();
				// get the length
				int atlen = at.getLength();
				// install global attributes
				if (type.equalsIgnoreCase("char") || type.equalsIgnoreCase("java.lang.String")) {
					String valc = at.getStringValue();
					evar.addAttribute(evar.getAttnum() + 1, attname, EPCHAR, atlen, (Object)valc);
				}
				else if (type.equalsIgnoreCase("short")) {
					// array of shorts
					sarray = new short[atlen];
					for (int ii = 0; ii < atlen; ii++) {
						sarray[ii] = ((Short)at.getNumericValue(ii)).shortValue();
					}
					evar.addAttribute(evar.getAttnum() + 1, attname, EPSHORT, atlen, (Object)sarray);
				}
				else if (type.equalsIgnoreCase("int")) {
					// array of ints
					iarray = new int[atlen];
					for (int ii = 0; ii < atlen; ii++) {
						iarray[ii] = ((Integer)at.getNumericValue(ii)).intValue();
					}
					evar.addAttribute(evar.getAttnum() + 1, attname, EPINT, atlen, (Object)iarray);
				}
				else if (type.equalsIgnoreCase("long")) {
					// array of longs
					iarray = new int[atlen];
					for (int ii = 0; ii < atlen; ii++) {
						iarray[ii] = ((Integer)at.getNumericValue(ii)).intValue();
					}
					evar.addAttribute(evar.getAttnum() + 1, attname, EPINT, atlen, (Object)iarray);
				}
				else if (type.equalsIgnoreCase("float")) {
					// array of floats
					rarray = new float[atlen];
					for (int ii = 0; ii < atlen; ii++) {
						rarray[ii] = ((Float)at.getNumericValue(ii)).floatValue();
					}
					evar.addAttribute(evar.getAttnum() + 1, attname, EPREAL, atlen, (Object)rarray);
				}
				else if (type.equalsIgnoreCase("double")) {
					// array of doubles
					darray = new double[atlen];
					for (int ii = 0; ii < atlen; ii++) {
						darray[ii] = ((Double)at.getNumericValue(ii)).doubleValue();
					}
					evar.addAttribute(evar.getAttnum() + 1, attname, EPDOUBLE, atlen, (Object)darray);
				}
			}
			// get EPIC key code
			Attribute att = null;
			if ((att = mvar.findAttributeIgnoreCase("epic_code")) != null) {
				String attVal = att.getStringValue();
				evar.setID(Integer.valueOf(attVal).intValue());
			}
			else
				evar.setID(this.mIDCount--);
			/*
			 * get information from netCDF file. Information in the data file will be
			 * used. If var->id > 0, but information is not found in the data file,
			 * then key file information will be used.
			 */
			Key epKey = null;
			if (evar.getID() > 0) {
				try {
					epKey = mEpicKeyDB.findKey(evar.getID());
				}
				catch (Exception ex) {
				}
			}
			// set the original name
			evar.setOname(name);
			// get sname, i.e. "name" attribute of the variable
			if ((att = mvar.findAttributeIgnoreCase("name")) != null) {
				String attVal = att.getStringValue();
				evar.setSname(attVal);
			}
			else {
				if (evar.getID() > 0 && epKey != null)
					evar.setSname(epKey.getSname());
				else
					evar.setSname(name);
			}
			// get the long name
			if ((att = mvar.findAttributeIgnoreCase("long_name")) != null) {
				String attVal = att.getStringValue();
				evar.setLname(attVal);
			}
			else {
				if (evar.getID() > 0 && epKey != null)
					evar.setLname(epKey.getLname());
				else
					evar.setLname(name);
			}
			// get the generic name
			if ((att = mvar.findAttributeIgnoreCase("generic_name")) != null) {
				String attVal = att.getStringValue();
				evar.setGname(attVal);
			}
			else {
				if (evar.getID() > 0 && epKey != null)
					evar.setGname(epKey.getGname());
				else
					evar.setGname(name);
			}
			// get the FORTRAN format
			if ((att = mvar.findAttributeIgnoreCase("FORTRAN_format")) != null) {
				String attVal = att.getStringValue();
				evar.setFrmt(attVal);
			}
			else {
				if (evar.getID() > 0 && epKey != null)
					evar.setFrmt(epKey.getFrmt());
				else
					evar.setFrmt(name);
			}
			// get the units
			if ((att = mvar.findAttributeIgnoreCase("units")) != null) {
				String attVal = att.getStringValue();
				evar.setUnits(attVal);
			}
			else {
				if (evar.getID() > 0 && epKey != null)
					evar.setUnits(epKey.getUnits());
				else
					evar.setUnits(name);
			}
			// get information from epic.key file, check it against the variable
			// in the data file.
			if (evar.getID() > 0) {
				try {
					if ((epKey = mEpicKeyDB.findKey(evar.getID())) != null) {
						String sv = evar.getSname().trim();
						String se = epKey.getSname().trim();
						String lv = evar.getLname().trim();
						String le = epKey.getLname().trim();
						String gv = evar.getGname().trim();
						String ge = epKey.getGname().trim();
						String fv = evar.getFrmt().trim();
						String fe = epKey.getFrmt().trim();
						String uv = evar.getUnits().trim();
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
							 * System.out.println("long names don't match " + lv + "<>" + le);
							 * if (!c3) System.out.println("generic names don't match " + gv +
							 * "<>" + ge); if (!c4) System.out.println("formats don't match "
							 * + fv + " <> " + fe); if (!c5)
							 * System.out.println("units don't match " + uv + " <> " + ue);
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
			if (evar.getID() < 0) {
				// store as new key
				Key newKey = new Key();
				newKey.setID(evar.getID());
				newKey.setSname(evar.getSname());
				newKey.setLname(evar.getLname());
				newKey.setGname(evar.getGname());
				newKey.setFrmt(evar.getFrmt());
				newKey.setUnits(evar.getUnits());
				newKey.setType(evar.getDtype());
				// add to list
				mEpicKeyDB.installKey(newKey);
			}
			// connect variable to axes
			// var->dim_order[i] = 0,1,2,3 means t,z,y,x axis is at the ith location
			// in input var
			// dim_used[0,1,2,3] = 1 means, t,z,y,x has appeared once in the variable.
			Axis[] varaxis = new Axis[4];
			boolean[] dim_used = new boolean[4];
			// initialize things
			for (int j = 0; j < 4; j++) {
				varaxis[j] = null;
				evar.setLci(j, 1);
				evar.setUci(j, 1);
				evar.setDimorder(j, -1); // means axis is missing
				dim_used[j] = false;
			}
			for (int j = 0; j < nvardim; j++) {
				evar.setDimorder(j, -2); // means did know the type of the axis
				switch (axlist[vardims[j]].getAxisType()) {
					case EPTAXIS:
					case EPTIME:
						if (!dim_used[0]) {
							varaxis[0] = axlist[vardims[j]];
							evar.setDimorder(j, 0);
							dim_used[0] = true;
						}
						break;
					case EPZAXIS:
						if (!dim_used[1]) {
							varaxis[1] = axlist[vardims[j]];
							evar.setDimorder(j, 1);
							dim_used[1] = true;
						}
						break;
					case EPYAXIS:
						if (!dim_used[2]) {
							varaxis[2] = axlist[vardims[j]];
							evar.setDimorder(j, 2);
							dim_used[2] = true;
						}
						break;
					case EPXAXIS:
						if (!dim_used[3]) {
							varaxis[3] = axlist[vardims[j]];
							evar.setDimorder(j, 3);
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
						if (evar.getDimorder(j) == -2)
							found = true;
						else
							j++;
					}
					if (found) {
						evar.setDimorder(j, k);
						varaxis[k] = axlist[vardims[j]];
					}
					else {
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
				evar.setUci(3 - k, varaxis[k].getLen());
			}
			evar.setT(varaxis[0]);
			evar.setZ(varaxis[1]);
			evar.setY(varaxis[2]);
			evar.setX(varaxis[3]);
			// store a multiarray
			int[] origin = new int[mvar.getRank()];
			int[] extent = mvar.getShape();
			Array arr = mvar.read(origin, extent);
			Object anArray = arr.copyToNDJavaArray();
			// double[] tmp = (double[])anArray;
			ArrayMultiArray tMA = new ArrayMultiArray(anArray);
			evar.setData(tMA);
			mOwnerDBase.addEPSVariable(evar);
			// System.out.println("tMA  rank = " + tMA.getRank());
			int[] maextent = tMA.getLengths();
			// System.out.println("numLevels = " + numLevels);
			// System.out.println("numLats = " + numLats);
			// System.out.println("numLons = " + numLons);
			int numTimes = extent[0];
			int numLevels = extent[1];
			int numLats = extent[2];
			int numLons = extent[3];
			// System.out.println("numTimes = " + numTimes);
			// System.out.println("numLevels = " + numLevels);
			// System.out.println("numLats = " + numLats);
			// System.out.println("numLons = " + numLons);
			// slicing test
			// MultiArray sma = sliceMA(2, 90);
			// try to create a multarray that represents an individual profile
			/*
			 * int[] torigin = {0, 0, 0, 0}; int[] textent = {1, 33, 1, 1}; Array tarr
			 * = mvar.read(origin, extent); Object atArray = tarr.copyTo1DJavaArray();
			 * ArrayMultiArray ttMA = new ArrayMultiArray(atArray);; double[] varray =
			 * null; try { varray = EPS_Util.get1DDoubleArray(ttMA,
			 * EPS_Util.getMeasuredDim(ttMA)); } catch (Exception ex) {}
			 * System.out.println("len of varray = " + varray.length); //for (int
			 * jj=0; jj<varray.length; jj++) // System.out.println("varray[" + jj +
			 * "] = " + varray[jj]);
			 */
		}
		// turn this dbase into individual profiles
		// a profile is a location (lon, lat), some z values, and at time = n
		// if more than one time, have to prompt user to select a time
		// for each profile, I have to add a variable for each measured parameter
		// each profile gets new axes with lat, lon, and time having only one value
		// the only axis with multiple values is the z axis
		ncFile.close();
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
		return null;
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
		return null;
	}

	/**
	 * Get an array of dimensions for a named variable.
	 * 
	 * @param name
	 *          Variable name to look for in file
	 * 
	 * @return Array of lengths for the variable's dimensions.
	 **/
	private int[] getVariableDims(Variable v) {
		// this may fail if variable is a scaler
		if (v != null) {
			int rank = v.getRank();
			int[] ra = new int[rank];
			if (rank == 1)
				return ra;
			else {
				// get a dimension iterator for the variable
				List al = v.getDimensions();
				int vc = 0;
				for (int i = 0; i < al.size(); i++) {
					Dimension vd = (Dimension)al.get(i);
					// loop through the axis dimensions
					// the array gets filled with ids not the length of the dimension
					Iterator di = ncFile.getDimensionIterator();
					int ii = 0;
					while (di.hasNext()) {
						Dimension d = (Dimension)di.next();
						if (vd.getName().equals(d.getName()))
							ra[vc] = ii;// = d.getLength(); // not sure about this one
						ii++;
					}
					vc++;
				}
				return ra;
			}
		}
		return null;
	}
}
package gov.noaa.pmel.eps2;

import java.io.*;
import java.awt.AWTEvent;
import java.awt.Component;
import java.util.*;
import ucar.multiarray.*;
import gov.noaa.pmel.util.*;
import java.text.*;
import ucar.netcdf.*;


@SuppressWarnings("serial")
public class Dbase extends Component implements EPSConstants {
	Dbase mParent;
	/**
	* mode of data file (EPREAD, EPCREATE and EPEDIT)
	*/
	protected int mMode;
	/**
	* String description of data type (ctd, bottle, etc...)
	*/
	protected String mType; 
	/**
	* maximum attribute number in system
	*/
	protected int mAttnum;
	/**
	* name of the unlimited dimension (axis) 
	*/
	protected String mUnlimited;
	/**
	* axes
	*/
	protected Vector<Axis> mAxes;
	/**
	* vector of EPSVariable
	*/
	protected Vector<EPSVariable> mVariables;
	/**
	* vector of EPSAttribute
	*/
	protected Vector<EPSAttribute> mAttributes;
	/**
	* The file reader object attached to this database
	*/
	protected EPSFileReader mFileReader;
	/**
	* Database iterator
	*/
	protected EPSDBIterator mDBItor = null;
	/**
	* Sub-Databases i.e., section
	*/
	protected Vector<Dbase> mDBEntries = null;
	/**
	* registered listeners to events sent to the Dbase
	*/
	protected Vector<FileReadProgressListener> mListeners = new Vector<FileReadProgressListener>();
	/**
	* File associated with this database record (used primarily for section databases)
	*/
	protected String mFile;
	/**
	* The actual EPIC pointer entry that created this database record
	*/
	protected EpicPtr mPtr;
	
	private boolean DEBUG = false;
	private Vector<String> mComments = new Vector<String> ();
	
	private EPIC_Key_DB keyDB = null;
	
  	/**
   	* Construct a new <code>Dbase</code> .
   	*/
	public Dbase() {
		super();
		init();
	}
	
	/**
	* Close this database
	*/
	public void close() {
		mDBItor.remove(this);
	}
	
	/**
	* Instantiate the vectors for axes, variables, and attributes
	*/
	public void init() {
		mAxes = new Vector<Axis>();
		mVariables = new Vector<EPSVariable>();
		mAttributes = new Vector<EPSAttribute>();
		this.enableEvents(AWTEvent.KEY_EVENT_MASK);
	}
	
	/**
	* Sets the file reader object for this database
	*
   	* @param inReader File reader
   	*
 	* @see EPSFileReader
	*/
	public void setFileReader(EPSFileReader inReader) {
		mFileReader = inReader;
	}
	
  	/**
  	* Get dbase file reader.
   	*
  	* @return file reader
   	**/
	public EPSFileReader getFileReader() {
		return mFileReader;
	}
	
  	/**
   	* Set the descriptive name of the type of data this is, e.g., ctd, bottle, time series.
   	*
   	* @param s unlimited dimension name
   	*/
	public void setDataType(String inType) {
		mType = new String(inType);
	}
	
  	/**
  	* Get the data type.
   	*
  	* @return data type
   	**/
	public String getDataType() {
		return mType;
	}
	
  	/**
   	* Set the name of the unlimited dimension.
   	*
   	* @param s unlimited dimension name
   	*/
	public void setUnlimited(String s) {
		mUnlimited = null;
		mUnlimited = new String(s);
	}
	
 	/**
  	* Get the name of the unlimited dimension.
   	*
  	* @return unlimited dimension
   	**/	
	public String getUnlimited() {
		return mUnlimited;
	}
	
 	/**
  	* Get the read/write mode of the database.
   	*
  	* @return mode
   	**/	
	public int getMode() {
		return mMode;
	}
	
  	/**
   	* Set the read/write mode of the database.
   	* @param m mode code
   	*/
	public void setMode(int m) {
		mMode = m;
	}
	
 	/**
  	* Get a Vector of attributes.
   	*
  	* @return Vector of attributes
   	**/	
	public Vector<EPSAttribute> getAttributes() {
		return mAttributes;
	}
	
 	/**
  	* Get a Vector of axes.
   	*
  	* @return Vector of axes
   	**/	
	public Vector<Axis> getAxes() {
		return mAxes;
	}
	
 	/**
  	* Get an EPSVariable by it's ID number.
   	*
   	* @param varid variable code
   	*
  	* @return EPSVariable that matches ID. null ==> not found
   	**/	
	public EPSVariable getEPSVariable(int varid) {
		for (int i=0; i<mVariables.size(); i++) {
  			EPSVariable var = mVariables.elementAt(i);
			if (var.getID() == varid)
				return var;
		}
		return null;
	}
	
 	/**
  	* Get an EPSVariable by it's name.
   	*
   	* @param name variable name
   	*
  	* @return EPSVariable that matches ID. null ==> not found
   	**/	
	public EPSVariable getEPSVariable(String tstName) {
		for (int i=0; i<mVariables.size(); i++) {
  			EPSVariable var = mVariables.elementAt(i);
			if (var.getOname().equals(tstName))
				return var;
		}
		return null;
	}
	
 	/**
  	* Get an EPSVariable by it's ID number.
  	* @deprecated
  	* @see getEPSVariable
   	*
   	* @param varid variable code
   	*
  	* @return EPSVariable that matches ID. null ==> not found
   	**/	
	public EPSVariable getEPSVariableByID(int varid) {
		for (int i=0; i<mVariables.size(); i++) {
  			EPSVariable var = mVariables.elementAt(i);
			if (var.getID() == varid)
				return var;
		}
		return null;
	}
	
 	/**
  	* Get an EPSVariable by it's name.
  	* @deprecated
  	* @see getEPSVariable
   	*
   	* @param name variable name
   	*
  	* @return EPSVariable that matches ID. null ==> not found
   	**/	
	public EPSVariable getEPSVariableByName(String tstName) {
		for (int i=0; i<mVariables.size(); i++) {
  			EPSVariable var = mVariables.elementAt(i);
			if (var.getOname().equals(tstName))
				return var;
		}
		return null;
	}
	
 	/**
  	* Get an EPSVariable by it's name. Relax constraints on case and equality.
  	*
   	* @param name variable name
   	*
  	* @return EPSVariable matching name. null ==> not found
   	**/	
	public EPSVariable getEPSVariableByFuzzyName(String name) {
  		return getEPSVariableByFuzzyName(name, false);
	}
	
 	/**
  	* Get an EPSVariable by it's name. Relax constraints on case and equality.
  	*
   	* @param name variable name
   	* @param skipDims true if want to skip dimensions, false to consider matches
   	*        with dimension variables
   	*
  	* @return EPSVariable matching name. null ==> not found
   	**/	
	public EPSVariable getEPSVariableByFuzzyName(String name, boolean skipDims) {
		String lcName = name.toLowerCase();
		if (DEBUG)
			System.out.println("looking for " + lcName);
		for (int i=0; i<mVariables.size(); i++) {
			EPSVariable var = mVariables.elementAt(i);
			String lcVar = var.getOname().toLowerCase();
			if (DEBUG)
				System.out.println("considering " + lcVar);
			if (isDimensionVariable(var) && skipDims) {
				if (DEBUG)
					System.out.println("skipping dimension " + lcVar);
				continue;
			}
			if (var.getOname().equals(name))
				return var;
			else if (var.getOname().equalsIgnoreCase(name)) {
				if (var.getOname().toUpperCase().indexOf("QC") < 0)
					return var;
			}
			else {
				if (lcVar.startsWith(lcName)) {
					if (lcVar.indexOf("qc") < 0)
						return var;
				}
				else {
					if (lcVar.indexOf(lcName) >= 0) {
						if (lcVar.indexOf("qc") < 0)
							return var;
					}
				}
			}
		}
		return null;
	}
	
 	/**
  	* Get an Axis by it's name.
   	*
   	* @param name axis name
   	*
  	* @return Axis that matches name. null ==> not found
   	**/	
	public Axis getAxis(String name) {
  		if (name == null) 
  			return null;
  		if (DEBUG)
			System.out.println("Searching for axis = " + name);

		for (int i=0; i<mAxes.size(); i++) {
  			Axis ax = mAxes.elementAt(i);
  			if (DEBUG)
  				System.out.println("comparing to " + ax.getName().toLowerCase());
			if (ax.getName().toLowerCase().equalsIgnoreCase(name)) {
	  			if (DEBUG)
	  				System.out.println("******equals match to " + ax.getName());
				return ax;
			}
			else if (ax.getName().toLowerCase().indexOf(name) >= 0) {
	  			if (DEBUG)
	  				System.out.println("*******contains match to " + ax.getName());
				return ax;
			}
		}
		return null;
	}
	
 	/**
  	* Set an existing axis to value of the parameter axis.
   	*
   	* @param ax input axis
   	*
  	* @return new Axis
   	**/		
   	public Axis setAxis(Axis ax) {
  		Axis axold = getAxis(ax.getName());

  		if (axold == null) {
			 // axis is new
			mAxes.addElement(ax);
			return ax;
		} 
  		else {
		 	// axis already exists, clear old strings and copy pointers
		 	axold.setFrmt(ax.getFrmt());
		 	axold.setUnits(ax.getUnits());
		 	axold.setType(ax.getType());
			axold.setData(ax.getData());
			
			if (axold.getLen() != ax.getLen()){
				System.out.println("install_axis: can't change size of axis");
			}
			
			axold.setID(ax.getID());
			axold.setNewAxis(true);
			return axold;
		}
	}
	
 	/**
  	* Add an EPSAttribute to the dbase and fills with values.
   	*
   	* @param name attribute name
   	* @param type attribute type
   	* @param len attribute length
   	* @param value values (could be an array or a string)
   	*
  	* @return new EPSAttribute
   	**/	
	public EPSAttribute addEPSAttribute(String name, int type, int len, Object value) {
		// creates a new attribute (eps) and fills with values
		EPSAttribute at = new EPSAttribute();
		at.setNum(++this.mAttnum);
		at.setName(new String(name));
		at.setNewatt(true);
		at.setType(type);
		at.setLen(len);
		at.setValue(value);
		
		// install in dbase
		this.mAttributes.addElement(at);
		
		return at;
	}
	
 	/**
  	* Add an EPSVariable to the the dbase.
   	*
   	* @param var input EPSVariable
   	*
  	* @return new EPSVariable
   	**/	
	public EPSVariable addEPSVariable(EPSVariable var){
		if (var != null) {
			this.mVariables.addElement(var);
		}
		return var;
	}
	
 	/**
  	* Logical function that returns null if variable not found in dbase
	* str may be of the form -epic_code (eg -120 or eps120) or
	* generic name (eg u) 
   	*
   	* @param varid variable code
   	* @param name variable name
   	*
  	* @return EPSVariable that matches ID. null ==> not found
   	**/	
	public EPSVariable getEPSVariableByNameOrID(String name, int varid) {
		int id = 0;

		// is str of "-120" form?
		try {
			id = Integer.valueOf(name).intValue();   //will throw if name is not a number 
		}
		catch (NumberFormatException ex) {
			id = 0;
		}
		
		// is str of "eps" form??
		if (name.startsWith("eps")) 
			id = -1;
		
		if (id < 0) {
			// search for varid
			try {
				int len = name.length();
				if (name.startsWith("epsm"))
					id = -(Integer.valueOf(name.substring(4, len)).intValue());
				else if (name.startsWith("eps"))
					id = Integer.valueOf(name.substring(3, len)).intValue();
				else
					id = -id;
			}
			catch (NumberFormatException ex) {
				id = -id;
			}
			
			for (int i=0; i<mVariables.size(); i++) {
	  			EPSVariable var = mVariables.elementAt(i);
				if (var.getID() == varid)
					return var;
			}
  		} 
  		else {
			// search for generic name
			for (int i=0; i<mVariables.size(); i++) {
	  			EPSVariable var = mVariables.elementAt(i);
				if (var.getGname().equals(name))
					return var;
			}
		}
		return null;
	}
	
 	/**
  	* Determine whether EPSVariable is in the dbase.
   	*
   	* @param varid variable id
   	*
  	* @return true if variable is in dbase
  	*
  	* @exception EPSVarDoesNotExistExcept Variable not found in dbase.
   	**/	
	public boolean isEPSVariableInDbase(int varid) throws EPSVarDoesNotExistExcept {
		EPSVariable var = getEPSVariable(varid);
		if (var == null) {
			throw new EPSVarDoesNotExistExcept("inqvar: Variable not found (var code = (" + varid + ")");
		}
		return true;
	}
	
 	/**
  	* Get EPSVariable ID by it's name.
   	*
   	* @param vname variable name
   	*
  	* @return variable id if variable is in dbase
  	*
  	* @exception EPSVarDoesNotExistExcept Variable not found in dbase.
   	**/	
   	public int getEPSVariableID(String vname) throws EPSVarDoesNotExistExcept {
		// get variable id from the variable name (uses short name)
		for (int i=0; i<mVariables.size(); i++) {
  			EPSVariable var = mVariables.elementAt(i);
			if (var.getSname().equals(vname))
				return var.getID();
		}
		
		throw new EPSVarDoesNotExistExcept("varnmetoid: short variable name " + vname + " not found");
	}
	
 	/**
  	* Get an array of EPSVariable IDs in the dbase.
   	*
  	* @return array of int variable IDs
   	**/	
	public int[] getEPSVariableIDList() {
		// get list of variable ids that are in the dbase
		int[] retVals = new int[this.mVariables.size()];
		
		for (int i=0; i<mVariables.size(); i++) {
  			EPSVariable var = mVariables.elementAt(i);
			retVals[i] = var.getID();
		}
		
		return retVals;
	}
	
 	/**
  	* Add an an EPSVariable to the dbase initialized with varid found in the EPIC Key file.
   	*
   	* @param epickeyvarid variable id from EPIC key file
  	* @return empty EPSVariable
   	**/	
	public EPSVariable addEmptyEPSVariable(int epickeyvarid) {
		// create a variable structure and initialize
		
		Key key = null;
		try {
			if (keyDB == null) {
				String keyDir;
				if (EPSProperties.epicKeySubDir != null)
					keyDir = EPSProperties.epicKeySubDir + File.pathSeparator + "epic.key";
				else
					keyDir = "epic.key";
				keyDB = new EPIC_Key_DB(keyDir);
			}

			key = keyDB.findKey(epickeyvarid);
		}
		catch (Exception ex) {
		}
		
		if (key == null && epickeyvarid > 0) 
			return null;
		
		EPSVariable var = new EPSVariable();
		
		var.setID(epickeyvarid);
		if (key != null) {
			// copy epic key code information to variable
			var.setSname(key.getSname());
			var.setLname(key.getLname());
			var.setGname(key.getGname());
			var.setFrmt(key.getFrmt());
			var.setUnits(key.getUnits());
			var.setDtype(key.getType());
		}
		
		for (int i=0; i<4; i++) {
			var.setLci(i, 0);
			var.setUci(i, 0);
			var.setDimorder(i, -1);
		}
		
		var.setX(null);
		var.setY(null);
		var.setZ(null);
		var.setT(null);
		
		addEPSVariable(var);
		
		return var;
	}
	
 	/**
  	* Set geographical axis information (name, frmt, units and type) for varid and axis. \n
   	* Will try to create a new variable if varid not in database
   	*
   	* @param varid variable id in dbase
   	* @param axis axis number in variable (1=x, 2=y, 3=z, 4=t)
   	* @param axid axis id number
   	* @param name axis name
   	* @param frmt axis format
   	* @param units axis units
   	* @param type axis type
  	* @exception IOException Illegal operation when dbase in READ mode.
  	* @exception EPSVarDoesNotExistExcept Variable not found and couldn't be created in dbase.
  	* @exception AxisErrorException axis not found in variable.
   	**/	
	public void setEPSVariableGeogAxis(int varid, int axis, int axid, String name, String frmt,
						    String units, String type) throws IOException, EPSVarDoesNotExistExcept, 
						    AxisErrorException  {
		// set geographical axis information (name, frmt, units and type)
		if (this.mMode == EPREAD) {
			throw new IOException("setEPSVariableGeogAxis: illegal operation in read mode");
		}
		
		int iax = axis;
		EPSVariable var = getEPSVariable(varid);
		if (var == null) {
			// create variable entry
			if ((var = addEmptyEPSVariable(varid)) == null) {
				throw new EPSVarDoesNotExistExcept("setEPSVariableGeogAxis: Variable code " + 
					varid + " is not defined in epic.key file"); 
			} 
		} 

		// get an axis
		Axis ax = null;
		if (iax == 1)
			ax = var.getX();
		else if (iax == 2)
			ax = var.getY();
		else if (iax == 3)
			ax = var.getZ();
		else if (iax == 4)
			ax = var.getT();
	
		if (ax == null)
			ax = getAxis(name);
		else if (ax.isTimeAxis()) {
			throw new AxisErrorException("setEPSVariableGeogAxis: axis is a time axis (var code = " + varid + ")");
		}
	
		if (ax == null) {
			// create axis entry and install in dbase
			ax = new Axis();
			setAxis(ax);
		} 

		// make changes to axis structure
		ax.setNewAxis(true);
		ax.setTime(false);
		ax.setAxisType(EPSPACE);
		ax.setID(axid);
		
		ax.setName(name);
		ax.setFrmt(frmt);
		ax.setUnits(units);
		ax.setType(type);
	
		// replace axis structure in variable structure
		if (iax == 1)
			var.setX(ax);
		else if (iax == 2)
			var.setY(ax);
		else if (iax == 3)
			var.setZ(ax);
		else if (iax == 4)
			var.setT(ax);
	}
	
 	/**
  	* Set time axis information (name, frmt, units and type) for varid and axis. \n
   	* Will try to create a new variable if varid not in database
   	*
   	* @param varid variable id in dbase
   	* @param name axis name
   	* @param frmt axis format
   	* @param units axis units
   	* @param type axis type
  	* @exception IOException Illegal operation when dbase in READ mode.
  	* @exception EPSVarDoesNotExistExcept Variable not found and couldn't be created in dbase.
  	* @exception AxisErrorException axis not found in variable.
   	**/	
	public void setTimeAxis(int varid, String name, String frmt, String units, String type) 
		throws IOException, EPSVarDoesNotExistExcept, AxisErrorException {
		// store time axis information in axis structure		
		if (this.mMode == EPREAD) {
			throw new IOException("setTimeAxis: illegal operation in read mode");
		}
		
		EPSVariable var = getEPSVariable(varid);
		if (var == null) {
			// create variable entry
			if ((var = addEmptyEPSVariable(varid)) == null) {
				throw new EPSVarDoesNotExistExcept("setTimeAxis: Variable code " + varid + "is not defined in epic.key file"); 
			} 
		}
		
		Axis ax = var.getT();
		if (ax == null) 
			ax = getAxis(name);
		else if (!ax.isTimeAxis()) {
			throw new AxisErrorException("setTimeAxis: axis is not a time axis (var code = " + varid+ ")");
		}
		if (ax == null) {
			// create axis entry and install in dbase
			ax = new Axis();
			setAxis(ax);
		} 

		// make changes to axis structure
		ax.setNewAxis(true);
		ax.setTime(true);
		ax.setAxisType(EPTIME);
		ax.setID(624);		// epic system time
		
		ax.setName(name);
		ax.setFrmt(frmt);
		ax.setUnits(units);
		ax.setType(type);
	
		// replace axis structure in variable structure
		var.setT(ax);
	}
	
 	/**
  	* Set geographical axis coordinates (origin, lci, uci, and array) for varid and axis. \n
   	* Will try to create a new variable if varid not in database
   	*
   	* @param varid variable id in dbase
   	* @param axis axis number in variable (1=x, 2=y, 3=z, 4=t)
   	* @param origin origin
   	* @param lci coordinates
   	* @param uci coordinates
   	* @param array axis values
  	* @exception IOException Illegal operation when dbase in READ mode.
  	* @exception EPSVarDoesNotExistExcept Variable not found and couldn't be created in dbase.
  	* @exception AxisErrorException axis not found in variable.
   	**/	
	public void setEPSVariableGeogAxis(int varid, int axis, int[] origin, int[] lci, 
		int[] uci, float[] array) throws EPSVarDoesNotExistExcept, AxisErrorException, IOException {
		//load geographical axis coordinate information for varid
		if (this.mMode == EPREAD) {
			throw new IOException("setEPSVariableGeogAxis: illegal operation in read mode");
		}
		
		int iax = axis;
		EPSVariable var = getEPSVariable(varid);
		if (var == null) {
			throw new EPSVarDoesNotExistExcept("setEPSVariableGeogAxis: variable does not exist (var code = " + 
				varid + ")");
		}
		  
		// get axis
		Axis ax = null;
		if (iax == 1)
			ax = var.getX();
		else if (iax == 2)
			ax = var.getY();
		else if (iax == 3)
			ax = var.getZ();
		else if (iax == 4) 		// accept the axis if it is not a time axis
			ax = var.getT();
		
		if (ax == null) {
			throw new AxisErrorException("setEPSVariableGeogAxis: axis does not exist (var code = " + varid + ")");
		} 
		
		// make changes to length and reset flags
		int len = uci[iax-1] - lci[iax-1] + 1;
		float[] tempu = new float[len];
		ax.setLen(len);
		ax.setTime(false);
		ax.setAxisType(EPSPACE);
		ax.setNewAxis(true);
		
		for(int i=lci[iax-1]; i<=uci[iax-1]; i++) {
			tempu[i-lci[iax-1]] = array[(i-origin[iax-1])];
		}
		ArrayMultiArray ma = new ArrayMultiArray(tempu);
		ax.setData(ma);
		
		// replace axis structure
		if (iax == 1)
			var.setX(ax);
		else if (iax == 2)
			var.setY(ax);
		else if (iax == 3)
			var.setZ(ax);
		else if (iax == 4)
			var.setT(ax);
	}
	
 	/**
  	* Set time axis coordinates (origin, lci, uci, and array) for varid and axis. \n
   	* Will try to create a new variable if varid not in database
   	*
   	* @param varid variable id in dbase
   	* @param origin origin
   	* @param lci coordinates
   	* @param uci coordinates
   	* @param array time values (these are the double integer EPIC format times)
  	* @exception IOException Illegal operation when dbase in READ mode.
  	* @exception EPSVarDoesNotExistExcept Variable not found and couldn't be created in dbase.
  	* @exception AxisErrorException axis not found in variable.
   	**/	
	public void setEPSVariableTimeAxis(int varid, int[] origin, int[] lci, int[] uci, long[] array) 
		throws EPSVarDoesNotExistExcept, AxisErrorException, IOException {
		// store time axis coordinate information for varid
		if (this.mMode == EPREAD) {
			throw new IOException("setEPSVariableTimeAxis: illegal operation in read mode");
		}
		
		EPSVariable var = getEPSVariable(varid);
		if (var == null) {
			throw new EPSVarDoesNotExistExcept("setEPSVariableTimeAxis: variable does not exist (var code = " 
				+ varid + ")");
		}
		
		Axis ax = null;
		ax = var.getT();
		if (ax == null) {
			throw new AxisErrorException("setEPSVariableTimeAxis: axis does not exist");
		} 
		else if (ax.isTimeAxis()) {
			throw new AxisErrorException("puttaxis: axis is not a time axis");
		}
		
		// make changes to shape and reset flags
		int len = uci[3] - lci[3] + 1;
		int[] tempt = new int[len];
		ax.setLen(len);
		ax.setTime(true);
		ax.setAxisType(EPTIME);
		ax.setNewAxis(true);
		
		for (int i=lci[3]; i<=uci[3]; i++) {
			tempt[2*(i-lci[3])] = (int)array[2*(i-origin[3])];
			tempt[2*(i-lci[3])+1] = (int)array[2*(i-origin[3])+1];
		}
		GeoDate[] gda = new GeoDate[len];
		for (int i=0; i<len; i++)
			gda[i] = new GeoDate(tempt[i*2-1], tempt[i*2]);
		ax.setData(new ArrayMultiArray(gda));
	}
	
 	/**
  	* Get a geographical axis by varid and axis id.
  	*
   	* @param varid variable id in dbase
   	* @param axis axis number in variable (1=x, 2=y, 3=z, 4=t)
   	*
  	* @return Axis
  	* @exception EPSVarDoesNotExistExcept Variable not found.
  	* @exception AxisErrorException axis not found in variable.
  	* @exception AxisErrorException axis is a time axis.
   	**/	
	public Axis getGeogAxisByIDAndAxisNum(int varid, int axis) throws AxisErrorException, EPSVarDoesNotExistExcept {
		// get geographical axis information
		EPSVariable var = getEPSVariable(varid);
		if (var == null) {
			throw new EPSVarDoesNotExistExcept("getGeogAxisByIDAndAxisNum: variable does not exist (var code = " 
				+ varid + ")");
		}
		int iax = axis - 1;

		// get particular axis pointer
		Axis ax = null;
		if (iax == 0)
			ax = var.getX();
		else if (iax == 1)
			ax = var.getY();
		else if (iax == 2)
			ax = var.getZ();
		else if (iax == 3)   // accept 4th axis if it is not EPTIME axis 
			ax = var.getT();
		else 
			ax = null;
		
		if (ax == null) { 
			throw new AxisErrorException("getGeogAxisByIDAndAxisNum: requested axis does not exist (var code = " + varid + ")");
		} 
		else  if (ax.isTimeAxis()) {
			throw new AxisErrorException("getGeogAxisByIDAndAxisNum: requested axis is a time axis (var code = " + varid + ")");
		}
		return ax;
		
		// This spacing test may still have to be done in calling routine
		// I added a spacing convenience function below oz:01/06/00
		
		/*if ((strcmp(ax->type,EPEVEN) == 0 || strcmp(ax->type,EPEVEN_REAL) == 0 ||
		   strcmp(ax->type,EPEVEN_INT) == 0) && (ax->len > 1)){	// even spacing 
			*spcing = ax->u[1] - ax->u[0];
		} 
		else {
			*spcing = 0.0f;
		} */
	}
	
 	/**
  	* Get geographical axis spacing by varid and axis id.
  	*
   	* @param varid variable id in dbase
   	* @param axis axis number in variable (1=x, 2=y, 3=z, 4=t)
   	*
  	* @return axis spacing
  	* @exception AxisErrorException axis not found in variable.
   	**/	
	public double getAxisSpacing(int varid, int axis) throws AxisErrorException, EPSVarDoesNotExistExcept {
		// get particular axis
		Axis ax = null;
		try {
			ax = getGeogAxisByIDAndAxisNum(varid, axis);
		}
		catch (AxisErrorException e) {
			throw e;
		}
		catch (EPSVarDoesNotExistExcept ex) {
			throw ex;
		}
				
		if ((ax.getType().equals(EPEVEN) || ax.getType().equals(EPEVEN_REAL) ||
		   	ax.getType().equals(EPEVEN_INT)) && (ax.getLen() > 1)) {	
		   	// even spacing 
		   	MultiArray ma = ax.getData();
		   	double retVal = 0.0;
		   	try {
		   		double d1 = ma.getDouble(new int[] {1});
		   		double d0 = ma.getDouble(new int[] {0});
		   		retVal = d1 - d0;
		   	}
		   	catch (Exception ex) {
		   		throw new AxisErrorException("getAxisSpacing error getting double out of multiarray");
		   	}
			return (retVal);//= ax->u[1] - ax->u[0];
		} 
		else {
			return 0.0;
		}
	}
	
 	/**
  	* Get a time axis by varid and axis id.
  	*
   	* @param varid variable id in dbase
   	* @param axis axis number in variable (1=x, 2=y, 3=z, 4=t)
   	*
  	* @return Axis
  	* @exception EPSVarDoesNotExistExcept Variable not found.
  	* @exception AxisErrorException axis not found in variable.
   	**/	
	public Axis getTimeAxisFromIDAndAxisNum(int varid, int axis) throws EPSVarDoesNotExistExcept, AxisErrorException {
		// get geographical axis information
		EPSVariable var = getEPSVariable(varid);
		if (var == null) {
			throw new EPSVarDoesNotExistExcept("getTimeAxisFromIDAndAxisNum: variable not found (var code = " + varid + ")");
		}
		// get particular axis pointer
		Axis ax = null;
		ax = var.getT();
		
		if (ax == null &&  
     		!(ax.getAxisType() == EPTIME || ax.getAxisType() == EPTAXIS || ax.getAxisType() == EPTDUMMY)) {
		    throw new AxisErrorException("getTimeAxisFromIDAndAxisNum: requested axis is not a time axis (var code = " + varid + ")");
  		} 
		
		return ax;
	}
	
 	/**
  	* Get time axis spacing by varid and axis id.
  	*
   	* @param varid variable id in dbase
   	* @param axis axis number in variable (1=x, 2=y, 3=z, 4=t)
   	*
  	* @return time axis spacing as a GeoDate
  	* @exception AxisErrorException axis not found in variable.
   	**/	
	public GeoDate getTimeAxisSpacing(int varid, int axis) throws AxisErrorException, EPSVarDoesNotExistExcept {
		// get particular axis
		Axis ax = null;
		try {
			ax = getTimeAxisFromIDAndAxisNum(varid, axis);
		}
		catch (AxisErrorException ex) {
			throw ex;
		}
				
		if ((ax.getType().equals(EPEVEN) || ax.getType().equals(EPEVEN_REAL) ||
		   	ax.getType().equals(EPEVEN_INT)) && (ax.getLen() > 1)) {	
		   	// even spacing 
		   	MultiArray tma = ax.getData();
		   	GeoDate gd1 = null;
		   	GeoDate gd0 = null;
		   	try {
		   		gd1 = (GeoDate)tma.get(new int[] {1});
		   		gd0 = (GeoDate)tma.get(new int[] {0});
				return new GeoDate(gd1.subtract(gd0)); 
		   	}
		   	catch (Exception ex) {
		   		return new GeoDate(0, 0);
		   	}
		} 
		else {
			return new GeoDate(0, 0); 
		}
	}
	
 	/**
  	* Get geog axis shape and coordinates by varid and axis id.
  	*
   	* @param varid variable id in dbase
   	* @param axis axis number in variable (1=x, 2=y, 3=z, 4=t)
   	* @param lci coordinates to check range of request against valid range
   	* @param uci coordinates to check range of request against valid range
   	* @param dim axis dimension
   	*
  	* @return axis shape array
  	* @exception EPSVarDoesNotExistExcept Variable not found.
  	* @exception AxisErrorException axis not found in variable.
   	**/	
	public double[] getAxisShapeByIDAndCoord(int varid, int axis, int[] lci, int[] uci, int dim) 
		throws EPSVarDoesNotExistExcept, AxisErrorException {
		double[] array = null;
		// get axis shape and coordinates from dbase (for a given
		// variable code and geographical coordinate)
		EPSVariable var = getEPSVariable(varid);
		if (var == null) {
			throw new EPSVarDoesNotExistExcept("getAxisShapeByIDAndCoord: variable not found (var code = " + varid + ")");
		}
		int iax = axis - 1;
		
		// get particular axis pointer
		Axis ax = null;
		if (iax == 0)
			ax = var.getX();
		else if (iax == 1)
			ax = var.getY();
		else if (iax == 2)
			ax = var.getZ();
		else if (iax == 3)   // accept 4th axis if it is not EPTIME axis 
			ax = var.getT();
		else 
			ax = null;
			
		if (ax == null) {
			throw new AxisErrorException("getAxisShapeByIDAndCoord: axis does not exist");
		} 
		
		if (ax == null || ax.getData() == null) { 
			// axis does not exist 
			throw new AxisErrorException("getAxisShapeByIDAndCoord: requested axis is not a time axis (var code = " + varid + ")");
		}
		
		// check range of request against valid range
		if ((lci[iax] < var.getLci(iax)) || (lci[iax] > var.getUci(iax)) ||
			(uci[iax] > var.getUci(iax)) || (uci[iax] < var.getLci(iax))) {
			throw new AxisErrorException("getAxisShapeByIDAndCoord: range outside available data");
		}

		//  check that array dimensions are great enough
		if ((uci[iax] - lci[iax] + 1) > dim) {
			throw new AxisErrorException("getAxisShapeByIDAndCoord: request exceeds array storage (var code = " + varid + ")");
		}

		// get a multiarray
		MultiArray ma = ax.getData();
		
		// dimension array
		array = new double[ma.getRank()];
		
		// copy axis data
		try {
			for(int i=lci[iax]; i<=uci[iax]; i++) 
				array[i-lci[iax]] = ma.getFloat(new int[] {i-1});
		}
		catch (Exception ex) {
			throw new AxisErrorException("threw copying axis into array");
		}
			
		return array;
	}
	
 	/**
  	* Get time axis shape and coordinates by varid and axis id.
  	*
   	* @param varid variable id in dbase
   	* @param lci coordinates to check range of request against valid range
   	* @param uci coordinates to check range of request against valid range
   	* @param dim axis dimension to check against valid dimension
   	*
  	* @return GeoDate array
  	* @exception EPSVarDoesNotExistExcept Variable not found.
  	* @exception AxisErrorException axis not found in variable.
   	**/	
	public GeoDate[] getTAxis(int varid, int[] lci, int[] uci, int dim) throws EPSVarDoesNotExistExcept, 
							  AxisErrorException {
		GeoDate[] array = null;
		// get axis shape and coordinates from dbase (for a given
		// variable code and geographical coordinate)
		
		EPSVariable var = getEPSVariable(varid);
		if (var == null) {
			throw new EPSVarDoesNotExistExcept("inqtaxis: variable not found (var code = " + varid + ")");
		}
		int iax = 3;
		
		// get particular axis pointer
		Axis ax = null;
		ax = var.getT();
			
		if (ax == null) {
			throw new AxisErrorException("puttaxis: axis does not exist");
		} 
		
		if (ax == null) { 
			// axis does not exist 
			throw new AxisErrorException("gettaxis: requested axis is not a time axis (var code = " + varid + ")");
		} 
		else if (!(ax.getAxisType() == EPTIME || ax.getAxisType() == EPTDUMMY)) {
			throw new AxisErrorException("gettaxis: axis is not a time axis");
		}
		
		// check range of request against valid range
		if ((lci[iax] < var.getLci(iax)) || (lci[iax] > var.getUci(iax)) ||
			(uci[iax] > var.getUci(iax)) || (uci[iax] < var.getLci(iax))) {
			throw new AxisErrorException("gettaxis: range outside available data");
		}

		//  check that array dimensions are great enough
		if ((uci[iax] - lci[iax] + 1) > dim) {
			throw new AxisErrorException("gettaxis: request exceeds array storage (var code = " + varid + ")");
		}

		// get a multiarray
		MultiArray tma = ax.getData();
		
		// dimension array
		array = new GeoDate[tma.getLengths()[0]];
		
		// copy axis data
		for (int i=lci[iax]; i<=uci[iax]; i++) 
			try {
				array[i-lci[iax]] = new GeoDate((GeoDate)tma.get(new int[] {i-1}));
			}
			catch (Exception ex) {}
			
		return array;
	}
	
 	/**
  	* Copy the axes and shape information from this dbase and variable to another dbase variable.
  	*
   	* @param dbout destination dbase
   	* @param varin variable id in this dbase
   	* @param varout variable id in destination dbase
   	*
  	* @exception EPSVarDoesNotExistExcept Variable not found in source or destination dbase.
  	* @exception IOException axis out dbase is not in write mode.
   	**/	
	public void copyGrid(Dbase dbout, int varin, int varout) throws EPSVarDoesNotExistExcept, 
		IOException {
		if (dbout.getMode() == EPREAD) {
			throw new IOException("copyGrid: illegal to copy to read mode file");
		}

		// find input variable
		EPSVariable var1 = this.getEPSVariable(varin);
		if (var1 == null) {
			throw new EPSVarDoesNotExistExcept("copyGrid: input variable not found");
		}
		
		// find output variable 
		EPSVariable var2 = dbout.getEPSVariable(varout);
		if (var2 == null) {
			// doesn't exist so create variable entry
			if ((var2 = dbout.addEmptyEPSVariable(varout)) == null) {
				throw new EPSVarDoesNotExistExcept("copygrid: Variable code " + varout + "is not defined in epic.key file"); 
			} 
		}
		
		// copy axis data 
		Axis axin = null;
		for (int i=0; i<4; i++) {
			Axis axout = null;
			if (i == 0)
				axin = var1.getX();
			else if (i == 1)
				axin = var1.getY();
			else if (i == 2)
				axin = var1.getZ();
			else
				axin = var1.getT();
			
			// copy axin to axout using the copy constructor
			axout = new Axis(axin);
			axout.setNewAxis(true);
			
			// install axis is other database
		    axout = dbout.setAxis(axout);
		    
		    if (i == 0) 
		      var2.setX(axout);
		    else if (i == 1)
		      var2.setY(axout);
		    else if (i == 2) 
		      var2.setZ(axout);
		    else
		      var2.setT(axout);
		}
		
		// set the unlimited dimension
		dbout.setUnlimited(this.getUnlimited());
	}
	
 	/**
  	* Check to see if varid1 and varid2 have the same grid structure if match(coord) = 1.
  	*
   	* @param varid1 variable id of first variable
   	* @param varid2 variable id of variable to be compared to
   	* @param match array of booleans to specify which of the axes to compare
   	*
  	* @exception EPSVarDoesNotExistExcept One of the input variables not found dbase.
   	**/	
	public boolean isSameGrid(int varid1, int varid2, boolean[] match) throws EPSVarDoesNotExistExcept {
		EPSVariable var1 = getEPSVariable(varid1);
		if (var1 == null) {
			throw new EPSVarDoesNotExistExcept("isSameGrid: first variable not found (var code = " + varid1 + ")");
		}
		
		EPSVariable var2 = getEPSVariable(varid2);
		if (var1 == null) {
			throw new EPSVarDoesNotExistExcept("isSameGrid: second variable not found (var code = " + varid2 + ")");
		}
		
		// get axes
		Axis ax1 = null;
		Axis ax2 = null;
		for (int i=0; i<4; i++) {
			if (!match[i]) 
				continue;
			
			if (i == 0) {
				ax1 = var1.getX();
				ax2 = var2.getX();
			} 
			else if (i == 1) {
				ax1 = var1.getY();
				ax2 = var2.getY();
			} 
			else if (i == 2) {
				ax1 = var1.getZ();
				ax2 = var2.getZ();
			}
			
			if (i != 3) {
				// compare two spatial axes
				if (ax1.getLen() != ax2.getLen()) {
					return false;
				}
				// array optimization
				for(int j=0; j<ax1.getLen(); j++) {
					try {
						double val1 = ax1.getData().getDouble(new int[] {j});
						double val2 = ax2.getData().getDouble(new int[] {j});
						if (val1 != val2) {
							return false;
						}
					}
					catch (Exception ex) {
						return false;
					}
				}
			} 
			else {
				// compare two temporal axes
				ax1 = var1.getT();
				ax2 = var2.getT();
				if (ax1.getLen() != ax2.getLen()) {
					return false;
				}
				GeoDate gda1 = null;
				GeoDate gda2 = null;
				MultiArray tma1 = ax1.getData();
				MultiArray tma2 = ax2.getData();
				// array optimization
				for (int j=0; j<ax1.getLen(); j++) {
					try {
						gda1 = (GeoDate)tma1.get(new int[] {j});
						gda2 = (GeoDate)tma2.get(new int[] {j});
						if (!gda1.equals(gda2))
							return false;
					}
					catch (Exception ex) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
 	/**
  	* Get an dbase (global) attribute by name.
  	*
   	* @param name attribute name
   	*
  	* @return EPSAttribute matching name. null ==> not found
   	**/	
	public EPSAttribute getAttribute(String name) {
  		for (int i=0; i<mAttributes.size(); i++) {
  			EPSAttribute at = mAttributes.elementAt(i);
			if (at.getName().equals(name))
				return at;
     	}
  		return null;
	}
	
 	/**
  	* Get an dbase (global) attribute by name.
  	* @deprecated
  	* @see getAttribute
  	*
   	* @param name attribute name
   	*
  	* @return EPSAttribute matching name. null ==> not found
   	**/	
	public EPSAttribute getAttributeByName(String name) {
  		for (int i=0; i<mAttributes.size(); i++) {
  			EPSAttribute at = mAttributes.elementAt(i);
			if (at.getName().equals(name))
				return at;
     	}
  		return null;
	}
	
 	/**
  	* Get an dbase (global) attribute by name. Relax constraints on case and equality.
  	*
   	* @param name attribute name
   	*
  	* @return EPSAttribute matching name. null ==> not found
   	**/	
	public EPSAttribute getAttributeByFuzzyName(String name) {
  		for (int i=0; i<mAttributes.size(); i++) {
  			EPSAttribute at = mAttributes.elementAt(i);
			if (at.getName().equals(name))
				return at;
			else if (at.getName().equalsIgnoreCase(name))
				return at;
			else {
				String lcAtt= at.getName().toLowerCase();
				String lcName = name.toLowerCase();
				if (lcAtt.indexOf(lcName) >= 0)
					return at;
			}
     	}
  		return null;
	}
	
 	/**
  	* Adds a programmer comment to the global attributes of this dbase. \n
  	* New comment is added at end of current comments. Only 4 programmer comments are allowed.
  	*
   	* @param cmnt comment to add
   	**/	
	public void setProgComment(String cmnt) {
		EPSAttribute[] at_stack = new EPSAttribute[4];
		String[] atname = new String[4];
		
		int len = cmnt.length();
		if (len > 40) {
			len = 40;
			cmnt = cmnt.substring(0, 39);
		}
			
		atname[0] = new String(EPPROGCMNT1);
		atname[1] = new String(EPPROGCMNT2);
		atname[2] = new String(EPPROGCMNT3);
		atname[3] = new String(EPPROGCMNT4);
		
		for(int i=0; i<4; i++) 
			at_stack[i] = getAttribute(atname[i]);
		
		int bottom = 0;
		while (at_stack[bottom] != null  && bottom < 4)  
			bottom++;
		
		if (bottom == 0)
			addEPSAttribute(atname[0], EPCHAR, len, cmnt); 
		else if (bottom == 4)  
			at_stack[3].setValue(null); 
		else
			addEPSAttribute(atname[bottom], EPCHAR, at_stack[bottom-1].getLen(), at_stack[bottom-1].getValue()); 
		
		for (int i=bottom-1; i>=0; i--) { 
			if (i == 0) {
				at_stack[i].setLen(len);
				at_stack[i].setValue(cmnt);
			}
			else {
				at_stack[i].setLen(at_stack[i-1].getLen());
				at_stack[i].setValue(at_stack[i-1].getValue());
			}
			at_stack[i].setNewatt(true);
		}
	}
	
	/**
  	* Adds a data comment to the global attributes of this dbase. 
  	*
   	* @param Data cmnt comment to add
   	**/	
	public void setDataComment(String cmnt) {
		addEPSAttribute(EPDATACMNT, EPCHAR, cmnt.length(), cmnt); 
	}
	
 	/**
  	* Returns a string array of the current data and programmer's comments. \n
  	* dcmnt=comments[0], prog1=comments[1], prog2=comments[2], prog3=comments[3], prog4=comments[4]
  	*
   	* @return array of strings 
   	**/	
	public String[] getComments() {
		String[] comments = new String[5];
		boolean gotComment = false;
		
		EPSAttribute at = null;
		if ((at = getAttribute(EPDATACMNT)) == null)
			comments[0] = null;
		else {
			comments[0] = new String((String)at.getValue());
			gotComment = true;
		}
		
		if ((at = getAttribute(EPPROGCMNT1)) == null)
			comments[1] = null;
		else {
			comments[1] = new String((String)at.getValue());
			gotComment = true;
		}
		
		if ((at = getAttribute(EPPROGCMNT2)) == null)
			comments[2] = null;
		else {
			comments[2] = new String((String)at.getValue());
			gotComment = true;
		}
		
		if ((at = getAttribute(EPPROGCMNT3)) == null)
			comments[3] = null;
		else {
			comments[3] = new String((String)at.getValue());
			gotComment = true;
		}
		
		if ((at = getAttribute(EPPROGCMNT4)) == null)
			comments[4] = null;
		else {
			comments[4] = new String((String)at.getValue());
			gotComment = true;
		}
		
		if (!gotComment) {
			// try  the WOCE equivalent
			if ((at = getAttribute("ORIGINAL_HEADER")) != null)
				comments[0] = new String((String)at.getValue());
		}
			
		return comments;
	}
	
 	/**
  	* Convenience function to return the value of a global attribute as Number.
  	*
   	* @param name attribute name
   	*
  	* @return object value of attribute. Returns Integer(0) if attribute note found
   	**/
	public Object getAttributeValue(String name) {
		// get integer value from attribute dbase, if not found return 0
		EPSAttribute at = null;
		if ((at = getAttribute(name)) == null) {
			return new Integer(0);
		}
		else {
			return at.getValue();
		}
	}
	
 	/**
  	* Convenience function to return the value of a global attribute as an integer.
  	*
   	* @param name attribute name
   	*
  	* @return integer value of attribute. Returns 0 if attribute note found
   	**/
	public int getIntegerAttributeValue(String name) {
		// get integer value from attribute dbase, if not found return 0
		EPSAttribute at = null;
		if ((at = getAttribute(name)) == null) {
			return 0;
		}
		else {
			try {
				return ((int[])at.getValue())[0];
			}
			catch (Exception ex) {
				try {
					return (int)(((short[])at.getValue())[0]);
				}
				catch (Exception eex) {
					return 0;
				}
			}
		}
	}
	
 	/**
  	* Convenience function to return the value of a global attribute as an float.
  	*
   	* @param name attribute name
   	*
  	* @return floating point value of attribute. Returns 0 if attribute note found
   	**/
	public float getRealAttributeValue(String name) {
		// get real value from attribute dbase, if not found return 0.0
		EPSAttribute at = null;
		if ((at = getAttribute(name)) == null) {
			return (float)0.0;
		}
		else {
			try {
				return ((float[])at.getValue())[0];
			}
			catch (Exception ex) {
				try {
					return (float)(((double[])at.getValue())[0]);
				}
				catch (Exception eex) {
					return 0;
				}
			}
    	}
	}
	
 	/**
  	* Convenience function to return a set of global attributes as a HydroAttributes object.
  	*
  	* @see HydroAttributes
  	*
  	* @return attributes packaged as HydroAttributes
   	**/	
	public HydroAttributes getHydroAttributes() {
		String cruise = "na";
		String cast = "na";
		String inst = "na";
		String crdate = "na";
		int bottle = 0;
		int castnum = 0;
		
		EPSAttribute at = null;
		if ((at = getAttribute(EPCRUISE)) != null)
			cruise = new String((String)at.getValue());
		else if ((at = getAttribute("WOCE_ID")) != null)
			cruise  = new String((String)at.getValue());
			
		if ((at = getAttribute(EPCAST)) != null)
			cast = new String((String)at.getValue());
		else if ((at = getAttribute("STATION_NUMBER")) != null)
			cast = new String((String)at.getValue());
			
		if ((at = getAttribute(EPINSTTYPE)) != null)
			inst = new String((String)at.getValue());
		else if ((at = getAttribute("DATA_TYPE")) != null)
			inst = new String((String)at.getValue());
			
		if ((at = getAttribute(EPCRDATE)) != null)
			crdate = new String((String)at.getValue());
		else if ((at = getAttribute("Creation_Time")) != null)
			crdate = new String((String)at.getValue());
			
		bottle = getIntegerAttributeValue(EPBOTTLE);
		
		if ((at = getAttribute(EPCASTNUM)) != null)
			castnum = getIntegerAttributeValue(EPCASTNUM);
			
		at = getAttribute("CAST_NUMBER");
		
		if (castnum == 0 && at != null) {
			try {
				String temp = (String)at.getValue();
				castnum = Integer.valueOf(temp.trim()).intValue();
			}
			catch (Exception ex) {}
		}
			
		if (castnum == 0 && (at = getAttribute("CAST")) != null) {
			try {
				String temp = new String((String)at.getValue());
				castnum = Integer.valueOf(temp.trim()).intValue();
			}
			catch (Exception ex) {}
		}
		
		return new HydroAttributes(cruise, cast, inst, crdate, bottle, castnum);
	}
	
 	/**
  	* Convenience function to return a set of global attributes as a TimeSeriesAttributes object.
  	*
  	* @see TimeSeriesAttributes
  	*
  	* @return attributes packaged as TimeSeriesAttributes
   	**/
	public TimeSeriesAttributes getTimeSeriesAttributes() {
	     String expr;
	     String proj;
	     String moor;
	     String delt;
	     String inst;
	     String crdate;
		
		EPSAttribute at = null;
		if ((at = getAttribute(EPEXPERIMENT)) == null)
			expr = null;
		else
			expr = new String((String)at.getValue());
			
		if ((at = getAttribute(EPPROJECT)) == null)
			proj = null;
		else
			proj = new String((String)at.getValue());
			
		if ((at = getAttribute(EPMOORING)) == null)
			moor = null;
		else
			moor = new String((String)at.getValue());
			
		if ((at = getAttribute(EPDELTAT)) == null)
			delt = null;
		else
			delt = new String((String)at.getValue());
			
		if ((at = getAttribute(EPINSTTYPE)) == null)
			inst = null;
		else
			inst = new String((String)at.getValue());
			
		if ((at = getAttribute(EPCRDATE)) == null)
			crdate = null;
		else
			crdate = new String((String)at.getValue());
	
	    return new TimeSeriesAttributes(expr, proj, moor, delt, inst, crdate);
	}

 	/**
  	* Convenience function to return a set of global attributes as a CompositeTSInfo object.
  	*
  	* @see CompositeTSInfo
  	*
  	* @return attributes packaged as CompositeTSInfo
   	**/
	public CompositeTSInfo getCompositeTSData() {
		int number = getIntegerAttributeValue(EPCOMPOSITE);
		int position = getIntegerAttributeValue(EPPOSCONST);
		int depth = getIntegerAttributeValue(EPDEPTHCONST);
		
		return new CompositeTSInfo(number, position, depth);
	}

 	/**
  	* Convenience function to return a set of global attributes as a CompositeData object.
  	*
  	* @see CompositeData
  	*
  	* @return attributes packaged as CompositeData
   	**/
	public CompositeData getCompositeData(int num) {
		EPSAttribute at = null;
		String start;
		String end;
		String cmnt;
		
		String frmt = new String("{0}{1,number}"); 
		MessageFormat msgf = new MessageFormat(frmt);
		
		//sprintf(str,"%s%d\0", EPLATPC, i); 
		Object[] objs = {new String(EPLATPC), new Integer(num)};
		StringBuffer out = new StringBuffer();
		msgf.format(objs, out, null);
		String str = new String(out);
		float lat = getRealAttributeValue(new String(str));
		
		//sprintf(str,"%s%d\0", EPLONGPC, i);
		objs[0] = new String(EPLONGPC);
		objs[1] = new Integer(num);
		out = new StringBuffer();
		msgf.format(objs, out, null);
		str = new String(out);
		float lon = getRealAttributeValue(str);
		
		//sprintf(str,"%s%d\0", EPSTARTPC, i);
		objs[0] = new String(EPSTARTPC);
		objs[1] = new Integer(num);
		out = new StringBuffer();
		msgf.format(objs, out, null);
		str = new String(out);
		if ((at = getAttribute(str)) == null)
			start = null;
		else
			start = (String)at.getValue();
		
		//sprintf(str,"%s%d\0", EPENDPC, i);
		objs[0] = new String(EPENDPC);
		objs[1] = new Integer(num);
		out = new StringBuffer();
		msgf.format(objs, out, null);
		str = new String(out);
		if ((at = getAttribute(str)) == null)
			end = null;
		else
			end = (String)at.getValue();
		
		//sprintf(str,"%s%d\0", EPINSTDEPTHPC, i);
		objs[0] = new String(EPINSTDEPTHPC);
		objs[1] = new Integer(num);
		out = new StringBuffer();
		msgf.format(objs, out, null);
		str = new String(out);
		float depth = getRealAttributeValue(str);
		
		//sprintf(str,"%s%d\0", EPIDENTPC, i);
		objs[0] = new String(EPIDENTPC);
		objs[1] = new Integer(num);
		out = new StringBuffer();
		msgf.format(objs, out, null);
		str = new String(out);
		if ((at = getAttribute(str)) == null)
			cmnt = null;
		else
			cmnt = (String)at.getValue();
		return new CompositeData(lat, lon, start, end, depth, cmnt);
	}
	
 	/**
  	* Set the unlimited variable for this dbase.
  	*
   	* @param name name of unlimited Variable
   	*
  	* @exception IOException dbase not in write mode.
   	**/	
	public void setUnlimitedDimension(String name) throws IOException {
		if (this.getMode() == EPCREATE) 
			this.setUnlimited(name);
		else {
			throw new IOException("setUnlimitedDimension: file is not for write");
		}
	}
	
 	/**
  	* Return the name of the unlimited dimension.
  	*
  	* @return unlimited dimension
  	*
   	**/	
	public String getUnlimitedDimension() {
		return this.getUnlimited();
	}  
	
 	/**
  	* Is variable a dimension variable.
  	*
   	* @return true if dimension variable
   	**/	
   	
   public boolean isDimensionVariable(EPSVariable epsVar) {			
		// get name
		String name = epsVar.getOname();
		
		// get the number of dimensions and distinguish between a dimension variable and a measured variable
		boolean isAxis = false;
		for (int n=0; n<mAxes.size(); n++) {
			Axis ax = mAxes.elementAt(n);
			
			if (ax.getName().equals(name)) {
				// this is a dimension variable 
				isAxis = true;
				break;
			}
		}
		return isAxis;
   }
	
 	/**
  	* Get an array of non-axis variables in this dbase.
  	*
   	* @return Array of variables name
   	**/	
	public Vector<EPSVariable> getStnVariables() {
		Vector<EPSVariable> varCollector = new Vector<EPSVariable>(50);
		for (int i=0; i<this.mVariables.size(); i++) {
			EPSVariable epsVar = this.mVariables.elementAt(i);
			String st = epsVar.getOname().toUpperCase();
			if (epsVar.getDtype() == EPCHAR || (st.indexOf("DATE") >= 0) || (st.indexOf("TIME") >= 0))
				continue;			

			if (!isDimensionVariable(epsVar)) {
				if (epsVar.getZ() != null && epsVar.getZ().getLen() == 1) {
					varCollector.addElement(epsVar);
				}
			}
		}
		return varCollector;
	}
 	/**
	* Get an array of non-axis variables in this dbase.
	*
 	* @return Array of variables name
 	**/	
public Vector<EPSVariable> getMeasuredVariables(boolean excludeStnVars) {
	Vector<EPSVariable> varCollector = new Vector<EPSVariable>(50);
	for (int i=0; i<this.mVariables.size(); i++) {
		EPSVariable epsVar = this.mVariables.elementAt(i);
		String st = epsVar.getOname().toUpperCase();
		if (epsVar.getDtype() == EPCHAR || (st.indexOf("DATE") >= 0) || (st.indexOf("TIME") >= 0))
			continue;			

		if (!isDimensionVariable(epsVar)) {
			if (excludeStnVars && epsVar.getZ() != null && epsVar.getZ().getLen() == 1) {
				// test the sign of the data--surface parameters have to have a negative value
				MultiArray zma = epsVar.getZ().getData();
				double[] zarray = null;
				try {
					zarray = EPS_Util.get1DDoubleArray(zma, EPS_Util.getMeasuredDim(zma));
					if (zarray[0] >= 0) {
						varCollector.addElement(epsVar);
					}
				}
				catch (Exception ex) {
					// silent
				}
				continue;
			}
			else {
				varCollector.addElement(epsVar);
			}
		}
	}
	return varCollector;
}
	
 	/**
  	* Get the number of non-axis variables in this dbase.
  	*
   	* @return Number of non axis variables
   	**/	
	public int getNumMeasuredVariables(boolean excludeStnVars) {
		int numVars = 0;
		for (int i=0; i<this.mVariables.size(); i++) {
			EPSVariable epsVar = this.mVariables.elementAt(i);
			
			// skip character variables
			String st = epsVar.getOname().toUpperCase();
			if (epsVar.getDtype() == EPCHAR || (st.indexOf("DATE") >= 0) || (st.indexOf("TIME") >= 0))
				continue;	
		
			// get name
			String name = epsVar.getOname();
			
			// get the number of dimensions and distinguish between a dimension variable and a measured variable
			boolean isAxis = false;
			for (int n=0; n<mAxes.size(); n++) {
				Axis ax = mAxes.elementAt(n);
				if (ax.getName().equalsIgnoreCase(name)) {
					// this is a dimension variable 
					isAxis = true;
					break;
				}
			}
			
			if (!isAxis) {
				if (excludeStnVars && epsVar.getZ() != null && epsVar.getZ().getLen() == 1) {
					continue;
				}
				else {
					numVars++;
				}
			}
		}
		return numVars;
	}
	
 	/**
  	* Get the number of non-axis variables in this dbase.
  	*
   	* @return Number of non axis variables
   	**/	
	public int getNumStnVariables() {
		int numStnVars = 0;
		for (int i=0; i<this.mVariables.size(); i++) {
			EPSVariable epsVar = this.mVariables.elementAt(i);
			
			// skip character variables
			String st = epsVar.getOname().toUpperCase();
			if (epsVar.getDtype() == EPCHAR || (st.indexOf("DATE") >= 0) || (st.indexOf("TIME") >= 0))
				continue;	
		
			// get name
			String name = epsVar.getOname();
			
			// get the number of dimensions and distinguish between a dimension variable and a measured variable
			boolean isAxis = false;
			for (int n=0; n<mAxes.size(); n++) {
				Axis ax = mAxes.elementAt(n);
				if (ax.mName.equals(name)) {
					// this is a dimension variable 
					isAxis = true;
					break;
				}
			}
			
			if (!isAxis && epsVar.getZ() != null && epsVar.getZ().getLen() == 1) {
				numStnVars++;
			}
			
		}
		return numStnVars;
	}
	
 	/**
  	* Write dbase as a new netCDF file.
  	*
   	* @param filename name of new netCDF file
   	*
  	* @exception IOException An error occurred writing to the new file.
  	* @exception EPSVariableException Couldn't create a ProtoVariable from an EPS Variable.
   	**/	
	@SuppressWarnings("unchecked")
  public void writeNetCDF(File outFile) throws IOException, EPSVariableException {
		// write dbase to a new file
		
		// build the schema (protovariable array and global attributes)
		
		// get the global attributes
		int numAtts = this.mAttributes.size();
		Attribute[] globalAtts = new Attribute[numAtts];
		for (int a=0; a<numAtts; a++) {
			EPSAttribute epAtt = this.mAttributes.elementAt(a);
			globalAtts[a] = epAtt.getNCAttribute();
		}
		// build the dimensions
		int numDims = 4;
		if (mAxes.size() < 4)
			numDims = mAxes.size();
		Dimension[] ds = null;
		
		// dimension the protovariable array
		ProtoVariable[] pvs = new ProtoVariable[this.mVariables.size()];
		
		// loop through the EPSVariables and construct protovariables
		for (int i=0; i<this.mVariables.size(); i++) {
			EPSVariable epsVar = this.mVariables.elementAt(i);
			
			// name
			String name = epsVar.getOname();
			
			// get the number of dimensions and distinguish between a dimension variable and a measured variable
			boolean isAxis = false;
			for (int n=0; n<mAxes.size(); n++) {
				Axis ax = mAxes.elementAt(n);
				if (ax.getName().equals(name)) {
					// this is a dimension variable 
					ds = null;
					ds = new Dimension[1];
					
					if (ax.isUnlimited()) {
						UnlimitedDimension ud = new UnlimitedDimension(ax.getName());
						ds[0] = ud;
					}
					else {
						ds[0] = new Dimension(ax.getName(), ax.getLen());
					}
					isAxis = true;
					break;
				}
			}
			
			if (!isAxis) {
				// have to match up this mVariables axes with the dimensions
				// and get them in the right order
				ds = null;
				ds = new Dimension[numDims];
				int ac = 0;
				for (int nn=0; nn<numDims; nn++) {
					Axis ax = null;//(Axis)axes.elementAt(nn);
					int dimorder = epsVar.getDimorder(nn);
					switch (dimorder) {
						case 0: // time axis
								ax = epsVar.getT();
								if (ax != null && ax.isUnlimited())
									ds[ac++] = new UnlimitedDimension(ax.getName());
								else if (ax != null)
									ds[ac++] = new Dimension(ax.getName(), ax.getLen());
								break;
						case 1: // depth axis
								ax = epsVar.getZ();
								if (ax != null && ax.isUnlimited())
									ds[ac++] = new UnlimitedDimension(ax.getName());
								else if (ax != null)
									ds[ac++] = new Dimension(ax.getName(), ax.getLen());
								break;
						case 2: // lat axis
								ax = epsVar.getY();
								if (ax != null && ax.isUnlimited())
									ds[ac++] = new UnlimitedDimension(ax.getName());
								else if (ax != null)
									ds[ac++] = new Dimension(ax.getName(), ax.getLen());
								break;
						case 3: // lon axis
								ax = epsVar.getX();
								if (ax != null && ax.isUnlimited())
									ds[ac++] = new UnlimitedDimension(ax.getName());
								else if (ax != null)
									ds[ac++] = new Dimension(ax.getName(), ax.getLen());
								break;
						default: // axis not defined
								break;
					}
				}
				
				if (ac < numDims) {
					//variable has less dimensions than dimensions of file
					int numActDims = ac;
					Dimension[] temp = new Dimension[numActDims];
					for (int na=0; na<numActDims; na++)
						temp[na] = ds[na];
					ds = null;
					ds = new Dimension[numActDims];
					for (int na=0; na<numActDims; na++)
						ds[na] = temp[na];
				}
			}
			//Class
			Class clss = epsVar.getVclass();
			
			//attributes
			numAtts = epsVar.getAttributes().size();
			Attribute[] aa = new Attribute[numAtts];
			for (int a=0; a<numAtts; a++) {
				EPSAttribute epAtt = (EPSAttribute)epsVar.getAttributes().elementAt(a);
				aa[a] = epAtt.getNCAttribute();
			}
			
			// create a protovariable
			try{
				pvs[i] = new ProtoVariable(name, clss, ds, aa);
			}
			catch (Exception ex) {
				throw new EPSVariableException("Couldn't create a ProtoVariable for " + name);
			}
		}
		// construct the schema
		Schema sc = new Schema(pvs, globalAtts);
		
		try {
			// construct a new netCDF file with schema
			NetcdfFile ncNew = new NetcdfFile(outFile, true, true, sc);

			// get the file variable and write data to them
			VariableIterator vi = ncNew.iterator();
			while (vi.hasNext()) {
				Variable v = vi.next();
				int[] origin = new int[v.getRank()];
				
				// get the matching EPSVariable
				EPSVariable epsVar = this.getEPSVariable(v.getName());

				MultiArray varMA = epsVar.getData();
				//System.out.println(" varMA = " + varMA);
				
				int[] lens = varMA.getLengths();
				//System.out.println(" lens = " + lens.length);
				double[] array = null;
				array = EPS_Util.get1DDoubleArray(varMA, EPS_Util.getMeasuredDim(varMA));
				//System.out.println(" array = " + array + " l=" +lens.length);
				for (int b=0; b<array.length; b++) {
					double val;
					try {
						val = array[b];
					}
					catch (Exception ex) {
						System.out.println("Exception at " + b);
					}
				}

				v.copyin(origin, varMA);
			}
			
			ncNew.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
 	/**
  	* Create a vector of dbase subentries--used for section-oriented files.
  	*
   	* @param numEntries Number of subentries
   	* @param filename Name of section file
   	**/	
	public void createSubEntries(int numEntries, String filename) {
		mFile = new String(filename);
		mDBEntries = new Vector<Dbase>(numEntries);
	}
	
	
 	/**
  	* Add a Dbase entries to the subentries list.
  	*
   	* @param db Dbase entry to add
   	**/	
	public void addSubEntry(Dbase db) {
		mDBEntries.addElement(db);
	}
	
	
 	/**
  	* Get an iterator to the sub-database entries.
  	*
   	* @return SubDBIterator
   	**/	
	public SubDBIterator iterator() {
		return new SubDBIterator(mDBEntries, mFile);
	}
	
 	/**
  	* Returns flag whether this Dbase has subentries attached to it..
  	*
   	* @return true if contains subentries
   	**/	
	public boolean isSectionDB() {
		return !(mDBEntries == null);
	}
	
 	/**
  	* Add a file read progress listener to this Dbase.
  	*
   	* @param l A class that implements the FileReadProgressListener interface
   	**/	
	public void addFileReadProgressListener(FileReadProgressListener l) {
		if (mListeners.indexOf(l) <= 0)
			mListeners.addElement(l);
	}
	
 	/**
  	* Remove a file read progress listener from this Dbase.
  	*
   	* @param l A class that implements the FileReadProgressListener interface
   	**/	
	public void removeFileReadProgressListener(FileReadProgressListener l) {
		mListeners.removeElement(l);
	}
	
	
 	/**
  	* Notify registered listeners of FileReadProgressEvents.
  	*
   	* @param evt AWTEvent
   	**/	
	public void processEvent(AWTEvent evt) {
		if (evt instanceof FileReadProgressEvent) {
			if (mListeners != null) {
				for (int i=0; i<mListeners.size(); i++) {
					((FileReadProgressListener)mListeners.elementAt(i)).percentChange((FileReadProgressEvent)evt);
				}
			}
		}
	}
	
 	/**
  	* Set the actual pointer file entry that created this database.
  	*
   	**/	
	public void setEpicPtr(EpicPtr ep) {
		mPtr = ep;
	}
	
 	/**
  	* Returns the actual pointer file entry that created this database.
  	*
   	* @return EPICPtr object used to created this Dbase
   	**/	
	public EpicPtr getEpicPtr() {
		return mPtr;
	}

	
	//CompositeDbase getCompositeParent() {
	//	return mParent;
	//}
	
	public void dump() {
		System.out.println("Type = " + mType);
		System.out.println("Number of Attributes = " + mAttnum);
		System.out.println("Unlimited Dimension = " + mUnlimited);
		System.out.println("Number of Axes = " + mAxes.size());
		for (int i=0; i<mAxes.size(); i++) {
			Axis ax = mAxes.get(i);
			System.out.println("Axes #" + i + " = " + ax.getName());
			System.out.println("    Axes Type = " + ax.getType());
		}
		
		System.out.println("Number of Variables = " + mVariables.size());
		for (int i=0; i<mVariables.size(); i++) {
			EPSVariable var = mVariables.get(i);
			System.out.println("Variable #" + i +" = " + var.getName() + " " + var.getUnits());
			System.out.println("    X Axis = " + var.getX().getName());
			System.out.println("    Y Axis = " + var.getY().getName());
			System.out.println("    Z Axis = " + var.getZ().getName());
			System.out.println("    T Axis = " + var.getT().getName());
			for (int d = 0; d<4; d++) {
				System.out.println("    dimorder[" + d + "] = " + var.getDimorder(d));
			}
			
		}
		mPtr.toString();
		
	}
	
	public void addComment(String s) {
		mComments.add(s);
	}
	
	public Vector<String> getCommentsToo() {
		return mComments;
	}
}
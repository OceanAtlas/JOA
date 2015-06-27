package gov.noaa.pmel.eps2;

import ucar.multiarray.*;
import java.util.*;
import java.io.*;
import java.text.*;
import javaoceanatlas.resources.JOAConstants;
import gov.noaa.pmel.util.*;

/**
 * <code>EPSVariable</code> 
 * Object that stores a parsed variable from a data file
 *
 * @author oz
 * @version 1.0
 */
public class EPSVariable {
	/**
	* Variable identifier code
	*/
	protected int id;
	/**
	* Original name
	*/
	protected String oname;
	/**
	* Short name
	*/
	protected String sname;
	/**
	* Long name
	*/
	protected String lname;
	/**
	* Generic name 
	*/
	protected String gname;
	/**
	* Format
	*/
	protected String frmt;
	/**
	* Units
	*/
	protected String units;
	protected int prec = JOAConstants.JOA_DEFAULT_PRECISION;
	protected int sigDigits = 0;
	/**
	* Data type
	*/
	protected int dtype;
	/**
	* Class of data 
	*/
	protected Class vClass;
	/**
	* Real data (EPIC file storage only)
	*/
	protected MultiArray data;
	/**
	* index of the lower grid corner
	*/
	protected int[] lci = new int[4];
	/**
	* index of the upper grid corner
	*/
	protected int[] uci = new int[4];
	/**
	* Dimension order, (0=t,1=z,2=y,3=x)
	*/
	protected int[] dimorder = new int[4];
	/**
	* X axis structure
	*/
	protected Axis x;
	/**
	* Y axis Structure
	*/
	protected Axis y;
	/**
	* Z Axis Structure
	*/
	protected Axis z;
	/**
	* T axis structure
	*/
	protected Axis t;
	/**
	* Maximum attribute number in variable
	*/
	protected int attnum;
	/**
	* Collection of attributes for this variable
	*/
	protected Vector mAttributes;
	
  	/**
   	* Construct a new <code>EPSVariable</code>
   	*
   	*/
	public EPSVariable() {
		// zero arg constructor
		this.mAttributes = new Vector();
	}
	
  	/**
   	* Construct a new <code>EPSVariable</code> by copying an existing EPSVariable.
   	*
   	* @param var EPSVariable to copy
   	*
   	*/
	public EPSVariable(EPSVariable var) {
		// copy constructor
		this.mAttributes = new Vector();
		this.id = var.getID();
		this.sname = new String(var.getSname());
		this.lname = new String(var.getLname());
		this.gname = new String(var.getGname());
		this.frmt = new String(var.getFrmt());
		this.units = new String(var.getUnits());
		this.dtype = var.getDtype();
		this.prec = var.getDisplayPrecision();
		
		if (var.getData() != null) {
			try {
				this.setData(var.getData());
			}
			catch (IOException ex) {}
		}
		
		for (int i=0; i<4; i++)
			this.lci[i] = var.getLci(i);
		
		for (int i=0; i<4; i++)
			this.uci[i] = var.getUci(i);
		
		for (int i=0; i<4; i++)
			this.dimorder[i] = var.getDimorder(i);
			
		if (var.getX() != null)
			this.x = new Axis(var.getX());
		if (var.getY() != null)
			this.y = new Axis(var.getY());
		if (var.getZ() != null)
			this.z = new Axis(var.getZ());
		if (var.getT() != null)
			this.t = new Axis(var.getT());
		
		Vector atts = var.getAttributes();
		for (int i=0; i<atts.size(); i++) {
			EPSAttribute at = (EPSAttribute)atts.elementAt(i);
			this.mAttributes.addElement(new EPSAttribute(at));
		}
		
	}
	
  	/**
   	* Creates a new attribute (eps) and fills with values.
   	*
   	* @param attnum Sequential number of this attribute
   	* @param name Attribute name
   	* @param len Length of string attribute or number of elements of an array attribute
   	* @param value String or array
   	*/
	public void addAttribute(int attnum, String name, int type, int len, Object value) {
		// 
		EPSAttribute at = new EPSAttribute(attnum, name, type, len, value);
		at.setNum(attnum);
		at.setName(new String(name));
		at.setNewatt(true);
		at.setType(type);
		at.setLen(len);
		at.setValue(value);
		
		// install in collection
		this.mAttributes.addElement(at);
	}
	
  	/**
   	* Adds an existing attribute the to collection.
   	*
   	* @param at EPSAttribute to add to variable
   	*/
	public void addAttribute(EPSAttribute at) {
		mAttributes.addElement(at);
	}
	
  	/**
   	* Return a Vector of all the attributes for this variable.
   	*
   	* @return Vector of Attribute objects
   	*/
	public Vector getAttributes() {
		return mAttributes;
	}
	
  	/**
   	* Return the ith attribute for this variable.
   	*
   	* @return Attribute object
   	*/
	public EPSAttribute getAttribute(int indx) {
		return (EPSAttribute)mAttributes.elementAt(indx);
	}
	
  	/**
   	* Return the ith attribute for this variable.
   	*
   	* @return Attribute object
   	*/
	public int getAttnum() {
		return attnum;
	}
	
	/**
  	* Convenience function to return the value of an attribute as an integer.
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
  	* Convenience function to return the value of an attribute as an float.
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
  	* Get an attribute by name. Relax constraints on case and equality.
  	* @deprecated
  	* @see getAttribute
  	*
   	* @param name attribute name
   	*
  	* @return EPSAttribute matching name. null ==> not found
   	**/	
	public EPSAttribute getAttributeByName(String name) {
  		for (int i=0; i<mAttributes.size(); i++) {
  			EPSAttribute at = (EPSAttribute)mAttributes.elementAt(i);
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
  	* Get an attribute by name. Relax constraints on case and equality.
  	*
   	* @param name attribute name
   	*
  	* @return EPSAttribute matching name. null ==> not found
   	**/	
	public EPSAttribute getAttribute(String name) {
  		for (int i=0; i<mAttributes.size(); i++) {
  			EPSAttribute at = (EPSAttribute)mAttributes.elementAt(i);
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
   	* Set a pointer to the data for this variable.
   	*
   	* @param tma MultiArray of data to copy into new MultiArray
   	*
   	* @exception IOException Couldn't create a new MultiArray
   	*/
	public void setData(MultiArray tma) throws IOException {
		data = tma;
	}
	
  	/**
   	* Get the data as MultiArray this variable.
   	*
   	* @return MultiArray
   	*/
	public MultiArray getData() {
		return data;
	}
	
  	/**
   	* Set the data type for this variable.
   	*
   	* @param type Type of Variable (EPDOUBLE, EPBYTE, EPSHORT, EPINT, EPREAL)
   	*/
	public void setDtype(int type) {
		dtype = type;	
	}
	
  	/**
   	* Get the data Type this variable.
   	*
   	* @return EPDOUBLE, EPBYTE, EPSHORT, EPINT, EPREAL
   	*/
	public int getDtype() {
		return dtype;
	}
	
  	/**
   	* Get the variable ID (usually the EPIC Key Code) this variable.
   	*
   	* @return EPIC Key Code
   	*/
	public int getID() {
		return id;
	}
	
  	/**
   	* Set the ID for this variable.
   	*
   	* @param sid Usually the EPIC Key Code
   	*/
	public void setID(int sid) {
		id = sid;
	}
	
  	/**
   	* Get the name this variable.
   	*
   	* @return  name
   	*/
	public String getName() {
		return sname;
	}
	
  	/**
   	* Get the short name this variable.
   	*
   	* @return Short name
   	*/
	public String getSname() {
		return sname;
	}
	
  	/**
   	* Set the short name for this variable.
   	*
   	* @param s Short name
   	*/
	public void setSname(String s) {
		sname = s;
	}
	
  	/**
   	* Get the original name this variable.
   	*
   	* @return Original name
   	*/
	public String getOname() {
		return oname;
	}
	
  	/**
   	* Set the original name for this variable.
   	*
   	* @param s Original name
   	*/
	public void setOname(String s) {
		oname = s;
	}
	
  	/**
   	* Get the long name this variable.
   	*
   	* @return Long name
   	*/
	public String getLname() {
		return lname;
	}
	
  	/**
   	* Set the long name for this variable.
   	*
   	* @param s Long name
   	*/
	public void setLname(String s) {
		lname = s;
	}
	
  	/**
   	* Get the generic name this variable.
   	*
   	* @return Generic name
   	*/
	public String getGname() {
		return gname;
	}
	
  	/**
   	* Set the FORTRAN format for this variable.
   	*
   	* @param s FORTRAN format
   	*/
	public void setFrmt(String s) {
		frmt = s;
	}
	
  	/**
   	* Get the FORTRAN format for this variable.
   	*
   	* @return FORTRAN format
   	*/
	public String getFrmt() {
		return frmt;
	}
	
  	/**
   	* Set the generic name for this variable.
   	*
   	* @param s Generic name
   	*/
	public void setGname(String s) {
		gname = s;
	}
	
  	/**
   	* Get the measured units for this variable.
   	*
   	* @return Units
   	*/
	public String getUnits() {
		if (units != null)
			return units;
		else {
			// look for a units attribute
			EPSAttribute at = this.getAttribute("units");
			if (at != null) {
				String ustr = at.getStringVal();
				if (ustr != null)
					return ustr;
				else
					return "n.a."; 
			}
			else
				return "n.a.";
		}
	}
	
  	/**
   	* Set the units for this variable.
   	*
   	* @param s Units
   	*/
	public void setUnits(String s) {
		units = s;
	}
	
  	/**
   	* Set the lower corner coordinates for this variable.
   	*
   	* @param s indx Index
   	* @param s val Value
   	*/
	public void setLci(int indx, int val) {
		lci[indx] = val;
	}
	
  	/**
   	* Get the lower corner for the given index for this variable.
   	*
   	* @param indx Index
   	*
   	* @return Index
   	*/
	public int getLci(int indx) {
		return lci[indx];
	}
	
  	/**
   	* Get the lower corner indices for this variable.
   	*
   	* @return Array of indices
   	*/
	public int[] getLci() {
		return lci;
	}
	
  	/**
   	* Set the upper corner coordinates for this variable.
   	*
   	* @param s indx Index
   	* @param s val Value
   	*/
	public void setUci(int indx, int val) {
		uci[indx] = val;
	}
	
  	/**
   	* Get the upper corner for the given index for this variable.
   	*
   	* @param indx Index
   	*
   	* @return Index
   	*/
	public int getUci(int indx) {
		return uci[indx];
	}
	
  	/**
   	* Get the upper corner indices for this variable.
   	*
   	* @return Array of indices
   	*/
	public int[] getUci() {
		return uci;
	}
	
  	/**
   	* Set the dimension order for the ith axis for this variable.
   	*
   	* @param indx Axis index (0, 1, 2, 3)
   	* @param indx Order of the axes (0, 1, 2, 3)
   	*/
	public void setDimorder(int indx, int val) {
		dimorder[indx] = val;
	}
	
  	/**
   	* Get the dimension order for the ith axis for this variable.
   	*
   	* @param indx Axis number
   	*
   	* @return Order (0, 1, 2, 3)
   	*/
	public int getDimorder(int indx) {
		return dimorder[indx];
	}
	
  	/**
   	* Set the T axis for this variable.
   	*
   	* @return ax New T axis
   	*/
	public void setT(Axis ax) {
		t = ax;
	}
	
  	/**
   	* Set the X axis for this variable.
   	*
   	* @return ax New X axis
   	*/
	public void setX(Axis ax) {
		x = ax;
	}
	
  	/**
   	* Set the Y axis for this variable.
   	*
   	* @return ax New Y axis
   	*/
	public void setY(Axis ax) {
		y = ax;
	}
	
  	/**
   	* Set the Z axis for this variable.
   	*
   	* @return ax New Z axis
   	*/
	public void setZ(Axis ax) {
		z = ax;
	}
	
  	/**
   	* Get the X axis for this variable.
   	*
   	* @return X axis
   	*/
	public Axis getX() {
		return x;
	}
	
  	/**
   	* Get the Y axis for this variable.
   	*
   	* @return Y axis
   	*/
	public Axis getY() {
		return y;
	}
	
  	/**
   	* Get the Z axis for this variable.
   	*
   	* @return Z axis
   	*/
	public Axis getZ() {
		return z;
	}
	
  	/**
   	* Get the T axis for this variable.
   	*
   	* @return T axis
   	*/
	public Axis getT() {
		return t;
	}
	
  	/**
   	* Set name, format, and type fields for this variable.
   	*
   	* @param sname Short name
   	* @param lname Long name
   	* @param gname Generic name
   	* @param frmt FORTRAN format
   	* @param type Type (EPDOUBLE, EPBYTE, EPSHORT, EPINT, EPREAL)
   	*/
	public void set(String sname, String lname, String gname,
						   String frmt, String units, int type) {
		this.setSname(sname);
		this.setLname(lname);
		this.setGname(gname);
		this.setFrmt(frmt);
		this.setUnits(units);
		this.setDtype(type);
	}
	
  	/**
   	* Set class for this variable.
   	*
   	* @param c Class
   	*/
	public void setVclass(Class c) {
		vClass = c;
	}
	
  	/**
   	* Get class for this variable.
   	*
   	* @return Class
   	*/
	public Class getVclass() {
		return vClass;
	}
	
 	/**
  	* Convert index to user units (uses the axes in a variable structure) (i,j,k,l) --> (x,y,z,t).
  	*
   	* @param pt point to convert
   	* @param coord converted point
   	* @param time converted time
   	*
  	* @exception EPSVariableException An error occurred converting index to user coordinates.
   	**/	
	public void indexToUser(int[] pt, float[] coord, GeoDate[] time) throws EPSVariableException {
		try {
			coord[0] = this.getX().getData().getFloat(new int[] {pt[0] - this.getLci(0)});
			coord[1] = this.getY().getData().getFloat(new int[] {pt[1] - this.getLci(1)});
			coord[2] = this.getZ().getData().getFloat(new int[] {pt[2] - this.getLci(2)});
			MultiArray tma = this.getT().getData();
			time[0] = (GeoDate)tma.get(new int[] {pt[3] - this.getLci(3)});



		}
		catch (Exception ex) {
			throw new EPSVariableException("Couldn't convert index to user coordinates");
		}
	}

 	/**
  	* convert from user to index units (uses the axes in a variable structure) (x,y,z,t) --> (i,j,k,l).
  	*
   	* @param pt converted point
   	* @param coord user coordinates to convert
   	* @param time times to convert (should probably be GeoDates!!!!)
   	*
  	* @exception EPSVariableException An error occurred converting index to user coordinates.
   	**/
	public void userToIndex(float[] coord, long[] time, int[] pt) {
		long[] delta0 = new long[2];
		long[] delta1 = new long[2];
		long[] delta2 = new long[2];
		
		// find the closest index for the given coord value for each dimension
		// as a convenience, get the Axes and and the Axes Multiarrays
		Axis x = this.getX();
		Axis y = this.getY();
		Axis z = this.getY();
		Axis t = this.getY();
		
		ArrayMultiArray xMa = (ArrayMultiArray)x.getData();
		ArrayMultiArray yMa = (ArrayMultiArray)y.getData();
		ArrayMultiArray zMa = (ArrayMultiArray)z.getData();
		MultiArray tMa = t.getData();
		
		// x dimension
		if (this.getLci(0) == this.getUci(0)) {
			pt[0] = this.getLci(0);
		} 
		else {
			if (xMa.getFloat(new int[] {0}) < xMa.getFloat(new int[] {this.getUci(0) - this.getLci(0)})) {
				if (coord[0] <= xMa.getFloat(new int[] {0})) {
					pt[0] = this.getLci(0);
				} 
				else if (coord[0] >= xMa.getFloat(new int[] {this.getUci(0) - this.getLci(0)})) {
					pt[0] = this.getUci(0);
				} 
				else {
					for (int i=0; i<(this.getUci(0) - this.getLci(0)); i++) {
						if (coord[0] >= xMa.getFloat(new int[] {i}) && coord[0] <= xMa.getFloat(new int[] {i+1})) {
							if (coord[0] - xMa.getFloat(new int[] {i}) < xMa.getFloat(new int[] {i+1}) - coord[0])
								pt[0] = this.getLci(0) + i;
							else
								pt[0] = this.getLci(0) + i + 1;
						}
					}
				}
			} 
			else {
				if (coord[0] >= xMa.getFloat(new int[] {0})) {
					pt[0] = this.getLci(0);
				} 
				else if (coord[0] <= xMa.getFloat(new int[] {this.getUci(0) - this.getLci(0)})) {
					pt[0] = this.getUci(0);
				} 
				else {
					for (int i=0; i<(this.getUci(0) - this.getLci(0)); i++) {
						if (coord[0] <= xMa.getFloat(new int[] {i}) && coord[0] >= xMa.getFloat(new int[] {i+1})) {
							if (coord[0] - xMa.getFloat(new int[] {i+1}) > xMa.getFloat(new int[] {i}) - coord[0])
								pt[0] = this.getLci(0)+i;
							else
								pt[0] = this.getLci(0)+i+1;
						}
					}
				}
			}
		}
		
		// y dimension
		if (this.getLci(1) == this.getUci(1)) {
			pt[1] = this.getLci(1);
		} 
		else {
			if (yMa.getFloat(new int[] {0}) < yMa.getFloat(new int[] {this.getUci(1) - this.getLci(1)})) {
				if (coord[1] <= yMa.getFloat(new int[] {0})) {
					pt[1] = this.getLci(1);
				} 
				else if (coord[1] >= yMa.getFloat(new int[] {this.getUci(1) - this.getLci(1)})) {
					pt[1] = this.getUci(1);
				} 
				else {
					for (int i=0; i < (this.getUci(1)-this.getLci(1)); i++) {
						if (coord[1] >= yMa.getFloat(new int[] {i}) && coord[1] <= yMa.getFloat(new int[] {i+1})) {
							if (coord[1] - yMa.getFloat(new int[] {i}) < yMa.getFloat(new int[] {i+1}) - coord[1])
								pt[1] = this.getLci(1) + i;
							else
								pt[1] = this.getLci(1) + i + 1;
						}
					}
				}
			} 
			else {
				if (coord[1] >= yMa.getFloat(new int[] {0})) {
					pt[1] = this.getLci(1);
				} 
				else if (coord[1] <= yMa.getFloat(new int[] {this.getUci(1) - this.getLci(1)})) {
					pt[1] = this.getUci(1);
				} 
				else {
					for (int i=0; i<(this.getUci(1) - this.getLci(1)); i++) {
						if (coord[1] <= yMa.getFloat(new int[] {i}) && coord[1] >= yMa.getFloat(new int[] {i+1})) {
							if (coord[1] - yMa.getFloat(new int[] {i+1}) > yMa.getFloat(new int[] {i}) - coord[1])
								pt[1] = this.getLci(1) + i;
							else
								pt[1] = this.getLci(1) + i + 1;
						}
					}
				}
			}
		}
		
		// z dimension
		if (this.getLci(2) == this.getUci(2)) {
			pt[2] = this.getLci(2);
		} 
		else {
			if (zMa.getFloat(new int[] {0}) < zMa.getFloat(new int[] {this.getUci(2) - this.getLci(2)})) {
				if (coord[2] <= zMa.getFloat(new int[] {0})) {
					pt[2] = this.getLci(2);
				} 
				else if (coord[2] >= zMa.getFloat(new int[] {this.getUci(2) - this.getLci(2)})) {
					pt[2] = this.getUci(2);
				} 
				else {
					for (int i=0; i < (this.getUci(2) - this.getLci(2)); i++) {
						if (coord[2] >= zMa.getFloat(new int[] {i}) && coord[2] <= zMa.getFloat(new int[] {i+1})) {
							if (coord[2] - zMa.getFloat(new int[] {i}) < zMa.getFloat(new int[] {i+1}) - coord[2])
								pt[2] = this.getLci(2)+i;
							else
								pt[2] = this.getLci(2)+i+1;
						}
					}
				}
			} 
			else {
				if (coord[2] >= zMa.getFloat(new int[] {0})) {
					pt[2] = this.getLci(2);
				} 
				else if (coord[2] <= zMa.getFloat(new int[] {this.getUci(2) - this.getLci(2)})) {
					pt[2] = this.getUci(2);
				} 
				else {
					for (int i=0; i < (this.getUci(2) - this.getLci(2)); i++) {
						if (coord[2] <= zMa.getFloat(new int[] {i}) && coord[2] >= zMa.getFloat(new int[] {i+1})) {
							if (coord[2] - zMa.getFloat(new int[] {i+1}) > zMa.getFloat(new int[] {i}) - coord[2])
								pt[2] = this.getLci(2)+i;
							else
								pt[2] = this.getLci(2)+i+1;
						}
					}
				}
			}
		}

		//// t dimension
		if (this.getLci(3) == this.getUci(3)) {
			pt[3] = this.getLci(3);
		} 
		else {
			/* assume time is increasing with increasing index
			ep_time_sub(&var->t->t[0], time, delta0);
			ep_time_sub(time, &var->t->t[2*(this.getUci(3) - this.getLci(3))], delta1);
			if (delta0[0] < 0 || delta0[1] < 0 || (delta0[0]+delta0[1] == 0)) {
				pt[3] = this.getLci(3);
			} 
			else if (delta1[0] < 0 || delta1[1] < 0 || (delta1[0]+delta1[1] == 0)) {
				pt[3] = this.getUci(3);
			} 
			else {
				for (int i=0; i < (this.getUci(3)-this.getLci(3)); i++) {
					ep_time_sub(&var->t->t[2*i], time, delta0);
					ep_time_sub(time, &var->t->t[2*(i+1)], delta1);
					if (delta0[0] >= 0 && delta0[1] >= 0 && delta1[0] >= 0 && delta1[1] >= 0) {
						ep_time_sub(delta0, delta1, delta2);
						if (delta2[0] >= 0 && delta2[1] >= 0) 
							pt[3] = this.getLci(3)+i;
						else
							pt[3] = this.getLci(3)+i+1;
					}
				}
			}*/
		}
	}
	
	
 	/**
  	* Returns a TimeBase object for an EPSVariable.
  	*
  	* @see TimeBase
  	*
  	* @exception AxisErrorException axis Variable doesn't have a time axis.
   	**/	
	public TimeBase getTimeBaseByID(int varid) throws AxisErrorException { 
		// returns time base, units and type 
		String t_base = new String();
		String t_base2 = new String();
		String t_units = new String();
		
		Axis ax = this.getT();
		if (ax == null) {  
			// axis does not exist 
			throw new AxisErrorException("getTimeBaseByID: time axis does not exist (var code = " + 
				varid + ")");
		} 
		else if (!ax.isTimeAxis()) { 
			// axis is not a time axis 
			throw new AxisErrorException("getTimeBaseByID: requested axis is not a time axis (var code = " + 
				varid + ")");
		}
		
		if (ax.getUnits().indexOf("since") >= 0) {
			//sscanf(ax->units,"%s since %s %s",units,tbase,t_base2);
			Object[] objs = {new String(), new String(), new String()};
			MessageFormat msgf = new MessageFormat("{0} since {1} {2}");
			try {
				objs = msgf.parse(ax.getUnits());
				t_units = (String)objs[0];
				t_base = (String)objs[1];
				t_base2 = (String)objs[2];
			}
			catch (ParseException pe) {
				System.out.println("parse error");
			}
			t_base = t_base + " " + t_base2;
		}
		else if (ax.getUnits().indexOf("time_origin") >= 0) {
			//sscanf(ax->units,"%s time_origin %s %s",units,tbase,t_base2);
			Object[] objs = {new String(), new String(), new String()};
			MessageFormat msgf = new MessageFormat("{0} time_origin {1} {2}");
			try {
				objs = msgf.parse(ax.getUnits());
				t_units = (String)objs[0];
				t_base = (String)objs[1];
				t_base2 = (String)objs[2];
			}
			catch (ParseException pe) {
				System.out.println("parse error");
			}
			t_base = t_base + " " + t_base2;
		}
		else {
			t_base = new String("");
			t_units = ax.getUnits();
		}
		
		return new TimeBase(t_base, t_units);
	}
	
	public int getDisplayPrecision() {
		return prec;
	}
	
	public void setDisplayPrecision(int i) {
		prec = i;
	}
	
	public int getSignificantDigits() {
		return sigDigits;
	}
	
	public void setSignificantDigits(int sd) {
		if (sd > sigDigits) {
			sigDigits = sd;
		}
	}
	
	public void dumpVarData(String prefix) {
		MultiArray vma = this.getData();
		int[] lens = vma.getLengths();
		if (prefix != null)
			System.out.println(prefix);
		for (int b=0; b<lens[0]; b++) {
			double varVal = -99;
			// array optimization
			try {
				varVal = vma.getDouble(new int[] {0, b, 0, 0});
			}
			catch (Exception ex) {
				varVal = -99;
			}
			String name = null;
			if (this.getGname() != null)
				name = this.getGname();
			else if (this.getSname() != null)
				name = this.getSname();
			else if (this.getOname() != null)
				name = this.getOname();
			else if (this.getLname() != null)
				name = this.getLname();
			System.out.println(b + " " + name + " " + varVal);
		}
	}	
	
	public boolean isPressure() {
		if (oname != null) {
			if (oname.equalsIgnoreCase("depth"))
				return true;
			if (oname.equalsIgnoreCase("pres"))
				return true;
			if (oname.equalsIgnoreCase("pressure"))
				return true;
			if (oname.equalsIgnoreCase("p"))
				return true;
			if (oname.equalsIgnoreCase("dep"))
				return true;
			if (oname.equalsIgnoreCase("ctdprs"))
				return true;
		}
		
		if (sname != null) {
			if (sname.equalsIgnoreCase("depth"))
				return true;
			if (sname.equalsIgnoreCase("pres"))
				return true;
			if (sname.equalsIgnoreCase("pressure"))
				return true;
			if (sname.equalsIgnoreCase("p"))
				return true;
			if (sname.equalsIgnoreCase("dep"))
				return true;
			if (sname.equalsIgnoreCase("ctdprs"))
				return true;
		}
		
		if (lname != null) {
			if (lname.equalsIgnoreCase("depth"))
				return true;
			if (lname.equalsIgnoreCase("pres"))
				return true;
			if (lname.equalsIgnoreCase("pressure"))
				return true;
			if (lname.equalsIgnoreCase("p"))
				return true;
			if (lname.equalsIgnoreCase("dep"))
				return true;
			if (lname.equalsIgnoreCase("ctdprs"))
				return true;
		}
		
		if (gname != null) {
			if (gname.equalsIgnoreCase("depth"))
				return true;
			if (gname.equalsIgnoreCase("pres"))
				return true;
			if (gname.equalsIgnoreCase("pressure"))
				return true;
			if (gname.equalsIgnoreCase("p"))
				return true;
			if (gname.equalsIgnoreCase("dep"))
				return true;
			if (gname.equalsIgnoreCase("ctdprs"))
				return true;
		}
		return false;
	}

}
package gov.noaa.pmel.eps2;

import java.util.*;
import ucar.multiarray.*;

/**
 * <code>Axis</code> Object to store an axis from an netCDF file
 *
 *
 * @author oz
 * @version 1.1
 */
 
public class Axis {
    /**
     * new flag, = true, not written yet
     */
	protected boolean mNewaxis; 
    /**
     * dimension flag, = true, axis is a dimension for the netCDF file
     */
	protected boolean mIsDimension = false; 
    /**
     * variable ident code 
     */
	protected int mID;
    /**
     * true if time axis 
     */
	protected boolean mIsTime;	
    /**
     * integer code for what the axis is
     */
	protected int mAxisType;	
    /**
     * axis name
     */
	protected String mName;	
    /**
     * translated axis name
     */
	protected String mTranslatedName;
    /**
     * axis format 
     */
	protected String mFormat;
    /**
     * axis units
     */
	protected String mUnits;
    /**
     * axis type
     */
	protected String mType;
    /**
     * axis data storage  (MultiArray)
     */
	protected MultiArray mData;
    /**
     * length of storage 
     */
	protected int mLen;
    /**
     * maximum attribute number in axis
     */
	protected int mAttrNum; 
    /**
     * attributes in axis (Attribute)
     */
	protected Vector mAttributes;  
    /**
     * true if this is the unlimited dimension
     */
	protected boolean mIsUnlimited = false; 
	
    /**
     * Construct a new <code>Axis</code>.
     */
	public Axis() {
		// zero arg constructor
		mAttributes = new Vector();
	}
	
    /**
     * Construct a new <code>Axis</code> from another 
     * <code>Axis</code>. This is a copy constructor
     *
     * @param ax Axis to copy into new Axis
     */
	public Axis(Axis ax) {
		// copy constructor
		this.mAttributes = new Vector();
		this.mNewaxis = ax.getNewAxis();
		this.mID = ax.getID();
		this.mIsTime = ax.isTimeAxis();
		this.mAxisType = ax.getAxisType();
		this.mName = new String(ax.getName());
		this.mFormat = new String(ax.getFrmt());
		this.mUnits = new String(ax.getUnits());
		this.mType = new String(ax.getType());
		if (ax.getData() != null) {
			try {
				this.mData = new ArrayMultiArray(ax.getData());
			}
			catch (Exception ex) {}
		}
		this.mLen = ax.getLen();
		this.mAttrNum = ax.getAttnum();
		
		Vector atts = ax.getAttributes();
		for (int i=0; i<atts.size(); i++) {
			EPSAttribute at = (EPSAttribute)atts.elementAt(i);
			this.mAttributes.addElement(new EPSAttribute(at));
		}
	}
	
    /**
     * Add an EPSAttribute to this axis
     *
     * @param at EPSAttribute to add to this axis
     */
	public void addAttribute(int attnum, String name, int type, int len, Object value) {
		// creates a new attribute (eps) and fills with values
		EPSAttribute at = new EPSAttribute(attnum, name, type, len, value);
		at.setNum(attnum);
		at.setName(new String(name));
		at.setNewatt(true);
		at.setType(type);
		at.setLen(len);
		at.setValue(value);
		
		// install in dbase
		this.mAttributes.addElement(at);
	}
	
    /**
     * Add an EPSAttribute to this axis
     *
     * @param at EPSAttribute to add to this axis
     */
	public void addAttribute(EPSAttribute at) {
		mAttributes.addElement(at);
	}
	
    /**
     * return a Vector of the EPSAttribute in this axis
     *
     * @return Vector of EPSAttribute
     */
	public Vector getAttributes() {
		return mAttributes;
	}
	
 	/**
  	* Get an axis attribute by name.
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
  	* Get an axis attribute by name.
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
     * returns the ith EPSAttribute in this axis
     *
     * @param indx index of EPSAttribute to return
     * @return ith EPSAttribute
     */
	public EPSAttribute getAttribute(int indx) {
		return (EPSAttribute)mAttributes.elementAt(indx);
	}

    /**
     * set the newaxis flag for this axis
     *
     * @param newa true or false
     */
	public void setNewAxis(boolean newa) {
		mNewaxis = newa;
	}
	
    /**
     * Returns the "new axis" flag for this axis
     *
     * @return new axis flag
     */
	public boolean getNewAxis() {
		return mNewaxis;
	}
	
	/**
  	* Set the name of the axis.
  	*
   	* @param nm axis name string
   	**/	
	public void setName(String nm) {
		mName = null;
		mName = new String(nm);
		if (nm.equalsIgnoreCase("pressure") && mIsDimension)
			mTranslatedName = new String("depth");
		else if (nm.equalsIgnoreCase("ctdprs") && mIsDimension)
			mTranslatedName = new String("depth");
		else
			mTranslatedName = new String(nm);
	}
	
    /**
     * Returns the name for this axis
     *
     * @return axis name
     */
	public String getName() {
		return mName;
	}
	
    /**
     * Returns the translated name for this axis
     *
     * @return translated axis name
     */
	public String getTranslatedName() {
		return mTranslatedName;
	}
	
	/**
  	* Set flag indicating whether axis is a time axis.
  	*
   	* @param tm true = time axis, false = geographical axis
   	**/	
	public void setTime(boolean tm) {
		mIsTime = tm;
	}
	
    /**
     * Returns the integer code for the type this axis
     *
     * @return axis type
     */
	public int getAxisType() {
		return mAxisType;
	}
	
	/**
  	* Set axis type code.
  	*
   	* @param tm axis type code
   	**/	
	public void setAxisType(int tm) {
		mAxisType = tm;
	}
	
    /**
     * Returns whether this axis is atime axis.
     *
     * @return true if time axis, false if geographical axis
     */
	public boolean isTimeAxis() {
		return mIsTime;
	}
	
	/**
  	* Set FORTRAN format string of axis.
  	*
   	* @param frm FORTRAN format specification string
   	**/	
	public void setFrmt(String frm) {
		mFormat = null;
		mFormat = new String(frm);
	}
	
    /**
     * Returns the FORTRAN format for this axis.
     *
     * @return FORTRAN format
     */
	public String getFrmt() {
		return mFormat;
	}
	
	/**
  	* Set units string of axis.
  	*
   	* @param uni Units specification string
   	**/	
	public void setUnits(String uni) {
		mUnits = null;
		mUnits = new String(uni);
	}
	
    /**
     * Returns the units string for this axis.
     *
     * @return units as string
     */
	public String getUnits() {
		return mUnits;
	}
	
	/**
  	* Set type of axis.
  	*
   	* @param typ Type
   	**/	
	public void setType(String typ) {
		mType = null;
		mType = new String(typ);
	}
	
    /**
     * Returns the type of this axis.
     *
     * @return axis type
     */
	public String getType() {
		return mType;
	}
	
	/**
  	* Set id code of axis.
  	*
   	* @param idd ID code
   	**/	
	public void setID(int idd) {
		mID = idd;
	}
	
    /**
     * Returns the ID code of this axis.
     *
     * @return axis id code
     */
	public int getID() {
		return mID;
	}
	
	/**
  	* Set the length of the axis.
  	*
   	* @param ln new axis length
   	**/	
	public void setLen(int ln) {
		mLen = ln;
	}
	
    /**
     * Returns the length of this axis.
     *
     * @return axis length
     */
	public int getLen() {
		return mLen;
	}

    /**
     * Returns the data as a MultiArray for this axis.
     *
     * @return MultiArray of data
     */
	public MultiArray getData() {
		return mData;
	}
		
	/**
  	* Set the MultiArray data for this axis. Data will be either a MultiArray
  	* of values (double, float, int, short, etc...) or a MultiArray of GeoDates
  	* if axis is a time axis.
  	*
   	* @param ma MultiArray of either values or GeoDates
   	**/	
	public void setData(MultiArray ma) {
		mData = ma;
	}
	
	/**
  	* Set the maximum number of attributes for this axis.
  	*
   	* @param att Max attribute number
   	**/	
	public void setAttnum(int att) {
		mAttrNum = att;
	}
	
    /**
     * Returns the attribute number for this axis.
     *
     * @return attribute number
     */
	public int getAttnum() {
		return mAttrNum;
	}
	
	/**
  	* Set flag that indicates whether axis is a dimension.
  	*
   	* @param flg Flag indicating dimension status
   	**/	
	public void setDimension(boolean flg) {
		mIsDimension = flg;
	}
	
	/**
  	* Set flag that indicates whether axis is the unlimited dimension.
  	*
   	* @param unl Flag indicating unlimited status
   	**/	
	public void setUnlimited(boolean unl) {
		mIsUnlimited = unl;
	}
	
    /**
     * Returns whether this axis is the unlimited dimension.
     *
     * @return true = unlimited, false = fix dimension
     */
	public boolean isUnlimited() {
		return mIsUnlimited;
	}
	
    /**
     * Returns whether this axis is a dimension.
     *
     * @return true = dimension, false = not a dimension
     */
	public boolean isDimension() {
		return mIsDimension;
	}
}
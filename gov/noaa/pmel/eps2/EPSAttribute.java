package gov.noaa.pmel.eps2;

import ucar.multiarray.*;
import ucar.netcdf.*;

/**
 * <code>EPSAttribute</code> 
 * Object that stores a variable attribute
 *
 * @author oz
 * @version 1.0
 */
public class EPSAttribute implements EPSConstants {
	/**
	* new flag, true =  not written yet
	*/
	protected boolean newatt;
	/**
	* attribute number 
	*/
	protected int num; 
	/**
	* mattribute name 
	*/
	protected String name;
	/**
	* length of string or array 
	*/
	protected int len;
	/**
	* type of attribute 
	*/
	protected int type;
	/**
	* multiarray representation of data
	*/
	protected MultiArray val;
	/**
	* array of string values
	*/
	protected String sval;
	/**
	* array of integer values
	*/
	protected int[] ival;
	/**
	* array of short values
	*/
	protected short[] shval;
	/**
	*  array of float values
	*/
	protected float[] fval;
	/**
	* array of double values
	*/
	protected double[] dval;

  	/**
   	* Construct a new <code>EPSAttribute</code>.
   	*
   	*/
	public EPSAttribute() {
		// zero arg constructor
	}	
	
  	/**
   	* Construct a new <code>EPSAttribute</code> from values.
	*
   	* @param name Attribute name
   	* @param len Length of attribute (string length or array length)
   	* @param value Attribute value (either string or array of values)
   	*/
	public EPSAttribute(int attnum, String name, int type, int len, Object value) {
		this.setNum(attnum);
		this.setName(new String(name));
		this.setNewatt(true);
		this.setType(type);
		this.setLen(len);
		this.setValue(value);
	}
	
  	/**
   	* Construct a new <code>EPSAttribute</code> an existing EPSAttribute.
	*
   	* @param at Attribute to copy
   	*/
	public EPSAttribute(EPSAttribute at) {
		// copy constructor
		newatt = at.isNewAtt();
		num = at.getNum();
		name = new String(at.getName());
		len = at.getLen();
		type = at.getType();
		sval = new String(at.getStringVal());
		try {
			val = new ArrayMultiArray(at.getValMa());
		}
		catch (Exception ex) {}
		if (at.getIntVals() != null) {
			int dim = at.getIntVals().length;
			ival = new int[dim];
			for (int i=0; i<dim; i++)
				ival[i] = at.getIntVals()[i];
		}
		
		if (at.getShortVals() != null) {
			int dim = at.getShortVals().length;
			shval = new short[dim];
			for (int i=0; i<dim; i++)
				shval[i] = at.getShortVals()[i];
		}
		
		if (at.getFloatVals() != null) {
			int dim = at.getFloatVals().length;
			fval = new float[dim];
			for (int i=0; i<dim; i++)
				fval[i] = at.getFloatVals()[i];
		}
		
		if (at.getDoubleVals() != null) {
			int dim = at.getDoubleVals().length;
			dval = new double[dim];
			for (int i=0; i<dim; i++)
				dval[i] = at.getDoubleVals()[i];
		}
		
	}
	
 	/**
  	* Get the string value array.
  	* @deprecated
  	* @see getStringVal
   	*
  	* @return array of values
   	**/	
	public String getSval() {
		return sval;
	}
	
 	/**
  	* Get the string value array.
   	*
  	* @return array of values
   	**/	
	public String getStringVal() {
		return sval;
	}
	
 	/**
  	* Get the integer value array.
  	* @deprecated
  	* @see getIntVals
   	*
  	* @return array of values
   	**/	
	public int[] getIval() {
		return ival;
	}
	
 	/**
  	* Get the integer value array.
   	*
  	* @return array of values
   	**/	
	public int[] getIntVals() {
		return ival;
	}
	
 	/**
  	* Get the short value array.
  	* @deprecated
  	* @see getShortVals
   	*
  	* @return array of values
   	**/	
	public short[] getShval() {
		return shval;
	}
	
 	/**
  	* Get the short value array.
   	*
  	* @return array of values
   	**/	
	public short[] getShortVals() {
		return shval;
	}
	
 	/**
  	* Get the float value array.
  	* @deprecated
  	* @see getFloatVals
   	*
  	* @return array of values
   	**/	
	public float[] getFval() {
		return fval;
	}
	
 	/**
  	* Get the float value array.
   	*
  	* @return array of values
   	**/	
	public float[] getFloatVals() {
		return fval;
	}
	
 	/**
  	* Get the double value array.
  	* @deprecated
  	* @see getDoubleVals
   	*
  	* @return array of values
   	**/	
	public double[] getDval() {
		return dval;
	}
	
 	/**
  	* Get the double value array.
   	*
  	* @return array of values
   	**/	
	public double[] getDoubleVals() {
		return dval;
	}
	
 	/**
  	* Get the multiarray of values associated with this attribute.
  	* @deprecated
  	* @see getMultiArrayValue
   	*
  	* @return array of values
   	**/	
	public MultiArray getValMa() {
		return val;
	}
	
 	/**
  	* Get the multiarray of values associated with this attribute.
   	*
  	* @return array of values
   	**/	
	public MultiArray getMultiArrayValue() {
		return val;
	}
	
  	/**
   	* Set the new attribute flag.
   	*
   	* @param b true = new attribute
   	*/
	public void setNewatt(boolean b) {
		newatt = b;
	}
	
 	/**
  	* Get the new attribute flag.
   	*
  	* @return true = new attribute
   	**/	
	public boolean isNewAtt() {
		return newatt;
	}
	
  	/**
   	* Set the attribute number.
   	*
   	* @param n Attribute number
   	*/
	public void setNum(int n) {
		num = n;
	}
	
 	/**
  	* Get the attribute number.
   	*
  	* @return attribute number
   	**/
	public int getNum() {
		return num;
	}
	
  	/**
   	* Set the attribute name.
   	*
   	* @param s Attribute name
   	*/
	public void setName(String s) {
		name = null;
		name = new String(s);
	}
	
 	/**
  	* Get the attribute name.
   	*
  	* @return attribute name
   	**/
	public String getName() {
		return name;
	}
	
  	/**
   	* Set the attribute's multiarray.
   	* @deprecated
   	* @see setValue
   	*
   	* @param ma Multiarray
   	*/
	public void setVal(MultiArray ma) {
		val = ma;
	}
	
  	/**
   	* Set the attribute's multiarray.
   	*
   	* @param ma Multiarray
   	*/
	public void setValue(MultiArray ma) {
		val = ma;
	}
	
  	/**
   	* Set the attribute's value array.
   	* @deprecated
   	* @see setValue
   	*
   	* @param inVal Object that represents either a String or array of primitive types
   	*/
	public void setVal(Object inVal) {
	 	//copy array stored in value into new object stored in epsattribute
	 	if (inVal == null) {
	 		sval = null;
			ival = null;
			shval = null;
			fval = null;
			dval = null;
			return;
	 	}
	 		
	  	switch(type) {
			case EPCHAR:  
			 	val = new ArrayMultiArray(new String[] {(String)inVal});
			 	//System.out.println(EPS_Util.MultiArrayToString(val));
				sval = new String((String)inVal);
				break;
			case EPSHORT:   
			 	val = new ArrayMultiArray(inVal);
				shval = new short[len];
				for (int i=0; i<len; i++) {
					shval[i] = ((short[])inVal)[i];
				}
				break;
			case EPINT:  
			 	val = new ArrayMultiArray(inVal);
				ival = new int[len];
				for (int i=0; i<len; i++) {
					ival[i] = ((int[])inVal)[i];
				}
				break;
			case EPREAL:  
			 	val = new ArrayMultiArray(inVal);
				fval = new float[len];
				for (int i=0; i<len; i++) {
					fval[i] = ((float[])inVal)[i];
				}
				break;
			case EPDOUBLE:   
			 	val = new ArrayMultiArray(inVal);
				dval = new double[len];
				for (int i=0; i<len; i++) {
					dval[i] = ((double[])inVal)[i];
				}
				break;
			default: 
				System.out.println("hit default");
				break;
		}
	}
	
  	/**
   	* Set the attribute's value array.
   	*
   	* @param inVal Object that represents either a String or array of primitive types
   	*/
	public void setValue(Object inVal) {
	 	//copy array stored in value into new object stored in epsattribute
	 	if (inVal == null) {
	 		sval = null;
			ival = null;
			shval = null;
			fval = null;
			dval = null;
			return;
	 	}
	 		
	  	switch(type) {
			case EPCHAR:  
			 	val = new ArrayMultiArray(new String[] {(String)inVal});
			 	//System.out.println(EPS_Util.MultiArrayToString(val));
				sval = new String((String)inVal);
				break;
			case EPSHORT:   
			 	val = new ArrayMultiArray(inVal);
				shval = new short[len];
				for (int i=0; i<len; i++) {
					shval[i] = ((short[])inVal)[i];
				}
				break;
			case EPINT:  
			 	val = new ArrayMultiArray(inVal);
				ival = new int[len];
				for (int i=0; i<len; i++) {
					ival[i] = ((int[])inVal)[i];
				}
				break;
			case EPREAL:  
			 	val = new ArrayMultiArray(inVal);
				fval = new float[len];
				for (int i=0; i<len; i++) {
					fval[i] = ((float[])inVal)[i];
				}
				break;
			case EPDOUBLE:   
			 	val = new ArrayMultiArray(inVal);
				dval = new double[len];
				for (int i=0; i<len; i++) {
					dval[i] = ((double[])inVal)[i];
				}
				break;
			default: 
				System.out.println("hit default");
				break;
		}
	}
	
 	/**
  	* Get the attribute value string or array.
   	* @deprecated
   	* @see getValue
   	*
  	* @return Value array as a generic object
   	**/
	public Object getVal() {
	 	//copy array stored in value into new object stored in epsattribute
	  	switch(type) {
			case EPCHAR: 
				if (sval != null)
					return sval;
				else 
					return new String("Not Available");
			case EPSHORT: 
				return shval;
			case EPINT:  
				return ival;
			case EPREAL:  
				return fval ;
			case EPDOUBLE:   
				return dval;
			default: 
				return null;
		}
	}
	
 	/**
  	* Get the attribute value string or array.
   	*
  	* @return Value array as a generic object
   	**/
	public Object getValue() {
	 	//copy array stored in value into new object stored in epsattribute
	  	switch(type) {
			case EPCHAR: 
				if (sval != null)
					return sval;
				else 
					return new String("Not Available");
			case EPSHORT: 
				return shval;
			case EPINT:  
				return ival;
			case EPREAL:  
				return fval;
			case EPDOUBLE:   
				return dval;
			default: 
				return null;
		}
	}
	
  	/**
   	* Set the attribute's type.
   	*
   	* @param typ Type
   	*/
	public void setType(int typ) {
		type = typ;
	}
	
 	/**
  	* Get the attribute type.
   	*
  	* @return Type
   	**/
	public int getType() {
		return type;
	}
	
  	/**
   	* Set the attribute's length (string or array length).
   	*
   	* @param l Length
   	*/
	public void setLen(int l) {
		len = l;
	}
	
 	/**
  	* Get the attribute length (string length or number of array elements).
   	*
  	* @return Value array as a generic object
   	**/
	public int getLen() {
		return len;
	}
	
 	/**
  	* Get the attribute as a netCDF attribute.
   	*
  	* @return netCDF attribute
   	**/
	public Attribute getNCAttribute() {
	  	switch(type) {
			case EPCHAR:
				if (sval == null || sval.length() == 0)
					sval = "UNKNOWN";
				return new Attribute(getName(), sval);
			case EPSHORT: 
				return new Attribute(getName(), shval);
			case EPINT:  
				return new Attribute(getName(), ival);
			case EPREAL:  
				return new Attribute(getName(), fval);
			case EPDOUBLE:   
				return new Attribute(getName(), dval);
			default: 
				return null;
		}
	}
}
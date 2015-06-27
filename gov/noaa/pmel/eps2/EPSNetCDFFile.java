package gov.noaa.pmel.eps2;

import ucar.netcdf.*;
import java.io.*;
import java.net.URL;


/**
 * <code>EPSNetCDFFile</code>
 * Wrapper object for a netCDF file. This object defines a simplified API into a netCDF
 * file. Using these, a netCDF reader doesn't have to use actually iterate through the
 * file to recover information.
 *
 * @author oz
 * @version 1.0
 */

public class EPSNetCDFFile extends NetcdfFile implements EPSConstants {
  	/**
  	* Construct a netCDF file wrapper object.
   	*
  	* @param file Source netCDF file
  	* @param clobber
  	* @param fill
  	* @param template
   	**/
	public EPSNetCDFFile(java.io.File file, boolean clobber, boolean fill, Schema template) throws IOException {
		super(file, clobber, fill, template);
	}

  	/**
  	* Construct a netCDF file wrapper object.
   	*
  	* @param path Path to source netCDF file
  	* @param ro Read-only flag
   	**/
 	public EPSNetCDFFile(java.lang.String path, boolean ro) throws IOException {
		super(path.trim(), ro);
	}

  	/**
  	* Construct a netCDF file wrapper object.
   	*
  	* @param file Source netCDF file
  	* @param ro Read-only flag
   	**/
 	public EPSNetCDFFile(java.io.File file, boolean ro) throws IOException {
		super(file, ro);
	}

  	/**
  	* Construct a netCDF file wrapper object.
   	*
  	* @param Path to source netCDF file
  	* @param clobber
  	* @param fill
  	* @param template
   	**/
 	public EPSNetCDFFile(java.lang.String path, boolean clobber, boolean fill, Schema template) throws IOException {
		super(path, clobber, fill, template);
	}

  	/**
  	* Construct a netCDF file wrapper object.
   	*
  	* @param Path to source netCDF file
  	* @param clobber
  	* @param fill
  	* @param template
   	**/
 	public EPSNetCDFFile(URL url, boolean ro) throws IOException {
		super(url);
	}

  	/**
  	* Get the number of variables in the netCDF file.
   	*
  	* @return Number of variables in file
   	**/
	public int getNumVariables() {
		VariableIterator vi = this.iterator();
		int i = 0;
		while (vi.hasNext()) {
			Variable v = vi.next();
			i++;
		}
		return i;
	}

  	/**
  	* Get the number of dimensions in the netCDF file.
   	*
  	* @return Number of dimensions in file
   	**/
	public int getNumDimensions() {
		// the array gets filled with ids not the length of the dimension
		DimensionIterator di = this.getDimensions().iterator();
		int ii = 0;
		while (di.hasNext()) {
			Dimension d = di.next();
			ii++;
		}
		return ii;
	}

  	/**
  	* Get the ID code for a named variable.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return ID code of variable
   	**/
	public int getVarID(String name) {
		VariableIterator vi = this.iterator();
		int i = 0;
		while (vi.hasNext()) {
			Variable v = vi.next();
			if (name.equalsIgnoreCase(v.getName()))
				return i;
			i++;
		}
		return -1;
	}

  	/**
  	* Get a Variable from the file by name.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return Variable
   	**/
	public Variable getVariable(String name) {
		VariableIterator vi = this.iterator();
		int i = 0;
		while (vi.hasNext()) {
			Variable v = vi.next();
			if (name.equalsIgnoreCase(v.getName()))
				return v;
			i++;
		}
		return null;
	}

  	/**
  	* Get a type code for a named variable.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return Integer type code for variable.
   	**/
	public int getVariableDataType(String name) {
		Variable v = getVariable(name);
		if (v != null) {
			Class c = v.getComponentType();
			String type = c.getName();
			if (type.equalsIgnoreCase("char")) {
				return EPSConstants.NC_CHAR;
			}
			else if (type.equalsIgnoreCase("short")) {
				return EPSConstants.NC_SHORT;
			}
			else if (type.equalsIgnoreCase("int")) {
				return EPSConstants.NC_LONG;
			}
			else if (type.equalsIgnoreCase("long")) {
				return EPSConstants.NC_LONG;
			}
			else if (type.equalsIgnoreCase("float")) {
				return EPSConstants.NC_FLOAT;
			}
			else if (type.equalsIgnoreCase("double")) {
				return EPSConstants.NC_DOUBLE;
			}
		}
		return -1;
	}

  	/**
  	* Get a named variable's class.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return Class of variable.
   	**/
	public Class getVariableClass(String name) {
		Variable v = getVariable(name);
		if (v != null)
			return v.getComponentType();

		return null;
	}

  	/**
  	* Get the number of attributes for a named variable.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return Number of attributes.
   	**/
	public int getVariableAtrributeCount(String name) {
		Variable v = getVariable(name);
		AttributeSet as = v.getAttributes();
		return as.size();
	}


  	/**
  	* Get a variable's rank.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return Variable rank.
   	**/
	public int getVariableRank(String name) {
		Variable v = getVariable(name);
		if (v != null) {
			return v.getRank();
		}
		return -1;
	}

  	/**
  	* Get an array of dimensions for a named variable.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return Array of lengths for the variable's dimensions.
   	**/
	public int[] getVariableDims(String name) {
		Variable v = getVariable(name);
		if (v != null) {
			int rank = v.getRank();

			int[] ra = new int[rank];
			if (rank == 1)
				return ra;
			else {
				// get a dimension iterator for the variable
				DimensionIterator vdi = v.getDimensionIterator();
				int vc = 0;
				while (vdi.hasNext()) {
					Dimension vd = vdi.next();

					// loop through the axis dimensions
					// the array gets filled with ids not the length of the dimension
					DimensionIterator di = this.getDimensions().iterator();
					int ii = 0;
					while (di.hasNext()) {
						Dimension d = di.next();

						if (vd.getName().equals(d.getName()))
							ra[vc] = ii;// = d.getLength();	// not sure about this one
							ii++;
					}
					vc++;
				}
				return ra;
			}
		}
		return null;
	}

  	/**
  	* Test whether a named attribute is in a named variable.
  	*
  	* @param iname Variable name to look for in file
  	* @param j The attribute's name
   	*
  	* @return true = attribute is present in variable, false attribute not present in variable.
   	**/
	public boolean isAttributeInVariable(String iname, String att) {
		Variable v = getVariable(iname);
		if (v != null) {
			AttributeIterator ai = v.getAttributes().iterator();
			int i = 0;
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = ai.next();
				String name = at.getName();
				if (name.equalsIgnoreCase(att))
					return true;
				i++;
			}
			return false;
		}
		return false;
	}

  	/**
  	* Get an attribute's length for a variable.
  	*
  	* @param iname Variable name to look for in file
  	* @param j The attribute's name
   	*
  	* @return Attribute's length.
   	**/
	public int getVariableAttributeLength(String iname, String att) {
		Variable v = getVariable(iname);
		if (v != null) {
			AttributeIterator ai = v.getAttributes().iterator();
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = ai.next();
				String name = at.getName();
				if (name.equalsIgnoreCase(att))
					return at.getLength();
			}
			return -1;
		}
		return -1;
	}

  	/**
  	* Get an attribute's class for a variable.
  	*
  	* @param iname Variable name to look for in file
  	* @param j The attribute's name
   	*
  	* @return Attributes class.
   	**/
	public String getVariableAttributeClass(String iname, String att) {
		Variable v = getVariable(iname);
		if (v != null) {
			AttributeIterator ai = v.getAttributes().iterator();
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = ai.next();
				String name = at.getName();
				if (name.equalsIgnoreCase(att))
					return at.getComponentType().getName();
			}
			return null;
		}
		return null;
	}

  	/**
  	* Get an attribute name for a variable by it's name.
  	*
  	* @param iname Variable name to look for in file
  	* @param j The attribute's name
   	*
  	* @return Attributes name.
   	**/
	public String getVariableAttributeName(String iname, String att) {
		Variable v = getVariable(iname);
		if (v != null) {
			AttributeIterator ai = v.getAttributes().iterator();
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = ai.next();
				String name = at.getName();
				if (name.equalsIgnoreCase(att)) {
					try {
           				String s = (String)at.getValue();
           				return s;
           			}
           			catch (ClassCastException ex) {
           				// type is numeric--package it up as a string
						String type = at.getComponentType().getName();

						if (type.equalsIgnoreCase("char")) {
							return at.getStringValue();
						}
						else if (type.equalsIgnoreCase("short")) {
							return Short.toString(((Short)at.getNumericValue()).shortValue());
						}
						else if (type.equalsIgnoreCase("int")) {
							return Integer.toString(((Integer)at.getNumericValue()).intValue());
						}
						else if (type.equalsIgnoreCase("long")) {
							return Integer.toString(((Integer)at.getNumericValue()).intValue());
						}
						else if (type.equalsIgnoreCase("float")) {
							return Float.toString(((Float)at.getNumericValue()).floatValue());
						}
						else if (type.equalsIgnoreCase("double")) {
							return Double.toString(((Double)at.getNumericValue()).doubleValue());
						}
           			}
				}
			}
		}
		return new String("");
	}

  	/**
  	* Get an attribute name for a variable by it's index number.
  	*
  	* @param iname Variable name to look for in file
  	* @param j The attribute's index number in variable
   	*
  	* @return Attributes name.
   	**/
	public String getVariableAttributeName(String iname, int j) {
		Variable v = getVariable(iname);
		if (v != null) {
			AttributeIterator ai = v.getAttributes().iterator();
			int i = 0;
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = ai.next();
				if (i == j)
					return at.getName();
				i++;
			}
			return null;
		}
		return null;
	}

  	/**
  	* Get an attribute name for a variable by it's index number.
  	* @deprecated
  	* @see getVariableAttributeName
  	*
  	* @param iname Variable name to look for in file
  	* @param j The attribute's index number in variable
   	*
  	* @return Attributes name.
   	**/
	public String getVariableAttributeNameByIndex(String iname, int j) {
		Variable v = getVariable(iname);
		if (v != null) {
			AttributeIterator ai = v.getAttributes().iterator();
			int i = 0;
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = ai.next();
				if (i == j)
					return at.getName();
				i++;
			}
			return null;
		}
		return null;
	}

  	/**
  	* Get an attribute for a variable by it's index number.
  	*
  	* @param iname Variable name to look for in file
  	* @param j The attribute's index number in variable
   	*
  	* @return The ith Attribute
   	**/
	public Attribute getVariableAttributeByIndex(String iname, int j) {
		Variable v = getVariable(iname);
		if (v != null) {
			AttributeIterator ai = v.getAttributes().iterator();
			int i = 0;
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = ai.next();
				if (i == j)
					return at;
				i++;
			}
			return null;
		}
		return null;
	}
}
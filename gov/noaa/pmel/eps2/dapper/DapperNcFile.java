/*
 * $Id: DapperNcFile.java,v 1.1 2004/06/30 18:22:36 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.eps2.dapper;

import ucar.nc2.*;
import java.util.Vector;
import java.util.Iterator;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.nc2.station.StationDataset;
import gov.noaa.pmel.eps2.*;
import java.util.List;

/**
 * Extends NetcdfFile to provide application required features.
 *
 * @author Donald Denbo
 * @version $Revision: 1.1 $, $Date: 2004/06/30 18:22:36 $
 */
public class DapperNcFile implements NcFile {
  private NcUtil util_;
  private StationDataset dataset_ = null;

  public DapperNcFile(StationDataset dataset) {
    dataset_ = dataset;
    util_ = new NcUtil(this);
    if(Debug.DEBUG)
	System.out.println("Instantiating DapperNcFile = " + dataset.getPathName());
  }

  public Iterator getDimensionVariables() {
    Vector varDim = new Vector();
    Iterator di = getDimensionIterator();
    while(di.hasNext()) {
      ucar.nc2.Dimension dim = (ucar.nc2.Dimension)di.next();
      Variable var = dim.getCoordinateVariable();
      if(var != null) varDim.addElement(var);
    }
    return varDim.iterator();
  }

  public Iterator getNonDimensionVariables() {
    boolean is624 = false;
    Attribute epic_code;
    Vector varDim = new Vector();
    Iterator vi = getVariableIterator();
    while(vi.hasNext()) {
      Variable var = (Variable)vi.next();
      epic_code = var.findAttribute("epic_code");
      if(epic_code != null) {
        is624 = epic_code.getNumericValue().intValue() == 624;
      } else {
        is624 = false;
      }
      if(!is624) {
        if(!var.isCoordinateVariable()) varDim.addElement(var);
      }
    }
    return varDim.iterator();
  }

  public boolean isDODS() {return true;};
  public boolean isFile() {return false;};
  public boolean isHttp() {return false;};

  public String getFileName() {
    String path = getPathName();
    return path.substring(path.lastIndexOf("/")+1);
  }
  //
  // time and array utility methods
  //
  public boolean isVariableTime(Variable var) {
    return util_.isVariableTime(var);
  }

  public Object getArrayValue(Variable var, int index) {
    return util_.getArrayValue(var, index);
  }

  public Object getArray(Variable var, int[] origin, int[] shape) {
    return util_.getArray(var, origin, shape);
  }

  public boolean is624() {
    return util_.is624();
  }

  public int[] getTime2() {
    return util_.getTime2();
  }

  public GeoDate getRefDate() {
    return util_.getRefDate();
  }

  public int getIncrement() {
    return util_.getIncrement();
  }

  /**
   * findAttValueIgnoreCase
   *
   * @param v Variable
   * @param attName String
   * @param defaultValue String
   * @return String
   */
  public String findAttValueIgnoreCase(Variable v, String attName,
                                       String defaultValue) {
    return dataset_.findAttValueIgnoreCase(v, attName, defaultValue);
  }

  /**
   * findDimension
   *
   * @param name String
   * @return Dimension
   */
  public Dimension findDimension(String name) {
    return dataset_.findDimension(name);
  }

  /**
   * findGlobalAttribute
   *
   * @param name String
   * @return Attribute
   */
  public Attribute findGlobalAttribute(String name) {
    return dataset_.findGlobalAttribute(name);
  }

  /**
   * findGlobalAttributeIgnoreCase
   *
   * @param name String
   * @return Attribute
   */
  public Attribute findGlobalAttributeIgnoreCase(String name) {
    return dataset_.findGlobalAttributeIgnoreCase(name);
  }

  /**
   * findVariable
   *
   * @param name String
   * @return Variable
   */
  public Variable findVariable(String name) {
    return dataset_.findVariable(name);
  }

  /**
   * getDimensionIterator
   *
   * @return Iterator
   */
  public Iterator getDimensionIterator() {
    return dataset_.getDimensionIterator();
  }

  /**
   * getGlobalAttributeIterator
   *
   * @return Iterator
   */
  public Iterator getGlobalAttributeIterator() {
    return dataset_.getGlobalAttributeIterator();
  }

  /**
   * getPathName
   *
   * @return String
   */
  public String getPathName() {
    return dataset_.getPathName();
  }

  /**
   * getVariableIterator
   *
   * @return Iterator
   */
  public Iterator getVariableIterator() {
    return dataset_.getVariableIterator();
  }

  /**
   * toString
   *
   * @return String
   */
  public String toString() {
    return dataset_.toString();
  }

  /**
   * toStringDebug
   *
   * @return String
   */
  public String toStringDebug() {
    return dataset_.toStringDebug();
  }
  
   	/**
  	* Get the ID code for a named variable.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return ID code of variable
   	**/
	public int getVarID(String name) {
		Iterator vi = this.getVariableIterator();
		int i = 0;
		while (vi.hasNext()) {
			Variable v = (Variable)vi.next();
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
		Iterator vi = this.getVariableIterator();
		int i = 0;
		while (vi.hasNext()) {
			Variable v = (Variable)vi.next();
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
			Class c = v.getElementType();
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
  	* Get the number of attributes for a named variable.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return Number of attributes.
   	**/
	public int getVariableAtrributeCount(String name) {
		Variable v = getVariable(name);
		List as = v.getAttributes();
		return as.size();
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
			Iterator ai = v.getAttributeIterator();
			int i = 0;
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = (Attribute)ai.next();
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
			Iterator ai = v.getAttributeIterator();
			int i = 0;
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = (Attribute)ai.next();
				if (i == j)
					return at.getName();
				i++;
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
			Iterator ai = v.getAttributeIterator();
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = (Attribute)ai.next();
				String name = at.getName();
				if (name.equalsIgnoreCase(att)) {
					try {
           				String s = (String)at.getValue();
           				return s;
           			}
           			catch (ClassCastException ex) {
           				// type is numeric--package it up as a string
						String type = at.getClass().getName();

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
			Iterator ai = v.getAttributeIterator();
			int i = 0;
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = (Attribute)ai.next();
				if (i == j)
					return at;
				i++;
			}
			return null;
		}
		return null;
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
			Iterator ai = v.getAttributeIterator();
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = (Attribute)ai.next();
				String name = at.getName();
				if (name.equalsIgnoreCase(att))
					return at.getClass().getName();
			}
			return null;
		}
		return null;
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
			Iterator ai = v.getAttributeIterator();
			while (ai.hasNext()) {
				// get the attribute name
				Attribute at = (Attribute)ai.next();
				String name = at.getName();
				if (name.equalsIgnoreCase(att))
					return at.getLength();
			}
			return -1;
		}
		return -1;
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
				List vdi = v.getDimensions();
				Iterator itor1 = vdi.iterator();
				int vc = 0;
				while (itor1.hasNext()) {
					Dimension vd = (Dimension)itor1.next();

					// loop through the axis dimensions
					// the array gets filled with ids not the length of the dimension
					Iterator di = this.getDimensionIterator();
					int ii = 0;
					while (di.hasNext()) {
						Dimension d = (Dimension)di.next();

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
  	* Get a named variable's class.
  	*
  	* @param name Variable name to look for in file
   	*
  	* @return Class of variable.
   	**/
	public Class getVariableClass(String name) {
		Variable v = getVariable(name);
		if (v != null)
			return v.getClass();

		return null;
	}
}

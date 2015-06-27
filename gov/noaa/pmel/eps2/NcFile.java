/**
 *  $Id: NcFile.java,v 1.1 2003/06/04 13:03:08 oz Exp $
 */
package gov.noaa.pmel.eps2;

import ucar.nc2.*;

import java.util.Iterator;

import gov.noaa.pmel.util.GeoDate;

/**
 * Extends NetcdfFile to provide application required features.
 *
 * @author Donald Denbo
 * @version $Revision: 1.1 $, $Date: 2003/06/04 13:03:08 $
 */
public interface NcFile {

  public Iterator getDimensionVariables();
  public Iterator getNonDimensionVariables();

  public String findAttValueIgnoreCase(Variable v,
                                       String attName,
                                       String defaultValue);
  public Dimension findDimension(String name);
  public Attribute findGlobalAttribute(String name);
  public Attribute findGlobalAttributeIgnoreCase(String name);
  public Variable findVariable(String name);
  public Iterator getDimensionIterator();
  public Iterator getGlobalAttributeIterator();
  public String getPathName();
  public Iterator getVariableIterator();
  public String toString();
  public String toStringDebug();

  public boolean isFile();
  public boolean isDODS();
  public boolean isHttp();

  public String getFileName();
  //
  // time and array utility methods
  //
  public boolean isVariableTime(Variable var);
  public Object getArrayValue(Variable var, int index);
  public Object getArray(Variable var, int[] origin, int[] shape);
  public boolean is624();
  public int[] getTime2();
  public GeoDate getRefDate();
  public int getIncrement();
}

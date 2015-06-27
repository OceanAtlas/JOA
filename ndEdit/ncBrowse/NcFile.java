/*
 * $Id: NcFile.java,v 1.2 2005/02/15 18:31:11 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit.ncBrowse;

import ucar.nc2.*;
import java.util.Iterator;
import gov.noaa.pmel.util.GeoDate;

/**
 * Extends NetcdfFile to provide application required features.
 *
 * @author Donald Denbo
 * @version $Revision: 1.2 $, $Date: 2005/02/15 18:31:11 $
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
  // time and array utility methods
  public boolean isVariableTime(Variable var);
  public Object getArrayValue(Variable var, int index);
  public Object getArray(Variable var, int[] origin, int[] shape);
  public boolean is624();
  public int[] getTime2();
  public GeoDate getRefDate();
  public int getIncrement();
}

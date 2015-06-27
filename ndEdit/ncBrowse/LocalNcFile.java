/*
 * $Id: LocalNcFile.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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

import java.util.Vector;
import java.util.Iterator;
//import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import gov.noaa.pmel.util.GeoDate;

/**
 * Extends NetcdfFile to provide application required features.
 *
 * @author Donald Denbo
 * @version $Revision: 1.2 $, $Date: 2005/02/15 18:31:11 $
 */
public class LocalNcFile extends NetcdfFile implements NcFile {
  private NcUtil util_;

  public LocalNcFile(File file) throws IOException {
    super(file.getAbsolutePath());
    util_ = new NcUtil(this);
  }

  public LocalNcFile(String path) throws IOException {
    super(path);
    util_ = new NcUtil(this);
  }

  /**
   * added by dwd to support URL files
   */
  public LocalNcFile(URL url) throws IOException {
    super(url);
    util_ = new NcUtil(this);
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

  public boolean isDODS() {return false;};

  public boolean isFile() {
    return !isHttp();
  }

  public boolean isHttp() {
    return getPathName().startsWith("http:");
  }

  public String getFileName() {
    String path = getPathName();
    if(isHttp()) {
      return path.substring(path.lastIndexOf("/")+1);
    } else {
      return path.substring(path.lastIndexOf(System.getProperty("file.separator","/")) + 1);
    }
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

}

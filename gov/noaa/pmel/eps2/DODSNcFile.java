/**
 *  $Id: DODSNcFile.java,v 1.2 2004/02/24 05:01:43 oz Exp $
 */
package gov.noaa.pmel.eps2;

import ucar.nc2.*;
import ucar.nc2.dods.DODSNetcdfFile;
import dods.dap.DODSException;

import java.util.Vector;
import java.util.Iterator;
//import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import gov.noaa.pmel.util.GeoDate;

/**
 * Extends NetcdfFile to provide application required features.
 *
 * @author Donald Denbo
 * @version $Revision: 1.2 $, $Date: 2004/02/24 05:01:43 $
 */
public class DODSNcFile extends DODSNetcdfFile implements NcFile {
  private NcUtil util_;

  public DODSNcFile(String path)
    throws IOException, DODSException, MalformedURLException {
    super(path);
    util_ = new NcUtil(this);
    //System.out.println("Instantiating DODSNcFile = " + path);
  }

  public Iterator getDimensionVariables() {
    Vector varDim = new Vector();
    Iterator di = getDimensionIterator();
    while (di.hasNext()) {
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
}

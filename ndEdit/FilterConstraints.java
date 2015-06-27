  /*
 * $Id: FilterConstraints.java,v 1.5 2005/02/15 18:31:09 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;
//
// Contains the set of filter constraints that's currently active.
//
 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class FilterConstraints {
  public double[] latRange = new double[2];
  public double[] lonRange = new double[2];
  public double[] depthRange = new double[2];
  public double[] timeRange = new double[2];
  public String[] standardVariables;
  public String[] additionalFields;

  // ---------------------------------------------------------------------
  //
  public FilterConstraints(double[] latRange,
			double[] lonRange,
			double[] depthRange,
			double[] timeRange,
			String[] standardVariables,
			String[] additionalFields) {
     this.latRange = latRange;
     this.lonRange = lonRange;
     this.depthRange = depthRange;
     this.timeRange = timeRange;
     this.standardVariables = standardVariables;
     this.additionalFields = additionalFields;
  }
  
  public double[] getMinMaxLon() {
  	return lonRange;
  }
  
  public double[] getMinMaxDepth() {
  	return depthRange;
  }
  
  public double[] getMinMaxTime() {
  	return timeRange;
  }
  
  public double[] getMinMaxLat() {
  	return latRange;
  }

  // ---------------------------------------------------------------------
  //
  public String toString() {
      StringBuffer str = new StringBuffer();
      str.append(" Lat: " + latRange[0] + "       " + latRange[1] + "\n");
      str.append(" Lon: " + lonRange[0] + "       " + lonRange[1] + "\n");
      str.append(" Depth: " + depthRange[0] + "       " + depthRange[1] + "\n");
      str.append(" Time: " + timeRange[0] + "       " + timeRange[1] + "\n");
      return str.toString();
  }
}

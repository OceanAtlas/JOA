/*
 * $Id: Pointer.java,v 1.4 2005/02/15 18:31:10 oz Exp $
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
/**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 *
 * @note The data object containing the master data upon which filtering 
  * will be performed.  It contains "standard" variables, latRange, 
  * lonRange, timeRange, depthRange, and variables.  Also could 
  * contain a non-standard set of add-on variables.
  */
public class Pointer {
  private double[] latRange = new double[2];
  private double[] lonRange = new double[2];
  private double[] depthRange = new double[2];
  private long[] timeRange = new long[2];
  private String[] standardVariables;
  private String URD;
  private String[] additionalFields;

  public Pointer(	double latStart, 
			double latStop,
			double lonStart,
			double lonStop,
			double depthStart,
			double depthStop,
			long timeStart,
			long timeStop,
			String URD) {

     latRange[0] = latStart;
     latRange[1] = latStop;
     lonRange[0] = lonStart;
     lonRange[1] = lonStop;
     depthRange[0] = depthStart;
     depthRange[1] = depthStop;
     timeRange[0] = timeStart;
     timeRange[1] = timeStop;
     this.URD = URD;
  }
}

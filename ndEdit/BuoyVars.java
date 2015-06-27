/*
 * $Id: BuoyVars.java,v 1.5 2005/02/15 18:31:08 oz Exp $
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

import java.util.Vector;
import java.text.DecimalFormat;
import java.io.IOException;

import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import ucar.netcdf.DimensionIterator;
import ucar.multiarray.MultiArray;
import gov.noaa.pmel.util.GeoDate;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */

public class BuoyVars {
  String ident_;
  String lat_;
  String lon_;
  Vector vars_;
  double lat1, lat2;
  double lon1, lon2;
  double dep1, dep2;
  long tim1, tim2;
    
  public BuoyVars(String file, String ident) {
    Netcdf nc = null;
    VariableIterator vi;
    Variable variable;
    ucar.netcdf.Dimension dim;
    DimensionIterator di;
    double lat = 0.0f;
    double lon = 0.0f;
    double dep = 0.0f;
    long tim = 0;
    boolean first = true;
    ident_ = ident;
    lat_ = null;
    lon_ = null;
    vars_ = new Vector(10,10);
    boolean haveLatRange = false;
    //
    // open file, get lat, lon and format
    // read variable list and units
    //
    System.out.println("Attempt to open file: " + file);
    try {
      nc = new NetcdfFile(file, true);
      System.out.println("Opened file: " + file);
    } catch (Exception e) {System.out.println(e + ": new NetcdfFile");}

    for(vi = nc.iterator(); vi.hasNext();) {
      variable = vi.next();
      dim = variable.getDimensionIterator().next();
      if(!(dim.getName().equals(variable.getName()) ||
	   variable.getName().equals("time2"))) {
	String str = variable.getName() + ";" + 
	  stripNull(variable.getAttribute("units").getStringValue());
	vars_.addElement(str);
	if(first) {
	  //
	  // first variable get lat, lon!
	  //
	  first = false;
	  for(di = variable.getDimensionIterator(); di.hasNext();) {
	    dim = di.next();
	    if(dim.getName().equals("latitude")) {
	      double[] latVal = null;
	      Variable latVar = nc.get("latitude");
	      try {
		MultiArray latMa = latVar.copyout(new int[latVar.getRank()],
						  latVar.getLengths());
		latVal = (double[])latMa.toArray();
	      } catch (IOException e) {}
	      lat1 = latVal[0];
	      lat2 = latVal[0];
	      // 
	      // Get min/max if lat is a range of values
	      //
	      for (int k = 0; k < latVal.length; k++) {
		 lat1 = Math.min(lat1, latVal[k]);
		 lat2 = Math.max(lat2, latVal[k]);
	      }
	    } else if(dim.getName().equals("longitude")) {
	      //
	      // test if longitude is degree_east or degree_west
	      //
	      boolean deg_east = true;
	      double[] lonVal = null;
	      Variable lonVar = nc.get("longitude");
	      try {
		MultiArray lonMa = lonVar.copyout(new int[lonVar.getRank()],
						  lonVar.getLengths());
		lonVal = (double[])lonMa.toArray();
	      } catch (IOException e) {}
	      //	      String units = lonVar.getAttribute("units").getStringValue();
	      int ecode =
		((Integer)lonVar.getAttribute("epic_code").getNumericValue()).intValue();
	      if(ecode == 501) deg_east=false;
	      //	      System.out.println("longitude: " + units + ", epic_code = " + ecode);
	      if(!deg_east) {
		for(int i=0; i < lonVal.length; i++) {
		  lonVal[i] = -lonVal[i];
		}
	      }
	      lon1 = lonVal[0];
	      lon2 = lonVal[0];
	      // 
	      // Get min/max if lon is a range of values
	      //
	      for (int k = 0; k < lonVal.length; k++) {
		 lon1 = Math.min(lon1, lonVal[k]);
		 lon2 = Math.max(lon2, lonVal[k]);
	      }
	    }
	    else if(dim.getName().equals("depth")) {
	      double[] depVal = null;
	      Variable depVar = nc.get("depth");
	      try {
		MultiArray depMa = depVar.copyout(new int[depVar.getRank()],
						  depVar.getLengths());
		depVal = (double[])depMa.toArray();
	      } catch (IOException e) {}
	      dep1 = depVal[0];
	      dep2 = depVal[0];
	      // 
	      // Get min/max if lon is a range of values
	      //
	      for (int k = 0; k < depVal.length; k++) {
		 dep1 = Math.min(dep1, depVal[k]);
		 dep2 = Math.max(dep2, depVal[k]);
	      }
	    }
            else if(dim.getName().equals("time")) {
	      int[] timeVal = null;
	      int[] time2Val = null;
	      Variable timeVar = nc.get("time");
	      Variable time2Var = nc.get("time2");
	      try {
	        MultiArray timeMa = timeVar.copyout(new int[timeVar.getRank()],
					      timeVar.getLengths());
	        MultiArray time2Ma = time2Var.copyout(new int[time2Var.getRank()],
						time2Var.getLengths());
	        timeVal = (int[])timeMa.toArray();
	        time2Val = (int[])time2Ma.toArray();

	        long dd =  (new GeoDate(timeVal[0], time2Val[0])).getTime();
	        tim1 = dd;
	        tim2 = dd;
	        // 
	        // Get min/max if time is a range of values
	        //
	        for (int k = 0; k < timeVal.length; k++) {
	           long dt1 = (new GeoDate(timeVal[k], time2Val[k])).getTime();
		   tim1 = Math.min(tim1, dt1);
		   tim2 = Math.max(tim2, dt1);
	        }
	      } catch (IOException e) {}
            }
	  }
	}
      }
    }
    DecimalFormat dfLon = new DecimalFormat("###.##;###.##W");
    DecimalFormat dfLat = new DecimalFormat("##.##N;##.##S");
    double mlon = (lon + 360.0f) % 360.0f;
    dfLon.setPositiveSuffix("E");
    if(mlon > 180.0f) {
      mlon = mlon - 360.0f;
    }
    lat_ = dfLat.format((double)lat1);
    lon_ = dfLon.format((double)mlon);
    try {
      ((NetcdfFile)nc).close();
    } catch (IOException e) {System.out.println(e + ": close netcdf file");}

  }
  public String stripNull(String in) {
    if(in.charAt(in.length() - 1) == 0) {
      return in.substring(0, in.length() - 1);
    }
    return in;
  }
  
  public String getIdent() {
    return ident_;
  }
  public String getLat() {
    return lat_;
  }
  public String getLon() {
    return lon_;
  }
  public Vector getVars() {
    return vars_;
  }
  public double getLat1() {
    return lat1;
  }
  public double getLat2() {
    return lat2;
  }
  public double getLon1() {
    return lon1;
  }
  public double getLon2() {
    return lon2;
  }
  public double getDepth1() {
    return dep1;
  }
  public double getDepth2() {
    return dep2;
  }
  public long getTime1() {
    return tim1;
  }
  public long getTime2() {
    return tim2;
  }
}

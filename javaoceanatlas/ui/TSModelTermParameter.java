/**
 * 
 */
package javaoceanatlas.ui;

/**
 * @author oz
 *
 */
public enum TSModelTermParameter {
	TEMPERATURE("temp", 0),
	SQTEMPERATURE("sqtemp", 1),
	LONGITUDE("lon", 2),
	LATITUDE("lat", 3),
	LONxLAT("lon*lat", 4);

  public final String mParamType;
  public final int mOrd;

  TSModelTermParameter(String pt, int ord) {
  	mParamType = pt;
  	mOrd = ord;
  }
  
  public static TSModelTermParameter getParam(String ps) {
  	if (ps.equalsIgnoreCase("temp")) {
  		return TEMPERATURE;
  	}
  	else if (ps.equalsIgnoreCase("sqtemp")) {
  		return SQTEMPERATURE;
  	}
  	else if (ps.equalsIgnoreCase("lon")) {
  		return LONGITUDE;
  	}
  	else if (ps.equalsIgnoreCase("lat")) {
  		return LATITUDE;
  	}
  	else if (ps.equalsIgnoreCase("lon*lat")) {
  		return LONxLAT;
  	}
  	return null;
  }
  
  public String toString() {
    return mParamType;
  }
  
  public int getOrd() {
  	return mOrd;
  }
}

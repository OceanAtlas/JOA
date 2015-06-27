/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public enum TemperatureScale {
	UNKNOWN("Unknown", new UnityScaler()),
	T68("IPTS68)", new UnityScaler()),
	ITS90("ITS90", new ITS90Scaler());

	private final String mScaleName;
	private final ValueScaler mScaler;

	TemperatureScale(String scale, ValueScaler scaler) {
		this.mScaleName = scale;
		this.mScaler = scaler;
	}
	
	public static ValueScaler scalerFromString(String inScale) {			
    for (TemperatureScale scale : values()) {		
      if (scale.mScaleName.equalsIgnoreCase(inScale)) {
        return scale.mScaler;
      }
    }
		return new UnityScaler();
	}
}
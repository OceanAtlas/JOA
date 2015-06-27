/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public enum PreferPressureParameterRule {
	PREFER_PRESSURE_PARAMETER("Prefer pressure parameter if present"),
	USE_NODC_DEPTH_PARAMETER("Use NODC Depth");

	private final String mHRString;

	PreferPressureParameterRule(String hr) {
		this.mHRString = hr;
	}
  
	 public static PreferPressureParameterRule fromValue(String value) {  
	   if (value != null) {  
	     for (PreferPressureParameterRule rule : values()) {  
	       if (rule.name().equals(value)) {  
	         return rule;  
	       }  
	     }  
	   }
	   return PREFER_PRESSURE_PARAMETER;
	 }
}

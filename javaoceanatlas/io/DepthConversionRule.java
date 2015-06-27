/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */

public enum DepthConversionRule {
	CONVERT_DEPTH_TO_PRESSURE("convert depth in all units to pressure"),
	USE_DEPTH_IN_METERS("Use Depth in meters");

	private final String mHRString;

	DepthConversionRule(String hr) {
		mHRString = hr;
	}
  
	 public static DepthConversionRule fromValue(String value) {  
	   if (value != null) {  
	     for (DepthConversionRule rule : values()) {  
	       if (rule.name().equals(value)) {  
	         return rule;  
	       }  
	     }  
	   }
	   return CONVERT_DEPTH_TO_PRESSURE;
	 }
}

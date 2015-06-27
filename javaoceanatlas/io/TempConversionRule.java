/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */

public enum TempConversionRule {
	ITS90_TO_IPTS68("Convert Temperatures from ITS90 to IPTS68 when possible"),
	NO_TEMP_CONVERSION("None");

	private final String mHRString;

	TempConversionRule(String hr) {
		mHRString = hr;
	}
  
	 public static TempConversionRule fromValue(String value) {  
	   if (value != null) {  
	     for (TempConversionRule rule : values()) {  
	       if (rule.name().equals(value)) {  
	         return rule;  
	       }  
	     }  
	   }
	   return ITS90_TO_IPTS68;
	 }
}
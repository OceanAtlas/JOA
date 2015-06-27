/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public enum DestinationQCRule {
	WOCE("WOCE"),
	WMO("WMO IGOSS");

	private final String mHRString;

	DestinationQCRule(String hReadable) {
		mHRString = hReadable;
	}
  
	 public static DestinationQCRule fromValue(String value) {  
	   if (value != null) {  
	     for (DestinationQCRule rule : values()) {  
	       if (rule.name().equals(value)) {  
	         return rule;  
	       }  
	     }  
	   }
	   return WOCE;
	 }
}
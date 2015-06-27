/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public enum SectionIDRule {
	ORIG_CRUISE_ID("Originators Cruise ID"),
	NODC_CRUISE_ID("NODC Cruise ID");

	private final String mHRString;

	SectionIDRule(String hr) {
		mHRString = hr;
	}
  
	 public static SectionIDRule fromValue(String value) {  
	   if (value != null) {  
	     for (SectionIDRule rule : values()) {  
	       if (rule.name().equals(value)) {  
	         return rule;  
	       }  
	     }  
	   }
	   return ORIG_CRUISE_ID;
	 }
}
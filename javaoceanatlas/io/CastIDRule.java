/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public enum CastIDRule {
	ORIG_STN_ID("Originators Station ID"),
	WOD_UNIQUE("CAST");

	private final String mHRString;

	CastIDRule(String hr) {
		mHRString = hr;
	}
  
	 public static CastIDRule fromValue(String value) {  
	   if (value != null) {  
	     for (CastIDRule rule : values()) {  
	       if (rule.name().equals(value)) {  
	         return rule;  
	       }  
	     }  
	   }
	   return WOD_UNIQUE;
	 }
}


/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public enum CastNumberRule {
	CAST_TOW_ONLY("Only Accept Cast/Tow"),
	JOA_SUBSTITUTION("JOA Substitues for missing CAST/TOW");

	private final String mHRString;

	CastNumberRule(String hr) {
		mHRString = hr;
	}
  
	 public static CastNumberRule fromValue(String value) {  
	   if (value != null) {  
	     for (CastNumberRule rule : values()) {  
	       if (rule.name().equals(value)) {  
	         return rule;  
	       }  
	     }  
	   }
	   return JOA_SUBSTITUTION;
	 }
}
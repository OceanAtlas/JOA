/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public enum ConvertParameterNamesRule {
	CONVERT_TO_JOA_LEXICON("Convert parameter names to JOA Lexicon"),
	KEEP_WOD_PARAMETER_NAMES("Use WOD parameter names");

	private final String mHRString;

	ConvertParameterNamesRule(String hr) {
		mHRString = hr;
	}
  
	 public static ConvertParameterNamesRule fromValue(String value) {  
	   if (value != null) {  
	     for (ConvertParameterNamesRule rule : values()) {  
	       if (rule.name().equals(value)) {  
	         return rule;  
	       }  
	     }  
	   }
	   return CONVERT_TO_JOA_LEXICON;
	 }
}
/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public enum QCConversionRule {
	READ_ORIG_QC_FLAGS("Read originators qc flags if present"),
	IGNORE_QC_FLAGS("Ignore any quality flags");

	private final String mHRString;

	QCConversionRule(String hr) {
		mHRString = hr;
	}
  
	 public static QCConversionRule fromValue(String value) {  
	   if (value != null) {  
	     for (QCConversionRule rule : values()) {  
	       if (rule.name().equals(value)) {  
	         return rule;  
	       }  
	     }  
	   }
	   return READ_ORIG_QC_FLAGS;
	 }
}

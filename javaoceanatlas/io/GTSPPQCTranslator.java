/**
 * 
 */
package javaoceanatlas.io;


/**
 * @author oz
 *
 */
public class GTSPPQCTranslator implements QCTranslator {

	/* (non-Javadoc)
	 * @see javaoceanatlas.io.QCTranslator#translate(int, javaoceanatlas.io.QCConvention)
	 */
	public int translate(int inQC, WODQCStandard destConvention) {
			/*
				The WMO IGOSS observation quality codes are:
				0 No quality control yet assigned to this element
				1 The element appears to be correct
				2 The element is probably good
				3 The element is probably bad
				4 The element appears erroneous
				5 The element has been changed
				6 to 8 Reserved for future use
				9 The element is missing
				
				The WOCE Observation QC Codes are:
				1 Sample for this measurement was drawn from water bottle but analysis not
					received. Note that if water is drawn for any measurement from a water bottle,
					the quality flag for that parameter must be set equal to 1 initially to ensure that all
					water samples are accounted for.
				2 Acceptable measurement.
				3 Questionable measurement.
				4 Bad measurement.
				5 Not reported.
				6 Mean of replicate measurements (Number of replicates should be specified in
					the Ñ.DOC file and replicate data tabulated).
				7 Manual chromatographic peak measurement.
				8 Irregular digital chromatographic peak integration.
				9 Sample not drawn for this measurement from this bottle.

			 GTSPP QC Codes: 
			 1 - good quality 
			 2 - ÒprobablyÓ good quality 
			 3 - ÒprobablyÓ bad quality 
			 4 - bad quality 
			 5 - data changed
			 */

		if (destConvention == WODQCStandard.WMO) {
			if (inQC == 1) {	// good quality -> The element appears to be correct
				return 1;
			}
			else if (inQC == 2) {	//ÒprobablyÓ good quality -> The element is probably good
				return 2;
			}
			else if (inQC == 3) {	// ÒprobablyÓ bad quality -> The element is probably bad
				return 3;
			}
			else if (inQC == 4) { // bad quality -> The element is probably bad
				return 3; 
			}
			else if (inQC == 5) { // data changed -> The element has been changed
				return 5; 
			}
			return inQC;			
		}
		else if (destConvention == WODQCStandard.WOCE) {
			if (inQC == 1) {	// good quality -> Acceptable measurement
				return 2;
			}
			else if (inQC == 2) {	//ÒprobablyÓ good quality -> Acceptable measurement
				return 2;
			}
			else if (inQC == 3) {	// ÒprobablyÓ bad quality -> Questionable measurement
				return 3;
			}
			else if (inQC == 4) { // bad quality -> Bad measurement
				return 4; 
			}
			else if (inQC == 5) { // data changed -> Not reported
				return 5; 
			}
			return inQC;			
		}
		return inQC;
	}
}

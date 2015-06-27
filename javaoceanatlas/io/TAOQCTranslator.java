/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public class TAOQCTranslator implements QCTranslator {
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

	 	TAO QC Codes: 
		 	1 - highest quality 
		 	2 - default quality 
		 	3 - adjusted data 
		 	4 - lower quality 
		 	5 - sensor failed
	*/
		public int translate(int inQC, WODQCStandard destConvention) {
			if (destConvention == WODQCStandard.WMO) {
				if (inQC == 1) { // highest quality -> The element appears to be correct
					return 1;
				}
				else if (inQC == 2) { // default quality -> The element appears to be correct
					return 1;
				}
				if (inQC == 3) { // adjusted data -> The element has been changed
					return 5;
				}
				else if (inQC == 4) { // lower quality -> The element is probably good
					return 3;
				}
				else if (inQC == 5) { // sensor failed -> The element is probably bad
					return 3;
				}
				return 0; // -> No quality control yet assigned to this element
			}
			else if (destConvention == WODQCStandard.WOCE) {
				if (inQC == 1) { // highest quality -> Acceptable measurement
					return 2;
				}
				else if (inQC == 2) { // default quality -> -> Acceptable measurement
					return 2;
				}
				if (inQC == 3) { // adjusted data -> Acceptable measurement
					return 2;
				}
				else if (inQC == 4) { // lower quality -> Questionable measurement
					return 3;
				}
				else if (inQC == 5) { // sensor failed -> Bad measurement
					return 4;
				}
				return 5; // -> Not reported
			}
			return inQC;
		}
}

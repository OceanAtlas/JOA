/**
 * 
 */
package javaoceanatlas.io;


/**
 * @author oz
 *
 */
public class WilkesQCTranslator implements QCTranslator {
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

 	Wilkes QC Codes: 
	1 - Improbable value 
*/
	public int translate(int inQC, WODQCStandard destConvention) {
		if (destConvention == WODQCStandard.WMO) {
			if (inQC == 1) { // Improbable value -> The element is probably bad
				return 3;
			}
			return 0; // -> No quality control yet assigned to this element
		}
		else if (destConvention == WODQCStandard.WOCE) {
			if (inQC == 1) { // Improbable value -> Questionable measurement
				return 3;
			}
			return 5; // -> No quality control yet assigned to this element
		}
		return inQC;
	}
}

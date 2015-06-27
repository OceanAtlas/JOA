/**
 * 
 */
package javaoceanatlas.io;


/**
 * @author oz
 *
 */
public class CalCOFIQCTranslator implements QCTranslator {

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

	 	CalCOFI QC Codes: 
		6 - data okay, but from a CTD device 
		8 - suspect data
		2 - data okay	 
	*/

		if (destConvention == WODQCStandard.WMO) {
			if (inQC == 2) { // data okay -> The element appears to be correct
				return 1;
			}
			else if (inQC == 6) { // data okay, but from a CTD device -> The element appears to be correct
				return 1;
			}
			else if (inQC == 8) { // suspect data -> The element is probably bad
				return 3;
			}
			return 0; // -> No quality control yet assigned to this element
		}
		else if (destConvention == WODQCStandard.WOCE) {
			if (inQC == 2) { // data okay -> Acceptable measurement
				return 2;
			}
			else if (inQC == 6) { // data okay, but from a CTD device -> Acceptable measurement
				return 2;
			}
			else if (inQC == 8) { // suspect data -> Questionable measurement
				return 3;
			}
			return 5; // -> No quality control yet assigned to this element
		}
		return inQC;
	}
}

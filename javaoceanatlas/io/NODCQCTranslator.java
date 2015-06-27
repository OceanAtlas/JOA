/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public class NODCQCTranslator implements QCTranslator {

	/* (non-Javadoc)
	 * @see javaoceanatlas.io.QCTranslator#translate(int, javaoceanatlas.io.WODQCStandard)
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

	 	NODC "Observed Level" QC Codes: 
			0 accepted value
		  1 range outlier ( outside of broad range check )
		  2 failed inversion check
			3 failed gradient check
			4 observed level ÒbullseyeÓ flag and zero gradient check
			5 combined gradient and inversion checks
			6 failed range and inversion checks
			7 failed range and gradient checks
			8 failed range and questionable data checks
			9 failed range and combined gradient and inversion checks		
		*/
		
		if (destConvention == WODQCStandard.WMO) {
			if (inQC == 0) { // accepted value -> The element appears to be correct
				return 1;
			}
			return 3; // -> The element is probably bad
		}
		else if (destConvention == WODQCStandard.WOCE) {
			if (inQC == 0) { // accepted value -> Acceptable measurement
				return 2;
			}
			return 3; // -> Questionable measurement.
		}
		return inQC;
	}
}

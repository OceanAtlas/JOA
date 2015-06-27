/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 * 
 */
public class WOCEQCTranslator implements QCTranslator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javaoceanatlas.io.QCTranslator#translate(int,
	 * javaoceanatlas.io.QCConvention)
	 */
	public int translate(int inQC, WODQCStandard destConvention) {
		if (destConvention == WODQCStandard.WMO) {
			if (inQC == 1) {
				return 0;
			}
			else if (inQC == 2) {
				return 1;
			}
			else if (inQC == 3) {
				return 2;
			}
			else if (inQC == 4) {
				return 4;
			}
			else if (inQC == 5) {
				return 0;
			}
			else if (inQC == 6) {
				return 2;
			}
			else if (inQC == 7) {
				return 2;
			}
			else if (inQC == 8) {
				return 2;
			}
			else if (inQC == 9) { return 9; }
		}
		else if (destConvention == WODQCStandard.WOCE){
			return inQC;
		}
		
		return inQC;
	}
}

/**
 * 
 */
package javaoceanatlas.io;


/**
 * @author oz
 * 
 */
public class WMOQCTranslator implements QCTranslator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javaoceanatlas.io.QCTranslator#translate(int,
	 * javaoceanatlas.io.QCConvention)
	 */
	public int translate(int inQC, WODQCStandard destConvention) {
		if (destConvention == WODQCStandard.WOCE) {
			if (inQC == 0) {
				return 1;
			}
			else if (inQC == 1) {
				return 2;
			}
			else if (inQC == 2) {
				return 3;
			}
			else if (inQC == 4) {
				return 4;
			}
			else if (inQC == 9) { 
				return 9; 
			}
			return inQC;
		}
		else if (destConvention == WODQCStandard.WMO) {
			return inQC;
		}
		
		return inQC;
	}

}

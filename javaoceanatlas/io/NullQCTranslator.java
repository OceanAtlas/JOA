/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public class NullQCTranslator implements QCTranslator {

	/* (non-Javadoc)
	 * @see javaoceanatlas.io.QCTranslator#translate(int, javaoceanatlas.io.WODQCStandard)
	 */
	public int translate(int inQC, WODQCStandard destConvention) {
		return inQC;
	}

}

/**
 * 
 */
package javaoceanatlas.io;

/**
 * @author oz
 *
 */
public class ITS90Scaler implements ValueScaler {

	/* (non-Javadoc)
	 * @see javaoceanatlas.io.ValueScaler#scale(double)
	 */
	public float scale(float inVal) {
		return (float)(inVal * 1.0024);
	}
}

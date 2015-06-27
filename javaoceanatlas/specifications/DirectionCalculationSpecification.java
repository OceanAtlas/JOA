/**
 * 
 */
package javaoceanatlas.specifications;

/**
 * @author oz
 *
 */
public class DirectionCalculationSpecification {
	private String mName;
	private String mUComp;
	private String mVComp;
	
	public DirectionCalculationSpecification(String name, String uC, String vC) {
		setName(name);
		setUComp(uC);
		setVComp(vC);
	}

	public void setName(String mName) {
	  this.mName = mName;
  }

	public String getName() {
	  return mName;
  }

	public void setUComp(String mUComp) {
	  this.mUComp = mUComp;
  }

	public String getUComp() {
	  return mUComp;
  }

	public void setVComp(String mVComp) {
	  this.mVComp = mVComp;
  }

	public String getVComp() {
	  return mVComp;
  }
}

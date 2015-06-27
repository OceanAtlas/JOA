/**
 * 
 */
package javaoceanatlas.specifications;

/**
 * @author oz
 * 
 */
public class SpeedCalculationSpecification {
	private String mName;
	private String mUnits;
	private String mUComp;
	private String mVComp;
	
	public SpeedCalculationSpecification(String name, String units, String uC, String vC) {
		setName(name);
		setUnits(units);
		setUComp(uC);
		setVComp(vC);
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

	public void setName(String mName) {
	  this.mName = mName;
  }

	public String getName() {
	  return mName;
  }

	public void setUnits(String mUnits) {
	  this.mUnits = mUnits;
  }

	public String getUnits() {
	  return mUnits;
  }
}

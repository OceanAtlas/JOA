package gov.noaa.pmel.eps2;

/**
 * <code>CompositeTSInfo</code> 
 * Composite metadata for a time series
 * @see DBase
 *
 * @author oz
 * @version 1.0
 */
public class CompositeTSInfo {
	/**
	* Composite number
	*/
	protected int mNumber;
	/**
	* Position Constant
	*/
	protected int mPosition;
	/**
	* Depth Constant
	*/
	protected int mDepth;
	
  	/**
   	* Construct a new <code>CompositeTSInfo</code>.
   	*/
	public CompositeTSInfo() {
		// zero arg ctor
	}
	
  	/**
   	* Construct a new <code>CompositeTSInfo</code> from values.
   	* @param num composite number
   	* @param pos position constant
   	* @param dep depth constant
   	*/
	public CompositeTSInfo(int num, int pos, int dep) {
		mNumber = num;
		mPosition = pos;
		mDepth = dep;
	}
	
  	/**
   	* Copy constructor.
   	* @param tsInfo CompositeTSInfo object
   	*/
	public CompositeTSInfo(CompositeTSInfo tsInfo) {
		mNumber = tsInfo.mNumber;
		mPosition = tsInfo.mPosition;
		mDepth = tsInfo.mDepth;
	}
	
  	/**
   	* Set the composite number.
   	* @param num composite number
   	*/
	public void setNumber(int num) {
		mNumber = num;
	}
	
  	/**
   	* Set the position constant.
   	* @param pos position constant
   	*/
	public void setPosition(int pos) {
		mPosition = pos;
	}
	
  	/**
   	* Set the depth constant.
   	* @param dep depth constant
   	*/
	public void setDepth(int dep) {
		mDepth = dep;
	}
	
  	/**
  	* Get the composite number.
   	*
  	* @return composite number
   	**/
	public int getNumber() {
		return mNumber;
	}
	
  	/**
  	* Get the position constant.
   	*
  	* @return position constant
   	**/
	public int getPosition() {
		return mPosition;
	}
	
  	/**
  	* Get the depth constant.
   	*
  	* @return depth constant
   	**/
	public int getDepth() {
		return mDepth;
	}
}
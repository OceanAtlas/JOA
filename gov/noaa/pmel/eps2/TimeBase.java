package gov.noaa.pmel.eps2;

/**
 * <code>TimeBase</code> 
 * Object that encapsulates a base time and the measured time units.
 *
 * @author oz
 * @version 1.0
 */
public class TimeBase { 
	/**
	* Time base
	*/
	protected String mTbase; 
	/**
	* Units
	*/
	protected String mUnits;
	
  	/**
   	* Construct a new <code>Dbase</code> TimeBase.
   	*/
	public TimeBase() {
		// zero arg ctor
	}
	
  	/**
   	* Construct a new <code>Dbase</code> TimeBase a base time and units.
   	*
   	* @param tp Base time
   	* @param un Units
   	*/
	public TimeBase(String tb, String un) {
		mTbase = new String(tb);
		mUnits = new String(un);
	}
	
  	/**
   	* Construct a new <code>Dbase</code> TimeBase by copying an existing TimeBase.
   	*
   	* @param tbs Another TimeBase
   	*/
	public TimeBase(TimeBase tbs) {
		mTbase = new String(tbs.mTbase);
		mUnits = new String(tbs.mUnits);
	}
	
  	/**
   	* Set the base time string.
   	*
   	* @param tb Base time
   	*/
	public void setTbase(String tb) {
		mTbase = null;
		mTbase = new String(tb);
	}
	
  	/**
   	* Set the units.
   	*
   	* @param tb Units
   	*/
	public void setUnits(String un) {
		mUnits = null;
		mUnits = new String(un);
	}
	
  	/**
   	* Get the base time.
   	*
   	* @return Base time
   	*/	
	public String getTbase() {
		return mTbase;
	}
	
  	/**
   	* Get the units.
   	*
   	* @return Units
   	*/		
	public String getUnits() {
		return mUnits;
	}
}

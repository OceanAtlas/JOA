package gov.noaa.pmel.eps2;

/**
 * <code>HydroAttributes</code> 
 * Summary information for a Bottle (Hydro) cast.
 *
 * @author oz
 * @version 1.0
 */
public class HydroAttributes {
	/**
	* Cruise identifier
	*/
	protected String mCruise;
	/**
	* Cast identifier
	*/
	protected String mCast;
	/**
	* Instrument Type
	*/
	protected String mInst;
	/**
	* Creation Date
	*/
	protected String mCrdate;
	/**
	* Bottle number
	*/
	protected int mBottle;
	/**
	* cast number
	*/
	protected int mCastNumber;
	
	/**
   	* Construct a new <code>HydroAttributes</code>.
   	*/
	public HydroAttributes() {
		// zero arg ctor
	}
	
	/**
   	* Construct a new <code>HydroAttributes</code> with values.
   	*
   	* @param cr Cruise identifier
   	* @param ca Cast identifier
   	* @param crd Creation date
   	* @param in Instrument type
   	* @param bott Bottle number
   	* @param castNum Cast number
   	*/
	public HydroAttributes(String cr, String ca, String in, String crd, int bott, int castNum) {
		mCruise = new String(cr);
		mCast = new String(ca);
		mInst = new String(in);
		mCrdate = new String(crd);
		mBottle = bott;
		mCastNumber = castNum;
	}
	
	/**
   	* Construct a new <code>HydroAttributes</code> by coying values from another HydroAttributes.
   	*
   	* @param hyd Existing HydroAttributes object
   	*/
	public HydroAttributes(HydroAttributes hyd) {
		mCruise = new String(hyd.mCruise);
		mCast = new String(hyd.mCast);
		mInst = new String(hyd.mInst);
		mCrdate = new String(hyd.mCrdate);
		mBottle = hyd.mBottle;
		mCastNumber = hyd.mCastNumber;
	}
	
	/**
   	* Get the Cruise attribute.
   	*
   	* @return Cruise ID
   	*/
	public String getCruise() {
		return mCruise;
	}
	
	/**
   	* Get the Cast ID.
   	*
   	* @return Cast ID
   	*/
	public String getCast() {
		return mCast;
	}
	
	/**
   	* Get the Instrument Typr.
   	*
   	* @return Instrument Type
   	*/
	public String getInst() {
		return mInst;
	}
	
	/**
   	* Get the Creation Date.
   	*
   	* @return Creation date as a string
   	*/
	public String getCrdate() {
		return mCrdate;
	}
	
	/**
   	* Get the Bottle Number.
   	*
   	* @return Bottle number as a int
   	*/
	public int getBottle() {
		return mBottle;
	}
	
	/**
   	* Get the Cast Number.
   	*
   	* @return cast number as a int
   	*/
	public int getCastNum() {
		return mCastNumber;  
	}
	
	public String toString() {
		return new String(mCruise + " " + mCast + " " + mInst + " " + mCrdate + " " + mBottle + " " + mCastNumber);
	}
}

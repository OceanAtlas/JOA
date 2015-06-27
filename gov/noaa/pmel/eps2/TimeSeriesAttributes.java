package gov.noaa.pmel.eps2;

/**
 * <code>TimeSeriesAttributes</code> 
 * Encapsulates information about a time series record
 *
 * @author oz
 * @version 1.0
 */
public class TimeSeriesAttributes {
	/**
	* Experiment
	*/
	protected String mExpr;
	/**
	* Project
	*/
	protected String mProj;
	/**
	* Mooring
	*/
	protected String mMoor;
	/**
	* Delta time
	*/
	protected String mDeltaT;
	/**
	* Instrument
	*/
	protected String mInst;
	/**
	* Creation Date
	*/
	protected String mCrDate;
	
  	/**
   	* Construct a new <code>TimeSeriesAttributes</code>.
   	*/
	public TimeSeriesAttributes() {
		// zero arg ctor
	}
	
  	/**
   	* Construct a new <code>Dbase</code> with values.
   	*
   	* @param exp Experiment
   	* @param pro Project
   	* @param mor Mooring
   	* @param del Delta T
   	* @param ins Instrument
   	* @param crd Creation date
   	*/
	public TimeSeriesAttributes(String exp, String pro, String mor, String del, String ins, String crd) {
		if (exp != null)
			mExpr = new String(exp);
		if (pro != null)
			mProj = new String(pro);
		if (mor != null)
			mMoor = new String(mor);
		if (exp != null)
			del = new String(del);
		if (exp != null)
			ins = new String(ins);
		if (exp != null)
			crd = new String(crd);
	}
	
  	/**
   	* Construct a new <code>TimeSeriesAttributes</code> from an existing TimeSeriesAttributes object.
   	*
   	* @param tsa TimeSeriesAttributes
   	*/
	public TimeSeriesAttributes(TimeSeriesAttributes tsa) {
		mExpr = new String(tsa.mExpr);
		mProj = new String(tsa.mProj);
		mMoor = new String(tsa.mMoor);
		mDeltaT = new String(tsa.mDeltaT);
		mInst = new String(tsa.mInst);
		mCrDate = new String(tsa.mCrDate);
	}
	
	/**
   	* Get the Experiment attribute.
   	*
   	* @return Experiment
   	*/
	public String getExperiment() {
		return mExpr;
	}
	
	/**
   	* Get the Project.
   	*
   	* @return Project
   	*/
	public String getProject() {
		return mProj;
	}
	
	/**
   	* Get the Mooring.
   	*
   	* @return Mooring
   	*/
	public String getMooring() {
		return mMoor;
	}
	
	/**
   	* Get Delta T.
   	*
   	* @return Delta T
   	*/
	public String getDeltaT() {
		return mDeltaT;
	}
	
	/**
   	* Get the Instrument Type.
   	*
   	* @return Instrument
   	*/
	public String getInstrument() {
		return mInst;
	}
	
	/**
   	* Get the Creation date.
   	*
   	* @return Creation date
   	*/
	public String getCrDate() {
		return mCrDate;
	}
	
	public String toString() {
		return new String(mExpr + " " + mProj + " " + mMoor + " " + mDeltaT + " " + mInst + " " + mCrDate);
	}
}

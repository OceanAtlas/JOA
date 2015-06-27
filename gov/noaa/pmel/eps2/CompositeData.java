package gov.noaa.pmel.eps2;

/**
 * <code>CompositeData</code> 
 * Composite metadata for a profile. 
 *
 * @see DBase
 *
 * @author oz
 * @version 1.0
 */
public class CompositeData {
	/**
	* Latitude
	*/
	protected float mLat;
	/**
	* Longitude
	*/
	protected float mLon;
	/**
	* Start
	*/
	protected String mStart;
	/**
	* End
	*/
	protected String mEnd;
	/**
	* Instrument Depth
	*/
	protected float mDepth;
	/**
	* Comment
	*/
	protected String mComment;
	
  	/**
   	* Construct a new <code>CompositeData</code>.
   	*/
	public CompositeData() {
		// zero arg ctor
	}
	
  	/**
   	* Construct a new <code>CompositeData</code> from values.
   	* @param lt latitude
   	* @param ln longitude
   	* @param st start
   	* @param en end
  	* @param dep instrument depth
   	* @param cm comment
   	*/
	public CompositeData(float lt, float ln, String st, String en, float dep, String cm) {
		mLat = lt;
		mLon = ln;
		mStart = new String(st);
		mEnd = new String(en);
		mComment = new String(cm);
		mDepth = dep;
	}
	
  	/**
   	* Copy constructor.
   	* @param compData CompositeData object
   	*/
	public CompositeData(CompositeData compData) {
		mLat = compData.mLat;
		mLon = compData.mLon;
		mStart = new String(compData.mStart);
		mEnd =  new String(compData.mEnd);
		mComment =  new String(compData.mComment);
		mDepth = compData.mDepth;
	}
	
  	/**
   	* Set the latitude.
   	* @param lt  latitude
   	*/
	public void setLat(float lt) {
		mLat = lt;
	}
	
  	/**
   	* Set the longitude.
   	* @param ln longitude
   	*/
	public void setLon(float ln) {
		mLon = ln;
	}
	
  	/**
   	* Set the start.
   	* @param st start
   	*/
	public void setStart(String st) {
		mStart = null;
		mStart = new String(st);
	}
	
  	/**
   	* Set the end.
   	* @param en end
   	*/
	public void setEnd(String en) {
		mEnd = null;
		mEnd = new String(en);
	}
	
  	/**
   	* Set the comment.
   	* @param com comment
   	*/
	public void setCmnt(String com) {
		mComment = null;
		mComment = new String(com);
	}
	
  	/**
   	* Set the instrument depth.
   	* @param dep instrument depth
   	*/
	public void setDepth(float dep) {
		mDepth = dep;
	}
	
  	/**
  	* Get the latitude.
   	*
  	* @return latitude
   	**/
	public float getLat() {
		return mLat;
	}
	
  	/**
  	* Get the longitude.
   	*
  	* @return longitude
   	**/
	public float getLon() {
		return mLon;
	}
	
  	/**
  	* Get the start.
   	*
  	* @return start
   	**/
	public String getStart() {
		return mStart;
	}
	
  	/**
  	* Get the end.
   	*
  	* @return end
   	**/
	public String getEnd() {
		return mEnd;
	}

  	/**
  	* Get the comment.
   	*
  	* @return latitude
   	**/	
   	public String getCmnt() {
		return mComment;
	}
	
  	/**
  	* Get the instrument depth.
   	*
  	* @return depth
   	**/
	public float getDepth() {
		return mDepth;
	}
}
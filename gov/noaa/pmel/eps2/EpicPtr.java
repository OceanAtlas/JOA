package gov.noaa.pmel.eps2;

import gov.noaa.pmel.util.*;
import java.util.*;

/**
 * <code>EpicPtr</code> base class for epic ptr objects.
 *
 *
 * @author oz
 * @version 1.0
 */

public class EpicPtr implements EPSConstants, Comparable {
	/**
	* format of this EPIC pointer. Can be PROFILEPTRS or TSPTRS
	*/
	int mFormat;
	/**
	* netCDF, JOA, POA, etc...
	*/
	int mFileFormat;
	/**
	* Database name parsed from pointer file
	*/
	String mDatabase;
	/**
	* time series or profile
	*/
	String mDataType;
	/**
	* Latitude
	*/
	double mLat;
	/**
	* Latitude for end point
	*/
	double mLat2 = -99.0;
	/**
	* Longitude
	*/
	double mLon;
	/**
	* Longitude for end point
	*/
	double mLon2;
	/**
	* Pointer file name
	*/
	String mFilename;
	/**
	* file path: base path to a particular file or reference
	*/
	String mPath = "na";
	/**
	* maximum attribute number in system
	*/
	GeoDate mStartTime;
	/**
	* maximum attribute number in system
	*/
	GeoDate mEndTime;
	/**
	* Delta time for time series records
	*/
	double mDelTime;
	/**
	* Cruise name
	*/
	String mFileSet;
	/**
	* Cast identifier for profiles
	*/
	String mID;
	/**
	* minimum and maximum depth
	*/
	double mZmin, mZmax, mBottomDepth;
	/**
	* maximum attribute number in system
	*/
	int mSortKey;
	/**
	* Parameter list: collection of export parameters
	*/
	ArrayList mParamList = null;
	/**
	* mProgressStr: String to display in progress dialog
	*/
	String mProgressStr = null;
	
	/**
	* mIsURL: This flags whether file is actually a distant netCDF file (DODS)
	*/
	boolean mIsURL = false;
	/**
	* Collection of comments for this pointer
	*/
	ArrayList mComments = null;
	
	/**
	* Collection of attributes for this pointer
	*/
	HashMap mAttributes = new HashMap();
	
	/**
	* Collection of station calculation values for this pointer: used only by JOA
	*/
	ArrayList mStnCalcs;
	
	/**
	* String describing the lexicon of this pointer entry
	*/
	String mLexicon = null;
	
	/**
	* Strings for other data attached to the pointer--typically used for Argo data
	*/
	String mExtraStr1 = null;
	String mExtraStr2 = null;
	String mExtraStr3 = null;
	String mExtraStr4 = null;
	String mExtraStr5 = null;
	String mExtraStr6 = null;
	String mExtraStr7 = null;
	String mExtraStr8 = null;
	String mExtraStr9 = null;
	String mExtraStr10 = null;
	
	/**
	* flag to indicate a relative path rather than absolute
	*/
	boolean mIsPathRelative = false;
	
	/**
	* Zero argument constructor
	*/
	public EpicPtr() {
	}

    /**
	* Convenience constructor for a time series file
	*
	* @param frmt Format
	* @param db Source database
	* @param type File type (ts or profile)
	* @param dep Depth of instrument
	* @param lat Latitude
	* @param lon Longitude
	* @param stTime Start time in GeoDate
	* @param eTime End time in GeoDate
	* @param delT delta time
	* @param paramList list a parameters in file in a comma-delimited string
	* @param filename filename
	* @param path file path
	*/
	public EpicPtr(int frmt, String db, String type, double dep, double lat, double lon,
		                  GeoDate stTime, GeoDate eTime, double delT, ArrayList paramList, String filename, String path) {
		initCommon(TSPTRS, frmt, db, type, lat, lon, filename, path);
		if (paramList != null)
			mParamList = paramList;
		mZmin = dep;
		mZmax = Double.NaN;
		mStartTime = new GeoDate(stTime);
		mEndTime = new GeoDate(eTime);
		mDelTime = delT;
	}
	
    /**
	* Convenience constructor for a time series file
	*
	* @param frmt Format
	* @param db Source database
	* @param type File type (ts or profile)
	* @param dep Depth of instrument
	* @param lat Latitude
	* @param lon Longitude
	* @param stTime Start time in GeoDate
	* @param eTime End time in GeoDate
	* @param delT delta time
	* @param filename filename
	* @param path file path
	*/
	public EpicPtr(int frmt, String db, String type, double dep, double lat, double lon,
		                  GeoDate stTime, GeoDate eTime, double delT, String filename, String path) {
		initCommon(TSPTRS, frmt, db, type, lat, lon, filename, path);
		mZmin = dep;
		mZmax = Double.NaN;
		mStartTime = new GeoDate(stTime);
		mEndTime = new GeoDate(eTime);
		mDelTime = delT;
	}

    /**
	* Convenience constructor for a simple ptr file
	*
	* @param frmt Format
	* @param filename filename
	* @param path file path
	*/
	public EpicPtr(int frmt, String filename, String path) {
		initCommon(SIMPLEPTRS, frmt, "", "", Double.NaN, Double.NaN, filename, path);
	}

    /**
	* Convenience constructor for a simple ptr file with progress string
	*
	* @param frmt Format
	* @param filename filename
	* @param path file path
	*/
	public EpicPtr(int frmt, String filename, String path, String progressStr) {
		initCommon(SIMPLEPTRS, frmt, "", "", Double.NaN, Double.NaN, filename, path);
		mProgressStr = new String(progressStr);
	}

    /**
	* Convenience constructor for a profile file
	*
	* @param frmt Format
	* @param db Source database
	* @param type File type (ts or profile)
	* @param crs Cruise name
	* @param cst Cast ID
	* @param lat Latitude
	* @param lon Longitude
	* @param inDate Observation time in GeoDate
	* @param zMn Min depth
	* @param zMx Max depth
	* @param paramList list a parameters in an array
	* @param filename filename
	* @param path file path
	* @param stncalcs ArrayList of anyting calculated at the individual profile, e.g., MLD
	*/
	public EpicPtr(int frmt, String db, String type, String crs, String cst, double lat, double lon,
		                  GeoDate inDate, double zMn, double zMx, ArrayList paramList, String filename, String path,
		                  ArrayList stncalcs) {
		initCommon(PROFILEPTRS, frmt, db, type, lat, lon, filename, path);
		mFileSet = new String(crs);
		mID = new String(cst);
		if (paramList != null)
			mParamList = paramList;
		mStartTime = new GeoDate(inDate);
		mEndTime = new GeoDate(inDate);
		mZmin = zMn;
		mZmax = zMx;
		mStnCalcs = stncalcs;
	}
	
    /**
	* Convenience constructor for a profile file
	*
	* @param frmt Format
	* @param db Source database
	* @param type File type (ts or profile)
	* @param crs Cruise name
	* @param cst Cast ID
	* @param lat Latitude
	* @param lon Longitude
	* @param inDate Observation time in GeoDate
	* @param zMn Min depth
	* @param zMx Max depth
	* @param filename filename
	* @param path file path
	*/
	public EpicPtr(int frmt, String db, String type, String crs, String cst, double lat, double lon,
		                  GeoDate inDate, double zMn, double zMx, String filename, String path) {
		initCommon(PROFILEPTRS, frmt, db, type, lat, lon, filename, path);
		mFileSet = new String(crs);
		mID = new String(cst);
		mStartTime = new GeoDate(inDate);
		mEndTime = new GeoDate(inDate);
		mZmin = zMn;
		mZmax = zMx;
	}
	
    /**
	* Convenience constructor for a track data
	*
	* @param frmt Format
	* @param db Source database
	* @param type File type (ts or profile)
	* @param slat start Latitude
	* @param elat end Latitude
	* @param slon Longitude
	* @param elon Longitude
	* @param stTime Start time in GeoDate
	* @param eTime End time in GeoDate
	* @param delT delta time
	* @param filename filename
	* @param path file path
	*/
	public EpicPtr(int frmt, String db, String type, String crs, String cst, double slat, double elat, double slon, double elon,
		                  GeoDate stTime, GeoDate eTime, double zmin, double zmax, 
		                  	String file,
		                  	String ocean,
		                  	String posQC,
		                  	String timeQC,
		                  	String dataCtr,
		                  	String dataMode,
		                  	String numLevels,
		                  	String numParams,
		                  	String paramList, 
		                  	String URL) {
		mFormat = frmt;
		mDatabase = new String(db);
		mDataType = new String(type);
		mFileSet = new String(crs);
		mID = new String(cst);
		
		mZmin = zmin;
		mZmax = zmax;
		mStartTime = new GeoDate(stTime);
		mEndTime = new GeoDate(eTime);
		mLat = slat;
		mLat2 = elat;
		mLon = slon;
		mLon2 = elon;
		
		if (URL != null)
			mPath = new String(URL);
			
		if (file != null)
			mFilename = new String(file);
			
		if (file != null)
			mExtraStr1 = new String(file);
			
		if (ocean != null)
			mExtraStr2 = new String(ocean);
			
		if (posQC != null)
			mExtraStr3 = new String(posQC);
			
		if (timeQC != null)
			mExtraStr4 = new String(timeQC);
			
		if (dataCtr != null)
			mExtraStr5 = new String(dataCtr);
			
		if (dataMode != null)
			mExtraStr6 = new String(dataMode);
			
		if (numLevels != null)
			mExtraStr7 = new String(numLevels);
			
		if (numParams != null)
			mExtraStr8 = new String(numParams);
			
		if (paramList != null)
			mExtraStr9 = new String(paramList);
	}
	
	public String getExtraField1() {
		return mExtraStr1;
	}
	
	public String getExtraField2() {
		return mExtraStr2;
	}
	
	public String getExtraField3() {
		return mExtraStr3;
	}
	
	public String getExtraField4() {
		return mExtraStr4;
	}
	
	public String getExtraField5() {
		return mExtraStr5;
	}
	
	public String getExtraField6() {
		return mExtraStr6;
	}
	
	public String getExtraField7() {
		return mExtraStr7;
	}
	
	public String getExtraField8() {
		return mExtraStr8;
	}
	
	public String getExtraField9() {
		return mExtraStr9;
	}
	
	public String getExtraField10() {
		return mExtraStr10;
	}

    /**
    * Set the common fields
    *
	* @param ifrmt Format code whether profile or time series data
	* @param frmt Format what kind of profile data is it? netcdf, joa, poa, etc...
	* @param db Source database
	* @param type File type (ts or profile)
	* @param lat Latitude
	* @param lon Longitude
	* @param filename filename
	* @param path file path
    */
	public void initCommon(int ifrmt, int filefrmt, String db, String typ, double lt, double ln, String fn, String pth) {
		mFormat = ifrmt;
		mFileFormat = filefrmt;
		mDatabase = new String(db);
		mDataType = new String(typ);
		mLat = lt;
		mLon = ln;
		mFilename = new String(fn);
		if (pth != null)
			mPath = new String(pth);
	}

    /**
     * get the file format
     *
     * @return file format
     */
	public int getFormat() {
		return mFileFormat;
	}

    /**
     * get the database name
     *
     * @return name of database
     */
	public String getDatabase() {
		return mDatabase;
	}

    /**
     * get the data type
     *
     * @return data type
     */
	public String getDataType() {
		return mDataType;
	}

    /**
     * get the fileset
     *
     * @return cruise
     */
	public String getFileSet() {
		return mFileSet;
	}

    /**
     * get the cast
     *
     * @return cast
     */
	public String getID() {
		return mID;
	}

    /**
     * get the file path
     *
     * @return file path
     */
	public String getPath() {
		return mPath;
	}

    /**
     * get the filename
     *
     * @return filename
     */
	public String getFileName() {
		return mFilename;
	}

    /**
     * Return true if file is a profile
     *
     * @return whether ptr is CTD
     */
	public boolean isProfile() {
		return mFormat == PROFILEPTRS;
	}

    /**
     * Return true if file is a time series
     *
     * @return whether ptr is a time series
     */
	public boolean isTimeSeries() {
		return mFormat == TSPTRS;
	}

    /**
     * Get longitude
     *
     * @return longitude
     */
	public double getLon() {
		return mLon;
	}

    /**
     * Get Min longitude
     *
     * @return min longitude
     */
	public double getMinLon() {
		return mLon;
	}

    /**
     * Get Max longitude
     *
     * @return max longitude
     */
	public double getMaxLon() {
		return mLon2;
	}

    /**
     * Get latitude
     *
     * @return latitude
     */
	public double getLat() {
		return mLat;
	}

    /**
     * Get Min latitude
     *
     * @return latitude
     */
	public double getMinLat() {
		return mLat;
	}

    /**
     * Get Max latitude
     *
     * @return latitude
     */
	public double getMaxLat() {
		return mLat2;
	}
	
    /**
     * Is Lat Axis Scaler?
     *
     * @return true if axis is scaler, false if vector
     */
	public boolean isLatScaler() {
		return (mLat2 != -99.0);
	}
	
    /**
     * Is Lon Axis Scaler?
     *
     * @return true if axis is scaler, false if vector
     */
	public boolean isLonScaler() {
		return (mLon2 != -99.0);
	}

    /**
     * Get Start Time
     *
     * @return start time
     */
	public GeoDate getStartTime() {
		return mStartTime;
	}

    /**
     * Get End Time
     *
     * @return end time
     */
	public GeoDate getEndTime() {
		return mEndTime;
	}
	
    /**
     * Is Time Axis Scaler?
     *
     * @return true if axis is scaler, false if vector
     */
	public boolean isTimeScaler() {
		return (mStartTime.getTime() == mEndTime.getTime());
	}

    /**
     * Get min depth
     *
     * @return zmin
     */
	public double getZMin() {
		return mZmin;
	}

    /**
     * Get max depth
     *
     * @return zmax
     */
	public double getZMax() {
		return mZmax;
	}

    /**
     * Get bottom depth
     *
     * @return mBottomDepth
     */
	public double getBottomDepth() {
		return mBottomDepth;
	}

    /**
     * Set bottom depth
     *
     * @param bottom
     */
	public void setBottomDepth(double ibd) {
		mBottomDepth = ibd;
	}
	
    /**
     * Is Z Axis Scaler?
     *
     * @return true if axis is scaler, false if vector
     */
	public boolean isZScaler() {
		return (mZmin == mZmax);
	}

    /**
     * Get depth
     *
     * @return zMax for profiles and zMin for time series
     */
	public double getZ() {
		if (mFormat == PROFILEPTRS)
			return mZmax;
		else if (mFormat == TSPTRS)
			return mZmin;
		return mZmax;
	}

    /**
     * Get delta T
     *
     * @return delta t for time series
     */
	public double getDeltaT() {
		return mDelTime;
	}

    /**
     * Get date
     *
     * @return start time
     */
	public GeoDate getT() {
		if (mFormat == PROFILEPTRS)
			return mStartTime;
		else if (mFormat == TSPTRS)
			return mStartTime;
		return mStartTime;
	}

    /**
     * Set the sort keys
     *
     * @param inKey set the sort key for this record (X_DSC, X_ASC, Y_DSC, ...)
     */
	public void setSortKey(int inKey) {
		mSortKey = inKey;
	}

    /**
     * Return a string representation of this ptr object
     *
     * @return string of ptr
     */
	public String toString() {
		return mFilename + " start loc= " + mLat + "," + mLon + " end loc= " + mLat2  + "," + mLon2 + " zrange=" + mZmin + "-" + mZmax +
		       	   " date range= " + mStartTime.toString() + "-" + mEndTime.toString() + "\n" +
					"mFilename = " + mFilename + "\n" +
					"mPath = " + mPath + "\n" +
					"es2 = " + mExtraStr2 + "\n" +
					"es3 = " + mExtraStr3 + "\n" +
					"es4 = " + mExtraStr4 + "\n" +
					"es5 = " + mExtraStr5 + "\n" +
					"es6 = " + mExtraStr6 + "\n" +
					"es7 = " + mExtraStr7 + "\n" +
					"es8 = " + mExtraStr8 + "\n" +
					"es9 = " + mExtraStr9 + "\n" +
					"es10 = " + mExtraStr10 + "\n";
	}

    /**
     * Compares EpicPtr objects for sorting
     *
     * @param o The object to compare this pointer to
     *
     * @return <code>int</code> 0 => ==, < 0 => <, > 0 => >
     */
	public int compareTo(Object o) {
		EpicPtr ep = (EpicPtr)o;
		if (mSortKey == X_ASC) {
			// sort ascending by longitude
			double val0 = this.getLon();
			double val1 = ep.getLon();
			if (val0 < val1)
				return -1;
			else if (val0 > val1)
				return 1;
			else
				return 0;
		}
		else if (mSortKey == X_DSC) {
			// sort ascending by longitude
			double val0 = this.getLon();
			double val1 = ep.getLon();
			if (val0 < val1)
				return 1;
			else if (val0 > val1)
				return -1;
			else
				return 0;
		}

		if (mSortKey == Y_ASC) {
			// sort ascending by latitude
			double val0 = this.getLat();
			double val1 = ep.getLat();
			if (val0 < val1)
				return -1;
			else if (val0 > val1)
				return 1;
			else
				return 0;
		}
		else if (mSortKey == Y_DSC) {
			// sort ascending by latitude
			double val0 = this.getLat();
			double val1 = ep.getLat();
			if (val0 < val1)
				return 1;
			else if (val0 > val1)
				return -1;
			else
				return 0;
		}

		// depth
		if (mSortKey == Z_ASC) {
			// sort ascending by depth
			double val0 = this.getZ();
			double val1 = ep.getZ();
			if (val0 < val1)
				return -1;
			else if (val0 > val1)
				return 1;
			else
				return 0;
		}
		else if (mSortKey == Z_DSC) {
			// sort ascending by depth
			double val0 = this.getZ();
			double val1 = ep.getZ();
			if (val0 < val1)
				return 1;
			else if (val0 > val1)
				return -1;
			else
				return 0;
		}

		// time axis
		if (mSortKey == T_ASC) {
			// sort ascending by depth
			GeoDate val0 = this.getT();
			GeoDate val1 = ep.getT();
			double delta = val1.subtract(val0).getGMTMinutes();
			if (delta < 0)
				return 1;
			else if (delta > 0)
				return -1;
			else
				return 0;
		}
		else if (mSortKey == T_DSC) {
			// sort ascending by depth
			GeoDate val0 = this.getT();
			GeoDate val1 = ep.getT();
			GeoDate gdelta = val1.subtract(val0);
			double delta = val0.subtract(val1).getGMTMinutes();
			if (delta < 0)
				return -1;
			else if (delta > 0)
				return 1;
			else
				return 0;
		}
		return 0;
	}

    /**
     * Get Progress dialog string
     *
     * @return progress string
     */
	public String getProgressStr() {
		return mProgressStr;
	}
	
	public void setIsURL(boolean bval) {
		mIsURL = bval;
	}
	
	public String getURL() {
		return mFilename;
	}

	public int getNumStnCalcs() {
		if (mStnCalcs != null)
			return mStnCalcs.size();
		else
			return 0;
	}
	
	public ArrayList getStnCalcs() {
		return mStnCalcs;
	}
	
	public ArrayList getParameters() {
		return mParamList;
	}
	
	public void addComment(String comment) {
		if (mComments == null)
			mComments = new ArrayList();
		mComments.add(comment);
	}
	
	public ArrayList getComments() {
		return mComments;
	}
	
	public void addAttribute(String name, String atval) {
		mAttributes.put(name, atval);
	}
	
	public HashMap getAttributes() {
		return mAttributes;
	}
	
	public int getNumAttributes() {
		return mAttributes.size();
	}
	
	public String getLexicon() {
		return mLexicon;
	}
	
	public boolean isRelativePath() {
		return mIsPathRelative;
	}
	
	public void setIsRelativePath(boolean b) {
		mIsPathRelative = b;
	}
}

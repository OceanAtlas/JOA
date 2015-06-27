package gov.noaa.pmel.eps2;

import java.util.*;
import java.io.*;
import gov.noaa.pmel.util.*;

/**
 * <code>PointerFileAttributes</code> defines a set of file-level attributes for a pointer file.
 *
 * @see EpicPtrWriter, XMLPtrWriter
 *
 * @author oz
 * @version 1.0
 */
 
public class PointerFileAttributes {
	String mPath;		//includes the protocol
	String mCreator;
	String mDate;
	String mMissingValue;
	String mFillValue;
	String mType;
	ArrayList mLats;
	ArrayList mLons;
	ArrayList mVerticals;
	ArrayList mTimesOrDates;
	Hashtable mAttributes = new Hashtable();
	ArrayList mVarList = null;
	ArrayList mComments = null;
	String mOriginalPtrFile;
	/*
	* Stores attributes of a pointer file.
	*
	*/
	public PointerFileAttributes(){
		// null ctor--will use the API to populate the object
	}
	
	public PointerFileAttributes(String origPtrFile, String path, String type, ArrayList lats, ArrayList lons,
								 ArrayList verts, ArrayList timesordates, ArrayList vars) {
		// ctor with absolute minimum amount of information
		if (origPtrFile != null)
			mOriginalPtrFile = new String(origPtrFile);
		if (path != null)
			mPath = new String(path);
		if (type != null)
			mType = new String(type);
		mLats = lats;
		mLons = lons;
		mVerticals = verts;
		mTimesOrDates = timesordates;
		mVarList = vars;
	}
	
	public String getOrigFilename() {
		return mOriginalPtrFile;
	}
	
	public void addComment(String comment) {
		if (mComments == null)
			mComments = new ArrayList();
		mComments.add(comment);
	}
	
	public ArrayList getComments() {
		return mComments;
	}

	public ArrayList getVarList() {
		return mVarList;
	}
	
	public ArrayList getLats() {
		return mLats;
	}
	
	public ArrayList getLons() {
		return mLons;
	}
	
	public ArrayList getVerticals() {
		return mVerticals;
	}
	
	public ArrayList getTimesOrDates() {
		return mTimesOrDates;
	}
	
	public String getPath() {
		return mPath;
	}
	
	public String getType() {
		return mType;
	}
	
	public void setPath(String path) {
		mPath = new String(path);
	}
	
	public void setType(String type) {
		mType = new String(type);
	}
	
	public void addAttribute(String name, String atval) {
		mAttributes.put(name, atval);
	}
	
	public Hashtable getAttributes() {
		return mAttributes;
	}
	
	public int getNumAttributes() {
		return mAttributes.size();
	}
	
	// convenience methods
	public void setCreator(String creator) {
		addAttribute("creator", creator);
	}
	
	public void setCreationDate(String date) {
		addAttribute("creation_date", date);
	}
	
	public void setMissingValue(double missingVal) {
		addAttribute("missing_value", Double.toString(missingVal));
	}
	
	public void setFillValue(double fillVal) {
		addAttribute("fill_value", Double.toString(fillVal));
		mFillValue = new String(Double.toString(fillVal));
	}
}

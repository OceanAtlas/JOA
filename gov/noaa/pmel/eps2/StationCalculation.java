package gov.noaa.pmel.eps2;

import gov.noaa.pmel.util.*;
import java.util.*;

/**
 * <code>StationCalculation</code> Stores calculated values for a station.
 *
 *
 * @author oz
 * @version 1.0
 */

public class StationCalculation {
	String mVarName = null;
	String mCalcMethod = null;
	String mVarUnits = null;
	String mLexicon = null;
	double mValue;
	boolean mIsMissing;
	/**
	* Collection of comments for this variable
	*/
	ArrayList mComments = null;
	/**
	* Collection of attributes for this variable
	*/
	Hashtable mAttributes = new Hashtable();
	
	public StationCalculation(String var, String units, String method, String lexicon, double value, boolean missing) {
		if (var != null)
			mVarName = new String(var);
		if (units != null)
			mVarUnits = new String(units);
		if (method != null)
			mCalcMethod = new String(method);
		if (lexicon != null)
			mLexicon = new String(lexicon);
		mValue = value;
		mIsMissing = missing;
	}
	
	public String getVarName() {
		return mVarName;
	}
	
	public String getVarUnits() {
		return mVarUnits;
	}
	
	public String getCalcMethod() {
		return mCalcMethod;
	}
	
	public String getLexicon() {
		return mLexicon;
	}
	
	public double getValue() {
		return mValue;
	}
	
	public boolean isMissing() {
		return mIsMissing;
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
	
	public Hashtable getAttributes() {
		return mAttributes;
	}
	
	public int getNumAttributes() {
		return mAttributes.size();
	}
}

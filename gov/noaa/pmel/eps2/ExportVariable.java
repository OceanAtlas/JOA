package gov.noaa.pmel.eps2;

import gov.noaa.pmel.util.*;
import java.util.*;
import javax.swing.*;

/**
 * <code>ExportVariable</code> Stores variable information written to XML files.
 *
 *
 * @author oz
 * @version 1.0
 */

public class ExportVariable {
	String mVarName = "";
	String mVarUnits = "";
	String mLexicon = "";
	String mAlgorithmnRef = "";
	String mSQLType = "DOUBLE";
	String mPresentationVarName = "";
	int mEpicCode = -99;
	
	/**
	* Collection of comments for this variable
	*/
	ArrayList mComments = new ArrayList();
	/**
	* Collection of attributes for this variable
	*/
	Hashtable mAttributes = new Hashtable();
	
	public ExportVariable(String var, String units, String lexicon) {
		this(var, units, null, lexicon, -99);
	}
	
	public ExportVariable(String var, String units, int ec) {
		this(var, units, null, "EPIC", ec);
	}

	public ExportVariable(String var, String units, String lex, int ec) {
		this(var, units, null, lex, ec);
	}
	
	public ExportVariable(String var, String units, String algoRef, String lexicon) {
		this(var, units, null, lexicon, -99);
	}
	
	public ExportVariable(String var, String units, String algoRef, String lexicon, int ec) {
		mVarName = new String(var);
		mPresentationVarName = new String(var);
		if (units != null)
			mVarUnits = new String(units);
		if (lexicon != null)
			mLexicon = new String(lexicon);
		if (algoRef != null)
			mAlgorithmnRef = new String(algoRef);
		mEpicCode = ec;
	}
	
	public int getEPICCode() {
		return mEpicCode;
	}
	
	public void setEPICCode(int i) {
		mEpicCode = i;
	}
	
	public String getVarName() {
		return mVarName;
	}
	
	public String getPresentationVarName() {
		return mPresentationVarName;
	}
	
	public boolean isNameTranslated() {
		if (mPresentationVarName != null && !mPresentationVarName.equalsIgnoreCase(mVarName))
			return true;
		else
			return false;
	}
	
	public String getVarUnits() {
		return mVarUnits;
	}
	
	public void setVarUnits(String s) {
		mVarUnits = s;
	}
	
	public void setPresentationVarName(String s) {
		mPresentationVarName = s;
	}
	
	public String getLexicon() {
		return mLexicon;
	}
	
	public void setLexicon(String s) {
		mLexicon = s;
	}
	
	public String getAlgorithmRef() {
		return mAlgorithmnRef;
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
	
	public String getSQLType() {
		return mSQLType;
	}
	
	public void setSQLType(String type) {
		mSQLType = new String(type);
	}
	
	public String toString(int tabs) {
		String retVal = "";
		for (int i=0; i<tabs; i++)
			retVal += "\t";
		return retVal + mVarName + " " + mVarUnits + " " + mLexicon + " " + mAlgorithmnRef;
	}
}

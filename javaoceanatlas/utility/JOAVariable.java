/*
 * $Id: JOAVariable.java,v 1.3 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.util.*;
import javaoceanatlas.ui.*;

/**
 * <code>ExportVariable</code> Stores variable information including parent FileViewer.
 *
 *
 * @author oz
 * @version 1.0
 */

public class JOAVariable {
	String mVarName = "";
	String mVarUnits = "";
	String mLexicon = "";
	String mAlgorithmnRef = "";
	String mSQLType = "DOUBLE";
	FileViewer mFileViewer;
	int mVarPos;
	
	/**
	* Collection of comments for this variable
	*/
	ArrayList<String> mComments = new ArrayList<String>();
	/**
	* Collection of attributes for this variable
	*/
	Hashtable<String, String> mAttributes = new Hashtable<String, String>();
	
	public JOAVariable(FileViewer fv, String var, String units, String lexicon) {
		this(fv, var, units, null, lexicon);
	}
	
	public JOAVariable(FileViewer fv, String var, String units, String algoRef, String lexicon) {
		mFileViewer = fv;
		mVarName = new String(var);
		if (units != null)
			mVarUnits = new String(units);
		if (lexicon != null)
			mLexicon = new String(lexicon);
		if (algoRef != null)
			mAlgorithmnRef = new String(algoRef);
		mVarPos = mFileViewer.getPropertyPos(mVarName, false);
	}
	
	public boolean equals(JOAVariable tstVar) {
		if (this.getVarName().equalsIgnoreCase(tstVar.getVarName()))
			return true;
		else
			return false;
	}
	
	public FileViewer getFileViewer() {
		return mFileViewer;
	}
	
	public String getVarName() {
		return mVarName;
	}
	
	public String getVarUnits() {
		return mVarUnits;
	}
	
	public String getLexicon() {
		return mLexicon;
	}
	
	public String getAlgorithmRef() {
		return mAlgorithmnRef;
	}
	
	public int getVariablePos() {
		return mVarPos;
	}
	
	public void addComment(String comment) {
		if (mComments == null)
			mComments = new ArrayList<String>();
		mComments.add(comment);
	}
	
	public ArrayList<String> getComments() {
		return mComments;
	}
	
	public void addAttribute(String name, String atval) {
		mAttributes.put(name, atval);
	}
	
	public Hashtable<String, String> getAttributes() {
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
	
	public String toString() {
		String retVal = "";
		return retVal + mVarName + " " + mVarUnits + " " + mLexicon + " " + mAlgorithmnRef;
	}
	
	public double getPlotMin() {
		return mFileViewer.mAllProperties[mVarPos].mPlotMin;
	}
	
	public double getPlotMax() {
		return mFileViewer.mAllProperties[mVarPos].mPlotMax;
	}
}

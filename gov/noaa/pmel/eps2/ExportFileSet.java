package gov.noaa.pmel.eps2;

import gov.noaa.pmel.util.*;
import java.util.*;

/**
 * <code>ExportVariable</code> Stores variable information written to XML files.
 *
 *
 * @author oz
 * @version 1.0
 */

public class ExportFileSet {
	String mID;
	String mURI;
	/**
	* Collection of ExportVariables for this fileset
	*/
	ArrayList mExportStations = null;
	
	/**
	* Collection of ExportVariables for this fileset
	*/
	ArrayList mVariables = null;
	
	/**
	* Collection of comments for this variable
	*/
	ArrayList mComments = new ArrayList();
	/**
	* Collection of attributes for this variable
	*/
	Hashtable mAttributes = new Hashtable();
	String mLexicon = null;
		
	public ExportFileSet(String id, String uri, ArrayList vars, ArrayList stns) {
		mID = new String(id);
		if (uri != null)
			mURI = new String(uri);
		if (vars != null)
			mVariables = vars;
		mExportStations = stns;
	}
	
	public String getID() {
		return mID;
	}
	
	public String getURI() {
		return mURI;
	}
	
	public ArrayList getVariables() {
		return mVariables;
	}
	
	public ArrayList getStations() {
		return mExportStations;
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

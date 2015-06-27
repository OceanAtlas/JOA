package gov.noaa.pmel.eps2;

import gov.noaa.pmel.util.*;
import java.util.*;
import javax.swing.*;

/**
 * <code>CalculatedVariable</code> Extends ExportVariable to store stuff specific to a calculation.
 *
 *
 * @author oz
 * @version 1.0
 */

public class CalculatedVariable extends ExportVariable {
	boolean mIsEditable;
	Object mUserDefinedSupportObject; // could be a reference to the calc specification, actual calc object
	boolean mStnVariable;
	
	public CalculatedVariable(String var, String units, boolean editable, boolean stnvar, String lexicon, Object userObj) {
		super(var, units, null, lexicon);
		mIsEditable = editable;
		mStnVariable = stnvar;
		mUserDefinedSupportObject = userObj;
	}
	
	public CalculatedVariable(String var, String units, boolean editable, boolean stnvar, String algoRef, String lexicon, Object userObj) {
		super(var, units, algoRef, lexicon);
		mIsEditable = editable;
		mStnVariable = stnvar;
		mUserDefinedSupportObject = userObj;
	}
	
	public boolean isEditable() {
		return mIsEditable;
	}
	
	public boolean isStationVar() {
		return mStnVariable;
	}
	
	public Object getUserObject() {
		return mUserDefinedSupportObject;
	}
	
	public void setUserObject(Object obj) {
		mUserDefinedSupportObject = obj;
	}
}

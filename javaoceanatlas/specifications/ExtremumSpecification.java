/*
 * $Id: ExtremumSpecification.java,v 1.5 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import java.awt.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.ui.*;
import java.awt.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import javaoceanatlas.utility.*;

public class ExtremumSpecification implements PlotSpecification {
	protected int mExtVarCode, mWRTVarCode;
	protected boolean mSearchForMax;
	protected double mStartSurfValue, mEndSurfValue;
	protected String[] mOtherParams;
	protected String[] mOtherUnits;
	String mExtVar, mWRTVar;

	public ExtremumSpecification() {

	}

	public ExtremumSpecification(FileViewer fv, int eVar, int wrtVar, boolean searchMax, double startVal, double endVal,
	    String[] otherParams, String[] otherUnits) {
		mExtVar = fv.mAllProperties[eVar].getVarLabel();
		mWRTVar = fv.mAllProperties[wrtVar].getVarLabel();
		mExtVarCode = eVar;
		mWRTVarCode = wrtVar;
		mSearchForMax = searchMax;
		mStartSurfValue = startVal;
		mEndSurfValue = endVal;
		if (otherParams != null && otherParams.length > 0) {
			mOtherParams = otherParams;
			mOtherUnits = otherUnits;
		}
	}

	public ExtremumSpecification(ExtremumSpecification inSpec) {
		mExtVar = new String(inSpec.mExtVar);
		mWRTVar = new String(inSpec.mWRTVar);
		mExtVarCode = inSpec.mExtVarCode;
		mWRTVarCode = inSpec.mWRTVarCode;
		mSearchForMax = inSpec.mSearchForMax;
		mStartSurfValue = inSpec.mStartSurfValue;
		mEndSurfValue = inSpec.mEndSurfValue;
		mOtherParams = inSpec.mOtherParams;
		mOtherUnits = inSpec.mOtherUnits;
	}

	public int getIntVar() {
		return mExtVarCode;
	}

	public int getWRTVar() {
		return mWRTVarCode;
	}

	public double getStartSurfValue() {
		return mStartSurfValue;
	}

	public double getEndSurfValue() {
		return mEndSurfValue;
	}

	public boolean isSearchForMax() {
		return mSearchForMax;
	}

	public String[] getOtherParams() {
		return mOtherParams;
	}

	public String[] getOtherUnits() {
		return mOtherUnits;
	}

	public void saveAsXML(FileViewer fv, Document doc, Element item) {
	}

	public void writeToLog(String preamble) throws IOException {
		try {
			JOAConstants.LogFileStream.writeBytes(preamble);
			JOAConstants.LogFileStream.writeBytes("\t" + "Extremum Calculation: " + mExtVar + " WRT " + mWRTVar
			    + " Start Surface Value: " + JOAFormulas.formatDouble(mStartSurfValue, 3, false) + " End Surface Value: "
			    + JOAFormulas.formatDouble(mEndSurfValue, 3, false) + "\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	public String exportJSON(File file) {
		return null;
	}
}

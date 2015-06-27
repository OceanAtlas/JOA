/*
 * $Id: InterpolationSpecification.java,v 1.4 2005/06/17 18:04:10 oz Exp $
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

public class InterpolationSpecification implements PlotSpecification {
    protected int mIntVarCode, mWRTVarCode;
    protected double mIntAtValue;
    protected boolean mUseDeepest;
    protected boolean mAtSurface;
    protected double mSurfaceDepthLimit;
    protected boolean mAtBottom;
    protected int mSearchMethod;
    protected boolean mInterpolateMissing;
    String mIntVar, mWRTVar;
	
	public InterpolationSpecification() {
	
	}
	
	public InterpolationSpecification(FileViewer fv, int interpVar, int wrtVar, double val, boolean atSurface, 
					double depthLimit, boolean atBottom, boolean useDeepest, int search, boolean interpMissing) {
		mIntVar = fv.mAllProperties[interpVar].getVarLabel();
		mWRTVar = fv.mAllProperties[wrtVar].getVarLabel();
	    mIntVarCode = interpVar; 
	    mWRTVarCode = wrtVar;
	    mIntAtValue = val;
	    mAtSurface = atSurface;
	    mSurfaceDepthLimit = depthLimit;
	    mAtBottom = atBottom;
	    mUseDeepest = useDeepest;
	    mSearchMethod = search;
	    mInterpolateMissing = interpMissing;
	}
	
	public InterpolationSpecification(InterpolationSpecification inSpec) {
		mIntVar = new String(inSpec.mIntVar);
		mWRTVar = new String(inSpec.mWRTVar);
	    mIntVarCode = inSpec.mIntVarCode; 
	    mWRTVarCode = inSpec.mWRTVarCode;
	    mIntAtValue = inSpec.mIntAtValue;
	    mAtSurface = inSpec.mAtSurface;
	    mAtBottom = inSpec.mAtBottom;
	    mUseDeepest = inSpec.mUseDeepest;
	    mSearchMethod = inSpec.mSearchMethod;
	    mInterpolateMissing = inSpec.mInterpolateMissing;
	}
	
	public int getIntVar() {
		return mIntVarCode;
	}
	
	public int getWRTVar() {
		return mWRTVarCode;
	}
	
	public int getSearchMethod() {
		return mSearchMethod;
	}
	
	public double getAtVal() {
		return mIntAtValue;
	}
	
	public double getDepthLimit() {
		return mSurfaceDepthLimit;
	}
	
	public boolean isAtSurface() {
		return mAtSurface;
	}
	
	public boolean isAtBottom() {
		return mAtBottom;
	}
	
	public boolean isUseDeepest() {
		return mUseDeepest;
	}
	
	public boolean isInterpolateMissing() {
		return mInterpolateMissing;
	}
	
	public void saveAsXML(FileViewer fv, Document doc, Element item) {
	}
	
	public String exportJSON(File file) {    
		return null;
	}
        
    public void writeToLog(String preamble) throws IOException {
    	String dirText = " (top down):";
    	if (mSearchMethod == JOAConstants.SEARCH_BOTTOM_UP)
    		dirText = " (bottom up):";
    	try {
	    	JOAConstants.LogFileStream.writeBytes(preamble);
			JOAConstants.LogFileStream.writeBytes("\t" + "Interpolation " + dirText + " " + mIntVar + " WRT " + mWRTVar +
				 " Int. at Value = " + JOAFormulas.formatDouble(mIntAtValue, 3, false) +
				 " Use deepest = " + Boolean.toString(mUseDeepest) + 
				 " At Surface = " + Boolean.toString(mAtSurface) + 
				 " Surface Depth Limit = " + JOAFormulas.formatDouble(mSurfaceDepthLimit, 3, false) + 
				 " At Bottom = " + Boolean.toString(mAtBottom) + 
				 " Interpolate Missing = " + Boolean.toString(mInterpolateMissing) + 
				 "\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
    }
}
    
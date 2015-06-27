/*
 * $Id: MixedLayerCalcSpec.java,v 1.5 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import javaoceanatlas.resources.*;
import java.awt.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
import javaoceanatlas.ui.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import javaoceanatlas.utility.*;

public class MixedLayerCalcSpec implements PlotSpecification {
	private int mMethod;
	private double mDepth;
	private double mStartDepth;
	private double mTolerance;
	private String mParam;
	
	public MixedLayerCalcSpec(int method, String param, double startz, double z, double toln) {
		mMethod = method;
		mParam = new String(param);
		mStartDepth = startz;
		mDepth = z;
		mTolerance = toln;
	}
	
	public String exportJSON(File file) {    
		return null;
	}
	
	public int getMethod() {
		return mMethod;
	}
	
	public void setMethod(int i) {
		mMethod = i;
	}
	
	public String getParam() {
		return mParam;
	}
	
	public void setParam(String s) {
		mParam = new String(s);
	}
	
	public double getDepth() {
		return mDepth;
	}
	
	public void setDepth(double d) {
		mDepth = d;
	}
	
	public double getStartDepth() {
		return mStartDepth;
	}
	
	public void setStartDepth(double d) {
		mStartDepth = d;
	}
	
	public double getTolerance() {
		return mTolerance;
	}
	
	public void setTolerance(double d) {
		mTolerance = d;
	}
	
	public void saveAsXML(FileViewer fv, Document doc, Element root) {
    	/*item.setAttribute("mixedlayermethod", String.valueOf(this.getMethod()));
    	item.setAttribute("mixedlayerparam", this.getParam());
		if (this.getMethod() == JOAConstants.MIXED_LAYER_DIFFERENCE) {
    		item.setAttribute("mixedlayerdepth", String.valueOf(this.getDepth()));
		}
		if (this.getMethod() == JOAConstants.MIXED_LAYER_SURFACE) {
    		item.setAttribute("mixedlayerstartdepth", String.valueOf(this.getStartDepth()));
		}
		if (this.getMethod() == JOAConstants.MIXED_LAYER_SLOPE) {
    		item.setAttribute("mixedlayerdepth", String.valueOf(this.getDepth()));
		}
    	item.setAttribute("mixedlayertolerance", String.valueOf(this.getTolerance()));*/
	}
        
    public void writeToLog(String preamble) throws IOException {
    	String methodText = "";
    	if (mMethod == JOAConstants.MIXED_LAYER_DIFFERENCE)
    		methodText = " Method = Difference, Depth = " + JOAFormulas.formatDouble(this.getDepth(), 3, false) + " tol. = " + JOAFormulas.formatDouble(mTolerance, 3, false);
    	else if (mMethod == JOAConstants.MIXED_LAYER_SLOPE)
    		methodText = " Method = Slope, Depth = " + JOAFormulas.formatDouble(this.getDepth(), 3, false) + " tol. = " + JOAFormulas.formatDouble(mTolerance, 3, false);
    	else if (mMethod == JOAConstants.MIXED_LAYER_SURFACE)
    		methodText = " Method = Surface, Start depth = " + JOAFormulas.formatDouble(this.getStartDepth(), 3, false) + " tol. = " + JOAFormulas.formatDouble(mTolerance, 3, false);
    	
    	try {
	    	JOAConstants.LogFileStream.writeBytes(preamble + "\n");
			JOAConstants.LogFileStream.writeBytes("\t" + "Mixed-layer Calculation: " + "Param = " + mParam + methodText + "\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
    }
}
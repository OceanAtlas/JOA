/*
 * $Id: StnStatisticsSpecification.java,v 1.4 2005/06/17 18:04:10 oz Exp $
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
import javaoceanatlas.resources.*;

public class StnStatisticsSpecification implements PlotSpecification {
    protected int mVarCode;
    protected int mCalcMethod;
    String varName = "";
	
	public StnStatisticsSpecification() {
	
	}
	
	public StnStatisticsSpecification(FileViewer fv, int var, int calcMethod) {
		varName = fv.mAllProperties[var].getVarLabel(); 
	    mVarCode = var; 
	    mCalcMethod = calcMethod;
	}
	
	public StnStatisticsSpecification(StnStatisticsSpecification inSpec) {
	    mVarCode = inSpec.mVarCode; 
	    mCalcMethod = inSpec.mCalcMethod;
	}
	
	public int getVar() {
		return mVarCode;
	}
	
	public int getCalcMethod() {
		return mCalcMethod;
	}
	
	public void saveAsXML(FileViewer fv, Document doc, Element item) {
	}
	
	public String exportJSON(File file) {    
		return null;
	}
        
    public void writeToLog(String preamble) throws IOException {
    	String method = "";
    	if (mCalcMethod == JOAConstants.CALC_AVERAGE)
    		method = "Average";
    	else if (mCalcMethod == JOAConstants.CALC_DEPTH_OF_MAX)
    		method = "Depth of Max";
    	else if (mCalcMethod == JOAConstants.CALC_DEPTH_OF_MIN)
    		method = "Depth of Min";
    	else if (mCalcMethod == JOAConstants.CALC_MAX)
    		method = "Max";
    	else if (mCalcMethod == JOAConstants.CALC_MIN)
    		method = "Min";
    	else if (mCalcMethod == JOAConstants.CALC_N)
    		method = "Number non missing values";
    		
    	try {
	    	JOAConstants.LogFileStream.writeBytes(preamble + "\n");
			JOAConstants.LogFileStream.writeBytes("\t" + "Computed " + method + " for: " + varName + "\n");
		}
		catch (IOException ex) {
			throw ex;
		}
	}
}
    
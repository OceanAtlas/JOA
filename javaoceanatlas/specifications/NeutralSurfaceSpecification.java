/*
 * $Id: NeutralSurfaceSpecification.java,v 1.4 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import java.awt.*;import org.w3c.dom.*;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
import javaoceanatlas.resources.*;
import java.io.DataOutputStream;
import java.io.IOException;
import javaoceanatlas.utility.*;

public class NeutralSurfaceSpecification {
    protected double mGamma;
    protected boolean mIncludeErrorTerms = false;
    protected boolean mReturnTemponNS;
    protected boolean mReturnSaltonNS;
    protected boolean mReturnPresonNS;
	
	public NeutralSurfaceSpecification() {
	
	}
	
	public NeutralSurfaceSpecification(double gamma, boolean retTemp, boolean retSalt, boolean retPres, boolean includeErrs) {
	    mGamma = gamma;
	    mReturnTemponNS = retTemp;
	    mReturnSaltonNS = retSalt;
	    mReturnPresonNS = retPres;
	    mIncludeErrorTerms = includeErrs;
	}
	
	public NeutralSurfaceSpecification(NeutralSurfaceSpecification inSpec) {
	    mGamma = inSpec.mGamma;
	    mReturnTemponNS = inSpec.mReturnTemponNS;
	    mReturnSaltonNS = inSpec.mReturnSaltonNS; 
	    mReturnPresonNS = inSpec.mReturnPresonNS;
	    mIncludeErrorTerms = inSpec.mIncludeErrorTerms;
	}
	
	public double getGamma() {
		return mGamma;
	}
	
	public boolean isReturnTemponNS() {
		return mReturnTemponNS;
	}
	
	public boolean isReturnSaltonNS() {
		return mReturnSaltonNS;
	}
	
	public boolean isReturnPresonNS() {
		return mReturnPresonNS;
	}
	
	public boolean isIncludeErrorTerms() {
		return mIncludeErrorTerms;
	}
	
	public int getNumSurfaces() {
		int cnt = 0;
		cnt = mReturnTemponNS ? ++cnt : cnt;
		cnt = mReturnSaltonNS ? ++cnt : cnt;
		cnt = mReturnPresonNS ? ++cnt : cnt;
		return cnt;
	}
	
	public void saveAsXML(Element item) {
    	item.setAttribute("neutralsurfacegamma", String.valueOf(this.getGamma()));
    	item.setAttribute("neutralsurfacereturntemp", String.valueOf(this.isReturnTemponNS()));
    	item.setAttribute("neutralsurfacereturnsalt", String.valueOf(this.isReturnSaltonNS()));
    	item.setAttribute("neutralsurfacereturnpres", String.valueOf(this.isReturnPresonNS()));
    	item.setAttribute("neutralsurfaceincludeerrors", String.valueOf(this.isIncludeErrorTerms()));
	}
        
    public void writeToLog(String preamble) throws IOException {
    	String paramsText = "";
    	if (this.isReturnTemponNS())
    		paramsText += "Temperature,";
    	else if (this.isReturnSaltonNS())
    		paramsText += "Salinity,";
    	else if (this.isReturnPresonNS())
    		paramsText += "Pressure";
    		
    	paramsText += " include error terms = " + String.valueOf(this.isIncludeErrorTerms());
    	try {
	    	JOAConstants.LogFileStream.writeBytes(preamble + "\n");
			JOAConstants.LogFileStream.writeBytes("\t" + "Neutral Surface Calculation: " + "gamma = " + JOAFormulas.formatDouble(this.getGamma(), 3, false) + paramsText + "\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
    }
}
    
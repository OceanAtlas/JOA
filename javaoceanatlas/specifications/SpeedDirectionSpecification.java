
/*
 * $Id: SpeedDirectionSpecification.java,v 1.5 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import javaoceanatlas.resources.*;
import javaoceanatlas.ui.*;
import org.w3c.dom.*;
import java.io.File;
import java.io.IOException;

public class SpeedDirectionSpecification implements PlotSpecification {
    protected int mUVarCode, mVVarCode;
    protected String mSpeedVarName;
    protected String mSpeedUnits;
    String mDirectionVarNme;
	
	public SpeedDirectionSpecification() {
	
	}
	
	public SpeedDirectionSpecification(FileViewer fv, int uVar, int vVar, String svn, String speedUnits, String dvn) {
		mUVarCode = uVar; 
		mVVarCode = vVar;
		mSpeedVarName = svn;
		mSpeedUnits = speedUnits;
		mSpeedUnits = dvn;
	}
	
	public SpeedDirectionSpecification(SpeedDirectionSpecification inSpec) {
		mUVarCode = inSpec.mUVarCode; 
		mVVarCode = inSpec.mVVarCode;
		mSpeedVarName = inSpec.mSpeedVarName;
		mSpeedUnits = inSpec.mSpeedUnits;
		mSpeedUnits = inSpec.mSpeedUnits;
	}
	
	public int getUVar() {
		return mUVarCode;
	}
	
	public int getVVar() {
		return mVVarCode;
	}
		
	public String getSpeedVarName() {
		return mSpeedVarName;
	}
	
	public String getSpeedUnits() {
		return mSpeedUnits;
	}
	
	public void saveAsXML(FileViewer fv, Document doc, Element item) {
	}
	
	public String exportJSON(File file) {    
		return null;
	}
        
    public void writeToLog(String preamble) throws IOException {
    	try {
	    	JOAConstants.LogFileStream.writeBytes(preamble);
			JOAConstants.LogFileStream.writeBytes("\t" + "WS/Direction Calculation: " + "\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
    }
}
    
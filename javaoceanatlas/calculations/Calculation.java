/*
 * $Id: Calculation.java,v 1.6 2005/06/17 18:01:47 oz Exp $
 *
 */

package javaoceanatlas.calculations;

import java.util.Vector;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import javaoceanatlas.ui.*;
import org.w3c.dom.*;
import java.io.IOException;

public class Calculation {
    Object mArg = null;    // will be either Boolean or Double or null
    int mTypeCalc = JOAConstants.OBS_CALC_TYPE;
    String mCalcType = null;
    String mUnits = null;
    boolean mCustomDensity = false;
    boolean mBuoyancyFrequency = false;
    Vector<Section> mSectionsCalculated = new Vector<Section>();
    boolean mIncludedErrorTerms = false;
    
    public Calculation(String inType, Object inArg, String units) {
    	mCalcType = new String(inType);
    	mArg = inArg;
    	mTypeCalc = JOAConstants.OBS_CALC_TYPE;
    	if (units != null)
    		mUnits = new String(units);
	}
    
    public Calculation(String inType, Object inArg, int calcType, String units) {
    	mCalcType = new String(inType);
    	mArg = inArg;
    	mTypeCalc = calcType;
    	if (units != null)
    		mUnits = new String(units);
    }
    
    public Calculation(String inType, Object inArg, int calcType) {
    	this(inType, inArg, calcType, null);
    }
        
    public Calculation(String inType, int calcType, String units) {
    	mCalcType = new String(inType);
    	mTypeCalc = calcType;
    	if (units != null)
    		mUnits = new String(units);
    }
        
    public Calculation(String inType, int calcType) {
    	this(inType, calcType, null);
    }
    
    public void setUnits(String un) {
    	mUnits = new String(un);
    }
    
    public String getUnits() {
    	return mUnits;
    }
    
    public void setIncludeErrorTerms(boolean flag) {
    	mIncludedErrorTerms = flag;
    }
    
    public boolean isIncludeErrorTerms() {
    	return mIncludedErrorTerms;
    }
    
    public int getTypeCalc() {
    	return mTypeCalc;
    }
    
    public String getCalcType() {
    	return mCalcType;
    }
    
    public void setCalcType(String s) {
    	mCalcType = new String(s);
    }
    
    public void setIsCalculated(Section sech) {
    	mSectionsCalculated.addElement(sech);
	}
    
	public boolean isCalculated(Section sech) {
		if (mSectionsCalculated.indexOf(sech) >= 0)
			return true;
		else
			return false;
	}
	
	public boolean isCustomDensity() {
		return mCustomDensity;
	}
	
	public void setIsCustomDensity() {
    	mCustomDensity = true;
	}
	
	public double getArgAsDouble() {
		return ((Double)mArg).doubleValue();
	}
	
	public MixedLayerCalcSpec getArgAsMixedLayerCalcSpec() {
		return (MixedLayerCalcSpec)mArg;
	}
	
	public Object getArg() {
		return mArg;
	}
	
	public IntegrationSpecification getArgAsIntegrationSpecification() {
		return (IntegrationSpecification)mArg;
	}
	
	public NeutralSurfaceSpecification getArgAsNeutralSurfaceSpecification() {
		return (NeutralSurfaceSpecification)mArg;
	}
	
	public ExtremumSpecification getArgAsExtremumSpecification() {
		return (ExtremumSpecification)mArg;
	}
	
	public InterpolationSpecification getArgAsInterpolationSpecification() {
		return (InterpolationSpecification)mArg;
	}
	
	public StnStatisticsSpecification getArgAsStnStatisticsSpecification() {
		return (StnStatisticsSpecification)mArg;
	}
	
	public boolean getArgAsBoolean() {
		return ((Boolean)mArg).booleanValue();
	}
	
	public void setIsBuoyanceFrequency() {
    	mBuoyancyFrequency = true;
	}
	
	public boolean isBuoyanceFrequency() {
		return mBuoyancyFrequency;
	}
	    
	public void saveAsXML(FileViewer fv, Document doc, Element root) {
		Element item = doc.createElement("calculation");
		//attributes: isBuoyanceFrequency, isCustomDensity,,
    	item.setAttribute("parameter", String.valueOf(this.getCalcType()));
    	item.setAttribute("calctype", String.valueOf(this.getTypeCalc()));
    	if (this.getUnits() != null && this.getUnits().length() > 0)
    		item.setAttribute("calcunits", String.valueOf(this.getUnits()));
    	if (this.isBuoyanceFrequency())
    		item.setAttribute("efolding", String.valueOf(this.getArgAsDouble()));
    	if (this.isCustomDensity())
    		item.setAttribute("customdensity", String.valueOf(this.getArgAsDouble()));
		
		// arguments
		if (getArg() != null) {
			if (getArg() instanceof IntegrationSpecification)
				getArgAsIntegrationSpecification().saveAsXML(fv, doc, item);
			else if (getArg() instanceof MixedLayerCalcSpec)
				getArgAsMixedLayerCalcSpec().saveAsXML(fv, doc, item);
			else if (getArg() instanceof NeutralSurfaceSpecification) {
				getArgAsNeutralSurfaceSpecification().saveAsXML(item);
			}
		}
    	item.setAttribute("includeerrorterms", String.valueOf(this.isIncludeErrorTerms()));
		
		root.appendChild(item);
	}
        
    public void writeToLog(String preamble) throws IOException {
    	try {
	    	JOAConstants.LogFileStream.writeBytes(preamble + "\n");
			JOAConstants.LogFileStream.writeBytes("\t" + "Observation Calculation: " + mCalcType + ", units = " + mUnits);
			if (getArg() != null) {
				if (this.isBuoyanceFrequency()) {
					JOAConstants.LogFileStream.writeBytes(", e folding = " + String.valueOf(this.getArgAsDouble()));
				}
				else if (this.isCustomDensity()) {
					JOAConstants.LogFileStream.writeBytes(", ref. press. = " + String.valueOf(this.getArgAsDouble()));
				}
			}
			JOAConstants.LogFileStream.writeBytes("\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
    }
}
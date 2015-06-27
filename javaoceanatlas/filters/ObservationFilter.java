/*
 * $Id: ObservationFilter.java,v 1.4 2005/06/17 18:03:16 oz Exp $
 *
 */

package javaoceanatlas.filters;

import java.awt.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.utility.*;
import java.io.DataOutputStream;
import java.io.IOException;
import javaoceanatlas.utility.*;

public class ObservationFilter {
	protected int mNumCriteria;
	boolean mCriteria1Active = false;
	boolean mCriteria2Active = false;
	boolean mCriteria3Active = false;
	boolean mCriteria4Active = false;
	boolean mCriteria1IsQC = false;
	boolean mCriteria2IsQC = false;
	boolean mCriteria3IsQC = false;
	boolean mCriteria4IsQC = false;
	double[] mMinVals = new double[4]; 
	double[] mMaxVals = new double[4]; 
	String[] mParams = new String[4];
	int[] mParamIndices = new int[4];
	int[] mPopupIndices = new int[4];
	boolean mCriteria1IsAnd = true;
	boolean mCriteria2IsAnd = true;
	boolean mCrit1AndCrit2IsAnd = true;
	boolean mShowOnlyMatching = true;
	boolean mEnlargeSymbol = false;
	boolean mUseContrastingColor = false;
	Color mContrastingColor = null;
    protected int mSymbolSize = 4;
	protected int mSymbol;
	protected int mQCStandard;
	    
    public ObservationFilter() {
	}
	
	public void dumpFilter() {
		System.out.println("mCriteria1Active="+mCriteria1Active);
		System.out.println("mCriteria2Active="+mCriteria2Active);
		System.out.println("mCriteria3Active="+mCriteria3Active);
		System.out.println("mCriteria4Active="+mCriteria4Active);
		System.out.println("mCriteria1IsQC="+mCriteria1IsQC);
		System.out.println("mCriteria2IsQC="+mCriteria2IsQC);
		System.out.println("mCriteria3IsQC="+mCriteria3IsQC);
		System.out.println("mCriteria4IsQC="+mCriteria4IsQC);
		System.out.println("mParams[0]="+mParams[0] + "mParamIndices[0]="+mParamIndices[0]);
		System.out.println("mParams[1]="+mParams[1] + "mParamIndices[1]="+mParamIndices[1]);
		System.out.println("mParams[2]="+mParams[2] + "mParamIndices[2]="+mParamIndices[2]);
		System.out.println("mParams[3]="+mParams[3] + "mParamIndices[3]="+mParamIndices[3]);
		System.out.println("mMinVals[0]="+mMinVals[0] + "mMaxVals[0]="+mMaxVals[0]);
		System.out.println("mMinVals[1]="+mMinVals[1] + "mMaxVals[1]="+mMaxVals[1]);
		System.out.println("mMinVals[2]="+mMinVals[2] + "mMaxVals[2]="+mMaxVals[2]);
		System.out.println("mMinVals[3]="+mMinVals[3] + "mMaxVals[3]="+mMaxVals[3]);
		System.out.println("mCriteria1IsAnd="+mCriteria1IsAnd);
		System.out.println("mCriteria2IsAnd="+mCriteria2IsAnd);
		System.out.println("mCrit1AndCrit2IsAnd="+mCrit1AndCrit2IsAnd);
		System.out.println("mShowOnlyMatching="+mShowOnlyMatching);
		System.out.println("mEnlargeSymbol="+mEnlargeSymbol);
		System.out.println("mUseContrastingColor="+mUseContrastingColor);
		System.out.println("mContrastingColor="+mContrastingColor);
		System.out.println("mSymbolSize="+mSymbolSize);
		System.out.println("mSymbol="+mSymbol);
		System.out.println("mQCStandard="+mQCStandard);
	}
	
	
	public boolean isInHiliteMode() {
		return !mShowOnlyMatching;
	}
	
	public void setCriteria1Active(boolean b) {
		mCriteria1Active = b;
	}
	
	public boolean isCriteria1Active() {
		return mCriteria1Active;
	}
	
	public boolean isCriteria1ActiveQC() {
		return mCriteria1IsQC;
	}
	
	public void setCriteria1ActiveQC(boolean b) {
		mCriteria1IsQC = b;
	}
	
	public boolean isCriteria2Active() {
		return mCriteria2Active;
	}
	
	public void setCriteria2Active(boolean b) {
		mCriteria2Active = b;
	}
	
	public boolean isCriteria2ActiveQC() {
		return mCriteria2IsQC;
	}
	
	public void setCriteria2ActiveQC(boolean b) {
		mCriteria2IsQC = b;
	}
	
	public boolean isCriteria3Active() {
		return mCriteria3Active;
	}
	
	public void setCriteria3Active(boolean b) {
		mCriteria3Active = b;
	}
	
	public boolean isCriteria3ActiveQC() {
		return mCriteria3IsQC;
	}
	
	public void setCriteria3ActiveQC(boolean b) {
		mCriteria3IsQC = b;
	}
	
	public boolean isCriteria4Active() {
		return mCriteria4Active;
	}
	
	public void setCriteria4Active(boolean b) {
		mCriteria4Active = b;
	}
	
	public boolean isCriteria4ActiveQC() {
		return mCriteria4IsQC;
	}
	
	public void setCriteria4ActiveQC(boolean b) {
		mCriteria4IsQC = b;
	}
	
	public boolean testQCValues(short qc1, short qc2, short qc3, short qc4) {
	    boolean c1 = true;
	    boolean c2 = true;
	    boolean c3 = true;
	    boolean c4 = true;
	    
    	if (mCriteria1Active) {
			if (!(qc1 != JOAConstants.MISSINGVALUE && qc1 == mMaxVals[0]))
				c1 = false;
		}
		
    	if (mCriteria2Active) {
			if (!(qc2 != JOAConstants.MISSINGVALUE && qc2 == mMaxVals[1]))
					c2 = false;
		}
		
    	if (mCriteria3Active) {
			if (!(qc3 != JOAConstants.MISSINGVALUE && qc3 == mMaxVals[2]))
				c3 = false;
		}
		
    	if (mCriteria4Active) {
			if (!(qc4 != JOAConstants.MISSINGVALUE && qc4 == mMaxVals[3]))
				c4 = false;
		}
		
		boolean cond1 = false;
		if (mCriteria1IsAnd) {
			if (mCriteria1Active && mCriteria2Active)
				cond1 = c1 && c2;
			else if (mCriteria1Active)
				cond1 = c1;
			else if (mCriteria2Active)
				cond1 = c2;
		}
		else {
			if (mCriteria1Active && mCriteria2Active)
				cond1 = c1 || c2;
			else if (mCriteria1Active)
				cond1 = c1;
			else if (mCriteria2Active)
				cond1 = c2;
		}
		
		boolean cond2 = false;
		if (mCriteria2IsAnd)
			if (mCriteria3Active && mCriteria4Active)
				cond2 = c3 && c4;
			else if (mCriteria3Active)
				cond2 = c3;
			else if (mCriteria4Active)
				cond2 = c4;
		else {
			if (mCriteria3Active && mCriteria4Active)
				cond2 = c3 || c4;
			else if (mCriteria3Active)
				cond2 = c3;
			else if (mCriteria4Active)
				cond2 = c4;
		}
			
		if ((mCriteria1Active || mCriteria2Active) && (mCriteria3Active || mCriteria4Active)) {
			if (mCrit1AndCrit2IsAnd)
				return cond1 && cond2;
			else
				return cond1 || cond2;
		}
		else if (mCriteria1Active || mCriteria2Active)
			return cond1;
		else if (mCriteria3Active || mCriteria4Active)
			return cond2;
		return true;
	}
    
    public boolean testValues(double crit1Val, double crit2Val, double crit3Val, double crit4Val, Bottle bh, int c1Pos, int c2Pos, int c3Pos, int c4Pos) {
	    boolean c1 = true;
	    boolean c2 = true;
	    boolean c3 = true;
	    boolean c4 = true;
	    
    	if (mCriteria1Active) {
    		if (mCriteria1IsQC) {
    			short qc1 = bh.mQualityFlags[c1Pos];
				if (!(qc1 != JOAConstants.MISSINGVALUE && qc1 == mMaxVals[0]))
					c1 = false;
			}
			else {
				if (!(crit1Val > mMinVals[0] && crit1Val <= mMaxVals[0]))
					c1 = false;
			}
		}
		
    	if (mCriteria2Active) {
    		if (mCriteria2IsQC) {
    			short qc2 = bh.mQualityFlags[c2Pos];
				if (!(qc2 != JOAConstants.MISSINGVALUE && qc2 == mMaxVals[1]))
					c2 = false;
			}
			else {
				if (!(crit2Val > mMinVals[1] && crit2Val <= mMaxVals[1]))
					c2 = false;
			}
		}
		
    	if (mCriteria3Active) {
    		if (mCriteria3IsQC) {
    			short qc3 = bh.mQualityFlags[c3Pos];
				if (!(qc3 != JOAConstants.MISSINGVALUE && qc3 == mMaxVals[2]))
					c3 = false;
			}
			else {
				if (!(crit3Val > mMinVals[2] && crit3Val <= mMaxVals[2]))
					c3 = false;
			}
		}
		
    	if (mCriteria4Active) {
    		if (mCriteria4IsQC) {
    			short qc4 = bh.mQualityFlags[c4Pos];
				if (!(qc4 != JOAConstants.MISSINGVALUE && qc4 == mMaxVals[3]))
					c4 = false;
			}
			else {
				if (!(crit4Val > mMinVals[3] && crit4Val <= mMaxVals[3]))
					c4 = false;
			}
		}
		
		boolean cond1 = false;
		if (mCriteria1IsAnd) {
			if (mCriteria1Active && mCriteria2Active)
				cond1 = c1 && c2;
			else if (mCriteria1Active)
				cond1 = c1;
			else if (mCriteria2Active)
				cond1 = c2;
		}
		else {
			if (mCriteria1Active && mCriteria2Active)
				cond1 = c1 || c2;
			else if (mCriteria1Active)
				cond1 = c1;
			else if (mCriteria2Active)
				cond1 = c2;
		}
		
		boolean cond2 = false;
		if (mCriteria2IsAnd)
			if (mCriteria3Active && mCriteria4Active)
				cond2 = c3 && c4;
			else if (mCriteria3Active)
				cond2 = c3;
			else if (mCriteria4Active)
				cond2 = c4;
		else {
			if (mCriteria3Active && mCriteria4Active)
				cond2 = c3 || c4;
			else if (mCriteria3Active)
				cond2 = c3;
			else if (mCriteria4Active)
				cond2 = c4;
		}
			
		if ((mCriteria1Active || mCriteria2Active) && (mCriteria3Active || mCriteria4Active)) {
			if (mCrit1AndCrit2IsAnd)
				return cond1 && cond2;
			else
				return cond1 || cond2;
		}
		else if (mCriteria1Active || mCriteria2Active)
			return cond1;
		else if (mCriteria3Active || mCriteria4Active)
			return cond2;
		return true;
    
    }
    
    public boolean testValues(double crit1Val, double crit2Val, double crit3Val, double crit4Val) {
	    boolean c1 = true;
	    boolean c2 = true;
	    boolean c3 = true;
	    boolean c4 = true;
	    
    	if (mCriteria1Active) {
			if (!(crit1Val > mMinVals[0] && crit1Val <= mMaxVals[0]))
				c1 = false;
		}
		
    	if (mCriteria2Active) {
			if (!(crit2Val > mMinVals[1] && crit2Val <= mMaxVals[1]))
				c2 = false;
		}
		
    	if (mCriteria3Active) {
			if (!(crit3Val > mMinVals[2] && crit3Val <= mMaxVals[2]))
				c3 = false;
		}
		
    	if (mCriteria4Active) {
			if (!(crit4Val > mMinVals[3] && crit4Val <= mMaxVals[3]))
				c4 = false;
		}
		
		boolean cond1 = false;
		if (mCriteria1IsAnd) {
			if (mCriteria1Active && mCriteria2Active)
				cond1 = c1 && c2;
			else if (mCriteria1Active)
				cond1 = c1;
			else if (mCriteria2Active)
				cond1 = c2;
		}
		else {
			if (mCriteria1Active && mCriteria2Active)
				cond1 = c1 || c2;
			else if (mCriteria1Active)
				cond1 = c1;
			else if (mCriteria2Active)
				cond1 = c2;
		}
		
		boolean cond2 = false;
		if (mCriteria2IsAnd)
			if (mCriteria3Active && mCriteria4Active)
				cond2 = c3 && c4;
			else if (mCriteria3Active)
				cond2 = c3;
			else if (mCriteria4Active)
				cond2 = c4;
		else {
			if (mCriteria3Active && mCriteria4Active)
				cond2 = c3 || c4;
			else if (mCriteria3Active)
				cond2 = c3;
			else if (mCriteria4Active)
				cond2 = c4;
		}
			
		if ((mCriteria1Active || mCriteria2Active) && (mCriteria3Active || mCriteria4Active)) {
			if (mCrit1AndCrit2IsAnd)
				return cond1 && cond2;
			else
				return cond1 || cond2;
		}
		else if (mCriteria1Active || mCriteria2Active)
			return cond1;
		else if (mCriteria3Active || mCriteria4Active)
			return cond2;
		return true;
    
    }

    public boolean testObservation(FileViewer fv, Section sech, Bottle bh) {
	    double val;
	    int pos;
	    boolean c1 = true;
	    boolean c2 = true;
	    boolean c3 = true;
	    boolean c4 = true;
	    int qcStd = sech.getQCStandard();
	    boolean convertIGOSStoWOCE = false;
	    boolean convertWOCEtoIGOSS = false;
	    
	    convertIGOSStoWOCE = false;
	    convertWOCEtoIGOSS = false;
	    if (qcStd != mQCStandard) {
	    	if (mQCStandard == JOAConstants.WOCE_QC_STD && qcStd == JOAConstants.IGOSS_QC_STD)
	    		convertIGOSStoWOCE = true;
	    	else if (mQCStandard == JOAConstants.IGOSS_QC_STD && qcStd == JOAConstants.WOCE_QC_STD) 
	    		convertWOCEtoIGOSS = true;
	    }

	    if (mCriteria1Active) {
	    	pos = sech.getVarPos(fv.mAllProperties[mParamIndices[0]].getVarLabel(), false);
	    	if (pos < 0)
	    		return false;
	    	val = bh.mDValues[pos];
	    	if (mCriteria1IsQC) {
	    		int qc1 = bh.mQualityFlags[pos];
	    		if (convertIGOSStoWOCE)
	    			qc1 = JOAFormulas.translateIGOSSQBToWOCE(qc1);
	    		else if (convertWOCEtoIGOSS)
	    			qc1 = JOAFormulas.translateWOCESampleQBToIGOSS(qc1);

	    		if (!(qc1 != JOAConstants.MISSINGVALUE && qc1 == mMaxVals[0]))
	    			c1 = false;
	    	}
	    	else {
	    		if (!(val > mMinVals[0] && val <= mMaxVals[0]))
	    			c1 = false;
	    	}
	    }
		
    	if (mCriteria2Active) {
			pos = sech.getVarPos(fv.mAllProperties[mParamIndices[1]].getVarLabel(), false);
			if (pos < 0)
				return false;
			val = bh.mDValues[pos];
    		if (mCriteria2IsQC) {
				int qc2 = bh.mQualityFlags[pos];
    			if (convertIGOSStoWOCE)
    				qc2 = JOAFormulas.translateIGOSSQBToWOCE(qc2);
    			else if (convertWOCEtoIGOSS)
    				qc2 = JOAFormulas.translateWOCESampleQBToIGOSS(qc2);
				if (!(qc2 != JOAConstants.MISSINGVALUE && qc2 == mMaxVals[1]))
					c2 = false;
			}
			else {
				if (!(val > mMinVals[1] && val <= mMaxVals[1]))
					c2 = false;
			}
		}
		
    	if (mCriteria3Active) {
			pos = sech.getVarPos(fv.mAllProperties[mParamIndices[2]].getVarLabel(), false);
			if (pos < 0)
				return false;
			val = bh.mDValues[pos];
    		if (mCriteria3IsQC) {
				int qc3 = bh.mQualityFlags[pos];
    			if (convertIGOSStoWOCE)
    				qc3 = JOAFormulas.translateIGOSSQBToWOCE(qc3);
    			else if (convertWOCEtoIGOSS)
    				qc3 = JOAFormulas.translateWOCESampleQBToIGOSS(qc3);
				if (!(qc3 != JOAConstants.MISSINGVALUE && qc3 == mMaxVals[2]))
					c3 = false;
			}
			else {
				if (!(val > mMinVals[2] && val <= mMaxVals[2]))
					c3 = false;
			}
		}
		
    	if (mCriteria4Active) {
			pos = sech.getVarPos(fv.mAllProperties[mParamIndices[3]].getVarLabel(), false);
			if (pos < 0)
				return false;
			val = bh.mDValues[pos];
    		if (mCriteria4IsQC) {
				int qc4 = bh.mQualityFlags[pos];
    			if (convertIGOSStoWOCE)
    				qc4 = JOAFormulas.translateIGOSSQBToWOCE(qc4);
    			else if (convertWOCEtoIGOSS)
    				qc4 = JOAFormulas.translateWOCESampleQBToIGOSS(qc4);
				if (!(qc4 != JOAConstants.MISSINGVALUE && qc4 == mMaxVals[3]))
					c4 = false;
			}
			else {
				if (!(val > mMinVals[3] && val <= mMaxVals[3]))
					c4 = false;
			}
		}
		
		boolean cond1 = false;
		if (mCriteria1IsAnd) {
			if (mCriteria1Active && mCriteria2Active)
				cond1 = c1 && c2;
			else if (mCriteria1Active)
				cond1 = c1;
			else if (mCriteria2Active)
				cond1 = c2;
		}
		else {
			if (mCriteria1Active && mCriteria2Active)
				cond1 = c1 || c2;
			else if (mCriteria1Active)
				cond1 = c1;
			else if (mCriteria2Active)
				cond1 = c2;
		}
		
		boolean cond2 = false;
		if (mCriteria2IsAnd)
			if (mCriteria3Active && mCriteria4Active)
				cond2 = c3 && c4;
			else if (mCriteria3Active)
				cond2 = c3;
			else if (mCriteria4Active)
				cond2 = c4;
		else {
			if (mCriteria3Active && mCriteria4Active)
				cond2 = c3 || c4;
			else if (mCriteria3Active)
				cond2 = c3;
			else if (mCriteria4Active)
				cond2 = c4;
		}
			
		if ((mCriteria1Active || mCriteria2Active) && (mCriteria3Active || mCriteria4Active)) {
			if (mCrit1AndCrit2IsAnd)
				return cond1 && cond2;
			else
				return cond1 || cond2;
		}
		else if (mCriteria1Active || mCriteria2Active)
			return cond1;
		else if (mCriteria3Active || mCriteria4Active)
			return cond2;
		return true;
    }
    
    public int getParamIndex(int i) {
    	return mParamIndices[i];
    }
    
    public int getPopupIndex(int i) {
    	return mPopupIndices[i];
    }
    
    public int setQCStandard(int i) {
    	return mQCStandard = i;
    }
    
    public int getQCStandard() {
    	return mQCStandard;
    }
    
    public void setNumCriteria(int i) {
    	mNumCriteria = i;
    }
    
	public void setMinVal(int i, double d) {
		mMinVals[i] = d;
	}
	
	public double getMinVal(int i) {
		return mMinVals[i];
	}
	
	public void setMaxVal(int i, double d) {
		mMaxVals[i] = d;
	}
	
	public double getMaxVal(int i) {
		return mMaxVals[i];
	}
	
	public void setParamIndex(int i, int ii) {
		mParamIndices[i] = ii;
	}
	
	public void setPopupIndex(int i, int ii) {
		mPopupIndices[i] = ii;
	}
	
	public void setCriteria1IsAnd(boolean b) {
		mCriteria1IsAnd = b;
	}
	
	public boolean isCriteria1IsAnd() {
		return mCriteria1IsAnd;
	}

	public void setCriteria2IsAnd(boolean b) {
		mCriteria2IsAnd = b;
	}
	
	public boolean isCriteria2IsAnd() {
		return mCriteria2IsAnd;
	}
	
	public void setCrit1AndCrit2IsAnd(boolean b) {
		mCrit1AndCrit2IsAnd = b;
	}
	
	public boolean isCrit1AndCrit2IsAnd() {
		return mCrit1AndCrit2IsAnd;
	}
	
	public void setEnlargeSymbol(boolean b) {
		mEnlargeSymbol = b;
	}
	
	public boolean isEnlargeSymbol() {
		return mEnlargeSymbol;
	}
	
	public void setUseContrastingColor(boolean b) {
		mUseContrastingColor = b;
	}
	
	public boolean isUseContrastingColor() {
		return mUseContrastingColor;
	}
	
	public void setShowOnlyMatching(boolean b) {
		mShowOnlyMatching = b;
	}
	
	public boolean isShowOnlyMatching() {
		return mShowOnlyMatching;
	}
	
	public void setSymbolSize(int i) {
		mSymbolSize = i;
	}
	
	public int getSymbolSize() {
		return mSymbolSize;
	}
	
	public void setSymbol(int i) {
		mSymbol = i;
	}
	
	public int getSymbol() {
		return mSymbol;
	}
	
	public void setContrastingColor(Color c) {
		mContrastingColor = new Color(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	public Color getContrastingColor() {
		return mContrastingColor;
	}
	
	public String getParam(int i) {
		return mParams[i];
	}
	
	public void setParam(int i, String s) {
		mParams[i] = new String(s);
	}
        
    public void writeToLog(String preamble) throws IOException {
    	try {
	    	JOAConstants.LogFileStream.writeBytes(preamble);
			JOAConstants.LogFileStream.writeBytes("\t" + "Observation Filter: ");
			
			String crit1Str = new String();
			String crit2Str = new String();
			String crit3Str = new String();
			String crit4Str = new String();
			
			if (mCriteria1Active) {
				if (mCriteria1IsQC) {
					crit1Str = mParams[0] + " = " + String.valueOf((int)mMaxVals[0]);
				}
				else {
					String minValStr = new String("");
					String maxValStr = new String("");
					if (mMinVals[0] > -1.0e10)
						minValStr = JOAFormulas.formatDouble(mMinVals[0], 3, false) + " <=  ";
					if (mMaxVals[0] < 1.0e10)
						maxValStr =  " <=  "  + JOAFormulas.formatDouble(mMaxVals[0], 3, false);
						
					crit1Str = minValStr + mParams[0] + maxValStr;
				}
			}
			
			if (mCriteria2Active) {
				if (mCriteria2IsQC) {
					crit2Str = mParams[1] + " = " + String.valueOf((int)mMaxVals[1]);
				}
				else {
					String minValStr = new String("");
					String maxValStr = new String("");
					if (mMinVals[1] > -1.0e10)
						minValStr = JOAFormulas.formatDouble(mMinVals[1], 3, false) + " <=  ";
					if (mMaxVals[1] < 1.0e10)
						maxValStr =  " <=  "  + JOAFormulas.formatDouble(mMaxVals[1], 3, false);
						
					crit2Str = minValStr + mParams[1] + maxValStr;
				}
			}
			
			if (mCriteria3Active) {
				if (mCriteria3IsQC) {
					crit3Str = mParams[2] + " = " + String.valueOf((int)mMaxVals[2]);
				}
				else {
					String minValStr = new String("");
					String maxValStr = new String("");
					if (mMinVals[2] > -1.0e10)
						minValStr = JOAFormulas.formatDouble(mMinVals[2], 3, false) + " <=  ";
					if (mMaxVals[2] < 1.0e10)
						maxValStr =  " <=  "  + JOAFormulas.formatDouble(mMaxVals[2], 3, false);
						
					crit2Str = minValStr + mParams[2] + maxValStr;
				}
			}
			
			if (mCriteria4Active) {
				if (mCriteria4IsQC) {
					crit4Str = mParams[3] + " = " + String.valueOf((int)mMaxVals[3]);
				}
				else {
					String minValStr = new String("");
					String maxValStr = new String("");
					if (mMinVals[3] > -1.0e10)
						minValStr = JOAFormulas.formatDouble(mMinVals[3], 3, false) + " <=  ";
					if (mMaxVals[3] < 1.0e10)
						maxValStr =  " <=  "  + JOAFormulas.formatDouble(mMaxVals[3], 3, false);
						
					crit2Str = minValStr + mParams[3] + maxValStr;
				}
			}
				
			if (mCriteria1Active && mCriteria2Active)
				JOAConstants.LogFileStream.writeBytes("(");
				
			if (mCriteria1Active)
				JOAConstants.LogFileStream.writeBytes(crit1Str);
			
			if (mCriteria1Active && mCriteria2Active && mCriteria1IsAnd)
				JOAConstants.LogFileStream.writeBytes(" and ");
			else if (mCriteria1Active && mCriteria2Active && !mCriteria1IsAnd)
				JOAConstants.LogFileStream.writeBytes(" or ");
				
			if (mCriteria2Active)
				JOAConstants.LogFileStream.writeBytes(crit2Str);
			
			if (mCriteria1Active && mCriteria2Active)
				JOAConstants.LogFileStream.writeBytes(")");
				
			if ((mCriteria1Active || mCriteria2Active) && (mCriteria3Active || mCriteria4Active)) {
				if (mCrit1AndCrit2IsAnd)
					JOAConstants.LogFileStream.writeBytes(" and ");
				else if (!mCrit1AndCrit2IsAnd)
					JOAConstants.LogFileStream.writeBytes(" or ");
			}
			
			if (mCriteria3Active && mCriteria4Active)
				JOAConstants.LogFileStream.writeBytes("(");
				
			if (mCriteria3Active)
				JOAConstants.LogFileStream.writeBytes(crit3Str);
			
			if (mCriteria3Active && mCriteria4Active && mCriteria2IsAnd)
				JOAConstants.LogFileStream.writeBytes(" and ");
			else if (mCriteria3Active && mCriteria4Active && !mCriteria2IsAnd)
				JOAConstants.LogFileStream.writeBytes(" or ");
				
			if (mCriteria4Active)
				JOAConstants.LogFileStream.writeBytes(crit4Str);
			
			if (mCriteria3Active && mCriteria4Active)
				JOAConstants.LogFileStream.writeBytes(")");

			JOAConstants.LogFileStream.writeBytes("\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
		ex.printStackTrace();
			throw ex;
		}
    }
}
    
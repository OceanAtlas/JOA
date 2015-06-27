/*
 * $Id: CustomCalculation.java,v 1.3 2005/09/07 18:41:12 oz Exp $
 *
 */

package javaoceanatlas.calculations;

public class CustomStnCalculation {
    boolean mObsCalc = true;
    String mCalcString = null;
    String mParamName = null;
    boolean mIsCalculated = false;
    int mOperator;
    double mConstant1, mConstant2;
    String mOperand1, mOperand2, mUnits;
    boolean mReverseY;
    boolean mIsTransform = false;
    
    public CustomStnCalculation(String calcString, String newParam, String units, String operand1, String operand2, int op, double constant1, double constant2, boolean reverseY) {
    	mCalcString = new String(calcString);
    	mParamName = new String(newParam);
    	mUnits = new String(units);
    	if (operand1 != null)
    		mOperand1 = new String(operand1);
    	if (operand2 != null)
    		mOperand2 = new String(operand2);
    	mOperator = op;
    	mConstant1 = constant1;
    	mConstant2 = constant2;
    	mReverseY = reverseY;
    }
    
    public int getOperator() {
    	return mOperator;
    }
    
    public String getOperand1() {
    	return mOperand1;
    }
    
    public String getOperand2() {
    	return mOperand2;
    }
    
    public String getParamName() {
    	return mParamName;
    }
    
    public String getUnits() {
    	return mUnits;
    }
    
    public String getCalcString() {
    	return mCalcString;
    }
    
    public void setIsCalculated() {
    	mIsCalculated = true;
	}
    
	public boolean isCalculated() {
		return mIsCalculated;
	}
    
	public boolean isReverseY() {
		return mReverseY;
	}
    
	public double getConstant1() {
		return mConstant1;
	}
    
	public double getConstant2() {
		return mConstant2;
	}
    
    public void setIsTransform() {
    	mIsTransform = true;
	}
    
	public boolean isTransform() {
		return mIsTransform;
	}
}
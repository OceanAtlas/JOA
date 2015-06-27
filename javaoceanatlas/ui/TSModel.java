/**
 * 
 */
package javaoceanatlas.ui;

import java.awt.Graphics;
import java.util.Vector;
import javaoceanatlas.utility.JOAFormulas;

/**
 * @author oz
 *
 */
public class TSModel {
	private Vector<TSModelTermUI> mAdditionalTerms = new Vector<TSModelTermUI>();
	private double mIntercept = 0.0;
	private boolean mIsZRangeConstrained = false;
	private double mZMin;
	private double mZMax;
	private boolean mIsTRangeConstrained = false;
	private double mTMin;
	private double mTMax;
	
	public TSModel() {}
	
	public String toString() {
		String formulaString = "";
		formulaString += JOAFormulas.formatDouble(mIntercept, 2, false);
		
		for (TSModelTermUI mt : mAdditionalTerms) {
			formulaString += " + ";
			formulaString += mt.toString();
		}
		
		return formulaString;
	}
	
	public void drawFormattedModel(Graphics g) {
		
	}

	public void setIntercept(double mIntercept) {
	  this.mIntercept = mIntercept;
  }

	public double getIntercept() {
	  return mIntercept;
  }

	public void setAdditionalTerms(Vector<TSModelTermUI> mAdditionalTerms) {
	  this.mAdditionalTerms = mAdditionalTerms;
  }

	public Vector<TSModelTermUI> getAdditionalTerms() {
	  return mAdditionalTerms;
  }
	
	public void setZRangeConstraint(double min, double max) {
		 mIsZRangeConstrained = true;
		 mZMin = min;
		 mZMax = max;
	}

	public void setZRangeConstrained(boolean mIsRangeConstrained) {
	  this.mIsZRangeConstrained = mIsRangeConstrained;
  }

	public boolean isZRangeConstrained() {
	  return mIsZRangeConstrained;
  }

	public void setZMinConstraint(double mMin) {
	  this.mZMin = mMin;
  }

	public double getZMinConstraint() {
	  return mZMin;
  }

	public void setZMaxConstraint(double mMax) {
	  this.mZMax = mMax;
  }

	public double getZMaxConstraint() {
	  return mZMax;
  }
	
	public void setTRangeConstraint(double min, double max) {
		 mIsTRangeConstrained = true;
		 mTMin = min;
		 mTMax = max;
	}

	public void setTRangeConstrained(boolean mIsTRangeConstrained) {
	  this.mIsTRangeConstrained = mIsTRangeConstrained;
  }

	public boolean isTRangeConstrained() {
	  return mIsTRangeConstrained;
  }

	public void setTMinConstraint(double mMin) {
	  this.mTMin = mMin;
  }

	public double getTMinConstraint() {
	  return mTMin;
  }

	public void setTMaxConstraint(double mMax) {
	  this.mTMax = mMax;
  }

	public double getTMaxConstraint() {
	  return mTMax;
  }
	
}

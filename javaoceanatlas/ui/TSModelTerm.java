/**
 * 
 */
package javaoceanatlas.ui;

import javaoceanatlas.utility.JOAFormulas;

/**
 * @author oz
 *
 */
public class TSModelTerm {
	private double mConstant = 0.0;
	private double mExponent = 1.0;
	private TSModelTermParameter mParam = TSModelTermParameter.TEMPERATURE;
	
	public TSModelTerm(double constant, double exp, TSModelTermParameter param) {
		mConstant = constant;
		mExponent = exp;
		mParam = param;
	}

	public void setConstant(double c) {
	  mConstant = c;
  }

	public void setEponent(double e) {
		mExponent = e;
  }

	public double getConstant() {
			return mConstant;
  }

	public double getExponent() {
			return mExponent;
  }
	
	public TSModelTermParameter getParam() {
		return mParam;
	}

	public String toHTMLToo() {
		double scaler = getConstant();
		double exp = getExponent();
		String sign = " + ";
		if (scaler < 0) {
			sign = " - ";
			scaler = -scaler;
		}
		
		String formulaString = sign;
		formulaString += String.valueOf(scaler);
		
		formulaString += getParam();
		if (exp >  1) {
			formulaString += "<sup>";
			formulaString += String.valueOf((int)exp);
			formulaString += "</sup>";
		}
		return formulaString;
	}

	public String toHTML() {
		double scaler = getConstant();
		double exp = getExponent();
		String sign = " + ";
		if (scaler < 0) {
			sign = " - ";
			scaler = -scaler;
		}
		
		String formulaString = sign;
		formulaString += String.valueOf(scaler);
		
		formulaString += getParam();
		if (exp >  1) {
			formulaString += "^";
			formulaString += String.valueOf((int)exp);
		}
		return formulaString;
	}
}

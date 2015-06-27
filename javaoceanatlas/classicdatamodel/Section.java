/*
 * $Id: Section.java,v 1.7 2005/06/17 18:02:04 oz Exp $
 *
 */

package javaoceanatlas.classicdatamodel;

import java.awt.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

public class Section {
	public int mOrdinal;
	public StnsLRVector mStations = new StnsLRVector();
	public String mSectionDescription;
	private String mShipCode;
	public int mNumProperties = 0;
	public String mProperties[] = new String[1000];
	public String mUnits[] = new String[1000];
	public String mOriginalUnits[] = new String[1000];
	public int mNumCasts = 0, mNumVars = 0;
	public String[] mStnProperties = new String[100];
	public String[] mStnUnits = new String[100];
	public String[] mStnMethod = new String[100];
	public int mNumStnVars = 0;
	public Color mSectionColor;
	public int mNumStnProperties = 0;
	public int mQCStandard = JOAConstants.NONE_QC_STD;
	private String mExpoCode = null;

	public Section(int ord, String descrip, String ship, int numCasts, int numVars) {
		mOrdinal = ord;
		mSectionDescription = new String(descrip);
		setShipCode(new String(ship));
		mNumCasts = numCasts;
		mNumVars = numVars;
	}

	public Section(Section inSec) {
		mOrdinal = inSec.mOrdinal;
		mSectionDescription = new String(inSec.mSectionDescription);
		setShipCode(new String(inSec.getShipCode()));
		mNumCasts = inSec.mNumCasts;
		mNumVars = inSec.mNumVars;
		mExpoCode = inSec.mExpoCode;
		mQCStandard = inSec.mQCStandard;
		mNumStnProperties = inSec.mNumStnProperties;
		mNumProperties = inSec.mNumProperties;
		mSectionColor = new Color(inSec.mSectionColor.getRed(), inSec.mSectionColor.getGreen(), inSec.mSectionColor.getBlue());

		for (int i=0; i<inSec.mNumProperties; i++) {
			mProperties[i] = new String(inSec.mProperties[i]);
			mUnits[i] = new String(inSec.mUnits[i]);
			mOriginalUnits[i] = new String(inSec.mOriginalUnits[i]);
		}

		for (int i=0; i<inSec.mNumStnProperties; i++) {
			mStnProperties[i] = new String(inSec.mStnProperties[i]);
			mStnUnits[i] = new String(inSec.mStnUnits[i]);
			mStnMethod[i] = new String(inSec.mStnMethod[i]);
		}
		
		// copy the stations
    for (int stc = 0; stc < inSec.mStations.size(); stc++) {
      Station sh = (Station)inSec.mStations.elementAt(stc);
      if (!sh.mUseStn) {
        continue;
      }
      mStations.add(new Station(sh));
    }
	}

	public Station getCurrStation() {
		return mStations.getCurrElement();
	}

	public void addNewVarToSection(String varLabel, String units) {
		mProperties[mNumProperties] = new String(varLabel);
		mUnits[mNumProperties] = new String(units);
		mOriginalUnits[mNumProperties] = new String(units);
		mNumProperties++;
		mNumVars = mNumProperties;

		// set new values to missing
		for (int s = 0; s < mStations.size(); s++) {
			Station sh = (Station) mStations.elementAt(s);
			for (int b = 0; b < sh.getNumBottles(); b++) {
				Bottle bh = (Bottle) sh.mBottles.elementAt(b);
				bh.mDValues[mNumProperties - 1] = JOAConstants.MISSINGVALUE;
			}
		}
	}

	public void changeUnits(String varLabel, String units) {
		int pos = this.getVarPos(varLabel, false);
		if (pos > 0) {
			this.setIthParamName(pos, varLabel,  units, false, "foo");
		}
	}

	public void addNewStnVarToSection(String varLabel, String units, String method) {
		mStnProperties[mNumStnProperties] = new String(varLabel);
		mStnUnits[mNumStnProperties] = new String(units);
		mStnMethod[mNumStnProperties] = new String(method);
		mNumStnProperties++;
		mNumStnVars = mNumStnProperties;
	}

	public void setIthParamName(int i, String newName, String units, boolean original, String method) {
		mProperties[i] = null;
		mProperties[i] = new String(newName);
		mUnits[i] = null;
		mUnits[i] = new String(units);
		if (original)
			mOriginalUnits[i] = new String(units);
		mStnMethod[i] = null;
		mStnMethod[i] = new String(method);
	}

	public void setIthParamName(int i, String newName) {
		mProperties[i] = null;
		mProperties[i] = new String(newName);
	}

	public int getPRESVarPos() {
		int retVal = -1;
		int i = 0;
		while (i < mNumProperties) {
			if (mProperties[i].equalsIgnoreCase("depth")) {
				retVal = i;
				break;
			}
			if (mProperties[i].equalsIgnoreCase("pres")) {
				retVal = i;
				break;

			}
			if (mProperties[i].equalsIgnoreCase("deph")) {
				retVal = i;
				break;

			}
			if (mProperties[i].equalsIgnoreCase("pressure")) {
				retVal = i;
				break;

			}
			if (mProperties[i].equalsIgnoreCase("p")) {
				retVal = i;
				break;

			}
			if (mProperties[i].equalsIgnoreCase("dep")) {
				retVal = i;
				break;

			}
			if (mProperties[i].equalsIgnoreCase("ctdprs")) {
				retVal = i;
				break;

			}
			if (mProperties[i].equalsIgnoreCase("ctdp")) {
				retVal = i;
				break;

			}
			if (mProperties[i].equalsIgnoreCase("dept")) {
				retVal = i;
				break;

			}

			if (mProperties[i].equalsIgnoreCase("z")) {
				retVal = i;
				break;

			}
		}
		return retVal;
	}

	public int getVarPos(String inVar, boolean translate) {
		int retVal = -1;
		if (translate) {
			// have to compare translated names
			for (int p = 0; p < this.getNumParams(); p++) {
				String tparam = new String(this.getParam(p));
				tparam = tparam.toUpperCase();

				// translate to JOA parameter names
				tparam = JOAFormulas.paramNameToJOAName(tparam);
				String tinparam = JOAFormulas.paramNameToJOAName(inVar);

				if (tparam.equalsIgnoreCase(tinparam)) {
					retVal = p;
					break;
				}
			}
		}
		else if (!translate) {
			for (int p = 0; p < this.getNumParams(); p++) {
				String tparam = new String(this.getParam(p));
				tparam = tparam.toUpperCase();

				if (tparam.equalsIgnoreCase(inVar)) {
					retVal = p;
					break;
				}
			}
		}
		else {
			for (int i = 0; i < mNumProperties; i++) {
				if (mProperties[i].equalsIgnoreCase(inVar)) {
					retVal = i;
					break;
				}
			}

			if (retVal == -1) {
				for (int i = 0; i < mNumProperties; i++) {
					if (inVar.indexOf(mProperties[i]) >= 0) {
						retVal = i;
						break;
					}
				}
			}
		}
		return retVal;
	}

	public int getNumParams() {
		return mNumProperties;
	}

	public String getParam(int i) {
		return mProperties[i];
	}

	public String getParamUnits(int i) {
		return mUnits[i];
	}

	public String getOriginalParamUnits(int i) {
		if (mOriginalUnits[i] == null)
			return mUnits[i];
		return mOriginalUnits[i];
	}

	public int getNumStnVars() {
		return mNumStnProperties;
	}

	public String getStnVar(int i) {
		return mStnProperties[i];
	}

	public String getStnVarUnits(int i) {
		return mStnUnits[i];
	}

	public String getStnVarMethod(int i) {
		return mStnMethod[i];
	}

	public int getStnVarPos(String inVar) {
		for (int i = 0; i < mNumStnProperties; i++) {
			if (mStnProperties[i].equalsIgnoreCase(inVar)) { return i; }
		}
		return -1;
	}

	public int getQCStandard() {
		return mQCStandard;
	}

	public void setQCStandard(int qc) {
		mQCStandard = qc;
	}

	public void setOrdinal(int ord) {
		mOrdinal = ord;
	}

	// returns the position of a property in the mStnProperties array
	int getPropertyPos(String inVar) {
		int i = 0;
		while (i < mStnProperties.length) {
			if (inVar.equalsIgnoreCase(mStnProperties[i])) { return (i); }
			i++;
		}

		i = 0;
		while (i < mStnProperties.length) {
			if (mStnProperties[i].indexOf(inVar) >= 0) { return (i); }
			i++;
		}

		return (-1);
	}

	public String getID() {
		return mSectionDescription;
	}

	public String getExpoCode() {
		return mExpoCode;
	}

	public void setExpoCode(String s) {
		mExpoCode = s;
	}

	/**
   * @param mShipCode the mShipCode to set
   */
  public void setShipCode(String mShipCode) {
	  this.mShipCode = mShipCode;
  }

	/**
   * @return the mShipCode
   */
  public String getShipCode() {
	  return mShipCode;
  }
}

/*
 * $Id: ProfileResults.java,v 1.5 2005/09/20 22:06:01 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.utility;

public class ProfileResults {
  boolean[] mUseResults = new boolean[8];
  String mDBKey = new String();
  double[] mResults = new double[8];
  String[] mVariableNames = new String[8];

  public ProfileResults() {
    for (int i = 0; i < 8; i++) {
      mUseResults[i] = false;
    }
  }

  public void addResult(int resultFld, String dbkey, String varName, double result) {
    mUseResults[resultFld] = true;
    mDBKey = new String(dbkey);
    mVariableNames[resultFld] = new String(varName);
    mResults[resultFld] = result;
  }

  public String getDBKey() {
    return mDBKey;
  }

  public String getVarName(int resultFld) {
    return mVariableNames[resultFld];
  }

  public double getResultValue(int resultFld) {
    return mResults[resultFld];
  }

  public boolean isResultValueUsed(int resultFld) {
    return mUseResults[resultFld];
  }

  public void dump(Logger log) {
    for (int i = 0; i < 8; i++) {
      if (mUseResults[i]) {
        log.logMessage("ProfileResults " + i + " " + mDBKey + " " + mVariableNames[i] + " = " + mResults[i], true);
      }
    }
  }

  public void dump() {
    for (int i = 0; i < 8; i++) {
      if (mUseResults[i]) {
	System.out.println("ProfileResults " + i + " " + mDBKey + " " + mVariableNames[i] + " = " + mResults[i]);
      }
    }
  }

}

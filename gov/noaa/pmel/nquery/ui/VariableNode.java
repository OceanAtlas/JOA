/*
 * $Id: VariableNode.java,v 1.2 2004/09/14 19:11:26 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.ui;

import javax.swing.JFrame;

public class VariableNode {
  String mVariableName;
  String mJOAName;
  String description;
  String varUnits;
  boolean mIsSelected = true;
  boolean mIsEditable = false;
  boolean mIsItalic = false;
  JFrame mParentFrame;

  public VariableNode(JFrame par, String name, String joaname, String units, String desc, boolean editable, boolean italic) {
    mVariableName = name;
    mJOAName = joaname;
    description = desc;
    varUnits = units;
    mIsEditable = editable;
    mParentFrame = par;
    mIsItalic = italic;
  }

  public String toString() {
    if (mIsEditable) {
      return mVariableName + " " + varUnits + "...";
    }
    else {
      if (mJOAName != null)
	return mVariableName + " (" + mJOAName + ") " + varUnits;
      else
	return mVariableName + " " + varUnits;
    }
  }

  public boolean isItalic() {
    return mIsItalic;
  }

  public void setItalic(boolean b) {
    mIsItalic = b;
  }

  public String getName() {
    return mVariableName;
  }

  public String getJOAName() {
    return mJOAName;
  }

  public String getUnits() {
    return varUnits;
  }

  public String getDescription() {
    return varUnits;
  }

  public boolean isSelected() {
    return mIsSelected;
  }

  public void setSelected(boolean b) {
    mIsSelected = b;
  }

  public boolean isEditable() {
    return mIsEditable;
  }
}

package javaoceanatlas.utility;

/*
 * $Id: CriteriaContainer.java,v 1.8 2004/09/14 19:11:26 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */


import javax.swing.JPanel;
import java.util.Vector;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.Orientation;
import java.awt.Component;
import javaoceanatlas.ui.MergeControlPanel;

/**
 * <code>CriteriaContainer</code> Container for individual search criterion panels .
 *
 * @author oz
 * @version 1.0
 */

@SuppressWarnings("serial")
public class MergeCriteriaContainer extends JPanel {
  Vector<String> mParams;
  MergeControlPanel mParent;
  boolean mEnabled = true;

  public MergeCriteriaContainer(Vector<String> params, MergeControlPanel par) {
    mParams = params;
    mParent = par;
    this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));

    // add first criterion panel
    this.add(new MergeCriterionPanel(mParams, true, this), 0);
  }

  public MergeCriteriaContainer() {}

  // API
  public void setEnabled(boolean b) {
    mEnabled = b;
    Component[] comps = this.getComponents();
    for (int i = 0; i < comps.length; i++) {
      MergeCriterionPanel c = (MergeCriterionPanel)comps[i];
      c.setEnabled(b);
    }
  }

  public boolean isEnabled() {
    return mEnabled;
  }

  public boolean isQueryComplete() {
    Component[] comps = this.getComponents();
    for (int i = 0; i < comps.length; i++) {
      MergeCriterionPanel c = (MergeCriterionPanel)comps[i];
      if (!c.isCriterionComplete()) {
        return false;
      }
    }
    return true;
  }

  public void addCriterion(MergeCriterionPanel afterPanel) {
    int pos = -99;
    boolean isFirst = true;
    if (afterPanel != null) {
      Component[] comps = this.getComponents();
      for (int i = 0; i < comps.length; i++) {
        MergeCriterionPanel c = (MergeCriterionPanel)comps[i];
        if (c == afterPanel) {
          pos = i + 1;
	  isFirst = false;
          break;
        }
      }
    }
    else {
      pos = 0;
      isFirst = false;
    }
    this.add(new MergeCriterionPanel(mParams, isFirst, this), pos);
    mParent.invalidate();
    mParent.validate();
    mParent.scroll();
  }

  public void removeCriterion(MergeCriterionPanel oldPanel) {
    // remove this panel from the view heirarchy
    this.remove(oldPanel);
    this.invalidate();
    this.validate();
    this.updateUI();
    //mCriteriaScroller.scrollRectToVisible(new Rectangle(1000, 1000, 1000, 1000));
  }

  public void setParams(Vector<String> inparams) {
    mParams = inparams;
    Component[] comps = this.getComponents();
    for (int i = 0; i < comps.length; i++) {
      MergeCriterionPanel c = (MergeCriterionPanel)comps[i];
      c.setParams(mParams);
    }
  }

  public boolean isChanged() {
    Component[] comps = this.getComponents();
    for (int i = 0; i < comps.length; i++) {
      MergeCriterionPanel c = (MergeCriterionPanel)comps[i];
      if (c.isChanged()) {
        return true;
      }
    }
    return false;
  }

  public String[] getClauses() {
    Component[] comps = this.getComponents();
    String[] retClauses = new String[comps.length];
    for (int i = 0; i < comps.length; i++) {
      MergeCriterionPanel c = (MergeCriterionPanel)comps[i];
      int p = c.getParam();
      int crit = c.getCriterion();
      retClauses[i] = new String(crit + ":" + p);
    }
    return retClauses;
  }
}

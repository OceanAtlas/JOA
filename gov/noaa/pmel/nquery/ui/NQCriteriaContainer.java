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

package gov.noaa.pmel.nquery.ui;

import javax.swing.JPanel;
import java.util.Vector;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.Orientation;
import java.awt.Component;

/**
 * <code>CriteriaContainer</code> Container for individual search criterion panels .
 *
 * @author oz
 * @version 1.0
 */

public class NQCriteriaContainer extends JPanel {
  Vector mParams;
  DatabaseDocument mParent;
  boolean mEnabled = true;

  public NQCriteriaContainer(DatabaseDocument par, Vector params) {
    mParent = par;
    mParams = params;
    this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 10));
  }

  public NQCriteriaContainer() {

  }

  public NQCriteriaContainer(DatabaseDocument par) {
    mParent = par;
  }


  // API
  public void setEnabled(boolean b) {
    mEnabled = b;
    Component[] comps = this.getComponents();
    for (int i = 0; i < comps.length; i++) {
      NQCriterionPanel c = (NQCriterionPanel)comps[i];
      c.setEnabled(b);
    }
  }

  public boolean isEnabled() {
    return mEnabled;
  }

  public boolean isQueryComplete() {
    Component[] comps = this.getComponents();
    for (int i = 0; i < comps.length; i++) {
      NQCriterionPanel c = (NQCriterionPanel)comps[i];
      if (!c.isCriterionComplete()) {
        return false;
      }
    }
    return true;
  }

  public void addCriterion(NQCriterionPanel afterPanel) {
    Component[] comps = this.getComponents();
    int pos = -99;
    for (int i = 0; i < comps.length; i++) {
      NQCriterionPanel c = (NQCriterionPanel)comps[i];
      if (c == afterPanel) {
        pos = i + 1;
        break;
      }
    }
    this.add(new NQCriterionPanel(mParams, false, this), pos);
    mParent.invalidate();
    mParent.validate();
    mParent.scroll();
  }

  public void removeCriterion(NQCriterionPanel oldPanel) {
    // remove this panel from the view heirarchy
    this.remove(oldPanel);
    this.invalidate();
    this.validate();
    this.updateUI();
    mParent.scroll();
  }

  public void setParams(Vector inparams) {
    mParams = inparams;
    Component[] comps = this.getComponents();
    for (int i = 0; i < comps.length; i++) {
      NQCriterionPanel c = (NQCriterionPanel)comps[i];
      c.setParams(mParams);
    }
  }

  public boolean isChanged() {
    Component[] comps = this.getComponents();
    for (int i = 0; i < comps.length; i++) {
      NQCriterionPanel c = (NQCriterionPanel)comps[i];
      if (c.isChanged()) {
        return true;
      }
    }
    return false;
  }

  public String[] getClauses(boolean shortenParams) {
    Component[] comps = this.getComponents();
    String[] retClauses = new String[comps.length];
    for (int i = 0; i < comps.length; i++) {
      NQCriterionPanel c = (NQCriterionPanel)comps[i];
      retClauses[i] = new String(c.getClause(shortenParams));
    }
    return retClauses;
  }
}

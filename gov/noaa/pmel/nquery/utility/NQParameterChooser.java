/*
 * $Id: ParameterChooser.java,v 1.6 2005/09/20 22:06:01 oz Exp $
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

import javax.swing.JPanel;
import java.util.ArrayList;
import java.awt.Component;
import javax.swing.JList;
import java.util.Vector;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;
import java.util.Iterator;
import gov.noaa.pmel.eps2.ExportVariable;

/**
 * <code>ParameterChooser</code> UI for presenting a scrolling list of variables.
 *
 * @author John Osborne
 * @version $Revision: 1.6 $, $Date: 2005/09/20 22:06:01 $
 */

public class NQParameterChooser extends JPanel {
  protected ArrayList mVarList;
  protected String mTitle;
  protected Component mComp;
  protected JList mList = null;
  protected String mProtoCell = null;
  protected Vector mListAdditions = new Vector();
  protected int mVisibleRows = 6;
  protected JLabel l1;
  private boolean mAllowMultipleSelection = false;

  public NQParameterChooser(ArrayList varList, String title, Component comp) {
    mVarList = varList;
    mTitle = title;
    mComp = comp;
  }

  public NQParameterChooser(ArrayList varList, String title, Component comp, int numVisible) {
    mVarList = varList;
    mTitle = title;
    mComp = comp;
    mVisibleRows = numVisible;
  }

  public NQParameterChooser(ArrayList varList, String title, Component comp, int numVisible, String prototypeCell,
                            boolean allowMultiple) {
    this(varList, title, comp, numVisible, prototypeCell);
    mAllowMultipleSelection = allowMultiple;
  }

  public NQParameterChooser(ArrayList varList, String title, Component comp, int numVisible, String prototypeCell) {
    mVarList = varList;
    mTitle = title;
    mComp = comp;
    mVisibleRows = numVisible;
    mProtoCell = prototypeCell;
  }

  public NQParameterChooser(ArrayList varList, String title, Component comp, String prototypeCell) {
    mVarList = varList;
    mTitle = title;
    mComp = comp;
    mProtoCell = prototypeCell;
  }

  public void init() {
    this.setLayout(new BorderLayout(5, 5));
    l1 = new JLabel(mTitle, JLabel.LEFT);
    this.add(l1, "North");
    buildList();
    mList.setVisibleRowCount(mVisibleRows - 1);
    if (mAllowMultipleSelection) {
      mList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
    else {
      mList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    if (mProtoCell != null) {
      mList.setPrototypeCellValue(mProtoCell);
    }
    else {
      mList.setPrototypeCellValue("SALT         ");
    }
    JScrollPane listScroller = new JScrollPane(mList);
    this.add(listScroller, "Center");
    mList.addListSelectionListener((ListSelectionListener)mComp);
  }

  public JList getJList() {
    return mList;
  }

  public void setEnabled(boolean state) {
    if (state == false) {
      this.getJList().clearSelection();
    }
    this.getJList().setEnabled(state);
    l1.setEnabled(state);
    l1.invalidate();
  }

  public void addToList(String inValue) {
    mListAdditions.addElement(inValue);
    buildList();
  }

  protected void buildList() {
    Vector listData = new Vector();
    Iterator itor = mVarList.iterator();
    while (itor.hasNext()) {
      ExportVariable var = (ExportVariable)itor.next();
      listData.addElement(var.getPresentationVarName());
    }

    // deal with additions
    for (int i = 0; i < mListAdditions.size(); i++) {
      listData.addElement((String)mListAdditions.elementAt(i));
    }
    if (mList == null) {
      mList = new JList(listData);
    }
    else {
      mList.setListData(listData);
      mList.invalidate();
    }
  }

  public void setSelectedLine(int line) {
    mList.setSelectedIndex(line);
  }

  public void setSelectedLine(ExportVariable inVar) {
    Iterator itor = mVarList.iterator();
    int line = 0;
    while (itor.hasNext()) {
      ExportVariable var = (ExportVariable)itor.next();
      if (var.getVarName().equalsIgnoreCase(inVar.getVarName())) {
        mList.setSelectedIndex(line);
        break;
      }
      line++;
    }
  }

  public void clearSelection() {
    this.getJList().clearSelection();
  }
}

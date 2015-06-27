/*
 * $Id: ParameterChooser.java,v 1.4 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.util.*;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.ui.widgets.*;

@SuppressWarnings("serial")
public class StationVarChooser extends JPanel {
  protected FileViewer mFileViewer;
  protected String mTitle;
  protected Component mComp;
  protected JOAJList mList = null;
  protected String mProtoCell = null;
  protected Vector<String> mListAdditions = new Vector<String>();
  protected int mVisibleRows = 6;
  protected JOAJLabel l1;
  private boolean mAllowMultipleSelection = false;

  public StationVarChooser(FileViewer fv, String title, Component comp) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
  }

  public StationVarChooser(FileViewer fv, String title, Component comp, int numVisible) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
    mVisibleRows = numVisible;
  }

  public StationVarChooser(FileViewer fv, String title, Component comp, int numVisible, String prototypeCell,
                          boolean allowMultiple) {
    this(fv, title, comp, numVisible, prototypeCell);
    mAllowMultipleSelection = allowMultiple;
  }

  public StationVarChooser(FileViewer fv, String title, Component comp, int numVisible, String prototypeCell) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
    mVisibleRows = numVisible;
    mProtoCell = prototypeCell;
  }

  public StationVarChooser(FileViewer fv, String title, Component comp, String prototypeCell) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
    mProtoCell = prototypeCell;
  }

  public StationVarChooser(FileViewer fv, String title, Component comp, String prototypeCell, boolean allowMultiple) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
    mProtoCell = prototypeCell;
    mAllowMultipleSelection = allowMultiple;
  }

  public void init() {
    this.setLayout(new BorderLayout(5, 5));
    l1 = new JOAJLabel(mTitle, JOAJLabel.LEFT);
    this.add(l1, BorderLayout.NORTH);
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
      mList.setPrototypeCellValue("SALT                          ");
    }
    JScrollPane listScroller = new JScrollPane(mList);
    this.add(listScroller, BorderLayout.CENTER);
    mList.addListSelectionListener((ListSelectionListener)mComp);
  }

  public JOAJList getJList() {
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

  private void buildList() {
		Vector<String> params = new Vector<String>();
		OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.currElement();
		Section sech = (Section) of.mSections.currElement();
		for (int i = 0; i < sech.getNumStnVars(); i++) {
			params.addElement(sech.getStnVar(i));
		}

		if (mList == null) {
			mList = new JOAJList(params);
		}
		else {
			mList.setListData(params);
			mList.invalidate();
		}
  }

  public void setSelectedLine(int line) {
    mList.setSelectedIndex(line);
  }

  public void clearSelection() {
    this.getJList().clearSelection();
  }

  public String getSelectedVariable() {
    return (String)(mList.getSelectedValue());
  }
}


/*
 * $Id: SectionChooser.java,v 1.4 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.util.*;
import javaoceanatlas.ui.widgets.*;

@SuppressWarnings("serial")
public class SectionChooser extends JPanel {
  protected FileViewer mFileViewer;
  protected String mTitle;
  protected Component mComp;
  protected JOAJList mList = null;
  protected String mProtoCell = null;
  protected Vector<String> mListAdditions = new Vector<String>();
  protected int mVisibleRows = 6;
  protected JOAJLabel l1;
  private boolean mAllowMultipleSelection = false;
  Vector<JOAViewer> mAllFVs;

  public SectionChooser(FileViewer fv, String title, Component comp) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
    mAllFVs = mFileViewer.getOpenFileViewers();
  }

  public SectionChooser(FileViewer fv, String title, Component comp, int numVisible) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
    mVisibleRows = numVisible;
    mAllFVs = mFileViewer.getOpenFileViewers();
  }

  public SectionChooser(FileViewer fv, String title, Component comp, int numVisible, String prototypeCell,
                        boolean allowMultiple) {
    this(fv, title, comp, numVisible, prototypeCell);
    mAllowMultipleSelection = allowMultiple;
  }

  public SectionChooser(FileViewer fv, String title, Component comp, int numVisible, String prototypeCell) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
    mVisibleRows = numVisible;
    mProtoCell = prototypeCell;
    mAllFVs = mFileViewer.getOpenFileViewers();
  }

  public SectionChooser(FileViewer fv, String title, Component comp, String prototypeCell) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
    mProtoCell = prototypeCell;
    mAllFVs = mFileViewer.getOpenFileViewers();
  }

  public SectionChooser(FileViewer fv, String title, Component comp, String prototypeCell, boolean allowMultiple) {
    mFileViewer = fv;
    mTitle = title;
    mComp = comp;
    mProtoCell = prototypeCell;
    mAllowMultipleSelection = allowMultiple;
    mAllFVs = mFileViewer.getOpenFileViewers();
  }

  public void init() {
    this.setLayout(new BorderLayout(5, 5));
    l1 = new JOAJLabel(mTitle, JOAJLabel.LEFT);
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

  protected void buildList() {
    Vector<String> listData = new Vector<String>();

    for (int i = 0; i < mAllFVs.size(); i++) {
      FileViewer fv = (FileViewer)mAllFVs.elementAt(i);
      listData.addElement(fv.getTitle());
    }

    // deal with additions
    for (int i = 0; i < mListAdditions.size(); i++) {
      listData.addElement((String)mListAdditions.elementAt(i));
    }

    if (mList == null) {
      mList = new JOAJList(listData);
    }
    else {
      mList.setListData(listData);
      mList.invalidate();
    }
    //mList.setFont(new Font("dialog", Font.PLAIN, 11));
  }

  public void setSelectedLine(int line) {
    mList.setSelectedIndex(line);
  }

  public void clearSelection() {
    this.getJList().clearSelection();
  }

  public FileViewer getSelectedFileViewer() {
    int selParam = mList.getSelectedIndex();
    if (selParam >= 0) {
      return (FileViewer)mAllFVs.elementAt(selParam);
    }
    else {
      return null;
    }
  }

}

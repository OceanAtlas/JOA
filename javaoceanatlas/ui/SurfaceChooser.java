/*
 * $Id: SurfaceChooser.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.util.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;

@SuppressWarnings("serial")
public class SurfaceChooser extends JPanel {
	FileViewer mFileViewer;
	String mTitle;
	Component mComp;
	JOAJList mList = null;
	int mVisibleRows = 6;
	
    public SurfaceChooser(FileViewer fv, String title, Component comp) {
    	mFileViewer = fv;
    	mTitle = title;
    	mComp = comp;
	}
	
    public SurfaceChooser(FileViewer fv, String title, Component comp, int numRows) {
    	mFileViewer = fv;
    	mTitle = title;
    	mComp = comp;
    	mVisibleRows = numRows;
	}
    
    public void init() {
    	this.setLayout(new BorderLayout(5, 5));
    	JOAJLabel l1 = new JOAJLabel(mTitle, JOAJLabel.LEFT);
    	this.add(l1, "North");
    	Vector<String> listData = JOAFormulas.getSurfaceList();
    	mList = new JOAJList(listData);
    	mList.setVisibleRowCount(mVisibleRows);
    	mList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	mList.setPrototypeCellValue("PRES-0-1000 (63)_srf.xml ");
    	JScrollPane listScroller = new JScrollPane(mList);
    	this.add(listScroller, "Center");
    	mList.addListSelectionListener((ListSelectionListener)mComp);
    }
    
    public JOAJList getJList() {
    	return mList;
    }
}
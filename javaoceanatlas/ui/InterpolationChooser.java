/*
 * $Id: InterpolationChooser.java,v 1.2 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import javaoceanatlas.ui.widgets.*;

@SuppressWarnings("serial")
public class InterpolationChooser extends JPanel {
	FileViewer mFileViewer;
	String mTitle;
	Component mComp;
	JOAJList mList = null;
	
    public InterpolationChooser(FileViewer fv, String title, Component comp) {
    	mFileViewer = fv;
    	mTitle = title;
    	mComp = comp;
	}
    
    public void init() {
    	this.setLayout(new BorderLayout(5, 5));
    	JOAJLabel l1 = new JOAJLabel(mTitle, JOAJLabel.LEFT);
    	this.add(l1, "North");
    	mList = new JOAJList();
    	mList.setVisibleRowCount(6);
    	mList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	mList.setPrototypeCellValue("SALT on PRES;0-6000@1000.000/td ");
    	JScrollPane listScroller = new JScrollPane(mList);
    	listScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    	this.add(listScroller, "Center");
    	mList.addListSelectionListener((ListSelectionListener)mComp);
    }
    
    public JOAJList getJList() {
    	return mList;
    }
}
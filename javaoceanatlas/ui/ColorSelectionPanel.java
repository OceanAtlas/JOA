/*
 * $Id: ColorSelectionPanel.java,v 1.2 2005/06/17 18:08:51 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.swing.border.*;
import gov.noaa.pmel.swing.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ColorSelectionPanel extends JPanel {
	private Color mFG, mBG;
	private Swatch mBGSwatch = null;
	private Swatch mFGSwatch = null;
	
    public ColorSelectionPanel(Color fg, Color bg) {
    	mFG = fg;
    	mBG = bg;
	}
    
    public ColorSelectionPanel() {
    	this(Color.black, Color.white);
    	init();
    }
    
    protected void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
    	this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));
    	this.add(new JOAJLabel("Background color:"));
    	mBGSwatch = new Swatch(mBG, 12, 12);
    	this.add(mBGSwatch);
    	this.add(new JOAJLabel("   "));
    	this.add(new JOAJLabel("Axes and labels color:"));
    	mFGSwatch = new Swatch(mFG, 12, 12);
    	this.add(mFGSwatch);
    	TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kColors"));
		if (JOAConstants.ISMAC) {
			//tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
    	this.setBorder(tb);
    }
    
    public Color getFGColor() {
    	return mFGSwatch.getColor();
    }
    
    public Color getBGColor() {
    	return mBGSwatch.getColor();
    }
}
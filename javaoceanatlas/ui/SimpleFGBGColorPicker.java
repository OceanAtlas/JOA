/*
 * $Id: SimpleFGBGColorPicker.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.border.*;
import gov.noaa.pmel.swing.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class SimpleFGBGColorPicker extends JPanel implements ItemListener {
	Color mFG, mBG;
    Swatch mPlotBg = null;
    Swatch mAxesColor = null;
    JOAJComboBox mPresetColorSchemes = null;
	
	public SimpleFGBGColorPicker(Color bg, Color fg) {
		mFG = fg;
		mBG = bg;
		
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	
		this.setLayout(new BorderLayout(0, 0));
		
		JPanel colorNamePanel = new JPanel();
		colorNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			    
		// swatches
		colorNamePanel.add(new JOAJLabel(b.getString("kBackgroundColor")));
		mPlotBg = new Swatch(mBG, 12, 12);
		colorNamePanel.add(mPlotBg);
		colorNamePanel.add(new JOAJLabel("  "));
		colorNamePanel.add(new JOAJLabel(b.getString("kGridColor")));
		mAxesColor = new Swatch(mFG, 12, 12);
		colorNamePanel.add(mAxesColor);
		colorNamePanel.add(new JOAJLabel("  "));
		colorNamePanel.add(new JOAJLabel(b.getString("kColorSchemes")));
		Vector<String> presetSchemes = new Vector<String>();
		presetSchemes.addElement(b.getString("kDefault"));
		presetSchemes.addElement(b.getString("kWhiteBackground"));
		presetSchemes.addElement(b.getString("kBlackBackground"));
		mPresetColorSchemes = new JOAJComboBox(presetSchemes);
		mPresetColorSchemes.setSelectedItem(b.getString("kDefault"));
		mPresetColorSchemes.addItemListener(this);
		colorNamePanel.add(mPresetColorSchemes);
		
		// add the title
    	TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kColors"));
		if (JOAConstants.ISMAC) {
			//tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		this.setBorder(tb);
		
		// add the color panel
		this.add("North", new TenPixelBorder(colorNamePanel, 0, 0, 5, 0));
	}
	
	public void itemStateChanged(ItemEvent evt) {
    	if (evt.getSource() instanceof JOAJComboBox) {
    		JOAJComboBox cb = (JOAJComboBox)evt.getSource();
    		if (cb == mPresetColorSchemes) {
	    		int colorScheme = cb.getSelectedIndex();
	    		if (colorScheme == 0) {
	    			// default
					mPlotBg.setColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
					mAxesColor.setColor(Color.black);
	    		}
	    		else if (colorScheme == 1) {
	    			// white bg
					mPlotBg.setColor(Color.white);
					mAxesColor.setColor(Color.black);
	    		}
	    		else {
	    			// color bg
					mPlotBg.setColor(Color.black);
					mAxesColor.setColor(Color.white);
	    		}
	    	}
    	}
    }
    
    public Color getBGColor() {
    	return mPlotBg.getColor();
    }
    
    public Color getFGColor() {
    	return mAxesColor.getColor();
    }
}
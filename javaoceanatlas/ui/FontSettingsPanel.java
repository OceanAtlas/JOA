/*
 * $Id: FontSettingsPanel.java,v 1.3 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import javaoceanatlas.ui.widgets.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class FontSettingsPanel extends JPanel {
	JOAJLabel mLabel = null;
	JOAJLabel mLabel2 = null;
	Color mColor = Color.black;
	int mFontSize = -99;
	int mFontStyle = Font.PLAIN;
	String mFont;
	Font[] mFonts;
	JOAJComboBox mFontList;
	JOAJComboBox mStyles;
	JSpinner mFontSizer;
	Swatch colorSwatch;
	
	public FontSettingsPanel(Font[] fonts) {
		this(fonts, null, 12, 0, Color.black);
	}
	
	public FontSettingsPanel(Font[] fonts, String cFont, int size, int style, Color c) { 
		this.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		mFontSize = size;
		mFontStyle = style;
		mColor = new Color(c.getRed(),c.getGreen(), c.getBlue());
		mFont = new String(cFont);
		mFonts = fonts;
		
		// build the font popup
    	Vector<String> listData = new Vector<String>();
    	int currFont = 0;
		for (int i=0; i<mFonts.length; i++) {
			listData.addElement(mFonts[i].getName());
			if (mFont.equalsIgnoreCase(mFonts[i].getName()))
				currFont = i;
		}
		mFontList = new JOAJComboBox(listData);
		this.add(mFontList);
		
		// size
		SpinnerNumberModel model = new SpinnerNumberModel(mFontSize, 6, 100, 1); 
		mFontSizer = new JSpinner(model);
			
		this.add(mFontSizer);
		this.add(new JOAJLabel("(pt)"));
		
		// build the style popup
    	Vector<String> styleData = new Vector<String>();
		styleData.addElement("Plain");
		styleData.addElement("Bold");
		styleData.addElement("Italic");
		styleData.addElement("Bold + Italic");
		mStyles = new JOAJComboBox(styleData);
		this.add(mStyles);
		
		// add the color swatch
    	colorSwatch = new Swatch(mColor);
		this.add(colorSwatch);
		
		// set to selected font
		if (mFont != null) {
			mFontList.setSelectedIndex(currFont);
		}
		
		// set the selected Inded
		mStyles.setSelectedIndex(mFontStyle);
	}
		
	public String getFontName() {
		// return curr element of font popup
		return (String)mFontList.getSelectedItem();
	}
	
	public void setFontName(String font) {
    	int currFont = 0;
    	mFont = null;
		for (int i=0; i<mFonts.length; i++) {
			if (font.equalsIgnoreCase(mFonts[i].getName())) {
				currFont = i;
				mFont = new String(mFonts[i].getName());
				mFontList.setSelectedIndex(currFont);
				break;
			}
		}
	}
		
	public int getFontStyle() {
		// return curr element of font popup
		return mStyles.getSelectedIndex();
	}
	
	public void setFontStyle(int style) {
		mStyles.setSelectedIndex(style);
	}
		
	public int getFontSize() {
		// return curr element of font popup
		return ((Integer)mFontSizer.getValue()).intValue();
	}
	
	public void setFontSize(int size) {
		mFontSizer.setValue(new Integer(size));
	}
    	
	public Color getColor() {
		return colorSwatch.getColor();
	}
    	
	public void setColor(Color c) {
		colorSwatch.setColor(c);
	}
}
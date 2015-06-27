/*
 * $Id: Swatch.java,v 1.2 2005/02/15 18:31:10 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.colorchooser.*;
import java.beans.*;

public class Swatch extends JPanel implements MouseListener {
	Color mColor, mNewColor;
	int mHeight, mWidth;
	Component mParent = null;
	JPanel mThis;
	Graphics thisG;
	String mPropertyName = new String("property");
	boolean mEditable = true;
	
    public Swatch(Color inColor) {
    	this(inColor, 10, 10);
    }
    
    public void setEditable(boolean mode) {
    	mEditable = mode;
    }
    
    public Swatch(Color inColor, int width, int height) {
    	mParent = this.getParent();
    	mColor = inColor;
    	mHeight = height + 3;
    	mWidth = width + 3;
		addMouseListener(this);
		mThis = this;
    }
    
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (mEditable) {
			// paint the drop shadow
			g.setColor(this.getBackground().darker());
			g.fillRect(3, 3, mWidth-1, mHeight-1);
			g.setColor(mColor);
			g.fillRect(1, 1, mWidth-4, mHeight-4);
		}
		else {
			g.setColor(mColor);
			g.fillRect(2, 2, mWidth-3, mHeight-3);
		}
		thisG = g;
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(mWidth, mHeight);
	}
	
	public Insets getInsets() {
		return new Insets(0, 0, 0, 0);
	}
	
	public Color getColor() {
		return mColor;
	}
	
	public void setColor(Color inColor) {
		mColor = inColor;
		Graphics g = mThis.getGraphics();
		if (g != null) {
			paintComponent(g);
			g.dispose();
		}
	}
	
	public void mouseClicked(MouseEvent me) {
		if (!mEditable)
			return;
		// get a new color
		Color oldColor = mColor;
		mColor = JColorChooser.showDialog(mParent, "Choose a new color:", mColor);
		if (mColor == null)
			mColor = oldColor;
		else {
			Graphics g = mThis.getGraphics();
			if (g != null) {
				paintComponent(g);
				g.dispose();
			}
		}
	}
	
	public void mousePressed(MouseEvent me) {
	}
	
	public void mouseReleased(MouseEvent me) {
	}
	
	public void mouseEntered(MouseEvent me) {
	}
	
	public void mouseExited(MouseEvent me) {
	}
}
/*
 * $Id: SimpleColorPalettePanel.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui;

import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.events.*;
import javaoceanatlas.utility.*;

@SuppressWarnings("serial")
public class SimpleColorPalettePanel extends ColorPalettePanel {
    
	public SimpleColorPalettePanel(ColorPalette inPalette) {
		super(inPalette);
	}
	
	public SimpleColorPalettePanel() {
		super();
	}
	
	public void init() {
		addMouseListener(new MyMouseHandler());
		mThis = this;
    	mParent = this.getParent();
	}
	
	public class MyMouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent me) {
			// turn click into a color cell
			int x = me.getX();
			int y = me.getY();
			int row = y/12 + 1;
			int col = x/12 + 1;
			mSelectedEntry = (row-1) * 16 + col - 1;
			mFirstHilited = mSelectedEntry;
			mRangeHilited = false;
			mLastHilited = -99;
			Graphics g = mThis.getGraphics();
			if (g != null) {
				removeHilite(g);
				paintHilite(g);
				g.dispose();
				
				// post a custom event
				ColorSelChangedEvent csce = new ColorSelChangedEvent((ColorPalettePanel)mThis);
				csce.setColor(mColorPalette.getColor(mSelectedEntry), mSelectedEntry);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(csce);
			}
		}
	}
}
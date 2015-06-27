/*
 * $Id: ColorFillIcon.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;
import java.awt.*;

public class ColorFillIcon implements Icon {
	public ColorFillIcon(Color fill, 
							int width,
							int height,
							int borderSize) {
		this.fillColor		= fill;
		this.borderSize		= borderSize;
		this.width			= width;
		this.height			= height;
		this.shadow			= Color.black;
		this.fillWidth		= width - 2 * borderSize;
		this.fillHeight		= height - 2 * borderSize;
	}

	public ColorFillIcon(Color fill, int size) {
		this(fill, size, size, BORDER_SIZE);
	}

	public ColorFillIcon(Color fill) {
		this(fill, DEFAULT_SIZE, DEFAULT_SIZE, BORDER_SIZE);
	}
	
	// Set non-default shadow color
	public void setShadow(Color c){
		shadow = c;
	}

	// Change the main color
	public void setFillColor(Color c) {
		fillColor = c;
	}

	// The Icon interface
	public int getIconWidth() {
		return width;
	}

	public int getIconHeight() {
		return height;
	}

	public void paintIcon(Component comp, Graphics g, int x, int y) {
		Color c = g.getColor();
		
		// Draw the border
		if (borderSize > 0) {
			g.setColor(shadow);				
			for (int i = 0; i < borderSize; i++) {
				g.drawRect(x + i, y + i, width - 2 * i - 1, height - 2 * i - 1);
			}		
		}

		// Fill the remainder of the icon
		g.setColor(fillColor);
		g.fillRect(x + borderSize, y + borderSize, fillWidth, fillHeight);

		g.setColor(c);

	}

	// Icon state
	protected int width;		// Color fill width
	protected int height;		// Color fill height
	protected Color fillColor;	// Color to fill with
	protected Color shadow;		// Shadow color
	protected int borderSize;	// Border size in pixels
	protected int fillHeight;	// Height of area to fill
	protected int fillWidth;	// Width of area to fill

	// Constants
	public static final int BORDER_SIZE = 2;
	public static final int DEFAULT_SIZE = 32; 
}

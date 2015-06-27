/*
 * $Id: TenPixelBorder.java,v 1.2 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class TenPixelBorder extends JPanel {
	private int left=10, right=10, top=10, bottom=10;

	public TenPixelBorder(Container borderMe) {
		setLayout(new BorderLayout());
		add("Center", borderMe);
	}

	public TenPixelBorder(Component borderMe, int inTop, int inLeft, int inBottom, int inRight) {
		setLayout(new BorderLayout());
		add("Center", borderMe);
		left = inLeft;
		top = inTop;
		bottom = inBottom;
		right = inRight;
	}

	public TenPixelBorder(Container borderMe, int inTop, int inLeft, int inBottom, int inRight) {
		setLayout(new BorderLayout());
		add("Center", borderMe);
		left = inLeft;
		top = inTop;
		bottom = inBottom;
		right = inRight;
	}

	public TenPixelBorder(Container borderMe, int inTop, int inLeft, int inBottom, int inRight, Color bgColor) {
		setLayout(new BorderLayout());
		add("Center", borderMe);
		this.setBackground(bgColor);
		left = inLeft;
		top = inTop;
		bottom = inBottom;
		right = inRight;
	}

	public Insets getInsets() {
		return new Insets(top, left, bottom, right);
	}

	/*public void update( Graphics g ) { this.paint(g); }
        public void paint( Graphics g )
        {
            Dimension dim = this.getSize();
            //g.setColor(this.getBackground());
			//g.fillRect(0, 0, dim.width, dim.height);
        }*/

}

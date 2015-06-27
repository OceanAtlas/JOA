/*
 * $Id: TenPixelBorder.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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

import java.awt.*;
import javax.swing.*;

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

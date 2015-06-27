/*
 * $Id: Bargauge.java,v 1.2 2005/06/17 18:08:51 oz Exp $
 *
 */

package javaoceanatlas.ui;

import java.awt.*;
import javax.swing.*;
import javaoceanatlas.utility.*;

/**
 * A bargauge which can be filled (wholly or partially) with a client-specified
 * color. Fill color is specified at construction time; both fill color and fill
 * percent may be set after construction time.
 * <p>
 * 
 * @version 1.0, Apr 1 1996
 * @author David Geary
 * @see ThreeDRectangle
 * @see gjt.test.BargaugeTest
 */
@SuppressWarnings("serial")
public class Bargauge extends JPanel {
	private double percentFill = 0;
	private ThreeDRectangle border = new ThreeDRectangle(this);
	private Color fillColor;
	private Color bgColor;

	public Bargauge(Color fillColor, Color bgColor) {
		setFillColor(fillColor);
		setBGColor(bgColor);
		this.setOpaque(true);
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	public void setBGColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public void setFillPercent(double percentage) {
		// Assert.notFalse(percentage >= 0 && percentage <= 100);
		percentFill = percentage;
		fill();
	}

	public double getFillPercent() {
		// Assert.notFalse(percentage >= 0 && percentage <= 100);
		return percentFill;
	}

	public void setSize(int w, int h) {
		setBounds(getLocation().x, getLocation().y, w, h);
	}

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		border.setSize(w, h);
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public Dimension getPreferredSize() {
		int w = border.getThickness() * 3;
		return new Dimension(w, w * 4);
	}

	public void paint(Graphics g) {
		Rectangle d = this.getBounds();
		g.setColor(this.bgColor);
		g.fillRect(d.x, d.y, d.width, d.height);
		fill();
	}

	public void fill() {
		Graphics g = getGraphics();
		if ((g != null) && (percentFill > 0)) {
			Rectangle b = border.getInnerBounds();
			int fillw = b.width;
			int fillh = b.height;

			if (b.width > b.height)
				fillw *= percentFill / 100;
			else
				fillh *= percentFill / 100;

			g.setColor(Color.black);
			g.drawRect(b.x, b.y, b.width - 1, b.height - 1);

			g.setColor(fillColor);

			if (b.width > b.height)
				g.fillRect(b.x, b.y, fillw, b.height);
			else
				g.fillRect(b.x, b.y + b.height - fillh, b.width - 1, fillh);
		}
		else if ((g != null) && (percentFill == 0)) {
			Rectangle b = border.getInnerBounds();

			g.setColor(Color.black);
			g.drawRect(b.x, b.y, b.width - 1, b.height - 1);

			g.setColor(bgColor);
			if (b.width > b.height)
				g.fillRect(b.x + 1, b.y + 1, b.width - 1, b.height - 1);
			else
				g.fillRect(b.x, b.y + b.height - b.height, b.width - 1, b.height);

		}
		if (g != null)
			g.dispose();
	}

	public void clear() {
		border.clearInterior();
	}

	protected String paramString() {
		Dimension size = getSize();
		Orientation orient = size.width > size.height ? Orientation.HORIZONTAL : Orientation.VERTICAL;
		String str = "fill percent=" + percentFill + "," + "orientation=" + orient + "," + "color" + fillColor;
		return str;
	}
}

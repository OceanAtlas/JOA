package gov.noaa.pmel.eps2;

import java.awt.*;

/**
 * A bargauge which can be filled (wholly or partially) with a 
 * client-specified color.  Fill color is specified at 
 * construction time; both fill color and fill percent may be 
 * set after construction time.<p>
 *
 * @version 1.0, Apr 1 1996
 * @author  David Geary
 * @see     ThreeDRectangle
 * @see     gjt.test.BargaugeTest
 */
public class EPSBargauge extends Canvas {
    private double          percentFill = 0;
    private ThreeDRectangle border = new ThreeDRectangle(this);
    private Color           fillColor;

    public EPSBargauge(Color fillColor) {
        setFillColor(fillColor);
    }
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }
    public void setFillPercent(double percentage) {
        //Assert.notFalse(percentage >= 0 && percentage <= 100);
        percentFill = percentage;
    }
    public void setSize(int w, int h) { 
        setBounds(getLocation().x, getLocation().y, w, h);
    }
    public void setBounds(int x, int y, int w, int h) { 
        super.setBounds(x,y,w,h);
        border.setSize(w,h); 
    }
    public Dimension getMinimumSize() { return getPreferredSize(); }

    public Dimension getPreferredSize() {
        int w = border.getThickness() * 3;
        return new Dimension(w, w*4);
    }
    public void paint(Graphics g) {
        border.raise();
        border.paint();
        fill();
    }
    public void fill() {
        Graphics g = getGraphics();

        if ((g != null) && (percentFill > 0)) {
            Rectangle b       = border.getInnerBounds();
            int       fillw   = b.width; 
            int       fillh   = b.height;

            if (b.width > b.height)
            	fillw *= percentFill/100;
            else
            	fillh *= percentFill/100;

            g.setColor(fillColor);
            //border.clearInterior();

            if(b.width > b.height) 
                g.fillRect(b.x, b.y, fillw, b.height-1);
            else                   
                g.fillRect(b.x, b.y + b.height - fillh, 
                           b.width-1, fillh);
        }
		if (g != null)
			g.dispose();
    }
    protected String paramString() {
        Dimension size = getSize();
        Orientation orient = size.width > size.height ? 
                             Orientation.HORIZONTAL :
                             Orientation.VERTICAL;
        String    str  = "fill percent=" + percentFill + "," + 
                         "orientation="  + orient      + "," +
                         "color"         + fillColor;
        return str;
    }
}

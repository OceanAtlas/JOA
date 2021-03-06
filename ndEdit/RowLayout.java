/*
 * $Id: RowLayout.java,v 1.2 2005/02/15 18:31:10 oz Exp $
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

/**
 * RowLayout lays out components in a row.  At construction
 * time, both horizontal orientation and vertical orientation
 * may be specified, along with the gap to use between
 * components.<p>
 *
 * Horizontal orientation must be one of the following:
 * <dl>
 * <dd> LEFT
 * <dd> CENTER
 * <dd> RIGHT
 * </dl>
 *
 * Vertical orientation must be one of the following:
 * <dl>
 * <dd> TOP
 * <dd> CENTER
 * <dd> BOTTOM
 * </dl>
 *
 * @version 1.0, Apr 1 1996
 * @author  David Geary
 * @see     ColumnLayout
 * @see     Orientation
 */
public class RowLayout implements LayoutManager {
    static private int _defaultGap = 5;

     int gap;
     Orientation verticalOrientation;
     Orientation horizontalOrientation;

    public RowLayout() {
        this(Orientation.CENTER,
             Orientation.CENTER, _defaultGap);
	}
    public RowLayout(int gap) {
        this(Orientation.CENTER, Orientation.CENTER, gap);
    }
    public RowLayout(Orientation horizontalOrient,
                     Orientation verticalOrient) {
        this(horizontalOrient, verticalOrient, _defaultGap);
    }
    public RowLayout(Orientation horizontalOrient,
                     Orientation verticalOrient, int gap) {
        Assert.isTrue(gap >= 0);
        Assert.isTrue(
            horizontalOrient == Orientation.LEFT   ||
            horizontalOrient == Orientation.CENTER ||
            horizontalOrient == Orientation.RIGHT);
        Assert.isTrue(
            verticalOrient   == Orientation.TOP    ||
            verticalOrient   == Orientation.CENTER ||
            verticalOrient   == Orientation.BOTTOM);

        this.gap                   = gap;
        this.verticalOrientation   = verticalOrient;
        this.horizontalOrientation = horizontalOrient;
    }

    public void addLayoutComponent(String name, Component comp) {
    }
    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container target) {
        Insets    insets      = target.getInsets();
        Dimension dim         = new Dimension(0,0);
        int       ncomponents = target.getComponentCount();
        Component comp;
        Dimension d;

        for (int i = 0 ; i < ncomponents ; i++) {
            comp = target.getComponent(i);

            if(comp.isVisible()) {
                d = comp.getPreferredSize();

                dim.width  += d.width;
                dim.height  = Math.max(d.height, dim.height);

                if(i > 0) dim.width += gap;
            }
        }
        dim.width  += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;

        return dim;
    }
    public Dimension minimumLayoutSize(Container target) {
        Insets    insets      = target.getInsets();
        Dimension dim         = new Dimension(0,0);
        int       ncomponents = target.getComponentCount();
        Component comp;
        Dimension d;

        for (int i = 0 ; i < ncomponents ; i++) {
            comp = target.getComponent(i);

            if(comp.isVisible()) {
                d = comp.getMinimumSize();

                dim.width  += d.width;
                dim.height  = Math.max(d.height, dim.height);

                if(i > 0) dim.width += gap;
            }
        }
        dim.width  += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;

        return dim;
    }
    public void layoutContainer(Container target) {
        Insets    insets      = target.getInsets();
        int       ncomponents = target.getComponentCount();
        int       top         = 0;
        int       left        = insets.left;
        Dimension tps         = target.getPreferredSize();
        Dimension targetSize  = target.getSize();
        Component comp;
        Dimension ps;

        if(horizontalOrientation == Orientation.CENTER)
            left = left + (targetSize.width/2) - (tps.width/2);
        if(horizontalOrientation == Orientation.RIGHT)
            left = left + targetSize.width - tps.width;

        for (int i = 0 ; i < ncomponents ; i++) {
            comp = target.getComponent(i);

            if(comp.isVisible()) {
                ps  = comp.getPreferredSize();

                if(verticalOrientation == Orientation.CENTER)
                    top = (targetSize.height/2) - (ps.height/2);
                else if(verticalOrientation == Orientation.TOP)
                    top = insets.top;
                else if(
                    verticalOrientation == Orientation.BOTTOM)
                    top = targetSize.height -
                          ps.height - insets.bottom;

                //System.out.println("RowLayout: left: " + left + " top: " + top + " width: " + ps.width + " height: " + ps.height);
                comp.setBounds(left,top,ps.width,ps.height);
                left += ps.width + gap;
                comp.repaint();
            }
        }
    }
}

/*
 * $Id: HorizontalBargauge.java,v 1.2 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import java.awt.*;
import javaoceanatlas.utility.*;

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
@SuppressWarnings("serial")
public class HorizontalBargauge extends Bargauge {
    public HorizontalBargauge(Color fillColor, Color bgColor) {
        super(fillColor, bgColor);
    }

    public Dimension getPreferredSize() {
        return new Dimension(100,20);
    }
}

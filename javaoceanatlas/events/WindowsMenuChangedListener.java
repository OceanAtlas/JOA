/*
 * $Id: WindowsMenuChangedListener.java,v 1.2 2005/06/17 18:02:59 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package javaoceanatlas.events;

import java.awt.*;
import java.awt.event.*;

public interface WindowsMenuChangedListener {
    public void windowsMenuChanged(WindowsMenuChangedEvent evt);
}
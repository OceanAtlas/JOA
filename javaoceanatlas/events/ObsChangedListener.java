/*
 * $Id: ObsChangedListener.java,v 1.2 2005/06/17 18:02:59 oz Exp $
 *
 */

package javaoceanatlas.events;

import java.awt.*;
import java.awt.event.*;

public interface ObsChangedListener {
    public void obsChanged(ObsChangedEvent evt);
}
/*
 * $Id: JOAJTabbedPane.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJTabbedPane extends JTabbedPane {
  public JOAJTabbedPane() {
    super();
  }

  public JOAJTabbedPane(int place) {
    super(place);
  }

  public Dimension getMaximumSize() {
    return new Dimension(this.getWidth(), 100);
  }
}

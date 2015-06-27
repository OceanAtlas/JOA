/*
 * $Id: CloseableFrame.java,v 1.2 2005/06/17 18:08:51 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.utility.*;

@SuppressWarnings("serial")
public class CloseableFrame extends JFrame implements WindowListener {
  protected boolean centered;
  Container container;

  public CloseableFrame(Frame parent) {
    this.addWindowListener(this);
    container = parent;
  }

  public CloseableFrame(Frame parent, String title) {
    super(title);
    this.addWindowListener(this);
    container = parent;
  }

  public CloseableFrame(Frame parent, String title, boolean centered) {
    super(title);
    this.addWindowListener(this);
    setCentered(centered);
    container = parent;
  }

  public void setCentered(boolean centered) {
    this.centered = centered;
  }

  public void disposeOfMe() {
    Frame f = Util.getFrame(this);

    this.dispose();
    f.toFront();
  }

  public void windowClosing(WindowEvent e) {
    //this.dispose();
    disposeOfMe();
  }

  public void windowOpened(WindowEvent e) {}

  public void windowClosed(WindowEvent e) {}

  public void windowIconified(WindowEvent e) {}

  public void windowDeiconified(WindowEvent e) {}

  public void windowActivated(WindowEvent e) {}

  public void windowDeactivated(WindowEvent e) {}

  public void locateOnParentFrame() {
    // Center over parent
    Rectangle bounds = null;
    Point location = container.getLocation();
    Dimension cd = container.getSize();
    bounds = new Rectangle(location.x, location.y, cd.width, cd.height);
    Rectangle abounds = getBounds();
    int x = bounds.x + (bounds.width - abounds.width) / 2;
    int y = bounds.y + (bounds.height - abounds.height) / 2;

    // Clip to screen. If the dialog extends off the screen,
    // center of screen rather than parent.
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    if (sd.width > 0 && sd.height > 0) {
      Rectangle scr = new Rectangle(0, 0, sd.width - abounds.width, sd.height - abounds.height);

      // Oops. Some portion of the dialog is off the screen. Screw the parent, lets
      // center on the screen.
      if (!scr.contains(new Point(x, y))) {
        x = bounds.x + (sd.width - abounds.width) / 2;
        y = bounds.y + (sd.height - abounds.height) / 2;
      }
    }
    setLocation(x, y);
  }

  public void setVisible(boolean b) {
    locateOnParentFrame();
    super.setVisible(b);
  }
}

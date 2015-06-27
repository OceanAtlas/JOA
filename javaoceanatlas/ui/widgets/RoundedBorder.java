package javaoceanatlas.ui.widgets;

import java.awt.Insets;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Graphics;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RoundedBorder implements Border {

  private int radius;

  RoundedBorder(int radius) {
    this.radius = radius;
  }

  public Insets getBorderInsets(Component c) {
    return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
  }

  public boolean isBorderOpaque() {
    return true;
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
  }
}

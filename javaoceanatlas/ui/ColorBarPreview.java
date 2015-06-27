/*
 * $Id: ColorBarPreview.java,v 1.5 2005/06/17 18:08:51 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ColorBarPreview extends JPanel {
  Color[] mColors = null;
  SwatchGroup mSwatchGroup = null;
  SimpleColorPalettePanel mColorPalette = null;
  JOAFocusableSwatch[] swatches = null;
  MyScroller swatchScroller;
  JPanel swatchPane = new JPanel();

  public ColorBarPreview(SwatchGroup sg, SimpleColorPalettePanel colorPalette) {
    mSwatchGroup = sg;
    mColorPalette = colorPalette;
    swatchPane.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 0));
    swatchScroller = new MyScroller(swatchPane);
    this.add(swatchScroller);
  }

  public ColorBarPreview(SwatchGroup sg, SimpleColorPalettePanel colorPalette, Color[] inColors) {
    mSwatchGroup = sg;
    mColorPalette = colorPalette;
    swatchPane.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 0));
    swatchScroller = new MyScroller(swatchPane);
    this.add(swatchScroller);
    setColors(inColors);
  }

  public void cleanUp() {
    swatchPane.removeAll();
    swatches = null;
  }

  //public Dimension getPreferredSize() {
  //	return new Dimension(25, 200);
  //}

  public Color[] getColors() {
    // update the colors from the swatches
    for (int i = 0; i < mColors.length; i++) {
      mColors[mColors.length - i - 1] = swatches[i].getColor();
    }
    return mColors;
  }

  public void setColors(Color[] inColors) {
    mColors = inColors;
    cleanUp();
    swatches = new JOAFocusableSwatch[mColors.length];
    int height = 8; //(this.getHeight() - 2)/mColors.length;
    //if (height < 7) {
    //	height = 7;
    //}
    for (int i = 0; i < mColors.length; i++) {
      swatches[i] = new JOAFocusableSwatch(mColors[mColors.length - i - 1], height, height, mSwatchGroup);
      swatches[i].setEditable(true);
      swatches[i].setShadowed(false);
      mColorPalette.addColorSelChangedListener(swatches[i]);
      swatchPane.add(swatches[i]);
    }
  }

  private class MyScroller extends JScrollPane {
    public MyScroller(Component c) {
      super(c);
      this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public Dimension getPreferredSize() {
      return new Dimension(35, 200);
    }
  }
}

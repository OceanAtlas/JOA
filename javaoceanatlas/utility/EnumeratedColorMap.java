package javaoceanatlas.utility;

import gov.noaa.pmel.sgt.IndexedColorMap;
import java.awt.Color;
import java.util.Arrays;

/**
 * <p>
 * Title: EnumeratedColorMap
 * </p>
 * 
 * <p>
 * Description: Allows for enumerating color-value pairs than rely on a linear
 * transformation
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author oz
 * @version 1.0
 */

public class EnumeratedColorMap extends IndexedColorMap {
  private static final long serialVersionUID = 8257083897372874917L;
  private double[] mVals;

  public EnumeratedColorMap(int[] red, int[] green, int[] blue) {
    super(red, green, blue);
    mVals = new double[red.length];
  }

  public EnumeratedColorMap(float[] red, float[] green, float[] blue) {
    super(red, green, blue);
    mVals = new double[red.length];
  }

  public EnumeratedColorMap(Color[] colors) {
    super(colors);
    mVals = new double[colors.length];
  }

  public EnumeratedColorMap(EnumeratedColorMap cbar) {
    super(cbar.getColors());
    Color[] colors = cbar.getColors();
    double[] vals = cbar.getValues();

    mVals = new double[colors.length];
    mVals = new double[colors.length];

    for (int i = 0; i < colors.length; i++) {
      mVals[i] = vals[i];
      this.colors_[i] = new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), colors[i].getAlpha());
    }
  }

  public void setEnumeratedValues(double[] vals) {
    // these are always in the base units
    mVals = vals;
  }

  public void setEnumeratedColors(Color[] colors) {
    this.colors_ = new Color[colors.length];
    for (int i = 0; i < colors.length; i++) {
      this.colors_[i] = colors[i];
    }
  }


  public double[] getValues() {
    return Arrays.copyOf(mVals,mVals.length);
  }

  public Color[] getColors() {
    Color[] colors = new Color[mVals.length];
    for (int i = 0; i < mVals.length; i++) {
      colors[i] = this.getColor(i);
    }
    return colors;
  }

  public Color getColor(double val) {
    // val has to be in the base units
    if (val <= mVals[0]) { return this.getColorByIndex(0); }
    for (int i = 1; i < mVals.length; i++) {
      if (val > mVals[i - 1] && val <= mVals[i]) { return this.getColorByIndex(i - 1); }
    }
    return this.getColorByIndex(mVals.length - 1);
  }

  public Color getColor(int i) {
    return colors_[i];
  }

  public int getNumLevels() {
    return mVals.length;
  }

  public double getBaseLevel() {
    return  mVals[0];
  }


  public double getEndLevel(boolean convertUnits) {
    return mVals[mVals.length - 1];
  }

  public double getDoubleValue(int i) {
    return mVals[i];
  }

}

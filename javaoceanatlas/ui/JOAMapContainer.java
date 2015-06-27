/*
 * $Id: JOAMapContainer.java,v 1.2 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javaoceanatlas.utility.NewColorBar;

public interface JOAMapContainer {
  public boolean isFirstPlot();
  public void setFirstPlot(boolean first);
  public void setPercentComplete(double percent);
  public double getPercentComplete();
  public void reset();
  public MapLegend getLegend();
  public ColorBarPanel getColorBarPanel(String id);
  public void setLegend(MapLegend legend);
  public void addColorBar(String id, NewColorBar cb, boolean linked);
  public void replaceColorBar(String id, NewColorBar cb);
  public void removeColorBar(String id);
  public void displayLocation(double lat, double lon);
}

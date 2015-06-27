/*
 * $Id: LineBathyRenderer.java,v 1.9 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import com.visualtek.png.PNGEncoder;
import javaoceanatlas.ui.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.MapSpecification;

public class LineBathyRenderer {
  private Image mOffScreen;
  private MapPlotPanel mMapPlotPanel;
  boolean threadFinished = false;

  public LineBathyRenderer(Image im, MapPlotPanel mpp) {
    mOffScreen = im;
    mMapPlotPanel = mpp;

		class BasicThread extends Thread {
			public void run() {
				if (threadFinished)
					return;
		    mMapPlotPanel.setBusy(true);
		    final Graphics og = mOffScreen.getGraphics();

		    // draw the background filled bathymetry from external netCDF file
		    if (mMapPlotPanel.getMapSpec().isColorFill()) {
		        mMapPlotPanel.drawFilledBathy(og);
		    }

		    // draw the isobath lines from external netCDF files
		      mMapPlotPanel.drawIsobaths(og);

		    // plot the Map
		    mMapPlotPanel.drawMap(og);

		    // draw the map graticule
		    if (mMapPlotPanel.getMapSpec().isDrawGraticule()) {
		        mMapPlotPanel.plotBorder(og);
		    }

		    //if (mMapPlotPanel.getMapSpec().isPlotSectionLabels())
		    //	mMapPlotPanel.plotSectionLabels(og);

		    //if (mMapPlotPanel.getMapSpec().isPlotStnLabels())
		    //	mMapPlotPanel.plotStationLabels(og);

		    // draw the offscreen into the graphics context of the panel
		    mMapPlotPanel.getGraphics().drawImage(mOffScreen, 2, 2, null);

		    // dispose of the offscreen
		    og.dispose();

		    // set a flag in the map container to say that we have plotted the map for the first time
		    mMapPlotPanel.getContents().setFirstPlot(false);
		    mMapPlotPanel.setBusy(false);
		    mMapPlotPanel.paintComponent(mMapPlotPanel.getGraphics());
		    threadFinished = true;
			}
		}

		// Create a thread and run it
		Thread thread = new BasicThread();
		String name = ("LineBathyRenderer Thread" + System.currentTimeMillis());
		thread.setName(name);
		thread.start();
  }
}

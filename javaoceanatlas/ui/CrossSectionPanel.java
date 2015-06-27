/*
 * $Id: CrossSectionPanel.java,v 1.15 2005/10/18 23:42:18 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import com.visualtek.png.PNGEncoder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.events.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.classicdatamodel.*;
import java.awt.geom.*;
import java.io.File;
import java.io.FilenameFilter;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class CrossSectionPanel extends RubberbandPanel implements ObsChangedListener, DialogClient, DataAddedListener,
    StnFilterChangedListener, PrefsChangedListener, ActionListener {
  public static int CSPLEFT = 5;
  public static int CSPRIGHT = 5;
  public static int CSPTOP = 5;
  public static int CSPBOTTOM = 5;
  protected LRVector mOpenFiles;
  protected FileViewer mFileViewer;
  protected int mStnDisplayOffset = JOAConstants.OFFSET_DISTANCE;
  protected boolean mStnDisplayIsReversed = false;
  protected boolean mBlackBG = false;
  protected int mColorByMode;
  protected int mStnSymbolSize = 1;
  protected double mStationDisplayHScale;
  protected double mStationDisplayVScale;
  protected ObsMarker mObsMarker = null;
  protected DialogClient mDialogClient = null;
  protected JPopupMenu mPopupMenu = null;
  protected boolean mNtoSSection = false;
  protected int currX, currY;
  private Image mOffScreen = null;
  protected double mMinDepth = 0, mMaxDepth;
  protected double mWinYOrigin;
  private Rubberband rbRect;
  BasicStroke lw2 = new BasicStroke(2);
  GeneralPath mSelection = new GeneralPath();
  String mMissingParam = null;
  double mLeftLon = 0.0;

  public CrossSectionPanel(FileViewer fv) {
    mFileViewer = fv;

    // initialize the depth range to be
    computeDepthRangeFromData();

    // compute scale for display
    computeDisplayScales();

    this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    this.addMouseListener(new MyMouseHandler());
    mFileViewer.addObsChangedListener(this);
    mFileViewer.addDataAddedListener(this);
    mFileViewer.addStnFilterChangedListener(this);
    if (PowerOceanAtlas.getInstance() != null) {
      PowerOceanAtlas.getInstance().addPrefsChangedListener(this);
    }
    rbRect = new RubberbandVLine(this);
    rbRect.setConstrainVertical(true);
    rbRect.setActive(true);
    mDialogClient = this;
  }

  public void setRubberbandDisplayObject(Object obj, boolean concat) {
    // this routine expects a Rectangle Object
    mSelection = (GeneralPath)obj;
    repaint();
  }

  public int getMinX() {
    return 0;
  }

  public int getMinY() {
    return 3;
  }

  public int getMaxX() {
    return this.getSize().width - 1;
  }

  public int getMaxY() {
    return this.getSize().height - 2;
  }

  public void rubberbandEnded(Rubberband rb) {
  }

  public void computeDepthRangeFromData() {
    mMaxDepth = JOAConstants.MISSINGVALUE;
    int p = mFileViewer.getPRESPropertyPos();
    if (p < 0) {
      return;
    }

    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          if (!sh.mUseStn) {
            continue;
          }

          // look to see if bottom is recorded
          if (sh.mBottomDepthInDBARS != JOAConstants.MISSINGVALUE) {
            double bottom = sh.mBottomDepthInDBARS;
            mMaxDepth = bottom > mMaxDepth ? bottom : mMaxDepth;
          }
        }
      }
    }

    if (mMaxDepth == JOAConstants.MISSINGVALUE || mMaxDepth <= 0.0) {
      // look for maximum pressure
      double presMax = 0.0;
      for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
        OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

        for (int sec = 0; sec < of.mNumSections; sec++) {
          Section sech = (Section)of.mSections.elementAt(sec);
          if (sech.mNumCasts == 0) {
            continue;
          }

          int pPos = sech.getPRESVarPos();
          if (pPos < 0) {
            return;
          }
          for (int stc = 0; stc < sech.mStations.size(); stc++) {
            Station sh = (Station)sech.mStations.elementAt(stc);
            for (int b = 0; b < sh.mNumBottles; b++) {
              Bottle bh = (Bottle)sh.mBottles.elementAt(b);
              double val = bh.mDValues[pPos];
              if (val != JOAConstants.MISSINGVALUE) {
                if (val > presMax) {
                  presMax = val;
                }
              }
            }
          }
        }

      }
      mMaxDepth = presMax;
    }
  }

  public void createPopup(Point point) {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
    mPopupMenu = new JPopupMenu();
    JMenuItem openContextualMenu = new JMenuItem(b.getString("kProperties"));
    openContextualMenu.setActionCommand("opencontextual");
    openContextualMenu.addActionListener(this);
    mPopupMenu.add(openContextualMenu);
    mPopupMenu.addSeparator();
    
    JMenuItem exportXSec = new JMenuItem("Export Image...");
    exportXSec.setActionCommand("exportimage");
    exportXSec.addActionListener(this);
    mPopupMenu.add(exportXSec);
    
    mPopupMenu.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    mPopupMenu.show(this, point.x, point.y);
  }

  public void showConfigDialog() {
    // show configuration dialog
    ConfigureXSecPanel cp = new ConfigureXSecPanel(mFileViewer, mDialogClient, mStnDisplayOffset, mBlackBG,
        mStnDisplayIsReversed, mColorByMode, mMissingParam, mStnSymbolSize, mMinDepth, mMaxDepth);
    cp.pack();

    // show dialog at center of screen
    Rectangle dBounds = cp.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    cp.setLocation(x, y);
    cp.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("opencontextual")) {
      showConfigDialog();
    }
    else if (cmd.equals("exportimage")) {
    	saveAsPNG();
    }
  }
  
  public void saveAsPNG() {
		class BasicThread extends Thread {
			// ask for filename
			public void run() {
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.endsWith("png")) {
							return true;
						}
						else {
							return false;
						}
					}
				};

				Frame fr = new Frame();
				String directory = System.getProperty("user.dir"); // + File.separator
				// + "JOA_Support" +
				// File.separator;
				FileDialog f = new FileDialog(fr, "Save cross section as:", FileDialog.SAVE);
				f.setDirectory(directory);
				f.setFilenameFilter(filter);
				f.setFile(mFileViewer.getTitle() + "_xsec.png");
				f.setVisible(true);
				directory = f.getDirectory();
				f.dispose();
				if (directory != null && f.getFile() != null) {
					String path = directory + File.separator + f.getFile();
					try {
						(new PNGEncoder(mOffScreen, path)).encode();
					}
					catch (Exception ex) {
					}
					try {
						JOAConstants.LogFileStream.writeBytes("Saved Cross Section in: " + path + "\n");
						JOAConstants.LogFileStream.flush();
					}
					catch (Exception ex) {
					}

				}
			}
		}

		// Create a thread and run it
		Thread thread = new BasicThread();
		thread.start();
	}

  public class MyMouseHandler extends MouseAdapter {
    public void mouseClicked(MouseEvent me) {
      if (me.getClickCount() == 2) {
        showConfigDialog();
      }
      else {
        if (me.isPopupTrigger()) {
          createPopup(me.getPoint());
        }
        else {
          // find a new observation
          findByXY(me.getX(), me.getY());
        }
      }
    }

    public void mouseReleased(MouseEvent me) {
      super.mouseReleased(me);
      setRubberbandDisplayObject(null, false);

      if (me.isPopupTrigger()) {
        createPopup(me.getPoint());
      }
      else {
        if (rbRect != null && !rbRect.isPoint() && me.getID() == MouseEvent.MOUSE_RELEASED) {
          zoomPlot(rbRect.getBounds(), me.isAltDown());
        }
      }
    }

    public void mousePressed(MouseEvent me) {
      super.mousePressed(me);
      if (me.isPopupTrigger()) {
        createPopup(me.getPoint());
      }
    }
  }

  public void zoomPlot(Rectangle newRect, boolean mode) {
    // convert corners of rectangle to new plot range
    Rectangle newBounds = rbRect.lastBounds();

    if (newBounds.height < 2) {
      return;
    }
    int y1 = newBounds.y;
    int y2 = y1 + newBounds.height;
    mMinDepth = (y1 - 4) / mStationDisplayVScale + mWinYOrigin;
    mMaxDepth = (y2 - 4) / mStationDisplayVScale + mWinYOrigin;
    this.invalidate();
    paintComponent(this.getGraphics());
    rbRect.end(rbRect.getStretched());
  }

  public void findByXY(int x, int y) {
    boolean found = false;
    double offset = 0;
    OpenDataFile foundFile = null;
    Section foundSection = null;
    Station foundStation = null;
    Bottle foundBottle = null;

    if (mStnDisplayIsReversed) {
      x = this.getSize().width - x;
    }

    double minOffset = 1.0E10;
    double testPres = (y - CSPTOP) / mStationDisplayVScale + mWinYOrigin;

    // compute the station offset
    if (mStnDisplayOffset == JOAConstants.OFFSET_LATITUDE) {
      if (!mNtoSSection) {
        offset = mFileViewer.mLatMin + (x - CSPLEFT) / mStationDisplayHScale;
      }
      else {
        offset = mFileViewer.mLatMax - (x - CSPLEFT) / mStationDisplayHScale;
      }
    }
    else if (mStnDisplayOffset == JOAConstants.OFFSET_LONGITUDE) {
      offset = mLeftLon + (x - CSPLEFT) / mStationDisplayHScale;
      if (offset > 180.0) {
        offset -= 360.0;
      }
    }
    else if (mStnDisplayOffset == JOAConstants.OFFSET_TIME) {
  		double leftTime = Math.abs((mFileViewer.getMinDate().getTime()/1000));

      offset = leftTime + (x - CSPLEFT) / mStationDisplayHScale;
    }
    else {
      offset = (x - CSPLEFT) / mStationDisplayHScale;
    }

    for (int fc = 0; fc < mFileViewer.mNumOpenFiles && !found; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections && !found; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }
        for (int stc = 0; stc < sech.mStations.size() && !found; stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          if (!sh.mUseStn) {
            continue;
          }

          if (mStnDisplayOffset == JOAConstants.OFFSET_DISTANCE) {
            double off = Math.abs(sh.mCumDist - offset);
            if (off < minOffset) {
              foundStation = sh;
              foundSection = sech;
              foundFile = of;
              minOffset = off;
            }
          }
          else if (mStnDisplayOffset == JOAConstants.OFFSET_SEQUENCE) {
            double off = Math.abs((sh.mOrdinal - 1) - offset);
            if (off < minOffset) {
              foundStation = sh;
              foundSection = sech;
              foundFile = of;
              minOffset = off;
            }
          }
          else if (mStnDisplayOffset == JOAConstants.OFFSET_LATITUDE) {
            double off;
            if (!mNtoSSection) {
              off = Math.abs(sh.mLat - offset);
            }
            else {
              off = Math.abs(sh.mLat - offset);
            }

            if (off < minOffset) {
              foundStation = sh;
              foundSection = sech;
              foundFile = of;
              minOffset = off;
            }
          }
          else if (mStnDisplayOffset == JOAConstants.OFFSET_LONGITUDE) {
            double off = Math.abs(sh.mLon - offset);
            if (off < minOffset) {
              foundStation = sh;
              foundSection = sech;
              foundFile = of;
              minOffset = off;
            }
          }
          else if (mStnDisplayOffset == JOAConstants.OFFSET_TIME) {
            double off = Math.abs((sh.getDate().getTime()/1000) - offset);
            if (off < minOffset) {
              foundStation = sh;
              foundSection = sech;
              foundFile = of;
              minOffset = off;
            }
          }
        }
      }
    }

    found = true;
    // post event so other components will update
    if (found && foundStation != null) {
      // look for bottle at same depth
      found = false;
      JOAConstants.currTestPres = testPres;
      foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
      if (foundBottle != null) {
        found = true;
      }

      if (found) {
        mFileViewer.mOpenFiles.setCurrElement(foundFile);
        foundFile.mSections.setCurrElement(foundSection);
        foundSection.mStations.setCurrElement(foundStation);
        foundStation.mBottles.setCurrElement(foundBottle);
        ObsChangedEvent oce = new ObsChangedEvent(mFileViewer);
        oce.setFoundObs(foundFile, foundSection, foundStation, foundBottle);
        paintImmediately(new Rectangle(0, 0, 1000, 1000));
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(oce);
      }
      else if (!found) {
        Toolkit.getDefaultToolkit().beep();
      }
    }
    else {
      Toolkit.getDefaultToolkit().beep();
    }
  }

  public boolean findByArrowKey(Integer direction) {
    OpenDataFile foundFile = null;
    Section foundSection = null;
    Station foundStation = null;
    Bottle foundBottle = null;
    int pPos = mFileViewer.getPRESPropertyPos();

    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    Section sech = (Section)of.mSections.currElement();
    Station sh = (Station)sech.mStations.currElement();

    // find new observation
    boolean found = false;
    switch (direction.intValue()) {
      case 1: //JOAConstants.NEXTSTN:
        // go to next station
        foundStation = (Station)sech.mStations.nextElement();
        if (foundStation == null) {
          // go to next section
          foundSection = (Section)of.mSections.nextElement();
          foundFile = of;
          if (foundSection != null) {
            foundSection.mStations.setCurrElementToFirst();
            foundStation = (Station)foundSection.mStations.currElement();
            foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
            if (foundBottle != null) {
              found = true;
            }
          }
          else {
            // look in next file
            foundFile = (OpenDataFile)mFileViewer.mOpenFiles.nextElement();

            if (foundFile != null) {
              foundSection = (Section)foundFile.mSections.currElement();
              foundStation = (Station)foundSection.mStations.currElement();
              foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
              if (foundBottle != null) {
                found = true;
              }
            }
          }
        }
        else {
          foundSection = sech;
          foundFile = of;

          // search for bottle by pressure
          foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
          found = true;
        }
        break;
      case 2: //JOAConstants.PREVSTN:
        // go to prev station
        foundStation = (Station)sech.mStations.prevElement();
        if (foundStation == null) {
          // go to next section
          foundSection = (Section)of.mSections.prevElement();
          foundFile = of;

          if (foundSection != null) {
            foundSection.mStations.setCurrElementToLast();
            foundStation = (Station)foundSection.mStations.currElement();
            foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
            if (foundBottle != null) {
              found = true;
            }
          }
          else {
            // look in next file
            foundFile = (OpenDataFile)mFileViewer.mOpenFiles.prevElement();

            if (foundFile != null) {
              foundSection = (Section)foundFile.mSections.currElement();
              foundStation = (Station)foundSection.mStations.currElement();
              foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
              if (foundBottle != null) {
                found = true;
              }
            }
          }
        }
        else {
          foundSection = sech;
          foundFile = of;

          // search for bottle by pressure
          foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
          if (foundBottle != null) {
            found = true;
          }
        }
        break;
      case 3: //JOAConstants.NEXTOBS:

        // go to next bottle
        foundBottle = (Bottle)sh.mBottles.nextElement();

        if (foundBottle != null) {
          foundStation = sh;
          foundSection = sech;
          foundFile = of;
          JOAConstants.currTestPres = foundBottle.mDValues[pPos];
          found = true;
        }
        break;
      case 4: //JOAConstants.PREVOBS:

        // go to previous bottle
        foundBottle = (Bottle)sh.mBottles.prevElement();

        if (foundBottle != null) {
          foundStation = sh;
          foundSection = sech;
          foundFile = of;
          JOAConstants.currTestPres = foundBottle.mDValues[pPos];
          found = true;
        }
        break;
    }

    // post event so other components will update
    if (found) {
      mFileViewer.mOpenFiles.setCurrElement(foundFile);
      foundFile.mSections.setCurrElement(foundSection);
      foundSection.mStations.setCurrElement(foundStation);
      foundStation.mBottles.setCurrElement(foundBottle);
      ObsChangedEvent oce = new ObsChangedEvent(mFileViewer);
      oce.setFoundObs(foundFile, foundSection, foundStation, foundBottle);
      Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(oce);
      paintImmediately(new Rectangle(0, 0, 1000, 1000));
      return true;
    }
    else {
      Toolkit.getDefaultToolkit().beep();
      return false;
    }
  }

  public void computeMarkerPos() {
    try {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
      Section sech = (Section)of.mSections.currElement();
      Station sh = (Station)sech.mStations.currElement();
      Bottle bh = (Bottle)sh.mBottles.currElement();

      int p = sech.getPRESVarPos();
      int x = 0, y;
      if (mStnDisplayOffset == JOAConstants.OFFSET_SEQUENCE) {
        x = (int)(CSPLEFT + ((sh.mOrdinal-1) * mStationDisplayHScale));
      }
      else if (mStnDisplayOffset == JOAConstants.OFFSET_DISTANCE) {
        x = (int)(CSPLEFT + (mStationDisplayHScale * sh.mCumDist));
      }
      else if (mStnDisplayOffset == JOAConstants.OFFSET_LATITUDE) {
        if (!mNtoSSection) {
          x = (int)(CSPLEFT + ((sh.mLat - mFileViewer.mLatMin) * mStationDisplayHScale));
        }
        else {
          x = (int)(CSPLEFT + ((mFileViewer.mLatMax - sh.mLat) * mStationDisplayHScale));
        }
      }
      else if (mStnDisplayOffset == JOAConstants.OFFSET_LONGITUDE) {
        double lon = sh.mLon;
        if (mLeftLon < 0) {
         if (lon < 0 && lon < mLeftLon) {
            lon += 360.0;
          }
       }
        x = (int)(CSPLEFT + ((lon - mLeftLon) * mStationDisplayHScale));
      }
      else if (mStnDisplayOffset == JOAConstants.OFFSET_TIME) {
    		double leftTime = Math.abs((mFileViewer.getMinDate().getTime()/1000));
        x = (int)(CSPLEFT + ((sh.getDate().getTime() / 1000 - leftTime) * mStationDisplayHScale));
      }

      if (mStnDisplayIsReversed) {
        x = this.getSize().width - x;
      }

      double val = bh.mDValues[p];
      y = (int)((val - mWinYOrigin) * mStationDisplayVScale);
      y += CSPTOP;
      currX = x;
      currY = y;

      if (mObsMarker == null) {
        mObsMarker = new ObsMarker(x, y, JOAConstants.DEFAULT_CURSOR_SIZE);
      }
      else {
        mObsMarker.setNewPos(x, y);
      }
    }
    catch (Exception ex) {}

  }

  public void prefsChanged(PrefsChangedEvent evt) {
    mObsMarker = null;
    mObsMarker = new ObsMarker(currX, currY, JOAConstants.DEFAULT_CURSOR_SIZE);
    this.paintComponent(this.getGraphics());
  }

  public void obsChanged(ObsChangedEvent evt) {
    // display the current observation
    // compute a new position
    int p = mFileViewer.getPRESPropertyPos();
    int x = 0, y;

    Station sh = evt.getFoundStation();

    if (mStnDisplayOffset == JOAConstants.OFFSET_SEQUENCE) {
      x = (int)(CSPLEFT + ((sh.mOrdinal - 1) * mStationDisplayHScale));
    }
    else if (mStnDisplayOffset == JOAConstants.OFFSET_DISTANCE) {
      x = (int)(CSPLEFT + (mStationDisplayHScale * sh.mCumDist));
    }
    else if (mStnDisplayOffset == JOAConstants.OFFSET_LATITUDE) {
      if (!mNtoSSection) {
        x = (int)(CSPLEFT + ((sh.mLat - mFileViewer.mLatMin) * mStationDisplayHScale));
      }
      else {
        x = (int)(CSPLEFT + ((mFileViewer.mLatMax - sh.mLat) * mStationDisplayHScale));
      }
    }
    else if (mStnDisplayOffset == JOAConstants.OFFSET_LONGITUDE) {
      double lon = sh.mLon;
      if (mLeftLon < 0) {
       if (lon < 0 && lon < mLeftLon) {
          lon += 360.0;
        }
     }
      x = (int)(CSPLEFT + ((lon - mLeftLon) * mStationDisplayHScale));
    }

    if (mStnDisplayIsReversed) {
      x = this.getSize().width - x;
    }

    Bottle bh = evt.getFoundBottle();
    double val = bh.mDValues[p];
    y = (int)((val - mWinYOrigin) * mStationDisplayVScale);
    y += CSPTOP;
    currX = x;
    currY = y;

    if (mObsMarker != null) {
      mObsMarker.setNewPos(x, y);
    }
    Graphics g = getGraphics();
    int width = this.getSize().width;
    int height = this.getSize().height;
    g.setClip(CSPLEFT - 10, CSPTOP - 10, width + 10, height + 10);
    paintImmediately(new Rectangle(0, 0, 6000, 6000));
  }

  public void dataAdded(DataAddedEvent evt) {
    mObsMarker = null;
    mOffScreen = null;
    this.paintComponent(this.getGraphics());
  }

  public void stnFilterChanged(StnFilterChangedEvent evt) {
    mObsMarker = null;
    invalidate();
    this.paintComponent(this.getGraphics());
  }

  public void invalidate() {
    try {
      super.invalidate();
      if (mOffScreen != null) {
        mOffScreen = null;
      }
      if (mObsMarker != null) {
        mObsMarker = null;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void paintComponent(Graphics gin) {
    Graphics2D g = (Graphics2D)gin;
    if (mOffScreen == null) {
      mOffScreen = createImage(getSize().width, getSize().height);
      Graphics og = (Graphics)mOffScreen.getGraphics();
      super.paintComponent(og);
      computeDisplayScales();
      drawBottom(og);
      drawSectionPoints(og);

      g.drawImage(mOffScreen, 0, 0, null);
      og.dispose();
    }
    else {
      g.drawImage(mOffScreen, 0, 0, null);
      if (mSelection != null) {
        g.setColor(Color.black);
        g.setStroke(lw2);
        g.draw(mSelection);
        g.setColor(Color.black);
      }
    }
    computeMarkerPos();
    if (mObsMarker != null) {
      mObsMarker.drawMarker(g, false);
    }
  }

  public Dimension getPreferredSize() {
    return new Dimension(200, 100);
  }

  public void computeDisplayScales() {
    Dimension d = this.getSize();
    mLeftLon = mFileViewer.findFirstStation().getLon();
    
    if (mStnDisplayOffset == JOAConstants.OFFSET_DISTANCE) {
      if (mFileViewer.mTotMercDist == 0) {
        mStationDisplayHScale = (d.width - CSPLEFT - CSPRIGHT) / 2;
      }
      else {
        mStationDisplayHScale = (d.width - CSPLEFT - CSPRIGHT) / mFileViewer.mTotMercDist;
      }
    }
    else if (mStnDisplayOffset == JOAConstants.OFFSET_LATITUDE) {
      if (mFileViewer.mTotMercDist == 0) {
        mStationDisplayHScale = (d.width - CSPLEFT - CSPRIGHT) / 2;
      }
      else {
        mStationDisplayHScale = (d.width - CSPLEFT - CSPRIGHT) / mFileViewer.mTotLatDegs;
      }
    }
    else if (mStnDisplayOffset == JOAConstants.OFFSET_LONGITUDE) {
      if (mFileViewer.mTotMercDist == 0) {
        mStationDisplayHScale = (d.width - CSPLEFT - CSPRIGHT) / 2;
      }
      else {
        mStationDisplayHScale = (d.width - CSPLEFT - CSPRIGHT) / mFileViewer.getLonRange();
      }
    }
    else if (mStnDisplayOffset == JOAConstants.OFFSET_TIME) {
    		long deltaTime =  mFileViewer.getTimeLengthSecs();
    		double ddeltaTime = (double)deltaTime;
        mStationDisplayHScale = (d.width - CSPLEFT - CSPRIGHT) / (double)deltaTime;
    }
    else {
      if (mFileViewer.mTotalStations == 0) {
        mStationDisplayHScale = (d.width - CSPLEFT - CSPRIGHT) / 2;
      }
      else {
      	mStationDisplayHScale = (d.width - CSPLEFT - CSPRIGHT) / (double)(mFileViewer.mTotalStations - 1);
      }
    }

    // VScale
    if (mMaxDepth != JOAConstants.MISSINGVALUE && mMaxDepth > 0.0) {
      mStationDisplayVScale = (d.height - CSPTOP /* - 2*CSPBOTTOM*/) / (mMaxDepth - mMinDepth);
    }
    mWinYOrigin = mMinDepth;

    // determine the direction for latitude offset
    mNtoSSection = isNtoSSection();
  }

  private boolean isNtoSSection() {
    double startLat = 0.0;
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      // loop over the sections
      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }

        // loop over the stations
        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);

          if (stc == 0 && sec == 0 && fc == 0) {
            startLat = sh.mLat;
            continue;
          }
          else if (sh.mLat < startLat) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public void drawBottom(Graphics g) {
    int h = 0, v, oldh = JOAConstants.MISSINGVALUE, oldv = JOAConstants.MISSINGVALUE;

    if (mStnDisplayOffset == JOAConstants.LATERAL) {
      return;
    }

    int width = this.getSize().width;
    int height = this.getSize().height;
    g.setClip(CSPLEFT, CSPTOP, width - CSPRIGHT, height - CSPBOTTOM);

    if (mBlackBG) {
      g.setColor(Color.black);
      g.fillRect(CSPLEFT, CSPTOP, width - CSPRIGHT - CSPLEFT, height - CSPBOTTOM);
      g.setColor(Color.gray);
    }
    else {
      g.setColor(Color.white);
      g.fillRect(CSPLEFT, CSPTOP, width - CSPRIGHT - CSPLEFT, height - CSPBOTTOM);
      g.setColor(Color.black);
    }
    int stnCnt = 0;
    double prevLon = mLeftLon;
    boolean crossed180 = false;
    double offset = 0.0;
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }
        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          if (!sh.mUseStn) {
            continue;
          }

          // look to see if bottom is recorded
          if (sh.mBottomDepthInDBARS != JOAConstants.MISSINGVALUE && sh.mBottomDepthInDBARS != 0.0) {
            double bottom = sh.mBottomDepthInDBARS;
            v = CSPTOP + (int)((JOAFormulas.zToPres(bottom) - mWinYOrigin) * mStationDisplayVScale);

            if (mStnDisplayOffset == JOAConstants.OFFSET_SEQUENCE) {
              h = (int)(CSPLEFT + (stnCnt * mStationDisplayHScale));
            }
            else if (mStnDisplayOffset == JOAConstants.OFFSET_DISTANCE) {
              h = (int)(CSPLEFT + (mStationDisplayHScale * sh.mCumDist));
            }
            else if (mStnDisplayOffset == JOAConstants.OFFSET_LATITUDE) {
              if (!mNtoSSection) {
                h = (int)(CSPLEFT + ((sh.mLat - mFileViewer.mLatMin) * mStationDisplayHScale));
              }
              else {
                h = (int)(CSPLEFT + ((mFileViewer.mLatMax - sh.mLat) * mStationDisplayHScale));
              }
            }
            else if (mStnDisplayOffset == JOAConstants.OFFSET_LONGITUDE) {
              double lon = sh.mLon;
              if (!crossed180 && lon < prevLon) {
              	offset = 360.0;
              	crossed180 = true;
              }
              lon += offset;
              h = (int)(CSPLEFT + ((lon - mLeftLon) * mStationDisplayHScale));
              prevLon = lon;
            }
            stnCnt++;

            if (mStnDisplayIsReversed) {
              h = this.getSize().width - h;
            }

            if (oldh != JOAConstants.MISSINGVALUE && oldv != JOAConstants.MISSINGVALUE) {
              g.drawLine(oldh, oldv, h, v);
              int[] xpoints = {oldh, h, h, oldh};
              int[] ypoints = {oldv, v, 200, 200};
              Polygon poly = new Polygon(xpoints, ypoints, 4);
              g.fillPolygon(poly);
              oldh = h;
              oldv = v;
            }
            else {
              oldh = h;
              oldv = v;
            }
          }
          else {
            stnCnt++;
            oldh = JOAConstants.MISSINGVALUE;
            oldv = JOAConstants.MISSINGVALUE;
          }
        }
      }
    }
  }

  public void drawSectionPoints(Graphics g) {
    int h = 0, v, p;

    p = mFileViewer.getPRESPropertyPos();
    int width = this.getSize().width;
    int height = this.getSize().height;
    g.setClip(CSPLEFT-5, CSPTOP-5, width - CSPRIGHT + 5, height - CSPBOTTOM + 5) ;

    int stnCnt = 0;
    double prevLon = mLeftLon;
    boolean crossed180 = false;
    double offset = 0.0;
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);
        if (sech.mNumCasts == 0) {
          continue;
        }
        if (sech.mSectionColor != null) {
          g.setColor(sech.mSectionColor);
        }
        else {
          g.setColor(Color.blue);
        }
        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          if (!sh.mUseStn) {
            continue;
          }
          if (mStnDisplayOffset == JOAConstants.OFFSET_SEQUENCE) {
            h = (int)(CSPLEFT + (stnCnt * mStationDisplayHScale));
          }
          else if (mStnDisplayOffset == JOAConstants.OFFSET_DISTANCE) {
            h = (int)(CSPLEFT + (mStationDisplayHScale * sh.mCumDist));
          }
          else if (mStnDisplayOffset == JOAConstants.OFFSET_LATITUDE) {
            if (!mNtoSSection) {
              h = (int)(CSPLEFT + ((sh.mLat - mFileViewer.mLatMin) * mStationDisplayHScale));
            }
            else {
              h = (int)(CSPLEFT + ((mFileViewer.mLatMax - sh.mLat) * mStationDisplayHScale));
            }
          }
          else if (mStnDisplayOffset == JOAConstants.OFFSET_LONGITUDE) {
            double lon = sh.mLon;
            if (!crossed180 && lon < prevLon) {
            	offset = 360.0;
            	crossed180 = true;
            }
            lon += offset;
            h = (int)(CSPLEFT + ((lon - mLeftLon) * mStationDisplayHScale));
            prevLon = lon;
          }
          else if (mStnDisplayOffset == JOAConstants.OFFSET_TIME) {
          	if (sh.getDate() != null) {
          		double delTime = Math.abs((mFileViewer.getMinDate().getTime()/1000) - (sh.getDate().getTime()/1000));
          		h = (int)(CSPLEFT + (delTime * mStationDisplayHScale));
          	}
          	else {
          		h = 0;
          	}
          }
          stnCnt++;

          if (mStnDisplayIsReversed) {
            h = this.getSize().width - h;
          }

          for (int b = 0; b < sh.mNumBottles; b++) {
            Bottle bh = (Bottle)sh.mBottles.elementAt(b);
            if (mColorByMode == ConfigureXSecPanel.COLOR_BY_STN_QUAL) {
              g.setColor(JOAFormulas.getQCColor(sech, bh));
            }
            else if (mColorByMode == ConfigureXSecPanel.COLOR_BY_DEFAULT) {
              if (sech.mSectionColor != null) {
                g.setColor(sech.mSectionColor);
              }
              else {
                g.setColor(Color.blue);
              }
            }
            else if (mColorByMode == ConfigureXSecPanel.COLOR_BY_PARAM_PRESENCE) {
              int pos = sech.getVarPos(mMissingParam, false);
              if (pos < 0 || bh.mDValues[pos] == JOAConstants.MISSINGVALUE) {
                g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
              }
              else {
                if (sech.mSectionColor != null) {
                  g.setColor(sech.mSectionColor);
                }
                else {
                  g.setColor(Color.blue);
                }
              }
            }

            double val = bh.mDValues[p];
            v = CSPTOP + (int)((val - mWinYOrigin) * mStationDisplayVScale);
            g.fillRect(h - mStnSymbolSize / 2, v - mStnSymbolSize / 2, mStnSymbolSize, mStnSymbolSize);
          }
        }
      }
    }
  }

  // OK Button
  public void dialogDismissed(JDialog d) {
    mStnDisplayOffset = ((ConfigureXSecPanel)d).getDistanceOffset();
    mStnDisplayIsReversed = ((ConfigureXSecPanel)d).getReverseStns();
    mBlackBG = ((ConfigureXSecPanel)d).getBlackBG();
    mColorByMode = ((ConfigureXSecPanel)d).getColorByMode();
    mMissingParam = ((ConfigureXSecPanel)d).getMissingParam();
    mStnSymbolSize = ((ConfigureXSecPanel)d).getSymbolSize();
    try {
      mMaxDepth = ((ConfigureXSecPanel)d).getZMax();
    }
    catch (Exception ex) {

    }

    try {
      mMinDepth = ((ConfigureXSecPanel)d).getZMin();
    }
    catch (Exception ex) {

    }
    computeDisplayScales();
    mObsMarker = null;
    this.invalidate();
    paintComponent(this.getGraphics());
  }

  // Cancel button
  public void dialogCancelled(JDialog d) {
    mStnDisplayOffset = ((ConfigureXSecPanel)d).getOriginalDistanceOffset();
    mStnDisplayIsReversed = ((ConfigureXSecPanel)d).getOriginalReverseStns();
    mBlackBG = ((ConfigureXSecPanel)d).getOriginalBlackBG();
    mColorByMode = ((ConfigureXSecPanel)d).getOriginalColorByMode();
    mMissingParam = ((ConfigureXSecPanel)d).getOriginalMissingParam();
    mStnSymbolSize = ((ConfigureXSecPanel)d).getOriginalSymbolSize();
    try {
      mMaxDepth = ((ConfigureXSecPanel)d).getOriginalZMax();
    }
    catch (Exception ex) {

    }

    try {
      mMinDepth = ((ConfigureXSecPanel)d).getOriginalZMin();
    }
    catch (Exception ex) {

    }
    computeDisplayScales();
    mObsMarker = null;
    this.invalidate();
    paintComponent(this.getGraphics());
  }

  // something other than the OK button
  public void dialogDismissedTwo(JDialog d) {
    ;
  }

  // Applxy button, OK w/o dismissing the dialog
  public void dialogApply(JDialog d) {
    mStnDisplayOffset = ((ConfigureXSecPanel)d).getDistanceOffset();
    mStnDisplayIsReversed = ((ConfigureXSecPanel)d).getReverseStns();
    mBlackBG = ((ConfigureXSecPanel)d).getBlackBG();
    mColorByMode = ((ConfigureXSecPanel)d).getColorByMode();
    mMissingParam = ((ConfigureXSecPanel)d).getMissingParam();
    mStnSymbolSize = ((ConfigureXSecPanel)d).getSymbolSize();
    try {
      mMaxDepth = ((ConfigureXSecPanel)d).getZMax();
    }
    catch (Exception ex) {

    }

    try {
      mMinDepth = ((ConfigureXSecPanel)d).getZMin();
    }
    catch (Exception ex) {

    }
    computeDisplayScales();
    mObsMarker = null;
    this.invalidate();
    paintComponent(this.getGraphics());
  }

  public void dialogApplyTwo(Object d) {
  }

  public UVCoordinate getCorrectedXY(int x, int y) {
    return new UVCoordinate(Double.NaN, Double.NaN);
  }

  public double[] getInvTransformedX(double x) {
    double[] xvals = new double[1];
    xvals[0] = Double.NaN;
    return xvals;
  }

  public double[] getInvTransformedY(double y) {
    double[] yvals = new double[1];
    yvals[0] = Double.NaN;
    return yvals;
  }
}

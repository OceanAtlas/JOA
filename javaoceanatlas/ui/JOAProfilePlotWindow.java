/*
 * $Id: JOAProfilePlotWindow.java,v 1.24 2005/09/23 14:51:24 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.print.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.events.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import gov.noaa.pmel.util.GeoDate;
import com.visualtek.png.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class JOAProfilePlotWindow extends JOAWindow implements ColorBarChangedListener, ActionListener,
    DataAddedListener, ObsFilterChangedListener, StnFilterChangedListener, PrefsChangedListener,
    WindowsMenuChangedListener, ConfigurableWindow {
  public static int SECLEFT = 70;
  public static int SECRIGHT = 20;
  public static int SECTOP = 35;
  public static int SECBOTTOM = 20;
  protected double mXScale, mYScale, mXOrigin, mYOrigin;
  protected double mWinXPlotMax;
  protected double mWinYPlotMax;
  protected double mWinXPlotMin;
  protected double mWinYPlotMin;
  protected boolean mPlotAxes;
  protected int mSymbolSize;
  protected int mXVarCode, mYVarCode;
  protected boolean mXGrid, mYGrid;
  protected double mYInc, mXInc;
  protected int mXTics, mYTics;
  protected FileViewer mFileViewer;
  protected double mWinXScale;
  protected double mWinXOrigin;
  protected double mWinYScale;
  protected double mWinYOrigin;
  protected ObsMarker mObsMarker = null;
  protected int mHeightCurrWindow;
  protected int mWidthCurrWindow;
  protected double mScaleHeight;
  protected double mScaleWidth;
  protected String mWinTitle = null;
  protected int mSectionType;
  protected double mTraceOffset;
  protected double mAmplitude;
  protected int mSecOrigin;
  protected int mLineWidth;
  protected int mMaxH;
  protected Color mFG, mBG;
  protected boolean mIncludeCBAR = false, mIncludeObsPanel = false;
  protected boolean mFirst = true;
  protected JScrollPane mScroller = null;
  protected Container mContentPane = null;
  protected JOAProfilePlotWindow mThisFrame = null;
  protected int mWidth = 400;
  protected int mHeight = 400;
  protected ColorBarPanel mColorBarLegend = null;
  ProfilePlotPanel mProfilePlotPanel = null;
  protected int mSymbol;
  protected boolean mPlotSymbols;
  ProfilePlotSpecification mPlotSpec = null;
  protected Container mContents = null;
  protected boolean mPrinting = false;
  protected JFrame mParent;
  protected JOAWindow mFrame;
  protected boolean mOverRideColor = false;
  protected Color mContrastColor = null;
  private ResourceBundle rb = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
  private boolean mColorBarWasChanged = false;
  protected boolean mAccumulateStns;
  SmallIconButton mEraseBtn = null;

  public JOAProfilePlotWindow(ProfilePlotSpecification ps, JFrame parent) {
    super(true, true, true, true, true, ps);
    mPlotSpec = ps;
    mParent = parent;
    mFrame = this;
    mWidth = ps.getWidth();
    mHeight = ps.getHeight();
    mFG = ps.getFGColor();
    mBG = ps.getBGColor();
    mFileViewer = ps.getFileViewer();
    mXVarCode = ps.getXVarCode();
    mYVarCode = ps.getYVarCode();
    mWinTitle = ps.getWinTitle();
    mIncludeCBAR = ps.isIncludeCBAR();
    mIncludeObsPanel = ps.isIncludeObsPanel();
    mYGrid = ps.isYGrid();
    mSymbolSize = ps.getSymbolSize();
    mYTics = ps.getYTics();

    // axes ranges
    mWinXPlotMin = ps.getWinXPlotMin();
    mWinXPlotMax = ps.getWinXPlotMax();

    mWinYPlotMin = ps.getWinYPlotMin();
    mWinYPlotMax = ps.getWinYPlotMax();
    mYInc = ps.getYInc();
    mSectionType = ps.getSectionType();
    mAmplitude = ps.getAmplitude();
    mSecOrigin = ps.getSecOrigin();
    mLineWidth = ps.getLineWidth();
    mSymbol = ps.getSymbol();
    mPlotSymbols = ps.isPlotSymbols();
    mPlotAxes = ps.isPlotAxes();
    mAccumulateStns = ps.isAccumulateStns();

    init();
  }

  public Dimension getPreferredSize() {
    return new Dimension(mWidth, mHeight);
  }

  protected void init() {
    mThisFrame = this;
    mContents = this.getContentPane();

    // add the plot panel
    mProfilePlotPanel = new ProfilePlotPanel();
    mContents.add("Center", mProfilePlotPanel);

    // add the toolbar
    mToolBar = new JOAJToolBar();
    JOAJToggleButton lockTool = new JOAJToggleButton(new ImageIcon(javaoceanatlas.PowerOceanAtlas.class.getResource("images/lock_open.gif")), true);
    lockTool.setSelectedIcon(new ImageIcon(javaoceanatlas.PowerOceanAtlas.class.getResource("images/lock_closed.gif")));
    lockTool.setSelected(false);
    lockTool.setToolTipText(rb.getString("kLockPlot"));
    lockTool.setActionCommand("lock");
    lockTool.addActionListener(this);
    mToolBar.add(lockTool); 

    try {
	     mEraseBtn = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource("images/eraser.png")));
    }
    catch (ClassNotFoundException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
    	System.out.println("JOAProfilePlotWindow:init");
    }    
    mEraseBtn.setToolTipText("Erase Plot (Works only in Paint Stations mode)");
    mEraseBtn.setActionCommand("erase");
    mEraseBtn.addActionListener(this);
    mToolBar.add(mEraseBtn);

    // make an array of y labels
    String[] ylabels = new String[1];
    int[] yprecs = new int[1];
    ylabels[0] = new String(mFileViewer.mAllProperties[mPlotSpec.getYVarCode()].getVarLabel());
    yprecs[0] = 3;

    ValueToolbar mValToolBar = new ValueToolbar(mProfilePlotPanel, ylabels, yprecs);
    mToolBar.add(mValToolBar); ;
    double[] xvals = new double[1];
    xvals[0] = Double.NaN;
    double[] yvals = new double[1];
    yvals[0] = Double.NaN;
    mValToolBar.setLocation(xvals, yvals);
    mContents.add(mToolBar, "North");

    if (mIncludeCBAR) {
      mColorBarLegend = new ColorBarPanel(mFrame, mFileViewer, mFileViewer.mDefaultCB, mFG,
                                          JOAConstants.DEFAULT_FRAME_COLOR, false, false);
      mColorBarLegend.setEnhanceable(true);
      this.getContentPane().add("East", mColorBarLegend);
    }

    getRootPane().registerKeyboardAction(new RightListener((Object)mProfilePlotPanel, mProfilePlotPanel.getClass()),
                                         KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false),
                                         JComponent.WHEN_IN_FOCUSED_WINDOW);
    getRootPane().registerKeyboardAction(new LeftListener((Object)mProfilePlotPanel, mProfilePlotPanel.getClass()),
                                         KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false),
                                         JComponent.WHEN_IN_FOCUSED_WINDOW);
    getRootPane().registerKeyboardAction(new UpListener((Object)mProfilePlotPanel, mProfilePlotPanel.getClass()),
                                         KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                                         JComponent.WHEN_IN_FOCUSED_WINDOW);
    getRootPane().registerKeyboardAction(new DownListener((Object)mProfilePlotPanel, mProfilePlotPanel.getClass()),
                                         KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                                         JComponent.WHEN_IN_FOCUSED_WINDOW);

    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        closeMe();
      }

      public void windowActivated(WindowEvent we) {
        if (!JOAConstants.ISMAC) {
          return;
        }
        //ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
        //Menu fmenu = mMenuBar.getAWTMenuBar().getMenu(0);
        //MenuItem mi = fmenu.getItem(6);
        //mi.setLabel(b.getString("kSaveGraphic"));

      }
    };
    this.addWindowListener(windowListener);

    if (mWinTitle.length() == 0) {
      this.setTitle(new String(mFileViewer.mAllProperties[mXVarCode].getVarLabel() + "-" +
                               mFileViewer.mAllProperties[mYVarCode].getVarLabel()));
    }
    else {
      this.setTitle(new String(mWinTitle));
    }
    mFileViewer.addColorBarChangedListener(this);
    mFileViewer.addDataAddedListener(this);
    mFileViewer.addObsFilterChangedListener(this);
    mFileViewer.addStnFilterChangedListener(this);
    PowerOceanAtlas.getInstance().addPrefsChangedListener(this);
    PowerOceanAtlas.getInstance().addWindowsMenuChangedListener(this);
    mMenuBar = new JOAMenuBar(this, true, mFileViewer);

    // offset the window down and right from 'parent' frame
    Rectangle r = mParent.getBounds();
    this.setLocation(r.x + 20, r.y + 20);
  }

  public void closeMe() {
    mFileViewer.removeOpenWindow(mThisFrame);
    mFileViewer.removeColorBarChangedListener((ColorBarChangedListener)mThisFrame);
    mFileViewer.removeDataAddedListener((DataAddedListener)mThisFrame);
    mFileViewer.removeStnFilterChangedListener((StnFilterChangedListener)mThisFrame);
    mFileViewer.removeObsFilterChangedListener((ObsFilterChangedListener)mThisFrame);
    PowerOceanAtlas.getInstance().removePrefsChangedListener((PrefsChangedListener)mThisFrame);
    mFileViewer.removeObsChangedListener((ObsChangedListener)mProfilePlotPanel);
    PowerOceanAtlas.getInstance().removeWindowsMenuChangedListener((WindowsMenuChangedListener)mThisFrame);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("lock")) {
      mWindowIsLocked = !mWindowIsLocked;
      if (!mWindowIsLocked) {
        // are now unlocking the window
        this.setSize(this.getSize().width + 1, this.getSize().height);
        this.setSize(this.getSize().width, this.getSize().height);
        if (mColorBarLegend != null && mColorBarWasChanged) {
          this.getContentPane().remove(mColorBarLegend);
          mColorBarLegend = null;
          mColorBarLegend = new ColorBarPanel(mFrame, mFileViewer, mFileViewer.mDefaultCB, mFG,
                                              JOAConstants.DEFAULT_FRAME_COLOR, false, false);
          mColorBarLegend.setEnhanceable(true);
          this.getContentPane().add("East", mColorBarLegend);
          mColorBarWasChanged = false;
        }
        else if (mColorBarLegend != null) {
          mColorBarLegend.setLinked(true);
          mColorBarLegend.setLocked(false);
        }

        //if (mCurrObsPanel != null)
        //	mCurrObsPanel.setLocked(false);

        mProfilePlotPanel.invalidate();
        mProfilePlotPanel.validate();
        this.invalidate();
        this.validate();
        mProfilePlotPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.setResizable(true);
      }
      else {
        if (mColorBarLegend != null) {
          mColorBarLegend.setLinked(false);
          mColorBarLegend.setLocked(true);
        }
        mProfilePlotPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        this.setResizable(false);

        //if (mCurrObsPanel != null)
        //	mCurrObsPanel.setLocked(true);
      }
    }
    else if (cmd.equals("erase")) {
    	mEraseBtn.setSelected(false);
    	// set the skip flag for all stns
      for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
        OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

        for (int sec = 0; sec < of.mNumSections; sec++) {
          Section sech = (Section)of.mSections.elementAt(sec);

          for (int stc = 0; stc < sech.mStations.size(); stc++) {
            Station sh = (Station)sech.mStations.elementAt(stc);
            sh.setSkipStn(true);
          }
        }
      }
      mProfilePlotPanel.revalidate();
      mProfilePlotPanel.repaint();
    }
    else if (cmd.equals("saveas")) {
      saveAsPNG();
    }
    else if (cmd.equals("close")) {
      closeMe();
      this.dispose();
    }
    else if (cmd.equals("print")) {
      if (JOAConstants.DEFAULT_PAGEFORMAT == null) {
        JOAConstants.DEFAULT_PRINTERJOB = PrinterJob.getPrinterJob();
        JOAConstants.DEFAULT_PAGEFORMAT = JOAConstants.DEFAULT_PRINTERJOB.defaultPage();
        JOAConstants.DEFAULT_PAGEFORMAT = JOAConstants.DEFAULT_PRINTERJOB.pageDialog(JOAConstants.DEFAULT_PAGEFORMAT);
      }
      if (JOAConstants.DEFAULT_PRINTERJOB.printDialog()) {
        JOAConstants.DEFAULT_PRINTERJOB.setPrintable(mProfilePlotPanel, JOAConstants.DEFAULT_PAGEFORMAT);
        try {
          JOAConstants.DEFAULT_PRINTERJOB.print();
        }
        catch (PrinterException ex) {}
      }
    }
    else {
      mFileViewer.doCommand(cmd, mFrame);
    }
  }

  public void showConfigDialog() {
    // show configuration dialog
    mProfilePlotPanel.showConfigDialog();
  }

  public void saveAsPNG() {
    class BasicThread extends Thread {
      // ask for filename
      public void run() {
        Image image = mProfilePlotPanel.makeOffScreen((Graphics2D)mProfilePlotPanel.getGraphics(), true, true);

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
        String directory = System.getProperty("user.dir"); // + File.separator + "JOA_Support" + File.separator;
        FileDialog f = new FileDialog(fr, "Save image as:", FileDialog.SAVE);
        f.setDirectory(directory);
        f.setFilenameFilter(filter);
        f.setFile("untitled.png");
        f.setVisible(true);
        directory = f.getDirectory();
        f.dispose();
        if (directory != null && f.getFile() != null) {
          String path = directory + File.separator + f.getFile();
          try {
            (new PNGEncoder(image, path)).encode();
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
          try {
            JOAConstants.LogFileStream.writeBytes("Saved Plot:" + mParent.getTitle() + " as " + path + "\n");
            JOAConstants.LogFileStream.flush();
          }
          catch (Exception ex) {}
        }
      }
    }

    // Create a thread and run it
    Thread thread = new BasicThread();
    thread.start();
  }

  public void colorBarChanged(ColorBarChangedEvent evt) {
    if (mWindowIsLocked) {
      mColorBarWasChanged = true;
      return;
    }
    mProfilePlotPanel.invalidate();
    mProfilePlotPanel.setSize(this.getSize().width + 1, this.getSize().height);
    mProfilePlotPanel.setSize(this.getSize().width, this.getSize().height);
    if (mColorBarLegend != null) {
      this.getContentPane().remove(mColorBarLegend);
      mColorBarLegend = null;
      mColorBarLegend = new ColorBarPanel(mFrame, mFileViewer, mFileViewer.mDefaultCB, mFG,
                                          JOAConstants.DEFAULT_FRAME_COLOR, false, false);
      mColorBarLegend.setEnhanceable(true);
      this.getContentPane().add("East", mColorBarLegend);
    }
    this.invalidate();
    this.validate();
  }

  public void dataAdded(DataAddedEvent evt) {
    if (mWindowIsLocked) {
      return;
    }
    mProfilePlotPanel.invalidate();
    mProfilePlotPanel.setSize(this.getSize().width + 1, this.getSize().height);
    mProfilePlotPanel.setSize(this.getSize().width, this.getSize().height);
    this.invalidate();
    this.validate();
  }

  public void prefsChanged(PrefsChangedEvent evt) {
    if (mWindowIsLocked) {
      return;
    }
    mProfilePlotPanel.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
    mBG = JOAConstants.DEFAULT_CONTENTS_COLOR;
    mPlotSpec.setBGColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
    if (mColorBarLegend != null) {
      mColorBarLegend.setNewBGColor(JOAConstants.DEFAULT_FRAME_COLOR);
    }
    mProfilePlotPanel.invalidate();
    mProfilePlotPanel.setSize(this.getSize().width + 1, this.getSize().height);
    mProfilePlotPanel.setSize(this.getSize().width, this.getSize().height);
    this.invalidate();
    this.validate();
  }

  public void obsFilterChanged(ObsFilterChangedEvent evt) {
    if (mWindowIsLocked) {
      return;
    }
    mProfilePlotPanel.invalidate();
    mProfilePlotPanel.setSize(this.getSize().width + 1, this.getSize().height);
    mProfilePlotPanel.setSize(this.getSize().width, this.getSize().height);
    this.invalidate();
    this.validate();
  }

  public void stnFilterChanged(StnFilterChangedEvent evt) {
    if (mWindowIsLocked) {
      return;
    }
    mProfilePlotPanel.invalidate();
    mProfilePlotPanel.setSize(this.getSize().width + 1, this.getSize().height);
    mProfilePlotPanel.setSize(this.getSize().width, this.getSize().height);
    this.invalidate();
    this.validate();
  }

  protected void setXScale(int width) {
    mWidthCurrWindow = width;
    mScaleWidth = width;
    mWinXScale = (double)width / (double)(mWinXPlotMax - mWinXPlotMin);
    mWinXOrigin = mWinXPlotMin;
  }

  protected Point computeXYWidths() {
    int maxX = 0;
    int maxY = 0;
    int sectionOffset = 0;

    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);

        int xPos = sech.getVarPos(mFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
        int yPos = sech.getVarPos(mFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
        if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
          continue;
        }
        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          if (!sh.mUseStn) {
            continue;
          }

          // compute the offset
          if (stc > 0) {
            sectionOffset += mTraceOffset;
          }

          // plot the profiles
          for (int b = 0; b < sh.mNumBottles - 1; b++) {
            // get the first bottle
            Bottle l_bh = (Bottle)sh.mBottles.elementAt(b);
            double x0 = l_bh.mDValues[xPos];
            double y0 = l_bh.mDValues[yPos];

            // get the second bottle
            Bottle h_bh = (Bottle)sh.mBottles.elementAt(b + 1);
            double x = h_bh.mDValues[xPos];
            double y = h_bh.mDValues[yPos];
            int secx, secy, secx0, secy0;

            if (x0 == JOAConstants.MISSINGVALUE || x == JOAConstants.MISSINGVALUE) {
              if (x0 != JOAConstants.MISSINGVALUE) {
                secx0 = (int)(sectionOffset + mAmplitude * (x0 - mWinXOrigin) * mWinXScale);
                secy0 = (int)((y0 - mWinYOrigin) * mWinYScale);

                if (mDrawAxes && !(mFileViewer.mAllProperties[mYVarCode].isReverseY())) {
                  secx0 += SECLEFT;
                  secy0 += SECTOP;
                }
                else if (mDrawAxes && mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                  secy0 = mHeightCurrWindow - secy0;
                  secx0 += SECLEFT;
                  secy0 -= SECBOTTOM;
                }
                else if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                  secy0 = mHeightCurrWindow - secy0;
                }
                maxX = secx0 > maxX ? secx0 : maxX;
                maxY = secy0 > maxY ? secy0 : maxY;
              }
            }
            else {
              secx0 = (int)(sectionOffset + mAmplitude * (x0 - mWinXOrigin) * mWinXScale);
              secy0 = (int)((y0 - mWinYOrigin) * mWinYScale);

              secx = (int)(sectionOffset + mAmplitude * (x - mWinXOrigin) * mWinXScale);
              secy = (int)((y - mWinYOrigin) * mWinYScale);

              if (mDrawAxes && !(mFileViewer.mAllProperties[mYVarCode].isReverseY())) {
                secx0 += SECLEFT;
                secy0 += SECTOP;
                secx += SECLEFT;
                secy += SECTOP;
              }
              else if (mDrawAxes && mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                secy0 = mHeightCurrWindow - secy0;
                secy = mHeightCurrWindow - secy;
                secx0 += SECLEFT;
                secy0 -= SECBOTTOM;
                secx += SECLEFT;
                secy -= SECBOTTOM;
              }
              else if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                secy0 = mHeightCurrWindow - secy0;
                secy = mHeightCurrWindow - secy;
              }
              maxX = secx0 > maxX ? secx0 : maxX;
              maxX = secx > maxX ? secx : maxX;
              maxY = secy0 > maxY ? secy0 : maxY;
              maxY = secy > maxY ? secy : maxY;
            } // else
          } // for b
        } // for stc
      } // for sec
    } // for fc
    return new Point(maxX, maxY);
  }

  protected void setYScale(int height) {
    mHeightCurrWindow = height;
    mScaleHeight = height; //this.getSize().height;
    if (mDrawAxes) {
      mScaleHeight -= (SECBOTTOM + SECTOP);
    }
    mWinYScale = -mScaleHeight / (mWinYPlotMax - mWinYPlotMin);
    //mWinYOrigin = (mWinYPlotMax - mYOrigin);
    mWinYOrigin = mWinYPlotMax;
  }

  private class ProfilePlotPanel extends RubberbandPanel implements ObsChangedListener, DialogClient, ActionListener,
      Printable {
    Image mOffScreen = null;
    private Rubberband rbRect;
    DialogClient mDialogClient = null;
    private JPopupMenu mPopupMenu = null;
    BasicStroke lw2 = new BasicStroke(2);
    GeneralPath mSelection = new GeneralPath();
    long minDate = mFileViewer.getMinDate().getTime();

    public ProfilePlotPanel() {
      this.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
      mFileViewer.addObsChangedListener(this);
      addMouseListener(new XYMouseHandler());
      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      rbRect = new RubberbandRectangle(this);
      //rbRect.setConstrainVertical(true);
      rbRect.setActive(true);
      mDialogClient = this;
    }

    public void setRubberbandDisplayObject(Object obj, boolean concat) {
      // this routine expects a Rectangle Object
      mSelection = (GeneralPath)obj;
      repaint();
    }

    public int print(Graphics gin, PageFormat pageFormat, int pageIndex) {
      if (pageIndex == 0) {
        Graphics2D g = (Graphics2D)gin;

        // compute the offset for the legend
        Dimension od = this.getSize();
        Dimension od2 = null;
        double xOffset;
        double yOffset;

        // compute scale factor
        int cbwidth = 0;
        if (mColorBarLegend != null) {
          cbwidth = getColorBarWidth(g);
          od2 = mColorBarLegend.mSwatchPanel.getSize();
        }
        double xScale = 1.0;
        double yScale = 1.0;

        if (od.width + cbwidth > pageFormat.getImageableWidth()) {
          xScale = pageFormat.getImageableWidth() / ((double)(od.width + cbwidth));
        }

        if (od.height > pageFormat.getImageableHeight()) {
          yScale = pageFormat.getImageableHeight() / ((double)od.height);
        }

        xScale = Math.min(xScale, yScale);
        yScale = xScale;

        if (mColorBarLegend != null) {
          xOffset = (pageFormat.getImageableWidth() - (od.width * xScale)) / 2 + ((cbwidth * xScale) / 2);
          yOffset = (pageFormat.getImageableHeight() - (od.height * yScale)) / 2;
        }
        else {
          xOffset = (pageFormat.getImageableWidth() - (od.width * xScale)) / 2;
          yOffset = (pageFormat.getImageableHeight() - (od.height * yScale)) / 2;
        }

        // center the plot on the page
        if (mColorBarLegend != null) {
          g.translate(pageFormat.getImageableX() + (xOffset - (cbwidth * xScale)), pageFormat.getImageableY() + yOffset);
        }
        else {
          g.translate(pageFormat.getImageableX() + xOffset, pageFormat.getImageableY() + yOffset);
        }

        g.scale(xScale, yScale);

        // add the title
        String sTemp = mThisFrame.getTitle();
        Hashtable<TextAttribute, Serializable> map = new Hashtable<TextAttribute, Serializable>();
        map.put(TextAttribute.FAMILY, JOAConstants.DEFAULT_PLOT_TITLE_FONT);
        map.put(TextAttribute.SIZE, new Float(JOAConstants.DEFAULT_PLOT_TITLE_SIZE));
        if (JOAConstants.DEFAULT_PLOT_TITLE_STYLE == Font.BOLD ||
            JOAConstants.DEFAULT_PLOT_TITLE_STYLE == (Font.BOLD | Font.ITALIC)) {
          map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        }
        if (JOAConstants.DEFAULT_PLOT_TITLE_STYLE == Font.ITALIC ||
            JOAConstants.DEFAULT_PLOT_TITLE_STYLE == (Font.BOLD | Font.ITALIC)) {
          map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        map.put(TextAttribute.FOREGROUND, JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

        // layout the title
        Map<TextAttribute, Serializable> map2 = (Map<TextAttribute, Serializable>)map;
				TextLayout tl = new TextLayout(sTemp, map2, g.getFontRenderContext());
        Rectangle2D strbounds = tl.getBounds();
        double strWidth = strbounds.getWidth();
        double hh = (SECLEFT + (od.width - SECRIGHT - SECLEFT) / 2) - strWidth / 2;
				double vv = SECTOP/2 + strbounds.getHeight()/2;

  			JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, g, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
  			    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
  			    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

        // Add the BG color to the plot
        int x1, y1, width, height;
        if (mPlotAxes) {
          x1 = SECLEFT;
          width = this.getSize().width - SECLEFT - SECRIGHT;
          y1 = SECTOP;
          height = this.getSize().height - SECTOP - SECBOTTOM;
        }
        else {
          x1 = 0;
          y1 = 0;
          width = this.getSize().width;
          height = this.getSize().height;
        }
        g.setColor(mBG);
        g.setClip(0, 0, 1000, 1000);
        g.fillRect(x1, y1, width, height);
        g.setColor(Color.black);
        g.drawRect(x1, y1, width, height);

        plotProfiles(g);
        if (mPlotAxes) {
          drawYAxis(g);
        }

        g.setClip(0, 0, 20000, 20000);
        if (mColorBarLegend != null) {
          g.translate(od.width, od.height / 2 - od2.height / 2);
          drawColorBar(g, height);
        }
        return PAGE_EXISTS;
      }
      else {
        return NO_SUCH_PAGE;
      }
    }

    public int getColorBarWidth(Graphics2D g) {
      g.setFont(new Font(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
                         JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE));
      FontMetrics fm = g.getFontMetrics();
      int numColors = mColorBarLegend.getColorBar().getNumLevels();

      // draw the color ramp and labels
      double base = mColorBarLegend.getColorBar().getBaseLevel();
      double end = mColorBarLegend.getColorBar().getEndLevel();
      double diff = Math.abs(end - base);
      int numPlaces = 2;
      if (diff < 10) {
        numPlaces = 3;
      }
      else if (diff >= 10 && diff < 100) {
        numPlaces = 2;
      }
      else if (diff >= 100 && diff < 1000) {
        numPlaces = 1;
      }
      else if (diff >= 1000) {
        numPlaces = 1;
      }
      int maxLabelH = 0;
      for (int i = 0; i < numColors; i++) {
        String sTemp = mColorBarLegend.getColorBar().getFormattedValue(i, numPlaces, true);
        int strWidth = fm.stringWidth(sTemp);
        maxLabelH = strWidth > maxLabelH ? strWidth : maxLabelH;
      }
      return maxLabelH + 45;
    }

    public void drawColorBar(Graphics2D g, int height) {
      int numColors = mColorBarLegend.getColorBar().getNumLevels();
      int left = 0;
      int top = 20;
      int bottom = height;
      //if (!(mLabelPanel && mFancyLabelPanel))
      //	bottom -= 15;
      int pixelsPerBand = (bottom - top - 2) / numColors;
      int bandTop = 0;
      int bandBottom = 0;
      g.setFont(new Font(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
                         JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE));
      FontMetrics fm = g.getFontMetrics();

      // draw the color ramp and labels
      double base = mColorBarLegend.getColorBar().getBaseLevel();
      double end = mColorBarLegend.getColorBar().getEndLevel();
      double diff = Math.abs(end - base);
      int numPlaces = 2;
      if (diff < 10) {
        numPlaces = 3;
      }
      else if (diff >= 10 && diff < 100) {
        numPlaces = 2;
      }
      else if (diff >= 100 && diff < 1000) {
        numPlaces = 1;
      }
      else if (diff >= 1000) {
        numPlaces = 1;
      }

      int labelInc = 0;
      if (numColors <= 16) {
        labelInc = 1;
      }
      else if (numColors > 16 && numColors <= 32) {
        labelInc = 2;
      }
      else if (numColors > 32 && numColors <= 48) {
        labelInc = 3;
      }
      else if (numColors > 48 && numColors <= 64) {
        labelInc = 4;
      }
      else if (numColors > 64) {
        labelInc = 5;
      }

      for (int i = 0; i < numColors; i++) {
        //swatch
        bandTop = (int)(top + (i) * pixelsPerBand);
        bandBottom = (int)(bandTop + pixelsPerBand);
        g.setColor(mColorBarLegend.getColorBar().getColorValue(i));
        g.fillRect(left + 10, bandTop, left + 25, bandBottom - bandTop);

        // label
        g.setColor(Color.black);
        if (i % labelInc == 0) {
          String sTemp = mColorBarLegend.getColorBar().getFormattedValue(i, numPlaces, true);
          g.drawString(sTemp, left + 35, bandBottom);
        }
      }
      // put the label here
      String panelLabel = null;
      if (mFileViewer != null) {
        int pPos = mFileViewer.getPropertyPos(mColorBarLegend.getColorBar().getParam(), false);
        if (pPos >= 0 && mFileViewer.mAllProperties[pPos].getUnits() != null &&
            mFileViewer.mAllProperties[pPos].getUnits().length() > 0) {
          panelLabel = new String(mColorBarLegend.getColorBar().getParam() + " (" +
                                  mFileViewer.mAllProperties[pPos].getUnits() + ")");
        }
        else {
          panelLabel = new String(mColorBarLegend.getColorBar().getParam());
        }
      }
      else {
        panelLabel = new String(mColorBarLegend.getColorBar().getParam());
      }
      int strWidth = fm.stringWidth(panelLabel);
      JOAFormulas.drawStyledString(panelLabel, left + 25 - strWidth / 2, bandBottom + 15, fm, (Graphics2D)g);
    }

    public Dimension getPreferredSize() {
      return new Dimension(mWidth, mHeight);
    }

    public int getMinX() {
      if (mDrawAxes) {
        return SECLEFT;
      }
      else {
        return 1;
      }
    }

    public int getMinY() {
      if (mDrawAxes) {
        return SECTOP;
      }
      else {
        return 1;
      }
    }

    public int getMaxX() {
      if (mDrawAxes) {
        return this.getSize().width - 1;
      }
      else {
        return this.getSize().width - 1;
      }
    }

    public int getMaxY() {
      if (mDrawAxes) {
        return this.getSize().height - SECBOTTOM;
      }
      else {
        return this.getSize().height - 1;
      }
    }

    public void rubberbandEnded(Rubberband rb) {
    }

    public void createPopup(Point point) {
      mPopupMenu = new JPopupMenu();
      JMenuItem openContextualMenu = new JMenuItem(rb.getString("kProperties"));
      openContextualMenu.setActionCommand("opencontextual");
      openContextualMenu.addActionListener(this);
      mPopupMenu.add(openContextualMenu);
      mPopupMenu.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      mPopupMenu.show(this, point.x, point.y);
    }

    public void showConfigDialog() {
      // show configuration dialog
      ConfigureProfilePlotDC cp = new ConfigureProfilePlotDC(mFrame, mFileViewer, mDialogClient, mPlotSpec);
      //if (JOAConstants.ISMAC)
      //	cp.setSize(416, 410);
      //else
      //	cp.setSize(410, 430);
      cp.pack();
      cp.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();

      if (cmd.equals("opencontextual")) {
        showConfigDialog();
      }
    }

    public class XYMouseHandler extends MouseAdapter {
      public void mouseClicked(MouseEvent me) {
        if (mWindowIsLocked) {
          return;
        }

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
        if (mWindowIsLocked) {
          return;
        }
        super.mouseReleased(me);
        setRubberbandDisplayObject(null, false);

        if (me.isPopupTrigger()) {
          createPopup(me.getPoint());
        }
        else {
          if (rbRect != null && me.getID() == MouseEvent.MOUSE_RELEASED) {
            zoomPlot(rbRect.getBounds(), me.isAltDown());
          }
        }
      }

      public void mousePressed(MouseEvent me) {
        if (mWindowIsLocked) {
          return;
        }
        super.mousePressed(me);
        if (me.isPopupTrigger()) {
          createPopup(me.getPoint());
        }
      }
    }

    public void zoomPlot(Rectangle newRect, boolean mode) {
      if (mWindowIsLocked) {
        return;
      }
      boolean reverseY = mFileViewer.mAllProperties[mYVarCode].isReverseY();
      // convert corners of rectangle to new plot range
      Rectangle newBounds = rbRect.lastBounds();
      int y1 = newBounds.y;
      int y2 = y1 + newBounds.height;

      if (newBounds.height < 10) {
        return;
      }

      // adjust for axes labels
      if (mDrawAxes && !reverseY) {
        y1 -= SECTOP;
        y2 -= SECTOP;
      }
      else if (mDrawAxes && reverseY) {
        y1 = mHeightCurrWindow - y1;
        y1 -= SECBOTTOM;
        y2 = mHeightCurrWindow - y2;
        y2 -= SECBOTTOM;
      }
      else if (reverseY) {
        y1 = mHeightCurrWindow - y1;
        y2 = mHeightCurrWindow - y2;
      }

      if (mode) {
        if (reverseY) {
          mWinYPlotMax = y2 / mWinYScale + mWinYOrigin;
          mWinYPlotMin = y1 / mWinYScale + mWinYOrigin;
        }
        else {
          mWinYPlotMax = y1 / mWinYScale + mWinYOrigin;
          mWinYPlotMin = y2 / mWinYScale + mWinYOrigin;
        }

        Triplet newRange = JOAFormulas.GetPrettyRange(mWinYPlotMin, mWinYPlotMax);
        mWinYPlotMin = newRange.getVal1();
        mWinYPlotMax = newRange.getVal2();
        mYInc = newRange.getVal3();
        mPlotSpec.setYInc(mYInc);
        mPlotSpec.setWinYPlotMax(mWinYPlotMax);
        mPlotSpec.setWinYPlotMin(mWinYPlotMin);
        mPlotSpec.setAccumulateStns(mAccumulateStns);

        // zoom using current window
        // invalidate and replot
        invalidate();
        paintComponent(this.getGraphics());
        rbRect.end(rbRect.getStretched());
      }
      else {
        ProfilePlotSpecification ps = new ProfilePlotSpecification();
        if (reverseY) {
          ps.setWinYPlotMax(y2 / mWinYScale + mWinYOrigin);
          ps.setWinYPlotMin(y1 / mWinYScale + mWinYOrigin);
        }
        else {
          ps.setWinYPlotMax(y1 / mWinYScale + mWinYOrigin);
          ps.setWinYPlotMin(y2 / mWinYScale + mWinYOrigin);
        }

        Triplet newRange = JOAFormulas.GetPrettyRange(ps.getWinYPlotMin(), ps.getWinYPlotMax());
        ps.setWinYPlotMin(newRange.getVal1());
        ps.setWinYPlotMax(newRange.getVal2());
        ps.setYInc(newRange.getVal3());

        ps.setWinXPlotMin(mWinXPlotMin);
        ps.setWinXPlotMax(mWinXPlotMax);

        ps.setFGColor(mFG);
        ps.setBGColor(mBG);
        ps.setFileViewer(mFileViewer);
        ps.setXVarCode(mXVarCode);
        ps.setYVarCode(mYVarCode);
        ps.setWinTitle(mWinTitle + "z");
        ps.setIncludeCBAR(mIncludeCBAR);
        ps.setIncludeObsPanel(mIncludeObsPanel);
        ps.setYGrid(mYGrid);
        ps.setSymbolSize(mSymbolSize);
        ps.setSymbol(mSymbol);
        ps.setYTics(mYTics);
        if (ps.isIncludeCBAR()) {
          ps.setWidth(mWidth);
          ps.setHeight(mHeight);
        }
        else {
          ps.setWidth(mWidth);
          ps.setHeight(mHeight);
        }
        ps.setSectionType(mSectionType);
        ps.setTraceOffset(mTraceOffset);
        ps.setAmplitude(mAmplitude);
        ps.setSecOrigin(0);
        ps.setLineWidth(mLineWidth);
        ps.setPlotSymbols(mPlotSymbols);
        ps.setPlotAxes(mPlotAxes);
        ps.setAccumulateStns(mAccumulateStns);

        try {
          ps.writeToLog("Zoomed Profile Plot: " + mParent.getTitle());
        }
        catch (Exception ex) {}

        // make a new plot window
        JOAProfilePlotWindow plotWind = new JOAProfilePlotWindow(ps, mThisFrame);
        plotWind.pack();
        plotWind.setVisible(true);
        mFileViewer.addOpenWindow(plotWind);
      }
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

      }
    }

    @SuppressWarnings("unchecked")
    public Image makeOffScreen(Graphics2D g, boolean addTitle, boolean plotOverlays) {
      Image outImage = null;
      int pWidth = getSize().width;

      if (mColorBarLegend != null) {
        pWidth += 100;
      }
      outImage = createImage(pWidth, getSize().height);
      Dimension d = this.getSize();

      setXScale(d.width);

      // compute the trace offset and amplitude
      mTraceOffset = mPlotSpec.getTraceOffset();
      double maxOffset = 0;
      if (mFirst) {
        if (mSectionType == JOAConstants.PROFSEQUENCE) {
          maxOffset = SECLEFT + mFileViewer.mTotalStations * mTraceOffset;
        }
        else if (mSectionType == JOAConstants.PROFDISTANCE) {
          maxOffset = SECLEFT + mTraceOffset * mFileViewer.mTotMercDist;
        }
        else if (mSectionType == JOAConstants.PROFTIME) {
          maxOffset = SECLEFT + mTraceOffset * mFileViewer.getTimeLengthDays();
        }

        double maxX = mAmplitude * (mWinXPlotMax - mWinXOrigin) * mWinXScale;

        while (maxX + maxOffset >= d.width) {
          mAmplitude -= 0.05;
          if (mAmplitude <= 0.5) {
            mAmplitude = 1.0;
            if (mSectionType == JOAConstants.PROFSEQUENCE) {
              mTraceOffset -= 1.0;
            }
            else if (mSectionType == JOAConstants.PROFDISTANCE) {
              mTraceOffset *= 0.80;
            }
            else if (mSectionType == JOAConstants.PROFTIME) {
              mTraceOffset *= 0.80;
            }
          }

          if (mSectionType == JOAConstants.PROFSEQUENCE) {
            maxOffset = SECLEFT + mFileViewer.mTotalStations * mTraceOffset;
          }
          else if (mSectionType == JOAConstants.PROFDISTANCE) {
            maxOffset = SECLEFT + mTraceOffset * mFileViewer.mTotMercDist;
          }
          else if (mSectionType == JOAConstants.PROFTIME) {
            maxOffset = SECLEFT + mTraceOffset * mFileViewer.getTimeLengthDays();
          }

          maxX = mAmplitude * (mWinXPlotMax - mWinXOrigin) * mWinXScale;
        }
        mPlotSpec.setTraceOffset(mTraceOffset);
        mPlotSpec.setAmplitude(mAmplitude);
        mFirst = false;
      }

      setYScale(d.height);
      Graphics2D og = (Graphics2D)outImage.getGraphics();
      super.paintComponent(og);

      og.setColor(Color.white);
      og.fillRect(0, 0, 2000, 2000);

      int x1, y1, width, height;
      if (mPlotAxes) {
        x1 = SECLEFT;
        width = this.getSize().width - SECLEFT - SECRIGHT;
        y1 = SECTOP;
        height = this.getSize().height - SECTOP - SECBOTTOM;
      }
      else {
        x1 = 0;
        y1 = 0;
        width = this.getSize().width;
        height = this.getSize().height;
      }
      og.setColor(mBG);
      og.fillRect(x1, y1, width, height);
      og.setColor(Color.black);
      og.drawRect(x1, y1, width, height);
      plotProfiles(og);

      if (mPlotAxes) {
        drawYAxis((Graphics2D)og);
      }

      if (addTitle) {
        String sTemp = mThisFrame.getTitle();

        // layout the title
	      Font font = new Font(JOAConstants.DEFAULT_PLOT_TITLE_FONT, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
	          JOAConstants.DEFAULT_PLOT_TITLE_SIZE);
	  	      FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
	          int strWidth = fm.stringWidth(sTemp);
        double hh = (SECLEFT + (this.getWidth() - SECRIGHT - SECLEFT) / 2) - strWidth / 2;
				double vv = SECTOP/2 + fm.getHeight()/2;
        JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, og, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
            JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
            JOAConstants.DEFAULT_PLOT_TITLE_COLOR);
      }

      Dimension od = this.getSize();
      if (mColorBarLegend != null) {
        og.setClip(0, 0, 2000, 2000);
        og.translate(od.width, SECTOP);
        this.drawColorBar(og, od.height - SECTOP - SECBOTTOM);
      }

      og.dispose();
      return outImage;
    }

    public void paintComponent(Graphics gin) {
      Graphics2D g = (Graphics2D)gin;
      if (mOffScreen == null) {
        mOffScreen = makeOffScreen(g, JOAConstants.DEFAULT_PLOT_TITLES, false);
        g.drawImage(mOffScreen, 0, 0, null);
        mFirst = false;
      }
      else {
        g.drawImage(mOffScreen, 0, 0, null);
        if (mSelection != null) {
          g.setColor(JOAConstants.DEFAULT_SELECTION_REGION_OUTLINE_COLOR);
          g.setStroke(lw2);
          g.draw(mSelection);
          g.setColor(Color.black);
        }
      }

      if (mObsMarker != null && !mWindowIsLocked) {
        mObsMarker.drawMarker(g, false);
      }
    }

    private void plotProfiles(Graphics2D g) {
      int x1, width, y1, height;
      int currSymbol = mSymbol;
      int currSymbolSize = mSymbolSize;
      int currLineWidth = mLineWidth;
      boolean currPlotSymbols = mPlotSymbols;
      ;

      boolean colorByMetaData = false;
      boolean isDateMetadata = false;
      boolean isMonthMetadata = false;
      boolean isLatMetadata = false;
      boolean isLonMetadata = false;

      double stnLat = 0.0;
      double stnLon = 0.0;
      GeoDate stnDate = null;
      int stnMonth = 0;

      boolean enhanceByMetadata = false;
      boolean enhanceObs = false;

      Color stnMetadataColor = null;

      colorByMetaData = mFileViewer.mDefaultCB.isMetadataColorBar();
      if (colorByMetaData) {
        isDateMetadata = mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTime"));
        isMonthMetadata = mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTimeMonth"));
        isLatMetadata = mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLatitude"));
        isLonMetadata = mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLongitude"));
      }

      if (mDrawAxes) {
        x1 = SECLEFT;
        width = this.getSize().width - SECLEFT;
        y1 = SECTOP;
        height = this.getSize().height - SECTOP - SECBOTTOM;
      }
      else {
        x1 = 0;
        y1 = 0;
        width = this.getSize().width;
        height = this.getSize().height;
      }
      g.setClip(x1, y1 + 1, width - SECRIGHT, height - 1);

      int sectionOffset = 0;

      for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
        OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

        for (int sec = 0; sec < of.mNumSections; sec++) {
          Section sech = (Section)of.mSections.elementAt(sec);

          int xPos = sech.getVarPos(mFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
          int yPos = sech.getVarPos(mFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
          if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
            continue;
          }

          for (int stc = 0; stc < sech.mStations.size(); stc++) {
            Station sh = (Station)sech.mStations.elementAt(stc);
            if (!sh.mUseStn || (mAccumulateStns && sh.isSkipStn())) {
              continue;
            }

            if (colorByMetaData) {
              stnLat = sh.getLat();
              stnLon = sh.getLon();
              stnDate = sh.getDate();
              stnMonth = sh.getMonth() - 1;

              enhanceByMetadata = mFileViewer.mDefaultCB.isColorEnhanced(mFileViewer.mDefaultCB.getColorIndex(stnLat)) ||
                  mFileViewer.mDefaultCB.isColorEnhanced(mFileViewer.mDefaultCB.getColorIndex(stnLon)) ||
                  mFileViewer.mDefaultCB.isColorEnhanced(mFileViewer.mDefaultCB.getColorIndex(stnDate)) ||
                  mFileViewer.mDefaultCB.isColorEnhanced(mFileViewer.mDefaultCB.getColorIndex(stnMonth));

              if (isDateMetadata) {
                stnMetadataColor = mFileViewer.mDefaultCB.getColor(stnDate);
              }
              else if (isMonthMetadata) {
                stnMetadataColor = mFileViewer.mDefaultCB.getColor(stnMonth);
              }
              else if (isLatMetadata) {
                stnMetadataColor = mFileViewer.mDefaultCB.getColor(stnLat);
              }
              else if (isLonMetadata) {
                stnMetadataColor = mFileViewer.mDefaultCB.getColor(stnLon);
              }
            }

            // plot the profiles
            if (xPos >= 0 && yPos >= 0) {
              // compute the offset
              if (mSectionType == JOAConstants.PROFSEQUENCE) {
                sectionOffset = (int)(sh.mOrdinal * mTraceOffset);
              }
              else if (mSectionType == JOAConstants.PROFDISTANCE) {
                sectionOffset = (int)(mTraceOffset * sh.mCumDist);
              }
              else if (mSectionType == JOAConstants.PROFTIME) {
                sectionOffset = (int)(mTraceOffset * (sh.getDate().getTime() - minDate) / (1000.0 * 86400.0));
              }
              sectionOffset += mSecOrigin;

              for (int b = 0; b < sh.mNumBottles - 1; b++) {
                enhanceObs = false;
                // get the first bottle
                Bottle l_bh = (Bottle)sh.mBottles.elementAt(b);
                double x0 = l_bh.mDValues[xPos];
                double y0 = l_bh.mDValues[yPos];

                // get the second bottle
                Bottle h_bh = (Bottle)sh.mBottles.elementAt(b + 1);
                double x = h_bh.mDValues[xPos];
                double y = h_bh.mDValues[yPos];

                boolean keepBottle1 = true, keepBottle2 = true;
                if (mFileViewer.mObsFilterActive) {
                  keepBottle1 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, l_bh);
                }
                else {
                  keepBottle1 = true;
                }

                if (mFileViewer.mObsFilterActive) {
                  keepBottle2 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, h_bh);
                }
                else {
                  keepBottle2 = true;
                }

                if (mFileViewer.mObsFilterActive && keepBottle1 && keepBottle2 && mPlotSymbols) {
                  // keep both and plot symbols
                  if (mFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
                    currSymbolSize = mSymbolSize;
                    currLineWidth = mLineWidth;
                  }
                }
                else if (mFileViewer.mObsFilterActive && keepBottle1 && keepBottle2 && !mPlotSymbols) {
                  // keep both but don't plot symbols
                  currPlotSymbols = false;
                  currLineWidth = mLineWidth;
                }
                else if (mFileViewer.mObsFilterActive && (!keepBottle1 || !keepBottle2) && !mPlotSymbols) {
                  if (mFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
                    continue;
                  }
                }
                else if (mFileViewer.mObsFilterActive && (!keepBottle1 || !keepBottle2) && mPlotSymbols) {
                  if (mFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
                    continue;
                  }
                }

                double theFirstVal = JOAConstants.MISSINGVALUE, theSecVal = JOAConstants.MISSINGVALUE;
                Color firstColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
                int firstColorIndex, secondColorIndex;
                int secx, secy, secx0, secy0;

                if (x0 == JOAConstants.MISSINGVALUE || y0 == JOAConstants.MISSINGVALUE ||
                    x == JOAConstants.MISSINGVALUE || y == JOAConstants.MISSINGVALUE) {
                  if (x0 != JOAConstants.MISSINGVALUE && y0 != JOAConstants.MISSINGVALUE) {
                    if (mFileViewer.mDefaultCB.isMetadataColorBar()) {
                      // get the value of the metadatatype for this station
                      if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTime"))) {
                        firstColor = mFileViewer.mDefaultCB.getColor(sh.getDate());
                      }
                      else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLatitude"))) {
                        firstColor = mFileViewer.mDefaultCB.getColor(sh.getLat());
                      }
                      else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLongitude"))) {
                        firstColor = mFileViewer.mDefaultCB.getColor(sh.getLon());
                      }
                    }
                    else {
                      // get the value of the color variable
                      theFirstVal = JOAFormulas.getValueOfColorVariable(mFileViewer, sech, l_bh);

                      // get a color for this value
                      firstColor = mFileViewer.mDefaultCB.getColor(theFirstVal);
                    }

                    secx0 = (int)(sectionOffset + mAmplitude * (x0 - mWinXOrigin) * mWinXScale);
                    secy0 = (int)((y0 - mWinYOrigin) * mWinYScale);

                    if (mDrawAxes && !(mFileViewer.mAllProperties[mYVarCode].isReverseY())) {
                      secx0 += SECLEFT;
                      secy0 += SECTOP;
                    }
                    else if (mDrawAxes && mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                      secy0 = mHeightCurrWindow - secy0;
                      secx0 += SECLEFT;
                      secy0 -= SECBOTTOM;
                    }
                    else if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                      secy0 = mHeightCurrWindow - secy0;
                    }

                    if (theFirstVal != JOAConstants.MISSINGVALUE) {
                      g.setColor(firstColor);
                    }
                    else {
                      g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
                    }

                    JOAFormulas.plotThickLine(g, secx0, secy0, secx0, secy0, currLineWidth);
                    if (mPlotSymbols || currPlotSymbols) {
                      g.setColor(Color.black);
                      JOAFormulas.plotSymbol(g, currSymbol, secx0, secy0, currSymbolSize);
                    }
                  }
                }
                else {
                  secx0 = (int)(sectionOffset + mAmplitude * (x0 - mWinXOrigin) * mWinXScale);
                  secy0 = (int)((y0 - mWinYOrigin) * mWinYScale);

                  secx = (int)(sectionOffset + mAmplitude * (x - mWinXOrigin) * mWinXScale);
                  secy = (int)((y - mWinYOrigin) * mWinYScale);

                  if (mDrawAxes && !(mFileViewer.mAllProperties[mYVarCode].isReverseY())) {
                    secx0 += SECLEFT;
                    secy0 += SECTOP;
                    secx += SECLEFT;
                    secy += SECTOP;
                  }
                  else if (mDrawAxes && mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                    secy0 = mHeightCurrWindow - secy0;
                    secy = mHeightCurrWindow - secy;
                    secx0 += SECLEFT;
                    secy0 -= SECBOTTOM;
                    secx += SECLEFT;
                    secy -= SECBOTTOM;
                  }
                  else if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                    secy0 = mHeightCurrWindow - secy0;
                    secy = mHeightCurrWindow - secy;
                  }

                  if (mFileViewer.mDefaultCB.isMetadataColorBar()) {
                    // get the value of the metadatatype for this station
                    if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTime"))) {
                      firstColor = mFileViewer.mDefaultCB.getColor(sh.getDate());
                    }
                    else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLatitude"))) {
                      firstColor = mFileViewer.mDefaultCB.getColor(sh.getLat());
                    }
                    else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLongitude"))) {
                      firstColor = mFileViewer.mDefaultCB.getColor(sh.getLon());
                    }

                    g.setColor(firstColor);
                    JOAFormulas.plotThickLine(g, secx0, secy0, secx, secy, currLineWidth);

                    if (mPlotSymbols || currPlotSymbols) {
                      if (mOverRideColor) {
                        g.setColor(mContrastColor);
                        mOverRideColor = false;
                      }
                      g.setColor(Color.black);
                      JOAFormulas.plotSymbol(g, currSymbol, secx0, secy0, currSymbolSize);
                    }
                  }
                  else {
                    // get the value of the contour variable at this point
                    theFirstVal = JOAFormulas.getValueOfColorVariable(mFileViewer, sech, l_bh);

                    // get a color for this value
                    firstColor = mFileViewer.mDefaultCB.getColor(theFirstVal);
                    firstColorIndex = mFileViewer.mDefaultCB.getColorIndex(theFirstVal);

                    // get the value of the contour variable at the next point
                    theSecVal = JOAFormulas.getValueOfColorVariable(mFileViewer, sech, h_bh);

                    secondColorIndex = mFileViewer.mDefaultCB.getColorIndex(theSecVal);

                    if (theFirstVal == (double)JOAConstants.MISSINGVALUE ||
                        theSecVal == (double)JOAConstants.MISSINGVALUE) {
                      g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR); // current missing value color
                      JOAFormulas.plotThickLine(g, secx0, secy0, secx, secy, currLineWidth);
                      if (mPlotSymbols || currPlotSymbols) {
                        if (mOverRideColor) {
                          g.setColor(mContrastColor);
                          mOverRideColor = false;
                        }
                        g.setColor(Color.black);
                        JOAFormulas.plotSymbol(g, currSymbol, secx0, secy0, currSymbolSize);
                      }
                    }
                    else {
                      if (firstColorIndex == secondColorIndex) {
                        g.setColor(firstColor);
                        JOAFormulas.plotThickLine(g, secx0, secy0, secx, secy, currLineWidth);
                      }
                      else {
                        // interpolate
                        lineInterp(secx0, secy0, theFirstVal, firstColorIndex, secx, secy, theSecVal, secondColorIndex,
                                   g, currLineWidth);
                      } //else
                      g.setColor(firstColor);
                      if (mPlotSymbols || currPlotSymbols) {
                        if (mOverRideColor) {
                          g.setColor(mContrastColor);
                          mOverRideColor = false;
                        }
                        g.setColor(Color.black);
                        JOAFormulas.plotSymbol(g, currSymbol, secx0, secy0, currSymbolSize);
                        JOAFormulas.plotSymbol(g, currSymbol, secx, secy, currSymbolSize);
                      }
                    } // else
                  }
                }
              }
            }
          }
        }
      }

      // plot symbols if obs filter is active
      if (mFileViewer.mObsFilterActive) {
        for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
          OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

          for (int sec = 0; sec < of.mNumSections; sec++) {
            Section sech = (Section)of.mSections.elementAt(sec);

            int xPos = sech.getVarPos(mFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
            int yPos = sech.getVarPos(mFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
            if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
              continue;
            }

            for (int stc = 0; stc < sech.mStations.size(); stc++) {
              Station sh = (Station)sech.mStations.elementAt(stc);
              if (!sh.mUseStn || (mAccumulateStns && sh.isSkipStn())) {
                continue;
              }

              // plot the points
              if (xPos >= 0 && yPos >= 0) {
                // compute the offset
                if (mSectionType == JOAConstants.PROFSEQUENCE) {
                  sectionOffset = (int)(sh.mOrdinal * mTraceOffset);
                }
                else if (mSectionType == JOAConstants.PROFDISTANCE) {
                  sectionOffset = (int)(mTraceOffset * sh.mCumDist);
                }
                else if (mSectionType == JOAConstants.PROFTIME) {
                  sectionOffset = (int)(mTraceOffset * (sh.getDate().getTime() - minDate) / (1000.0 * 86400.0));
                }

                sectionOffset += mSecOrigin;

                for (int b = 0; b < sh.mNumBottles - 1; b++) {
                  // get the first bottle
                  Bottle l_bh = (Bottle)sh.mBottles.elementAt(b);
                  double x0 = l_bh.mDValues[xPos];
                  double y0 = l_bh.mDValues[yPos];

                  boolean keepBottle = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, l_bh);

                  if (mFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
                    currSymbolSize = mSymbolSize;
                  }
                  else if (mFileViewer.mCurrObsFilter.isEnlargeSymbol()) {
                    currPlotSymbols = true;
                    currSymbolSize = mSymbolSize * 2;
                    if (mFileViewer.mCurrObsFilter.isUseContrastingColor()) {
                      mOverRideColor = true;
                      mContrastColor = mFileViewer.mCurrObsFilter.getContrastingColor();
                    }
                  }
                  else if (!mFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
                    currPlotSymbols = true;
                    currSymbol = mFileViewer.mCurrObsFilter.getSymbol();
                    currSymbolSize = mFileViewer.mCurrObsFilter.getSymbolSize();
                    if (mFileViewer.mCurrObsFilter.isUseContrastingColor()) {
                      mOverRideColor = true;
                      mContrastColor = mFileViewer.mCurrObsFilter.getContrastingColor();
                    }
                  }

                  if (keepBottle && x0 != JOAConstants.MISSINGVALUE && y0 != JOAConstants.MISSINGVALUE) {
                    g.setColor(Color.black);

                    int secx0 = (int)(sectionOffset + mAmplitude * (x0 - mWinXOrigin) * mWinXScale);
                    int secy0 = (int)((y0 - mWinYOrigin) * mWinYScale);

                    if (mDrawAxes && !(mFileViewer.mAllProperties[mYVarCode].isReverseY())) {
                      secx0 += SECLEFT;
                      secy0 += SECTOP;
                    }
                    else if (mDrawAxes && mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                      secy0 = mHeightCurrWindow - secy0;
                      secx0 += SECLEFT;
                      secy0 -= SECBOTTOM;
                    }
                    else if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                      secy0 = mHeightCurrWindow - secy0;
                    }

                    if (mPlotSymbols || currPlotSymbols) {
                      if (mOverRideColor) {
                        g.setColor(mContrastColor);
                        mOverRideColor = false;
                      }
                      else {
                        g.setColor(JOAFormulas.getContrastingColor(mPlotSpec.getBGColor()));
                      }
                      JOAFormulas.plotSymbol(g, currSymbol, secx0, secy0, currSymbolSize);
                    }
                  }
                }
              }
            }
          }
        }
      }
      else if (!mFileViewer.mObsFilterActive) {
        // hilite any enhanced points if needed to do after other points have been
        for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
          OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

          for (int sec = 0; sec < of.mNumSections; sec++) {
            Section sech = (Section)of.mSections.elementAt(sec);

            int xPos = sech.getVarPos(mFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
            int yPos = sech.getVarPos(mFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
            if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
              continue;
            }
            for (int stc = 0; stc < sech.mStations.size(); stc++) {
              Station sh = (Station)sech.mStations.elementAt(stc);
              if (!sh.mUseStn || (mAccumulateStns && sh.isSkipStn())) {
                continue;
              }

              if (colorByMetaData) {
                stnLat = sh.getLat();
                stnLon = sh.getLon();
                stnDate = sh.getDate();
                stnMonth = sh.getMonth() - 1;

                enhanceByMetadata = mFileViewer.mDefaultCB.isColorEnhanced(mFileViewer.mDefaultCB.getColorIndex(stnLat)) ||
                    mFileViewer.mDefaultCB.isColorEnhanced(mFileViewer.mDefaultCB.getColorIndex(stnLon)) ||
                    mFileViewer.mDefaultCB.isColorEnhanced(mFileViewer.mDefaultCB.getColorIndex(stnDate)) ||
                    mFileViewer.mDefaultCB.isColorEnhanced(mFileViewer.mDefaultCB.getColorIndex(stnMonth));

                if (isDateMetadata) {
                  stnMetadataColor = mFileViewer.mDefaultCB.getColor(stnDate);
                }
                else if (isMonthMetadata) {
                  stnMetadataColor = mFileViewer.mDefaultCB.getColor(stnMonth);
                }
                else if (isLatMetadata) {
                  stnMetadataColor = mFileViewer.mDefaultCB.getColor(stnLat);
                }
                else if (isLonMetadata) {
                  stnMetadataColor = mFileViewer.mDefaultCB.getColor(stnLon);
                }
              }

              if (xPos >= 0 && yPos >= 0) {
                // compute the offset
                if (mSectionType == JOAConstants.PROFSEQUENCE) {
                  sectionOffset = (int)(sh.mOrdinal * mTraceOffset);
                }
                else if (mSectionType == JOAConstants.PROFDISTANCE) {
                  sectionOffset = (int)(mTraceOffset * sh.mCumDist);
                }
                else if (mSectionType == JOAConstants.PROFTIME) {
                  sectionOffset = (int)(mTraceOffset * (sh.getDate().getTime() - minDate) / (1000.0 * 86400.0));
                }

                sectionOffset += mSecOrigin;

                for (int b = 0; b < sh.mNumBottles; b++) {
                  enhanceObs = false;
                  Bottle bh = (Bottle)sh.mBottles.elementAt(b);
                  boolean keepBottle;
                  if (mFileViewer.mObsFilterActive) {
                    keepBottle = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh);
                  }
                  else {
                    keepBottle = true;
                  }

                  if (mFileViewer.mObsFilterActive && !keepBottle) {
                    if (mFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
                      continue;
                    }
                  }

                  currSymbol = mSymbol;
                  currSymbolSize = mSymbolSize;
                  xPos = sech.getVarPos(mFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
                  if (xPos == -1) {
                    continue;
                  }

                  double xd = bh.mDValues[xPos];
                  double yd = bh.mDValues[yPos];
                  if (xd != JOAConstants.MISSINGVALUE && yd != JOAConstants.MISSINGVALUE) {
                    int x = (int)(sectionOffset + mAmplitude * (xd - mWinXOrigin) * mWinXScale);
                    int y = (int)((yd - mWinYOrigin) * mWinYScale);

                    // adjust for axes labels
                    if (mDrawAxes && !(mFileViewer.mAllProperties[mYVarCode].isReverseY())) {
                      x += SECLEFT;
                      y += SECTOP;
                    }
                    else if (mDrawAxes && mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                      y = mHeightCurrWindow - y;
                      x += SECLEFT;
                      y -= SECBOTTOM;
                    }
                    else if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                      y = mHeightCurrWindow - y;
                    }

                    Color currColor = Color.black;

                    // get a color for this value
                    if (colorByMetaData) {
                      currColor = stnMetadataColor;
                    }
                    else {
                      // plot alternate symbols if enhancement turned on
                      // get the value of the color variable at this point
                      double theVal = JOAFormulas.getValueOfColorVariable(mFileViewer, sech, bh);
                      if (mFileViewer.mDefaultCB.isColorEnhanced(mFileViewer.mDefaultCB.getColorIndex(theVal))) {
                        enhanceObs = true;
                      }
                    }

                    // plot point
                    if (enhanceByMetadata || enhanceObs) {
                      if (JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL) {
                        currSymbolSize *= 1.0 + JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL_BY;
                      }
                      else if (JOAConstants.DEFAULT_ENHANCE_REPLACE_CURRENT_SYMBOL) {
                        currSymbolSize *= 1.0 + JOAConstants.DEFAULT_ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY;
                        currSymbol = JOAConstants.DEFAULT_ENHANCE_CONTRASTING_SYMBOL;
                      }

                      if (JOAConstants.DEFAULT_ENHANCE_USE_CONTRASTING_COLOR) {
                        currColor = JOAConstants.DEFAULT_ENHANCE_CONTRASTING_COLOR;
                      }

                      g.setColor(currColor);
                      JOAFormulas.plotSymbol(g, currSymbol, (int)x, (int)y, currSymbolSize);
                    }
                  }
                }
              }
            }
          }
        }
      }

      // initialize the spot
      if (mObsMarker == null) {
        OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
        Section sech = (Section)of.mSections.currElement();
        Station sh = (Station)sech.mStations.currElement();
        Bottle bh = (Bottle)sh.mBottles.currElement();

        // compute the offset
        if (mSectionType == JOAConstants.PROFSEQUENCE) {
          sectionOffset = (int)(sh.mOrdinal * mTraceOffset);
        }
        else if (mSectionType == JOAConstants.PROFDISTANCE) {
          sectionOffset = (int)(mTraceOffset * sh.mCumDist);
        }
        else if (mSectionType == JOAConstants.PROFTIME) {
          sectionOffset = (int)(mTraceOffset * (sh.getDate().getTime() - minDate) / (1000.0 * 86400.0));
        }

        sectionOffset += mSecOrigin;

        int xPos = sech.getVarPos(mFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
        int yPos = sech.getVarPos(mFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
        if (xPos == -1 || yPos == -1) {
          xPos = mXVarCode;
          yPos = mYVarCode;
        }
        double x = bh.mDValues[xPos];
        double y = bh.mDValues[yPos];
        if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
          int secx0 = (int)(sectionOffset + mAmplitude * (x - mWinXOrigin) * mWinXScale);
          int secy0 = (int)((y - mWinYOrigin) * mWinYScale);

          if (mDrawAxes && !(mFileViewer.mAllProperties[mYVarCode].isReverseY())) {
            secx0 += SECLEFT;
            secy0 += SECTOP;
          }
          else if (mDrawAxes && mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
            secy0 = mHeightCurrWindow - secy0;
            secx0 += SECLEFT;
            secy0 -= SECBOTTOM;
          }
          else if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
            secy0 = mHeightCurrWindow - secy0;
          }
          mObsMarker = new ObsMarker(secx0, secy0, JOAConstants.DEFAULT_CURSOR_SIZE);
        }
      }
      g.setClip(0, 0, this.getSize().width, this.getSize().height);

    }

    private void lineInterp(int x1, int y1, double theFirstVal, int firstColorLevel, int x2, int y2, double theSecVal,
                            int secondColorLevel, Graphics2D g, int currLineWidth) {
      double dx = x2 - x1;
      double dy = y2 - y1;

      int numSegments = Math.abs(firstColorLevel - secondColorLevel);
      double denom = Math.abs(theSecVal - theFirstVal);
      int oldX = x1;
      int oldY = y1;
      for (int iseg = 0; iseg < numSegments; ++iseg) {
        int i = (firstColorLevel < secondColorLevel) ? firstColorLevel + iseg : firstColorLevel - iseg;

        Color interpColor = mFileViewer.mDefaultCB.getColorValue(i);
        double conVal = mFileViewer.mDefaultCB.getDoubleValue(i);

        double num1 = Math.abs(conVal - theFirstVal);
        double num2 = Math.abs(conVal - theSecVal);
        double frac = num1 / denom;

        // Triangle inequality.
        if (num1 > denom && num1 > num2) {
          frac = 1.0;
        }
        if (num2 > denom && num2 > num1) {
          frac = 0.0;
        }

        double xi = x1 + (frac * dx);
        double yi = y1 + (frac * dy);

        g.setColor(interpColor);
        //g.drawLine(oldX, oldY, (int)xi, (int)yi);
        JOAFormulas.plotThickLine(g, oldX, oldY, (int)xi, (int)yi, currLineWidth);
        oldX = (int)xi;
        oldY = (int)yi;
      }

      Color interpColor = mFileViewer.mDefaultCB.getColorValue(secondColorLevel);
      g.setColor(interpColor);
      //g.drawLine(oldX, oldY, x2, y2);
      JOAFormulas.plotThickLine(g, oldX, oldY, x2, y2, currLineWidth);
    }

    @SuppressWarnings("deprecation")
    private void drawYAxis(Graphics2D g) {
      g.setColor(Color.black);
      int bottom = (int)mHeightCurrWindow - SECBOTTOM;
      int top = SECTOP;
      int left = SECLEFT;
      int right = this.getSize().width - SECRIGHT;
			int leftMTicPos = SECLEFT - 5 - 2;

      double yDiff = (mWinYPlotMax - mWinYPlotMin);
      int majorYTicks = (int)(yDiff / mYInc);
      double yInc = (double)(bottom - top) / (yDiff / mYInc);
      double minorYInc = yInc / ((double)mYTics + 1);

      /* draw the Y axis */
      g.drawLine(left, top, left, bottom);

      /* draw the Y tic marks */
      for (int i = 0; i <= majorYTicks; i++) {
        g.setColor(Color.black);
        int v = (int)(bottom - (i * yInc));
        if (mBG == Color.black) {
          g.setColor(Color.black);
          g.drawLine(left - 2, v, left, v);
          g.setColor(Color.white);
          g.drawLine(left, v, left + 2, v);
        }
        else {
          g.drawLine(left - 2, v, left + 2, v);
        }

        /* plot the minor ticks */
        if (i < majorYTicks) {
          for (int vv = 0; vv < mYTics + 1; vv++) {
            int newV = (int)(v - (vv * minorYInc));
            if (mBG == Color.black) {
              g.setColor(Color.black);
              g.drawLine(left - 1, newV, left, newV);
              g.setColor(Color.white);
              g.drawLine(left, newV, left + 1, newV);
            }
            else {
              g.drawLine(left - 1, newV, left + 1, newV);
            }
          }
        }

        /* plot the grid */
        if (mYGrid && i < majorYTicks) {
          g.setColor(mFG);
          g.drawLine(left, v, right, v);
        }
      }

      // always complete the box
      if (mYGrid) {
        g.setColor(Color.black);
        g.drawLine(left, top, right, top);
      }

      // set the Y precision
      int numPlaces = JOAFormulas.GetDisplayPrecision(mYInc);

      // label the axes
      Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
                           JOAConstants.DEFAULT_AXIS_VALUE_SIZE);
      FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
      double vOrigin;
      double myVal;
      if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
        vOrigin = mWinYPlotMax;
      }
      else {
        vOrigin = mWinYPlotMin;
      }

      int maxStrLen = 0;
      for (int i = 0; i <= majorYTicks; i++) {
        if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
          myVal = vOrigin - (i * mYInc);
        }
        else {
          myVal = vOrigin + (i * mYInc);
        }
        if (myVal == -0.0) {
          myVal = 0.0;
        }
        int v = (int)(bottom - (i * yInc));

        String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), numPlaces, false);
        int strLen = fm.stringWidth(sTemp);
				if (strLen > maxStrLen)
					maxStrLen = strLen;
        JOAFormulas.drawStyledString(sTemp, leftMTicPos - strLen, v + 5, g, 0.0, JOAConstants.DEFAULT_AXIS_VALUE_FONT,
                                     JOAConstants.DEFAULT_AXIS_VALUE_SIZE, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
                                     JOAConstants.DEFAULT_AXIS_VALUE_COLOR);
      }

      // add variable label
      font = new Font(JOAConstants.DEFAULT_AXIS_LABEL_FONT, JOAConstants.DEFAULT_AXIS_LABEL_STYLE,
                      JOAConstants.DEFAULT_AXIS_LABEL_SIZE);
      g.setFont(font);
      fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
      int width = 0;
      String axisLabel = null;
      if (mFileViewer.mAllProperties[mYVarCode].getUnits() != null &&
          mFileViewer.mAllProperties[mYVarCode].getUnits().length() > 0) {
        axisLabel = mFileViewer.mAllProperties[mYVarCode].getVarLabel() + " (" +
            mFileViewer.mAllProperties[mYVarCode].getUnits() + ")";
        width = fm.stringWidth(axisLabel);
      }
      else {
        axisLabel = mFileViewer.mAllProperties[mYVarCode].getVarLabel();
        width = fm.stringWidth(axisLabel);
      }
			int hcenter = (leftMTicPos - maxStrLen)/2;
			int hpos = hcenter + fm.getHeight()/2;
      int height = this.getSize().height;
      JOAFormulas.drawStyledString(axisLabel, hpos, SECTOP + ((height - SECTOP - SECBOTTOM) / 2) + width / 2, g, 90,
                                   JOAConstants.DEFAULT_AXIS_LABEL_FONT, JOAConstants.DEFAULT_AXIS_LABEL_SIZE,
                                   JOAConstants.DEFAULT_AXIS_LABEL_STYLE, JOAConstants.DEFAULT_AXIS_LABEL_COLOR);
    }

    public void obsChanged(ObsChangedEvent evt) {
      if (mWindowIsLocked) {
        return;
      }
      // display the current station
      Station sh = evt.getFoundStation();
      Section sech = evt.getFoundSection();
      setRecord(sech, sh);
    }

    private void setRecord(Section inSec, Station inStn) {
      Bottle bh = (Bottle)inStn.mBottles.currElement();
      Graphics2D g = (Graphics2D)getGraphics();
      
      if (mAccumulateStns) {
      	inStn.setSkipStn(false);
        this.invalidate();
        this.paintComponent(g);
      }

      // compute the section offset
      int sectionOffset = 0;
      if (mSectionType == JOAConstants.PROFSEQUENCE) {
        sectionOffset = (int)(inStn.mOrdinal * mTraceOffset);
      }
      else if (mSectionType == JOAConstants.PROFDISTANCE) {
        sectionOffset = (int)(mTraceOffset * inStn.mCumDist);
      }
      else if (mSectionType == JOAConstants.PROFTIME) {
        sectionOffset = (int)(mTraceOffset * (inStn.getDate().getTime() - minDate) / (1000.0 * 86400.0));
      }
      sectionOffset += mSecOrigin;

      int xPos = inSec.getVarPos(mFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
      int yPos = inSec.getVarPos(mFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
      if (xPos == -1 || yPos == -1) {
        xPos = mXVarCode;
        yPos = mYVarCode;
      }
      double x = bh.mDValues[xPos];
      double y = bh.mDValues[yPos];
      if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
        int secx = (int)(sectionOffset + mAmplitude * (x - mWinXOrigin) * mWinXScale);
        int secy = (int)((y - mWinYOrigin) * mWinYScale);

        if (mDrawAxes && !(mFileViewer.mAllProperties[mYVarCode].isReverseY())) {
          secx += SECLEFT;
          secy += SECTOP;
        }
        else if (mDrawAxes && mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
          secy = mHeightCurrWindow - secy;
          secx += SECLEFT;
          secy -= SECBOTTOM;
        }
        else if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
          secy = mHeightCurrWindow - secy;
        }

        if (mObsMarker == null) {
          mObsMarker = new ObsMarker(secx, secy, JOAConstants.DEFAULT_CURSOR_SIZE);
          paintImmediately(0, 0, 2000, 2000);
        }
        else {
          mObsMarker.setNewPos(secx, secy);
          paintImmediately(0, 0, 2000, 2000);
        }
      }
    }

    private void findByXY(int xx, int yy) {
      boolean found = false;
      OpenDataFile foundFile = null;
      Section foundSection = null;
      Station foundStation = null;
      Bottle foundBottle = null;
      OpenDataFile oldof = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
      Section oldsech = (Section)oldof.mSections.currElement();
      Station oldsh = (Station)oldsech.mStations.currElement();
      Bottle oldBottle = (Bottle)oldsh.mBottles.currElement();
      if (!mSpotable) {
        return;
      }
      
      double minOffset = 10000.0;
      // search for a matching observation
      for (int fc = 0; fc < mFileViewer.mNumOpenFiles && !found; fc++) {
        OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

        for (int sec = 0; sec < of.mNumSections && !found; sec++) {
          Section sech = (Section)of.mSections.elementAt(sec);
          if (sech.mNumCasts == 0) {
            continue;
          }
          int xPos = sech.getVarPos(mFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
          int yPos = sech.getVarPos(mFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
          if (xPos == -1 || yPos == -1) {
            continue;
          }

          for (int stc = 0; stc < sech.mStations.size() && !found; stc++) {
            Station sh = (Station)sech.mStations.elementAt(stc);
            if (!sh.mUseStn) {
              continue;
            }

            // compute the offset
            int sectionOffset = 0;
            if (mSectionType == JOAConstants.PROFSEQUENCE) {
              sectionOffset = (int)(sh.mOrdinal * mTraceOffset);
            }
            else if (mSectionType == JOAConstants.PROFDISTANCE) {
              sectionOffset = (int)(mTraceOffset * sh.mCumDist);
            }
            else if (mSectionType == JOAConstants.PROFTIME) {
              sectionOffset = (int)(mTraceOffset * (sh.getDate().getTime() - minDate) / (1000.0 * 86400.0));
            }

            sectionOffset += mSecOrigin;

            for (int b = 0; b < sh.mNumBottles && !found; b++) {
              Bottle bh = (Bottle)sh.mBottles.elementAt(b);
              double x = bh.mDValues[xPos];
              double y = bh.mDValues[yPos];
              if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
                x = sectionOffset + mAmplitude * (x - mWinXOrigin) * mWinXScale;
                y = (y - mWinYOrigin) * mWinYScale;

                if (mDrawAxes && !(mFileViewer.mAllProperties[mYVarCode].isReverseY())) {
                  x += SECLEFT;
                  y += SECTOP;
                }
                else if (mDrawAxes && mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                  y = mHeightCurrWindow - y;
                  x += SECLEFT;
                  y -= SECBOTTOM;
                }
                else if (mFileViewer.mAllProperties[mYVarCode].isReverseY()) {
                  y = mHeightCurrWindow - y;
                }
              }

              double off = Math.sqrt(((x - xx) * (x - xx)) + ((y - yy) * (y - yy)));

              // if observation filter is active we want to snap to only visible points
              boolean keepBottle;
              if (mFileViewer.mObsFilterActive) {
                keepBottle = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh);
              }
              else {
                keepBottle = true;
              }

              if (mFileViewer.mObsFilterActive && mFileViewer.mCurrObsFilter.isShowOnlyMatching() && !keepBottle) {
                continue;
              }

              if (off < minOffset) {
                foundStation = sh;
                foundSection = sech;
                foundFile = of;
                foundBottle = bh;
                minOffset = off;
              }
            }
          }
        }
      }

      found = true;
      // post event so other components will update
      if (found) {
        if (found && foundBottle != oldBottle) {
          mFileViewer.mOpenFiles.setCurrElement(foundFile);
          foundFile.mSections.setCurrElement(foundSection);
          foundSection.mStations.setCurrElement(foundStation);
          foundStation.mBottles.setCurrElement(foundBottle);
          int p = mFileViewer.getPRESPropertyPos();
          JOAConstants.currTestPres = foundBottle.mDValues[p];
          ObsChangedEvent oce = new ObsChangedEvent(mFileViewer);
          oce.setFoundObs(foundFile, foundSection, foundStation, foundBottle);
          paintImmediately(new Rectangle(0, 0, 2000, 2000));
          Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(oce);
        }
        else {
          Toolkit.getDefaultToolkit().beep();
        }
      }
      else {
        Toolkit.getDefaultToolkit().beep();
      }
    }

    public boolean findByArrowKey(Integer direction) {
      if (mWindowIsLocked) {
        return false;
      }
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
              found = true;
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
                found = true;
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
            if (foundBottle != null) {
              found = true;
            }
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
              found = true;
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
                found = true;
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
        paintImmediately(new Rectangle(0, 0, 2000, 2000));
        return true;
      }
      else {
        Toolkit.getDefaultToolkit().beep();
        return false;
      }
    }

    // OK Button
    public void dialogDismissed(JDialog d) {
      mPlotSpec = ((ConfigureProfilePlotDC)d).createPlotSpec();
      try {
        mPlotSpec.writeToLog("Edited existing plot: " + mParent.getTitle());
      }
      catch (Exception ex) {}
      mWidth = mPlotSpec.getWidth();
      mHeight = mPlotSpec.getHeight();
      mFG = mPlotSpec.getFGColor();
      mBG = mPlotSpec.getBGColor();
      mFileViewer = mPlotSpec.getFileViewer();
      mXVarCode = mPlotSpec.getXVarCode();
      mYVarCode = mPlotSpec.getYVarCode();
      mWinTitle = mPlotSpec.getWinTitle();
      mIncludeCBAR = mPlotSpec.isIncludeCBAR();
      mIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
      mYGrid = mPlotSpec.isYGrid();
      mSymbol = mPlotSpec.getSymbol();
      mSymbolSize = mPlotSpec.getSymbolSize();
      mYTics = mPlotSpec.getYTics();
      mTraceOffset = mPlotSpec.getTraceOffset();
      mAmplitude = mPlotSpec.getAmplitude();
      mSecOrigin = mPlotSpec.getSecOrigin();
      mSectionType = mPlotSpec.getSectionType();
      mLineWidth = mPlotSpec.getLineWidth();
      mPlotAxes = mPlotSpec.isPlotAxes();
      mPlotSymbols = mPlotSpec.isPlotSymbols();

      // axes ranges
      mWinXPlotMin = mPlotSpec.getWinXPlotMin();
      mWinXPlotMax = mPlotSpec.getWinXPlotMax();

      mWinYPlotMin = mPlotSpec.getWinYPlotMin();
      mWinYPlotMax = mPlotSpec.getWinYPlotMax();
      mYInc = mPlotSpec.getYInc();

      // resize the window if necessary
      if (mColorBarLegend != null && ((ConfigureProfilePlotDC)d).removingColorBar()) {
        // remove existing color bar component (if there is one)
        mContents.remove(mColorBarLegend);
        mColorBarLegend = null;
        mThisFrame.setSize(mThisFrame.getSize().width - 100, mThisFrame.getSize().height);
      }
      else if (((ConfigureProfilePlotDC)d).addingColorBar()) {
        mColorBarLegend = new ColorBarPanel(mFrame, mFileViewer, mFileViewer.mDefaultCB, mFG,
                                            JOAConstants.DEFAULT_FRAME_COLOR, false, false);
        mColorBarLegend.setEnhanceable(true);
        mContents.add("East", mColorBarLegend);
        mThisFrame.setSize(mThisFrame.getSize().width + 100, mThisFrame.getSize().height);
        mThisFrame.validate();
      }

      mOffScreen = null;
      this.invalidate();
      paintComponent(this.getGraphics());
    }

    // Cancel button
    public void dialogCancelled(JDialog d) {
      mPlotSpec = ((ConfigureProfilePlotDC)d).getOrigPlotSpec();
      mWidth = mPlotSpec.getWidth();
      mHeight = mPlotSpec.getHeight();
      mFG = mPlotSpec.getFGColor();
      mBG = mPlotSpec.getBGColor();
      mFileViewer = mPlotSpec.getFileViewer();
      mXVarCode = mPlotSpec.getXVarCode();
      mYVarCode = mPlotSpec.getYVarCode();
      mWinTitle = mPlotSpec.getWinTitle();
      mIncludeCBAR = mPlotSpec.isIncludeCBAR();
      mIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
      mYGrid = mPlotSpec.isYGrid();
      mSymbol = mPlotSpec.getSymbol();
      mSymbolSize = mPlotSpec.getSymbolSize();
      mYTics = mPlotSpec.getYTics();
      mTraceOffset = mPlotSpec.getTraceOffset();
      mAmplitude = mPlotSpec.getAmplitude();
      mSecOrigin = mPlotSpec.getSecOrigin();
      mSectionType = mPlotSpec.getSectionType();
      mLineWidth = mPlotSpec.getLineWidth();
      mPlotAxes = mPlotSpec.isPlotAxes();
      mPlotSymbols = mPlotSpec.isPlotSymbols();

      // axes ranges
      mWinXPlotMin = mPlotSpec.getWinXPlotMin();
      mWinXPlotMax = mPlotSpec.getWinXPlotMax();

      mWinYPlotMin = mPlotSpec.getWinYPlotMin();
      mWinYPlotMax = mPlotSpec.getWinYPlotMax();
      mYInc = mPlotSpec.getYInc();

      if (((ConfigureProfilePlotDC)d).mLayoutChanged) {
        if (mIncludeCBAR && !((ConfigureProfilePlotDC)d).mOriginalIncludeColorBar) {
          // add the color legend if it has been removed
          mColorBarLegend = new ColorBarPanel(mFrame, mFileViewer, mFileViewer.mDefaultCB, mFG,
                                              JOAConstants.DEFAULT_FRAME_COLOR, false, false);
          mColorBarLegend.setEnhanceable(true);
          mContents.add("East", mColorBarLegend);
          mThisFrame.setSize(mThisFrame.getSize().width + 100, mThisFrame.getSize().height);
        }
        else if (!mIncludeCBAR && ((ConfigureProfilePlotDC)d).mOriginalIncludeColorBar) {
          // remove the color legend if it has been added
          mContents.remove(mColorBarLegend);
          mColorBarLegend = null;
          mThisFrame.setSize(mThisFrame.getSize().width - 100, mThisFrame.getSize().height);
        }
      }

      mOffScreen = null;
      this.invalidate();
      paintComponent(this.getGraphics());
      mThisFrame.setSize(mThisFrame.getSize().width + 1, mThisFrame.getSize().height);
      mThisFrame.setSize(mThisFrame.getSize().width - 1, mThisFrame.getSize().height);
    }

    // something other than the OK button
    public void dialogDismissedTwo(JDialog d) {
      ;
    }

    public void dialogApplyTwo(Object d) {
    }

    // Apply button, OK w/o dismissing the dialog
    public void dialogApply(JDialog d) {
      mPlotSpec = ((ConfigureProfilePlotDC)d).createPlotSpec();
      try {
        mPlotSpec.writeToLog("Edited existing plot: " + mParent.getTitle());
      }
      catch (Exception ex) {}
      mWidth = mPlotSpec.getWidth();
      mHeight = mPlotSpec.getHeight();
      mFG = mPlotSpec.getFGColor();
      mBG = mPlotSpec.getBGColor();
      mFileViewer = mPlotSpec.getFileViewer();
      mXVarCode = mPlotSpec.getXVarCode();
      mYVarCode = mPlotSpec.getYVarCode();
      mWinTitle = mPlotSpec.getWinTitle();
      mIncludeCBAR = mPlotSpec.isIncludeCBAR();
      mIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
      mYGrid = mPlotSpec.isYGrid();
      mSymbol = mPlotSpec.getSymbol();
      mSymbolSize = mPlotSpec.getSymbolSize();
      mYTics = mPlotSpec.getYTics();
      mTraceOffset = mPlotSpec.getTraceOffset();
      mAmplitude = mPlotSpec.getAmplitude();
      mSecOrigin = mPlotSpec.getSecOrigin();
      mSectionType = mPlotSpec.getSectionType();
      mLineWidth = mPlotSpec.getLineWidth();
      mPlotAxes = mPlotSpec.isPlotAxes();
      mPlotSymbols = mPlotSpec.isPlotSymbols();

      // axes ranges
      mWinXPlotMin = mPlotSpec.getWinXPlotMin();
      mWinXPlotMax = mPlotSpec.getWinXPlotMax();

      mWinYPlotMin = mPlotSpec.getWinYPlotMin();
      mWinYPlotMax = mPlotSpec.getWinYPlotMax();
      mYInc = mPlotSpec.getYInc();

      // resize the window if necessary
      if (mColorBarLegend != null && ((ConfigureProfilePlotDC)d).removingColorBar()) {
        // remove existing color bar component (if there is one)
        mContents.remove(mColorBarLegend);
        mColorBarLegend = null;
        mThisFrame.setSize(mThisFrame.getSize().width - 100, mThisFrame.getSize().height);
        ((ConfigureProfilePlotDC)d).removedColorLegend();
      }
      else if (((ConfigureProfilePlotDC)d).addingColorBar()) {
        mColorBarLegend = new ColorBarPanel(mFrame, mFileViewer, mFileViewer.mDefaultCB, mFG,
                                            JOAConstants.DEFAULT_FRAME_COLOR, false, false);
        mColorBarLegend.setEnhanceable(true);
        mContents.add("East", mColorBarLegend);
        mThisFrame.setSize(mThisFrame.getSize().width + 100, mThisFrame.getSize().height);
        ((ConfigureProfilePlotDC)d).addedColorLegend();
        mThisFrame.validate();
      }

      mOffScreen = null;
      this.invalidate();
      paintComponent(this.getGraphics());
    }

    public UVCoordinate getCorrectedXY(int x, int y) {
      boolean reverseY = mFileViewer.mAllProperties[mYVarCode].isReverseY();
      double dy = (double)y;
      double dx = (double)x;
      if (y < getMinY() || y > getMaxY() || x < getMinX() || x > getMaxX()) {
        dy = Double.NaN;
        dx = Double.NaN;
        return new UVCoordinate(dx, dy);
      }

      if (mDrawAxes && !reverseY) {
        dy -= SECTOP;
      }
      else if (mDrawAxes && reverseY) {
        dy = mHeightCurrWindow - y;
        dy -= SECBOTTOM;
      }
      else if (reverseY) {
        dy = mHeightCurrWindow - y;
      }

      return new UVCoordinate(dx, dy);
    }

    public double[] getInvTransformedX(double x) {
      double[] xvals = new double[1];
      xvals[0] = Double.NaN; //y/mWinYScale + mWinYOrigin;
      return xvals;
    }

    public double[] getInvTransformedY(double y) {
      double[] yvals = new double[1];
      yvals[0] = y / mWinYScale + mWinYOrigin;
      return yvals;
    }
  }

  public RubberbandPanel getPanel() {
    return mProfilePlotPanel;
  }
}

/*
 * $Id: JOAXYPlotWindow.java,v 1.39 2005/10/18 23:42:19 oz Exp $
 *
 */

package javaoceanatlas.ui;

import gov.noaa.pmel.swing.NPixelBorder;
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
import com.visualtek.png.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class JOALinePlotWindow extends JOAWindow implements DataAddedListener, ActionListener,
    ObsFilterChangedListener, StnFilterChangedListener, PrefsChangedListener, WindowsMenuChangedListener,
    ConfigurableWindow, ObsChangedListener {
	public static int PVPLEFT = 70;
	public static int PVPRIGHT = 20;
	public static int PVPTOP = 35;
	public static int PVPBOTTOM = 50;
	public static int SIGGRIDSIZE = 50;
	public static int TGRIDSIZE = 500;

	protected double mWinXPlotMax;
	protected double mWinYPlotMax;
	protected double mWinXPlotMin;
	protected double mWinYPlotMin;
	protected boolean mLineDrawn;
	protected int mSymbol;
	protected int mSymbolSize;
	protected int mXVarCode;
	protected int mYVarCode;
	protected boolean mXGrid, mYGrid;
	protected double mYInc;
	protected double mXInc;
	protected int mXTics;
	protected int mYTics;
	protected FileViewer mMainFileViewer;
	protected Vector<FileViewer> mFileViewers;
	protected double mWinXScale;
	protected double mWinXOrigin;
	protected double mWinYScale;
	protected double mWinYOrigin;
	protected ObsMarker mObsMarker;
	protected boolean mUseBottle = true;
	protected int mHeightCurrWindow;
	protected int mWidthCurrWindow;
	protected double mScaleHeight;
	protected double mScaleWidth;
	protected String mWinTitle = null;
	protected boolean mIncludeCBAR, mIncludeObsPanel;
	protected Color mFG, mBG;
	protected int mHeight, mWidth;
	protected LinePlotPanel mLinePlot = null;
	protected JScrollPane mObsScroller = null;
	protected CurrentObsPanel2 mCurrObsPanel = null;
	protected Station oldStn = null;
	protected Section oldSec = null;
	protected boolean mPlotAxes;
	protected LinePlotSpecification mPlotSpec = null;
	protected Container mContents = null;
	protected JOAWindow mFrame = null;
	protected boolean mPlotIsopycnals = false;
	private double mRefPress;
	private boolean mPrinting = false;
	private boolean mCanPlotIsoPycnals;
	private boolean mPlotOnlyCurrStn;
	private boolean mAccumulateStns;
	private JFrame mParent;
	private boolean mOverRideColor = false;
	private Color mContrastColor = null;
	private JOALinePlotWindow mThis = null;
	private ResourceBundle rb = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	private ValueToolbar mValToolBar;
	private MetadataToolbar mMetaTB;
	private GeneralPath stnLine = new GeneralPath();
	private boolean stnLineStarted;
	private SmallIconButton mEraseBtn = null;
	private SmallIconButton mLinearRegressionBtn = null;
	private double mSlopes;
	private double mIntercepts;
	private double mRSquares;
	private double svar;
	private double svar1;
	private double svar0;
	private boolean mPlotRegress = false;
	private BasicStroke lw1 = new BasicStroke(1);
	private BasicStroke lw2 = new BasicStroke(2);
	private BasicStroke lw4 = new BasicStroke(4);
	private boolean mClipToWindow = false;
	private ColorPalette mColorPalette = null;
	private int mNumColors = 256;
	private HashMap<String, Color> mStnColors = new HashMap<String, Color>();
	private BasicStroke mConnectLW;
	private StnLegendPanel mStationLegend;

	public JOALinePlotWindow(LinePlotSpecification ps, JFrame parent) {
		super(true, true, true, true, true, ps);
		mPlotSpec = ps;
		mParent = parent;
		mWidth = ps.getWidth();
		mHeight = ps.getHeight();
		mFG = ps.getFGColor();
		mBG = ps.getBGColor();
		mFileViewers = ps.getFileViewer();
		mMainFileViewer = ps.getFileViewer().elementAt(0);
		mXVarCode = ps.getXVarCode();
		mXTics = ps.getXTics();
		mWinXPlotMin = ps.getWinXPlotMin();
		mWinXPlotMax = ps.getWinXPlotMax();
		mXInc = ps.getXInc();
		mSymbol = ps.getSymbol();
		mSymbolSize = ps.getSymbolSize();

		mYVarCode = ps.getYVarCode();
		mWinTitle = ps.getWinTitle();
		mIncludeObsPanel = ps.isIncludeObsPanel();
		mXGrid = ps.isXGrid();
		mYGrid = ps.isYGrid();
		mYTics = ps.getYTics();
		mPlotAxes = ps.isPlotAxes();
		mPlotIsopycnals = ps.isPlotIsopycnals();
		mCanPlotIsoPycnals = ps.isCanPlotIsoPycnals();
		mAccumulateStns = ps.isAccumulateStns();
		mPlotOnlyCurrStn = ps.isPlotOnlyCurrStn();
		mRefPress = ps.getRefPress();

		// axes ranges
		mWinYPlotMin = ps.getWinYPlotMin();
		mWinYPlotMax = ps.getWinYPlotMax();
		mYInc = ps.getYInc();
		mColorPalette = this.getColorPalette(ps.getStnCycleColorPalette());
		mConnectLW = new BasicStroke(ps.getLineWidth());
		init();
		mThis = this;
	}

	public void init() {
		mFrame = this;
		mContents = this.getContentPane();
		mContents.setLayout(new BorderLayout(0, 0));
		
		mColorPalette = this.getColorPalette(mPlotSpec.getStnCycleColorPalette());
		mNumColors = mColorPalette.getNumNonBlackColors();
		assignStationColors();

		mLinePlot = new LinePlotPanel();

		mStationLegend = new StnLegendPanel(mMainFileViewer, mStnColors);
		if (this.mAccumulateStns || this.mPlotOnlyCurrStn) {
			mStationLegend.setAllUnpainted();
		}
		mStationLegend.setPlotOnlyCurrStn(mPlotOnlyCurrStn);
		
		if (!mAccumulateStns) {
			mStationLegend.updateCurrStn();
		}

		mContents.add("East", new NPixelBorder(mStationLegend, 0, 0, 0, 10));
		
		mContents.add("Center", mLinePlot);
		if (mIncludeObsPanel) {
			mCurrObsPanel = new CurrentObsPanel2(mMainFileViewer, this);
			mObsScroller = new JScrollPane(mCurrObsPanel);
			mObsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			mObsScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			mContents.add("South", mObsScroller);
		}
		mLinePlot.requestFocus();

		// add the toolbar
		mToolBar = new JOAJToolBar();
		JOAJToggleButton lockTool = new JOAJToggleButton(new ImageIcon(javaoceanatlas.PowerOceanAtlas.class
		    .getResource("images/lock_open.gif")), true);
		lockTool.setSelectedIcon(new ImageIcon(javaoceanatlas.PowerOceanAtlas.class.getResource("images/lock_closed.gif")));
		lockTool.setSelected(false);
		lockTool.setToolTipText(rb.getString("kLockPlot"));
		lockTool.setActionCommand("lock");
		lockTool.addActionListener(this);
		mToolBar.add(lockTool);
		mToolBar.add(new JLabel("  "));

		try {
			mEraseBtn = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
			    "images/eraser.png")));
			mLinearRegressionBtn = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas")
			    .getResource("images/Linear_regress.png")));
		}
		catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		mEraseBtn.setToolTipText("Erase Plot (Works only in Paint Stations mode)");
		mEraseBtn.setActionCommand("erase");
		mEraseBtn.addActionListener(this);
		mToolBar.add(mEraseBtn);
		mToolBar.add(new JLabel("  "));

		mLinearRegressionBtn
		    .setToolTipText("Toggle Regression Line (holding option/alt key restricts regression to current plot domain)");
		mLinearRegressionBtn.setActionCommand("toggleregress");
		mLinearRegressionBtn.addActionListener(this);
		mToolBar.add(mLinearRegressionBtn);

		// make an array of x labels
		String[] xlabels = new String[1];
		int[] xprecs = new int[1];
		xlabels[0] = new String(mMainFileViewer.mAllProperties[mPlotSpec.getXVarCode()].getVarLabel());
		xprecs[0] = 3;

		// make an array of y labels
		String[] ylabels = new String[1];
		int[] yprecs = new int[1];
		ylabels[0] = new String(mMainFileViewer.mAllProperties[mPlotSpec.getYVarCode()].getVarLabel());
		yprecs[0] = 3;

		mValToolBar = new ValueToolbar(mLinePlot, xlabels, xprecs, ylabels, yprecs);
		mToolBar.add(mValToolBar);
		mMetaTB = new MetadataToolbar(mLinePlot);
		OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.currElement();
		Section sech = (Section)of.mSections.currElement();
		Station sh = (Station)sech.mStations.currElement();
		mMetaTB.setNewStn(sech, sh);
		mToolBar.add(mMetaTB);
		
		mContents.add(mToolBar, "North");
		double[] xvals = new double[1];
		xvals[0] = Double.NaN;
		double[] yvals = new double[1];
		yvals[0] = Double.NaN;
		mValToolBar.setLocation(xvals, yvals);
		
		getRootPane().registerKeyboardAction(new RightListener((Object)mLinePlot, mLinePlot.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new LeftListener((Object)mLinePlot, mLinePlot.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new UpListener((Object)mLinePlot, mLinePlot.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new DownListener((Object)mLinePlot, mLinePlot.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeMe();
			}

			public void windowActivated(WindowEvent we) {
				if (!JOAConstants.ISMAC) { return; }
				// ResourceBundle b =
				// ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
				// Menu fmenu = mMenuBar.getAWTMenuBar().getMenu(0);
				// MenuItem mi = fmenu.getItem(6);
				// mi.setLabel(b.getString("kSaveGraphic"));

			}
		};
		this.addWindowListener(windowListener);

		if (mWinTitle.length() == 0) {
			this.setTitle(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel() + "-"
			    + mMainFileViewer.mAllProperties[mYVarCode].getVarLabel());
		}
		else {
			this.setTitle(mWinTitle);
		}
		mMainFileViewer.addObsChangedListener(this);
		mMainFileViewer.addDataAddedListener(this);
		mMainFileViewer.addObsFilterChangedListener(this);
		mMainFileViewer.addStnFilterChangedListener(this);
		PowerOceanAtlas.getInstance().addPrefsChangedListener(this);
		PowerOceanAtlas.getInstance().addWindowsMenuChangedListener(this);
		mMenuBar = new JOAMenuBar(this, true, mMainFileViewer);

		// offset the window down and right from 'parent' frame
		Rectangle r = mParent.getBounds();
		this.setLocation(r.x + 20, r.y + 20);

		stnLine = new GeneralPath();
		stnLineStarted = false;
	}
	
	private void assignStationColors() {
		int c = 0;
		for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);

				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					
						if (!sh.mUseStn) {
							continue;
						}
						
						mStnColors.put(sh.mStnNum, mColorPalette.getColor(c++));
						if (c == mNumColors) {
							c = 0;
						}
				}
			}
		}
	}

	public void closeMe() {
		mMainFileViewer.removeOpenWindow(mFrame);
		mMainFileViewer.removeDataAddedListener((DataAddedListener)mFrame);
		mMainFileViewer.removeStnFilterChangedListener((StnFilterChangedListener)mFrame);
		mMainFileViewer.removeObsFilterChangedListener((ObsFilterChangedListener)mFrame);
		PowerOceanAtlas.getInstance().removePrefsChangedListener((PrefsChangedListener)mFrame);
		mMainFileViewer.removeObsChangedListener((ObsChangedListener)mLinePlot);
		PowerOceanAtlas.getInstance().removeWindowsMenuChangedListener((WindowsMenuChangedListener)mFrame);
		mPlotSpec = null;
		mParent = null;
		mLinePlot = null;
		mObsMarker = null;
		mCurrObsPanel = null;
		mObsScroller = null;
		oldStn = null;
		oldSec = null;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("toggleregress")) {
			mPlotRegress = !mPlotRegress;
			if (e.getModifiers() == 24) {
				mClipToWindow = !mClipToWindow;
				mLinePlot.invalidate();
			}
			mLinePlot.revalidate();
			mLinePlot.repaint();
		}
		else if (cmd.equals("lock")) {
			mWindowIsLocked = !mWindowIsLocked;
			if (!mWindowIsLocked) {
				// are now unlocking the window
				this.setSize(this.getSize().width + 1, this.getSize().height);
				this.setSize(this.getSize().width, this.getSize().height);

				if (mCurrObsPanel != null) {
					mCurrObsPanel.setLocked(false);
				}

				mLinePlot.invalidate();
				mLinePlot.validate();
				this.invalidate();
				this.validate();
				mLinePlot.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				this.setResizable(true);
			}
			else {
				mLinePlot.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				this.setResizable(false);

				if (mCurrObsPanel != null) {
					mCurrObsPanel.setLocked(true);
				}
			}
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
				JOAConstants.DEFAULT_PRINTERJOB.setPrintable(mLinePlot, JOAConstants.DEFAULT_PAGEFORMAT);
				try {
					JOAConstants.DEFAULT_PRINTERJOB.print();
				}
				catch (PrinterException ex) {
				}
			}
		}
		else if (cmd.equals("erase") && mAccumulateStns) {
			mEraseBtn.setSelected(false);
			// set the skip flag for all stns
			for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);

					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						sh.setSkipStn(true);
					}
				}
			}
			
			mStationLegend.setAllUnpainted();
			mStationLegend.updateCurrStn();
			mLinePlot.revalidate();
			mLinePlot.repaint();
		}
		else {
			mMainFileViewer.doCommand(cmd, mFrame);
		}
	}

	public void saveAsPNG() {
		class BasicThread extends Thread {
			// ask for filename
			public void run() {
				Image image = mLinePlot.makeOffScreen((Graphics2D)mLinePlot.getGraphics(), true, true);

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
						JOAConstants.LogFileStream.writeBytes("Saved Plot:" + mThis.getTitle() + " as " + path + "\n");
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

	public void setXScale(int width) {
		mWidthCurrWindow = width;
		mScaleWidth = width;
		if (mPlotAxes) {
			mScaleWidth -= (PVPLEFT + PVPRIGHT);
		}

		mWinXScale = mScaleWidth / (mWinXPlotMax - mWinXPlotMin);
		mWinXOrigin = mWinXPlotMin;
	}

	public void setYScale(int height) {
		mHeightCurrWindow = height;
		mScaleHeight = height;
		if (mPlotAxes) {
			mScaleHeight -= ((PVPBOTTOM) + PVPTOP);
		}
		mWinYScale = -mScaleHeight / (mWinYPlotMax - mWinYPlotMin);
		mWinYOrigin = mWinYPlotMax;
	}

	public void dataAdded(DataAddedEvent evt) {
		if (mWindowIsLocked) { return; }
		mLinePlot.invalidate();
		mLinePlot.setSize(this.getSize().width + 1, this.getSize().height);
		mLinePlot.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
		
		mStationLegend.setStnColorHash(mStnColors);
		if (mAccumulateStns || mPlotOnlyCurrStn) {
			mStationLegend.setAllUnpainted();
		}
		else {
			mStationLegend.setAllPainted();
		}
		
		mStationLegend.setPlotOnlyCurrStn(mPlotOnlyCurrStn);
		mStationLegend.invalidate();
		mStationLegend.validate();
	}

	public void prefsChanged(PrefsChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		mLinePlot.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
		mBG = JOAConstants.DEFAULT_CONTENTS_COLOR;
		mPlotSpec.setBGColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
		mLinePlot.invalidate();
		mLinePlot.setSize(this.getSize().width + 1, this.getSize().height);
		mLinePlot.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
		
		mStationLegend.setStnColorHash(mStnColors);
		if (mAccumulateStns || this.mPlotOnlyCurrStn) {
			mStationLegend.setAllUnpainted();
		}
		else {
			mStationLegend.setAllPainted();
		}
		mStationLegend.setPlotOnlyCurrStn(mPlotOnlyCurrStn);
		mStationLegend.invalidate();
		mStationLegend.validate();
	}

	public void obsFilterChanged(ObsFilterChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		mLinePlot.invalidate();
		mLinePlot.setSize(this.getSize().width + 1, this.getSize().height);
		mLinePlot.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
	}

	public void stnFilterChanged(StnFilterChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		mLinePlot.invalidate();
		mLinePlot.setSize(this.getSize().width + 1, this.getSize().height);
		mLinePlot.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
		
		mStationLegend.setStnColorHash(mStnColors);
		mStationLegend.invalidate();
		mStationLegend.validate();
	}

	public void metadataChanged(MetadataChangedEvent evt) {
		if (mWindowIsLocked) { return; }
	}

	public void showConfigDialog() {
		// show configuration dialog
		mLinePlot.showConfigDialog();
	}

	@SuppressWarnings("serial")
	private class LinePlotPanel extends RubberbandPanel implements ObsChangedListener, DialogClient, ActionListener,
	    Printable {
		private Image mOffScreen = null;
		private Rubberband rbRect;
		private DialogClient mDialogClient = null;
		private JPopupMenu mPopupMenu = null;
		private Rectangle mSelectionRect = new Rectangle(0, 0, 0, 0);
		BasicStroke lw2 = new BasicStroke(2);
		Rectangle oldRect = new Rectangle();

		public LinePlotPanel() {
			this.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
			mMainFileViewer.addObsChangedListener(this);
			addMouseListener(new XYMouseHandler());
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			rbRect = new RubberbandRectangle(this);
			rbRect.setActive(true);
			mDialogClient = this;
		}

		public void setRubberbandDisplayObject(Object obj, boolean concat) {
			// this routine expects a Rectangle Object
			GeneralPath p = (GeneralPath)obj;
			Rectangle r = p.getBounds();
			mSelectionRect.setRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
			oldRect.add(mSelectionRect.getBounds());
			if (oldRect.getWidth() == 0 && oldRect.getHeight() == 0) { return; }
			oldRect.setBounds(oldRect.x - 4, oldRect.y - 4, oldRect.width + 8, oldRect.height + 8);
			paintImmediately(oldRect);
			oldRect = mSelectionRect.getBounds();
		}

		@SuppressWarnings("unchecked")
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

				xOffset = (pageFormat.getImageableWidth() - (od.width * xScale)) / 2;
				yOffset = (pageFormat.getImageableHeight() - (od.height * yScale)) / 2;

				// center the plot on the page
				g.translate(pageFormat.getImageableX() + xOffset, pageFormat.getImageableY() + yOffset);

				g.scale(xScale, yScale);

				// add the title
				String sTemp = mThis.getTitle();
				if (mPlotSpec.getOverrideLabel() != null) {
					sTemp = mPlotSpec.getOverrideLabel();
				}
				Hashtable<TextAttribute, Serializable> map = new Hashtable<TextAttribute, Serializable>();
				map.put(TextAttribute.FAMILY, JOAConstants.DEFAULT_PLOT_TITLE_FONT);
				map.put(TextAttribute.SIZE, new Float(JOAConstants.DEFAULT_PLOT_TITLE_SIZE));
				if (JOAConstants.DEFAULT_PLOT_TITLE_STYLE == Font.BOLD
				    || JOAConstants.DEFAULT_PLOT_TITLE_STYLE == (Font.BOLD | Font.ITALIC)) {
					map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
				}
				if (JOAConstants.DEFAULT_PLOT_TITLE_STYLE == Font.ITALIC
				    || JOAConstants.DEFAULT_PLOT_TITLE_STYLE == (Font.BOLD | Font.ITALIC)) {
					map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
				}
				map.put(TextAttribute.FOREGROUND, JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

				// layout the title
				TextLayout tl = new TextLayout(sTemp, (Map)map, g.getFontRenderContext());
				Rectangle2D strbounds = tl.getBounds();
				double strWidth = strbounds.getWidth();
				double hh = (PVPLEFT + (od.width - PVPRIGHT - PVPLEFT) / 2) - strWidth / 2;
				double vv = PVPTOP / 2 + strbounds.getHeight() / 2;

				JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, g, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
				    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
				    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

				// Add the BG color to the plot
				int x1, y1, width, height;
				if (mPlotAxes) {
					x1 = PVPLEFT;
					width = this.getSize().width - PVPLEFT - PVPRIGHT;
					y1 = PVPTOP;
					height = this.getSize().height - PVPTOP - PVPBOTTOM;
				}
				else {
					x1 = 0;
					y1 = 0;
					width = this.getSize().width;
					height = this.getSize().height;
				}
				g.setColor(mBG);
				g.setClip(0, 0, 2000, 2000);
				g.fillRect(x1, y1, width, height);

				if (mPlotRegress)
					plotRegressionLines(g);

				plotXY(g);

				if (mPlotAxes) {
					drawYAxis(g);
					drawXAxis(g);
				}

				if (mPlotIsopycnals) {
					plotIsopycnals(g);
				}

				g.setClip(0, 0, 20000, 20000);
				return PAGE_EXISTS;
			}
			else {
				return NO_SUCH_PAGE;
			}
		}

		public UVCoordinate getCorrectedXY(int x, int y) {
			boolean reverseY = mMainFileViewer.mAllProperties[mYVarCode].isReverseY();
			double dy = (double)y;
			double dx = (double)x;
			if (y < getMinY() || y > getMaxY() || x < getMinX() || x > getMaxX()) {
				dy = Double.NaN;
				dx = Double.NaN;
				return new UVCoordinate(dx, dy);
			}

			if (mPlotAxes && !reverseY) {
				dx -= PVPLEFT;
				dy -= PVPTOP;
			}
			else if (mPlotAxes && reverseY) {
				dy = mHeightCurrWindow - dy;
				dx -= PVPLEFT;
				dy -= PVPBOTTOM;
			}
			else if (reverseY) {
				dy = mHeightCurrWindow - dy;
			}

			return new UVCoordinate(dx, dy);
		}

		public double[] getInvTransformedX(double x) {
			double[] xvals = new double[1];
			xvals[0] = x / mWinXScale + mWinXOrigin;
			return xvals;
		}

		public double[] getInvTransformedY(double y) {
			double[] yvals = new double[1];
			yvals[0] = y / mWinYScale + mWinYOrigin;
			return yvals;
		}

		public int getMinX() {
			if (mPlotAxes) {
				return PVPLEFT;
			}
			else {
				return 1;
			}
		}

		public int getMinY() {
			if (mPlotAxes) {
				return PVPTOP;
			}
			else {
				return 1;
			}
		}

		public int getMaxX() {
			if (mPlotAxes) {
				return this.getSize().width - PVPRIGHT;
			}
			else {
				return this.getSize().width - 1;
			}
		}

		public int getMaxY() {
			if (mPlotAxes) {
				return this.getSize().height - PVPBOTTOM;
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

			if (mPlotRegress) {
				JMenuItem saveRegressItem = new JMenuItem("Save Regression Model...");
				saveRegressItem.setActionCommand("saveregress");
				saveRegressItem.addActionListener(this);
				mPopupMenu.add(saveRegressItem);
			}
			mPopupMenu.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			mPopupMenu.show(this, point.x, point.y);
		}

		public void showConfigDialog() {
			// show configuration dialog
			ConfigLinePlotDC cp = new ConfigLinePlotDC(mFrame, mMainFileViewer, mDialogClient, mPlotSpec);
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
		}

		public class XYMouseHandler extends MouseAdapter {
			public void mouseClicked(MouseEvent me) {
				if (mWindowIsLocked) { return; }
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
				if (mWindowIsLocked) { return; }
				super.mouseReleased(me);
				setRubberbandDisplayObject(new GeneralPath(), false);

				if (me.isPopupTrigger()) {
					createPopup(me.getPoint());
				}
				else {
					if (rbRect != null && me.getID() == MouseEvent.MOUSE_RELEASED) {
						zoomPlot(rbRect.getBounds(), me.isAltDown(), me.isShiftDown());
					}
				}
			}

			public void mousePressed(MouseEvent me) {
				if (mWindowIsLocked) { return; }
				super.mousePressed(me);
				if (me.isPopupTrigger()) {
					createPopup(me.getPoint());
				}
			}
		};

		@SuppressWarnings("unchecked")
		public void plotIsopycnals(Graphics2D g) {
			int saltPos = mPlotSpec.isSaltAxis();

			// compute the coordinates of the y grid
			double[] y = new double[SIGGRIDSIZE + 1];
			double[] tempVals = new double[SIGGRIDSIZE + 1];
			double yRange = mWinYPlotMax - mWinYPlotMin;
			double yDelta = yRange / SIGGRIDSIZE;
			for (int i = 0; i < SIGGRIDSIZE; i++) {
				tempVals[i] = mWinYPlotMin + (double)i * yDelta;
				y[i] = (tempVals[i] - mWinYOrigin) * mWinYScale;
				// adjust for axes labels
				if (mPlotAxes) {
					y[i] += PVPTOP; // + PVPBOTTOM;
				}
			}
			tempVals[SIGGRIDSIZE] = mWinYPlotMin + (double)SIGGRIDSIZE * yDelta;
			y[SIGGRIDSIZE] = (tempVals[SIGGRIDSIZE] - mWinYOrigin) * mWinYScale;
			if (mPlotAxes) {
				y[SIGGRIDSIZE] += PVPTOP; // + PVPBOTTOM;
			}

			// compute the coordinates of the x grid
			double[] x = new double[SIGGRIDSIZE + 1];
			double[] saltVals = new double[SIGGRIDSIZE + 1];
			double xRange = mWinXPlotMax - mWinXPlotMin;
			double xDelta = xRange / SIGGRIDSIZE;
			for (int i = 0; i < SIGGRIDSIZE; i++) {
				saltVals[i] = mWinXPlotMin + (double)i * xDelta;
				x[i] = (saltVals[i] - mWinXOrigin) * mWinXScale;

				// adjust for axes labels
				if (mPlotAxes) {
					x[i] += PVPLEFT;
				}
			}
			saltVals[SIGGRIDSIZE] = mWinXPlotMin + (double)SIGGRIDSIZE * xDelta;
			x[SIGGRIDSIZE] = (saltVals[SIGGRIDSIZE] - mWinXOrigin) * mWinXScale;
			if (mPlotAxes) {
				x[SIGGRIDSIZE] += PVPLEFT;
			}

			// compute the array densities to plot
			double sigmaVals[][] = new double[SIGGRIDSIZE + 1][SIGGRIDSIZE + 1];
			double maxSigma = 0;
			double minSigma = 10000;
			for (int i = 0; i < SIGGRIDSIZE + 1; i++) {
				for (int j = 0; j < SIGGRIDSIZE + 1; j++) {
					double salt = saltVals[j];
					double temp = tempVals[i];
					double sigma = JOAFormulas.sigma(salt, temp, mPlotSpec.getRefPress());
					sigmaVals[i][j] = sigma;
					maxSigma = sigmaVals[i][j] > maxSigma ? sigmaVals[i][j] : maxSigma;
					minSigma = sigmaVals[i][j] < minSigma ? sigmaVals[i][j] : minSigma;
				}
			}

			// get a pretty range for the max and min sigmas
			Triplet prettyNums = JOAFormulas.GetPrettyRange(minSigma, maxSigma);
			minSigma = prettyNums.getVal1();
			maxSigma = prettyNums.getVal2();
			double mSigmaInc = prettyNums.getVal3() / 2.0;
			if (maxSigma - minSigma > 10.0) {
				mSigmaInc = 1.0;
			}

			// create the raster layer
			// need the colorbar and the transparency setting
			double[] zBox = new double[4];
			int c = 0;
			double z1 = 0.0;
			double z2 = 0.0;
			double z3 = 0.0;
			double z4 = 0.0;
			int nFoul = 0;
			int lowerX = 0;
			int upperX = 0;
			int numXPixels = 0;
			double delta = 0.0;
			int newX = 0;
			double val = 0.0;
			int newY = 0;
			int startX = 0;
			int startY = 0;
			int colorIndex = 0;
			int oldColorIndex = 0;
			double t, u;

			// create autoscaled colorbar

			Color[] colors;
			// colors = mColorBar.getColors();
			// for (int i1 = 0, i2 = 1; i1 < SIGGRIDSIZE; i1++, i2++) {
			// int lowerY = (int) y[c];
			// int upperY = (int) y[c + 1];
			// int numYPixels = upperY - lowerY;
			// if (numYPixels == 0) {
			// numYPixels = 1;
			// }
			// if (numYPixels < 0) {
			// numYPixels = -numYPixels;
			// upperY = (int) y[c];
			// lowerY = (int) y[c + 1];
			// }
			// yDelta = upperY - lowerY;
			// for (int j1 = 0, j2 = 1; j1 < SIGGRIDSIZE; j1++, j2++) {
			// z1 = sigmaVals[i1][j1];
			// z2 = sigmaVals[i1][j2];
			// z3 = sigmaVals[i2][j2];
			// z4 = sigmaVals[i2][j1];
			//          
			// double max4 = JOAConstants.MISSINGVALUE;
			// double min4 = JOAConstants.MISSINGVALUE;
			// nFoul = 0;
			// for (int i = 0; i < 4; i++) {
			// if (JOAConstants.MISSINGVALUE != zBox[i]) {
			// max4 = min4 = zBox[i];
			// }
			// if (JOAConstants.MISSINGVALUE == zBox[i]) {
			// nFoul++;
			// }
			// }
			// if (nFoul > 1) {
			// continue;
			// }
			//          
			// lowerX = (int) x[i1];
			// upperX = (int) x[i1 + 1];
			//
			// // compute the number of new pixels in each direction
			// numXPixels = upperX - lowerX;
			// if (numXPixels == 0) {
			// numXPixels = 1;
			// }
			// xDelta = upperX - lowerX;
			//					
			//
			// // ignore cases with 2 missing values
			//
			// // ignore cases with 1 missing values
			//
			// // got all four values {
			// // interpolate
			// if (numYPixels == 1) {
			// delta = (z2 - z1) / numXPixels;
			// // interpolate along the row
			// for (int jj = 0; jj < numXPixels; jj++) {
			// newX = lowerX + jj;
			// val = z1 + jj * delta;
			// if (!mFileViewer.mAllProperties[surfVarNum].isReverseY()) {
			// newY = maxY - newY + 2 * pTopMargin;
			// }
			// if (mColorBar.getColorIndex(val) != JOAConstants.MISSINGVALUE) {
			// g.setColor(colors[mColorBar.getColorIndex(val)]);
			// }
			// else {
			// g.setColor(Color.white);
			// }
			// g.drawLine(newX, lowerY, newX, lowerY);
			// }
			// }
			// else if (numXPixels == 1) {
			// // interpolate along the column
			// delta = (z4 - z1) / numYPixels;
			// for (int ii = 0; ii < numYPixels; ii++) {
			// newY = lowerY + ii;
			// val = z1 + ii * delta;
			// if (!mFileViewer.mAllProperties[surfVarNum].isReverseY()) {
			// newY = maxY - newY + 2 * pTopMargin;
			// }
			// if (mColorBar.getColorIndex(val) != JOAConstants.MISSINGVALUE) {
			// g.setColor(colors[mColorBar.getColorIndex(val)]);
			// }
			// else {
			// g.setColor(Color.white);
			// }
			// g.drawLine(lowerX, newY, lowerX, newY);
			// }
			// }
			// else {
			// for (int jj = 0; jj < numXPixels; jj++) {
			// for (int ii = 0; ii < numYPixels; ii++) {
			// newX = lowerX + jj;
			// newY = lowerY + ii;
			// if (newX == 0 && newY == 0) {
			// continue;
			// }
			// t = (newX - lowerX) / xDelta;
			// u = 0;
			// if (yDelta > 0) {
			// u = (newY - lowerY) / yDelta;
			// }
			// else {
			// u = (lowerY - newY) / yDelta;
			// }
			// if (!mFileViewer.mAllProperties[surfVarNum].isReverseY()) {
			// newY = maxY - newY + 2 * pTopMargin;
			// }
			//
			// val = (1 - t) * (1 - u) * z1 + t * (1 - u) * z2 + t * u * z3 + (1 - t)
			// * u * z4;
			//
			// // apply an observation filter here
			//
			// /*
			// * if (mFileViewer.mObsFilterActive) { double c1Val = 0.0, c2Val =
			// * 0.0, c3Val = 0.0, c4Val = 0.0; if
			// * (mFileViewer.mCurrObsFilter.mCriteria1Active) c1Val =
			// * bh.mDValues[c1Pos]; if
			// * (mFileViewer.mCurrObsFilter.mCriteria2Active) c2Val =
			// * bh.mDValues[c2Pos]; if
			// * (mFileViewer.mCurrObsFilter.mCriteria3Active) c3Val =
			// * bh.mDValues[c3Pos]; if
			// * (mFileViewer.mCurrObsFilter.mCriteria4Active) c4Val =
			// * bh.mDValues[c4Pos];
			// *
			// * if (!mFileViewer.mCurrObsFilter.testValues(c1Val, c2Val,
			// * c3Val, c4Val, 0, 0, 0, 0)) val =
			// * 100;//JOAConstants.MISSINGVALUE; }
			// */
			//
			// colorIndex = mColorBar.getColorIndex(val);
			// if (mColorBar.getColorIndex(val) != JOAConstants.MISSINGVALUE) {
			// g.setColor(colors[mColorBar.getColorIndex(val)]);
			// }
			// else {
			// g.setColor(Color.white);
			// }
			// if (ii == 0) {
			// oldColorIndex = colorIndex;
			// startX = newX;
			// startY = newY;
			// }
			// else {
			// if (colorIndex != oldColorIndex) {
			// if (oldColorIndex != JOAConstants.MISSINGVALUE) {
			// g.setColor(colors[oldColorIndex]);
			// }
			// else {
			// g.setColor(Color.white);
			// }
			// g.drawLine(startX, startY, newX, newY);
			// oldColorIndex = colorIndex;
			// startX = newX;
			// startY = newY;
			// }
			// }
			// }
			// // finish the line
			// if (oldColorIndex != JOAConstants.MISSINGVALUE) {
			// g.setColor(colors[oldColorIndex]);
			// }
			// else {
			// g.setColor(Color.white);
			// }
			// g.drawLine(startX, startY, newX, newY);
			// }
			// }
			// }
			// c++;
			//
			// }

			// draw the contour liness
			UVCoordinate side1 = null;
			UVCoordinate side2 = null;
			UVCoordinate side3 = null;
			UVCoordinate side4 = null;
			UVCoordinate side5 = null;
			int numContours = (int)((maxSigma - minSigma) / mSigmaInc);
			double contourValues[] = new double[numContours];
			int numContourPairs[] = new int[numContours];
			Vector<UVCoordinate>[] vectors = new Vector[numContours];
			Vector<UVCoordinate>[] contourCoords = vectors;
			GeneralPath[] contourLines = new GeneralPath[numContours];
			for (int i = 0; i < numContours; i++) {
				contourValues[i] = minSigma + (double)i * mSigmaInc;
				contourCoords[i] = new Vector<UVCoordinate>();
				contourLines[i] = new GeneralPath();
				numContourPairs[i] = 0;
			}
			double level = 0.0, xCross1 = 0.0, yCross2 = 0.0, xCross3 = 0.0, yCross4 = 0.0, xCross5 = 0.0, yCross1 = 0.0, xCross2 = 0.0, yCross3 = 0.0, xCross4 = 0.0, yCross5 = 0.0;
			g.setColor(JOAFormulas.getContrastingColor(mBG));
			for (int i1 = 0, i2 = 1; i1 < SIGGRIDSIZE; i1++, i2++) {
				for (int j1 = 0, j2 = 1; j1 < SIGGRIDSIZE; j1++, j2++) {
					z1 = sigmaVals[i1][j1];
					z2 = sigmaVals[i1][j2];
					z3 = sigmaVals[i2][j2];
					z4 = sigmaVals[i2][j1];

					zBox[0] = z1;
					zBox[1] = z2;
					zBox[2] = z3;
					zBox[3] = z4;
					double max4 = JOAConstants.MISSINGVALUE;
					double min4 = JOAConstants.MISSINGVALUE;
					nFoul = 0;
					for (int i = 0; i < 4; i++) {
						if (JOAConstants.MISSINGVALUE != zBox[i]) {
							max4 = min4 = zBox[i];
						}
						if (JOAConstants.MISSINGVALUE == zBox[i]) {
							nFoul++;
						}
					}
					if (nFoul > 1) {
						continue;
					}
					for (int i = 0; i < 4; i++) {
						min4 = (zBox[i] < min4 && zBox[i] != JOAConstants.MISSINGVALUE) ? zBox[i] : min4;
						max4 = (zBox[i] > max4 && zBox[i] != JOAConstants.MISSINGVALUE) ? zBox[i] : max4;
					}

					for (int k = 0; k < numContours; k++) {
						level = contourValues[k];
						if (!(min4 <= level && level <= max4)) {
							continue;
						}

						side1 = intersect(level, x[j2], y[i1], z2, x[j1], y[i2], z4);
						if (side1 != null) {
							xCross1 = side1.u;
							yCross1 = side1.v;
						}

						side2 = intersect(level, x[j1], y[i1], z1, x[j1], y[i2], z4);
						if (side2 != null) {
							xCross2 = side2.u;
							yCross2 = side2.v;
						}

						side3 = intersect(level, x[j1], y[i1], z1, x[j2], y[i1], z2);
						if (side3 != null) {
							xCross3 = side3.u;
							yCross3 = side3.v;
						}

						side4 = intersect(level, x[j2], y[i1], z2, x[j2], y[i2], z3);
						if (side4 != null) {
							xCross4 = side4.u;
							yCross4 = side4.v;
						}

						side5 = intersect(level, x[j1], y[i2], z4, x[j2], y[i2], z3);
						if (side5 != null) {
							xCross5 = side5.u;
							yCross5 = side5.v;
						}

						UVCoordinate uv = null;
						if (side1 != null && side2 != null) {
							uv = new UVCoordinate(xCross1, yCross1);
							contourCoords[k].addElement(uv);
							uv = new UVCoordinate(xCross2, yCross2);
							contourCoords[k].addElement(uv);
							numContourPairs[k]++;
						}

						if (side2 != null && side3 != null) {
							uv = new UVCoordinate(xCross2, yCross2);
							contourCoords[k].addElement(uv);
							uv = new UVCoordinate(xCross3, yCross3);
							contourCoords[k].addElement(uv);
							numContourPairs[k]++;
						}

						if (side3 != null && side1 != null) {
							uv = new UVCoordinate(xCross3, yCross3);
							contourCoords[k].addElement(uv);
							uv = new UVCoordinate(xCross1, yCross1);
							contourCoords[k].addElement(uv);
							numContourPairs[k]++;
						}

						if (side1 != null && side4 != null) {
							uv = new UVCoordinate(xCross1, yCross1);
							contourCoords[k].addElement(uv);
							uv = new UVCoordinate(xCross4, yCross4);
							contourCoords[k].addElement(uv);
							numContourPairs[k]++;
						}

						if (side4 != null && side5 != null) {
							uv = new UVCoordinate(xCross4, yCross4);
							contourCoords[k].addElement(uv);
							uv = new UVCoordinate(xCross5, yCross5);
							contourCoords[k].addElement(uv);
							numContourPairs[k]++;
						}

						if (side5 != null && side1 != null) {
							uv = new UVCoordinate(xCross5, yCross5);
							contourCoords[k].addElement(uv);
							uv = new UVCoordinate(xCross1, yCross1);
							contourCoords[k].addElement(uv);
							numContourPairs[k]++;
						}
					} // End loop over contour levels.
				} // End loop across.
			} // End loop down

			Hashtable<TextAttribute, Serializable> map = new Hashtable<TextAttribute, Serializable>();
			map.put(TextAttribute.FAMILY, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_FONT);
			map.put(TextAttribute.SIZE, new Float(JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE));
			if (JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == Font.BOLD
			    || JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == (Font.BOLD | Font.ITALIC)) {
				map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
			}
			if (JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == Font.ITALIC
			    || JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == (Font.BOLD | Font.ITALIC)) {
				map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
			}
			map.put(TextAttribute.FOREGROUND, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR);

			double uright, vright;
			UVCoordinate[] leftGapLoc = new UVCoordinate[numContours];
			// generate the contour shapes
			for (int k = 0; k < numContours; k++) {
				if (numContourPairs[k] == 0) {
					continue;
				}

				// mid point of current contour
				int mid = numContourPairs[k];

				// value of current contour
				level = contourValues[k];

				// figure out how long this string is going to be
				String valStr = JOAFormulas.formatDouble(level, 2, false);
				TextLayout tl = new TextLayout(valStr, (Map)map, g.getFontRenderContext());
				Rectangle2D strbounds = tl.getBounds();
				double strWidth = strbounds.getWidth();

				// point 2 points to the left of the middle point
				UVCoordinate uvLeft = (UVCoordinate)contourCoords[k].elementAt(mid - 2);
				uright = uvLeft.u;
				vright = uvLeft.v;

				// form the contour with break for the level label
				boolean leftGap = false;
				boolean setGap = false;
				double slope = 0.0;
				for (int j = 0; j < contourCoords[k].size() - 1; j++) {
					UVCoordinate uvRight = (UVCoordinate)contourCoords[k].elementAt(j + 1);
					UVCoordinate uvMid = (UVCoordinate)contourCoords[k].elementAt(j);

					// compute distance
					double dist = Math.sqrt(((uvRight.u - uvLeft.u) * (uvRight.u - uvLeft.u))
					    + ((uvRight.v - uvLeft.v) * (uvRight.v - uvLeft.v)));
					if (j > mid - 3 && dist >= strWidth && !leftGap) {
						// compute slope
						slope = (uvRight.v - uvLeft.v) / (uvRight.u - uvLeft.u);
						double yNew = uvLeft.v;
						double xNew = uvLeft.u;

						// compute y intercept
						double b = yNew - slope * xNew;

						// u coord of end point
						uright = uvLeft.u + dist;

						// v coord of end point
						vright = slope * uright + b;

						// add line segment to contour
						// contourLines[k].moveTo((float)uright, (float)vright);
						// contourLines[k].lineTo((float)uvRight.u, (float)uvRight.v);
						leftGap = true;

						// j += 2;

						// store the points that mark the label blank area
						leftGapLoc[k] = uvLeft;
						continue;
					}
					else if (j > mid - 3 && dist < strWidth) {
						continue;
					}

					UVCoordinate uv = (UVCoordinate)contourCoords[k].elementAt(j);
					UVCoordinate uv2 = (UVCoordinate)contourCoords[k].elementAt(j + 1);
					if (leftGap && !setGap) {
						if (Math.abs(slope) > 1.0) {
							contourLines[k].moveTo((float)uvMid.u, (float)uvMid.v);
						}
						else {
							contourLines[k].moveTo((float)uright, (float)vright);
						}
						setGap = true;
					}
					else {
						contourLines[k].moveTo((float)uv.u, (float)uv.v);
					}
					contourLines[k].lineTo((float)uv2.u, (float)uv2.v);
				}
			}

			// plot the contours
			if (JOAConstants.THICKEN_CONTOUR_LINES) {
				g.setStroke(new BasicStroke(2.5f));
			}
			else {
				g.setStroke(new BasicStroke(0.5f));
			}
			for (int k = 0; k < numContours; k++) {
				// set the clip
				// compute a rectangle to subtract from clip area
				level = contourValues[k];

				/*
				 * if (contourCoords[k].size() > 30) { // get the points on either side
				 * of the mid-point int mid = contourCoords[k].size()/2; UVCoordinate
				 * uvMid = (UVCoordinate)contourCoords[k].elementAt(mid); UVCoordinate
				 * uvLeft = (UVCoordinate)contourCoords[k].elementAt(mid - 2);
				 * UVCoordinate uvRight = (UVCoordinate)contourCoords[k].elementAt(mid +
				 * 2); // compute hypotenuse double hyp = Math.sqrt(((uvRight.u -
				 * uvLeft.u) * (uvRight.u - uvLeft.u)) + ((uvRight.v - uvLeft.v) *
				 * (uvRight.v - uvLeft.v))); double opp = Math.abs(uvRight.v -
				 * uvLeft.v);
				 * 
				 * double rangle = Math.asin(opp/hyp); double angle = rangle *
				 * (180.0/Math.PI);
				 * 
				 * String valStr = JOAFormulas.formatDouble(level, 2, false); TextLayout
				 * tl = new TextLayout(valStr, (Map)map, g.getFontRenderContext());
				 * Rectangle2D strbounds = tl.getBounds(); double strWidth =
				 * strbounds.getWidth(); double ox = uvMid.u + (Math.sin(rangle) * 5);
				 * double oy = uvMid.v + (Math.cos(rangle) * 5); int[] xpoints = new
				 * int[4]; int[] ypoints = new int[4]; int npoints = 4; xpoints[0] =
				 * JOAFormulas.rotatePixelX(ox, rangle); ypoints[0] =
				 * JOAFormulas.rotatePixelY(oy, rangle); xpoints[1] =
				 * JOAFormulas.rotatePixelX(ox, rangle); ypoints[1] =
				 * JOAFormulas.rotatePixelY(oy-10, rangle); xpoints[2] =
				 * JOAFormulas.rotatePixelX(ox+strWidth, rangle); ypoints[2] =
				 * JOAFormulas.rotatePixelY(oy-10, rangle); xpoints[3] =
				 * JOAFormulas.rotatePixelX(ox+strWidth, rangle); ypoints[3] =
				 * JOAFormulas.rotatePixelY(oy, rangle); Polygon poly = new
				 * Polygon(xpoints, ypoints, npoints);
				 * 
				 * g.setClip(0, 0, 10000, 10000); int ix = (int)ox; int iy = (int)oy;
				 * rect2.setBounds((int)ix, (int)iy, 195, 236); fancyClip = null;
				 * fancyClip = new Area(rect); fancyClip.subtract(new Area(poly));
				 * g.setClip(fancyClip); } else g.setClip(x1, y1, width, height);
				 */

				try {
					// contourLines[k].closePath();
					g.draw(contourLines[k]);
				}
				catch (Exception ex) {
				}
				g.setClip(0, 0, 20000, 20000);
			}

			// plot the contour labels
			for (int k = 0; k < numContours; k++) {
				level = contourValues[k];

				if (contourCoords[k].size() > 30) {
					// get the points on either side of the mid-point
					int mid = contourCoords[k].size() / 2;
					UVCoordinate uvLeft = (UVCoordinate)contourCoords[k].elementAt(mid - 2);
					UVCoordinate uvRight = (UVCoordinate)contourCoords[k].elementAt(mid + 2);

					// compute hypotenuse
					double hyp = Math.sqrt(((uvRight.u - uvLeft.u) * (uvRight.u - uvLeft.u))
					    + ((uvRight.v - uvLeft.v) * (uvRight.v - uvLeft.v)));
					double opp = Math.abs(uvRight.v - uvLeft.v);

					double rangle = Math.asin(opp / hyp);
					double angle = rangle * (180.0 / Math.PI);
					// double angle = JOAFormulas.getAngle(uvLeft.u, uvRight.u, uvLeft.v,
					// uvRight.v);

					String valStr = JOAFormulas.formatDouble(level, 2, false);
					double ox = uvLeft.u + (Math.sin(rangle) * 5);
					double oy = uvLeft.v + (Math.cos(rangle) * 5);
					JOAFormulas.drawStyledString(valStr, ox, oy, g, angle, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_FONT,
					    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE,
					    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR);
				}
			}

			// plot the label
			String sTemp = "P = " + JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getRefPress()), 1, false) + " (DB)";
			JOAFormulas.drawStyledString(sTemp, PVPLEFT + 10, this.getHeight() - PVPBOTTOM - 10, g, 0.0,
			    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_FONT, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE,
			    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR);
		}

		public UVCoordinate intersect(double level, double x1, double y1, double v1, double x2, double y2, double v2) {
			double s, minV, maxV, Oms;
			double xCross, yCross;

			if (v1 == JOAConstants.MISSINGVALUE || v2 == JOAConstants.MISSINGVALUE) { return null; }
			minV = (v1 < v2) ? v1 : v2;
			maxV = (v1 > v2) ? v1 : v2;
			if (!(minV <= level && level < maxV)) { return null; }
			s = (level - minV) / Math.abs(v1 - v2);
			Oms = 1 - s;
			if (v1 < v2) {
				xCross = s * x2 + Oms * x1;
				yCross = s * y2 + Oms * y1;
			}
			else {
				xCross = s * x1 + Oms * x2;
				yCross = s * y1 + Oms * y2;
			}
			return new UVCoordinate(xCross, yCross);
		}

		public void zoomPlot(Rectangle newRect, boolean mode, boolean mode2) {
			if (mWindowIsLocked) { return; }
			boolean reverseY = mMainFileViewer.mAllProperties[mYVarCode].isReverseY();
			// convert corners of rectangle to new plot range
			Rectangle newBounds = rbRect.lastBounds();
			int x1 = newBounds.x;
			int x2 = x1 + newBounds.width;
			int y1 = newBounds.y;
			int y2 = y1 + newBounds.height;

			if (newBounds.width < 10 || newBounds.height < 10) { return; }

			// adjust for axes labels
			if (mPlotAxes && !reverseY) {
				x1 -= PVPLEFT;
				y1 -= PVPTOP;
				x2 -= PVPLEFT;
				y2 -= PVPTOP;
			}
			else if (mPlotAxes && reverseY) {
				y1 = mHeightCurrWindow - y1;
				x1 -= PVPLEFT;
				y1 -= PVPBOTTOM;
				y2 = mHeightCurrWindow - y2;
				x2 -= PVPLEFT;
				y2 -= PVPBOTTOM;
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
				mPlotSpec.setWinYPlotMax(mWinYPlotMax);
				mPlotSpec.setYInc(mYInc);

				if (!mode2) {
					mWinXPlotMax = x2 / mWinXScale + mWinXOrigin;
					mWinXPlotMin = x1 / mWinXScale + mWinXOrigin;

					newRange = JOAFormulas.GetPrettyRange(mWinXPlotMin, mWinXPlotMax);
					mWinXPlotMin = newRange.getVal1();
					mWinXPlotMax = newRange.getVal2();
					mXInc = newRange.getVal3();
					mPlotSpec.setWinXPlotMin(mWinXPlotMin);
					mPlotSpec.setWinXPlotMax(mWinXPlotMax);
					mPlotSpec.setXInc(mXInc);
				}

				mPlotSpec.setPlotIsopycnals(mPlotIsopycnals);
				mPlotSpec.setCanPlotIsoPycnals(mCanPlotIsoPycnals);
				mPlotSpec.setPlotOnlyCurrStn(mPlotOnlyCurrStn);
				mPlotSpec.setAccumulateStns(mAccumulateStns);
				mPlotSpec.setRefPress(mRefPress);

				// zoom using current window
				// invalidate and replot
				invalidate();
				paintComponent(this.getGraphics());
				rbRect.end(rbRect.getStretched());
			}
			else {
				LinePlotSpecification ps = new LinePlotSpecification();
				for (FileViewer fv : mFileViewers) {
					ps.setFileViewer(fv);
				}

				if (reverseY) {
					ps.setWinYPlotMax(y2 / mWinYScale + mWinYOrigin);
					ps.setWinYPlotMin(y1 / mWinYScale + mWinYOrigin);
				}
				else {
					ps.setWinYPlotMax(y1 / mWinYScale + mWinYOrigin);
					ps.setWinYPlotMin(y2 / mWinYScale + mWinYOrigin);
				}

				if (!mode2) {
					ps.setWinXPlotMax(x2 / mWinXScale + mWinXOrigin);
					ps.setWinXPlotMin(x1 / mWinXScale + mWinXOrigin);
					mPlotSpec.setXInc(mXInc);

					Triplet newRange = JOAFormulas.GetPrettyRange(ps.getWinXPlotMin(), ps.getWinXPlotMax());
					ps.setWinXPlotMin(newRange.getVal1());
					ps.setWinXPlotMax(newRange.getVal2());
					ps.setXInc(newRange.getVal3());
					ps.setXVarCode(mXVarCode);
					ps.setXTics(mXTics);
					 ps.setSymbol(mSymbol);
					 ps.setSymbolSize(mSymbolSize);
				}
				else {
					ps.setWinXPlotMax(mWinXPlotMax);
					ps.setWinXPlotMin(mWinXPlotMin);
					ps.setXInc(mXInc);
					ps.setXVarCode(mXVarCode);
					ps.setXTics(mXTics);
				}

				Triplet newRange = JOAFormulas.GetPrettyRange(ps.getWinYPlotMin(), ps.getWinYPlotMax());
				ps.setWinYPlotMin(newRange.getVal1());
				ps.setWinYPlotMax(newRange.getVal2());
				ps.setYInc(newRange.getVal3());

				ps.setFGColor(mFG);
				ps.setBGColor(mBG);
				ps.setFileViewer(mMainFileViewer);
				ps.setYVarCode(mYVarCode);
				ps.setWinTitle(mWinTitle + "z");
				ps.setIncludeObsPanel(mIncludeObsPanel);
				ps.setXGrid(mXGrid);
				ps.setYGrid(mYGrid);
				ps.setYTics(mYTics);
				ps.setPlotAxes(mPlotAxes);
				ps.setWidth(450);
				ps.setHeight(450);
				ps.setReverseY(mMainFileViewer.mAllProperties[mYVarCode].isReverseY());
				ps.setPlotIsopycnals(mPlotIsopycnals);
				ps.setCanPlotIsoPycnals(mCanPlotIsoPycnals);
				ps.setPlotOnlyCurrStn(mPlotOnlyCurrStn);
				ps.setAccumulateStns(mAccumulateStns);
				ps.setRefPress(mRefPress);
				ps.setOverrideLabel(mPlotSpec.getOverrideLabel());
				ps.setStnCycleColorPalette(mPlotSpec.getStnCycleColorPalette());
				ps.setIgnoreMissingObs(mPlotSpec.isIgnoreMissingObs());
				ps.setLineWidth(mPlotSpec.getLineWidth());

				// make a new plot window
				try {
					ps.writeToLog("Zoomed XY Plot: (" + mParent.getTitle() + "):");
				}
				catch (Exception ex) {
				}
				
				JOALinePlotWindow plotWind = new JOALinePlotWindow(ps, mFrame);
				plotWind.pack();
				plotWind.setVisible(true);
				mMainFileViewer.addOpenWindow(plotWind);
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(mWidth, mHeight - 50);
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

				if (oldStn != null) {
					oldStn = null;
				}
			}
			catch (Exception e) {

			}
		}

		@SuppressWarnings("unchecked")
		public Image makeOffScreen(Graphics2D g, boolean addTitle, boolean plotOverlays) {
			Image outImage = null;
			int pWidth = getSize().width;

			outImage = createImage(pWidth, getSize().height);

			Graphics2D og = (Graphics2D)outImage.getGraphics();
			super.paintComponent(og);

			og.setColor(Color.white);
			og.fillRect(0, 0, 2000, 2000);

			int x1, y1, width, height;
			if (mPlotAxes) {
				x1 = PVPLEFT;
				width = this.getSize().width - PVPLEFT - PVPRIGHT;
				y1 = PVPTOP;
				height = this.getSize().height - PVPTOP - PVPBOTTOM;
			}
			else {
				x1 = 0;
				y1 = 0;
				width = this.getSize().width;
				height = this.getSize().height;
			}
			og.setColor(mBG);
			og.fillRect(x1, y1, width, height);

			setXScale(getSize().width);
			setYScale(getSize().height);

			computeRegressions();

			plotXY(og);
			if (mPlotAxes) {
				drawYAxis(og);
				drawXAxis(og);
			}

			if (mPlotIsopycnals) {
				plotIsopycnals(og);
			}

			if (mPlotRegress)
				plotRegressionLines(og);

			if (addTitle) {
				// add the title
				String sTemp = mThis.getTitle();
				if (mPlotSpec.getOverrideLabel() != null) {
					sTemp = mPlotSpec.getOverrideLabel();
				}

				// layout the title
				Font font = new Font(JOAConstants.DEFAULT_PLOT_TITLE_FONT, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
				    JOAConstants.DEFAULT_PLOT_TITLE_SIZE);
				FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
				int strWidth = fm.stringWidth(sTemp);
				double hh = (PVPLEFT + (this.getWidth() - PVPRIGHT - PVPLEFT) / 2) - strWidth / 2;
				double vv = PVPTOP / 2 + fm.getHeight() / 2;

				JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, og, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
				    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
				    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);
			}

			if (plotOverlays) {
					for (FileViewer fv : mFileViewers) {
						plotStnLine(og, false, fv);
					}
			}

			Dimension od = this.getSize();

			og.dispose();
			return outImage;
		}

		public void paintComponent(Graphics gin) {
			Graphics2D g = (Graphics2D)gin;
			if (mOffScreen == null) {
				mOffScreen = makeOffScreen(g, JOAConstants.DEFAULT_PLOT_TITLES, false);
				g.drawImage(mOffScreen, 0, 0, null);
			}
			else {
				g.drawImage(mOffScreen, 0, 0, null);
				g.setColor(JOAConstants.DEFAULT_SELECTION_REGION_BG_COLOR);
				g.fillRect((int)mSelectionRect.getX(), (int)mSelectionRect.getY(), (int)mSelectionRect.getWidth(),
				    (int)mSelectionRect.getHeight());
				g.setColor(JOAConstants.DEFAULT_SELECTION_REGION_OUTLINE_COLOR);
				g.setStroke(lw2);
				g.drawRect((int)mSelectionRect.getX(), (int)mSelectionRect.getY(), (int)mSelectionRect.getWidth(),
				    (int)mSelectionRect.getHeight());
				g.setColor(Color.black);
			}

//			if (!mWindowIsLocked) {
//				for (FileViewer fv : mFileViewers) {
//					plotStnLine(g, false, fv);
//				}
//			}

			if (!mWindowIsLocked) {
				g.setClip(0, 0, this.getSize().width, this.getSize().height);
				if (mObsMarker != null) {
					mObsMarker.drawMarker(g, false);
				}
			}
		}

		public void plotXY(Graphics2D g) {
			int xPos, yPos;
			double theVal = -99;
			double x, y;
			int currSymbol;
			int currSymbolSize;

			int x1, width, y1, height;
			if (mPlotAxes) {
				x1 = PVPLEFT;
				width = this.getSize().width - PVPLEFT - PVPRIGHT;
				y1 = PVPTOP;
				height = this.getSize().height - PVPTOP - PVPBOTTOM;
			}
			else {
				x1 = 0;
				y1 = 0;
				width = this.getSize().width;
				height = this.getSize().height;
			}
			g.setClip(x1, y1, width, height);

			boolean reverseY = mMainFileViewer.mAllProperties[mYVarCode].isReverseY();

			if (mPlotOnlyCurrStn) {
				for (FileViewer fv : mFileViewers) {
					plotOneStn(fv, g, reverseY);
				}
			}
			else {
				// plot all the stations
				for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
					OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

					for (int sec = 0; sec < of.mNumSections; sec++) {
						Section sech = (Section)of.mSections.elementAt(sec);

						xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
						yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
						if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
							continue;
						}
						for (int stc = 0; stc < sech.mStations.size(); stc++) {
							Station sh = (Station)sech.mStations.elementAt(stc);
							if (!sh.mUseStn || (mAccumulateStns && sh.isSkipStn())) {
								continue;
							}
							Color stnColor = mStnColors.get(sh.mStnNum);

							if (xPos >= 0 && yPos >= 0) {
								for (int b = 0; b < sh.mNumBottles; b++) {
									Bottle bh = (Bottle)sh.mBottles.elementAt(b);
									boolean keepBottle;
									if (mMainFileViewer.mObsFilterActive) {
										keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
									}
									else {
										keepBottle = true;
									}

									if (mMainFileViewer.mObsFilterActive && !keepBottle) {
										if (mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
											continue;
										}
									}

									currSymbol = mSymbol;
									currSymbolSize = mSymbolSize;
									xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
									if (xPos == -1) {
										continue;
									}

									x = bh.mDValues[xPos];
									y = bh.mDValues[yPos];
									if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
										x = (x - mWinXOrigin) * mWinXScale;
										y = (y - mWinYOrigin) * mWinYScale;

										// adjust for axes labels
										if (mPlotAxes && !reverseY) {
											x += PVPLEFT;
											y += PVPTOP;
										}
										else if (mPlotAxes && reverseY) {
											y = mHeightCurrWindow - y;
											x += PVPLEFT;
											y -= PVPBOTTOM;
										}
										else if (reverseY) {
											y = mHeightCurrWindow - y;
										}
										// plot point
										g.setColor(stnColor);
										JOAFormulas.plotSymbol(g, currSymbol, (int)x, (int)y, currSymbolSize);
									}
								}
								for (FileViewer fv : mFileViewers) {
									plotLine(g, sech, sh, fv, stnColor);
								}
							}
						}
					}
				}
			}

			// re plot the points if an obsfilter active
			// active observation filter overrides plot enhancement
			if (mMainFileViewer.mObsFilterActive && !mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
				if (mPlotOnlyCurrStn) {
					OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.currElement();
					Section sech = (Section)of.mSections.currElement();
					yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
					boolean canPlot = true;
					if (sech.mNumCasts == 0 || yPos == -1) {
						canPlot = false;
					}
					Station sh = (Station)sech.mStations.currElement();
					if (!sh.mUseStn) {
						canPlot = false;
					}

					Color stnColor = mStnColors.get(sh.mStnNum);

					if (canPlot) {
						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle)sh.mBottles.elementAt(b);
							boolean keepBottle;
							if (mMainFileViewer.mObsFilterActive) {
								keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
							}
							else {
								keepBottle = true;
							}

							currSymbol = mSymbol;
							currSymbolSize = mSymbolSize;
							xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
							if (xPos == -1) {
								continue;
							}

							if (keepBottle) {
								if (mMainFileViewer.mObsFilterActive && !keepBottle) {
									if (mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
										continue;
									}
								}
								else if (mMainFileViewer.mObsFilterActive && keepBottle) {
									if (mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
										currSymbolSize = mSymbolSize;
										if (mMainFileViewer.mCurrObsFilter.isUseContrastingColor()) {
											mOverRideColor = true;
											mContrastColor = mMainFileViewer.mCurrObsFilter.getContrastingColor();
										}
									}
									else if (mMainFileViewer.mCurrObsFilter.isEnlargeSymbol()) {
										currSymbolSize = currSymbolSize * 2;
										if (mMainFileViewer.mCurrObsFilter.isUseContrastingColor()) {
											mOverRideColor = true;
											mContrastColor = mMainFileViewer.mCurrObsFilter.getContrastingColor();
										}
									}
									else if (!mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
										mOverRideColor = false;
										currSymbol = mMainFileViewer.mCurrObsFilter.getSymbol();
										currSymbolSize = mMainFileViewer.mCurrObsFilter.getSymbolSize();
										if (mMainFileViewer.mCurrObsFilter.isUseContrastingColor()) {
											mOverRideColor = true;
											mContrastColor = mMainFileViewer.mCurrObsFilter.getContrastingColor();
										}
									}
								}

								x = bh.mDValues[xPos];
								y = bh.mDValues[yPos];
								if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
									x = (x - mWinXOrigin) * mWinXScale;
									y = (y - mWinYOrigin) * mWinYScale;

									// adjust for axes labels
									if (mPlotAxes && !reverseY) {
										x += PVPLEFT;
										y += PVPTOP;
									}
									else if (mPlotAxes && reverseY) {
										y = mHeightCurrWindow - y;
										x += PVPLEFT;
										y -= PVPBOTTOM;
									}
									else if (reverseY) {
										y = mHeightCurrWindow - y;
									}

									// get a color for this value
									Color currColor = stnColor;

									if (mOverRideColor) {
										currColor = mContrastColor;
										mOverRideColor = false;
									}

									// plot point
									g.setColor(currColor);
									JOAFormulas.plotSymbol(g, currSymbol, (int)x, (int)y, currSymbolSize);
								}
							}
						} // for
					}
				}
				else {
					// plot all stations
					for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
						OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

						for (int sec = 0; sec < of.mNumSections; sec++) {
							Section sech = (Section)of.mSections.elementAt(sec);

							yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
							if (sech.mNumCasts == 0 || yPos == -1) {
								continue;
							}
							for (int stc = 0; stc < sech.mStations.size(); stc++) {
								Station sh = (Station)sech.mStations.elementAt(stc);
								if (!sh.mUseStn || (mAccumulateStns && sh.isSkipStn())) {
									continue;
								}
								Color stnColor = mStnColors.get(sh.mStnNum);

								if (yPos >= 0) {
									for (int b = 0; b < sh.mNumBottles; b++) {
										Bottle bh = (Bottle)sh.mBottles.elementAt(b);
										boolean keepBottle;
										if (mMainFileViewer.mObsFilterActive) {
											keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
										}
										else {
											keepBottle = true;
										}

										currSymbol = mSymbol;
										currSymbolSize = mSymbolSize;
										xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
										if (xPos == -1) {
											continue;
										}

										if (keepBottle) {
											if (mMainFileViewer.mObsFilterActive && !keepBottle) {
												if (mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
													continue;
												}
											}
											else if (mMainFileViewer.mObsFilterActive && keepBottle) {
												if (mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
													currSymbolSize = mSymbolSize;
													if (mMainFileViewer.mCurrObsFilter.isUseContrastingColor()) {
														mOverRideColor = true;
														mContrastColor = mMainFileViewer.mCurrObsFilter.getContrastingColor();
													}
												}
												else if (mMainFileViewer.mCurrObsFilter.isEnlargeSymbol()) {
													currSymbolSize = currSymbolSize * 2;
													if (mMainFileViewer.mCurrObsFilter.isUseContrastingColor()) {
														mOverRideColor = true;
														mContrastColor = mMainFileViewer.mCurrObsFilter.getContrastingColor();
													}
												}
												else if (!mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
													mOverRideColor = false;
													currSymbol = mMainFileViewer.mCurrObsFilter.getSymbol();
													currSymbolSize = mMainFileViewer.mCurrObsFilter.getSymbolSize();
													if (mMainFileViewer.mCurrObsFilter.isUseContrastingColor()) {
														mOverRideColor = true;
														mContrastColor = mMainFileViewer.mCurrObsFilter.getContrastingColor();
													}
												}
											}

											x = bh.mDValues[xPos];
											y = bh.mDValues[yPos];
											if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
												x = (x - mWinXOrigin) * mWinXScale;
												y = (y - mWinYOrigin) * mWinYScale;

												// adjust for axes labels
												if (mPlotAxes && !reverseY) {
													x += PVPLEFT;
													y += PVPTOP;
												}
												else if (mPlotAxes && reverseY) {
													y = mHeightCurrWindow - y;
													x += PVPLEFT;
													y -= PVPBOTTOM;
												}
												else if (reverseY) {
													y = mHeightCurrWindow - y;
												}

												// plot point
												g.setColor(stnColor);
												JOAFormulas.plotSymbol(g, currSymbol, (int)x, (int)y, currSymbolSize);
											}
										}
									} // for
								}
							}
						}
					}
				}
			}
			else if (!mMainFileViewer.mObsFilterActive) {
				// hilite any enhanced points if needed to do after other points have
				// been
				if (mPlotOnlyCurrStn) {
					OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.currElement();
					Section sech = (Section)of.mSections.currElement();
					yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
					boolean canPlot = true;
					if (sech.mNumCasts == 0 || yPos == -1) {
						canPlot = false;
					}
					Station sh = (Station)sech.mStations.currElement();
					if (!sh.mUseStn) {
						canPlot = false;
					}

					if (canPlot) {
						Color stnColor = mStnColors.get(sh.mStnNum);
						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle)sh.mBottles.elementAt(b);
							boolean keepBottle;
							if (mMainFileViewer.mObsFilterActive) {
								keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
							}
							else {
								keepBottle = true;
							}

							if (mMainFileViewer.mObsFilterActive && !keepBottle) {
								if (mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
									continue;
								}
							}

							currSymbol = mSymbol;
							currSymbolSize = mSymbolSize;
							xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
							if (xPos == -1) {
								continue;
							}
							x = bh.mDValues[xPos];
							y = bh.mDValues[yPos];
							if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
								x = (x - mWinXOrigin) * mWinXScale;
								y = (y - mWinYOrigin) * mWinYScale;

								// adjust for axes labels
								if (mPlotAxes && !reverseY) {
									x += PVPLEFT;
									y += PVPTOP;
								}
								else if (mPlotAxes && reverseY) {
									y = mHeightCurrWindow - y;
									x += PVPLEFT;
									y -= PVPBOTTOM;
								}
								else if (reverseY) {
									y = mHeightCurrWindow - y;
								}

								// plot point
								g.setColor(stnColor);
								JOAFormulas.plotSymbol(g, currSymbol, (int)x, (int)y, currSymbolSize);
							}
						}
					}
				}
				else {
					// plot all the stations
					for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
						OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

						for (int sec = 0; sec < of.mNumSections; sec++) {
							Section sech = (Section)of.mSections.elementAt(sec);

							xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
							yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
							if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
								continue;
							}
							for (int stc = 0; stc < sech.mStations.size(); stc++) {
								Station sh = (Station)sech.mStations.elementAt(stc);
								if (!sh.mUseStn || (mAccumulateStns && sh.isSkipStn())) {
									continue;
								}
								Color stnColor = mStnColors.get(sh.mStnNum);

								if (xPos >= 0 && yPos >= 0) {
									for (int b = 0; b < sh.mNumBottles; b++) {
										Bottle bh = (Bottle)sh.mBottles.elementAt(b);
										boolean keepBottle;
										if (mMainFileViewer.mObsFilterActive) {
											keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
										}
										else {
											keepBottle = true;
										}

										if (mMainFileViewer.mObsFilterActive && !keepBottle) {
											if (mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
												continue;
											}
										}

										currSymbol = mSymbol;
										currSymbolSize = mSymbolSize;
										xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
										if (xPos == -1) {
											continue;
										}

										x = bh.mDValues[xPos];
										y = bh.mDValues[yPos];
										if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
											x = (x - mWinXOrigin) * mWinXScale;
											y = (y - mWinYOrigin) * mWinYScale;

											// adjust for axes labels
											if (mPlotAxes && !reverseY) {
												x += PVPLEFT;
												y += PVPTOP;
											}
											else if (mPlotAxes && reverseY) {
												y = mHeightCurrWindow - y;
												x += PVPLEFT;
												y -= PVPBOTTOM;
											}
											else if (reverseY) {
												y = mHeightCurrWindow - y;
											}

											Color currColor = stnColor;
										}

									}
								}
							}
						}
					}
				}
			}

			// initialize the spot
			OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.currElement();
			Section sech = (Section)of.mSections.currElement();
			Station sh = (Station)sech.mStations.currElement();
			Bottle bh = (Bottle)sh.mBottles.currElement();
			yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
			xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
			x = JOAConstants.MISSINGVALUE;
			y = JOAConstants.MISSINGVALUE;
			if (xPos > -1 && yPos > -1) {
				x = bh.mDValues[xPos];
				y = bh.mDValues[yPos];
			}

			if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
				x = (int)((x - mWinXOrigin) * mWinXScale);
				y = (int)((y - mWinYOrigin) * mWinYScale);

				// adjust for axes labels
				if (mPlotAxes && !reverseY) {
					x += PVPLEFT;
					y += PVPTOP;
				}
				else if (mPlotAxes && reverseY) {
					y = mHeightCurrWindow - y;
					x += PVPLEFT;
					y -= PVPBOTTOM;
				}
				else if (reverseY) {
					y = mHeightCurrWindow - y;
				}
			}
			mObsMarker = new ObsMarker((int)x, (int)y, JOAConstants.DEFAULT_CURSOR_SIZE);
			g.setClip(0, 0, this.getSize().width, this.getSize().height);
		}

		private void plotOneStn(FileViewer fv, Graphics2D g, boolean reverseY) {
			OpenDataFile of = (OpenDataFile)fv.mOpenFiles.currElement();
			Section sech = (Section)of.mSections.currElement();
			int yPos = sech.getVarPos(fv.mAllProperties[mYVarCode].getVarLabel(), false);
			boolean canPlot = true;
			if (sech.mNumCasts == 0 || yPos == -1) {
				canPlot = false;
			}
			Station sh = (Station)sech.mStations.currElement();
			if (!sh.mUseStn) {
				canPlot = false;
			}
			Color stnColor = mStnColors.get(sh.mStnNum);

			if (canPlot) {
				for (int b = 0; b < sh.mNumBottles; b++) {
					Bottle bh = (Bottle)sh.mBottles.elementAt(b);
					boolean keepBottle;
					if (fv.mObsFilterActive) {
						keepBottle = fv.mCurrObsFilter.testObservation(fv, sech, bh);
					}
					else {
						keepBottle = true;
					}

					if (fv.mObsFilterActive && !keepBottle) {
						if (fv.mCurrObsFilter.isShowOnlyMatching()) {
							continue;
						}
					}

					int currSymbol = mSymbol;
					int currSymbolSize = mSymbolSize;
					int xPos = sech.getVarPos(fv.mAllProperties[mXVarCode].getVarLabel(), false);
					if (xPos == -1) {
						continue;
					}
					double x = bh.mDValues[xPos];
					double y = bh.mDValues[yPos];

					if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
						x = (x - mWinXOrigin) * mWinXScale;
						y = (y - mWinYOrigin) * mWinYScale;

						// adjust for axes labels
						if (mPlotAxes && !reverseY) {
							x += PVPLEFT;
							y += PVPTOP;
						}
						else if (mPlotAxes && reverseY) {
							y = mHeightCurrWindow - y;
							x += PVPLEFT;
							y -= PVPBOTTOM;
						}
						else if (reverseY) {
							y = mHeightCurrWindow - y;
						}
						g.setColor(stnColor);
						// plot point
						JOAFormulas.plotSymbol(g, currSymbol, x, y, currSymbolSize);

					}

					plotLine(g, sech, sh, fv, stnColor);
				}
			}
		}

		public void plotStnLine(Graphics2D g, boolean flag, FileViewer fv) {
			OpenDataFile of = (OpenDataFile)fv.mOpenFiles.currElement();
			Section sech = (Section)of.mSections.currElement();
			Station sh = (Station)sech.mStations.currElement();
			
			int x1, width, y1, height;
			if (mPlotAxes) {
				x1 = PVPLEFT;
				width = this.getSize().width - PVPLEFT - PVPRIGHT;
				y1 = PVPTOP;
				height = this.getSize().height - PVPTOP - PVPBOTTOM;
			}
			else {
				x1 = 0;
				y1 = 0;
				width = this.getSize().width;
				height = this.getSize().height;
			}
			g.setClip(x1, y1, width, height);

			// plot the new line
			plotLine(g, sech, sh, fv, mStnColors.get(sh));
			g.setClip(0, 0, this.getSize().width, this.getSize().height);
		}

		public void plotRegressionLines(Graphics2D g) {
			boolean reverseY = mMainFileViewer.mAllProperties[mYVarCode].isReverseY();
			
			if (JOAConstants.THICKEN_CONTOUR_LINES) {
				g.setStroke(lw4);
			}
			else {
				g.setStroke(lw2);
			}

			int x, width, y, height;
			if (mPlotAxes) {
				x = PVPLEFT;
				width = this.getSize().width - PVPLEFT - PVPRIGHT;
				y = PVPTOP;
				height = this.getSize().height - PVPTOP - PVPBOTTOM;
			}
			else {
				x = 0;
				y = 0;
				width = this.getSize().width;
				height = this.getSize().height;
			}
			g.setClip(x, y, width, height);

			g.setColor(Color.black);
			// compute points on the boundary
			double x0 = mWinXPlotMin;
			double x1 = mWinXPlotMax;
			double y0 = mSlopes * x0 + mIntercepts;
			double y1 = mSlopes * x1 + mIntercepts;

			x0 = (x0 - mWinXOrigin) * mWinXScale;
			y0 = (y0 - mWinYOrigin) * mWinYScale;

			// adjust for axes labels
			if (mPlotAxes && !reverseY) {
				x0 += PVPLEFT;
				y0 += PVPTOP;
			}
			else if (mPlotAxes && reverseY) {
				y0 = mHeightCurrWindow - y0;
				x0 += PVPLEFT;
				y0 -= PVPBOTTOM;
			}
			else if (reverseY) {
				y0 = mHeightCurrWindow - y0;
			}

			x1 = (x1 - mWinXOrigin) * mWinXScale;
			y1 = (y1 - mWinYOrigin) * mWinYScale;

			// adjust for axes labels
			if (mPlotAxes && !reverseY) {
				x1 += PVPLEFT;
				y1 += PVPTOP;
			}
			else if (mPlotAxes && reverseY) {
				y1 = mHeightCurrWindow - y1;
				x1 += PVPLEFT;
				y1 -= PVPBOTTOM;
			}
			else if (reverseY) {
				y1 = mHeightCurrWindow - y1;
			}

			g.drawLine((int)x0, (int)y0, (int)x1, (int)y1);

			double ymin = mWinYPlotMin;
			double ymax = mWinYPlotMax;
			double xmin = mWinXPlotMin;
			double xmax = mWinXPlotMax;
			double offsetIntercept = mIntercepts + 0;
			double xAtYmin = (ymin - offsetIntercept) / mSlopes;
			double xAtYmax = (ymax - offsetIntercept) / mSlopes;
			double xmid = 0;
			double ymid = 0;
			double deltaY = 0;
			double deltaX = 0;
			double xTranslation = 0.0;

			if (mSlopes > 0) {
				xTranslation = -5.0;
				if (xAtYmin > xmin && xAtYmax < xmax) {
					xmid = (xAtYmin + xAtYmax) / 2.0;
					ymid = (ymin + ymax) / 2.0;

					UVCoordinate uvmax = transformToPlot(xAtYmax, ymax, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					UVCoordinate uvmin = transformToPlot(xAtYmin, ymin, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					deltaY = uvmin.getV() - uvmax.getV();
					deltaX = uvmax.getU() - uvmin.getU();
				}
				else if (xAtYmin > xmin && xAtYmax > xmax) {
					xmid = (xAtYmin + xmax) / 2.0;
					double yAtXMax = mSlopes * xmax + offsetIntercept;
					ymid = (ymin + yAtXMax) / 2.0;

					UVCoordinate uvmax = transformToPlot(xmax, yAtXMax, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					UVCoordinate uvmin = transformToPlot(xAtYmin, ymin, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);

					deltaY = uvmin.getV() - uvmax.getV();
					deltaX = uvmax.getU() - uvmin.getU();
				}
				else if (xAtYmin < xmin && xAtYmax < xmax) {
					xTranslation = 5.0;
					double yAtXMin = mSlopes * xmin + offsetIntercept;
					xmid = (xmin + xAtYmax) / 2.0;
					ymid = (yAtXMin + ymax) / 2.0;

					UVCoordinate uvmax = transformToPlot(xAtYmax, ymax, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					UVCoordinate uvmin = transformToPlot(xmin, yAtXMin, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);

					deltaY = uvmin.getV() - uvmax.getV();
					deltaX = uvmax.getU() - uvmin.getU();
				}
				else if (xAtYmin < xmin && xAtYmax > xmax) {
					double yAtXMax = mSlopes * xmax + offsetIntercept;
					double yAtXMin = mSlopes * xmin + offsetIntercept;
					xmid = (xmin + xmax) / 2.0;
					ymid = (yAtXMin + yAtXMax) / 2.0;

					UVCoordinate uvmax = transformToPlot(xmax, yAtXMax, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					UVCoordinate uvmin = transformToPlot(xmin, yAtXMin, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);

					deltaY = uvmin.getV() - uvmax.getV();
					deltaX = uvmax.getU() - uvmin.getU();
				}
			}
			else if (mSlopes < 0.0) {
				if (xAtYmax > xmin && xAtYmin > xmax) {
					xmid = (xmax + xAtYmax) / 2.0;
					double yAtXMax = mSlopes * xmax + offsetIntercept;
					ymid = (yAtXMax + ymax) / 2.0;

					UVCoordinate uvmin = transformToPlot(xAtYmax, ymax, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					UVCoordinate uvmax = transformToPlot(xmax, yAtXMax, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					deltaY = uvmin.getV() - uvmax.getV();
					deltaX = uvmax.getU() - uvmin.getU();
				}
				else if (xAtYmax > xmin && xAtYmin < xmax) {
					xmid = (xAtYmin + xAtYmax) / 2.0;
					ymid = (ymin + ymax) / 2.0;

					UVCoordinate uvmin = transformToPlot(xAtYmax, ymax, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					UVCoordinate uvmax = transformToPlot(xAtYmin, ymin, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					deltaY = uvmin.getV() - uvmax.getV();
					deltaX = uvmax.getU() - uvmin.getU();
				}
				else if (xAtYmax < xmin && xAtYmin < xmax) {
					xmid = (xmin + xAtYmin) / 2.0;
					double yAtXMin = mSlopes * xmin + offsetIntercept;
					ymid = (ymin + yAtXMin) / 2.0;

					UVCoordinate uvmin = transformToPlot(xmin, yAtXMin, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					UVCoordinate uvmax = transformToPlot(xAtYmin, ymin, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					deltaY = uvmin.getV() - uvmax.getV();
					deltaX = uvmax.getU() - uvmin.getU();
				}
				else if (xAtYmax < xmin && xAtYmin > xmax) {
					xmid = (xmin + xmax) / 2.0;
					double yAtXMin = mSlopes * xmin + offsetIntercept;
					double yAtXMax = mSlopes * xmax + offsetIntercept;
					ymid = (yAtXMin + yAtXMax) / 2.0;

					UVCoordinate uvmin = transformToPlot(xmin, yAtXMin, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					UVCoordinate uvmax = transformToPlot(xmax, yAtXMax, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale,
					    reverseY);
					deltaY = uvmin.getV() - uvmax.getV();
					deltaX = uvmax.getU() - uvmin.getU();
				}
			}
			else {
				// slope is zero
				deltaY = 0.0;
				deltaX = 100.0;
			}
			double slope = deltaY / deltaX;
			double angle = Math.atan(slope);
			angle = angle * (180.0 / Math.PI);

			String slopeStr = JOAFormulas.formatDouble(mSlopes, 3, false);
			String interceptStr = JOAFormulas.formatDouble(mIntercepts, 2, false);
			String r2String = JOAFormulas.formatDouble(mRSquares, 2, false);
			String valStr = "y=" + slopeStr + "x+" + interceptStr + " r^2=" + r2String;

			UVCoordinate uv = transformToPlot(xmid, ymid, mWinXOrigin, mWinXScale, mWinYOrigin, mWinYScale, reverseY);
			g.translate(xTranslation, -4.0);
			JOAFormulas.drawStyledString(valStr, uv.getU(), uv.getV(), g, angle, JOAConstants.DEFAULT_REGRESSION_FONT,
			    JOAConstants.DEFAULT_REGRESSION_FONT_SIZE, JOAConstants.DEFAULT_REGRESSION_FONT_STYLE,
			    JOAConstants.DEFAULT_REGRESSION_FONT_COLOR);

			g.translate(-xTranslation, 4.0);
			g.setClip(0, 0, 3000, 3000);
		}

		public UVCoordinate transformToPlot(double x, double y, double xOrigin, double xScale, double yOrigin,
		    double yScale, boolean reverseY) {
			double xx = (x - xOrigin) * xScale;
			double yy = (y - yOrigin) * yScale;

			// adjust for axes labels
			if (mPlotAxes && !reverseY) {
				xx += PVPLEFT;
				yy += PVPTOP;
			}
			else if (mPlotAxes && reverseY) {
				yy = mHeightCurrWindow - yy;
				xx += PVPLEFT;
				yy -= PVPBOTTOM;
			}
			else if (reverseY) {
				yy = mHeightCurrWindow - y;
			}

			return new UVCoordinate(xx, yy);
		}

		public void plotLine(Graphics2D g, Section sech, Station sh, FileViewer fv, Color c) {
			double x0, y0, x, y;
			boolean reverseY = fv.mAllProperties[mYVarCode].isReverseY();

			g.setStroke(mConnectLW);
			stnLine.reset();
			stnLineStarted = false;

			int yPos = sech.getVarPos(fv.mAllProperties[mYVarCode].getVarLabel(), false);
			if (yPos == -1) {
				yPos = mYVarCode;
			}

			for (int b = 0; b < sh.mNumBottles - 1; b++) {
				Bottle bh = (Bottle)sh.mBottles.elementAt(b);
				int xPos = sech.getVarPos(fv.mAllProperties[mXVarCode].getVarLabel(), false);
				if (xPos == -1) {
					xPos = mXVarCode;
				}
				x0 = bh.mDValues[xPos];
				y0 = bh.mDValues[yPos];
				Bottle bh2 = (Bottle)sh.mBottles.elementAt(b + 1);
				x = bh2.mDValues[xPos];
				y = bh2.mDValues[yPos];

				boolean keepBottle1 = true;
				if (fv.mObsFilterActive && !fv.mCurrObsFilter.isInHiliteMode()) {
					keepBottle1 = fv.mCurrObsFilter.testObservation(fv, sech, bh);
				}
				boolean keepBottle2 = true;
				if (fv.mObsFilterActive && !fv.mCurrObsFilter.isInHiliteMode()) {
					keepBottle2 = fv.mCurrObsFilter.testObservation(fv, sech, bh2);
				}

				if (!keepBottle1 || !keepBottle2) {
					continue;
				}

				boolean firstPtMissingSomething = x0 == JOAConstants.MISSINGVALUE || y0 == JOAConstants.MISSINGVALUE;
				boolean secondPtMissingSomething = x == JOAConstants.MISSINGVALUE || y == JOAConstants.MISSINGVALUE;
				boolean missingAnything = firstPtMissingSomething || secondPtMissingSomething;

				if (!missingAnything) {
					x = (x - mWinXOrigin) * mWinXScale;
					y = (y - mWinYOrigin) * mWinYScale;
					x0 = (x0 - mWinXOrigin) * mWinXScale;
					y0 = (y0 - mWinYOrigin) * mWinYScale;

					// adjust for axes labels
					if (mPlotAxes && !reverseY) {
						x += PVPLEFT;
						y += PVPTOP;
						x0 += PVPLEFT;
						y0 += PVPTOP;
					}
					else if (mPlotAxes && reverseY) {
						y = mHeightCurrWindow - y;
						x += PVPLEFT;
						y -= PVPBOTTOM;
						y0 = mHeightCurrWindow - y0;
						x0 += PVPLEFT;
						y0 -= PVPBOTTOM;
					}
					else if (reverseY) {
						y = mHeightCurrWindow - y;
						y0 = mHeightCurrWindow - y0;
					}

					stnLine.moveTo((float)x0, (float)y0);
					stnLineStarted = true;
					stnLine.lineTo((float)x, (float)y);
				}
				else if (mPlotSpec.isIgnoreMissingObs()) {
					// missing something
					if (!secondPtMissingSomething) {
						// connect from last point plotted to current point
						x = (x - mWinXOrigin) * mWinXScale;
						y = (y - mWinYOrigin) * mWinYScale;
						// adjust for axes labels
						if (mPlotAxes && !reverseY) {
							x += PVPLEFT;
							y += PVPTOP;
						}
						else if (mPlotAxes && reverseY) {
							y = mHeightCurrWindow - y;
							x += PVPLEFT;
							y -= PVPBOTTOM;
						}
						else if (reverseY) {
							y = mHeightCurrWindow - y;
						}
						if (!stnLineStarted) {
							stnLine.moveTo((float)x, (float)y);
							stnLineStarted = true;
						}
						stnLine.lineTo((float)x, (float)y);
					}
				}

			}
			g.setColor(c);
			g.draw(stnLine);
			g.setStroke(lw1);
		}

		@SuppressWarnings("deprecation")
		public void drawYAxis(Graphics2D g) {
			g.setColor(Color.black);
			int bottom = (int)mHeightCurrWindow - 1 * PVPBOTTOM;
			int top = PVPTOP;
			int left = PVPLEFT;
			int right = (int)mWidthCurrWindow - PVPRIGHT;
			int leftMTicPos = PVPLEFT - 5 - 2;

			double yDiff = (mWinYPlotMax - mWinYPlotMin);
			int majorYTicks = (int)(yDiff / mYInc);
			double yInc = (double)(bottom - top) / (yDiff / mYInc);
			double minorYInc = yInc / ((double)mYTics + 1);

			// draw the Y axis
			g.drawLine(left, top, left, bottom);

			// draw the Y tic marks
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

				// plot the minor ticks
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

				// plot the grid
				if (mYGrid && i < majorYTicks) {
					g.setColor(mFG);
					g.drawLine(left, v, right, v);
				}
			}

			// complete the box
			g.setColor(Color.black);
			g.drawLine(right, top, right, bottom);

			// set the Y precision
			int numPlaces = JOAFormulas.GetDisplayPrecision(mYInc);

			// label the axes
			Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
			    JOAConstants.DEFAULT_AXIS_VALUE_SIZE);
			FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
			double vOrigin;
			double myVal;
			if (mMainFileViewer.mAllProperties[mYVarCode].isReverseY()) {
				vOrigin = mWinYPlotMax;
			}
			else {
				vOrigin = mWinYPlotMin;
			}

			int maxStrLen = 0;
			for (int i = 0; i <= majorYTicks; i++) {
				if (mMainFileViewer.mAllProperties[mYVarCode].isReverseY()) {
					myVal = vOrigin - (i * mYInc);
				}
				else {
					myVal = vOrigin + (i * mYInc);
				}
				if (myVal == -0.0) {
					myVal = 0.0;
				}
				int v = (int)(bottom - (i * yInc));
				String sTemp = JOAFormulas.roundNDecimals(myVal, numPlaces);
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
			if (mMainFileViewer.mAllProperties[mYVarCode].getUnits() != null
			    && mMainFileViewer.mAllProperties[mYVarCode].getUnits().length() > 0) {
				axisLabel = mMainFileViewer.mAllProperties[mYVarCode].getVarLabel() + " ("
				    + mMainFileViewer.mAllProperties[mYVarCode].getUnits() + ")";
				width = fm.stringWidth(axisLabel);
			}
			else {
				axisLabel = mMainFileViewer.mAllProperties[mYVarCode].getVarLabel();
				width = fm.stringWidth(axisLabel);
			}

			// label has to fit in 0 -> leftMTicPos - maxStrLen
			int hcenter = (leftMTicPos - maxStrLen) / 2;
			int hpos = hcenter + fm.getHeight() / 2;
			int height = this.getSize().height;
			JOAFormulas.drawStyledString(axisLabel, hpos, PVPTOP + ((height - PVPTOP - PVPBOTTOM) / 2) + width / 2, g, 90,
			    JOAConstants.DEFAULT_AXIS_LABEL_FONT, JOAConstants.DEFAULT_AXIS_LABEL_SIZE,
			    JOAConstants.DEFAULT_AXIS_LABEL_STYLE, JOAConstants.DEFAULT_AXIS_LABEL_COLOR);
		}

		@SuppressWarnings("deprecation")
		public void drawXAxis(Graphics2D g) {
			g.setColor(Color.black);
			int bottom = (int)mHeightCurrWindow - PVPBOTTOM;
			int top = PVPTOP;
			int left = PVPLEFT;
			int right = (int)mWidthCurrWindow - PVPRIGHT;

			double xDiff = (mWinXPlotMax - mWinXPlotMin);
			int majorXTicks = (int)(xDiff / mXInc);
			double xInc = (double)(right - left) / (xDiff / mXInc);
			double minorXInc = xInc / ((double)mXTics + 1);

			// draw the X axis
			g.drawLine(left, bottom, right, bottom);

			// draw the X tic marks
			for (int ii = 0; ii <= majorXTicks; ii++) {
				g.setColor(Color.black);
				int h = (int)(left + (ii * xInc));
				if (mBG == Color.black) {
					g.setColor(Color.black);
					g.drawLine(h, bottom + 2, h, bottom);
					g.setColor(Color.white);
					g.drawLine(h, bottom, h, bottom - 2);
				}
				else {
					g.drawLine(h, bottom + 2, h, bottom - 2);
				}

				// plot the minor ticks
				if (ii < majorXTicks) {
					for (int hh = 0; hh < mXTics + 1; hh++) {
						int newH = (int)(h + (hh * minorXInc));
						if (mBG == Color.black) {
							g.setColor(Color.black);
							g.drawLine(newH, bottom + 1, newH, bottom);
							g.setColor(Color.white);
							g.drawLine(newH, bottom + 1, newH, bottom - 1);
						}
						else {
							g.setColor(Color.black);
							g.drawLine(newH, bottom, newH, bottom - 1);
						}
					}
				}

				// plot the grid
				if (mXGrid) {
					g.setColor(mFG);
					g.drawLine(h, bottom, h, top);
				}
			}

			// complete the box
			g.setColor(Color.black);
			g.drawLine(left, top, right, top);

			// set the X precision
			int numPlaces = JOAFormulas.GetDisplayPrecision(mXInc);

			// label the x axis values
			// get the font metrics
			Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
			    JOAConstants.DEFAULT_AXIS_VALUE_SIZE);
			g.setFont(font);
			FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);

			double vOrigin = mWinXPlotMin;
			String sTemp = null;
			int maxv = fm.getHeight();
			int voffset = bottom + maxv;

			for (int ii = 0; ii <= majorXTicks; ii++) {
				double myVal = vOrigin + (ii * mXInc);
				if (myVal == -0.0) {
					myVal = 0.0;
				}
				int h = (int)((left) + (ii * xInc));

				sTemp = JOAFormulas.roundNDecimals(myVal, numPlaces);
				int len = fm.stringWidth(sTemp);
				JOAFormulas.drawStyledString(sTemp, h - (len / 2), voffset, g, 0.0, JOAConstants.DEFAULT_AXIS_VALUE_FONT,
				    JOAConstants.DEFAULT_AXIS_VALUE_SIZE, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
				    JOAConstants.DEFAULT_AXIS_VALUE_COLOR);
			}

			// add variable label
			int centerV = voffset + (((bottom + PVPBOTTOM) - voffset) / 2);

			font = new Font(JOAConstants.DEFAULT_AXIS_LABEL_FONT, JOAConstants.DEFAULT_AXIS_LABEL_STYLE,
			    JOAConstants.DEFAULT_AXIS_LABEL_SIZE);
			g.setFont(font);
			fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
			Color textColor = JOAConstants.DEFAULT_AXIS_LABEL_COLOR;
			int len = 0;
			String axisLabel = null;
			if (mMainFileViewer.mAllProperties[mXVarCode].getUnits() != null
			    && mMainFileViewer.mAllProperties[mXVarCode].getUnits().length() > 0) {
				axisLabel = mMainFileViewer.mAllProperties[mXVarCode].getVarLabel() + " ("
				    + mMainFileViewer.mAllProperties[mXVarCode].getUnits() + ")";
				len = fm.stringWidth(axisLabel);
			}
			else {
				axisLabel = mMainFileViewer.mAllProperties[mXVarCode].getVarLabel();
				len = fm.stringWidth(axisLabel);
			}
			int maxLblHeight = fm.getHeight() - fm.getLeading() - 4;

			int hh = mWidthCurrWindow / 2 - (len / 2) + PVPRIGHT;
			int vv = centerV + maxLblHeight / 2;
			JOAFormulas.drawStyledString(axisLabel, hh, vv, g, 0, JOAConstants.DEFAULT_AXIS_LABEL_FONT,
			    JOAConstants.DEFAULT_AXIS_LABEL_SIZE, JOAConstants.DEFAULT_AXIS_LABEL_STYLE, textColor);
			g.setColor(Color.black);
		}

		public void obsChanged(ObsChangedEvent evt) {
			if (mWindowIsLocked) { return; }
			// display the current station
			Station sh = evt.getFoundStation();
			Section sech = evt.getFoundSection();
			setRecord(sech, sh);
		}

		public void setRecord(Section inSec, Station inStn) {
			boolean reverseY = mMainFileViewer.mAllProperties[mYVarCode].isReverseY();
			Bottle bh = (Bottle)inStn.mBottles.currElement();
			int yPos = inSec.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
			// Graphics2D g = (Graphics2D) getGraphics();
			double x, y;

			if (mAccumulateStns) {
				inStn.setSkipStn(false);
			}

			if (mPlotOnlyCurrStn || mAccumulateStns) {
				this.invalidate();
				Graphics2D g = (Graphics2D)getGraphics();
				this.paintComponent(g);
			}
			else {
					repaint();
			}

			// browsing tied to first axis
			int xPos = inSec.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);

			if (xPos == -1 || yPos == -1) {
				x = JOAConstants.MISSINGVALUE;
				y = JOAConstants.MISSINGVALUE;
			}
			else {
				x = bh.mDValues[xPos];
				y = bh.mDValues[yPos];
			}

			if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
				x = (int)((x - mWinXOrigin) * mWinXScale);
				y = (int)((y - mWinYOrigin) * mWinYScale);

				// adjust for axes labels
				if (mPlotAxes && !reverseY) {
					x += PVPLEFT;
					y += PVPTOP;
				}
				else if (mPlotAxes && reverseY) {
					y = mHeightCurrWindow - y;
					x += PVPLEFT;
					y -= PVPBOTTOM;
				}
				else if (reverseY) {
					y = mHeightCurrWindow - y;
				}
			}

			if (mObsMarker != null) {
				mObsMarker.setNewPos((int)x, (int)y);
			}
			paintImmediately(0, 0, 2000, 2000);
		}

		public void findByXY(int x, int y) {
			boolean found = false;
			OpenDataFile foundFile = null;
			Section foundSection = null;
			Station foundStation = null;
			Bottle foundBottle = null;
			OpenDataFile oldof = (OpenDataFile)mMainFileViewer.mOpenFiles.currElement();
			Section oldsech = (Section)oldof.mSections.currElement();
			Station oldsh = (Station)oldsech.mStations.currElement();
			Bottle oldBottle = (Bottle)oldsh.mBottles.currElement();
			boolean reverseY = mMainFileViewer.mAllProperties[mYVarCode].isReverseY();
			int minY = getMinY();
			int maxY = getMaxY();
			int minX = getMinX();
			int maxX = getMaxX();

			if (!mSpotable) { return; }

			// search for a matching observation
			double minOffset = 10000.0;
			for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles && !found; fc++) {
				OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections && !found; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}
					int yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
					if (yPos == -1) {
						continue;
					}

					for (int stc = 0; stc < sech.mStations.size() && !found; stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							continue;
						}

						for (int b = 0; b < sh.mNumBottles && !found; b++) {
							Bottle bh = (Bottle)sh.mBottles.elementAt(b);
							int xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
							if (xPos == -1) {
								continue;
							}

							double xx = bh.mDValues[xPos];
							double yy = bh.mDValues[yPos];
							if (xx != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
								xx = (xx - mWinXOrigin) * mWinXScale;
								yy = (yy - mWinYOrigin) * mWinYScale;

								// adjust for axes labels
								if (mPlotAxes && !reverseY) {
									xx += PVPLEFT;
									yy += PVPTOP;
								}
								else if (mPlotAxes && reverseY) {
									yy = mHeightCurrWindow - yy;
									xx += PVPLEFT;
									yy -= PVPBOTTOM;
								}
								else if (reverseY) {
									yy = mHeightCurrWindow - yy;
								}
							}

							if (yy < minY || yy > maxY || xx < minX || xx > maxX) {
								continue;
							}

							double off = Math.sqrt(((x - xx) * (x - xx)) + ((y - yy) * (y - yy)));

							// if observation filter is active we want to snap to only
							// visible points
							boolean keepBottle;
							if (mMainFileViewer.mObsFilterActive) {
								keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
							}
							else {
								keepBottle = true;
							}

							if (mMainFileViewer.mObsFilterActive && mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()
							    && !keepBottle) {
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
			if (found && foundSection != null && foundFile != null && foundStation != null && foundBottle != null) {
				if (found && foundBottle != oldBottle) {
					mMainFileViewer.mOpenFiles.setCurrElement(foundFile);
					foundFile.mSections.setCurrElement(foundSection);
					foundSection.mStations.setCurrElement(foundStation);
					foundStation.mBottles.setCurrElement(foundBottle);
					ObsChangedEvent oce = new ObsChangedEvent(mMainFileViewer);
					oce.setFoundObs(foundFile, foundSection, foundStation, foundBottle);
					// paintImmediately(new Rectangle(0, 0, 1000, 1000));
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
			if (mWindowIsLocked) { return false; }
			OpenDataFile foundFile = null;
			Section foundSection = null;
			Station foundStation = null;
			Bottle foundBottle = null;
			int pPos = mMainFileViewer.getPRESPropertyPos();
			boolean found = false;

			OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.currElement();
			Section sech = (Section)of.mSections.currElement();
			Station sh = (Station)sech.mStations.currElement();

			// find new observation
			found = false;
			switch (direction.intValue()) {
				case 1: // JOAConstants.NEXTSTN:
					// go to next station
					foundStation = (Station)sech.mStations.nextElement();
					if (foundStation == null) {
						// go to next section
						foundSection = (Section)of.mSections.nextElement();
						foundFile = of;
						if (foundSection != null) {
							foundSection.mStations.setCurrElementToFirst();
							foundStation = (Station)foundSection.mStations.currElement();
							foundBottle = JOAFormulas.findBottleByPres(mMainFileViewer, foundStation);
							found = true;
							if (foundBottle != null) {
								found = true;
							}
						}
						else {
							// look in next file
							foundFile = (OpenDataFile)mMainFileViewer.mOpenFiles.nextElement();

							if (foundFile != null) {
								foundSection = (Section)foundFile.mSections.currElement();
								foundStation = (Station)foundSection.mStations.currElement();
								foundBottle = JOAFormulas.findBottleByPres(mMainFileViewer, foundStation);
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
						foundBottle = JOAFormulas.findBottleByPres(mMainFileViewer, foundStation);
						found = true;
						if (foundBottle != null) {
							found = true;
						}
					}
					break;
				case 2: // JOAConstants.PREVSTN:

					// go to prev station
					foundStation = (Station)sech.mStations.prevElement();
					if (foundStation == null) {
						// go to next section
						foundSection = (Section)of.mSections.prevElement();
						foundFile = of;

						if (foundSection != null) {
							foundSection.mStations.setCurrElementToLast();
							foundStation = (Station)foundSection.mStations.currElement();
							foundBottle = JOAFormulas.findBottleByPres(mMainFileViewer, foundStation);
							found = true;
							if (foundBottle != null) {
								found = true;
							}
						}
						else {
							// look in next file
							foundFile = (OpenDataFile)mMainFileViewer.mOpenFiles.prevElement();

							if (foundFile != null) {
								foundSection = (Section)foundFile.mSections.currElement();
								foundStation = (Station)foundSection.mStations.currElement();
								foundBottle = JOAFormulas.findBottleByPres(mMainFileViewer, foundStation);
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
						foundBottle = JOAFormulas.findBottleByPres(mMainFileViewer, foundStation);
						found = true;
						if (foundBottle != null) {
							found = true;
						}
					}
					break;
				case 3: // JOAConstants.NEXTOBS:

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
				case 4: // JOAConstants.PREVOBS:

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
				mMainFileViewer.mOpenFiles.setCurrElement(foundFile);
				foundFile.mSections.setCurrElement(foundSection);
				foundSection.mStations.setCurrElement(foundStation);
				foundStation.mBottles.setCurrElement(foundBottle);
				ObsChangedEvent oce = new ObsChangedEvent(mMainFileViewer);
				oce.setFoundObs(foundFile, foundSection, foundStation, foundBottle);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(oce);
				return true;
			}
			else {
				Toolkit.getDefaultToolkit().beep();
				return false;
			}
		}

		// OK Button
		public void dialogDismissed(JDialog d) {
			LinePlotSpecification newPlotSpec = ((ConfigLinePlotDC)d).createPlotSpec();
			try {
				mPlotSpec.writeToLog("Edited existing plot: " + mParent.getTitle());
			}
			catch (Exception ex) {
			}
			mWidth = newPlotSpec.getWidth();
			mHeight = newPlotSpec.getHeight();
			mFG = newPlotSpec.getFGColor();
			mBG = newPlotSpec.getBGColor();
			mMainFileViewer = newPlotSpec.getFileViewer().elementAt(0);

			mXVarCode = newPlotSpec.getXVarCode();
			mXTics = newPlotSpec.getXTics();
			mWinXPlotMin = newPlotSpec.getWinXPlotMin();
			mWinXPlotMax = newPlotSpec.getWinXPlotMax();
			mXInc = newPlotSpec.getXInc();

			mYVarCode = newPlotSpec.getYVarCode();
			mWinTitle = newPlotSpec.getWinTitle();
			mIncludeObsPanel = newPlotSpec.isIncludeObsPanel();
			mXGrid = newPlotSpec.isXGrid();
			mYGrid = newPlotSpec.isYGrid();
			mSymbol = newPlotSpec.getSymbol();
			mSymbolSize = newPlotSpec.getSymbolSize();
			mYTics = newPlotSpec.getYTics();
			mPlotAxes = newPlotSpec.isPlotAxes();
			mPlotIsopycnals = newPlotSpec.isPlotIsopycnals();
			mRefPress = newPlotSpec.getRefPress();
			mPlotOnlyCurrStn = newPlotSpec.isPlotOnlyCurrStn();
			mAccumulateStns = newPlotSpec.isAccumulateStns();

			// axes ranges
			mWinYPlotMin = newPlotSpec.getWinYPlotMin();
			mWinYPlotMax = newPlotSpec.getWinYPlotMax();
			mYInc = newPlotSpec.getYInc();

			boolean addingPaint = false;
			boolean removingPaint = false;
			if (!mPlotSpec.isAccumulateStns() && mAccumulateStns) {
				// state of paint has changed
				// adding accumulation
				addingPaint = true;
				
				// set the skip flag for all stns
				for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
					OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

					for (int sec = 0; sec < of.mNumSections; sec++) {
						Section sech = (Section)of.mSections.elementAt(sec);

						for (int stc = 0; stc < sech.mStations.size(); stc++) {
							Station sh = (Station)sech.mStations.elementAt(stc);
							sh.setSkipStn(true);
						}
					}
				}
			}
			else if (mPlotSpec.isAccumulateStns() && !mAccumulateStns) {
				removingPaint = true;
				
				// set the skip flag for all stns
				for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
					OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

					for (int sec = 0; sec < of.mNumSections; sec++) {
						Section sech = (Section)of.mSections.elementAt(sec);

						for (int stc = 0; stc < sech.mStations.size(); stc++) {
							Station sh = (Station)sech.mStations.elementAt(stc);
							sh.setSkipStn(false);
						}
					}
				}
			}

			mPlotSpec = new LinePlotSpecification(newPlotSpec);
			mColorPalette = getColorPalette(mPlotSpec.getStnCycleColorPalette());
			mNumColors = mColorPalette.getNumNonBlackColors();
			mConnectLW = null;
			mConnectLW = new BasicStroke(mPlotSpec.getLineWidth());
			assignStationColors();

			// resize the window if necessary
			if (mCurrObsPanel != null && ((ConfigLinePlotDC)d).removingObsBrowser()) {
				// remove existing color bar component (if there is one)
				mContents.remove(mObsScroller);
				mObsScroller = null;
				mCurrObsPanel = null;
				mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height - 40);
			}
			else if (((ConfigLinePlotDC)d).addingObsBrowser()) {
				mCurrObsPanel = new CurrentObsPanel2(mMainFileViewer, mThis);
				mObsScroller = new JScrollPane(mCurrObsPanel);
				mObsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				mObsScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				mContents.add("South", mObsScroller);
				mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height + 40);
				mFrame.validate();
			}
			
			mStationLegend.cachePaintedDetails();
			mStationLegend.setStnColorHash(mStnColors);
			if (addingPaint || mPlotOnlyCurrStn) {
				mStationLegend.setAllUnpainted();
			}
			else if (removingPaint){
				mStationLegend.setAllPainted();
			}
			else {
				mStationLegend.restoreCachedPaintedDetails();
			}
			mStationLegend.updateCurrStn();
			mStationLegend.setPlotOnlyCurrStn(mPlotOnlyCurrStn);
			mStationLegend.invalidate();
			mStationLegend.validate();

			mOffScreen = null;
			paintComponent(this.getGraphics());
		}

		// Cancel button
		public void dialogCancelled(JDialog d) {
			mPlotSpec = ((ConfigLinePlotDC)d).getOrigPlotSpec();
			mWidth = mPlotSpec.getWidth();
			mHeight = mPlotSpec.getHeight();
			mFG = mPlotSpec.getFGColor();
			mBG = mPlotSpec.getBGColor();
			mMainFileViewer = mPlotSpec.getFileViewer().elementAt(0);

			mXVarCode = mPlotSpec.getXVarCode();
			mXTics = mPlotSpec.getXTics();
			mWinXPlotMin = mPlotSpec.getWinXPlotMin();
			mWinXPlotMax = mPlotSpec.getWinXPlotMax();
			mXInc = mPlotSpec.getXInc();

			mYVarCode = mPlotSpec.getYVarCode();
			mWinTitle = mPlotSpec.getWinTitle();
			mIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
			mXGrid = mPlotSpec.isXGrid();
			mYGrid = mPlotSpec.isYGrid();
			mSymbol = mPlotSpec.getSymbol();
			mSymbolSize = mPlotSpec.getSymbolSize();
			mYTics = mPlotSpec.getYTics();
			mPlotAxes = mPlotSpec.isPlotAxes();
			mPlotIsopycnals = mPlotSpec.isPlotIsopycnals();
			mRefPress = mPlotSpec.getRefPress();
			mPlotOnlyCurrStn = mPlotSpec.isPlotOnlyCurrStn();
			mAccumulateStns = mPlotSpec.isAccumulateStns();

			// axes ranges
			mWinYPlotMin = mPlotSpec.getWinYPlotMin();
			mWinYPlotMax = mPlotSpec.getWinYPlotMax();
			mYInc = mPlotSpec.getYInc();

			if (((ConfigLinePlotDC)d).mLayoutChanged) {
			}
			mColorPalette = getColorPalette(mPlotSpec.getStnCycleColorPalette());
			mNumColors = mColorPalette.getNumNonBlackColors();
			mConnectLW = null;
			mConnectLW = new BasicStroke(mPlotSpec.getLineWidth());
			assignStationColors();

			mOffScreen = null;
			paintComponent(this.getGraphics());
			mFrame.setSize(mFrame.getSize().width + 1, mFrame.getSize().height);
			mFrame.setSize(mFrame.getSize().width - 1, mFrame.getSize().height);
		}

		// something other than the OK button
		public void dialogDismissedTwo(JDialog d) {
			;
		}

		public void dialogApplyTwo(Object d) {
		}

		// Apply button, OK w/o dismissing the dialog
		public void dialogApply(JDialog d) {
			LinePlotSpecification newPlotSpec = ((ConfigLinePlotDC)d).createPlotSpec();
			try {
				mPlotSpec.writeToLog("Edited existing plot: " + mParent.getTitle());
			}
			catch (Exception ex) {
			}
			mWidth = newPlotSpec.getWidth();
			mHeight = newPlotSpec.getHeight();
			mFG = newPlotSpec.getFGColor();
			mBG = newPlotSpec.getBGColor();
			mMainFileViewer = newPlotSpec.getFileViewer().elementAt(0);

			mXVarCode = newPlotSpec.getXVarCode();
			mXTics = newPlotSpec.getXTics();
			mWinXPlotMin = newPlotSpec.getWinXPlotMin();
			mWinXPlotMax = newPlotSpec.getWinXPlotMax();
			mXInc = newPlotSpec.getXInc();

			mYVarCode = newPlotSpec.getYVarCode();
			mWinTitle = newPlotSpec.getWinTitle();
			mIncludeObsPanel = newPlotSpec.isIncludeObsPanel();
			mXGrid = newPlotSpec.isXGrid();
			mYGrid = newPlotSpec.isYGrid();
			mSymbol = newPlotSpec.getSymbol();
			mSymbolSize = newPlotSpec.getSymbolSize();
			mYTics = newPlotSpec.getYTics();
			mPlotAxes = newPlotSpec.isPlotAxes();
			mPlotIsopycnals = newPlotSpec.isPlotIsopycnals();
			mRefPress = newPlotSpec.getRefPress();
			mPlotOnlyCurrStn = newPlotSpec.isPlotOnlyCurrStn();
			mAccumulateStns = newPlotSpec.isAccumulateStns();

			// axes ranges
			mWinYPlotMin = newPlotSpec.getWinYPlotMin();
			mWinYPlotMax = newPlotSpec.getWinYPlotMax();
			mYInc = newPlotSpec.getYInc();

			boolean addingPaint = false;
			boolean removingPaint = false;
			if (!mPlotSpec.isAccumulateStns() && mAccumulateStns) {
				// state of paint has changed
				// adding accumulation
				addingPaint = true;
				
				// set the skip flag for all stns
				for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
					OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

					for (int sec = 0; sec < of.mNumSections; sec++) {
						Section sech = (Section)of.mSections.elementAt(sec);

						for (int stc = 0; stc < sech.mStations.size(); stc++) {
							Station sh = (Station)sech.mStations.elementAt(stc);
							sh.setSkipStn(true);
						}
					}
				}
			}
			else if (mPlotSpec.isAccumulateStns() && !mAccumulateStns) {
				removingPaint = true;
				
				// set the skip flag for all stns
				for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
					OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

					for (int sec = 0; sec < of.mNumSections; sec++) {
						Section sech = (Section)of.mSections.elementAt(sec);

						for (int stc = 0; stc < sech.mStations.size(); stc++) {
							Station sh = (Station)sech.mStations.elementAt(stc);
							sh.setSkipStn(false);
						}
					}
				}
			}

			mPlotSpec = new LinePlotSpecification(newPlotSpec);
			mColorPalette = getColorPalette(mPlotSpec.getStnCycleColorPalette());
			mNumColors = mColorPalette.getNumNonBlackColors();
			mConnectLW = null;
			mConnectLW = new BasicStroke(mPlotSpec.getLineWidth());
			assignStationColors();

			// resize the window if necessary
			if (mCurrObsPanel != null && ((ConfigLinePlotDC)d).removingObsBrowser()) {
				// remove existing color bar component (if there is one)
				mContents.remove(mObsScroller);
				mObsScroller = null;
				mCurrObsPanel = null;
				mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height - 40);
			}
			else if (((ConfigLinePlotDC)d).addingObsBrowser()) {
				mCurrObsPanel = new CurrentObsPanel2(mMainFileViewer, mThis);
				mObsScroller = new JScrollPane(mCurrObsPanel);
				mObsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				mObsScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				mContents.add("South", mObsScroller);
				mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height + 40);
				mFrame.validate();
			}
			
			mStationLegend.cachePaintedDetails();
			mStationLegend.setStnColorHash(mStnColors);
			if (addingPaint || mPlotOnlyCurrStn) {
				mStationLegend.setAllUnpainted();
			}
			else if (removingPaint){
				mStationLegend.setAllPainted();
			}
			else {
				mStationLegend.restoreCachedPaintedDetails();
			}
			mStationLegend.updateCurrStn();
			mStationLegend.setPlotOnlyCurrStn(mPlotOnlyCurrStn);
			mStationLegend.invalidate();
			mStationLegend.validate();

			mOffScreen = null;
			paintComponent(this.getGraphics());
		}
	}

	public RubberbandPanel getPanel() {
		return mLinePlot;
	}

	public boolean inWindow(float x, float y) {
		if (x > mWinXPlotMax || x < mWinXPlotMin) { return false; }

		if (y > mWinYPlotMax || y < mWinYPlotMin) { return false; }
		return true;
	}

	private void computeRegressions() {
		// compute linear regressions for all plotted parameters
		double sumx;
		double sumy;
		double sumx2;
		int n;
		double xbar;
		double ybar;
		double xxbar;
		double yybar;
		double xybar;

		sumx = 0.0;
		sumy = 0.0;
		sumx2 = 0.0;
		n = 0;
		xbar = Double.NaN;
		ybar = Double.NaN;
		xxbar = 0.0;
		yybar = 0.0;
		xybar = 0.0;

		// pass #1
		for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);

				int xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
				int yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
				if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
					continue;
				}
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					if (xPos >= 0 && yPos >= 0) {
						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle)sh.mBottles.elementAt(b);
							boolean keepBottle;
							if (mMainFileViewer.mObsFilterActive) {
								keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
							}
							else {
								keepBottle = true;
							}

							if (mMainFileViewer.mObsFilterActive && !keepBottle) {
								if (mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
									continue;
								}
							}

							xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
							if (xPos == -1) {
								continue;
							}

							float x = bh.mDValues[xPos];
							float y = bh.mDValues[yPos];
							if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
								if (mClipToWindow && !inWindow(x, y)) {
									continue;
								}
								sumx += x;
								sumx2 += x * x;
								sumy += y;
								n++;
							}
						}
					}
				}
			}
		}

		double[] tempX = null;
		double[] tempY = null;
		int ic = 0;

		if (n > 0) {
			xbar = sumx / n;
			ybar = sumy / n;
			tempX = new double[n];
			tempY = new double[n];
			ic = 0;
		}

		// pass #2
		for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mMainFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);

				int xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
				int yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
				if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
					continue;
				}
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					if (xPos >= 0 && yPos >= 0) {
						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle)sh.mBottles.elementAt(b);
							boolean keepBottle;
							if (mMainFileViewer.mObsFilterActive) {
								keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
							}
							else {
								keepBottle = true;
							}

							if (mMainFileViewer.mObsFilterActive && !keepBottle) {
								if (mMainFileViewer.mCurrObsFilter.isShowOnlyMatching()) {
									continue;
								}
							}

							xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode].getVarLabel(), false);
							if (xPos == -1) {
								continue;
							}

							float x = bh.mDValues[xPos];
							float y = bh.mDValues[yPos];
							if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
								if (mClipToWindow && !inWindow(x, y)) {
									continue;
								}
								tempX[ic] = x;
								tempY[ic] = y;
								ic++;
								xxbar += (x - xbar) * (x - xbar);
								yybar += (y - ybar) * (y - ybar);
								xybar += (x - xbar) * (y - ybar);
							}
						}
					}
				}
			}
		}

		// compute regression line coeffs
		if (n > 0) {
			mSlopes = xybar / xxbar;
			mIntercepts = ybar - mSlopes * xbar;
		}

		// analyze results
		int df = n - 2;
		double rss = 0.0; // residual sum of squares
		double ssr = 0.0; // regression sum of squares
		for (int j = 0; j < n; j++) {
			double fit = mSlopes * tempX[j] + mIntercepts;
			rss += (fit - tempY[j]) * (fit - tempY[j]);
			ssr += (fit - ybar) * (fit - ybar);
		}
		mRSquares = ssr / yybar;
		svar = rss / df;
		svar1 = svar / xxbar;
		svar0 = svar / n + xbar * xbar * svar1;
	}

	public ColorPalette getColorPalette(String newPalName) {
		// read the palette from disk
		try {
			ColorPalette newPal = JOAFormulas.readPalette(newPalName);
			return newPal;
		}
		catch (Exception ex) {
			System.out.println("An error occured reading a palette from a file");
		}

		return null;
	}

	public void obsChanged(ObsChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		// display the current station
		Station sh = evt.getFoundStation();
		Section sech = evt.getFoundSection();
		mMetaTB.setNewStn(sech, sh);
	}
}

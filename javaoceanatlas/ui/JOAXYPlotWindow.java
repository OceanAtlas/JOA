/*
 * $Id: JOAXYPlotWindow.java,v 1.39 2005/10/18 23:42:19 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import net.sourceforge.openforecast.ForecastingModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import com.ibm.xml.parser.TXDocument;
import com.visualtek.png.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class JOAXYPlotWindow extends JOAWindow implements ColorBarChangedListener, DataAddedListener, ActionListener,
    ObsFilterChangedListener, StnFilterChangedListener, PrefsChangedListener, WindowsMenuChangedListener,
    MetadataChangedListener, ConfigurableWindow {
	public static int PVPLEFT = 70;
	public static int PVPRIGHT = 20;
	public static int PVPTOP = 35;
	public static int PVPBOTTOM = 50;
	public static int SIGGRIDSIZE = 50;
	public static int TGRIDSIZE = 500;

	protected int mNumXAxes;
	protected double[] mWinXPlotMax = new double[10];
	protected double mWinYPlotMax;
	protected double[] mWinXPlotMin = new double[10];
	protected double mWinYPlotMin;
	protected boolean mLineDrawn;
	protected int[] mSymbol = new int[10];
	protected int[] mSymbolSize = new int[10];
	protected int mXVarCode[] = new int[10];
	protected int mYVarCode;
	protected boolean mXGrid, mYGrid;
	protected double mYInc;
	protected double[] mXInc = new double[10];
	protected int[] mXTics = new int[10];
	protected int mYTics;
	protected FileViewer mMainFileViewer;
	protected Vector<FileViewer> mFileViewers;
	protected double[] mWinXScale = new double[10];
	protected double[] mWinXOrigin = new double[10];
	protected double mWinYScale;
	protected double mWinYOrigin;
	protected ObsMarker[] mObsMarker = new ObsMarker[10];
	protected boolean mUseBottle = true;
	protected int mHeightCurrWindow;
	protected int mWidthCurrWindow;
	protected double mScaleHeight;
	protected double mScaleWidth;
	protected String mWinTitle = null;
	protected boolean mIncludeCBAR, mIncludeObsPanel;
	protected Color mFG, mBG;
	protected int mHeight, mWidth;
	protected XYPlotPanel mXYPlot = null;
	protected ColorBarPanel mColorBarLegend = null;
	protected JScrollPane mObsScroller = null;
	protected CurrentObsPanel2 mCurrObsPanel = null;
	protected boolean mConnectObs;
	protected Station oldStn = null;
	protected Section oldSec = null;
	protected boolean mPlotAxes;
	protected XYPlotSpecification mPlotSpec = null;
	protected Container mContents = null;
	protected JOAWindow mFrame = null;
	protected boolean mPlotIsopycnals = false;
	protected double mRefPress;
	protected boolean mPrinting = false;
	protected boolean mCanPlotIsoPycnals;
	protected boolean mPlotOnlyCurrStn;
	protected boolean mAccumulateStns;
	protected JFrame mParent;
	protected boolean mOverRideColor = false;
	protected Color mContrastColor = null;
	private JOAXYPlotWindow mThis = null;
	private ResourceBundle rb = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	private boolean mColorBarWasChanged = false;
	protected ValueToolbar mValToolBar;
	GeneralPath[] stnLine = new GeneralPath[10];
	boolean[] stnLineStarted = new boolean[10];
	SmallIconButton mEraseBtn = null;
	SmallIconButton mLinearRegressionBtn = null;
	double[] mSlopes = new double[10];
	double[] mIntercepts = new double[10];
	double[] mRSquares = new double[10];
	double[] svar = new double[10];
	double[] svar1 = new double[10];
	double[] svar0 = new double[10];
	private boolean mPlotRegress = false;
	BasicStroke lw2 = new BasicStroke(2);
	BasicStroke lw4 = new BasicStroke(4);
	private boolean mClipToWindow = false;

	public JOAXYPlotWindow(XYPlotSpecification ps, JFrame parent) {
		super(true, true, true, true, true, ps);
		mPlotSpec = ps;
		mParent = parent;
		mWidth = ps.getWidth();
		mHeight = ps.getHeight();
		mFG = ps.getFGColor();
		mBG = ps.getBGColor();
		mFileViewers = ps.getFileViewer();
		mMainFileViewer = ps.getFileViewer().elementAt(0);
		mNumXAxes = ps.getNumXAxes();
		for (int i = 0; i < ps.getNumXAxes(); i++) {
			mXVarCode[i] = ps.getXVarCode(i);
			mXTics[i] = ps.getXTics(i);
			mWinXPlotMin[i] = ps.getWinXPlotMin(i);
			mWinXPlotMax[i] = ps.getWinXPlotMax(i);
			mXInc[i] = ps.getXInc(i);
			mSymbol[i] = ps.getSymbol(i);
			mSymbolSize[i] = ps.getSymbolSize(i);
		}
		mYVarCode = ps.getYVarCode();
		mWinTitle = ps.getWinTitle();
		mIncludeCBAR = ps.isIncludeCBAR();
		mIncludeObsPanel = ps.isIncludeObsPanel();
		mConnectObs = ps.isConnectObs();
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
		init();
		mThis = this;
	}

	// public Dimension getPreferredSize() {
	// return new Dimension(mWidth, mHeight);
	// }

	public void init() {
		mFrame = this;
		mContents = this.getContentPane();
		mContents.setLayout(new BorderLayout(0, 0));

		if (mIncludeCBAR) {
			mColorBarLegend = new ColorBarPanel(mFrame, mMainFileViewer, mMainFileViewer.mDefaultCB, mFG,
			    JOAConstants.DEFAULT_FRAME_COLOR, false, false);
			mColorBarLegend.setEnhanceable(true);
			mContents.add("East", mColorBarLegend);
		}

		mXYPlot = new XYPlotPanel();
		mContents.add("Center", mXYPlot);
		if (mIncludeObsPanel) {
			mCurrObsPanel = new CurrentObsPanel2(mMainFileViewer, this);
			mObsScroller = new JScrollPane(mCurrObsPanel);
			mObsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			mObsScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			mContents.add("South", mObsScroller);
		}
		mXYPlot.requestFocus();

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

		mLinearRegressionBtn.setToolTipText("Toggle Regression Line (holding option/alt key restricts regression to current plot domain)");
		mLinearRegressionBtn.setActionCommand("toggleregress");
		mLinearRegressionBtn.addActionListener(this);
		mToolBar.add(mLinearRegressionBtn);

		// make an array of x labels
		String[] xlabels = new String[mNumXAxes];
		int[] xprecs = new int[mNumXAxes];
		for (int i = 0; i < mNumXAxes; i++) {
			xlabels[i] = new String(mMainFileViewer.mAllProperties[mPlotSpec.getXVarCode(i)].getVarLabel());
			xprecs[i] = 3;
		}

		// make an array of y labels
		String[] ylabels = new String[1];
		int[] yprecs = new int[1];
		ylabels[0] = new String(mMainFileViewer.mAllProperties[mPlotSpec.getYVarCode()].getVarLabel());
		yprecs[0] = 3;

		mValToolBar = new ValueToolbar(mXYPlot, xlabels, xprecs, ylabels, yprecs);
		mToolBar.add(mValToolBar);
		mContents.add(mToolBar, "North");
		double[] xvals = new double[mNumXAxes];
		for (int i = 0; i < mNumXAxes; i++) {
			xvals[i] = Double.NaN;
		}
		double[] yvals = new double[1];
		yvals[0] = Double.NaN;
		mValToolBar.setLocation(xvals, yvals);

		getRootPane().registerKeyboardAction(new RightListener((Object) mXYPlot, mXYPlot.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new LeftListener((Object) mXYPlot, mXYPlot.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new UpListener((Object) mXYPlot, mXYPlot.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new DownListener((Object) mXYPlot, mXYPlot.getClass()),
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
			this.setTitle(mMainFileViewer.mAllProperties[mXVarCode[0]].getVarLabel() + "-"
			    + mMainFileViewer.mAllProperties[mYVarCode].getVarLabel());
		}
		else {
			this.setTitle(mWinTitle);
		}
		mMainFileViewer.addColorBarChangedListener(this);
		mMainFileViewer.addDataAddedListener(this);
		mMainFileViewer.addObsFilterChangedListener(this);
		mMainFileViewer.addStnFilterChangedListener(this);
		PowerOceanAtlas.getInstance().addPrefsChangedListener(this);
		PowerOceanAtlas.getInstance().addWindowsMenuChangedListener(this);
		mMainFileViewer.addMetadataChangedListener(this);
		mMenuBar = new JOAMenuBar(this, true, mMainFileViewer);

		// offset the window down and right from 'parent' frame
		Rectangle r = mParent.getBounds();
		this.setLocation(r.x + 20, r.y + 20);

		for (int i = 0; i < 10; i++) {
			stnLine[i] = new GeneralPath();
			stnLineStarted[i] = false;
		}
	}

	public void closeMe() {
		mMainFileViewer.removeOpenWindow(mFrame);
		mMainFileViewer.removeColorBarChangedListener((ColorBarChangedListener) mFrame);
		mMainFileViewer.removeDataAddedListener((DataAddedListener) mFrame);
		mMainFileViewer.removeStnFilterChangedListener((StnFilterChangedListener) mFrame);
		mMainFileViewer.removeObsFilterChangedListener((ObsFilterChangedListener) mFrame);
		PowerOceanAtlas.getInstance().removePrefsChangedListener((PrefsChangedListener) mFrame);
		mMainFileViewer.removeMetadataChangedListener((MetadataChangedListener) mFrame);
		mMainFileViewer.removeObsChangedListener((ObsChangedListener) mXYPlot);
		PowerOceanAtlas.getInstance().removeWindowsMenuChangedListener((WindowsMenuChangedListener) mFrame);
		mPlotSpec = null;
		mParent = null;
		mXYPlot = null;
		for (int i = 0; i < 10; i++) {
			mObsMarker[i] = null;
		}
		mColorBarLegend = null;
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
				mXYPlot.invalidate();
			}
			mXYPlot.revalidate();
			mXYPlot.repaint();
		}
		else if (cmd.equals("lock")) {
			mWindowIsLocked = !mWindowIsLocked;
			if (!mWindowIsLocked) {
				// are now unlocking the window
				this.setSize(this.getSize().width + 1, this.getSize().height);
				this.setSize(this.getSize().width, this.getSize().height);
				if (mColorBarLegend != null && mColorBarWasChanged) {
					this.getContentPane().remove(mColorBarLegend);
					mColorBarLegend = null;
					mColorBarLegend = new ColorBarPanel(mFrame, mMainFileViewer, mMainFileViewer.mDefaultCB, mFG,
					    JOAConstants.DEFAULT_FRAME_COLOR, false, false);
					mColorBarLegend.setEnhanceable(true);
					this.getContentPane().add("East", mColorBarLegend);
					mColorBarWasChanged = false;
				}
				else if (mColorBarLegend != null) {
					mColorBarLegend.setLinked(true);
					mColorBarLegend.setLocked(false);
				}

				if (mCurrObsPanel != null) {
					mCurrObsPanel.setLocked(false);
				}

				mXYPlot.invalidate();
				mXYPlot.validate();
				this.invalidate();
				this.validate();
				mXYPlot.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				this.setResizable(true);
			}
			else {
				if (mColorBarLegend != null) {
					mColorBarLegend.setLinked(false);
					mColorBarLegend.setLocked(true);
				}
				mXYPlot.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
				JOAConstants.DEFAULT_PRINTERJOB.setPrintable(mXYPlot, JOAConstants.DEFAULT_PAGEFORMAT);
				try {
					JOAConstants.DEFAULT_PRINTERJOB.print();
				}
				catch (PrinterException ex) {
				}
			}
		}
		else if (cmd.equals("erase")) {
			mEraseBtn.setSelected(false);
			// set the skip flag for all stns
			for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section) of.getSection(sec);

					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station) sech.mStations.elementAt(stc);
						sh.setSkipStn(true);
					}
				}
			}
			mXYPlot.revalidate();
			mXYPlot.repaint();
		}
		else {
			mMainFileViewer.doCommand(cmd, mFrame);
		}
	}

	public void saveAsPNG() {
		class BasicThread extends Thread {
			// ask for filename
			public void run() {
				Image image = mXYPlot.makeOffScreen((Graphics2D) mXYPlot.getGraphics(), true, true);

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

		for (int i = 0; i < mNumXAxes; i++) {
			mWinXScale[i] = mScaleWidth / (mWinXPlotMax[i] - mWinXPlotMin[i]);
			mWinXOrigin[i] = mWinXPlotMin[i];
		}
	}

	public void setYScale(int height) {
		mHeightCurrWindow = height;
		mScaleHeight = height;
		if (mPlotAxes) {
			mScaleHeight -= ((PVPBOTTOM * mNumXAxes) + PVPTOP);
		}
		mWinYScale = -mScaleHeight / (mWinYPlotMax - mWinYPlotMin);
		mWinYOrigin = mWinYPlotMax;
	}

	public void colorBarChanged(ColorBarChangedEvent evt) {
		if (mWindowIsLocked) {
			mColorBarWasChanged = true;
			return;
		}
		mXYPlot.invalidate();
		mXYPlot.setSize(this.getSize().width + 1, this.getSize().height);
		mXYPlot.setSize(this.getSize().width, this.getSize().height);
		if (mColorBarLegend != null) {
			this.getContentPane().remove(mColorBarLegend);
			mColorBarLegend = null;
			mColorBarLegend = new ColorBarPanel(mFrame, mMainFileViewer, mMainFileViewer.mDefaultCB, mFG,
			    JOAConstants.DEFAULT_FRAME_COLOR, false, false);
			mColorBarLegend.setEnhanceable(true);
			this.getContentPane().add("East", mColorBarLegend);
		}
		this.invalidate();
		this.validate();
	}

	public void dataAdded(DataAddedEvent evt) {
		if (mWindowIsLocked) { return; }
		mXYPlot.invalidate();
		mXYPlot.setSize(this.getSize().width + 1, this.getSize().height);
		mXYPlot.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
	}

	public void prefsChanged(PrefsChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		mXYPlot.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
		mBG = JOAConstants.DEFAULT_CONTENTS_COLOR;
		mPlotSpec.setBGColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
		if (mColorBarLegend != null) {
			mColorBarLegend.setNewBGColor(JOAConstants.DEFAULT_FRAME_COLOR);
		}
		mXYPlot.invalidate();
		mXYPlot.setSize(this.getSize().width + 1, this.getSize().height);
		mXYPlot.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
	}

	public void obsFilterChanged(ObsFilterChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		mXYPlot.invalidate();
		mXYPlot.setSize(this.getSize().width + 1, this.getSize().height);
		mXYPlot.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
	}

	public void stnFilterChanged(StnFilterChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		mXYPlot.invalidate();
		mXYPlot.setSize(this.getSize().width + 1, this.getSize().height);
		mXYPlot.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
	}

	public void metadataChanged(MetadataChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		if (mColorBarLegend == null || !mColorBarLegend.getColorBar().isMetadataColorBar()) { return; }
		mColorBarLegend.invalidate();
		mColorBarLegend.setSize(this.getSize().width + 1, this.getSize().height);
		mColorBarLegend.setSize(this.getSize().width, this.getSize().height);
		mColorBarLegend.invalidate();
		mColorBarLegend.validate();
	}

	public void showConfigDialog() {
		// show configuration dialog
		mXYPlot.showConfigDialog();
	}

	@SuppressWarnings("serial")
	private class XYPlotPanel extends RubberbandPanel implements ObsChangedListener, DialogClient, ActionListener,
	    Printable {
		private Image mOffScreen = null;
		private Rubberband rbRect;
		private DialogClient mDialogClient = null;
		private JPopupMenu mPopupMenu = null;
		private Rectangle mSelectionRect = new Rectangle(0, 0, 0, 0);
		BasicStroke lw2 = new BasicStroke(2);
		Rectangle oldRect = new Rectangle();

		public XYPlotPanel() {
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
			GeneralPath p = (GeneralPath) obj;
			Rectangle r = p.getBounds();
			mSelectionRect.setRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
			oldRect.add(mSelectionRect.getBounds());
			if (oldRect.getWidth() == 0 && oldRect.getHeight() == 0) { return; }
			oldRect.setBounds(oldRect.x - 4, oldRect.y - 4, oldRect.width + 8, oldRect.height + 8);
			paintImmediately(oldRect);
			oldRect = mSelectionRect.getBounds();
		}

		@SuppressWarnings("unchecked")
		public int print(Graphics gin, PageFormat pageFormat, int pageIndex) {
			if (pageIndex == 0) {
				Graphics2D g = (Graphics2D) gin;

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
					xScale = pageFormat.getImageableWidth() / ((double) (od.width + cbwidth));
				}

				if (od.height > pageFormat.getImageableHeight()) {
					yScale = pageFormat.getImageableHeight() / ((double) od.height);
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
					g
					    .translate(pageFormat.getImageableX() + (xOffset - (cbwidth * xScale)), pageFormat.getImageableY()
					        + yOffset);
				}
				else {
					g.translate(pageFormat.getImageableX() + xOffset, pageFormat.getImageableY() + yOffset);
				}

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
				TextLayout tl = new TextLayout(sTemp, (Map) map, g.getFontRenderContext());
				Rectangle2D strbounds = tl.getBounds();
				double strWidth = strbounds.getWidth();
				double hh = (PVPLEFT + (od.width - PVPRIGHT - PVPLEFT) / 2) - strWidth / 2;
				double vv = PVPTOP/2 + strbounds.getHeight()/2;

				JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, g, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
				    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
				    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

				// Add the BG color to the plot
				int x1, y1, width, height;
				if (mPlotAxes) {
					x1 = PVPLEFT;
					width = this.getSize().width - PVPLEFT - PVPRIGHT;
					y1 = PVPTOP;
					height = this.getSize().height - PVPTOP - (PVPBOTTOM * mNumXAxes);
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
				
				if (mPlotSpec.getTSModelTerms() != null && mPlotSpec.getTSModelTerms().size() > 0) {
					plotTSCurve(g);
				}
				

					if (mConnectObs) {
						for (FileViewer fv : mFileViewers) {
							plotStnLine(g, false, fv);
						}				
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
			// if (!(mLabelPanel && mFancyLabelPanel))
			// bottom -= 15;
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

			// find the length of the longest label
			for (int i = 0; i < numColors; i++) {
				// swatch
				bandTop = (int) (top + (i) * pixelsPerBand);
				bandBottom = (int) (bandTop + pixelsPerBand);
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
			if (mMainFileViewer != null) {
				int pPos = mMainFileViewer.getPropertyPos(mColorBarLegend.getColorBar().getParam(), false);
				if (pPos >= 0 && mMainFileViewer.mAllProperties[pPos].getUnits() != null
				    && mMainFileViewer.mAllProperties[pPos].getUnits().length() > 0) {
					panelLabel = new String(mColorBarLegend.getColorBar().getParam() + " ("
					    + mMainFileViewer.mAllProperties[pPos].getUnits() + ")");
				}
				else {
					panelLabel = new String(mColorBarLegend.getColorBar().getParam());
				}
			}
			else {
				panelLabel = new String(mColorBarLegend.getColorBar().getParam());
			}
			int strWidth = fm.stringWidth(panelLabel);
			JOAFormulas.drawStyledString(panelLabel, left + 25 - strWidth / 2, bandBottom + 15, fm, (Graphics2D) g);
		}

		public UVCoordinate getCorrectedXY(int x, int y) {
			boolean reverseY = mMainFileViewer.mAllProperties[mYVarCode].isReverseY();
			double dy = (double) y;
			double dx = (double) x;
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
				dy -= PVPBOTTOM * mNumXAxes;
			}
			else if (reverseY) {
				dy = mHeightCurrWindow - dy;
			}

			return new UVCoordinate(dx, dy);
		}

		public double[] getInvTransformedX(double x) {
			double[] xvals = new double[mNumXAxes];
			for (int i = 0; i < mNumXAxes; i++) {
				xvals[i] = x / mWinXScale[i] + mWinXOrigin[i];
			}
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
				return this.getSize().height - (PVPBOTTOM * mNumXAxes);
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
			
			// add options for exporting the TS Model or Regression model
			if (mPlotSpec.getTSModelTerms() != null) {
				JMenuItem saveTSItem = new JMenuItem("Save TS Model...");
				saveTSItem.setActionCommand("savets");
				saveTSItem.addActionListener(this);
				mPopupMenu.add(saveTSItem);
			}
			
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
			ConfigurePvPPlotDC cp = new ConfigurePvPPlotDC(mFrame, mMainFileViewer, mDialogClient, mPlotSpec);
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
			else if (cmd.equals("savets")) {
				save("untitled");
			}
			else if (cmd.equals("saveregress")) {
				saveRegress("untitled");
			}
		}
		
		public void save(String suggestedMapName) {
			// get a filename
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith("_tsmodel.xml")) {
						return true;
					}
					else {
						return false;
					}
				}
			};
			Frame fr = new Frame();
			String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
			FileDialog f = new FileDialog(fr, "Save TS Model settings with name ending in \"_tsmodel.xml\"", FileDialog.SAVE);
			f.setDirectory(directory);
			f.setFilenameFilter(filter);
			f.setFile(suggestedMapName);
			f.setVisible(true);
			directory = f.getDirectory();
			String fs = f.getFile();
			f.dispose();
			if (directory != null && fs != null) {
				File nf = new File(directory, fs);
				try {
					saveModelSettings(nf);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		public void saveRegress(String suggestedMapName) {
			// get a filename
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith("_tsmodel.xml")) {
						return true;
					}
					else {
						return false;
					}
				}
			};
			Frame fr = new Frame();
			String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
			FileDialog f = new FileDialog(fr, "Save TS Model settings with name ending in \"_tsmodel.xml\"", FileDialog.SAVE);
			f.setDirectory(directory);
			f.setFilenameFilter(filter);
			f.setFile(suggestedMapName);
			f.setVisible(true);
			directory = f.getDirectory();
			String fs = f.getFile();
			f.dispose();
			if (directory != null && fs != null) {
				File nf = new File(directory, fs);
				try {
					saveRegression(nf);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		public void saveModelSettings(File file) {
			double intercept = 1.0;
			double min = -999;
			double max = -999;
			double tmin = -999;
			double tmax = -999;
			int maxDegree = 10;
			int prec = 5;
			int smallestExp = 5;
			double smallest = Math.pow(10, smallestExp);
			
			NumericFilter zfp = mPlotSpec.getZFilter();
			NumericFilter tfp = mPlotSpec.getTFilter();
			Vector<JOATimeFilter> mTimeFilters = mPlotSpec.getTimeFilters();

			if (zfp != null) {
					min = zfp.getMin();
					max = zfp.getMax();
			}

			if (tfp != null) {
					tmin = tfp.getMin();
					tmax = tfp.getMax();
			}

			// save preferences as XML
			try {
				Document doc = (Document) Class.forName("com.ibm.xml.parser.TXDocument").newInstance();
				
				//Pop up dialog to assign comment

				// make joapreferences the root element
				Element root = doc.createElement("joatsmodel");
				root.setAttribute("intercept", JOAFormulas.formatDouble(mPlotSpec.getModelIntercept(), prec, false));
					root.setAttribute("description", "Exported from plot");

				for (TSModelTerm tsmt : mPlotSpec.getTSModelTerms()) {
					// make the map region element and add it
					double scaler = tsmt.getConstant();
					double exp = tsmt.getExponent();

					if (exp <= maxDegree && Math.abs(scaler) > smallest) {
						Element item = doc.createElement("tsterm");
						item.setAttribute("type", String.valueOf(tsmt.getParam().toString()));
						// filter

						item.setAttribute("scaler", JOAFormulas.formatDouble(scaler, prec, false));
						item.setAttribute("exponent", String.valueOf((int) exp));
						root.appendChild(item);
					}
				}

				if (tfp != null) {
					Element item = doc.createElement("trangefilter");
					item.setAttribute("min", String.valueOf(tmin));
					item.setAttribute("max", String.valueOf(tmax));
					root.appendChild(item);
				}

				if (zfp != null) {
					Element item = doc.createElement("zrangefilter");
					item.setAttribute("min", String.valueOf(min));
					item.setAttribute("max", String.valueOf(max));
					root.appendChild(item);
				}
				
				for (JOATimeFilter jtf : mTimeFilters) {
					String tagName = "foobar";
					
					if (jtf instanceof DateRangeFilter) {
						tagName = "datefilter";
						Element item = doc.createElement(tagName);
						item.setAttribute("min", String.valueOf(((DateRangeFilter)jtf).getMin()));
						item.setAttribute("max", String.valueOf(((DateRangeFilter)jtf).getMax()));	
						root.appendChild(item);					
					}
					else if (jtf instanceof SeasonFilter) {
						tagName = "seasonfilter";
						Element item = doc.createElement(tagName);
						item.setAttribute("ord", String.valueOf(((SeasonFilter)jtf).getSeason()));
						root.appendChild(item);					
					}
					else if (jtf instanceof MonthRangeFilter) {
						tagName = "monthfilter";
						Element item = doc.createElement(tagName);
						item.setAttribute("min", String.valueOf(((MonthRangeFilter)jtf).getMinMonth()));
						item.setAttribute("max", String.valueOf(((MonthRangeFilter)jtf).getMaxMonth()));	
						root.appendChild(item);					
					}
					else if (jtf instanceof NumericFilter) {
						tagName = "yeardayfilter";
						Element item = doc.createElement(tagName);
						item.setAttribute("min", String.valueOf(((NumericFilter)jtf).getMin()));
						item.setAttribute("max", String.valueOf(((NumericFilter)jtf).getMax()));	
						root.appendChild(item);					
					}
				}
				
				//These error terms are not set when plot spec is created

				double[] errTerms = mPlotSpec.getModelErrTerms();
				Element item = doc.createElement("Bias");
				item.setAttribute("value", String.valueOf(errTerms[0]));
				item.setAttribute("description", "Arithmetic mean of errors");
				root.appendChild(item);

				item = doc.createElement("MAD");
				item.setAttribute("value", String.valueOf(errTerms[1]));
				item.setAttribute("description", "Mean Absolute Deviation");
				root.appendChild(item);

				item = doc.createElement("MASE");
				item.setAttribute("value", String.valueOf(errTerms[2]));
				item.setAttribute("description", "Mean Square Error");
				root.appendChild(item);

				item = doc.createElement("MAPE");
				item.setAttribute("value", String.valueOf(errTerms[3]));
				item.setAttribute("description", "Mean Absolute % Error");
				root.appendChild(item);
				
				item = doc.createElement("SAE");
				item.setAttribute("value", String.valueOf(errTerms[4]));
				item.setAttribute("description", "Sum Average Errors");
				root.appendChild(item);

				doc.appendChild(root);
				((TXDocument) doc).setVersion("1.0");
				FileWriter fr = new FileWriter(file);
				((TXDocument) doc).printWithFormat(fr);
				fr.flush();
				fr.close();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		public void saveRegression(File file) {
			double intercept = 1.0;
			double min = -999;
			double max = -999;
			double tmin = -999;
			double tmax = -999;
			int maxDegree = 10;
			int prec = 5;
			int smallestExp = 5;
			double smallest = Math.pow(10, smallestExp);
			
			NumericFilter zfp = mPlotSpec.getZFilter();
			NumericFilter tfp = mPlotSpec.getTFilter();
			Vector<JOATimeFilter> mTimeFilters = mPlotSpec.getTimeFilters();

			if (zfp != null) {
					min = zfp.getMin();
					max = zfp.getMax();
			}

			if (tfp != null) {
					tmin = tfp.getMin();
					tmax = tfp.getMax();
			}

			// save preferences as XML
			try {
				Document doc = (Document) Class.forName("com.ibm.xml.parser.TXDocument").newInstance();
				
//				Pop up dialog to assign comment
				// bneed to loop through all the regression models

				// make joapreferences the root element
				Element root = doc.createElement("joaregressionmodel");
				root.setAttribute("intercept", JOAFormulas.formatDouble(mPlotSpec.getModelIntercept(), prec, false));
					root.setAttribute("description", "Exported from plot");


				doc.appendChild(root);
				((TXDocument) doc).setVersion("1.0");
				FileWriter fr = new FileWriter(file);
				((TXDocument) doc).printWithFormat(fr);
				fr.flush();
				fr.close();
			}
			catch (Exception ex) {
				ex.printStackTrace();
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
		
		public void plotTSCurve(Graphics2D g) {// compute the coordinates of the y grid
			int saltPos = mPlotSpec.isSaltAxis();
			double[] computedSalinities = new double[TGRIDSIZE + 1];
			double[] xCoords = new double[TGRIDSIZE + 1];
			double[] yCoords = new double[TGRIDSIZE + 1];
			double[] temperatureVals = new double[TGRIDSIZE + 1];
			double[] sqtemperatureVals = new double[TGRIDSIZE + 1];
			double yRange = mPlotSpec.getTMaxOfModel() - mPlotSpec.getTMinOfModel();
			double yDelta = yRange / TGRIDSIZE;
			for (int i = 0; i < TGRIDSIZE; i++) {
				temperatureVals[i] = mPlotSpec.getTMinOfModel() + (double) i * yDelta;
				sqtemperatureVals[i] = temperatureVals[i] * temperatureVals[i];
				yCoords[i] = (temperatureVals[i] - mWinYOrigin) * mWinYScale;
				// adjust for axes labels
				if (mPlotAxes) {
					yCoords[i] += PVPTOP; // + PVPBOTTOM;
				}
			}
			temperatureVals[TGRIDSIZE] = mPlotSpec.getTMinOfModel() + (double) TGRIDSIZE * yDelta;
			sqtemperatureVals[TGRIDSIZE] = temperatureVals[TGRIDSIZE] * temperatureVals[TGRIDSIZE];
			yCoords[TGRIDSIZE] = (temperatureVals[TGRIDSIZE] - mWinYOrigin) * mWinYScale;
			if (mPlotAxes) {
				yCoords[TGRIDSIZE] += PVPTOP; // + PVPBOTTOM;
			}

			Vector<TSModelTerm> terms = mPlotSpec.getTSModelTerms();
			for (int i=0; i<TGRIDSIZE + 1; i++) {
				double temp = temperatureVals[i];
				double sqtemp = sqtemperatureVals[i];
				double salt = mPlotSpec.getModelIntercept();
				for (TSModelTerm term : terms) {
					if (term.getParam() == TSModelTermParameter.TEMPERATURE) {
						salt += term.getConstant() * Math.pow(temp, term.getExponent());
					}
					else if (term.getParam() == TSModelTermParameter.SQTEMPERATURE) {
						salt += term.getConstant() * Math.pow(sqtemp, term.getExponent());
					}
				}
				computedSalinities[i] = salt;
				xCoords[i] = (computedSalinities[i] - mWinXOrigin[saltPos]) * mWinXScale[saltPos];
				// adjust for axes labels
				if (mPlotAxes) {
					xCoords[i] += PVPLEFT;
				}
			}
			
			// create the general path
			GeneralPath modelLine = new GeneralPath();
			modelLine.moveTo(xCoords[0], yCoords[0]);
			for (int i=1; i<TGRIDSIZE + 1; i++) {
				modelLine.lineTo(xCoords[i], yCoords[i]);
			}
			//modelLine.closePath();

			g.setColor(Color.white);
			if (JOAConstants.THICKEN_CONTOUR_LINES) {
				g.setStroke(lw4);
			}
			else {
				g.setStroke(lw2);
			}
			try {
				g.draw(modelLine);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@SuppressWarnings("unchecked")
		public void plotIsopycnals(Graphics2D g) {
			int saltPos = mPlotSpec.isSaltAxis();

			// compute the coordinates of the y grid
			double[] y = new double[SIGGRIDSIZE + 1];
			double[] tempVals = new double[SIGGRIDSIZE + 1];
			double yRange = mWinYPlotMax - mWinYPlotMin;
			double yDelta = yRange / SIGGRIDSIZE;
			for (int i = 0; i < SIGGRIDSIZE; i++) {
				tempVals[i] = mWinYPlotMin + (double) i * yDelta;
				y[i] = (tempVals[i] - mWinYOrigin) * mWinYScale;
				// adjust for axes labels
				if (mPlotAxes) {
					y[i] += PVPTOP; // + PVPBOTTOM;
				}
			}
			tempVals[SIGGRIDSIZE] = mWinYPlotMin + (double) SIGGRIDSIZE * yDelta;
			y[SIGGRIDSIZE] = (tempVals[SIGGRIDSIZE] - mWinYOrigin) * mWinYScale;
			if (mPlotAxes) {
				y[SIGGRIDSIZE] += PVPTOP; // + PVPBOTTOM;
			}

			// compute the coordinates of the x grid
			double[] x = new double[SIGGRIDSIZE + 1];
			double[] saltVals = new double[SIGGRIDSIZE + 1];
			double xRange = mWinXPlotMax[saltPos] - mWinXPlotMin[saltPos];
			double xDelta = xRange / SIGGRIDSIZE;
			for (int i = 0; i < SIGGRIDSIZE; i++) {
				saltVals[i] = mWinXPlotMin[saltPos] + (double) i * xDelta;
				x[i] = (saltVals[i] - mWinXOrigin[saltPos]) * mWinXScale[saltPos];

				// adjust for axes labels
				if (mPlotAxes) {
					x[i] += PVPLEFT;
				}
			}
			saltVals[SIGGRIDSIZE] = mWinXPlotMin[saltPos] + (double) SIGGRIDSIZE * xDelta;
			x[SIGGRIDSIZE] = (saltVals[SIGGRIDSIZE] - mWinXOrigin[saltPos]) * mWinXScale[saltPos];
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
			int numContours = (int) ((maxSigma - minSigma) / mSigmaInc);
			double contourValues[] = new double[numContours];
			int numContourPairs[] = new int[numContours];
			Vector<UVCoordinate>[] vectors = new Vector[numContours];
			Vector<UVCoordinate>[] contourCoords = vectors;
			GeneralPath[] contourLines = new GeneralPath[numContours];
			for (int i = 0; i < numContours; i++) {
				contourValues[i] = minSigma + (double) i * mSigmaInc;
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
				TextLayout tl = new TextLayout(valStr, (Map) map, g.getFontRenderContext());
				Rectangle2D strbounds = tl.getBounds();
				double strWidth = strbounds.getWidth();

				// point 2 points to the left of the middle point
				UVCoordinate uvLeft = (UVCoordinate) contourCoords[k].elementAt(mid - 2);
				uright = uvLeft.u;
				vright = uvLeft.v;

				// form the contour with break for the level label
				boolean leftGap = false;
				boolean setGap = false;
				double slope = 0.0;
				for (int j = 0; j < contourCoords[k].size() - 1; j++) {
					UVCoordinate uvRight = (UVCoordinate) contourCoords[k].elementAt(j + 1);
					UVCoordinate uvMid = (UVCoordinate) contourCoords[k].elementAt(j);

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

//						j += 2;

						// store the points that mark the label blank area
						leftGapLoc[k] = uvLeft;
						continue;
					}
					else if (j > mid - 3 && dist < strWidth) {
						continue;
					}

					UVCoordinate uv = (UVCoordinate) contourCoords[k].elementAt(j);
					UVCoordinate uv2 = (UVCoordinate) contourCoords[k].elementAt(j + 1);
					if (leftGap && !setGap) {
						if (Math.abs(slope) > 1.0) {
							contourLines[k].moveTo((float) uvMid.u, (float) uvMid.v);
						}
						else {
							contourLines[k].moveTo((float) uright, (float) vright);
						}
						setGap = true;
					}
					else {
						contourLines[k].moveTo((float) uv.u, (float) uv.v);
					}
					contourLines[k].lineTo((float) uv2.u, (float) uv2.v);
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
					UVCoordinate uvLeft = (UVCoordinate) contourCoords[k].elementAt(mid - 2);
					UVCoordinate uvRight = (UVCoordinate) contourCoords[k].elementAt(mid + 2);

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

		public void zoomPlot(Rectangle newRect, boolean sameWindowZoom, boolean newWindowCopyMode) {
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
				y1 -= PVPBOTTOM * mNumXAxes;
				y2 = mHeightCurrWindow - y2;
				x2 -= PVPLEFT;
				y2 -= PVPBOTTOM * mNumXAxes;
			}
			else if (reverseY) {
				y1 = mHeightCurrWindow - y1;
				y2 = mHeightCurrWindow - y2;
			}

			if (sameWindowZoom) {
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

				if (!newWindowCopyMode) {
					mPlotSpec.setNumXAxes(mNumXAxes);
					for (int i = 0; i < mNumXAxes; i++) {
						mWinXPlotMax[i] = x2 / mWinXScale[i] + mWinXOrigin[i];
						mWinXPlotMin[i] = x1 / mWinXScale[i] + mWinXOrigin[i];

						newRange = JOAFormulas.GetPrettyRange(mWinXPlotMin[i], mWinXPlotMax[i]);
						mWinXPlotMin[i] = newRange.getVal1();
						mWinXPlotMax[i] = newRange.getVal2();
						mXInc[i] = newRange.getVal3();
						mPlotSpec.setWinXPlotMin(i, mWinXPlotMin[i]);
						mPlotSpec.setWinXPlotMax(i, mWinXPlotMax[i]);
						mPlotSpec.setXInc(i, mXInc[i]);
					}
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
				XYPlotSpecification ps = new XYPlotSpecification();
				for (FileViewer fv: mFileViewers) {
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

				ps.setNumXAxes(mNumXAxes);
				if (!newWindowCopyMode) {
					for (int i = 0; i < mNumXAxes; i++) {
						ps.setWinXPlotMax(i, x2 / mWinXScale[i] + mWinXOrigin[i]);
						ps.setWinXPlotMin(i, x1 / mWinXScale[i] + mWinXOrigin[i]);
						mPlotSpec.setXInc(i, mXInc[i]);

						Triplet newRange = JOAFormulas.GetPrettyRange(ps.getWinXPlotMin(i), ps.getWinXPlotMax(i));
						ps.setWinXPlotMin(i, newRange.getVal1());
						ps.setWinXPlotMax(i, newRange.getVal2());
						ps.setXInc(i, newRange.getVal3());
						ps.setXVarCode(i, mXVarCode[i]);
						ps.setXTics(i, mXTics[i]);
						// ps.setSymbol(mSymbol);
						// ps.setSymbolSize(mSymbolSize);
					}
				}
				else {
					for (int i = 0; i < mNumXAxes; i++) {
						ps.setWinXPlotMax(i, mWinXPlotMax[i]);
						ps.setWinXPlotMin(i, mWinXPlotMin[i]);
						ps.setXInc(i, mXInc[i]);
						ps.setXVarCode(i, mXVarCode[i]);
						ps.setXTics(i, mXTics[i]);
					}
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
				ps.setIncludeCBAR(mIncludeCBAR);
				ps.setIncludeObsPanel(mIncludeObsPanel);
				ps.setConnectObs(mConnectObs);
				ps.setXGrid(mXGrid);
				ps.setYGrid(mYGrid);
				ps.setYTics(mYTics);
				ps.setPlotAxes(mPlotAxes);
				if (ps.isIncludeCBAR()) {
					ps.setWidth(550);
					ps.setHeight(450);
				}
				else {
					ps.setWidth(450);
					ps.setHeight(450);
				}
				ps.setReverseY(mMainFileViewer.mAllProperties[mYVarCode].isReverseY());
				ps.setPlotIsopycnals(mPlotIsopycnals);
				ps.setCanPlotIsoPycnals(mCanPlotIsoPycnals);
				ps.setPlotOnlyCurrStn(mPlotOnlyCurrStn);
				ps.setAccumulateStns(mAccumulateStns);
				ps.setRefPress(mRefPress);
				ps.setColorByCBParam(mPlotSpec.isColorByCBParam());
				ps.setColorByConnectLineColor(mPlotSpec.isColorByConnectLineColor());
				
				ps.setFilteredOutColor(mPlotSpec.getFilteredOutColor());
				ps.setTimeFilters(mPlotSpec.getTimeFilters());
				ps.setTSModelTerms(mPlotSpec.getTSModelTerms());
				ps.setZFilter(mPlotSpec.getZFilter());
				ps.setTFilter(mPlotSpec.getTFilter());
				ps.setOverrideLabel(mPlotSpec.getOverrideLabel());
				ps.setModelIntercept(mPlotSpec.getModelIntercept());
				ps.setTMinOfModel(mPlotSpec.getTMinOfModel());
				ps.setTMaxOfModel(mPlotSpec.getTMaxOfModel());
				ps.setModelErrTerms(mPlotSpec.getModelErrTerms());

				// make a new plot window
				try {
					ps.writeToLog("Zoomed XY Plot: (" + mParent.getTitle() + "):");
				}
				catch (Exception ex) {
				}
				JOAXYPlotWindow plotWind = new JOAXYPlotWindow(ps, mFrame);
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
				for (int i = 0; i < 10; i++) {
					if (mObsMarker[i] != null) {
						mObsMarker[i] = null;
					}
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

			if (mColorBarLegend != null) {
				pWidth += 100;
			}
			outImage = createImage(pWidth, getSize().height);

			Graphics2D og = (Graphics2D) outImage.getGraphics();
			super.paintComponent(og);

			og.setColor(Color.white);
			og.fillRect(0, 0, 2000, 2000);

			int x1, y1, width, height;
			if (mPlotAxes) {
				x1 = PVPLEFT;
				width = this.getSize().width - PVPLEFT - PVPRIGHT;
				y1 = PVPTOP;
				height = this.getSize().height - PVPTOP - (PVPBOTTOM * mNumXAxes);
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
		
			if (mPlotSpec.getTSModelTerms() != null && mPlotSpec.getTSModelTerms().size() > 0) {
				plotTSCurve(og);
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
				double vv = PVPTOP/2 + fm.getHeight()/2;

				JOAFormulas.drawStyledString(sTemp, (int) hh, (int) vv, og, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
				    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
				    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);
			}

			if (plotOverlays) {
				if (mConnectObs) {
					for (FileViewer fv : mFileViewers) {
						plotStnLine(og, false, fv);
					}
				}
			}

			Dimension od = this.getSize();
			if (mColorBarLegend != null) {
				og.setClip(0, 0, 2000, 2000);
				og.translate(od.width, PVPTOP);
				this.drawColorBar(og, od.height - PVPTOP - PVPBOTTOM);
			}

			og.dispose();
			return outImage;
		}

		public void paintComponent(Graphics gin) {
			Graphics2D g = (Graphics2D) gin;
			if (mOffScreen == null) {
				mOffScreen = makeOffScreen(g, JOAConstants.DEFAULT_PLOT_TITLES, false);
				g.drawImage(mOffScreen, 0, 0, null);
			}
			else {
				g.drawImage(mOffScreen, 0, 0, null);
				g.setColor(JOAConstants.DEFAULT_SELECTION_REGION_BG_COLOR);
				g.fillRect((int) mSelectionRect.getX(), (int) mSelectionRect.getY(), (int) mSelectionRect.getWidth(),
				    (int) mSelectionRect.getHeight());
				g.setColor(JOAConstants.DEFAULT_SELECTION_REGION_OUTLINE_COLOR);
				g.setStroke(lw2);
				g.drawRect((int) mSelectionRect.getX(), (int) mSelectionRect.getY(), (int) mSelectionRect.getWidth(),
				    (int) mSelectionRect.getHeight());
				g.setColor(Color.black);
			}

			if (mConnectObs && !mWindowIsLocked) {
				for (FileViewer fv : mFileViewers) {
					plotStnLine(g, false, fv);
				}
			}

			if (!mWindowIsLocked) {
				g.setClip(0, 0, this.getSize().width, this.getSize().height);
				for (int i = 0; i < mNumXAxes; i++) {
					if (mObsMarker[i] != null) {
						mObsMarker[i].drawMarker(g, false);
					}
				}
			}
		}

		public void plotXY(Graphics2D g) {
			int xPos, yPos;
			double theVal = -99;
			double x, y;
			int currSymbol;
			int currSymbolSize;

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

			colorByMetaData = mMainFileViewer.mDefaultCB.isMetadataColorBar();
			if (colorByMetaData) {
				isDateMetadata = mMainFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTime"));
				isMonthMetadata = mMainFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTimeMonth"));
				isLatMetadata = mMainFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLatitude"));
				isLonMetadata = mMainFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLongitude"));
			}

			int x1, width, y1, height;
			if (mPlotAxes) {
				x1 = PVPLEFT;
				width = this.getSize().width - PVPLEFT - PVPRIGHT;
				y1 = PVPTOP;
				height = this.getSize().height - PVPTOP - (PVPBOTTOM * mNumXAxes);
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
					plotOneStn(fv, g, colorByMetaData, isDateMetadata,
						 isMonthMetadata,  isLatMetadata, isLonMetadata, reverseY);
				}
			}
			else {
				// plot all the stations
				for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
					OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.elementAt(fc);

					for (int sec = 0; sec < of.mNumSections; sec++) {
						Section sech = (Section) of.getSection(sec);

						xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[0]].getVarLabel(), false);
						yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
						if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
							continue;
						}
						for (int stc = 0; stc < sech.mStations.size(); stc++) {
							Station sh = (Station) sech.mStations.elementAt(stc);
							if (!sh.mUseStn || (mAccumulateStns && sh.isSkipStn())) {
								continue;
							}

							if (colorByMetaData) {
								stnLat = sh.getLat();
								stnLon = sh.getLon();
								stnDate = sh.getDate();
								stnMonth = sh.getMonth() - 1;

								enhanceByMetadata = mMainFileViewer.mDefaultCB
								    .isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnLat))
								    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnLon))
								    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnDate))
								    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnMonth));

								if (isDateMetadata) {
									stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnDate);
								}
								else if (isMonthMetadata) {
									stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnMonth);
								}
								else if (isLatMetadata) {
									stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLat);
								}
								else if (isLonMetadata) {
									stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLon);
								}
							}

							if (xPos >= 0 && yPos >= 0) {
								for (int b = 0; b < sh.mNumBottles; b++) {
									enhanceObs = false;
									Bottle bh = (Bottle) sh.mBottles.elementAt(b);
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

									for (int i = 0; i < mNumXAxes; i++) {
										currSymbol = mSymbol[i];
										currSymbolSize = mSymbolSize[i];
										xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel(), false);
										if (xPos == -1) {
											continue;
										}

										x = bh.mDValues[xPos];
										y = bh.mDValues[yPos];
										if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
											x = (x - mWinXOrigin[i]) * mWinXScale[i];
											y = (y - mWinYOrigin) * mWinYScale;

											// adjust for axes labels
											if (mPlotAxes && !reverseY) {
												x += PVPLEFT;
												y += PVPTOP;
											}
											else if (mPlotAxes && reverseY) {
												y = mHeightCurrWindow - y;
												x += PVPLEFT;
												y -= PVPBOTTOM * mNumXAxes;
											}
											else if (reverseY) {
												y = mHeightCurrWindow - y;
											}

											Color currColor = Color.black;
											if (mPlotSpec.isColorByCBParam()) {
												// get a color for this value
												if (colorByMetaData) {
													currColor = stnMetadataColor;
												}
												else {
													// get the value of the color variable at this point
													theVal = JOAFormulas.getValueOfColorVariable(mMainFileViewer, sech, bh);
													currColor = mMainFileViewer.mDefaultCB.getColor(theVal);
													if (mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(theVal))) {
														enhanceObs = true;
													}
												}
											}
											else if (mPlotSpec.isColorByConnectLineColor()) {
												currColor = mPlotSpec.getConnectStnColor(i);
											}
											// Determine override color if there is a ZTFilter or time
											// filters
											boolean overRideColor = false;

											int pPos = sech.getPRESVarPos();
											int tPos = sech.getVarPos("TEMP", true);

											if (tPos >= 0 || pPos >= 0) {
												if (mPlotSpec.getTimeFilters() != null && mPlotSpec.getTimeFilters().size() > 0) {
													// Apply Time Filters
													for (JOATimeFilter jtFilter : mPlotSpec.getTimeFilters()) {
														if (!jtFilter.test(sh)) {
															overRideColor = true;
															break;
														}
													}
												}

												// apply ZT Filters
												boolean c1 = true;
												if (mPlotSpec.getZFilter() != null) {
													c1 = mPlotSpec.getZFilter().test(bh, pPos);
												}

												boolean c2 = true;
												if (c1 && mPlotSpec.getTFilter() != null) {
													c2 = mPlotSpec.getTFilter().test(bh, tPos);
												}

												if (!c1 || !c2) {
													overRideColor = true;
												}
											}

											if (overRideColor) {
												currColor = mPlotSpec.getFilteredOutColor();
											}

											// plot point
											g.setColor(currColor);
											if (!enhanceObs && !enhanceByMetadata) {
												JOAFormulas.plotSymbol(g, currSymbol, (int) x, (int) y, currSymbolSize);
											}
										}
									}
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
					OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.currElement();
					Section sech = (Section) of.getCurrSection();
					yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
					boolean canPlot = true;
					if (sech.mNumCasts == 0 || yPos == -1) {
						canPlot = false;
					}
					Station sh = (Station) sech.mStations.currElement();
					if (!sh.mUseStn) {
						canPlot = false;
					}

					if (canPlot && colorByMetaData) {
						stnLat = sh.getLat();
						stnLon = sh.getLon();
						stnDate = sh.getDate();
						stnMonth = sh.getMonth() - 1;

						enhanceByMetadata = mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnLat))
						    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnLon))
						    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnDate))
						    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnMonth));

						if (isDateMetadata) {
							stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnDate);
						}
						else if (isMonthMetadata) {
							stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnMonth);
						}
						else if (isLatMetadata) {
							stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLat);
						}
						else if (isLonMetadata) {
							stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLon);
						}
					}

					if (canPlot) {
						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);
							boolean keepBottle;
							if (mMainFileViewer.mObsFilterActive) {
								keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
							}
							else {
								keepBottle = true;
							}

							for (int i = 0; i < mNumXAxes; i++) {
								currSymbol = mSymbol[i];
								currSymbolSize = mSymbolSize[i];
								xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel(), false);
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
											currSymbolSize = mSymbolSize[i];
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
										x = (x - mWinXOrigin[i]) * mWinXScale[i];
										y = (y - mWinYOrigin) * mWinYScale;

										// adjust for axes labels
										if (mPlotAxes && !reverseY) {
											x += PVPLEFT;
											y += PVPTOP;
										}
										else if (mPlotAxes && reverseY) {
											y = mHeightCurrWindow - y;
											x += PVPLEFT;
											y -= PVPBOTTOM * mNumXAxes;
										}
										else if (reverseY) {
											y = mHeightCurrWindow - y;
										}

										// get a color for this value
										Color currColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
										if (colorByMetaData) {
											currColor = stnMetadataColor;
										}
										else {
											// get the value of the color variable at this point
											theVal = JOAFormulas.getValueOfColorVariable(mMainFileViewer, sech, bh);
											currColor = mMainFileViewer.mDefaultCB.getColor(theVal);
										}

										if (mOverRideColor) {
											currColor = mContrastColor;
											mOverRideColor = false;
										}
										// else
										// currColor = mFileViewer.mDefaultCB.getColor(theVal);
										// Determine override color if there is a ZTFilter or time
										// filters
										boolean overRideColor = false;

										int pPos = sech.getPRESVarPos();
										int tPos = sech.getVarPos("TEMP", true);

										if (tPos >= 0 || pPos >= 0) {
											if (mPlotSpec.getTimeFilters() != null && mPlotSpec.getTimeFilters().size() > 0) {
												// Apply Time Filters
												for (JOATimeFilter jtFilter : mPlotSpec.getTimeFilters()) {
													if (!jtFilter.test(sh)) {
														overRideColor = true;
														break;
													}
												}
											}

											// apply ZT Filters
											boolean c1 = true;
											if (mPlotSpec.getZFilter() != null) {
												c1 = mPlotSpec.getZFilter().test(bh, pPos);
											}

											boolean c2 = true;
											if (c1 && mPlotSpec.getTFilter() != null) {
												c2 = mPlotSpec.getTFilter().test(bh, tPos);
											}

											if (!c1 || !c2) {
												overRideColor = true;
											}
										}

										if (overRideColor) {
											currColor = mPlotSpec.getFilteredOutColor();
										}

										// plot point
										g.setColor(currColor);
										JOAFormulas.plotSymbol(g, currSymbol, (int) x, (int) y, currSymbolSize);
									}
								}
							}
						} // for

					}
				}
				else {
					// plot all stations
					for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
						OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.elementAt(fc);

						for (int sec = 0; sec < of.mNumSections; sec++) {
							Section sech = (Section) of.getSection(sec);

							yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
							if (sech.mNumCasts == 0 || yPos == -1) {
								continue;
							}
							for (int stc = 0; stc < sech.mStations.size(); stc++) {
								Station sh = (Station) sech.mStations.elementAt(stc);
								if (!sh.mUseStn || (mAccumulateStns && sh.isSkipStn())) {
									continue;
								}

								if (colorByMetaData) {
									stnLat = sh.getLat();
									stnLon = sh.getLon();
									stnDate = sh.getDate();
									stnMonth = sh.getMonth() - 1;

									enhanceByMetadata = mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB
									    .getColorIndex(stnLat))
									    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnLon))
									    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnDate))
									    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnMonth));

									if (isDateMetadata) {
										stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnDate);
									}
									else if (isMonthMetadata) {
										stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnMonth);
									}
									else if (isLatMetadata) {
										stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLat);
									}
									else if (isLonMetadata) {
										stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLon);
									}
								}

								if (yPos >= 0) {
									for (int b = 0; b < sh.mNumBottles; b++) {
										Bottle bh = (Bottle) sh.mBottles.elementAt(b);
										boolean keepBottle;
										if (mMainFileViewer.mObsFilterActive) {
											keepBottle = mMainFileViewer.mCurrObsFilter.testObservation(mMainFileViewer, sech, bh);
										}
										else {
											keepBottle = true;
										}

										for (int i = 0; i < mNumXAxes; i++) {
											currSymbol = mSymbol[i];
											currSymbolSize = mSymbolSize[i];
											xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel(), false);
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
														currSymbolSize = mSymbolSize[i];
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
													x = (x - mWinXOrigin[i]) * mWinXScale[i];
													y = (y - mWinYOrigin) * mWinYScale;

													// adjust for axes labels
													if (mPlotAxes && !reverseY) {
														x += PVPLEFT;
														y += PVPTOP;
													}
													else if (mPlotAxes && reverseY) {
														y = mHeightCurrWindow - y;
														x += PVPLEFT;
														y -= PVPBOTTOM * mNumXAxes;
													}
													else if (reverseY) {
														y = mHeightCurrWindow - y;
													}

													Color currColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
													if (colorByMetaData) {
														currColor = stnMetadataColor;
													}
													else {
														// get the value of the color variable at this point
														theVal = JOAFormulas.getValueOfColorVariable(mMainFileViewer, sech, bh);

														// get a color for this value
														if (mOverRideColor) {
															currColor = mContrastColor;
															mOverRideColor = false;
														}
														else {
															currColor = mMainFileViewer.mDefaultCB.getColor(theVal);
														}
													}
													
													// Determine override color if there is a ZTFilter or time
													// filters
													boolean overRideColor = false;

													int pPos = sech.getPRESVarPos();
													int tPos = sech.getVarPos("TEMP", true);

													if (tPos >= 0 || pPos >= 0) {
														if (mPlotSpec.getTimeFilters() != null && mPlotSpec.getTimeFilters().size() > 0) {
															// Apply Time Filters
															for (JOATimeFilter jtFilter : mPlotSpec.getTimeFilters()) {
																if (!jtFilter.test(sh)) {
																	overRideColor = true;
																	break;
																}
															}
														}

														// apply ZT Filters
														boolean c1 = true;
														if (mPlotSpec.getZFilter() != null) {
															c1 = mPlotSpec.getZFilter().test(bh, pPos);
														}

														boolean c2 = true;
														if (c1 && mPlotSpec.getTFilter() != null) {
															c2 = mPlotSpec.getTFilter().test(bh, tPos);
														}

														if (!c1 || !c2) {
															overRideColor = true;
														}
													}

													if (overRideColor) {
														currColor = mPlotSpec.getFilteredOutColor();
													}

													// plot point
													g.setColor(currColor);
													JOAFormulas.plotSymbol(g, currSymbol, (int) x, (int) y, currSymbolSize);
												}
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
					OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.currElement();
					Section sech = (Section) of.getCurrSection();
					yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
					boolean canPlot = true;
					if (sech.mNumCasts == 0 || yPos == -1) {
						canPlot = false;
					}
					Station sh = (Station) sech.mStations.currElement();
					if (!sh.mUseStn) {
						canPlot = false;
					}

					if (colorByMetaData) {
						stnLat = sh.getLat();
						stnLon = sh.getLon();
						stnDate = sh.getDate();
						stnMonth = sh.getMonth() - 1;

						enhanceByMetadata = mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnLat))
						    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnLon))
						    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnDate))
						    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnMonth));

						if (isDateMetadata) {
							stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnDate);
						}
						else if (isMonthMetadata) {
							stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnMonth);
						}
						else if (isLatMetadata) {
							stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLat);
						}
						else if (isLonMetadata) {
							stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLon);
						}
					}

					if (canPlot) {
						for (int b = 0; b < sh.mNumBottles; b++) {
							enhanceObs = false;
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);
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

							for (int i = 0; i < mNumXAxes; i++) {
								currSymbol = mSymbol[i];
								currSymbolSize = mSymbolSize[i];
								xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel(), false);
								if (xPos == -1) {
									continue;
								}
								x = bh.mDValues[xPos];
								y = bh.mDValues[yPos];
								if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
									x = (x - mWinXOrigin[i]) * mWinXScale[i];
									y = (y - mWinYOrigin) * mWinYScale;

									// adjust for axes labels
									if (mPlotAxes && !reverseY) {
										x += PVPLEFT;
										y += PVPTOP;
									}
									else if (mPlotAxes && reverseY) {
										y = mHeightCurrWindow - y;
										x += PVPLEFT;
										y -= PVPBOTTOM * mNumXAxes;
									}
									else if (reverseY) {
										y = mHeightCurrWindow - y;
									}

									Color currColor = Color.black;
									if (mPlotSpec.isColorByCBParam()) {
										// get a color for this value
										if (colorByMetaData) {
											currColor = stnMetadataColor;
										}
										else {
											// get the value of the color variable at this point
											theVal = JOAFormulas.getValueOfColorVariable(mMainFileViewer, sech, bh);
											currColor = mMainFileViewer.mDefaultCB.getColor(theVal);
											if (mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(theVal))) {
												enhanceObs = true;
											}
										}
									}
									else if (mPlotSpec.isColorByConnectLineColor()) {
										currColor = mPlotSpec.getConnectStnColor(i);
									}

									if (enhanceByMetadata || enhanceObs) {
										if (JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL) {
											currSymbolSize *= 1.0 + (JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL_BY / 100.0);
										}
										else if (JOAConstants.DEFAULT_ENHANCE_REPLACE_CURRENT_SYMBOL) {
											currSymbolSize *= 1.0 + (JOAConstants.DEFAULT_ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY / 100.0);
											currSymbol = JOAConstants.DEFAULT_ENHANCE_CONTRASTING_SYMBOL;
										}

										if (JOAConstants.DEFAULT_ENHANCE_USE_CONTRASTING_COLOR) {
											currColor = JOAConstants.DEFAULT_ENHANCE_CONTRASTING_COLOR;
										}								// Determine override color if there is a ZTFilter or time
										// filters
										boolean overRideColor = false;

										int pPos = sech.getPRESVarPos();
										int tPos = sech.getVarPos("TEMP", true);

										if (tPos >= 0 || pPos >= 0) {
											if (mPlotSpec.getTimeFilters() != null && mPlotSpec.getTimeFilters().size() > 0) {
												// Apply Time Filters
												for (JOATimeFilter jtFilter : mPlotSpec.getTimeFilters()) {
													if (!jtFilter.test(sh)) {
														overRideColor = true;
														break;
													}
												}
											}

											// apply ZT Filters
											boolean c1 = true;
											if (mPlotSpec.getZFilter() != null) {
												c1 = mPlotSpec.getZFilter().test(bh, pPos);
											}

											boolean c2 = true;
											if (c1 && mPlotSpec.getTFilter() != null) {
												c2 = mPlotSpec.getTFilter().test(bh, tPos);
											}

											if (!c1 || !c2) {
												overRideColor = true;
											}
										}

										if (overRideColor) {
											currColor = mPlotSpec.getFilteredOutColor();
										}

										// plot point
										g.setColor(currColor);
										JOAFormulas.plotSymbol(g, currSymbol, (int) x, (int) y, currSymbolSize);
									}

								}
							}
						}
					}
				}
				else {
					// plot all the stations
					for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
						OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.elementAt(fc);

						for (int sec = 0; sec < of.mNumSections; sec++) {
							Section sech = (Section) of.getSection(sec);

							xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[0]].getVarLabel(), false);
							yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
							if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
								continue;
							}
							for (int stc = 0; stc < sech.mStations.size(); stc++) {
								Station sh = (Station) sech.mStations.elementAt(stc);
								if (!sh.mUseStn || (mAccumulateStns && sh.isSkipStn())) {
									continue;
								}

								if (colorByMetaData) {
									stnLat = sh.getLat();
									stnLon = sh.getLon();
									stnDate = sh.getDate();
									stnMonth = sh.getMonth() - 1;

									enhanceByMetadata = mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB
									    .getColorIndex(stnLat))
									    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnLon))
									    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnDate))
									    || mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(stnMonth));

									if (isDateMetadata) {
										stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnDate);
									}
									else if (isMonthMetadata) {
										stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnMonth);
									}
									else if (isLatMetadata) {
										stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLat);
									}
									else if (isLonMetadata) {
										stnMetadataColor = mMainFileViewer.mDefaultCB.getColor(stnLon);
									}
								}

								if (xPos >= 0 && yPos >= 0) {
									for (int b = 0; b < sh.mNumBottles; b++) {
										enhanceObs = false;
										Bottle bh = (Bottle) sh.mBottles.elementAt(b);
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

										for (int i = 0; i < mNumXAxes; i++) {
											currSymbol = mSymbol[i];
											currSymbolSize = mSymbolSize[i];
											xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel(), false);
											if (xPos == -1) {
												continue;
											}

											x = bh.mDValues[xPos];
											y = bh.mDValues[yPos];
											if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
												x = (x - mWinXOrigin[i]) * mWinXScale[i];
												y = (y - mWinYOrigin) * mWinYScale;

												// adjust for axes labels
												if (mPlotAxes && !reverseY) {
													x += PVPLEFT;
													y += PVPTOP;
												}
												else if (mPlotAxes && reverseY) {
													y = mHeightCurrWindow - y;
													x += PVPLEFT;
													y -= PVPBOTTOM * mNumXAxes;
												}
												else if (reverseY) {
													y = mHeightCurrWindow - y;
												}

												Color currColor = Color.black;
												if (mPlotSpec.isColorByCBParam()) {
													// get a color for this value
													if (colorByMetaData) {
														currColor = stnMetadataColor;
													}
													else {
														// get the value of the color variable at this point
														theVal = JOAFormulas.getValueOfColorVariable(mMainFileViewer, sech, bh);
														currColor = mMainFileViewer.mDefaultCB.getColor(theVal);
														if (mMainFileViewer.mDefaultCB.isColorEnhanced(mMainFileViewer.mDefaultCB.getColorIndex(theVal))) {
															enhanceObs = true;
														}
													}
												}
												else if (mPlotSpec.isColorByConnectLineColor()) {
													currColor = mPlotSpec.getConnectStnColor(i);
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
													}								// Determine override color if there is a ZTFilter or time
													// filters
													boolean overRideColor = false;

													int pPos = sech.getPRESVarPos();
													int tPos = sech.getVarPos("TEMP", true);

													if (tPos >= 0 || pPos >= 0) {
														if (mPlotSpec.getTimeFilters() != null && mPlotSpec.getTimeFilters().size() > 0) {
															// Apply Time Filters
															for (JOATimeFilter jtFilter : mPlotSpec.getTimeFilters()) {
																if (!jtFilter.test(sh)) {
																	overRideColor = true;
																	break;
																}
															}
														}

														// apply ZT Filters
														boolean c1 = true;
														if (mPlotSpec.getZFilter() != null) {
															c1 = mPlotSpec.getZFilter().test(bh, pPos);
														}

														boolean c2 = true;
														if (c1 && mPlotSpec.getTFilter() != null) {
															c2 = mPlotSpec.getTFilter().test(bh, tPos);
														}

														if (!c1 || !c2) {
															overRideColor = true;
														}
													}

													if (overRideColor) {
														currColor = mPlotSpec.getFilteredOutColor();
													}

													g.setColor(currColor);
													JOAFormulas.plotSymbol(g, currSymbol, (int) x, (int) y, currSymbolSize);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			// initialize the spot
			OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.currElement();
			Section sech = (Section) of.getCurrSection();
			Station sh = (Station) sech.mStations.currElement();
			Bottle bh = (Bottle) sh.mBottles.currElement();
			yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
			for (int i = 0; i < mNumXAxes; i++) {
				xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel(), false);
				x = JOAConstants.MISSINGVALUE;
				y = JOAConstants.MISSINGVALUE;
				if (xPos > -1 && yPos > -1) {
					x = bh.mDValues[xPos];
					y = bh.mDValues[yPos];
				}

				if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
					x = (int) ((x - mWinXOrigin[i]) * mWinXScale[i]);
					y = (int) ((y - mWinYOrigin) * mWinYScale);

					// adjust for axes labels
					if (mPlotAxes && !reverseY) {
						x += PVPLEFT;
						y += PVPTOP;
					}
					else if (mPlotAxes && reverseY) {
						y = mHeightCurrWindow - y;
						x += PVPLEFT;
						y -= PVPBOTTOM * mNumXAxes;
					}
					else if (reverseY) {
						y = mHeightCurrWindow - y;
					}
				}
				mObsMarker[i] = new ObsMarker((int) x, (int) y, JOAConstants.DEFAULT_CURSOR_SIZE);
			}
			g.setClip(0, 0, this.getSize().width, this.getSize().height);
		}
		
		private void plotOneStn(FileViewer fv, Graphics2D g, boolean colorByMetaData, boolean isDateMetadata,
				boolean isMonthMetadata, boolean isLatMetadata, boolean isLonMetadata, boolean reverseY) {
			Color stnMetadataColor = null;
			boolean enhanceByMetadata = false;
			
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.currElement();
			Section sech = (Section) of.getCurrSection();
			int yPos = sech.getVarPos(fv.mAllProperties[mYVarCode].getVarLabel(), false);
			boolean canPlot = true;
			if (sech.mNumCasts == 0 || yPos == -1) {
				canPlot = false;
			}
			Station sh = (Station) sech.mStations.currElement();
			if (!sh.mUseStn) {
				canPlot = false;
			}

			if (colorByMetaData) {
				double stnLat = sh.getLat();
				double stnLon = sh.getLon();
				GeoDate stnDate = sh.getDate();
				double stnMonth = sh.getMonth() - 1;

				enhanceByMetadata = fv.mDefaultCB.isColorEnhanced(fv.mDefaultCB.getColorIndex(stnLat))
				    || fv.mDefaultCB.isColorEnhanced(fv.mDefaultCB.getColorIndex(stnLon))
				    || fv.mDefaultCB.isColorEnhanced(fv.mDefaultCB.getColorIndex(stnDate))
				    || fv.mDefaultCB.isColorEnhanced(fv.mDefaultCB.getColorIndex(stnMonth));

				if (isDateMetadata) {
					stnMetadataColor = fv.mDefaultCB.getColor(stnDate);
				}
				else if (isMonthMetadata) {
					stnMetadataColor = fv.mDefaultCB.getColor(stnMonth);
				}
				else if (isLatMetadata) {
					stnMetadataColor = fv.mDefaultCB.getColor(stnLat);
				}
				else if (isLonMetadata) {
					stnMetadataColor = fv.mDefaultCB.getColor(stnLon);
				}
			}

			if (canPlot) {
				for (int b = 0; b < sh.mNumBottles; b++) {
					boolean enhanceObs = false;
					Bottle bh = (Bottle) sh.mBottles.elementAt(b);
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

					for (int i = 0; i < mNumXAxes; i++) {
						int currSymbol = mSymbol[i];
						int currSymbolSize = mSymbolSize[i];
						int xPos = sech.getVarPos(fv.mAllProperties[mXVarCode[i]].getVarLabel(), false);
						if (xPos == -1) {
							continue;
						}
						double x = bh.mDValues[xPos];
						double y = bh.mDValues[yPos];

						if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
							x = (x - mWinXOrigin[i]) * mWinXScale[i];
							y = (y - mWinYOrigin) * mWinYScale;

							// adjust for axes labels
							if (mPlotAxes && !reverseY) {
								x += PVPLEFT;
								y += PVPTOP;
							}
							else if (mPlotAxes && reverseY) {
								y = mHeightCurrWindow - y;
								x += PVPLEFT;
								y -= PVPBOTTOM * mNumXAxes;
							}
							else if (reverseY) {
								y = mHeightCurrWindow - y;
							}

							Color currColor = Color.black;
							if (mPlotSpec.isColorByCBParam()) {
								// get a color for this value
								if (colorByMetaData) {
									currColor = stnMetadataColor;
								}
								else {
									// get the value of the color variable at this point
									double theVal = JOAFormulas.getValueOfColorVariable(fv, sech, bh);
									currColor = fv.mDefaultCB.getColor(theVal);
									if (fv.mDefaultCB.isColorEnhanced(fv.mDefaultCB.getColorIndex(theVal))) {
										enhanceObs = true;
									}
								}
							}
							else if (mPlotSpec.isColorByConnectLineColor()) {
								currColor = mPlotSpec.getConnectStnColor(i);
							}

							// Determine override color if there is a ZTFilter or time
							// filters
							boolean overRideColor = false;

							int pPos = sech.getPRESVarPos();
							int tPos = sech.getVarPos("TEMP", true);

							if (tPos >= 0 || pPos >= 0) {
								if (mPlotSpec.getTimeFilters() != null && mPlotSpec.getTimeFilters().size() > 0) {
									// Apply Time Filters
									for (JOATimeFilter jtFilter : mPlotSpec.getTimeFilters()) {
										if (!jtFilter.test(sh)) {
											overRideColor = true;
											break;
										}
									}
								}

								// apply ZT Filters
								boolean c1 = true;
								if (mPlotSpec.getZFilter() != null) {
									c1 = mPlotSpec.getZFilter().test(bh, pPos);
								}

								boolean c2 = true;
								if (c1 && mPlotSpec.getTFilter() != null) {
									c2 = mPlotSpec.getTFilter().test(bh, tPos);
								}

								if (!c1 || !c2) {
									overRideColor = true;
								}
							}

							if (overRideColor) {
								currColor = mPlotSpec.getFilteredOutColor();
							}

							g.setColor(currColor);
							if (!enhanceObs && !enhanceByMetadata) {
								// plot point
								JOAFormulas.plotSymbol(g, currSymbol, x, y, currSymbolSize);
							}
						}
					}
				}
			}
		}

		public void plotStnLine(Graphics2D g, boolean flag, FileViewer fv) {
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.currElement();
			Section sech = (Section) of.getCurrSection();
			Station sh = (Station) sech.mStations.currElement();

			int x1, width, y1, height;
			if (mPlotAxes) {
				x1 = PVPLEFT;
				width = this.getSize().width - PVPLEFT - PVPRIGHT;
				y1 = PVPTOP;
				height = this.getSize().height - PVPTOP - (PVPBOTTOM * mNumXAxes);
			}
			else {
				x1 = 0;
				y1 = 0;
				width = this.getSize().width;
				height = this.getSize().height;
			}
			g.setClip(x1, y1, width, height);

			// plot the old line
//			if (oldStn != null && flag) {
//				plotLine(g, oldSec, oldStn);
//			}

			// plot the new line
			plotLine(g, sech, sh, fv);
//			oldStn = sh;
//			oldSec = sech;
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
				height = this.getSize().height - PVPTOP - (PVPBOTTOM * mNumXAxes);
			}
			else {
				x = 0;
				y = 0;
				width = this.getSize().width;
				height = this.getSize().height;
			}
			g.setClip(x, y, width, height);

			for (int i = 0; i < mNumXAxes; i++) {
				g.setColor(mPlotSpec.getConnectStnColor(i));
				// compute points on the boundary
				double x0 = mWinXPlotMin[i];
				double x1 = mWinXPlotMax[i];
				double y0 = mSlopes[i] * x0 + mIntercepts[i];
				double y1 = mSlopes[i] * x1 + mIntercepts[i];

				x0 = (x0 - mWinXOrigin[i]) * mWinXScale[i];
				y0 = (y0 - mWinYOrigin) * mWinYScale;

				// adjust for axes labels
				if (mPlotAxes && !reverseY) {
					x0 += PVPLEFT;
					y0 += PVPTOP;
				}
				else if (mPlotAxes && reverseY) {
					y0 = mHeightCurrWindow - y0;
					x0 += PVPLEFT;
					y0 -= PVPBOTTOM * mNumXAxes;
				}
				else if (reverseY) {
					y0 = mHeightCurrWindow - y0;
				}

				x1 = (x1 - mWinXOrigin[i]) * mWinXScale[i];
				y1 = (y1 - mWinYOrigin) * mWinYScale;

				// adjust for axes labels
				if (mPlotAxes && !reverseY) {
					x1 += PVPLEFT;
					y1 += PVPTOP;
				}
				else if (mPlotAxes && reverseY) {
					y1 = mHeightCurrWindow - y1;
					x1 += PVPLEFT;
					y1 -= PVPBOTTOM * mNumXAxes;
				}
				else if (reverseY) {
					y1 = mHeightCurrWindow - y1;
				}

				g.drawLine((int) x0, (int) y0, (int) x1, (int) y1);

				double ymin = mWinYPlotMin;
				double ymax = mWinYPlotMax;
				double xmin = mWinXPlotMin[i];
				double xmax = mWinXPlotMax[i];
				double offsetIntercept = mIntercepts[i] + 0;
				double xAtYmin = (ymin - offsetIntercept) / mSlopes[i];
				double xAtYmax = (ymax - offsetIntercept) / mSlopes[i];
				double xmid = 0;
				double ymid = 0;
				double deltaY = 0;
				double deltaX = 0;
				double xTranslation = 0.0;

				if (mSlopes[i] > 0) {
					xTranslation = -5.0;
					if (xAtYmin > xmin && xAtYmax < xmax) {
						xmid = (xAtYmin + xAtYmax) / 2.0;
						ymid = (ymin + ymax) / 2.0;

						UVCoordinate uvmax = transformToPlot(xAtYmax, ymax, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						UVCoordinate uvmin = transformToPlot(xAtYmin, ymin, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						deltaY = uvmin.getV() - uvmax.getV();
						deltaX = uvmax.getU() - uvmin.getU();
					}
					else if (xAtYmin > xmin && xAtYmax > xmax) {
						xmid = (xAtYmin + xmax) / 2.0;
						double yAtXMax = mSlopes[i] * xmax + offsetIntercept;
						ymid = (ymin + yAtXMax) / 2.0;

						UVCoordinate uvmax = transformToPlot(xmax, yAtXMax, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						UVCoordinate uvmin = transformToPlot(xAtYmin, ymin, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);

						deltaY = uvmin.getV() - uvmax.getV();
						deltaX = uvmax.getU() - uvmin.getU();
					}
					else if (xAtYmin < xmin && xAtYmax < xmax) {
						xTranslation = 5.0;
						double yAtXMin = mSlopes[i] * xmin + offsetIntercept;
						xmid = (xmin + xAtYmax) / 2.0;
						ymid = (yAtXMin + ymax) / 2.0;

						UVCoordinate uvmax = transformToPlot(xAtYmax, ymax, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						UVCoordinate uvmin = transformToPlot(xmin, yAtXMin, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);

						deltaY = uvmin.getV() - uvmax.getV();
						deltaX = uvmax.getU() - uvmin.getU();
					}
					else if (xAtYmin < xmin && xAtYmax > xmax) {
						double yAtXMax = mSlopes[i] * xmax + offsetIntercept;
						double yAtXMin = mSlopes[i] * xmin + offsetIntercept;
						xmid = (xmin + xmax) / 2.0;
						ymid = (yAtXMin + yAtXMax) / 2.0;

						UVCoordinate uvmax = transformToPlot(xmax, yAtXMax, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						UVCoordinate uvmin = transformToPlot(xmin, yAtXMin, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);

						deltaY = uvmin.getV() - uvmax.getV();
						deltaX = uvmax.getU() - uvmin.getU();
					}
				}
				else if (mSlopes[i] < 0.0) {
					if (xAtYmax > xmin && xAtYmin > xmax) {
						xmid = (xmax + xAtYmax) / 2.0;
						double yAtXMax = mSlopes[i] * xmax + offsetIntercept;
						ymid = (yAtXMax + ymax) / 2.0;

						UVCoordinate uvmin = transformToPlot(xAtYmax, ymax, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						UVCoordinate uvmax = transformToPlot(xmax, yAtXMax, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						deltaY = uvmin.getV() - uvmax.getV();
						deltaX = uvmax.getU() - uvmin.getU();
					}
					else if (xAtYmax > xmin && xAtYmin < xmax) {
						xmid = (xAtYmin + xAtYmax) / 2.0;
						ymid = (ymin + ymax) / 2.0;

						UVCoordinate uvmin = transformToPlot(xAtYmax, ymax, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						UVCoordinate uvmax = transformToPlot(xAtYmin, ymin, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						deltaY = uvmin.getV() - uvmax.getV();
						deltaX = uvmax.getU() - uvmin.getU();
					}
					else if (xAtYmax < xmin && xAtYmin < xmax) {
						xmid = (xmin + xAtYmin) / 2.0;
						double yAtXMin = mSlopes[i] * xmin + offsetIntercept;
						ymid = (ymin + yAtXMin) / 2.0;

						UVCoordinate uvmin = transformToPlot(xmin, yAtXMin, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						UVCoordinate uvmax = transformToPlot(xAtYmin, ymin, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						deltaY = uvmin.getV() - uvmax.getV();
						deltaX = uvmax.getU() - uvmin.getU();
					}
					else if (xAtYmax < xmin && xAtYmin > xmax) {
						xmid = (xmin + xmax) / 2.0;
						double yAtXMin = mSlopes[i] * xmin + offsetIntercept;
						double yAtXMax = mSlopes[i] * xmax + offsetIntercept;
						ymid = (yAtXMin + yAtXMax) / 2.0;

						UVCoordinate uvmin = transformToPlot(xmin, yAtXMin, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
						    reverseY);
						UVCoordinate uvmax = transformToPlot(xmax, yAtXMax, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale,
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

				String slopeStr = JOAFormulas.formatDouble(mSlopes[i], 3, false);
				String interceptStr = JOAFormulas.formatDouble(mIntercepts[i], 2, false);
				String r2String = JOAFormulas.formatDouble(mRSquares[i], 2, false);
				String valStr = "y=" + slopeStr + "x+" + interceptStr + " r^2=" + r2String;

				UVCoordinate uv = transformToPlot(xmid, ymid, mWinXOrigin[i], mWinXScale[i], mWinYOrigin, mWinYScale, reverseY);
				g.translate(xTranslation, -4.0);
				JOAFormulas.drawStyledString(valStr, uv.getU(), uv.getV(), g, angle, JOAConstants.DEFAULT_REGRESSION_FONT,
				    JOAConstants.DEFAULT_REGRESSION_FONT_SIZE, JOAConstants.DEFAULT_REGRESSION_FONT_STYLE,
				    JOAConstants.DEFAULT_REGRESSION_FONT_COLOR);

				g.translate(-xTranslation, 4.0);
				g.setClip(0, 0, 3000, 3000);
			}
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
				yy -= PVPBOTTOM * mNumXAxes;
			}
			else if (reverseY) {
				yy = mHeightCurrWindow - y;
			}

			return new UVCoordinate(xx, yy);
		}

		public void plotLine(Graphics2D g, Section sech, Station sh, FileViewer fv) {
			double x0, y0, x, y;
			boolean reverseY = fv.mAllProperties[mYVarCode].isReverseY();

			g.setStroke(new BasicStroke(JOAConstants.CONNECT_LINE_WIDTH));
			for (int i = 0; i < mNumXAxes; i++) {
				stnLine[i].reset();
				stnLineStarted[i] = false;
			}

			int yPos = sech.getVarPos(fv.mAllProperties[mYVarCode].getVarLabel(), false);
			if (yPos == -1) {
				yPos = mYVarCode;
			}
			
			int lastPlottedPoint = 0;
			for (int b = 0; b < sh.mNumBottles - 1; b++) {
				Bottle bh = (Bottle) sh.mBottles.elementAt(b);
				for (int i = 0; i < mNumXAxes; i++) {
					int xPos = sech.getVarPos(fv.mAllProperties[mXVarCode[i]].getVarLabel(), false);
					if (xPos == -1) {
						xPos = mXVarCode[0];
					}
					x0 = bh.mDValues[xPos];
					y0 = bh.mDValues[yPos];
					Bottle bh2 = (Bottle) sh.mBottles.elementAt(b + 1);
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
					
					boolean firstPtMissingSomething =  x0 == JOAConstants.MISSINGVALUE || y0 == JOAConstants.MISSINGVALUE;
					boolean secondPtMissingSomething =  x == JOAConstants.MISSINGVALUE || y == JOAConstants.MISSINGVALUE;
					boolean missingAnything = firstPtMissingSomething || secondPtMissingSomething;

					if (!missingAnything) {
						x = (x - mWinXOrigin[i]) * mWinXScale[i];
						y = (y - mWinYOrigin) * mWinYScale;
						x0 = (x0 - mWinXOrigin[i]) * mWinXScale[i];
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
							y -= PVPBOTTOM * mNumXAxes;
							y0 = mHeightCurrWindow - y0;
							x0 += PVPLEFT;
							y0 -= PVPBOTTOM * mNumXAxes;
						}
						else if (reverseY) {
							y = mHeightCurrWindow - y;
							y0 = mHeightCurrWindow - y0;
						}

						stnLine[i].moveTo((float) x0, (float) y0);
						stnLineStarted[i] = true;
						stnLine[i].lineTo((float) x, (float) y);
						lastPlottedPoint = i;
					}
					else if (mPlotSpec.isIgnoreMissingObs()) {
						// missing something
						if (!secondPtMissingSomething) {
							// connect from last point plotted to current point
							x = (x - mWinXOrigin[i]) * mWinXScale[i];
							y = (y - mWinYOrigin) * mWinYScale;
							// adjust for axes labels
							if (mPlotAxes && !reverseY) {
								x += PVPLEFT;
								y += PVPTOP;
							}
							else if (mPlotAxes && reverseY) {
								y = mHeightCurrWindow - y;
								x += PVPLEFT;
								y -= PVPBOTTOM * mNumXAxes;
							}
							else if (reverseY) {
								y = mHeightCurrWindow - y;
							}
							if (!stnLineStarted[i]) {
								stnLine[i].moveTo((float) x, (float) y);
								stnLineStarted[i] = true;
							}
							stnLine[i].lineTo((float) x, (float) y);
							lastPlottedPoint = i;
						}
					}
				}
			}

			for (int i = 0; i < mNumXAxes; i++) {
				g.setColor(mPlotSpec.getConnectStnColor(i));
				// stnLine[i].closePath();
				g.draw(stnLine[i]);
			}
		}

		@SuppressWarnings("deprecation")
		public void drawYAxis(Graphics2D g) {
			g.setColor(Color.black);
			int bottom = (int) mHeightCurrWindow - 1 * (PVPBOTTOM * mNumXAxes);
			int top = PVPTOP;
			int left = PVPLEFT;
			int right = (int) mWidthCurrWindow - PVPRIGHT;
			int leftMTicPos = PVPLEFT - 5 - 2;

			double yDiff = (mWinYPlotMax - mWinYPlotMin);
			int majorYTicks = (int) (yDiff / mYInc);
			double yInc = (double) (bottom - top) / (yDiff / mYInc);
			double minorYInc = yInc / ((double) mYTics + 1);

			// draw the Y axis
			g.drawLine(left, top, left, bottom);

			// draw the Y tic marks
			for (int i = 0; i <= majorYTicks; i++) {
				g.setColor(Color.black);
				int v = (int) (bottom - (i * yInc));
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
						int newV = (int) (v - (vv * minorYInc));
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
				int v = (int) (bottom - (i * yInc));
				String sTemp = JOAFormulas.roundNDecimals(myVal, numPlaces);
				System.out.println(numPlaces + " " + sTemp + " " + myVal);
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
			int hcenter = (leftMTicPos - maxStrLen)/2;
			int hpos = hcenter + fm.getHeight()/2;
			int height = this.getSize().height;
			JOAFormulas.drawStyledString(axisLabel, hpos, PVPTOP + ((height - PVPTOP - (PVPBOTTOM * mNumXAxes)) / 2) + width
			    / 2, g, 90, JOAConstants.DEFAULT_AXIS_LABEL_FONT, JOAConstants.DEFAULT_AXIS_LABEL_SIZE,
			    JOAConstants.DEFAULT_AXIS_LABEL_STYLE, JOAConstants.DEFAULT_AXIS_LABEL_COLOR);
		}

		@SuppressWarnings("deprecation")
		public void drawXAxis(Graphics2D g) {
			g.setColor(Color.black);
			int bott = (int) mHeightCurrWindow - (PVPBOTTOM * mNumXAxes);
			int top = PVPTOP;
			int left = PVPLEFT;
			int right = (int) mWidthCurrWindow - PVPRIGHT;

			for (int i = 0; i < mNumXAxes; i++) {
				int bottom = bott + 40 * i;
				double xDiff = (mWinXPlotMax[i] - mWinXPlotMin[i]);
				int majorXTicks = (int) (xDiff / mXInc[i]);
				double xInc = (double) (right - left) / (xDiff / mXInc[i]);
				double minorXInc = xInc / ((double) mXTics[i] + 1);

				// draw the X axis
				g.drawLine(left, bottom, right, bottom);

				// draw the X tic marks
				for (int ii = 0; ii <= majorXTicks; ii++) {
					g.setColor(Color.black);
					int h = (int) (left + (ii * xInc));
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
						for (int hh = 0; hh < mXTics[i] + 1; hh++) {
							int newH = (int) (h + (hh * minorXInc));
							if (mBG == Color.black) {
								if (i > 0) {
									g.setColor(Color.black);
									g.drawLine(newH, bottom + 1, newH, bottom - 1);
									g.setColor(Color.white);
									g.drawLine(newH, bottom + 1, newH, bottom - 1);
								}
								else {
									g.setColor(Color.black);
									g.drawLine(newH, bottom + 1, newH, bottom);
									g.setColor(Color.white);
									g.drawLine(newH, bottom + 1, newH, bottom - 1);
								}
							}
							else {
								if (i > 0) {
									g.setColor(Color.black);
									g.drawLine(newH, bottom + 1, newH, bottom - 1);
								}
								else {
									g.setColor(Color.black);
									g.drawLine(newH, bottom, newH, bottom - 1);
								}
							}
						}
					}

					// plot the grid
					if (mXGrid && i == 0) {
						g.setColor(mFG);
						g.drawLine(h, bottom, h, top);
					}
				}

				// complete the box
				if (i == 0) {
					g.setColor(Color.black);
					g.drawLine(left, top, right, top);
				}

				// set the X precision
				int numPlaces = JOAFormulas.GetDisplayPrecision(mXInc[i]);

				// label the x axis values
				// get the font metrics
				Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
				    JOAConstants.DEFAULT_AXIS_VALUE_SIZE);
				g.setFont(font);
				FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);

				double vOrigin = mWinXPlotMin[i];
				String sTemp = null;
				int maxv = fm.getHeight();
				int voffset = bottom + maxv;
				
				for (int ii = 0; ii <= majorXTicks; ii++) {
					double myVal = vOrigin + (ii * mXInc[i]);
					if (myVal == -0.0) {
						myVal = 0.0;
					}
					int h = (int) ((left) + (ii * xInc));
					
					sTemp = JOAFormulas.roundNDecimals(myVal, numPlaces);
					int len = fm.stringWidth(sTemp);
					JOAFormulas.drawStyledString(sTemp, h - (len / 2), voffset, g, 0.0, JOAConstants.DEFAULT_AXIS_VALUE_FONT,
					    JOAConstants.DEFAULT_AXIS_VALUE_SIZE, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
					    JOAConstants.DEFAULT_AXIS_VALUE_COLOR);
				}

				// add variable label
				int centerV = voffset + (((bottom + PVPBOTTOM) - voffset)/2);
				
				font = new Font(JOAConstants.DEFAULT_AXIS_LABEL_FONT, JOAConstants.DEFAULT_AXIS_LABEL_STYLE,
				    JOAConstants.DEFAULT_AXIS_LABEL_SIZE);
				g.setFont(font);
				fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
				Color textColor;
				if (mPlotSpec.isConnectObs() || mPlotSpec.isColorByConnectLineColor()) {
					textColor = mPlotSpec.getConnectStnColor(i);
				}
				else {
					textColor = JOAConstants.DEFAULT_AXIS_LABEL_COLOR;
				}
				int len = 0;
				String axisLabel = null;
				if (mMainFileViewer.mAllProperties[mXVarCode[i]].getUnits() != null
				    && mMainFileViewer.mAllProperties[mXVarCode[i]].getUnits().length() > 0) {
					axisLabel = mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel() + " ("
					    + mMainFileViewer.mAllProperties[mXVarCode[i]].getUnits() + ")";
					len = fm.stringWidth(axisLabel);
				}
				else {
					axisLabel = mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel();
					len = fm.stringWidth(axisLabel);
				}
				int maxLblHeight = fm.getHeight() - fm.getLeading() - 4;

				int hh = mWidthCurrWindow / 2 - (len / 2) + PVPRIGHT;
				int vv = centerV + maxLblHeight/2 ;
				JOAFormulas.drawStyledString(axisLabel, hh, vv, g, 0, JOAConstants.DEFAULT_AXIS_LABEL_FONT,
				    JOAConstants.DEFAULT_AXIS_LABEL_SIZE, JOAConstants.DEFAULT_AXIS_LABEL_STYLE, textColor);
				g.setColor(Color.black);
			}
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
			Bottle bh = (Bottle) inStn.mBottles.currElement();
			int yPos = inSec.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
//			Graphics2D g = (Graphics2D) getGraphics();
			double x, y;

			if (mAccumulateStns) {
				inStn.setSkipStn(false);
			}

			if (mPlotOnlyCurrStn || mAccumulateStns) {
				this.invalidate();
				Graphics2D g = (Graphics2D) getGraphics();
				this.paintComponent(g);
			}
			else {
				if (mConnectObs) {
					repaint();
				}
			}

			for (int i = 0; i < mNumXAxes; i++) {
				// browsing tied to first axis
				int xPos = inSec.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel(), false);

				if (xPos == -1 || yPos == -1) {
					x = JOAConstants.MISSINGVALUE;
					y = JOAConstants.MISSINGVALUE;
				}
				else {
					x = bh.mDValues[xPos];
					y = bh.mDValues[yPos];
				}

				if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
					x = (int) ((x - mWinXOrigin[i]) * mWinXScale[i]);
					y = (int) ((y - mWinYOrigin) * mWinYScale);

					// adjust for axes labels
					if (mPlotAxes && !reverseY) {
						x += PVPLEFT;
						y += PVPTOP;
					}
					else if (mPlotAxes && reverseY) {
						y = mHeightCurrWindow - y;
						x += PVPLEFT;
						y -= PVPBOTTOM * mNumXAxes;
					}
					else if (reverseY) {
						y = mHeightCurrWindow - y;
					}
				}

				if (mObsMarker[i] != null) {
					mObsMarker[i].setNewPos((int) x, (int) y);
				}
			}
			paintImmediately(0, 0, 2000, 2000);
		}

		public void findByXY(int x, int y) {
			boolean found = false;
			OpenDataFile foundFile = null;
			Section foundSection = null;
			Station foundStation = null;
			Bottle foundBottle = null;
			OpenDataFile oldof = (OpenDataFile) mMainFileViewer.mOpenFiles.currElement();
			Section oldsech = (Section) oldof.getCurrSection();
			Station oldsh = (Station) oldsech.mStations.currElement();
			Bottle oldBottle = (Bottle) oldsh.mBottles.currElement();
			boolean reverseY = mMainFileViewer.mAllProperties[mYVarCode].isReverseY();
			int minY = getMinY();
			int maxY = getMaxY();
			int minX = getMinX();
			int maxX = getMaxX();

			if (!mSpotable) { return; }

			// search for a matching observation
			double minOffset = 10000.0;
			for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles && !found; fc++) {
				OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections && !found; sec++) {
					Section sech = (Section) of.getSection(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}
					int yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
					if (yPos == -1) {
						continue;
					}

					for (int stc = 0; stc < sech.mStations.size() && !found; stc++) {
						Station sh = (Station) sech.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							continue;
						}

						for (int b = 0; b < sh.mNumBottles && !found; b++) {
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);
							for (int ax = 0; ax < mNumXAxes; ax++) {
								int xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[ax]].getVarLabel(), false);
								if (xPos == -1) {
									continue;
								}

								double xx = bh.mDValues[xPos];
								double yy = bh.mDValues[yPos];
								if (xx != JOAConstants.MISSINGVALUE && yy != JOAConstants.MISSINGVALUE) {
									xx = (xx - mWinXOrigin[ax]) * mWinXScale[ax];
									yy = (yy - mWinYOrigin) * mWinYScale;

									// adjust for axes labels
									if (mPlotAxes && !reverseY) {
										xx += PVPLEFT;
										yy += PVPTOP;
									}
									else if (mPlotAxes && reverseY) {
										yy = mHeightCurrWindow - yy;
										xx += PVPLEFT;
										yy -= PVPBOTTOM * mNumXAxes;
									}
									else if (reverseY) {
										yy = mHeightCurrWindow - yy;
									}
								}
								else {
									continue;
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

								if (mMainFileViewer.mObsFilterActive && mMainFileViewer.mCurrObsFilter.isShowOnlyMatching() && !keepBottle) {
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

			OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.currElement();
			Section sech = (Section) of.getCurrSection();
			Station sh = (Station) sech.mStations.currElement();

			// find new observation
			found = false;
			switch (direction.intValue()) {
				case 1: // JOAConstants.NEXTSTN:
					// go to next station
					foundStation = (Station) sech.mStations.nextElement();
					if (foundStation == null) {
						// go to next section
						foundSection = (Section) of.mSections.nextElement();
						foundFile = of;
						if (foundSection != null) {
							foundSection.mStations.setCurrElementToFirst();
							foundStation = (Station) foundSection.mStations.currElement();
							foundBottle = JOAFormulas.findBottleByPres(mMainFileViewer, foundStation);
							found = true;
							if (foundBottle != null) {
								found = true;
							}
						}
						else {
							// look in next file
							foundFile = (OpenDataFile) mMainFileViewer.mOpenFiles.nextElement();

							if (foundFile != null) {
								foundSection = (Section) foundFile.getCurrSection();
								foundStation = (Station) foundSection.mStations.currElement();
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
					foundStation = (Station) sech.mStations.prevElement();
					if (foundStation == null) {
						// go to next section
						foundSection = (Section) of.mSections.prevElement();
						foundFile = of;

						if (foundSection != null) {
							foundSection.mStations.setCurrElementToLast();
							foundStation = (Station) foundSection.mStations.currElement();
							foundBottle = JOAFormulas.findBottleByPres(mMainFileViewer, foundStation);
							found = true;
							if (foundBottle != null) {
								found = true;
							}
						}
						else {
							// look in next file
							foundFile = (OpenDataFile) mMainFileViewer.mOpenFiles.prevElement();

							if (foundFile != null) {
								foundSection = (Section) foundFile.getCurrSection();
								foundStation = (Station) foundSection.mStations.currElement();
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
					foundBottle = (Bottle) sh.mBottles.nextElement();

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
					foundBottle = (Bottle) sh.mBottles.prevElement();

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
			XYPlotSpecification newPlotSpec = ((ConfigurePvPPlotDC) d).createPlotSpec();
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

			mNumXAxes = newPlotSpec.getNumXAxes();
			for (int i = 0; i < mNumXAxes; i++) {
				mXVarCode[i] = newPlotSpec.getXVarCode(i);
				mXTics[i] = newPlotSpec.getXTics(i);
				mWinXPlotMin[i] = newPlotSpec.getWinXPlotMin(i);
				mWinXPlotMax[i] = newPlotSpec.getWinXPlotMax(i);
				mXInc[i] = newPlotSpec.getXInc(i);
			}

			mYVarCode = newPlotSpec.getYVarCode();
			mWinTitle = newPlotSpec.getWinTitle();
			mIncludeCBAR = newPlotSpec.isIncludeCBAR();
			mIncludeObsPanel = newPlotSpec.isIncludeObsPanel();
			mConnectObs = newPlotSpec.isConnectObs();
			mXGrid = newPlotSpec.isXGrid();
			mYGrid = newPlotSpec.isYGrid();
			mSymbol = newPlotSpec.getSymbols();
			mSymbolSize = newPlotSpec.getSymbolSizes();
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

			if (mPlotSpec.isAccumulateStns() != mAccumulateStns) {
				// state of paint has changed
				boolean state = false;
				if (mAccumulateStns) {
					// adding accumulation
					state = true;
				}
				// set the skip flag for all stns
				for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
					OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.elementAt(fc);

					for (int sec = 0; sec < of.mNumSections; sec++) {
						Section sech = (Section) of.getSection(sec);

						for (int stc = 0; stc < sech.mStations.size(); stc++) {
							Station sh = (Station) sech.mStations.elementAt(stc);
							sh.setSkipStn(state);
						}
					}
				}
			}

			mPlotSpec = new XYPlotSpecification(newPlotSpec);

			// resize the window if necessary
			if (mColorBarLegend != null && ((ConfigurePvPPlotDC) d).removingColorBar()) {
				// remove existing color bar component (if there is one)
				mContents.remove(mColorBarLegend);
				mColorBarLegend = null;
				mFrame.setSize(mFrame.getSize().width - 100, mFrame.getSize().height);
			}
			else if (((ConfigurePvPPlotDC) d).addingColorBar()) {
				mColorBarLegend = new ColorBarPanel(mFrame, mMainFileViewer, mMainFileViewer.mDefaultCB, mFG,
				    JOAConstants.DEFAULT_FRAME_COLOR, false, false);
				mColorBarLegend.setEnhanceable(true);
				mContents.add("East", mColorBarLegend);
				mFrame.setSize(mFrame.getSize().width + 100, mFrame.getSize().height);
				mFrame.validate();
			}

			if (mCurrObsPanel != null && ((ConfigurePvPPlotDC) d).removingObsBrowser()) {
				// remove existing color bar component (if there is one)
				mContents.remove(mObsScroller);
				mObsScroller = null;
				mCurrObsPanel = null;
				mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height - 40);
			}
			else if (((ConfigurePvPPlotDC) d).addingObsBrowser()) {
				mCurrObsPanel = new CurrentObsPanel2(mMainFileViewer, mThis);
				mObsScroller = new JScrollPane(mCurrObsPanel);
				mObsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				mObsScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				mContents.add("South", mObsScroller);
				mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height + 40);
				mFrame.validate();
			}

			mOffScreen = null;
			paintComponent(this.getGraphics());
		}

		private void handleOKorApply() {

		}

		// Cancel button
		public void dialogCancelled(JDialog d) {
			mPlotSpec = ((ConfigurePvPPlotDC) d).getOrigPlotSpec();
			mWidth = mPlotSpec.getWidth();
			mHeight = mPlotSpec.getHeight();
			mFG = mPlotSpec.getFGColor();
			mBG = mPlotSpec.getBGColor();
			mMainFileViewer = mPlotSpec.getFileViewer().elementAt(0);

			mNumXAxes = mPlotSpec.getNumXAxes();
			for (int i = 0; i < mNumXAxes; i++) {
				mXVarCode[i] = mPlotSpec.getXVarCode(i);
				mXTics[i] = mPlotSpec.getXTics(i);
				mWinXPlotMin[i] = mPlotSpec.getWinXPlotMin(i);
				mWinXPlotMax[i] = mPlotSpec.getWinXPlotMax(i);
				mXInc[i] = mPlotSpec.getXInc(i);
			}

			mYVarCode = mPlotSpec.getYVarCode();
			mWinTitle = mPlotSpec.getWinTitle();
			mIncludeCBAR = mPlotSpec.isIncludeCBAR();
			mIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
			mConnectObs = mPlotSpec.isConnectObs();
			mXGrid = mPlotSpec.isXGrid();
			mYGrid = mPlotSpec.isYGrid();
			mSymbol = mPlotSpec.getSymbols();
			mSymbolSize = mPlotSpec.getSymbolSizes();
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

			if (((ConfigurePvPPlotDC) d).mLayoutChanged) {
				if (mIncludeCBAR && !((ConfigurePvPPlotDC) d).mOriginalIncludeColorBar) {
					// add the color legend if it has been removed
					mColorBarLegend = new ColorBarPanel(mFrame, mMainFileViewer, mMainFileViewer.mDefaultCB, mFG,
					    JOAConstants.DEFAULT_FRAME_COLOR, false, false);
					mColorBarLegend.setEnhanceable(true);
					mContents.add("East", mColorBarLegend);
					mFrame.setSize(mFrame.getSize().width + 100, mFrame.getSize().height);
				}
				else if (!mIncludeCBAR && ((ConfigurePvPPlotDC) d).mOriginalIncludeColorBar) {
					// remove the color legend if it has been added
					mContents.remove(mColorBarLegend);
					mColorBarLegend = null;
					mFrame.setSize(mFrame.getSize().width - 100, mFrame.getSize().height);
				}
			}

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
			XYPlotSpecification newPlotSpec = ((ConfigurePvPPlotDC) d).createPlotSpec();
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

			mNumXAxes = newPlotSpec.getNumXAxes();
			for (int i = 0; i < mNumXAxes; i++) {
				mXVarCode[i] = newPlotSpec.getXVarCode(i);
				mXTics[i] = newPlotSpec.getXTics(i);
				mWinXPlotMin[i] = newPlotSpec.getWinXPlotMin(i);
				mWinXPlotMax[i] = newPlotSpec.getWinXPlotMax(i);
				mXInc[i] = newPlotSpec.getXInc(i);
			}

			mYVarCode = newPlotSpec.getYVarCode();
			mWinTitle = newPlotSpec.getWinTitle();
			mIncludeCBAR = newPlotSpec.isIncludeCBAR();
			mIncludeObsPanel = newPlotSpec.isIncludeObsPanel();
			mConnectObs = newPlotSpec.isConnectObs();
			mXGrid = newPlotSpec.isXGrid();
			mYGrid = newPlotSpec.isYGrid();
			mSymbol = newPlotSpec.getSymbols();
			mSymbolSize = newPlotSpec.getSymbolSizes();
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

			if (mPlotSpec.isAccumulateStns() != mAccumulateStns) {
				// state of paint has changed
				boolean state = false;
				if (mAccumulateStns) {
					// adding accumulation
					state = true;
				}
				// set the skip flag for all stns
				for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
					OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.elementAt(fc);

					for (int sec = 0; sec < of.mNumSections; sec++) {
						Section sech = (Section) of.getSection(sec);

						for (int stc = 0; stc < sech.mStations.size(); stc++) {
							Station sh = (Station) sech.mStations.elementAt(stc);
							sh.setSkipStn(state);
						}
					}
				}
			}

			mPlotSpec = new XYPlotSpecification(newPlotSpec);

			// resize the window if necessary
			int widthAdjustment = 0;
			int heightAdjustment = 0;
			if (mColorBarLegend != null && ((ConfigurePvPPlotDC) d).removingColorBar()) {
				// remove existing color bar component (if there is one)
				mContents.remove(mColorBarLegend);
				mColorBarLegend = null;
				widthAdjustment -= 100;
				((ConfigurePvPPlotDC) d).removedColorLegend();
			}
			else if (((ConfigurePvPPlotDC) d).addingColorBar()) {
				mColorBarLegend = new ColorBarPanel(mFrame, mMainFileViewer, mMainFileViewer.mDefaultCB, mFG,
				    JOAConstants.DEFAULT_FRAME_COLOR, false, false);
				mColorBarLegend.setEnhanceable(true);
				mContents.add("East", mColorBarLegend);
				widthAdjustment += 100;
				((ConfigurePvPPlotDC) d).addedColorLegend();
			}

			if (mCurrObsPanel != null && ((ConfigurePvPPlotDC) d).removingObsBrowser()) {
				// remove existing color bar component (if there is one)
				mContents.remove(mObsScroller);
				mObsScroller = null;
				mCurrObsPanel = null;
				heightAdjustment -= 40;
				((ConfigurePvPPlotDC) d).removedObsBrowser();
			}
			else if (((ConfigurePvPPlotDC) d).addingObsBrowser()) {
				mCurrObsPanel = new CurrentObsPanel2(mMainFileViewer, mThis);
				mObsScroller = new JScrollPane(mCurrObsPanel);
				mObsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				mObsScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				mContents.add("South", mObsScroller);
				heightAdjustment += 40;
				((ConfigurePvPPlotDC) d).addedObsBrowser();
			}

			if (widthAdjustment != 0 || heightAdjustment != 0) {
				mFrame.setSize(mFrame.getSize().width + widthAdjustment, mFrame.getSize().height + heightAdjustment);
				widthAdjustment = 0;
				heightAdjustment = 0;
				mFrame.validate();
			}

			mOffScreen = null;
			paintComponent(this.getGraphics());
		}
	}

	public RubberbandPanel getPanel() {
		return mXYPlot;
	}

	public boolean inWindow(float x, float y, int axis) {
		if (x > mWinXPlotMax[axis] || x < mWinXPlotMin[axis]) { return false; }

		if (y > mWinYPlotMax || y < mWinYPlotMin) { return false; }
		return true;
	}

	private void computeRegressions() {
		// compute linear regressions for all plotted parameters
		double[] sumx = new double[10];
		double[] sumy = new double[10];
		double[] sumx2 = new double[10];
		int[] n = new int[10];
		double[] xbar = new double[10];
		double[] ybar = new double[10];
		double[] xxbar = new double[10];
		double[] yybar = new double[10];
		double[] xybar = new double[10];

		for (int i = 0; i < 10; i++) {
			sumx[i] = 0.0;
			sumy[i] = 0.0;
			sumx2[i] = 0.0;
			n[i] = 0;
			xbar[i] = Double.NaN;
			ybar[i] = Double.NaN;
			xxbar[i] = 0.0;
			yybar[i] = 0.0;
			xybar[i] = 0.0;
		}

		// pass #1
		for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section) of.getSection(sec);

				int xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[0]].getVarLabel(), false);
				int yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
				if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
					continue;
				}
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					if (xPos >= 0 && yPos >= 0) {
						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);
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

							for (int i = 0; i < mNumXAxes; i++) {
								xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel(), false);
								if (xPos == -1) {
									continue;
								}

								float x = bh.mDValues[xPos];
								float y = bh.mDValues[yPos];
								if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
									if (mClipToWindow && !inWindow(x, y, i)) {
										continue;
									}
									sumx[i] += x;
									sumx2[i] += x * x;
									sumy[i] += y;
									n[i]++;
								}
							}
						}
					}
				}
			}
		}

		double[][] tempX = null;
		double[][] tempY = null;
		int[] ic = null;

		tempX = new double[10][];
		tempY = new double[10][];
		ic = new int[10];

		for (int i = 0; i < 10; i++) {
			if (n[i] > 0) {
				xbar[i] = sumx[i] / n[i];
				ybar[i] = sumy[i] / n[i];
				tempX[i] = new double[n[i]];
				tempY[i] = new double[n[i]];
				ic[i] = 0;
			}
		}

		// pass #2
		for (int fc = 0; fc < mMainFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) mMainFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section) of.getSection(sec);

				int xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[0]].getVarLabel(), false);
				int yPos = sech.getVarPos(mMainFileViewer.mAllProperties[mYVarCode].getVarLabel(), false);
				if (sech.mNumCasts == 0 || xPos == -1 || yPos == -1) {
					continue;
				}
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					if (xPos >= 0 && yPos >= 0) {
						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);
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

							for (int i = 0; i < mNumXAxes; i++) {
								xPos = sech.getVarPos(mMainFileViewer.mAllProperties[mXVarCode[i]].getVarLabel(), false);
								if (xPos == -1) {
									continue;
								}

								float x = bh.mDValues[xPos];
								float y = bh.mDValues[yPos];
								if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
									if (mClipToWindow && !inWindow(x, y, i)) {
										continue;
									}
									tempX[i][ic[i]] = x;
									tempY[i][ic[i]] = y;
									ic[i]++;
									xxbar[i] += (x - xbar[i]) * (x - xbar[i]);
									yybar[i] += (y - ybar[i]) * (y - ybar[i]);
									xybar[i] += (x - xbar[i]) * (y - ybar[i]);
								}
							}
						}
					}
				}
			}
		}

		// compute regression line coeffs
		for (int i = 0; i < 10; i++) {
			if (n[i] > 0) {
				mSlopes[i] = xybar[i] / xxbar[i];
				mIntercepts[i] = ybar[i] - mSlopes[i] * xbar[i];
			}
		}

		// analyze results
		for (int i = 0; i < 10; i++) {
			int df = n[i] - 2;
			double rss = 0.0; // residual sum of squares
			double ssr = 0.0; // regression sum of squares
			for (int j = 0; j < n[i]; j++) {
				double fit = mSlopes[i] * tempX[i][j] + mIntercepts[i];
				rss += (fit - tempY[i][j]) * (fit - tempY[i][j]);
				ssr += (fit - ybar[i]) * (fit - ybar[i]);
			}
			mRSquares[i] = ssr / yybar[i];
			svar[i] = rss / df;
			svar1[i] = svar[i] / xxbar[i];
			svar0[i] = svar[i] / n[i] + xbar[i] * xbar[i] * svar1[i];
		}
	}
}

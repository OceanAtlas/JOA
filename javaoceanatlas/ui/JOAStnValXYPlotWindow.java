/*
 * $Id: JOAStnValPlotWindow.java,v 1.21 2005/09/23 14:51:24 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.events.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import java.awt.font.*;
import java.awt.print.*;
import java.awt.geom.*;
import com.visualtek.png.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class JOAStnValXYPlotWindow extends JOAWindow implements ActionListener, DataAddedListener,
    StnFilterChangedListener, PrefsChangedListener, WindowsMenuChangedListener, ConfigurableWindow {
	public static int SECLEFT = 70;
	public static int SECRIGHT = 20;
	public static int SECTOP = 35;
	public static int SECBOTTOM = 50;
	protected double mXScale, mYScale, mXOrigin, mYOrigin;
	protected double mWinXPlotMax;
	protected double mWinYPlotMax;
	protected double mWinXPlotMin;
	protected double mWinYPlotMin;
	protected int mSymbolSize;
	protected int mYStnVarCode, mXStnVarCode;
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
	protected Color mFG, mBG;
	protected boolean mFirst = true;
	protected Container mContentPane = null;
	protected JOAStnValXYPlotWindow mThisFrame = null;
	protected int mWidth = 400;
	protected int mHeight = 400;
	private StnPlotPanel mStnPlotPanel = null;
	protected int mSymbol;
	protected StnPlotSpecification mPlotSpec = null;
	protected Container mContents = null;
	protected boolean mPrinting = false;
	protected JFrame mParent;
	protected JOAWindow mFrame;
	protected double pixelsPerUnit = 1;
	protected String mXStnVarName = null;
	protected String mXStnVarUnits = null;
	protected String mYStnVarName = null;
	protected String mYStnVarUnits = null;
	protected boolean mReverseY;
	protected boolean mHitShallow = false, mHitDeep = false, mHitBoth = false;
	ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	SmallIconButton mLinearRegressionBtn = null;
	double mSlope;
	double mIntercept;
	double mRSquare;
	double svar;
	double svar1;
	double svar0;
	private boolean mPlotRegress = false;
	BasicStroke lw2 = new BasicStroke(2);
	private boolean mClipToWindow = false;

	public JOAStnValXYPlotWindow(StnPlotSpecification ps, JFrame parent) {
		super(true, true, true, true, true, ps);
		mPlotSpec = ps;
		mParent = parent;
		mFrame = this;
		mFG = ps.getFGColor();
		mBG = ps.getBGColor();
		mFileViewer = ps.getFileViewer();
		mYStnVarCode = ps.getYStnVarCode();
		mXStnVarCode = ps.getXStnVarCode();
		mWinTitle = ps.getWinTitle();
		mYGrid = ps.isYGrid();
		mXGrid = ps.isXGrid();
		mSymbolSize = ps.getSymbolSize();
		mYTics = ps.getYTics();
		mXTics = ps.getXTics();
		mReverseY = mPlotSpec.isReverseY();

		// axes ranges
		mWinXPlotMin = ps.getWinXPlotMin();
		mWinXPlotMax = ps.getWinXPlotMax();
		mXInc = ps.getXInc();

		mWinYPlotMin = ps.getWinYPlotMin();
		mWinYPlotMax = ps.getWinYPlotMax();
		mYInc = ps.getYInc();

		mSectionType = ps.getSectionType();
		mSymbol = ps.getSymbol();

		init();
	}

	public Dimension getPreferredSize() {
		return new Dimension(mWidth, mHeight);
	}

	protected void init() {
		mThisFrame = this;
		mContents = this.getContentPane();

		mStnPlotPanel = new StnPlotPanel();
		mContents.add("Center", mStnPlotPanel);

		// add the toolbar
		mToolBar = new JOAJToolBar();
		JOAJToggleButton lockTool = new JOAJToggleButton(new ImageIcon(getClass().getResource("images/lock_open.gif")),
		    true);
		try {
			mLinearRegressionBtn = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas")
			    .getResource("images/Linear_regress.png")));
		}
		catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		lockTool.setSelectedIcon(new ImageIcon(getClass().getResource("images/lock_closed.gif")));
		lockTool.setToolTipText(b.getString("kLockPlot"));
		lockTool.setSelected(false);
		lockTool.setActionCommand("lock");
		lockTool.addActionListener(this);
		mToolBar.add(lockTool);
		mToolBar.add(new JLabel("  "));

		mLinearRegressionBtn
		    .setToolTipText("Toggle Regression Line (holding option/alt key restricts regression to current plot domain)");
		mLinearRegressionBtn.setActionCommand("toggleregress");
		mLinearRegressionBtn.addActionListener(this);
		mToolBar.add(mLinearRegressionBtn);

		OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(0);
		Section sech = (Section)of.mSections.elementAt(0);
		mYStnVarName = sech.getStnVar(mYStnVarCode);
		mXStnVarName = sech.getStnVar(mXStnVarCode);

		// make an array of y labels
		String[] ylabels = new String[1];
		int[] yprecs = new int[1];
		ylabels[0] = new String(mYStnVarName);
		yprecs[0] = 3;

		// make an array of y labels
		String[] xlabels = new String[1];
		int[] xprecs = new int[1];
		xlabels[0] = new String(mXStnVarName);
		xprecs[0] = 3;

		ValueToolbar mValToolBar = new ValueToolbar(mStnPlotPanel, xlabels, xprecs, ylabels, yprecs);
		mToolBar.add(mValToolBar);
		double[] xvals = new double[1];
		xvals[0] = Double.NaN;
		double[] yvals = new double[1];
		yvals[0] = Double.NaN;
		mValToolBar.setLocation(xvals, yvals);
		mContents.add(mToolBar, "North");

		getRootPane().registerKeyboardAction(new RightListener((Object)mStnPlotPanel, mStnPlotPanel.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new LeftListener((Object)mStnPlotPanel, mStnPlotPanel.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new UpListener((Object)mStnPlotPanel, mStnPlotPanel.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new DownListener((Object)mStnPlotPanel, mStnPlotPanel.getClass()),
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

		if (mWinTitle == null || mWinTitle.length() == 0) {
			this.setTitle("Station Value XY Plot");
		}
		else {
			this.setTitle(mWinTitle);
		}
		mFileViewer.addDataAddedListener(this);
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
		mFileViewer.removeDataAddedListener((DataAddedListener)mThisFrame);
		mFileViewer.removeStnFilterChangedListener((StnFilterChangedListener)mThisFrame);
		PowerOceanAtlas.getInstance().removePrefsChangedListener((PrefsChangedListener)mThisFrame);
		mFileViewer.removeObsChangedListener((ObsChangedListener)mStnPlotPanel);
		PowerOceanAtlas.getInstance().removeWindowsMenuChangedListener((WindowsMenuChangedListener)mThisFrame);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("toggleregress")) {
			mPlotRegress = !mPlotRegress;
			if (e.getModifiers() == 24) {
				mClipToWindow = !mClipToWindow;
				mStnPlotPanel.invalidate();
			}
			mStnPlotPanel.revalidate();
			mStnPlotPanel.repaint();
		}
		else if (cmd.equals("lock")) {
			mWindowIsLocked = !mWindowIsLocked;
			if (!mWindowIsLocked) {
				// are now unlocking the window
				this.setSize(this.getSize().width + 1, this.getSize().height);
				this.setSize(this.getSize().width, this.getSize().height);

				// if (mCurrObsPanel != null)
				// mCurrObsPanel.setLocked(false);

				mStnPlotPanel.invalidate();
				mStnPlotPanel.validate();
				this.invalidate();
				this.validate();
				mStnPlotPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				this.setResizable(true);
			}
			else {
				mStnPlotPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				this.setResizable(false);

				// if (mCurrObsPanel != null)
				// mCurrObsPanel.setLocked(true);
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
				JOAConstants.DEFAULT_PRINTERJOB.setPrintable(mStnPlotPanel, JOAConstants.DEFAULT_PAGEFORMAT);
				try {
					JOAConstants.DEFAULT_PRINTERJOB.print();
				}
				catch (PrinterException ex) {
				}
			}
		}
		else {
			mFileViewer.doCommand(cmd, mFrame);
		}
	}

	public void showConfigDialog() {
		// show configuration dialog
		mStnPlotPanel.showConfigDialog();
	}

	public void saveAsPNG() {
		class BasicThread extends Thread {
			// ask for filename
			public void run() {
				Image image = mStnPlotPanel.makeOffScreen((Graphics2D)mStnPlotPanel.getGraphics(), true, true);
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
						JOAConstants.LogFileStream.writeBytes("Saved Plot:" + mParent.getTitle() + " as " + path + "\n");
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

	public void dataAdded(DataAddedEvent evt) {
		if (mWindowIsLocked) { return; }
		// // rescale the x axis to the total distance range
		// double tempXMax = mFileViewer.mTotMercDist * 1.852;
		// Triplet newRange = JOAFormulas.GetPrettyRange(0, tempXMax);
		// mWinXPlotMin = newRange.getVal1();
		// mWinXPlotMax = newRange.getVal2();
		// mXInc = newRange.getVal3();
		// mStnPlotPanel.invalidate();
		// mStnPlotPanel.setSize(this.getSize().width + 1, this.getSize().height);
		// mStnPlotPanel.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
	}

	public void prefsChanged(PrefsChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		mStnPlotPanel.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
		mBG = JOAConstants.DEFAULT_CONTENTS_COLOR;
		mPlotSpec.setBGColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
		mStnPlotPanel.invalidate();
		mStnPlotPanel.setSize(this.getSize().width + 1, this.getSize().height);
		mStnPlotPanel.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
	}

	public void stnFilterChanged(StnFilterChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		mStnPlotPanel.invalidate();
		mStnPlotPanel.setSize(this.getSize().width + 1, this.getSize().height);
		mStnPlotPanel.setSize(this.getSize().width, this.getSize().height);
		this.invalidate();
		this.validate();
	}

	public void setXScale(int width) {
		mWidthCurrWindow = width;
		mScaleWidth = width;
		mScaleWidth -= (SECLEFT + SECRIGHT);
		mWinXScale = mScaleWidth / (mWinXPlotMax - mWinXPlotMin);
		mWinXOrigin = mWinXPlotMin;
	}

	public void setYScale(int height) {
		mHeightCurrWindow = height;
		mScaleHeight = height;
		mScaleHeight -= (SECBOTTOM + SECTOP);
		mWinYScale = -mScaleHeight / (mWinYPlotMax - mWinYPlotMin);
		mWinYOrigin = mWinYPlotMax;
	}

	private class StnPlotPanel extends RubberbandPanel implements ObsChangedListener, DialogClient, ActionListener,
	    Printable {
		Image mOffScreen = null;
		private Rubberband rbRect;
		DialogClient mDialogClient = null;
		private JPopupMenu mPopupMenu = null;
		private Rectangle mSelectionRect = new Rectangle(0, 0, 0, 0);
		BasicStroke lw2 = new BasicStroke(2);
		BasicStroke lw4 = new BasicStroke(4);
		private Rectangle oldRect = new Rectangle(0, 0, 0, 0);

		public StnPlotPanel() {
			this.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
			mFileViewer.addObsChangedListener(this);
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
				double xOffset;
				double yOffset;

				// compute scale factor
				double xScale = 1.0;
				double yScale = 1.0;

				if (od.width > pageFormat.getImageableWidth()) {
					xScale = pageFormat.getImageableWidth() / ((double)od.width);
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
				String sTemp = mThisFrame.getTitle();
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
				double hh = (SECLEFT + (od.width - SECRIGHT - SECLEFT) / 2) - strWidth / 2;
				double vv = SECTOP / 2 + strbounds.getHeight() / 2;

				JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, g, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
				    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
				    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

				// Add the BG color to the plot
				int x1, y1, width, height;
				x1 = SECLEFT;
				width = this.getSize().width - SECLEFT - SECRIGHT;
				y1 = SECTOP;
				height = this.getSize().height - SECTOP - SECBOTTOM;
				g.setColor(mBG);
				g.setClip(0, 0, 1000, 1000);
				g.fillRect(x1, y1, width, height);
				plotStnTrace(g);
				drawYAxis(g);
				drawXAxis(g);

				if (mPlotRegress)
					plotRegressionLines(g);
				return PAGE_EXISTS;
			}
			else {
				return NO_SUCH_PAGE;
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(mWidth, mHeight);
		}

		public int getMinX() {
			return SECLEFT;
		}

		public int getMinY() {
			return SECTOP;
		}

		public int getMaxX() {
			return this.getSize().width - 1;
		}

		public int getMaxY() {
			return this.getSize().height - SECBOTTOM;
		}

		public void rubberbandEnded(Rubberband rb) {
		}

		public void createPopup(Point point) {
			mPopupMenu = new JPopupMenu();
			JMenuItem openContextualMenu = new JMenuItem(b.getString("kProperties"));
			openContextualMenu.setActionCommand("opencontextual");
			openContextualMenu.addActionListener(this);
			mPopupMenu.add(openContextualMenu);
			mPopupMenu.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			mPopupMenu.show(this, point.x, point.y);
		}

		public void showConfigDialog() {
			// show configuration dialog
			ConfigureStationValueXYPlotDC cp = new ConfigureStationValueXYPlotDC(mFrame, mFileViewer, mDialogClient,
			    mPlotSpec);
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
		}

		public void zoomPlot(Rectangle newRect, boolean mode, boolean mode2) {
			if (mWindowIsLocked) { return; }
			// convert corners of rectangle to new plot range
			Rectangle newBounds = rbRect.lastBounds();
			int y1 = newBounds.y;
			int y2 = y1 + newBounds.height;
			int x1 = newBounds.x;
			int x2 = x1 + newBounds.width;

			if (newBounds.height < 10) { return; }

			// adjust for axes labels
			if (!mReverseY) {
				x1 -= SECLEFT;
				y1 -= SECTOP;
				x2 -= SECLEFT;
				y2 -= SECTOP;
			}
			else if (mReverseY) {
				y1 = mHeightCurrWindow - y1;
				x1 -= SECLEFT;
				y1 -= SECBOTTOM;
				y2 = mHeightCurrWindow - y2;
				x2 -= SECLEFT;
				y2 -= SECBOTTOM;
			}

			if (mode) {
				if (!mode2) {
					mWinXPlotMax = x2 / mWinXScale + mWinXOrigin;
					mWinXPlotMin = x1 / mWinXScale + mWinXOrigin;

					Triplet newRange = JOAFormulas.GetPrettyRange(mWinXPlotMin, mWinXPlotMax);
					mWinXPlotMin = newRange.getVal1();
					mWinXPlotMax = newRange.getVal2();
					mXInc = newRange.getVal3();
					mPlotSpec.setXInc(mXInc);
					mPlotSpec.setWinXPlotMax(mWinXPlotMax);
					mPlotSpec.setWinXPlotMin(mWinXPlotMin);
				}

				if (mReverseY) {
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

				// zoom using current window
				// invalidate and replot
				invalidate();
				paintComponent(this.getGraphics());
				rbRect.end(rbRect.getStretched());
			}
			else {
				StnPlotSpecification ps = new StnPlotSpecification();
				if (!mode2) {
					ps.setWinXPlotMax(x2 / mWinXScale + mWinXOrigin);
					ps.setWinXPlotMin(x1 / mWinXScale + mWinXOrigin);

					Triplet newRange = JOAFormulas.GetPrettyRange(ps.getWinXPlotMin(), ps.getWinXPlotMax());
					ps.setWinXPlotMin(newRange.getVal1());
					ps.setWinXPlotMax(newRange.getVal2());
					ps.setXInc(newRange.getVal3());
				}
				else {
					ps.setWinXPlotMax(mWinXPlotMax);
					ps.setWinXPlotMin(mWinXPlotMin);
					ps.setXInc(mXInc);
				}

				if (mReverseY) {
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

				ps.setFGColor(mFG);
				ps.setBGColor(mBG);
				ps.setFileViewer(mFileViewer);
				ps.setXStnVarCode(mXStnVarCode);
				ps.setYStnVarCode(mYStnVarCode);
				ps.setWinTitle(mWinTitle + "z");
				ps.setYGrid(mYGrid);
				ps.setSymbolSize(mSymbolSize);
				ps.setSymbol(mSymbol);
				ps.setYTics(mYTics);
				ps.setXTics(mXTics);
				ps.setSectionType(mSectionType);
				ps.setReverseY(mReverseY);

				try {
					ps.writeToLog("Zoomed Station Value XY Plot" + mParent.getTitle());
				}
				catch (Exception ex) {
				}

				// make a new plot window
				JOAStnValXYPlotWindow plotWind = new JOAStnValXYPlotWindow(ps, mThisFrame);
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
			outImage = createImage(getSize().width, getSize().height);

			Graphics2D og = (Graphics2D)outImage.getGraphics();
			super.paintComponent(og);

			int x1, y1, width, height;
			x1 = SECLEFT;
			width = this.getSize().width - SECLEFT - SECRIGHT;
			y1 = SECTOP;
			height = this.getSize().height - SECTOP - SECBOTTOM;
			og.setColor(mBG);
			og.fillRect(x1, y1, width, height);

			computeRegressions();

			setXScale(getSize().width);
			setYScale(getSize().height);
			plotStnTrace(og);

			drawYAxis((Graphics2D)og);
			drawXAxis((Graphics2D)og);

			if (mPlotRegress)
				plotRegressionLines(og);

			if (addTitle) {
				// add the title
				String sTemp = mThisFrame.getTitle();

				// layout the title
				Font font = new Font(JOAConstants.DEFAULT_PLOT_TITLE_FONT, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
				    JOAConstants.DEFAULT_PLOT_TITLE_SIZE);
				FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
				int strWidth = fm.stringWidth(sTemp);

				double hh = (SECLEFT + (this.getWidth() - SECRIGHT - SECLEFT) / 2) - strWidth / 2;
				double vv = SECTOP / 2 + fm.getHeight() / 2;
				JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, og, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
				    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
				    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);
			}

			og.dispose();
			return outImage;
		}

		public void paintComponent(Graphics gin) {
			Graphics2D g = (Graphics2D)gin;
			/*
			 * if (g instanceof PrintGraphics) { int x1, y1, width, height; x1 =
			 * SECLEFT; width = this.getSize().width - SECLEFT - SECRIGHT; y1 =
			 * SECTOP; height = this.getSize().height - SECTOP - SECBOTTOM -
			 * SECBOTTOM; g.setColor(mBG); g.fillRect(x1, y1, width, height);
			 * g.setColor(Color.black); g.drawRect(x1, y1, width, height);
			 * plotStnTrace(g); drawYAxis((Graphics2D)g); drawXAxis((Graphics2D)g); }
			 * else {
			 */
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

			if (mObsMarker != null && !mWindowIsLocked) {
				mObsMarker.drawMarker(g, false);
			}
			// }
		}

		public void plotRegressionLines(Graphics2D g) {
			boolean reverseY = mPlotSpec.isReverseY();

			if (JOAConstants.THICKEN_CONTOUR_LINES) {
				g.setStroke(lw4);
			}
			else {
				g.setStroke(lw2);
			}

			int x, width, y, height;
			x = SECLEFT;
			width = this.getSize().width - SECLEFT - SECRIGHT;
			y = SECTOP;
			height = this.getSize().height - SECTOP - SECBOTTOM;
			g.setClip(x, y, width, height);

			g.setColor(Color.black);
			// compute points on the boundary
			double x0 = mWinXPlotMin;
			double x1 = mWinXPlotMax;
			double y0 = mSlope * x0 + mIntercept;
			double y1 = mSlope * x1 + mIntercept;

			x0 = (x0 - mWinXOrigin) * mWinXScale;
			y0 = (y0 - mWinYOrigin) * mWinYScale;

			// adjust for axes labels
			if (!reverseY) {
				x0 += SECLEFT;
				y0 += SECTOP;
			}
			else {
				y0 = mHeightCurrWindow - y0;
				x0 += SECLEFT;
				y0 -= SECBOTTOM;
			}

			x1 = (x1 - mWinXOrigin) * mWinXScale;
			y1 = (y1 - mWinYOrigin) * mWinYScale;

			// adjust for axes labels
			if (!reverseY) {
				x1 += SECLEFT;
				y1 += SECTOP;
			}
			else {
				y1 = mHeightCurrWindow - y1;
				x1 += SECLEFT;
				y1 -= SECBOTTOM;
			}

			g.drawLine((int)x0, (int)y0, (int)x1, (int)y1);

			double ymin = mWinYPlotMin;
			double ymax = mWinYPlotMax;
			double xmin = mWinXPlotMin;
			double xmax = mWinXPlotMax;
			double offsetIntercept = mIntercept + 0;
			double xAtYmin = (ymin - offsetIntercept) / mSlope;
			double xAtYmax = (ymax - offsetIntercept) / mSlope;
			double xmid = 0;
			double ymid = 0;
			double deltaY = 0;
			double deltaX = 0;
			double xTranslation = 0.0;

			if (mSlope > 0) {
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
					double yAtXMax = mSlope * xmax + offsetIntercept;
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
					double yAtXMin = mSlope * xmin + offsetIntercept;
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
					double yAtXMax = mSlope * xmax + offsetIntercept;
					double yAtXMin = mSlope * xmin + offsetIntercept;
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
			else if (mSlope < 0.0) {
				if (xAtYmax > xmin && xAtYmin > xmax) {
					xmid = (xmax + xAtYmax) / 2.0;
					double yAtXMax = mSlope * xmax + offsetIntercept;
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
					double yAtXMin = mSlope * xmin + offsetIntercept;
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
					double yAtXMin = mSlope * xmin + offsetIntercept;
					double yAtXMax = mSlope * xmax + offsetIntercept;
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

			String slopeStr = JOAFormulas.formatDouble(mSlope, 3, false);
			String interceptStr = JOAFormulas.formatDouble(mIntercept, 2, false);
			String r2String = JOAFormulas.formatDouble(mRSquare, 2, false);
			String valStr = "y=" + slopeStr + "x+" + interceptStr + " r2=" + r2String;

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
			if (!reverseY) {
				xx += SECLEFT;
				yy += SECTOP;
			}
			else {
				yy = mHeightCurrWindow - yy;
				xx += SECLEFT;
				yy -= SECBOTTOM;
			}

			return new UVCoordinate(xx, yy);

		}

		private void plotStnTrace(Graphics2D g) {
			int x1, width, y1, height;
			int currSymbol = mSymbol;
			int currSymbolSize = mSymbolSize;

			x1 = SECLEFT;
			width = this.getSize().width - SECLEFT - SECRIGHT;
			y1 = SECTOP;
			height = this.getSize().height - SECTOP - SECBOTTOM;
			g.setClip(x1, y1 + 1, width, height - 1);

			g.setColor(Color.black);
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);

					if (sec == 0) {
						mYStnVarName = sech.getStnVar(mYStnVarCode);
						mXStnVarName = sech.getStnVar(mXStnVarCode);
						mYStnVarUnits = sech.getStnVarUnits(mYStnVarCode);
						mXStnVarUnits = sech.getStnVarUnits(mXStnVarCode);
					}

					if (sech.mNumCasts == 0) {
						continue;
					}

					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);

						if (!sh.mUseStn) {
							continue;
						}

						// plot the values
						double x = sh.getStnValue(mXStnVarCode);
						double y = sh.getStnValue(mYStnVarCode);

						if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE) {
							int secy = (int)((y - mWinYOrigin) * mWinYScale);
							int secx = (int)((x - mWinXOrigin) * mWinXScale);

							if (mReverseY) {
								secy = mHeightCurrWindow - secy;
								secy -= SECBOTTOM;
							}
							secx += SECLEFT;
							g.setColor(Color.black);

							JOAFormulas.plotSymbol(g, currSymbol, secx, secy, currSymbolSize);
						}
					}
				}
			}

			// initialize the spot
			if (mObsMarker == null) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
				Section sech = (Section)of.mSections.currElement();
				Station sh = (Station)sech.mStations.currElement();

				double y = sh.getStnValue(mYStnVarCode);
				double x = sh.getStnValue(mXStnVarCode);
				if (y != JOAConstants.MISSINGVALUE) {
					y = (y - mWinYOrigin) * mWinYScale;
					x = (x - mWinXOrigin) * mWinXScale;
					if (mReverseY) {
						y = mHeightCurrWindow - y;
						y -= SECBOTTOM;
					}
					x += SECLEFT;
					mObsMarker = new ObsMarker((int)x, (int)y, JOAConstants.DEFAULT_CURSOR_SIZE);
				}
			}
			g.setClip(0, 0, this.getSize().width, this.getSize().height);
		}

		@SuppressWarnings("deprecation")
		public void drawXAxis(Graphics2D g) {
			g.setColor(Color.black);
			int maxY = this.getSize().height - SECTOP - SECBOTTOM;
			int bottom = maxY + SECTOP;
			int top = SECTOP;
			int left = SECLEFT;
			int right = this.getSize().width - SECRIGHT;

			double xDiff = mWinXPlotMax - mWinXPlotMin;
			int majorXTicks = (int)(xDiff / mXInc);
			double xInc = (double)(right - left) / (xDiff / mXInc);
			double minorXInc = xInc / ((double)mPlotSpec.getXTics() + 1);

			// draw the X axis
			g.drawLine(left, bottom, right, bottom);

			// draw the X tic marks
			for (int i = 0; i <= majorXTicks; i++) {
				int h = (int)(left + (i * xInc));
				// if (h <= right)
				// g.drawLine(h, bottom+5, h, bottom);
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
				if (i <= majorXTicks) {
					for (int hh = 0; hh < mPlotSpec.getXTics() + 1; hh++) {
						int newH = (int)(h + (hh * minorXInc));
						if (newH <= right) {
							g.drawLine(newH, bottom + 3, newH, bottom);
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

			// label the x axis
			Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
			    JOAConstants.DEFAULT_AXIS_VALUE_SIZE);
			g.setFont(font);
			FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
			double vOrigin = mWinXPlotMin;
			String sTemp = null;
			int maxv = fm.getHeight();
			int voffset = bottom + 2 + maxv;

			for (int i = 0; i <= majorXTicks; i++) {
				double myVal = vOrigin + (i * mXInc);
				if (myVal == -0.0) {
					myVal = 0.0;
				}
				int h = (int)((left) + (i * xInc));

				if (numPlaces > 0) {
					sTemp = JOAFormulas.roundNDecimals(myVal, numPlaces);
				}
				else {
					sTemp = String.valueOf((int)myVal);
				}
				int len = fm.stringWidth(sTemp);
				JOAFormulas.drawStyledString(sTemp, h - (len / 2), voffset, g, 0.0, JOAConstants.DEFAULT_AXIS_VALUE_FONT,
				    JOAConstants.DEFAULT_AXIS_VALUE_SIZE, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
				    JOAConstants.DEFAULT_AXIS_VALUE_COLOR);
			}

			int centerV = voffset + (((bottom + SECBOTTOM) - voffset) / 2);

			// x-axis label
			right = this.getSize().width - SECRIGHT;
			ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
			font = new Font(JOAConstants.DEFAULT_AXIS_LABEL_FONT, JOAConstants.DEFAULT_AXIS_LABEL_STYLE,
			    JOAConstants.DEFAULT_AXIS_LABEL_SIZE);
			g.setFont(font);
			fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
			int width = 0;
			String axisLabel = "MLSF";
			if (mXStnVarUnits != null && mXStnVarUnits.length() > 0) {
				axisLabel = mXStnVarName + " (" + mXStnVarUnits + ")";
				width = fm.stringWidth(axisLabel);
			}
			else {
				axisLabel = mXStnVarName;
				width = fm.stringWidth(axisLabel);
			}
			int hh = mWidthCurrWindow / 2 - (width / 2) + SECRIGHT;
			int maxLblHeight = fm.getHeight() - fm.getLeading() - 4;
			int vv = centerV + maxLblHeight / 2;
			JOAFormulas.drawStyledString(axisLabel, hh, vv, g, 0, JOAConstants.DEFAULT_AXIS_LABEL_FONT,
			    JOAConstants.DEFAULT_AXIS_LABEL_SIZE, JOAConstants.DEFAULT_AXIS_LABEL_STYLE,
			    JOAConstants.DEFAULT_AXIS_LABEL_COLOR);
		}

		@SuppressWarnings( { "deprecation", "deprecation" })
		public void drawYAxis(Graphics2D g) {
			g.setColor(Color.black);
			int bottom = (int)mHeightCurrWindow - 1 * SECBOTTOM;
			int top = SECTOP;
			int left = SECLEFT;
			int right = (int)mWidthCurrWindow - SECRIGHT;
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
			if (mReverseY) {
				vOrigin = mWinYPlotMax;
			}
			else {
				vOrigin = mWinYPlotMin;
			}

			int maxStrLen = 0;
			for (int i = 0; i <= majorYTicks; i++) {
				if (mReverseY) {
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
			String axisLabel = "MLSF";
			if (mYStnVarUnits != null && mYStnVarUnits.length() > 0) {
				axisLabel = mYStnVarName + " (" + mYStnVarUnits + ")";
				width = fm.stringWidth(axisLabel);
			}
			else {
				axisLabel = mYStnVarName;
				width = fm.stringWidth(axisLabel);
			}
			int height = this.getSize().height;
			int hcenter = (leftMTicPos - maxStrLen) / 2;
			int hpos = hcenter + fm.getHeight() / 2;
			JOAFormulas.drawStyledString(axisLabel, hpos, SECTOP + ((height - SECTOP - SECBOTTOM) / 2) + width / 2, g, 90,
			    JOAConstants.DEFAULT_AXIS_LABEL_FONT, JOAConstants.DEFAULT_AXIS_LABEL_SIZE,
			    JOAConstants.DEFAULT_AXIS_LABEL_STYLE, JOAConstants.DEFAULT_AXIS_LABEL_COLOR);
		}

		public void obsChanged(ObsChangedEvent evt) {
			if (mWindowIsLocked) { return; }
			// display the current station
			Station sh = evt.getFoundStation();
			Section sech = evt.getFoundSection();
			setRecord(sech, sh);
		}

		private void setRecord(Section inSec, Station inStn) {
			// compute the section offset
			double x = inStn.getStnValue(mXStnVarCode);
			double y = inStn.getStnValue(mYStnVarCode);

			if (y != JOAConstants.MISSINGVALUE && x != JOAConstants.MISSINGVALUE) {
				y = (y - mWinYOrigin) * mWinYScale;
				x = (x - mWinXOrigin) * mWinXScale;
				if (mReverseY) {
					y = mHeightCurrWindow - y;
					y -= SECBOTTOM;
				}
				x += SECLEFT;

				/*
				 * if (!mReverseY) { x += SECLEFT; y += SECTOP; } else if (mReverseY) {
				 * y = mHeightCurrWindow - y; x += SECLEFT; y -= SECBOTTOM; }
				 */

				if (mObsMarker == null) {
					mObsMarker = new ObsMarker((int)x, (int)y, JOAConstants.DEFAULT_CURSOR_SIZE);
					paintImmediately(0, 0, 2000, 2000);
				}
				else {
					mObsMarker.setNewPos((int)x, (int)y);
					paintImmediately(0, 0, 2000, 2000);
				}
			}
		}

		private void findByXY(int x, int y) {
			boolean found = false;
			OpenDataFile foundFile = null;
			Section foundSection = null;
			Station foundStation = null;
			Bottle foundBottle = null;
			OpenDataFile oldof = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
			Section oldsech = (Section)oldof.mSections.currElement();
			Station oldsh = (Station)oldsech.mStations.currElement();
			Bottle oldBottle = (Bottle)oldsh.mBottles.currElement();

			if (!mSpotable) { return; }

			double minOffset = 10000.0;
			// search for a matching observation
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

						double xx = sh.getStnValue(mXStnVarCode);
						double yy = sh.getStnValue(mYStnVarCode);

						if (yy != JOAConstants.MISSINGVALUE && xx != JOAConstants.MISSINGVALUE) {
							yy = (yy - mWinYOrigin) * mWinYScale;
							xx = (xx - mWinXOrigin) * mWinXScale;

							if (mReverseY) {
								yy = mHeightCurrWindow - yy;
								yy -= SECBOTTOM;
							}
							xx += SECLEFT;

							double off = Math.sqrt(((x - xx) * (x - xx)) + ((y - yy) * (y - yy)));

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

			// post event so other components will update
				if (foundStation != oldsh) {
					mFileViewer.mOpenFiles.setCurrElement(foundFile);
					foundFile.mSections.setCurrElement(foundSection);
					foundSection.mStations.setCurrElement(foundStation);
					foundStation.mBottles.setCurrElement(foundBottle);
					int pPos = mFileViewer.getPRESPropertyPos();

					// find the closest matching bottle to the current depth and
					// station
					int xPos = foundSection.getPRESVarPos();
					double testPres = oldBottle.mDValues[xPos];
					JOAConstants.currTestPres = testPres;
					foundBottle = JOAFormulas.findBottleByPres(mFileViewer, oldsh);
					
					ObsChangedEvent oce = new ObsChangedEvent(mFileViewer);
					oce.setFoundObs(foundFile, foundSection, foundStation, foundBottle);
					paintImmediately(new Rectangle(0, 0, 2000, 2000));
					Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(oce);
				}
		}

		public boolean findByArrowKey(Integer direction) {
			if (mWindowIsLocked) { return false; }
			OpenDataFile foundFile = null;
			Section foundSection = null;
			Station foundStation = null;
			Bottle foundBottle = null;
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
			Section sech = (Section)of.mSections.currElement();
			// find new observation
			boolean found = false;
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
			}

			// post event so other components will update
			if (found) {
				mFileViewer.mOpenFiles.setCurrElement(foundFile);
				foundFile.mSections.setCurrElement(foundSection);
				foundSection.mStations.setCurrElement(foundStation);
				foundStation.mBottles.setCurrElement(foundBottle);
				ObsChangedEvent oce = new ObsChangedEvent(mFileViewer);
				oce.setFoundObs(foundFile, foundSection, foundStation, foundBottle);
				paintImmediately(new Rectangle(0, 0, 2000, 2000));
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
			mPlotSpec = ((ConfigureStationValueXYPlotDC)d).createPlotSpec();
			try {
				mPlotSpec.writeToLog("Edited existing plot: " + mParent.getTitle());
			}
			catch (Exception ex) {
			}
			mWidth = mPlotSpec.getWidth();
			mHeight = mPlotSpec.getHeight();
			mFG = mPlotSpec.getFGColor();
			mBG = mPlotSpec.getBGColor();
			mFileViewer = mPlotSpec.getFileViewer();
			mYStnVarCode = mPlotSpec.getYStnVarCode();
			mWinTitle = mPlotSpec.getWinTitle();
			mYGrid = mPlotSpec.isYGrid();
			mXGrid = mPlotSpec.isXGrid();
			mSymbol = mPlotSpec.getSymbol();
			mSymbolSize = mPlotSpec.getSymbolSize();
			mYTics = mPlotSpec.getYTics();
			mXTics = mPlotSpec.getXTics();
			mSectionType = mPlotSpec.getSectionType();
			mReverseY = mPlotSpec.isReverseY();

			// axes ranges
			mWinXPlotMin = mPlotSpec.getWinXPlotMin();
			mWinXPlotMax = mPlotSpec.getWinXPlotMax();

			mWinYPlotMin = mPlotSpec.getWinYPlotMin();
			mWinYPlotMax = mPlotSpec.getWinYPlotMax();
			mYInc = mPlotSpec.getYInc();

			mOffScreen = null;
			this.invalidate();
			paintComponent(this.getGraphics());
		}

		// Cancel button
		public void dialogCancelled(JDialog d) {
			mPlotSpec = ((ConfigureStationValueXYPlotDC)d).getOrigPlotSpec();
			mWidth = mPlotSpec.getWidth();
			mHeight = mPlotSpec.getHeight();
			mFG = mPlotSpec.getFGColor();
			mBG = mPlotSpec.getBGColor();
			mFileViewer = mPlotSpec.getFileViewer();
			mYStnVarCode = mPlotSpec.getYStnVarCode();
			mWinTitle = mPlotSpec.getWinTitle();
			mYGrid = mPlotSpec.isYGrid();
			mXGrid = mPlotSpec.isXGrid();
			mSymbol = mPlotSpec.getSymbol();
			mSymbolSize = mPlotSpec.getSymbolSize();
			mYTics = mPlotSpec.getYTics();
			mXTics = mPlotSpec.getXTics();
			mSectionType = mPlotSpec.getSectionType();
			mReverseY = mPlotSpec.isReverseY();

			// axes ranges
			mWinXPlotMin = mPlotSpec.getWinXPlotMin();
			mWinXPlotMax = mPlotSpec.getWinXPlotMax();

			mWinYPlotMin = mPlotSpec.getWinYPlotMin();
			mWinYPlotMax = mPlotSpec.getWinYPlotMax();
			mYInc = mPlotSpec.getYInc();

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
			mPlotSpec = ((ConfigureStationValueXYPlotDC)d).createPlotSpec();
			try {
				mPlotSpec.writeToLog("Edited existing plot: " + mParent.getTitle());
			}
			catch (Exception ex) {
			}
			mWidth = mPlotSpec.getWidth();
			mHeight = mPlotSpec.getHeight();
			mFG = mPlotSpec.getFGColor();
			mBG = mPlotSpec.getBGColor();
			mFileViewer = mPlotSpec.getFileViewer();
			mYStnVarCode = mPlotSpec.getYStnVarCode();
			mWinTitle = mPlotSpec.getWinTitle();
			mYGrid = mPlotSpec.isYGrid();
			mXGrid = mPlotSpec.isXGrid();
			mSymbol = mPlotSpec.getSymbol();
			mSymbolSize = mPlotSpec.getSymbolSize();
			mYTics = mPlotSpec.getYTics();
			mXTics = mPlotSpec.getXTics();
			mSectionType = mPlotSpec.getSectionType();
			mReverseY = mPlotSpec.isReverseY();

			// axes ranges
			mWinXPlotMin = mPlotSpec.getWinXPlotMin();
			mWinXPlotMax = mPlotSpec.getWinXPlotMax();

			mWinYPlotMin = mPlotSpec.getWinYPlotMin();
			mWinYPlotMax = mPlotSpec.getWinYPlotMax();
			mYInc = mPlotSpec.getYInc();

			mOffScreen = null;
			this.invalidate();
			paintComponent(this.getGraphics());
		}

		public UVCoordinate getCorrectedXY(int x, int y) {
			double dy = (double)y;
			double dx = (double)x;
			if (y < getMinY() || y > getMaxY() || x < getMinX() || x > getMaxX()) {
				dy = Double.NaN;
				dx = Double.NaN;
				return new UVCoordinate(dx, dy);
			}

			if (!mReverseY) {
				dy += SECTOP;
			}
			else if (mReverseY) {
				dy = mHeightCurrWindow - dy;
				dy -= SECBOTTOM;
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
	}

	public RubberbandPanel getPanel() {
		return mStnPlotPanel;
	}

	private void computeRegressions() {
		// compute linear regressions for all plotted parameters
		double sumx = 0.0;
		double sumy = 0.0;
		double sumx2 = 0.0;
		int n = 0;
		double xbar = Double.NaN;
		double ybar = Double.NaN;
		double xxbar = 0.0;
		double yybar = 0.0;
		double xybar = 0.0;

		// pass #1
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

					double x = sh.getStnValue(mXStnVarCode);
					double y = sh.getStnValue(mYStnVarCode);

					if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE && !Double.isNaN(x) && !Double.isNaN(y)) {
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

		double[] tempX = null;
		double[] tempY = null;
		int ic = 0;

		if (n > 0) {
			xbar = sumx / n;
			ybar = sumy / n;
			tempX = new double[n];
			tempY = new double[n];
		}

		// pass #2
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

					// plot the values
					double x = sh.getStnValue(mXStnVarCode);

					// get the station value
					double y = sh.getStnValue(mYStnVarCode);

					if (x != JOAConstants.MISSINGVALUE && y != JOAConstants.MISSINGVALUE && !Double.isNaN(x) && !Double.isNaN(y)) {
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

		// compute regression line coeffs
		if (n > 0) {
			mSlope = xybar / xxbar;
			mIntercept = ybar - mSlope * xbar;
		}

		// analyze results
		int df = n - 2;
		double rss = 0.0; // residual sum of squares
		double ssr = 0.0; // regression sum of squares
		for (int j = 0; j < n; j++) {
			double fit = mSlope * tempX[j] + mIntercept;
			rss += (fit - tempY[j]) * (fit - tempY[j]);
			ssr += (fit - ybar) * (fit - ybar);
		}
		mRSquare = ssr / yybar;
		svar = rss / df;
		svar1 = svar / xxbar;
		svar0 = svar / n + xbar * xbar * svar1;
	}

	public boolean inWindow(double x, double y) {
		if (x > mWinXPlotMax || x < mWinXPlotMin) { return false; }

		if (y > mWinYPlotMax || y < mWinYPlotMin) { return false; }
		return true;
	}
}

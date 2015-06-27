/*
 * $Id: JOAMapPlotWindow.java,v 1.19 2005/09/07 18:49:29 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.events.*;
import java.awt.print.*;
import com.visualtek.png.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class JOAMapPlotWindow extends JOAWindow implements DataAddedListener, ActionListener, ConfigurableWindow,
    StnFilterChangedListener, PrefsChangedListener, JOAMapContainer, WindowsMenuChangedListener {

	protected MapSpecification mMapSpec;
	protected FileViewer mFileViewer;
	protected ObsMarker mObsMarker = null;
	protected int mWidth = 0, mHeight = 0, mLegendHeight = 0;
	protected MapLegend mLegend = null;
	protected Container mContents;
	protected JOAWindow mFrame = null;
	protected MapPlotPanel mMap = null;
	private LocationToolbar locToolBar;
	protected JOAJToggleButton zoomTool = null;
	protected JOAJToggleButton selectTool = null;
	protected JOAJToggleButton sectionTool = null;
	protected JOAJToggleButton polygonTool = null;
	protected JOAJToggleButton mapCtrTool = null;
	protected JOAJTextField sectionWidthFld = null;
	protected JFrame mParent;
	protected HorizontalBargauge bargauge;
	protected boolean mFirstPlot = true;
	ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	static String RELATIVE = "relative";
	static String CLAMPEDTOGROUND = "clampedToGround";
	MapColorBarPanel mColorBarLegend = null;

	public JOAMapPlotWindow(FileViewer fv, MapSpecification mapSpec, int width, int height, JFrame parent) {
		super(true, true, true, true, true, mapSpec);
		this.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
		mWidth = width;
		mHeight = height;
		mFileViewer = fv;
		mMapSpec = mapSpec;
		mParent = parent;
		init();
		mMap.requestFocus();
	}

	public void init() {
		mFrame = this;
		mContents = this.getContentPane();
		mContents.setLayout(new BorderLayout(0, 0));

		// add the toolbar
		mToolBar = new JOAJToolBar();
		zoomTool = new JOAJToggleButton(new ImageIcon(getClass().getResource("images/arrowcursor.gif")), true);
		zoomTool.setToolTipText(b.getString("kMapPtrToolTipText"));
		selectTool = new JOAJToggleButton(new ImageIcon(getClass().getResource("images/handcursor.gif")), false);
		selectTool.setToolTipText(b.getString("kHandToolTipText"));
		sectionTool = new JOAJToggleButton(new ImageIcon(getClass().getResource("images/sectiontool.gif")), false);
		sectionTool.setToolTipText(b.getString("kSectionToolTipText"));
		polygonTool = new JOAJToggleButton(new ImageIcon(getClass().getResource("images/polygontool.gif")), false);
		polygonTool.setToolTipText(b.getString("kPolygonToolTipText"));
		mapCtrTool = new JOAJToggleButton(new ImageIcon(getClass().getResource("images/polygontool.gif")), false);
		//mapCtrTool.setToolTipText(b.getString("kLockPlot"));
		JOAJToggleButton lockTool = new JOAJToggleButton(new ImageIcon(getClass().getResource("images/lock_open.gif")),
		    true);
		lockTool.setSelectedIcon(new ImageIcon(getClass().getResource("images/lock_closed.gif")));
		lockTool.setToolTipText(b.getString("kLockPlot"));
		lockTool.setSelected(false);
		ButtonGroup bg = new ButtonGroup();
		bg.add(zoomTool);
		bg.add(sectionTool);
		bg.add(selectTool);
		bg.add(polygonTool);
		bg.add(mapCtrTool);
		zoomTool.setActionCommand("zoom");
		sectionTool.setActionCommand("section");
		selectTool.setActionCommand("select");
		polygonTool.setActionCommand("polyselect");
		mapCtrTool.setActionCommand("center");
		lockTool.setActionCommand("lock");
		zoomTool.addActionListener(this);
		sectionTool.addActionListener(this);
		selectTool.addActionListener(this);
		polygonTool.addActionListener(this);
		mapCtrTool.addActionListener(this);
		lockTool.addActionListener(this);
		mToolBar.add(zoomTool);
		mToolBar.add(selectTool);
		mToolBar.add(sectionTool);
		mToolBar.add(polygonTool);
		mToolBar.add(lockTool);
//		mToolBar.add(mapCtrTool);

		JPanel widthPanel = new JPanel();
		widthPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		widthPanel.add(mToolBar);
		widthPanel.add(new JOAJLabel(b.getString("kSectionWidth")));
		sectionWidthFld = new JOAJTextField(5);
		sectionWidthFld.setToolTipText(b.getString("kSectionWidthToolTipText"));
		sectionWidthFld.setFocusTraversable(true);
		sectionWidthFld.setText(JOAFormulas.formatDouble(String.valueOf(JOAConstants.SECTION_WIDTH), 0, false));
		sectionWidthFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		sectionWidthFld.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				double val;
				try {
					val = Double.valueOf(sectionWidthFld.getText()).doubleValue();
				}
				catch (Exception e) {
					val = 200.0;
				}

				JOAConstants.SECTION_WIDTH = val;
			}
		});

		sectionWidthFld.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent me) {
				double val;
				try {
					val = Double.valueOf(sectionWidthFld.getText()).doubleValue();
				}
				catch (Exception e) {
					val = 200.0;
				}

				JOAConstants.SECTION_WIDTH = val;
			}
		});
		widthPanel.add(sectionWidthFld);
		widthPanel.add(new JOAJLabel(b.getString("kKM")));
		bargauge = new HorizontalBargauge(Color.blue, this.getBackground());
		widthPanel.add(bargauge);
		JPanel toolCont = new JPanel();
		toolCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 2));
		toolCont.add(widthPanel);
		locToolBar = new LocationToolbar();
		PowerOceanAtlas.getInstance().addPrefsChangedListener(locToolBar);
		toolCont.add(locToolBar);
		mContents.add(toolCont, "North");

		// add the legend
		mLegend = new MapLegend(this, mParent, mFileViewer, mMapSpec);
		mLegend.setOpaque(false);
		JPanel mapLegendCont = new JPanel();
		mapLegendCont.setBackground(Color.white);
		mapLegendCont.setOpaque(true);
		mapLegendCont.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mapLegendCont.add(mLegend);
		TenPixelBorder tpb = new TenPixelBorder(mapLegendCont, 5, 5, 5, 5);
		tpb.setBackground(Color.white);
		tpb.setOpaque(true);
		mContents.add("South", tpb);
		mColorBarLegend = new MapColorBarPanel(this, mFileViewer);
		mContents.add(BorderLayout.EAST, mColorBarLegend);
		
		// add a color legend for the stns
		if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE ||
				mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR ||
				mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
			ColorBarPanel cbp = new ColorBarPanel(mFrame, mFileViewer, mMapSpec.getStnColorColorBar(), mMapSpec.getCoastColor(),
			    JOAConstants.DEFAULT_FRAME_COLOR, false, false);
			cbp.setLinked(false);
			cbp.setBroadcastMode(false);
			cbp.setEnhanceable(false);
			mColorBarLegend.add(cbp.getName(), cbp);
		}
		
		// add a color legend for the overlay contours
		if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE ||
				mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
			ColorBarPanel cbp = new ColorBarPanel(mFrame, mFileViewer, mMapSpec.getOverlayContoursColorBar(), mMapSpec.getCoastColor(),
			    JOAConstants.DEFAULT_FRAME_COLOR, false, false);
			cbp.setLinked(false);
			cbp.setBroadcastMode(false);
			cbp.setEnhanceable(false);
			mColorBarLegend.add(cbp.getName(), cbp);
		}		
		
		Dimension d = mLegend.getPreferredSize();
		mLegendHeight = d.height;
		mMap = new MapPlotPanel(mFileViewer, mMapSpec, mObsMarker, this, mFrame, mLegend, mColorBarLegend, -50, -50, 0.8,
		    0.8);
		mMap.setBorder(BorderFactory.createEtchedBorder());
		mMap.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		mMap.init();
		mContents.add("Center", new TenPixelBorder(mMap, 5, 5, 5, 5, JOAConstants.DEFAULT_FRAME_COLOR));

		getRootPane().registerKeyboardAction(new javaoceanatlas.ui.RightListener((Object) mMap, mMap.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new javaoceanatlas.ui.LeftListener((Object) mMap, mMap.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new javaoceanatlas.ui.UpListener((Object) mMap, mMap.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(new javaoceanatlas.ui.DownListener((Object) mMap, mMap.getClass()),
		    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeMe();
			}

			public void windowActivated(WindowEvent we) {
				if (!JOAConstants.ISMAC) { return; }
				// ResourceBundle bb =
				// ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
				// Menu fmenu = mMenuBar.getAWTMenuBar().getMenu(0);
				// MenuItem mi = fmenu.getItem(6);
				// mi.setLabel(bb.getString("kSaveGraphic"));

			}
		};
		this.addWindowListener(windowListener);

		if (mMapSpec.getMapName() == null || mMapSpec.getMapName().length() == 0) {
			mMapSpec.setMapName(new String("Untitled Map"));
			this.setTitle(mMapSpec.getMapName());
		}
		else {
			this.setTitle(new String(mMapSpec.getMapName()));
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
		mFileViewer.removeOpenWindow(mFrame);
		mFileViewer.removeDataAddedListener((DataAddedListener) mFrame);
		mFileViewer.removeStnFilterChangedListener((StnFilterChangedListener) mFrame);
		PowerOceanAtlas.getInstance().removePrefsChangedListener((PrefsChangedListener) mFrame);
		mFileViewer.removeObsChangedListener((ObsChangedListener) mMap);
		mFileViewer.removeMetadataChangedListener((MetadataChangedListener) mMap);
		PowerOceanAtlas.getInstance().removeWindowsMenuChangedListener((WindowsMenuChangedListener) mFrame);
		PowerOceanAtlas.getInstance().removePrefsChangedListener(locToolBar);
		mFileViewer = null;
		mMapSpec = null;
		mParent = null;
	}

	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("lock")) {
			mWindowIsLocked = !mWindowIsLocked;
			;
			mMap.setLocked(mWindowIsLocked);
			if (!mWindowIsLocked) {
				// are now unlocking the window
				this.setSize(this.getSize().width + 1, this.getSize().height);
				this.setSize(this.getSize().width, this.getSize().height);

				// if (mCurrObsPanel != null)
				// mCurrObsPanel.setLocked(false);

				mMap.invalidate();
				mMap.validate();
				this.invalidate();
				this.validate();
				mMap.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				this.setResizable(true);

				if (mColorBarLegend != null) {
					mColorBarLegend.setLocked(false);
					mLegend.setLocked(false);
				}
			}
			else {
				mMap.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				this.setResizable(false);

				if (mColorBarLegend != null) {
					mColorBarLegend.setLocked(true);
					mLegend.setLocked(true);
				}

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
		else if (cmd.equals("zoom")) {
			mMap.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if (mMapSpec.getProjection() == JOAConstants.MILLERPROJECTION
			    || mMapSpec.getProjection() == JOAConstants.MERCATORPROJECTION) {
				mMap.setSelectionAsRectangle();
			}
			// other else ifs for other projections
			else if (mMapSpec.getProjection() > JOAConstants.MILLERPROJECTION
			    && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
				mMap.setSelectionAsPSRectangle();
			}
			// other else ifs for other projections
			else if (mMapSpec.getProjection() == JOAConstants.ROBINSONPROJECTION) {
				mMap.setSelectionAsPSRectangle(); // to do
			}
			// other else ifs for other projections
			else if (mMapSpec.getProjection() == JOAConstants.ECKERT4PROJECTION) {
				mMap.setSelectionAsRectangle(); // to do
			}
			else {
				mMap.setSelectionAsPolarCircle();
			}

			mMap.setZoomMode();
		}
		else if (cmd.equals("section")) {
			mMap.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			mMap.setSelectionAsLine();
			mMap.setSectionMode();
		}
		else if (cmd.equals("select")) {
			mMap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			if (mMapSpec.getProjection() == JOAConstants.MILLERPROJECTION
			    || mMapSpec.getProjection() == JOAConstants.MERCATORPROJECTION) {
				mMap.setSelectionAsRectangle();
			}
			// other map projections
			else if (mMapSpec.getProjection() > JOAConstants.MILLERPROJECTION
			    && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
				mMap.setSelectionAsPSRectangle();
			}
			else if (mMapSpec.getProjection() == JOAConstants.ROBINSONPROJECTION) {
				mMap.setSelectionAsPSRectangle();
			}
			else {
				mMap.setSelectionAsPolarCircle();
			}
			mMap.setSelectionMode();
		}
		else if (cmd.equals("polyselect")) {
			mMap.setCursor(Cursor.getPredefinedCursor(CROSSHAIR_CURSOR));
			mMap.setSelectionAsPolygon();
			mMap.setSelectionMode();
		}
		else if (cmd.equals("center")) {
			mMap.setCursor(Cursor.getPredefinedCursor(CROSSHAIR_CURSOR));
			mMap.setSelectionAsCentering();
			mMap.setCenteringMode();
		}
		else if (cmd.equals("print")) {
			try {
				if (JOAConstants.DEFAULT_PAGEFORMAT == null) {
					JOAConstants.DEFAULT_PRINTERJOB = PrinterJob.getPrinterJob();
					JOAConstants.DEFAULT_PAGEFORMAT = JOAConstants.DEFAULT_PRINTERJOB.defaultPage();
					JOAConstants.DEFAULT_PAGEFORMAT = JOAConstants.DEFAULT_PRINTERJOB.pageDialog(JOAConstants.DEFAULT_PAGEFORMAT);
				}
				if (JOAConstants.DEFAULT_PRINTERJOB.printDialog()) {
					JOAConstants.DEFAULT_PRINTERJOB.setPrintable(mMap, JOAConstants.DEFAULT_PAGEFORMAT);
					try {
						JOAConstants.DEFAULT_PRINTERJOB.print();
					}
					catch (PrinterException ex) {
						ex.printStackTrace();
					}
				}
			}
			catch (Exception exx) {
				exx.printStackTrace();
			}

			/*
			 * String sTemp = this.getTitle(); Properties props = new Properties();
			 * PrintJob pj = Toolkit.getDefaultToolkit().getPrintJob(this, "Print
			 * Map", props); if (pj != null) { Graphics g = pj.getGraphics();
			 * Dimension od = mMap.getSize(); Dimension od2 = null; if
			 * (mColorBarLegend != null) od2 = mColorBarLegend.mSwatchPanel.getSize();
			 * Dimension pd = pj.getPageDimension(); int xOffset = (pd.width -
			 * od.width)/2; int yOffset = (pd.height - od.height)/2; // add the title
			 * int size = 12; if (JOAConstants.ISSUNOS) size = 14; g.setFont(new
			 * Font("Courier", Font.BOLD, size)); FontMetrics fm = g.getFontMetrics();
			 * int charWidth = fm.charWidth(' '); int strWidth =
			 * fm.stringWidth(sTemp); int hh = xOffset + od.width/2 - strWidth/2; if
			 * (mColorBarLegend != null) hh = hh - od2.width/2 - 20;
			 * 
			 * int vv = yOffset - 5; g.drawString(sTemp, hh, vv);
			 * 
			 * if (mColorBarLegend != null) { g.translate(xOffset - od2.width/2 - 20,
			 * yOffset); } else { g.translate(xOffset, yOffset); }
			 * 
			 * int fsize = 12; if (JOAConstants.ISSUNOS) fsize = 14; g.setFont(new
			 * Font("Courier", Font.PLAIN, fsize)); mMap.paintComponent(g); // plot
			 * box around map g.setColor(Color.black); g.drawRect(0, 0, od.width,
			 * od.height); // draw the map legend if (mLegend != null) {
			 * g.translate(0, od.height + 10); mLegend.paintComponent(g);
			 * g.translate(0, -od.height - 10); } // draw the color legend
			 * g.setClip(0, 0, 20000, 20000); if (mColorBarLegend != null) {
			 * g.translate(od.width, od.height/2 - od2.height/2);
			 * mColorBarLegend.mSwatchPanel.paintComponent((Graphics2D)g); }
			 * 
			 * g.dispose(); pj.end(); }
			 */
		}
		else {
			mFileViewer.doCommand(cmd, mFrame);
		}
	}

	public void showConfigDialog() {
		// show configuration dialog
		mMap.showConfigDialog();
	}

	public void saveAsPNG() {
		class BasicThread extends Thread {
			// ask for filename
			public void run() {
				Image image = mMap.getCompositeMapImage();

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
		mLegend.reset(mMapSpec);
		Dimension d = mLegend.getPreferredSize();
		mLegendHeight = d.height;
		mMap.invalidate();
		mMap.paintComponent(mMap.getGraphics());
		mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height);
		mFrame.validate();
	}

	public void prefsChanged(PrefsChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		this.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
		mMapSpec.setBGColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
		mMap.setBackground(JOAConstants.DEFAULT_CONTENTS_COLOR);
		if (mColorBarLegend != null) {
			mColorBarLegend.setNewBGColor(JOAConstants.DEFAULT_FRAME_COLOR);
		}
		if (mLegend != null) {
			mLegend.setNewBGColor(JOAConstants.DEFAULT_FRAME_COLOR);
		}
		mMap.invalidate();
		mMap.paintComponent(mMap.getGraphics());
		mFrame.setSize(mFrame.getSize().width + 1, mFrame.getSize().height);
		mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height);
		mFrame.validate();
	}

	public void stnFilterChanged(StnFilterChangedEvent evt) {
		if (mWindowIsLocked) { return; }
		this.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
		mMap.invalidate();
		mMap.paintComponent(mMap.getGraphics());
		mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height);
		mFrame.validate();
	}

	public Dimension getPreferredSize() {
		return new Dimension(mWidth, mHeight);
	}

	public boolean isFirstPlot() {
		return mFirstPlot;
	}

	public void setFirstPlot(boolean first) {
		mFirstPlot = first;
	}

	public void setPercentComplete(double percent) {
		bargauge.setFillPercent(percent);
		bargauge.fill();
	}

	public double getPercentComplete() {
		return (bargauge.getFillPercent());
	}

	public void reset() {
		bargauge.setFillPercent(0);
	}

	public MapLegend getLegend() {
		return mLegend;
	}

	public ColorBarPanel getColorBarPanel(String id) {
		return mColorBarLegend.getColorBarPanel(id);
	}

	public void setLegend(MapLegend legend) {
		mLegend = legend;
	}

	public void addColorBar(String id, NewColorBar cb, boolean linked) {
		mColorBarLegend.addColorBar(id, cb);
	}

	public void replaceColorBar(String id, NewColorBar cb) {
		mColorBarLegend.replaceColorBar(id, cb);
	}

	public void removeColorBar(String id) {
		mColorBarLegend.removeColorBarPanel(id);
	}

	public void displayLocation(double lat, double lon) {
		locToolBar.setLocation(lon, lat, Double.NaN, Double.NaN);
	}

	public void forceRedraw() {
		mMap.invalidate();
		mMap.validate();
	}

	public RubberbandPanel getPanel() {
		return mMap;
	}
	
	public void saveAsKML() {
		class BasicThread extends Thread {
			// ask for filename
			public void run() {
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.endsWith(".kml") || name.endsWith(".kmz")) {
							return true;
						}
						else {
							return false;
						}
					}
				};

				Frame fr = new Frame();
				String directory = System.getProperty("user.dir"); // + File.separator
				FileDialog f = new FileDialog(fr, "Save map KML as:", FileDialog.SAVE);
				f.setDirectory(directory);
				f.setFilenameFilter(filter);
				f.setFile("untitled.kml");
				f.setVisible(true);
				directory = f.getDirectory();
				f.dispose();
				if (directory != null && f.getFile() != null) {
					String path = directory + File.separator + f.getFile();
					File kmlFile = new File(path);
					try {
						writeKMLMap(kmlFile);
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
					try {
						JOAConstants.LogFileStream.writeBytes("Saved KML Map:" + mParent.getTitle() + " as " + path + "\n");
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
	
  public void plotStations(FileWriter fw, int indent) throws Exception {
		double oldU = JOAConstants.MISSINGVALUE, oldV = JOAConstants.MISSINGVALUE;
		double lat;
		double lon;

		try {
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section) of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					// add a folder for each section
					startKMLFolder(fw, sech.getID(), sech.mSectionDescription, indent);
					++indent;

					boolean atSectionBoundary = true;

					// draw the station points
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station) sech.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							// TODO: set the style for a missing value color
							// g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
						}
						else {
							// set the color
							if (sech.mSectionColor != null) {
								// TODO: set the style for this section color
								// g.setColor(sech.mSectionColor);
							}
							else {
								// TODO set to the default color: g.setColor(Color.blue);
							}
						}

						sh.mCurrSymbolSize = mMapSpec.getSymbolSize();
						lat = sh.mLat;
						lon = sh.mLon;
						UVCoordinate uv = new UVCoordinate(lon, lat);
						// draw the station point

						// add the stations as placemarks
						startKMLPlacemark(fw, sh.mStnNum, sh.mStnNum, indent);
						++indent;

						// add a style reference

						// extrude
						writeKMLExtrude(fw, false, indent);

						// tessellate
						writeKMLTeselate(fw, true, indent);

						// add a point
						writeKMLPoint(fw, sh.getStn(), false, uv, 0.0, CLAMPEDTOGROUND, indent);

						--indent;
						endKMLPlacemark(fw, indent);

						if (mMapSpec.getSymbolSize() == 1) {
							// TODO plot a symbol: g.drawLine((int)uv.u + 2, (int)uv.v + 2,
							// (int)uv.u + 2, (int)uv.v + 2);
						}
						else {
							int width = mMapSpec.getSymbolSize();
							// TODO plot a scaled symbol:
							// JOAFormulas.plotSymbol((Graphics2D)g,
							// mMapSpec.getSymbol(), (int)uv.u + 2, (int)uv.v + 2, width);
						}

						// connect if necessary
						if (mMapSpec.isConnectStns() && oldU != JOAConstants.MISSINGVALUE) {
							if (atSectionBoundary) {
								if (mMapSpec.isConnectStnsAcrossSections()) {
									// TODO add a conneection line: g.drawLine((int)oldU + 2,
									// (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
								}
							}
							else {
								// TODO add a connection line: g.drawLine((int)oldU + 2,
								// (int)oldV
								// + 2, (int)uv.u + 2, (int)uv.v + 2);
							}
						}
						atSectionBoundary = false;
						oldU = lon;
						oldV = lat;
					}

					// close folder
					--indent;
					endKMLFolder(fw, indent);
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	public void writeKMLMap(File kmlFile) throws IOException {
		// write Google Earth KML file
		try {
			int indent = 0;
			FileWriter fw = new FileWriter(kmlFile);

			// prolog
			writeKMLProlog(fw);
			++indent;

			startKMLDocument(fw, indent);

			writeKMLLookAt(fw, mMapSpec.getCenLon(), mMapSpec.getCenLat(), indent);

			writeSomeDefaultStyles(fw, indent);
			
			plotStations(fw, indent);
				
			--indent;
			endKMLDocument(fw, indent);

			// postlog
			writeKMLPostlog(fw);

		  fw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    catch (Exception e) {
	    e.printStackTrace();
    }
	}
	
	public void writeIndentLevel(FileWriter fw, int id) {
		for (int i=0; i<id; i++) {
			try {
				fw.write("  ");
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void writeKMLProlog(FileWriter fw) {
		try {
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			fw.write("<kml xmlns=\"http://earth.google.com/kml/2.1\">\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeKMLPostlog(FileWriter fw) {
		try {
			// finish and close doc.kml
			fw.write("</kml>\n");
			fw.write("\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startKMLFolder(FileWriter fw, String folname, String description, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("<Folder>\n");
			writeIndentLevel(fw, id+1);fw.write("<name>" + folname + "</name>\n");
			writeIndentLevel(fw, id+1);fw.write("<open>0</open>\n");
			writeIndentLevel(fw, id+1);fw.write("<description>" + description + "</description>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void endKMLFolder(FileWriter fw, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("</Folder>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startKMLDocument(FileWriter fw, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("<Document>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void endKMLDocument(FileWriter fw, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("</Document>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startKMLPlacemark(FileWriter fw, String name, String description, int id) {
		try {
			writeIndentLevel(fw, id);fw.write(" <Placemark>\n");
			writeIndentLevel(fw, id+1);fw.write("<name>Stn: " + name + "</name>\n");
			writeIndentLevel(fw, id+1);fw.write("<description>\n");
			writeIndentLevel(fw, id+1);fw.write("<![CDATA[\n");
			writeIndentLevel(fw, id+2);fw.write("description" + description + "</b></h1>\n");
			writeIndentLevel(fw, id+1);fw.write("]]>\n");
			writeIndentLevel(fw, id+1);fw.write("</description>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void endKMLPlacemark(FileWriter fw, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("</Placemark>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void writeSomeDefaultStyles(FileWriter fw, int id) {
		String quote = "\"";

		try {
			writeIndentLevel(fw, id);
			fw.write("<Style id=" + quote + "pushpin" + quote + ">\n");
			writeIndentLevel(fw, id);
			fw.write("<IconStyle id=" + quote + "mystyle" + quote + ">\n");
			writeIndentLevel(fw, id);
			fw.write("<Icon>\n");
			writeIndentLevel(fw, id);
			fw.write("<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>" + "\n");
			writeIndentLevel(fw, id);
			fw.write("<scale>1.0</scale>" + "\n");
			writeIndentLevel(fw, id);
			fw.write("</Icon>" + "\n");
			writeIndentLevel(fw, id);
			fw.write("</IconStyle>" + "\n");
			writeIndentLevel(fw, id);
			fw.write("</Style>" + "\n");
		}
		catch (Exception ex) {

		}
	}

	public void defineKMLLineStyle(FileWriter fw, String styleName, Color c, int width, int id) {
		String quote = "\"";
		String rgb = Integer.toHexString(c.getRGB());
		try {
			writeIndentLevel(fw, id);fw.write("<Style id=" + quote + styleName + quote + ">\n");
			writeIndentLevel(fw, id+1);fw.write("<LineStyle>\n");
			writeIndentLevel(fw, id+1);fw.write("<color>" + rgb + "</color>\n");
			writeIndentLevel(fw, id+2);fw.write("<width>" + width + "</width>\n");
			writeIndentLevel(fw, id+2);fw.write("</LineStyle>\n");
			writeIndentLevel(fw, id);fw.write("</Style>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void defineKMLPolyStle(FileWriter fw, String styleName, Color c, int id) {
		String quote = "\"";
		String rgb = Integer.toHexString(c.getRGB());
		try {
			writeIndentLevel(fw, id);fw.write("<Style id=" + quote + styleName + quote + ">\n");
			writeIndentLevel(fw, id+1);fw.write("<PolyStyle>\n");
			writeIndentLevel(fw, id+2);fw.write("<color>" + rgb + "</color>\n");
			writeIndentLevel(fw, id+1);fw.write("</PolyStyle>\n");
			writeIndentLevel(fw, id);fw.write("</Style>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startKMLStyle(FileWriter fw, String styleName, int id) {
		String quote = "\"";
		try {
			writeIndentLevel(fw, id);fw.write("<Style id=" + quote + styleName + quote + ">\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void endKMLStyle(FileWriter fw, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("</Style>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeKMLVisibility(FileWriter fw, boolean viz, int id) throws Exception {
		try {
			if (viz) {
				writeIndentLevel(fw, id);fw.write("<visibility>1</visibility>\n");
			}
			else {
				writeIndentLevel(fw, id);fw.write("<visibility>0</visibility>\n");
			}
		}
		catch (Exception ex) {
			throw ex;
		}
	}

	public void writeKMLExtrude(FileWriter fw, boolean extrudeFlag, int id) throws Exception {
		try {
			if (extrudeFlag) {
				writeIndentLevel(fw, id);fw.write("<extrude>1</extrude>\n");
			}
			else {
				writeIndentLevel(fw, id);fw.write("<extrude>0</extrude>\n");
			}
		}
		catch (Exception ex) {
			throw ex;
		}
	}

	public void writeKMLTeselate(FileWriter fw, boolean tessellateF, int id) throws Exception {
		try {
			if (tessellateF) {
				writeIndentLevel(fw, id);fw.write("<tessellate>1</tessellate>\n");
			}
			else {
				writeIndentLevel(fw, id);fw.write("<tessellate>0</tessellate>\n");
			}
		}
		catch (Exception ex) {
			throw ex;
		}
	}

	public void startGroundOverlay(FileWriter fw, String name, boolean visible, String descrip, Color c, int id) {
		try {		
			String rgb = Integer.toHexString(c.getRGB());

			writeIndentLevel(fw, id);fw.write("<GroundOverlay>\n");
			writeIndentLevel(fw, id+1);fw.write("<name>" + name + ">\n");
			writeKMLVisibility(fw, visible, id+1);
			writeIndentLevel(fw, id+1);fw.write("<description>" + descrip + "</description>\n");
			writeIndentLevel(fw, id+1);fw.write("<color>" + rgb + "</color>");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void endGroundOverlay(FileWriter fw, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("</GroundOverlay>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void defineKMLAltitudeMode(FileWriter fw, String alt, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("<altitudeMode>" + alt + "</altitudeMode>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void defineKMLIcon(FileWriter fw, String url, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("<Icon>\n");
			writeIndentLevel(fw, id+1);fw.write("<href>" + url + "</href>\n");
			writeIndentLevel(fw, id);fw.write("</Icon>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	public void writeKMLLookAt(FileWriter fw, double lon, double lat, int id) {
		try {
			writeKMLLookAt(fw, lon, lat, 8000000.0, id);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeKMLLookAt(FileWriter fw, double lon, double lat, double range, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("<LookAt>\n");
			writeIndentLevel(fw, id+1);fw.write("<longitude>" + lon + "</longitude>\n");
			writeIndentLevel(fw, id+1);fw.write("<latitude>" + lat + "</latitude>\n");
			writeIndentLevel(fw, id+1);fw.write("<range>" + range + "</range>\n");
			writeIndentLevel(fw, id);fw.write("</LookAt>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeKMLRectangle(FileWriter fw, Rectangle[] rect, String altmode, int id, boolean tessellateF) {
		try {
			writeIndentLevel(fw, id);fw.write("<Polygon>\n");
			writeKMLTeselate(fw, tessellateF, id+1);
			defineKMLAltitudeMode(fw, altmode, id+1);
			writeIndentLevel(fw, id+1);fw.write("<outerBoundaryIs>\n");
			writeIndentLevel(fw, id+2);fw.write("<LinearRing>\n");
			writeIndentLevel(fw, id+2);fw.write("<coordinates> " + rect[0].x + "," + rect[0].y + ",1000\n");
			writeIndentLevel(fw, id+3);fw.write(rect[1].x + "," + rect[1].y + ",1000\n");
			writeIndentLevel(fw, id+3);fw.write(rect[2].x + "," + rect[2].y + ",1000\n");
			writeIndentLevel(fw, id+3);fw.write(rect[3].x + "," + rect[3].y + ",1000\n");
			writeIndentLevel(fw, id+3);fw.write(rect[0].x + "," + rect[0].y + ",1000 </coordinates>\n");
			writeIndentLevel(fw, id+2);fw.write("</LinearRing>\n");
			writeIndentLevel(fw, id+1);fw.write("</outerBoundaryIs>\n");
			writeIndentLevel(fw, id);fw.write("</Polygon>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeKMLLineString(FileWriter fw, UVCoordinate[] coords, double altitude, String altMode, int id, boolean tessellateF) {
		try {
			writeIndentLevel(fw, id);fw.write("<LineString>\n");
			writeKMLTeselate(fw, tessellateF, id+1);
			defineKMLAltitudeMode(fw, altMode, id+1);
			writeIndentLevel(fw, id+1);fw.write("<coordinates>");
			for (UVCoordinate c : coords) {
				writeIndentLevel(fw, id+2);fw.write(c.getU() + "," + c.getV() + "," + altitude + "\n");
			}
			writeIndentLevel(fw, id+1);fw.write("</coordinates>\n");
			writeIndentLevel(fw, id);fw.write("</LineString>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

public void writeKMLPoint(FileWriter fw, String ID, boolean extrudeFlag, UVCoordinate coord, double altitude, String altMode, int id) {
	String quote = "\"";
	try {
		writeIndentLevel(fw, id);fw.write("<Point>" + quote + ID + quote + "\n");
		writeKMLExtrude(fw, extrudeFlag, id+1);
		defineKMLAltitudeMode(fw, altMode, id+1);
		writeIndentLevel(fw, id+2);fw.write("<coordinates>\n");
		writeIndentLevel(fw, id+3);fw.write(coord.getU() + "," + coord.getV() + "," + altitude + "\n");
		writeIndentLevel(fw, id+2);fw.write("</coordinates>\n");
		writeIndentLevel(fw, id);fw.write("</Point>\n");
	}
	catch (Exception ex) {
		ex.printStackTrace();
	}
}

	public void writeKMLPolygon(FileWriter fw, UVCoordinate[] coords, double altitude, int id, boolean tessellateF) {
		try {
			writeIndentLevel(fw, id);fw.write("<Polygon>\n");
			writeKMLTeselate(fw, tessellateF, id+1);

			writeIndentLevel(fw, id+1);fw.write("<altitudeMode>relativeToGround</altitudeMode>\n");
			writeIndentLevel(fw, id+1);fw.write("<outerBoundaryIs>\n");
			writeIndentLevel(fw, id+2);fw.write("<LinearRing>\n");
			writeIndentLevel(fw, id+2);fw.write("<coordinates>");
			for (UVCoordinate c : coords) {
				writeIndentLevel(fw, id+3);fw.write(c.getU() + "," + c.getV() + "," + altitude + "\n");
			}
			writeIndentLevel(fw, id+2);fw.write("</coordinates>\n");
			writeIndentLevel(fw, id+2);fw.write("</LinearRing>\n");
			writeIndentLevel(fw, id+1);fw.write("</outerBoundaryIs>\n");
			writeIndentLevel(fw, id);fw.write("</Polygon>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeKMLLatLonBox(FileWriter fw, String b, String e, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("<TimeSpan>\n");
			writeIndentLevel(fw, id+1);fw.write("<begin>" + b + "</begin>\n");
			writeIndentLevel(fw, id+1);fw.write("<end>" + e + "</end>\n");
			writeIndentLevel(fw, id);fw.write("</TimeSpan>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeKMLTimeSpan(FileWriter fw, double maxLat, double minLat, double minLon, double maxLon,
	    double rotation, int id) {

		try {
			writeIndentLevel(fw, id);fw.write("<LatLonBox>\n");
			writeIndentLevel(fw, id+1);fw.write("<north>" + maxLat + "</north>\n");
			writeIndentLevel(fw, id+1);fw.write("<south>" + minLat + "</south>\n");
			writeIndentLevel(fw, id+1);fw.write("<east>" + minLon + "</east>\n");
			writeIndentLevel(fw, id+1);fw.write("<west>" + maxLon + "</west>\n");
			writeIndentLevel(fw, id+1);fw.write("<rotation>" + rotation + "</rotation>\n");
			writeIndentLevel(fw, id);fw.write("</LatLonBox>\n");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeScreenOverlay(FileWriter fw, String name, String icon, int id) {
		try {
			writeIndentLevel(fw, id);fw.write("<ScreenOverlay>\n");
			writeIndentLevel(fw, id+1);fw.write("<name>" +name + "</name>\n");
			writeIndentLevel(fw, id+1);fw.write("<Icon><href>" + icon + "</href></Icon>\n");
			writeIndentLevel(fw, id+1);fw.write("<overlayXY x=\"0.5\" y=\"0.5\" xunits=\"faction\"yunits=\"fraction\"/>\n");
			writeIndentLevel(fw, id+1);fw.write("<screenXY x=\"0.1\" y=\"0.5\" xunits=\"fraction\"yunits=\"fraction\"/>\n");
			writeIndentLevel(fw, id+1);fw.write("<rotationXY x=\"0\" y=\"0\" xunits=\"fraction\"yunits=\"fraction\"/>\n");
			writeIndentLevel(fw, id+1);fw.write("<size x=\"0\" y=\"0\" xunits=\"fraction\"yunits=\"fraction\"/>\n");
			writeIndentLevel(fw, id);fw.write("</ScreenOverlay>\n");		
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}


// public class CMIUtil {
//
// public static void writeFault(FileWriter fw, UnitSource us) {
// Point3D[] rect = us.getRect();
// for (int j = 0; j < rect.length; j++) {
// if (rect[j].x > 180.0) {
// rect[j].x -= 360.0;
// }
// }
// if (rect.length != 4) { return; }
// try {
// fw.write(" <Placemark>\n");
// fw.write(" <name>Fault Plane: " + us.getName() + "</name>\n");
// fw.write(" <description>\n");
// fw.write(" <![CDATA[\n");
// fw.write(" <h1>" + us.getName() + " <b>Slip: " + us.getSlip() +
// "</b></h1>\n");
// fw.write(" <p>Lon: " + us.getLongitude() + "<br>\n");
// fw.write(" Lat: " + us.getLatitude() + "<br>\n");
// fw.write(" Strike: " + us.getStrike() + "<br>\n");
// fw.write(" Dip: " + us.getDip() + "<br>\n");
// fw.write(" Depth: " + us.getDepth() + "<br>\n");
// fw.write(" Length: " + us.getLength() + "<br>\n");
// fw.write(" Width: " + us.getWidth() + "<br>\n");
// fw.write(" Rake: " + us.getRake() + "<br></p>\n");
// fw.write(" ]]>\n");
// fw.write(" </description>\n");
// fw.write(" <styleUrl>#fault</styleUrl>\n");
// fw.write(" <Polygon>\n");
// fw.write(" <tessellate>1</tessellate>\n");
// fw.write(" <altitudeMode>relativeToGround</altitudeMode>\n");
// fw.write(" <outerBoundaryIs>\n");
// fw.write(" <LinearRing>\n");
// fw.write(" <coordinates> " + rect[0].x + ", " + rect[0].y + ", 1000\n");
// fw.write(" " + rect[1].x + ", " + rect[1].y + ", 1000\n");
// fw.write(" " + rect[2].x + ", " + rect[2].y + ", 1000\n");
// fw.write(" " + rect[3].x + ", " + rect[3].y + ", 1000\n");
// fw.write(" " + rect[0].x + ", " + rect[0].y + ", 1000 </coordinates>\n");
// fw.write(" </LinearRing>\n");
// fw.write(" </outerBoundaryIs>\n");
// fw.write(" </Polygon>\n");
// fw.write(" </Placemark>\n");
// }
// catch (IOException ioe) {
// SiftShare.log.log(Level.WARNING, "error", ioe);
// }
// }
//
// // Google Earth
// public static void WriteKMZfile(Object obj, SourceCombo sc, File kmzFile,
// SMaxImagePanel mip, SBathyGridsPanel bgp,
// MostResultsPanel mrp) {
// Rectangle2D.Double rec;
// double minLon, maxLon, minLat, maxLat;
// File maxImageFile, aGridFile, bGridFile, cGridFile;
// DecimalFormat intFormat = new DecimalFormat("000");
// intFormat.setMaximumIntegerDigits(3);
//
// try {
// File imagesDir = new File(CMIUtil.workingDirName + File.separator +
// CMIUtil.currentSiteName + "_run2d"
// + File.separator + "images");
// if (!imagesDir.exists()) {
// imagesDir.mkdir();
// }
// // Should we clean up the images directory here? or let user Clear Model
// // Run files?
//
// // get grid extents
// SiteInfo siteInfo = CMIUtil.currentSiteInfo;
// GridInfo aGridInfo = new GridInfo(new File(siteInfo.getDirName() +
// File.separator + siteInfo.getAGridName()));
// GridInfo bGridInfo = new GridInfo(new File(siteInfo.getDirName() +
// File.separator + siteInfo.getBGridName()));
// GridInfo cGridInfo = new GridInfo(new File(siteInfo.getDirName() +
// File.separator + siteInfo.getCGridName()));
// // get fault-plane extents
// Vector sv = sc.getSources();
//
// // write Google Earth KML file
// File docFile = new File(CMIUtil.workingDirName + File.separator +
// CMIUtil.currentSiteName + "_run2d"
// + File.separator + "doc.kml");
// docFile.delete();
// FileWriter fw = new FileWriter(docFile);
// fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
// fw.write("<kml xmlns=\"http://earth.google.com/kml/2.1\">\n");
// fw.write(" <Document>\n");
// fw.write(" <name>MOST Model Output: " + CMIUtil.currentSiteName + " Mw: "
// + dfx.format(sc.getMomentMagnitude()) + "</name>\n");
// fw.write(" <open>1</open>\n");
// fw.write(" <description>MOST Model Output</description>\n");
// fw.write(" <LookAt>\n");
// fw.write(" <longitude>" + ((aGridInfo.getMaxLon() + aGridInfo.getMinLon()) /
// 2.0) + "</longitude>\n");
// fw.write(" <latitude>" + ((aGridInfo.getMaxLat() + aGridInfo.getMinLat()) /
// 2.0) + "</latitude>\n");
// fw.write(" <range>" + 700000 + "</range>\n");
// fw.write(" </LookAt>\n");
// fw.write(" <Style id=\"transGreen\">\n");
// fw.write(" <LineStyle>\n");
// fw.write(" <color>cf00ff00</color>\n");
// fw.write(" <width>4</width>\n");
// fw.write(" </LineStyle>\n");
// fw.write(" </Style>\n");
// fw.write(" <Style id=\"transYellow\">\n");
// fw.write(" <LineStyle>\n");
// fw.write(" <color>cf00ffff</color>\n");
// fw.write(" <width>4</width>\n");
// fw.write(" </LineStyle>\n");
// fw.write(" </Style>\n");
// fw.write(" <Style id=\"transRed\">\n");
// fw.write(" <LineStyle>\n");
// fw.write(" <color>cf0000ff</color>\n");
// fw.write(" <width>4</width>\n");
// fw.write(" </LineStyle>\n");
// fw.write(" </Style>\n");
// fw.write(" <Style id=\"fault\">\n");
// fw.write(" <LineStyle>\n");
// fw.write(" <color>cfffffcc</color>\n");
// fw.write(" <width>4</width>\n");
// fw.write(" </LineStyle>\n");
// fw.write(" <PolyStyle>\n");
// fw.write(" <color>ff00ff00</color>\n");
// fw.write(" </PolyStyle>\n");
// fw.write(" </Style>\n");
//
// // Folder of Grid outlines
// fw.write(" <Folder>\n");
// fw.write(" <name>MOST Grids</name>\n");
// fw.write(" <open>0</open>\n");
// fw.write(" <description>MOST nested grids</description>\n");
// fw.write(" <Placemark>\n");
// fw.write(" <name>" + CMIUtil.currentSiteName + " A-Grid Outline</name>\n");
// fw.write(" <visibility>1</visibility>\n");
// fw.write(" <description>" + CMIUtil.currentSiteName + " A-Grid
// Outline</description>\n");
// fw.write(" <styleUrl>#transGreen</styleUrl>\n");
// fw.write(" <LineString>\n");
// fw.write(" <tessellate>1</tessellate>\n");
// fw.write(" <altitudeMode>relative</altitudeMode>\n");
// fw.write(" <coordinates> " + aGridInfo.getMinLon() + ", " +
// aGridInfo.getMinLat() + ", 140.0\n");
// fw.write(" " + aGridInfo.getMaxLon() + ", " + aGridInfo.getMinLat() + ",
// 140.0\n");
// fw.write(" " + aGridInfo.getMaxLon() + ", " + aGridInfo.getMaxLat() + ",
// 140.0\n");
// fw.write(" " + aGridInfo.getMinLon() + ", " + aGridInfo.getMaxLat() + ",
// 140.0\n");
// fw.write(" " + aGridInfo.getMinLon() + ", " + aGridInfo.getMinLat() + ",
// 140.0 </coordinates>\n");
// fw.write(" </LineString>\n");
// fw.write(" </Placemark>\n");
//
// bgp.readGrid(CMIUtil.currentSiteName + " A-Grid",
// CMIUtil.currentSiteInfo.getAGrid());
// rec = bgp.getImageCornersLatLon();
// minLon = rec.x;
// maxLat = rec.y;
// maxLon = rec.x + rec.width;
// minLat = rec.y - rec.height;
// aGridFile = new File(imagesDir, "aGrid.jpg");
// bgp.writeMaxImageFile(aGridFile);
//
// fw.write(" <GroundOverlay>\n");
// fw.write(" <name>A-Grid Bathymetry</name>\n");
// fw.write(" <visibility>0</visibility>\n");
// fw.write(" <description>A-Grid Bathymetry [m]</description>\n");
// fw.write(" <color>afffffff</color>");
// fw.write(" <Icon>\n");
// fw.write(" <href>images/aGrid.jpg</href>\n");
// fw.write(" </Icon>\n");
// // fw.write(" <altitude>1000</altitude>\n");
// fw.write(" <altitudeMode>clampedToGround</altitudeMode>\n");
// fw.write(" <LatLonBox>\n");
// fw.write(" <north>" + maxLat + "</north>\n");
// fw.write(" <south>" + minLat + "</south>\n");
// fw.write(" <east>" + minLon + "</east>\n");
// fw.write(" <west>" + maxLon + "</west>\n");
// fw.write(" <rotation>0.0</rotation>\n");
// fw.write(" </LatLonBox>\n");
// fw.write(" </GroundOverlay>\n");
//
// fw.write(" <Placemark>\n");
// fw.write(" <name>" + CMIUtil.currentSiteName + " B-Grid Outline</name>\n");
// fw.write(" <visibility>1</visibility>\n");
// fw.write(" <description>" + CMIUtil.currentSiteName + " B-Grid
// Outline</description>\n");
// fw.write(" <styleUrl>#transYellow</styleUrl>\n");
// fw.write(" <LineString>\n");
// fw.write(" <tessellate>0</tessellate>\n");
// fw.write(" <altitudeMode>relative</altitudeMode>\n");
// fw.write(" <coordinates> " + bGridInfo.getMinLon() + ", " +
// bGridInfo.getMinLat() + ", 140.0\n");
// fw.write(" " + bGridInfo.getMaxLon() + ", " + bGridInfo.getMinLat() + ",
// 140.0\n");
// fw.write(" " + bGridInfo.getMaxLon() + ", " + bGridInfo.getMaxLat() + ",
// 140.0\n");
// fw.write(" " + bGridInfo.getMinLon() + ", " + bGridInfo.getMaxLat() + ",
// 140.0\n");
// fw.write(" " + bGridInfo.getMinLon() + ", " + bGridInfo.getMinLat() + ",
// 140.0 </coordinates>\n");
// fw.write(" </LineString>\n");
// fw.write(" </Placemark>\n");
//
// bgp.readGrid(CMIUtil.currentSiteName + " B-Grid",
// CMIUtil.currentSiteInfo.getBGrid());
// rec = bgp.getImageCornersLatLon();
// minLon = rec.x;
// maxLat = rec.y;
// maxLon = rec.x + rec.width;
// minLat = rec.y - rec.height;
// bGridFile = new File(imagesDir, "bGrid.jpg");
// bgp.writeMaxImageFile(bGridFile);
//
// fw.write(" <GroundOverlay>\n");
// fw.write(" <name>B-Grid Bathymetry</name>\n");
// fw.write(" <visibility>0</visibility>\n");
// fw.write(" <description>B-Grid Bathymetry [m]</description>\n");
// fw.write(" <color>afffffff</color>");
// fw.write(" <Icon>\n");
// fw.write(" <href>images/bGrid.jpg</href>\n");
// fw.write(" </Icon>\n");
// // fw.write(" <altitude>1000</altitude>\n");
// fw.write(" <altitudeMode>clampedToGround</altitudeMode>\n");
// fw.write(" <LatLonBox>\n");
// fw.write(" <north>" + maxLat + "</north>\n");
// fw.write(" <south>" + minLat + "</south>\n");
// fw.write(" <east>" + minLon + "</east>\n");
// fw.write(" <west>" + maxLon + "</west>\n");
// fw.write(" <rotation>0.0</rotation>\n");
// fw.write(" </LatLonBox>\n");
// fw.write(" </GroundOverlay>\n");
//
// fw.write(" <Placemark>\n");
// fw.write(" <name>" + CMIUtil.currentSiteName + " C-Grid Outline</name>\n");
// fw.write(" <visibility>1</visibility>\n");
// fw.write(" <description>" + CMIUtil.currentSiteName + " C-Grid
// Outline</description>\n");
// fw.write(" <styleUrl>#transRed</styleUrl>\n");
// fw.write(" <LineString>\n");
// fw.write(" <tessellate>0</tessellate>\n");
// fw.write(" <altitudeMode>relative</altitudeMode>\n");
// fw.write(" <coordinates> " + cGridInfo.getMinLon() + ", " +
// cGridInfo.getMinLat() + ", 140.0\n");
// fw.write(" " + cGridInfo.getMaxLon() + ", " + cGridInfo.getMinLat() + ",
// 140.0\n");
// fw.write(" " + cGridInfo.getMaxLon() + ", " + cGridInfo.getMaxLat() + ",
// 140.0\n");
// fw.write(" " + cGridInfo.getMinLon() + ", " + cGridInfo.getMaxLat() + ",
// 140.0\n");
// fw.write(" " + cGridInfo.getMinLon() + ", " + cGridInfo.getMinLat() + ",
// 140.0 </coordinates>\n");
// fw.write(" </LineString>\n");
// fw.write(" </Placemark>\n");
//
// bgp.readGrid(CMIUtil.currentSiteName + " C-Grid",
// CMIUtil.currentSiteInfo.getCGrid());
// cGridFile = new File(imagesDir, "cGrid.jpg");
// bgp.writeMaxImageFile(cGridFile);
// rec = bgp.getImageCornersLatLon();
// minLon = rec.x;
// maxLat = rec.y;
// maxLon = rec.x + rec.width;
// minLat = rec.y - rec.height;
//
// fw.write(" <GroundOverlay>\n");
// fw.write(" <name>C-Grid Bathymetry</name>\n");
// fw.write(" <visibility>0</visibility>\n");
// fw.write(" <description>C-Grid Bathymetry [m]</description>\n");
// fw.write(" <color>afffffff</color>");
// fw.write(" <Icon>\n");
// fw.write(" <href>images/cGrid.jpg</href>\n");
// fw.write(" </Icon>\n");
// // fw.write(" <altitude>1000</altitude>\n");
// fw.write(" <altitudeMode>clampedToGround</altitudeMode>\n");
// fw.write(" <LatLonBox>\n");
// fw.write(" <north>" + maxLat + "</north>\n");
// fw.write(" <south>" + minLat + "</south>\n");
// fw.write(" <east>" + minLon + "</east>\n");
// fw.write(" <west>" + maxLon + "</west>\n");
// fw.write(" <rotation>0.0</rotation>\n");
// fw.write(" </LatLonBox>\n");
// fw.write(" </GroundOverlay>\n");
//
// fw.write(" </Folder>\n");
//
// // Folder of fault-planes used
// if (sc.getNumberOfSources() > 0) {
// fw.write(" <Folder>\n");
// fw.write(" <name>Fault Planes</name>\n");
// fw.write(" <open>0</open>\n");
// fw.write(" <visibility>1</visibility>\n");
// fw.write(" <description>Fault Planes used to force MOST
// model</description>\n");
// for (int i = 0; i < sc.getNumberOfSources(); i++) {
// writeFault(fw, (UnitSource) sv.get(i));
// }
// fw.write(" </Folder>\n");
// }
//
// // add MaxAmp plot of C-Grid
// // create maxAmp image file
// maxImageFile = new File(imagesDir, "maxWave.png");
// mip.showTopography(false);
// mip.writeMaxImageFile(maxImageFile);
// mip.showTopography(true);
// // save the colorbar
// File maxImageCBFile = new File(imagesDir, "maxWaveColorbar.png");
// mip.PrintColorbar(maxImageCBFile);
//
// rec = mip.getImageCornersLatLon();
// minLon = rec.x;
// maxLat = rec.y;
// maxLon = rec.x + rec.width;
// minLat = rec.y - rec.height;
//
// fw.write(" <Folder>\n");
// fw.write(" <name>MOST Model Output</name>\n");
// fw.write(" <open>1</open>\n");
// fw.write(" <visibility>1</visibility>\n");
// fw.write(" <description>MOST Model Output</description>\n");
// fw.write(" <GroundOverlay>\n");
// fw.write(" <name>C-Grid Maximum Wave</name>\n");
// fw.write(" <visibility>0</visibility>\n");
// fw.write(" <description>C-Grid Maximum Wave Amplitude [cm]</description>\n");
// fw.write(" <color>afffffff</color>\n");
// fw.write(" <Icon>\n");
// fw.write(" <href>images/maxWave.png</href>\n");
// fw.write(" </Icon>\n");
// // fw.write(" <altitude>1000</altitude>\n");
// fw.write(" <altitudeMode>clampedToGround</altitudeMode>\n");
// fw.write(" <LatLonBox>\n");
// fw.write(" <north>" + maxLat + "</north>\n");
// fw.write(" <south>" + minLat + "</south>\n");
// fw.write(" <east>" + minLon + "</east>\n");
// fw.write(" <west>" + maxLon + "</west>\n");
// fw.write(" <rotation>0.0</rotation>\n");
// fw.write(" </LatLonBox>\n");
// fw.write(" </GroundOverlay>\n");
// fw.write(" <GroundOverlay>\n");
// fw.write(" <name>Maximum Wave Colorbar</name>\n");
// fw.write(" <visibility>0</visibility>\n");
// fw.write(" <description>Colorbar: Wave Amplitude [cm]</description>\n");
// fw.write(" <color>afffffff</color>\n");
// fw.write(" <Icon>\n");
// fw.write(" <href>images/maxWaveColorbar.png</href>\n");
// fw.write(" </Icon>\n");
// // fw.write(" <altitude>1000</altitude>\n");
// fw.write(" <altitudeMode>clampedToGround</altitudeMode>\n");
// fw.write(" <LatLonBox>\n");
// fw.write(" <north>" + maxLat + "</north>\n");
// fw.write(" <south>" + minLat + "</south>\n");
// fw.write(" <east>" + (minLon + 0.02) + "</east>\n");
// fw.write(" <west>" + minLon + "</west>\n");
// fw.write(" <rotation>0.0</rotation>\n");
// fw.write(" </LatLonBox>\n");
// fw.write(" </GroundOverlay>\n");
//
// // write out animation
// int numTimeSteps = mrp.getParamLen();
// // int numTimeSteps = 10;
// SAnimPanel sAnimPanel = mrp.getSAnimPanel();
// String gridName = "";
// String imageFileName = "";
// int gridShowing = mrp.getGridShowing();
// switch (gridShowing) {
// case MostResultsPanel.AGRID:
// gridName = "A-Grid";
// break;
// case MostResultsPanel.BGRID:
// gridName = "B-Grid";
// break;
// case MostResultsPanel.CGRID:
// gridName = "C-Grid";
// break;
// }
//
// // unpackFile(obj,"images/WaveLegend.png",imagesDir,"WaveLegend.png");
// sAnimPanel.PrintColorbar(new File(imagesDir, "WaveLegend.png"));
//
// fw.write(" <Folder>\n");
// fw.write(" <name>" + gridName + " Wave Amplitude animation</name>\n");
// fw.write(" <open>0</open>\n");
// fw.write(" <visibility>1</visibility>\n");
// fw.write(" <description>" + gridName + " Wave Amplitude
// animation</description>\n");
// fw.write(" <ScreenOverlay>\n");
// fw.write(" <name>Wave Amplitude Legend</name>\n");
// fw.write(" <Icon><href>images/WaveLegend.png</href></Icon>\n");
// fw.write(" <overlayXY x=\"0.5\" y=\"0.5\" xunits=\"faction\"
// yunits=\"fraction\"/>\n");
// fw.write(" <screenXY x=\"0.1\" y=\"0.5\" xunits=\"fraction\"
// yunits=\"fraction\"/>\n");
// fw.write(" <rotationXY x=\"0\" y=\"0\" xunits=\"fraction\"
// yunits=\"fraction\"/>\n");
// fw.write(" <size x=\"0\" y=\"0\" xunits=\"fraction\"
// yunits=\"fraction\"/>\n");
// fw.write(" </ScreenOverlay>\n");
// sAnimPanel.showTopography(false);
// for (int step = 0; step < numTimeSteps; step++) {
// switch (gridShowing) {
// case MostResultsPanel.AGRID:
// imageFileName = "gridA" + intFormat.format(step + 1) + ".png";
// break;
// case MostResultsPanel.BGRID:
// imageFileName = "gridB" + intFormat.format(step + 1) + ".png";
// break;
// case MostResultsPanel.CGRID:
// imageFileName = "gridC" + intFormat.format(step + 1) + ".png";
// break;
// }
// sAnimPanel.Print(new File(imagesDir, imageFileName), step);
// rec = sAnimPanel.getImageCornersLatLon();
// minLon = rec.x;
// maxLat = rec.y;
// maxLon = rec.x + rec.width;
// minLat = rec.y - rec.height;
// fw.write(" <GroundOverlay>\n");
// fw.write(" <name>Wave Amplitude at timestep " + (step + 1) + "</name>\n");
// fw.write(" <description>" + gridName + " Maximum Wave Amplitude
// [cm]</description>\n");
// fw.write(" <TimeSpan>\n");
// fw.write(" <begin>" + (step + 1) + "</begin>\n");
// fw.write(" <end>" + (step + 2) + "</end>\n");
// fw.write(" </TimeSpan>\n");
// fw.write(" <Icon>\n");
// fw.write(" <href>images/" + imageFileName + "</href>\n");
// fw.write(" </Icon>\n");
// // fw.write(" <altitude>1000</altitude>\n");
// fw.write(" <altitudeMode>clampedToGround</altitudeMode>\n");
// fw.write(" <LatLonBox>\n");
// fw.write(" <north>" + maxLat + "</north>\n");
// fw.write(" <south>" + minLat + "</south>\n");
// fw.write(" <east>" + minLon + "</east>\n");
// fw.write(" <west>" + maxLon + "</west>\n");
// fw.write(" <rotation>0.0</rotation>\n");
// fw.write(" </LatLonBox>\n");
// fw.write(" </GroundOverlay>\n");
// try {
// Thread.sleep(5);
// }
// catch (InterruptedException ex) {
// return;
// }
// }
// sAnimPanel.showTopography(true);
// fw.write(" </Folder>\n");
//
// // end MOST Model Output folder
// fw.write(" </Folder>\n");
//
// // finish and close doc.kml
// fw.write(" </Document>\n");
// fw.write("</kml>\n");
// fw.write("\n");
// fw.close();
//
// // Zip to a kmz file
// byte[] buf = new byte[1024];
// kmzFile.delete();
// ZipOutputStream out = new ZipOutputStream(new FileOutputStream(kmzFile));
// FileInputStream in = new FileInputStream(docFile);
// out.putNextEntry(new ZipEntry("doc.kml"));
// int len;
// while ((len = in.read(buf)) > 0) {
// out.write(buf, 0, len);
// }
// out.closeEntry();
// in.close();
//
// in = new FileInputStream(maxImageFile);
// out.putNextEntry(new ZipEntry("images/maxWave.png")); // GE cant handle
// // windows file
// // seperator in
// // kmz
// while ((len = in.read(buf)) > 0) {
// out.write(buf, 0, len);
// }
// out.closeEntry();
// in.close();
//
// in = new FileInputStream(maxImageCBFile);
// out.putNextEntry(new ZipEntry("images/maxWaveColorbar.png")); // GE cant
// // handle
// // windows
// // file
// // seperator
// // in kmz
// while ((len = in.read(buf)) > 0) {
// out.write(buf, 0, len);
// }
// out.closeEntry();
// in.close();
//
// in = new FileInputStream(aGridFile);
// out.putNextEntry(new ZipEntry("images/aGrid.jpg")); // GE cant handle
// // windows file
// // seperator in kmz
// while ((len = in.read(buf)) > 0) {
// out.write(buf, 0, len);
// }
// out.closeEntry();
// in.close();
//
// in = new FileInputStream(bGridFile);
// out.putNextEntry(new ZipEntry("images/bGrid.jpg")); // GE cant handle
// // windows file
// // seperator in kmz
// while ((len = in.read(buf)) > 0) {
// out.write(buf, 0, len);
// }
// out.closeEntry();
// in.close();
//
// in = new FileInputStream(cGridFile);
// out.putNextEntry(new ZipEntry("images/cGrid.jpg")); // GE cant handle
// // windows file
// // seperator in kmz
// while ((len = in.read(buf)) > 0) {
// out.write(buf, 0, len);
// }
// out.closeEntry();
// in.close();
//
// cGridFile = new File(imagesDir, "WaveLegend.png");
// in = new FileInputStream(cGridFile);
// out.putNextEntry(new ZipEntry("images/WaveLegend.png")); // GE cant
// // handle
// // windows
// // file
// // seperator
// // in kmz
// while ((len = in.read(buf)) > 0) {
// out.write(buf, 0, len);
// }
// out.closeEntry();
// in.close();
//
// File imageFile;
// // loop over anim images
// for (int step = 0; step < numTimeSteps; step++) {
// switch (gridShowing) {
// case MostResultsPanel.AGRID:
// imageFileName = "gridA" + intFormat.format(step + 1) + ".png";
// break;
// case MostResultsPanel.BGRID:
// imageFileName = "gridB" + intFormat.format(step + 1) + ".png";
// break;
// case MostResultsPanel.CGRID:
// imageFileName = "gridC" + intFormat.format(step + 1) + ".png";
// break;
// }
// imageFile = new File(imagesDir, imageFileName);
//
// in = new FileInputStream(imageFile);
// out.putNextEntry(new ZipEntry("images/" + imageFileName)); // GE cant
// // handle
// // windows
// // file
// // seperator
// // in kmz
// while ((len = in.read(buf)) > 0) {
// out.write(buf, 0, len);
// }
// out.closeEntry();
// in.close();
// }
//
// // close zip file
// out.close();
//
// docFile.delete();
// }
// catch (IOException ioe) {
// SiftShare.log.log(Level.WARNING, "error", ioe);
// }
// }
//
// public static void writeREADME() {
// writeREADME(workingDirName + "/" + currentSiteName + "_run2d/README.txt");
// }
//
// public static void writeREADME(String fn) {
// SiftShare.log.entering("CMIUtil", "writeREADME");
// try {
// FileWriter fw = new FileWriter(fn);
// fw.write("# DO NOT EDIT THIS FILE - GENERATED AUTOMATICALLY BY ComMIT\n");
// fw.write("# this file stores site-specific user settings for running the MOST
// model\n");
// fw.write("# through the Community Model Interface
// (http://nctr.pmel.noaa.gov/ComMIT)\n");
// fw.write("#\n");
// fw.write("#\n");
// fw.write("# Source name(s) and slip(s) for the initial condition:\n");
// fw.write("Initial Condition = \n");
// fw.write("# Timeseries locations:\n");
// fw.write("A-Grid Timeseries Latitude = \n");
// fw.write("A-Grid Timeseries Longitude = \n");
// fw.write("B-Grid Timeseries Latitude = \n");
// fw.write("B-Grid Timeseries Longitude = \n");
// fw.write("C-Grid Timeseries Latitude = \n");
// fw.write("C-Grid Timeseries Longitude = \n");
// fw.write("# Time (in seconds) of first time step in MOST output file:\n");
// fw.write("First Timestep Time = \n");
// fw.write("# maximum Timestep for grids (CFL condition)\n");
// fw.write("Maximum Timestep = \n");
// fw.close();
// }
// catch (Exception e) {
// SiftShare.log.log(Level.WARNING, "error writing README file", e);
// }
// SiftShare.log.exiting("CMIUtil", "writeREADME");
// }
//
// public static void updateREADME(String key, int val) {
// updateREADME(workingDirName + File.separator + currentSiteName + "_run2d" +
// File.separator + "README.txt", key,
// Integer.toString(val));
// }
//
// public static void updateREADME(String fn, String key, int val) {
// updateREADME(fn, key, Integer.toString(val));
// }
//
// public static void updateREADME(String key, double val) {
// updateREADME(workingDirName + File.separator + currentSiteName + "_run2d" +
// File.separator + "README.txt", key,
// Double.toString(val));
// }
//
// public static void updateREADME(String fn, String key, double val) {
// updateREADME(fn, key, Double.toString(val));
// }
//
// public static void updateREADME(String key, String val) {
// updateREADME(workingDirName + File.separator + currentSiteName + "_run2d" +
// File.separator + "README.txt", key,
// val);
// }
//
// public static void updateREADME(String fn, String key, String val) { // key:
// // "Timeseries
// // Latitude
// // = ",
// // val:
// // "19.7892"
// String inputLine = null;
// try {
// File tempFile, readFile;
// tempFile = File.createTempFile("tempCMI", "tmp");
// readFile = new File(fn);
// FileWriter fw = new FileWriter(tempFile, false); // overwrite any old
// // temp file of same
// // name
// FileReader fr = new FileReader(readFile);
// BufferedReader br = new BufferedReader(fr);
// while ((inputLine = br.readLine()) != null) {
//
// if (!inputLine.startsWith(key)) {
// fw.write(inputLine + "\n");
// }
// else {
// fw.write(key + val + "\n");
// }
// }
// fw.close();
// fr.close();
// br.close();
// if (!readFile.delete()) {
// SiftShare.log.warning("cant delete README file");
// }
// // if(!tempFile.renameTo(readFile)) {
// // if (DEBUG) System.out.println("CMIUtil.updateREADME ... cant use
// // renameTo, calling copyFile");
// copyFile(tempFile, readFile); // if File.renameTo() doesn't work
// // }
// tempFile.delete();
// }
// catch (Exception e) {
// SiftShare.log.log(Level.WARNING, "error in updateREADME, key:" + key + "
// value: " + val, e);
// }
// }
//
// public static String readREADME(String key) {
// return readREADME(workingDirName + File.separator + currentSiteName +
// "_run2d" + File.separator + "README.txt",
// key);
// }
//
// public static String readREADME(String fn, String key) { // key should be
// // "Initial
// // Condition = ",
// // etc
// String inputLine = null;
// String result = "";
// try {
// FileReader fr = new FileReader(fn);
// BufferedReader br = new BufferedReader(fr);
// while ((inputLine = br.readLine()) != null) {
// if (inputLine.startsWith(key)) {
// result = inputLine.replaceAll(key, "");
// }
// }
// br.close();
// fr.close();
// }
// catch (IOException e) {
// SiftShare.log.log(Level.WARNING, "error reading README", e);
// }
// return result;
// }
//
// public static void copyFile(File inFile, File outFile) throws IOException {
// FileInputStream fis = new FileInputStream(inFile);
// FileOutputStream fos = new FileOutputStream(outFile);
// byte[] buf = new byte[1024];
// int read = 0;
// while ((read = fis.read(buf)) != -1) {
// fos.write(buf, 0, read);
// }
// fis.close();
// fos.flush();
// fos.close();
// }
//
// public static SourceCombo getSiteCombo(SiteInfo si, EventEditor2 ee) {
// String namesAndSlips = si.getSourceNamesAndSlips();
// return ee.getSourceComboFromString(namesAndSlips);
// }
//
// /**
// * Parse grid file to find min/max Lat and Long, return as Point2D array of
// * "corners"
// */
// public static Point2D.Double[] createGridBox(String path, String gridName) {
// Point2D.Double[] Pt = new Point2D.Double[4];
// double minLat = 0.0, maxLat = 0.0, minLon = 0.0, maxLon = 0.0;
// int type, xind, yind;
// double xllcorner, yllcorner;
// float cellsize, val;
// Double NODATA = null;
// String inputLine;
// String[] inputArray;
// FileReader fr = null;
// BufferedReader br = null;
// if (IsValidMOSTFile(new File(path, gridName)) > 0) {
// try {
// fr = new FileReader(path + File.separator + gridName);
// br = new BufferedReader(fr);
// StreamTokenizer st = new StreamTokenizer(br);
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return null; }
// xind = (int) st.nval;
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return null; }
// yind = (int) st.nval;
// st.nextToken();
// minLon = st.nval;
// for (int i = 1; i < xind - 1; i++) {
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return null; }
// }
// st.nextToken();
// maxLon = st.nval;
// st.nextToken();
// maxLat = st.nval; // WATCH OUT! LAT DECREASES!
// for (int j = 1; j < yind - 1; j++) {
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return null; }
// }
// st.nextToken();
// minLat = st.nval;
// br.close();
// fr.close();
// }
// catch (IOException e) {
// SiftShare.log.log(Level.WARNING, "error reading file: " + path + gridName,
// e);
// }
// finally {
// try {
// br.close();
// }
// catch (Exception ignore) {
// }
// try {
// fr.close();
// }
// catch (Exception ignore) {
// }
// }
//
// }
// else if (IsValidGridFile(new File(path, gridName)) > 0) {
// try {
// fr = new FileReader(path + File.separator + gridName);
// br = new BufferedReader(fr);
// if ((inputLine = br.readLine()) == null) { return Pt; }
// inputArray = inputLine.trim().split("\\s+"); // split on all
// // whitespace
// xind = Integer.parseInt(inputArray[1]);
// if ((inputLine = br.readLine()) == null) { return Pt; }
// inputArray = inputLine.trim().split("\\s+"); // split on all
// // whitespace
// yind = Integer.parseInt(inputArray[1]);
// if ((inputLine = br.readLine()) == null) { return Pt; }
// inputArray = inputLine.trim().split("\\s+"); // split on all
// // whitespace
// xllcorner = Double.parseDouble(inputArray[1]);
// if ((inputLine = br.readLine()) == null) { return Pt; }
// inputArray = inputLine.trim().split("\\s+"); // split on all
// // whitespace
// yllcorner = Double.parseDouble(inputArray[1]);
// if ((inputLine = br.readLine()) == null) { return Pt; }
// inputArray = inputLine.trim().split("\\s+"); // split on all
// // whitespace
// cellsize = Float.parseFloat(inputArray[1]);
// minLon = xllcorner + 0.5 * cellsize;
// maxLon = xllcorner + 0.5 * cellsize + cellsize * (double) (xind - 1);
// minLat = yllcorner + 0.5 * cellsize;// WATCH OUT! LAT DECREASES!
// maxLat = yllcorner + 0.5 * cellsize + cellsize * (double) (yind - 1);
// br.close();
// fr.close();
// }
// catch (IOException e) {
// SiftShare.log.log(Level.WARNING, "error reading file: " + path + gridName,
// e);
// }
// }
//
// // counter-clockwise from lower-left
// Pt[0] = new Point2D.Double(minLon, minLat); // ll
// Pt[1] = new Point2D.Double(maxLon, minLat); // lr
// Pt[2] = new Point2D.Double(maxLon, maxLat); // ur
// Pt[3] = new Point2D.Double(minLon, maxLat); // ul
//
// return Pt;
// }
//
// public static int IsValidGridFile(File f) {
// int type, xind = 0, yind = 0;
// double xllcorner, yllcorner;
// float cellsize, val;
// Double NODATA = null;
// try {
// StreamTokenizer st = new StreamTokenizer(new BufferedReader(new
// FileReader(f)));
// // xind,yind - dimensions of the array
// type = st.nextToken();
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return 0; }
// xind = (int) st.nval;
// type = st.nextToken();
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return 0; }
// yind = (int) st.nval;
// type = st.nextToken();
// type = st.nextToken();
// xllcorner = st.nval;
// if ((xllcorner > 360.0) || (xllcorner < -360.0)) { return -1;// probably
// // a UTM
// // grid
// }
// type = st.nextToken();
// type = st.nextToken();
// yllcorner = st.nval;
// type = st.nextToken();
// type = st.nextToken();
// cellsize = (float) st.nval;
// type = st.nextToken();
// if (type == StreamTokenizer.TT_WORD) {
// type = st.nextToken();
// NODATA = new Double(st.nval);
// }
// for (int j = 0; j < yind; j++) {
// for (int i = 0; i < xind; i++) {
// st.nextToken();
// // don't check type != TT_NUMBER because it may be Fortran sci
// // notation
// if ((type == StreamTokenizer.TT_EOF)) { return 0; }
// }
// }
// }
// catch (IOException ignore) {
// xind = 0;
// yind = 0;
// }
// return xind * yind; // total number of grid points
// }
//
// public static int IsValidMOSTFile(File f) {
// int type, xind = 0, yind = 0;
// try {
// StreamTokenizer st = new StreamTokenizer(new BufferedReader(new
// FileReader(f)));
// // xind,yind - dimensions of the array
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return 0; }
// xind = (int) st.nval;
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return 0; }
// yind = (int) st.nval;
// // xind lines of x-coordinates
// for (int i = 0; i < xind; i++) {
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return 0; }
// }
// // yind lines of y-coordinates
// for (int j = 0; j < yind; j++) {
// type = st.nextToken();
// if ((type != StreamTokenizer.TT_NUMBER) || (type == StreamTokenizer.TT_EOF))
// { return 0; }
// }
// // yind lines of xind values of amplitude
// for (int i = 0; i < xind; i++) {
// for (int j = 0; j < yind; j++) {
// type = st.nextToken();
// // don't check type != TT_NUMBER because it may be Fortran sci
// // notation
// if ((type == StreamTokenizer.TT_EOF)) { return 0; }
// }
// }
// }
// catch (IOException ignore) {
// xind = 0;
// yind = 0;
// }
// return xind * yind; // total number of grid points
// }
//
// // maxTimeStepInGrid() moved to BathyGrid.getMaxTimeStep()
// // maxTimeStepInModelRun() moved to SiteInfo.getMaxTimeStep()
// // creates a netcdf file with the maximum amplitude over time
// public static boolean maxAmplitudeInNetcdf(int icorrunup) {
// SiftShare.log.entering("CMIUtil", "maxAmplitudeInNetcdf");
// boolean isValid = false;
// NetcdfFile ncInFile = null;
// NetcdfFileWriteable ncOutFile = null;
// try {
// String prefix = "";
// // DateFormatter ncdatefmt= new DateFormatter();
// SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
// float missing_value = -1.e+34F;
// if (icorrunup == INITIAL_CONDITION) {
// prefix = workingDirName + File.separator + currentSiteName + "_run2d" +
// File.separator + "linCoh";
// }
// else if (icorrunup == RUNUP) {
// prefix = workingDirName + File.separator + currentSiteName + "_run2d" +
// File.separator + currentSiteName
// + "_runup_ha";
// }
// // ncInFile = NetcdfFile.open(prefix+".nc");
// ncInFile = new NetcdfFile(prefix + ".nc");
// // ncOutFile = NetcdfFileWriteable.createNew(prefix+"_max.nc");
// ncOutFile = new NetcdfFileWriteable();
// ncOutFile.setName(prefix + "_max.nc");
// ArrayDouble.D1 lonA = null, latA = null, timeA = null;
// if (icorrunup == RUNUP) {
// lonA = (ArrayDouble.D1) ncInFile.findVariable("LON").read();
// latA = (ArrayDouble.D1) ncInFile.findVariable("LAT").read();
// timeA = (ArrayDouble.D1) ncInFile.findVariable("TIME").read();
// }
// else {
// lonA = (ArrayDouble.D1) ncInFile.findVariable("lon").read();
// latA = (ArrayDouble.D1) ncInFile.findVariable("lat").read();
// timeA = (ArrayDouble.D1) ncInFile.findVariable("time").read();
// }
// Thread.sleep(5);
// Variable var = null;
// if (icorrunup == RUNUP) {
// var = ncInFile.findVariable("HA");
// }
// else {
// var = ncInFile.findVariable("ha_lc");
// }
// // write dims, vars, attr, call create()
// int nLon = (int) lonA.getSize();
// int nLat = (int) latA.getSize();
// int nTime = (int) timeA.getSize();
// Dimension lonDim = ncOutFile.addDimension("lon", nLon);
// Dimension latDim = ncOutFile.addDimension("lat", nLat);
// Dimension[] dim2 = new Dimension[2];
// dim2[0] = latDim;
// dim2[1] = lonDim;
// ncOutFile.addVariable("lon", double.class, new Dimension[] { lonDim });
// ncOutFile.addVariableAttribute("lon", "units", "degrees_east");
// ncOutFile.addVariableAttribute("lon", "point_spacing", "even");
//
// // double lat(lat) ;
// // lon:units = "degrees_north" ;
// ncOutFile.addVariable("lat", double.class, new Dimension[] { latDim });
// ncOutFile.addVariableAttribute("lat", "units", "degrees_north");
// ncOutFile.addVariableAttribute("lat", "point_spacing", "uneven");
//
// ncOutFile.addVariable("maxAmp", float.class, dim2);
// ncOutFile.addVariableAttribute("maxAmp", "units", "centimeters");
// ncOutFile.addVariableAttribute("maxAmp", "long_name", "Maximum Wave
// Amplitude");
// ncOutFile.addVariableAttribute("maxAmp", "missing_value", missing_value);
// ncOutFile.addVariableAttribute("maxAmp", "_FillValue", missing_value);
//
// ncOutFile.addGlobalAttribute("title", "Maximum Wave Amplitude");
// // String datestr = ncdatefmt.toDateTimeString(new Date());
// String datestr = sdf.format(new Date());
// ncOutFile.addGlobalAttribute("history", datestr + " Created by ComMIT for " +
// currentSiteName + " model");
//
// // create the file
// ncOutFile.create();
// ncOutFile.write("lon", lonA);
// ncOutFile.write("lat", latA);
// ArrayFloat.D2 maxArray = new ArrayFloat.D2(nLat, nLon);
// MAMath.setDouble(maxArray, missing_value);
// ArrayFloat.D3 ampArray = new ArrayFloat.D3(1, nLat, nLon);
// int[] origin = new int[var.getRank()];
// int[] shape = var.getShape();
// shape[0] = 1; // always read just one timestep
// float holder;
// for (int t = 0; t < nTime; t++) {
// origin[0] = t; // time step to read
// ampArray = (ArrayFloat.D3) var.read(origin, shape);
// for (int j = 0; j < nLat; j++) {
// for (int i = 0; i < nLon; i++) {
// holder = ampArray.get(0, j, i);
// if (maxArray.get(j, i) < holder) {
// maxArray.set(j, i, holder);
// }
// }
// }
// Thread.sleep(5);
// }
// ncOutFile.write("maxAmp", maxArray);
// ncOutFile.close();
// ncInFile.close();
// isValid = true;
// }
// catch (IOException ex) {
// SiftShare.log.info("error creating maxAmplitude file (MOST probably exited
// before writing first time step");
// SiftShare.log.log(Level.WARNING, "error creating maxAmp file", ex);
// }
// catch (InvalidRangeException ignore) {
// }
// catch (InterruptedException e) {
// try {
// ncOutFile.close();
// ncInFile.close();
// }
// catch (IOException ignore) {
// }
// }
// SiftShare.log.exiting("CMIUtil", "maxAmplitudeInNetcdf");
// return isValid;
// }
//
// // returns the number of timesteps in a netcdf file with dimension name
// // "TIME"
// public static int timeStepsInNetcdf(String ncPath) {
// int numTimeSteps = 0;
// NetcdfFile nc;
// try {
// nc = new NetcdfFile(ncPath);
// numTimeSteps = nc.findDimension("TIME").getLength();
// nc.close();
// }
// catch (Exception ex) {
// SiftShare.log.info("can't read timesteps in netcdf file");
// numTimeSteps = 0;
// }
// return numTimeSteps;
// }
//
// // returns the first value of the Time axis of MOST netcdf output file
// // (seconds)
// public static double firstTimeInNetcdf(String ncPath) {
// NetcdfFile nc;
// try {
// nc = new NetcdfFile(ncPath);
// Variable ti = nc.findVariable("TIME");
// int[] origin = new int[ti.getRank()];
// int[] shape = ti.getShape();
// double[] te = (double[]) ti.read(new int[] { 0 }, new int[] { 1
// }).copyTo1DJavaArray();
// nc.close();
// return te[0];
// }
// catch (Exception e) {
// SiftShare.log.info("can't read netcdf file (probably not available yet)");
// }
// return -1.0;
// }
//
// public static String getPublishError() {
// return publishError;
// }
//
// public static Boolean publish(SiftShare ss) {
// SiftShare.log.info("attempting to copy all output files and images to server
// directory: " + publishDirName);
// Boolean result = new Boolean("true");
// FileWriter fw;
// String fileName;
// File f;
// File publishDir = new File(publishDirName, currentSiteName + "_run2d");
//
// f = new File(publishDirName);
// if (!f.canWrite()) {
// SiftShare.log.warning("CMIUtil.publish, Error - we don't have write
// permission for: " + publishDirName);
// publishError = "You don't have write permission for publish directory: " +
// publishDirName;
// return new Boolean("false");
// }
// // make subdir: publishDirName/siteName_run2d
// if (publishDir.exists()) {
// deleteDir(publishDir); // delete previously-published results
// }
// if (!publishDir.mkdir()) {
// SiftShare.log.warning("CMIUtil.publish, Error - we can't make directory: " +
// publishDir.getAbsolutePath());
// publishError = "Can't make publish sub-directory: " +
// publishDir.getAbsolutePath();
// return new Boolean("false");
// }
//
// // copy MOST output files (netcdf) to publishDir
// String currentDirName = workingDirName + File.separator + currentSiteName +
// "_run2d";
// File ncFile, ncFilePublish;
// ncFile = new File(currentDirName + File.separator + currentSiteName +
// "_runupA_ha.nc");
// ncFilePublish = new File(publishDir, currentSiteName + "_runupA_ha.nc");
// StringBuffer errorBuffer = new StringBuffer(
// "No Results available: must Launch model for this site before publishing\n");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + currentSiteName +
// "_runupA_ua.nc");
// ncFilePublish = new File(publishDir, currentSiteName + "_runupA_ua.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + currentSiteName +
// "_runupA_va.nc");
// ncFilePublish = new File(publishDir, currentSiteName + "_runupA_va.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + currentSiteName +
// "_runupB_ha.nc");
// ncFilePublish = new File(publishDir, currentSiteName + "_runupB_ha.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + currentSiteName +
// "_runupB_ua.nc");
// ncFilePublish = new File(publishDir, currentSiteName + "_runupB_ua.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + currentSiteName +
// "_runupB_va.nc");
// ncFilePublish = new File(publishDir, currentSiteName + "_runupB_va.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + currentSiteName +
// "_runup_ha.nc");
// ncFilePublish = new File(publishDir, currentSiteName + "_runup_ha.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + currentSiteName +
// "_runup_ua.nc");
// ncFilePublish = new File(publishDir, currentSiteName + "_runup_ua.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + currentSiteName +
// "_runup_va.nc");
// ncFilePublish = new File(publishDir, currentSiteName + "_runup_va.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// // now copy IC files
// ncFile = new File(currentDirName + File.separator + "linCoh.nc");
// ncFilePublish = new File(publishDir, "linCoh.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + "linCou.nc");
// ncFilePublish = new File(publishDir, "linCou.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + "linCov.nc");
// ncFilePublish = new File(publishDir, "linCov.nc");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// // now copy input/output text files
// ncFile = new File(currentDirName + File.separator + "most3_facts_nc.in");
// ncFilePublish = new File(publishDir, "most3_facts_nc.in");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + "output_" +
// currentSiteName + ".lis");
// ncFilePublish = new File(publishDir, "output_" + currentSiteName + ".lis");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
// ncFile = new File(currentDirName + File.separator + "README.txt");
// ncFilePublish = new File(publishDir, "README.txt");
// try {
// copyFile(ncFile, ncFilePublish);
// }
// catch (Exception ex) {
// errorBuffer.append("File: " + ncFile.getAbsolutePath() + " missing.\n");
// result = new Boolean("false");
// }
//
// // now create images!!!!!!
//
// // now write applet file
// int c, nread = 0;
// InputStream is = ss.getClass().getResourceAsStream("applet/CMIApplet.jar");
// try {
// FileOutputStream os = new FileOutputStream(CMIUtil.publishDirName +
// File.separator + CMIUtil.currentSiteName
// + "_run2d" + File.separator + "CMIApplet.jar");
// while ((c = is.read()) != -1) {
// os.write(c);
// }
// if (is != null) {
// is.close();
// }
// if (os != null) {
// os.close();
// }
// }
// catch (IOException e) {
// SiftShare.log.log(Level.WARNING, "error writing CMIApplet.jar", e);
// }
//
// // now write Results.html
// try {
// fw = new FileWriter(publishDirName + File.separator + currentSiteName +
// "_run2d" + File.separator
// + "Results.html", false);
// fw.write("<html>\n");
// fw.write("<head>\n");
// fw.write("<title></title>\n");
// fw.write("</head>\n");
// fw.write("<body>\n");
// fw
// .write("<embed width=\"1073\" height=\"1080\"
// code=\"gov.noaa.tsunami.cmi.CMIApplet\" archive=\"CMIApplet.jar\"\n");
// fw
// .write("type=\"application/x-java-applet;version=1.5\"
// pluginspage=\"http://java.sun.com/j2se/1.5.0/download.html\"></embed>\n");
// fw.write("</body>\n");
// fw.write("</html>\n");
// fw.close();
// }
// catch (IOException e) {
// SiftShare.log.log(Level.WARNING, "error writing Results.html", e);
// }
//
// publishError = errorBuffer.toString();
//
// return result;
// }
//
// public static void updateInfoSZ(String directoryName, javax.swing.JFrame
// mainApp) {
// File dir = new File(directoryName);
// if (!dir.exists()) {
// JOptionPane.showMessageDialog(mainApp, "Directory: " + directoryName + "
// doesn't exist!",
// "Error updating Propagation DB", JOptionPane.ERROR_MESSAGE);
// return;
// }
// if (!dir.canWrite()) {
// JOptionPane.showMessageDialog(mainApp, "You don't have write permission for
// directory: " + directoryName,
// "Error updating Propagation DB", JOptionPane.ERROR_MESSAGE);
// return;
// }
// File infoFile = new File(directoryName, "info_sz.dat");
// // if ( ! infoFile.canWrite() ) {
// // JOptionPane.showMessageDialog(mainApp,"You don't have write permission
// // for info_sz.dat file: "+directoryName+File.separator+"info_sz.dat",
// // "Error updating Propagation DB",JOptionPane.ERROR_MESSAGE);
// // return;
// // }
// java.text.SimpleDateFormat sdf = new
// java.text.SimpleDateFormat("yyyyMMdd-HH-mm");
// String currentTime = sdf.format((Calendar.getInstance()).getTime());
// if (infoFile.exists()) {
// try {
// copyFile(infoFile, new File(directoryName, "info_sz.dat" + currentTime)); //
// save
// // old
// // file
// FileWriter fw = new FileWriter(infoFile, false); // over-write the
// // info_sz.dat file,
// // add header
// fw.write("# Propagation database metadata file\n");
// fw.write("# OpenDAP URL prefix, NONE\n");
// fw
// .write("# Name, Filename/URL, Long(deg), Lat(deg), Slip(m), Strike(deg),
// Dip(deg), Depth(km), Length(km), Width(km), Rake(deg)\n");
// fw.close();
// }
// catch (IOException ex) {
// SiftShare.log.log(Level.WARNING, "can't copy info_sz.dat file", ex);
// }
// }
//
// File[] files = dir.listFiles();
// String fileName;
// try {
// for (File f : files) {
// fileName = f.getCanonicalPath();
// if (fileName.endsWith("ha.nc")) {
// if (new File(fileName.replaceAll("ha.nc", "ua.nc")).exists()
// && new File(fileName.replaceAll("ha.nc", "va.nc")).exists()) {
// addInfoSZline(infoFile, fileName);
// }
// }
// }
// }
// catch (IOException ex) {
// ex.printStackTrace();
// }
// sortInfoSZ(infoFile);
// }
//
// public static void addInfoSZline(File infoFile, String netCDFFileName) {
// try {
// FileWriter fw = new FileWriter(infoFile, true);
// NetcdfFile ncFile = new NetcdfFile(netCDFFileName);
// Attribute att = ncFile.findGlobalAttribute("Source_Zone_Code");
// if (att == null) {
// SiftShare.log.info("old-version Propagation file: " + netCDFFileName);
// ncFile.close();
// fw.close();
// return;
// }
// String zoneCode = att.getStringValue() + "sz";
// String row = ncFile.findGlobalAttribute("Source_Row").getStringValue();
// String col = ncFile.findGlobalAttribute("Source_Column").getStringValue();
// float lat =
// ncFile.findGlobalAttribute("Rectangle_Lower_Edge_Center_Latitude").getNumericValue().floatValue();
// float lon =
// ncFile.findGlobalAttribute("Rectangle_Lower_Edge_Center_Longitude").getNumericValue().floatValue();
// float slip =
// ncFile.findGlobalAttribute("Slip").getNumericValue().floatValue();
// float strike =
// ncFile.findGlobalAttribute("Strike").getNumericValue().floatValue();
// float dip = ncFile.findGlobalAttribute("Dip").getNumericValue().floatValue();
// float depth =
// ncFile.findGlobalAttribute("Depth").getNumericValue().floatValue();
// float len =
// ncFile.findGlobalAttribute("Source_Length").getNumericValue().floatValue();
// float wid =
// ncFile.findGlobalAttribute("Source_Width").getNumericValue().floatValue();
// float rake =
// ncFile.findGlobalAttribute("Rake").getNumericValue().floatValue();
// fw.write(zoneCode + col + row + ", " + netCDFFileName + ", " +
// dfxxxx.format(lon) + ", " + dfxxxx.format(lat)
// + ", " + dfxx.format(slip) + ", " + dfxx.format(strike) + ", " +
// dfxx.format(dip) + ", "
// + dfx.format(depth) + ", " + dfx.format(len) + ", " + dfx.format(wid) + ", "
// + dfx.format(rake) + "\n");
// fw.close();
// ncFile.close();
// }
// catch (Exception e) {
// SiftShare.log.log(Level.WARNING, "error in addInfoSZline: ", e);
// }
// }
//
// public static void removeInfoSZline(File infoFile, String netCDFFileName) {
// StringBuffer sb = new StringBuffer();
// String inputLine;
// try {
// FileReader fr = new FileReader(infoFile);
// BufferedReader br = new BufferedReader(fr);
// while ((inputLine = br.readLine()) != null) {
// if (inputLine.indexOf(netCDFFileName) != -1) {
// sb.append(inputLine);
// }
// }
// br.close();
// fr.close();
// FileWriter fw = new FileWriter(infoFile, false); // overwrite
// fw.write(sb.toString());
// fw.close();
// }
// catch (Exception ex) {
// SiftShare.log.log(Level.WARNING, "error in removeInfoSZline: ", ex);
// }
// }
//
// static final Comparator<String> UNIT_SOURCE_ORDER = new Comparator<String>()
// {
//
// public int compare(String s1, String s2) {
// int result = 0;
// String zone1, zone2, letter1, letter2, hold1, hold2;
// int number1, number2, numberCmp, zoneCmp;
// hold1 = s1.split(",")[0].trim();
// hold2 = s2.split(",")[0].trim();
// if (hold1.indexOf("sz") != 2 || hold2.indexOf("sz") != 2) { return 0; // not
// // in
// // format
// // "iosza23"
// }
// letter1 = hold1.substring(4, 5);
// letter2 = hold2.substring(4, 5);
// zone1 = hold1.substring(0, 4);
// zone2 = hold2.substring(0, 4);
// number1 = Integer.parseInt(hold1.replaceAll(zone1, "").replace(letter1, ""));
// number2 = Integer.parseInt(hold2.replaceAll(zone2, "").replace(letter2, ""));
// zoneCmp = zone1.compareTo(zone2);
// if (number1 == number2) {
// numberCmp = 0;
// }
// else {
// numberCmp = number1 > number2 ? 1 : -1;
// }
// return (zoneCmp != 0 ? zoneCmp : numberCmp != 0 ? numberCmp :
// letter1.compareTo(letter2));
// }
// };
//
// public static void sortInfoSZ(File infoFile) {
// String inputLine;
// ArrayList<String> infoList = new ArrayList<String>(200);
// try {
// FileReader fr = new FileReader(infoFile);
// BufferedReader br = new BufferedReader(fr);
// while ((inputLine = br.readLine()) != null) {
// infoList.add(inputLine);
// }
// br.close();
// fr.close();
// Collections.sort(infoList, UNIT_SOURCE_ORDER);
// FileWriter fw = new FileWriter(infoFile, false); // overwrite
// for (String s : infoList) {
// fw.write(s + "\n");
// }
// fw.close();
// }
// catch (Exception ex) {
// SiftShare.log.log(Level.WARNING, "error in removeInfoSZline: ", ex);
// }
// }
// }
//
// }

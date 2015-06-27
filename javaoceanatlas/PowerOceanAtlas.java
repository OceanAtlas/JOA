/*
 * $Id: PowerOceanAtlas.java,v 1.38 2005/10/18 23:42:18 oz Exp $
 *
 */

package javaoceanatlas;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import gov.noaa.pmel.util.*;
import gov.noaa.pmel.eps2.*;
import gov.noaa.pmel.eps2.dapper.*;
import javax.swing.*;
import ndEdit.*;
import javax.swing.event.*;
import javaoceanatlas.events.*;
import javaoceanatlas.ui.AboutDialog;
import javaoceanatlas.ui.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.io.FileImportException;
import javaoceanatlas.io.FileImportCancelledException;
import gov.noaa.pmel.swing.dapper.*;
import gov.noaa.pmel.nc2.station.*;
import ucar.nc2.Attribute;
import com.apple.eawt.*;
import gov.noaa.pmel.nquery.utility.NQueryFormulas;
import javaoceanatlas.io.SaveSelectedPtrsAction;
import gov.noaa.pmel.nquery.ui.DatabaseDocument;
import gov.noaa.pmel.nquery.ui.NQVariableInspector;
import gov.noaa.pmel.nquery.utility.BrowseSelectedPtrsAction;
import java.sql.DriverManager;
import java.sql.Connection;

public class PowerOceanAtlas extends JOAWindow implements ActionListener, WindowsMenuChangedListener, 
SelectionListener {
	private Vector<JCheckBoxMenuItem> viewCheckBoxes = new Vector<JCheckBoxMenuItem>();
	private Vector viewCheckBoxesActions = new Vector();
	private Vector fileMenuItems = new Vector();
	private Vector fileMenuActions = new Vector();
	private Vector zoomMenuItems = new Vector();
	private Vector zoomMenuActions = new Vector();
	private Vector editMenuItems = new Vector();
	private Vector editMenuActions = new Vector();
	private Vector<JMenuItem> viewMenuItems = new Vector<JMenuItem>();
	private Vector viewMenuActions = new Vector();
	private Vector<JOAViewer> mOpenFileViewers = new Vector<JOAViewer>();
	private Vector<NdEdit> mOpenNdEdits = new Vector<NdEdit>();
	public Vector<PrefsChangedListener> mPrefsChangedListeners = new Vector<PrefsChangedListener>();
	public Vector<WindowsMenuChangedListener> mWindowsMenuChangedListeners = new Vector<WindowsMenuChangedListener>();
	private NdEdit nd = null;
	private DisclosureButton mDiscloseNdEdit = null;
	private JMenu jwindow = null;
	ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	// Application joaApp = Application.getInstance();
	private DapperWizard mDapperWiz = null;
	private static PowerOceanAtlas mInstance = null;
	private boolean mUseNdEdit = true;
  private Preferences mPrefs = null;

	public PowerOceanAtlas() {
		super(true, true, true, true, true, null);
		// MRJApplicationUtils.registerOpenDocumentHandler(this);
		// MRJApplicationUtils.registerOpenApplicationHandler(this);

		String opSys = System.getProperty("os.name");
		JOAConstants.ISMAC = opSys.startsWith("Mac OS");
		JOAConstants.ISMACOSX = opSys.startsWith("Mac OS X");
		JOAConstants.ISSUNOS = opSys.startsWith("Sun");
		String javaVersion = System.getProperty("java.version");
		JOAConstants.ISJAVA14 = javaVersion.startsWith("1.4");
		JOAConstants.LEXICON = new Lexicon();
		try {
	    mDiscloseNdEdit = new DisclosureButton(b.getString("kShowNdedit"), new ImageIcon(getClass().getResource(
	        "images/discloseright.gif")));
    }
    catch (RuntimeException e) {
	    e.printStackTrace();
    }
		try {
	    mDiscloseNdEdit.setSelectedIcon(new ImageIcon(getClass().getResource("images/disclosedown.gif")));
    }
    catch (RuntimeException e) {
	    e.printStackTrace();
    }
    
    int month = 5;
    //int mon,int day,int year,int hour,int min,int sec,int msec
    try {
	    GeoDate gd = new GeoDate(6, 11, 1995, 12, 5, 30, 0);
	    gd.increment(1.0, GeoDate.MONTHS);
    }
    catch (IllegalTimeValue e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
    }

		mDiscloseNdEdit.setBorderPainted(false);
		mDiscloseNdEdit.setContentAreaFilled(false);
		mDiscloseNdEdit.setActionCommand("options");
		mDiscloseNdEdit.addActionListener(this);
		mDiscloseNdEdit.setHorizontalAlignment(SwingConstants.LEFT);
		
		// the preferences at this point are just default values
		// set default values for the Feature Management
		FeatureGroup fg = new FeatureGroup("kFilters", "Filters", true);
		ManagedFeature mf = new ManagedFeature(fg, "knFilter", "Allow Observation Filtering", "1.0", "rel", true, true);
		mf = new ManagedFeature(fg, "kStationFilter", "Allow Station Filtering", "1.0", "rel", true, true);
		fg.addFeature("knFilter", mf);
		JOAConstants.JOA_FEATURESET.put("kFilters", fg);
		
		fg = new FeatureGroup("kFile", "File Options", true);
		mf = new ManagedFeature(fg, "kExportEPICPtr", "Export EPIC Pointer file", "1.0", "rel", true, true);
		fg.addFeature("kExportEPICPtr", mf);
		mf = new ManagedFeature(fg, "kExportEPICPtr", "Allow File Merge", "1.0", "dev", false, true);
		fg.addFeature("kExportEPICPtr", mf);
		mf = new ManagedFeature(fg, "kExportStnCalcs", "Allow Exporting Station Calculations", "1.0", "rel", true, true);
		fg.addFeature("kExportStnCalcs", mf);
		mf = new ManagedFeature(fg, "kOpenDapper", "Access Dapper Servers", "1.0", "rel", true, true);
		fg.addFeature("kOpenDapper", mf);
		mf = new ManagedFeature(fg, "kWOCEExchange", "Export WOCE Exchange Files", "1.0", "rel", false, true);
		fg.addFeature("kWOCEExchange", mf);
		mf = new ManagedFeature(fg, "kExportNetCDF", "Export netCDF Section", "1.0", "rel", true, true);
		fg.addFeature("kExportNetCDF", mf);
		mf = new ManagedFeature(fg, "kExportSpreadsheet", "Export Spreadsheet Files", "1.0", "rel", true, true);	
		fg.addFeature("kExportSpreadsheet", mf);
		mf = new ManagedFeature(fg, "kExportJSON", "Export JSON Products", "1.0", "rel", true, true);	
		fg.addFeature("kExportJSON", mf);
		JOAConstants.JOA_FEATURESET.put("kFile", fg);
			
		fg = new FeatureGroup("kCalculations", "Calculations", true);
		mf = new ManagedFeature(fg, "kStationCalculation", "Allow Station Calculations", "1.0", "rel", true, true);
		fg.addFeature("kStationCalculation", mf);
		mf = new ManagedFeature(fg, "kParameter", "Allow Section Calculations", "1.0", "rel", true, true);
		fg.addFeature("kParameter", mf);
		mf = new ManagedFeature(fg, "kSectionCalculations", "Allow Section Calculations", "1.0", "rel", true, true);
		fg.addFeature("kSectionCalculations", mf);
		mf = new ManagedFeature(fg, "kCustomParameter", "Allow Custom Calculations", "1.0", "rel", true, true);
		fg.addFeature("kCustomParameter", mf);
		mf = new ManagedFeature(fg, "kModelTSRelationship", "Allow Modeling TS relationships", "1.0", "rel", false, true);
		fg.addFeature("kModelTSRelationship", mf);
		mf = new ManagedFeature(fg, "kRecodeValues", "Allow Value Recoding", "1.0", "rel", false, true);
		fg.addFeature("kRecodeValues", mf);
		mf = new ManagedFeature(fg, "kParameterTransformations", "Allow Parameter Transformations", "1.0", "rel", false, true);
		fg.addFeature("kParameterTransformations", mf);
		JOAConstants.JOA_FEATURESET.put("kCalculations", fg);
		
		fg = new FeatureGroup("kNdEdit", "NdEdit Options", false);
		mf = new ManagedFeature(fg, "kNdEdit", "Allow NdEdit Browsing", "1.0", "rel", true, true);
		fg.addFeature("kNdEdit", mf);
		JOAConstants.JOA_FEATURESET.put("kNdEdit", fg);
		
		fg = new FeatureGroup("kMapTab4", "Allow Overlay Contours on Map Plots", true);
		mf = new ManagedFeature(fg, "kStationCalculation", "Overlay Contours Based on a Calculated Station Parameter", "1.0", "rel", true, true);
		fg.addFeature("kStationCalculation", mf);
		mf = new ManagedFeature(fg, "kIsoSurface", "Overlay Contours Based on an Isosurface", "1.0", "rel", true, true);
		fg.addFeature("kIsoSurface", mf);
		JOAConstants.JOA_FEATURESET.put("kMapTab4", fg);
		
		fg = new FeatureGroup("kOverlayContours", "Contour Plots", true);
		mf = new ManagedFeature(fg, "kZGridInterpolation", "Objective Analysis in Interpolation", "0.5", "dev", true, true);
		fg.addFeature("kZGridInterpolation", mf);
		mf = new ManagedFeature(fg, "kOverlayContours", "Overlay Contours", "1.0", "rel", true, true);
		fg.addFeature("kOverlayContours", mf);
		JOAConstants.JOA_FEATURESET.put("kOverlayContours", fg);
		
		fg = new FeatureGroup("kPlots", "Plots", true);
		mf = new ManagedFeature(fg, "kStnPlot", "Allow Station Calculation Plots", "1.0", "rel", true, true);
		fg.addFeature("kStnPlot", mf);
		mf = new ManagedFeature(fg, "kContourGradient", "Allow Gradient Contour Plots", "0.7", "dev", false, true);
		fg.addFeature("kContourGradient", mf);
		mf = new ManagedFeature(fg, "kProfile", "Allow Profile Plots", "1.0", "rel", true, true);
		fg.addFeature("kProfile", mf);
		mf = new ManagedFeature(fg, "kLinePlot", "Allow Line Plots", "1.0", "rel", true, true);
		fg.addFeature("kLinePlot", mf);
		mf = new ManagedFeature(fg, "kMap", "Allow Map Plots", "1.0", "rel", true, true);
		fg.addFeature("kMap", mf);
		mf = new ManagedFeature(fg, "kContour", "Allow Contour Plots", "1.0", "rel", true, true);
		fg.addFeature("kContour", mf);
		mf = new ManagedFeature(fg, "kStnXYPlot", "Allow Station Calculation XY Plots", "1.0", "rel", true, true);	
		fg.addFeature("kStnXYPlot", mf);
		mf = new ManagedFeature(fg, "kPropertyProperty", "Allow Property-Property Plots", "1.0", "rel", true, true);	
		fg.addFeature("kPropertyProperty", mf);
		JOAConstants.JOA_FEATURESET.put("kPlots", fg);
		
		fg = new FeatureGroup("kStationCalculation", "Station Calculations", true);
		mf = new ManagedFeature(fg, "kCalcNS", "Allow Neutral Surface Calculations", "1.0", "rel", true, true);
		fg.addFeature("kCalcNS", mf);
		mf = new ManagedFeature(fg, "kCalcWindSpeed", "Allow Neutral Surface Calculations", "1.0", "rel", true, true);
		fg.addFeature("kCalcWindSpeed", mf);
		mf = new ManagedFeature(fg, "kCalcStnStats", "Allow Station Statistics Calculations", "1.0", "rel", true, true);
		fg.addFeature("kCalcStnStats", mf);
		mf = new ManagedFeature(fg, "kCalcInterp", "Allow Interpolations Calculations", "1.0", "rel", true, true);
		fg.addFeature("kCalcInterp", mf);
		mf = new ManagedFeature(fg, "kCalcInteg", "Allow Interpolations Calculations", "1.0", "rel", true, true);
		fg.addFeature("kCalcInteg", mf);
		mf = new ManagedFeature(fg, "kCalcMLD", "Allow Mixed-layer Depth Calculations", "1.0", "rel", true, true);
		fg.addFeature("kCalcMLD", mf);
		mf = new ManagedFeature(fg, "kCalcExtrema", "Allow Extrema Calculations", "1.0", "rel", true, true);	
		fg.addFeature("kCalcExtrema", mf);
		JOAConstants.JOA_FEATURESET.put("kStationCalculation", fg);

		fg = new FeatureGroup("kStationColors", "Map Station Colors", true);
		mf = new ManagedFeature(fg, "kMetadataStnColors", "Station Coloring Based on Station Metadata", "1.0", "rel", true, true);
		fg.addFeature("kMetadataStnColors", mf);
		mf = new ManagedFeature(fg, "kIsoSurface", "Station Coloring Based on an Isosurface", "1.0", "rel", true, true);
		fg.addFeature("kIsoSurface", mf);
		mf = new ManagedFeature(fg, "kStationCalculationStnColors", "Station Coloring Based on a Calculated Station Parameter", "1.0", "rel", true, true);
		fg.addFeature("kStationCalculationStnColors", mf);
		JOAConstants.JOA_FEATURESET.put("kStationColors", fg);

		fg = new FeatureGroup("kResources", "Resource Editors", true);
		mf = new ManagedFeature(fg, "kSurfaceManager", "Allow Editing/Creating Interpolation Surfaces", "1.0", "rel", true, true);
		fg.addFeature("kSurfaceManager", mf);
		mf = new ManagedFeature(fg, "kContourManager", "Allow Editing/Creating Color Bars", "1.0", "rel", true, true);
		fg.addFeature("kContourManager", mf);
		mf = new ManagedFeature(fg, "kColorPaletteEditor", "Allow Editing/Creating Color Palettes", "1.0", "rel", true, true);
		fg.addFeature("kColorPaletteEditor", mf);
		JOAConstants.JOA_FEATURESET.put("kResources", fg);
		
		fg = new FeatureGroup("kJOAPreferences", "Preferences", true);
		mf = new ManagedFeature(fg, "kGeneral", "Show General Preferences", "1.0", "rel", true, true);
		fg.addFeature("kGeneral", mf);
		mf = new ManagedFeature(fg, "kCTDDecimation", "Show CTD Decimation Preferences", "1.0", "rel", true, true);
		fg.addFeature("kCTDDecimation", mf);
		mf = new ManagedFeature(fg, "kEnhancementOptions", "Show Enhancement Preferences", "1.0", "rel", true, true);
		fg.addFeature("kEnhancementOptions", mf);
		mf = new ManagedFeature(fg, "kParameterSubstitutions", "Show Parameter Substitutions Preferences", "1.0", "rel", true, true);
		fg.addFeature("kParameterSubstitutions", mf);
		mf = new ManagedFeature(fg, "kImport", "Show Import Preferences", "1.0", "rel", true, true);
		fg.addFeature("kImport", mf);
		mf = new ManagedFeature(fg, "kFeatureManagement", "Allow Users to Manage Features", "1.0", "rel", true, true);
		fg.addFeature("kFeatureManagement", mf);
		mf = new ManagedFeature(fg, "kFonts", "Show Font Preferences", "1.0", "rel", true, true);	
		fg.addFeature("kFonts", mf);
		JOAConstants.JOA_FEATURESET.put("kJOAPreferences", fg);
		
		fg = new FeatureGroup("kSectionCalculations", "Section Calculations", true);
		mf = new ManagedFeature(fg, "kSectionDifference", "Allow Section Difference Calculations", "0.5", "dev", true, true);
		fg.addFeature("kSectionDifference", mf);
		mf = new ManagedFeature(fg, "kMeanCast3", "Allow Mean Cast Calculations", "1.0", "rel", true, true);
		fg.addFeature("kMeanCast3", mf);
		JOAConstants.JOA_FEATURESET.put("kSectionCalculations", fg);
		
		fg = new FeatureGroup("kEditMenu", "Section Editors", true);
		mf = new ManagedFeature(fg, "kSectionManager", "Show Section Manager", "1.0", "rel", true, true);
		fg.addFeature("kSectionManager", mf);
		mf = new ManagedFeature(fg, "kParamProperties", "Allow Editing Parameter Properties", "1.0", "rel", true, true);
		fg.addFeature("kParamProperties", mf);
		mf = new ManagedFeature(fg, "kEditPlot", "Allow Station Data Editing", "1.0", "rel", true, true);
		fg.addFeature("kEditPlot", mf);
		mf = new ManagedFeature(fg, "kFileProperties", "Allow Editing File Properties", "1.0", "rel", true, true);
		fg.addFeature("kFileProperties", mf);
		JOAConstants.JOA_FEATURESET.put("kEditMenu", fg);
		
		// read the preferences
		try {
			initPrefs();
		}
		catch (Exception pex) {
			System.out.println("Couldn't read the preferences!");
		}

		try {
	    init();
    }
    catch (RuntimeException e) {
	    e.printStackTrace();
    }

		this.invalidate();
		this.validate();
		this.setVisible(true);
		this.addWindowsMenuChangedListener(this);
		
//		GeoDate testGD = new GeoDate(1337991420L * 1000L);
//		System.out.println(testGD.toString());

		// can JOA connect up to the default database server?
		try {
			String driver = "com.mysql.jdbc.Driver";
			// load the database driver
			Class.forName(driver);
			Connection c = (Connection)DriverManager.getConnection(JOAConstants.DEFAULT_DB_URI + ":" + JOAConstants.DEFAULT_DB_PORT + "/",
					JOAConstants.DEFAULT_DB_NAME, "foobar");
			JOAConstants.CONNECTED_TO_DB = true;
		}
		catch (Exception ex) {
			if (ex.toString().indexOf("Access denied") >= 0) {
				JOAConstants.CONNECTED_TO_DB = true;
			}
		}

		if (JOAConstants.ISMAC) {
			Application application = Application.getApplication();
			application.setEnabledPreferencesMenu(true);
			application.addApplicationListener(new AppleApplicationListener());
		}

	}

	public static PowerOceanAtlas getInstance() {
		if (mInstance == null) {
			mInstance = new PowerOceanAtlas();
		}
		return mInstance;
	}

	public class AppleApplicationListener extends ApplicationAdapter {
		public void handleOpenFile(ApplicationEvent inEvent) {
			File file = new File(inEvent.getFilename());
			handleMacOpenFile(file);
			inEvent.setHandled(true);
		}

		public void handleOpenApplication(ApplicationEvent inEvent) {
			if (inEvent.getFilename() != null) {
				File file = new File(inEvent.getFilename());
				if (file != null) {
					System.out.println("open application sent file = " + file);
					handleMacOpenFile(file);
				}
			}
			inEvent.setHandled(true);
		}

		public void handleQuit(ApplicationEvent inEvent) {
			inEvent.setHandled(true);
			handleMacQuit();
		}

		public void handleAbout(ApplicationEvent inEvent) {
			handleMacAbout();
			inEvent.setHandled(true);
		}

		public void handlePreferences(ApplicationEvent inEvent) {
			handleMacPrefs();
			inEvent.setHandled(true);
		}
	}

	public Vector<JOAViewer> getOpenFileViewers() {
		return mOpenFileViewers;
	}

	public int getNumOpenFileViewer() {
		return mOpenFileViewers.size();
	}

	public void addOpenFileViewer(JOAViewer view) {
		mOpenFileViewers.addElement(view);
	}

	public void removeOpenFileViewer(JOAViewer view) {
		mOpenFileViewers.removeElement(view);
		// kill JOA if this is the last open Windows
		if (mUseNdEdit) {
		if (mOpenFileViewers.size() == 0 && mOpenNdEdits.size() == 0) {
			// close the log file
			try {
				JOAConstants.LogFileStream.flush();
				JOAConstants.LogFileStream.close();
			}
			catch (Exception ex) {
			}
			System.exit(0);
		}
		}
	}

	public void addNdEdit(NdEdit nd) {
		mOpenNdEdits.addElement(nd);
	}

	public void removeNdEdit(NdEdit nd) {
		mOpenNdEdits.removeElement(nd);
		// kill JOA if this is the last open Windows
		if (mOpenFileViewers.size() == 0 && mOpenNdEdits.size() == 0) {
			// close the log file
			if (JOAConstants.LogFileStream != null) {
				try {
					JOAConstants.LogFileStream.flush();
					JOAConstants.LogFileStream.close();
				}
				catch (Exception ex) {
				}
			}
			System.exit(0);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		doCommand(cmd, (JOAWindow) this);
	}

	public void init() {
		/*
		 * try { UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName()); }
		 * catch (Exception e) {
		 */
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex) {

		}
		// }

		Container contents = getContentPane();
		contents.setLayout(new BorderLayout(0, 0));
		final JFrame fr = this;
		fr.setSize(400, 100);
		fr.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				fr.setVisible(false);
				fr.dispose();
			}
		});

		for (int j = 0; j < JOAConstants.gs.length; j++) {
			GraphicsDevice gd = JOAConstants.gs[j];
			JOAConstants.gc = gd.getConfigurations();
		}

		// set a system property so that NdEdit Panel in Dapper knows where to find
		// stuff
		System.setProperty("ndedit.support.dir", System.getProperty("user.dir"));

		// MRJ stuff
		// MRJApplicationUtils.registerQuitHandler(this);
		// MRJApplicationUtils.registerAboutHandler(this);

		FeatureGroup fg = JOAConstants.JOA_FEATURESET.get("kNdEdit");
		if (fg != null && fg.hasFeature("kNdEdit") && !fg.isFeatureEnabled("kNdEdit")) {
			mUseNdEdit = false;
		}

		nd = new NdEdit((Container) fr, false, false);
		NdEditActionList ndalist = nd.getActions();

		if (mUseNdEdit) {
			// instantiate NdEdit and merge menus
			this.addNdEdit(nd);

			WindowListener windowListener = new WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					PowerOceanAtlas.getInstance().removeNdEdit(nd);
					e.getWindow().dispose();
				}
			};
			fr.addWindowListener(windowListener);

			JPanel disclosureCont = new JPanel();
			disclosureCont.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 1));
			disclosureCont.add(mDiscloseNdEdit);
			disclosureCont.add(new JOAJLabel("                                                         "));

			JPanel jp = new JPanel();
			jp.setLayout(new BorderLayout(0, 0));
			jp.add("North", disclosureCont);
			jp.add("Center", nd);
			nd.setVisible(false);
			fr.getContentPane().add(jp);
		}
		fr.setSize(400, 100);
		fr.validate();
		fr.invalidate();

		// build a swing menubar
		JMenuBar menubar = new JMenuBar();
		// JMenuBar.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		// create the menus
		JMenu file = new JMenu(b.getString("kFile"));
		JMenu edit = new JMenu(b.getString("kEditMenu"));
		JMenu view = new JMenu(b.getString("kView"));
		JMenu zoom = new JMenu(b.getString("kZoom"));
		JMenu color = new JMenu(b.getString("kResources"));
		jwindow = new JMenu(b.getString("kWindows"));
		JMenu help;
		if (JOAConstants.ISMACOSX) {
			help = new JMenu(b.getString("kNews"));
		}
		else {
			help = new JMenu(b.getString("kHelp2"));
		}

		// add file menu items
		JMenuItem open = new JMenuItem(b.getString("kOpen"));
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
		    false));
		open.setActionCommand("open");
		file.add(open);

		fg = JOAConstants.JOA_FEATURESET.get("kFile");

		if (fg != null && fg.hasFeature("kOpenDapper") && fg.isFeatureEnabled("kOpenDapper") && fg.hasFeature("kNdEdit")
		    && fg.isFeatureEnabled("kNdEdit")) {
			// add file menu items
			JMenuItem openDapper = new JMenuItem(b.getString("kOpenDapper"));
			openDapper.setActionCommand("opendapper");
			file.add(openDapper);
			file.addSeparator();
			openDapper.addActionListener(this);
		}

		if (mUseNdEdit) {
			// ndedit file commands
			UserSettingsAction prefsAction = (UserSettingsAction) ndalist.getAction("Preferences...");

			Vector fa = ndalist.getActions("file");
			boolean addedSelFilesMenu = false;
			for (int i = 0; i < fa.size(); i++) {
				NdEditAction nda = (NdEditAction) fa.elementAt(i);

				if (!nda.getText().equalsIgnoreCase("exit") && !nda.getText().equalsIgnoreCase("close")
				    && !nda.getText().equalsIgnoreCase("print...")) {
					if (nda.getText().equals("-------")) {
						file.addSeparator();
					}
					else {
						JMenuItem ndItem = new JMenuItem(nda.getText());
						if (nda.getText().equalsIgnoreCase("browse...")) {
							nda.setAccelerator(null);
						}

						fileMenuItems.addElement(ndItem);
						fileMenuActions.addElement(nda);
						KeyStroke ks = nda.getAccelerator();
						if (ks != null) {
							if (!(nda.getText().indexOf("Open") >= 0)) {
								ndItem.setAccelerator(ks);
							}
						}

						if (nda.getText().indexOf("Save Selection") >= 0 && !addedSelFilesMenu) {
							// make a new save item action
							SaveSelectedPtrsAction nnda = null;
              try {
	              nnda = new SaveSelectedPtrsAction("Open Selected Files in New Data Window...",
	                  new ImageIcon("action.gif"), nda.getParent());
              }
              catch (RuntimeException e1) {
	              // TODO Auto-generated catch block
	              e1.printStackTrace();
              }
							JMenuItem nndItem = new JMenuItem(nnda.getText());
							nndItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit()
							    .getMenuShortcutKeyMask()
							    + java.awt.event.InputEvent.ALT_MASK, false));
							nndItem.addActionListener(nnda);
							file.add(nndItem);
							fileMenuItems.addElement(nndItem);
							fileMenuActions.addElement(nnda);

							// make a new save item action
							BrowseSelectedPtrsAction bspa = null;
              try {
	              bspa = new BrowseSelectedPtrsAction("Create New Database From Selection...",
	                  new ImageIcon("action.gif"), nda.getParent());
              }
              catch (RuntimeException e1) {
	              // TODO Auto-generated catch block
	              e1.printStackTrace();
              }
							JMenuItem browseItem = new JMenuItem(bspa.getText());
							browseItem.addActionListener(bspa);
							//file.add(browseItem);
							//fileMenuItems.addElement(browseItem);
							//fileMenuActions.addElement(bspa);

							addedSelFilesMenu = true;
						}
						ndItem.addActionListener(nda);
						file.add(ndItem);
					}
				}
			}
		}

		JMenuItem quit = null;
		if (!JOAConstants.ISMACOSX) {
			quit = new JMenuItem(b.getString("kExit"));
			quit.setActionCommand("quit");
			quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
			    false));
			file.addSeparator();
			file.add(quit);
		}
		menubar.add(file);

		file.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent evt) {
				for (int i = 0; i < fileMenuItems.size(); i++) {
					JMenuItem ndItem = (JMenuItem) fileMenuItems.elementAt(i);
					AbstractAction itemAction = (AbstractAction) fileMenuActions.elementAt(i);
					ndItem.setEnabled(itemAction.isEnabled());
				}
			}

			public void menuDeselected(MenuEvent evt) {

			}

			public void menuCanceled(MenuEvent evt) {

			}
		});

		if (mUseNdEdit) {
			// ndedit edit commands
			Vector ea = ndalist.getActions("edit");
			for (int i = 0; i < ea.size(); i++) {
				NdEditAction nda = (NdEditAction) ea.elementAt(i);
				if (nda.getText().equals("-------")) {
					edit.addSeparator();
				}
				else {
					JMenuItem ndItem = new JMenuItem("NdEdit " + nda.getText());
					editMenuItems.addElement(ndItem);
					editMenuActions.addElement(nda);
					KeyStroke ks = nda.getAccelerator();
					if (ks != null) {
						ndItem.setAccelerator(ks);
					}
					ndItem.addActionListener(nda);
					edit.add(ndItem);
				}
			}
		}

		JMenuItem prefs = new JMenuItem(b.getString("kJOAPreferences"));
		if (!JOAConstants.ISMAC) {
			prefs.setActionCommand("prefs");
			edit.add(prefs);
			menubar.add(edit);
		}

		if (mUseNdEdit) {
			edit.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent evt) {
					for (int i = 0; i < editMenuItems.size(); i++) {
						JMenuItem ndItem = (JMenuItem) editMenuItems.elementAt(i);
						NdEditAction itemAction = (NdEditAction) editMenuActions.elementAt(i);
						ndItem.setEnabled(itemAction.isEnabled());
					}

				}

				public void menuDeselected(MenuEvent evt) {

				}

				public void menuCanceled(MenuEvent evt) {

				}
			});
		}

		if (mUseNdEdit) {
			// ndedit view commands
			Vector va = ndalist.getActions("view");
			for (int i = 0; i < va.size(); i++) {
				NdEditAction nda = (NdEditAction) va.elementAt(i);
				if (nda.getText().equals("-------")) {
					view.addSeparator();
				}
				else {
					if (nda.isCheckBox()) {
						JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(nda.getText());
						viewCheckBoxes.addElement(cbItem);
						viewCheckBoxesActions.addElement(nda);
						KeyStroke ks = cbItem.getAccelerator();
						if (ks != null) {
							cbItem.setAccelerator(ks);
						}
						cbItem.addActionListener(nda);
						view.add(cbItem);
					}
					else {
						JMenuItem ndItem = new JMenuItem(nda.getText());
						viewMenuItems.addElement(ndItem);
						viewMenuActions.addElement(nda);
						KeyStroke ks = nda.getAccelerator();
						if (ks != null) {
							ndItem.setAccelerator(ks);
						}
						ndItem.addActionListener(nda);
						view.add(ndItem);
					}
				}
			}
			menubar.add(view);

			view.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent evt) {
					// set the state of the menu checkboxes
					for (int i = 0; i < viewCheckBoxesActions.size(); i++) {
						JCheckBoxMenuItem cbItem = (JCheckBoxMenuItem) viewCheckBoxes.elementAt(i);
						NdEditAction cbItemAction = (NdEditAction) viewCheckBoxesActions.elementAt(i);
						cbItem.setEnabled(cbItemAction.isEnabled());
						cbItem.setSelected(cbItemAction.getState());
					}

					for (int i = 0; i < viewMenuItems.size(); i++) {
						JMenuItem ndItem = (JMenuItem) viewMenuItems.elementAt(i);
						NdEditAction itemAction = (NdEditAction) viewMenuActions.elementAt(i);
						ndItem.setEnabled(itemAction.isEnabled());
					}
				}

				public void menuDeselected(MenuEvent evt) {

				}

				public void menuCanceled(MenuEvent evt) {

				}
			});

			// ndedit zoom commands
			Vector za = ndalist.getActions("zoom");
			for (int i = 0; i < za.size(); i++) {
				NdEditAction nda = (NdEditAction) za.elementAt(i);
				if (nda.getText().equals("-------")) {
					zoom.addSeparator();
				}
				else {
					JMenuItem ndItem = new JMenuItem(nda.getText());
					zoomMenuItems.addElement(ndItem);
					zoomMenuActions.addElement(nda);
					KeyStroke ks = nda.getAccelerator();
					if (ks != null) {
						ndItem.setAccelerator(ks);
					}
					ndItem.addActionListener(nda);
					zoom.add(ndItem);
				}
			}
			menubar.add(zoom);

			zoom.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent evt) {
					for (int i = 0; i < zoomMenuItems.size(); i++) {
						JMenuItem ndItem = (JMenuItem) zoomMenuItems.elementAt(i);
						NdEditAction itemAction = (NdEditAction) zoomMenuActions.elementAt(i);
						ndItem.setEnabled(itemAction.isEnabled());
					}
				}

				public void menuDeselected(MenuEvent evt) {

				}

				public void menuCanceled(MenuEvent evt) {

				}
			});
		}

		// add resources menu
		JMenuItem contourManager = new JMenuItem(b.getString("kContourManager"));
		contourManager.setActionCommand("contourManager");
		contourManager.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit()
		    .getMenuShortcutKeyMask(), false));
		JMenuItem colorPalEdit = new JMenuItem(b.getString("kColorPaletteEditor"));
		colorPalEdit.setActionCommand("colorPalEdit");
		JMenuItem surfaceEdit = new JMenuItem(b.getString("kSurfaceManager"));
		surfaceEdit.setActionCommand("surfaceEdit");
		// JMenuItem colorMapEdit = new JMenuItem("Color Map Editor...");
		// colorMapEdit.setActionCommand("colorMapEdit");
		colorPalEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit()
		    .getMenuShortcutKeyMask(), false));
		color.add(contourManager);
		color.add(colorPalEdit);
		color.add(surfaceEdit);
		// color.add(colorMapEdit);
		menubar.add(color);
		menubar.add(jwindow);

		// help/news
//		JMenuItem about = null;
//		if (!JOAConstants.ISMACOSX) {
//			about = new JMenuItem(b.getString("kAbout"));
//			about.setActionCommand("about");
//			help.add(about);
//			// add the about menu item
//			help.addSeparator();
//		}
//		JMenuItem joanews = new JMenuItem(b.getString("kJOANews"));
//		joanews.setActionCommand("joanews");
//		help.add(joanews);
//
//		if (mUseNdEdit) {
//			JMenuItem ndeditnews = new JMenuItem(b.getString("kNdEditNews"));
//			ndeditnews.setActionCommand("ndeditnews");
//			help.add(ndeditnews);
//			ndeditnews.addActionListener(this);
//		}

//		menubar.add(help);
		this.setJMenuBar(menubar);

		// add the listeners
		open.addActionListener(this);
		if (quit != null) {
			quit.addActionListener(this);
		}
		prefs.addActionListener(this);
		colorPalEdit.addActionListener(this);
		contourManager.addActionListener(this);
		surfaceEdit.addActionListener(this);
		// colorMapEdit.addActionListener(this);
//		joanews.addActionListener(this);
//		if (!JOAConstants.ISMACOSX) {
//			about.addActionListener(this);
//		}
		
		String joaVersion = "Java OceanAtlas " + VersionInfo.getVersion();

		if (JOAConstants.ISMACOSX && mUseNdEdit) {
			this.setTitle("NdEdit 2.0");
		}
		else if (JOAConstants.ISMACOSX && !mUseNdEdit) {
			this.setTitle(joaVersion);
		}
		else {
			if (mUseNdEdit) {
				this.setTitle(joaVersion + "'/NdEdit 2.0");
			}
			else {
				this.setTitle(joaVersion);
			}
		}
	}

	public static void main(String args[]) {
		if (JOAConstants.ISMAC) { return; }
		PowerOceanAtlas.getInstance();

		//Thread.setDefaultUncaughtExceptionHandler(PowerOceanAtlas.getInstance());

		ResourceBundle bb = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		// create the log file
		try {
			FileOutputStream fos = new FileOutputStream(JOAFormulas.getNewLogFile());
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
			JOAConstants.LogFileStream = new DataOutputStream(bos);
		}
		catch (Exception ex) {
			JFrame f = new JFrame(bb.getString("kLogFileError"));
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(f, bb.getString("kCouldntCreateLogFile"));
		}

		// Tell EPS that the key files are in the JOA_Support folder
		EPS_Util.setKeySubDir("JOA_Support");

		// poainst.init();
		if (args.length >= 0) {
			// attempt to read file(s) from arguments provided
			for (int i = 0; i < args.length; i++) {
				// get directory and file from the argument
				File file = new File(args[i]);

				if (file.isDirectory()) {

				}

				String directory = file.getParent();
				String filename = file.getName();

				if (directory != null && filename != null) {
					// get the file format
					try {
						int fileFormat = EPS_Util.getFileFormat(directory, filename);
						if (fileFormat == EPSConstants.PTRFILEFORMAT || fileFormat == EPSConstants.ZIPSECTIONFILEFORMAT) {
							// create a pointer file database
							int[] sortKeys = { 0, 0, 0, 0 };
							if (fileFormat == EPSConstants.PTRFILEFORMAT) {
								sortKeys[0] = EPSConstants.T_DSC;
							}
							EpicPtrFactory ptrfact = new EpicPtrFactory();
							EpicPtrs prtDB = ptrfact.createEpicPtrs(file, sortKeys, false, true);
							EPSDbase tstDB = new EPSDbase(prtDB.iterator(), false);

							// turn this dbase into a JOA's internal data structures
							Frame fr = new Frame();
							FileViewer fv = new FileViewer(fr, tstDB);
							getInstance().addOpenFileViewer(fv);
							fv.setFile(directory, filename);
							fv.pack();
							fv.setVisible(true);
							fv.setSavedState(JOAConstants.ISCOLLECTION, null);
						}
						else if (fileFormat == EPSConstants.JOPIFORMAT || fileFormat == EPSConstants.WOCEHYDFORMAT) {
							EpicPtrs ptrDB = new EpicPtrs();

							// create a pointer
							EpicPtr epPtr = new EpicPtr(fileFormat, "Bottle Import", "BOTTLE", "na", "na", -99, -99, new GeoDate(),
							    -99, -99, null, filename, directory, null);

							// set the data of ptrDB to this one entry
							ptrDB.setFile(file);
							ptrDB.setData(epPtr);

							// create a database
							PointerDBIterator pdbi = ptrDB.iterator();
							EPSDbase tstDB = new EPSDbase(pdbi, false);

							// turn this dbase into a JOA's internal data structures
							Frame fr = new Frame();
							FileViewer fv = new FileViewer(fr, tstDB);
							getInstance().addOpenFileViewer(fv);
							fv.setFile(directory, filename);
							fv.pack();
							fv.setVisible(true);
						}
						else if (fileFormat == EPSConstants.WOCECTDFORMAT || fileFormat == EPSConstants.NETCDFFORMAT
						    || fileFormat == EPSConstants.XYZFORMAT || fileFormat == EPSConstants.ARGOGDACNETCDFFORMAT) {
							EpicPtrs ptrDB = new EpicPtrs();

							// create a pointer
							EpicPtr epPtr = new EpicPtr(fileFormat, "CTD Import", "CTD", "na", "na", -99, -99, new GeoDate(), -99,
							    -99, null, filename, directory, null);

							// set the data of ptrDB to this one entry
							ptrDB.setFile(new File(directory, filename));
							ptrDB.setData(epPtr);

							// create a database
							PointerDBIterator pdbi = ptrDB.iterator();
							EPSDbase tstDB = new EPSDbase(pdbi, false);

							// turn this dbase into a JOA's internal data structures
							Frame fr = new Frame();
							FileViewer fv = new FileViewer(fr, tstDB);
							getInstance().addOpenFileViewer(fv);
							fv.setFile(directory, filename);
							fv.pack();
							fv.setVisible(true);
						}
						else if (fileFormat == EPSConstants.NETCDFXBTFORMAT) {
							EpicPtrs ptrDB = new EpicPtrs();

							// create a pointer
							EpicPtr epPtr = new EpicPtr(fileFormat, "XBT Import", "XBT", "na", "na", -99, -99, new GeoDate(), -99,
							    -99, null, filename, directory, null);

							// set the data of ptrDB to this one entry
							ptrDB.setFile(new File(directory, filename));
							ptrDB.setData(epPtr);

							// create a database
							PointerDBIterator pdbi = ptrDB.iterator();
							EPSDbase tstDB = new EPSDbase(pdbi, false);

							// turn this dbase into a JOA's internal data structures
							Frame fr = new Frame();
							FileViewer fv = new FileViewer(fr, tstDB);
							getInstance().addOpenFileViewer(fv);
							fv.setFile(directory, filename);
							fv.pack();
							fv.setVisible(true);
						}
						else if (fileFormat == EPSConstants.SSFORMAT || fileFormat == EPSConstants.POAFORMAT
						    || fileFormat == EPSConstants.JOAFORMAT || fileFormat == EPSConstants.SD2FORMAT
						    || fileFormat == EPSConstants.WODCSVFORMAT) {
							// construct a FileViewer with a native file reader
							Frame fr = new Frame();
							FileViewer fv = new FileViewer(fr, file, fileFormat);
							getInstance().addOpenFileViewer(fv);
							fv.setFile(directory, filename);
							fv.pack();
							fv.setVisible(true);
						}
						else {
							JFrame ff = new JFrame("Unknown File Format Error");
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(ff, "The file extension for: " + filename
							    + " is not recognized by Java OceanAtlas.\n" + "Refer to user guide for a list of recognized files.");

						}
					}
					catch (Exception ex) {
						if (!(ex instanceof FileImportCancelledException)) {
							JFrame ff = new JFrame("File Import Error");
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(ff, "An error occurred trying to import " + filename + "\n");
						}
					}
				}
			}
		}

		try {
			JOAFormulas.readGamma();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void handleMacPrefs() {
		JFrame frame = new JFrame();
		ConfigPreferences prf = new ConfigPreferences(frame);
		prf.pack();

		// show dialog at center of screen
		Rectangle dBounds = prf.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		prf.setLocation(x, y);
		prf.setVisible(true);
	}

	public void handleMacOpenFile(File file) {
		String directory = file.getParent();
		String filename = file.getName();
		if (directory != null && filename != null) {
			// get the file format
			try {
				int fileFormat = EPS_Util.getFileFormat(directory, filename);
				if (fileFormat == EPSConstants.PTRFILEFORMAT || fileFormat == EPSConstants.ZIPSECTIONFILEFORMAT) {
					// create a pointer file database
					int[] sortKeys = { 0, 0, 0, 0 };
					if (fileFormat == EPSConstants.PTRFILEFORMAT) {
						sortKeys[0] = EPSConstants.T_DSC;
					}
					try {
						EpicPtrFactory ptrfact = new EpicPtrFactory();
						EpicPtrs prtDB = ptrfact.createEpicPtrs(file, sortKeys, false, true);
						EPSDbase tstDB = new EPSDbase(prtDB.iterator(), false);

						// turn this dbase into a JOA's internal data structures
						Frame fr = new Frame();
						FileViewer fv = new FileViewer(fr, tstDB);
						PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
						fv.setFile(directory, filename);
						fv.pack();
						fv.setVisible(true);
						fv.setSavedState(JOAConstants.ISCOLLECTION, null);
					}
					catch (Exception exx) {

					}
				}
				else if (fileFormat == EPSConstants.JOPIFORMAT || fileFormat == EPSConstants.WOCEHYDFORMAT) {
					EpicPtrs ptrDB = new EpicPtrs();

					// create a pointer
					EpicPtr epPtr = new EpicPtr(fileFormat, "Bottle Import", "BOTTLE", "na", "na", -99, -99, new GeoDate(), -99,
					    -99, null, filename, directory, null);

					// set the data of ptrDB to this one entry
					ptrDB.setFile(file);
					ptrDB.setData(epPtr);

					// create a database
					PointerDBIterator pdbi = ptrDB.iterator();
					EPSDbase tstDB = new EPSDbase(pdbi, false);

					// turn this dbase into a JOA's internal data structures
					Frame fr = new Frame();
					FileViewer fv = new FileViewer(fr, tstDB);
					PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
					fv.setFile(directory, filename);
					fv.pack();
					fv.setVisible(true);
				}
				else if (fileFormat == EPSConstants.WOCECTDFORMAT || fileFormat == EPSConstants.NETCDFFORMAT
				    || fileFormat == EPSConstants.XYZFORMAT || fileFormat == EPSConstants.ARGOGDACNETCDFFORMAT) {
					EpicPtrs ptrDB = new EpicPtrs();

					// create a pointer
					EpicPtr epPtr = new EpicPtr(fileFormat, "CTD Import", "CTD", "na", "na", -99, -99, new GeoDate(), -99, -99,
					    null, filename, directory, null);

					// set the data of ptrDB to this one entry
					ptrDB.setFile(new File(directory, filename));
					ptrDB.setData(epPtr);

					// create a database
					PointerDBIterator pdbi = ptrDB.iterator();
					EPSDbase tstDB = new EPSDbase(pdbi, false);

					// turn this dbase into a JOA's internal data structures
					Frame fr = new Frame();
					FileViewer fv = new FileViewer(fr, tstDB);
					PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
					fv.setFile(directory, filename);
					fv.pack();
					fv.setVisible(true);
				}
				else if (fileFormat == EPSConstants.NETCDFXBTFORMAT) {
					EpicPtrs ptrDB = new EpicPtrs();

					// create a pointer
					EpicPtr epPtr = new EpicPtr(fileFormat, "XBT Import", "XBT", "na", "na", -99, -99, new GeoDate(), -99, -99,
					    null, filename, directory, null);

					// set the data of ptrDB to this one entry
					ptrDB.setFile(new File(directory, filename));
					ptrDB.setData(epPtr);

					// create a database
					PointerDBIterator pdbi = ptrDB.iterator();
					EPSDbase tstDB = new EPSDbase(pdbi, false);

					// turn this dbase into a JOA's internal data structures
					Frame fr = new Frame();
					FileViewer fv = new FileViewer(fr, tstDB);
					PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
					fv.setFile(directory, filename);
					fv.pack();
					fv.setVisible(true);
				}
				else if (fileFormat == EPSConstants.SSFORMAT || fileFormat == EPSConstants.POAFORMAT
				    || fileFormat == EPSConstants.JOAFORMAT || fileFormat == EPSConstants.SD2FORMAT
				    || fileFormat == EPSConstants.WODCSVFORMAT) {
					// construct a FileViewer with a native file reader
					Frame fr = new Frame();
					FileViewer fv = new FileViewer(fr, file, fileFormat);
					PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
					fv.setFile(directory, filename);
					fv.pack();
					fv.setVisible(true);
				}
				else {
					JFrame ff = new JFrame("Unknown File Format Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(ff, "The file extension for: " + filename
					    + " is not recognized by Java OceanAtlas.\n" + "Refer to user guide for a list of recognized files.");
				}
			}
			catch (Exception ex) {
				if (!(ex instanceof FileImportCancelledException)) {
					JFrame ff = new JFrame("File Import Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(ff, "#2 An error occurred trying to import " + filename + "." + "\n"
					    + "The file may be a newer version than allowed by this version of JOA");
					ex.printStackTrace();
				}
			}
		}
	}

	public void handleMacOpenApplication() {
	}

	public void doCommand(String cmd, JOAWindow frame) {
		if (cmd.equals("close")) {
			this.dispose();
		}
		else if (cmd.equals("options")) {
			if (mDiscloseNdEdit.isSelected()) {
				nd.setVisible(true);
				this.setSize(990, 500);
				this.invalidate();
				this.validate();
			}
			else {
				nd.setVisible(false);
				this.pack();
			}
		}
		else if (cmd.equals("help")) {
			/*
			 * QBSetupHelpFrame ff = new QBSetupHelpFrame(this, "Quality Byte Setup
			 * Help"); ff.init(); ff.pack(); ff.setVisible(true);
			 */
		}
		else if (cmd.equals("about")) {
			AboutDialog ff = new AboutDialog(frame, true);
			// ff.init();
			ff.pack();

			// show dialog at center of screen
			Rectangle dBounds = ff.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			ff.setLocation(x, y);
			ff.setVisible(true);
		}
		else if (cmd.equals("ndeditnews")) {
			try {
				JFrame f = new JFrame("NdEdit News");
				JEditorPane ep = new JEditorPane();
				ep.setEditable(false);
				JScrollPane sp = new JScrollPane(ep);
				f.getContentPane().add(sp);
				ep.setPage("http://www.oceanatlas.com/ndedit_news.html");
				f.setSize(500, 400);
				f.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
					}
				});

				// show dialog at center of screen
				Rectangle dBounds = f.getBounds();
				Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				int x = sd.width / 2 - dBounds.width / 2;
				int y = sd.height / 2 - dBounds.height / 2;
				f.setLocation(x, y);
				f.setVisible(true);
			}
			catch (Exception ex) {
				JFrame f = new JFrame("News Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Future Feature.\n" + "Questions? Mail to oz@oceanatlas.com");
			}
		}
		else if (cmd.equals("joanews")) {
			try {
				JFrame f = new JFrame("JOA News");
				JEditorPane ep = new JEditorPane();
				ep.setEditable(false);
				JScrollPane sp = new JScrollPane(ep);
				f.getContentPane().add(sp);
				ep.setPage("http://www.oceanatlas.com/java_oceanatlas_news.html");
				f.setSize(500, 400);
				f.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
					}
				});

				// show dialog at center of screen
				Rectangle dBounds = f.getBounds();
				Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				int x = sd.width / 2 - dBounds.width / 2;
				int y = sd.height / 2 - dBounds.height / 2;
				f.setLocation(x, y);
				f.setVisible(true);
			}
			catch (Exception ex) {
				JFrame f = new JFrame("News Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "An error occurred loading the JOA News URL.\n"
				    + "Send mail to jswift@ucsd.edu");
			}
		}
		else if (cmd.equals("quit")) {
			// close the log file
			try {
				JOAConstants.LogFileStream.flush();
				JOAConstants.LogFileStream.close();
			}
			catch (Exception ex) {
			}
			System.exit(0);
		}
		else if (cmd.equals("opendapper")) {
			if (mDapperWiz == null) {
				mDapperWiz = new DapperWizard(JOAConstants.DEFAULT_DAPPER_SERVERS, false);
				mDapperWiz.addSelectionListener(this);
			}
			else {
				mDapperWiz.reset();
			}
			mDapperWiz.setVisible(true);
		}
		else if (cmd.equals("opendods")) {
			boolean keepAsking = true;
			while (keepAsking) {
				javaoceanatlas.ui.OPeNDAPSelectionDialog dsd_ = new javaoceanatlas.ui.OPeNDAPSelectionDialog();
				dsd_.setModal(true);
				dsd_.setVisible(true);
				String dods = dsd_.getOPeNDAPPath();
				if (dods != null) {
					EpicPtrs ptrDB = new EpicPtrs();

					// create a pointer
					EpicPtr epPtr = new EpicPtr(EPSConstants.DODSNETCDFFORMAT, "DODS Import", "UNK", "na", "na", -99, -99,
					    new GeoDate(), -99, -99, null, dods, dods, null);

					// set the data of ptrDB to this one entry
					// ptrDB.setFile(new File(mDirectory, f.getFile()));
					ptrDB.setURL(dods);
					ptrDB.setData(epPtr);

					// create a database
					PointerDBIterator pdbi = ptrDB.iterator();
					EPSDbase tstDB = new EPSDbase(pdbi, false);

					// turn this dbase into a JOA's internal data structures
					Frame fr = new Frame();
					FileViewer fv = new FileViewer(fr, tstDB);
					PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
					fv.setFile(dods, dods);
					fv.pack();
					fv.setVisible(true);
					keepAsking = false;
				}
			}
		}
		else if (cmd.equals("open")) {
			boolean keepAsking = true;
			while (keepAsking) {
				FileDialog f = new FileDialog(this, "Open File", FileDialog.LOAD);
				// f.setDirectory(mDirectory);
				f.setVisible(true);
				String mDirectory = f.getDirectory();
				f.dispose();
				if (mDirectory != null && f.getFile() != null) {
					// get the file format
					try {
						int fileFormat = EPS_Util.getFileFormat(mDirectory, f.getFile());
						if (fileFormat == EPSConstants.XMLPTRFILEFORMAT) {
							File inFile = new File(mDirectory, f.getFile());
							keepAsking = false;
							try {
								XMLPtrFileReader xmlReader = new XMLPtrFileReader(inFile);
								ArrayList al = xmlReader.parse();

								NQVariableInspector varUI = new NQVariableInspector();
								NQueryFormulas.centerFrameOnScreen(varUI, false);
								varUI.setAttributes(xmlReader.getAttributes());
								varUI.setFileSets(xmlReader.getFileSets());
								varUI.setVisible(true);
								keepAsking = false;

								this.addOpenFileViewer(varUI);
								WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(this);
								Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
							}
							catch (Exception ex) {
								ex.printStackTrace();
								keepAsking = true;
							}
						}
						else if (fileFormat == EPSConstants.NQDBFORMAT) {
							File inFile = new File(mDirectory, f.getFile());
							keepAsking = false;
							try {
								DatabaseDocument doc = new DatabaseDocument(inFile);
								NQueryFormulas.centerFrameOnScreen(doc, false);
								doc.setVisible(true);
								keepAsking = false;
							}
							catch (Exception ex) {
								ex.printStackTrace();
								keepAsking = true;
							}
						}
						else if (fileFormat == EPSConstants.PTRFILEFORMAT || fileFormat == EPSConstants.ZIPSECTIONFILEFORMAT) {
							// create a pointer file database
							int[] sortKeys = { 0, 0, 0, 0 };
							if (fileFormat == EPSConstants.PTRFILEFORMAT) {
								sortKeys[0] = EPSConstants.T_DSC;
							}

							try {
								EpicPtrFactory ptrfact = new EpicPtrFactory();
								EpicPtrs prtDB = ptrfact.createEpicPtrs(new File(mDirectory, f.getFile()), sortKeys, false, true);
								EPSDbase tstDB = new EPSDbase(prtDB.iterator(), false);

								// turn this dbase into a JOA's internal data structures
								Frame fr = new Frame();
								FileViewer fv = new FileViewer(fr, tstDB);
								PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
								fv.setFile(mDirectory, f.getFile());
								fv.pack();
								fv.setVisible(true);
								keepAsking = false;
								fv.setSavedState(JOAConstants.ISCOLLECTION, null);
							}
							catch (Exception exx) {
								exx.printStackTrace();
							}
						}
						else if (fileFormat == EPSConstants.JOPIFORMAT || /*
																															 * fileFormat ==
																															 * EPSConstants.JOAFORMAT ||
																															 * fileFormat ==
																															 * EPSConstants.SSFORMAT ||
																															 * fileFormat ==
																															 * EPSConstants.SD2FORMAT ||
																															 */
						fileFormat == EPSConstants.WOCEHYDFORMAT) {
							EPSDbase tstDB = null;
							try {
								EpicPtrs ptrDB = new EpicPtrs();

								// create a pointer
								EpicPtr epPtr = new EpicPtr(fileFormat, "Bottle Import", "BOTTLE", "na", "na", -99, -99, new GeoDate(),
								    -99, -99, null, f.getFile(), mDirectory, null);

								// set the data of ptrDB to this one entry
								ptrDB.setFile(new File(mDirectory, f.getFile()));
								ptrDB.setData(epPtr);

								// create a database
								PointerDBIterator pdbi = ptrDB.iterator();
								tstDB = new EPSDbase(pdbi, false);
							}
							catch (Exception exx) {
								exx.printStackTrace();
							}
							// turn this dbase into a JOA's internal data structures
							Frame fr = new Frame();
							FileViewer fv = new FileViewer(fr, tstDB);
							PowerOceanAtlas.getInstance().addOpenFileViewer(fv);

							fv.setFile(mDirectory, f.getFile());
							fv.pack();
							fv.setVisible(true);
							keepAsking = false;
						}
						else if (fileFormat == EPSConstants.WOCECTDFORMAT || fileFormat == EPSConstants.NETCDFFORMAT
						    || fileFormat == EPSConstants.XYZFORMAT || fileFormat == EPSConstants.ARGOGDACNETCDFFORMAT) {
							EpicPtrs ptrDB = new EpicPtrs();

							// create a pointer
							EpicPtr epPtr = new EpicPtr(fileFormat, "CTD Import", "CTD", "na", "na", -99, -99, new GeoDate(), -99,
							    -99, null, f.getFile(), mDirectory, null);

							// set the data of ptrDB to this one entry
							ptrDB.setFile(new File(mDirectory, f.getFile()));
							ptrDB.setData(epPtr);

							// create a database
							PointerDBIterator pdbi = ptrDB.iterator();
							EPSDbase tstDB = new EPSDbase(pdbi, false);

							// turn this dbase into a JOA's internal data structures
							Frame fr = new Frame();
							FileViewer fv = new FileViewer(fr, tstDB);
							getInstance().addOpenFileViewer(fv);
							fv.setFile(mDirectory, f.getFile());
							fv.pack();
							fv.setVisible(true);
							keepAsking = false;
						}
						else if (fileFormat == EPSConstants.NETCDFXBTFORMAT) {
							EpicPtrs ptrDB = new EpicPtrs();

							// create a pointer
							EpicPtr epPtr = new EpicPtr(fileFormat, "XBT Import", "XBT", "na", "na", -99, -99, new GeoDate(), -99,
							    -99, null, f.getFile(), mDirectory, null);

							// set the data of ptrDB to this one entry
							ptrDB.setFile(new File(mDirectory, f.getFile()));
							ptrDB.setData(epPtr);

							// create a database
							PointerDBIterator pdbi = ptrDB.iterator();
							EPSDbase tstDB = new EPSDbase(pdbi, false);

							// turn this dbase into a JOA's internal data structures
							Frame fr = new Frame();
							FileViewer fv = new FileViewer(fr, tstDB);
							getInstance().addOpenFileViewer(fv);
							fv.setFile(mDirectory, f.getFile());
							fv.pack();
							fv.setVisible(true);
							keepAsking = false;
						}
						else if (fileFormat == EPSConstants.SSFORMAT || fileFormat == EPSConstants.POAFORMAT
						    || fileFormat == EPSConstants.JOAFORMAT || fileFormat == EPSConstants.SD2FORMAT
						    || fileFormat == EPSConstants.WODCSVFORMAT) {
							// construct a FileViewer with a native file reader
							Frame fr = new Frame();
							FileViewer fv = new FileViewer(fr, new File(mDirectory, f.getFile()), fileFormat);
							getInstance().addOpenFileViewer(fv);
							fv.setFile(mDirectory, f.getFile());
							fv.pack();
							fv.setVisible(true);
							keepAsking = false;
						}
						else {
							JFrame ff = new JFrame("Unknown File Format Error");
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(ff, "The file extension for: " + f.getFile()
							    + " is not recognized by Java OceanAtlas.\n" + "Refer to user guide for a list of recognized files.");
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
						if (!(ex instanceof FileImportCancelledException)) {
							String errStr = null;
							if (ex instanceof FileImportException) {
								errStr = ((FileImportException) ex).getErrorType();
								errStr = errStr + "\n" + "Error occurred on line #" + ((FileImportException) ex).getErrorLine();
							}
							else {
								errStr = "An unknown error occurred trying to read " + f.getFile() + ".";
							}
							JFrame ff = new JFrame("File Read Error");
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(ff, errStr);
						}
						else {
							keepAsking = false;
						}
					}
				}
				else {
					keepAsking = false;
				}
			}

			WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(this);
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
		}
		else if (cmd.equals("browse")) {
			/*
			 * // open up ndedit in this frame final JFrame fr = this; fr.setSize(900,
			 * 700); //fr.setBackground(new Color(200, 200, 200));
			 * fr.addWindowListener(new WindowAdapter() { public void
			 * windowClosing(WindowEvent e) { fr.setVisible(false); fr.dispose(); }
			 * });
			 * 
			 * 
			 * NdEdit nd = new NdEdit(fr, false);
			 * 
			 * UserSettings uset = new UserSettings("myUserSettings.txt");
			 * nd.setUserSettings(uset);
			 * 
			 * //UserSettingsAction userSettingsAction = new UserSettingsAction("User
			 * Settings ...", new ImageIcon("action.gif")), uset);
			 * //userSettingsAction.addChangeListener(nd); // Create a menu bar and
			 * give it a bevel border //NdEditMenuBar mMenuBar = new NdEditMenuBar(fr,
			 * nd, false); fr.getContentPane().add(nd); fr.validate();
			 * fr.invalidate();
			 */
		}
		else if (cmd.equals("colorPalEdit")) {
			// Open the calculations dialog
			ConfigColorPalette colorPalDialog = new ConfigColorPalette(frame);
			colorPalDialog.pack();

			// show dialog at center of screen
			Rectangle dBounds = colorPalDialog.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			colorPalDialog.setLocation(x, y);

			colorPalDialog.setVisible(true);
		}
		else if (cmd.equals("contourManager")) {
			// Open the calculations dialog
			ConfigColorbar colorbarDialog = new ConfigColorbar(frame, null);
			colorbarDialog.pack();
			Dimension d = colorbarDialog.getSize();
			colorbarDialog.setSize(d.width + 100, d.height);

			// show dialog at center of screen
			Rectangle dBounds = colorbarDialog.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			colorbarDialog.setLocation(x, y);
			colorbarDialog.setVisible(true);
		}
		else if (cmd.equals("surfaceEdit")) {
			// Open the calculations dialog
			ConfigSurface surfaceDialog = new ConfigSurface(frame, null);
			surfaceDialog.pack();
			// if (JOAConstants.ISMAC)
			// surfaceDialog.setSize(230, 180);
			// else
			// surfaceDialog.setSize(245, 255);

			// surfaceDialog.pack();

			// show dialog at center of screen
			Rectangle dBounds = surfaceDialog.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			surfaceDialog.setLocation(x, y);

			surfaceDialog.setVisible(true);
		}
		else if (cmd.equals("prefs")) {
			// Open the preferences dialog
			ConfigPreferences prf = new ConfigPreferences(frame);
			prf.pack();

			// show dialog at center of screen
			Rectangle dBounds = prf.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			prf.setLocation(x, y);
			prf.setVisible(true);
		}
	}

	public void addPrefsChangedListener(PrefsChangedListener l) {
		if (mPrefsChangedListeners.indexOf(l) < 0) {
			mPrefsChangedListeners.addElement(l);
		}
	}

	public void removePrefsChangedListener(PrefsChangedListener l) {
		mPrefsChangedListeners.removeElement(l);
	}

	public void addWindowsMenuChangedListener(WindowsMenuChangedListener l) {
		if (mWindowsMenuChangedListeners.indexOf(l) < 0) {
			mWindowsMenuChangedListeners.addElement(l);
		}
	}

	public void removeWindowsMenuChangedListener(WindowsMenuChangedListener l) {
		mWindowsMenuChangedListeners.removeElement(l);
	}

	public void processEvent(AWTEvent evt) {
		if (evt instanceof PrefsChangedEvent) {
			JOAFormulas.createLatFormatter();
			JOAFormulas.createLonFormatter();
			if (mPrefsChangedListeners != null) {
				for (int i = 0; i < mPrefsChangedListeners.size(); i++) {
					((PrefsChangedListener) mPrefsChangedListeners.elementAt(i)).prefsChanged((PrefsChangedEvent) evt);
				}
			}
		}
		else if (evt instanceof WindowsMenuChangedEvent) {
			if (mWindowsMenuChangedListeners != null) {
				for (int i = 0; i < mWindowsMenuChangedListeners.size(); i++) {
					((WindowsMenuChangedListener) mWindowsMenuChangedListeners.elementAt(i))
					    .windowsMenuChanged((WindowsMenuChangedEvent) evt);
				}
			}
		}
	}

	public void windowsMenuChanged(WindowsMenuChangedEvent evt) {
		{
			try {

				jwindow.removeAll();

				for (int f = 0; f < PowerOceanAtlas.getInstance().getOpenFileViewers().size(); f++) {
					try {
						// make a menu item for an open FileViewer
						final FileViewer fv = (FileViewer) (PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(f));
						final Vector windowList = fv.getOpenWindowList();
						String title = fv.getTitle();
						JMenuItem item = new JMenuItem(title);
						item.setActionCommand(title);

						// attach a actionlistener to the menu
						item.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								for (int ff = 0; ff < PowerOceanAtlas.getInstance().getOpenFileViewers().size(); ff++) {
									try {
										FileViewer ffv = (FileViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);

										Vector windowList4 = ffv.getOpenWindowList();
										for (int ii = 0; ii < windowList4.size(); ii++) {
											JOAWindow theWin = (JOAWindow) windowList4.elementAt(ii);
											String wtitle = theWin.getTitle();
											if (wtitle.equals(e.getActionCommand())) {
												theWin.toFront();
												break;
											}
										}
									}
									catch (ClassCastException cce) {
										// Some other kind of JOAViewer window was selected
										JOAViewer joavv = (JOAViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);
										String wtitle = joavv.getTitle();
										if (wtitle.equals(e.getActionCommand())) {
											((JOAWindow) joavv).toFront();
											break;
										}
									}
								}
							}
						});
						jwindow.add(item);

						// deal with the windows created by this FileViewer
						for (int i = 1; i < windowList.size(); i++) {
							JOAWindow joaWin = (JOAWindow) windowList.elementAt(i);
							title = joaWin.getTitle();
							item = new JMenuItem("  " + title);
							item.setActionCommand(title);
							item.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									for (int ff = 0; ff < PowerOceanAtlas.getInstance().getOpenFileViewers().size(); ff++) {
										try {
											FileViewer ffv = (FileViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);
											Vector windowList5 = ffv.getOpenWindowList();
											for (int ii = 0; ii < windowList5.size(); ii++) {
												JOAWindow theWin = (JOAWindow) windowList5.elementAt(ii);
												String wtitle = theWin.getTitle();
												if (wtitle.equals(e.getActionCommand())) {
													theWin.toFront();
													break;
												}
											}
										}
										catch (ClassCastException cce) {
											// Some other kind of JOAViewer window was selected
											JOAViewer joavv = (JOAViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);
											String wtitle = joavv.getTitle();
											if (wtitle.equals(e.getActionCommand())) {
												((JOAWindow) joavv).toFront();
												break;
											}
										}
									}
								}
							});
							jwindow.add(item);
						}
					}
					catch (ClassCastException ex) {
						// Make some other kind of JOAViewer window
						JOAViewer joavv = (JOAViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(f);
						String title = joavv.getTitle();
						JMenuItem item = new JMenuItem(title);
						item.setActionCommand(title);

						// attach a actionlistener to the menu
						item.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								for (int ff = 0; ff < PowerOceanAtlas.getInstance().getOpenFileViewers().size(); ff++) {
									try {
										FileViewer ffv = (FileViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);

										Vector windowList4 = ffv.getOpenWindowList();
										for (int ii = 0; ii < windowList4.size(); ii++) {
											JOAWindow theWin = (JOAWindow) windowList4.elementAt(ii);
											String wtitle = theWin.getTitle();
											if (wtitle.equals(e.getActionCommand())) {
												theWin.toFront();
												break;
											}
										}
									}
									catch (ClassCastException cce) {
										// Some other kind of JOAViewer window was selected
										JOAViewer joavv = (JOAViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);
										String wtitle = joavv.getTitle();
										if (wtitle.equals(e.getActionCommand())) {
											((JOAWindow) joavv).toFront();
											break;
										}
									}
								}
							}
						});
						jwindow.add(item);
					}
				}
			}
			catch (Exception exx) {
				exx.printStackTrace();
			}
		}
	}

	public void handleMacQuit() {
		// close the log file
		if (JOAConstants.LogFileStream != null) {
			try {
				JOAConstants.LogFileStream.flush();
				JOAConstants.LogFileStream.close();
			}
			catch (Exception ex) {
			}
		}
		System.exit(0); // Quit the MRJ runtime
	}

	public void handleMacAbout() {
		AboutDialog ff = new AboutDialog((JFrame) this, true);
		// ff.init();
		ff.pack();

		// show dialog at center of screen
		Rectangle dBounds = ff.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		ff.setLocation(x, y);
		ff.setVisible(true);
	}

	/**
	 * selectionPerformed
	 * 
	 * @param e
	 *          SelectionEvent
	 * @todo Implement this ncBrowse.dapper.SelectionListener method
	 */

	private SelectionEvent le = null;
	private String dataset;
	private Vector dbases = null;
	private boolean interrupted = false;

	public void selectionPerformed(SelectionEvent e) {
		// prompt for destination: FileViewer or Build Database
//		Object[] opt = new String[3];
//		Object def;
//		if (JOAConstants.ISMACOSX) {
//			opt[0] = b.getString("kCancel");
//			opt[1] = b.getString("kBuildDatabase");
//			opt[2] = b.getString("kOpenDataWindow");
//			def = opt[2];
//		}
//		else {
//			opt[0] = b.getString("kOpenDataWindow");
//			opt[1] = b.getString("kBuildDatabase");
//			opt[2] = b.getString("kCancel");
//			def = opt[0];
//		}
//
//		Toolkit.getDefaultToolkit().beep();
//		int result = JOptionPane.showOptionDialog(null, b.getString("kDBorFV"), b.getString("kJOAAlert"),
//		    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opt, def);
//
//		if ((result == 0 && !JOAConstants.ISMACOSX) || (result == 2 && JOAConstants.ISMACOSX)) {
//			// open FV
			openDapperSelection(e);
//
//		}
//		else if (result == 1) {
//			// build a database
//			StationCollection coll = e.getCollection();
//			GeoDomain[] refs = e.getReferences();
//			String dataset = e.getDataSet();
//
//			NQVariableInspector varUI = new NQVariableInspector(coll, refs, dataset);
//			NQueryFormulas.centerFrameOnScreen(varUI, false);
//			varUI.setVisible(true);
//		}
	}

	public void openDapperSelection(SelectionEvent e) {
		le = e;
		final gov.noaa.pmel.swing.dapper.SwingWorker worker = new gov.noaa.pmel.swing.dapper.SwingWorker() {
			public Object construct() {
				StationCollection coll = le.getCollection();
				GeoDomain[] refs = le.getReferences();
				Iterator stIter = coll.getStationIterator(refs, null);

				JProgressBar progressBar = new JProgressBar(0, refs.length);
				progressBar.setString("Contacting Server (getting station data)...");
				progressBar.setValue(0);
				progressBar.setStringPainted(true);

				JFrame jf = new JFrame();
				JPanel jp = new JPanel();
				jp.add("South", progressBar);
				jf.getContentPane().add("Center", jp);
				jf.pack();
				Rectangle dBounds = jf.getBounds();
				Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				int x = sd.width / 2 - dBounds.width / 2;
				int y = sd.height / 2 - dBounds.height / 2;
				jf.setLocation(x, y);
				jf.setVisible(true);

				WindowListener windowListener = new WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						interrupted = true;
						e.getWindow().dispose();
					}
				};
				jf.addWindowListener(windowListener);

				// from the selection event get the dataset name and the collection
				// dataset name is the actual place the data came from--not the
				// collection (e.g., EPIC)
				dataset = le.getDataSet();

				// ingest the files
				dbases = new Vector();
				int c = -1;
				double lat = JOAConstants.MISSINGVALUE, lon = JOAConstants.MISSINGVALUE, zMn = JOAConstants.MISSINGVALUE, zMx = JOAConstants.MISSINGVALUE;
				GeoDate date;
				String datatype = null;
				String datasubtype = null;
				String insttype = null;
				while (stIter.hasNext() && !interrupted) {
					progressBar.setValue(++c);
					DapperNcFile ncFile = new DapperNcFile((StationDataset) stIter.next());

					// data type for this profile
					Iterator ai = ncFile.getGlobalAttributeIterator();
					while (ai.hasNext()) {
						// get the name
						Attribute at = (Attribute) ai.next();
						String name = at.getName();

						if (name.equalsIgnoreCase("DATA_TYPE")) {
							datatype = at.getStringValue();
						}

						if (name.equalsIgnoreCase("DATA_SUBTYPE")) {
							datasubtype = at.getStringValue();
							if (datasubtype != null) {
								if (datasubtype.length() == 0) {
									datasubtype = null;
								}
								else if (datasubtype.equals(" ")) {
									datasubtype = null;
								}
							}
						}

						if (name.equalsIgnoreCase("INST_TYPE")) {
							insttype = at.getStringValue();
							if (insttype != null) {
								if (insttype.length() == 0) {
									insttype = null;
								}
								else if (insttype.equals(" ")) {
									insttype = null;
								}
								else if (insttype.toUpperCase().indexOf("XBT") >= 0) {
									insttype = "XBT";
								}
								else if (insttype.toUpperCase().indexOf("CTD") >= 0) {
									insttype = "CTD";
								}
								else {
									insttype = null;
								}
							}
						}
					}

					GeoDomain domain = refs[c];

					if (domain.hasNorthLat()) {
						lat = domain.getNorthLat();
					}
					else if (domain.hasSouthLat()) {
						lat = domain.getSouthLat();
					}

					if (domain.hasEastLon()) {
						lon = domain.getEastLon();
					}
					else if (domain.hasWestLon()) {
						lon = domain.getWestLon();
					}

					if (domain.hasMinimumT()) {
						date = new GeoDate(domain.getMinimumT());
					}
					else if (domain.hasMaximumT()) {
						date = new GeoDate(domain.getMaximumT());
					}
					else {
						date = new GeoDate();
					}

					if (domain.hasMinimumZ()) {
						zMn = domain.getMinimumZ();
					}
					if (domain.hasMaximumZ()) {
						zMx = domain.getMaximumZ();
					}

					String type = "unknown";
					if (datasubtype != null) {
						if (datasubtype.toUpperCase().indexOf("CTD") >= 0) {
							type = "CTD";
						}
						else if (datasubtype.toUpperCase().indexOf("BOT") >= 0) {
							type = "BOTTLE";
						}
					}
					else if (datatype != null) {
						if (datatype.toUpperCase().indexOf("CTD") >= 0) {
							type = "CTD";
						}
						else {
							type = "BOTTLE";
						}
					}
					else if (insttype != null) {
						if (insttype.toUpperCase().indexOf("CTD") >= 0) {
							type = "CTD";
						}
						else {
							type = "BOTTLE";
						}
					}

					// construct an EPIC Pointer
					EpicPtr ep = new EpicPtr(EPSConstants.NETCDFFORMAT, String.valueOf(domain.getId()), type, "Dapper", String
					    .valueOf(domain.getId()), lat, lon, date, zMn, zMx, ncFile.getFileName(), null);

					// make a file reader object
					Dbase dname = new Dbase();
					DapperNetCDFReader reader = new DapperNetCDFReader(dname, ncFile, ep);

					// parse it and store dbase in Vector
					try {
						reader.parse();
						dbases.addElement(dname);
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				if (interrupted) { return null; }

				// done with the progress bar
				jf.setVisible(false);
				jf.dispose();

				NameItDialog nameIt = new NameItDialog("Name New Data Window", "Section (dataset) name:", dataset);
				nameIt.setModal(true);
				nameIt.setVisible(true);
				dataset = nameIt.getName();

				// turn this dbase into a JOA's internal data structures
				Frame fr = new Frame();
				FileViewer fv = new FileViewer(fr, dataset, dbases);
				PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
				fv.pack();
				fv.setVisible(true);
				fv.setSavedState(JOAConstants.ISCOLLECTION, null);
				return null;
			}
		};
		worker.start();
	}

	public RubberbandPanel getPanel() {
		return null;
	}

	/* (non-Javadoc)
   * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
   */
  public void uncaughtException(Thread t, Throwable e) {
		System.out.println("The thread " + t.getName() + " failed with exception:");
		e.printStackTrace();
	  
  }
  
  private void initPrefs() {
  	// get and parse the preferences
    try {
      mPrefs = Preferences.userNodeForPackage(PowerOceanAtlas.class);
      int keyCount = 0;
      String prefsAsXMLString = "";
      while (true) {
      	String key = "JOA Preferences" + keyCount++;
        String subStr = mPrefs.get(key, "");
        if (subStr == null || subStr.length() == 0) {
        	break;
        }
        prefsAsXMLString += subStr;
      }
      
      JOAFormulas.readPreferences(prefsAsXMLString);
      
      keyCount = 0;   
      String defaultMapAsXMLString = "";
      while (true) {
      	String key = "JOA Default Map" + keyCount++;
        String subStr = mPrefs.get(key, "");
        if (subStr == null || subStr.length() == 0) {
        	break;
        }
        defaultMapAsXMLString += subStr;
      }
      
      if (defaultMapAsXMLString != null && defaultMapAsXMLString.length() > 0) {
      	JOAConstants.DEFAULT_MAP_SPECIFICATION = JOAFormulas.parseMapSpec(defaultMapAsXMLString);
      }

      keyCount = 0;   
      String defaultCBAsXMLString = "";
      while (true) {
      	String key = "JOA Default Colorbar" + keyCount++;
        String subStr = mPrefs.get(key, "");
        if (subStr == null || subStr.length() == 0) {
        	break;
        }
        defaultCBAsXMLString += subStr;
      }

      if (defaultCBAsXMLString != null && defaultCBAsXMLString.length() > 0) {
      	JOAConstants.DEFAULT_COLORBAR = JOAFormulas.parseCB(defaultCBAsXMLString);
      }
    } 
    catch (Exception bse) {
      try {
        reset();
        localSync("foobar", "foobar");
        mPrefs = Preferences.userNodeForPackage(PowerOceanAtlas.class);
      } 
      catch (Exception ex) {
      	ex.printStackTrace();
      }
    }
  }

  public void localSync(String keyBase, String prefsAsXML) throws BackingStoreException {
  	// remove existing preferences
  	for (int i=0; i<100; i++) {
  		String key = keyBase + i;
  		int startIndex = i * 1000;
  		int endIndex = startIndex + 1000;
  		if (endIndex > prefsAsXML.length()) {
  			endIndex = prefsAsXML.length() - 1;
  		}
  		mPrefs.remove(key);
      mPrefs.sync();
  	}
  	
  	// put the xml version of the prefs into the Preferences
  	int numPrefNodes = prefsAsXML.length() / 1000 + 1;
  	for (int i=0; i<numPrefNodes; i++) {
  		String key = keyBase + i;
  		int startIndex = i * 1000;
  		int endIndex = startIndex + 1000;
  		if (endIndex > prefsAsXML.length()) {
  			endIndex = prefsAsXML.length() - 1;
  		}
  		String valString = prefsAsXML.substring(startIndex, endIndex);
      mPrefs.put(key, valString);
      mPrefs.sync();
  	}
  }


  public void reset() {
  }

}

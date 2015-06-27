/*
 * $Id: ConfigMapPlot.java,v 1.24 2005/09/07 18:49:29 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.util.prefs.BackingStoreException;
import java.io.*;
import javax.swing.border.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
import javaoceanatlas.PowerOceanAtlas;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.calculations.*;
import javaoceanatlas.events.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ConfigMapPlot extends JOAJDialog implements ActionListener, ItemListener, ListSelectionListener,
    ButtonMaintainer, ChangeListener {
	protected int mColorByFlag = MapSpecification.COLOR_STNS_BY_JOADEFAULT;
	protected int mContourByFlag = MapSpecification.CONTOUR_OVERLAY_BY_NONE;
	protected FileViewer mFileViewer = null;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mApplyButton = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJButton mSaveButton = null;
	protected JOAJButton mReadButton = null;
	protected JOAJButton mMakeDefaultButton = null;
	protected JSpinner leftLonFld = null;
	protected JSpinner topLatFld = null;
	protected JSpinner rightLonFld = null;
	protected JSpinner bottLatFld = null;
	protected JOAJTextField mCenterLonFld = null;
	protected JOAJTextField mCenterLatFld = null;
	protected Swatch[] swatches = new Swatch[120];
	protected JOAJCheckBox[] checks = new JOAJCheckBox[120];
	protected int[] bathyColorRamp = new int[150];
	protected int[] grayBathyColorRamp = new int[150];
	protected int[] rainbowBathyColorRamp = new int[150];
	protected MapSpecification mMapSpec = null;
	protected MapSpecification mOriginalMapSpec = null;
	protected JOAJTextField mNameField = null;
	protected JOAJComboBox mProjectionsPopup = null;
	protected JOAJRadioButton noCoast = null;
	protected JOAJRadioButton coarseRez = null;
	protected JOAJRadioButton fineRez = null;
	protected JOAJCheckBox mapGraticule = null;
	protected JOAJCheckBox plotStnLabels = null;
	protected JOAJCheckBox plotStnSymbols = null;
	protected JOAJCheckBox plotSectionLabels = null;
	protected JOAJCheckBox plotGratLabels = null;
	protected JOAJCheckBox lockRgn = null;
	protected JOAJTextField spacing = null;
	protected JOAJLabel mLatGratVal = null;
	protected JOAJLabel mLonGratVal = null;
	protected JOAJCheckBox connectStns = null;
	protected JOAJCheckBox connectStnsAcrossSections = null;
	protected JOAJComboBox mSymbolPopup = null;
	protected int mCurrSymbol = JOAConstants.SYMBOL_SQUAREFILLED;
	protected Icon[] symbolData = null;
	protected JOAJTextField mSizeField = null;
	protected JOAJComboBox presetRegions = null;
	protected JOAJComboBox presetColorSchemes = null;
	protected Swatch mapBG = null;
	protected Swatch coastline = null;
	protected Swatch gratColor = null;
	protected JOAJCheckBox retainAspect = null;
	protected DialogClient mClient = null;
	protected JOAJTextField mLatSpacing = null;
	protected JOAJTextField mLonSpacing = null;
	public String mStnCBName = null;
	public String mOvlCBName = null;	
	protected boolean mLayoutChanged = false;
	protected boolean mBathyApplied = false;
	protected JSpinner mSymbolSizeSpinner = null;
	protected JDialog mFrame = null;
	protected JPanel mBathyPane = null;
	protected JOAJTabbedPane mStationColorTabPane = null;
	protected JOAJTabbedPane mOverlayContoursTabPane = null;
	protected JCheckBox mColorFill = null;
	protected JOAJList mEtopoList = null;
	protected JOAJList mFillColorbarList = null;
	protected SmallIconButton checkAll = null;
	protected SmallIconButton checkNone = null;
	protected SmallIconButton ramp = null;
	protected SmallIconButton ramp2 = null;
	protected SmallIconButton ramp3 = null;
	protected JPanel custBathyCont = null;
	protected int numIsobaths = 12;
	protected int numCustomIsos = 0;
	protected int numCustomCoasts = 0;
	protected String[] allIsobathValues = null;
	protected String[] allIsobathDescrips = null;
	protected String[] allIsobathUnits = null;
	protected String[] allIsobathPaths = null;
	protected JOAJComboBox customCoastsList = null;
	protected JRadioButton builtinCoast = null;
	protected JRadioButton customCoast = null;
	protected String[] custCoastPaths = null;
	protected String[] custCoastDescrips = null;
	protected String initalCoastPath;
	protected boolean ignoreItemChange = false;
	private Timer timer = new Timer();
	protected JFrame mParent;
	protected Interpolation mInterpolation = null;
	protected double[] mColorBarValues = null;
	protected Color[] mColorBarColors = null;
	// protected NewColorBar mColorBar = null;
	// protected NewColorBar mContourColorBar = null;
	protected NewInterpolationSurface mSurface = null;
	protected String mInterpName = null;
	Vector<String> spl = JOAFormulas.getSurfaceList();
	ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	ConfigMapPlot mThis;
	IsoSurfaceSpecificationSymbols isoSymbolsPanel = null;
	IsoSurfaceSpecificationContours isoContourPanel = null;
	StationCalculationSymbols stnCalcPanel = null;
	StationCalculationContours stnCalcContourPanel = null;
	DefaultSymbols defaultPanel = null;
	NoContours noContoursPanel = null;
	MetadataSymbols metadataPanel = null;
	boolean mColorBarIsAutoscaled = false;
	JOAJLabel mLblOffsetLabel;
	JOAJLabel mLblAngleLabel;
	JOAJLabel mLblPrecLabel;
	JSpinner mLblOffset;
	JSpinner mLblAngle;
	JSpinner mLblPrec;
	JOAJLabel mLblOffsetUnitsLabel;
	JOAJLabel mLblAngleUnitsLabel;
	boolean mIgnoreStateChanged = false;
	
	public static int STN_CB_NO_CHANGE = 0;
	public static int STN_CB_ADDED = 1;
	public static int STN_CB_REMOVED = 2;
	public static int STN_CB_CHANGED = 3;
	
	public static int OVL_CB_NO_CHANGE = 0;
	public static int OVL_CB_ADDED = 1;
	public static int OVL_CB_REMOVED = 2;
	public static int OVL_CB_CHANGED = 3;

	protected String mProjSpecificMapRegions[][] = {
	    { "Select Map Region", "World 160W Center", "World 120E Center", "World 60W Center", "World   0E Center",
	        "North Pacific", "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic",
	        "Mediteranean", "Indian", "Bering Sea" },
	    {
	        "Select Map Region", // Mercator
	        "World 160W Center", "World 120E Center", "World  60W Center", "World   0E Center", "North Pacific",
	        "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic", "Mediteranean",
	        "Indian", "Bering Sea" }, // Miller
	    { "Select Map Region", "World 30W Ctr. (Atlantic)", "World 60E Ctr. (Indian)", "World 90W Ctr. (Pacific)",
	        "North Pacific", "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic",
	        "Mediteranean", "Indian", "Bering Sea" },
	    {
	        "Select Map Region", // Orthographic
	        "World 30W Ctr. (Atlantic)", "World 60E Ctr. (Indian)", "World 90W Ctr. (Pacific)", "North Pacific",
	        "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic", "Mediteranean",
	        "Indian", "Bering Sea" }, // Mollweide
	    { "Select Map Region", "World 30W Ctr. (Atlantic)", "World 60E Ctr. (Indian)", "World 90W Ctr. (Pacific)",
	        "North Pacific", "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic",
	        "Mediteranean", "Indian", "Bering Sea" },
	    {
	        "Select Map Region", // Lambert
	        "World 30W Ctr. (Atlantic)", "World 60E Ctr. (Indian)", "World 90W Ctr. (Pacific)", "North Pacific",
	        "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic", "Mediteranean",
	        "Indian", "Bering Sea" },
	    { "Select Map Region", "North Pole" },
	    { "Select Map Region", "South Pole" }, // Stereo, polar
//	    { "Select Map Region", "World", "North Pacific", "South Pacific", "North Atlantic", "South Atlantic", "Arctic",
//	        "Indian" }, // GCS
//	    { "Select Map Region", "World", "North Pacific", "South Pacific", "North Atlantic", "South Atlantic", "Arctic",
//	        "Indian" }, // Robinson to do
	    { "Select Map Region", "World", "North Pacific", "South Pacific", "North Atlantic", "South Atlantic", "Arctic",
	        "Indian" } // Eckert to do

	};

	protected double mProjRegionSpecificMinLats[][] = {
	// NP CP SP NA CA SA M I B
	    { -85.0, -85.0, -85.0, -85.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0, 52.0, -85.0 }, // Mercator
	    { -90.0, -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0, 52.0, -90.0 }, // Miller
	    { -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0, 52.0, -90.0 }, // Orthographic
	    { -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0, 52.0, -90.0 }, // Mollweide
	    { -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0, 52.0, -90.0 }, // Lambert
	    { -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0, 52.0, -90.0 }, // Stereo
	    { 0.0, 0.0 }, // north pole
	    { -90.0, -90.0 }, // south pole
//	    { -90.0, 0.0, -90.0, 0.0, -90.0, -90, 0.0, -90.0 }, // GCS
//	    { -90.0, 0.0, -90.0, 0.0, -90.0, -90, 0.0, -90.0 }, // Robinson to do
	    { -90.0, 0.0, -90.0, 0.0, -90.0, 70.0, 0.0} // Eckert to do
	};

	protected double mProjRegionSpecificMaxLats[][] = {
	// NP CP SP NA CA SA M I B
	    { 85.0, 85.0, 85.0, 85.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0, 61.0, 85.0 }, // Mercator
	    { 90.0, 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0, 61.0, 90.0 }, // Miller
	    { 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0, 61.0, 90.0 }, // Orthographic
	    { 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0, 61.0, 90.0 }, // Mollweide
	    { 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0, 61.0, 90.0 }, // Lambert
	    { 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0, 61.0, 90.0 }, // Stereo
	    { 90.0, 90.0 }, // north pole
	    { 0.0, 0.0 }, // south pole
//	    { 90.0, 90.0, 0.0, 90.0, 0.0, 90, 30.0, 90.0 }, // GCS
//	    { 90.0, 90.0, 0.0, 90.0, 0.0, 90, 30.0, 90.0 }, // Robinson to do
	    { 90.0, 90.0, 0.0, 90.0, 0.0, 90, 30.0} // Eckert to do
	};

	protected double mProjRegionSpecificLeftLons[][] = {
	    // NP CP SP NA CA SA M I B
	    { 20.0, -59.9, 120.0, -180.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0, -171.0, 20.0 },
	    { 20.0, -59.9, 120.0, -180.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0, -171.0, 20.0 },
	    { -120.0, -30.0, 120.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0, -171.0, -120.0 },
	    { -120.0, -30.0, 120.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0, -171.0, -120.0 },
	    { -120.0, -30.0, 120.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0, -171.0, -120.0 },
	    { -120.0, -30.0, 120.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0, -171.0, -120.0 },
	    { -180.0, -180.0 }, // north pole
	    { -180.0, -180.0 }, // south pole
//	    { 20.0, 120.0, 120.0, -80.0, -80.0, 20, 20.0, 20.0 }, // GCS
//	    { 20.0, 120.0, 120.0, -80.0, -80.0, 20, 20.0, 20.0 }, // Robinson
	    { 20.0, 120.0, 120.0, -80.0, -80.0, 20, 20.0} // Eckert
	};

	protected double mProjRegionSpecificRightLons[][] = {
	    // NP CP SP NA CA SA M I B
	    { 19.99, -60.0, 119.99, 180.0, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0, -157.0, 19.99 },
	    { 19.99, -60.0, 119.99, 180.0, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0, -157.0, 19.99 },
	    { 60.00, 150.0, -60.00, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0, -157.0, 60.0 },
	    { 60.00, 150.0, -60.00, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0, -157.0, 60.0 },
	    { 60.00, 150.0, -60.00, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0, -157.0, 60.0 },
	    { 60.00, 150.0, -60.00, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0, -157.0, 60.0 }, 
	    { 180.0, 180.0 },// north pole
	    { 180.0, 180.0 }, //south pole
//	    { 19.99, -80, -80, 20.0, 20.0, 120, 120.0, 19.99 }, //GCS
//	    { 19.99, -80, -80, 20.0, 20.0, 120, 120.0, 19.99 }, // Robinson to do
	    { 19.99, -80, -80, 20.0, 20.0, 120, 120.0} // Eckert to do
	};

	public ConfigMapPlot(JFrame par, FileViewer fv) {
		super(par, "Configure Map Plot", false);
		mFileViewer = fv;
		mParent = par;
		mMapSpec = new MapSpecification();
		mMapSpec.setEtopoFile("etopo60.nc");
		mOriginalMapSpec = null;

		// init the mapspecification object
		initMapSpec();

		// init the interface
		init(true);
		setMapCenter();

		mFrame = this;
		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (mClient != null) {
					mClient.dialogCancelled(mFrame);
				}
			}
		};
		this.addWindowListener(windowListener);
	}

	public ConfigMapPlot(JOAWindow par, FileViewer fv, DialogClient client, MapSpecification mapSpec) {
		super(par, "Configure Map Plot", false);
		mParent = par;
		mFileViewer = fv;
		mClient = client;
		mMapSpec = mapSpec;
		mOriginalMapSpec = new MapSpecification(mMapSpec);
		// mIsCustomMap = mMapSpec.isCustomMap();

		init(false);

		mFrame = this;
		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (mClient != null) {
					mClient.dialogCancelled(mFrame);
				}
			}
		};
		this.addWindowListener(windowListener);
	}

	private class ComparableString implements Comparable<Object> {
		String mData;

		public ComparableString(String inStr) {
			mData = inStr;
		}

		public int compareTo(Object o) {
			return mData.compareTo(((ComparableString)o).getString());
		}

		public String getString() {
			return mData;
		}
	}

	private class InvisFileFilter implements FilenameFilter {
		public boolean accept(File dir, java.lang.String name) {
			File f = new File(dir + File.separator + name);
			if (f == null) {
				return false;
			}
			else if (f.isHidden() || name.indexOf(".") == 0) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	@SuppressWarnings( { "unchecked", "deprecation" })
	public void init(boolean isnew) {
		mThis = this;
		symbolData = new Icon[] { new ImageIcon(getClass().getResource("images/sym_square.gif")),
		    new ImageIcon(getClass().getResource("images/sym_squarefilled.gif")),
		    new ImageIcon(getClass().getResource("images/sym_circle.gif")),
		    new ImageIcon(getClass().getResource("images/sym_circlefilled.gif")),
		    new ImageIcon(getClass().getResource("images/sym_diamond.gif")),
		    new ImageIcon(getClass().getResource("images/sym_diamondfilled.gif")),
		    new ImageIcon(getClass().getResource("images/sym_triangle.gif")),
		    new ImageIcon(getClass().getResource("images/sym_trianglefilled.gif")),
		    new ImageIcon(getClass().getResource("images/sym_cross1.gif")),
		    new ImageIcon(getClass().getResource("images/sym_cross2.gif")) };

		// create the tab pane for the station color options
		FeatureGroup fg = JOAConstants.JOA_FEATURESET.get("kMapTab4");

		boolean f1 = false;
		boolean f2 = false;
		boolean f4 = false;
		if (fg.hasFeature("kIsoSurface") && fg.isFeatureEnabled("kIsoSurface")) {
			f1 = true;
		}
		if (fg.hasFeature("kStationCalculation") && fg.isFeatureEnabled("kStationCalculation")) {
			f4 = true;
		}

		fg = JOAConstants.JOA_FEATURESET.get("kStationColors");
		if (fg.hasFeature("kStationCalculationStnColors") && fg.isFeatureEnabled("kStationCalculationStnColors")) {
			f2 = true;
		}
		boolean f3 = false;
		boolean f5 = false;

		if (fg.hasFeature("kIsoSurface") && fg.isFeatureEnabled("kIsoSurface")) {
			f3 = true;
		}

		if (fg.hasFeature("kMetadataStnColors") && fg.isFeatureEnabled("kMetadataStnColors")) {
			f5 = true;
		}

		DefaultFocusManager.disableSwingFocusManager();
		// build the color ramps
		float hue = 120, sat = 1, light = 1, startSat = 120, satAngleDelta = 1;
		for (int i = 0; i < 150; i++) {
			hue = (startSat + ((float)i * satAngleDelta)) / 360;
			bathyColorRamp[i] = Color.HSBtoRGB(hue, sat, light);
		}

		hue = 0;
		sat = 0;
		light = 1;
		float startLight = (float)288;
		float lightAngleDelta = 1;
		for (int i = 0; i < 150; i++) {
			light = (startLight - ((float)i * lightAngleDelta)) / 360;
			grayBathyColorRamp[i] = Color.HSBtoRGB(hue, sat, light);
		}

		hue = 0;
		sat = 1;
		light = 1;
		float startHue = 0;
		float hueAngleDelta = (float)1.8;
		for (int i = 0; i < 150; i++) {
			hue = (startHue + ((float)i * hueAngleDelta)) / 360;
			rainbowBathyColorRamp[i] = Color.HSBtoRGB(hue, sat, light);
		}

		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JOAJTabbedPane everyThingPanel = new JOAJTabbedPane();

		JPanel mainPanel = new JPanel(); // everything goes in here
		mainPanel.setLayout(new BorderLayout(5, 5));

		JPanel upperLeftPanel = new JPanel();
		upperLeftPanel.setLayout(new BorderLayout(5, 3)); // region, projection

		JPanel stationColorPanel = new JPanel();
		stationColorPanel.setLayout(new BorderLayout(5, 3)); // station color tabs

		JPanel overlayContoursPanel = new JPanel();
		overlayContoursPanel.setLayout(new BorderLayout(5, 3)); // overlay contours
		// tabs

		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new RowLayout(Orientation.CENTER, Orientation.TOP, 5)); // upperleft,
		// upperright
		// goes
		// in
		// here

		// Region Panel/Projection
		// Projection
		JPanel projPanel = new JPanel();
		projPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		JOAJLabel l0 = new JOAJLabel(b.getString("kProjection"));
		mProjectionsPopup = new JOAJComboBox();
		mProjectionsPopup.addItem(b.getString("kMercator"));
		mProjectionsPopup.addItem(b.getString("kCylindrical"));
		mProjectionsPopup.addItem(b.getString("kOrthographic"));
		mProjectionsPopup.addItem(b.getString("kMollweide"));
		mProjectionsPopup.addItem(b.getString("kLambert"));
		mProjectionsPopup.addItem(b.getString("kStereographic"));
		mProjectionsPopup.addItem(b.getString("kNorthPole"));
		mProjectionsPopup.addItem(b.getString("kSouthPole"));
		// mProjectionsPopup.addItem("GCS");
		// mProjectionsPopup.addItem("Robinson");
		 mProjectionsPopup.addItem("Eckert IV");
		int proj = mMapSpec.getProjection();
		int projIndex = proj > 5 ? proj - 5 : proj - 1;
		ignoreItemChange = true;
		mProjectionsPopup.setSelectedIndex(projIndex);
		ignoreItemChange = true;
		mProjectionsPopup.addItemListener(this);
		projPanel.add(l0);
		projPanel.add(mProjectionsPopup);

		JPanel regionPanel = new JPanel();
		regionPanel.setLayout(new BorderLayout(5, 5));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kRegion"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		regionPanel.setBorder(tb);

		JPanel prRegionPanel = new JPanel();
		prRegionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		JOAJLabel ll = new JOAJLabel(b.getString("kPresetRegion"));
		prRegionPanel.add(ll);

		// custom region
		JPanel crContPanel = new JPanel();
		JPanel crRegionPanel = new JPanel();
		crContPanel.setLayout(new BorderLayout(0, 5));
		crRegionPanel.setLayout(new GridLayout(3, 3));
		crRegionPanel.add(new JOAJLabel(" "));

		JPanel topLat = new JPanel();
		topLat.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		topLat.add(new JOAJLabel("T:"));

		SpinnerNumberModel modelTL = new SpinnerNumberModel(mMapSpec.getLatMax(), -90, 90, 0.5);
		topLatFld = new JSpinner(modelTL);

		topLat.add(topLatFld);
		crRegionPanel.add(topLat);
		crRegionPanel.add(new JOAJLabel(" "));

		topLatFld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				if (mIgnoreStateChanged) {
					mIgnoreStateChanged = false;
					return;
				}

				// test whether value of field has changed
				double v = ((Double)topLatFld.getValue()).doubleValue();
				if (v != mMapSpec.getLatMax()) {
					// mIsCustomMap = true;
					// ignoreItemChange++;
					// ignoreItemChange++;
					// setAvailableMapRegions();
				}
				JSpinner te = (JSpinner)evt.getSource();
				if (te == leftLonFld || te == rightLonFld) {
					// recompute the center longitude
					setCenterLongitude();
				}
				else {
					// redo the center latitude
					setCenterLatitude();
				}
				generatePlotName();
			}
		});

		topLatFld.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent me) {
				// test whether value of field has changed
				double v = ((Double)topLatFld.getValue()).doubleValue();
				if (v != mMapSpec.getLatMax()) {
					// mIsCustomMap = true;
					// ignoreItemChange++;
					// ignoreItemChange++;
					// setAvailableMapRegions();
				}

				JSpinner te = (JSpinner)me.getSource();
				if (te == leftLonFld || te == rightLonFld) {
					// recompute the center longitude
					setCenterLongitude();
				}
				else {
					// redo the center latitude
					setCenterLatitude();
				}
				generatePlotName();
			}
		});

		JPanel leftLon = new JPanel();
		leftLon.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		leftLon.add(new JOAJLabel("L:"));
		SpinnerNumberModel modelLL = new SpinnerNumberModel(mMapSpec.getLonLft(), -180, 180, 0.5);
		leftLonFld = new JSpinner(modelLL);

		leftLon.add(leftLonFld);
		crRegionPanel.add(leftLon);
		crRegionPanel.add(new JOAJLabel(" "));

		leftLonFld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				if (mIgnoreStateChanged) {
					mIgnoreStateChanged = false;
					return;
				}
				// test whether value of field has changed
				double v = ((Double)leftLonFld.getValue()).doubleValue();
				if (v != mMapSpec.getLonLft()) {
					// mIsCustomMap = true;
					// ignoreItemChange++;
					// ignoreItemChange++;
					// setAvailableMapRegions();
				}

				JSpinner te = (JSpinner)evt.getSource();
				if (te == leftLonFld || te == rightLonFld) {
					// recompute the center longitude
					setCenterLongitude();
				}
				else {
					// redo the center latitude
					setCenterLatitude();
				}
				generatePlotName();
			}
		});

		leftLonFld.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent me) {
				// test whether value of field has changed
				double v = ((Double)leftLonFld.getValue()).doubleValue();
				if (v != mMapSpec.getLonLft()) {
					// mIsCustomMap = true;
					// ignoreItemChange++;
					// ignoreItemChange++;
					// setAvailableMapRegions();
				}

				JSpinner te = (JSpinner)me.getSource();
				if (te == leftLonFld || te == rightLonFld) {
					// recompute the center longitude
					setCenterLongitude();
				}
				else {
					// redo the center latitude
					setCenterLatitude();
				}
				generatePlotName();
			}
		});

		JPanel rightLon = new JPanel();
		rightLon.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		rightLon.add(new JOAJLabel("R:"));

		SpinnerNumberModel modelRL = new SpinnerNumberModel(mMapSpec.getLonRt(), -180, 180, 0.5);
		rightLonFld = new JSpinner(modelRL);

		rightLon.add(rightLonFld);
		crRegionPanel.add(rightLon);
		rightLonFld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				if (mIgnoreStateChanged) {
					mIgnoreStateChanged = false;
					return;
				}
				// test whether value of field has changed
				double v = ((Double)rightLonFld.getValue()).doubleValue();
				if (v != mMapSpec.getLonRt()) {
					// mIsCustomMap = true;
					// ignoreItemChange++;
					// ignoreItemChange++;
					// setAvailableMapRegions();
				}

				JSpinner te = (JSpinner)evt.getSource();
				if (te == leftLonFld || te == rightLonFld) {
					// recompute the center longitude
					setCenterLongitude();
				}
				else {
					// redo the center latitude
					setCenterLatitude();
				}
				generatePlotName();
			}
		});

		rightLonFld.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent me) {
				// test whether value of field has changed
				double v = ((Double)rightLonFld.getValue()).doubleValue();
				if (v != mMapSpec.getLonRt()) {
					// mIsCustomMap = true;
					// ignoreItemChange++;
					// ignoreItemChange++;
					// setAvailableMapRegions();
				}

				JSpinner te = (JSpinner)me.getSource();
				if (te == leftLonFld || te == rightLonFld) {
					// recompute the center longitude
					setCenterLongitude();
				}
				else {
					// redo the center latitude
					setCenterLatitude();
				}
				generatePlotName();
			}
		});

		JPanel bottLat = new JPanel();
		bottLat.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		bottLat.add(new JOAJLabel("B:"));

		SpinnerNumberModel modelBL = new SpinnerNumberModel(mMapSpec.getLatMin(), -90, 90, 0.5);
		bottLatFld = new JSpinner(modelBL);

		bottLat.add(bottLatFld);
		bottLatFld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				if (mIgnoreStateChanged) {
					mIgnoreStateChanged = false;
					return;
				}
				// test whether value of field has changed
				double v = ((Double)bottLatFld.getValue()).doubleValue();
				if (v != mMapSpec.getLatMin()) {
					// mIsCustomMap = true;
					// ignoreItemChange++;
					// ignoreItemChange++;
					// setAvailableMapRegions();
				}

				JSpinner te = (JSpinner)evt.getSource();
				if (te == leftLonFld || te == rightLonFld) {
					// recompute the center longitude
					setCenterLongitude();
				}
				else {
					// redo the center latitude
					setCenterLatitude();
				}
				generatePlotName();
			}
		});

		bottLatFld.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent me) {
				// test whether value of field has changed
				double v = ((Double)bottLatFld.getValue()).doubleValue();
				if (v != mMapSpec.getLatMin()) {
					// mIsCustomMap = true;
					// ignoreItemChange++;
					// ignoreItemChange++;
					// setAvailableMapRegions();
				}

				JSpinner te = (JSpinner)me.getSource();
				if (te == leftLonFld || te == rightLonFld) {
					// recompute the center longitude
					setCenterLongitude();
				}
				else {
					// redo the center latitude
					setCenterLatitude();
				}
				generatePlotName();
			}
		});

		crRegionPanel.add(new JOAJLabel(" "));
		crRegionPanel.add(bottLat);
		crRegionPanel.add(new JOAJLabel(" "));
		crContPanel.add("Center", crRegionPanel);

		// center lat
		JPanel centerAndLocked = new JPanel();
		centerAndLocked.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 2));
		JPanel mapCenter = new JPanel();
		mapCenter.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		mapCenter.add(new JOAJLabel(b.getString("kMapCenter") + " "));
		mCenterLonFld = new JOAJTextField(5);
		mCenterLonFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mCenterLatFld = new JOAJTextField(5);
		mCenterLatFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mapCenter.add(new JOAJLabel(b.getString("kLon") + " "));
		mapCenter.add(mCenterLonFld);
		mapCenter.add(new JOAJLabel(b.getString("kLat")));
		mapCenter.add(mCenterLatFld);
		mCenterLonFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getCenLon()), 2, false));
		mCenterLatFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getCenLat()), 2, false));
		centerAndLocked.add(mapCenter);
		lockRgn = new JOAJCheckBox(b.getString("kLockRgn"));
		centerAndLocked.add(lockRgn);
		crContPanel.add("South", centerAndLocked);

		tb = BorderFactory.createTitledBorder(b.getString("kCustomRegion"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		crContPanel.setBorder(tb);

		// build region panel and add to upperleft
		regionPanel.add("North", projPanel);
		regionPanel.add("Center", prRegionPanel);
		regionPanel.add("South", crContPanel);
		upperLeftPanel.add("Center", regionPanel);

		// bathy
		JPanel bathyCont = new JPanel();
		bathyCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));

		JPanel bathyContCont = new JPanel();
		bathyContCont.setLayout(new BorderLayout(5, 5));

		tb = BorderFactory.createTitledBorder(b.getString("kIsobaths"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		bathyContCont.setBorder(tb);

		// the bathy detail panel
		// arrays for the built-in bathymetry
		String defValues[] = { " 500", "1000", "1500", "2000", "2500", "3000", "3500", "4000", "4500", "5000", "5500",
		    "6000" };
		String defDescriptions[] = { "Built in", "Built in", "Built in", "Built in", "Built in", "Built in", "Built in",
		    "Built in", "Built in", "Built in", "Built in", "Built in" };
		String defUnits[] = { "m", "m", "m", "m", "m", "m", "m", "m", "m", "m", "m", "m" };
		String defPaths[] = { "", "", "", "", "", "", "", "", "", "", "", "" };
		// look for custom isobaths in the "custom isobaths" folder
		String[] custValues = null;
		String[] custDescrips = null;
		String[] custUnits = null;
		String[] custPaths = null;
		File customIsobathDir = null;
		File customCoastDir = null;

		String[] customIsos = null;
		int numCustomIsos = 0;
		InvisFileFilter myFilter = new InvisFileFilter();
		try {
			// try to get it from the JOA support directory
			String directory = JOAFormulas.getSupportPath() + File.separator;
			customIsobathDir = new File(directory, "Custom_Isobaths");
			customIsos = customIsobathDir.list(myFilter);
			numCustomIsos = customIsos.length;
		}
		catch (Exception ex) {
		}

		String[] customCoasts = null;
		int numCustomCoasts = 0;
		try {
			// try to get it from the JOA support directory
			String directory = JOAFormulas.getCustomCoastlinePath();
			customCoastDir = new File(directory);
			customCoasts = customCoastDir.list(myFilter);
			numCustomCoasts = customCoasts.length;
		}
		catch (Exception ex) {
		}

		if (numCustomIsos > 0 && customIsobathDir != null && customIsobathDir.isDirectory()) {
			custValues = new String[customIsos.length / 2];
			custDescrips = new String[customIsos.length / 2];
			custUnits = new String[customIsos.length / 2];
			custPaths = new String[customIsos.length / 2];
			numCustomIsos = customIsos.length / 2;

			// sort the list
			ArrayList al = new ArrayList();
			for (int i = 0; i < customIsos.length; i++) {
				al.add(new ComparableString(customIsos[i]));
			}
			Collections.sort(al);

			int cnt = 0;
			for (int i = 0; i < customIsos.length; i++) {
				String nme = ((ComparableString)al.get(i)).getString();
				String inLine = null;
				if (nme.indexOf(".txt") > 0) {
					// open the file and get the description
					try {
						File mFile = new File(customIsobathDir + File.separator + nme);
						FileReader fr = new FileReader(mFile);
						LineNumberReader in = new LineNumberReader(fr, 10000);
						inLine = in.readLine();
						in.close();
					}
					catch (Exception ex) {
					}
					// isolate the values
					StringTokenizer st = new StringTokenizer(inLine, ":");
					custValues[cnt] = (String)st.nextElement();
					custUnits[cnt] = (String)st.nextElement();
					custDescrips[cnt] = (String)st.nextElement();
				}
				else {
					custPaths[cnt] = customIsobathDir + File.separator + nme;
				}

				if (custDescrips[cnt] != null && custValues[cnt] != null) {
					cnt++;
				}
			}

			// merge the default values and custom values into final array
			allIsobathValues = new String[defValues.length + numCustomIsos];
			allIsobathDescrips = new String[defValues.length + numCustomIsos];
			allIsobathUnits = new String[defValues.length + numCustomIsos];
			allIsobathPaths = new String[defValues.length + numCustomIsos];
			for (int i = 0; i < defValues.length; i++) {
				allIsobathValues[i] = defValues[i];
				allIsobathDescrips[i] = defDescriptions[i];
				allIsobathUnits[i] = defUnits[i];
				allIsobathPaths[i] = defPaths[i];
			}
			for (int j = 0; j < numCustomIsos; j++) {
				allIsobathValues[defValues.length + j] = custValues[j];
				allIsobathDescrips[defValues.length + j] = custDescrips[j];
				allIsobathUnits[defValues.length + j] = custUnits[j];
				allIsobathPaths[defValues.length + j] = custPaths[j];
			}
		}
		else {
			// just copy the default values into final array
			allIsobathValues = new String[defValues.length];
			allIsobathDescrips = new String[defValues.length];
			allIsobathUnits = new String[defValues.length];
			allIsobathPaths = new String[defValues.length];
			for (int i = 0; i < defValues.length; i++) {
				allIsobathValues[i] = defValues[i];
				allIsobathDescrips[i] = defDescriptions[i];
				allIsobathUnits[i] = defUnits[i];
				allIsobathPaths[i] = defPaths[i];
			}
		}

		// sort by the value of the isobath:
		for (int i = 0; i < allIsobathValues.length - 1; i++) {
			for (int j = i + 1; j < allIsobathValues.length; j++) {
				double val1 = Double.valueOf(allIsobathValues[i]).doubleValue();
				double val2 = Double.valueOf(allIsobathValues[j]).doubleValue();
				if (val2 < val1) {
					// swap everything
					String t1 = allIsobathValues[i];
					allIsobathValues[i] = allIsobathValues[j];
					allIsobathValues[j] = t1;
					String t2 = allIsobathDescrips[i];
					allIsobathDescrips[i] = allIsobathDescrips[j];
					allIsobathDescrips[j] = t2;
					String t3 = allIsobathUnits[i];
					allIsobathUnits[i] = allIsobathUnits[j];
					allIsobathUnits[j] = t3;
					String t4 = allIsobathPaths[i];
					allIsobathPaths[i] = allIsobathPaths[j];
					allIsobathPaths[j] = t4;
				}
			}
		}

		// the bathy detail panel
		for (int i = 0; i < allIsobathValues.length; i++) {
			JPanel detailCont = new JPanel();
			detailCont.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			checks[i] = new JOAJCheckBox("");
			checks[i].addItemListener(this);
			swatches[i] = new Swatch(Color.black, 12, 12);
			JLabel label = new JOAJLabel(allIsobathValues[i] + allIsobathUnits[i] + ":" + allIsobathDescrips[i]);
			label.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			label.setForeground(java.awt.Color.black);
			detailCont.add(checks[i]);
			detailCont.add(swatches[i]);
			detailCont.add(label);
			bathyCont.add(detailCont);
		}

		// set the initial values of the widgets from the mMapSpec
		if (mMapSpec != null) {
			for (int i = 0; i < mMapSpec.getNumIsobaths(); i++) {
				// find a matching detail line
				for (int j = 0; j < allIsobathValues.length; j++) {
					boolean valMatch = (Double.valueOf(allIsobathValues[j]).doubleValue()) == mMapSpec.getValue(i);
					boolean descripMatch = allIsobathDescrips[j].equals(mMapSpec.getDescrip(i));
					if (valMatch && descripMatch) {
						checks[j].setSelected(true);
						swatches[j].setColor(mMapSpec.getColor(i));
					}
				}
			}
		}

		JOAJScrollPane listScroller = new JOAJScrollPane(bathyCont);
		listScroller.getVerticalScrollBar().setUnitIncrement(12);
		bathyContCont.add("Center", new TenPixelBorder(listScroller, 0, 5, 0, 5));

		// convenience buttons
		JPanel bathyBtnCont = new JPanel();
		bathyBtnCont.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
		checkAll = new SmallIconButton(new ImageIcon(getClass().getResource("images/checkall.gif")));
		bathyBtnCont.add(checkAll);
		checkNone = new SmallIconButton(new ImageIcon(getClass().getResource("images/checknone.gif")));
		bathyBtnCont.add(checkNone);
		ramp = new SmallIconButton(new ImageIcon(getClass().getResource("images/colorramp.gif")));
		bathyBtnCont.add(ramp);
		ramp2 = new SmallIconButton(new ImageIcon(getClass().getResource("images/grayramp.gif")));
		bathyBtnCont.add(ramp2);
		ramp3 = new SmallIconButton(new ImageIcon(getClass().getResource("images/rainbowramp.gif")));
		bathyBtnCont.add(ramp3);
		checkAll.addActionListener(this);
		checkNone.addActionListener(this);
		ramp.addActionListener(this);
		ramp2.addActionListener(this);
		ramp3.addActionListener(this);
		checkAll.setActionCommand("all");
		checkNone.setActionCommand("none");
		ramp.setActionCommand("ramp");
		ramp2.setActionCommand("ramp2");
		ramp3.setActionCommand("ramp3");
		bathyContCont.add("South", bathyBtnCont);

		checkAll.setToolTipText("Select all isobaths in the list");
		checkNone.setToolTipText("Deselect selected isobaths");

		ramp.setToolTipText("Assign isobath colors from green-to-blue palette");
		ramp2.setToolTipText("Assign isobath colors from gray-scale palette");
		ramp3.setToolTipText("Assign isobath colors from rainbow color palette");

		// color fill preferences
		CheckBoxBorderPanel colorFillCont = new CheckBoxBorderPanel(b.getString("kColorFillBathy"), false);
		colorFillCont.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		mColorFill = colorFillCont.getCheckBox();
		mColorFill.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.DESELECTED) {
					disableEtopoStuff();
				}
				else if (evt.getStateChange() == ItemEvent.SELECTED) {
					enableEtopoStuff();
				}
			}
		});
		mColorFill.setSelected(mMapSpec.isColorFill());

		JPanel cfLine2 = new JPanel();
		cfLine2.setLayout(new BorderLayout(5, 5));
		cfLine2.add(new JOAJLabel("   "));
		cfLine2.add(new JOAJLabel(b.getString("kBathymetryFiles")), "North");

		// look for etopo files
		Vector<String> etopFiles = JOAFormulas.getEtopoList();
		mEtopoList = new JOAJList(etopFiles);
		mEtopoList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mEtopoList.setPrototypeCellValue("Temperature                     ");
		mEtopoList.setVisibleRowCount(8);
		JScrollPane listScroller3 = new JScrollPane(mEtopoList);
		cfLine2.add(new TenPixelBorder(listScroller3, 0, 5, 0, 0), "Center");
		int[] indices = JOAFormulas.getSelIndices(mMapSpec.getEtopoFiles(), mMapSpec.getNumEtopoFiles(), mEtopoList);
		mEtopoList.setSelectedIndices(indices);
		colorFillCont.add(cfLine2);

		// Color Bar List
		JPanel cfLine3 = new JPanel();
		cfLine3.setLayout(new BorderLayout(5, 5));
		cfLine3.add(new JOAJLabel(b.getString("kColorBars:")), "North");
		Vector<String> cbs = JOAFormulas.getColorBarList();
		mFillColorbarList = new JOAJList(cbs);
		mFillColorbarList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mFillColorbarList.setPrototypeCellValue("Temperature                                   ");
		mFillColorbarList.setVisibleRowCount(8);
		JScrollPane listScroller2 = new JScrollPane(mFillColorbarList);
		cfLine3.add(new TenPixelBorder(listScroller2, 0, 5, 0, 0), "Center");

		// highlight current color bar
		Vector<NewColorBar> ncb = new Vector<NewColorBar>(100);
		for (int i = 0; i < cbs.size(); i++) {
			NewColorBar cb = null;

			String newCBARName = (String)cbs.elementAt(i);

			// read the color bar from disk
			try {
				cb = JOAFormulas.getColorBar(newCBARName);
			}
			catch (Exception ex) {
				cb = null;
			}

			ncb.addElement(cb);
			if (cb != null && mMapSpec.getBathyColorBar() != null) {
				String cbName1 = cb.getParam();
				String cbName2 = mMapSpec.getBathyColorBar().getParam();
				String cbTitle1 = cb.getTitle();
				String cbTitle2 = mMapSpec.getBathyColorBar().getTitle();
				if (cbName1.equalsIgnoreCase(cbName2) && cbTitle1.equalsIgnoreCase(cbTitle2)) {
					mFillColorbarList.setSelectedIndex(i);
					mFillColorbarList.ensureIndexIsVisible(i);
					break;
				}
			}
		}

		colorFillCont.add(cfLine3);

		if (!mMapSpec.isColorFill()) {
			disableEtopoStuff();
		}

		// all of this goes in a JSplitPane
		JSplitPane mBathyPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bathyContCont, colorFillCont);

		// mBathyPane = new JPanel();
		// mBathyPane.setLayout(new BorderLayout(5,
		// 5));//ColumnLayout(Orientation.LEFT, Orientation.CENTER, 3));
		// mBathyPane.add("North", bathyContCont);
		// mBathyPane.add("Center", colorFillCont);

		// Options
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 3));
		JPanel middleContPanel = new JPanel();
		middleContPanel.setLayout(new BorderLayout(0, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kOptions"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		middleContPanel.setBorder(tb);

		// custom coastlines
		if (numCustomCoasts > 0 && customCoastDir != null && customCoastDir.isDirectory()) {
			custCoastDescrips = new String[customCoasts.length / 2];
			custCoastPaths = new String[customCoasts.length / 2];
			numCustomCoasts = customCoasts.length / 2;

			// sort the list
			ArrayList<ComparableString> al = new ArrayList<ComparableString>();
			for (int i = 0; i < customCoasts.length; i++) {
				al.add(new ComparableString(customCoasts[i]));
			}
			Collections.sort(al);

			int cnt = 0;
			for (int i = 0; i < customCoasts.length; i++) {
				String nme = ((ComparableString)al.get(i)).getString();
				String inLine = null;
				if (nme.indexOf(".txt") > 0) {
					// open the file and get the description
					try {
						File mFile = new File(customCoastDir + File.separator + nme);
						FileReader fr = new FileReader(mFile);
						LineNumberReader in = new LineNumberReader(fr, 10000);
						inLine = in.readLine();
						in.close();
					}
					catch (Exception ex) {
					}
					// isolate the values
					StringTokenizer st = new StringTokenizer(inLine, ":");
					custCoastDescrips[cnt] = (String)st.nextElement();
				}
				else {
					custCoastPaths[cnt] = customCoastDir + File.separator + nme;
				}

				if (custCoastDescrips[cnt] != null) {
					cnt++;
				}
			}
		}

		// Coastline resolution
		JPanel line4 = new JPanel();
		line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		line4.add(new JOAJLabel("Plot built-in coastline:"));
		coarseRez = new JOAJRadioButton("Coarse resolution",
		    mMapSpec.getCoastLineRez() == JOAConstants.COARSERESOLUTION);
		fineRez = new JOAJRadioButton("Fine resolution", mMapSpec.getCoastLineRez() == JOAConstants.FINERESOLUTION);
		noCoast = new JOAJRadioButton("None", mMapSpec.getCoastLineRez() == JOAConstants.NOCOAST);
		customCoast = new JOAJRadioButton(b.getString("kCustom"), !(mMapSpec.getCustCoastPath() == null));
		ButtonGroup rez = new ButtonGroup();
		rez.add(noCoast);
		rez.add(coarseRez);
		rez.add(fineRez);
		rez.add(customCoast);
		line4.add(noCoast);
		line4.add(coarseRez);
		line4.add(fineRez);
		line4.add(customCoast);
		noCoast.addItemListener(this);
		coarseRez.addItemListener(this);
		fineRez.addItemListener(this);
		customCoast.addItemListener(this);

		if (numCustomCoasts == 0) {
			customCoast.setEnabled(false);
		}
		else {
			// add a popup menu for the custom coastlines
			Vector coasts = new Vector();
			for (int i = 0; i < numCustomCoasts; i++) {
				coasts.addElement(custCoastDescrips[i]);
			}
			customCoastsList = new JOAJComboBox(coasts);
			line4.add(customCoastsList);

			customCoastsList.setEnabled(false);
			if (mMapSpec.getCustCoastPath() != null) {
				for (int i = 0; i < numCustomCoasts; i++) {
					if (custCoastDescrips[i].equals(mMapSpec.getCustCoastDescrip())) {
						// mMapSpec.mCustCoastPath = custCoastDescrips[i];
						customCoastsList.setSelectedIndex(i);
						break;
					}
				}
				customCoastsList.setEnabled(true);
			}
		}

		middlePanel.add(line4);

		// symbols
		JPanel line1 = new JPanel();


		plotStnSymbols = new JOAJCheckBox("Plot stations", mMapSpec.isPlotStnSymbols());
		line1.add(plotStnSymbols);
		
		line1.add(new JOAJLabel(b.getString("kStationSymbol")));
		mSymbolPopup = new JOAJComboBox();
		for (int i = 0; i < symbolData.length; i++) {
			mSymbolPopup.addItem(symbolData[i]);
		}
		mSymbolPopup.setSelectedIndex(mMapSpec.getSymbol() - 1);
		mSymbolPopup.addItemListener(this);
		line1.add(mSymbolPopup);

		// symbol size
		line1.add(new JOAJLabel(b.getString("kSize")));

		SpinnerNumberModel model = new SpinnerNumberModel(mMapSpec.getSymbolSize(), 1, 100, 1);
		mSymbolSizeSpinner = new JSpinner(model);

		line1.add(mSymbolSizeSpinner);
		middlePanel.add(line1);

		// connect station
		line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		connectStns = new JOAJCheckBox(b.getString("kConnectStations"), mMapSpec.isConnectStns());
		connectStns.addItemListener(this);

		connectStns.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					mMapSpec.setConnectStns(true);
					enableConnectOption();
				}
				else {
					mMapSpec.setConnectStns(false);
					disableConnectOption();
				}
			}
		});

		line1.add(connectStns);
		connectStnsAcrossSections = new JOAJCheckBox(b.getString("kConnectStationsAcross"), mMapSpec
		    .isConnectStnsAcrossSections());

		connectStnsAcrossSections.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					mMapSpec.setConnectStnsAcrossSections(true);
				}
				else {
					mMapSpec.setConnectStnsAcrossSections(false);
				}
			}
		});

		line1.add(connectStnsAcrossSections);
		if (mMapSpec.isConnectStns()) {
			enableConnectOption();
		}
		else {
			disableConnectOption();
		}

		JPanel line1a = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel line1b = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		plotSectionLabels = new JOAJCheckBox(b.getString("kPlotSectionLabels"), mMapSpec.isPlotSectionLabels());
		line1a.add(plotSectionLabels);

		plotStnLabels = new JOAJCheckBox(b.getString("kPlotStationLabels"), mMapSpec.isPlotStnLabels());
		line1a.add(plotStnLabels);

		plotStnLabels.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.DESELECTED) {
					mLblOffsetLabel.setEnabled(false);
					mLblAngleLabel.setEnabled(false);
					mLblOffset.setEnabled(false);
					mLblAngle.setEnabled(false);
					mLblOffsetUnitsLabel.setEnabled(false);
					mLblAngleUnitsLabel.setEnabled(false);
				}
				else {
					mLblOffsetLabel.setEnabled(true);
					mLblAngleLabel.setEnabled(true);
					mLblOffset.setEnabled(true);
					mLblAngle.setEnabled(true);
					mLblOffsetUnitsLabel.setEnabled(true);
					mLblAngleUnitsLabel.setEnabled(true);
				}
			}
		});

		SpinnerNumberModel model2 = new SpinnerNumberModel(mMapSpec.getStnLabelOffset(), 1, 100, 1);
		mLblOffset = new JSpinner(model2);

		SpinnerNumberModel model3 = new SpinnerNumberModel(mMapSpec.getStnLabelAngle(), 0.0, 360.0, 1.0);
		mLblAngle = new JSpinner(model3);

		SpinnerNumberModel model4 = new SpinnerNumberModel(mMapSpec.getContourLabelPrec(), 0, 10, 1);
		mLblPrec = new JSpinner(model4);

		mLblOffsetLabel = new JOAJLabel(b.getString("kLabelOffset"));
		mLblAngleLabel = new JOAJLabel("   " + b.getString("kLabelAngle"));
		mLblPrecLabel = new JOAJLabel("Contour Label Precision:");
		mLblOffsetUnitsLabel = new JOAJLabel(b.getString("kPixels"));
		mLblAngleUnitsLabel = new JOAJLabel(b.getString("kDegrees"));

		line1a.add(mLblOffsetLabel);
		line1a.add(mLblOffset);
		line1a.add(mLblOffsetUnitsLabel);
		line1a.add(mLblAngleLabel);
		line1a.add(mLblAngle);
		line1a.add(mLblAngleUnitsLabel);
		line1b.add(mLblPrecLabel);
		line1b.add(mLblPrec);

		if (!mMapSpec.isPlotStnLabels()) {
			mLblOffsetLabel.setEnabled(false);
			mLblAngleLabel.setEnabled(false);
			mLblOffset.setEnabled(false);
			mLblAngle.setEnabled(false);
			mLblOffsetUnitsLabel.setEnabled(false);
			mLblAngleUnitsLabel.setEnabled(false);
		}
		else {
			mLblOffsetLabel.setEnabled(true);
			mLblAngleLabel.setEnabled(true);
			mLblOffset.setEnabled(true);
			mLblAngle.setEnabled(true);
			mLblOffsetUnitsLabel.setEnabled(true);
			mLblAngleUnitsLabel.setEnabled(true);
		}

		middlePanel.add(line1a);
		middlePanel.add(line1b);

		// map graticule and spacing
		JPanel line2b = new JPanel();
		line2b.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		mapGraticule = new JOAJCheckBox(b.getString("kMapGraticule"), mMapSpec.isDrawGraticule());
		mapGraticule.addItemListener(this);
		line2b.add(mapGraticule);
		plotGratLabels = new JOAJCheckBox(b.getString("kPlotLabels"), mMapSpec.isPlotGratLabels());
		plotGratLabels.addItemListener(this);
		line2b.add(plotGratLabels);
		middlePanel.add(line2b);

		JPanel line2 = new JPanel();
		line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		line2.add(new JOAJLabel(b.getString("kLatSpacing")));
		mLatSpacing = new JOAJTextField(4);
		mLatSpacing.setText(JOAFormulas.formatDouble(Double.toString(mMapSpec.getLatGratSpacing()), 1, false));
		mLatSpacing.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line2.add(mLatSpacing);
		line2.add(new JOAJLabel("   " + b.getString("kLonSpacing")));
		mLonSpacing = new JOAJTextField(4);
		mLonSpacing.setText(JOAFormulas.formatDouble(Double.toString(mMapSpec.getLonGratSpacing()), 1, false));
		mLonSpacing.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line2.add(mLonSpacing);
		middlePanel.add(new TenPixelBorder(line2, 0, 10, 0, 0));

		// retain cartographic aspect
		JPanel line3 = new JPanel();
		line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		retainAspect = new JOAJCheckBox(b.getString("kRetainAspect"), mMapSpec.isRetainProjAspect());
		retainAspect.addItemListener(this);
		line3.add(retainAspect);
		middlePanel.add(line3);

		// window name
		JPanel line0 = new JPanel();
		line0.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		line0.add(new JOAJLabel(b.getString("kWindowName")));
		mNameField = new JOAJTextField(40);
		mNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line0.add(mNameField);
		middlePanel.add(line0);

		// swatches
		JPanel line5 = new JPanel();
		line5.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		line5.add(new JOAJLabel(b.getString("kBackgroundColor")));
		mapBG = new Swatch(mMapSpec.getBGColor(), 12, 12);
		line5.add(mapBG);
		JPanel line6 = new JPanel();
		line6.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		line6.add(new JOAJLabel(b.getString("kCoastlineColor")));
		coastline = new Swatch(mMapSpec.getCoastColor(), 12, 12);
		line6.add(coastline);
		JPanel line7 = new JPanel();
		line7.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		line7.add(new JOAJLabel(b.getString("kGraticuleColor")));
		gratColor = new Swatch(mMapSpec.getGratColor(), 12, 12);
		line7.add(gratColor);
		JPanel line8 = new JPanel();
		line8.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		line8.add(new JOAJLabel(b.getString("kColorSchemes")));
		Vector<String> presetSchemes = new Vector<String>();
		presetSchemes.addElement(b.getString("kDefault"));
		presetSchemes.addElement(b.getString("kWhiteBackground"));
		presetSchemes.addElement(b.getString("kBlackBackground"));
		presetColorSchemes = new JOAJComboBox(presetSchemes);
		presetColorSchemes.setSelectedItem(b.getString("kDefault"));
		presetColorSchemes.addItemListener(this);
		line8.add(presetColorSchemes);

		JPanel swatchCont = new JPanel();
		swatchCont.setLayout(new GridLayout(4, 1, 0, 5));
		swatchCont.add(line5);
		swatchCont.add(line6);
		swatchCont.add(line7);
		JPanel swatchContCont = new JPanel();
		swatchContCont.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		swatchContCont.add(swatchCont);
		swatchContCont.add(line8);
		tb = BorderFactory.createTitledBorder(b.getString("kMapColors"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		swatchContCont.setBorder(tb);
		upperLeftPanel.add("East", swatchContCont);

		// original station color state
		if (mMapSpec.getStnColorMode() != MapSpecification.COLOR_STNS_BY_JOADEFAULT) {
			mStnCBName = mMapSpec.getStnColorColorBar().getTitle();
		}

		if (mMapSpec.getContourOverlayMode() != MapSpecification.CONTOUR_OVERLAY_BY_NONE) {
			mOvlCBName = mMapSpec.getOverlayContoursColorBar().getTitle();
		}

		// create the tab pane for the station color options
		mStationColorTabPane = new JOAJTabbedPane();

		isoSymbolsPanel = new IsoSurfaceSpecificationSymbols(mFileViewer, b.getString("kStartingIsoValue"));
		stnCalcPanel = new StationCalculationSymbols(mFileViewer);
		defaultPanel = new DefaultSymbols(mFileViewer);
		metadataPanel = new MetadataSymbols(mFileViewer);

		mStationColorTabPane.addTab(b.getString("kAssignedStnColors"), new TenPixelBorder(defaultPanel, 20, 10, 10, 0));

		if (f3) {
			mStationColorTabPane.addTab(b.getString("kIsoSurface"), new TenPixelBorder(isoSymbolsPanel, 20, 10, 0, 10));
		}

		if (f4) {
			mStationColorTabPane.addTab(b.getString("kStnValStnColors"), new TenPixelBorder(stnCalcPanel, 20, 10, 10, 0));
		}

		if (f5) {
			mStationColorTabPane.addTab(b.getString("kMetadataStnColors"), new TenPixelBorder(metadataPanel, 20, 10, 10, 0));
		}

		mStationColorTabPane.addChangeListener(this);
		stationColorPanel.add(mStationColorTabPane);
		middleContPanel.add(middlePanel, "Center");

		// initialize the station coloring options
		if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
			mStationColorTabPane.setSelectedIndex(1);
			isoSymbolsPanel.setSelectedParam(mMapSpec.getStnColorByIsoVarCode());

			// test whether the colorbar is autoscaled
			if (mMapSpec.isStnColorByIsoAutoscaledColorCB()) {
				// display the autoscaled colorbar
				isoSymbolsPanel.setColorBar(mMapSpec.getStnColorColorBar());
				isoSymbolsPanel.updateColorBar();
			}
			else {
				String cbName2 = mMapSpec.getStnColorColorBar().getParam();
				String cbTitle2 = mMapSpec.getStnColorColorBar().getTitle();

				// display a built-in or saved colorbar
				int c = 0;
				for (int i = 0; i < cbs.size(); i++) {
					NewColorBar cb = null;

					String newCBARName = (String)cbs.elementAt(i);

					// read the color bar from disk
					try {
						cb = JOAFormulas.getColorBar(newCBARName);
					}
					catch (Exception ex) {
						cb = null;
					}

					if (cb.isMetadataColorBar()) {
						continue;
					}

					if (mMapSpec.getStnColorColorBar() == null) {
						break;
					}

					String cbName1 = cb.getParam();
					String cbTitle1 = cb.getTitle();

					if (cbName1.equalsIgnoreCase(cbName2) && cbTitle1.equalsIgnoreCase(cbTitle2)) {
						isoSymbolsPanel.setSelectedIsoCB(c);
						break;
					}

					c++;
				}
			}

			// Highlight either the selected surface or the default surface
			if (mMapSpec.getStnColorByIsoSurface() != null) {
				for (int i = 0; i < spl.size(); i++) {
					NewInterpolationSurface is = null;

					String newSurfaceName = (String)spl.elementAt(i);

					// read the surface from disk
					try {
						is = JOAFormulas.getSurface(newSurfaceName);
					}
					catch (Exception ex) {
						is = null;
					}

					if (mMapSpec.getStnColorColorBar() == null) {
						break;
					}

					try {
						String cbName1 = new String(is.getParam());
						String cbName2 = mMapSpec.getStnColorByIsoSurface().getParam();
						String cbTitle1 = new String(is.getTitle());
						String cbTitle2 = mMapSpec.getStnColorByIsoSurface().getTitle();
						String cbDescrip1 = new String(is.getDescrip());
						String cbDescrip2 = mMapSpec.getStnColorByIsoSurface().getDescrip();

						if (mMapSpec.getStnColorByIsoSurface().getParam() != null
						    && (cbName1.equalsIgnoreCase(cbName2) && cbTitle1.equalsIgnoreCase(cbTitle2) && cbDescrip1
						        .equalsIgnoreCase(cbDescrip2))) {
							isoSymbolsPanel.setSelectedSurface(i);
							isoSymbolsPanel.setColorBar(mMapSpec.getStnColorColorBar());
							isoSymbolsPanel.updateColorBar();
							break;
						}
					}
					catch (Exception ex) {
						// silent--can't be a match if a surface doesn't define a
						// description and another does.
					}
				}
			}

			isoSymbolsPanel.getSurfaceSpinner().setValues(mMapSpec.getStnColorByIsoSurface().getValues(),
			    mMapSpec.getStnColorByIsoSurface().getNumLevels());

			if (mMapSpec.getStnColorByIsoReferenceLevel() >= 0) {
				isoSymbolsPanel.setReferenceLevel(mMapSpec.getStnColorByIsoReferenceLevel());
			}

			if (mMapSpec.getStnColorByIsoSurface().getParam().equalsIgnoreCase("PRES")
			    || mMapSpec.getStnColorByIsoSurface().getParam().startsWith("SIG")) {
				;
			}
			isoSymbolsPanel.getSurfaceSpinner().setReversed(true);

			isoSymbolsPanel.getSurfaceSpinner().setValue(mMapSpec.getStnColorByIsoIsoSurfaceValue());

			if (mMapSpec.isStnColorByIsoMinIsoSurfaceValue() || mMapSpec.isStnColorByIsoMaxIsoSurfaceValue()) {
				isoSymbolsPanel.disableValueSpinner();
			}
			else {
				isoSymbolsPanel.enableValueSpinner();
			}
		}
		else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
			mStationColorTabPane.setSelectedIndex(2);
			// show the selected station variable in the list
			stnCalcPanel.setSelectedVar(mMapSpec.getStnColorByStnValVarCode());

			// show the selected colorbar in the list
			// test whether the colorbar is autoscaled
			if (mMapSpec.isStnColorByStnValAutoscaledColorBar()) {
				// display the autoscaled colorbar
				stnCalcPanel.setColorBar(mMapSpec.getStnColorColorBar());
				stnCalcPanel.updateColorBar();
			}
			else {
				for (int i = 0; i < cbs.size(); i++) {
					NewColorBar cb = null;

					String newCBARName = (String)cbs.elementAt(i);

					// read the color bar from disk
					try {
						cb = JOAFormulas.getColorBar(newCBARName);
					}
					catch (Exception ex) {
						cb = null;
					}

					if (mMapSpec.getStnColorColorBar() == null) {
						break;
					}

					String cbName1 = cb.getParam();
					String cbName2 = mMapSpec.getStnColorColorBar().getParam();
					String cbTitle1 = cb.getTitle();
					String cbTitle2 = mMapSpec.getStnColorColorBar().getTitle();

					if (cbName1.equalsIgnoreCase(cbName2) && cbTitle1.equalsIgnoreCase(cbTitle2)) {
						stnCalcPanel.setSelectedCB(i - 1);
					}
				}
			}
		}
		else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
			mStationColorTabPane.setSelectedIndex(3);
			// show the selected colorbar in the list
			int mdcbCount = -1;
			for (int i = 0; i < cbs.size(); i++) {
				NewColorBar cb = null;

				String newCBARName = (String)cbs.elementAt(i);

				// read the color bar from disk
				try {
					cb = JOAFormulas.getColorBar(newCBARName);
				}
				catch (Exception ex) {
					cb = null;
				}

				if (mMapSpec.getStnColorColorBar() == null) {
					break;
				}

				if (cb.isMetadataColorBar()) {
					mdcbCount++;
				}
				String cbName1 = cb.getParam();
				String cbName2 = mMapSpec.getStnColorColorBar().getParam();
				String cbTitle1 = cb.getTitle();
				String cbTitle2 = mMapSpec.getStnColorColorBar().getTitle();

				if (cbName1.equalsIgnoreCase(cbName2) && cbTitle1.equalsIgnoreCase(cbTitle2)) {
					metadataPanel.setSelectedCB(mdcbCount);
				}
			}
		}

		mOverlayContoursTabPane = new JOAJTabbedPane();

		noContoursPanel = new NoContours();
		isoContourPanel = new IsoSurfaceSpecificationContours(mFileViewer, b.getString("kIsoValue"));
		stnCalcContourPanel = new StationCalculationContours(mFileViewer);

		mOverlayContoursTabPane.addTab(b.getString("kNoContoursL"), new TenPixelBorder(noContoursPanel, 20, 10, 0, 10));
		if (f1) {
			mOverlayContoursTabPane.addTab(b.getString("kIsoSurface"), new TenPixelBorder(isoContourPanel, 20, 10, 0, 10));
		}
		if (f2) {
			mOverlayContoursTabPane.addTab(b.getString("kStnValStnColors"), new TenPixelBorder(stnCalcContourPanel, 20, 10,
			    10, 0));
		}
		mOverlayContoursTabPane.addChangeListener(this);

		if (f1 || f2) {
			overlayContoursPanel.add(mOverlayContoursTabPane);
		}

		// initialize the station contouring options
		if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
			mContourByFlag = mMapSpec.getContourOverlayMode();
			mOverlayContoursTabPane.setSelectedIndex(1);
			isoContourPanel.setSelectedParam(mMapSpec.getIsoContourVarCode());

			// test whether the colorbar is autoscaled
			if (mMapSpec.isIsoContourAutoscaledColorCB()) {
				// display the autoscaled colorbar
				isoContourPanel.setColorBar(mMapSpec.getOverlayContoursColorBar());
				isoContourPanel.updateColorBar();
			}
			else {
				String cbName2 = mMapSpec.getOverlayContoursColorBar().getParam();
				String cbTitle2 = mMapSpec.getOverlayContoursColorBar().getTitle();

				// display a built-in or saved colorbar
				int c = 0;
				for (int i = 0; i < cbs.size(); i++) {
					NewColorBar cb = null;

					String newCBARName = (String)cbs.elementAt(i);

					// read the color bar from disk
					try {
						cb = JOAFormulas.getColorBar(newCBARName);
					}
					catch (Exception ex) {
						cb = null;
					}

					if (cb.isMetadataColorBar()) {
						continue;
					}

					if (mMapSpec.getOverlayContoursColorBar() == null) {
						break;
					}

					String cbName1 = cb.getParam();
					String cbTitle1 = cb.getTitle();

					if (cbName1.equalsIgnoreCase(cbName2) && cbTitle1.equalsIgnoreCase(cbTitle2)) {
						isoContourPanel.setSelectedIsoCB(c);
						break;
					}

					c++;
				}
			}

			// Highlight either the selected surface or the default surface
			if (mMapSpec.getIsoContourSurface() != null) {
				for (int i = 0; i < spl.size(); i++) {
					NewInterpolationSurface is = null;

					String newSurfaceName = (String)spl.elementAt(i);

					// read the surface from disk
					try {
						is = JOAFormulas.getSurface(newSurfaceName);
					}
					catch (Exception ex) {
						is = null;
					}

					if (mMapSpec.getOverlayContoursColorBar() == null) {
						break;
					}

					try {
						String cbName1 = new String(is.getParam());
						String cbName2 = mMapSpec.getIsoContourSurface().getParam();
						String cbTitle1 = new String(is.getTitle());
						String cbTitle2 = mMapSpec.getIsoContourSurface().getTitle();
						String cbDescrip1 = new String(is.getDescrip());
						String cbDescrip2 = mMapSpec.getIsoContourSurface().getDescrip();

						if (mMapSpec.getIsoContourSurface().getParam() != null
						    && (cbName1.equalsIgnoreCase(cbName2) && cbTitle1.equalsIgnoreCase(cbTitle2) && cbDescrip1
						        .equalsIgnoreCase(cbDescrip2))) {
							isoContourPanel.setSelectedSurface(i);
							isoContourPanel.setColorBar(mMapSpec.getOverlayContoursColorBar());
							isoContourPanel.updateColorBar();
							break;
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
						// silent--can't be a match if a surface doesn't define a
						// description and another does.
					}
				}
			}

			isoContourPanel.getSurfaceSpinner().setValues(mMapSpec.getIsoContourSurface().getValues(),
			    mMapSpec.getIsoContourSurface().getNumLevels());

			if (mMapSpec.getIsoContourSurface().getParam().equalsIgnoreCase("PRES")
			    || mMapSpec.getIsoContourSurface().getParam().startsWith("SIG")) {
				;
			}
			isoContourPanel.getSurfaceSpinner().setReversed(true);

			if (mMapSpec.getIsoContourReferenceLevel() >= 0) {
				isoContourPanel.setReferenceLevel(mMapSpec.getIsoContourReferenceLevel());
			}

			isoContourPanel.getSurfaceSpinner().setValue(mMapSpec.getContourIsoSurfaceValue());

			if (mMapSpec.isIsoContourMinSurfaceValue() || mMapSpec.isIsoContourMaxSurfaceValue()) {
				isoContourPanel.disableValueSpinner();
			}
			else {
				isoContourPanel.enableValueSpinner();
			}
		}
		else if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
			mContourByFlag = MapSpecification.CONTOUR_OVERLAY_BY_STNVAR;
			mOverlayContoursTabPane.setSelectedIndex(2);
			// show the selected station variable in the list
			stnCalcContourPanel.setSelectedVar(mMapSpec.getStnCalcContourVarCode());

			// show the selected colorbar in the list
			// test whether the colorbar is autoscaled
			if (mMapSpec.isStnCalcContourAutoscaledColorCB()) {
				// display the autoscaled colorbar
				stnCalcContourPanel.setColorBar(mMapSpec.getOverlayContoursColorBar());
				stnCalcContourPanel.updateColorBar();
			}
			else {
				for (int i = 0; i < cbs.size(); i++) {
					NewColorBar cb = null;

					String newCBARName = (String)cbs.elementAt(i);

					// read the color bar from disk
					try {
						cb = JOAFormulas.getColorBar(newCBARName);
					}
					catch (Exception ex) {
						cb = null;
					}

					if (mMapSpec.getOverlayContoursColorBar() == null) {
						break;
					}

					String cbName1 = cb.getParam();
					String cbName2 = mMapSpec.getOverlayContoursColorBar().getParam();
					String cbTitle1 = cb.getTitle();
					String cbTitle2 = mMapSpec.getOverlayContoursColorBar().getTitle();

					if (cbName1.equalsIgnoreCase(cbName2) && cbTitle1.equalsIgnoreCase(cbTitle2)) {
						stnCalcContourPanel.setSelectedCB(i - 1);
					}
				}
			}
		}

		// lower panel
		if (mClient != null) {
			mOKBtn = new JOAJButton(b.getString("kOK"));
		}
		else {
			mOKBtn = new JOAJButton(b.getString("kPlot"));
		}
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mApplyButton = new JOAJButton(b.getString("kApply"));
		mApplyButton.setActionCommand("apply");
		mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		mSaveButton = new JOAJButton("Save Map...");
		mSaveButton.setActionCommand("save");
		mReadButton = new JOAJButton("Load Map...");
		mReadButton.setActionCommand("read");
		mMakeDefaultButton = new JOAJButton("Make Map Default");
		mMakeDefaultButton.setActionCommand("defaults");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(2, 3, 5, 5));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mApplyButton);
			if (mClient == null) {
				mApplyButton.setEnabled(false);
			}
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mReadButton);
			dlgBtnsPanel.add(mSaveButton);
			dlgBtnsPanel.add(mMakeDefaultButton);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mApplyButton);
			if (mClient == null) {
				mApplyButton.setEnabled(false);
			}
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mSaveButton);
			dlgBtnsPanel.add(mReadButton);
			dlgBtnsPanel.add(mMakeDefaultButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mOKBtn.addActionListener(this);
		mCancelButton.addActionListener(this);
		mSaveButton.addActionListener(this);
		mReadButton.addActionListener(this);
		mApplyButton.addActionListener(this);
		mMakeDefaultButton.addActionListener(this);

		// build the upperPanel
		upperPanel.add(upperLeftPanel);
		upperPanel.add(swatchContCont);

		// add all the sub panels to main panel
		mainPanel.add(new TenPixelBorder(upperPanel, 5, 5, 5, 5), "North");
		mainPanel.add(new TenPixelBorder(middleContPanel, 5, 5, 5, 5), "Center");

		everyThingPanel.addTab(b.getString("kMapTab1"), mainPanel);
		everyThingPanel.addTab(b.getString("kMapTab2"), new TenPixelBorder(mBathyPane, 10, 10, 10, 10));
		if (f3 || f4 || f5)
			everyThingPanel.addTab(b.getString("kMapTab3"), new TenPixelBorder(stationColorPanel, 10, 10, 10, 10));
		if (f1 || f2)
			everyThingPanel.addTab(b.getString("kMapTab4"), new TenPixelBorder(overlayContoursPanel, 10, 10, 10, 10));
		contents.add(new TenPixelBorder(everyThingPanel, 5, 5, 5, 5), "Center");
		contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		everyThingPanel.setSelectedIndex(0);
		this.pack();
		mBathyPane.setDividerLocation(0.6);

		/*
		 * if (mMapSpec.isColorByIsosurface()) { enableIsoSurfaceStuff(); } else {
		 * disableIsoSurfaceStuff(); }
		 * 
		 * if (mMapSpec.isColorByStnVar()) { enableStnVarStuff(); } else {
		 * disableStnVarStuff(); }
		 * 
		 * if (mMapSpec.isColorByMetadata()) { enableMetadataStuff(); } else {
		 * disableMetadataStuff(); }
		 */

		// update the UI
		// setUIFromMapSpec();
		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);

		setAvailableMapRegions();
		// if (isnew)
		// setToBasin();

		prRegionPanel.add(presetRegions);
		presetRegions.addItemListener(this);
		if (mMapSpec.getMapName() != null) {
			mNameField.setText(mMapSpec.getMapName());
		}
		else {
			generatePlotName();
		}

		runTimer();
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	public void setAvailableMapRegions() {
		Vector<String> presetData = new Vector<String>();
		int proj = mMapSpec.getProjection();
		int projIndex = proj > 5 ? proj - 5 : proj - 1;

		for (int i = 0; i < mProjSpecificMapRegions[projIndex].length; i++) {
			presetData.addElement(mProjSpecificMapRegions[projIndex][i]);
		}

		if (presetRegions != null) {
			presetRegions.removeAllItems();
			presetRegions.removeItemListener(this);
			for (int i = 0; i < presetData.size(); i++) {
				ignoreItemChange = true;
				presetRegions.addItem(presetData.elementAt(i));
			}
			presetRegions.addItemListener(this);
		}
		else {
			presetRegions = new JOAJComboBox(presetData);
			presetRegions.addItemListener(this);
		}

		/*
		 * if (!mIsCustomMap) { if (presetRegions != null) {
		 * presetRegions.removeAllItems(); for (int i=0; i<presetData.size(); i++)
		 * presetRegions.addItem(presetData.elementAt(i)); } else presetRegions =
		 * new JOAJComboBox(presetData);
		 * 
		 * presetRegions.setSelectedIndex(0); mMapSpec.setCurrBasin(0); } else { if
		 * (presetRegions == null) presetRegions = new JOAJComboBox(presetData);
		 * 
		 * presetRegions.setSelectedIndex(presetData.size() - 1);
		 * mMapSpec.setCurrBasin(presetData.size() - 1); }
		 */
	}

	public void disableConnectOption() {
		connectStnsAcrossSections.setEnabled(false);
	}

	public void enableConnectOption() {
		connectStnsAcrossSections.setEnabled(true);
	}

	public void disableEtopoStuff() {
		if (mEtopoList == null) { return; }
		mEtopoList.setEnabled(false);
		mFillColorbarList.setEnabled(false);
	}

	public void enableEtopoStuff() {
		if (mEtopoList == null) { return; }
		mEtopoList.setEnabled(true);
		mFillColorbarList.setEnabled(true);
	}

	public void disableCustomCoast() {
		if (customCoastsList != null) {
			customCoastsList.setEnabled(false);
		}
	}

	public void enableCustomCoast() {
		if (customCoastsList != null) {
			customCoastsList.setEnabled(true);
		}
	}

	public void initMapSpec() {
		// set up default map if no saved default map
		mMapSpec.setProjection(JOAConstants.MERCATORPROJECTION);
		mMapSpec.setConnectStns(false);
		mMapSpec.setConnectStnsAcrossSections(true);
		mMapSpec.setSymbolSize(3);
		mMapSpec.setPlotStnSymbols(true);
		mMapSpec.setSymbol(JOAConstants.SYMBOL_SQUAREFILLED);
		mMapSpec.setLineWidth(1);
		mMapSpec.setLatMax(85.0);
		mMapSpec.setLatMin(-85.0);
		mMapSpec.setLonRt(19.99);
		mMapSpec.setLonLft(20);
		mMapSpec.setCenLat(0.0);
		mMapSpec.setCenLon(0.0);
		mMapSpec.setDrawGraticule(true);
		mMapSpec.setLatGratSpacing(15.0);
		mMapSpec.setLonGratSpacing(15.0);
		mMapSpec.setBGColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
		mMapSpec.setCoastColor(Color.black);
		mMapSpec.setGratColor(new Color(0, 50, 50));
		mMapSpec.setSectionColor(Color.red);
		mMapSpec.setCurrBasin(0);
		mMapSpec.setRetainProjAspect(true);
		mMapSpec.setColorFill(true);
		String cbar = "ROSE-grayscale_cbr.xml";
		try {
			mMapSpec.setBathyColorBar(JOAFormulas.getColorBar(cbar));
		}
		catch (Exception ex) {
			mMapSpec.setBathyColorBar(null);
			mMapSpec.setColorFill(false);
		}
		mMapSpec.setGlobe(false);
		mMapSpec.setCustomMap(false);
		mMapSpec.setLabelColor(new Color(204, 51, 0));
		mMapSpec.setNumIsobaths(0);

		// todo
		// mMapSpec.resetNumEtopoFiles();
		// mMapSpec.setEtopoFile("etopo60.nc");

		mMapSpec.setPlotSectionLabels(false);
		mMapSpec.setPlotStnLabels(false);
		mMapSpec.setStnLabelAngle(45.0);
		mMapSpec.setStnLabelOffset(5);
		mMapSpec.setContourLabelPrec(2);

		// init the station color variables
		mMapSpec.setStnColorMode(MapSpecification.COLOR_STNS_BY_JOADEFAULT);

		// init variables for coloring stations by isosurface
		mMapSpec.setContourIsoSurfaceValue(0.0);
		mMapSpec.setIsoContourMinSurfaceValue(false);
		mMapSpec.setIsoContourMaxSurfaceValue(false);
		mMapSpec.setIsoContIsResidualInterp(false);
		mMapSpec.setIsoContRefLevel(JOAConstants.MISSINGVALUE);
		mMapSpec.setIsoContMeanCastStnList(null);

		// init variables for contouring
		mMapSpec.setContourOverlayMode(MapSpecification.CONTOUR_OVERLAY_BY_NONE);

		// set the contour variables for isosurface
		mMapSpec.setContourIsoSurfaceValue(0.0);
		mMapSpec.setIsoContourMinSurfaceValue(false);
		mMapSpec.setIsoContourMaxSurfaceValue(false);
		mMapSpec.setIsoContourBottomUpSearch(false);
		mMapSpec.setIsoContourLocalInterpolation(false);
		mMapSpec.setIsoContourMaxInterpDistance(2);
		mMapSpec.setIsoContourAutoscaledColorCB(false);
		mMapSpec.setIsoContourColor(Color.black);

		// set the contour variables for stn value
		mMapSpec.setStnCalcContourAutoscaledColorCB(false);
		mMapSpec.setStnVarCalcContourColor(Color.black);

		// common contour variables
		mMapSpec.setPlotEveryNthContour(1);

		// look for a default map
		try {
			if (JOAConstants.DEFAULT_MAP_SPECIFICATION != null) {
				mMapSpec = new MapSpecification(JOAConstants.DEFAULT_MAP_SPECIFICATION);
			}
		}
		catch (Exception ex) {
			// fall through and just use defaults
		}
	}

	public void valueChanged(ListSelectionEvent evt) {
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel") && mClient != null) {
			// mClient.dialogCancelled(this);
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok") && mClient != null) {
			this.setVisible(false);
			mClient.dialogDismissed(this);
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("cancel")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("apply") && mClient != null) {
			mClient.dialogApply(this);
			mOriginalMapSpec = null;
			mOriginalMapSpec = getMapSpec();

			if (mMapSpec.getStnColorMode() != MapSpecification.COLOR_STNS_BY_JOADEFAULT) {
				mStnCBName = mMapSpec.getStnColorColorBar().getTitle();
			}
			else {
				mStnCBName = null;
			}

			if (mMapSpec.getContourOverlayMode() != MapSpecification.CONTOUR_OVERLAY_BY_NONE) {
				mOvlCBName = mMapSpec.getOverlayContoursColorBar().getTitle();
			}
			else {
				mOvlCBName = null;
			}
			
			mLayoutChanged = false;
		}
		else if (cmd.equals("ok")) {
//			if (customCoast.isSelected()) {
//				mMapSpec.setCustCoastPath(new String(custCoastPaths[customCoastsList.getSelectedIndex()]));
//				mMapSpec.setCustCoastDescrip(new String(custCoastDescrips[customCoastsList.getSelectedIndex()]));
//			}
//			else {
//				mMapSpec.setCustCoastPath(null);
//				mMapSpec.setCustCoastDescrip(null);
//			}
//
//			// get the state of the swatches
//			int cnt = 0;
//			mMapSpec.setNumIsobaths(0);
//			for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
//				if (checks[i].isSelected()) {
//					mMapSpec.setValue(cnt, Double.valueOf(allIsobathValues[i]).doubleValue());
//					mMapSpec.setColor(cnt, swatches[i].getColor());
//					mMapSpec.setDescrip(cnt, allIsobathDescrips[i]);
//					mMapSpec.setPath(cnt, allIsobathPaths[i]);
//					mMapSpec.setNumIsobaths(++cnt);
//				}
//			}
//
//			// get the custom region
//			mMapSpec.setLatMax(((Double)topLatFld.getValue()).doubleValue());
//			mMapSpec.setLatMin(((Double)bottLatFld.getValue()).doubleValue());
//			mMapSpec.setLonLft(((Double)leftLonFld.getValue()).doubleValue());
//			mMapSpec.setLonRt(((Double)rightLonFld.getValue()).doubleValue());
//
//			// get the map center
//			String fldText = mCenterLatFld.getText();
//			mMapSpec.setCenLat(Double.valueOf(fldText).doubleValue());
//			fldText = mCenterLonFld.getText();
//			mMapSpec.setCenLon(Double.valueOf(fldText).doubleValue());
//			// mMapSpec.setIsCustomMap(mIsCustomMap);
//
//			// setMapCenter();
//
//			// get the custom colors
//			mMapSpec.setGratColor(gratColor.getColor());
//			mMapSpec.setBGColor(mapBG.getColor());
//			mMapSpec.setCoastColor(coastline.getColor());
//
//			// grat spacings
//			fldText = mLatSpacing.getText();
//			mMapSpec.setLatGratSpacing(Double.valueOf(fldText).intValue());
//			if (mMapSpec.getLatGratSpacing() < 1.0) {
//				mMapSpec.setLatGratSpacing(1.0);
//			}
//			fldText = mLonSpacing.getText();
//			mMapSpec.setLonGratSpacing(Double.valueOf(fldText).intValue());
//			if (mMapSpec.getLonGratSpacing() < 1.0) {
//				mMapSpec.setLonGratSpacing(1.0);
//			}
//
//			// get the symbol size
//			mMapSpec.setPlotStnSymbols(plotStnSymbols.isSelected());
//			mMapSpec.setSymbolSize(((Integer)mSymbolSizeSpinner.getValue()).intValue());
//			mMapSpec.setPlotStnLabels(plotStnLabels.isSelected());
//
//			// get the station color stuff
//			mMapSpec.setStnColorMode(mColorByFlag);
//
//			if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
//				mMapSpec.setStnColorByIsoAutoscaledColorCB(false);
//				mMapSpec.setStnColorByIsoMinIsoSurfaceValue(isoSymbolsPanel.isMinSurfVal());
//				mMapSpec.setStnColorByIsoMaxIsoSurfaceValue(isoSymbolsPanel.isMaxSurfVal());
//				mMapSpec.setStnColorByIsoVarCode(mFileViewer.getPropertyPos(isoSymbolsPanel.getParamName(), false));
//
//				try {
//					mMapSpec.setStnColorByIsoIsoSurfaceValue(isoSymbolsPanel.getSurfaceSpinner().getValue());
//				}
//				catch (Exception ex) {
//				}
//
//				if (isoSymbolsPanel.mUseRefLevel.isSelected()) {
//					mMapSpec.setStnColorByIsoReferenceLevel(isoSymbolsPanel.getReferenceLevel());
//					mMapSpec.setStnColorByIsoIsReferenced(true);
//				}
//				else {
//					mMapSpec.setStnColorByIsoIsReferenced(false);
//				}
//
//				// use either the on-disk colorbar or the autoscale one
//				mMapSpec.setStnColorByIsoAutoscaledColorCB(isoSymbolsPanel.isAutoScaledColor());
//				try {
//					mMapSpec.setStnColorColorBar(isoSymbolsPanel.getColorBar());
//				}
//				catch (Exception ex) {
//					mMapSpec.setStnColorColorBar(null);
//				}
//
//				String newSurfaceName = isoSymbolsPanel.getSurfaceName();
//
//				// read the surface from disk
//				try {
//					mMapSpec.setStnColorByIsoSurface(JOAFormulas.getSurface(newSurfaceName));
//				}
//				catch (Exception ex) {
//					mMapSpec.setStnColorByIsoSurface(null);
//				}
//				mMapSpec.setStnColorByIsoSurfVarCode(mFileViewer.getPropertyPos(mMapSpec.getStnColorByIsoSurface().getParam(),
//				    false));
//
//				mMapSpec.setStnColorByIsoBottomUpSearch(isoSymbolsPanel.isBottomUpSearch());
//				mMapSpec.setStnColorByIsoLocalInterpolation(isoSymbolsPanel.isLocalInterpolation());
//				mMapSpec.setStnColorByIsoMaxInterpDistance(isoSymbolsPanel.getMaxInterpDistance());
//			}
//			else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
//				mMapSpec.setStnColorByStnValAutoscaledColorBar(false);
//				// get the station var to color by
//				mMapSpec.setStnColorByStnValVarCode(stnCalcPanel.getStnCalcIndex());
//
//				mMapSpec.setStnColorByStnValAutoscaledColorBar(stnCalcPanel.isAutoScaledColor());
//				try {
//					mMapSpec.setStnColorColorBar(stnCalcPanel.getColorBar());
//				}
//				catch (Exception ex) {
//					ex.printStackTrace();
//					mMapSpec.setStnColorColorBar(null);
//				}
//			}
//			else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
//				// get the metadata colorbar
//				String newCBARName = metadataPanel.getColorBarName();
//
//				// read the color bar from disk
//				try {
//					mMapSpec.setStnColorColorBar(JOAFormulas.getColorBar(newCBARName));
//
//					// This colorbar has to know about the max and min values from the fileviewer
//					mMapSpec.getStnColorColorBar().setMetadata(mFileViewer.getMinLat(), mFileViewer.getMaxLat(),
//					    mFileViewer.getMinLon(), mFileViewer.getMaxLon(), mFileViewer.getMinDate(), mFileViewer.getMaxDate());
//					// add this as a listener
//					mFileViewer.addMetadataChangedListener(mMapSpec.getStnColorColorBar());
//				}
//				catch (Exception ex) {
//					ex.printStackTrace();
//					mMapSpec.setStnColorColorBar(null);
//				}
//			}
//
//			// get the overlay contour stuff
//			mMapSpec.setContourOverlayMode(mContourByFlag);
//
//			if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
//				mMapSpec.setIsoContourAutoscaledColorCB(false);
//				mMapSpec.setIsoContourVarCode(mFileViewer.getPropertyPos(isoContourPanel.getParamName(), false));
//				mMapSpec.setIsoContourMinSurfaceValue(isoContourPanel.isMinSurfVal());
//				mMapSpec.setIsoContourMaxSurfaceValue(isoContourPanel.isMaxSurfVal());
//
//				try {
//					mMapSpec.setContourIsoSurfaceValue(isoContourPanel.getSurfaceSpinner().getValue());
//				}
//				catch (Exception ex) {
//				}
//
//				mMapSpec.setIsoContourReferenceLevel(isoContourPanel.getReferenceLevel());
//
//				// use either the on-disk colorbar or the autoscale one
//				mMapSpec.setIsoContourAutoscaledColorCB(isoContourPanel.isAutoScaledColor());
//				try {
//					mMapSpec.setOverlayContoursColorBar(isoContourPanel.getColorBar());
//				}
//				catch (Exception ex) {
//					mMapSpec.setOverlayContoursColorBar(null);
//				}
//
//				String newSurfaceName = isoContourPanel.getSurfaceName();
//
//				// read the surface from disk
//				try {
//					mMapSpec.setIsoContourSurface(JOAFormulas.getSurface(newSurfaceName));
//				}
//				catch (Exception ex) {
//					mMapSpec.setIsoContourSurface(null);
//				}
//				mMapSpec
//				    .setIsoContourSurfVarCode(mFileViewer.getPropertyPos(mMapSpec.getIsoContourSurface().getParam(), false));
//				mMapSpec.setIsoContourBottomUpSearch(isoContourPanel.isBottomUpSearch());
//				mMapSpec.setIsoContourLocalInterpolation(isoContourPanel.isLocalInterpolation());
//				mMapSpec.setIsoContourMaxInterpDistance(isoContourPanel.getMaxInterpDistance());
//				mMapSpec.setPlotEveryNthContour(isoContourPanel.getContourSkipInterval());
//				mMapSpec.setIsoContourColor(isoContourPanel.getOverlayContourColor());
//				mMapSpec.setNRng(isoContourPanel.getNrng());
//				mMapSpec.setNX(isoContourPanel.getNumX());
//				mMapSpec.setNY(isoContourPanel.getNumY());
//				mMapSpec.setCAY(isoContourPanel.getCay());
//				mMapSpec.setMaskCoast(isoContourPanel.isLandMask());
//				mMapSpec.setFilledIsoContours(isoContourPanel.isFilledIsoContours());
//			}
//			else if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
//				mMapSpec.setStnCalcContourAutoscaledColorCB(false);
//				// get the station var to color by
//				mMapSpec.setStnCalcContourVarCode(stnCalcContourPanel.getStnCalcIndex());
//
//				mMapSpec.setStnCalcContourAutoscaledColorCB(stnCalcContourPanel.isAutoScaledColor());
//				try {
//					mMapSpec.setOverlayContoursColorBar(stnCalcContourPanel.getStnCalcContourColorBar());
//				}
//				catch (Exception ex) {
//					mMapSpec.setOverlayContoursColorBar(null);
//				}
//				mMapSpec.setPlotEveryNthContour(stnCalcContourPanel.getContourSkipInterval());
//				mMapSpec.setIsoContourColor(stnCalcContourPanel.getOverlayContourColor());
//				mMapSpec.setNRng(stnCalcContourPanel.getNrng());
//				mMapSpec.setNX(stnCalcContourPanel.getNumX());
//				mMapSpec.setNY(stnCalcContourPanel.getNumY());
//				mMapSpec.setCAY(stnCalcContourPanel.getCay());
//				mMapSpec.setMaskCoast(stnCalcContourPanel.isLandMask());
//				mMapSpec.setFilledStnContours(stnCalcContourPanel.isFilledStnContours());
//			}
//
//			// color fill bathy
//			if (mColorFill.isSelected()) {
//				mMapSpec.setColorFill(true);
//				// get the color fill colorbar
//				String newCBARName = (String)mFillColorbarList.getSelectedValue();
//				try {
//					mMapSpec.setBathyColorBar(JOAFormulas.getColorBar(newCBARName));
//				}
//				catch (Exception ex) {
//					mMapSpec.setBathyColorBar(null);
//					mMapSpec.setColorFill(false);
//				}
//				mMapSpec.setNumIsobaths(0);
//
//				mMapSpec.resetEtopoFiles();
//				Object[] o = mEtopoList.getSelectedValues();
//				for (int i = 0; i < o.length; i++) {
//					mMapSpec.setEtopoFile((String)o[i]);
//				}
//			}
//			else {
//				mMapSpec.setColorFill(false);
//			}
//			mMapSpec.setPlotSectionLabels(plotSectionLabels.isSelected());
//			mMapSpec.setPlotStnLabels(plotStnLabels.isSelected());
//			mMapSpec.setMapName(mNameField.getText());
//
//			if (plotStnLabels.isSelected()) {
//				// get the values for the other stn label parameters from the UI
//				mMapSpec.setStnLabelOffset(((Integer)mLblOffset.getValue()).intValue());
//				mMapSpec.setStnLabelAngle(((Double)mLblAngle.getValue()).doubleValue());
//			}
//			mMapSpec.setContourLabelPrec(((Integer)mLblPrec.getValue()).intValue());
//
//			try {
//				mMapSpec.writeToLog("New Map Plot (" + mFileViewer.getTitle() + ")");
//			}
//			catch (Exception ex) {
//			}
			this.getMapSpec();

			JOAMapPlotWindow plotWind = new JOAMapPlotWindow(mFileViewer, mMapSpec, 650, 650, mFileViewer);
			plotWind.pack();
			plotWind.setVisible(true);
			mFileViewer.addOpenWindow(plotWind);
			this.dispose();
		}
		else if (cmd.equals("all")) {
			for (int i = 0; i < allIsobathValues.length; i++) {
				checks[i].setSelected(true);
				swatches[i].setColor(Color.black);
			}
			checkAll.setSelected(false);
		}
		else if (cmd.equals("none")) {
			for (int i = 0; i < allIsobathValues.length; i++) {
				checks[i].setSelected(false);
				swatches[i].setColor(Color.black);
			}
			checkNone.setSelected(false);
		}
		else if (cmd.equals("ramp")) {
			ramp.setSelected(false);
			int numChecked = 0;
			for (int i = 0; i < allIsobathValues.length; i++) {
				if (checks[i].isSelected()) {
					numChecked++;
				}
			}
			int step;
			if (numChecked == 1) {
				step = 150;
			}
			else {
				step = 150 / (numChecked - 1);
			}
			int nc = 0;
			for (int i = 0; i < allIsobathValues.length; i++) {
				if (checks[i].isSelected()) {
					int index = (nc * step) < 150 ? nc * step : 150 - 1;
					nc++;
					swatches[i].setColor(new Color(bathyColorRamp[index]));
				}
				else {
					swatches[i].setColor(Color.black);
				}
			}
		}
		else if (cmd.equals("ramp2")) {
			ramp2.setSelected(false);
			int numChecked = 0;
			for (int i = 0; i < allIsobathValues.length; i++) {
				if (checks[i].isSelected()) {
					numChecked++;
				}
			}
			int step;
			if (numChecked == 1) {
				step = 150;
			}
			else {
				step = 150 / (numChecked - 1);
			}
			int nc = 0;
			for (int i = 0; i < allIsobathValues.length; i++) {
				if (checks[i].isSelected()) {
					int index = (nc * step) < 150 ? nc * step : 150 - 1;
					nc++;
					swatches[i].setColor(new Color(grayBathyColorRamp[index]));
				}
				else {
					swatches[i].setColor(Color.black);
				}
			}
		}
		else if (cmd.equals("ramp3")) {
			ramp3.setSelected(false);
			int numChecked = 0;
			for (int i = 0; i < allIsobathValues.length; i++) {
				if (checks[i].isSelected()) {
					numChecked++;
				}
			}
			int step;
			if (numChecked == 1) {
				step = 150;
			}
			else {
				step = 150 / (numChecked - 1);
			}
			int nc = 0;
			for (int i = 0; i < allIsobathValues.length; i++) {
				if (checks[i].isSelected()) {
					int index = (nc * step) < 150 ? nc * step : 150 - 1;
					nc++;
					swatches[i].setColor(new Color(rainbowBathyColorRamp[index]));
				}
				else {
					swatches[i].setColor(Color.black);
				}
			}
		}
		else if (cmd.equals("save")) {
			save("untitled_map.xml");
		}
		else if (cmd.equals("read")) {
			readSettings();

			// update the UI
			setUIFromMapSpec();
		}
		else if (cmd.equals("defaults")) {
			String mapAsXML = getXML();
			try {
	      PowerOceanAtlas.getInstance().localSync("JOA Default Map", mapAsXML);
      }
      catch (BackingStoreException e1) {
	      // TODO Auto-generated catch block
	      e1.printStackTrace();
      }
		}
	}

	public boolean isBathyNeeded() {
		for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
			if (checks[i].isSelected()) { return true; }
		}
		return false;
	}

	protected boolean isGlobe() {
		if (mMapSpec.getProjection() >= JOAConstants.ORTHOGRAPHICPROJECTION
		    && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
			// test whether regions are correct
			if ((mMapSpec.getLatMax() - mMapSpec.getLatMin() == 180)
			    && Math.abs(mMapSpec.getLonLft() - mMapSpec.getLonRt()) == 180) {
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}

	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() instanceof JTabbedPane) {
			// changes to station symbol coloring
			JTabbedPane jtp = (JTabbedPane)evt.getSource();

			if (jtp == mStationColorTabPane) {
				mColorByFlag = jtp.getSelectedIndex();
//				if (mColorByFlag > 0) {
//					mIncludeStnColorBar = true;
//				}
//				else {
//					mIncludeStnColorBar = false;
//				}
			}

			if (jtp == mOverlayContoursTabPane) {
				mContourByFlag = jtp.getSelectedIndex();
//				if (mColorByFlag > 0) {
//					mIncludeContColorBar = true;
//				}
//				else {
//					mIncludeContColorBar = false;
//				}
			}
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (ignoreItemChange) {
			ignoreItemChange = false;
			return;
		}
		if (evt.getSource() instanceof JOAJComboBox) {
			JOAJComboBox cb = (JOAJComboBox)evt.getSource();
			if (cb == mSymbolPopup) {
				mMapSpec.setSymbol(cb.getSelectedIndex() + 1);
			}
			else if (cb == presetRegions) {
				// mIsCustomMap = false;
				if (cb.getSelectedIndex() - 1 >= 0) {
					mMapSpec.setCurrBasin(cb.getSelectedIndex() - 1);
					mMapSpec.setGlobe(this.isGlobe());
					setToBasin();
					generatePlotName();
				}
			}
			else if (cb == mProjectionsPopup) {
				int newProj = cb.getSelectedIndex();
				if (newProj >= 5) {
					newProj += 5;
				}
				else {
					newProj++;
				}
				mMapSpec.setProjection(newProj);
				mMapSpec.setCurrBasin(0);
				mMapSpec.setGlobe(this.isGlobe());
				setAvailableMapRegions();

				/**
				 * @todo provide a projection-specific map region1
				 */
				setToBasin();
				generatePlotName();
			}
			else if (cb == presetColorSchemes) {
				int colorScheme = cb.getSelectedIndex();
				if (colorScheme == 0) {
					// default
					mapBG.setColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
					coastline.setColor(Color.black);
					mMapSpec.setBGColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
					mMapSpec.setCoastColor(Color.black);
				}
				else if (colorScheme == 1) {
					// white bg
					mapBG.setColor(Color.white);
					coastline.setColor(Color.black);
					mMapSpec.setBGColor(Color.white);
					mMapSpec.setCoastColor(Color.black);
				}
				else {
					// color bg
					mapBG.setColor(Color.black);
					coastline.setColor(Color.white);
					mMapSpec.setBGColor(Color.black);
					mMapSpec.setCoastColor(Color.white);
				}
			}

			if (mMapSpec.getStnColorByIsoSurface() != null
			    && !JOAFormulas.paramExists(mFileViewer, mMapSpec.getStnColorByIsoSurface().getParam())) {
				// post an alert
				if (JOAFormulas.isCalculatable(mMapSpec.getStnColorByIsoSurface().getParam())) {
					// make a new calculation
					Calculation calc = JOAFormulas.createCalcFromName(mFileViewer, mMapSpec.getStnColorByIsoSurface().getParam());

					if (calc != null) {
						// do calculation
						mFileViewer.addCalculation(calc);
						mFileViewer.doCalcs();
					}
				}
				else {
					JFrame f = new JFrame("Iso-Surface Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "Surface parameter does not exist in this file" + "\n"
					    + "and couldn't be calculated");
				}
			}

			if (mMapSpec.getIsoContourSurface() != null
			    && !JOAFormulas.paramExists(mFileViewer, mMapSpec.getIsoContourSurface().getParam())) {
				// post an alert
				if (JOAFormulas.isCalculatable(mMapSpec.getIsoContourSurface().getParam())) {
					// make a new calculation
					Calculation calc = JOAFormulas.createCalcFromName(mFileViewer, mMapSpec.getIsoContourSurface().getParam());

					if (calc != null) {
						// do calculation
						mFileViewer.addCalculation(calc);
						mFileViewer.doCalcs();
					}
				}
				else {
					JFrame f = new JFrame("Iso-Surface Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "Surface parameter does not exist in this file" + "\n"
					    + "and couldn't be calculated");
				}
			}
		}
		else if (evt.getSource() instanceof JOAJCheckBox) {
			JOAJCheckBox cb = (JOAJCheckBox)evt.getSource();
			// other checkboxes
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == mapGraticule) {
				mMapSpec.setDrawGraticule(true);
			}
			if (evt.getStateChange() == ItemEvent.DESELECTED && cb == mapGraticule) {
				mMapSpec.setDrawGraticule(false);
			}
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == retainAspect) {
				mMapSpec.setRetainProjAspect(true);
			}
			if (evt.getStateChange() == ItemEvent.DESELECTED && cb == retainAspect) {
				mMapSpec.setRetainProjAspect(false);
			}
		}
		else if (evt.getSource() instanceof JOAJRadioButton) {
			JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && rb == coarseRez) {
				mMapSpec.setCoastLineRez(JOAConstants.COARSERESOLUTION);
				disableCustomCoast();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == fineRez) {
				mMapSpec.setCoastLineRez(JOAConstants.FINERESOLUTION);
				disableCustomCoast();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == noCoast) {
				mMapSpec.setCoastLineRez(JOAConstants.NOCOAST);
				disableCustomCoast();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == customCoast) {
				mMapSpec.setCoastLineRez(JOAConstants.CUSTOMCOAST);
				enableCustomCoast();
			}
		}
	}

	public void setToBasin() {
		if (lockRgn.isSelected()) { return; }

		int proj = mMapSpec.getProjection();
		int projIndex = proj > 5 ? proj - 5 : proj - 1;
		// if (projIndex < 0 || mMapSpec.getCurrBasin() < 0) {
		// return;
		// }

		mMapSpec.setLatMin(mProjRegionSpecificMinLats[projIndex][mMapSpec.getCurrBasin()]);
		mMapSpec.setLatMax(mProjRegionSpecificMaxLats[projIndex][mMapSpec.getCurrBasin()]);
		mMapSpec.setLonLft(mProjRegionSpecificLeftLons[projIndex][mMapSpec.getCurrBasin()]);
		mMapSpec.setLonRt(mProjRegionSpecificRightLons[projIndex][mMapSpec.getCurrBasin()]);

		leftLonFld.setValue(new Double(mMapSpec.getLonLft()));
		topLatFld.setValue(new Double(mMapSpec.getLatMax()));
		rightLonFld.setValue(new Double(mMapSpec.getLonRt()));
		bottLatFld.setValue(new Double(mMapSpec.getLatMin()));
		setMapCenter();
	}

	public void setCenterLongitude() {
		if (mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {

			mMapSpec.setLonLft(((Double)leftLonFld.getValue()).doubleValue());
			mMapSpec.setLonRt(((Double)rightLonFld.getValue()).doubleValue());

			double diff = 0;
			if (mMapSpec.getLonLft() > 0 && mMapSpec.getLonRt() > 0 && mMapSpec.getLonLft() > mMapSpec.getLonRt()) {
				mMapSpec.setCenLon(-180 + mMapSpec.getLonRt());
			}
			else if (mMapSpec.getLonLft() < 0 && mMapSpec.getLonRt() < 0 && mMapSpec.getLonLft() > mMapSpec.getLonRt()) {
				mMapSpec.setCenLon(180 + mMapSpec.getLonLft());
			}
			else if (mMapSpec.getLonLft() > 0 && mMapSpec.getLonRt() < 0) {
				diff = Math.abs(((180 + mMapSpec.getLonRt()) + (180 - mMapSpec.getLonLft())) / 2);
				mMapSpec
				    .setCenLon(diff + mMapSpec.getLonLft() > 180 ? mMapSpec.getLonRt() - diff : mMapSpec.getLonLft() + diff);
			}
			else {
				diff = Math.abs((mMapSpec.getLonRt() - mMapSpec.getLonLft()) / 2);
				mMapSpec
				    .setCenLon(diff + mMapSpec.getLonLft() > 180 ? mMapSpec.getLonRt() - diff : mMapSpec.getLonLft() + diff);
			}
		}
		else if (mMapSpec.getProjection() == JOAConstants.NORTHPOLEPROJECTION) {
			mMapSpec.setCenLon(0.0);
		}
		else if (mMapSpec.getProjection() == JOAConstants.SOUTHPOLEPROJECTION) {
			mMapSpec.setCenLon(0.0);
		}
		mCenterLonFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getCenLon()), 2, false));
	}

	public void setCenterLatitude() {
		if (mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
			double latMax = ((Double)topLatFld.getValue()).doubleValue();
			double latMin = ((Double)bottLatFld.getValue()).doubleValue();

			mMapSpec.setCenLat(latMin + (latMax - latMin) / 2.0);
		}
		else if (mMapSpec.getProjection() == JOAConstants.NORTHPOLEPROJECTION) {
			mMapSpec.setCenLat(90.0);
		}
		else if (mMapSpec.getProjection() == JOAConstants.SOUTHPOLEPROJECTION) {
			mMapSpec.setCenLat(-90.0);
		}
		mCenterLatFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getCenLat()), 2, false));
	}

	public void setMapCenter() {
		setCenterLongitude();
		setCenterLatitude();
	}

	public int getBathyWidget(JOAJCheckBox cb) {
		for (int i = 0; i < allIsobathValues.length; i++) {
			if (checks[i] == cb) { return i; }
		}
		return -99;
	}

	public MapSpecification getOrigMapSpec() {
		return mOriginalMapSpec;
	}

	public MapSpecification getMapSpec() {
		// get the state of the isobath swatches
		int cnt = 0;
		mMapSpec.setNumIsobaths(0);
		for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
			if (checks[i].isSelected()) {
				mMapSpec.setValue(cnt, Double.valueOf(allIsobathValues[i]).doubleValue());
				mMapSpec.setColor(cnt, swatches[i].getColor());
				mMapSpec.setDescrip(cnt, allIsobathDescrips[i]);
				mMapSpec.setPath(cnt, allIsobathPaths[i]);
				mMapSpec.setNumIsobaths(++cnt);
			}
		}

		// custom coast?
		if (customCoast.isSelected()) {
			mMapSpec.setCustCoastPath(new String(custCoastPaths[customCoastsList.getSelectedIndex()]));
			mMapSpec.setCustCoastDescrip(new String(custCoastDescrips[customCoastsList.getSelectedIndex()]));
		}
		else {
			mMapSpec.setCustCoastPath(null);
			mMapSpec.setCustCoastDescrip(null);
		}

		// get the custom region
		mMapSpec.setLatMax(((Double)topLatFld.getValue()).doubleValue());
		mMapSpec.setLatMin(((Double)bottLatFld.getValue()).doubleValue());
		mMapSpec.setLonLft(((Double)leftLonFld.getValue()).doubleValue());
		mMapSpec.setLonRt(((Double)rightLonFld.getValue()).doubleValue());

		// setMapCenter();

		// get the map center
		String fldText = mCenterLatFld.getText();
		mMapSpec.setCenLat(Double.valueOf(fldText).doubleValue());
		fldText = mCenterLonFld.getText();
		mMapSpec.setCenLon(Double.valueOf(fldText).doubleValue());

		// mMapSpec.setIsCustomMap(mIsCustomMap);

		// get the custom colors
		mMapSpec.setGratColor(gratColor.getColor());
		mMapSpec.setBGColor(mapBG.getColor());
		mMapSpec.setCoastColor(coastline.getColor());

		// grat spacings
		fldText = mLatSpacing.getText();
		mMapSpec.setLatGratSpacing(Double.valueOf(fldText).intValue());
		if (mMapSpec.getLatGratSpacing() < 1.0) {
			mMapSpec.setLatGratSpacing(1.0);
		}
		fldText = mLonSpacing.getText();
		mMapSpec.setLonGratSpacing(Double.valueOf(fldText).intValue());
		if (mMapSpec.getLonGratSpacing() < 1.0) {
			mMapSpec.setLonGratSpacing(1.0);
		}

		// get the symbol
		mMapSpec.setPlotStnSymbols(plotStnSymbols.isSelected());
		mMapSpec.setSymbol(mSymbolPopup.getSelectedIndex() + 1);

		// get the symbol size
		mMapSpec.setSymbolSize(((Integer)mSymbolSizeSpinner.getValue()).intValue());
		mMapSpec.setPlotSectionLabels(plotSectionLabels.isSelected());

		// get the station color stuff
		mMapSpec.setStnColorMode(mColorByFlag);

		// contour overlay
		mMapSpec.setContourOverlayMode(mContourByFlag);

		if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
			// get the iso surface stuff
			mMapSpec.setStnColorByIsoAutoscaledColorCB(false);
			mMapSpec.setStnColorByIsoMinIsoSurfaceValue(isoSymbolsPanel.isMinSurfVal());
			mMapSpec.setStnColorByIsoMaxIsoSurfaceValue(isoSymbolsPanel.isMaxSurfVal());
			mMapSpec.setStnColorByIsoVarCode(mFileViewer.getPropertyPos(isoSymbolsPanel.getParamName(), false));

			try {
				mMapSpec.setStnColorByIsoIsoSurfaceValue(isoSymbolsPanel.getSurfaceSpinner().getValue());
			}
			catch (Exception ex) {
			}

			if (isoSymbolsPanel.mUseRefLevel.isSelected()) {
				mMapSpec.setStnColorByIsoReferenceLevel(isoSymbolsPanel.getReferenceLevel());
				mMapSpec.setStnColorByIsoIsReferenced(true);
			}
			else {
				mMapSpec.setStnColorByIsoIsReferenced(false);
			}

			mMapSpec.setStnColorByIsoAutoscaledColorCB(isoSymbolsPanel.isAutoScaledColor());
			String newCBARName = isoSymbolsPanel.getColorBarName();

			if (!isoSymbolsPanel.isAutoScaledColor()) {
				// read the color bar from disk
				try {
					mMapSpec.setStnColorColorBar(JOAFormulas.getColorBar(newCBARName));
				}
				catch (Exception ex) {
					mMapSpec.setStnColorColorBar(null);
				}
			}
			else {
				mMapSpec.setStnColorColorBar(isoSymbolsPanel.getColorBar());
			}

			// read the surface from disk
			String newSurfaceName = isoSymbolsPanel.getSurfaceName();
			try {
				mMapSpec.setStnColorByIsoSurface(JOAFormulas.getSurface(newSurfaceName));
			}
			catch (Exception ex) {
				mMapSpec.setStnColorByIsoSurface(null);
			}
			mMapSpec.setStnColorByIsoBottomUpSearch(isoSymbolsPanel.isBottomUpSearch());
			mMapSpec.setStnColorByIsoSurfVarCode(mFileViewer.getPropertyPos(mMapSpec.getStnColorByIsoSurface().getParam(),
			    false));
			mMapSpec.setStnColorByIsoLocalInterpolation(isoSymbolsPanel.isLocalInterpolation());
			mMapSpec.setStnColorByIsoMaxInterpDistance(isoSymbolsPanel.getMaxInterpDistance());
		}
		else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
			// get the station var to color by
			mMapSpec.setStnColorByStnValAutoscaledColorBar(false);
			mMapSpec.setStnColorByStnValVarCode(stnCalcPanel.getStnCalcIndex());
			mMapSpec.setStnColorByStnValAutoscaledColorBar(stnCalcPanel.isAutoScaledColor());

			if (!stnCalcPanel.isAutoScaledColor()) {
				// get the associated colorbar
				String newCBARName = stnCalcPanel.getColorBarName();

				// read the color bar from disk
				try {
					mMapSpec.setStnColorColorBar(JOAFormulas.getColorBar(newCBARName));
				}
				catch (Exception ex) {
					ex.printStackTrace();
					mMapSpec.setStnColorColorBar(null);
				}
			}
			else {
				mMapSpec.setStnColorColorBar(stnCalcPanel.getColorBar());
			}
		}
		else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
			// get the metadata colorbar
			String newCBARName = metadataPanel.getColorBarName();

			// read the color bar from disk
			try {
				mMapSpec.setStnColorColorBar(JOAFormulas.getColorBar(newCBARName));

				// This colorbar has to know about the max and min values from the
				// fileviewer
				mMapSpec.getStnColorColorBar().setMetadata(mFileViewer.getMinLat(), mFileViewer.getMaxLat(),
				    mFileViewer.getMinLon(), mFileViewer.getMaxLon(), mFileViewer.getMinDate(), mFileViewer.getMaxDate());
				// add this as a listener
				mFileViewer.addMetadataChangedListener(mMapSpec.getStnColorColorBar());
			}
			catch (Exception ex) {
				ex.printStackTrace();
				mMapSpec.setStnColorColorBar(null);
			}
		}

		if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
			// get the iso surface stuff
			mMapSpec.setIsoContourAutoscaledColorCB(false);
			mMapSpec.setIsoContourVarCode(mFileViewer.getPropertyPos(isoContourPanel.getParamName(), false));
			mMapSpec.setIsoContourMinSurfaceValue(isoContourPanel.isMinSurfVal());
			mMapSpec.setIsoContourMaxSurfaceValue(isoContourPanel.isMaxSurfVal());

			try {
				mMapSpec.setContourIsoSurfaceValue(isoContourPanel.getSurfaceSpinner().getValue());
			}
			catch (Exception ex) {
			}

			mMapSpec.setIsoContourAutoscaledColorCB(isoContourPanel.isAutoScaledColor());
			String newCBARName = isoContourPanel.getColorBarName();

			if (!isoContourPanel.isAutoScaledColor()) {
				// read the color bar from disk
				try {
					mMapSpec.setOverlayContoursColorBar(JOAFormulas.getColorBar(newCBARName));
				}
				catch (Exception ex) {
					mMapSpec.setOverlayContoursColorBar(null);
				}
			}
			else {
				mMapSpec.setOverlayContoursColorBar(isoContourPanel.getColorBar());
			}

			// read the surface from disk
			String newSurfaceName = isoContourPanel.getSurfaceName();
			try {
				mMapSpec.setIsoContourSurface(JOAFormulas.getSurface(newSurfaceName));
			}
			catch (Exception ex) {
				mMapSpec.setIsoContourSurface(null);
			}
			mMapSpec.setIsoContourBottomUpSearch(isoContourPanel.isBottomUpSearch());
			mMapSpec.setIsoContourSurfVarCode(mFileViewer.getPropertyPos(mMapSpec.getIsoContourSurface().getParam(), false));
			mMapSpec.setIsoContourLocalInterpolation(isoContourPanel.isLocalInterpolation());
			mMapSpec.setIsoContourMaxInterpDistance(isoContourPanel.getMaxInterpDistance());
			mMapSpec.setPlotEveryNthContour(isoContourPanel.getContourSkipInterval());
			mMapSpec.setIsoContourColor(isoContourPanel.getOverlayContourColor());
			mMapSpec.setNRng(isoContourPanel.getNrng());
			mMapSpec.setNX(isoContourPanel.getNumX());
			mMapSpec.setNY(isoContourPanel.getNumY());
			mMapSpec.setCAY(isoContourPanel.getCay());
			mMapSpec.setMaskCoast(isoContourPanel.isLandMask());
			mMapSpec.setIsoContourStyle(isoContourPanel.getContourStyle());
			mMapSpec.setFilledIsoContours(isoContourPanel.isFilledIsoContours());

			if (isoContourPanel.mUseRefLevel.isSelected()) {
				mMapSpec.setIsoContourReferenceLevel(isoContourPanel.getReferenceLevel());
				mMapSpec.setIsoContourReferenced(true);
			}
			else {
				mMapSpec.setIsoContourReferenced(false);
			}
		}
		else if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
			mMapSpec.setStnCalcContourAutoscaledColorCB(false);
			// get the station var to color by
			mMapSpec.setStnCalcContourVarCode(stnCalcContourPanel.getStnCalcIndex());
			mMapSpec.setStnCalcContourAutoscaledColorCB(stnCalcContourPanel.isAutoScaledColor());

			if (!stnCalcContourPanel.isAutoScaledColor()) {
				// get the associated colorbar
				String newCBARName = stnCalcPanel.getColorBarName();

				// read the color bar from disk
				try {
					mMapSpec.setOverlayContoursColorBar(JOAFormulas.getColorBar(newCBARName));
				}
				catch (Exception ex) {
					mMapSpec.setOverlayContoursColorBar(null);
				}
			}
			else {
				mMapSpec.setOverlayContoursColorBar(stnCalcContourPanel.getStnCalcContourColorBar());
			}
			mMapSpec.setFilledStnContours(stnCalcContourPanel.isFilledStnContours());
			mMapSpec.setFilledIsoContours(isoContourPanel.isFilledIsoContours());
			mMapSpec.setStnVarCalcContourColor(stnCalcContourPanel.getOverlayContourColor());
			mMapSpec.setNRng(stnCalcContourPanel.getNrng());
			mMapSpec.setNX(stnCalcContourPanel.getNumX());
			mMapSpec.setNY(stnCalcContourPanel.getNumY());
			mMapSpec.setCAY(stnCalcContourPanel.getCay());
			mMapSpec.setMaskCoast(stnCalcContourPanel.isLandMask());
			mMapSpec.setStnContourStyle(stnCalcContourPanel.getCountourStyle());
		}

		// color fill stuff
		mMapSpec.setColorFill(mColorFill.isSelected());
		if (mMapSpec.isColorFill()) {
			// get the color fill colorbar
			String newCBARName = (String)mFillColorbarList.getSelectedValue();
			try {
				mMapSpec.setBathyColorBar(JOAFormulas.getColorBar(newCBARName));
			}
			catch (Exception ex) {
				mMapSpec.setBathyColorBar(null);
				mMapSpec.setColorFill(false);
			}
			mMapSpec.setNumEtopoFiles(0);
			Object[] o = mEtopoList.getSelectedValues();

			for (int i = 0; i < o.length; i++) {
				mMapSpec.setEtopoFile((String)o[i]);
			}
		}

		mMapSpec.setPlotGratLabels(plotGratLabels.isSelected());
		mMapSpec.setPlotStnLabels(plotStnLabels.isSelected());

		// get the values for the other stn label parameters from the UI
		mMapSpec.setStnLabelOffset(((Integer)mLblOffset.getValue()).intValue());
		mMapSpec.setStnLabelAngle(((Double)mLblAngle.getValue()).doubleValue());
		mMapSpec.setContourLabelPrec(((Integer)mLblPrec.getValue()).intValue());
		mMapSpec.setMapName(mNameField.getText());
		return mMapSpec;
	}
	
	public int getStnCBState() {
		mLayoutChanged = false;
		return STN_CB_NO_CHANGE;
	}
	
	public int getOvlCBState() {
		mLayoutChanged = false;
		return OVL_CB_NO_CHANGE;
	}

	public boolean mapSpecChanged() {
		if (mMapSpec.getProjection() != mOriginalMapSpec.getProjection()) { return true; }
		if (mMapSpec.getCurrBasin() != mOriginalMapSpec.getCurrBasin()) { return true; }
		if (mMapSpec.getCoastLineRez() != mOriginalMapSpec.getCoastLineRez()) { return true; }
		if (mMapSpec.getLatMax() != mOriginalMapSpec.getLatMax()) { return true; }
		if (mMapSpec.getLatMin() != mOriginalMapSpec.getLatMin()) { return true; }
		if (mMapSpec.getLonRt() != mOriginalMapSpec.getLonRt()) { return true; }
		if (mMapSpec.getLonLft() != mOriginalMapSpec.getLonLft()) { return true; }
		if (mMapSpec.getCenLat() != mOriginalMapSpec.getCenLat()) { return true; }
		if (mMapSpec.getCenLon() != mOriginalMapSpec.getCenLon()) { return true; }
		if (mMapSpec.isConnectStns() != mOriginalMapSpec.isConnectStns()) { return true; }
		if (mMapSpec.isConnectStnsAcrossSections() != mOriginalMapSpec.isConnectStnsAcrossSections()) { return true; }
		if (mMapSpec.isDrawGraticule() != mOriginalMapSpec.isDrawGraticule()) { return true; }
		if (mMapSpec.isRetainProjAspect() != mOriginalMapSpec.isRetainProjAspect()) { return true; }
		if (mMapSpec.isAutoGraticule() != mOriginalMapSpec.isAutoGraticule()) { return true; }
		if (mMapSpec.isDrawLegend() != mOriginalMapSpec.isDrawLegend()) { return true; }
		if (mMapSpec.getLatGratSpacing() != mOriginalMapSpec.getLatGratSpacing()) { return true; }
		if (mMapSpec.getLonGratSpacing() != mOriginalMapSpec.getLonGratSpacing()) { return true; }

		for (int i = 0; i < mMapSpec.getNumIsobaths(); i++) {
			if (mMapSpec.getColor(i) != mOriginalMapSpec.getColor(i)) { return true; }
		}
		if (mMapSpec.getBGColor() != mOriginalMapSpec.getBGColor()) { return true; }
		if (mMapSpec.getGratColor() != mOriginalMapSpec.getGratColor()) { return true; }
		if (mMapSpec.getCoastColor() != mOriginalMapSpec.getCoastColor()) { return true; }
		if (mMapSpec.getLabelColor() != mOriginalMapSpec.getLabelColor()) { return true; }
		if (mMapSpec.isColorFill() != mOriginalMapSpec.isColorFill()) { return true; }
		if (mMapSpec.getBathyColorBar() != null && !mMapSpec.getBathyColorBar().equals(mOriginalMapSpec.getBathyColorBar())) { return true; }
		if (mMapSpec.isPlotSectionLabels() != mOriginalMapSpec.isPlotSectionLabels()) { return true; }
		if (mMapSpec.isPlotSectionLabels() != mOriginalMapSpec.isPlotSectionLabels()) { return true; }
		if (mMapSpec.getNumEtopoFiles() != mOriginalMapSpec.getNumEtopoFiles()) { return true; }

		try {
			for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
				if (mMapSpec.getEtopoFile(i) != null && !mMapSpec.getEtopoFile(i).equals(mOriginalMapSpec.getEtopoFile(i))) { return true; }
			}
		}
		catch (Exception ex) {
			return true;
		}
		return false;
	}

	public boolean etopoFileChanged() {
		if (mMapSpec.getNumEtopoFiles() != mOriginalMapSpec.getNumEtopoFiles()) { return true; }

		try {
			for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
				if (mMapSpec.getEtopoFile(i) != null && !mMapSpec.getEtopoFile(i).equals(mOriginalMapSpec.getEtopoFile(i))) { return true; }
			}
		}
		catch (Exception ex) {
			return true;
		}
		return false;
	}

	public void generatePlotName() {
		// name is based upon projection and actual region--not a named region
		String proj = (String)mProjectionsPopup.getSelectedItem();

		double latmax = ((Double)topLatFld.getValue()).doubleValue();
		double latmin = ((Double)bottLatFld.getValue()).doubleValue();
		double lonlft = ((Double)leftLonFld.getValue()).doubleValue();
		double lonrt = ((Double)rightLonFld.getValue()).doubleValue();

		String nameString = new String(mFileViewer.mFileViewerName + " (" + proj + ")");
		mNameField.setText(nameString);
	}
	

	public String getXML() {
		getMapSpec();

		// save preferences as XML
		try {
			// create a documentobject
			Document doc = (Document)Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

			// make joapreferences the root element
			Element root = doc.createElement("joamap");
			root.setAttribute("mapbgcolor", String.valueOf(mMapSpec.getBGColor().getRed()) + ","
					+ String.valueOf(mMapSpec.getBGColor().getGreen()) + "," + String.valueOf(mMapSpec.getBGColor().getBlue()));

			// make the map region element and add it
			Element item = doc.createElement("mapregion");
			item.setAttribute("customregion", String.valueOf(mMapSpec.isCustomMap()));
			item.setAttribute("basin", String.valueOf(mMapSpec.getCurrBasin()));
			item.setAttribute("minlat", String.valueOf(mMapSpec.getLatMin()));
			item.setAttribute("maxlat", String.valueOf(mMapSpec.getLatMax()));
			item.setAttribute("lonleft", String.valueOf(mMapSpec.getLonLft()));
			item.setAttribute("lonright", String.valueOf(mMapSpec.getLonRt()));
			item.setAttribute("centerlat", String.valueOf(mMapSpec.getCenLat()));
			item.setAttribute("centerlon", String.valueOf(mMapSpec.getCenLon()));
			root.appendChild(item);

			// make the graticule element and add it
			item = doc.createElement("graticule");
			item.setAttribute("drawgraticule", String.valueOf(mMapSpec.isDrawGraticule()));
			item.setAttribute("labelgraticules", String.valueOf(plotGratLabels.isSelected()));
			item.setAttribute("latspc", String.valueOf(mMapSpec.getLatGratSpacing()));
			item.setAttribute("lonspc", String.valueOf(mMapSpec.getLonGratSpacing()));
			item.setAttribute("gratcolor", String.valueOf(mMapSpec.getGratColor().getRed()) + ","
					+ String.valueOf(mMapSpec.getGratColor().getGreen()) + ","
					+ String.valueOf(mMapSpec.getGratColor().getBlue()));
			root.appendChild(item);

			// make the projection element and add it
			item = doc.createElement("projection");
			int projCode = mProjectionsPopup.getSelectedIndex() + 1 > 5 ? mProjectionsPopup.getSelectedIndex() + 5
					: mProjectionsPopup.getSelectedIndex() + 1;
			item.setAttribute("projcode", String.valueOf(projCode));
			item.setAttribute("retainprojaspect", String.valueOf(mMapSpec.isRetainProjAspect()));
			item.setAttribute("isglobe", String.valueOf(mMapSpec.isGlobe()));
			root.appendChild(item);

			// make the coastline element and add it
			item = doc.createElement("coastline");
			item.setAttribute("resolution", String.valueOf(mMapSpec.getCoastLineRez()));
			item.setAttribute("coastcolor", String.valueOf(mMapSpec.getCoastColor().getRed()) + ","
					+ String.valueOf(mMapSpec.getCoastColor().getGreen()) + ","
					+ String.valueOf(mMapSpec.getCoastColor().getBlue()));
			if (mMapSpec.getCustCoastPath() != null) {
				item.setAttribute("customcoastpath", mMapSpec.getCustCoastPath());
				item.setAttribute("customcoastdescrip", mMapSpec.getCustCoastDescrip());
			}
			root.appendChild(item);

			// make the sectionline element and add it
			item = doc.createElement("sectionline");
			item.setAttribute("plotsymbols", String.valueOf(mMapSpec.isPlotStnSymbols()));
			item.setAttribute("symbolcode", String.valueOf(mMapSpec.getSymbol()));
			item.setAttribute("symbolsize", String.valueOf(mMapSpec.getSymbolSize()));
			item.setAttribute("linewidth", String.valueOf(mMapSpec.getLineWidth()));
			item.setAttribute("connectstns", String.valueOf(mMapSpec.isConnectStns()));
			item.setAttribute("connectstnsacross", String.valueOf(mMapSpec.isConnectStnsAcrossSections()));
			item.setAttribute("sectionlabels", String.valueOf(mMapSpec.isPlotSectionLabels()));
			item.setAttribute("stationlabels", String.valueOf(mMapSpec.isPlotStnLabels()));
			item.setAttribute("stnlabeloffset", String.valueOf(mMapSpec.getStnLabelOffset()));
			item.setAttribute("stnlabelangle", String.valueOf(mMapSpec.getStnLabelAngle()));
			root.appendChild(item);
			
			item = doc.createElement("overlaycontour");
			item.setAttribute("contourlabelprecision", String.valueOf(mMapSpec.getContourLabelPrec()));
			root.appendChild(item);

			if (mColorFill.isSelected()) {
				// etopo overlays
				item = doc.createElement("etopoovl");
				item.setAttribute("colorbarname", String.valueOf((String)mFillColorbarList.getSelectedValue()));
				for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
					Element etopoItem = doc.createElement("etopofile");
					etopoItem.setAttribute("etopofilename", String.valueOf(mMapSpec.getEtopoFile(i)));
					item.appendChild(etopoItem);
				}
				root.appendChild(item);
			}

			if (mMapSpec.getNumIsobaths() > 0) {
				// etopo overlays
				item = doc.createElement("isobathovl");
				for (int i = 0; i < mMapSpec.getNumIsobaths(); i++) {
					Element isoItem = doc.createElement("isobath");
					isoItem.setAttribute("value", String.valueOf(mMapSpec.getValue(i)));
					isoItem.setAttribute("color", String.valueOf(mMapSpec.getColor(i).getRed()) + ","
							+ String.valueOf(mMapSpec.getColor(i).getGreen()) + "," + String.valueOf(mMapSpec.getColor(i).getBlue()));
					isoItem.setAttribute("path", String.valueOf(mMapSpec.getPath(i)));
					isoItem.setAttribute("description", String.valueOf(mMapSpec.getDescrip(i)));
					item.appendChild(isoItem);
				}
				root.appendChild(item);
			}


			doc.appendChild(root);
			((TXDocument) doc).setVersion("1.0");
			StringWriter sw = new StringWriter();
			((TXDocument) doc).printWithFormat(sw);
			String mapAsXMLStr = sw.toString();
			return mapAsXMLStr;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void saveMapSettings(File file) {
		// save preferences as XML
		getMapSpec();
		try {
			// create a documentobject
			Document doc = (Document)Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

			// make joapreferences the root element
			Element root = doc.createElement("joamap");
			root.setAttribute("mapbgcolor", String.valueOf(mMapSpec.getBGColor().getRed()) + ","
			    + String.valueOf(mMapSpec.getBGColor().getGreen()) + "," + String.valueOf(mMapSpec.getBGColor().getBlue()));

			// make the map region element and add it
			Element item = doc.createElement("mapregion");
			item.setAttribute("customregion", String.valueOf(mMapSpec.isCustomMap()));
			item.setAttribute("basin", String.valueOf(mMapSpec.getCurrBasin()));
			item.setAttribute("minlat", String.valueOf(mMapSpec.getLatMin()));
			item.setAttribute("maxlat", String.valueOf(mMapSpec.getLatMax()));
			item.setAttribute("lonleft", String.valueOf(mMapSpec.getLonLft()));
			item.setAttribute("lonright", String.valueOf(mMapSpec.getLonRt()));
			item.setAttribute("centerlat", String.valueOf(mMapSpec.getCenLat()));
			item.setAttribute("centerlon", String.valueOf(mMapSpec.getCenLon()));
			root.appendChild(item);

			// make the graticule element and add it
			item = doc.createElement("graticule");
			item.setAttribute("drawgraticule", String.valueOf(mMapSpec.isDrawGraticule()));
			item.setAttribute("labelgraticules", String.valueOf(plotGratLabels.isSelected()));
			item.setAttribute("latspc", String.valueOf(mMapSpec.getLatGratSpacing()));
			item.setAttribute("lonspc", String.valueOf(mMapSpec.getLonGratSpacing()));
			item.setAttribute("gratcolor", String.valueOf(mMapSpec.getGratColor().getRed()) + ","
			    + String.valueOf(mMapSpec.getGratColor().getGreen()) + ","
			    + String.valueOf(mMapSpec.getGratColor().getBlue()));
			root.appendChild(item);

			// make the projection element and add it
			item = doc.createElement("projection");
			int projCode = mProjectionsPopup.getSelectedIndex() + 1 > 5 ? mProjectionsPopup.getSelectedIndex() + 5
			    : mProjectionsPopup.getSelectedIndex() + 1;
			item.setAttribute("projcode", String.valueOf(projCode));
			item.setAttribute("retainprojaspect", String.valueOf(mMapSpec.isRetainProjAspect()));
			item.setAttribute("isglobe", String.valueOf(mMapSpec.isGlobe()));
			root.appendChild(item);

			// make the coastline element and add it
			item = doc.createElement("coastline");
			item.setAttribute("resolution", String.valueOf(mMapSpec.getCoastLineRez()));
			item.setAttribute("coastcolor", String.valueOf(mMapSpec.getCoastColor().getRed()) + ","
			    + String.valueOf(mMapSpec.getCoastColor().getGreen()) + ","
			    + String.valueOf(mMapSpec.getCoastColor().getBlue()));
			if (mMapSpec.getCustCoastPath() != null) {
				item.setAttribute("customcoastpath", mMapSpec.getCustCoastPath());
				item.setAttribute("customcoastdescrip", mMapSpec.getCustCoastDescrip());
			}
			root.appendChild(item);

			// make the sectionline element and add it
			item = doc.createElement("sectionline");
			item.setAttribute("plotsymbols", String.valueOf(mMapSpec.isPlotStnSymbols()));
			item.setAttribute("symbolcode", String.valueOf(mMapSpec.getSymbol()));
			item.setAttribute("symbolsize", String.valueOf(mMapSpec.getSymbolSize()));
			item.setAttribute("linewidth", String.valueOf(mMapSpec.getLineWidth()));
			item.setAttribute("connectstns", String.valueOf(mMapSpec.isConnectStns()));
			item.setAttribute("connectstnsacross", String.valueOf(mMapSpec.isConnectStnsAcrossSections()));
			item.setAttribute("sectionlabels", String.valueOf(mMapSpec.isPlotSectionLabels()));
			item.setAttribute("stationlabels", String.valueOf(mMapSpec.isPlotStnLabels()));	
			item.setAttribute("stnlabeloffset", String.valueOf(mMapSpec.getStnLabelOffset()));
			item.setAttribute("stnlabelangle", String.valueOf(mMapSpec.getStnLabelAngle()));
			root.appendChild(item);
			
			item = doc.createElement("overlaycontour");
			item.setAttribute("contourlabelprecision", String.valueOf(mMapSpec.getContourLabelPrec()));
			root.appendChild(item);
			
			if (mColorFill.isSelected()) {
				// etopo overlays
				item = doc.createElement("etopoovl");
				item.setAttribute("colorbarname", String.valueOf((String)mFillColorbarList.getSelectedValue()));
				for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
					Element etopoItem = doc.createElement("etopofile");
					etopoItem.setAttribute("etopofilename", String.valueOf(mMapSpec.getEtopoFile(i)));
					item.appendChild(etopoItem);
				}
				root.appendChild(item);
			}

			if (mMapSpec.getNumIsobaths() > 0) {
				// etopo overlays
				item = doc.createElement("isobathovl");
				for (int i = 0; i < mMapSpec.getNumIsobaths(); i++) {
					Element isoItem = doc.createElement("isobath");
					isoItem.setAttribute("value", String.valueOf(mMapSpec.getValue(i)));
					isoItem.setAttribute("color", String.valueOf(mMapSpec.getColor(i).getRed()) + ","
					    + String.valueOf(mMapSpec.getColor(i).getGreen()) + "," + String.valueOf(mMapSpec.getColor(i).getBlue()));
					isoItem.setAttribute("path", String.valueOf(mMapSpec.getPath(i)));
					isoItem.setAttribute("description", String.valueOf(mMapSpec.getDescrip(i)));
					item.appendChild(isoItem);
				}
				root.appendChild(item);
			}

			doc.appendChild(root);
			((TXDocument)doc).setVersion("1.0");
			FileWriter fr = new FileWriter(file);
			((TXDocument)doc).printWithFormat(fr);
			fr.flush();
			fr.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void save(String suggestedMapName) {
		// get a filename
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("_map.xml")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		Frame fr = new Frame();
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		FileDialog f = new FileDialog(fr, "Save map settings with name ending in \"_map.xml\"", FileDialog.SAVE);
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
				saveMapSettings(nf);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void readSettings() {
		// get a filename
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("_map.xml")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		Frame fr = new Frame();
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		FileDialog f = new FileDialog(fr, "Read map settings from:", FileDialog.LOAD);
		f.setDirectory(directory);
		f.setFilenameFilter(filter);
		f.setVisible(true);
		directory = f.getDirectory();
		String fs = f.getFile();
		f.dispose();
		if (directory != null && fs != null) {
			File nf = new File(directory, fs);
			try {
				mMapSpec = JOAFormulas.parseMapSpec(nf);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	protected void setUIFromMapSpec() {
		boolean lockState = lockRgn.isSelected();
		lockRgn.setSelected(true);
		int proj = mMapSpec.getProjection();
		int projIndex = proj > 5 ? proj - 5 : proj - 1;
		ignoreItemChange = true;
		mProjectionsPopup.setSelectedIndex(projIndex);
		ignoreItemChange = true;
		setAvailableMapRegions();
		presetRegions.setSelectedIndex(mMapSpec.getCurrBasin());

		mIgnoreStateChanged = true;

		topLatFld.setValue(new Double(mMapSpec.getLatMax()));
		mIgnoreStateChanged = true;
		leftLonFld.setValue(new Double(mMapSpec.getLonLft()));
		mIgnoreStateChanged = true;
		rightLonFld.setValue(new Double(mMapSpec.getLonRt()));
		mIgnoreStateChanged = true;
		bottLatFld.setValue(new Double(mMapSpec.getLatMin()));

		mCenterLonFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getCenLon()), 2, false));
		mCenterLatFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getCenLat()), 2, false));

		// isobaths: check for matches with existing isobath collection
		int ctr = 0;
		if (mMapSpec.getNumIsobaths() > 0) {
			// turn off current selection
			for (int i = 0; i < allIsobathValues.length; i++) {
				checks[i].setSelected(false);
				swatches[i].setColor(Color.black);
			}
			checkNone.setSelected(false);

			mMapSpec.setNumIsobaths(0);
			for (int i = 0; i < mMapSpec.getNumIsobaths(); i++) { // from XML file
				for (int j = 0; j < allIsobathValues.length; j++) { // from built in and
					// custom isobaths on disk
					double[] isoVals = mMapSpec.getIsobathValues();
					String[] isoDescrips = mMapSpec.getIsobathDescrips();
					boolean valMatch = (Double.valueOf(allIsobathValues[j]).doubleValue()) == isoVals[i];
					boolean descripMatch = allIsobathDescrips[j].equals(isoDescrips[i]);
					if (valMatch && descripMatch) {
						checks[j].setSelected(true);
						swatches[j].setColor(mMapSpec.getColor(ctr));
						ctr++;
					}
				}
			}
		}
		mMapSpec.setNumIsobaths(ctr);

		mColorFill.setSelected(mMapSpec.isColorFill());
		disableEtopoStuff();
		if (mMapSpec.isColorFill()) {
			enableEtopoStuff();
			mEtopoList.setSelectedIndices(JOAFormulas.getSelIndices(mMapSpec.getEtopoFiles(), mMapSpec.getNumEtopoFiles(),
			    mEtopoList));
			mFillColorbarList.setSelectedValue(mMapSpec.getBathyColorBar().getTitle(), true);
		}

		coarseRez.setSelected(mMapSpec.getCoastLineRez() == JOAConstants.COARSERESOLUTION);
		fineRez.setSelected(mMapSpec.getCoastLineRez() == JOAConstants.FINERESOLUTION);
		noCoast.setSelected(mMapSpec.getCoastLineRez() == JOAConstants.NOCOAST);
		customCoast.setSelected(!(mMapSpec.getCustCoastPath() == null));
		connectStns.setSelected(mMapSpec.isConnectStns());
		connectStnsAcrossSections.setSelected(mMapSpec.isConnectStnsAcrossSections());
		if (mMapSpec.isConnectStns()) {
			enableConnectOption();
		}
		else {
			disableConnectOption();
		}

		ignoreItemChange = true;
		mSymbolPopup.setSelectedIndex(mMapSpec.getSymbol() - 1);
		mSymbolSizeSpinner.setValue(new Integer(mMapSpec.getSymbolSize()));
		mapGraticule.setSelected(mMapSpec.isDrawGraticule());
		plotStnLabels.setSelected(mMapSpec.isPlotStnLabels());
		plotSectionLabels.setSelected(mMapSpec.isPlotSectionLabels());
		mLatSpacing.setText(JOAFormulas.formatDouble(Double.toString(mMapSpec.getLatGratSpacing()), 1, false));
		mLonSpacing.setText(JOAFormulas.formatDouble(Double.toString(mMapSpec.getLonGratSpacing()), 1, false));
		retainAspect.setSelected(mMapSpec.isRetainProjAspect());
		mapBG.setColor(mMapSpec.getBGColor());
		coastline.setColor(mMapSpec.getCoastColor());
		gratColor.setColor(mMapSpec.getGratColor());
		
		mLblOffset.setValue(mMapSpec.getStnLabelOffset());
		mLblAngle.setValue(mMapSpec.getStnLabelAngle());
		mLblOffset.setEnabled(mMapSpec.isPlotStnLabels());
		mLblAngle.setEnabled(mMapSpec.isPlotStnLabels());

		mLblPrec.setValue(mMapSpec.getContourLabelPrec());

		// custom coast
		if (mMapSpec.getCustCoastPath() != null) {
			customCoastsList.setSelectedItem(mMapSpec.getCustCoastDescrip());
		}
		lockRgn.setSelected(lockState);

		if (mMapSpec.getMapName() != null) {
			mNameField.setText(mMapSpec.getMapName());
		}
		else {
			generatePlotName();
		}
	}

	public void maintainButtons() {
		// maintain the buttons of the subpanel UIs
		isoSymbolsPanel.maintainUI();
		stnCalcPanel.maintainUI();
		defaultPanel.maintainUI();
		metadataPanel.maintainUI();
		isoContourPanel.maintainUI();
		stnCalcContourPanel.maintainUI();

		switch (mColorByFlag) {
			case 0:
				if (defaultPanel.isUIReady()) {
					mOKBtn.setEnabled(true);
				}
				else {
					mOKBtn.setEnabled(false);
				}
				break;
			case 1:
				if (isoSymbolsPanel.isUIReady()) {
					mOKBtn.setEnabled(true);
				}
				else {
					mOKBtn.setEnabled(false);
				}
				break;
			case 2:
				if (stnCalcPanel.isUIReady()) {
					mOKBtn.setEnabled(true);
				}
				else {
					mOKBtn.setEnabled(false);
				}
				break;
			case 3:
				if (metadataPanel.isUIReady()) {
					mOKBtn.setEnabled(true);
				}
				else {
					mOKBtn.setEnabled(false);
				}
				break;
		}

		switch (mContourByFlag) {
			case 1:
				if (isoContourPanel.isUIReady()) {
					mOKBtn.setEnabled(true);
				}
				else {
					mOKBtn.setEnabled(false);
				}
				break;
			case 2:
				if (stnCalcContourPanel.isUIReady()) {
					mOKBtn.setEnabled(true);
				}
				else {
					mOKBtn.setEnabled(false);
				}
				break;
		}

		switch (mColorByFlag) {
			case 0:
				if (defaultPanel.isUIReady() && mOriginalMapSpec != null) {
					mApplyButton.setEnabled(true);
				}
				else {
					mApplyButton.setEnabled(false);
				}
				break;
			case 1:
				if (isoSymbolsPanel.isUIReady() && mOriginalMapSpec != null) {
					mApplyButton.setEnabled(true);
				}
				else {
					mApplyButton.setEnabled(false);
				}
				break;
			case 2:
				if (stnCalcPanel.isUIReady() && mOriginalMapSpec != null) {
					mApplyButton.setEnabled(true);
				}
				else {
					mApplyButton.setEnabled(false);
				}
				break;
			case 3:
				if (metadataPanel.isUIReady() && mOriginalMapSpec != null) {
					mApplyButton.setEnabled(true);
				}
				else {
					mApplyButton.setEnabled(false);
				}
				break;
		}

		switch (mContourByFlag) {
			case 1:
				if (isoContourPanel.isUIReady() && mOriginalMapSpec != null) {
					mApplyButton.setEnabled(true);
				}
				else {
					mApplyButton.setEnabled(false);
				}
				break;
			case 2:
				if (stnCalcContourPanel.isUIReady() && mOriginalMapSpec != null) {
					mApplyButton.setEnabled(true);
				}
				else {
					mApplyButton.setEnabled(false);
				}
				break;
		}
	}

	private class DefaultSymbols extends JPanel implements UIPanelMaintainer {
		FileViewer mFileViewer = null;

		public DefaultSymbols(FileViewer fv) {
			mFileViewer = fv;
			this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 3));
			JTextArea ta = new JTextArea(2, 100);
			ta.setWrapStyleWord(true);
			ta.setLineWrap(true);
			ta.setBackground(this.getBackground());
			ta.setText(b.getString("kDefaultSymbols"));
			int size = 12;
			if (JOAConstants.ISSUNOS) {
				size = 14;
			}
			ta.setFont(new Font("Courier", Font.PLAIN, size));
			ta.setEditable(false);
			this.add(ta);
		}

		public void maintainUI() {
		}

		public boolean isUIReady() {
			return true;
		}
	}

	private class NoContours extends JPanel implements UIPanelMaintainer {
		public NoContours() {
			this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 3));
			JTextArea ta = new JTextArea(2, 100);
			ta.setWrapStyleWord(true);
			ta.setLineWrap(true);
			ta.setBackground(this.getBackground());
			ta.setText(b.getString("kNoContours"));
			int size = 12;
			if (JOAConstants.ISSUNOS) {
				size = 14;
			}
			ta.setFont(new Font("Courier", Font.PLAIN, size));
			ta.setEditable(false);
			this.add(ta);
		}

		public void maintainUI() {
		}

		public boolean isUIReady() {
			return true;
		}
	}

	private class IsoSurfaceSpecificationSymbols extends JPanel implements ActionListener, ListSelectionListener,
	    ItemListener, ParameterAddedListener, UIPanelMaintainer, DialogClient {
		private LargeIconButton mLinear = null;
		private LargeIconButton mPowerUp = null;
		private LargeIconButton mPowerDown = null;
		private LargeIconButton mLogistic = null;
		private ColorBarPanel mColorBarPanel = null;
		private JOAJList mColorbarList = null;
		private JPanel isoStuffCont;
		private JOAJList mParamList = null;
		private int mSelParam = -1;
		private JOAJLabel ccLbl1 = null;
		private JOAJLabel ccLbl2 = null;
		private JOAJLabel ccLbl3 = null;
		private JOAJLabel ccLbl4 = null;
		private JOAJComboBox mColorCombo = null;
		private JOAJList mSurfaceList = null;
		private JPanel autoscalePanel;
		private JOAJRadioButton minVal = null;
		private JOAJRadioButton maxVal = null;
		private JOAJRadioButton customVal = null;
		private Spinner mSurfaceSpinner = null;
		private JOAJCheckBox mLocalInterpolation;
		private JOAJTextField mNumBottlesField = null;
		private int mAutoscaleColorScheme = 1;
		private JOAJCheckBox mBottomUp = null;
		private FileViewer mFileViewer = null;
		private boolean mUIState = false;
		private boolean mAutoScaleCreated = false;
		private JOAJLabel iLabel3;
		private boolean mIgnore = false;
		private NewColorBar mColorBar = null;
		protected Spinner mRefLevels = null;
		protected double mRefLevel = -99;
		private JCheckBox mUseRefLevel = new JOAJCheckBox(b.getString("kReferenceLevel2"), false);
		protected JOAJLabel mParamLabel = null;
		protected int mSelLevel = -1;
		Vector<String> params = new Vector<String>();
		Vector<String> mdcbs = new Vector<String>();

		public double getReferenceLevel() {
			return mRefLevels.getValue();
		}

		public IsoSurfaceSpecificationSymbols(FileViewer fv, String lbl) {
			mFileViewer = fv;
			// this panel contains all the JLists and preview panel
			this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 3));
			isoStuffCont = new JPanel();
			isoStuffCont.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 5));

			// this panel contains the parameter and surface jlists
			JPanel isoStuffCont1 = new JPanel();
			isoStuffCont1.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 5));

			// this panel contains the color bar selection and controls for creating a
			// autoscale color
			JPanel isoStuffCont2 = new JPanel();
			isoStuffCont2.setLayout(new GridLayout(2, 1, 5, 5));

			// build the parameter list
			JPanel stnl1 = new JPanel();
			stnl1.setLayout(new BorderLayout(5, 5));
			ccLbl1 = new JOAJLabel(b.getString("kParameter:"));
			stnl1.add(ccLbl1, "North");
			buildParamList();

			mParamList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mParamList.setPrototypeCellValue("Temperature  ");
			mParamList.setVisibleRowCount(6);
			JScrollPane listScroller22 = new JScrollPane(mParamList);
			mParamList.addListSelectionListener(this);
			stnl1.add(listScroller22, "Center");
			isoStuffCont1.add(stnl1);

			// build the surface param list
			JPanel stnl3 = new JPanel();
			stnl3.setLayout(new BorderLayout(5, 5));
			mSurfaceList = new JOAJList(spl);

			mSurfaceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mSurfaceList.setPrototypeCellValue("Temperature                          ");
			mSurfaceList.setVisibleRowCount(6);
			JScrollPane listScroller44 = new JScrollPane(mSurfaceList);
			mSurfaceList.addListSelectionListener(this);
			ccLbl3 = new JOAJLabel(b.getString("kSurface"));
			stnl3.add(ccLbl3, "North");
			stnl3.add(listScroller44, "Center");

			mBottomUp = new JOAJCheckBox(b.getString("kBottomUpSearch"), mMapSpec.isStnColorByIsoBottomUpSearch());

			stnl3.add("South", mBottomUp);
			isoStuffCont1.add(stnl3);

			// build the color mapping list
			JPanel stnl2 = new JPanel();
			stnl2.setLayout(new BorderLayout(5, 5));
			buildCBList();

			mColorbarList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mColorbarList.setPrototypeCellValue("Temperature                          ");
			mColorbarList.setVisibleRowCount(6);
			JScrollPane listScroller33 = new JScrollPane(mColorbarList);
			mColorbarList.addListSelectionListener(this);
			ccLbl2 = new JOAJLabel(b.getString("kColors:"));
			stnl2.add(ccLbl2, "North");
			stnl2.add(listScroller33, "Center");

			// panel for autoscaling goes here
			autoscalePanel = new JPanel();
			autoscalePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 10));
			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kCreateAutoscaleColorbar"));
			if (JOAConstants.ISMAC) {
				// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
			}
			autoscalePanel.setBorder(tb);

			JPanel autoscaleBtnsPanel = new JPanel();
			autoscaleBtnsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			// line6.add(new JOAJLabel(b.getString("kBasedOn")));
			JPanel shapePanel = new JPanel();
			shapePanel.setLayout(new GridLayout(1, 4, 5, 0));
			mLinear = new LargeIconButton(new ImageIcon(getClass().getResource("images/linear.gif")));
			shapePanel.add(mLinear);
			mPowerUp = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerup.gif")));
			shapePanel.add(mPowerUp);
			mPowerDown = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerdown.gif")));
			shapePanel.add(mPowerDown);
			mLogistic = new LargeIconButton(new ImageIcon(getClass().getResource("images/logistic.gif")));
			shapePanel.add(mLogistic);
			mLinear.addActionListener(this);
			mPowerUp.addActionListener(this);
			mPowerDown.addActionListener(this);
			mLogistic.addActionListener(this);
			mLinear.setActionCommand("linear");
			mPowerUp.setActionCommand("powerUp");
			mPowerDown.setActionCommand("powerDown");
			mLogistic.setActionCommand("logistic");
			autoscaleBtnsPanel.add(shapePanel);
			autoscalePanel.add(autoscaleBtnsPanel);

			mLinear.setToolTipText(b.getString("kLinearTip"));
			mPowerUp.setToolTipText(b.getString("kIncreasingExpTip"));
			mPowerDown.setToolTipText(b.getString("kDecreasingExpTip"));
			mLogistic.setToolTipText(b.getString("kReverseSTip"));

			JPanel autoscaleComboPanel = new JPanel();
			autoscaleComboPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			ccLbl4 = new JOAJLabel(b.getString("kUsing"));
			autoscaleComboPanel.add(ccLbl4);
			Vector<String> autoScaleChoices = new Vector<String>();
			autoScaleChoices.addElement(new String("Blue-White-Red-32"));
			autoScaleChoices.addElement(new String("Blue-White-Red-16"));
			autoScaleChoices.addElement(new String("Red-White-Blue-32"));
			autoScaleChoices.addElement(new String("Red-White-Blue-16"));
			autoScaleChoices.addElement(new String("Rainbow-32"));
			autoScaleChoices.addElement(new String("Rainbow-16"));
			autoScaleChoices.addElement(new String("Rainbow(inv)-32"));
			autoScaleChoices.addElement(new String("Rainbow(inv)-16"));
			mColorCombo = new JOAJComboBox(autoScaleChoices);
			mColorCombo.setSelectedIndex(JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME);
			mAutoscaleColorScheme = JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME + 1;
			mColorCombo.addItemListener(this);
			autoscaleComboPanel.add(mColorCombo);
			autoscalePanel.add(autoscaleComboPanel);
			isoStuffCont2.add(stnl2);
			isoStuffCont2.add(autoscalePanel);

			// blank color bar panel
			mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, null);
			mColorBarPanel.setLinked(false);
			mColorBarPanel.setEnhanceable(false);
			mColorBarPanel.setBroadcastMode(false);
			isoStuffCont.add(mColorBarPanel);

			// build the top UI
			isoStuffCont.add(isoStuffCont1);
			isoStuffCont.add(isoStuffCont2);
			this.add(isoStuffCont);

			/*
			 * // first get the range of the surface String newSurfaceName =
			 * (String)mSurfaceList.getSelectedValue(); // read the surface from disk
			 * NewInterpolationSurface mSurface = null; try { mSurface =
			 * JOAFormulas.getSurface(newSurfaceName); } catch (Exception ex) {
			 * mSurface = null; }
			 */

			// here are the containers for the surface value options and the missing
			// value Options
			JPanel optPanel = new JPanel();
			optPanel.setLayout(new GridLayout(1, 3, 5, 0));
			JPanel stnl4 = new JPanel();
			stnl4.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 2));
			JPanel spinnerCont = new JPanel();
			spinnerCont.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 1));

			minVal = new JOAJRadioButton(b.getString("kMinSurf"), mMapSpec.isStnColorByIsoMinIsoSurfaceValue());
			maxVal = new JOAJRadioButton(b.getString("kMaxSurf"), mMapSpec.isStnColorByIsoMaxIsoSurfaceValue());
			customVal = new JOAJRadioButton(lbl, !mMapSpec.isStnColorByIsoMinIsoSurfaceValue()
			    && !mMapSpec.isStnColorByIsoMaxIsoSurfaceValue());
			ButtonGroup vals = new ButtonGroup();
			vals.add(minVal);
			vals.add(maxVal);
			vals.add(customVal);
			spinnerCont.add(customVal);
			minVal.addItemListener(this);
			maxVal.addItemListener(this);
			customVal.addItemListener(this);

			// get an array of values in the surface and construct a spinner
			mSurfaceSpinner = new Spinner(Spinner.SPINNER_HORIZONTAL, false);
			mSurfaceSpinner.setPrecision(2);

			// stnl4.add(new JOAJLabel(b.getString("kValue")));

			// add the referece level stuff

			// reference level
			JPanel refLevel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
			refLevel.add(mUseRefLevel);
			mParamLabel = new JOAJLabel("----");
			refLevel.add(mParamLabel);

			mRefLevels = new Spinner(Spinner.SPINNER_HORIZONTAL, false);
			mRefLevels.setPrecision(2);
			refLevel.add(mRefLevels);

			spinnerCont.add(mSurfaceSpinner);
			stnl4.add(spinnerCont);
			stnl4.add(refLevel);
			stnl4.add(minVal);
			stnl4.add(maxVal);

			mUseRefLevel.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						maintainRefLevelStuff(true);
					}
					else {
						maintainRefLevelStuff(false);
					}
				}
			});

			mUseRefLevel.setSelected(mMapSpec.isStnColorByIsoIsReferenced());

			customVal.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						mUseRefLevel.setEnabled(true);
						maintainRefLevelStuff(mUseRefLevel.isSelected());
					}
				}
			});

			minVal.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						mUseRefLevel.setEnabled(false);
						mUseRefLevel.setSelected(false);
						maintainRefLevelStuff(false);
					}
				}
			});

			maxVal.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						mUseRefLevel.setEnabled(false);
						mUseRefLevel.setSelected(false);
						maintainRefLevelStuff(false);
					}
				}
			});

			tb = BorderFactory.createTitledBorder(b.getString("kSurfaceValue"));
			stnl4.setBorder(tb);

			JPanel missingValueOptions = new JPanel();
			missingValueOptions.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
			tb = BorderFactory.createTitledBorder(b.getString("kMissingValueOptions"));
			missingValueOptions.setBorder(tb);

			mLocalInterpolation = new JOAJCheckBox(b.getString("kLocalInterpolation2"), mMapSpec
			    .isStnColorByIsoLocalInterpolation());
			missingValueOptions.add(mLocalInterpolation);

			iLabel3 = new JOAJLabel("  " + b.getString("kObsAboveBelowStdLevel2"));

			JPanel missingValueConstraints = new JPanel();
			missingValueConstraints.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
			mNumBottlesField = new JOAJTextField(4);

			mNumBottlesField.setText(String.valueOf(mMapSpec.getStnColorByIsoMaxInterpDistance()));
			mNumBottlesField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			missingValueConstraints.add(mNumBottlesField);
			missingValueConstraints.add(iLabel3);
			missingValueOptions.add(missingValueConstraints);

			// add the options
			optPanel.add(stnl4);
			optPanel.add(missingValueOptions);
			this.add(optPanel);
		}

		public boolean isAutoScaledColor() {
			return mAutoScaleCreated;
		}

		public void parameterAdded(ParameterAddedEvent evt) {
			// redo the parameter list
			buildParamList();
		}

		public void disableValueSpinner() {
			mSurfaceSpinner.setEnabled(false);
		}

		public void enableValueSpinner() {
			mSurfaceSpinner.setEnabled(true);
		}

		public String getParamName() {
			return (String)mParamList.getSelectedValue();
		}

		public String getColorBarName() {
			return (String)mColorbarList.getSelectedValue();
		}

		public int getColorBarIndex() {
			return mColorbarList.getSelectedIndex();
		}

		public int getContourVarCode() {
			return mParamList.getSelectedIndex();
		}

		public String getSurfaceName() {
			return (String)mSurfaceList.getSelectedValue();
		}

		public int getSurfaceIndex() {
			return mSurfaceList.getSelectedIndex();
		}

		public void setSelectedSurface(int i) {
			mSurfaceList.setSelectedIndex(i);
			mSurfaceList.ensureIndexIsVisible(i);
		}

		public Spinner getSurfaceSpinner() {
			return mSurfaceSpinner;
		}

		public boolean isMinSurfVal() {
			return minVal.isSelected();
		}

		public boolean isMaxSurfVal() {
			return maxVal.isSelected();
		}

		public boolean isCustomSurfVal() {
			return customVal.isSelected();
		}

		public void setMinSurfVal(boolean b) {
			minVal.setSelected(b);
		}

		public void setMaxSurfVal(boolean b) {
			maxVal.setSelected(b);
		}

		public void setCustomSurfVal(boolean b) {
			customVal.setSelected(b);
		}

		public void setReferenceLevel(double d) {
			mRefLevel = d;
			mRefLevels.setValue(d);
		}

		public boolean isBottomUpSearch() {
			return mBottomUp.isSelected();
		}

		public void setBottomUpSearch(boolean b) {
			mBottomUp.setSelected(b);
		}

		public boolean isLocalInterpolation() {
			return mLocalInterpolation.isSelected();
		}

		public void setIsLocalInterpolation(boolean b) {
			mLocalInterpolation.setSelected(b);
		}

		public int getMaxInterpDistance() {
			String fldText = mNumBottlesField.getText();
			int retVal = 2;
			try {
				retVal = Integer.valueOf(fldText).intValue();
			}
			catch (Exception ex) {
			}
			return retVal;
		}

		public void setMaxInterpDistance(int i) {
			mNumBottlesField.setText(String.valueOf(i));
		}

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("linear")) {
				createColorBar(JOAConstants.LINEAR);
				updateColorBar();
				mLinear.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("powerUp")) {
				createColorBar(JOAConstants.EXPONENTIALUP);
				updateColorBar();
				mPowerUp.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("powerDown")) {
				createColorBar(JOAConstants.EXPONENTIALDOWN);
				updateColorBar();
				mPowerDown.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("logistic")) {
				createColorBar(JOAConstants.LOGISTIC);
				updateColorBar();
				mLogistic.setSelected(false);
				mAutoScaleCreated = true;
			}
		}

		public void itemStateChanged(ItemEvent evt) {
			if (evt.getSource() instanceof JOAJComboBox) {
				JOAJComboBox cb = (JOAJComboBox)evt.getSource();
				if (cb == mColorCombo) {
					mAutoscaleColorScheme = cb.getSelectedIndex() + 1;
				}
			}
			else if (evt.getSource() instanceof JOAJRadioButton) {
				JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
				if (evt.getStateChange() == ItemEvent.SELECTED && rb == maxVal) {
					disableValueSpinner();
				}

				if (evt.getStateChange() == ItemEvent.SELECTED && rb == minVal) {
					disableValueSpinner();
				}

				if (evt.getStateChange() == ItemEvent.SELECTED && rb == customVal) {
					enableValueSpinner();
				}
			}
		}

		public void setSelectedIsoCB(int i) {
			mIgnore = true;
			mColorbarList.setSelectedIndex(i);
			mColorbarList.ensureIndexIsVisible(i);
		}

		public void setSelectedParam(int i) {
			mIgnore = true;
			mParamList.setSelectedIndex(i);
			mParamList.ensureIndexIsVisible(i);
			mSelParam = i;
		}

		public void setSelectedColorBar(Object o, boolean b) {
			mIgnore = true;
			mColorbarList.setSelectedValue(o, b);
		}

		public void setSelectedSurface(Object o, boolean b) {
			mIgnore = true;
			mSurfaceList.setSelectedValue(o, b);
		}

		public UVCoordinate computeReferencedRange() {
			double minRefRange = 10e35;
			double maxRefRange = -10e25;

			String newSurfaceName = (String)mSurfaceList.getSelectedValue();
			// read the surface from disk
			NewInterpolationSurface mCurrSurface = null;
			try {
				mCurrSurface = JOAFormulas.getSurface(newSurfaceName);
			}
			catch (Exception ex) {
				mCurrSurface = null;
			}

			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					int iPos = sech.getVarPos(mFileViewer.mAllProperties[mParamList.getSelectedIndex()].getVarLabel(), false);
					int sPos = sech.getVarPos(mCurrSurface.getParam(), false);

					if (iPos < 0 || sPos < 0) {
						continue;
					}

					// loop through the stations
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						if (sh.mUseStn) {
							// create arrays
							double[] sVals = new double[sh.mNumBottles];
							double[] mVals = new double[sh.mNumBottles];

							for (int b = 0; b < sh.mNumBottles; b++) {
								Bottle bh = (Bottle)sh.mBottles.elementAt(b);
								sVals[b] = bh.mDValues[sPos];
								mVals[b] = bh.mDValues[iPos];
							}

							// dereference
							JOAFormulas.dereferenceStation(sh.mNumBottles, mRefLevel, sVals, mVals);

							// compute max min
							for (int b = 0; b < sh.mNumBottles; b++) {
								if (mVals[b] != JOAConstants.MISSINGVALUE && mVals[b] > maxRefRange) {
									maxRefRange = mVals[b];
								}
								if (mVals[b] != JOAConstants.MISSINGVALUE && mVals[b] < minRefRange) {
									minRefRange = mVals[b];
								}
							}
						}
					}
				}
			}
			return new UVCoordinate(minRefRange, maxRefRange);
		}

		public void createColorBar(int curveShape) {
			UVCoordinate refLevelRange = null;
			double refLevel = 0.0;

			// get base and end levels from the selected parameter
			double baseLevel = mFileViewer.mAllProperties[mSelParam].getPlotMin();
			double endLevel = mFileViewer.mAllProperties[mSelParam].getPlotMax();
			boolean isReferenced = mUseRefLevel.isSelected() && mUseRefLevel.isEnabled();

			// have to compute reference level
			if (isReferenced) {
				mRefLevel = mRefLevels.getValue();
				refLevelRange = computeReferencedRange();
				baseLevel = refLevelRange.getU();
				endLevel = refLevelRange.getV();
			}

			// get base and end levels from the selected parameter
			double numLevels = 0;
			if (mAutoscaleColorScheme == 1 || mAutoscaleColorScheme == 3 || mAutoscaleColorScheme == 5
			    || mAutoscaleColorScheme == 7) {
				numLevels = 32;
			}
			else {
				numLevels = 16;
			}

			// compute new color bar values
			mColorBarValues = null;
			mColorBarValues = new double[(int)numLevels];
			mColorBarColors = new Color[(int)numLevels];
			if (curveShape == JOAConstants.LINEAR) {
				double increment = (endLevel - baseLevel) / (numLevels - 1);
				for (int i = 0; i < (int)numLevels; i++) {
					mColorBarValues[i] = baseLevel + (i * increment);
				}
			}
			else if (curveShape == JOAConstants.EXPONENTIALUP || curveShape == JOAConstants.EXPONENTIALDOWN) {
				double shape = JOAFormulas.getShape(baseLevel, endLevel);
				double scaledMax = Math.abs(endLevel - baseLevel);
				double lnScaledMin = Math.log(shape);
				double lnScaledMax = Math.log(scaledMax + shape);
				double increment = (lnScaledMax - lnScaledMin) / (numLevels - 1);

				for (int i = 0; i < (int)numLevels; i++) {
					if (curveShape == JOAConstants.EXPONENTIALUP) {
						// lower
						if (baseLevel < endLevel) {
							mColorBarValues[i] = baseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
						}
						else {
							mColorBarValues[i] = baseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
						}
					}
					else if (curveShape == JOAConstants.EXPONENTIALDOWN) {
						// upper
						if (baseLevel < endLevel) {
							mColorBarValues[(int)numLevels - i - 1] = endLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
						}
						else {
							mColorBarValues[(int)numLevels - i - 1] = endLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
						}
					}
				}
			}
			else if (curveShape == JOAConstants.LOGISTIC) {
				// logistic is a pieced together from upper and lower asymptote
				int mid = 0;
				int nl = (int)numLevels;
				if (nl % 2 > 0) {
					mid = (nl / 2) + 1;
				}
				else {
					mid = nl / 2;
				}

				// upper asymptote from base level to midpoint
				double newEndLevel = (baseLevel + endLevel) / 2;
				double shape = JOAFormulas.getShape(baseLevel, newEndLevel);
				double scaledMax = Math.abs(baseLevel - newEndLevel);
				double lnScaledMin = Math.log(shape);
				double lnScaledMax = Math.log(scaledMax + shape);
				double increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

				// lower
				for (int i = 0; i < mid; i++) {
					if (baseLevel < newEndLevel) {
						mColorBarValues[mid - i - 1] = newEndLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
					else {
						mColorBarValues[mid - i - 1] = newEndLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
				}

				// lower asymptote from midpoint to endlevel
				double newBaseLevel = newEndLevel;
				shape = JOAFormulas.getShape(newBaseLevel, endLevel);
				scaledMax = Math.abs(newBaseLevel - endLevel);
				lnScaledMin = Math.log(shape);
				lnScaledMax = Math.log(scaledMax + shape);
				increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

				// upper
				int endl = 0;
				if (nl % 2 > 0) {
					endl = mid - 1;
				}
				else {
					endl = mid;
				}
				for (int i = 0; i < endl; i++) {
					if (newBaseLevel < endLevel) {
						mColorBarValues[i + mid] = newBaseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
					else {
						mColorBarValues[i + mid] = newBaseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
				}
			}

			// assign colors to color bar
			Color startColor = Color.blue;
			Color midColor = Color.white;
			Color endColor = Color.red;
			if (mAutoscaleColorScheme <= 4) {
				// blend colors
				// get current colors
				if (mAutoscaleColorScheme <= 2) {
					startColor = Color.blue;
					midColor = Color.white;
					endColor = Color.red;
				}
				else {
					startColor = Color.red;
					midColor = Color.white;
					endColor = Color.blue;
				}

				int nl = (int)numLevels;
				int mid = 0;
				double deltaRed = 0;
				double deltaGreen = 0;
				double deltaBlue = 0;
				if (nl % 2 > 0) {
					// odd number of entries--middle color is middle color swatch
					mid = (nl / 2) + 1;
					mColorBarColors[mid - 1] = midColor;

					// blend from start to mid
					deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)mid;
					deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)mid;
					deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)mid;

					int c = 1;
					for (int i = 0; i < mid - 1; i++) {
						double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}

					// blend from mid to end
					deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)mid;
					deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)mid;
					deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)mid;

					c = 1;
					for (int i = mid; i < (int)numLevels; i++) {
						double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}
				}
				else {
					// even number of entries--middle color is in between middle values
					mid = nl / 2;

					// blend from start to mid
					deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)(mid + 1);
					deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)(mid + 1);
					deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)(mid + 1);

					int c = 1;
					for (int i = 0; i < mid; i++) {
						double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}

					// blend from mid to end
					deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)(mid + 1);
					deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)(mid + 1);
					deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)(mid + 1);

					c = 1;
					for (int i = mid; i < (int)numLevels; i++) {
						double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}
				}
			}
			else {
				// create a rainbow
				float hue = 0;
				float sat = 1;
				float light = 1;
				float startHue = 0;
				if (mAutoscaleColorScheme < 7) {
					startHue = 0;
				}
				else {
					startHue = 270;
				}
				float hueAngleDelta = (float)270 / (float)numLevels;
				for (int i = 0; i < (int)numLevels; i++) {
					if (mAutoscaleColorScheme < 7) {
						hue = (startHue + ((float)i * hueAngleDelta)) / 360;
					}
					else {
						hue = (startHue - ((float)i * hueAngleDelta)) / 360;
					}
					mColorBarColors[i] = new Color(Color.HSBtoRGB(hue, sat, light));
					// System.out.println("h= " + (hue * 360) + " s= " + (sat * 100) + "
					// light= " + (light*100));
				}
			}

			// create a new NewColorBar
			String paramText = mFileViewer.mAllProperties[mSelParam].getVarLabel();
			String paramUnits = mFileViewer.mAllProperties[mSelParam].getUnits();
			String titleText = new String("Autoscale");
			String descripText = new String("Autoscale");
			if (mColorBar == null) {
				mColorBar = new NewColorBar(mColorBarColors, mColorBarValues, (int)numLevels, paramText, paramUnits, titleText, descripText);
			}
			else {
				// modify existing color bar
				mColorBar.setValues(mColorBarValues);
				mColorBar.setColors(mColorBarColors);
				mColorBar.setBaseLevel(mColorBarValues[0]);
				mColorBar.setEndLevel(mColorBarValues[(int)numLevels - 1]);
				mColorBar.setTitle(titleText);
				mColorBar.setParam(paramText);
				mColorBar.setDescription(descripText);
				mColorBar.setNumLevels((int)numLevels);
			}
		}

		public void setColorBar(NewColorBar cb) {
			mColorBar = cb;
		}

		public NewColorBar getColorBar() {
			return mColorBar;
		}

		public void updateColorBar() {
			// display the new color bar
			if (mColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				isoStuffCont.remove(mColorBarPanel);
				mColorBarPanel = null;
			}

			mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar);

			mColorBarPanel.setLinked(false);
			mColorBarPanel.setEnhanceable(false);
			mColorBarPanel.setBroadcastMode(false);
			isoStuffCont.add(mColorBarPanel);
			isoStuffCont.invalidate();
			this.validate();
			mAutoScaleCreated = true;
		}

		private void buildCBList() {
			mColorbarList = null;

			Vector<String> cbs = JOAFormulas.getColorBarList();

			for (int i = 0; i < cbs.size(); i++) {
				NewColorBar cb = null;

				String newCBARName = (String)cbs.elementAt(i);

				// read the color bar from disk
				try {
					cb = JOAFormulas.getColorBar(newCBARName);
				}
				catch (Exception ex) {
					cb = null;
				}

				if (!cb.isMetadataColorBar()) {
					mdcbs.addElement(new String(newCBARName));
				}
			}
			mColorbarList = new JOAJList(mdcbs);
		}

		private void buildParamList() {
			for (int i = 0; i < mFileViewer.gNumProperties; i++) {
				params.addElement(mFileViewer.mAllProperties[i].getVarLabel());
			}

			if (mParamList == null) {
				mParamList = new JOAJList(params);
			}
			else {
				mParamList.setListData(params);
				mParamList.setSelectedIndex(mMapSpec.getStnColorByIsoVarCode() - 1);
				mParamList.invalidate();
			}
		}

		public void valueChanged(ListSelectionEvent evt) {
			JOAJList cb = (JOAJList)evt.getSource();
			if (mIgnore) {
				mIgnore = false;
				return;
			}

			if (cb == mColorbarList) {
				// get a new NewColorBar
				String newCBARName = (String)cb.getSelectedValue();
				mColorBar = null;

				// read the color bar from disk
				try {
					mColorBar = JOAFormulas.getColorBar(newCBARName);
					mMapSpec.setStnColorColorBar(mColorBar);
				}
				catch (Exception ex) {
					mMapSpec.setStnColorColorBar(null);
					return;
				}

				// display the preview color bar
				if (mColorBarPanel != null) {
					// remove existing color bar component (if there is one)
					isoStuffCont.remove(mColorBarPanel);
					mColorBarPanel = null;
				}

				mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar);

				mColorBarPanel.setLinked(false);
				mColorBarPanel.setEnhanceable(false);
				mColorBarPanel.setBroadcastMode(false);
				isoStuffCont.add(mColorBarPanel);
				isoStuffCont.invalidate();
				this.validate();
				mAutoScaleCreated = false;
			}
			else if (cb == mParamList) {
				mSelParam = cb.getSelectedIndex();
				if (mSelParam < 0) { return; }
				String selParamText = (String)mParamList.getSelectedValue();

				// make sure value of the param is not missing
				int yerrLine = -1;
				double tempYMin = mFileViewer.mAllProperties[mSelParam].getPlotMin();
				double tempYMax = mFileViewer.mAllProperties[mSelParam].getPlotMax();
				Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
				double yInc = newRange.getVal3();
				if (Double.isNaN(yInc)) {
					yerrLine = mSelParam;
				}

				if (yerrLine >= 0) {
					// disable the y param
					JFrame f = new JFrame("Parameter Values Missing Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "All values for " + selParamText + " are missing. " + "\n"
					    + "Select a new parameter");
					mParamList.clearSelection();
					mSelParam = 0;
				}
				
				// select a colorbar in the colorbar list
				String selParamStr = ((String) mParamList.getSelectedValue()).toUpperCase();
				boolean found = false;
				for (int i = 0; i < mdcbs.size(); i++) {
					String param = ((String) mdcbs.elementAt(i)).toUpperCase();
					if (param.startsWith(selParamStr)) {
						// position list
						mColorbarList.setSelectedIndex(i);
						mColorbarList.ensureIndexIsVisible(i);
						found = true;
						mIgnore = true;
						break;
					}
					else if (param.indexOf(selParamStr) >= 0) {
						// position list
						mColorbarList.setSelectedIndex(i);
						mColorbarList.ensureIndexIsVisible(i);
						found = true;
						mIgnore = true;
						break;
					}
				}

				if (!found) {
					mColorbarList.clearSelection();
				}
			}
			else if (cb == mSurfaceList) {
				// get a new Surface
				String newSurfaceName = (String)cb.getSelectedValue();

				// read the surface from disk
				try {
					mSurface = JOAFormulas.getSurface(newSurfaceName);
					mMapSpec.setStnColorByIsoSurface(mSurface);

					// assign a param label
					mParamLabel.setText(mSurface.getParam() + " =");
					mParamLabel.invalidate();

					// make sure surface param exists in the data
					if (!JOAFormulas.paramExists(mFileViewer, mMapSpec.getStnColorByIsoSurface().getParam())) {
						if (JOAFormulas.isCalculatable(mMapSpec.getStnColorByIsoSurface().getParam())) {
							// make a new calculation
							Calculation calc = JOAFormulas.createCalcFromName(mFileViewer, mMapSpec.getStnColorByIsoSurface()
							    .getParam());

							if (calc != null) {
								// do calculation
								mFileViewer.addCalculation(calc);
								mFileViewer.doCalcs();
							}
						}
						else {
							JFrame f = new JFrame("Contour Manager Error");
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(f, "Surface parameter does not exist in this file" + "\n"
							    + "and couldn't be calculated");
							return;
						}
					}

					// set the value of the spinner
					boolean reverseSpinner = false;
					int precision = 2;
					if (mMapSpec.getStnColorByIsoSurface().getParam().equalsIgnoreCase("PRES")
					    || mMapSpec.getStnColorByIsoSurface().getParam().startsWith("SIG")) {
						;
					}
					reverseSpinner = true;
					if (mMapSpec.getStnColorByIsoSurface().getParam().equalsIgnoreCase("PRES")) {
						precision = 0;
					}
					double[] newVals = mMapSpec.getStnColorByIsoSurface().getValues();
					mSurfaceSpinner.setNewValues(newVals, mMapSpec.getStnColorByIsoSurface().getNumLevels(), reverseSpinner);
					mSurfaceSpinner.setValue(newVals[0]);
					mSurfaceSpinner.setPrecision(precision);
					mRefLevels.setNewValues(newVals, mMapSpec.getStnColorByIsoSurface().getNumLevels(), reverseSpinner);
					mRefLevels.setValue(newVals[0]);
					mRefLevels.setPrecision(precision);
				}
				catch (Exception ex) {
					ex.printStackTrace();
					mMapSpec.setStnColorByIsoSurface(null);
				}
			}
		}

		public void maintainRefLevelStuff(boolean b) {
			mRefLevels.setEnabled(b);
			mParamLabel.setEnabled(b);
		}

		public void disableIsoSurfaceStuff() {
			mSurfaceSpinner.setEnabled(false);
			mBottomUp.setEnabled(false);
			minVal.setEnabled(false);
			maxVal.setEnabled(false);
			customVal.setEnabled(false);
			mNumBottlesField.setEnabled(false);
			mLocalInterpolation.setEnabled(false);
			iLabel3.setEnabled(false);
			mRefLevels.setEnabled(false);
			maintainRefLevelStuff(false);
			mUseRefLevel.setEnabled(false);
		}

		public void enableIsoSurfaceStuff() {
			mSurfaceSpinner.setEnabled(true);
			mBottomUp.setEnabled(true);
			minVal.setEnabled(true);
			maxVal.setEnabled(true);
			customVal.setEnabled(true);
			mNumBottlesField.setEnabled(true);
			mLocalInterpolation.setEnabled(true);
			iLabel3.setEnabled(true);
			mUseRefLevel.setEnabled(!(minVal.isSelected() || maxVal.isSelected()));
			maintainRefLevelStuff(mUseRefLevel.isSelected());
		}

		public void disableAutoscaleStuff() {
			mLinear.setEnabled(false);
			mPowerUp.setEnabled(false);
			mPowerDown.setEnabled(false);
			mLogistic.setEnabled(false);
			mColorCombo.setEnabled(false);
			ccLbl4.setEnabled(false);
		}

		public void enableAutoscaleStuff() {
			mLinear.setEnabled(true);
			mPowerUp.setEnabled(true);
			mPowerDown.setEnabled(true);
			mLogistic.setEnabled(true);
			mColorCombo.setEnabled(true);
			ccLbl4.setEnabled(true);
		}

		public void maintainUI() {
			boolean c1 = mParamList.getSelectedIndex() >= 0;
			boolean c2 = mColorbarList.getSelectedIndex() >= 0 || mAutoScaleCreated == true;
			boolean c3 = mSurfaceList.getSelectedIndex() >= 0;

			if (!c1) {
				// no parameter selected
				disableAutoscaleStuff();
			}
			else {
				// can create an autoscale colorbar
				enableAutoscaleStuff();
			}

			if (!c3) {
				// no surface selected
				disableIsoSurfaceStuff();
			}
			else {
				enableIsoSurfaceStuff();
			}

			if (!c1 || !c2 || !c3) {
				mUIState = false;
			}
			else {
				mUIState = true;
			}
		}

		public boolean isUIReady() {
			return mUIState;
		}

		public void dialogDismissed(JDialog d) {
		}

		// Cancel button
		public void dialogCancelled(JDialog d) {
		}

		// something other than the OK button
		public void dialogDismissedTwo(JDialog d) {
			;
		}

		// Apply button, OK w/o dismissing the dialog
		public void dialogApply(JDialog d) {
		}

		public void dialogApplyTwo(Object d) {
			// got a color change from a color swatch
			if (mColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				isoStuffCont.remove(mColorBarPanel);
				mColorBarPanel = null;
				mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar, null);
				mColorBar = mColorBarPanel.getColorBar();
				mColorBarPanel.setLinked(false);
				mColorBarPanel.setEnhanceable(false);
				mColorBarPanel.setBroadcastMode(false);
				isoStuffCont.add(mColorBarPanel);
				isoStuffCont.invalidate();
				this.validate();
			}
		}
	}

	private class IsoSurfaceSpecificationContours extends JPanel implements ActionListener, ListSelectionListener,
	    ItemListener, ParameterAddedListener, UIPanelMaintainer, DialogClient {
		private LargeIconButton mLinear = null;
		private LargeIconButton mPowerUp = null;
		private LargeIconButton mPowerDown = null;
		private LargeIconButton mLogistic = null;
		private ColorBarPanel mColorBarPanel = null;
		private JOAJList mColorbarList = null;
		private JPanel isoStuffCont;
		private JOAJList mParamList = null;
		private int mSelParam = -1;
		private JOAJLabel ccLbl1 = null;
		private JOAJLabel ccLbl2 = null;
		private JOAJLabel ccLbl3 = null;
		private JOAJLabel ccLbl4 = null;
		private JOAJComboBox mColorCombo = null;
		private JOAJList mSurfaceList = null;
		private JPanel autoscalePanel;
		private JOAJRadioButton minVal = null;
		private JOAJRadioButton maxVal = null;
		private JOAJRadioButton customVal = null;
		private Spinner mSurfaceSpinner = null;
		private JOAJCheckBox mLocalInterpolation;
		private JOAJTextField mNumBottlesField = null;
		private int mAutoscaleColorScheme = 1;
		private JOAJCheckBox mBottomUp = null;
		private FileViewer mFileViewer = null;
		private boolean mUIState = false;
		private boolean mAutoScaleCreated = false;
		private JOAJLabel iLabel3;
		private boolean mIgnore = false;
		private NewColorBar mColorBar = null;
		private JOASwatch mOverlayContourColorSwatch;
		private JOAJRadioButton mBlackContours = null;
		private JOAJRadioButton mWhiteContours = null;
		private JOAJRadioButton mCustomSingleColor = null;
		private JOAJCheckBox mFilledIsoContours = null;
		// private JOAJRadioButton mColorsFromColorBar = null;
		private Color mOverlayLineColor = Color.black;
		private JOAJTextField mNumSkipField = null;
		private JSpinner mNumX, mNumY;
		private JOAJTextField cay, nrng;
		private JOAJCheckBox mMaskCoastline = null;
		protected Spinner mRefLevels = null;
		protected double mRefLevel = -99;
		private JCheckBox mUseRefLevel = new JOAJCheckBox(b.getString("kReferenceLevel2"), false);
		protected JOAJLabel mParamLabel = null;
		protected int mSelLevel = -1;
		private int mContourStyle;
		Vector<String> params = new Vector<String>();
		Vector<String> mdcbs = new Vector<String>();

		public Color getOverlayContourColor() {
			// if (mShowAsContours) {
			return mColorBarPanel.getLineColor();
			// }
		}

		public int getContourStyle() {
			return mContourStyle;
		}

		public boolean isFilledIsoContours() {
			return mFilledIsoContours.isSelected();
		}

		public double getReferenceLevel() {
			return mRefLevels.getValue();
		}

		public int getContourSkipInterval() {
			String fldText = mNumSkipField.getText();
			int retVal = 1;
			try {
				retVal = Integer.valueOf(fldText).intValue();
			}
			catch (Exception ex) {
			}
			return retVal;
		}

		public IsoSurfaceSpecificationContours(FileViewer fv, String lbl) {
			mFileViewer = fv;
			// this panel contains all the JLists and preview panel
			this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 3));
			isoStuffCont = new JPanel();
			isoStuffCont.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 5));

			// this panel contains the parameter and surface jlists
			JPanel isoStuffCont1 = new JPanel();
			isoStuffCont1.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 5));

			// this panel contains the color bar selection and controls for creating a
			// autoscale color
			JPanel isoStuffCont2 = new JPanel();
			isoStuffCont2.setLayout(new GridLayout(2, 1, 5, 5));

			// build the parameter list
			JPanel stnl1 = new JPanel();
			stnl1.setLayout(new BorderLayout(5, 5));
			ccLbl1 = new JOAJLabel(b.getString("kParameter:"));
			stnl1.add(ccLbl1, "North");
			buildParamList();

			mParamList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mParamList.setPrototypeCellValue("Temperature  ");
			mParamList.setVisibleRowCount(6);
			JScrollPane listScroller22 = new JScrollPane(mParamList);
			mParamList.addListSelectionListener(this);
			stnl1.add(listScroller22, "Center");
			isoStuffCont1.add(stnl1);

			// build the surface param list
			JPanel stnl3 = new JPanel();
			stnl3.setLayout(new BorderLayout(5, 5));
			mSurfaceList = new JOAJList(spl);

			mSurfaceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mSurfaceList.setPrototypeCellValue("Temperature                          ");
			mSurfaceList.setVisibleRowCount(6);
			JScrollPane listScroller44 = new JScrollPane(mSurfaceList);
			mSurfaceList.addListSelectionListener(this);
			ccLbl3 = new JOAJLabel(b.getString("kSurface"));
			stnl3.add(ccLbl3, "North");
			stnl3.add(listScroller44, "Center");

			mBottomUp = new JOAJCheckBox(b.getString("kBottomUpSearch"), mMapSpec.isIsoContourBottomUpSearch());

			stnl3.add("South", mBottomUp);
			isoStuffCont1.add(stnl3);

			// build the color mapping list
			JPanel stnl2 = new JPanel();
			stnl2.setLayout(new BorderLayout(5, 5));
			buildCBList();

			mColorbarList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mColorbarList.setPrototypeCellValue("Temperature                          ");
			mColorbarList.setVisibleRowCount(6);
			JScrollPane listScroller33 = new JScrollPane(mColorbarList);
			mColorbarList.addListSelectionListener(this);
			ccLbl2 = new JOAJLabel(b.getString("kColors:"));
			stnl2.add(ccLbl2, "North");
			stnl2.add(listScroller33, "Center");

			// panel for autoscaling goes here
			autoscalePanel = new JPanel();
			autoscalePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 10));
			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kCreateAutoscaleColorbar"));
			if (JOAConstants.ISMAC) {
				// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
			}
			autoscalePanel.setBorder(tb);

			JPanel autoscaleBtnsPanel = new JPanel();
			autoscaleBtnsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			// line6.add(new JOAJLabel(b.getString("kBasedOn")));
			JPanel shapePanel = new JPanel();
			shapePanel.setLayout(new GridLayout(1, 4, 5, 0));
			mLinear = new LargeIconButton(new ImageIcon(getClass().getResource("images/linear.gif")));
			shapePanel.add(mLinear);
			mPowerUp = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerup.gif")));
			shapePanel.add(mPowerUp);
			mPowerDown = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerdown.gif")));
			shapePanel.add(mPowerDown);
			mLogistic = new LargeIconButton(new ImageIcon(getClass().getResource("images/logistic.gif")));
			shapePanel.add(mLogistic);
			mLinear.addActionListener(this);
			mPowerUp.addActionListener(this);
			mPowerDown.addActionListener(this);
			mLogistic.addActionListener(this);
			mLinear.setActionCommand("linear");
			mPowerUp.setActionCommand("powerUp");
			mPowerDown.setActionCommand("powerDown");
			mLogistic.setActionCommand("logistic");
			autoscaleBtnsPanel.add(shapePanel);
			autoscalePanel.add(autoscaleBtnsPanel);

			mLinear.setToolTipText(b.getString("kLinearTip"));
			mPowerUp.setToolTipText(b.getString("kIncreasingExpTip"));
			mPowerDown.setToolTipText(b.getString("kDecreasingExpTip"));
			mLogistic.setToolTipText(b.getString("kReverseSTip"));

			JPanel autoscaleComboPanel = new JPanel();
			autoscaleComboPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			ccLbl4 = new JOAJLabel(b.getString("kUsing"));
			autoscaleComboPanel.add(ccLbl4);
			Vector<String> autoScaleChoices = new Vector<String>();
			autoScaleChoices.addElement(new String("Blue-White-Red-32"));
			autoScaleChoices.addElement(new String("Blue-White-Red-16"));
			autoScaleChoices.addElement(new String("Red-White-Blue-32"));
			autoScaleChoices.addElement(new String("Red-White-Blue-16"));
			autoScaleChoices.addElement(new String("Rainbow-32"));
			autoScaleChoices.addElement(new String("Rainbow-16"));
			autoScaleChoices.addElement(new String("Rainbow(inv)-32"));
			autoScaleChoices.addElement(new String("Rainbow(inv)-16"));
			mColorCombo = new JOAJComboBox(autoScaleChoices);
			mColorCombo.setSelectedIndex(JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME);
			mAutoscaleColorScheme = JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME + 1;
			mColorCombo.addItemListener(this);
			autoscaleComboPanel.add(mColorCombo);
			autoscalePanel.add(autoscaleComboPanel);
			isoStuffCont2.add(stnl2);
			isoStuffCont2.add(autoscalePanel);

			// blank color bar panel
			MapSpecification ms = mMapSpec;
			mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mMapSpec.getOverlayContoursColorBar(), mMapSpec
			    .getIsoContourColor());
			mColorBarPanel.setLinked(false);
			mColorBarPanel.setEnhanceable(false);
			mColorBarPanel.setBroadcastMode(false);
			isoStuffCont.add(mColorBarPanel);

			// build the top UI
			isoStuffCont.add(isoStuffCont1);
			isoStuffCont.add(isoStuffCont2);
			this.add(isoStuffCont);

			/*
			 * // first get the range of the surface String newSurfaceName =
			 * (String)mSurfaceList.getSelectedValue(); // read the surface from disk
			 * NewInterpolationSurface mSurface = null; try { mSurface =
			 * JOAFormulas.getSurface(newSurfaceName); } catch (Exception ex) {
			 * mSurface = null; }
			 */

			// here are the containers for the surface value options and the missing
			// value Options
			JPanel optPanel = new JPanel();
			optPanel.setLayout(new GridLayout(1, 3, 5, 0));
			JPanel stnl4 = new JPanel();
			stnl4.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 2));
			JPanel spinnerCont = new JPanel();
			spinnerCont.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 1));

			minVal = new JOAJRadioButton(b.getString("kMinSurf"), mMapSpec.isIsoContourMinSurfaceValue());
			maxVal = new JOAJRadioButton(b.getString("kMaxSurf"), mMapSpec.isIsoContourMaxSurfaceValue());
			customVal = new JOAJRadioButton(lbl, !mMapSpec.isIsoContourMinSurfaceValue()
			    && !mMapSpec.isIsoContourMaxSurfaceValue());
			ButtonGroup vals = new ButtonGroup();
			vals.add(minVal);
			vals.add(maxVal);
			vals.add(customVal);
			spinnerCont.add(customVal);
			minVal.addItemListener(this);
			maxVal.addItemListener(this);
			customVal.addItemListener(this);

			// get an array of values in the surface and construct a spinner
			mSurfaceSpinner = new Spinner(Spinner.SPINNER_HORIZONTAL, false);
			mSurfaceSpinner.setPrecision(2);

			// stnl4.add(new JOAJLabel(b.getString("kValue")));

			// add the referece level stuff

			// reference level
			JPanel refLevel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
			refLevel.add(mUseRefLevel);
			mParamLabel = new JOAJLabel("----");
			refLevel.add(mParamLabel);

			mRefLevels = new Spinner(Spinner.SPINNER_HORIZONTAL, false);
			mRefLevels.setPrecision(2);
			refLevel.add(mRefLevels);

			spinnerCont.add(mSurfaceSpinner);
			stnl4.add(spinnerCont);
			stnl4.add(refLevel);
			stnl4.add(minVal);
			stnl4.add(maxVal);

			mUseRefLevel.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						maintainRefLevelStuff(true);
					}
					else {
						maintainRefLevelStuff(false);
					}
				}
			});

			mUseRefLevel.setSelected(mMapSpec.isIsoContourReferenced());

			customVal.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						mUseRefLevel.setEnabled(true);
						maintainRefLevelStuff(mUseRefLevel.isSelected());
					}
				}
			});

			minVal.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						mUseRefLevel.setEnabled(false);
						mUseRefLevel.setSelected(false);
						maintainRefLevelStuff(false);
					}
				}
			});

			maxVal.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						mUseRefLevel.setEnabled(false);
						mUseRefLevel.setSelected(false);
						maintainRefLevelStuff(false);
					}
				}
			});

			tb = BorderFactory.createTitledBorder(b.getString("kSurfaceValue"));
			stnl4.setBorder(tb);

			JPanel missingValueOptions = new JPanel();
			missingValueOptions.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
			tb = BorderFactory.createTitledBorder(b.getString("kMissingValueOptions"));
			missingValueOptions.setBorder(tb);

			mLocalInterpolation = new JOAJCheckBox(b.getString("kLocalInterpolation2"), mMapSpec
			    .isIsoContourLocalInterpolation());
			missingValueOptions.add(mLocalInterpolation);

			iLabel3 = new JOAJLabel("  " + b.getString("kObsAboveBelowStdLevel2"));

			JPanel missingValueConstraints = new JPanel();
			missingValueConstraints.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
			mNumBottlesField = new JOAJTextField(4);

			mNumBottlesField.setText(String.valueOf(mMapSpec.getIsoContourMaxInterpDistance()));
			mNumBottlesField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			missingValueConstraints.add(mNumBottlesField);
			missingValueConstraints.add(iLabel3);
			missingValueOptions.add(missingValueConstraints);

			// add the options
			optPanel.add(stnl4);
			optPanel.add(missingValueOptions);
			this.add(optPanel);

			Color contColor = Color.black;
			if (mMapSpec.getIsoContourStyle() == MapSpecification.CUSTOM_CONTOURS) {
				contColor = (Color)mMapSpec.getIsoContourColor();
			}

			// if (mMapSpec.getIsoContourStyle() == MapSpecification.COLOR_CONTOURS)
			// {
			// mOverlayLineColor = (Color) mMapSpec.getIsoContourColor();
			// }

			// Style Panel
			JPanel overlayStyleCont = new JPanel();
			overlayStyleCont.setLayout(new BorderLayout(5, 0));
			tb = BorderFactory.createTitledBorder(b.getString("kStyle"));
			if (JOAConstants.ISMAC) {
				// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
			}
			overlayStyleCont.setBorder(tb);

			JPanel overlayStyles = new JPanel();
			overlayStyles.setLayout(new GridLayout(4, 1, 0, 0));
			mContourStyle = mMapSpec.getStnContourStyle();
			mBlackContours = new JOAJRadioButton(b.getString("kBlackContours"),
			    mMapSpec.getIsoContourStyle() == MapSpecification.BLACK_CONTOURS);
			mWhiteContours = new JOAJRadioButton(b.getString("kWhiteContours"),
			    mMapSpec.getIsoContourStyle() == MapSpecification.WHITE_CONTOURS);
			mCustomSingleColor = new JOAJRadioButton(b.getString("kCustomSingleColor"),
			    mMapSpec.getIsoContourStyle() == MapSpecification.CUSTOM_CONTOURS);
			boolean filled = mMapSpec.isFilledIsoContours();
			mFilledIsoContours = new JOAJCheckBox("Filled contours", filled);
			// mColorsFromColorBar = new
			// JOAJRadioButton(b.getString("kColorsFromColorBar"),
			// mMapSpec.getIsoContourStyle() == MapSpecification.COLOR_CONTOURS);

			mOverlayContourColorSwatch = new JOASwatch(contColor, this);
			mOverlayContourColorSwatch.setEnabled(false);
			JPanel custColorLine = new JPanel();
			custColorLine.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			custColorLine.add(mCustomSingleColor);
			custColorLine.add(mOverlayContourColorSwatch);
			overlayStyles.add(mBlackContours);
			overlayStyles.add(mWhiteContours);
			overlayStyles.add(custColorLine);
			overlayStyles.add(mFilledIsoContours);
			
			// overlayStyles.add(mColorsFromColorBar);
			ButtonGroup sbg = new ButtonGroup();
			sbg.add(mBlackContours);
			sbg.add(mWhiteContours);
			sbg.add(mCustomSingleColor);
			// sbg.add(mColorsFromColorBar);
			overlayStyleCont.add("North", overlayStyles);
			mBlackContours.addItemListener(this);
			mWhiteContours.addItemListener(this);
			mCustomSingleColor.addItemListener(this);
			// mColorsFromColorBar.addItemListener(this);

			JPanel line3a = new JPanel();
			line3a.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
			line3a.add(new JOAJLabel(b.getString("kPlotEvery")));
			mNumSkipField = new JOAJTextField(2);
			mNumSkipField.setText(String.valueOf(mMapSpec.getPlotEveryNthContour()));
			mNumSkipField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			line3a.add(mNumSkipField);
			line3a.add(new JOAJLabel(b.getString("kContours")));
			overlayStyleCont.add("South", line3a);

			JPanel zContourStuff = new JPanel();
			zContourStuff.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));

			zContourStuff.add(overlayStyleCont);

			// zgrid settings
			JPanel zGridStuff = new JPanel();
			zGridStuff.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 10));
			tb = BorderFactory.createTitledBorder(b.getString("kGridSettings"));
			if (JOAConstants.ISMAC) {
				// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
			}
			zGridStuff.setBorder(tb);

			JPanel zLine1 = new JPanel();
			JPanel zLine2 = new JPanel();
			JPanel zLine3 = new JPanel();
			zLine1.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
			zLine2.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
			zLine3.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));

			SpinnerNumberModel modely = new SpinnerNumberModel(mMapSpec.getNY(), 0, 1000, 10);
			SpinnerNumberModel modelx = new SpinnerNumberModel(mMapSpec.getNX(), 0, 1000, 10);

			mNumX = new JSpinner(modelx);
			mNumY = new JSpinner(modely);
			cay = new JOAJTextField(2);
			nrng = new JOAJTextField(2);
			cay.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			nrng.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			cay.setText(String.valueOf(mMapSpec.getCAY()));
			nrng.setText(String.valueOf(mMapSpec.getNRng()));
			mMaskCoastline = new JOAJCheckBox(b.getString("kMaskCoast"), mMapSpec.isMaskCoast());

			JOAJLabel lbl1 = new JOAJLabel(b.getString("kGridDimensions"));
			JOAJLabel lbl2 = new JOAJLabel(b.getString("kLon"));
			JOAJLabel lbl3 = new JOAJLabel(b.getString("kLat"));
			JOAJLabel lbl4 = new JOAJLabel(b.getString("kInterpTension"));
			JOAJLabel lbl5 = new JOAJLabel(b.getString("kNumGridSpaces"));

			zLine1.add(lbl1);
			zLine1.add(lbl2);
			zLine1.add(mNumX);
			zLine1.add(lbl3);
			zLine1.add(mNumY);

			zLine2.add(lbl4);
			zLine2.add(cay);

			zLine3.add(lbl5);
			zLine3.add(nrng);

			zGridStuff.add(zLine1);
			zGridStuff.add(zLine2);
			zGridStuff.add(zLine3);
			zGridStuff.add(mMaskCoastline);

			zContourStuff.add(zGridStuff);
			this.add(zContourStuff);
		}

		public int getNumX() {
			return ((Integer)mNumX.getValue()).intValue();
		}

		public int getNumY() {
			return ((Integer)mNumY.getValue()).intValue();
		}

		public int getNrng() {
			String fldText = nrng.getText();
			int retVal = 10;
			try {
				retVal = Integer.valueOf(fldText).intValue();
			}
			catch (Exception ex) {
			}
			return retVal;
		}

		public double getCay() {
			String fldText = cay.getText();
			double retVal = 5.0;
			try {
				retVal = Double.valueOf(fldText).doubleValue();
			}
			catch (Exception ex) {
			}
			return retVal;
		}

		public boolean isLandMask() {
			return mMaskCoastline.isSelected();
		}

		public boolean isAutoScaledColor() {
			return mAutoScaleCreated;
		}

		public void parameterAdded(ParameterAddedEvent evt) {
			// redo the parameter list
			buildParamList();
		}

		public void disableValueSpinner() {
			mSurfaceSpinner.setEnabled(false);
		}

		public void enableValueSpinner() {
			mSurfaceSpinner.setEnabled(true);
		}

		public String getParamName() {
			return (String)mParamList.getSelectedValue();
		}

		public String getColorBarName() {
			return (String)mColorbarList.getSelectedValue();
		}

		public int getColorBarIndex() {
			return mColorbarList.getSelectedIndex();
		}

		public int getContourVarCode() {
			return mParamList.getSelectedIndex();
		}

		public String getSurfaceName() {
			return (String)mSurfaceList.getSelectedValue();
		}

		public int getSurfaceIndex() {
			return mSurfaceList.getSelectedIndex();
		}

		public void setSelectedSurface(int i) {
			mSurfaceList.setSelectedIndex(i);
			mSurfaceList.ensureIndexIsVisible(i);
		}

		public Spinner getSurfaceSpinner() {
			return mSurfaceSpinner;
		}

		public boolean isMinSurfVal() {
			return minVal.isSelected();
		}

		public boolean isMaxSurfVal() {
			return maxVal.isSelected();
		}

		public boolean isCustomSurfVal() {
			return customVal.isSelected();
		}

		public void setMinSurfVal(boolean b) {
			minVal.setSelected(b);
		}

		public void setMaxSurfVal(boolean b) {
			maxVal.setSelected(b);
		}

		public void setCustomSurfVal(boolean b) {
			customVal.setSelected(b);
		}

		public void setReferenceLevel(double d) {
			mRefLevel = d;
			mRefLevels.setValue(d);
		}

		public boolean isBottomUpSearch() {
			return mBottomUp.isSelected();
		}

		public void setBottomUpSearch(boolean b) {
			mBottomUp.setSelected(b);
		}

		public boolean isLocalInterpolation() {
			return mLocalInterpolation.isSelected();
		}

		public void setIsLocalInterpolation(boolean b) {
			mLocalInterpolation.setSelected(b);
		}

		public int getMaxInterpDistance() {
			String fldText = mNumBottlesField.getText();
			int retVal = 2;
			try {
				retVal = Integer.valueOf(fldText).intValue();
			}
			catch (Exception ex) {
			}
			return retVal;
		}

		public void setMaxInterpDistance(int i) {
			mNumBottlesField.setText(String.valueOf(i));
		}

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("linear")) {
				createColorBar(JOAConstants.LINEAR);
				updateColorBar();
				mLinear.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("powerUp")) {
				createColorBar(JOAConstants.EXPONENTIALUP);
				updateColorBar();
				mPowerUp.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("powerDown")) {
				createColorBar(JOAConstants.EXPONENTIALDOWN);
				updateColorBar();
				mPowerDown.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("logistic")) {
				createColorBar(JOAConstants.LOGISTIC);
				updateColorBar();
				mLogistic.setSelected(false);
				mAutoScaleCreated = true;
			}
		}

		public void itemStateChanged(ItemEvent evt) {
			if (evt.getSource() instanceof JOAJComboBox) {
				JOAJComboBox cb = (JOAJComboBox)evt.getSource();
				if (cb == mColorCombo) {
					mAutoscaleColorScheme = cb.getSelectedIndex() + 1;
				}
			}
			else if (evt.getSource() instanceof JOAJCheckBox) {
				JOAJCheckBox cb = (JOAJCheckBox)evt.getSource();
			}
			else if (evt.getSource() instanceof JOAJRadioButton) {
				JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
				if (evt.getStateChange() == ItemEvent.SELECTED && rb == maxVal) {
					disableValueSpinner();
				}

				if (evt.getStateChange() == ItemEvent.SELECTED && rb == minVal) {
					disableValueSpinner();
				}

				if (evt.getStateChange() == ItemEvent.SELECTED && rb == customVal) {
					enableValueSpinner();
				}
				else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mBlackContours) {
					mContourStyle = MapSpecification.BLACK_CONTOURS;
					mOverlayLineColor = Color.black;
					mOverlayContourColorSwatch.setEnabled(false);
					if (mColorBarPanel != null) {
						// remove existing color bar component (if there is one)
						isoStuffCont.remove(mColorBarPanel);
						mColorBarPanel = null;
						mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar, mOverlayLineColor);
						mColorBarPanel.setLinked(false);
						mColorBarPanel.setEnhanceable(false);
						mColorBarPanel.setBroadcastMode(false);
						isoStuffCont.add(mColorBarPanel);
						isoStuffCont.invalidate();
						this.validate();
					}
				}
				else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mWhiteContours) {
					mContourStyle = MapSpecification.WHITE_CONTOURS;
					mOverlayLineColor = Color.white;
					mOverlayContourColorSwatch.setEnabled(false);
					if (mColorBarPanel != null) {
						// remove existing color bar component (if there is one)
						isoStuffCont.remove(mColorBarPanel);
						mColorBarPanel = null;
						mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar, mOverlayLineColor);
						mColorBarPanel.setLinked(false);
						mColorBarPanel.setEnhanceable(false);
						mColorBarPanel.setBroadcastMode(false);
						isoStuffCont.add(mColorBarPanel);
						isoStuffCont.invalidate();
						this.validate();
					}
				}
				else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mCustomSingleColor) {
					mContourStyle = MapSpecification.CUSTOM_CONTOURS;
					mOverlayLineColor = mOverlayContourColorSwatch.getColor();
					if (mColorBarPanel != null) {
						// remove existing color bar component (if there is one)
						isoStuffCont.remove(mColorBarPanel);
						mColorBarPanel = null;
						mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar, mOverlayLineColor);
						mColorBarPanel.setLinked(false);
						mColorBarPanel.setEnhanceable(false);
						mColorBarPanel.setBroadcastMode(false);
						isoStuffCont.add(mColorBarPanel);
						isoStuffCont.invalidate();
						this.validate();
					}
				}
				// else if (evt.getStateChange() == ItemEvent.SELECTED && rb ==
				// mColorsFromColorBar) {
				// mContourStyle = MapSpecification.COLOR_CONTOURS;
				// mOverlayContourColorSwatch.setEnabled(false);
				// if (mColorBarPanel != null) {
				// // remove existing color bar component (if there is one)
				// isoStuffCont.remove(mColorBarPanel);
				// mColorBarPanel = null;
				// mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar,
				// (Object) mColorBar);
				// mColorBarPanel.setLinked(false);
				// mColorBarPanel.setEnhanceable(false);
				// mColorBarPanel.setBroadcastMode(false);
				// isoStuffCont.add(mColorBarPanel);
				// isoStuffCont.invalidate();
				// this.validate();
				// }
				// }
			}
		}

		public void setSelectedIsoCB(int i) {
			mIgnore = true;
			mColorbarList.setSelectedIndex(i);
			mColorbarList.ensureIndexIsVisible(i);
		}

		public void setSelectedParam(int i) {
			mIgnore = true;
			mParamList.setSelectedIndex(i);
			mParamList.ensureIndexIsVisible(i);
			mSelParam = i;
		}

		public void setSelectedColorBar(Object o, boolean b) {
			mIgnore = true;
			mColorbarList.setSelectedValue(o, b);
		}

		public void setSelectedSurface(Object o, boolean b) {
			mIgnore = true;
			mSurfaceList.setSelectedValue(o, b);
		}

		public UVCoordinate computeReferencedRange() {
			double minRefRange = 10e35;
			double maxRefRange = -10e25;

			String newSurfaceName = (String)mSurfaceList.getSelectedValue();
			// read the surface from disk
			NewInterpolationSurface mCurrSurface = null;
			try {
				mCurrSurface = JOAFormulas.getSurface(newSurfaceName);
			}
			catch (Exception ex) {
				mCurrSurface = null;
			}

			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					int iPos = sech.getVarPos(mFileViewer.mAllProperties[mParamList.getSelectedIndex()].getVarLabel(), false);
					int sPos = sech.getVarPos(mCurrSurface.getParam(), false);

					if (iPos < 0 || sPos < 0) {
						continue;
					}

					// loop through the stations
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						if (sh.mUseStn) {
							// create arrays
							double[] sVals = new double[sh.mNumBottles];
							double[] mVals = new double[sh.mNumBottles];

							for (int b = 0; b < sh.mNumBottles; b++) {
								Bottle bh = (Bottle)sh.mBottles.elementAt(b);
								sVals[b] = bh.mDValues[sPos];
								mVals[b] = bh.mDValues[iPos];
							}

							// dereference
							JOAFormulas.dereferenceStation(sh.mNumBottles, mRefLevel, sVals, mVals);

							// compute max min
							for (int b = 0; b < sh.mNumBottles; b++) {
								if (mVals[b] != JOAConstants.MISSINGVALUE && mVals[b] > maxRefRange) {
									maxRefRange = mVals[b];
								}
								if (mVals[b] != JOAConstants.MISSINGVALUE && mVals[b] < minRefRange) {
									minRefRange = mVals[b];
								}
							}
						}
					}
				}
			}
			return new UVCoordinate(minRefRange, maxRefRange);
		}

		public void createColorBar(int curveShape) {
			UVCoordinate refLevelRange = null;
			double refLevel = 0.0;

			// get base and end levels from the selected parameter
			double baseLevel = mFileViewer.mAllProperties[mSelParam].getPlotMin();
			double endLevel = mFileViewer.mAllProperties[mSelParam].getPlotMax();
			boolean isReferenced = mUseRefLevel.isSelected() && mUseRefLevel.isEnabled();

			// have to compute reference level
			if (isReferenced) {
				mRefLevel = mRefLevels.getValue();
				refLevelRange = computeReferencedRange();
				baseLevel = refLevelRange.getU();
				endLevel = refLevelRange.getV();
			}

			// get base and end levels from the selected parameter
			double numLevels = 0;
			if (mAutoscaleColorScheme == 1 || mAutoscaleColorScheme == 3 || mAutoscaleColorScheme == 5
			    || mAutoscaleColorScheme == 7) {
				numLevels = 32;
			}
			else {
				numLevels = 16;
			}

			// compute new color bar values
			mColorBarValues = null;
			mColorBarValues = new double[(int)numLevels];
			mColorBarColors = new Color[(int)numLevels];
			if (curveShape == JOAConstants.LINEAR) {
				double increment = (endLevel - baseLevel) / (numLevels - 1);
				for (int i = 0; i < (int)numLevels; i++) {
					mColorBarValues[i] = baseLevel + (i * increment);
				}
			}
			else if (curveShape == JOAConstants.EXPONENTIALUP || curveShape == JOAConstants.EXPONENTIALDOWN) {
				double shape = JOAFormulas.getShape(baseLevel, endLevel);
				double scaledMax = Math.abs(endLevel - baseLevel);
				double lnScaledMin = Math.log(shape);
				double lnScaledMax = Math.log(scaledMax + shape);
				double increment = (lnScaledMax - lnScaledMin) / (numLevels - 1);

				for (int i = 0; i < (int)numLevels; i++) {
					if (curveShape == JOAConstants.EXPONENTIALUP) {
						// lower
						if (baseLevel < endLevel) {
							mColorBarValues[i] = baseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
						}
						else {
							mColorBarValues[i] = baseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
						}
					}
					else if (curveShape == JOAConstants.EXPONENTIALDOWN) {
						// upper
						if (baseLevel < endLevel) {
							mColorBarValues[(int)numLevels - i - 1] = endLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
						}
						else {
							mColorBarValues[(int)numLevels - i - 1] = endLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
						}
					}
				}
			}
			else if (curveShape == JOAConstants.LOGISTIC) {
				// logistic is a pieced together from upper and lower asymptote
				int mid = 0;
				int nl = (int)numLevels;
				if (nl % 2 > 0) {
					mid = (nl / 2) + 1;
				}
				else {
					mid = nl / 2;
				}

				// upper asymptote from base level to midpoint
				double newEndLevel = (baseLevel + endLevel) / 2;
				double shape = JOAFormulas.getShape(baseLevel, newEndLevel);
				double scaledMax = Math.abs(baseLevel - newEndLevel);
				double lnScaledMin = Math.log(shape);
				double lnScaledMax = Math.log(scaledMax + shape);
				double increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

				// lower
				for (int i = 0; i < mid; i++) {
					if (baseLevel < newEndLevel) {
						mColorBarValues[mid - i - 1] = newEndLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
					else {
						mColorBarValues[mid - i - 1] = newEndLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
				}

				// lower asymptote from midpoint to endlevel
				double newBaseLevel = newEndLevel;
				shape = JOAFormulas.getShape(newBaseLevel, endLevel);
				scaledMax = Math.abs(newBaseLevel - endLevel);
				lnScaledMin = Math.log(shape);
				lnScaledMax = Math.log(scaledMax + shape);
				increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

				// upper
				int endl = 0;
				if (nl % 2 > 0) {
					endl = mid - 1;
				}
				else {
					endl = mid;
				}
				for (int i = 0; i < endl; i++) {
					if (newBaseLevel < endLevel) {
						mColorBarValues[i + mid] = newBaseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
					else {
						mColorBarValues[i + mid] = newBaseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
				}
			}

			// assign colors to color bar
			Color startColor = Color.blue;
			Color midColor = Color.white;
			Color endColor = Color.red;
			if (mAutoscaleColorScheme <= 4) {
				// blend colors
				// get current colors
				if (mAutoscaleColorScheme <= 2) {
					startColor = Color.blue;
					midColor = Color.white;
					endColor = Color.red;
				}
				else {
					startColor = Color.red;
					midColor = Color.white;
					endColor = Color.blue;
				}

				int nl = (int)numLevels;
				int mid = 0;
				double deltaRed = 0;
				double deltaGreen = 0;
				double deltaBlue = 0;
				if (nl % 2 > 0) {
					// odd number of entries--middle color is middle color swatch
					mid = (nl / 2) + 1;
					mColorBarColors[mid - 1] = midColor;

					// blend from start to mid
					deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)mid;
					deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)mid;
					deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)mid;

					int c = 1;
					for (int i = 0; i < mid - 1; i++) {
						double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}

					// blend from mid to end
					deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)mid;
					deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)mid;
					deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)mid;

					c = 1;
					for (int i = mid; i < (int)numLevels; i++) {
						double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}
				}
				else {
					// even number of entries--middle color is in between middle values
					mid = nl / 2;

					// blend from start to mid
					deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)(mid + 1);
					deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)(mid + 1);
					deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)(mid + 1);

					int c = 1;
					for (int i = 0; i < mid; i++) {
						double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}

					// blend from mid to end
					deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)(mid + 1);
					deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)(mid + 1);
					deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)(mid + 1);

					c = 1;
					for (int i = mid; i < (int)numLevels; i++) {
						double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}
				}
			}
			else {
				// create a rainbow
				float hue = 0;
				float sat = 1;
				float light = 1;
				float startHue = 0;
				if (mAutoscaleColorScheme < 7) {
					startHue = 0;
				}
				else {
					startHue = 270;
				}
				float hueAngleDelta = (float)270 / (float)numLevels;
				for (int i = 0; i < (int)numLevels; i++) {
					if (mAutoscaleColorScheme < 7) {
						hue = (startHue + ((float)i * hueAngleDelta)) / 360;
					}
					else {
						hue = (startHue - ((float)i * hueAngleDelta)) / 360;
					}
					mColorBarColors[i] = new Color(Color.HSBtoRGB(hue, sat, light));
					// System.out.println("h= " + (hue * 360) + " s= " + (sat * 100) + "
					// light= " + (light*100));
				}
			}

			// create a new NewColorBar
			String paramText = mFileViewer.mAllProperties[mSelParam].getVarLabel();
			String paramUnits = mFileViewer.mAllProperties[mSelParam].getUnits();
			String titleText = new String("Autoscale");
			String descripText = new String("Autoscale");
			if (mColorBar == null) {
				mColorBar = new NewColorBar(mColorBarColors, mColorBarValues, (int)numLevels, paramText, paramUnits, titleText, descripText);
			}
			else {
				// modify existing color bar
				mColorBar.setValues(mColorBarValues);
				mColorBar.setColors(mColorBarColors);
				mColorBar.setBaseLevel(mColorBarValues[0]);
				mColorBar.setEndLevel(mColorBarValues[(int)numLevels - 1]);
				mColorBar.setTitle(titleText);
				mColorBar.setParam(paramText);
				mColorBar.setDescription(descripText);
				mColorBar.setNumLevels((int)numLevels);
			}
		}

		public void setColorBar(NewColorBar cb) {
			mColorBar = cb;
		}

		public NewColorBar getColorBar() {
			return mColorBar;
		}

		public void updateColorBar() {
			// display the new color bar
			if (mColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				isoStuffCont.remove(mColorBarPanel);
				mColorBarPanel = null;
			}

			mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar, mOverlayLineColor);

			mColorBarPanel.setLinked(false);
			mColorBarPanel.setEnhanceable(false);
			mColorBarPanel.setBroadcastMode(false);
			isoStuffCont.add(mColorBarPanel);
			isoStuffCont.invalidate();
			this.validate();
			mAutoScaleCreated = true;
		}

		private void buildCBList() {
			mColorbarList = null;

			Vector<String> cbs = JOAFormulas.getColorBarList();
			mdcbs = new Vector<String>();

			for (int i = 0; i < cbs.size(); i++) {
				NewColorBar cb = null;

				String newCBARName = (String)cbs.elementAt(i);

				// read the color bar from disk
				try {
					cb = JOAFormulas.getColorBar(newCBARName);
				}
				catch (Exception ex) {
					cb = null;
				}

				if (!cb.isMetadataColorBar()) {
					mdcbs.addElement(new String(newCBARName));
				}
			}
			mColorbarList = new JOAJList(mdcbs);
		}

		private void buildParamList() {
			for (int i = 0; i < mFileViewer.gNumProperties; i++) {
				params.addElement(mFileViewer.mAllProperties[i].getVarLabel());
			}

			if (mParamList == null) {
				mParamList = new JOAJList(params);
			}
			else {
				mParamList.setListData(params);
				mParamList.setSelectedIndex(mMapSpec.getIsoContourVarCode() - 1);
				mParamList.invalidate();
			}
		}

		public void valueChanged(ListSelectionEvent evt) {
			JOAJList cb = (JOAJList)evt.getSource();
			if (mIgnore) {
				mIgnore = false;
				return;
			}

			if (cb == mColorbarList) {
				// get a new NewColorBar
				String newCBARName = (String)cb.getSelectedValue();
				mColorBar = null;

				// read the color bar from disk
				try {
					mColorBar = JOAFormulas.getColorBar(newCBARName);
					mMapSpec.setOverlayContoursColorBar(mColorBar);
				}
				catch (Exception ex) {
					mMapSpec.setOverlayContoursColorBar(null);
					return;
				}

				// display the preview color bar
				if (mColorBarPanel != null) {
					// remove existing color bar component (if there is one)
					isoStuffCont.remove(mColorBarPanel);
					mColorBarPanel = null;
				}

				if (mBlackContours.isSelected()) {
					mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar, Color.black);
				}
				else if (mWhiteContours.isSelected()) {
					mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar, Color.white);
				}
				else if (mCustomSingleColor.isSelected()) {
					mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar, mOverlayContourColorSwatch.getColor());
				}
				else {
					mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar);
				}
				mColorBarPanel.setLinked(false);
				mColorBarPanel.setEnhanceable(false);
				mColorBarPanel.setBroadcastMode(false);
				isoStuffCont.add(mColorBarPanel);
				isoStuffCont.invalidate();
				this.validate();
				mAutoScaleCreated = false;
			}
			else if (cb == mParamList) {
				mSelParam = cb.getSelectedIndex();
				if (mSelParam < 0) { return; }
				String selParamText = (String)mParamList.getSelectedValue();

				// make sure value of the param is not missing
				int yerrLine = -1;
				double tempYMin = mFileViewer.mAllProperties[mSelParam].getPlotMin();
				double tempYMax = mFileViewer.mAllProperties[mSelParam].getPlotMax();
				Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
				double yInc = newRange.getVal3();
				if (Double.isNaN(yInc)) {
					yerrLine = mSelParam;
				}

				if (yerrLine >= 0) {
					// disable the y param
					JFrame f = new JFrame("Parameter Values Missing Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "All values for " + selParamText + " are missing. " + "\n"
					    + "Select a new parameter");
					mParamList.clearSelection();
					mSelParam = 0;
				}
				
				// select a colorbar in the colorbar list
				String selParamStr = ((String) mParamList.getSelectedValue()).toUpperCase();
				boolean found = false;
				for (int i = 0; i < mdcbs.size(); i++) {
					String param = ((String) mdcbs.elementAt(i)).toUpperCase();
					if (param.startsWith(selParamStr)) {
						// position list
						mColorbarList.setSelectedIndex(i);
						mColorbarList.ensureIndexIsVisible(i);
						found = true;
						break;
					}
					else if (param.indexOf(selParamStr) >= 0) {
						// position list
						mColorbarList.setSelectedIndex(i);
						mColorbarList.ensureIndexIsVisible(i);
						found = true;
						break;
					}
				}

				if (!found) {
					mColorbarList.clearSelection();
				}
			}
			else if (cb == mSurfaceList) {
				// get a new Surface
				String newSurfaceName = (String)cb.getSelectedValue();

				// read the surface from disk
				try {
					mSurface = JOAFormulas.getSurface(newSurfaceName);
					mMapSpec.setIsoContourSurface(mSurface);

					// assign a param label
					mParamLabel.setText(mSurface.getParam() + " =");
					mParamLabel.invalidate();

					// make sure surface param exists in the data
					if (!JOAFormulas.paramExists(mFileViewer, mMapSpec.getIsoContourSurface().getParam())) {
						if (JOAFormulas.isCalculatable(mMapSpec.getIsoContourSurface().getParam())) {
							// make a new calculation
							Calculation calc = JOAFormulas
							    .createCalcFromName(mFileViewer, mMapSpec.getIsoContourSurface().getParam());

							if (calc != null) {
								// do calculation
								mFileViewer.addCalculation(calc);
								mFileViewer.doCalcs();
							}
						}
						else {
							JFrame f = new JFrame("Contour Manager Error");
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(f, "Surface parameter does not exist in this file" + "\n"
							    + "and couldn't be calculated");
							return;
						}
					}

					// set the value of the spinner
					boolean reverseSpinner = false;
					int precision = 2;
					if (mMapSpec.getIsoContourSurface().getParam().equalsIgnoreCase("PRES")
					    || mMapSpec.getIsoContourSurface().getParam().startsWith("SIG")) {
						;
					}
					reverseSpinner = true;
					if (mMapSpec.getIsoContourSurface().getParam().equalsIgnoreCase("PRES")) {
						precision = 0;
					}
					double[] newVals = mMapSpec.getIsoContourSurface().getValues();
					mSurfaceSpinner.setNewValues(newVals, mMapSpec.getIsoContourSurface().getNumLevels(), reverseSpinner);
					mSurfaceSpinner.setValue(newVals[0]);
					mSurfaceSpinner.setPrecision(precision);
					mRefLevels.setNewValues(newVals, mMapSpec.getIsoContourSurface().getNumLevels(), reverseSpinner);
					mRefLevels.setValue(newVals[0]);
					mRefLevels.setPrecision(precision);
				}
				catch (Exception ex) {
					ex.printStackTrace();
					mMapSpec.setIsoContourSurface(null);
				}
			}
		}

		public void maintainRefLevelStuff(boolean b) {
			mRefLevels.setEnabled(b);
			mParamLabel.setEnabled(b);
		}

		public void disableIsoSurfaceStuff() {
			mSurfaceSpinner.setEnabled(false);
			mBottomUp.setEnabled(false);
			minVal.setEnabled(false);
			maxVal.setEnabled(false);
			customVal.setEnabled(false);
			mNumBottlesField.setEnabled(false);
			mLocalInterpolation.setEnabled(false);
			iLabel3.setEnabled(false);
			mRefLevels.setEnabled(false);
			maintainRefLevelStuff(false);
			mUseRefLevel.setEnabled(false);
		}

		public void enableIsoSurfaceStuff() {
			mSurfaceSpinner.setEnabled(true);
			mBottomUp.setEnabled(true);
			minVal.setEnabled(true);
			maxVal.setEnabled(true);
			customVal.setEnabled(true);
			mNumBottlesField.setEnabled(true);
			mLocalInterpolation.setEnabled(true);
			iLabel3.setEnabled(true);
			mUseRefLevel.setEnabled(!(minVal.isSelected() || maxVal.isSelected()));
			maintainRefLevelStuff(mUseRefLevel.isSelected());
		}

		public void disableAutoscaleStuff() {
			mLinear.setEnabled(false);
			mPowerUp.setEnabled(false);
			mPowerDown.setEnabled(false);
			mLogistic.setEnabled(false);
			mColorCombo.setEnabled(false);
			ccLbl4.setEnabled(false);
		}

		public void enableAutoscaleStuff() {
			mLinear.setEnabled(true);
			mPowerUp.setEnabled(true);
			mPowerDown.setEnabled(true);
			mLogistic.setEnabled(true);
			mColorCombo.setEnabled(true);
			ccLbl4.setEnabled(true);
		}

		public void maintainUI() {
			boolean c1 = mParamList.getSelectedIndex() >= 0;
			boolean c2 = mColorbarList.getSelectedIndex() >= 0 || mAutoScaleCreated == true;
			boolean c3 = mSurfaceList.getSelectedIndex() >= 0;

			if (!c1) {
				// no parameter selected
				disableAutoscaleStuff();
			}
			else {
				// can create an autoscale colorbar
				enableAutoscaleStuff();
			}

			if (!c3) {
				// no surface selected
				disableIsoSurfaceStuff();
			}
			else {
				enableIsoSurfaceStuff();
			}

			if (!c1 || !c2 || !c3) {
				mUIState = false;
			}
			else {
				mUIState = true;
			}
		}

		public boolean isUIReady() {
			return mUIState;
		}

		public void dialogDismissed(JDialog d) {
		}

		// Cancel button
		public void dialogCancelled(JDialog d) {
		}

		// something other than the OK button
		public void dialogDismissedTwo(JDialog d) {
			;
		}

		// Apply button, OK w/o dismissing the dialog
		public void dialogApply(JDialog d) {
		}

		public void dialogApplyTwo(Object d) {
			// got a color change from a color swatch
			mCustomSingleColor.setSelected(true);
			mOverlayLineColor = mOverlayContourColorSwatch.getColor();
			if (mColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				isoStuffCont.remove(mColorBarPanel);
				mColorBarPanel = null;
				mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar, mOverlayLineColor);
				mColorBar = mColorBarPanel.getColorBar();
				mColorBarPanel.setLinked(false);
				mColorBarPanel.setEnhanceable(false);
				mColorBarPanel.setBroadcastMode(false);
				isoStuffCont.add(mColorBarPanel);
				isoStuffCont.invalidate();
				this.validate();
			}
		}
	}

	private class MetadataSymbols extends JPanel implements ListSelectionListener, UIPanelMaintainer {
		JOAJLabel ccLbl6;
		JOAJList mMDColorbarList = null;
		FileViewer mFileViewer = null;
		boolean mUIState = false;
		private NewColorBar mColorBar = null;

		public MetadataSymbols(FileViewer fv) {
			mFileViewer = fv;
			this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
			JPanel mdColorStuffCont = new JPanel();
			mdColorStuffCont.setLayout(new GridLayout(1, 2, 5, 5));

			// now add a colorbar list
			JPanel metaDataColor = new JPanel();
			metaDataColor.setLayout(new BorderLayout(5, 5));
			buildMetaDataCBList();

			mMDColorbarList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mMDColorbarList.setPrototypeCellValue("Temperature  TemperatureTemperature ");
			mMDColorbarList.setVisibleRowCount(6);
			JScrollPane listScroller35 = new JScrollPane(mMDColorbarList);
			mMDColorbarList.addListSelectionListener(this);
			ccLbl6 = new JOAJLabel(b.getString("kMDColors:"));
			metaDataColor.add(ccLbl6, "North");
			metaDataColor.add(listScroller35, "Center");
			mdColorStuffCont.add(metaDataColor);
			this.add(mdColorStuffCont);
		}

		public void valueChanged(ListSelectionEvent evt) {
			JOAJList cb = (JOAJList)evt.getSource();

			if (cb == mMDColorbarList) {
				// get a new NewColorBar
				String newCBARName = (String)cb.getSelectedValue();
				mColorBar = null;

				// read the color bar from disk
				try {
					mColorBar = JOAFormulas.getColorBar(newCBARName);
					mMapSpec.setStnColorColorBar(mColorBar);
				}
				catch (Exception ex) {
					mMapSpec.setStnColorColorBar(null);
					return;
				}
			}
		}

		private void buildMetaDataCBList() {
			mMDColorbarList = null;

			Vector<String> cbs = JOAFormulas.getColorBarList();
			Vector<String> mdcbs = new Vector<String>();

			for (int i = 0; i < cbs.size(); i++) {
				NewColorBar cb = null;

				String newCBARName = (String)cbs.elementAt(i);

				// read the color bar from disk
				try {
					cb = JOAFormulas.getColorBar(newCBARName);
				}
				catch (Exception ex) {
					cb = null;
				}

				if (cb.isMetadataColorBar()) {
					mdcbs.addElement(new String(newCBARName));
				}
			}
			mMDColorbarList = new JOAJList(mdcbs);
		}

		public void setSelectedCB(int i) {
			mMDColorbarList.setSelectedIndex(i);
			mMDColorbarList.ensureIndexIsVisible(i);
		}

		public void setSelectedColorBar(Object o, boolean b) {
			mMDColorbarList.setSelectedValue(o, b);
		}

		public String getColorBarName() {
			return (String)mMDColorbarList.getSelectedValue();
		}

		public int getColorBarIndex() {
			return mMDColorbarList.getSelectedIndex();
		}

		public void maintainUI() {
			if (mMDColorbarList == null) {
				mUIState = false;
				return;
			}
			boolean c1 = mMDColorbarList.getSelectedIndex() >= 0;
			if (!c1) {
				mUIState = false;
			}
			else {
				mUIState = true;
			}
		}

		public boolean isUIReady() {
			return mUIState;
		}
	}

	private class StationCalculationContours extends JPanel implements ActionListener, ListSelectionListener,
	    ItemListener, UIPanelMaintainer, DialogClient {
		private LargeIconButton mLinear = null;
		private LargeIconButton mPowerUp = null;
		private LargeIconButton mPowerDown = null;
		private LargeIconButton mLogistic = null;
		private ColorBarPanel mStnContourColorBarPanel = null;
		private JOAJList mStnCalcContourColorbarList = null;
		private JOAJLabel ccLbl4 = null;
		private JOAJLabel ccLbl5 = null;
		private JOAJLabel ccLbl6 = null;
		private JPanel autoscalePanel2;
		private JOAJList mStnVarList = null;
		private int mAutoscaleColorScheme = 1;
		private JOAJComboBox mColorCombo = null;
		private int mSelStnParam = -1;
		private JPanel mUpperRow;;
		private FileViewer mFileViewer = null;
		private boolean mUIState = false;
		private boolean mAutoScaleCreated = false;
		private NewColorBar mStnCalcContourColorBar = null;
		private JOASwatch mOverlayContourColorSwatch;
		private JOAJRadioButton mBlackContours = null;
		private JOAJRadioButton mWhiteContours = null;
		private JOAJRadioButton mCustomSingleColor = null;
		private Color mOverlayLineColor = null;
		private JOAJTextField mNumSkipField = null;
		private JSpinner mNumX, mNumY;
		private JOAJTextField cay, nrng;
		private JOAJCheckBox mMaskCoastline = null;
		private int mContourStyle = MapSpecification.BLACK_CONTOURS;
		private JOAJCheckBox mFilledStnContours = null;

		public int getCountourStyle() {
			return mContourStyle;
		}

		public boolean isFilledStnContours() {
			return mFilledStnContours.isSelected();
		}

		public Color getOverlayContourColor() {
			return mStnContourColorBarPanel.getLineColor();
		}

		public int getNumX() {
			return ((Integer)mNumX.getValue()).intValue();
		}

		public int getNumY() {
			return ((Integer)mNumY.getValue()).intValue();
		}

		public int getNrng() {
			String fldText = nrng.getText();
			int retVal = 10;
			try {
				retVal = Integer.valueOf(fldText).intValue();
			}
			catch (Exception ex) {
			}
			return retVal;
		}

		public double getCay() {
			String fldText = cay.getText();
			double retVal = 5.0;
			try {
				retVal = Double.valueOf(fldText).doubleValue();
			}
			catch (Exception ex) {
			}
			return retVal;
		}

		public boolean isLandMask() {
			return mMaskCoastline.isSelected();
		}

		public int getContourSkipInterval() {
			String fldText = mNumSkipField.getText();
			int retVal = 1;
			try {
				retVal = Integer.valueOf(fldText).intValue();
			}
			catch (Exception ex) {
			}
			return retVal;
		}

		public StationCalculationContours(FileViewer fv) {
			mFileViewer = fv;
			this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 5));
			mUpperRow = new JPanel(new RowLayout(Orientation.LEFT, Orientation.TOP, 5));
			JPanel lowerRow = new JPanel(new RowLayout(Orientation.LEFT, Orientation.TOP, 5));

			// build the station var list
			JPanel stnVars = new JPanel();
			stnVars.setLayout(new BorderLayout(5, 5));
			ccLbl4 = new JOAJLabel(b.getString("kStationCalculation2"));
			stnVars.add(ccLbl4, "North");
			buildStnVarList();

			mStnVarList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mStnVarList.setPrototypeCellValue("Temperature  TemperatureTemperature");
			mStnVarList.setVisibleRowCount(8);
			JScrollPane listScroller23 = new JScrollPane(mStnVarList);
			mStnVarList.addListSelectionListener(this);
			stnVars.add(listScroller23, "Center");
			mUpperRow.add(stnVars);

			// now add a colorbar list
			JPanel stnVarColor = new JPanel();
			stnVarColor.setLayout(new BorderLayout(5, 5));
			buildStnVarCBList();

			mStnCalcContourColorbarList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mStnCalcContourColorbarList.setPrototypeCellValue("Temperature                          ");
			mStnCalcContourColorbarList.setVisibleRowCount(4);
			JScrollPane listScroller34 = new JScrollPane(mStnCalcContourColorbarList);
			mStnCalcContourColorbarList.addListSelectionListener(this);
			ccLbl5 = new JOAJLabel(b.getString("kColors:"));
			stnVarColor.add(ccLbl5, "North");
			stnVarColor.add(listScroller34, "Center");

			// panel for autoscaling goes here
			autoscalePanel2 = new JPanel();
			autoscalePanel2.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 10));
			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kCreateAutoscaleColorbar"));
			if (JOAConstants.ISMAC) {
				// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
			}
			autoscalePanel2.setBorder(tb);

			JPanel autoscaleBtnsPanel = new JPanel();
			autoscaleBtnsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			// line6.add(new JOAJLabel(b.getString("kBasedOn")));
			JPanel shapePanel = new JPanel();
			shapePanel.setLayout(new GridLayout(1, 4, 5, 0));
			mLinear = new LargeIconButton(new ImageIcon(getClass().getResource("images/linear.gif")));
			shapePanel.add(mLinear);
			mPowerUp = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerup.gif")));
			shapePanel.add(mPowerUp);
			mPowerDown = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerdown.gif")));
			shapePanel.add(mPowerDown);
			mLogistic = new LargeIconButton(new ImageIcon(getClass().getResource("images/logistic.gif")));
			shapePanel.add(mLogistic);
			mLinear.addActionListener(this);
			mPowerUp.addActionListener(this);
			mPowerDown.addActionListener(this);
			mLogistic.addActionListener(this);
			mLinear.setActionCommand("linear");
			mPowerUp.setActionCommand("powerUp");
			mPowerDown.setActionCommand("powerDown");
			mLogistic.setActionCommand("logistic");
			autoscaleBtnsPanel.add(shapePanel);
			autoscalePanel2.add(autoscaleBtnsPanel);

			mLinear.setToolTipText(b.getString("kLinearTip"));
			mPowerUp.setToolTipText(b.getString("kIncreasingExpTip"));
			mPowerDown.setToolTipText(b.getString("kDecreasingExpTip"));
			mLogistic.setToolTipText(b.getString("kReverseSTip"));

			JPanel autoscaleComboPanel = new JPanel();
			autoscaleComboPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			ccLbl6 = new JOAJLabel(b.getString("kUsing"));
			autoscaleComboPanel.add(ccLbl6);
			Vector<String> autoScaleChoices = new Vector<String>();
			autoScaleChoices.addElement(new String("Blue-White-Red-32"));
			autoScaleChoices.addElement(new String("Blue-White-Red-16"));
			autoScaleChoices.addElement(new String("Red-White-Blue-32"));
			autoScaleChoices.addElement(new String("Red-White-Blue-16"));
			autoScaleChoices.addElement(new String("Rainbow-32"));
			autoScaleChoices.addElement(new String("Rainbow-16"));
			autoScaleChoices.addElement(new String("Rainbow(inv)-32"));
			autoScaleChoices.addElement(new String("Rainbow(inv)-16"));
			mColorCombo = new JOAJComboBox(autoScaleChoices);
			mColorCombo.setSelectedIndex(JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME);
			mAutoscaleColorScheme = JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME + 1;
			mColorCombo.addItemListener(this);
			autoscaleComboPanel.add(mColorCombo);
			autoscalePanel2.add(autoscaleComboPanel);
			stnVarColor.add(autoscalePanel2, "South");
			mUpperRow.add(stnVarColor);
			Color contColor = mMapSpec.getStnVarCalcContourColor();

			// blank color bar panel
			mStnContourColorBarPanel = new ColorBarPanel(mParent, mFileViewer, null, contColor);
			mStnContourColorBarPanel.setLinked(false);
			mStnContourColorBarPanel.setEnhanceable(false);
			mStnContourColorBarPanel.setBroadcastMode(false);
			mUpperRow.add(mStnContourColorBarPanel);

			// Style Panel
			JPanel overlayStyleCont = new JPanel();
			overlayStyleCont.setLayout(new BorderLayout(5, 0));
			tb = BorderFactory.createTitledBorder(b.getString("kStyle"));
			overlayStyleCont.setBorder(tb);

			JPanel overlayStyles = new JPanel();
			overlayStyles.setLayout(new GridLayout(4, 1, 0, 0));
			mBlackContours = new JOAJRadioButton(b.getString("kBlackContours"),
			    mMapSpec.getStnContourStyle() == MapSpecification.BLACK_CONTOURS);
			mWhiteContours = new JOAJRadioButton(b.getString("kWhiteContours"),
			    mMapSpec.getStnContourStyle() == MapSpecification.WHITE_CONTOURS);
			mCustomSingleColor = new JOAJRadioButton(b.getString("kCustomSingleColor"),
			    mMapSpec.getStnContourStyle() == MapSpecification.CUSTOM_CONTOURS);
			mOverlayContourColorSwatch = new JOASwatch(contColor, this);
			mOverlayContourColorSwatch.setEnabled(false);
			mFilledStnContours = new JOAJCheckBox("Filled contours", mMapSpec.isFilledStnContours());
			
			JPanel custColorLine = new JPanel();
			custColorLine.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			custColorLine.add(mCustomSingleColor);
			custColorLine.add(mOverlayContourColorSwatch);
			overlayStyles.add(mBlackContours);
			overlayStyles.add(mWhiteContours);
			overlayStyles.add(custColorLine);
			overlayStyles.add(mFilledStnContours);
			ButtonGroup sbg = new ButtonGroup();
			sbg.add(mBlackContours);
			sbg.add(mWhiteContours);
			sbg.add(mCustomSingleColor);
			overlayStyleCont.add("North", overlayStyles);
			mBlackContours.addItemListener(this);
			mWhiteContours.addItemListener(this);
			mCustomSingleColor.addItemListener(this);

			JPanel line3a = new JPanel();
			line3a.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
			line3a.add(new JOAJLabel(b.getString("kPlotEvery")));
			mNumSkipField = new JOAJTextField(2);
			mNumSkipField.setText(String.valueOf(mMapSpec.getPlotEveryNthContour()));
			mNumSkipField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			line3a.add(mNumSkipField);
			line3a.add(new JOAJLabel(b.getString("kContours")));
			overlayStyleCont.add("South", line3a);

			JPanel zContourStuff = new JPanel();
			zContourStuff.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));

			zContourStuff.add(overlayStyleCont);

			// zgrid settings
			JPanel zGridStuff = new JPanel();
			zGridStuff.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 10));
			tb = BorderFactory.createTitledBorder(b.getString("kGridSettings"));
			zGridStuff.setBorder(tb);

			JPanel zLine1 = new JPanel();
			JPanel zLine2 = new JPanel();
			JPanel zLine3 = new JPanel();
			zLine1.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
			zLine2.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
			zLine3.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));

			SpinnerNumberModel modely = new SpinnerNumberModel(mMapSpec.getNY(), 0, 1000, 10);
			SpinnerNumberModel modelx = new SpinnerNumberModel(mMapSpec.getNY(), 0, 1000, 10);

			mNumX = new JSpinner(modelx);
			mNumY = new JSpinner(modely);
			cay = new JOAJTextField(2);
			nrng = new JOAJTextField(2);
			cay.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			nrng.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			cay.setText(String.valueOf(mMapSpec.getCAY()));
			nrng.setText(String.valueOf(mMapSpec.getNRng()));
			mMaskCoastline = new JOAJCheckBox(b.getString("kMaskCoast"), mMapSpec.isMaskCoast());

			JOAJLabel lbl1 = new JOAJLabel(b.getString("kGridDimensions"));
			JOAJLabel lbl2 = new JOAJLabel(b.getString("kLon"));
			JOAJLabel lbl3 = new JOAJLabel(b.getString("kLat"));
			JOAJLabel lbl4 = new JOAJLabel(b.getString("kInterpTension"));
			JOAJLabel lbl5 = new JOAJLabel(b.getString("kNumGridSpaces"));

			zLine1.add(lbl1);
			zLine1.add(lbl2);
			zLine1.add(mNumX);
			zLine1.add(lbl3);
			zLine1.add(mNumY);

			zLine2.add(lbl4);
			zLine2.add(cay);

			zLine3.add(lbl5);
			zLine3.add(nrng);

			zGridStuff.add(zLine1);
			zGridStuff.add(zLine2);
			zGridStuff.add(zLine3);
			zGridStuff.add(mMaskCoastline);

			zContourStuff.add(zGridStuff);

			lowerRow.add(zContourStuff);
			this.add(mUpperRow);
			this.add(lowerRow);
		}

		public boolean isAutoScaledColor() {
			return mAutoScaleCreated;
		}

		private void buildStnVarCBList() {
			mStnCalcContourColorbarList = null;

			Vector<String> cbs = JOAFormulas.getColorBarList();
			Vector<String> mdcbs = new Vector<String>();

			for (int i = 0; i < cbs.size(); i++) {
				NewColorBar cb = null;

				String newCBARName = (String)cbs.elementAt(i);

				// read the color bar from disk
				try {
					cb = JOAFormulas.getColorBar(newCBARName);
				}
				catch (Exception ex) {
					cb = null;
				}

				if (!cb.isMetadataColorBar()) {
					mdcbs.addElement(new String(newCBARName));
				}
			}
			mStnCalcContourColorbarList = new JOAJList(mdcbs);
		}

		public NewColorBar getStnCalcContourColorBar() {
			return mStnCalcContourColorBar;
		}

		public void updateColorBar() {
			// display the new color bar
			if (mStnContourColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				mUpperRow.remove(mStnContourColorBarPanel);
				mStnContourColorBarPanel = null;
			}

			mStnContourColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mStnCalcContourColorBar, mMapSpec
			    .getStnVarCalcContourColor());
			mStnContourColorBarPanel.setLinked(false);
			mStnContourColorBarPanel.setEnhanceable(false);
			mStnContourColorBarPanel.setBroadcastMode(false);
			mUpperRow.add(mStnContourColorBarPanel);
			mUpperRow.invalidate();
			mUpperRow.validate();
			this.invalidate();
			this.validate();
			mAutoScaleCreated = true;
		}

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("linear")) {
				createColorBar(JOAConstants.LINEAR);
				updateColorBar();
				mLinear.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("powerUp")) {
				createColorBar(JOAConstants.EXPONENTIALUP);
				updateColorBar();
				mPowerUp.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("powerDown")) {
				createColorBar(JOAConstants.EXPONENTIALDOWN);
				updateColorBar();
				mPowerDown.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("logistic")) {
				createColorBar(JOAConstants.LOGISTIC);
				updateColorBar();
				mLogistic.setSelected(false);
				mAutoScaleCreated = true;
			}
		}

		public void setSelectedCB(int i) {
			mStnCalcContourColorbarList.setSelectedIndex(i);
			mStnCalcContourColorbarList.ensureIndexIsVisible(i);
		}

		public void setSelectedVar(int varCode) {
			mStnVarList.setSelectedIndex(varCode);
			mStnVarList.ensureIndexIsVisible(varCode);
		}

		public int getStnCalcIndex() {
			return mStnVarList.getSelectedIndex();
		}

		public void createColorBar(int curveShape) {
			// get base and end levels from the selected parameter
			// have to compute the base level and end level from
			// running though the selected station variable

			double baseLevel = 99999999;
			double endLevel = -99999999;
			String mStnVarName = null;
			String mStnVarUnits = null;
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);

					if (sec == 0) {
						mStnVarName = sech.getStnVar(mSelStnParam);
						mStnVarUnits = sech.getStnVarUnits(mSelStnParam);
					}

					if (sech.mNumCasts == 0) {
						continue;
					}

					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);

						// get the station value
						double val = sh.getStnValue(mSelStnParam);
						if (val == JOAConstants.MISSINGVALUE) {
							continue;
						}
						baseLevel = val < baseLevel ? val : baseLevel;
						endLevel = val > endLevel ? val : endLevel;
					}
				}
			}

			double numLevels = 0;
			if (mAutoscaleColorScheme == 1 || mAutoscaleColorScheme == 3 || mAutoscaleColorScheme == 5
			    || mAutoscaleColorScheme == 7) {
				numLevels = 32;
			}
			else {
				numLevels = 16;
			}

			// compute new color bar values
			mColorBarValues = null;
			mColorBarValues = new double[(int)numLevels];
			mColorBarColors = new Color[(int)numLevels];
			if (curveShape == JOAConstants.LINEAR) {
				double increment = (endLevel - baseLevel) / (numLevels - 1);
				for (int i = 0; i < (int)numLevels; i++) {
					mColorBarValues[i] = baseLevel + (i * increment);
				}
			}
			else if (curveShape == JOAConstants.EXPONENTIALUP || curveShape == JOAConstants.EXPONENTIALDOWN) {
				double shape = JOAFormulas.getShape(baseLevel, endLevel);
				double scaledMax = Math.abs(endLevel - baseLevel);
				double lnScaledMin = Math.log(shape);
				double lnScaledMax = Math.log(scaledMax + shape);
				double increment = (lnScaledMax - lnScaledMin) / (numLevels - 1);

				for (int i = 0; i < (int)numLevels; i++) {
					if (curveShape == JOAConstants.EXPONENTIALUP) {
						// lower
						if (baseLevel < endLevel) {
							mColorBarValues[i] = baseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
						}
						else {
							mColorBarValues[i] = baseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
						}
					}
					else if (curveShape == JOAConstants.EXPONENTIALDOWN) {
						// upper
						if (baseLevel < endLevel) {
							mColorBarValues[(int)numLevels - i - 1] = endLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
						}
						else {
							mColorBarValues[(int)numLevels - i - 1] = endLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
						}
					}
				}
			}
			else if (curveShape == JOAConstants.LOGISTIC) {
				// logistic is a pieced together from upper and lower asymptote
				int mid = 0;
				int nl = (int)numLevels;
				if (nl % 2 > 0) {
					mid = (nl / 2) + 1;
				}
				else {
					mid = nl / 2;
				}

				// upper asymptote from base level to midpoint
				double newEndLevel = (baseLevel + endLevel) / 2;
				double shape = JOAFormulas.getShape(baseLevel, newEndLevel);
				double scaledMax = Math.abs(baseLevel - newEndLevel);
				double lnScaledMin = Math.log(shape);
				double lnScaledMax = Math.log(scaledMax + shape);
				double increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

				// lower
				for (int i = 0; i < mid; i++) {
					if (baseLevel < newEndLevel) {
						mColorBarValues[mid - i - 1] = newEndLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
					else {
						mColorBarValues[mid - i - 1] = newEndLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
				}

				// lower asymptote from midpoint to endlevel
				double newBaseLevel = newEndLevel;
				shape = JOAFormulas.getShape(newBaseLevel, endLevel);
				scaledMax = Math.abs(newBaseLevel - endLevel);
				lnScaledMin = Math.log(shape);
				lnScaledMax = Math.log(scaledMax + shape);
				increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

				// upper
				int endl = 0;
				if (nl % 2 > 0) {
					endl = mid - 1;
				}
				else {
					endl = mid;
				}
				for (int i = 0; i < endl; i++) {
					if (newBaseLevel < endLevel) {
						mColorBarValues[i + mid] = newBaseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
					else {
						mColorBarValues[i + mid] = newBaseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
				}

			}

			// assign colors to color bar
			Color startColor = Color.blue;
			Color midColor = Color.white;
			Color endColor = Color.red;
			if (mAutoscaleColorScheme <= 4) {
				// blend colors
				// get current colors
				if (mAutoscaleColorScheme <= 2) {
					startColor = Color.blue;
					midColor = Color.white;
					endColor = Color.red;
				}
				else {
					startColor = Color.red;
					midColor = Color.white;
					endColor = Color.blue;
				}

				int nl = (int)numLevels;
				int mid = 0;
				double deltaRed = 0;
				double deltaGreen = 0;
				double deltaBlue = 0;
				if (nl % 2 > 0) {
					// odd number of entries--middle color is middle color swatch
					mid = (nl / 2) + 1;
					mColorBarColors[mid - 1] = midColor;

					// blend from start to mid
					deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)mid;
					deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)mid;
					deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)mid;

					int c = 1;
					for (int i = 0; i < mid - 1; i++) {
						double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}

					// blend from mid to end
					deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)mid;
					deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)mid;
					deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)mid;

					c = 1;
					for (int i = mid; i < (int)numLevels; i++) {
						double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}
				}
				else {
					// even number of entries--middle color is in between middle values
					mid = nl / 2;

					// blend from start to mid
					deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)(mid + 1);
					deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)(mid + 1);
					deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)(mid + 1);

					int c = 1;
					for (int i = 0; i < mid; i++) {
						double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}

					// blend from mid to end
					deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)(mid + 1);
					deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)(mid + 1);
					deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)(mid + 1);

					c = 1;
					for (int i = mid; i < (int)numLevels; i++) {
						double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}
				}
			}
			else {
				// create a rainbow
				float hue = 0;
				float sat = 1;
				float light = 1;
				float startHue = 0;
				if (mAutoscaleColorScheme < 7) {
					startHue = 0;
				}
				else {
					startHue = 270;
				}
				float hueAngleDelta = (float)270 / (float)numLevels;
				for (int i = 0; i < (int)numLevels; i++) {
					if (mAutoscaleColorScheme < 7) {
						hue = (startHue + ((float)i * hueAngleDelta)) / 360;
					}
					else {
						hue = (startHue - ((float)i * hueAngleDelta)) / 360;
					}
					mColorBarColors[i] = new Color(Color.HSBtoRGB(hue, sat, light));
					// System.out.println("h= " + (hue * 360) + " s= " + (sat * 100) + "
					// light= " + (light*100));
				}
			}

			// create a new NewColorBar
			String titleText = new String("Autoscale");
			String descripText = new String("Autoscale");
			if (mStnCalcContourColorBar == null) {
				mStnCalcContourColorBar = new NewColorBar(mColorBarColors, mColorBarValues, (int)numLevels, mStnVarName,
						mStnVarUnits, titleText, descripText);
			}
			else {
				// modify existing color bar
				mStnCalcContourColorBar.setValues(mColorBarValues);
				mStnCalcContourColorBar.setColors(mColorBarColors);
				mStnCalcContourColorBar.setBaseLevel(mColorBarValues[0]);
				mStnCalcContourColorBar.setEndLevel(mColorBarValues[(int)numLevels - 1]);
				mStnCalcContourColorBar.setTitle(titleText);
				mStnCalcContourColorBar.setParam(mStnVarName);
				mStnCalcContourColorBar.setDescription(descripText);
				mStnCalcContourColorBar.setNumLevels((int)numLevels);
			}
		}

		public void disableStnVarStuff() {
			mStnCalcContourColorbarList.setEnabled(false);
			mStnVarList.setEnabled(false);
			ccLbl4.setEnabled(false);
			ccLbl5.setEnabled(false);
			mStnCalcContourColorbarList.clearSelection();
			mStnVarList.clearSelection();
		}

		public void enableStnVarStuff() {
			mStnCalcContourColorbarList.setEnabled(true);
			mStnVarList.setEnabled(true);
			ccLbl4.setEnabled(true);
			ccLbl5.setEnabled(true);
		}

		public void valueChanged(ListSelectionEvent evt) {
			JOAJList cb = (JOAJList)evt.getSource();

			if (cb == mStnCalcContourColorbarList) {
				// get a new NewColorBar
				String newCBARName = (String)cb.getSelectedValue();
				mStnCalcContourColorBar = null;

				// read the color bar from disk
				try {
					mStnCalcContourColorBar = JOAFormulas.getColorBar(newCBARName);
					mMapSpec.setOverlayContoursColorBar(mStnCalcContourColorBar);
				}
				catch (Exception ex) {
					mMapSpec.setOverlayContoursColorBar(null);
					return;
				}

				// display the preview color bar
				if (mStnContourColorBarPanel != null) {
					// remove existing color bar component (if there is one)
					mUpperRow.remove(mStnContourColorBarPanel);
					mStnContourColorBarPanel = null;
				}

				mStnContourColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mStnCalcContourColorBar, mMapSpec
				    .getStnVarCalcContourColor());
				mStnContourColorBarPanel.setLinked(false);
				mStnContourColorBarPanel.setEnhanceable(false);
				mStnContourColorBarPanel.setBroadcastMode(false);
				mUpperRow.add(mStnContourColorBarPanel);
				mUpperRow.invalidate();
				mUpperRow.validate();
				this.invalidate();
				this.validate();
				mAutoScaleCreated = false;
			}
			else if (cb == mStnVarList) {
				mSelStnParam = cb.getSelectedIndex();
			}
		}

		public void itemStateChanged(ItemEvent evt) {
			if (evt.getSource() instanceof JOAJComboBox) {
				JOAJComboBox cb = (JOAJComboBox)evt.getSource();
				if (cb == mColorCombo) {
					mAutoscaleColorScheme = cb.getSelectedIndex() + 1;
				}
			}
			else if (evt.getSource() instanceof JOAJRadioButton) {
				JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();

				if (evt.getStateChange() == ItemEvent.SELECTED && rb == mBlackContours) {
					mContourStyle = MapSpecification.BLACK_CONTOURS;
					mOverlayLineColor = Color.black;
					mOverlayContourColorSwatch.setEnabled(false);
					if (mStnContourColorBarPanel != null) {
						// remove existing color bar component (if there is one)
						mUpperRow.remove(mStnContourColorBarPanel);
						mStnContourColorBarPanel = null;
						mStnContourColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mStnCalcContourColorBar,
						    mOverlayLineColor);
						mStnContourColorBarPanel.setLinked(false);
						mStnContourColorBarPanel.setEnhanceable(false);
						mStnContourColorBarPanel.setBroadcastMode(false);
						mUpperRow.add(mStnContourColorBarPanel);
						mUpperRow.invalidate();
						this.validate();
					}
				}
				else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mWhiteContours) {
					mContourStyle = MapSpecification.WHITE_CONTOURS;
					mOverlayLineColor = Color.white;
					mOverlayContourColorSwatch.setEnabled(false);
					if (mStnContourColorBarPanel != null) {
						// remove existing color bar component (if there is one)
						mUpperRow.remove(mStnContourColorBarPanel);
						mStnContourColorBarPanel = null;
						mStnContourColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mStnCalcContourColorBar,
						    mOverlayLineColor);
						mStnContourColorBarPanel.setLinked(false);
						mStnContourColorBarPanel.setEnhanceable(false);
						mStnContourColorBarPanel.setBroadcastMode(false);
						mUpperRow.add(mStnContourColorBarPanel);
						mUpperRow.invalidate();
						this.validate();
					}
				}
				else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mCustomSingleColor) {
					mContourStyle = MapSpecification.CUSTOM_CONTOURS;
					mOverlayLineColor = mOverlayContourColorSwatch.getColor();
					if (mStnContourColorBarPanel != null) {
						// remove existing color bar component (if there is one)
						mUpperRow.remove(mStnContourColorBarPanel);
						mStnContourColorBarPanel = null;
						mStnContourColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mStnCalcContourColorBar,
						    mOverlayLineColor);
						mStnContourColorBarPanel.setLinked(false);
						mStnContourColorBarPanel.setEnhanceable(false);
						mStnContourColorBarPanel.setBroadcastMode(false);
						mUpperRow.add(mStnContourColorBarPanel);
						mUpperRow.invalidate();
						this.validate();
					}
				}
				// else if (evt.getStateChange() == ItemEvent.SELECTED && rb ==
				// mColorsFromColorBar) {
				// mContourStyle = MapSpecification.COLOR_CONTOURS;
				// mOverlayContourColorSwatch.setEnabled(false);
				// if (mColorBarPanel != null) {
				// // remove existing color bar component (if there is one)
				// mUpperRow.remove(mColorBarPanel);
				// mColorBarPanel = null;
				// mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar,
				// (Object) mColorBar);
				// mColorBarPanel.setLinked(false);
				// mColorBarPanel.setEnhanceable(false);
				// mColorBarPanel.setBroadcastMode(false);
				// mUpperRow.add(mColorBarPanel);
				// mUpperRow.invalidate();
				// this.validate();
				// }
				// }
			}
		}

		public void setColorBar(NewColorBar cb) {
			mStnCalcContourColorBar = cb;
		}

		public String getColorBarName() {
			return (String)mStnCalcContourColorbarList.getSelectedValue();
		}

		private void buildStnVarList() {
			Vector<String> params = new Vector<String>();
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
			Section sech = (Section)of.mSections.currElement();
			for (int i = 0; i < sech.getNumStnVars(); i++) {
				params.addElement(sech.getStnVar(i));
			}

			if (mStnVarList == null) {
				mStnVarList = new JOAJList(params);
			}
			else {
				mStnVarList.setListData(params);
				/*
				 * for (int i=0; i<mFileViewer.gNumProperties; i++) {
				 * mParamList.addItem(mFileViewer.mAllProperties[i].mVarLabel); } of =
				 * (OpenDataFile)mFileViewer.mOpenFiles.currElement(); sech =
				 * (Section)of.mSections.currElement(); for (int i=0;
				 * i<sech.getNumStnVars(); i++) { mParamList.addItem(sech.getStnVar(i));
				 * }
				 */
				mStnVarList.setSelectedIndex(mMapSpec.getStnCalcContourVarCode());
				mStnVarList.invalidate();
			}
		}

		public void disableAutoscaleStuff() {
			mLinear.setEnabled(false);
			mPowerUp.setEnabled(false);
			mPowerDown.setEnabled(false);
			mLogistic.setEnabled(false);
			mColorCombo.setEnabled(false);
			ccLbl6.setEnabled(false);
		}

		public void enableAutoscaleStuff() {
			mLinear.setEnabled(true);
			mPowerUp.setEnabled(true);
			mPowerDown.setEnabled(true);
			mLogistic.setEnabled(true);
			mColorCombo.setEnabled(true);
			ccLbl6.setEnabled(true);
		}

		public void maintainUI() {
			boolean c1 = mStnVarList.getSelectedIndex() >= 0;

			if (!c1) {
				disableAutoscaleStuff();
			}
			else {
				enableAutoscaleStuff();
			}

			boolean c2 = mStnCalcContourColorbarList.getSelectedIndex() >= 0 || mAutoScaleCreated;
			if (!c1 || !c2) {
				mUIState = false;
			}
			else {
				mUIState = true;
			}
		}

		public boolean isUIReady() {
			return mUIState;
		}

		public void dialogDismissed(JDialog d) {
		}

		// Cancel button
		public void dialogCancelled(JDialog d) {
		}

		// something other than the OK button
		public void dialogDismissedTwo(JDialog d) {
			;
		}

		// Apply button, OK w/o dismissing the dialog
		public void dialogApply(JDialog d) {
		}

		public void dialogApplyTwo(Object d) {
			// got a color change from a color swatch
			mCustomSingleColor.setSelected(true);
			mOverlayLineColor = mOverlayContourColorSwatch.getColor();
			if (mStnContourColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				mUpperRow.remove(mStnContourColorBarPanel);
				mStnContourColorBarPanel = null;
				mStnContourColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mStnCalcContourColorBar, mOverlayLineColor);
				mStnCalcContourColorBar = mStnContourColorBarPanel.getColorBar();
				mStnContourColorBarPanel.setLinked(false);
				mStnContourColorBarPanel.setEnhanceable(false);
				mStnContourColorBarPanel.setBroadcastMode(false);
				mUpperRow.add(mStnContourColorBarPanel);
				mUpperRow.invalidate();
				this.validate();
			}
		}
	}

	private class StationCalculationSymbols extends JPanel implements ActionListener, ListSelectionListener,
	    ItemListener, UIPanelMaintainer, DialogClient {
		private LargeIconButton mLinear = null;
		private LargeIconButton mPowerUp = null;
		private LargeIconButton mPowerDown = null;
		private LargeIconButton mLogistic = null;
		private ColorBarPanel mStnCalcColorBarPanel = null;
		private JOAJList mStnCalcColorbarList = null;
		private JOAJLabel ccLbl4 = null;
		private JOAJLabel ccLbl5 = null;
		private JOAJLabel ccLbl6 = null;
		private JPanel autoscalePanel2;
		private JOAJList mStnVarList = null;
		private int mAutoscaleColorScheme = 1;
		private JOAJComboBox mColorCombo = null;
		private int mSelParam = -1;
		private JPanel mUpperRow;;
		private FileViewer mFileViewer = null;
		private boolean mUIState = false;
		private boolean mAutoScaleCreated = false;
		private NewColorBar mStnCalcColorBar = null;

		public StationCalculationSymbols(FileViewer fv) {
			mFileViewer = fv;
			this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 5));
			mUpperRow = new JPanel(new RowLayout(Orientation.LEFT, Orientation.TOP, 5));

			// build the station var list
			JPanel stnVars = new JPanel();
			stnVars.setLayout(new BorderLayout(5, 5));
			ccLbl4 = new JOAJLabel(b.getString("kStationCalculation2"));
			stnVars.add(ccLbl4, "North");
			buildStnVarList();

			mStnVarList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mStnVarList.setPrototypeCellValue("Temperature  TemperatureTemperature");
			mStnVarList.setVisibleRowCount(8);
			JScrollPane listScroller23 = new JScrollPane(mStnVarList);
			mStnVarList.addListSelectionListener(this);
			stnVars.add(listScroller23, "Center");
			mUpperRow.add(stnVars);

			// now add a colorbar list
			JPanel stnVarColor = new JPanel();
			stnVarColor.setLayout(new BorderLayout(5, 5));
			buildStnVarCBList();

			mStnCalcColorbarList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mStnCalcColorbarList.setPrototypeCellValue("Temperature                          ");
			mStnCalcColorbarList.setVisibleRowCount(4);
			JScrollPane listScroller34 = new JScrollPane(mStnCalcColorbarList);
			mStnCalcColorbarList.addListSelectionListener(this);
			ccLbl5 = new JOAJLabel(b.getString("kColors:"));
			stnVarColor.add(ccLbl5, "North");
			stnVarColor.add(listScroller34, "Center");

			// panel for autoscaling goes here
			autoscalePanel2 = new JPanel();
			autoscalePanel2.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 10));
			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kCreateAutoscaleColorbar"));
			if (JOAConstants.ISMAC) {
				// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
			}
			autoscalePanel2.setBorder(tb);

			JPanel autoscaleBtnsPanel = new JPanel();
			autoscaleBtnsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			// line6.add(new JOAJLabel(b.getString("kBasedOn")));
			JPanel shapePanel = new JPanel();
			shapePanel.setLayout(new GridLayout(1, 4, 5, 0));
			mLinear = new LargeIconButton(new ImageIcon(getClass().getResource("images/linear.gif")));
			shapePanel.add(mLinear);
			mPowerUp = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerup.gif")));
			shapePanel.add(mPowerUp);
			mPowerDown = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerdown.gif")));
			shapePanel.add(mPowerDown);
			mLogistic = new LargeIconButton(new ImageIcon(getClass().getResource("images/logistic.gif")));
			shapePanel.add(mLogistic);
			mLinear.addActionListener(this);
			mPowerUp.addActionListener(this);
			mPowerDown.addActionListener(this);
			mLogistic.addActionListener(this);
			mLinear.setActionCommand("linear");
			mPowerUp.setActionCommand("powerUp");
			mPowerDown.setActionCommand("powerDown");
			mLogistic.setActionCommand("logistic");
			autoscaleBtnsPanel.add(shapePanel);
			autoscalePanel2.add(autoscaleBtnsPanel);

			mLinear.setToolTipText(b.getString("kLinearTip"));
			mPowerUp.setToolTipText(b.getString("kIncreasingExpTip"));
			mPowerDown.setToolTipText(b.getString("kDecreasingExpTip"));
			mLogistic.setToolTipText(b.getString("kReverseSTip"));

			JPanel autoscaleComboPanel = new JPanel();
			autoscaleComboPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			ccLbl6 = new JOAJLabel(b.getString("kUsing"));
			autoscaleComboPanel.add(ccLbl6);
			Vector<String> autoScaleChoices = new Vector<String>();
			autoScaleChoices.addElement(new String("Blue-White-Red-32"));
			autoScaleChoices.addElement(new String("Blue-White-Red-16"));
			autoScaleChoices.addElement(new String("Red-White-Blue-32"));
			autoScaleChoices.addElement(new String("Red-White-Blue-16"));
			autoScaleChoices.addElement(new String("Rainbow-32"));
			autoScaleChoices.addElement(new String("Rainbow-16"));
			autoScaleChoices.addElement(new String("Rainbow(inv)-32"));
			autoScaleChoices.addElement(new String("Rainbow(inv)-16"));
			mColorCombo = new JOAJComboBox(autoScaleChoices);
			mColorCombo.setSelectedIndex(JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME);
			mAutoscaleColorScheme = JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME + 1;
			mColorCombo.addItemListener(this);
			autoscaleComboPanel.add(mColorCombo);
			autoscalePanel2.add(autoscaleComboPanel);
			stnVarColor.add(autoscalePanel2, "South");
			mUpperRow.add(stnVarColor);

			// blank color bar panel
			mStnCalcColorBarPanel = new ColorBarPanel(mParent, mFileViewer, null);

			mStnCalcColorBarPanel.setLinked(false);
			mStnCalcColorBarPanel.setEnhanceable(false);
			mStnCalcColorBarPanel.setBroadcastMode(false);
			mUpperRow.add(mStnCalcColorBarPanel);
			this.add(mUpperRow);
		}
		

		public boolean isAutoScaledColor() {
			return mAutoScaleCreated;
		}

		private void buildStnVarCBList() {
			mStnCalcColorbarList = null;

			Vector<String> cbs = JOAFormulas.getColorBarList();
			Vector<String> mdcbs = new Vector<String>();

			for (int i = 0; i < cbs.size(); i++) {
				NewColorBar cb = null;

				String newCBARName = (String)cbs.elementAt(i);

				// read the color bar from disk
				try {
					cb = JOAFormulas.getColorBar(newCBARName);
				}
				catch (Exception ex) {
					cb = null;
				}

				if (!cb.isMetadataColorBar()) {
					mdcbs.addElement(new String(newCBARName));
				}
			}
			mStnCalcColorbarList = new JOAJList(mdcbs);
		}

		public NewColorBar getColorBar() {
			return mStnCalcColorBar;
		}

		public void updateColorBar() {
			// display the new color bar
			if (mStnCalcColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				mUpperRow.remove(mStnCalcColorBarPanel);
				mStnCalcColorBarPanel = null;
			}

			mStnCalcColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mStnCalcColorBar, null);

			mStnCalcColorBarPanel.setLinked(false);
			mStnCalcColorBarPanel.setEnhanceable(false);
			mStnCalcColorBarPanel.setBroadcastMode(false);
			mUpperRow.add(mStnCalcColorBarPanel);
			mUpperRow.invalidate();
			mUpperRow.validate();
			this.invalidate();
			this.validate();
			mAutoScaleCreated = true;
		}

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("linear")) {
				createColorBar(JOAConstants.LINEAR);
				updateColorBar();
				mLinear.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("powerUp")) {
				createColorBar(JOAConstants.EXPONENTIALUP);
				updateColorBar();
				mPowerUp.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("powerDown")) {
				createColorBar(JOAConstants.EXPONENTIALDOWN);
				updateColorBar();
				mPowerDown.setSelected(false);
				mAutoScaleCreated = true;
			}
			else if (cmd.equals("logistic")) {
				createColorBar(JOAConstants.LOGISTIC);
				updateColorBar();
				mLogistic.setSelected(false);
				mAutoScaleCreated = true;
			}
		}

		public void setSelectedCB(int i) {
			mStnCalcColorbarList.setSelectedIndex(i);
			mStnCalcColorbarList.ensureIndexIsVisible(i);
		}

		public void setSelectedVar(int varCode) {
			mStnVarList.setSelectedIndex(varCode);
			mStnVarList.ensureIndexIsVisible(varCode);
		}

		public int getStnCalcIndex() {
			return mStnVarList.getSelectedIndex();
		}

		public void createColorBar(int curveShape) {
			// get base and end levels from the selected parameter
			// have to compute the base level and end level from
			// running though the selected station variable

			double baseLevel = 99999999;
			double endLevel = -99999999;
			String mStnVarName = null;
			String mStnVarUnits = null;
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);

					if (sec == 0) {
						mStnVarName = sech.getStnVar(mSelParam);
						mStnVarUnits = sech.getStnVarUnits(mSelParam);
					}

					if (sech.mNumCasts == 0) {
						continue;
					}

					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);

						// get the station value
						double val = sh.getStnValue(mSelParam);
						if (val == JOAConstants.MISSINGVALUE) {
							continue;
						}
						baseLevel = val < baseLevel ? val : baseLevel;
						endLevel = val > endLevel ? val : endLevel;
					}
				}
			}

			double numLevels = 0;
			if (mAutoscaleColorScheme == 1 || mAutoscaleColorScheme == 3 || mAutoscaleColorScheme == 5
			    || mAutoscaleColorScheme == 7) {
				numLevels = 32;
			}
			else {
				numLevels = 16;
			}

			// compute new color bar values
			mColorBarValues = null;
			mColorBarValues = new double[(int)numLevels];
			mColorBarColors = new Color[(int)numLevels];
			if (curveShape == JOAConstants.LINEAR) {
				double increment = (endLevel - baseLevel) / (numLevels - 1);
				for (int i = 0; i < (int)numLevels; i++) {
					mColorBarValues[i] = baseLevel + (i * increment);
				}
			}
			else if (curveShape == JOAConstants.EXPONENTIALUP || curveShape == JOAConstants.EXPONENTIALDOWN) {
				double shape = JOAFormulas.getShape(baseLevel, endLevel);
				double scaledMax = Math.abs(endLevel - baseLevel);
				double lnScaledMin = Math.log(shape);
				double lnScaledMax = Math.log(scaledMax + shape);
				double increment = (lnScaledMax - lnScaledMin) / (numLevels - 1);

				for (int i = 0; i < (int)numLevels; i++) {
					if (curveShape == JOAConstants.EXPONENTIALUP) {
						// lower
						if (baseLevel < endLevel) {
							mColorBarValues[i] = baseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
						}
						else {
							mColorBarValues[i] = baseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
						}
					}
					else if (curveShape == JOAConstants.EXPONENTIALDOWN) {
						// upper
						if (baseLevel < endLevel) {
							mColorBarValues[(int)numLevels - i - 1] = endLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
						}
						else {
							mColorBarValues[(int)numLevels - i - 1] = endLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
						}
					}
				}
			}
			else if (curveShape == JOAConstants.LOGISTIC) {
				// logistic is a pieced together from upper and lower asymptote
				int mid = 0;
				int nl = (int)numLevels;
				if (nl % 2 > 0) {
					mid = (nl / 2) + 1;
				}
				else {
					mid = nl / 2;
				}

				// upper asymptote from base level to midpoint
				double newEndLevel = (baseLevel + endLevel) / 2;
				double shape = JOAFormulas.getShape(baseLevel, newEndLevel);
				double scaledMax = Math.abs(baseLevel - newEndLevel);
				double lnScaledMin = Math.log(shape);
				double lnScaledMax = Math.log(scaledMax + shape);
				double increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

				// lower
				for (int i = 0; i < mid; i++) {
					if (baseLevel < newEndLevel) {
						mColorBarValues[mid - i - 1] = newEndLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
					else {
						mColorBarValues[mid - i - 1] = newEndLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
				}

				// lower asymptote from midpoint to endlevel
				double newBaseLevel = newEndLevel;
				shape = JOAFormulas.getShape(newBaseLevel, endLevel);
				scaledMax = Math.abs(newBaseLevel - endLevel);
				lnScaledMin = Math.log(shape);
				lnScaledMax = Math.log(scaledMax + shape);
				increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

				// upper
				int endl = 0;
				if (nl % 2 > 0) {
					endl = mid - 1;
				}
				else {
					endl = mid;
				}
				for (int i = 0; i < endl; i++) {
					if (newBaseLevel < endLevel) {
						mColorBarValues[i + mid] = newBaseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
					else {
						mColorBarValues[i + mid] = newBaseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
				}

			}

			// assign colors to color bar
			Color startColor = Color.blue;
			Color midColor = Color.white;
			Color endColor = Color.red;
			if (mAutoscaleColorScheme <= 4) {
				// blend colors
				// get current colors
				if (mAutoscaleColorScheme <= 2) {
					startColor = Color.blue;
					midColor = Color.white;
					endColor = Color.red;
				}
				else {
					startColor = Color.red;
					midColor = Color.white;
					endColor = Color.blue;
				}

				int nl = (int)numLevels;
				int mid = 0;
				double deltaRed = 0;
				double deltaGreen = 0;
				double deltaBlue = 0;
				if (nl % 2 > 0) {
					// odd number of entries--middle color is middle color swatch
					mid = (nl / 2) + 1;
					mColorBarColors[mid - 1] = midColor;

					// blend from start to mid
					deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)mid;
					deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)mid;
					deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)mid;

					int c = 1;
					for (int i = 0; i < mid - 1; i++) {
						double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}

					// blend from mid to end
					deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)mid;
					deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)mid;
					deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)mid;

					c = 1;
					for (int i = mid; i < (int)numLevels; i++) {
						double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}
				}
				else {
					// even number of entries--middle color is in between middle values
					mid = nl / 2;

					// blend from start to mid
					deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)(mid + 1);
					deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)(mid + 1);
					deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)(mid + 1);

					int c = 1;
					for (int i = 0; i < mid; i++) {
						double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}

					// blend from mid to end
					deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)(mid + 1);
					deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)(mid + 1);
					deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)(mid + 1);

					c = 1;
					for (int i = mid; i < (int)numLevels; i++) {
						double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
						double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
						double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
						c++;
						mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
					}
				}
			}
			else {
				// create a rainbow
				float hue = 0;
				float sat = 1;
				float light = 1;
				float startHue = 0;
				if (mAutoscaleColorScheme < 7) {
					startHue = 0;
				}
				else {
					startHue = 270;
				}
				float hueAngleDelta = (float)270 / (float)numLevels;
				for (int i = 0; i < (int)numLevels; i++) {
					if (mAutoscaleColorScheme < 7) {
						hue = (startHue + ((float)i * hueAngleDelta)) / 360;
					}
					else {
						hue = (startHue - ((float)i * hueAngleDelta)) / 360;
					}
					mColorBarColors[i] = new Color(Color.HSBtoRGB(hue, sat, light));
					// System.out.println("h= " + (hue * 360) + " s= " + (sat * 100) + "
					// light= " + (light*100));
				}
			}

			// create a new NewColorBar
			String titleText = new String("Autoscale");
			String descripText = new String("Autoscale");
			if (mStnCalcColorBar == null) { 
				mStnCalcColorBar = new NewColorBar(mColorBarColors, mColorBarValues, (int)numLevels, mStnVarName, mStnVarUnits, titleText,
				    descripText);
			}
			else {
				// modify existing color bar
				mStnCalcColorBar.setValues(mColorBarValues);
				mStnCalcColorBar.setColors(mColorBarColors);
				mStnCalcColorBar.setBaseLevel(mColorBarValues[0]);
				mStnCalcColorBar.setEndLevel(mColorBarValues[(int)numLevels - 1]);
				mStnCalcColorBar.setTitle(titleText);
				mStnCalcColorBar.setParam(mStnVarName);
				mStnCalcColorBar.setParamUnits(mStnVarUnits);
				mStnCalcColorBar.setDescription(descripText);
				mStnCalcColorBar.setNumLevels((int)numLevels);
			}
		}

		public void disableStnVarStuff() {
			mStnCalcColorbarList.setEnabled(false);
			mStnVarList.setEnabled(false);
			ccLbl4.setEnabled(false);
			ccLbl5.setEnabled(false);
			mStnCalcColorbarList.clearSelection();
			mStnVarList.clearSelection();
		}

		public void enableStnVarStuff() {
			mStnCalcColorbarList.setEnabled(true);
			mStnVarList.setEnabled(true);
			ccLbl4.setEnabled(true);
			ccLbl5.setEnabled(true);
		}

		public void valueChanged(ListSelectionEvent evt) {
			JOAJList cb = (JOAJList)evt.getSource();

			if (cb == mStnCalcColorbarList) {
				// get a new NewColorBar
				String newCBARName = (String)cb.getSelectedValue();
				mStnCalcColorBar = null;

				// read the color bar from disk
				try {
					mStnCalcColorBar = JOAFormulas.getColorBar(newCBARName);
				}
				catch (Exception ex) {
					return;
				}

				// display the preview color bar
				if (mStnCalcColorBarPanel != null) {
					// remove existing color bar component (if there is one)
					mUpperRow.remove(mStnCalcColorBarPanel);
					mStnCalcColorBarPanel = null;
				}

				mStnCalcColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mStnCalcColorBar, null);
				mStnCalcColorBarPanel.setLinked(false);
				mStnCalcColorBarPanel.setEnhanceable(false);
				mStnCalcColorBarPanel.setBroadcastMode(false);
				mUpperRow.add(mStnCalcColorBarPanel);
				mUpperRow.invalidate();
				mUpperRow.validate();
				this.invalidate();
				this.validate();
				mAutoScaleCreated = false;
			}
			else if (cb == mStnVarList) {
				mSelParam = cb.getSelectedIndex();
				if (mSelParam < 0) { return; }
			}
		}

		public void itemStateChanged(ItemEvent evt) {
			if (evt.getSource() instanceof JOAJComboBox) {
				JOAJComboBox cb = (JOAJComboBox)evt.getSource();
				if (cb == mColorCombo) {
					mAutoscaleColorScheme = cb.getSelectedIndex() + 1;
				}
			}
		}

		public void setColorBar(NewColorBar cb) {
			mStnCalcColorBar = cb;
		}

		public String getColorBarName() {
			return (String)mStnCalcColorbarList.getSelectedValue();
		}

		private void buildStnVarList() {
			Vector<String> params = new Vector<String>();
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
			Section sech = (Section)of.mSections.currElement();
			for (int i = 0; i < sech.getNumStnVars(); i++) {
				params.addElement(sech.getStnVar(i));
			}

			if (mStnVarList == null) {
				mStnVarList = new JOAJList(params);
			}
			else {
				mStnVarList.setListData(params);
				/*
				 * for (int i=0; i<mFileViewer.gNumProperties; i++) {
				 * mParamList.addItem(mFileViewer.mAllProperties[i].mVarLabel); } of =
				 * (OpenDataFile)mFileViewer.mOpenFiles.currElement(); sech =
				 * (Section)of.mSections.currElement(); for (int i=0;
				 * i<sech.getNumStnVars(); i++) { mParamList.addItem(sech.getStnVar(i));
				 * }
				 */
				mStnVarList.setSelectedIndex(mMapSpec.getStnColorByStnValVarCode());
				mStnVarList.invalidate();
			}
		}

		public void disableAutoscaleStuff() {
			mLinear.setEnabled(false);
			mPowerUp.setEnabled(false);
			mPowerDown.setEnabled(false);
			mLogistic.setEnabled(false);
			mColorCombo.setEnabled(false);
			ccLbl6.setEnabled(false);
		}

		public void enableAutoscaleStuff() {
			mLinear.setEnabled(true);
			mPowerUp.setEnabled(true);
			mPowerDown.setEnabled(true);
			mLogistic.setEnabled(true);
			mColorCombo.setEnabled(true);
			ccLbl6.setEnabled(true);
		}

		public void maintainUI() {
			boolean c1 = mStnVarList.getSelectedIndex() >= 0;

			if (!c1) {
				disableAutoscaleStuff();
			}
			else {
				enableAutoscaleStuff();
			}

			boolean c2 = mStnCalcColorbarList.getSelectedIndex() >= 0 || mAutoScaleCreated;
			if (!c1 || !c2) {
				mUIState = false;
			}
			else {
				mUIState = true;
			}
		}

		public boolean isUIReady() {
			return mUIState;
		}

		public void dialogDismissed(JDialog d) {
		}

		// Cancel button
		public void dialogCancelled(JDialog d) {
		}

		// something other than the OK button
		public void dialogDismissedTwo(JDialog d) {
			;
		}

		// Apply button, OK w/o dismissing the dialog
		public void dialogApply(JDialog d) {
		}

		public void dialogApplyTwo(Object d) {
			// got a color change from a color swatch
			if (mStnCalcColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				mUpperRow.remove(mStnCalcColorBarPanel);
				mStnCalcColorBarPanel = null;
				mStnCalcColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mStnCalcColorBar, null);
				mStnCalcColorBar = mStnCalcColorBarPanel.getColorBar();
				mStnCalcColorBarPanel.setLinked(false);
				mStnCalcColorBarPanel.setEnhanceable(false);
				mStnCalcColorBarPanel.setBroadcastMode(false);
				mUpperRow.add(mStnCalcColorBarPanel);
				mUpperRow.invalidate();
				this.validate();
			}
		}
	}

	private class ContourStyleChooser extends JPanel {
		private JOASwatch mOverlayContourColorSwatch;
		private JOAJRadioButton mBlackContours = null;
		private JOAJRadioButton mWhiteContours = null;
		private JOAJRadioButton mCustomSingleColor = null;
		private Color mOverlayLineColor = Color.black;
		private JOAJTextField mNumSkipField = null;
		private int mContourStyle;

		public ContourStyleChooser(int style, Color cutomContColor, int numSkip) {
			mContourStyle = style;
			JPanel overlayStyleCont = new JPanel();
			this.setLayout(new BorderLayout(5, 0));
			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kStyle"));
			if (JOAConstants.ISMAC) {
			}
			this.setBorder(tb);

			overlayStyleCont.setLayout(new GridLayout(3, 1, 0, 0));
			mBlackContours = new JOAJRadioButton(b.getString("kBlackContours"), style == MapSpecification.BLACK_CONTOURS);
			mWhiteContours = new JOAJRadioButton(b.getString("kWhiteContours"), style == MapSpecification.WHITE_CONTOURS);
			mCustomSingleColor = new JOAJRadioButton(b.getString("kCustomSingleColor"),
			    style == MapSpecification.CUSTOM_CONTOURS);

			mOverlayContourColorSwatch = new JOASwatch(cutomContColor, null);
			mOverlayContourColorSwatch.setEnabled(false);
			JPanel custColorLine = new JPanel();
			custColorLine.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			custColorLine.add(mCustomSingleColor);
			custColorLine.add(mOverlayContourColorSwatch);
			overlayStyleCont.add(mBlackContours);
			overlayStyleCont.add(mWhiteContours);
			overlayStyleCont.add(custColorLine);
			ButtonGroup sbg = new ButtonGroup();
			sbg.add(mBlackContours);
			sbg.add(mWhiteContours);
			sbg.add(mCustomSingleColor);
			this.add("North", overlayStyleCont);

			JPanel line3a = new JPanel();
			line3a.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
			line3a.add(new JOAJLabel(b.getString("kPlotEvery")));
			mNumSkipField = new JOAJTextField(2);
			mNumSkipField.setText(String.valueOf(numSkip));
			mNumSkipField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			line3a.add(mNumSkipField);
			line3a.add(new JOAJLabel(b.getString("kContours")));
			this.add("South", line3a);
		}

		/**
		 * @param mOverlayContourColorSwatch
		 *          the mOverlayContourColorSwatch to set
		 */
		public void setCustomOverlayContourColor(Color c) {
			mOverlayContourColorSwatch.setColor(c);
		}

		/**
		 * @return the mOverlayContourColorSwatch
		 */
		public Color getCustomOverlayContourColor() {
			return mOverlayContourColorSwatch.getColor();
		}

		/**
		 * @param mOverlayLineColor
		 *          the mOverlayLineColor to set
		 */
		public void setOverlayLineColor(Color mOverlayLineColor) {
			this.mOverlayLineColor = mOverlayLineColor;
		}

		/**
		 * @return the mOverlayLineColor
		 */
		public Color getOverlayLineColor() {
			return mOverlayLineColor;
		}

		/**
		 * @param mNumSkipField
		 *          the mNumSkipField to set
		 */
		public void setNumSkipField(JOAJTextField mNumSkipField) {
			this.mNumSkipField = mNumSkipField;
		}

		/**
		 * @return the mNumSkipField
		 */
		public int getNumSkip() {
			return 1;
		}

		/**
		 * @param mContourStyle
		 *          the mContourStyle to set
		 */
		public void setContourStyle(int mContourStyle) {
			this.mContourStyle = mContourStyle;
		}

		/**
		 * @return the mContourStyle
		 */
		public int getContourStyle() {
			return mContourStyle;
		}
	}

}

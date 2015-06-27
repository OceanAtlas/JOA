/*
 * $Id: ConfigSimpleMapPlot.java,v 1.3 2005/06/17 18:08:52 oz Exp $
 *
 */

package javaoceanatlas.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javax.swing.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ConfigSimpleMapPlot extends JOAJDialog implements ActionListener, ItemListener {
	protected FileViewer mFileViewer = null;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mApplyButton = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJTextField leftLonFld = null;
	protected JOAJTextField topLatFld = null;
	protected JOAJTextField rightLonFld = null;
	protected JOAJTextField bottLatFld = null;
	protected JOAJTextField mCenterLonFld = null;
	protected JOAJTextField mCenterLatFld = null;
	protected MapSpecification mMapSpec = null;
	protected MapSpecification mOriginalMapSpec = null;
	protected JOAJCheckBox mapGraticule = null;
	protected JOAJCheckBox plotLabels = null;
	protected JOAJCheckBox lockRgn = null;
	protected JOAJTextField spacing = null;
	protected JOAJLabel mLatGratVal = null;
	protected JOAJLabel mLonGratVal = null;
	protected JOAJCheckBox connectStns = null;
	protected JOAJComboBox mSymbolPopup = null;
	protected int mCurrSymbol = JOAConstants.SYMBOL_SQUAREFILLED;
	protected Icon[] symbolData = null;
	protected JOAJTextField mSizeField = null;
	protected JOAJComboBox presetRegions = null;
	protected JOAJComboBox presetColorSchemes = null;
	protected Swatch mapBG = null;
	protected Swatch coastline = null;
	protected Swatch gratColor = null;
	protected DialogClient mClient = null;
	protected JOAJTextField mLatSpacing = null;
	protected JOAJTextField mLonSpacing = null;
	protected JSpinner mSymbolSizeSpinner = null;
	protected JDialog mFrame = null;
	protected int ignoreItemChange = 0;

	protected String mProjSpecificMapRegions[][] = {
	    { "World:160W Center", "World:120E Center", "World: 60W Center", "World:  0E Center", "North Pacific",
	        "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic", "Mediteranean",
	        "Indian" }, // Miller
	    { "World:30W Ctr. (Atlantic)", "World:60E Ctr. (Indian)", "World:90W Ctr. (Pacific)", "North Pacific",
	        "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic", "Mediteranean",
	        "Indian" }, // Orthographic
	    { "World:30W Ctr. (Atlantic)", "World:60E Ctr. (Indian)", "World:90W Ctr. (Pacific)", "North Pacific",
	        "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic", "Mediteranean",
	        "Indian" }, // Mollweide
	    { "World:30W Ctr. (Atlantic)", "World:60E Ctr. (Indian)", "World:90W Ctr. (Pacific)", "North Pacific",
	        "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic", "Mediteranean",
	        "Indian" }, // Lambert
	    { "World:30W Ctr. (Atlantic)", "World:60E Ctr. (Indian)", "World:90W Ctr. (Pacific)", "North Pacific",
	        "Central Pacific", "South Pacific", "North Atlantic", "Central Atlantic", "South Atlantic", "Mediteranean",
	        "Indian" }, // Stereo
	    { "North Pole" }, { "South Pole" }, // polar
	    { "World", "North Pacific", "South Pacific", "North Atlantic", "South Atlantic", "Arctic", "Indian" }, // GCS

	};

	protected double mProjRegionSpecificMinLats[][] = {
	// NP CP SP NA CA SA M I
	    { -90.0, -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0 }, // Miller
	    { -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0 }, // Orthographic
	    { -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0 }, // Mollweide
	    { -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0 }, // Lambert
	    { -90.0, -90.0, -90.0, -0.0, -70.0, -73.0, 0.0, -40.0, -73.0, 30.0, -71.0 }, // Stereo
	    { 0.0 }, // north pole
	    { -90.0 }, // south pole
	    { -90.0, 0.0, -90.0, 0.0, -90.0, -90, 0.0 }, // GCS
	};

	protected double mProjRegionSpecificMaxLats[][] = {
	// NP CP SP NA CA SA M I
	    { 90.0, 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0 }, // Miller
	    { 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0 }, // Orthographic
	    { 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0 }, // Mollweide
	    { 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0 }, // Lambert
	    { 90.0, 90.0, 90.0, 61.0, 50.0, 0.0, 73.0, 50.0, 0.0, 46.0, 26.0 }, // Stereo
	    { 90.0 }, // north pole
	    { 0.0 }, // south pole
	    { 90.0, 90.0, 0.0, 90.0, 0.0, 90, 30.0 }, // GCS
	};

	protected double mProjRegionSpecificLeftLons[][] = {
	// NP CP SP NA CA SA M I
	    { 20.0, -59.9, 120.0, -180.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0 }, // Miller
	    { -120.0, -30.0, 120.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0 }, // Orthographic
	    { -120.0, -30.0, 120.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0 }, // Mollweide
	    { -120.0, -30.0, 120.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0 }, // Lambert
	    { -120.0, -30.0, 120.0, 120.0, 120.0, 120.0, -100.0, -90.0, -70.0, -6.0, 19.0 }, // Stereo
	    { -180.0 }, // north pole
	    { -180.0 }, // south pole
	    { 20.0, 120.0, 120.0, -80.0, -80.0, 20, 20.0 }, // GCS
	};

	protected double mProjRegionSpecificRightLons[][] = {
	// NP CP SP NA CA SA M I
	    { 19.99, -60.0, 119.99, 180.0, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0 }, // Miller
	    { 60.0, 150.0, -60.0, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0 }, // Orthographic
	    { 60.0, 150.0, -60.0, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0 }, // Mollweide
	    { 60.0, 150.0, -60.0, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0 }, // Lambert
	    { 60.0, 150.0, -60.0, -80.0, -70.0, -60.0, 10.0, 20.0, 21.0, 37.0, 110.0 }, // Stereo
	    { 180.0 }, // north pole
	    { 180.0 }, // south pole
	    { 19.99, -80, -80, 20.0, 20.0, 120, 120.0 }, // GCS
	};

	public ConfigSimpleMapPlot(JOAWindow par, DialogClient client, MapSpecification spec) {
		super(par, "Configure Station Filter Map", false);
		mMapSpec = spec;
		mClient = client;

		// init the interface
		init();

		mFrame = this;
		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (mClient != null)
					mClient.dialogCancelled(mFrame);
			}
		};
		this.addWindowListener(windowListener);
	}

	@SuppressWarnings("deprecation")
  public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
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

		DefaultFocusManager.disableSwingFocusManager();

		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel(); // everything goes in here
		mainPanel.setLayout(new BorderLayout(5, 5));

		JPanel upperLeftPanel = new JPanel();
		upperLeftPanel.setLayout(new BorderLayout(5, 3)); // region

		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new RowLayout(Orientation.CENTER, Orientation.TOP, 5)); // upperleft,
																																									// upperright
																																									// goes
																																									// in
																																									// here

		// Region Panel
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
		setAvailableMapRegions();
		presetRegions.addItemListener(this);
		prRegionPanel.add(ll);
		prRegionPanel.add(presetRegions);

		// custom region
		JPanel crContPanel = new JPanel();
		JPanel crRegionPanel = new JPanel();
		crContPanel.setLayout(new BorderLayout(0, 5));
		crRegionPanel.setLayout(new GridLayout(3, 3));
		crRegionPanel.add(new JOAJLabel(" "));

		JPanel topLat = new JPanel();
		topLat.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		topLatFld = new JOAJTextField(5);
		topLatFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		topLat.add(topLatFld);
		topLat.add(new JOAJLabel("T"));
		crRegionPanel.add(topLat);
		crRegionPanel.add(new JOAJLabel(" "));
		topLatFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getLatMax()), 2, false));
		topLatFld.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JOAJTextField te = (JOAJTextField) evt.getSource();
				if (te == leftLonFld || te == rightLonFld)
					// recompute the center longitude
					setCenterLongitude();
				else
					// redo the center latitude
					setCenterLatitude();
			}
		});

		topLatFld.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent me) {
				JOAJTextField te = (JOAJTextField) me.getSource();
				if (te == leftLonFld || te == rightLonFld)
					// recompute the center longitude
					setCenterLongitude();
				else
					// redo the center latitude
					setCenterLatitude();
			}
		});

		JPanel leftLon = new JPanel();
		leftLon.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		leftLonFld = new JOAJTextField(6);
		leftLonFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		leftLon.add(leftLonFld);
		leftLon.add(new JOAJLabel("L"));
		crRegionPanel.add(leftLon);
		crRegionPanel.add(new JOAJLabel(" "));
		leftLonFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getLonLft()), 2, false));
		leftLonFld.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JOAJTextField te = (JOAJTextField) evt.getSource();
				if (te == leftLonFld || te == rightLonFld)
					// recompute the center longitude
					setCenterLongitude();
				else
					// redo the center latitude
					setCenterLatitude();
			}
		});

		leftLonFld.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent me) {
				JOAJTextField te = (JOAJTextField) me.getSource();
				if (te == leftLonFld || te == rightLonFld)
					// recompute the center longitude
					setCenterLongitude();
				else
					// redo the center latitude
					setCenterLatitude();
			}
		});

		JPanel rightLon = new JPanel();
		rightLon.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		rightLonFld = new JOAJTextField(6);
		rightLonFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		rightLon.add(rightLonFld);
		rightLon.add(new JOAJLabel("R"));
		crRegionPanel.add(rightLon);
		crRegionPanel.add(new JOAJLabel(" "));
		rightLonFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getLonRt()), 2, false));
		rightLonFld.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JOAJTextField te = (JOAJTextField) evt.getSource();
				if (te == leftLonFld || te == rightLonFld)
					// recompute the center longitude
					setCenterLongitude();
				else
					// redo the center latitude
					setCenterLatitude();
			}
		});

		rightLonFld.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent me) {
				JOAJTextField te = (JOAJTextField) me.getSource();
				if (te == leftLonFld || te == rightLonFld)
					// recompute the center longitude
					setCenterLongitude();
				else
					// redo the center latitude
					setCenterLatitude();
			}
		});

		JPanel bottLat = new JPanel();
		bottLat.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		bottLatFld = new JOAJTextField(5);
		bottLatFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		bottLat.add(bottLatFld);
		bottLat.add(new JOAJLabel("B"));
		bottLatFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getLatMin()), 2, false));
		bottLatFld.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JOAJTextField te = (JOAJTextField) evt.getSource();
				if (te == leftLonFld || te == rightLonFld)
					// recompute the center longitude
					setCenterLongitude();
				else
					// redo the center latitude
					setCenterLatitude();
			}
		});

		bottLatFld.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent me) {
				JOAJTextField te = (JOAJTextField) me.getSource();
				if (te == leftLonFld || te == rightLonFld)
					// recompute the center longitude
					setCenterLongitude();
				else
					// redo the center latitude
					setCenterLatitude();
			}
		});

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
		regionPanel.add("Center", prRegionPanel);
		regionPanel.add("South", crContPanel);
		upperLeftPanel.add("North", regionPanel);

		// Options
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 3));
		JPanel middleContPanel = new JPanel();
		middleContPanel.setLayout(new BorderLayout(0, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kOptions"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		middleContPanel.setBorder(tb);

		// connect station
		JPanel line1 = new JPanel();
		line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		connectStns = new JOAJCheckBox(b.getString("kConnectStations"), mMapSpec.isConnectStns());
		connectStns.addItemListener(this);
		line1.add(connectStns);

		// symbols
		line1.add(new JOAJLabel(b.getString("kSymbol")));
		mSymbolPopup = new JOAJComboBox();
		for (int i = 0; i < symbolData.length; i++)
			mSymbolPopup.addItem(symbolData[i]);
		mSymbolPopup.setSelectedIndex(mMapSpec.getSymbol() - 1);
		mSymbolPopup.addItemListener(this);
		line1.add(mSymbolPopup);

		// symbol size
		line1.add(new JOAJLabel(b.getString("kSize")));

		SpinnerNumberModel model = new SpinnerNumberModel(mMapSpec.getSymbolSize(), 1, 100, 1);
		mSymbolSizeSpinner = new JSpinner(model);

		mSymbolSizeSpinner.setEnabled(true);
		line1.add(mSymbolSizeSpinner);
		middlePanel.add(line1);

		// map graticule and spacing
		JPanel line22 = new JPanel();
		line22.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		mapGraticule = new JOAJCheckBox(b.getString("kMapGraticule"), mMapSpec.isDrawGraticule());
		mapGraticule.addItemListener(this);
		line22.add(mapGraticule);
		plotLabels = new JOAJCheckBox(b.getString("kPlotLabels"), mMapSpec.isPlotGratLabels());
		plotLabels.addItemListener(this);
		line22.add(plotLabels);
		middlePanel.add(line22);

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
		swatchCont.setLayout(new GridLayout(3, 1, 0, 5));
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
		middleContPanel.add(middlePanel);

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mCancelButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mOKBtn.addActionListener(this);
		mCancelButton.addActionListener(this);

		// build the upperPanel
		upperPanel.add(upperLeftPanel);

		// add all the sub panels to main panel
		mainPanel.add(new TenPixelBorder(upperPanel, 5, 5, 5, 5), "North");
		mainPanel.add(new TenPixelBorder(middleContPanel, 5, 5, 5, 5), "Center");
		mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", mainPanel);
		// mFileViewer.addParameterAddedListener(this);
		this.pack();

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);
	}

	public void setAvailableMapRegions() {
		Vector<String> presetData = new Vector<String>();
		int proj = mMapSpec.getProjection();
		int projIndex = proj > 4 ? proj - 6 : proj - 1;
		for (int i = 0; i < mProjSpecificMapRegions[projIndex].length; i++) {
			presetData.addElement(mProjSpecificMapRegions[projIndex][i]);
		}

		if (presetRegions != null) {
			presetRegions.removeAllItems();
			for (int i = 0; i < presetData.size(); i++)
				presetRegions.addItem(presetData.elementAt(i));
		}
		else
			presetRegions = new JOAJComboBox(presetData);
		presetRegions.setSelectedIndex(0);
		mMapSpec.setCurrBasin(0);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			// mClient.dialogCancelled(this);
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			// get the custom region
			String fldText = topLatFld.getText();
			mMapSpec.setLatMax(Double.valueOf(fldText).doubleValue());
			fldText = bottLatFld.getText();
			mMapSpec.setLatMin(Double.valueOf(fldText).doubleValue());
			fldText = leftLonFld.getText();
			mMapSpec.setLonLft(Double.valueOf(fldText).doubleValue());
			fldText = rightLonFld.getText();
			mMapSpec.setLonRt(Double.valueOf(fldText).doubleValue());

			// get the map center
			fldText = mCenterLatFld.getText();
			mMapSpec.setCenLat(Double.valueOf(fldText).doubleValue());
			fldText = mCenterLonFld.getText();
			mMapSpec.setCenLon(Double.valueOf(fldText).doubleValue());

			// get the custom colors
			mMapSpec.setGratColor(gratColor.getColor());
			mMapSpec.setBGColor(mapBG.getColor());
			mMapSpec.setCoastColor(coastline.getColor());

			// grat spacings
			fldText = mLatSpacing.getText();
			mMapSpec.setLatGratSpacing(Double.valueOf(fldText).intValue());
			fldText = mLonSpacing.getText();
			mMapSpec.setLonGratSpacing(Double.valueOf(fldText).intValue());

			// get the symbol size
			mMapSpec.setSymbolSize(((Integer) mSymbolSizeSpinner.getValue()).intValue());
			mMapSpec.setPlotStnSymbols(true);
			this.setVisible(false);
			mClient.dialogDismissed(this);
			this.dispose();
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (ignoreItemChange > 0) {
			ignoreItemChange--;
			return;
		}
		if (evt.getSource() instanceof JOAJComboBox) {
			JOAJComboBox cb = (JOAJComboBox) evt.getSource();
			if (cb == mSymbolPopup) {
				mMapSpec.setSymbol(cb.getSelectedIndex() + 1);
			}
			else if (cb == presetRegions) {
				mMapSpec.setCurrBasin(cb.getSelectedIndex());
				setToBasin();
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
		}
		else if (evt.getSource() instanceof JOAJCheckBox) {
			JOAJCheckBox cb = (JOAJCheckBox) evt.getSource();
			// other checkboxes
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == mapGraticule)
				mMapSpec.setDrawGraticule(true);
			if (evt.getStateChange() == ItemEvent.DESELECTED && cb == mapGraticule)
				mMapSpec.setDrawGraticule(false);
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == connectStns)
				mMapSpec.setConnectStns(true);
			if (evt.getStateChange() == ItemEvent.DESELECTED && cb == connectStns)
				mMapSpec.setConnectStns(false);
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == plotLabels)
				mMapSpec.setPlotGratLabels(true);
			if (evt.getStateChange() == ItemEvent.DESELECTED && cb == plotLabels)
				mMapSpec.setPlotGratLabels(false);
		}
		else if (evt.getSource() instanceof JOAJRadioButton) {
		}
	}

	public void setToBasin() {
		if (lockRgn.isSelected())
			return;
		int proj = mMapSpec.getProjection();
		int projIndex = proj > 4 ? proj - 6 : proj - 1;
		if (projIndex < 0 || mMapSpec.getCurrBasin() < 0)
			return;
		mMapSpec.setLatMin(mProjRegionSpecificMinLats[projIndex][mMapSpec.getCurrBasin()]);
		mMapSpec.setLatMax(mProjRegionSpecificMaxLats[projIndex][mMapSpec.getCurrBasin()]);
		mMapSpec.setLonLft(mProjRegionSpecificLeftLons[projIndex][mMapSpec.getCurrBasin()]);
		mMapSpec.setLonRt(mProjRegionSpecificRightLons[projIndex][mMapSpec.getCurrBasin()]);

		if (leftLonFld != null)
			leftLonFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getLonLft()), 2, false));
		if (topLatFld != null)
			topLatFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getLatMax()), 2, false));
		if (rightLonFld != null)
			rightLonFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getLonRt()), 2, false));
		if (bottLatFld != null)
			bottLatFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getLatMin()), 2, false));
		setMapCenter();
	}

	public void setCenterLongitude() {
		if (mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
			String fldText = leftLonFld.getText();
			mMapSpec.setLonLft(Double.valueOf(fldText).doubleValue());
			fldText = rightLonFld.getText();
			mMapSpec.setLonRt(Double.valueOf(fldText).doubleValue());
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
		mCenterLonFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getCenLon()), 2, false));
	}

	public void setCenterLatitude() {
		String fldText = topLatFld.getText();
		mMapSpec.setLatMax(Double.valueOf(fldText).doubleValue());
		fldText = bottLatFld.getText();
		mMapSpec.setLatMin(Double.valueOf(fldText).doubleValue());
		mMapSpec.setCenLat(mMapSpec.getLatMin() + (mMapSpec.getLatMax() - mMapSpec.getLatMin()) / 2.0);
		mCenterLatFld.setText(JOAFormulas.formatDouble(String.valueOf(mMapSpec.getCenLat()), 2, false));
	}

	public void setMapCenter() {
		setCenterLongitude();
		setCenterLatitude();
	}

	public MapSpecification getOrigMapSpec() {
		return mOriginalMapSpec;
	}

	public MapSpecification getMapSpec() {
		// get the custom region
		String fldText = topLatFld.getText();
		mMapSpec.setLatMax(Double.valueOf(fldText).doubleValue());
		fldText = bottLatFld.getText();
		mMapSpec.setLatMin(Double.valueOf(fldText).doubleValue());
		fldText = leftLonFld.getText();
		mMapSpec.setLonLft(Double.valueOf(fldText).doubleValue());
		fldText = rightLonFld.getText();
		mMapSpec.setLonRt(Double.valueOf(fldText).doubleValue());
		// setMapCenter();

		// get the map center
		fldText = mCenterLatFld.getText();
		mMapSpec.setCenLat(Double.valueOf(fldText).doubleValue());
		fldText = mCenterLonFld.getText();
		mMapSpec.setCenLon(Double.valueOf(fldText).doubleValue());

		// get the custom colors
		mMapSpec.setGratColor(gratColor.getColor());
		mMapSpec.setBGColor(mapBG.getColor());
		mMapSpec.setCoastColor(coastline.getColor());

		// grat spacings
		fldText = mLatSpacing.getText();
		mMapSpec.setLatGratSpacing(Double.valueOf(fldText).intValue());
		fldText = mLonSpacing.getText();
		mMapSpec.setLonGratSpacing(Double.valueOf(fldText).intValue());

		// get the symbol size
		mMapSpec.setSymbolSize(((Integer) mSymbolSizeSpinner.getValue()).intValue());
		mMapSpec.setPlotStnSymbols(true);

		return mMapSpec;
	}

	public boolean mapSpecChanged() {
		if (mMapSpec.getCurrBasin() != mOriginalMapSpec.getCurrBasin())
			return true;
		if (mMapSpec.getLatMax() != mOriginalMapSpec.getLatMax())
			return true;
		if (mMapSpec.getLatMin() != mOriginalMapSpec.getLatMin())
			return true;
		if (mMapSpec.getLonRt() != mOriginalMapSpec.getLonRt())
			return true;
		if (mMapSpec.getLonLft() != mOriginalMapSpec.getLonLft())
			return true;
		if (mMapSpec.getCenLat() != mOriginalMapSpec.getCenLat())
			return true;
		if (mMapSpec.getCenLon() != mOriginalMapSpec.getCenLon())
			return true;
		if (mMapSpec.isConnectStns() != mOriginalMapSpec.isConnectStns())
			return true;
		if (mMapSpec.isDrawGraticule() != mOriginalMapSpec.isDrawGraticule())
			return true;
		if (mMapSpec.isAutoGraticule() != mOriginalMapSpec.isAutoGraticule())
			return true;
		if (mMapSpec.getLatGratSpacing() != mOriginalMapSpec.getLatGratSpacing())
			return true;
		if (mMapSpec.getLonGratSpacing() != mOriginalMapSpec.getLonGratSpacing())
			return true;

		if (mMapSpec.getBGColor() != mOriginalMapSpec.getBGColor())
			return true;
		if (mMapSpec.getGratColor() != mOriginalMapSpec.getGratColor())
			return true;
		if (mMapSpec.getCoastColor() != mOriginalMapSpec.getCoastColor())
			return true;
		return false;
	}
}

/*
 * $Id: ConfigPreferences.java,v 1.14 2005/09/07 18:49:30 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.io.*;
import javax.swing.border.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
import javaoceanatlas.events.*;
import javaoceanatlas.io.CastIDRule;
import javaoceanatlas.io.CastNumberRule;
import javaoceanatlas.io.ConvertParameterNamesRule;
import javaoceanatlas.io.DepthConversionRule;
import javaoceanatlas.io.DestinationQCRule;
import javaoceanatlas.io.PreferPressureParameterRule;
import javaoceanatlas.io.QCConversionRule;
import javaoceanatlas.io.SectionIDRule;
import javaoceanatlas.io.TempConversionRule;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import gov.noaa.pmel.swing.*;
import javaoceanatlas.*;

@SuppressWarnings("serial")
public class ConfigPreferences extends JOAJDialog implements ActionListener, ItemListener {
	protected FileViewer mFileViewer = null;
	protected JTabbedPane mTabPane = null;
	protected GeneralPrefsPanel mGeneralPrefs = null;
	protected WOCEImportPrefsPanel mWOCEPrefs = null;
	protected CTDPrefsPanel mCTDPrefs = null;
	protected EnhancementPrefsPanel mEnhancePrefs = null;
	protected FontPrefsPanel mFontPrefs = null;
	protected ParamSubstitutionsPanel mParamSubs = null;
	protected WODSelectImportPrefsPanel mWODSelectPrefs = null;
//	protected BuiltInCalcsPrefsPanel mCalcsPrefs = null;
	private FeaturesPrefsPanel mFeaturesPrefs = null;
//	private DapperServerPrefsPanel mDapperPrefs;
	
	private SpinnerNumberModel mEnhancedCurrSymbolNumberModel;

	protected JOAJButton mOKBtn = null;
	protected JOAJButton mApplyBtn = null;
	protected JOAJButton mImportButton = null;
	protected JOAJComboBox mLineWidths = null;
	protected JOAJCheckBox mTranslateParams = null;
	protected JOAJCheckBox mPlotTitles = null;
	protected Swatch mMissingValColor = null;
	protected Swatch mPlotFrameColor = null;
	protected Swatch mPlotContentsColor = null;
	protected JOAJRadioButton mNoDecimation = null;
	protected JOAJRadioButton mConstantDecimation = null;
	protected JOAJRadioButton mStdLevelDecimation = null;
	protected JOAJRadioButton mCustomDecimation = null;
	protected JOAJCheckBox mConvertTemps = null;
	protected JOAJCheckBox mConvertMass = null;
	protected JOAJCheckBox mReplQB3 = null;
	protected JOAJCheckBox mReplQB4 = null;
	protected JOAJCheckBox mReplQB7 = null;
	protected JOAJCheckBox mReplQB8 = null;
	protected JOAJCheckBox mReplAllBQB4 = null;
	protected JOAJCheckBox mReplGasQB34 = null;
	protected JOAJCheckBox mTranslateWOCEToIGOSS = null;
	protected JOAJTextField mConstantField = null;
	protected JOAJComboBox mSurfaces = null;
	protected int mNumCustomRanges = 0;
	protected Triplet[] mCustomRanges = new Triplet[25];
	CustomDeciRanges mCustRanges = null;
	protected JOAJComboBox mColorCombo = null;
	protected JOAJComboBox mColorPaletteCombo = null;
	protected JOAJComboBox mSymbolPopup = null;
	protected JSpinner mSizeField = null;
	protected JSpinner mSizeField2 = null;
	public Icon[] symbolData = null;
	protected JOAJComboBox mGeographicFormats = null;
	protected JOAJComboBox mDateFormats = null;
	protected JOAJRadioButton mUseNewSymbol = null;
	protected JOAJRadioButton mEnlargeCurrSymbol = null;
	protected JOAJCheckBox mContrastColor = null;
	protected JOAJComboBox mSymbolPopup2 = null;
	protected int mCurrSymbol = JOAConstants.SYMBOL_SQUAREFILLED;
	ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	protected Swatch mContrastSwatch = null;
	protected JSpinner mEnhanceSize1;
	protected JSpinner mEnhanceSize2;
	protected FontSettingsPanel mFontSettings1;
	protected FontSettingsPanel mFontSettings2;
	protected FontSettingsPanel mFontSettings3;
	protected FontSettingsPanel mFontSettings4;
	protected FontSettingsPanel mFontSettings5;
	protected FontSettingsPanel mFontSettings6;
	protected FontSettingsPanel mFontSettings7;
	protected FontSettingsPanel mFontSettings8;
	protected FontSettingsPanel mFontSettings9;
	protected FontSettingsPanel mFontSettings10;
	protected JOAJComboBox mMasterFontList = null;
	protected JOAJComboBox presetColorSchemes = null;

//	// widgets for DB Prefs ;
//	protected JTextField dbURI = null;
//	protected JTextField dbPort = null;
//	protected JTextField dbUserName = null;
//	protected JPasswordField dbUserPW = null;
//	protected JTextField dbDefaultDir = null;
	private JCheckBox mThickContours;
	private JCheckBox mThickOverlayContours;

	// widgets for built-in calcs prefs
	protected JCheckBox mApplyToUserCalcs = null;
	protected JCheckBox mConvertDepth = null;
	protected JCheckBox mSetDebugMode = null;

	public ConfigPreferences(JFrame par) {
		super(par, "Configure Preferences", false);

		// init the interface
		init();
	}

	public ConfigPreferences(JOAWindow par, FileViewer fv) {
		super(par, "Configure Preferences", false);
		mFileViewer = fv;

		// init the interface
		init();
	}

	public ConfigPreferences(JOAWindow par) {
		super(par, "Configure Preferences", false);

		// init the interface
		init();
	}

	public void init() {
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

		mTabPane = new JTabbedPane();
		mGeneralPrefs = new GeneralPrefsPanel();
		mWOCEPrefs = new WOCEImportPrefsPanel();
		mCTDPrefs = new CTDPrefsPanel();
		mEnhancePrefs = new EnhancementPrefsPanel();
		mFontPrefs = new FontPrefsPanel();
		mParamSubs = new ParamSubstitutionsPanel();
		mWODSelectPrefs = new WODSelectImportPrefsPanel();
//		mCalcsPrefs = new BuiltInCalcsPrefsPanel();
		mFeaturesPrefs = new FeaturesPrefsPanel();
//		mDapperPrefs = new DapperServerPrefsPanel();
		MyScroller prefScroller = new MyScroller();
		prefScroller.setViewportView(mFeaturesPrefs);

		FeatureGroup fg = JOAConstants.JOA_FEATURESET.get("kJOAPreferences");
		int numIstalledFeatures = 0;
		if (fg != null && fg.hasFeature("kGeneral") && fg.isFeatureEnabled("kGeneral")) {
			mTabPane.addTab(b.getString("kGeneral"), mGeneralPrefs);
			numIstalledFeatures++;
		}
		if (fg != null && fg.hasFeature("kFonts") && fg.isFeatureEnabled("kFonts")) {
			mTabPane.addTab(b.getString("kFonts"), mFontPrefs);
			numIstalledFeatures++;
		}
		if (fg != null && fg.hasFeature("kEnhancementOptions") && fg.isFeatureEnabled("kEnhancementOptions")) {
			mTabPane.addTab(b.getString("kEnhancementOptions"), mEnhancePrefs);
			numIstalledFeatures++;
		}
		if (fg != null && fg.hasFeature("kImport") && fg.isFeatureEnabled("kImport")) {
			mTabPane.addTab(b.getString("kImport"), mWOCEPrefs);
			numIstalledFeatures++;
		}
		if (fg != null && fg.hasFeature("kWODSelect") && fg.isFeatureEnabled("kWODSelect")) {
			mTabPane.addTab(b.getString("kWODSelect"), mWODSelectPrefs);
			numIstalledFeatures++;
		}
		if (fg != null && fg.hasFeature("kCTDDecimation") && fg.isFeatureEnabled("kCTDDecimation")) {
			mTabPane.addTab(b.getString("kCTDDecimation"), mCTDPrefs);
			numIstalledFeatures++;
		}
		if (fg != null && fg.hasFeature("kParameterSubstitutions") && fg.isFeatureEnabled("kParameterSubstitutions")) {
			mTabPane.addTab(b.getString("kParameterSubstitutions"), mParamSubs);
			numIstalledFeatures++;
		}
//		if (fg != null && fg.hasFeature("kDatabaseCalculations") && fg.isFeatureEnabled("kDatabaseCalculations")) {
//			mTabPane.addTab(b.getString("kDatabaseCalculations"), mCalcsPrefs);
//			numIstalledFeatures++;
//		}
		if (fg != null && fg.hasFeature("kFeatureManagement") && fg.isFeatureEnabled("kFeatureManagement")) {
			mTabPane.addTab(b.getString("kFeatureManagement"), prefScroller);
			numIstalledFeatures++;
		}
		
//		fg = JOAConstants.JOA_FEATURESET.get("kFile");
//		if (fg != null && fg.hasFeature("kOpenDapper") && fg.isFeatureEnabled("kOpenDapper")) {
//			mTabPane.addTab(b.getString("kDapperPrefs"), mDapperPrefs);
//			numIstalledFeatures++;
//		}
		mTabPane.setSize(new Dimension(800, 400));
		mTabPane.setSelectedIndex(0);

		// lower panel
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel(); // everything goes in here
		mainPanel.setLayout(new BorderLayout(5, 5));
		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mApplyBtn = new JOAJButton(b.getString("kApply"));
		mApplyBtn.setActionCommand("apply");
		mImportButton = new JOAJButton("Import JOAPrefs.xml...");
		mImportButton.setActionCommand("import");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mImportButton);
			dlgBtnsPanel.add(mApplyBtn);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mApplyBtn);
			dlgBtnsPanel.add(mImportButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mOKBtn.addActionListener(this);
		mApplyBtn.addActionListener(this);
		mImportButton.addActionListener(this);

		// add all the sub panels to main panel
		mainPanel.add(new TenPixelBorder(mTabPane, 5, 5, 5, 5), "West");
		mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", mainPanel);
		this.pack();

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);
	}

	// public static Triplet[] DEFAULT_CUSTOM_DECIMATE_TRIPLETS = new Triplet[25];
	// public static int DEFAULT_NUMBER_OF_CUSTOM_TRIPLETS = 0;

	public void setDefaults() {
		// apply UI settings to current preferences
		JOAConstants.THICKEN_CONTOUR_LINES = mThickContours.isSelected();
		JOAConstants.THICKEN_OVERLAY_CONTOUR_LINES = mThickOverlayContours.isSelected();
    JOAConstants.CONNECT_LINE_WIDTH = mLineWidths.getSelectedIndex() + 1;
    JOAConstants.DEFAULT_FRAME_COLOR = mPlotFrameColor.getColor();
    JOAConstants.DEFAULT_CONTENTS_COLOR = mPlotContentsColor.getColor();
    JOAConstants.DEFAULT_MISSINGVAL_COLOR = mMissingValColor.getColor();
    JOAConstants.DEFAULT_TRANSLATE_PARAM_NAMES = mTranslateParams.isSelected();
    JOAConstants.DEFAULT_PLOT_TITLES = mPlotTitles.isSelected();
    JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME = mColorCombo.getSelectedIndex();
    JOAConstants.DEFAULT_CURSOR_SYMBOL = mSymbolPopup.getSelectedIndex() + 1;
    JOAConstants.DEFAULT_POSITION_FORMAT = mGeographicFormats.getSelectedIndex();
    JOAConstants.DEFAULT_DATE_FORMAT = (String)mDateFormats.getSelectedItem();
    JOAConstants.DEFAULT_LINE_PLOT_PALETTE = mColorPaletteCombo.getSelectedItem().toString();

    // get the browsing cursor size
    JOAConstants.DEFAULT_CURSOR_SIZE = ((Integer)mSizeField2.getValue()).intValue();

    if (mConstantDecimation.isSelected()) {
      JOAConstants.DEFAULT_NO_CTD_DECIMATION = false;
      JOAConstants.DEFAULT_CONSTANT_CTD_DECIMATION = true;
      JOAConstants.DEFAULT_STD_LEVEL_CTD_DECIMATION = false;

      // get the constant increment
      double inc = 5.0;
      try {
        inc = Double.valueOf(mConstantField.getText()).doubleValue();
      }
      catch (NumberFormatException ex) {
      }
      JOAConstants.DEFAULT_DECIMATE_CONSTANT = inc;
    }
    else if (mStdLevelDecimation.isSelected()) {
      JOAConstants.DEFAULT_NO_CTD_DECIMATION = false;
      JOAConstants.DEFAULT_CONSTANT_CTD_DECIMATION = false;
      JOAConstants.DEFAULT_STD_LEVEL_CTD_DECIMATION = true;
      JOAConstants.DEFAULT_CUSTOM_CTD_DECIMATION = false;

      // get the standard level selected
      JOAConstants.DEFAULT_DECIMATE_STD_LEVEL = (String)mSurfaces.getSelectedItem();
    }
    else if (mCustomDecimation.isSelected()) {
      JOAConstants.DEFAULT_NO_CTD_DECIMATION = false;
      JOAConstants.DEFAULT_CONSTANT_CTD_DECIMATION = false;
      JOAConstants.DEFAULT_STD_LEVEL_CTD_DECIMATION = false;
      JOAConstants.DEFAULT_CUSTOM_CTD_DECIMATION = true;

      // get custom decimation array
      mCustomRanges = mCustRanges.getDecimations();
      mNumCustomRanges = 0;
      for (int i = 0; i < 25; i++) {
        if (mCustomRanges[i].getVal1() != JOAConstants.MISSINGVALUE &&
            mCustomRanges[i].getVal2() != JOAConstants.MISSINGVALUE &&
            mCustomRanges[i].getVal3() != JOAConstants.MISSINGVALUE) {
          JOAConstants.DEFAULT_CUSTOM_DECIMATE_TRIPLETS[mNumCustomRanges] = mCustomRanges[i];
          mNumCustomRanges++;
        }
      }
      JOAConstants.DEFAULT_NUMBER_OF_CUSTOM_TRIPLETS = mNumCustomRanges;
    }
    else {
      JOAConstants.DEFAULT_NO_CTD_DECIMATION = true;
    }

    JOAConstants.DEFAULT_CONVERT_WOCE_TEMPS = mConvertTemps.isSelected();
    JOAConstants.DEFAULT_CONVERT_QCS = mTranslateWOCEToIGOSS.isSelected();
    JOAConstants.DEFAULT_CONVERT_MASS_TO_VOL = mConvertMass.isSelected();
    JOAConstants.DEFAULT_SET_MSG_QBEQ3 = mReplQB3.isSelected();
    JOAConstants.DEFAULT_SET_MSG_QBEQ4 = mReplQB4.isSelected();
    JOAConstants.DEFAULT_SET_MSG_QBEQ7 = mReplQB7.isSelected();
    JOAConstants.DEFAULT_SET_MSG_QBEQ8 = mReplQB8.isSelected();
    JOAConstants.DEFAULT_SET_ALL_PARAMS_MSG_BQBEQ4 = mReplAllBQB4.isSelected();
    JOAConstants.DEFAULT_SET_GAS_PARAMS_MSG_BQBEQ3_AND_O2QBEQ4 = mReplGasQB34.isSelected();

    // get the enhancement prefs
    JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL = mEnlargeCurrSymbol.isSelected();
    JOAConstants.DEFAULT_ENHANCE_REPLACE_CURRENT_SYMBOL = mUseNewSymbol.isSelected();
    JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL_BY = ((Double)mEnhanceSize1.getValue()).doubleValue() / 100.0;
    JOAConstants.DEFAULT_ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY = ((Double)mEnhanceSize2.getValue()).doubleValue() /
        100.0;
    JOAConstants.DEFAULT_ENHANCE_USE_CONTRASTING_COLOR = mContrastColor.isSelected();
    JOAConstants.DEFAULT_ENHANCE_CONTRASTING_COLOR = mContrastSwatch.getColor();
    JOAConstants.DEFAULT_ENHANCE_CONTRASTING_SYMBOL = mSymbolPopup2.getSelectedIndex();

    // style changes
    JOAConstants.DEFAULT_AXIS_VALUE_FONT = new String(mFontSettings1.getFontName());
    JOAConstants.DEFAULT_AXIS_LABEL_FONT = new String(mFontSettings2.getFontName());
    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_FONT = new String(mFontSettings3.getFontName());
    JOAConstants.DEFAULT_PLOT_TITLE_FONT = new String(mFontSettings4.getFontName());
    JOAConstants.DEFAULT_COLORBAR_LABEL_FONT = new String(mFontSettings5.getFontName());
    JOAConstants.DEFAULT_MAP_VALUE_FONT = new String(mFontSettings6.getFontName());
    JOAConstants.DEFAULT_MAP_STN_LABEL_FONT = new String(mFontSettings7.getFontName());
    JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_FONT = new String(mFontSettings8.getFontName());
    JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_FONT = new String(mFontSettings9.getFontName());
    JOAConstants.DEFAULT_REGRESSION_FONT = new String(mFontSettings10.getFontName());

    JOAConstants.DEFAULT_AXIS_VALUE_SIZE = mFontSettings1.getFontSize();
    JOAConstants.DEFAULT_AXIS_LABEL_SIZE = mFontSettings2.getFontSize();
    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE = mFontSettings3.getFontSize();
    JOAConstants.DEFAULT_PLOT_TITLE_SIZE = mFontSettings4.getFontSize();
    JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE = mFontSettings5.getFontSize();
    JOAConstants.DEFAULT_MAP_VALUE_SIZE = mFontSettings6.getFontSize();
    JOAConstants.DEFAULT_MAP_STN_LABEL_SIZE = mFontSettings7.getFontSize();
    JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_SIZE = mFontSettings8.getFontSize();
    JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_SIZE = mFontSettings9.getFontSize();
    JOAConstants.DEFAULT_REGRESSION_FONT_SIZE = mFontSettings10.getFontSize();

    JOAConstants.DEFAULT_AXIS_VALUE_STYLE = mFontSettings1.getFontStyle();
    JOAConstants.DEFAULT_AXIS_LABEL_STYLE = mFontSettings2.getFontStyle();
    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE = mFontSettings3.getFontStyle();
    JOAConstants.DEFAULT_PLOT_TITLE_STYLE = mFontSettings4.getFontStyle();
    JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE = mFontSettings5.getFontStyle();
    JOAConstants.DEFAULT_MAP_VALUE_STYLE = mFontSettings6.getFontStyle();
    JOAConstants.DEFAULT_MAP_STN_LABEL_STYLE = mFontSettings7.getFontStyle();
    JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_STYLE = mFontSettings8.getFontStyle();
    JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_STYLE = mFontSettings9.getFontStyle();
    JOAConstants.DEFAULT_REGRESSION_FONT_STYLE = mFontSettings10.getFontStyle();

    Color c1 = mFontSettings1.getColor();
    Color c2 = mFontSettings2.getColor();
    Color c3 = mFontSettings3.getColor();
    Color c4 = mFontSettings4.getColor();
    Color c5 = mFontSettings5.getColor();
    Color c6 = mFontSettings6.getColor();
    Color c7 = mFontSettings7.getColor();
    Color c8 = mFontSettings8.getColor();
    Color c9 = mFontSettings9.getColor();
    Color c10 = mFontSettings10.getColor();
    JOAConstants.DEFAULT_AXIS_VALUE_COLOR = new Color(c1.getRed(), c1.getGreen(), c1.getBlue());
    JOAConstants.DEFAULT_AXIS_LABEL_COLOR = new Color(c2.getRed(), c2.getGreen(), c2.getBlue());
    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR = new Color(c3.getRed(), c3.getGreen(), c3.getBlue());
    JOAConstants.DEFAULT_PLOT_TITLE_COLOR = new Color(c4.getRed(), c4.getGreen(), c4.getBlue());
    JOAConstants.DEFAULT_COLORBAR_LABEL_COLOR = new Color(c5.getRed(), c5.getGreen(), c5.getBlue());
    JOAConstants.DEFAULT_MAP_VALUE_COLOR = new Color(c6.getRed(), c6.getGreen(), c6.getBlue());
    JOAConstants.DEFAULT_MAP_STN_LABEL_COLOR = new Color(c7.getRed(), c7.getGreen(), c7.getBlue());
    JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_COLOR = new Color(c8.getRed(), c8.getGreen(), c8.getBlue());
    JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_COLOR = new Color(c9.getRed(), c9.getGreen(), c9.getBlue());
    JOAConstants.DEFAULT_REGRESSION_FONT_COLOR = new Color(c10.getRed(), c10.getGreen(), c10.getBlue());

    JOAConstants.DEFAULT_SALINITY_VARIABLE = mParamSubs.getSalinityDefault();
    JOAConstants.DEFAULT_SALINITY_SUBSTITUTION = mParamSubs.getSalinitySub();
    JOAConstants.DEFAULT_O2_VARIABLE = mParamSubs.getO2Default();
    JOAConstants.DEFAULT_O2_SUBSTITUTION = mParamSubs.getO2Sub();
    JOAConstants.DEFAULT_CONVERT_DEPTH = mConvertDepth.isSelected();
    
    mFeaturesPrefs.updateFeaturePreferences() ;
    
    JOAConstants.DEFAULT_CAST_ID_RULE = mWODSelectPrefs.getCastIDRule();
    JOAConstants.DEFAULT_CAST_NUMBER_RULE = mWODSelectPrefs.getCastNumberRule();
    JOAConstants.DEFAULT_SECTION_ID_RULE = mWODSelectPrefs.getSectionIDRule();
    JOAConstants.DEFAULT_DEPTH_CONVERSION_RULE = mWODSelectPrefs.getDepthConversionRule();
    JOAConstants.DEFAULT_PRES_PARAM_RULE = mWODSelectPrefs.getPreferPressureParameterRule();
    JOAConstants.DEFAULT_TEMP_CONV_RULE = mWODSelectPrefs.getTempConversionRule();
    JOAConstants.DEFAULT_DEST_QC_RULE = mWODSelectPrefs.getDestinationQCRule();
    JOAConstants.DEFAULT_CONVERT_PARAM_NAMES_RULE = mWODSelectPrefs.getConvertParameterNamesRule();
    JOAConstants.DEFAULT_COLLECT_METADATA_RULE = mWODSelectPrefs.isCollectMetadata();
    JOAConstants.DEFAULT_QC_PROCESSING_RULE = mWODSelectPrefs.getQCConversionRule();

    // broadcast an event to cause various plots to redraw
    if (PowerOceanAtlas.getInstance() != null) {
      PrefsChangedEvent pce = new PrefsChangedEvent(PowerOceanAtlas.getInstance());
      Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
    }
  }

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("apply")) {
			setDefaults();
		}
		else if (cmd.equals("ok")) {
			setDefaults();
			try {
	      PowerOceanAtlas.getInstance().localSync("JOA Preferences", savePrefs());
      }
      catch (BackingStoreException e1) {
	      // TODO Auto-generated catch block
	      e1.printStackTrace();
      }
			this.dispose();
		}
		else if (cmd.equals("import")) {
			importPrefsFromFile();
			setDefaults();
		}
	}

	public void itemStateChanged(ItemEvent evt) {
	}

	public void importPrefsFromFile() {
		// get a filename
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("xml")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		Frame fr = new Frame();
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		FileDialog f = new FileDialog(fr, "Import preferences:", FileDialog.LOAD);
		f.setDirectory(directory);
		f.setFilenameFilter(filter);
		f.setFile("joaprefs.xml");
		f.setVisible(true);
		directory = f.getDirectory();
		f.dispose();
		if (directory != null && f.getFile() != null) {
			File nf = new File(directory, f.getFile());
			FileInputStream in;
      try {
	      in = new FileInputStream(nf);
	      byte[] ba = new byte[1000000];
	      in.read(ba);
	      String prefsAsXML = new String(ba);
	      JOAFormulas.readPreferences(prefsAsXML);
      }
      catch (Exception e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		}

		if (mGeneralPrefs != null) {
			mGeneralPrefs.refreshUI();
		}
		if (mWOCEPrefs != null) {
			mWOCEPrefs.refreshUI();
		}
		if (mCTDPrefs != null) {
			mCTDPrefs.refreshUI();
		}
		if (mEnhancePrefs != null) {
			mEnhancePrefs.refreshUI();
		}
		if (mFontPrefs != null) {
			mFontPrefs.refreshUI();
		}
		if (mParamSubs != null) {
			mParamSubs.refreshUI();
		}
		if (mWODSelectPrefs != null) {
			mWODSelectPrefs.refreshUI();
		}
		if (mFeaturesPrefs != null) {
			mFeaturesPrefs.refreshUI();
		}
		
		this.setSize(this.getWidth() + 1, this.getHeight());
//		this.setSize(this.getWidth() - 1, this.getHeight());
	}

	@SuppressWarnings("unchecked")
	public String savePrefs() {
		// save preferences as XML
		try {
			// create a document object
			Document doc = (Document) Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

			// make joapreferences the root element
			Element root = doc.createElement("joapreferences");

			// make the linewidth element and add it
			Element item = doc.createElement("connectlinewidth");
			item.appendChild(doc.createTextNode(String.valueOf(mLineWidths.getSelectedIndex() + 1)));
			root.appendChild(item);

			// make the cursor size element and add it
			// get the browsing cursor size
			int size = ((Integer) mSizeField2.getValue()).intValue();
			item = doc.createElement("browsingcursorsize");
			item.appendChild(doc.createTextNode(String.valueOf(size)));
			root.appendChild(item);

			// make the cursor symbol element and add it
			item = doc.createElement("browsingcursorsymbol");
			item.appendChild(doc.createTextNode(String.valueOf(mSymbolPopup.getSelectedIndex() + 1)));
			root.appendChild(item);

			// position and date formats
			item = doc.createElement("positionformat");
			item.appendChild(doc.createTextNode(String.valueOf(mGeographicFormats.getSelectedIndex() + 1)));
			root.appendChild(item);
			item = doc.createElement("dateformat");
			item.appendChild(doc.createTextNode((String) mDateFormats.getSelectedItem()));
			root.appendChild(item);

			item = doc.createElement("plottitles");
			item.appendChild(doc.createTextNode(String.valueOf(mPlotTitles.isSelected())));
			root.appendChild(item);

			// default autoscale colorbar
			item = doc.createElement("defaultautoscalecolors");
			item.appendChild(doc.createTextNode(String.valueOf(mColorCombo.getSelectedIndex())));
			root.appendChild(item);

			// default autoscale colorbar
			item = doc.createElement("lineplotcolorpalette");
			item.appendChild(doc.createTextNode((String)mColorPaletteCombo.getSelectedItem()));
			root.appendChild(item);

			// make the framecolor element and add it
			item = doc.createElement("framecolor");
			item.setAttribute("red", String.valueOf(mPlotFrameColor.getColor().getRed()));
			item.setAttribute("green", String.valueOf(mPlotFrameColor.getColor().getGreen()));
			item.setAttribute("blue", String.valueOf(mPlotFrameColor.getColor().getBlue()));
			root.appendChild(item);

			// make the contentcolor element and add it
			item = doc.createElement("contentcolor");
			item.setAttribute("red", String.valueOf(mPlotContentsColor.getColor().getRed()));
			item.setAttribute("green", String.valueOf(mPlotContentsColor.getColor().getGreen()));
			item.setAttribute("blue", String.valueOf(mPlotContentsColor.getColor().getBlue()));
			root.appendChild(item);

			// make the missingvalcolor element and add it
			item = doc.createElement("missingvalcolor");
			item.setAttribute("red", String.valueOf(mMissingValColor.getColor().getRed()));
			item.setAttribute("green", String.valueOf(mMissingValColor.getColor().getGreen()));
			item.setAttribute("blue", String.valueOf(mMissingValColor.getColor().getBlue()));
			root.appendChild(item);

			// add a decimation tag if any decimation is active
			if (!mNoDecimation.isSelected()) {
				Element decItem = doc.createElement("decimation");

				if (mConstantDecimation.isSelected()) {
					item = doc.createElement("constantdecimation");

					// get the constant increment
					double inc = 5.0;
					try {
						inc = Double.valueOf(mConstantField.getText()).doubleValue();
					}
					catch (NumberFormatException ex) {
					}
					item.appendChild(doc.createTextNode(String.valueOf(inc)));
					decItem.appendChild(item);
				}
				else if (mStdLevelDecimation.isSelected()) {
					item = doc.createElement("stdleveldecimation");
					item.appendChild(doc.createTextNode((String) mSurfaces.getSelectedItem()));
					decItem.appendChild(item);
				}
				else if (mCustomDecimation.isSelected()) {
					// get custom decimation array
					mCustomRanges = mCustRanges.getDecimations();
					mNumCustomRanges = 0;
					Triplet[] tempRanges = new Triplet[25];
					for (int i = 0; i < 25; i++) {
						if (mCustomRanges[i].getVal1() != JOAConstants.MISSINGVALUE
						    && mCustomRanges[i].getVal2() != JOAConstants.MISSINGVALUE
						    && mCustomRanges[i].getVal3() != JOAConstants.MISSINGVALUE) {
							tempRanges[mNumCustomRanges] = mCustomRanges[i];
							mNumCustomRanges++;
						}
					}

					item = doc.createElement("customdecimation");
					for (int i = 0; i < mNumCustomRanges; i++) {
						Triplet vals = tempRanges[i];
						Element custItem = doc.createElement("minvalue");
						custItem.appendChild(doc.createTextNode(String.valueOf(vals.getVal1())));
						item.appendChild(custItem);
						custItem = doc.createElement("maxvalue");
						custItem.appendChild(doc.createTextNode(String.valueOf(vals.getVal2())));
						item.appendChild(custItem);
						custItem = doc.createElement("increment");
						custItem.appendChild(doc.createTextNode(String.valueOf(vals.getVal3())));
						item.appendChild(custItem);
					}
					decItem.appendChild(item);
				}

				root.appendChild(decItem);
			}

			boolean DEFAULT_CONVERT_WOCE_TEMPS = mConvertTemps.isSelected();
			boolean DEFAULT_CONVERT_QCS = mTranslateWOCEToIGOSS.isSelected();
			boolean DEFAULT_SET_MSG_QBEQ3 = mReplQB3.isSelected();
			boolean DEFAULT_SET_MSG_QBEQ4 = mReplQB4.isSelected();
			boolean DEFAULT_SET_MSG_QBEQ7 = mReplQB7.isSelected();
			boolean DEFAULT_SET_MSG_QBEQ8 = mReplQB8.isSelected();
			boolean DEFAULT_SET_ALL_PARAMS_MSG_BQBEQ4 = mReplAllBQB4.isSelected();
			boolean DEFAULT_SET_GAS_PARAMS_MSG_BQBEQ3_AND_O2QBEQ4 = mReplGasQB34.isSelected();

			// WOCE prefs go here;
			item = doc.createElement("woceprefs");
			item.setAttribute("convqc", String.valueOf(DEFAULT_CONVERT_QCS));
			item.setAttribute("convtemp", String.valueOf(DEFAULT_CONVERT_WOCE_TEMPS));
			item.setAttribute("repleq3", String.valueOf(DEFAULT_SET_MSG_QBEQ3));
			item.setAttribute("repleq4", String.valueOf(DEFAULT_SET_MSG_QBEQ4));
			item.setAttribute("repleq7", String.valueOf(DEFAULT_SET_MSG_QBEQ7));
			item.setAttribute("repleq8", String.valueOf(DEFAULT_SET_MSG_QBEQ8));
			item.setAttribute("replalleq4", String.valueOf(DEFAULT_SET_ALL_PARAMS_MSG_BQBEQ4));
			item.setAttribute("replgaseq34", String.valueOf(DEFAULT_SET_GAS_PARAMS_MSG_BQBEQ3_AND_O2QBEQ4));
			root.appendChild(item);

			// enhancement prefs
			// get the enhancement prefs
			boolean ENHANCE_ENLARGE_CURRENT_SYMBOL = mEnlargeCurrSymbol.isSelected();
			boolean ENHANCE_REPLACE_CURRENT_SYMBOL = mUseNewSymbol.isSelected();
			double ENHANCE_ENLARGE_CURRENT_SYMBOL_BY = ((Double) mEnhanceSize1.getValue()).doubleValue() / 100.0;
			double ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY = ((Double) mEnhanceSize2.getValue()).doubleValue() / 100.0;
			boolean ENHANCE_USE_CONTRASTING_COLOR = mContrastColor.isSelected();
			Color ENHANCE_CONTRASTING_COLOR = mContrastSwatch.getColor();
			int ENHANCE_CONTRASTING_SYMBOL = mSymbolPopup2.getSelectedIndex() + 1;

			item = doc.createElement("enhancementprefs");
			if (ENHANCE_ENLARGE_CURRENT_SYMBOL) {
				item.setAttribute("enlargecurrsymbol", String.valueOf(ENHANCE_ENLARGE_CURRENT_SYMBOL));
				item.setAttribute("replacecurrsymbol", String.valueOf(false));
				item.setAttribute("enlargecurrsymbolby", String.valueOf(ENHANCE_ENLARGE_CURRENT_SYMBOL_BY));
			}

			if (ENHANCE_REPLACE_CURRENT_SYMBOL) {
				item.setAttribute("replacecurrsymbol", String.valueOf(ENHANCE_REPLACE_CURRENT_SYMBOL));
				item.setAttribute("enlargecurrsymbol", String.valueOf(false));
				item.setAttribute("enlargecontrastingsymbolby", String.valueOf(ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY));
				item.setAttribute("contrastingsymbol", String.valueOf(ENHANCE_CONTRASTING_SYMBOL));
			}

			item.setAttribute("usecontrastingcolor", String.valueOf(ENHANCE_USE_CONTRASTING_COLOR));
			if (ENHANCE_USE_CONTRASTING_COLOR) {
				Element subitem = doc.createElement("contrastingcolor");
				subitem.setAttribute("red", String.valueOf(ENHANCE_CONTRASTING_COLOR.getRed()));
				subitem.setAttribute("green", String.valueOf(ENHANCE_CONTRASTING_COLOR.getGreen()));
				subitem.setAttribute("blue", String.valueOf(ENHANCE_CONTRASTING_COLOR.getBlue()));
				root.appendChild(subitem);
			}
			root.appendChild(item);

			// font style prefs
			item = doc.createElement("stylesheet");
			item.setAttribute("axisvaluefont", new String(mFontSettings1.getFontName()));
			item.setAttribute("axislabelfont", new String(mFontSettings2.getFontName()));
			item.setAttribute("isopycnalfont", new String(mFontSettings3.getFontName()));
			item.setAttribute("plottitlefont", new String(mFontSettings4.getFontName()));
			item.setAttribute("colorbarfont", new String(mFontSettings5.getFontName()));
			item.setAttribute("mapvaluefont", new String(mFontSettings6.getFontName()));
			item.setAttribute("mapstnlabelfont", new String(mFontSettings7.getFontName()));
			item.setAttribute("contourxsecvaluefont", new String(mFontSettings8.getFontName()));
			item.setAttribute("contourxseclabelfont", new String(mFontSettings9.getFontName()));
			item.setAttribute("regressionlabelfont", new String(mFontSettings10.getFontName()));

			item.setAttribute("axisvaluesize", String.valueOf(mFontSettings1.getFontSize()));
			item.setAttribute("axislabelsize", String.valueOf(mFontSettings2.getFontSize()));
			item.setAttribute("isopycnalsize", String.valueOf(mFontSettings3.getFontSize()));
			item.setAttribute("plottitlesize", String.valueOf(mFontSettings4.getFontSize()));
			item.setAttribute("colorbarsize", String.valueOf(mFontSettings5.getFontSize()));
			item.setAttribute("mapvaluesize", String.valueOf(mFontSettings6.getFontSize()));
			item.setAttribute("mapstnlabelsize", String.valueOf(mFontSettings7.getFontSize()));
			item.setAttribute("contourxsecvaluesize", String.valueOf(mFontSettings8.getFontSize()));
			item.setAttribute("contourxseclabelsize", String.valueOf(mFontSettings9.getFontSize()));
			item.setAttribute("regressionlabelsize", String.valueOf(mFontSettings10.getFontSize()));

			item.setAttribute("axisvaluestyle", String.valueOf(mFontSettings1.getFontStyle()));
			item.setAttribute("axislabelstyle", String.valueOf(mFontSettings2.getFontStyle()));
			item.setAttribute("isopycnalstyle", String.valueOf(mFontSettings3.getFontStyle()));
			item.setAttribute("plottitlestyle", String.valueOf(mFontSettings4.getFontStyle()));
			item.setAttribute("colorbarstyle", String.valueOf(mFontSettings5.getFontStyle()));
			item.setAttribute("mapvaluestyle", String.valueOf(mFontSettings6.getFontStyle()));
			item.setAttribute("mapstnlabelstyle", String.valueOf(mFontSettings7.getFontStyle()));
			item.setAttribute("contourxsecvaluestyle", String.valueOf(mFontSettings8.getFontStyle()));
			item.setAttribute("contourxseclabelstyle", String.valueOf(mFontSettings9.getFontStyle()));
			item.setAttribute("regressionlabelstyle", String.valueOf(mFontSettings10.getFontStyle()));

			Color c1 = mFontSettings1.getColor();
			Color c2 = mFontSettings2.getColor();
			Color c3 = mFontSettings3.getColor();
			Color c4 = mFontSettings4.getColor();
			Color c5 = mFontSettings5.getColor();
			Color c6 = mFontSettings6.getColor();
			Color c7 = mFontSettings7.getColor();
			Color c8 = mFontSettings8.getColor();
			Color c9 = mFontSettings9.getColor();
			Color c10 = mFontSettings10.getColor();
			Element subitem = doc.createElement("axisvaluecolor");
			subitem.setAttribute("red", String.valueOf(c1.getRed()));
			subitem.setAttribute("green", String.valueOf(c1.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c1.getBlue()));
			item.appendChild(subitem);
			 subitem = doc.createElement("axislabelcolor");
			subitem.setAttribute("red", String.valueOf(c2.getRed()));
			subitem.setAttribute("green", String.valueOf(c2.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c2.getBlue()));
			item.appendChild(subitem);
			subitem = doc.createElement("isopycnalcolor");
			subitem.setAttribute("red", String.valueOf(c3.getRed()));
			subitem.setAttribute("green", String.valueOf(c3.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c3.getBlue()));
			item.appendChild(subitem);
			subitem = doc.createElement("plottitlecolor");
			subitem.setAttribute("red", String.valueOf(c4.getRed()));
			subitem.setAttribute("green", String.valueOf(c4.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c4.getBlue()));
			item.appendChild(subitem);
			subitem = doc.createElement("colorbarcolor");
			subitem.setAttribute("red", String.valueOf(c5.getRed()));
			subitem.setAttribute("green", String.valueOf(c5.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c5.getBlue()));
			item.appendChild(subitem);
			subitem = doc.createElement("mapvaluecolor");
			subitem.setAttribute("red", String.valueOf(c6.getRed()));
			subitem.setAttribute("green", String.valueOf(c6.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c6.getBlue()));
			item.appendChild(subitem);
			subitem = doc.createElement("mapstnlabelcolor");
			subitem.setAttribute("red", String.valueOf(c7.getRed()));
			subitem.setAttribute("green", String.valueOf(c7.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c7.getBlue()));
			item.appendChild(subitem);
			subitem = doc.createElement("contourxsecvaluecolor");
			subitem.setAttribute("red", String.valueOf(c8.getRed()));
			subitem.setAttribute("green", String.valueOf(c8.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c8.getBlue()));
			item.appendChild(subitem);
			subitem = doc.createElement("contourxseclabelcolor");
			subitem.setAttribute("red", String.valueOf(c9.getRed()));
			subitem.setAttribute("green", String.valueOf(c9.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c9.getBlue()));
			item.appendChild(subitem);
			subitem = doc.createElement("regressionlabelcolor");
			subitem.setAttribute("red", String.valueOf(c10.getRed()));
			subitem.setAttribute("green", String.valueOf(c10.getGreen()));
			subitem.setAttribute("blue", String.valueOf(c10.getBlue()));
			item.appendChild(subitem);
			root.appendChild(item);

			// param sub prefs
			int DEFAULT_SALT = mParamSubs.getSalinityDefault() + 1;
			int SALT_SUB = mParamSubs.getSalinitySub() + 1;
			int DEFAULT_O2 = mParamSubs.getO2Default() + 1;
			int O2_SUB = mParamSubs.getO2Sub() + 1;

			item = doc.createElement("paramsubprefs");
			item.setAttribute("defaultsalt", String.valueOf(DEFAULT_SALT));
			item.setAttribute("saltsub", String.valueOf(SALT_SUB));
			item.setAttribute("defaulto2", String.valueOf(DEFAULT_O2));
			item.setAttribute("o2sub", String.valueOf(O2_SUB));
			root.appendChild(item);

			item = doc.createElement("importprefs");
			item.setAttribute("convertmass", String.valueOf(mConvertMass.isSelected()));
			item.setAttribute("translate", String.valueOf(mTranslateParams.isSelected()));
			item.setAttribute("tolexicon", "1"); // (String)mDestinationLexicon.getSelectedItem());
			item.setAttribute("convertdepth", String.valueOf(mConvertDepth.isSelected()));
			root.appendChild(item);
	    
			item = doc.createElement("wodprefs");
			String t = mWODSelectPrefs.getSectionIDRule().name();
			item.setAttribute("sectionidrule", t);
			t = mWODSelectPrefs.getCastIDRule().name();
			item.setAttribute("castidrule", t);
			t = mWODSelectPrefs.getCastNumberRule().name();
			item.setAttribute("castnumberrule", t);
			t = mWODSelectPrefs.getPreferPressureParameterRule().name();
			item.setAttribute("preferpressureparameterrule", t);
			t = mWODSelectPrefs.getDepthConversionRule().name();
			item.setAttribute("depthconversionrule", t);
			t = mWODSelectPrefs.getConvertParameterNamesRule().name();
			item.setAttribute("convertparameternamesrule", t);
			t = mWODSelectPrefs.getQCConversionRule().name();
			item.setAttribute("qcconversionrule", t);
			t = mWODSelectPrefs.getDestinationQCRule().name();
			item.setAttribute("destinationqcrule", t);
			t = mWODSelectPrefs.getTempConversionRule().name();
			item.setAttribute("tempconversionrule", t);
			t = String.valueOf(mWODSelectPrefs.isCollectMetadata());
			item.setAttribute("collectmetadata", t);
			root.appendChild(item);

			// write the managed features preferences
			for (FeatureGroup fg : JOAConstants.JOA_FEATURESET.values()) {
				// <featuregroup id="kJOAPreferences" name="Preferences" enabled="true">
				item = doc.createElement("featuregroup");
				item.setAttribute("id", fg.getID());
				item.setAttribute("name", fg.getDisplayName());
				item.setAttribute("enabled", String.valueOf(fg.isEnabled()));
				HashMap<String, ManagedFeature> mfs = fg.getFeatures();
				for (ManagedFeature mf : mfs.values()) {
					// <feature name="Show General Preferences" id="kGeneral"
					// version="1.0" status="rel" enabled="true" visible="true"/>
					Element fitem = doc.createElement("feature");
					fitem.setAttribute("id", mf.getID());
					fitem.setAttribute("name", mf.getDisplayName());
					fitem.setAttribute("version", mf.getVersion());
					fitem.setAttribute("status", mf.getDevelopmentStatus());
					fitem.setAttribute("enabled", String.valueOf(mf.isEnabled()));
					fitem.setAttribute("visible", String.valueOf(mf.isVisible()));
					item.appendChild(fitem);
				}

				// use this bit of code to add a new feature to existing feature group
//				if (fg.getID().equalsIgnoreCase("kFile")) {
//					ManagedFeature mf = new ManagedFeature(fg, "kExportJSON", "Export JSON Products", "1.0", "rel", true, true);	
//					Element fitem = doc.createElement("feature");
//					fitem.setAttribute("id", mf.getID());
//					fitem.setAttribute("name", mf.getDisplayName());
//					fitem.setAttribute("version", mf.getVersion());
//					fitem.setAttribute("status", mf.getDevelopmentStatus());
//					fitem.setAttribute("enabled", String.valueOf(mf.isEnabled()));
//					fitem.setAttribute("visible", String.valueOf(mf.isVisible()));
//					item.appendChild(fitem);
//				}
				root.appendChild(item);
			}

			doc.appendChild(root);
			((TXDocument) doc).setVersion("1.0");
			StringWriter sw = new StringWriter();
			((TXDocument) doc).printWithFormat(sw);
			String prefsAsStr = sw.toString();
			return prefsAsStr;
		}
		catch (Exception ex) {
				ex.printStackTrace();
				return null;
		}
	}

	private class EnhancementPrefsPanel extends JPanel implements ItemListener {
		public EnhancementPrefsPanel() {
			// init the interface
			init();
		}

		public void refreshUI() {
			mSymbolPopup2.setSelectedIndex(JOAConstants.DEFAULT_ENHANCE_CONTRASTING_SYMBOL);
			mEnlargeCurrSymbol.setSelected(JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL);
			mUseNewSymbol.setSelected(JOAConstants.DEFAULT_ENHANCE_REPLACE_CURRENT_SYMBOL);
			mContrastColor.setSelected(JOAConstants.DEFAULT_ENHANCE_USE_CONTRASTING_COLOR);
			Double stVal = new Double(JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL_BY * 100);
			mEnhanceSize1.setValue(stVal);
			Double stVal2 = new Double(JOAConstants.DEFAULT_ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY * 100);
			mEnhanceSize2.setValue(stVal2);
			mContrastSwatch.setColor(JOAConstants.DEFAULT_ENHANCE_CONTRASTING_COLOR);
			if (JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL) {
				disableSymbolStuff();
				enableEnlargeStuff();
			}
			else {
				enableSymbolStuff();
				disableEnlargeStuff();
			}
		}
		
		public void init() {
			this.setLayout(new BorderLayout(5, 5));
			mSymbolPopup2 = new JOAJComboBox();
			for (int i = 0; i < symbolData.length; i++) {
				mSymbolPopup2.addItem(symbolData[i]);
			}
			mSymbolPopup2.setSelectedIndex(JOAConstants.DEFAULT_ENHANCE_CONTRASTING_SYMBOL);

			// display options
			JPanel displayOptPanel = new JPanel();
			displayOptPanel.setLayout(new GridLayout(4, 1, 10, 5));

			ButtonGroup bg5 = new ButtonGroup();
			mEnlargeCurrSymbol = new JOAJRadioButton(b.getString("kEnlargeSymbolBy"),
			    JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL);
			mUseNewSymbol = new JOAJRadioButton(b.getString("kUseNewSymbols"),
			    JOAConstants.DEFAULT_ENHANCE_REPLACE_CURRENT_SYMBOL);
			mContrastColor = new JOAJCheckBox(b.getString("kContrastColor"),
			    JOAConstants.DEFAULT_ENHANCE_USE_CONTRASTING_COLOR);
			mEnlargeCurrSymbol.addItemListener(this);
			mUseNewSymbol.addItemListener(this);
			mContrastColor.addItemListener(this);
			bg5.add(mEnlargeCurrSymbol);
			bg5.add(mUseNewSymbol);

			// enlarge symbol
			JPanel enlargeSymPanel = new JPanel();
			enlargeSymPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			enlargeSymPanel.add(mEnlargeCurrSymbol);

			Double stVal = new Double(JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL_BY * 100);
			Double minVal = new Double(1.0);
			Double inc = new Double(1.0);
			Double max = new Double(1000.0);

			mEnhancedCurrSymbolNumberModel = null;
			try {
				mEnhancedCurrSymbolNumberModel = new SpinnerNumberModel(stVal, minVal, max, inc);
			}
			catch (Exception ex) {
				mEnhancedCurrSymbolNumberModel = new SpinnerNumberModel(minVal, minVal, max, inc);
			}
			mSizeField = new JSpinner(mEnhancedCurrSymbolNumberModel);

			mEnhanceSize1 = new JSpinner(mEnhancedCurrSymbolNumberModel);
			enlargeSymPanel.add(mEnhanceSize1);
			enlargeSymPanel.add(new JOAJLabel("(%)"));

			// new symbol
			JPanel newSymPanel = new JPanel();
			newSymPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			newSymPanel.add(mUseNewSymbol);
			newSymPanel.add(mSymbolPopup2);
			mSymbolPopup2.addItemListener(this);

			Double stVal2 = new Double(JOAConstants.DEFAULT_ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY * 100);

			SpinnerNumberModel model2 = null;
			try {
				model2 = new SpinnerNumberModel(stVal2, minVal, max, inc);
			}
			catch (Exception ex) {
				model2 = new SpinnerNumberModel(minVal, minVal, max, inc);
			}

			mEnhanceSize2 = new JSpinner(model2);

			newSymPanel.add(mEnhanceSize2);
			newSymPanel.add(new JOAJLabel("(%)"));

			// contrast color
			JPanel contrastPanel = new JPanel();
			contrastPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			contrastPanel.add(mContrastColor);
			mContrastSwatch = new Swatch(JOAConstants.DEFAULT_ENHANCE_CONTRASTING_COLOR);
			contrastPanel.add(mContrastSwatch);

			displayOptPanel.add(enlargeSymPanel);
			displayOptPanel.add(newSymPanel);
			displayOptPanel.add(contrastPanel);
			this.add(new TenPixelBorder(displayOptPanel, 10, 10, 10, 10), "North");

			if (JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL) {
				disableSymbolStuff();
				enableEnlargeStuff();
			}
			else {
				enableSymbolStuff();
				disableEnlargeStuff();
			}
		}

		public void itemStateChanged(ItemEvent evt) {
			// if (mIgnoreEvent) {
			// mIgnoreEvent = false;
			// return;
			// }
			if (evt.getSource() instanceof JOAJComboBox) {
				JOAJComboBox cb = (JOAJComboBox) evt.getSource();
				if (cb == mSymbolPopup2) {
					mCurrSymbol = cb.getSelectedIndex();
				}
			}
			else if (evt.getSource() instanceof JOAJRadioButton) {
				JOAJRadioButton rb = (JOAJRadioButton) evt.getSource();
				if (evt.getStateChange() == ItemEvent.SELECTED && rb == mEnlargeCurrSymbol) {
					enableEnlargeStuff();
					disableSymbolStuff();
				}
				else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mUseNewSymbol) {
					enableSymbolStuff();
					disableEnlargeStuff();
				}
			}
		}

		public void enableSymbolStuff() {
			mSymbolPopup2.setEnabled(true);
			mSymbolPopup2.invalidate();
			mEnhanceSize2.setEnabled(true);
			this.validate();
		}

		public void disableSymbolStuff() {
			mSymbolPopup2.setEnabled(false);
			mSymbolPopup2.invalidate();
			mEnhanceSize2.setEnabled(false);
			this.validate();
		}

		public void enableEnlargeStuff() {
			mEnhanceSize1.setEnabled(true);
			this.validate();
		}

		public void disableEnlargeStuff() {
			mEnhanceSize1.setEnabled(false);
			this.validate();
		}
	}

	private class GeneralPrefsPanel extends JPanel implements ItemListener {
		SpinnerNumberModel cursorSizeModel;
		
		public GeneralPrefsPanel() {
			// init the interface
			init();
		}

		public void refreshUI() {
			mLineWidths.setSelectedIndex(JOAConstants.CONNECT_LINE_WIDTH - 1);
			mMissingValColor.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
			mPlotFrameColor.setColor(JOAConstants.DEFAULT_FRAME_COLOR);
			mPlotContentsColor.setColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
			mPlotTitles.setSelected(JOAConstants.DEFAULT_PLOT_TITLES);
			mColorCombo.setSelectedIndex(JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME);
			mColorPaletteCombo.setSelectedItem(JOAConstants.DEFAULT_LINE_PLOT_PALETTE);
			mSizeField2.setValue(JOAConstants.DEFAULT_CURSOR_SIZE);
			mGeographicFormats.setSelectedIndex(JOAConstants.DEFAULT_POSITION_FORMAT);
			mDateFormats.setSelectedIndex(JOAConstants.DEFAULT_DATE_FORMAT);
			mThickContours.setSelected(JOAConstants.THICKEN_CONTOUR_LINES);
			mThickOverlayContours.setSelected(JOAConstants.THICKEN_OVERLAY_CONTOUR_LINES);
		}

		public void init() {
			JPanel mainPanel = new JPanel(); // everything goes in here
			mainPanel.setLayout(new BorderLayout(5, 5));

			JPanel upperPanel = new JPanel();
			upperPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

			// Line Width Panel
			JPanel line1 = new JPanel();
			line1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
			Vector<String> presetData = new Vector<String>();
			presetData.addElement("1");
			presetData.addElement("2");
			presetData.addElement("3");
			presetData.addElement("4");
			presetData.addElement("5");
			mLineWidths = new JOAJComboBox(presetData);
			mLineWidths.setSelectedIndex(JOAConstants.CONNECT_LINE_WIDTH - 1);
			mLineWidths.addItemListener(this);
			line1.add(new JOAJLabel(b.getString("kConnectLineWidth")));
			line1.add(mLineWidths);
			upperPanel.add(line1);

			// swatches
			JPanel line5 = new JPanel();
			line5.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mMissingValColor = new Swatch(JOAConstants.DEFAULT_MISSINGVAL_COLOR, 12, 12);
			line5.add(mMissingValColor);
			line5.add(new JOAJLabel(b.getString("kMissingValColor")));

			JPanel line6 = new JPanel();
			line6.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mPlotFrameColor = new Swatch(JOAConstants.DEFAULT_FRAME_COLOR, 12, 12);
			line6.add(mPlotFrameColor);
			line6.add(new JOAJLabel(b.getString("kPlotWindowBGColor")));

			JPanel line7 = new JPanel();
			line7.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mPlotContentsColor = new Swatch(JOAConstants.DEFAULT_CONTENTS_COLOR, 12, 12);
			line7.add(mPlotContentsColor);
			line7.add(new JOAJLabel(b.getString("kPlotInteriorColor")));

			JPanel line7b = new JPanel();
			line7b.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mPlotTitles = new JOAJCheckBox(b.getString("kPlotTitles2"), JOAConstants.DEFAULT_PLOT_TITLES);
			line7b.add(mPlotTitles);

			JPanel line8 = new JPanel();
			line8.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			line8.add(new JOAJLabel(b.getString("kDefaultAutoscaleColorScheme")));
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
			mColorCombo.addItemListener(this);
			line8.add(mColorCombo);
			
			JPanel line8a = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
			line8a.add(new JOAJLabel("Default color palette for line plots:"));
			
			Vector<String> mColorPaletteList = JOAFormulas.getColorPalList();
			mColorPaletteCombo = new JOAJComboBox(mColorPaletteList);
			mColorPaletteCombo.setSelectedItem(JOAConstants.DEFAULT_LINE_PLOT_PALETTE);
			mColorPaletteCombo.addItemListener(this);
			line8a.add(mColorPaletteCombo);

			// create the symbol popup menu
			mSymbolPopup = new JOAJComboBox();
			for (int i = 0; i < symbolData.length; i++) {
				mSymbolPopup.addItem(symbolData[i]);
			}
			mSymbolPopup.setSelectedIndex(JOAConstants.DEFAULT_CURSOR_SYMBOL - 1);

			// plot symbols
			JPanel line9 = new JPanel();
			line9.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			line9.add(new JOAJLabel(b.getString("kBrowserSymbol")));
			line9.add(mSymbolPopup);
			mSymbolPopup.addItemListener(this);
			line9.add(new JOAJLabel(b.getString("kSize")));

			SpinnerNumberModel cursorSizeModel = new SpinnerNumberModel(JOAConstants.DEFAULT_CURSOR_SIZE, 1, 100, 1);
			mSizeField2 = new JSpinner(cursorSizeModel);

			line9.add(mSizeField2);

			// text formating
			JPanel line10 = new JPanel();
			line10.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			line10.add(new JOAJLabel(b.getString("kGeographicFormat")));
			Vector<String> geoFormatChoices = new Vector<String>();
			geoFormatChoices.addElement(new String("159.717E"));
			geoFormatChoices.addElement(new String("154 43'0E"));
			mGeographicFormats = new JOAJComboBox(geoFormatChoices);
			line10.add(mGeographicFormats);
			mGeographicFormats.setSelectedIndex(JOAConstants.DEFAULT_POSITION_FORMAT);
			mGeographicFormats.addItemListener(this);
			line10.add(new JOAJLabel(b.getString("kDateFormat")));
			Vector<String> dateFormatChoices = new Vector<String>();
			dateFormatChoices.addElement(new String("dd/MM/yy"));
			dateFormatChoices.addElement(new String("MM/dd/yy"));
			dateFormatChoices.addElement(new String("dd-MM-yy"));
			dateFormatChoices.addElement(new String("MM-dd-yy"));
			dateFormatChoices.addElement(new String("dd-MMM-yy"));
			dateFormatChoices.addElement(new String("dd/MM/yyyy"));
			dateFormatChoices.addElement(new String("MM/dd/yyyy"));
			dateFormatChoices.addElement(new String("dd-MM-yyyy"));
			dateFormatChoices.addElement(new String("MM-dd-yyyy"));
			dateFormatChoices.addElement(new String("dd-MMM-yyyy"));
			dateFormatChoices.addElement(new String("yyyy-dd-MM"));
			dateFormatChoices.addElement(new String("yyyy-MM-dd"));
			dateFormatChoices.addElement(new String("yyyy-MMM-dd"));
			mDateFormats = new JOAJComboBox(dateFormatChoices);
			mDateFormats.setSelectedIndex(JOAConstants.DEFAULT_DATE_FORMAT);
			line10.add(mDateFormats);
			
			mThickContours = new JCheckBox("Enhance primary contours for preparing presentation plots (this JOA session only)", JOAConstants.THICKEN_CONTOUR_LINES);
			mThickOverlayContours = new JCheckBox("Enhance overlay contours for preparing presentation plots (this JOA session only)", JOAConstants.THICKEN_OVERLAY_CONTOUR_LINES);

			upperPanel.add(line5);
			upperPanel.add(line6);
			upperPanel.add(line7);
			upperPanel.add(line7b);
			upperPanel.add(line8);
			upperPanel.add(line8a);
			upperPanel.add(line9);
			upperPanel.add(line10);
			upperPanel.add(mThickContours);
			upperPanel.add(mThickOverlayContours);

			// add all the sub panels to main panel
			mainPanel.add(new TenPixelBorder(upperPanel, 5, 5, 5, 5), "West");
			this.add(mainPanel);
		}

		public void itemStateChanged(ItemEvent evt) {
			if (evt.getSource() instanceof JOAJComboBox) {
			}
		}
	}

	private class CTDPrefsPanel extends JPanel implements ItemListener, ActionListener {
		JOAJButton mLoadBtn = null;
		JOAJButton mSaveDeciButton = null;

		public CTDPrefsPanel() {
			// init the interface
			init();
		}
		
		public void refreshUI() {
			mNoDecimation.setSelected(JOAConstants.DEFAULT_NO_CTD_DECIMATION);
			mConstantDecimation.setSelected(JOAConstants.DEFAULT_CONSTANT_CTD_DECIMATION);
			mConstantField.setText(JOAFormulas.formatDouble(String.valueOf(JOAConstants.DEFAULT_DECIMATE_CONSTANT), 1, false));
			if (JOAConstants.DEFAULT_STD_LEVEL_CTD_DECIMATION) {
				mSurfaces.setSelectedItem(JOAConstants.DEFAULT_DECIMATE_STD_LEVEL);
			}
			else {
				mSurfaces.setSelectedIndex(0);
			}
			mStdLevelDecimation.setSelected(JOAConstants.DEFAULT_STD_LEVEL_CTD_DECIMATION);
			mCustomDecimation.setSelected(JOAConstants.DEFAULT_CUSTOM_CTD_DECIMATION);;
			if (JOAConstants.DEFAULT_CUSTOM_CTD_DECIMATION) {
				// restore the custom decimations
				for (int i = 0; i < JOAConstants.DEFAULT_NUMBER_OF_CUSTOM_TRIPLETS; i++) {
					mCustRanges.setRange(i, JOAConstants.DEFAULT_CUSTOM_DECIMATE_TRIPLETS[i]);
				}
			}
		}

		public void init() {
			JPanel mainPanel = new JPanel(); // everything goes in here
			mainPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));

			JPanel upperPanel = new JPanel();
			upperPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

			// No Decimation
			JPanel line1 = new JPanel();
			line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			mNoDecimation = new JOAJRadioButton(b.getString("kNoDecimation"), JOAConstants.DEFAULT_NO_CTD_DECIMATION);
			line1.add(mNoDecimation);
			upperPanel.add(line1);

			// Constant Decimation
			JPanel line2 = new JPanel();
			line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			mConstantDecimation = new JOAJRadioButton(b.getString("kConstant"), JOAConstants.DEFAULT_CONSTANT_CTD_DECIMATION);
			line2.add(mConstantDecimation);
			line2.add(new JOAJLabel(b.getString("kEvery")));
			mConstantField = new JOAJTextField(7);
			mConstantField
			    .setText(JOAFormulas.formatDouble(String.valueOf(JOAConstants.DEFAULT_DECIMATE_CONSTANT), 1, false));
			line2.add(mConstantField);
			line2.add(new JOAJLabel(b.getString("kDB")));
			upperPanel.add(line2);

			// Standard Levels Panel
			JPanel line3 = new JPanel();
			line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			Vector<String> presetData = JOAFormulas.getFilteredSurfaceList("pres");
			mSurfaces = new JOAJComboBox(presetData);
			if (JOAConstants.DEFAULT_STD_LEVEL_CTD_DECIMATION) {
				// position combo box to right surface
				mSurfaces.setSelectedItem(JOAConstants.DEFAULT_DECIMATE_STD_LEVEL);
			}
			else {
				mSurfaces.setSelectedIndex(0);
			}

			// mSurfaces.addItemListener(this);
			mStdLevelDecimation = new JOAJRadioButton(b.getString("kStandardLevels"),
			    JOAConstants.DEFAULT_STD_LEVEL_CTD_DECIMATION);
			line3.add(mStdLevelDecimation);
			line3.add(mSurfaces);
			upperPanel.add(line3);

			// Custom Decimation
			JPanel line4 = new JPanel();
			line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			mCustomDecimation = new JOAJRadioButton(b.getString("kCustomDotDotDot"),
			    JOAConstants.DEFAULT_CUSTOM_CTD_DECIMATION);
			line4.add(mCustomDecimation);
			upperPanel.add(line4);

			ButtonGroup bg = new ButtonGroup();
			bg.add(mNoDecimation);
			bg.add(mConstantDecimation);
			bg.add(mStdLevelDecimation);
			bg.add(mCustomDecimation);

			mNoDecimation.addItemListener(this);
			mConstantDecimation.addItemListener(this);
			mStdLevelDecimation.addItemListener(this);
			mCustomDecimation.addItemListener(this);

			// add the range detail
			mCustRanges = new CustomDeciRanges();
			if (JOAConstants.DEFAULT_CUSTOM_CTD_DECIMATION) {
				// restore the custom decimations
				for (int i = 0; i < JOAConstants.DEFAULT_NUMBER_OF_CUSTOM_TRIPLETS; i++) {
					mCustRanges.setRange(i, JOAConstants.DEFAULT_CUSTOM_DECIMATE_TRIPLETS[i]);
				}
			}

			mSaveDeciButton = new JOAJButton(b.getString("kSave"));
			mSaveDeciButton.setActionCommand("savedeci");
			mLoadBtn = new JOAJButton(b.getString("kLoad"));
			mLoadBtn.setActionCommand("load");
			JPanel dlgBtnsInset2 = new JPanel();
			JPanel dlgBtnsPanel2 = new JPanel();
			dlgBtnsInset2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
			dlgBtnsPanel2.setLayout(new GridLayout(1, 4, 15, 1));
			dlgBtnsPanel2.add(mLoadBtn);
			dlgBtnsPanel2.add(mSaveDeciButton);
			dlgBtnsInset2.add(dlgBtnsPanel2);

			mSaveDeciButton.addActionListener(this);
			mLoadBtn.addActionListener(this);

			MyScroller rangeScroller = new MyScroller();
			rangeScroller.setViewportView(mCustRanges);

			JPanel customDeciPanel = new JPanel();
			customDeciPanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 5));
			customDeciPanel.add(rangeScroller);
			customDeciPanel.add(dlgBtnsInset2);

			// add all the sub panels to main panel
			mainPanel.add(new TenPixelBorder(upperPanel, 5, 5, 5, 5));
			mainPanel.add(new TenPixelBorder(customDeciPanel, 5, 5, 5, 5));
			this.add(mainPanel);

			// set initial dialog state
			disableAll();
			if (JOAConstants.DEFAULT_CONSTANT_CTD_DECIMATION) {
				setConstantState(true);
			}
			else if (JOAConstants.DEFAULT_STD_LEVEL_CTD_DECIMATION) {
				setStdLevelState(true);
			}
			else if (JOAConstants.DEFAULT_CUSTOM_CTD_DECIMATION) {
				setCustomState(true);
			}
		}

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			if (cmd.equals("load")) {
				// get a filename
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.endsWith("_deci.xml")) {
							return true;
						}
						else {
							return false;
						}
					}
				};
				Frame fr = new Frame();
				String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
				FileDialog f = new FileDialog(fr, "Read decimation settings from:", FileDialog.LOAD);
				f.setDirectory(directory);
				f.setFilenameFilter(filter);
				f.setVisible(true);
				directory = f.getDirectory();
				String fs = f.getFile();
				f.dispose();
				if (directory != null && fs != null) {
					File nf = new File(directory, fs);
					try {
						readDecimations(nf);
						for (int i = 0; i < numTriplets; i++) {
							mCustRanges.setRange(i, triplets[i]);
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			else if (cmd.equals("savedeci")) {
				saveDecimation();
			}
		}

		public void saveDecimation() {
			// get a filename
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith("_deci.xml")) {
						return true;
					}
					else {
						return false;
					}
				}
			};
			Frame fr = new Frame();
			String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
			FileDialog f = new FileDialog(fr, "Save preferences as:", FileDialog.SAVE);
			f.setDirectory(directory);
			f.setFilenameFilter(filter);
			f.setFile("untitled_deci.xml");
			f.setVisible(true);
			directory = f.getDirectory();
			f.dispose();
			if (directory != null && f.getFile() != null) {
				File nf = new File(directory, f.getFile());
				saveDecis(nf);
			}
		}

		public void saveDecis(File file) {
			// save preferences as XML
			try {
				// read the DTD file
				// String dtdFile = "JOA_Support/joapreferences.dtd";
				// FileInputStream fis = new FileInputStream(dtdFile);
				// Parser dtdParser = new Parser(dtdFile);
				// DTD dtd = dtdParser.readDTDStream(fis);

				// create a documentobject
				Document doc = (Document) Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

				// make joapreferences the root element
				Element root = doc.createElement("decimation");

				// get custom decimation array
				mCustomRanges = mCustRanges.getDecimations();
				mNumCustomRanges = 0;
				Triplet[] tempRanges = new Triplet[25];
				for (int i = 0; i < 25; i++) {
					if (mCustomRanges[i].getVal1() != JOAConstants.MISSINGVALUE
					    && mCustomRanges[i].getVal2() != JOAConstants.MISSINGVALUE
					    && mCustomRanges[i].getVal3() != JOAConstants.MISSINGVALUE) {
						tempRanges[mNumCustomRanges] = mCustomRanges[i];
						mNumCustomRanges++;
					}
				}

				Element item = doc.createElement("customdecimation");
				for (int i = 0; i < mNumCustomRanges; i++) {
					Triplet vals = tempRanges[i];
					Element custItem = doc.createElement("minvalue");
					custItem.appendChild(doc.createTextNode(String.valueOf(vals.getVal1())));
					item.appendChild(custItem);
					custItem = doc.createElement("maxvalue");
					custItem.appendChild(doc.createTextNode(String.valueOf(vals.getVal2())));
					item.appendChild(custItem);
					custItem = doc.createElement("increment");
					custItem.appendChild(doc.createTextNode(String.valueOf(vals.getVal3())));
					item.appendChild(custItem);
				}
				root.appendChild(item);
				doc.appendChild(root);
				((TXDocument) doc).setVersion("1.0");
				((TXDocument) doc).printWithFormat(new FileWriter(file));
			}
			catch (Exception ex) {

			}
		}

		public void itemStateChanged(ItemEvent evt) {
			JOAJRadioButton rb = (JOAJRadioButton) evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && rb == mNoDecimation) {
				disableAll();
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mConstantDecimation) {
				disableAll();
				setConstantState(true);
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mStdLevelDecimation) {
				disableAll();
				setStdLevelState(true);
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mCustomDecimation) {
				disableAll();
				setCustomState(true);
			}
		}

		public void disableAll() {
			setConstantState(false);
			setStdLevelState(false);
			setCustomState(false);
		}

		public void setConstantState(boolean state) {
			mConstantField.setEnabled(state);
			mConstantField.requestFocus();
			mConstantField.selectAll();
		}

		public void setStdLevelState(boolean state) {
			mSurfaces.setEnabled(state);
		}

		public void setCustomState(boolean state) {
			mCustRanges.setEnabled(state);
			mLoadBtn.setEnabled(state);
			mSaveDeciButton.setEnabled(state);
		}

		private int NO_KEY = 0;
		private int CUD_KEY = 8;
		private int MIN_KEY = 9;
		private int MAX_KEY = 10;
		private int INC_KEY = 11;
		private int KEY_STATE = NO_KEY;
		private double MIN = JOAConstants.MISSINGVALUE;
		private double MAX = JOAConstants.MISSINGVALUE;
		private double INC = JOAConstants.MISSINGVALUE;
		private int numTriplets = 0;
		private Triplet[] triplets = new Triplet[25];

		private class DeciNotifyStr extends HandlerBase {
			public void startDocument() throws SAXException {
				numTriplets = 0;
				for (int i = 0; i < 25; i++) {
					triplets[i] = null;
				}
			}

			public void startElement(String name, AttributeList amap) throws SAXException {
				if (name.equals("customdecimation")) {
					KEY_STATE = CUD_KEY;
				}
				else if (name.equals("minvalue")) {
					KEY_STATE = MIN_KEY;
				}
				else if (name.equals("maxvalue")) {
					KEY_STATE = MAX_KEY;
				}
				else if (name.equals("increment")) {
					KEY_STATE = INC_KEY;
				}
				else {
					KEY_STATE = NO_KEY;
				}
			}

			public void characters(char[] ch, int start, int len) throws SAXException {
				String strVal = new String(ch, start, len);
				if (KEY_STATE == MIN_KEY) {
					try {
						MIN = Double.valueOf(strVal).doubleValue();
					}
					catch (Exception ex) {
						MIN = JOAConstants.MISSINGVALUE;
					}
				}
				else if (KEY_STATE == MAX_KEY) {
					try {
						MAX = Double.valueOf(strVal).doubleValue();
					}
					catch (Exception ex) {
						MAX = JOAConstants.MISSINGVALUE;
					}
				}
				else if (KEY_STATE == INC_KEY) {
					try {
						INC = Double.valueOf(strVal).doubleValue();
					}
					catch (Exception ex) {
						INC = JOAConstants.MISSINGVALUE;
					}
				}
			}

			public void endElement(String name) throws SAXException {
				if (name.equals("increment")) {
					// build a new triplet
					triplets[numTriplets++] = new Triplet(MIN, MAX, INC);
				}
			}
		}

		@SuppressWarnings("unchecked")
		public void readDecimations(File inFile) throws PreferencesErrorException {
			try {
				Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
				org.xml.sax.Parser parser = (org.xml.sax.Parser) c.newInstance();
				DeciNotifyStr notifyStr = new DeciNotifyStr();
				parser.setDocumentHandler(notifyStr);
				parser.parse(inFile.getPath());
			}
			catch (Exception ex) {
				throw new PreferencesErrorException("Couldn't read custom decimations");
			}
		}
	}

	public class FontPrefsPanel extends JPanel implements ActionListener, ItemListener {
		JOAJButton mLoadStyleBtn = null;
		JOAJButton mSaveStyleButton = null;

		public FontPrefsPanel() {
			// init the interface
			init();
		}
		
		public void refreshUI() {
			mFontSettings1.setFontName(JOAConstants.DEFAULT_AXIS_VALUE_FONT);
			mFontSettings1.setFontSize(JOAConstants.DEFAULT_AXIS_VALUE_SIZE);
			mFontSettings1.setFontStyle(JOAConstants.DEFAULT_AXIS_VALUE_STYLE);
			mFontSettings1.setColor(JOAConstants.DEFAULT_AXIS_VALUE_COLOR);
			
			mFontSettings2.setFontName(JOAConstants.DEFAULT_AXIS_LABEL_FONT);
			mFontSettings2.setFontSize(JOAConstants.DEFAULT_AXIS_LABEL_SIZE);
			mFontSettings2.setFontStyle(JOAConstants.DEFAULT_AXIS_LABEL_STYLE);
			mFontSettings2.setColor(JOAConstants.DEFAULT_AXIS_LABEL_COLOR);
			
			mFontSettings3.setFontName(JOAConstants.DEFAULT_ISOPYCNAL_LABEL_FONT);
			mFontSettings3.setFontSize(JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE);
			mFontSettings3.setFontStyle(JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE);
			mFontSettings3.setColor(JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR);
			
			mFontSettings4.setFontName(JOAConstants.DEFAULT_PLOT_TITLE_FONT);
			mFontSettings4.setFontSize(JOAConstants.DEFAULT_PLOT_TITLE_SIZE);
			mFontSettings4.setFontStyle(JOAConstants.DEFAULT_PLOT_TITLE_STYLE);
			mFontSettings4.setColor(JOAConstants.DEFAULT_PLOT_TITLE_COLOR);
			
			mFontSettings5.setFontName(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT);
			mFontSettings5.setFontSize(JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE);
			mFontSettings5.setFontStyle(JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE);
			mFontSettings5.setColor(JOAConstants.DEFAULT_COLORBAR_LABEL_COLOR);

			mFontSettings6.setFontName(JOAConstants.DEFAULT_MAP_VALUE_FONT);
			mFontSettings6.setFontSize(JOAConstants.DEFAULT_MAP_VALUE_SIZE);
			mFontSettings6.setFontStyle(JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE);
			mFontSettings6.setColor(JOAConstants.DEFAULT_MAP_VALUE_COLOR);
			
			mFontSettings7.setFontName(JOAConstants.DEFAULT_MAP_STN_LABEL_FONT);
			mFontSettings7.setFontSize(JOAConstants.DEFAULT_MAP_STN_LABEL_SIZE);
			mFontSettings7.setFontStyle(JOAConstants.DEFAULT_MAP_STN_LABEL_STYLE);
			mFontSettings7.setColor(JOAConstants.DEFAULT_MAP_STN_LABEL_COLOR);
			
			mFontSettings8.setFontName(JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_FONT);
			mFontSettings8.setFontSize(JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_SIZE);
			mFontSettings8.setFontStyle(JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_STYLE);
			mFontSettings8.setColor(JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_COLOR);
			
			mFontSettings9.setFontName(JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_FONT);
			mFontSettings9.setFontSize(JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_SIZE);
			mFontSettings9.setFontStyle(JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_STYLE);
			mFontSettings9.setColor(JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_COLOR);
			
			mFontSettings10.setFontName(JOAConstants.DEFAULT_REGRESSION_FONT);
			mFontSettings10.setFontSize(JOAConstants.DEFAULT_REGRESSION_FONT_SIZE);
			mFontSettings10.setFontStyle(JOAConstants.DEFAULT_REGRESSION_FONT_STYLE);
			mFontSettings10.setColor(JOAConstants.DEFAULT_REGRESSION_FONT_COLOR);
		}

		public void init() {
			this.setLayout(new BorderLayout(5, 5));
			JPanel contcont = new JPanel();
			contcont.setLayout(new BorderLayout(5, 5));
			JPanel cont = new JPanel();
			cont.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 5));
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			// long start = System.currentTimeMillis();
			Font fonts[] = env.getAllFonts();
			// long end = System.currentTimeMillis();

			// System.out.println("elapsed time = " + (end - start) + " for " +
			// fonts.length + " fonts");

			mMasterFontList = new JOAJComboBox();
			for (int i = 0; i < fonts.length; i++) {
				mMasterFontList.addItem(fonts[i].getName());
			}

			// line 0
			JPanel linem1 = new JPanel();
			linem1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			linem1.add(new JOAJLabel(b.getString("kSetAllFonts")));
			linem1.add(mMasterFontList);
			contcont.add("North", linem1);
			mMasterFontList.addItemListener(this);

			// line 1
			JPanel line0 = new JPanel();
			line0.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			line0
			    .add(new JOAJLabel(
			        "Font                                                 Size                                 Style           Color"));
			cont.add(line0);

			// line 1
			JPanel line1 = new JPanel();
			line1.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings1 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_AXIS_VALUE_FONT,
			    JOAConstants.DEFAULT_AXIS_VALUE_SIZE, JOAConstants.DEFAULT_AXIS_VALUE_STYLE,
			    JOAConstants.DEFAULT_AXIS_VALUE_COLOR);
			line1.add(new JOAJLabel(b.getString("kAxisValuesLabels")));
			line1.add(mFontSettings1);
			cont.add(line1);

			// line 2
			JPanel line2 = new JPanel();
			line2.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings2 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_AXIS_LABEL_FONT,
			    JOAConstants.DEFAULT_AXIS_LABEL_SIZE, JOAConstants.DEFAULT_AXIS_LABEL_STYLE,
			    JOAConstants.DEFAULT_AXIS_LABEL_COLOR);
			line2.add(new JOAJLabel(b.getString("kAxisLabels")));
			line2.add(mFontSettings2);
			cont.add(line2);

			// line 3
			JPanel line3 = new JPanel();
			line3.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings3 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_FONT,
			    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE,
			    JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR);
			line3.add(new JOAJLabel(b.getString("kIsopycnalLabels")));
			line3.add(mFontSettings3);
			cont.add(line3);

			// line 4
			JPanel line4 = new JPanel();
			line4.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings4 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
			    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
			    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);
			line4.add(new JOAJLabel(b.getString("kPlotTitleLabels")));
			line4.add(mFontSettings4);
			cont.add(line4);

			// line 5
			JPanel line5 = new JPanel();
			line5.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings5 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_COLORBAR_LABEL_FONT,
			    JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
			    JOAConstants.DEFAULT_COLORBAR_LABEL_COLOR);
			line5.add(new JOAJLabel(b.getString("kColorBarLabels")));
			line5.add(mFontSettings5);
			cont.add(line5);

			// line 6
			JPanel line6 = new JPanel();
			line6.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings6 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_MAP_VALUE_FONT,
			    JOAConstants.DEFAULT_MAP_VALUE_SIZE, JOAConstants.DEFAULT_MAP_VALUE_STYLE,
			    JOAConstants.DEFAULT_MAP_VALUE_COLOR);
			line6.add(new JOAJLabel(b.getString("kMapLabels")));
			line6.add(mFontSettings6);
			cont.add(line6);

			// line 7
			JPanel line7 = new JPanel();
			line7.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings7 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_MAP_STN_LABEL_FONT,
			    JOAConstants.DEFAULT_MAP_STN_LABEL_SIZE, JOAConstants.DEFAULT_MAP_STN_LABEL_STYLE,
			    JOAConstants.DEFAULT_MAP_STN_LABEL_COLOR);
			line7.add(new JOAJLabel(b.getString("kMapStnLabels")));
			line7.add(mFontSettings7);
			cont.add(line7);

			// line 8
			JPanel line8 = new JPanel();
			line8.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings8 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_FONT,
			    JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_SIZE, JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_STYLE,
			    JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_COLOR);
			line8.add(new JOAJLabel(b.getString("kXSecValues")));
			line8.add(mFontSettings8);
			cont.add(line8);

			// line 9
			JPanel line9 = new JPanel();
			line9.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings9 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_FONT,
			    JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_SIZE, JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_STYLE,
			    JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_COLOR);
			line9.add(new JOAJLabel(b.getString("kXSecLabels")));
			line9.add(mFontSettings9);
			cont.add(line9);
			
			JPanel line10 = new JPanel();
			line10.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mFontSettings10 = new FontSettingsPanel(fonts, JOAConstants.DEFAULT_REGRESSION_FONT,
			    JOAConstants.DEFAULT_REGRESSION_FONT_SIZE, JOAConstants.DEFAULT_REGRESSION_FONT_STYLE,
			    JOAConstants.DEFAULT_REGRESSION_FONT_COLOR);
			line10.add(new JOAJLabel("Regression line labels:"));
			line10.add(mFontSettings10);
			cont.add(line10);

			mSaveStyleButton = new JOAJButton(b.getString("kSaveStyle"));
			mSaveStyleButton.setActionCommand("savestyle");
			mLoadStyleBtn = new JOAJButton(b.getString("kLoadStyle"));
			mLoadStyleBtn.setActionCommand("loadstyle");
			JPanel dlgBtnsInset2 = new JPanel();
			JPanel dlgBtnsPanel2 = new JPanel();
			dlgBtnsInset2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
			dlgBtnsPanel2.setLayout(new GridLayout(1, 4, 15, 1));
			dlgBtnsPanel2.add(mLoadStyleBtn);
			dlgBtnsPanel2.add(mSaveStyleButton);
			dlgBtnsInset2.add(dlgBtnsPanel2);
			contcont.add("South", dlgBtnsInset2);
			contcont.add("Center", cont);
			this.add(new TenPixelBorder(contcont, 5, 5, 5, 5));

			mSaveStyleButton.addActionListener(this);
			mLoadStyleBtn.addActionListener(this);
		}

		public void itemStateChanged(ItemEvent evt) {
			if (evt.getSource() instanceof JOAJComboBox) {
				JOAJComboBox cb = (JOAJComboBox) evt.getSource();
				if (cb == mMasterFontList && evt.getStateChange() == ItemEvent.SELECTED) {
					String fName = (String) cb.getSelectedItem();
					mFontSettings1.setFontName(fName);
					mFontSettings2.setFontName(fName);
					mFontSettings3.setFontName(fName);
					mFontSettings4.setFontName(fName);
					mFontSettings5.setFontName(fName);
					mFontSettings6.setFontName(fName);
					mFontSettings7.setFontName(fName);
					mFontSettings8.setFontName(fName);
					mFontSettings9.setFontName(fName);
					mFontSettings10.setFontName(fName);
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			if (cmd.equals("savestyle")) {
				saveStyle();
			}
			else if (cmd.equals("loadstyle")) {
				// get a filename
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.endsWith("_style.xml")) {
							return true;
						}
						else {
							return false;
						}
					}
				};
				Frame fr = new Frame();
				String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
				FileDialog f = new FileDialog(fr, "Read style sheet from:", FileDialog.LOAD);
				f.setDirectory(directory);
				f.setFilenameFilter(filter);
				f.setVisible(true);
				directory = f.getDirectory();
				String fs = f.getFile();
				f.dispose();
				if (directory != null && fs != null) {
					File nf = new File(directory, fs);
					try {
						readStyle(nf);
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		public void saveStyle() {
			// get a filename
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith("_style.xml")) {
						return true;
					}
					else {
						return false;
					}
				}
			};
			Frame fr = new Frame();
			String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
			FileDialog f = new FileDialog(fr, "Save style sheet as:", FileDialog.SAVE);
			f.setDirectory(directory);
			f.setFilenameFilter(filter);
			f.setFile("untitled_style.xml");
			f.setVisible(true);
			directory = f.getDirectory();
			f.dispose();
			if (directory != null && f.getFile() != null) {
				File nf = new File(directory, f.getFile());
				saveStyles(nf);
			}
		}

		public void saveStyles(File file) {
			// save preferences as XML
			try {
				// create a documentobject
				Document doc = (Document) Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

				// make joapreferences the root element
				Element root = doc.createElement("stylesheet");

				// font style prefs
				root.setAttribute("axisvaluefont", mFontSettings1.getFontName());
				root.setAttribute("axislabelfont", mFontSettings2.getFontName());
				root.setAttribute("isopycnalfont", mFontSettings3.getFontName());
				root.setAttribute("plottitlefont", new String(mFontSettings4.getFontName()));
				root.setAttribute("colorbarfont", new String(mFontSettings5.getFontName()));
				root.setAttribute("mapvaluefont", new String(mFontSettings6.getFontName()));
				root.setAttribute("mapstnlabelfont", new String(mFontSettings7.getFontName()));
				root.setAttribute("contourxsecvaluefont", new String(mFontSettings8.getFontName()));
				root.setAttribute("contourxseclabelfont", new String(mFontSettings9.getFontName()));
				root.setAttribute("regressionlabelfont", new String(mFontSettings10.getFontName()));

				root.setAttribute("axisvaluesize", String.valueOf(mFontSettings1.getFontSize()));
				root.setAttribute("axislabelsize", String.valueOf(mFontSettings2.getFontSize()));
				root.setAttribute("isopycnalsize", String.valueOf(mFontSettings3.getFontSize()));
				root.setAttribute("plottitlesize", String.valueOf(mFontSettings4.getFontSize()));
				root.setAttribute("colorbarsize", String.valueOf(mFontSettings5.getFontSize()));
				root.setAttribute("mapvaluesize", String.valueOf(mFontSettings6.getFontSize()));
				root.setAttribute("mapstnlabelsize", String.valueOf(mFontSettings7.getFontSize()));
				root.setAttribute("contourxsecvaluesize", String.valueOf(mFontSettings8.getFontSize()));
				root.setAttribute("contourxseclabelsize", String.valueOf(mFontSettings9.getFontSize()));
				root.setAttribute("regressionlabelsize", String.valueOf(mFontSettings10.getFontSize()));

				root.setAttribute("axisvaluestyle", String.valueOf(mFontSettings1.getFontStyle()));
				root.setAttribute("axislabelstyle", String.valueOf(mFontSettings2.getFontStyle()));
				root.setAttribute("isopycnalstyle", String.valueOf(mFontSettings3.getFontStyle()));
				root.setAttribute("plottitlestyle", String.valueOf(mFontSettings4.getFontStyle()));
				root.setAttribute("colorbarstyle", String.valueOf(mFontSettings5.getFontStyle()));
				root.setAttribute("mapvaluestyle", String.valueOf(mFontSettings6.getFontStyle()));
				root.setAttribute("mapstnlabelstyle", String.valueOf(mFontSettings7.getFontStyle()));
				root.setAttribute("contourxsecvaluestyle", String.valueOf(mFontSettings8.getFontStyle()));
				root.setAttribute("contourxseclabelstyle", String.valueOf(mFontSettings9.getFontStyle()));
				root.setAttribute("regressionlabelstyle", String.valueOf(mFontSettings10.getFontStyle()));

				Color c1 = mFontSettings1.getColor();
				Color c2 = mFontSettings2.getColor();
				Color c3 = mFontSettings3.getColor();
				Color c4 = mFontSettings4.getColor();
				Color c5 = mFontSettings5.getColor();
				Color c6 = mFontSettings6.getColor();
				Color c7 = mFontSettings7.getColor();
				Color c8 = mFontSettings8.getColor();
				Color c9 = mFontSettings9.getColor();
				Color c10 = mFontSettings10.getColor();
				Element subitem = doc.createElement("axisvaluecolor");
				subitem.setAttribute("red", String.valueOf(c1.getRed()));
				subitem.setAttribute("green", String.valueOf(c1.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c1.getBlue()));
				root.appendChild(subitem);
				subitem = doc.createElement("axislabelcolor");
				subitem.setAttribute("red", String.valueOf(c2.getRed()));
				subitem.setAttribute("green", String.valueOf(c2.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c2.getBlue()));
				root.appendChild(subitem);
				subitem = doc.createElement("isopycnalcolor");
				subitem.setAttribute("red", String.valueOf(c3.getRed()));
				subitem.setAttribute("green", String.valueOf(c3.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c3.getBlue()));
				root.appendChild(subitem);
				subitem = doc.createElement("plottitlecolor");
				subitem.setAttribute("red", String.valueOf(c4.getRed()));
				subitem.setAttribute("green", String.valueOf(c4.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c4.getBlue()));
				root.appendChild(subitem);
				subitem = doc.createElement("colorbarcolor");
				subitem.setAttribute("red", String.valueOf(c5.getRed()));
				subitem.setAttribute("green", String.valueOf(c5.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c5.getBlue()));
				root.appendChild(subitem);
				subitem = doc.createElement("mapvaluecolor");
				subitem.setAttribute("red", String.valueOf(c6.getRed()));
				subitem.setAttribute("green", String.valueOf(c6.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c6.getBlue()));
				root.appendChild(subitem);
				subitem = doc.createElement("mapstnlabelcolor");
				subitem.setAttribute("red", String.valueOf(c7.getRed()));
				subitem.setAttribute("green", String.valueOf(c7.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c7.getBlue()));
				root.appendChild(subitem);
				subitem = doc.createElement("contourxsecvaluecolor");
				subitem.setAttribute("red", String.valueOf(c8.getRed()));
				subitem.setAttribute("green", String.valueOf(c8.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c8.getBlue()));
				root.appendChild(subitem);
				subitem = doc.createElement("contourxseclabelcolor");
				subitem.setAttribute("red", String.valueOf(c9.getRed()));
				subitem.setAttribute("green", String.valueOf(c9.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c9.getBlue()));
				root.appendChild(subitem);
				subitem = doc.createElement("regressionlabelcolor");
				subitem.setAttribute("red", String.valueOf(c10.getRed()));
				subitem.setAttribute("green", String.valueOf(c10.getGreen()));
				subitem.setAttribute("blue", String.valueOf(c10.getBlue()));
				root.appendChild(subitem);
				doc.appendChild(root);
				((TXDocument) doc).setVersion("1.0");
				((TXDocument) doc).printWithFormat(new FileWriter(file));
			}
			catch (Exception ex) {

			}
		}

		@SuppressWarnings("unchecked")
		public void readStyle(File inFile) throws PreferencesErrorException {
			try {
				Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
				org.xml.sax.Parser parser = (org.xml.sax.Parser) c.newInstance();
				StyleNotifyStr notifyStr = new StyleNotifyStr();
				parser.setDocumentHandler(notifyStr);
				parser.parse(inFile.getPath());
			}
			catch (Exception ex) {
				throw new PreferencesErrorException("Couldn't read style sheet!");
			}
		}

		private class StyleNotifyStr extends HandlerBase {
			String axisValueFont = null;
			String axisLabelFont = null;
			String isopycnalLabelFont = null;
			String plotTitleFont = null;
			String colorBarFont = null;
			String mapValueFont = null;
			String mapStnFont = null;
			String contourXsecValueFont = null;
			String contourXsecLabelFont = null;
			String regressionLabelFont = null;

			int axisValueSize;
			int axisLabelSize;
			int isopycnalSize;
			int plotTitleSize;
			int colorBarSize;
			int mapValueSize;
			int mapStnSize;
			int contourXsecValueSize;
			int contourXsecLabelSize;
			int regressionLabelSize;

			int axisValueStyle;
			int axisLabelStyle;
			int isopycnalLabelStyle;
			int plotTitleStyle;
			int colorBarStyle;
			int mapValueStyle;
			int mapStnStyle;
			int contourXsecValueStyle;
			int contourXsecLabelStyle;
			int regressionLabelStyle;

			Color axisValueColor;
			Color axisLabelColor;
			Color isopycnalLabelColor;
			Color plotTitleColor;
			Color colorBarColor;
			Color mapValueColor;
			Color mapStnColor;
			Color contourXsecValueColor;
			Color contourXsecLabelColor;
			Color regressionLabelColor;

			public void startDocument() throws SAXException {
			}

			public void startElement(String name, AttributeList amap) throws SAXException {
				if (name.equals("stylesheet")) {
					for (int i = 0; i < amap.getLength(); i++) {
						try {
							if (amap.getName(i).equals("axisvaluefont")) {
								axisValueFont = new String(amap.getValue(i));
							}
							else if (amap.getName(i).equals("axislabelfont")) {
								axisLabelFont = new String(amap.getValue(i));
							}
							else if (amap.getName(i).equals("isopycnalfont")) {
								isopycnalLabelFont = new String(amap.getValue(i));
							}
							else if (amap.getName(i).equals("plottitlefont")) {
								plotTitleFont = new String(amap.getValue(i));
							}
							else if (amap.getName(i).equals("colorbarfont")) {
								colorBarFont = new String(amap.getValue(i));
							}
							else if (amap.getName(i).equals("mapvaluefont")) {
								mapValueFont = new String(amap.getValue(i));
							}
							else if (amap.getName(i).equals("mapstnlabelfont")) {
								mapStnFont = new String(amap.getValue(i));
							}
							else if (amap.getName(i).equals("contourxsecvaluefont")) {
								contourXsecValueFont = new String(amap.getValue(i));
							}
							else if (amap.getName(i).equals("contourxseclabelfont")) {
								contourXsecLabelFont = new String(amap.getValue(i));
							}
							else if (amap.getName(i).equals("regressionlabelfont")) {
								regressionLabelFont = new String(amap.getValue(i));
							}

							if (amap.getName(i).equals("axislabelsize")) {
								axisLabelSize = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("axisvaluesize")) {
								axisValueSize = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("isopycnalsize")) {
								isopycnalSize = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("plottitlesize")) {
								plotTitleSize = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("colorbarsize")) {
								colorBarSize = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("mapvaluesize")) {
								mapValueSize = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("mapstnlabelsize")) {
								mapStnSize = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("contourxsecvaluesize")) {
								contourXsecValueSize = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("contourxseclabelsize")) {
								contourXsecLabelSize = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("regressionlabelsize")) {
								regressionLabelSize = Integer.valueOf(amap.getValue(i)).intValue();
							}

							if (amap.getName(i).equals("axislabelstyle")) {
								axisLabelStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("axisvaluestyle")) {
								axisValueStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("isopycnalstyle")) {
								isopycnalLabelStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("plottitlestyle")) {
								plotTitleStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("colorbarstyle")) {
								colorBarStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("mapvaluestyle")) {
								mapValueStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("mapstnlabelstyle")) {
								mapStnStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("contourxsecvaluestyle")) {
								contourXsecValueStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("contourxseclabelstyle")) {
								contourXsecLabelStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
							else if (amap.getName(i).equals("regressionlabelstyle")) {
								contourXsecLabelStyle = Integer.valueOf(amap.getValue(i)).intValue();
							}
						}
						catch (Exception ex) {
						}
					}
				}
				else if (name.equals("mapvaluecolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					mapValueColor = new Color(red, green, blue);
				}
				else if (name.equals("mapstnlabelcolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					mapStnColor = new Color(red, green, blue);
				}
				else if (name.equals("axislabelcolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					axisLabelColor = new Color(red, green, blue);
				}
				else if (name.equals("axisvaluecolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					axisValueColor = new Color(red, green, blue);
				}
				else if (name.equals("isopycnalcolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					isopycnalLabelColor = new Color(red, green, blue);
				}
				else if (name.equals("plottitlecolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					plotTitleColor = new Color(red, green, blue);
				}
				else if (name.equals("colorbarcolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					colorBarColor = new Color(red, green, blue);
				}
				else if (name.equals("contourxsecvaluecolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					contourXsecValueColor = new Color(red, green, blue);
				}
				else if (name.equals("contourxseclabelcolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					contourXsecLabelColor = new Color(red, green, blue);
				}
				else if (name.equals("regressionlabelcolor")) {
					int red = 0, green = 0, blue = 0;
					for (int i = 0; i < amap.getLength(); i++) {
						if (amap.getName(i).equals("red")) {
							try {
								red = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								red = 150;
							}
						}
						else if (amap.getName(i).equals("green")) {
							try {
								green = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								green = 150;
							}
						}
						else if (amap.getName(i).equals("blue")) {
							try {
								blue = Integer.valueOf(amap.getValue(i)).intValue();
							}
							catch (Exception ex) {
								blue = 150;
							}
						}
					}
					regressionLabelColor = new Color(red, green, blue);
				}

				// set the UI
				mFontSettings1.setFontName(axisValueFont);
				mFontSettings2.setFontName(axisLabelFont);
				mFontSettings3.setFontName(isopycnalLabelFont);
				mFontSettings4.setFontName(plotTitleFont);
				mFontSettings5.setFontName(colorBarFont);
				mFontSettings6.setFontName(mapValueFont);
				mFontSettings7.setFontName(mapStnFont);
				mFontSettings8.setFontName(contourXsecValueFont);
				mFontSettings9.setFontName(contourXsecLabelFont);
				mFontSettings10.setFontName(regressionLabelFont);

				mFontSettings1.setFontStyle(axisValueStyle);
				mFontSettings2.setFontStyle(axisLabelStyle);
				mFontSettings3.setFontStyle(isopycnalLabelStyle);
				mFontSettings4.setFontStyle(plotTitleStyle);
				mFontSettings5.setFontStyle(colorBarStyle);
				mFontSettings6.setFontStyle(mapValueStyle);
				mFontSettings7.setFontStyle(mapStnStyle);
				mFontSettings8.setFontStyle(contourXsecValueStyle);
				mFontSettings9.setFontStyle(contourXsecLabelStyle);
				mFontSettings10.setFontStyle(regressionLabelStyle);

				mFontSettings1.setFontSize(axisValueSize);
				mFontSettings2.setFontSize(axisLabelSize);
				mFontSettings3.setFontSize(isopycnalSize);
				mFontSettings4.setFontSize(plotTitleSize);
				mFontSettings5.setFontSize(colorBarSize);
				mFontSettings6.setFontSize(mapValueSize);
				mFontSettings7.setFontSize(mapStnSize);
				mFontSettings8.setFontSize(contourXsecValueSize);
				mFontSettings9.setFontSize(contourXsecLabelSize);
				mFontSettings10.setFontSize(regressionLabelSize);

				if (axisValueColor != null) {
					mFontSettings1.setColor(axisValueColor);
				}
				if (axisLabelColor != null) {
					mFontSettings2.setColor(axisLabelColor);
				}
				if (isopycnalLabelColor != null) {
					mFontSettings3.setColor(isopycnalLabelColor);
				}
				if (plotTitleColor != null) {
					mFontSettings4.setColor(plotTitleColor);
				}
				if (colorBarColor != null) {
					mFontSettings5.setColor(colorBarColor);
				}
				if (mapValueColor != null) {
					mFontSettings6.setColor(mapValueColor);
				}
				if (mapStnColor != null) {
					mFontSettings7.setColor(mapStnColor);
				}
				if (contourXsecValueColor != null) {
					mFontSettings8.setColor(contourXsecValueColor);
				}
				if (contourXsecLabelColor != null) {
					mFontSettings9.setColor(contourXsecLabelColor);
				}
				if (regressionLabelColor != null) {
					mFontSettings10.setColor(regressionLabelColor);
				}
			}

			public void characters(char[] ch, int start, int len) throws SAXException {
			}

			public void endElement(String name) throws SAXException {
			}
		}
	}

	private class WOCEImportPrefsPanel extends JPanel implements ItemListener {
		public WOCEImportPrefsPanel() {
			// init the interface
			init();
		}
		
		public void refreshUI() {
			mTranslateWOCEToIGOSS.setSelected(JOAConstants.DEFAULT_CONVERT_QCS);
			mConvertTemps.setSelected(JOAConstants.DEFAULT_CONVERT_WOCE_TEMPS);
			mReplQB3.setSelected(JOAConstants.DEFAULT_SET_MSG_QBEQ3);
			mReplQB7.setSelected(JOAConstants.DEFAULT_SET_MSG_QBEQ7);
			mReplQB4 .setSelected(JOAConstants.DEFAULT_SET_MSG_QBEQ4);
			mReplQB8.setSelected(JOAConstants.DEFAULT_SET_MSG_QBEQ8);
			mReplAllBQB4.setSelected(JOAConstants.DEFAULT_SET_ALL_PARAMS_MSG_BQBEQ4);
			mReplGasQB34.setSelected(JOAConstants.DEFAULT_SET_GAS_PARAMS_MSG_BQBEQ3_AND_O2QBEQ4);
			mConvertMass.setSelected(JOAConstants.DEFAULT_CONVERT_MASS_TO_VOL);
			mTranslateParams.setSelected(JOAConstants.DEFAULT_TRANSLATE_PARAM_NAMES);
			mConvertDepth.setSelected(JOAConstants.DEFAULT_CONVERT_DEPTH);		
		}

		public void init() {
			JPanel mainPanel = new JPanel(); // everything goes in here
			mainPanel.setLayout(new BorderLayout(5, 5));

			JPanel wocePanel = new JPanel();
			wocePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));

			JPanel line0 = new JPanel();
			line0.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			mTranslateWOCEToIGOSS = new JOAJCheckBox(b.getString("kTranslateQC"), JOAConstants.DEFAULT_CONVERT_QCS);
			line0.add(mTranslateWOCEToIGOSS);
			wocePanel.add(line0);

			JPanel line1 = new JPanel();
			line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			mConvertTemps = new JOAJCheckBox(b.getString("kConvertTemp"), JOAConstants.DEFAULT_CONVERT_WOCE_TEMPS);
			line1.add(mConvertTemps);
			wocePanel.add(line1);

			JPanel line3 = new JPanel();
			line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			line3.add(new JOAJLabel(b.getString("kReplaceMsg1")));
			wocePanel.add(line3);

			JPanel line4 = new JPanel();
			line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			JPanel line4g = new JPanel();
			line4g.setLayout(new GridLayout(2, 2, 0, 0));
			mReplQB3 = new JOAJCheckBox(b.getString("kReplQB3"), JOAConstants.DEFAULT_SET_MSG_QBEQ3);
			line4g.add(mReplQB3);
			mReplQB7 = new JOAJCheckBox(b.getString("kReplQB7"), JOAConstants.DEFAULT_SET_MSG_QBEQ7);
			line4g.add(mReplQB7);
			mReplQB4 = new JOAJCheckBox(b.getString("kReplQB4"), JOAConstants.DEFAULT_SET_MSG_QBEQ4);
			line4g.add(mReplQB4);
			mReplQB8 = new JOAJCheckBox(b.getString("kReplQB8"), JOAConstants.DEFAULT_SET_MSG_QBEQ8);
			line4g.add(mReplQB8);
			line4.add(new JOAJLabel("   "));
			line4.add(line4g);
			wocePanel.add(line4);

			JPanel line5 = new JPanel();
			line5.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			mReplAllBQB4 = new JOAJCheckBox(b.getString("kReplaceAllBottleParams"),
			    JOAConstants.DEFAULT_SET_ALL_PARAMS_MSG_BQBEQ4);
			line5.add(mReplAllBQB4);
			wocePanel.add(line5);

			JPanel line6 = new JPanel();
			line6.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			mReplGasQB34 = new JOAJCheckBox(b.getString("kReplaceAllGasParams"),
			    JOAConstants.DEFAULT_SET_GAS_PARAMS_MSG_BQBEQ3_AND_O2QBEQ4);
			line6.add(mReplGasQB34);
			wocePanel.add(line6);

			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kWOCE2"));
			wocePanel.setBorder(tb);

			JPanel genPanel = new JPanel();
			genPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));

			JPanel g1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mConvertMass = new JOAJCheckBox(b.getString("kConvertMassToVol"), JOAConstants.DEFAULT_CONVERT_MASS_TO_VOL);
			g1.add(mConvertMass);
			genPanel.add(g1);

			JPanel g2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mTranslateParams = new JOAJCheckBox(b.getString("kTranslateParamNames"),
			    JOAConstants.DEFAULT_TRANSLATE_PARAM_NAMES);
			g2.add(mTranslateParams);
			genPanel.add(g2);

			JPanel g3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
			mConvertDepth = new JCheckBox(b.getString("kConvertDepth"), JOAConstants.DEFAULT_CONVERT_DEPTH);
			g3.add(mConvertDepth);
			genPanel.add(g3);

			tb = BorderFactory.createTitledBorder(b.getString("kGeneral"));
			genPanel.setBorder(tb);

			JPanel upperPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			upperPanel.add(wocePanel);
			upperPanel.add(genPanel);

			// add all the sub panels to main panel
			mainPanel.add(new TenPixelBorder(upperPanel, 5, 5, 5, 5), "Center");
			this.add(mainPanel);
		}

		public void itemStateChanged(ItemEvent evt) {
			if (evt.getSource() instanceof JOAJComboBox) {
			}
		}
	}
	
	public class WODSelectImportPrefsPanel extends JPanel implements ItemListener {
		private JOAJRadioButton mOrigCastIDWSub;
		private JOAJRadioButton mWODCastID;
		private JOAJRadioButton mOrigSectionIDWSub;
		private JOAJRadioButton mNODCCruiseID;
		private JOAJCheckBox mPreferPressParam;
		private JOAJCheckBox mConvertFromMeters;
		private JOAJCheckBox mConvertITS90;
		private JOAJCheckBox mApplyCorrections;
		private JOAJRadioButton mConvertToWOCE;
		private JOAJRadioButton mConvertToWMO;
		private JOAJCheckBox mUseOriginatorsQC;
		private JOAJCheckBox mTranslateParamNames;
		private JOAJCheckBox mCollectMetadata;
		private JOAJCheckBox mCastNumOrTow;
		
		public WODSelectImportPrefsPanel() {
			// init the interface
			init();
		}
		
		public void refreshUI() {
			mOrigCastIDWSub.setSelected(JOAConstants.DEFAULT_CAST_ID_RULE == CastIDRule.ORIG_STN_ID);
			mNODCCruiseID.setSelected(JOAConstants.DEFAULT_CAST_ID_RULE == CastIDRule.WOD_UNIQUE);
			mCastNumOrTow.setSelected(JOAConstants.DEFAULT_CAST_NUMBER_RULE == CastNumberRule.JOA_SUBSTITUTION);
			mOrigSectionIDWSub.setSelected(JOAConstants.DEFAULT_SECTION_ID_RULE == SectionIDRule.ORIG_CRUISE_ID);
			mWODCastID.setSelected(JOAConstants.DEFAULT_SECTION_ID_RULE == SectionIDRule.NODC_CRUISE_ID);
			mPreferPressParam .setSelected(JOAConstants.DEFAULT_PRES_PARAM_RULE == PreferPressureParameterRule.PREFER_PRESSURE_PARAMETER);
			mConvertFromMeters.setSelected(JOAConstants.DEFAULT_DEPTH_CONVERSION_RULE == DepthConversionRule.CONVERT_DEPTH_TO_PRESSURE);
			mConvertITS90 .setSelected(JOAConstants.DEFAULT_TEMP_CONV_RULE == TempConversionRule.ITS90_TO_IPTS68);
			mConvertToWOCE.setSelected(JOAConstants.DEFAULT_DEST_QC_RULE == DestinationQCRule.WOCE);
			mConvertToWMO.setSelected(JOAConstants.DEFAULT_DEST_QC_RULE == DestinationQCRule.WMO);
			mUseOriginatorsQC.setSelected(JOAConstants.DEFAULT_QC_PROCESSING_RULE == QCConversionRule.READ_ORIG_QC_FLAGS);
			mTranslateParamNames.setSelected(JOAConstants.DEFAULT_CONVERT_PARAM_NAMES_RULE == ConvertParameterNamesRule.CONVERT_TO_JOA_LEXICON);
			mCollectMetadata.setSelected(JOAConstants.DEFAULT_COLLECT_METADATA_RULE);
			mUseOriginatorsQC .setSelected(JOAConstants.DEFAULT_QC_PROCESSING_RULE == QCConversionRule.READ_ORIG_QC_FLAGS);
		}
		
		public boolean isCollectMetadata() {
			return mCollectMetadata.isSelected();
		}

		public ConvertParameterNamesRule getConvertParameterNamesRule() {
			if (mTranslateParamNames.isSelected()) {
				return ConvertParameterNamesRule.CONVERT_TO_JOA_LEXICON;
			}
			else {
				return ConvertParameterNamesRule.KEEP_WOD_PARAMETER_NAMES;
			}
		}
		
		public QCConversionRule getQCConversionRule() {
			if (mUseOriginatorsQC.isSelected()) {
				return QCConversionRule.READ_ORIG_QC_FLAGS;
			}
			else {
				return QCConversionRule.IGNORE_QC_FLAGS;
			}
		}
		
		public DestinationQCRule getDestinationQCRule() {
			if (mConvertToWOCE.isSelected()) {
				return DestinationQCRule.WOCE;
			}
			else {
				return DestinationQCRule.WMO;
			}
		}
		
		public TempConversionRule getTempConversionRule() {
			if (mConvertITS90.isSelected()) {
				return TempConversionRule.ITS90_TO_IPTS68;
			}
			else {
				return TempConversionRule.NO_TEMP_CONVERSION;
			}
		}
		
		public DepthConversionRule getDepthConversionRule() {
			if (mConvertFromMeters.isSelected()) {
				return DepthConversionRule.CONVERT_DEPTH_TO_PRESSURE;
			}
			else {
				return DepthConversionRule.USE_DEPTH_IN_METERS;
			}
		}
		
		public PreferPressureParameterRule getPreferPressureParameterRule() {
			if (mPreferPressParam.isSelected()) {
				return PreferPressureParameterRule.PREFER_PRESSURE_PARAMETER;
			}
			else {
				return PreferPressureParameterRule.USE_NODC_DEPTH_PARAMETER;
			}
		}
		
		public CastIDRule getCastIDRule() {
			if (mOrigCastIDWSub.isSelected()) {
				return CastIDRule.ORIG_STN_ID;
			}
			else {
				return CastIDRule.WOD_UNIQUE;
			}
		}
		
		public CastNumberRule getCastNumberRule() {
			if (mOrigCastIDWSub.isSelected()) {
				return CastNumberRule.JOA_SUBSTITUTION;
			}
			else {
				return CastNumberRule.CAST_TOW_ONLY;
			}
		}
		
		public SectionIDRule getSectionIDRule() {
			if (mOrigSectionIDWSub.isSelected()) {
				return SectionIDRule.ORIG_CRUISE_ID;
			}
			else {
				return SectionIDRule.NODC_CRUISE_ID;
			}
		}

		public void init() {
			JPanel mainPanel = new JPanel(new BorderLayout(5, 5));		
			JPanel panelPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			
			// panels
			JPanel castIDPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			TitledBorder tb = BorderFactory.createTitledBorder("Station ID Rule");
			castIDPanel.setBorder(tb);
			
			JPanel sectionIDPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			tb = BorderFactory.createTitledBorder("Section ID Rule");
			sectionIDPanel.setBorder(tb);
			
			JPanel castNumPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			tb = BorderFactory.createTitledBorder("Cast Sequence Number Rule");
			castNumPanel.setBorder(tb);
			
			JPanel depthPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			tb = BorderFactory.createTitledBorder("Sample Depth Rule");
			depthPanel.setBorder(tb);
			
			JPanel scalingPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			tb = BorderFactory.createTitledBorder("Scaling Rules");
			scalingPanel.setBorder(tb);
			
			JPanel qcPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			tb = BorderFactory.createTitledBorder("Quality Code Rules");
			qcPanel.setBorder(tb);
			
			JPanel otherPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			tb = BorderFactory.createTitledBorder("Other Options");
			otherPanel.setBorder(tb);
			
			// castIDPanel
			ButtonGroup castIDBG = new ButtonGroup();
			mOrigCastIDWSub = new JOAJRadioButton("Originator's station ID (substitute WOD Unique Cast Number if missing)", 
					JOAConstants.DEFAULT_CAST_ID_RULE == CastIDRule.ORIG_STN_ID);
			castIDPanel.add(mOrigCastIDWSub);
			mNODCCruiseID = new JOAJRadioButton("WOD Unique Cast Number", JOAConstants.DEFAULT_CAST_ID_RULE == CastIDRule.WOD_UNIQUE);
			castIDPanel.add(mNODCCruiseID);
			castIDBG.add(mOrigCastIDWSub);
			castIDBG.add(mNODCCruiseID);

			// castNumPanel
			mCastNumOrTow = new JOAJCheckBox("JOA assigns cast sequence number if \"Cast/Tow\" tag is missing", 
					JOAConstants.DEFAULT_CAST_NUMBER_RULE == CastNumberRule.JOA_SUBSTITUTION);
			castNumPanel.add(mCastNumOrTow);
			castNumPanel.add(new JLabel("    (unchecked => use \"Cast/Tow\" tag if present otherwise assign as missing"));
			
			// sectionIDPanel
			ButtonGroup sectionIDBG = new ButtonGroup();
			mOrigSectionIDWSub = new JOAJRadioButton("Originator's cruise ID (substitute NODC Cruise ID if missing)", 
					JOAConstants.DEFAULT_SECTION_ID_RULE == SectionIDRule.ORIG_CRUISE_ID);
			sectionIDPanel.add(mOrigSectionIDWSub);
			mWODCastID = new JOAJRadioButton("NODC Cruise ID", JOAConstants.DEFAULT_SECTION_ID_RULE == SectionIDRule.NODC_CRUISE_ID);
			sectionIDPanel.add(mWODCastID);
			sectionIDBG.add(mOrigSectionIDWSub);
			sectionIDBG.add(mWODCastID);
			
			// depthPanel
			mPreferPressParam = new JOAJCheckBox("Prefer pressure parameter over depth parameter if present", 
					JOAConstants.DEFAULT_PRES_PARAM_RULE == PreferPressureParameterRule.PREFER_PRESSURE_PARAMETER);
			mConvertFromMeters = new JOAJCheckBox("Convert sample depths in meters to decibars", 
					JOAConstants.DEFAULT_DEPTH_CONVERSION_RULE == DepthConversionRule.CONVERT_DEPTH_TO_PRESSURE);
			depthPanel.add(mPreferPressParam);
			depthPanel.add(new JLabel("    (unchecked => always use NODC's depth parameter"));
			depthPanel.add(mConvertFromMeters);
			depthPanel.add(new JLabel("    (unchecked => use NODC's depth parameter units (typically meters)"));
			
			// scalingPanel
			mConvertITS90 = new JOAJCheckBox("Convert Temperatures from ITS90 to IPTS68 when temperature scale is defined", 
					JOAConstants.DEFAULT_TEMP_CONV_RULE == TempConversionRule.ITS90_TO_IPTS68);
			scalingPanel.add(mConvertITS90);
			scalingPanel.add(new JLabel("    (unchecked => don't correct reported temperatures"));
//			mApplyCorrections = new JOAJCheckBox("Apply parameter value corrections if present", true);
//			scalingPanel.add(mApplyCorrections);
			
			// qcPanel
			JPanel qcline1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
			JPanel qcline2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
			JLabel lbl1 = new JLabel("     Convert QC Flag to:");
			mConvertToWOCE = new JOAJRadioButton("WOCE", JOAConstants.DEFAULT_DEST_QC_RULE == DestinationQCRule.WOCE);
			mConvertToWMO = new JOAJRadioButton("WMO", JOAConstants.DEFAULT_DEST_QC_RULE == DestinationQCRule.WMO);
			mUseOriginatorsQC = new JOAJCheckBox("Read originator's QC flags if present", 
					JOAConstants.DEFAULT_QC_PROCESSING_RULE == QCConversionRule.READ_ORIG_QC_FLAGS);
			ButtonGroup qcStdBG = new ButtonGroup();
			qcStdBG.add(mConvertToWOCE);
			qcStdBG.add(mConvertToWMO);
			qcline2.add(lbl1);
			qcline2.add(new JLabel("  "));
			qcline2.add(mConvertToWOCE);
			qcline2.add(mConvertToWMO);
			qcline1.add(mUseOriginatorsQC);
			qcline1.add(new JLabel("    (unchecked => no quality flag processing"));
			
			qcPanel.add(qcline1);
			qcPanel.add(qcline2);
			
			// otherPanel
			mTranslateParamNames = new JOAJCheckBox("Translate parameter names to JOA convention", 
					JOAConstants.DEFAULT_CONVERT_PARAM_NAMES_RULE == ConvertParameterNamesRule.CONVERT_TO_JOA_LEXICON);
			mCollectMetadata = new JOAJCheckBox("Collect cast metadata", JOAConstants.DEFAULT_COLLECT_METADATA_RULE);
			otherPanel.add(mTranslateParamNames);
			otherPanel.add(new JLabel("    (unchecked => use NODC's parameter names"));
			otherPanel.add(mCollectMetadata);
			
			// add all the sub panels to main panel
			panelPanel.add(sectionIDPanel);
			panelPanel.add(castIDPanel);
			panelPanel.add(castNumPanel);
			panelPanel.add(depthPanel);
			panelPanel.add(scalingPanel);
			panelPanel.add(qcPanel);
			panelPanel.add(otherPanel);
			
			mainPanel.add(new TenPixelBorder(panelPanel, 5, 5, 5, 5), BorderLayout.NORTH);
			this.add(mainPanel);
		}

		public void itemStateChanged(ItemEvent evt) {
			if (evt.getSource() instanceof JOAJComboBox) {
			}
		}
	}

	private class CustomDeciRanges extends JPanel {
		RangeDetail[] theRanges = new RangeDetail[25];
		Triplet[] theDecimations = new Triplet[25];

		public CustomDeciRanges() {
			init();
		}
		
		public void refreshUI() {
			//set values of the detail panels
		}

		private void init() {
			this.setLayout(new BorderLayout(5, 5));
			JPanel detail = new JPanel();
			detail.setLayout(new GridLayout(26, 3, 0, 0)); 
			JPanel colHeaders = new JPanel(new GridLayout(1, 3, 0, 0));
			colHeaders.add(new JLabel(" Start"));
			colHeaders.add(new JLabel("End "));
			colHeaders.add(new JLabel("Incr."));
			detail.add(colHeaders);

			for (int i = 0; i < 25; i++) {
				theRanges[i] = new RangeDetail();
				detail.add(theRanges[i]);
			}
			this.add(detail, "South");
		}

		public Triplet[] getDecimations() {
			for (int i = 0; i < 25; i++) {
				theDecimations[i] = getRange(i);
			}
			return theDecimations;
		}

		public Triplet getRange(int i) {
			return theRanges[i].getValues();
		}

		public void setRange(int i, Triplet vals) {
			theRanges[i].setValues(vals);
		}

		public void setEnabled(boolean state) {
			for (int i = 0; i < 25; i++) {
				theRanges[i].setEnabled(state);
			}

			if (state) {
				theRanges[0].selectFirst();
			}
		}
	}

	private class RangeDetail extends JPanel {
		private JOAJTextField mMinField = null;
		private JOAJTextField mMaxField = null;
		private JOAJTextField mIncField = null;

		public RangeDetail() {
			init();
		}
		
		public RangeDetail(Triplet vals) {
			init();
			this.setValues(vals);
		}
		
		private void init() {
			this.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
			mMinField = new JOAJTextField(4);
			this.add(mMinField);
			mMaxField = new JOAJTextField(4);
			this.add(mMaxField);
			mIncField = new JOAJTextField(4);
			this.add(mIncField);
		}

		public void setValues(Triplet vals) {
			mMinField.setText(JOAFormulas.formatDouble(String.valueOf(vals.getVal1()), 1, false));
			mMaxField.setText(JOAFormulas.formatDouble(String.valueOf(vals.getVal2()), 1, false));
			mIncField.setText(JOAFormulas.formatDouble(String.valueOf(vals.getVal3()), 1, false));
		}

		public Triplet getValues() {
			double min = JOAConstants.MISSINGVALUE;
			try {
				min = Double.valueOf(mMinField.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
			}
			double max = JOAConstants.MISSINGVALUE;
			try {
				max = Double.valueOf(mMaxField.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
			}
			double inc = JOAConstants.MISSINGVALUE;
			try {
				inc = Double.valueOf(mIncField.getText()).doubleValue();
			}
			catch (NumberFormatException ex) {
			}
			return new Triplet(min, max, inc);
		}

		public void setEnabled(boolean state) {
			mMinField.setEnabled(state);
			mMaxField.setEnabled(state);
			mIncField.setEnabled(state);
		}

		public void selectFirst() {
			mMinField.requestFocus();
			mMinField.selectAll();
		}
	}

	private class MyScroller extends JScrollPane {
		public MyScroller() {
			super();
			this.getVerticalScrollBar().setUnitIncrement(20);
		}

		public Dimension getPreferredSize() {
			return new Dimension(300, 150);
		}
	}

	private class ParamSubstitutionsPanel extends JPanel {
		protected JComboBox mSalinityVar = null;
		protected JComboBox mSalinitySubstitute = null;
		protected JComboBox mO2Var = null;
		protected JComboBox mO2Substitute = null;

		public ParamSubstitutionsPanel() {
			// init the interface
			init();
		}
		
		public void refreshUI() {
			mSalinityVar.setSelectedIndex(JOAConstants.DEFAULT_SALINITY_VARIABLE);
			mSalinitySubstitute.setSelectedIndex(JOAConstants.DEFAULT_SALINITY_SUBSTITUTION);
			mO2Var.setSelectedIndex(JOAConstants.DEFAULT_O2_VARIABLE);
			mO2Substitute.setSelectedIndex(JOAConstants.DEFAULT_O2_SUBSTITUTION);
		}

		public void init() {
			this.setLayout(new BorderLayout(5, 5));

			JPanel contPanel = new JPanel();
			contPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
			JPanel line4 = new JPanel();
			line4.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
			JPanel line5 = new JPanel();
			line5.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));

			Vector<String> mSalinityChoices = new Vector<String>();
			mSalinityChoices.add(b.getString("kBottleSalinity"));
			mSalinityChoices.add(b.getString("kCTDSalinity"));
			mSalinityVar = new JComboBox(mSalinityChoices);
			mSalinityVar.setSelectedIndex(JOAConstants.DEFAULT_SALINITY_VARIABLE);

			Vector<String> mSalinitySubChoices = new Vector<String>();
			mSalinitySubChoices.add(b.getString("kBottleSalinity"));
			mSalinitySubChoices.add(b.getString("kCTDSalinity"));
			mSalinitySubChoices.add(b.getString("kDontSubstitute"));
			mSalinitySubstitute = new JComboBox(mSalinitySubChoices);
			mSalinitySubstitute.setSelectedIndex(JOAConstants.DEFAULT_SALINITY_SUBSTITUTION);

			JLabel label1 = new JLabel(b.getString("kVarToUseForSalt"));
			line4.add(label1);
			line4.add(mSalinityVar);
			JLabel label2 = new JLabel(b.getString("kSubstitution"));
			line4.add(label2);
			line4.add(mSalinitySubstitute);
			contPanel.add(line4);

			Vector<String> mO2Choices = new Vector<String>();
			mO2Choices.add(b.getString("kBottleO2"));
			mO2Choices.add(b.getString("kCTDO2"));
			mO2Var = new JComboBox(mO2Choices);
			mO2Var.setSelectedIndex(JOAConstants.DEFAULT_O2_VARIABLE);

			Vector<String> mO2SubChoices = new Vector<String>();
			mO2SubChoices.add(b.getString("kBottleO2"));
			mO2SubChoices.add(b.getString("kCTDO2"));
			mO2SubChoices.add(b.getString("kDontSubstitute"));
			mO2Substitute = new JComboBox(mO2SubChoices);
			mO2Substitute.setSelectedIndex(JOAConstants.DEFAULT_O2_SUBSTITUTION);

			JLabel label3 = new JLabel(b.getString("kVarToUseForO2"));
			line5.add(label3);
			line5.add(mO2Var);
			JLabel label4 = new JLabel(b.getString("kSubstitution"));
			line5.add(label4);
			line5.add(mO2Substitute);
			contPanel.add(line5);
			this.add(contPanel, "North");
		}

		public int getSalinityDefault() {
			return mSalinityVar.getSelectedIndex();
		}

		public int getSalinitySub() {
			return mSalinitySubstitute.getSelectedIndex();
		}

		public int getO2Default() {
			return mO2Var.getSelectedIndex();
		}

		public int getO2Sub() {
			return mO2Substitute.getSelectedIndex();
		}
	}

	private class FeaturesPrefsPanel extends JPanel {
		HashMap<String, FeatureDetailPanel> mAllPanels = new HashMap<String, FeatureDetailPanel>();		
		
		public void refreshUI() {
			// loop through FeatureGroups
			for (FeatureGroup fg : JOAConstants.JOA_FEATURESET.values()) {
				// Loop through it's features
				HashMap<String, ManagedFeature> mfs = fg.getFeatures();
				for (ManagedFeature mf : mfs.values()) {
					// make feature detail panels
					FeatureDetailPanel fdp = mAllPanels.get(mf.getID());
					if (fdp != null) {
						fdp.getJCheckBox().setSelected(mf.isEnabled());
					}
				}
			}
		}
		
		public void init() {
			// layout
			this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));

			// loop through FeatureGroups
			for (FeatureGroup fg : JOAConstants.JOA_FEATURESET.values()) {
				// make a panel for the Feature Group
				FeatureGroupPanel fgp = new FeatureGroupPanel(fg);
				this.add(fgp);

				// Loop through it's features
				HashMap<String, ManagedFeature> mfs = fg.getFeatures();
				for (ManagedFeature mf : mfs.values()) {
					// make feature detail panels
					FeatureDetailPanel fdp = new FeatureDetailPanel(mf);
					mAllPanels.put(mf.getID(), fdp);
					this.add(fdp);
				}
			}
		}

		public FeaturesPrefsPanel() {
			init();
		}

		public void updateFeaturePreferences() {
			// update global prefs
			for (FeatureGroup fg : JOAConstants.JOA_FEATURESET.values()) {
				HashMap<String, ManagedFeature> mfs = fg.getFeatures();
				for (ManagedFeature mf : mfs.values()) {
					FeatureDetailPanel fdp = mAllPanels.get(mf.getID());
					if (fdp != null) {
						mf.setEnabled(fdp.getJCheckBox().isSelected());
					}
				}
			}
		}
	}

	private class FeatureGroupPanel extends JPanel {
		public FeatureGroupPanel(FeatureGroup fg) {
			this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
			this.add(new JLabel(fg.getDisplayName()));
		}
	}

	private class FeatureDetailPanel extends JPanel {
		private JCheckBox cb;

		public FeatureDetailPanel(final ManagedFeature mf) {
			this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
			this.add(new JLabel("      "));
			cb = new JCheckBox(mf.getDisplayName(), mf.isEnabled());
			this.add(cb);
			String details = "(" + "version=" + mf.getVersion() + " status=" + mf.getDevelopmentStatus() + ")";
			this.add(new JLabel(details));
			
			cb.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					mf.setEnabled(cb.isSelected());
				}
			});
		}

		public JCheckBox getJCheckBox() {
			return cb;
		}
	}
	
	private class DapperServerPrefsPanel extends JPanel {
		JPanel mContents;
		MyDapperScroller mScroller;
		Vector<DapperURLDetailPanel> mAllDapperServers = new Vector<DapperURLDetailPanel>();
		
		public DapperServerPrefsPanel() {
			// layout
			 mScroller = new MyDapperScroller();
			mContents = new JPanel(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 0));
			this.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 0));
		  for (String ds : JOAConstants.DEFAULT_DAPPER_SERVERS) {
		  	DapperURLDetailPanel ddp = new DapperURLDetailPanel(ds);
		  	mContents.add(ddp);
		  	mAllDapperServers.add(ddp);
		  }

		  mScroller.setViewportView(mContents);
			this.add(mScroller);

			JOAJButton newDapper = new JOAJButton(b.getString("kNew"));
			JOAJButton deleteDapper = new JOAJButton(b.getString("kDelete"));
			JPanel dlgBtnsInset2 = new JPanel();
			JPanel dlgBtnsPanel2 = new JPanel();
			dlgBtnsInset2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
			dlgBtnsPanel2.setLayout(new GridLayout(1, 4, 15, 1));
			dlgBtnsPanel2.add(deleteDapper);
			dlgBtnsPanel2.add(newDapper);
			dlgBtnsInset2.add(dlgBtnsPanel2);
			this.add(dlgBtnsInset2);
			
			newDapper.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DapperURLDetailPanel ddp = new DapperURLDetailPanel("");
			  	mAllDapperServers.add(ddp);
					mContents.add(ddp);
					mContents.invalidate();
					mContents.validate();
				}
			});
			
			deleteDapper.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				  for (DapperURLDetailPanel ddp : mAllDapperServers) {
				  	if (ddp.isSelected()) {
				  		mAllDapperServers.remove(ddp);
				  	}
				  }
				  mContents.removeAll();
				  
				  for (DapperURLDetailPanel ddp : mAllDapperServers) {
						mContents.add(ddp);
				  }
					setSize(getWidth()+1, getHeight());
					setSize(getWidth()-1, getHeight());
				}
			});
		}

		private class MyDapperScroller extends JScrollPane {
			public MyDapperScroller() {
				super();
				this.getVerticalScrollBar().setUnitIncrement(20);
			}

			public Dimension getPreferredSize() {
				return new Dimension(450, 155);
			}
		}
	}

	private class DapperURLDetailPanel extends JPanel {
		JTextField mURLField;
		JCheckBox mSelected = new JCheckBox();
		
		public DapperURLDetailPanel(String url) {
			this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
			mURLField = new JTextField(30);
			mURLField.setText(url);
			this.add(mSelected);
			this.add(mURLField);
		}
		
		public boolean isSelected() {
			return mSelected.isSelected();
		}
		
		public String getURL() {
			return mURLField.getText();
		}
	}
}

/*
 * $Id: ConfigContourPlotDC.java,v 1.11 2005/09/07 18:49:29 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.io.*;
import javax.swing.border.*;
import com.apple.mrj.MRJFileUtils;
import com.apple.mrj.MRJOSType;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.classicdatamodel.Bottle;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ConfigContourPlotDC extends JOAJDialog implements ListSelectionListener, ActionListener, ButtonMaintainer,
    ItemListener, DialogClient {
	protected FileViewer mFileViewer;
	protected Component mComp;
	protected int mSelInterp = -1;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJButton mApplyButton = null;
	protected JOAJButton mSaveButton = null;
	protected JOAJTextField mNameField = null;
	protected JOAJTextField mMinValFld = null;
	protected JOAJTextField mMaxValFld = null;
	protected JSpinner mMarkerSizeFld = null;
	protected JOAJTextField mYInc, mXScale;
	protected JSpinner mYTics, mXTics;
	protected JOAJRadioButton mContourLines = null;
	protected JOAJRadioButton mColorFill = null;
	protected JOAJRadioButton mColorFillWContours = null;
	protected JOAJRadioButton mSequence = null;
	protected JOAJRadioButton mDistance = null;
	protected JOAJRadioButton mLatitude = null;
	protected JOAJRadioButton mLongitude = null;
	protected JOAJRadioButton mTime = null;
	protected JOAJRadioButton mNone = null;
	protected JOAJRadioButton mSurfaceLevels = null;
	protected JOAJRadioButton mObservations = null;
	protected JOAJRadioButton mAutoscaleXAxis = null;
	protected JOAJRadioButton mCustomXAxisScale = null;
	protected JOAJCheckBox mPlotBottom = null;
	protected JOAJCheckBox mColorLegend = null;
	protected JOAJCheckBox mEnableBrowsing = null;
	protected JOAJCheckBox mPlotAxes = null;
	protected NewColorBar mColorBar = null;
	protected int mSelectedCBAR = -99;
	protected ColorBarPanel mColorBarPanel = null;
	protected JOAJList mPaletteList = null;
	protected JPanel mUpperContents = null;
	protected LargeIconButton mLinear = null;
	protected LargeIconButton mPowerUp = null;
	protected LargeIconButton mPowerDown = null;
	protected LargeIconButton mLogistic = null;
	protected JOAJComboBox mColorCombo = null;
	protected Vector<String> mColorBarList = null;
	protected double[] mColorBarValues = null;
	protected Color[] mColorBarColors = null;
	protected double[] mOldColorBarValues = null;
	protected Color[] mOldColorBarColors = null;
	protected int mNumOldLevels;
	protected int mAutoscaleColorScheme = 1;
	protected int mStyle = JOAConstants.STYLE_FILLED;
	protected int mOffset = JOAConstants.OFFSET_DISTANCE;
	protected int mMarkers = JOAConstants.MARKERS_NONE;
	protected DialogClient mClient = null;
	protected ContourPlotSpecification mPlotSpec = null;
	protected ContourPlotSpecification mOriginalPlotSpec = null;
	protected boolean mOrigColorLegend, mOrigPlotAxes, mOrigEnableBrowsing, mOrigIncludeObsPanel;
	protected boolean mRemovingColorLegend = false;
	protected boolean mAddingColorLegend = false;
	protected boolean mRemovingAxes = false;
	protected boolean mAddingAxes = false;
	protected boolean mRemovingBrowsing = false;
	protected boolean mAddingBrowsing = false;
	protected boolean mLayoutChanged = false;
	protected boolean mColorBarChanged = false;
	protected JOAJButton mOptionBtn = null;
	protected JDialog mFrame = null;
	protected JOAWindow mParent;
	protected boolean mIsTSV = true;
	protected boolean mIsCSV = false;
	protected boolean mUseDefaultMissing = true;
	protected String mCustomDelim;
	protected String mCustomMissing;
	protected JOAJLabel mScaleUnitsLabel;
	protected Swatch mSymbolColorSwatch;

	protected JOAJButton mOverlayOptionBtn = null;
	protected ContourPlotSpecification mOverlayPlotSpec;
	protected Interpolation mOverlayInterpolation = null;
	protected JSpinner mOverlayMarkerSizeFld = null;
	protected JOAJRadioButton mOverlayNone = null;
	protected JOAJRadioButton mOverlaySurfaceLevels = null;
	protected JOAJRadioButton mOverlayObservations = null;
	protected ColorBarPanel mOverlayColorBarPanel = null;
	protected JOAJList mOverlayPaletteList = null;
	protected JPanel mOverlayUpperContents = null;
	protected LargeIconButton mOverlayLinear = null;
	protected LargeIconButton mOverlayPowerUp = null;
	protected LargeIconButton mOverlayPowerDown = null;
	protected LargeIconButton mOverlayLogistic = null;
	protected JOAJComboBox mOverlayColorCombo = null;
	protected Vector<String> mOverlayColorBarList = null;
	protected int mOverlayAutoscaleColorScheme = 1;
	protected double[] mOverlayColorBarValues = null;
	protected Color[] mOverlayColorBarColors = null;
	protected NewColorBar mOverlayColorBar = null;
	protected int mSelectedOverlayCBAR = -99;
	JOAJTabbedPane everyThingPanel = null;
	protected int mOverlayMarkers = JOAConstants.MARKERS_NONE;
	protected int mOverlayMarkerSize = 1;
	protected Swatch mOverlaySymbolColorSwatch;
	protected JOASwatch mOverlayContourColorSwatch;
	protected JOAJRadioButton mBlackContours = null;
	protected JOAJRadioButton mWhiteContours = null;
	protected JOAJRadioButton mCustomSingleColor = null;
	protected JOAJRadioButton mColorsFromColorBar = null;
	protected Color mOverlayLineColor = Color.black;
	protected JOAJLabel mUsingLbl = null;
	protected JOAJCheckBox mPlotOverlayBottom = null;
	protected JOAJCheckBox mEnableOverlayBrowsing = null;
	private Timer timer = new Timer();
	private JOAJRadioButton mOffsetParameter = null;
	private JOAJRadioButton mStnNumCast = null;
	private JOAJRadioButton mLatLabel = null;
	private JOAJRadioButton mLonLabel = null;
	private JSpinner mSkipLabelsFld;

	public ConfigContourPlotDC(JOAWindow parent, FileViewer fv, DialogClient client, ContourPlotSpecification spec) {
		super(parent, "Configure Contour Plot", false);

		mParent = parent;
		mClient = client;
		mFileViewer = fv;
		mPlotSpec = spec;
		mOrigColorLegend = mPlotSpec.isIncludeCBAR();
		mOrigIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
		mOrigPlotAxes = mPlotSpec.isPlotAxes();
		mOrigEnableBrowsing = mPlotSpec.isBrowsingEnabled();
		mOriginalPlotSpec = new ContourPlotSpecification(mPlotSpec);
		this.init();

		mFrame = this;
		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				mClient.dialogCancelled(mFrame);
			}
		};
		this.addWindowListener(windowListener);
	}

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		// store the NewColorBar values
		mNumOldLevels = mPlotSpec.getColorBar().getNumLevels();
		mOldColorBarValues = new double[mNumOldLevels];
		mOldColorBarColors = new Color[mNumOldLevels];
		for (int i = 0; i < mNumOldLevels; i++) {
			mOldColorBarValues[i] = mPlotSpec.getColorBar().getDoubleValue(i);
			mOldColorBarColors[i] = mPlotSpec.getColorBar().getColorValue(i);
		}

		// create the two parameter chooser lists
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(0, 0));

		everyThingPanel = new JOAJTabbedPane();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(0, 0));

		// upper panel contains the interpolation chooser and contour choices stuff
		mUpperContents = new JPanel();
		// mUpperContents.setLayout(new GridLayout(1, 2, 5, 0));
		mUpperContents.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 5));

		// color bar list
		JPanel palPanel = new JPanel();
		palPanel.setLayout(new BorderLayout(0, 0));
		palPanel.add(new JOAJLabel(b.getString("kColorbars"), JOAJLabel.LEFT), "North");
		mColorBarList = JOAFormulas.getColorBarList();
		mPaletteList = new JOAJList(mColorBarList);
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				;
			}
		};
		mPaletteList.addMouseListener(mouseListener);
		mPaletteList.setVisibleRowCount(5);
		mPaletteList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mPaletteList.setPrototypeCellValue("SALT on PRES;0-1000");
		JScrollPane listScroller = new JScrollPane(mPaletteList);
		palPanel.add(listScroller, "Center");

		// panel for autoscaling goes here
		JPanel autoscalePanel = new JPanel();
		autoscalePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 10));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kCreateAutoscaleColorbar"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		autoscalePanel.setBorder(tb);

		JPanel line6 = new JPanel();
		line6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
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
		line6.add(shapePanel);
		autoscalePanel.add(line6);
		
		mLinear.setToolTipText(b.getString("kLinearTip"));
		mPowerUp.setToolTipText(b.getString("kIncreasingExpTip"));
		mPowerDown.setToolTipText(b.getString("kDecreasingExpTip"));
		mLogistic.setToolTipText(b.getString("kReverseSTip"));

		JPanel line7 = new JPanel();
		line7.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		line7.add(new JOAJLabel(b.getString("kUsing")));
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
		line7.add(mColorCombo);
		autoscalePanel.add(line7);

		palPanel.add(autoscalePanel, "South");

		mPaletteList.addListSelectionListener(this);
		mUpperContents.add(palPanel);

		// color bar panel
		mColorBar = mPlotSpec.getColorBar();
		mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar);
		mColorBarPanel.setLinked(false);
		mColorBarPanel.setEnhanceable(false);
		mColorBarPanel.setBroadcastMode(false);
		mUpperContents.add(mColorBarPanel);

		// Middle panel is for options
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new BorderLayout(0, 0));

		// Options container goes in the north of the middle panel
		JPanel optionsContTop = new JPanel();
		optionsContTop.setLayout(new GridLayout(1, 3));

		JPanel optionsContBott = new JPanel();
		optionsContBott.setLayout(new GridLayout(1, 2));

		// Style Panel
		JPanel styleCont = new JPanel();
		styleCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));

		tb = BorderFactory.createTitledBorder(b.getString("kStyle"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		styleCont.setBorder(tb);
		JPanel styles = new JPanel();
		styles.setLayout(new GridLayout(3, 1, 0, 0));

		mColorFill = new JOAJRadioButton(b.getString("kColorFill"), mPlotSpec.getStyle() == JOAConstants.STYLE_FILLED);
		mColorFillWContours = new JOAJRadioButton(b.getString("kColorFillWContours"),
		    mPlotSpec.getStyle() == JOAConstants.STYLE_FILLED_CONTOURS);
		mContourLines = new JOAJRadioButton(b.getString("kContourLines"),
		    mPlotSpec.getStyle() == JOAConstants.STYLE_CONTOURS);

		mOptionBtn = new JOAJButton(b.getString("kContourOptions"));
		mOptionBtn.setActionCommand("options");
		mOptionBtn.addActionListener(this);
		JPanel optBtnInset = new JPanel();
		optBtnInset.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		optBtnInset.add(mOptionBtn);
		JPanel optBtnInset2 = new JPanel();
		optBtnInset2.setLayout(new BorderLayout(0, 0));
		optBtnInset2.add(optBtnInset);

		styles.add(mColorFill);
		styles.add(mColorFillWContours);
		styles.add(mContourLines);
		ButtonGroup bg = new ButtonGroup();
		bg.add(mContourLines);
		bg.add(mColorFill);
		bg.add(mColorFillWContours);
		styleCont.add("West", styles);
		styleCont.add("East", optBtnInset2);
		mContourLines.addItemListener(this);
		mColorFill.addItemListener(this);
		mColorFillWContours.addItemListener(this);
		mStyle = mPlotSpec.getStyle();
		optionsContTop.add(styleCont);

		// Offset Panel
		JPanel offsetCont = new JPanel();
		offsetCont.setLayout(new BorderLayout(0, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kOffset"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		offsetCont.setBorder(tb);
		JPanel offsets = new JPanel();
		offsets.setLayout(new GridLayout(3, 2, 0, 0));
		mSequence = new JOAJRadioButton(b.getString("kSequence"), mPlotSpec.getOffset() == JOAConstants.OFFSET_SEQUENCE);
		mDistance = new JOAJRadioButton(b.getString("kDistance"), mPlotSpec.getOffset() == JOAConstants.OFFSET_DISTANCE);
		mLatitude = new JOAJRadioButton(b.getString("kLatitude"), mPlotSpec.getOffset() == JOAConstants.OFFSET_LATITUDE);
		mLongitude = new JOAJRadioButton(b.getString("kLongitude"), mPlotSpec.getOffset() == JOAConstants.OFFSET_LONGITUDE);
		mTime = new JOAJRadioButton(b.getString("kTime"), mPlotSpec.getOffset() == JOAConstants.OFFSET_TIME);
		offsets.add(mSequence);
		offsets.add(mDistance);
		offsets.add(mLatitude);
		offsets.add(mLongitude);
		offsets.add(mTime);
		bg = new ButtonGroup();
		bg.add(mSequence);
		bg.add(mDistance);
		bg.add(mLatitude);
		bg.add(mLongitude);
		bg.add(mTime);
		offsetCont.add("North", offsets);
		mSequence.addItemListener(this);
		mDistance.addItemListener(this);
		mLatitude.addItemListener(this);
		mLongitude.addItemListener(this);
		mTime.addItemListener(this);
		optionsContTop.add(offsetCont);
		mOffset = mPlotSpec.getOffset();

		// Label Panel
		JPanel labelCont = new JPanel();
		// markerCont.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tb = BorderFactory.createTitledBorder(b.getString("kXAxisLabels"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}

		labelCont.setBorder(tb);
		JPanel labels = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		mOffsetParameter = new JOAJRadioButton(b.getString("kOffsetParameter"),
		    mPlotSpec.getXAxisLabels() == ContourPlotSpecification.LABEL_OFFSET_PARAMETER);
		mStnNumCast = new JOAJRadioButton(b.getString("kStationNumber"),
		    mPlotSpec.getXAxisLabels() == ContourPlotSpecification.STN_NUM_OFFSET_PARAMETER);
		mLatLabel = new JOAJRadioButton(b.getString("kLatitude"),
		    mPlotSpec.getXAxisLabels() == ContourPlotSpecification.LAT_OFFSET_PARAMETER);
		mLonLabel = new JOAJRadioButton(b.getString("kLongitude"),
		    mPlotSpec.getXAxisLabels() == ContourPlotSpecification.LON_OFFSET_PARAMETER);
		labels.add(mOffsetParameter);
		labels.add(mStnNumCast);
		labels.add(mLatLabel);
		labels.add(mLonLabel);
		ButtonGroup bgl = new ButtonGroup();
		bgl.add(mOffsetParameter);
		bgl.add(mStnNumCast);
		bgl.add(mLatLabel);
		bgl.add(mLonLabel);
		JPanel skipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		SpinnerNumberModel model2z = new SpinnerNumberModel(mPlotSpec.getSkipXAxisLabels(), 0, 100, 1);
		mSkipLabelsFld = new JSpinner(model2z);
		skipPanel.add(new JLabel(b.getString("kSkip")));
		skipPanel.add(mSkipLabelsFld);
		labels.add(skipPanel);
		labelCont.add(labels);
		optionsContTop.add(labelCont);

		// Marker Panel
		JPanel markerCont = new JPanel();
		markerCont.setLayout(new BorderLayout(5, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kMarkers"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		markerCont.setBorder(tb);
		JPanel markers = new JPanel();
		markers.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		mNone = new JOAJRadioButton(b.getString("kNone"), mPlotSpec.getMarkers() == JOAConstants.MARKERS_NONE);
		mSurfaceLevels = new JOAJRadioButton(b.getString("kSurfaceLevels"),
		    mPlotSpec.getMarkers() == JOAConstants.MARKERS_SURFACE_LEVELS);
		mObservations = new JOAJRadioButton(b.getString("kObservations"),
		    mPlotSpec.getMarkers() == JOAConstants.MARKERS_OBSERVATIONS);
		markers.add(mNone);
		JPanel linem = new JPanel();
		linem.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		linem.add(mSurfaceLevels);
		linem.add(new JOAJLabel(b.getString("kSize")));

		SpinnerNumberModel model = new SpinnerNumberModel(mPlotSpec.getMarkerSize(), 1, 100, 1);
		mMarkerSizeFld = new JSpinner(model);

		linem.add(mMarkerSizeFld);
		linem.add(new JOAJLabel(" " + b.getString("kColor2")));
		mSymbolColorSwatch = new Swatch(mPlotSpec.getSymbolColor());
		linem.add(mSymbolColorSwatch);
		markers.add(linem);
		markers.add(mObservations);
		bg = new ButtonGroup();
		bg.add(mNone);
		bg.add(mSurfaceLevels);
		bg.add(mObservations);
		markerCont.add("North", markers);

		mNone.addItemListener(this);
		mSurfaceLevels.addItemListener(this);
		mObservations.addItemListener(this);
		optionsContBott.add(markerCont);
		mMarkers = mPlotSpec.getMarkers();

		// Other Options
		JPanel otherCont = new JPanel();
		otherCont.setLayout(new BorderLayout(0, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kOther"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		otherCont.setBorder(tb);
		JPanel others = new JPanel();
		others.setLayout(new GridLayout(3, 1, 0, 0));
		mPlotAxes = new JOAJCheckBox(b.getString("kPlotAxes"), true); // mPlotSpec.mPlotAxes);
		mColorLegend = new JOAJCheckBox(b.getString("kColorLegend"), mPlotSpec.isIncludeCBAR());
		// mIncludeObsPanel = new JOAJCheckBox(b.getString("kIncludeBrowser"),
		// mPlotSpec.mIncludeObsPanel);
		mEnableBrowsing = new JOAJCheckBox(b.getString("kShowCrossSections"), mPlotSpec.isBrowsingEnabled());
		mPlotBottom = new JOAJCheckBox(b.getString("kPlotBottom"), mPlotSpec.isPlotBottom());
		// others.add(mPlotAxes);
		others.add(mColorLegend);
		others.add(mEnableBrowsing);
		others.add(mPlotBottom);
		otherCont.add("North", others);
		mPlotAxes.addItemListener(this);
		mColorLegend.addItemListener(this);
		mEnableBrowsing.addItemListener(this);
		optionsContBott.add(otherCont);

		// Ranges goes in the middle of the middle panel
		JPanel axisStuff = new JPanel();
		axisStuff.setLayout(new GridLayout(2, 1, 0, 0));

		// y axis
		JPanel line0 = new JPanel();
		line0.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));
		tb = BorderFactory.createTitledBorder(b.getString("kYAxis"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		line0.setBorder(tb);
		line0.add(new JOAJLabel(b.getString("kMinimum")));
		mMinValFld = new JOAJTextField(5);
		mMinValFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line0.add(mMinValFld);
		line0.add(new JOAJLabel(b.getString("kMaximum")));
		mMaxValFld = new JOAJTextField(5);
		mMaxValFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line0.add(mMaxValFld);
		mMinValFld.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinYPlotMin()), 2, false));
		mMaxValFld.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinYPlotMax()), 2, false));

		line0.add(new JOAJLabel(b.getString("kIncrement")));
		mYInc = new JOAJTextField(5);
		mYInc.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getYInc()), 2, false));
		mYInc.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line0.add(mYInc);

		line0.add(new JOAJLabel(b.getString("kNoMinorTicks")));

		SpinnerNumberModel model2 = new SpinnerNumberModel(mPlotSpec.getYTics(), 0, 100, 1);
		mYTics = new JSpinner(model2);
		line0.add(mYTics);

		// x axis
		JPanel line00 = new JPanel();
		line00.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));
		tb = BorderFactory.createTitledBorder(b.getString("kXAxis"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		line00.setBorder(tb);

		mAutoscaleXAxis = new JOAJRadioButton(b.getString("kAutoscale"), mPlotSpec.isAutoScaleXAxis());
		line00.add(mAutoscaleXAxis);

		mCustomXAxisScale = new JOAJRadioButton(b.getString("kCustom"), !mPlotSpec.isAutoScaleXAxis());
		line00.add(mCustomXAxisScale);

		mXScale = new JOAJTextField(6);
		mXScale.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getXAxisScale()), 3, false));
		mXScale.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		MouseListener mouseListener2 = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				mAutoscaleXAxis.setSelected(false);
				mCustomXAxisScale.setSelected(true);
			}
		};
		mXScale.addMouseListener(mouseListener2);
		line00.add(mXScale);
		mScaleUnitsLabel = new JOAJLabel(b.getString("kkm/cm"));
		line00.add(mScaleUnitsLabel);

		ButtonGroup xabg = new ButtonGroup();
		xabg.add(mAutoscaleXAxis);
		xabg.add(mCustomXAxisScale);

		line00.add(new JOAJLabel("   " + b.getString("kNoMinorTicks")));

		SpinnerNumberModel model3 = new SpinnerNumberModel(mPlotSpec.getXTics(), 0, 100, 1);
		mXTics = new JSpinner(model3);
		line00.add(mXTics);

		// add the upper panels
		axisStuff.add(line0);
		axisStuff.add(line00);
		middlePanel.add("North", axisStuff);
		middlePanel.add("Center", optionsContTop);
		middlePanel.add("South", optionsContBott);

		// build upper part of dialog
		mainPanel.add("North", new TenPixelBorder(mUpperContents, 5, 5, 0, 5));
		mainPanel.add("Center", new TenPixelBorder(middlePanel, 0, 5, 5, 5));

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		mApplyButton = new JOAJButton(b.getString("kApply"));
		mApplyButton.setActionCommand("apply");
		mSaveButton = new JOAJButton(b.getString("kSaveInterpolation"));
		mSaveButton.setActionCommand("save");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mSaveButton);
			dlgBtnsPanel.add(mApplyButton);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mApplyButton);
			dlgBtnsPanel.add(mSaveButton);
			dlgBtnsPanel.add(mCancelButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mOKBtn.addActionListener(this);
		mApplyButton.addActionListener(this);
		mCancelButton.addActionListener(this);
		mSaveButton.addActionListener(this);

		// overlay panel widgets
		JPanel overlayPanel = new JPanel();
		overlayPanel.setLayout(new BorderLayout(5, 0));

		// overlay upper panel contains the overlay chooser and contour choices
		// stuff
		mOverlayUpperContents = new JPanel();
		mOverlayUpperContents.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 5));

		// Middle panel is for options
		JPanel overlayMiddlePanel = new JPanel();
		overlayMiddlePanel.setLayout(new BorderLayout(5, 0));

		// Options container goes in the north of the middle panel
		JPanel overlayOptionsCont = new JPanel();
		overlayOptionsCont.setLayout(new GridLayout(1, 5));

		// color bar list
		JPanel overlayPalContPanel = new JPanel();
		overlayPalContPanel.setLayout(new BorderLayout(5, 0));

		JPanel overlayPalPanel = new JPanel();
		overlayPalPanel.setLayout(new BorderLayout(5, 5));

		overlayPalPanel.add(new JOAJLabel(b.getString("kContours2"), JOAJLabel.LEFT), "North");
		mOverlayColorBarList = JOAFormulas.getColorBarList();
		mOverlayPaletteList = new JOAJList(mOverlayColorBarList);
		MouseListener mouseListener3 = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				;
			}
		};
		mOverlayPaletteList.addMouseListener(mouseListener3);
		mOverlayPaletteList.setVisibleRowCount(8);
		mOverlayPaletteList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mOverlayPaletteList.setPrototypeCellValue("SALT on PRES;0-1000");
		JScrollPane ovlListScroller = new JScrollPane(mOverlayPaletteList);
		overlayPalPanel.add(ovlListScroller, "Center");
		overlayPalContPanel.add(overlayPalPanel, "North");

		// panel for autoscaling goes here
		JPanel overlayAutoscalePanel = new JPanel();
		overlayAutoscalePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 10));
		tb = BorderFactory.createTitledBorder(b.getString("kCreateAutoscaleContours"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		overlayAutoscalePanel.setBorder(tb);

		JPanel line7ov = new JPanel();
		line7ov.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		JPanel ovlShapePanel = new JPanel();
		ovlShapePanel.setLayout(new GridLayout(1, 4, 5, 0));
		mOverlayLinear = new LargeIconButton(new ImageIcon(getClass().getResource("images/linear.gif")));
		ovlShapePanel.add(mOverlayLinear);
		mOverlayPowerUp = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerup.gif")));
		ovlShapePanel.add(mOverlayPowerUp);
		mOverlayPowerDown = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerdown.gif")));
		ovlShapePanel.add(mOverlayPowerDown);
		mOverlayLogistic = new LargeIconButton(new ImageIcon(getClass().getResource("images/logistic.gif")));
		ovlShapePanel.add(mOverlayLogistic);
		mOverlayLinear.addActionListener(this);
		mOverlayPowerUp.addActionListener(this);
		mOverlayPowerDown.addActionListener(this);
		mOverlayLogistic.addActionListener(this);
		mOverlayLinear.setActionCommand("ovllinear");
		mOverlayPowerUp.setActionCommand("ovlpowerUp");
		mOverlayPowerDown.setActionCommand("ovlpowerDown");
		mOverlayLogistic.setActionCommand("ovllogistic");
		line7ov.add(ovlShapePanel);
		overlayAutoscalePanel.add(line7ov);
		
		mOverlayLinear.setToolTipText(b.getString("kLinearTip"));
		mOverlayPowerUp.setToolTipText(b.getString("kIncreasingExpTip"));
		mOverlayPowerDown.setToolTipText(b.getString("kDecreasingExpTip"));
		mOverlayLogistic.setToolTipText(b.getString("kReverseSTip"));

		JPanel line8ov = new JPanel();
		line8ov.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		mUsingLbl = new JOAJLabel(b.getString("kUsing"));
		line8ov.add(mUsingLbl);
		Vector<String> overlayAutoScaleChoices = new Vector<String>();
		overlayAutoScaleChoices.addElement(new String("Blue-White-Red-32"));
		overlayAutoScaleChoices.addElement(new String("Blue-White-Red-16"));
		overlayAutoScaleChoices.addElement(new String("Red-White-Blue-32"));
		overlayAutoScaleChoices.addElement(new String("Red-White-Blue-16"));
		overlayAutoScaleChoices.addElement(new String("Rainbow-32"));
		overlayAutoScaleChoices.addElement(new String("Rainbow-16"));
		overlayAutoScaleChoices.addElement(new String("Rainbow(inv)-32"));
		overlayAutoScaleChoices.addElement(new String("Rainbow(inv)-16"));
		mOverlayColorCombo = new JOAJComboBox(overlayAutoScaleChoices);
		mOverlayColorCombo.setSelectedIndex(JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME);
		mOverlayAutoscaleColorScheme = JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME + 1;
		mOverlayColorCombo.addItemListener(this);
		line8ov.add(mOverlayColorCombo);
		overlayAutoscalePanel.add(line8ov);

		JPanel ovlPalcont2 = new JPanel();
		ovlPalcont2.setLayout(new BorderLayout(5, 0));
		ovlPalcont2.add(overlayAutoscalePanel, "North");
		overlayPalContPanel.add(ovlPalcont2, "Center");

		mOverlayPaletteList.addListSelectionListener(this);
		mOverlayUpperContents.add(overlayPalContPanel);

		boolean contoursAreBlack = mPlotSpec.getOverlayContourColor() instanceof Color
		    && JOAFormulas.isSameColor((Color) mPlotSpec.getOverlayContourColor(), Color.black);
		boolean contoursAreWhite = mPlotSpec.getOverlayContourColor() instanceof Color
		    && JOAFormulas.isSameColor((Color) mPlotSpec.getOverlayContourColor(), Color.white);
		boolean contoursAreCustom = mPlotSpec.getOverlayContourColor() instanceof Color && !contoursAreBlack
		    && !contoursAreWhite;
		Color contColor = Color.black;
		if (contoursAreCustom) {
			contColor = (Color) mPlotSpec.getOverlayContourColor();
		}

		// blank color bar panel
		mOverlayColorBar = mPlotSpec.getOverlayColorBar();
		mOverlayInterpolation = mPlotSpec.getOverlayInterp();
		if (mPlotSpec.isOverlayContours()) {
			if (contoursAreBlack) {
				mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mPlotSpec.getOverlayColorBar(), Color.black);
			}
			else if (contoursAreWhite) {
				mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mPlotSpec.getOverlayColorBar(), Color.white);
			}
			else if (contoursAreCustom) {
				mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mPlotSpec.getOverlayColorBar(), contColor);
			}
			else {
			}
			mOverlayColorBarPanel.setLinked(false);
			mOverlayColorBarPanel.setEnhanceable(false);
			mOverlayColorBarPanel.setBroadcastMode(false);
			mOverlayUpperContents.add(mOverlayColorBarPanel);
		}
		overlayPanel.add(new TenPixelBorder(mOverlayUpperContents, 5, 5, 5, 5), "North");

		// Style Panel
		JPanel overlayStyleCont = new JPanel();
		overlayStyleCont.setLayout(new BorderLayout(5, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kStyle"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		overlayStyleCont.setBorder(tb);

		JPanel overlayStyles = new JPanel();
		overlayStyles.setLayout(new GridLayout(4, 1, 5, 0));

		mBlackContours = new JOAJRadioButton(b.getString("kBlackContours"), contoursAreBlack);
		mWhiteContours = new JOAJRadioButton(b.getString("kWhiteContours"), contoursAreWhite);
		mCustomSingleColor = new JOAJRadioButton(b.getString("kCustomSingleColor"), contoursAreCustom);
		mOverlayContourColorSwatch = new JOASwatch(contColor, this);
		mOverlayContourColorSwatch.setEnabled(false);
		JPanel custColorLine = new JPanel();
		custColorLine.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		custColorLine.add(mCustomSingleColor);
		custColorLine.add(mOverlayContourColorSwatch);
		mColorsFromColorBar = new JOAJRadioButton(b.getString("kColorsFromColorBar"),
		    mPlotSpec.getOverlayContourColor() instanceof NewColorBar);
		overlayStyles.add(mBlackContours);
		overlayStyles.add(mWhiteContours);
		overlayStyles.add(custColorLine);
//		overlayStyles.add(mColorsFromColorBar);
		ButtonGroup sbg = new ButtonGroup();
		sbg.add(mBlackContours);
		sbg.add(mWhiteContours);
		sbg.add(mCustomSingleColor);
		sbg.add(mColorsFromColorBar);
		overlayStyleCont.add("North", overlayStyles);
		mBlackContours.addItemListener(this);
		mWhiteContours.addItemListener(this);
		mCustomSingleColor.addItemListener(this);
//		mColorsFromColorBar.addItemListener(this);

		mOverlayOptionBtn = new JOAJButton(b.getString("kContourOptions"));
		mOverlayOptionBtn.setActionCommand("ovloptions");
		mOverlayOptionBtn.addActionListener(this);
		JPanel overlayOptBtnInset = new JPanel();
		overlayOptBtnInset.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		overlayOptBtnInset.add(mOverlayOptionBtn);
		overlayStyleCont.add(overlayOptBtnInset, "South");
		overlayOptionsCont.add(overlayStyleCont);

		// Overlay Marker Panel
		JPanel overlayMarkerCont = new JPanel();
		overlayMarkerCont.setLayout(new BorderLayout(5, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kMarkers"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		overlayMarkerCont.setBorder(tb);

		JPanel overlayMarkers = new JPanel();
		overlayMarkers.setLayout(new GridLayout(4, 1, 0, 0));
		mOverlayMarkers = mPlotSpec.getOverlayMarkers();
		mOverlayNone = new JOAJRadioButton(b.getString("kNone"), mPlotSpec.getOverlayMarkers() == JOAConstants.MARKERS_NONE);
		mOverlaySurfaceLevels = new JOAJRadioButton(b.getString("kSurfaceLevels"),
		    mPlotSpec.getOverlayMarkers() == JOAConstants.MARKERS_SURFACE_LEVELS);
		mOverlayObservations = new JOAJRadioButton(b.getString("kObservations"),
		    mPlotSpec.getOverlayMarkers() == JOAConstants.MARKERS_OBSERVATIONS);
		overlayMarkers.add(mOverlayNone);
		overlayMarkers.add(mOverlaySurfaceLevels);
		overlayMarkers.add(mOverlayObservations);
		ButtonGroup ovlbg = new ButtonGroup();
		ovlbg.add(mOverlayNone);
		ovlbg.add(mOverlaySurfaceLevels);
		ovlbg.add(mOverlayObservations);

		JPanel overlayLinem = new JPanel();
		overlayLinem.add(new JOAJLabel(b.getString("kSize")));

		mOverlayMarkerSize = mPlotSpec.getOverlayMarkerSize();
		SpinnerNumberModel model4 = new SpinnerNumberModel(mOverlayMarkerSize, 1, 100, 1);
		mOverlayMarkerSizeFld = new JSpinner(model4);
		overlayLinem.add(mOverlayMarkerSizeFld);

		overlayLinem.add(new JOAJLabel(" " + b.getString("kColor2")));
		mOverlaySymbolColorSwatch = new Swatch(mPlotSpec.getOverlaySymbolColor());
		overlayLinem.add(mOverlaySymbolColorSwatch);
		overlayMarkers.add(overlayLinem);
		overlayMarkerCont.add("North", overlayMarkers);

		mOverlayNone.addItemListener(this);
		mOverlaySurfaceLevels.addItemListener(this);
		mOverlayObservations.addItemListener(this);
		overlayOptionsCont.add(overlayMarkerCont);

		// Other Options
		JPanel otherOverlayCont = new JPanel();
		otherOverlayCont.setLayout(new BorderLayout(5, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kOther"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		otherOverlayCont.setBorder(tb);

		JPanel othersOvl = new JPanel();
		othersOvl.setLayout(new GridLayout(4, 1, 5, 0));
		mEnableOverlayBrowsing = new JOAJCheckBox(b.getString("kShowCrossSections"), false);
		mPlotOverlayBottom = new JOAJCheckBox(b.getString("kPlotBottom"), mPlotSpec.isPlotOverlayBottom());
		// others.add(mPlotAxes);
		othersOvl.add(mEnableOverlayBrowsing);
		othersOvl.add(mPlotOverlayBottom);
		othersOvl.add(new JOAJLabel(""));
		otherOverlayCont.add("North", othersOvl);
		overlayOptionsCont.add(otherOverlayCont);

		// add the upper panels
		overlayMiddlePanel.add("North", new TenPixelBorder(overlayOptionsCont, 5, 5, 5, 5));
		overlayPanel.add("Center", new TenPixelBorder(overlayMiddlePanel, 5, 5, 5, 5));

		everyThingPanel.addTab(b.getString("kContourTab4"), mainPanel);
		everyThingPanel.addTab(b.getString("kContourTab3a"), overlayPanel);
		contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", everyThingPanel);
		this.pack();

		runTimer();

		// set the advanced offset labels
		setOffsetLabels(mPlotSpec.getOffset());
		setOffsetScale(mPlotSpec.getOffset());

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	private void setOffsetLabels(int offset) {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		if (offset == JOAConstants.OFFSET_SEQUENCE) {
			mScaleUnitsLabel.setText(b.getString("kstn/cm"));
		}
		else if (offset == JOAConstants.OFFSET_DISTANCE) {
			mScaleUnitsLabel.setText(b.getString("kkm/cm"));
		}
		else if (offset == JOAConstants.OFFSET_LATITUDE) {
			mScaleUnitsLabel.setText(b.getString("kdeg/cm"));
		}
		else if (offset == JOAConstants.OFFSET_LONGITUDE) {
			mScaleUnitsLabel.setText(b.getString("kdeg/cm"));
		}
		else if (offset == JOAConstants.OFFSET_TIME) {
			mScaleUnitsLabel.setText("");
		}
	}

	private void setOffsetScale(int offset) {
		double defScale = 0.0;
		if (offset == JOAConstants.OFFSET_SEQUENCE) {
			if (mPlotSpec.getOffset() != offset) {
				defScale = 1.0 / (437 / mFileViewer.mTotalStations);
			}
			else {
				defScale = mPlotSpec.getXAxisScale();
			}
		}
		else if (offset == JOAConstants.OFFSET_DISTANCE) {
			if (mPlotSpec.getOffset() != offset) {
				defScale = (1 / (437 / mFileViewer.mTotMercDist)) * 1.852;
			}
			else {
				defScale = mPlotSpec.getXAxisScale();
			}

		}
		else if (offset == JOAConstants.OFFSET_LATITUDE) {
			if (mPlotSpec.getOffset() != offset) {
				defScale = 1.0 / (437 / mFileViewer.mTotLatDegs);
			}
			else {
				defScale = mPlotSpec.getXAxisScale();
			}
		}
		else if (offset == JOAConstants.OFFSET_LONGITUDE) {
			if (mPlotSpec.getOffset() != offset) {
				defScale = 1.0 / (437 / mFileViewer.mTotLonDegs);
			}
			else {
				defScale = mPlotSpec.getXAxisScale();
			}
		}
		else if (offset == JOAConstants.OFFSET_TIME) {
			if (mPlotSpec.getOffset() != offset) {
				defScale = 1.0 / (437 / mFileViewer.getTimeLength());
			}
			else {
				defScale = mPlotSpec.getXAxisScale();
			}
		}
		mXScale.setText(JOAFormulas.formatDouble(String.valueOf(defScale), 3, false));
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mPaletteList) {
			int tempSelectedCBAR = mPaletteList.getSelectedIndex();
			String newCBARName = (String) mPaletteList.getSelectedValue();
			if (newCBARName == null || tempSelectedCBAR == mSelectedCBAR) { return; }
			mSelectedCBAR = tempSelectedCBAR;

			// read the color bar from disk
			try {
				mColorBar = JOAFormulas.getColorBar(newCBARName);
			}
			catch (Exception ex) {
				return;
			}

			// display the preview color bar
			if (mColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				mUpperContents.remove(mColorBarPanel);
				mColorBarPanel = null;
			}
			mColorBarChanged = true;
			mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar);
			mColorBarPanel.setLinked(false);
			mColorBarPanel.setEnhanceable(false);
			mColorBarPanel.setBroadcastMode(false);
			mUpperContents.add(mColorBarPanel);
			mUpperContents.invalidate();
			this.validate();
		}
		else if (evt.getSource() == mOverlayPaletteList) {
			int tempSelectedCBAR = mOverlayPaletteList.getSelectedIndex();
			String newCBARName = (String) mOverlayPaletteList.getSelectedValue();
			if (newCBARName == null || tempSelectedCBAR == mSelectedCBAR) { return; }
			mSelectedOverlayCBAR = tempSelectedCBAR;

			// read the color bar from disk
			try {
				mOverlayColorBar = JOAFormulas.getColorBar(newCBARName);
			}
			catch (Exception ex) {
				return;
			}

			// display the preview color bar
			if (mOverlayColorBarPanel != null) {
				// remove existing color bar component (if there is one)
				mOverlayUpperContents.remove(mOverlayColorBarPanel);
				mOverlayColorBarPanel = null;
			}
			if (mBlackContours.isSelected()) {
				mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, Color.black);
			}
			else if (mWhiteContours.isSelected()) {
				mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, Color.white);
			}
			else if (mCustomSingleColor.isSelected()) {
				mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, mOverlayContourColorSwatch
				    .getColor());
			}
//			else if (mColorsFromColorBar.isSelected()) {
//				mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, mOverlayColorBar);
//			}
			mOverlayColorBarPanel.setLinked(false);
			mOverlayColorBarPanel.setEnhanceable(false);
			mOverlayColorBarPanel.setBroadcastMode(false);
			mOverlayUpperContents.add(mOverlayColorBarPanel);
			mOverlayUpperContents.invalidate();
			this.validate();
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJComboBox) {
			JOAJComboBox cb = (JOAJComboBox) evt.getSource();
			mAutoscaleColorScheme = cb.getSelectedIndex() + 1;
		}
		else if (evt.getSource() instanceof JOAJRadioButton) {
			JOAJRadioButton rb = (JOAJRadioButton) evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && rb == mContourLines) {
				mStyle = JOAConstants.STYLE_CONTOURS;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mColorFill) {
				mStyle = JOAConstants.STYLE_FILLED;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mColorFillWContours) {
				mStyle = JOAConstants.STYLE_FILLED_CONTOURS;
			}
			if (evt.getStateChange() == ItemEvent.SELECTED && rb == mSequence) {
				mOffset = JOAConstants.OFFSET_SEQUENCE;
				setOffsetLabels(mOffset);
				setOffsetScale(mOffset);
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mDistance) {
				mOffset = JOAConstants.OFFSET_DISTANCE;
				setOffsetLabels(mOffset);
				setOffsetScale(mOffset);
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mLatitude) {
				mOffset = JOAConstants.OFFSET_LATITUDE;
				setOffsetLabels(mOffset);
				setOffsetScale(mOffset);
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mLongitude) {
				mOffset = JOAConstants.OFFSET_LONGITUDE;
				setOffsetLabels(mOffset);
				setOffsetScale(mOffset);
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mTime) {
				mOffset = JOAConstants.OFFSET_TIME;
				setOffsetLabels(mOffset);
				setOffsetScale(mOffset);
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mNone) {
				mMarkers = JOAConstants.MARKERS_NONE;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mSurfaceLevels) {
				mMarkers = JOAConstants.MARKERS_SURFACE_LEVELS;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mObservations) {
				mMarkers = JOAConstants.MARKERS_OBSERVATIONS;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mOverlayNone) {
				mOverlayMarkers = JOAConstants.MARKERS_NONE;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mOverlaySurfaceLevels) {
				mOverlayMarkers = JOAConstants.MARKERS_SURFACE_LEVELS;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mOverlayObservations) {
				mOverlayMarkers = JOAConstants.MARKERS_OBSERVATIONS;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mBlackContours) {
				mOverlayLineColor = Color.black;
				mOverlayContourColorSwatch.setEnabled(false);
				if (mOverlayColorBarPanel != null) {
					// remove existing color bar component (if there is one)
					mOverlayUpperContents.remove(mOverlayColorBarPanel);
					mOverlayColorBarPanel = null;
					mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, mOverlayLineColor);
					mOverlayColorBarPanel.setLinked(false);
					mOverlayColorBarPanel.setEnhanceable(false);
					mOverlayColorBarPanel.setBroadcastMode(false);
					mOverlayUpperContents.add(mOverlayColorBarPanel);
					mOverlayUpperContents.invalidate();
					this.validate();
				}
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mWhiteContours) {
				mOverlayLineColor = Color.white;
				mOverlayContourColorSwatch.setEnabled(false);
				if (mOverlayColorBarPanel != null) {
					// remove existing color bar component (if there is one)
					mOverlayUpperContents.remove(mOverlayColorBarPanel);
					mOverlayColorBarPanel = null;
					mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, mOverlayLineColor);
					mOverlayColorBarPanel.setLinked(false);
					mOverlayColorBarPanel.setEnhanceable(false);
					mOverlayColorBarPanel.setBroadcastMode(false);
					mOverlayUpperContents.add(mOverlayColorBarPanel);
					mOverlayUpperContents.invalidate();
					this.validate();
				}
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mCustomSingleColor) {
				mOverlayLineColor = mOverlayContourColorSwatch.getColor();
				if (mOverlayColorBarPanel != null) {
					// remove existing color bar component (if there is one)
					mOverlayUpperContents.remove(mOverlayColorBarPanel);
					mOverlayColorBarPanel = null;
					mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, mOverlayLineColor);
					mOverlayColorBarPanel.setLinked(false);
					mOverlayColorBarPanel.setEnhanceable(false);
					mOverlayColorBarPanel.setBroadcastMode(false);
					mOverlayUpperContents.add(mOverlayColorBarPanel);
					mOverlayUpperContents.invalidate();
					this.validate();
				}
			}
//			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == mColorsFromColorBar) {
//				mOverlayContourColorSwatch.setEnabled(false);
//				if (mOverlayColorBarPanel != null) {
//					// remove existing color bar component (if there is one)
//					mOverlayUpperContents.remove(mOverlayColorBarPanel);
//					mOverlayColorBarPanel = null;
//					mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, (Object) mOverlayColorBar);
//					mOverlayColorBarPanel.setLinked(false);
//					mOverlayColorBarPanel.setEnhanceable(false);
//					mOverlayColorBarPanel.setBroadcastMode(false);
//					mOverlayUpperContents.add(mOverlayColorBarPanel);
//					mOverlayUpperContents.invalidate();
//					this.validate();
//				}
//			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			// mClient.dialogCancelled(this);
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			mClient.dialogDismissed(this);
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("options")) {
			ConfigContourOptions contOpts = new ConfigContourOptions(mParent, mFileViewer, this, mPlotSpec);
			contOpts.pack();
			Rectangle dBounds = contOpts.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			contOpts.setLocation(x, y);
			contOpts.setVisible(true);
		}
		else if (cmd.equals("ovloptions")) {
			// contour options
			ConfigOvlContourOptions contOpts = new ConfigOvlContourOptions(mParent, mFileViewer, this, mPlotSpec);
			contOpts.pack();
			Rectangle dBounds = contOpts.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			contOpts.setLocation(x, y);
			contOpts.setVisible(true);
		}
		else if (cmd.equals("apply")) {
			mClient.dialogApply(this);
			mOriginalPlotSpec = null;
			mOriginalPlotSpec = createPlotSpec();
			mOrigPlotAxes = mPlotAxes.isSelected();
			mOrigColorLegend = mColorLegend.isSelected();
			mOrigEnableBrowsing = mEnableBrowsing.isSelected();

			mRemovingColorLegend = false;
			mAddingColorLegend = false;
			mRemovingAxes = false;
			mAddingAxes = false;
			mRemovingBrowsing = false;
			mAddingBrowsing = false;
			mLayoutChanged = false;
		}
		else if (cmd.equals("linear")) {
			createColorBar(JOAConstants.LINEAR);
			mLinear.setSelected(false);
		}
		else if (cmd.equals("powerUp")) {
			createColorBar(JOAConstants.EXPONENTIALUP);
			mPowerUp.setSelected(false);
		}
		else if (cmd.equals("powerDown")) {
			createColorBar(JOAConstants.EXPONENTIALDOWN);
			mPowerDown.setSelected(false);
		}
		else if (cmd.equals("logistic")) {
			createColorBar(JOAConstants.LOGISTIC);
			mLogistic.setSelected(false);
		}
		else if (cmd.equals("ovllinear")) {
			createOverlayColorBar(JOAConstants.LINEAR);
			mOverlayLinear.setSelected(false);
		}
		else if (cmd.equals("ovlpowerUp")) {
			createOverlayColorBar(JOAConstants.EXPONENTIALUP);
			mOverlayPowerUp.setSelected(false);
		}
		else if (cmd.equals("ovlpowerDown")) {
			createOverlayColorBar(JOAConstants.EXPONENTIALDOWN);
			mOverlayPowerDown.setSelected(false);
		}
		else if (cmd.equals("ovllogistic")) {
			createOverlayColorBar(JOAConstants.LOGISTIC);
			mOverlayLogistic.setSelected(false);
		}
		else if (cmd.equals("save")) {
			// save options
			ConfigureInterpExport contOpts = new ConfigureInterpExport(mParent, this);
			contOpts.pack();

			// show dialog at center of screen
			Rectangle dBounds = contOpts.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			contOpts.setLocation(x, y);
			contOpts.setVisible(true);
		}
	}

	public ContourPlotSpecification getOrigPlotSpec() {
		return mOriginalPlotSpec;
	}

	public ContourPlotSpecification createPlotSpec() {
		ContourPlotSpecification cps = new ContourPlotSpecification();

		cps.setInterp(mPlotSpec.getInterp());
		cps.setOverlayInterp(mPlotSpec.getOverlayInterp());
		if (mNameField != null) {
			cps.setWinTitle(mNameField.getText());
		}
		else {
			cps.setWinTitle("Untitled");
		}
		if (cps.getWinTitle().length() == 0) {
			// create a window name
			cps.setWinTitle(new String(cps.getInterp().getParam().getVarName() + " on "
			    + mFileViewer.mAllProperties[cps.getInterp().getSurfParamNum()].getVarLabel()));
		}

		int markerSize = ((Integer) mMarkerSizeFld.getValue()).intValue();

		String fldText = mMinValFld.getText();
		double yMin, yMax;
		if (fldText.length() == 0) {
			yMin = 0.0;
		}
		else {
			try {
				yMin = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				yMin = 0.0;
			}
		}

		fldText = mMaxValFld.getText();
		if (fldText.length() == 0) {
			yMax = 6000.0;
		}
		else {
			try {
				yMax = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				yMax = 6000.0;
			}
		}

		// get a pretty range to compute increment
		double yInc;
		int yTics;
		Triplet prettyNums = JOAFormulas.GetPrettyRange(yMin, yMax);
		double tyInc = prettyNums.getVal3();
		fldText = mYInc.getText();
		if (fldText.length() == 0) {
			yInc = tyInc;
		}
		else {
			try {
				yInc = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				yInc = tyInc;
			}
		}

		yTics = ((Integer) mYTics.getValue()).intValue();
		int xTics = ((Integer) mXTics.getValue()).intValue();

		if (mCustomXAxisScale.isSelected()) {
			cps.setAutoScaleXAxis(false);
		}
		else {
			cps.setAutoScaleXAxis(true);
		}

		double tDefScale = (1 / (437 / mFileViewer.mTotMercDist)) * 1.852;
		fldText = mXScale.getText();
		if (fldText.length() == 0) {
			cps.setXAxisScale(tDefScale);
		}
		else {
			try {
				cps.setXAxisScale(Double.valueOf(fldText).doubleValue());
			}
			catch (NumberFormatException ex) {
				cps.setXAxisScale(tDefScale);
			}
		}

		cps.setColorBar(new NewColorBar(mColorBarPanel.getColorBar()));
		cps.setOffset(mOffset);
		cps.setStyle(mStyle);
		cps.setMarkers(mMarkers);
		cps.setMarkerSize(markerSize);
		cps.setPlotAxes(mPlotAxes.isSelected());
		cps.setIncludeCBAR(mColorLegend.isSelected());
		cps.setBrowsingEnabled(mEnableBrowsing.isSelected());
		cps.setWinYPlotMax(yMax);
		cps.setWinYPlotMin(yMin);
		cps.setYVar(cps.getInterp().getParam());
		cps.setFGColor(mPlotSpec.getFGColor());
		cps.setBGColor(mPlotSpec.getBGColor());
		cps.setColorLines(mPlotSpec.isColorLines());
		cps.setPlotEveryNthContour(mPlotSpec.getPlotEveryNthContour());
		cps.setLabelEveryNthContour(mPlotSpec.getLabelEveryNthContour());
		cps.setYInc(yInc);
		cps.setYTics(yTics);
		cps.setXTics(xTics);
		cps.setYVar(mPlotSpec.getYVar());
		cps.setPlotBottom(mPlotBottom.isSelected());

		if (mOrigColorLegend && !mColorLegend.isSelected()) {
			mRemovingColorLegend = true;
			mLayoutChanged = true;
		}
		if (!mOrigColorLegend && mColorLegend.isSelected()) {
			mAddingColorLegend = true;
			mLayoutChanged = true;
		}
		if (mOrigPlotAxes && !mPlotAxes.isSelected()) {
			mRemovingAxes = true;
			mLayoutChanged = true;
		}
		if (!mOrigPlotAxes && mPlotAxes.isSelected()) {
			mAddingAxes = true;
			mLayoutChanged = true;
		}
		if (mOrigEnableBrowsing && !mEnableBrowsing.isSelected()) {
			mRemovingBrowsing = true;
			mLayoutChanged = true;
		}
		if (!mOrigEnableBrowsing && mEnableBrowsing.isSelected()) {
			mAddingBrowsing = true;
			mLayoutChanged = true;
		}
		cps.setSymbolColor(mSymbolColorSwatch.getColor());

		// set whether there's an overlay contour field
		if (mOverlayInterpolation != null) {
			cps.setOverlayInterp(mOverlayInterpolation);
			for (int i = 0; i < mOverlayColorBar.getColors().length; i++) {
				if (mBlackContours.isSelected()) {
					mOverlayColorBar.setColorValue(i, Color.black);
				}
				else if (mWhiteContours.isSelected()) {
					mOverlayColorBar.setColorValue(i, Color.white);
				}
				else if (mCustomSingleColor.isSelected()) {
					mOverlayColorBar.setColorValue(i, mOverlayContourColorSwatch.getColor());
				}
			}
			cps.setOverlayColorBar(mOverlayColorBar);
			cps.setOverlayMarkers(mOverlayMarkers);
			cps.setOvlYVar(cps.getOverlayInterp().getParam());

			int overlayMarkerSize = ((Integer) mOverlayMarkerSizeFld.getValue()).intValue();

			cps.setOverlayMarkerSize(overlayMarkerSize);
			cps.setOverlaySymbolColor(mOverlaySymbolColorSwatch.getColor());
			cps.setOverlayContourColor(null);
			if (mBlackContours.isSelected()) {
				cps.setOverlayContourColor((Object) Color.black);
			}
			else if (mWhiteContours.isSelected()) {
				cps.setOverlayContourColor((Object) Color.white);
			}
			else if (mCustomSingleColor.isSelected()) {
				cps.setOverlayContourColor((Object) mOverlayContourColorSwatch.getColor());
			}
			cps.setPlotOverlayBottom(mPlotOverlayBottom.isSelected());
			cps.setPlotEveryNthOvlContour(mPlotSpec.getPlotEveryNthOvlContour());
		}
		cps.setLabelPrecision(mPlotSpec.getLabelPrecision());
		cps.setOvlLabelPrecision(mPlotSpec.getOvlLabelPrecision());

		if (mOffsetParameter.isSelected()) {
			cps.setXAxisLabels(ContourPlotSpecification.LABEL_OFFSET_PARAMETER);
		}
		else if (mStnNumCast.isSelected()) {
			cps.setXAxisLabels(ContourPlotSpecification.STN_NUM_OFFSET_PARAMETER);
		}
		else if (mLatLabel.isSelected()) {
			cps.setXAxisLabels(ContourPlotSpecification.LAT_OFFSET_PARAMETER);
		}
		else if (mLonLabel.isSelected()) {
			cps.setXAxisLabels(ContourPlotSpecification.LON_OFFSET_PARAMETER);
		}

		try {
			cps.setSkipXAxisLabels(((Integer) mSkipLabelsFld.getValue()).intValue());
		}
		catch (Exception ex) {
			cps.setSkipXAxisLabels(0);
		}
		return cps;
	}

	public boolean removingColorLegend() {
		return mRemovingColorLegend;
	}

	public boolean addingColorLegend() {
		return mAddingColorLegend;
	}

	public boolean removingAxes() {
		return mRemovingAxes;
	}

	public boolean addingAxes() {
		return mAddingAxes;
	}

	public boolean removingBrowsing() {
		return mRemovingBrowsing;
	}

	public boolean addingBrowsing() {
		return mAddingBrowsing;
	}

	public void addedColorLegend() {
		mOrigColorLegend = true;
		mAddingColorLegend = false;
		mLayoutChanged = true;
	}

	public void addedBrowsing() {
		mOrigEnableBrowsing = true;
		mAddingBrowsing = false;
		mLayoutChanged = true;
	}

	public void removedColorLegend() {
		mOrigColorLegend = false;
		mRemovingColorLegend = false;
		mLayoutChanged = true;
	}

	public void removedBrowsing() {
		mOrigEnableBrowsing = false;
		mRemovingBrowsing = false;
		mLayoutChanged = true;
	}

	public void restoreOriginalColorBar(ContourPlotSpecification inSpec) {
		inSpec.getColorBar().setNumLevels(mNumOldLevels);
		inSpec.getColorBar().setValues(mOldColorBarValues);
		inSpec.getColorBar().setColors(mOldColorBarColors);
	}

	public boolean isLayoutChanged() {
		return mLayoutChanged;
	}

	public boolean isColorBarChanged() {
		boolean result = mColorBarPanel.isChanged() || mColorBarChanged;
		if (mColorBarPanel.isChanged()) {
			mColorBarPanel.setChanged(false);
		}
		return result;
	}

	public void maintainButtons() {
		if (mPlotSpec.isOverlayContours()) {
			everyThingPanel.setEnabledAt(1, true);
		}
		else {
			everyThingPanel.setEnabledAt(1, false);
		}

		if (mColorsFromColorBar.isSelected()) {
			if (!mOverlayColorCombo.isEnabled()) {
				mOverlayColorCombo.setEnabled(true);
				mUsingLbl.setEnabled(true);
			}
		}
		else {
			if (mOverlayColorCombo.isEnabled()) {
				mOverlayColorCombo.setEnabled(false);
				mUsingLbl.setEnabled(false);
			}
		}
	}

	public void createOverlayColorBar(int curveShape) {
		// get the interpolation
		mOverlayInterpolation = mPlotSpec.getOverlayInterp();

		// get base and end levels from the interpolation
		double baseLevel = mOverlayInterpolation.getMinValue();
		double endLevel = mOverlayInterpolation.getMaxValue();
		double numLevels = 0;
		if (mOverlayAutoscaleColorScheme == 1 || mOverlayAutoscaleColorScheme == 3 || mOverlayAutoscaleColorScheme == 5
		    || mOverlayAutoscaleColorScheme == 7) {
			numLevels = 32;
		}
		else {
			numLevels = 16;
		}

		// compute new color bar values
		mOverlayColorBarValues = null;
		mOverlayColorBarValues = new double[(int) numLevels];
		mOverlayColorBarColors = new Color[(int) numLevels];
		if (curveShape == JOAConstants.LINEAR) {
			double increment = (endLevel - baseLevel) / (numLevels - 1);
			for (int i = 0; i < (int) numLevels; i++) {
				mOverlayColorBarValues[i] = baseLevel + (i * increment);
			}
		}
		else if (curveShape == JOAConstants.EXPONENTIALUP || curveShape == JOAConstants.EXPONENTIALDOWN) {
			double shape = JOAFormulas.getShape(baseLevel, endLevel);
			double scaledMax = Math.abs(endLevel - baseLevel);
			double lnScaledMin = Math.log(shape);
			double lnScaledMax = Math.log(scaledMax + shape);
			double increment = (lnScaledMax - lnScaledMin) / (numLevels - 1);

			for (int i = 0; i < (int) numLevels; i++) {
				if (curveShape == JOAConstants.EXPONENTIALUP) {
					// lower
					if (baseLevel < endLevel) {
						mOverlayColorBarValues[i] = baseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
					else {
						mOverlayColorBarValues[i] = baseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
				}
				else if (curveShape == JOAConstants.EXPONENTIALDOWN) {
					// upper
					if (baseLevel < endLevel) {
						mOverlayColorBarValues[(int) numLevels - i - 1] = endLevel - Math.exp(lnScaledMin + (i * increment))
						    + shape;
					}
					else {
						mOverlayColorBarValues[(int) numLevels - i - 1] = endLevel + Math.exp(lnScaledMin + (i * increment))
						    - shape;
					}
				}
			}
		}
		else if (curveShape == JOAConstants.LOGISTIC) {
			// logistic is a pieced together from upper and lower asymptote
			int mid = 0;
			int nl = (int) numLevels;
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
			double increment = (lnScaledMax - lnScaledMin) / ((double) mid - 1);

			// lower
			for (int i = 0; i < mid; i++) {
				if (baseLevel < newEndLevel) {
					mOverlayColorBarValues[mid - i - 1] = newEndLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
				}
				else {
					mOverlayColorBarValues[mid - i - 1] = newEndLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
				}
			}

			// lower asymptote from midpoint to endlevel
			double newBaseLevel = newEndLevel;
			shape = JOAFormulas.getShape(newBaseLevel, endLevel);
			scaledMax = Math.abs(newBaseLevel - endLevel);
			lnScaledMin = Math.log(shape);
			lnScaledMax = Math.log(scaledMax + shape);
			increment = (lnScaledMax - lnScaledMin) / ((double) mid - 1);

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
					mOverlayColorBarValues[i + mid] = newBaseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
				}
				else {
					mOverlayColorBarValues[i + mid] = newBaseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
				}
			}

		}

		// assign colors to color bar
		Color startColor = Color.blue;
		Color midColor = Color.white;
		Color endColor = Color.red;
		if (mOverlayAutoscaleColorScheme <= 4) {
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

			int nl = (int) numLevels;
			int mid = 0;
			double deltaRed = 0;
			double deltaGreen = 0;
			double deltaBlue = 0;
			if (nl % 2 > 0) {
				// odd number of entries--middle color is middle color swatch
				mid = (nl / 2) + 1;
				mColorBarColors[mid - 1] = midColor;

				// blend from start to mid
				deltaRed = (double) (midColor.getRed() - startColor.getRed()) / (double) mid;
				deltaGreen = (double) (midColor.getGreen() - startColor.getGreen()) / (double) mid;
				deltaBlue = (double) (midColor.getBlue() - startColor.getBlue()) / (double) mid;

				int c = 1;
				for (int i = 0; i < mid - 1; i++) {
					double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
					double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
					double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
					c++;
					mOverlayColorBarColors[i] = new Color((float) newRed, (float) newGreen, (float) newBlue);
				}

				// blend from mid to end
				deltaRed = (double) (endColor.getRed() - midColor.getRed()) / (double) mid;
				deltaGreen = (double) (endColor.getGreen() - midColor.getGreen()) / (double) mid;
				deltaBlue = (double) (endColor.getBlue() - midColor.getBlue()) / (double) mid;

				c = 1;
				for (int i = mid; i < (int) numLevels; i++) {
					double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
					double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
					double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
					c++;
					mOverlayColorBarColors[i] = new Color((float) newRed, (float) newGreen, (float) newBlue);
				}
			}
			else {
				// even number of entries--middle color is in between middle values
				mid = nl / 2;

				// blend from start to mid
				deltaRed = (double) (midColor.getRed() - startColor.getRed()) / (double) (mid + 1);
				deltaGreen = (double) (midColor.getGreen() - startColor.getGreen()) / (double) (mid + 1);
				deltaBlue = (double) (midColor.getBlue() - startColor.getBlue()) / (double) (mid + 1);

				int c = 1;
				for (int i = 0; i < mid; i++) {
					double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
					double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
					double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
					c++;
					mOverlayColorBarColors[i] = new Color((float) newRed, (float) newGreen, (float) newBlue);
				}

				// blend from mid to end
				deltaRed = (double) (endColor.getRed() - midColor.getRed()) / (double) (mid + 1);
				deltaGreen = (double) (endColor.getGreen() - midColor.getGreen()) / (double) (mid + 1);
				deltaBlue = (double) (endColor.getBlue() - midColor.getBlue()) / (double) (mid + 1);

				c = 1;
				for (int i = mid; i < (int) numLevels; i++) {
					double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
					double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
					double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
					c++;
					mOverlayColorBarColors[i] = new Color((float) newRed, (float) newGreen, (float) newBlue);
				}
			}
		}
		else {
			// create a rainbow
			float hue = 0;
			float sat = 1;
			float light = 1;
			float startHue = 0;
			if (mOverlayAutoscaleColorScheme < 7) {
				startHue = 0;
			}
			else {
				startHue = 270;
			}
			float hueAngleDelta = (float) 270 / (float) numLevels;
			for (int i = 0; i < (int) numLevels; i++) {
				if (mOverlayAutoscaleColorScheme < 7) {
					hue = (startHue + ((float) i * hueAngleDelta)) / 360;
				}
				else {
					hue = (startHue - ((float) i * hueAngleDelta)) / 360;
				}
				mOverlayColorBarColors[i] = new Color(Color.HSBtoRGB(hue, sat, light));
			}
		}

		// create a new NewColorBar
		String paramText = new String(mOverlayInterpolation.getParamName().toUpperCase());
		String paramUnits = mFileViewer.mAllProperties[mFileViewer.getPropertyPos(paramText, false)].getUnits();
		String titleText = new String("Autoscale");
		String descripText = new String("Autoscale");
		if (mOverlayColorBar == null) {
			mOverlayColorBar = new NewColorBar(mOverlayColorBarColors, mOverlayColorBarValues, (int) numLevels, paramText,
			    paramUnits, titleText, descripText);
		}
		else {
			// modify existing color bar
			mOverlayColorBar.setValues(mOverlayColorBarValues);
			mOverlayColorBar.setColors(mOverlayColorBarColors);
			mOverlayColorBar.setBaseLevel(mOverlayColorBarValues[0]);
			mOverlayColorBar.setEndLevel(mOverlayColorBarValues[(int) numLevels - 1]);
			mOverlayColorBar.setTitle(titleText);
			mOverlayColorBar.setParam(paramText);
			mOverlayColorBar.setDescription(descripText);
			mOverlayColorBar.setNumLevels((int) numLevels);
		}

		// display the new color bar
		if (mOverlayColorBarPanel != null) {
			// remove existing color bar component (if there is one)
			mOverlayUpperContents.remove(mOverlayColorBarPanel);
			mOverlayColorBarPanel = null;
		}

		if (mBlackContours.isSelected()) {
			mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, Color.black);
		}
		else if (mWhiteContours.isSelected()) {
			mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, Color.white);
		}
		else if (mCustomSingleColor.isSelected()) {
			mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, mOverlayContourColorSwatch
			    .getColor());
		}
//		else if (mColorsFromColorBar.isSelected()) {
//			mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, mOverlayColorBar);
//		}
		mOverlayColorBarPanel.setLinked(false);
		mOverlayColorBarPanel.setEnhanceable(false);
		mOverlayColorBarPanel.setBroadcastMode(false);
		mOverlayUpperContents.add(mOverlayColorBarPanel);
		mOverlayUpperContents.invalidate();
		this.validate();
	}

	public void createColorBar(int curveShape) {
		// get the interpolation
		Interpolation interp = mPlotSpec.getInterp();

		// get base and end levels from the interpolation
		double baseLevel = interp.getMinValue();
		double endLevel = interp.getMaxValue();
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
		mColorBarValues = new double[(int) numLevels];
		mColorBarColors = new Color[(int) numLevels];
		if (curveShape == JOAConstants.LINEAR) {
			double increment = (endLevel - baseLevel) / (numLevels - 1);
			for (int i = 0; i < (int) numLevels; i++) {
				mColorBarValues[i] = baseLevel + (i * increment);
			}
		}
		else if (curveShape == JOAConstants.EXPONENTIALUP || curveShape == JOAConstants.EXPONENTIALDOWN) {
			double shape = JOAFormulas.getShape(baseLevel, endLevel);
			double scaledMax = Math.abs(endLevel - baseLevel);
			double lnScaledMin = Math.log(shape);
			double lnScaledMax = Math.log(scaledMax + shape);
			double increment = (lnScaledMax - lnScaledMin) / (numLevels - 1);

			for (int i = 0; i < (int) numLevels; i++) {
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
						mColorBarValues[(int) numLevels - i - 1] = endLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
					}
					else {
						mColorBarValues[(int) numLevels - i - 1] = endLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
					}
				}
			}
		}
		else if (curveShape == JOAConstants.LOGISTIC) {
			// logistic is a pieced together from upper and lower asymptote
			int mid = 0;
			int nl = (int) numLevels;
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
			double increment = (lnScaledMax - lnScaledMin) / ((double) mid - 1);

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
			increment = (lnScaledMax - lnScaledMin) / ((double) mid - 1);

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

			int nl = (int) numLevels;
			int mid = 0;
			double deltaRed = 0;
			double deltaGreen = 0;
			double deltaBlue = 0;
			if (nl % 2 > 0) {
				// odd number of entries--middle color is middle color swatch
				mid = (nl / 2) + 1;
				mColorBarColors[mid - 1] = midColor;

				// blend from start to mid
				deltaRed = (double) (midColor.getRed() - startColor.getRed()) / (double) mid;
				deltaGreen = (double) (midColor.getGreen() - startColor.getGreen()) / (double) mid;
				deltaBlue = (double) (midColor.getBlue() - startColor.getBlue()) / (double) mid;

				int c = 1;
				for (int i = 0; i < mid - 1; i++) {
					double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
					double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
					double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
					c++;
					mColorBarColors[i] = new Color((float) newRed, (float) newGreen, (float) newBlue);
				}

				// blend from mid to end
				deltaRed = (double) (endColor.getRed() - midColor.getRed()) / (double) mid;
				deltaGreen = (double) (endColor.getGreen() - midColor.getGreen()) / (double) mid;
				deltaBlue = (double) (endColor.getBlue() - midColor.getBlue()) / (double) mid;

				c = 1;
				for (int i = mid; i < (int) numLevels; i++) {
					double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
					double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
					double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
					c++;
					mColorBarColors[i] = new Color((float) newRed, (float) newGreen, (float) newBlue);
				}
			}
			else {
				// even number of entries--middle color is in between middle values
				mid = nl / 2;

				// blend from start to mid
				deltaRed = (double) (midColor.getRed() - startColor.getRed()) / (double) (mid + 1);
				deltaGreen = (double) (midColor.getGreen() - startColor.getGreen()) / (double) (mid + 1);
				deltaBlue = (double) (midColor.getBlue() - startColor.getBlue()) / (double) (mid + 1);

				int c = 1;
				for (int i = 0; i < mid; i++) {
					double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
					double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
					double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
					c++;
					mColorBarColors[i] = new Color((float) newRed, (float) newGreen, (float) newBlue);
				}

				// blend from mid to end
				deltaRed = (double) (endColor.getRed() - midColor.getRed()) / (double) (mid + 1);
				deltaGreen = (double) (endColor.getGreen() - midColor.getGreen()) / (double) (mid + 1);
				deltaBlue = (double) (endColor.getBlue() - midColor.getBlue()) / (double) (mid + 1);

				c = 1;
				for (int i = mid; i < (int) numLevels; i++) {
					double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
					double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
					double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
					c++;
					mColorBarColors[i] = new Color((float) newRed, (float) newGreen, (float) newBlue);
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
			float hueAngleDelta = (float) 270 / (float) numLevels;
			for (int i = 0; i < (int) numLevels; i++) {
				if (mAutoscaleColorScheme < 7) {
					hue = (startHue + ((float) i * hueAngleDelta)) / 360;
				}
				else {
					hue = (startHue - ((float) i * hueAngleDelta)) / 360;
				}
				mColorBarColors[i] = new Color(Color.HSBtoRGB(hue, sat, light));
			}
		}

		// create a new NewColorBar
		String paramText = new String(mPlotSpec.getInterp().getParamName());
		String paramUnits = mFileViewer.mAllProperties[mFileViewer.getPropertyPos(paramText, false)].getUnits();
		String titleText = new String("Autoscale");
		String descripText = new String("Autoscale");
		if (mColorBar == null) {
			mColorBar = new NewColorBar(mColorBarColors, mColorBarValues, (int) numLevels, paramText, paramUnits, titleText, descripText);
		}
		else {
			// modify existing color bar
			mColorBar.setValues(mColorBarValues);
			mColorBar.setColors(mColorBarColors);
			mColorBar.setBaseLevel(mColorBarValues[0]);
			mColorBar.setEndLevel(mColorBarValues[(int) numLevels - 1]);
			mColorBar.setTitle(titleText);
			mColorBar.setParam(paramText);
			mColorBar.setDescription(descripText);
			mColorBar.setNumLevels((int) numLevels);
			// mColorBar.setSkipEvery((int)numLevels);
			// mColorBar.setLabelEvery((int)numLevels);
		}

		// display the new color bar
		if (mColorBarPanel != null) {
			// remove existing color bar component (if there is one)
			mUpperContents.remove(mColorBarPanel);
			mColorBarPanel = null;
		}
		mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mColorBar);
		mColorBarPanel.setLinked(false);
		mColorBarPanel.setEnhanceable(false);
		mColorBarPanel.setBroadcastMode(false);
		mUpperContents.add(mColorBarPanel);
		mUpperContents.invalidate();
		this.validate();
		mColorBarChanged = true;
	}

	// OK Button
	public void dialogDismissed(JDialog d) {
		if (d instanceof ConfigContourOptions) {
			ContourPlotSpecification mPlotSpecWContOptions = ((ConfigContourOptions) d).getPlotSpec();

			// incorporate the contour options into the plot spec
			mPlotSpec.setFGColor(mPlotSpecWContOptions.getFGColor());
			mPlotSpec.setBGColor(mPlotSpecWContOptions.getBGColor());
			mPlotSpec.setColorLines(mPlotSpecWContOptions.isColorLines());
			mPlotSpec.setPlotEveryNthContour(mPlotSpecWContOptions.getPlotEveryNthContour());
			mPlotSpec.setLabelEveryNthContour(mPlotSpecWContOptions.getLabelEveryNthContour());
			mPlotSpec.setLabelPrecision(mPlotSpecWContOptions.getLabelPrecision());
		}
		else if (d instanceof ConfigOvlContourOptions) {
			int numConts = ((ConfigOvlContourOptions) d).getNumContours();
			mPlotSpec.setPlotEveryNthOvlContour(numConts);
			int olp = ((ConfigOvlContourOptions) d).getOvlLabelPrecision();
			mPlotSpec.setOvlLabelPrecision(olp);
		}
		else if (d instanceof ConfigureInterpExport) {
			boolean mExportFomatIsJOA = ((ConfigureInterpExport) d).isJOAFormat();
			mIsTSV = ((ConfigureInterpExport) d).isTSV();
			mIsCSV = ((ConfigureInterpExport) d).isCSV();
			if (!mIsTSV && !mIsCSV) {
				mCustomDelim = ((ConfigureInterpExport) d).getCustomDelim();
				if (mCustomDelim == null || mCustomDelim.length() == 0) {
					mCustomDelim = "\t";
				}
			}

			mUseDefaultMissing = ((ConfigureInterpExport) d).isDefaultMissing();
			if (!mUseDefaultMissing) {
				mCustomMissing = ((ConfigureInterpExport) d).getCustomMissingVal();
				if (mCustomMissing == null || mCustomMissing.length() == 0) {
					mCustomMissing = "-99";
				}
			}
			save(mExportFomatIsJOA);
		}
	}

	// Cancel button
	public void dialogCancelled(JDialog d) {
		;
	}

	// something other than the OK button
	public void dialogDismissedTwo(JDialog d) {
		;
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogApply(JDialog d) {
	}

	public void dialogApplyTwo(Object d) {
	}

	public void save(boolean isJOAFlag) {
		String suggestedName = "interpvals.txt";
		if (isJOAFlag) {
			suggestedName = "interpvals.jos";
		}
		// get a filename
		Frame fr = new Frame();
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		FileDialog f = new FileDialog(fr, "Save interpolation values as:", FileDialog.SAVE);
		f.setDirectory(directory);
		f.setFile(suggestedName);
		f.setVisible(true);
		directory = f.getDirectory();
		String fs = f.getFile();
		f.dispose();
		if (directory != null && fs != null) {
			File nf = new File(directory, fs);
			try {
				if (!isJOAFlag) {
					saveInterpVals(nf);
				}
				else {
					exportSpreadsheet("-99", "\t", nf);
				}
			}
			catch (Exception ex) {
			}
		}
	}

	public void exportSpreadsheet(String missingValStr, String delimStr, File file) {
		Interpolation interp = ((JOAContourPlotWindow) mParent).getInterpolation();
		double[][] interpVals = interp.getValues();
		String[] stnIDs = interp.getStnValues();
		int nsta = interp.getNumStns();
		double[] lats = interp.getLatValues();
		double[] lons = interp.getLonValues();
		double[] bottoms = interp.getBottomDepths();
		int numLevels = interp.getLevels();

		// get the interpolation surface
		NewInterpolationSurface mSurface = interp.getSurface();
		double[] levelVals = mSurface.getValues();

		// create a progress dialog
		ProgressDialog progress = new ProgressDialog(mFileViewer, "Exporting Spreadsheet Data...", Color.blue, Color.white);
		progress.setVisible(true);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
			DataOutputStream out = new DataOutputStream(bos);

			// write the column headers */
			out.writeBytes("Section" + delimStr + "Stn. Num" + delimStr + "Cast Num." + delimStr + "Lat." + delimStr + "Lon."
			    + delimStr + "Bottom" + delimStr);

			String paramName = interp.getParamName();
			String surfaceName = interp.getSurface().getParam();

			// write the parameter headers
			boolean axisIsPres = false;
			out.writeBytes(surfaceName);
			if (surfaceName.toLowerCase().indexOf("pres") >= 0) {
				out.writeBytes(":R");
				axisIsPres = true;
			}
			out.writeBytes(delimStr);
			out.writeBytes(paramName);
			int paramPos = mFileViewer.getPropertyPos(paramName, false);

			// write the units
			if (paramPos >= 0) {
				out.writeBytes(" " + mFileViewer.mAllProperties[paramPos].getUnits());
			}

			out.writeBytes("\r");

			for (int s = 0; s < nsta; s++) {
				for (int z = 0; z < numLevels; z++) {
					if (bottoms[s] != JOAConstants.MISSINGVALUE && axisIsPres && levelVals[z] <= bottoms[s]) {
						continue;
					}
						String stnID = stnIDs[s];

						out.writeBytes(mFileViewer.getTitle() + ":" + interp.getName() + delimStr);
						out.writeBytes(stnID + delimStr);

						// cast number
						out.writeBytes(missingValStr + delimStr);

						// lat
						out.writeBytes(lats[s] + delimStr);

						// lon
						out.writeBytes(lons[s] + delimStr);

						out.writeBytes(bottoms[s] + delimStr);

						// now write the interps value
						// write the surface value (usually depth)
						out.writeBytes(levelVals[z] + delimStr);

						out.writeBytes(interpVals[z][s] + delimStr);
						out.writeBytes("\r");
				}
			}

			out.flush();
			out.close();

			// type the file if on the Mac
			if (JOAConstants.ISMAC) {
				MRJFileUtils.setFileTypeAndCreator(file, new MRJOSType("TEXT"), new MRJOSType("JOAA"));
			}
		}
		catch (Exception ex) {
			System.out.println("An Error occurred exporting the current data");
			ex.printStackTrace();
		}
		finally {
			progress.dispose();
		}
	}

	public void saveInterpVals(File file) {
		String delim;
		if (mIsTSV) {
			delim = "\t";
		}
		else if (mIsCSV) {
			delim = ",";
		}
		else {
			delim = mCustomDelim;
		}

		// get the interpolation
		Interpolation interp = ((JOAContourPlotWindow) mParent).getInterpolation();
		double[][] interpVals = interp.getValues();
		double[] distVals = interp.getDistValues();
		String[] stnIDs = interp.getStnValues();
		int nsta = interp.getNumStns();

		// get the interpolation surface
		NewInterpolationSurface mSurface = interp.getSurface();
		double[] levelVals = mSurface.getValues();

		// set up the file and write output
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
			DataOutputStream out = new DataOutputStream(bos);

			// write the top row of column headers: station IDs
			out.writeBytes(delim);
			for (int c = 0; c < nsta; c++) {
				if (c < nsta - 1) {
					out.writeBytes(stnIDs[c] + delim);
				}
				else {
					out.writeBytes(stnIDs[c] + "\r");
				}
			}

			// write the 2nd row of column headers: distance
			out.writeBytes(delim);
			for (int c = 0; c < nsta; c++) {
				if (c < nsta - 1) {
					out.writeBytes(distVals[c] + delim);
				}
				else {
					out.writeBytes(distVals[c] + "\r");
				}

			}

			for (int row = 0; row < mSurface.getNumLevels(); row++) {
				// write a row of data with level first
				out.writeBytes(levelVals[row] + delim);
				for (int c = 0; c < nsta; c++) {
					if (c < nsta - 1) {
						out.writeBytes(interpVals[row][c] + delim);
					}
					else {
						out.writeBytes(interpVals[row][c] + "\r");
					}
				}
			}

			out.flush();
			out.close();
		}
		catch (Exception ex) {
			System.out.println("An Error occurred exporting the current data");
		}
	}
}

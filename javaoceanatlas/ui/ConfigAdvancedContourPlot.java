/*
 * $Id: ConfigAdvancedContourPlot.java,v 1.16 2005/09/23 14:51:23 oz Exp $
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
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;
import javaoceanatlas.calculations.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ConfigAdvancedContourPlot extends JOAJDialog implements ListSelectionListener, ActionListener,
    ButtonMaintainer, ItemListener, DialogClient, TreeSelectionListener {
	protected FileViewer mFileViewer;
	protected String mTitle;
	protected Component mComp;
	protected int mSelParam = -1;
	protected int mSelSurface = -1;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJButton mExportBtn = null;
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
	private JOAJRadioButton mOffsetParameter = null;
	private JOAJRadioButton mStnNumCast = null;
	private JOAJRadioButton mLatLabel = null;
	private JOAJRadioButton mLonLabel = null;

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
	protected int mAutoscaleColorScheme = 1;
	protected int mStyle = JOAConstants.STYLE_FILLED;
	protected int mOffset = JOAConstants.OFFSET_DISTANCE;
	protected int mMarkers = JOAConstants.MARKERS_NONE;
	protected int mMarkerSize = 1;
	protected ContourPlotSpecification mPlotSpec;
	protected Color mFG = Color.white;
	protected Color mBG = JOAConstants.DEFAULT_CONTENTS_COLOR;
	protected Color mSymbolColor = Color.black;
	protected Swatch mSymbolColorSwatch;
	protected boolean mColorLines = false;
	protected int mPlotEveryNthContour = 2;
	protected int mLabelEveryNthContour = 3;
	protected int mPlotEveryNthOverlayContour = 1;
	protected JOAJButton mOptionBtn = null;
	protected ParameterChooser mParamList;
	protected SurfaceChooser mSurfaceList;
	protected Interpolation mInterpolation = null;
	protected boolean mClipExtrapolated = true;
	protected double mRefLevel = -99;
	protected double mFarFieldLimit = 200;
	protected boolean mUseFarFieldLimit = false;
	protected boolean mTopDownFlag = true;
	protected NewInterpolationSurface mSurface = null;
	protected JOAJButton mAdvInterpOpt = null;
	protected String mInterpName = null;
	protected boolean mIsResidualInterp;
	protected boolean[] mMeanCastStnList = null;
	protected JFrame mParent;
	protected JPanel mAdvancedPane = null;
	protected JOAJLabel mScaleUnitsLabel;
	protected boolean mFillEdges = false;
	protected boolean mClipExtrapolation = true;
	protected int mInterpolationType;
	protected int mFarStdLevelLimit;
	protected int mFarBottleLimit;
	protected int mFarStationLimit;
	protected UVCoordinate[] mMeanCastFromFile;
	protected String mMeanCastFV = null;
	private int mXDim;
	private int mYDim;
	private double mCay;
	private int mNumGridSpaces;
	private boolean mMask;

	// specific to overlay contour plots
	protected JOAJButton mOverlayOptionBtn = null;
	protected VariableInspector mOverlayParamList;
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
//	protected JOAJRadioButton mColorsFromColorBar = null;
	protected Color mOverlayLineColor = Color.black;
	protected JOAJLabel mUsingLbl = null;
	protected JOAJCheckBox mPlotBottom = null;
	protected JOAJCheckBox mPlotOverlayBottom = null;
	protected JOAJCheckBox mEnableOverlayBrowsing = null;
	private Timer timer = new Timer();
	private int mLabelPrecision = 3;
 	private int mOvlLabelPrecision = 3;
 	private boolean mOvlIsAvailable = true;
 	private JSpinner mSkipLabelsFld;

	public ConfigAdvancedContourPlot(JFrame par, FileViewer fv) {
		super(par, "Contour Plot", false);
		mParent = par;
		mFileViewer = fv;
		this.init();
	}

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		everyThingPanel = new JOAJTabbedPane();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 0));

		// upper panel contains the interpolation chooser and contour choices stuff
		mUpperContents = new JPanel();
		// mUpperContents.setLayout(new GridLayout(1, 3, 0, 0));
		mUpperContents.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 5));

		// container for the interpolation stuff
		JPanel interpContainer = new JPanel();
		interpContainer.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 5));
		JPanel interpListContainer = new JPanel();
		interpListContainer.setLayout(new GridLayout(2, 1, 0, 5));

		// create the parameter and surface choosers
		mParamList = new ParameterChooser(mFileViewer, new String(b.getString("kParameter")), this, 5,
		    "SALT                            ");
		mParamList.init();
		interpListContainer.add(mParamList);

		// create the surface list
		mSurfaceList = new SurfaceChooser(mFileViewer, new String(b.getString("kInterpolationSurfaces")), this, 5);
		mSurfaceList.init();
		interpListContainer.add(mSurfaceList);

		// add the adv features button
		mAdvInterpOpt = new JOAJButton(b.getString("kInterpOptions"));
		mAdvInterpOpt.setActionCommand("interpolations");
		mAdvInterpOpt.addActionListener(this);

		// add interpContainer to mUpperContents
		interpContainer.add(interpListContainer);
		interpContainer.add(mAdvInterpOpt);
		mUpperContents.add(interpContainer);

		// Y Axis Range goes in the middle of the middle panel
		JPanel axesStuffCont = new JPanel();
		axesStuffCont.setLayout(new BorderLayout(0, 0));
		JPanel axesStuff = new JPanel();
		axesStuff.setLayout(new GridLayout(1, 3, 0, 5));

		JPanel yAxisStuff = new JPanel();
		yAxisStuff.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.TOP, 0));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kYAxisRange"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		yAxisStuff.setBorder(tb);

		JPanel line0 = new JPanel();
		line0.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 2));
		line0.add(new JOAJLabel(b.getString("kMinimum")));
		mMinValFld = new JOAJTextField(6);
		mMinValFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line0.add(mMinValFld);

		JPanel line1 = new JPanel();
		line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 2));
		line1.add(new JOAJLabel(b.getString("kMaximum")));
		mMaxValFld = new JOAJTextField(6);
		mMaxValFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line1.add(mMaxValFld);

		JPanel line2 = new JPanel();
		line2.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 2));
		line2.add(new JOAJLabel(b.getString("kIncrement")));
		mYInc = new JOAJTextField(6);
		mYInc.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line2.add(mYInc);

		JPanel line3 = new JPanel();
		line3.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 2));
		line3.add(new JOAJLabel(b.getString("kNoMinorTicks")));

		SpinnerNumberModel model = new SpinnerNumberModel(1, 0, 100, 1);
		mYTics = new JSpinner(model);
		line3.add(mYTics);

		yAxisStuff.add(line0);
		yAxisStuff.add(line1);
		yAxisStuff.add(line2);
		yAxisStuff.add(line3);
		axesStuff.add(yAxisStuff);

		JPanel xAxisStuff = new JPanel();
		xAxisStuff.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kXAxisScale"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		xAxisStuff.setBorder(tb);

		JPanel line4 = new JPanel();
		line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 2));
		mAutoscaleXAxis = new JOAJRadioButton(b.getString("kAutoscale"), true);
		line4.add(mAutoscaleXAxis);

		JPanel line5 = new JPanel();
		line5.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 2));
		mCustomXAxisScale = new JOAJRadioButton(b.getString("kCustom"));
		line5.add(mCustomXAxisScale);

		ButtonGroup xabg = new ButtonGroup();
		xabg.add(mAutoscaleXAxis);
		xabg.add(mCustomXAxisScale);

		JPanel line6 = new JPanel();
		line6.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 2));
		line6.add(new JOAJLabel("      "));
		mXScale = new JOAJTextField(6);
		mXScale.setText(JOAFormulas.formatDouble("    ", 3, false));
		mXScale.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				mAutoscaleXAxis.setSelected(false);
				mCustomXAxisScale.setSelected(true);
			}
		};
		mXScale.addMouseListener(mouseListener);
		line6.add(mXScale);
		mScaleUnitsLabel = new JOAJLabel(b.getString("kkm/cm"));
		line6.add(mScaleUnitsLabel);

		JPanel line77 = new JPanel();
		line77.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 1));
		line77.add(new JOAJLabel(b.getString("kNoMinorTicks")));

		SpinnerNumberModel model2 = new SpinnerNumberModel(3, 0, 100, 1);
		mXTics = new JSpinner(model2);
		line77.add(mXTics);

		xAxisStuff.add(line4);
		xAxisStuff.add(line5);
		xAxisStuff.add(line6);
		xAxisStuff.add(line77);
		axesStuff.add(xAxisStuff);
		axesStuff.add(new JPanel());

		// color bar list
		JPanel palContPanel = new JPanel();
		palContPanel.setLayout(new BorderLayout(5, 0));

		JPanel palPanel = new JPanel();
		palPanel.setLayout(new BorderLayout(5, 5));

		palPanel.add(new JOAJLabel(b.getString("kColorbars"), JOAJLabel.LEFT), "North");
		mColorBarList = JOAFormulas.getColorBarList();
		mPaletteList = new JOAJList(mColorBarList);
		MouseListener mouseListener2 = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				;
			}
		};
		mPaletteList.addMouseListener(mouseListener2);
		mPaletteList.setVisibleRowCount(8);
		mPaletteList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mPaletteList.setPrototypeCellValue("SALT on PRES;0-1000");
		JScrollPane listScroller = new JScrollPane(mPaletteList);
		palPanel.add(listScroller, "Center");
		palContPanel.add(palPanel, "North");

		// panel for autoscaling goes here
		JPanel autoscalePanel = new JPanel();
		autoscalePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 10));
		tb = BorderFactory.createTitledBorder(b.getString("kCreateAutoscaleColorbar"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		autoscalePanel.setBorder(tb);

		JPanel line7 = new JPanel();
		line7.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
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
		line7.add(shapePanel);
		autoscalePanel.add(line7);
		
		mLinear.setToolTipText(b.getString("kLinearTip"));
		mPowerUp.setToolTipText(b.getString("kIncreasingExpTip"));
		mPowerDown.setToolTipText(b.getString("kDecreasingExpTip"));
		mLogistic.setToolTipText(b.getString("kReverseSTip"));
		
		JPanel line8 = new JPanel();
		line8.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		line8.add(new JOAJLabel(b.getString("kUsing")));
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
		line8.add(mColorCombo);
		autoscalePanel.add(line8);

		JPanel palcont2 = new JPanel();
		palcont2.setLayout(new BorderLayout(5, 0));
		palcont2.add(autoscalePanel, "North");
		palContPanel.add(palcont2, "Center");

		mPaletteList.addListSelectionListener(this);
		mUpperContents.add(palContPanel);

		// blank color bar panel
		mColorBarPanel = new ColorBarPanel(mParent, mFileViewer, null);
		mColorBarPanel.setLinked(false);
		mColorBarPanel.setEnhanceable(false);
		mColorBarPanel.setBroadcastMode(false);
		mUpperContents.add(mColorBarPanel);

		// Middle panel is for options
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new BorderLayout(5, 0));
		// middlePanel.setBorder(BorderFactory.createTitledBorder(b.getString("kOptions")));

		// Options container goes in the north of the middle panel
		JPanel optionsCont = new JPanel();
		optionsCont.setLayout(new GridLayout(2, 4));

		// Style Panel
		JPanel styleCont = new JPanel();
		styleCont.setLayout(new BorderLayout(5, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kStyle"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		styleCont.setBorder(tb);
		JPanel styles = new JPanel();
		styles.setLayout(new GridLayout(3, 1, 5, 0));
		mColorFill = new JOAJRadioButton(b.getString("kColorFill"), true);
		mColorFillWContours = new JOAJRadioButton(b.getString("kColorFillWContours"));
		mContourLines = new JOAJRadioButton(b.getString("kContourLines"));
		styles.add(mColorFill);
		styles.add(mColorFillWContours);
		styles.add(mContourLines);
		ButtonGroup bg = new ButtonGroup();
		bg.add(mColorFill);
		bg.add(mColorFillWContours);
		bg.add(mContourLines);
		styleCont.add("North", styles);
		mContourLines.addItemListener(this);
		mColorFill.addItemListener(this);
		mColorFillWContours.addItemListener(this);

		mOptionBtn = new JOAJButton(b.getString("kContourOptions"));
		mOptionBtn.setActionCommand("options");
		mOptionBtn.addActionListener(this);
		JPanel optBtnInset = new JPanel();
		optBtnInset.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		optBtnInset.add(mOptionBtn);
		styleCont.add(optBtnInset, "South");
		optionsCont.add(styleCont);

		// Offset Panel
		JPanel offsetCont = new JPanel(new BorderLayout(5, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kOffset"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		offsetCont.setBorder(tb);
		JPanel offsets = new JPanel(new GridLayout(4, 1, 5, 0));
		mSequence = new JOAJRadioButton(b.getString("kSequence"));
		mDistance = new JOAJRadioButton(b.getString("kDistance"), true);
		mLatitude = new JOAJRadioButton(b.getString("kLatitude"));
		mLongitude = new JOAJRadioButton(b.getString("kLongitude"));
		mTime = new JOAJRadioButton(b.getString("kTime"));
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
		optionsCont.add(offsetCont);

		// set the advanced offset labels
		setOffsetLabels(JOAConstants.OFFSET_DISTANCE);
		setOffsetScale(JOAConstants.OFFSET_DISTANCE);

		// Marker Panel
		JPanel markerCont = new JPanel();
		// markerCont.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		markerCont.setLayout(new BorderLayout(5, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kMarkers"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		markerCont.setBorder(tb);
		JPanel markers = new JPanel();
		markers.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		mNone = new JOAJRadioButton(b.getString("kNone"), true);
		mSurfaceLevels = new JOAJRadioButton(b.getString("kSurfaceLevels"));
		mObservations = new JOAJRadioButton(b.getString("kObservations"));
		markers.add(mNone);
		markers.add(mSurfaceLevels);
		markers.add(mObservations);
		bg = new ButtonGroup();
		bg.add(mNone);
		bg.add(mSurfaceLevels);
		bg.add(mObservations);
		
		// Label Panel
		JPanel labelCont = new JPanel(new BorderLayout(5, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kXAxisLabels"));
			
		labelCont.setBorder(tb);
		JPanel labels = new JPanel(new GridLayout(2, 1, 0, 0));//ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		mOffsetParameter = new JOAJRadioButton(b.getString("kOffsetParameter"), true);//mPlotSpec.getXAxisLabels() == ContourPlotSpecification.LABEL_OFFSET_PARAMETER);
		mStnNumCast = new JOAJRadioButton(b.getString("kStationNumber2"), false);//mPlotSpec.getXAxisLabels() == ContourPlotSpecification.STN_NUM_OFFSET_PARAMETER);
		mLatLabel = new JOAJRadioButton(b.getString("kLatitude"), false);//mPlotSpec.getXAxisLabels() == ContourPlotSpecification.LAT_OFFSET_PARAMETER);
		mLonLabel = new JOAJRadioButton(b.getString("kLongitude"), false);//mPlotSpec.getXAxisLabels() == ContourPlotSpecification.LON_OFFSET_PARAMETER);
		labels.add(mOffsetParameter);
		labels.add(mStnNumCast);
		labels.add(mLatLabel);
		labels.add(mLonLabel);
		ButtonGroup bgl = new ButtonGroup();
		bgl.add(mOffsetParameter);
		bgl.add(mStnNumCast);
		bgl.add(mLatLabel);
		bgl.add(mLonLabel);
		JPanel skipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		SpinnerNumberModel model2z = new SpinnerNumberModel(1, 1, 100, 1);
		mSkipLabelsFld = new JSpinner(model2z);
		skipPanel.add(new JLabel(b.getString("kSkip")));
		skipPanel.add(mSkipLabelsFld);
		labelCont.add(BorderLayout.NORTH, labels);
		labelCont.add(BorderLayout.CENTER, skipPanel);
		optionsCont.add(labelCont);

		JPanel linem = new JPanel();
		linem.add(new JOAJLabel(b.getString("kSize")));
		SpinnerNumberModel model3 = new SpinnerNumberModel(2, 1, 100, 1);
		mMarkerSizeFld = new JSpinner(model3);
		linem.add(mMarkerSizeFld);
		linem.add(new JOAJLabel(" " + b.getString("kColor2")));
		mSymbolColorSwatch = new Swatch(Color.black);
		linem.add(mSymbolColorSwatch);
		markers.add(linem);
		markerCont.add("North", markers);

		mNone.addItemListener(this);
		mSurfaceLevels.addItemListener(this);
		mObservations.addItemListener(this);
		optionsCont.add(markerCont);

		// Other Options
		JPanel otherCont = new JPanel();
		otherCont.setLayout(new BorderLayout(5, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kOther"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		otherCont.setBorder(tb);
		JPanel others = new JPanel();
		others.setLayout(new GridLayout(4, 1, 5, 0));
		mPlotAxes = new JOAJCheckBox(b.getString("kPlotAxes"), true);
		mColorLegend = new JOAJCheckBox(b.getString("kColorLegend"), true);
		mEnableBrowsing = new JOAJCheckBox(b.getString("kShowCrossSections"), false);
		mPlotBottom = new JOAJCheckBox(b.getString("kPlotBottom"), true);
		// others.add(mPlotAxes);
		others.add(mColorLegend);
		others.add(mEnableBrowsing);
		others.add(mPlotBottom);
		others.add(new JOAJLabel(""));
		otherCont.add("North", others);
		mPlotAxes.addItemListener(this);
		mColorLegend.addItemListener(this);
		mEnableBrowsing.addItemListener(this);
		optionsCont.add(otherCont);

		// add the upper panels
		middlePanel.add("North", optionsCont);

		// name goes in the center of the middle panel
		JPanel line10 = new JPanel();
		line10.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));
		line10.add(new JOAJLabel(b.getString("kWindowName")));
		mNameField = new JOAJTextField(40);
		mNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line10.add(mNameField);
		middlePanel.add("Center", new TenPixelBorder(line10, 5, 5, 5, 5));

		// build upper part of dialog
		mainPanel.add("North", new TenPixelBorder(mUpperContents, 5, 5, 0, 5));
		mainPanel.add("Center", new TenPixelBorder(middlePanel, 5, 5, 0, 5));

		// build the overlay panel
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

		JPanel inspectorContainer = new JPanel();
		inspectorContainer.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 5));
		mOverlayParamList = new VariableInspector(mFileViewer, new String(b.getString("kOverlayParameter")), 10, this);
		inspectorContainer.add(mOverlayParamList);
		mOverlayUpperContents.add(inspectorContainer);

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

		// blank color bar panel
		mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, null, Color.black);
		mOverlayColorBarPanel.setLinked(false);
		mOverlayColorBarPanel.setEnhanceable(false);
		mOverlayColorBarPanel.setBroadcastMode(false);
		mOverlayUpperContents.add(mOverlayColorBarPanel);
		overlayPanel.add(mOverlayUpperContents, "North");

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

		mBlackContours = new JOAJRadioButton(b.getString("kBlackContours"), true);
		mWhiteContours = new JOAJRadioButton(b.getString("kWhiteContours"));
		mCustomSingleColor = new JOAJRadioButton(b.getString("kCustomSingleColor"));
		mOverlayContourColorSwatch = new JOASwatch(Color.black, this);
		mOverlayContourColorSwatch.setEnabled(false);
		JPanel custColorLine = new JPanel();
		custColorLine.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		custColorLine.add(mCustomSingleColor);
		custColorLine.add(mOverlayContourColorSwatch);
//		mColorsFromColorBar = new JOAJRadioButton(b.getString("kColorsFromColorBar"));
		overlayStyles.add(mBlackContours);
		overlayStyles.add(mWhiteContours);
		overlayStyles.add(custColorLine);
//		overlayStyles.add(mColorsFromColorBar);
		ButtonGroup sbg = new ButtonGroup();
		sbg.add(mBlackContours);
		sbg.add(mWhiteContours);
		sbg.add(mCustomSingleColor);
//		sbg.add(mColorsFromColorBar);
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
		mOverlayNone = new JOAJRadioButton(b.getString("kNone"), true);
		mOverlaySurfaceLevels = new JOAJRadioButton(b.getString("kSurfaceLevels"));
		mOverlayObservations = new JOAJRadioButton(b.getString("kObservations"));
		overlayMarkers.add(mOverlayNone);
		overlayMarkers.add(mOverlaySurfaceLevels);
		overlayMarkers.add(mOverlayObservations);
		ButtonGroup ovlbg = new ButtonGroup();
		ovlbg.add(mOverlayNone);
		ovlbg.add(mOverlaySurfaceLevels);
		ovlbg.add(mOverlayObservations);

		JPanel overlayLinem = new JPanel();
		overlayLinem.add(new JOAJLabel(b.getString("kSize")));

		SpinnerNumberModel smodel = new SpinnerNumberModel(2, 1, 100, 1);
		mOverlayMarkerSizeFld = new JSpinner(smodel);

		overlayLinem.add(mOverlayMarkerSizeFld);
		overlayLinem.add(new JOAJLabel(" " + b.getString("kColor2")));
		mOverlaySymbolColorSwatch = new Swatch(Color.black);
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
		othersOvl.setLayout(new GridLayout(2, 1, 5, 0));
		// mEnableOverlayBrowsing = new
		// JOAJCheckBox(b.getString("kShowCrossSections"), false);
		mPlotOverlayBottom = new JOAJCheckBox(b.getString("kPlotBottom"), false);
		// others.add(mPlotAxes);
		// othersOvl.add(mEnableOverlayBrowsing);
		othersOvl.add(mPlotOverlayBottom);
		otherOverlayCont.add("North", othersOvl);
		overlayOptionsCont.add(otherOverlayCont);

		// add the upper panels
		overlayMiddlePanel.add("North", overlayOptionsCont);
		overlayPanel.add("South", overlayMiddlePanel);

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kPlot"));
		mOKBtn.setActionCommand("ok");
		mExportBtn = new JOAJButton("Export JSON Spec...");
		mExportBtn.setActionCommand("export");
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JOAJButton(b.getString("kDone"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mExportBtn);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mExportBtn);
			dlgBtnsPanel.add(mCancelButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mOKBtn.addActionListener(this);
		mExportBtn.addActionListener(this);
		mCancelButton.addActionListener(this);
		axesStuffCont.add("North", axesStuff);
		everyThingPanel.addTab(b.getString("kPrimaryContours"), mainPanel);
		
//
//	  <featuregroup id="kOverlayContours" name="Allow Overlay Contours on Contour Plots" enabled="true">
//	  	<feature name="Overlay Contours Based on an Isosurface" id="kIsoSurface" version="1.0" status="rel" enabled="true" visible="true"/>
//	  	<feature name="Overlay Contours Based on a Calculated Station Parameter" id="kStationCalculation" version="1.0" status="rel" enabled="true" visible="true"/>
		FeatureGroup fg = JOAConstants.JOA_FEATURESET.get("kOverlayContours");
		
		
		if (fg.hasFeature("kOverlayContours") && fg.isFeatureEnabled("kOverlayContours")) {
			mOvlIsAvailable = true;
		}
		else {
			mOvlIsAvailable = false;
		}
		
		if (mOvlIsAvailable) {
			everyThingPanel.addTab(b.getString("kOverlayContours"), overlayPanel);
		}
		everyThingPanel.addTab(b.getString("kAxesScales"), new TenPixelBorder(axesStuffCont, 10, 10, 10, 10));

		contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", everyThingPanel);
		this.pack();

		runTimer();

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
			defScale = 1.0 / (437 / mFileViewer.mTotalStations);
		}
		else if (offset == JOAConstants.OFFSET_DISTANCE) {
			defScale = (1 / (437 / mFileViewer.mTotMercDist)) * 1.852;

		}
		else if (offset == JOAConstants.OFFSET_LATITUDE) {
			defScale = 1.0 / (437 / mFileViewer.mTotLatDegs);
		}
		else if (offset == JOAConstants.OFFSET_LONGITUDE) {
			defScale = 1.0 / (437 / mFileViewer.mTotLonDegs);
		}
		else if (offset == JOAConstants.OFFSET_TIME) {
			defScale = 1.0 / (437 / mFileViewer.getTimeLength());
		}
		mXScale.setText(JOAFormulas.formatDouble(String.valueOf(defScale), 3, false));
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mParamList.getJList()) {
			// get the param
			mSelParam = mParamList.getJList().getSelectedIndex();
			if (mSelParam < 0) { return; }
			String selYParamText = (String) mParamList.getJList().getSelectedValue();

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
				JOptionPane.showMessageDialog(f, "All values for " + selYParamText + " are missing. " + "\n"
				    + "Select a new Y parameter");
				mParamList.clearSelection();
				mSelParam = 0;
			}
			else {
				// select a colorbar in the colorbar list
				String selParamStr = ((String) mParamList.getJList().getSelectedValue()).toUpperCase();
				boolean found = false;
				for (int i = 0; i < mColorBarList.size(); i++) {
					String param = ((String) mColorBarList.elementAt(i)).toUpperCase();
					if (param.startsWith(selParamStr)) {
						// position list
						mPaletteList.setSelectedIndex(i);
						mPaletteList.ensureIndexIsVisible(i);
						found = true;
						break;
					}
					else if (param.indexOf(selParamStr) >= 0) {
						// position list
						mPaletteList.setSelectedIndex(i);
						mPaletteList.ensureIndexIsVisible(i);
						found = true;
						break;
					}
				}

				if (!found) {
					mPaletteList.clearSelection();
				}

				generateInterpName();
			}
		}
		else if (evt.getSource() == mSurfaceList.getJList()) {
			generateInterpName();

			// fill in the reference level list
			// mSurface = null;
			int tempSelectedSurface = mSurfaceList.getJList().getSelectedIndex();
			String newSurfaceName = (String) mSurfaceList.getJList().getSelectedValue();
			if (newSurfaceName == null || tempSelectedSurface == mSelSurface) { return; }
			mSelSurface = tempSelectedSurface;

			// read the surface from disk
			try {
				mSurface = JOAFormulas.getSurface(newSurfaceName);
			}
			catch (Exception ex) {
				mSurface = null;
			}

			// fill in the range
			mMinValFld.setText(JOAFormulas.formatDouble(String.valueOf(mSurface.getBaseLevel()), 3, false));
			mMaxValFld.setText(JOAFormulas.formatDouble(String.valueOf(mSurface.getEndLevel()), 3, false));

			// get a pretty range to compute increment
			Triplet prettyNums = JOAFormulas.GetPrettyRange(mSurface.getBaseLevel(), mSurface.getEndLevel());
			double yInc = prettyNums.getVal3();
			mYInc.setText(JOAFormulas.formatDouble(String.valueOf(yInc), 3, false));

			int yTics = 3;
			mYTics.setValue(new Integer(yTics));

			// reset interpolation stuff
			mTopDownFlag = true;
			mInterpolationType = Interpolation.LOCAL_INTERPOLATION;
			mClipExtrapolated = true;
			mFillEdges = false;
			mFarBottleLimit = 2;
			mFarStdLevelLimit = 2;
			mFarStationLimit = 2;
			mFarFieldLimit = 200;
			mUseFarFieldLimit = false;
			mRefLevel = -99;
			mXDim = 101;
			mYDim = 101;
			mCay = 5.0;
			mNumGridSpaces = 10;
			mMask = false;

			// look for a default map
			try {
				File defaultFile = JOAFormulas.getSupportFile("JOADefault_interp.xml");
				if (defaultFile != null) {
					getInterpSpecification(defaultFile);
					return;
				}
			}
			catch (Exception ex) {
				// fall through and just use defaults
			}
		}
		else if (evt.getSource() == mPaletteList) {
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
			if (newCBARName == null || tempSelectedCBAR == mSelectedOverlayCBAR) { return; }
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

	private boolean TOPDOWN;
	private int INTERPOLATIONTYPE;
	private boolean CLIPEXTRAPOLATED;
	private boolean FILLEDGES;
	private int FARBOTTLELIMIT;
	private int FARSTDLEVELLIMIT;
	private int FARSTNLIMIT;
	private double FARFIELDLIMIT;
	private boolean USEFARFIELD;

	private class MapNotifyStr extends HandlerBase {
		public void startDocument() throws SAXException {
			TOPDOWN = true;
			INTERPOLATIONTYPE = Interpolation.LOCAL_INTERPOLATION;
			CLIPEXTRAPOLATED = true;
			FILLEDGES = false;
			FARBOTTLELIMIT = 2;
			FARSTDLEVELLIMIT = 2;
			FARSTNLIMIT = 2;
			FARFIELDLIMIT = 200.0;
			USEFARFIELD = false;
		}

		public void startElement(String name, AttributeList amap) throws SAXException {
			if (name.equals("joainterp")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("direction")) {
						if (amap.getValue(i).equals("bottomup")) {
							TOPDOWN = false;
						}
					}
					else if (amap.getName(i).equals("filledges")) {
						if (amap.getValue(i).equals("true")) {
							FILLEDGES = true;
						}
					}
					else if (amap.getName(i).equals("fclipextrapolation")) {
						if (amap.getValue(i).equals("false")) {
							CLIPEXTRAPOLATED = false;
						}
					}
				}
			}
			else if (name.equals("nointerpolation")) {
				INTERPOLATIONTYPE = Interpolation.NO_MISSING_INTERPOLATION;
			}
			else if (name.equals("vertinterpolation")) {
				INTERPOLATIONTYPE = Interpolation.LOCAL_INTERPOLATION;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("maxobs")) {
						try {
							FARBOTTLELIMIT = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
						}
					}
				}
			}
			else if (name.equals("horzinterpolation")) {
				INTERPOLATIONTYPE = Interpolation.FAR_FIELD_INTERPOLATION;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("maxstdlevls")) {
						try {
							FARSTDLEVELLIMIT = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
						}
					}
					if (amap.getName(i).equals("numfarstns")) {
						try {
							FARSTNLIMIT = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
						}
					}
					if (amap.getName(i).equals("maxdist")) {
						USEFARFIELD = true;
						try {
							FARFIELDLIMIT = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
						}
					}
				}
			}
		}

		public void characters(char[] ch, int start, int len) throws SAXException {
		}

		public void endElement(String name) throws SAXException {
		}
	}

	@SuppressWarnings("unchecked")
	protected void getInterpSpecification(File file) throws Exception {
		try {
			// parse as xml first
			Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
			org.xml.sax.Parser parser = (org.xml.sax.Parser) c.newInstance();
			MapNotifyStr notifyStr = new MapNotifyStr();
			parser.setDocumentHandler(notifyStr);
			parser.parse(file.getPath());

			mTopDownFlag = TOPDOWN;
			mInterpolationType = INTERPOLATIONTYPE;
			mClipExtrapolated = CLIPEXTRAPOLATED;
			mFillEdges = FILLEDGES;
			mFarBottleLimit = FARBOTTLELIMIT;
			mFarStdLevelLimit = FARSTDLEVELLIMIT;
			mFarStationLimit = FARSTNLIMIT;
			mFarFieldLimit = FARFIELDLIMIT;
			mUseFarFieldLimit = USEFARFIELD;
			
			/**
			 * @todo save and restore the zgrid settings
			 * 
			 */

		}
		catch (Exception xmlEx) {
			// xmlEx.printStackTrace();
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJComboBox) {
			JOAJComboBox cb = (JOAJComboBox) evt.getSource();
			if (cb == mColorCombo) {
				mAutoscaleColorScheme = cb.getSelectedIndex() + 1;
			}
			else if (cb == mOverlayColorCombo) {
				mOverlayAutoscaleColorScheme = cb.getSelectedIndex() + 1;
			}
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
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("export")) {
			// create a new interpolation
			createInterpolation();

			if (mInterpolation == null) { return; }
			ContourPlotSpecification spec = createPlotSpec();
			
    	Frame fr = new Frame();
    	String directory = System.getProperty("user.dir");
    	FileDialog f = new FileDialog(fr, "Export JSON data as:", FileDialog.SAVE);
    	f.setDirectory(directory);
    	if (mFileViewer.mCurrOutFileName != null) {
    		f.setFile(mFileViewer.mCurrOutFileName + "_JOAContourSpec.jsn");
    	}
    	else {
    		f.setFile(mFileViewer.getTitle() + "_JOAContourSpec.jsn");
    	}

    	Rectangle dBounds = f.getBounds();
    	Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    	int x = sd.width / 2 - dBounds.width / 2;
    	int y = sd.height / 2 - dBounds.height / 2;
    	f.setLocation(x, y);
    	f.setVisible(true);

    	directory = f.getDirectory();
    	f.dispose();
    	if (directory != null && f.getFile() != null) {
    		File outFile = new File(directory, f.getFile());
    		spec.exportJSON(outFile, mInterpolation);
    	}
		}
		else if (cmd.equals("ok")) {
			// create a new interpolation
			createInterpolation();

			if (mInterpolation == null) { return; }

			// save the interpolation
			mFileViewer.addInterpolation(mInterpolation);

			if (mOverlayColorBar != null) {
				createOverlayInterpolation();
				mFileViewer.addInterpolation(mOverlayInterpolation);
			}

			ContourPlotSpecification spec = createPlotSpec();
			try {
				spec.writeToLog("New Contour Plot (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}

			// create a contour plot
			JOAContourPlotWindow contPlot = new JOAContourPlotWindow(mFileViewer, spec, mFileViewer);
			contPlot.pack();
			contPlot.setVisible(true);
			mFileViewer.addOpenWindow(contPlot);

			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("options")) {
			if (mInterpolation == null) {
				// create a new interpolation
				createInterpolation();
			}

			// contour options
			ConfigContourOptions contOpts = new ConfigContourOptions(mParent, mFileViewer, this, createPlotSpec());
			contOpts.pack();
			contOpts.setVisible(true);
		}
		else if (cmd.equals("ovloptions")) {
			if (mInterpolation == null) {
				// create a new interpolation
				createInterpolation();
			}

			// contour options
			ConfigOvlContourOptions contOpts = new ConfigOvlContourOptions(mParent, mFileViewer, this, createPlotSpec());
			contOpts.pack();
			contOpts.setVisible(true);
		}
		else if (cmd.equals("interpolations")) {
			// create a contour plot
			ConfigInterpOptions contOpts = new ConfigInterpOptions(mParent, mFileViewer, this, mSurface,
			    mFileViewer.mAllProperties[mSelParam].getVarLabel(), mRefLevel, mTopDownFlag, mInterpolationType, mFillEdges,
			    mClipExtrapolated, mFarBottleLimit, mFarStdLevelLimit, mFarStationLimit, mUseFarFieldLimit, mFarFieldLimit,
			    mXDim, mYDim, mCay, mNumGridSpaces, mMask);
			contOpts.pack();
			contOpts.setVisible(true);
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
	}

	public ContourPlotSpecification createPlotSpec() {
		ContourPlotSpecification cps = new ContourPlotSpecification();

		cps.setInterp(mInterpolation);
		cps.setWinTitle(mNameField.getText());
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
		// cps.mIncludeCBAR = mIncludeObsPanel.isSelected();
		cps.setBrowsingEnabled(mEnableBrowsing.isSelected());
		cps.setWinYPlotMax(yMax);
		cps.setWinYPlotMin(yMin);
		cps.setWidth(600);
		cps.setHeight(500);
		cps.setYParam(cps.getInterp().getParam());
		cps.setFGColor(mFG);
		cps.setBGColor(mBG);
		cps.setSymbolColor(mSymbolColorSwatch.getColor());
		cps.setColorLines(mColorLines);
		cps.setPlotEveryNthContour(mPlotEveryNthContour);
		cps.setLabelEveryNthContour(mLabelEveryNthContour);
		cps.setPlotEveryNthOvlContour(mPlotEveryNthOverlayContour);
		cps.setYInc(yInc);
		cps.setYTics(yTics);
		cps.setXTics(xTics);
		cps.setPlotBottom(mPlotBottom.isSelected());
		cps.setLabelPrecision(mLabelPrecision);
		cps.setOvlLabelPrecision(mOvlLabelPrecision);

		// set whether there's an overlay contour field
		if (mOverlayInterpolation != null && mOverlayColorBar != null) {
			cps.setOverlayInterp(mOverlayInterpolation);
			cps.setOvlYVar(mOverlayInterpolation.getParam());
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

			int overlayMarkerSize = ((Integer) mOverlayMarkerSizeFld.getValue()).intValue();

			cps.setOverlayMarkerSize(overlayMarkerSize);
			cps.setOverlaySymbolColor(mOverlaySymbolColorSwatch.getColor());
			if (mBlackContours.isSelected()) {
				cps.setOverlayContourColor((Object) Color.black);
			}
			else if (mWhiteContours.isSelected()) {
				cps.setOverlayContourColor((Object) Color.white);
			}
			else if (mCustomSingleColor.isSelected()) {
				cps.setOverlayContourColor((Object) mOverlayContourColorSwatch.getColor());
			}
			else {
				cps.setOverlayContourColor(null);
			}
			cps.setPlotOverlayBottom(mPlotOverlayBottom.isSelected());
		}
		
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
		
		cps.setSkipXAxisLabels(((Integer) mSkipLabelsFld.getValue()).intValue());
		return cps;
	}

	public void maintainButtons() {
		if (mSelParam >= 0) {
			mSurfaceList.getJList().setEnabled(true);
		}
		else {
			mSurfaceList.getJList().setEnabled(false);
		}

		if (mSelParam >= 0 && mSelSurface >= 0 && mColorBar != null) {
			mOKBtn.setEnabled(true);
			mExportBtn.setEnabled(true);
			mLinear.setEnabled(true);
			mPowerUp.setEnabled(true);
			mPowerDown.setEnabled(true);
			mLogistic.setEnabled(true);
			mAdvInterpOpt.setEnabled(true);
			mOptionBtn.setEnabled(true);
			if (mOvlIsAvailable)
				everyThingPanel.setEnabledAt(2, true);
		}
		else if (mSelParam >= 0 && mSelSurface >= 0 && mColorBar == null) {
			mOKBtn.setEnabled(false);
			mExportBtn.setEnabled(false);
			mLinear.setEnabled(true);
			mPowerUp.setEnabled(true);
			mPowerDown.setEnabled(true);
			mLogistic.setEnabled(true);
			mAdvInterpOpt.setEnabled(true);
			mOptionBtn.setEnabled(false);
			// mPaletteList.setEnabled(true);
			if (mOvlIsAvailable)
				everyThingPanel.setEnabledAt(2, false);
		}
		else {
			mOKBtn.setEnabled(false);
			mExportBtn.setEnabled(false);
			mLinear.setEnabled(false);
			mPowerUp.setEnabled(false);
			mPowerDown.setEnabled(false);
			mLogistic.setEnabled(false);
			mOptionBtn.setEnabled(false);
			mAdvInterpOpt.setEnabled(false);
			// mPaletteList.setEnabled(false);
			if (mOvlIsAvailable)
				everyThingPanel.setEnabledAt(2, false);
		}

		if (mOverlayParamList.getSelectedVariable() == null) {
			mOverlayLinear.setEnabled(false);
			mOverlayPowerUp.setEnabled(false);
			mOverlayPowerDown.setEnabled(false);
			mOverlayLogistic.setEnabled(false);
		}
		else if (mOverlayParamList.getSelectedVariable() != null) {
			mOverlayLinear.setEnabled(true);
			mOverlayPowerUp.setEnabled(true);
			mOverlayPowerDown.setEnabled(true);
			mOverlayLogistic.setEnabled(true);
		}

//		if (mColorsFromColorBar.isSelected()) {
//			if (!mOverlayColorCombo.isEnabled()) {
//				mOverlayColorCombo.setEnabled(true);
//				mUsingLbl.setEnabled(true);
//			}
//		}
//		else {
			if (mOverlayColorCombo.isEnabled()) {
				mOverlayColorCombo.setEnabled(false);
				mUsingLbl.setEnabled(false);
			}
//		}
	}

	public void createInterpolation() {
		// Interpolate
		// get the parameter
		JOAVariable selectedParam = mParamList.getSelectedVariable();
		String newSurfaceName = (String) mSurfaceList.getJList().getSelectedValue();

		// read the surface from disk
		try {
			mSurface = JOAFormulas.getSurface(newSurfaceName);
		}
		catch (Exception ex) {
			mSurface = null;
		}

		boolean error = false;
		if (!JOAFormulas.paramExists(mFileViewer, mSurface.getParam())) {
			if (JOAFormulas.isCalculatable(mSurface.getParam())) {
				// make a new calculation
				Calculation calc = JOAFormulas.createCalcFromName(mFileViewer, mSurface.getParam());

				if (calc != null) {
					// do calculation
					mFileViewer.addCalculation(calc);
					mFileViewer.doCalcs();
				}
			}
			else {
				JFrame f = new JFrame("Interpolation Manager Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Surface parameter does not exist in this file" + "\n"
				    + "and couldn't be calculated");
				error = true;
			}
		}

		if (!error) {
			// create the interpolation
			if (mInterpolationType != Interpolation.ZGRID_INTERPOLATION ) {
				mInterpolation = new LinearInterpolation(mFileViewer, newSurfaceName, mSurface, selectedParam, mFileViewer
			    .getPropertyPos(mSurface.getParam(), false), mInterpName,
			    mFileViewer.mAllProperties[mSelParam].getVarLabel(), mRefLevel, mTopDownFlag, mInterpolationType, mFillEdges,
			    mClipExtrapolation, mFarBottleLimit, mFarStdLevelLimit, mFarStationLimit, mUseFarFieldLimit, mFarFieldLimit,
			    mIsResidualInterp, mMeanCastStnList, mMeanCastFromFile, mMeanCastFV);
			}
			else {
				System.out.println("mCay = " + mCay);
				System.out.println("mGridSpacing = " + mNumGridSpaces);
				System.out.println("mXDim = " + mXDim);
				System.out.println("mYDim" + mYDim);
				mInterpolation = new ZGridInterpolation(mFileViewer, newSurfaceName, mSurface, selectedParam, mFileViewer
				    .getPropertyPos(mSurface.getParam(), false), mInterpName, mFileViewer.mAllProperties[mSelParam].getVarLabel(), 
				    mRefLevel, mIsResidualInterp, mMeanCastStnList, mMeanCastFromFile, mMeanCastFV, mXDim,  mYDim,  mCay,  mNumGridSpaces,
				    mMask);
			}

			// dereference the interpolation to the reference level if needed
			mInterpolation.dereference();
		}
	}

	public void createOverlayInterpolation() {
		// Interpolate
		// get the parameter
		JOAVariable selectedParam = mOverlayParamList.getSelectedVariable();
		FileViewer fv = selectedParam.getFileViewer();

		String newSurfaceName = (String) mSurfaceList.getJList().getSelectedValue();

		// read the surface from disk
		try {
			mSurface = JOAFormulas.getSurface(newSurfaceName);
		}
		catch (Exception ex) {
			mSurface = null;
		}

		boolean error = false;
		if (!JOAFormulas.paramExists(fv, mSurface.getParam())) {
			if (JOAFormulas.isCalculatable(mSurface.getParam())) {
				// make a new calculation
				Calculation calc = JOAFormulas.createCalcFromName(mFileViewer, mSurface.getParam());

				if (calc != null) {
					// do calculation
					mFileViewer.addCalculation(calc);
					mFileViewer.doCalcs();
				}
			}
			else {
				JFrame f = new JFrame("Interpolation Manager Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Surface parameter does not exist in this file" + "\n"
				    + "and couldn't be calculated");
				error = true;
			}
		}

		if (!error) {
			// create the interpolation
			if (mInterpolationType != Interpolation.ZGRID_INTERPOLATION ) {
				mOverlayInterpolation = new LinearInterpolation(fv, newSurfaceName, mSurface, selectedParam, fv.getPropertyPos(mSurface
			    .getParam(), false), mInterpName, selectedParam.getVarName(), mRefLevel, mTopDownFlag, mInterpolationType,
			    mFillEdges, mClipExtrapolation, mFarBottleLimit, mFarStdLevelLimit, mFarStationLimit, mUseFarFieldLimit,
			    mFarFieldLimit, mIsResidualInterp, mMeanCastStnList, mMeanCastFromFile, mMeanCastFV);
			}
			else {
				mOverlayInterpolation = new ZGridInterpolation(mFileViewer, newSurfaceName, mSurface, selectedParam, mFileViewer
				    .getPropertyPos(mSurface.getParam(), false), mInterpName, mFileViewer.mAllProperties[mSelParam].getVarLabel(), 
				    mRefLevel, mIsResidualInterp, mMeanCastStnList, mMeanCastFromFile, mMeanCastFV, mXDim,  mYDim,  mCay,  mNumGridSpaces,
				    mMask);
			}

			// dereference the interpolation to the reference level if needed
			mOverlayInterpolation.dereference();
			generateInterpName();
		}
	}

	public void createColorBar(int curveShape) {
		// create a new interpolation
		createInterpolation();

		// get base and end levels from the interpolation
		double baseLevel = mInterpolation.getMinValue();
		double endLevel = mInterpolation.getMaxValue();

		boolean canCenterOnZero = false;
		// /if (baseLevel < 0 && endLevel > 0 && mAutoscaleColorScheme <= 4)
		// canCenterOnZero = true;

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

			if (canCenterOnZero) {
				/*
				 * for (int i=0; i<(int)numLevels; i++) { double levVal = baseLevel +
				 * (i * increment); if (levVal < whiteLevel) { minVal = levVal;
				 * whiteLevel = i; } } int nUL = (int)numLevels - whiteLevel - 1; int
				 * nLL = whiteLevel; double lInc= baseLevel/(nLL - 1); for (int i=0; i<(int)nLL;
				 * i++) { mColorBarValues[i] = baseLevel + (i * increment); }
				 * mColorBarValues[whiteLevel] = 0.0; double uInc= endLevel/(nUL - 1);
				 * for (int i=whiteLevel+1; i<(int)numLevels; i++) { mColorBarValues[i] =
				 * baseLevel + (i * increment); }
				 */
			}
			else {
				for (int i = 0; i < (int) numLevels; i++) {
					mColorBarValues[i] = baseLevel + (i * increment);
				}
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
		String paramText = new String(((String) mParamList.getJList().getSelectedValue()).toUpperCase());
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
			mColorBar.setParamUnits(paramUnits);
			mColorBar.setDescription(descripText);
			mColorBar.setNumLevels((int) numLevels);
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
	}

	public void createOverlayColorBar(int curveShape) {
		// create a new interpolation
		createOverlayInterpolation();

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
			if (mOverlayAutoscaleColorScheme <= 2) {
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
		String paramText = new String(mOverlayParamList.getSelectedVariable().getVarName().toUpperCase());
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

	// ok
	public void dialogDismissed(JDialog d) {
		if (d instanceof ConfigContourOptions) {
			mPlotSpec = ((ConfigContourOptions) d).getPlotSpec();
			mFG = mPlotSpec.getFGColor();
			mBG = mPlotSpec.getBGColor();
			mSymbolColor = mPlotSpec.getSymbolColor();
			mColorLines = mPlotSpec.isColorLines();
			mPlotEveryNthContour = mPlotSpec.getPlotEveryNthContour();
			mLabelEveryNthContour = mPlotSpec.getLabelEveryNthContour();
			mLabelPrecision = mPlotSpec.getLabelPrecision();
		}
		else if (d instanceof ConfigOvlContourOptions) {
			int numConts = ((ConfigOvlContourOptions) d).getNumContours();
			mPlotEveryNthOverlayContour = numConts;
			mOvlLabelPrecision = ((ConfigOvlContourOptions) d).getOvlLabelPrecision();
		}
		else if (d instanceof ConfigInterpOptions) {
			// get the interpolation options
			mTopDownFlag = ((ConfigInterpOptions) d).getTopDownFlag();
			mInterpolationType = ((ConfigInterpOptions) d).getInterpolationType();
			mFillEdges = ((ConfigInterpOptions) d).isFillEdges();
			mClipExtrapolated = ((ConfigInterpOptions) d).isClipExtrapolated();
			mFillEdges = ((ConfigInterpOptions) d).isFillEdges();
			mFarBottleLimit = ((ConfigInterpOptions) d).getFarBottleLimit();
			mFarStdLevelLimit = ((ConfigInterpOptions) d).getFarStdLevelLimit();
			mFarStationLimit = ((ConfigInterpOptions) d).getFarStationLimit();
			mFarFieldLimit = ((ConfigInterpOptions) d).getFarFieldLimit();
			mUseFarFieldLimit = ((ConfigInterpOptions) d).getUseFarFieldLimitFlag();
			mRefLevel = ((ConfigInterpOptions) d).getRefLevel();
			mInterpName = ((ConfigInterpOptions) d).getName();
			mIsResidualInterp = ((ConfigInterpOptions) d).isResidualInterp();
			mMeanCastFV = null;
			mMeanCastStnList = null;
			mXDim = ((ConfigInterpOptions) d).getXDim();
			mYDim = ((ConfigInterpOptions) d).getYDim();
			mCay = ((ConfigInterpOptions) d).getCay();
			mNumGridSpaces = ((ConfigInterpOptions) d).getNumGridSpaces();
			mMask = ((ConfigInterpOptions) d).isMask();

			if (mIsResidualInterp) {
				// figure out what kind of mean cast it is
				if (((ConfigInterpOptions) d).isMeanCastFromFile()) {
					// get the FV that mean Cast comes from
					FileViewer fv = ((ConfigInterpOptions) d).getMeanCastFV();

					// get the mean cast from the FileViewer
					mMeanCastFromFile = fv.getMeanCast((String) mParamList.getJList().getSelectedValue(), mSurface);
					mMeanCastFV = fv.getTitle();
				}
				else {
					mMeanCastStnList = ((ConfigInterpOptions) d).getStnList();
				}
			}
		}
		generateInterpName();
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
		if (mOverlayColorBarPanel != null) {
			// remove existing color bar component (if there is one)
			mOverlayUpperContents.remove(mOverlayColorBarPanel);
			mOverlayColorBarPanel = null;
			mOverlayColorBarPanel = new ColorBarPanel(mParent, mFileViewer, mOverlayColorBar, mOverlayLineColor);
			mOverlayColorBar = mOverlayColorBarPanel.getColorBar();
			mOverlayColorBarPanel.setLinked(false);
			mOverlayColorBarPanel.setEnhanceable(false);
			mOverlayColorBarPanel.setBroadcastMode(false);
			mOverlayUpperContents.add(mOverlayColorBarPanel);
			mOverlayUpperContents.invalidate();
			this.validate();
		}
	}

	public void generateInterpName() {
		String surfText = null;

		String residText = "";
		if (mIsResidualInterp) {
			residText = "Residual ";
		}

		if (mSurface != null) {
			surfText = mSurface.getParam() + "-" + mSurface.getTitle();
		}
		else {
			surfText = "?";
		}

		String refLevelText = null;
		if (mRefLevel == JOAConstants.MISSINGVALUE) {
			refLevelText = "";
			if (mMeanCastFV != null) {
				refLevelText = ", mean cast=" + mMeanCastFV;
			}
			else if (mMeanCastStnList != null && mMeanCastStnList.length > 0) {
				refLevelText = ", ref=comp. mean cast";
			}
		}
		else {
			refLevelText = ", ref=" + JOAFormulas.formatDouble(String.valueOf(mRefLevel), 2, false);
		}

		String mParam = mFileViewer.mAllProperties[mSelParam].getVarLabel().trim();
		if (mParam == null || mParam.length() == 0) {
			mParam = "?";
		}

		mInterpName = new String(surfText + refLevelText);

		String nameString = null;
		if (mOverlayInterpolation != null) {
			nameString = residText + mParam + " (" + mFileViewer.mFileViewerName + ") Overlay: "
			    + mOverlayInterpolation.getParamName().trim() + " ("
			    + mOverlayInterpolation.getParam().getFileViewer().mFileViewerName + ") on " + mInterpName;
		}
		else {
			nameString = residText + mParam + " (" + mFileViewer.mFileViewerName + ") on " + mInterpName;
		}

		//mNameField.setText(mXDim+","+mYDim+",k="+mCay+", ngs=" +mNumGridSpaces + ", mask="+mMask);//nameString);
		mNameField.setText(nameString);

	}

	public void valueChanged(TreeSelectionEvent e) {
		try {
			VariableNode vn = (VariableNode) e.getSource();

			// JOAVariable var = mOverlayParamList.getSelectedVariable();
			if (vn != null) {
				String selParamStr = vn.getName().toUpperCase();
				boolean found = false;
				for (int i = 0; i < mOverlayColorBarList.size(); i++) {
					String param = ((String) mOverlayColorBarList.elementAt(i)).toUpperCase();
					if (param.indexOf(selParamStr) >= 0) {
						// position list
						mOverlayPaletteList.setSelectedIndex(i);
						mOverlayPaletteList.ensureIndexIsVisible(i);
						found = true;
						break;
					}
				}

				if (!found) {
					mOverlayPaletteList.clearSelection();
				}

			}
		}
		catch (ClassCastException ex) {

		}
	}
}

/**
 * 
 */
/*
 * $Id: ConfigurePvPPlot.java,v 1.6 2005/09/07 18:49:30 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.beans.*;
import javax.swing.border.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ConfigLinePlot extends JOAJDialog implements ListSelectionListener, ActionListener,
    PropertyChangeListener, ButtonMaintainer, ItemListener, ChangeListener {

	protected FileViewer mFileViewer;
	protected String mTitle;
	protected Component mComp;
	protected ParameterChooser mXParamList;
	protected ParameterChooser mYParamList;
	protected int mSelXParam;
	protected int mSelYParam = -1;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJTextField mNameField = null;
	protected JSpinner mSizeField = null;
	protected JOAJTextField mRefPressField = null;
	protected JOAJCheckBox mIncludeObsPanel = null;
	protected JOAJCheckBox ignoreMissing = null;
	protected JOAJCheckBox mPlotAxes = null;
	protected JOAJCheckBox mPlotXGrid = null;
	protected JOAJCheckBox mPlotYGrid = null;
	protected JOAJCheckBox mPlotIsopycnals = null;
	protected JOAJRadioButton mPlotOnlyCurrStn = null;
	protected JOAJRadioButton mPlotAllStns = null;
	protected JOAJRadioButton mAccumulateStns = null;
	protected Swatch plotBg = null;
	protected Swatch axesColor = null;
	protected JOAJComboBox presetColorSchemes = null;
	protected AdvLinePlotOptions mAdvOpt = null;
	protected LinePlotSpecification mPlotSpec = null;
	protected JOAJComboBox mSymbolPopup = null;
	protected int mCurrSymbol = JOAConstants.SYMBOL_SQUAREFILLED;
	public Icon[] symbolData = null;
	protected JOAJSlider mPressSlider = null;
	protected boolean mCanPlotIsoPycnals = false;
	private Timer timer = new Timer();
	protected int mSaltAxis = 0;
	protected JOAJTabbedPane everyThingPanel;
	protected SectionChooser mSectionChooser;
	protected JOAJComboBox mColorCombo = null;
	protected Vector<String> mColorPaletteList = null;
	protected String mCurrPalette = null;
	protected JSpinner mLineWidth = null;

	public ConfigLinePlot(JFrame par, FileViewer fv) {
		super(par, "Line Plot", false);
		mFileViewer = fv;
		this.init();
	}

	public LinePlotSpecification createPlotSpec() {
		LinePlotSpecification ps = new LinePlotSpecification();
		// get the colors
		ps.setFGColor(axesColor.getColor());
		ps.setBGColor(plotBg.getColor());
		ps.setWidth(450);
		ps.setHeight(450);

		if (mIncludeObsPanel.isSelected()) {
			ps.setHeight(ps.getHeight() + 50);
		}

		ps.setSaltAxis(mSaltAxis);
		ps.setFileViewer(mFileViewer);
		ps.setXVarCode(mSelXParam);
		ps.setYVarCode(mSelYParam);
		ps.setWinTitle(mNameField.getText());
		ps.setIncludeObsPanel(mIncludeObsPanel.isSelected());
		ps.setIgnoreMissingObs(ignoreMissing.isSelected());
		ps.setXGrid(mPlotXGrid.isSelected());
		ps.setYGrid(mPlotYGrid.isSelected());
		ps.setPlotAxes(mPlotAxes.isSelected());
		ps.setPlotIsopycnals(mPlotIsopycnals.isSelected());
		ps.setCanPlotIsoPycnals(mCanPlotIsoPycnals);
		ps.setSymbol(mCurrSymbol);
		ps.setPlotOnlyCurrStn(mPlotOnlyCurrStn.isSelected());
		ps.setAccumulateStns(mAccumulateStns.isSelected());
		ps.setSymbolSize(((Integer)mSizeField.getValue()).intValue());
		ps.setLineWidth(((Integer)mLineWidth.getValue()).intValue());
		ps.setStnCycleColorPalette(mCurrPalette);

		double refPress;
		/*
		 * try { refPress = Double.valueOf(mRefPressField.getText()).doubleValue();
		 * } catch (Exception ex) { refPress = 0.0; }
		 */
		refPress = mPressSlider.getValue();
		ps.setRefPress(refPress);

		if (mAdvOpt.isChanged()) {
			mAdvOpt.upDateSettings();
			ps.setWinXPlotMin(mAdvOpt.getXMin());
			ps.setWinXPlotMax(mAdvOpt.getXMax());
			ps.setXInc(mAdvOpt.getXInc());
			ps.setXTics(mAdvOpt.getXTics());

			ps.setWinYPlotMin(mAdvOpt.getYMin());
			ps.setWinYPlotMax(mAdvOpt.getYMax());
			ps.setYInc(mAdvOpt.getYInc());
			ps.setYTics(mAdvOpt.getYTics());
		}
		else {
			// create pretty axes ranges really get these from the text fields if
			// needed
			ps.setXTics(1);
			ps.setYTics(1);
			double tempMin = mFileViewer.mAllProperties[mSelXParam].getPlotMin();
			double tempMax = mFileViewer.mAllProperties[mSelXParam].getPlotMax();
			Triplet newRange = JOAFormulas.GetPrettyRange(tempMin, tempMax);
			ps.setWinXPlotMin(newRange.getVal1());
			ps.setWinXPlotMax(newRange.getVal2());
			ps.setXInc(newRange.getVal3());

			tempMin = mFileViewer.mAllProperties[mSelYParam].getPlotMin();
			tempMax = mFileViewer.mAllProperties[mSelYParam].getPlotMax();
			newRange = JOAFormulas.GetPrettyRange(tempMin, tempMax);
			ps.setWinYPlotMin(newRange.getVal1());
			ps.setWinYPlotMax(newRange.getVal2());
			ps.setYInc(newRange.getVal3());
		}

		// get the param
		FileViewer mLinkedFV = mSectionChooser.getSelectedFileViewer();
		if (mLinkedFV != null) {
			ps.setFileViewer(mLinkedFV);
		}

		return ps;
	}

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

		// create the symbol popup menu
		mSymbolPopup = new JOAJComboBox();
		for (int i = 0; i < symbolData.length; i++) {
			mSymbolPopup.addItem(symbolData[i]);
		}
		mSymbolPopup.setSelectedIndex(mCurrSymbol - 1);

		// create the tabbed panel and the container panels
		everyThingPanel = new JOAJTabbedPane();
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 0));
		JPanel paramPanel = new JPanel();
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout(5, 0));
		paramPanel.setLayout(new GridLayout(1, 2, 5, 0));

		// create the two parameter chooser lists
		mXParamList = new ParameterChooser(mFileViewer, new String(b.getString("kXAxisParameter")), this, 5, "SALT", true);
		mYParamList = new ParameterChooser(mFileViewer, new String(b.getString("kYAxisParameter")), this, 5, "SALT");
		mXParamList.init();
		mYParamList.init();
		paramPanel.add(mXParamList);
		paramPanel.add(mYParamList);

		// add the section chooser
		mSectionChooser = new SectionChooser(mFileViewer, "Linked Section:", this, "XXXXXXXXXXXXXXXXXX");
		mSectionChooser.init();
		// paramPanel.add(mSectionChooser);

		upperPanel.add("Center", paramPanel);

		// Options
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 1));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kOptions"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		middlePanel.setBorder(tb);

		JPanel midStuff = new JPanel();
		midStuff.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));

		JPanel p1 = new JPanel();
		p1.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.LEFT, 0));
		mPlotAllStns = new JOAJRadioButton(b.getString("kPlotAllStns"), true);
		mPlotOnlyCurrStn = new JOAJRadioButton(b.getString("kPlotOlnyCurrStn"), false);
		mAccumulateStns = new JOAJRadioButton(b.getString("kAccumulateStns"), false);
		p1.add(mPlotAllStns);
		p1.add(mPlotOnlyCurrStn);
		p1.add(mAccumulateStns);
		midStuff.add(p1);

		ButtonGroup bg0 = new ButtonGroup();
		bg0.add(mPlotAllStns);
		bg0.add(mPlotOnlyCurrStn);
		bg0.add(mAccumulateStns);

		JPanel p2 = new JPanel();
		p2.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.LEFT, 0));
		mColorPaletteList = JOAFormulas.getColorPalList();
		mColorCombo = new JOAJComboBox(mColorPaletteList);

		if (JOAConstants.DEFAULT_LINE_PLOT_PALETTE != null && JOAConstants.DEFAULT_LINE_PLOT_PALETTE.length() > 0) {
			mCurrPalette = JOAConstants.DEFAULT_LINE_PLOT_PALETTE;
			mColorCombo.setSelectedItem((String)JOAConstants.DEFAULT_LINE_PLOT_PALETTE);
		}
		else {
			mCurrPalette = (String)mColorPaletteList.elementAt(0);
			mColorCombo.setSelectedIndex(0);
		}
		mColorCombo.addItemListener(this);

		JPanel stnColor = new JPanel(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));
		stnColor.add(new JLabel("Cycle station colors using:"));
		stnColor.add(mColorCombo);
		p2.add(stnColor);
		
		JPanel lwPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		lwPanel.add(new JLabel("Line width:"));
		SpinnerNumberModel model3 = new SpinnerNumberModel(3, 1, 15, 1);
		mLineWidth = new JSpinner(model3);
		mLineWidth.addChangeListener(this);
		lwPanel.add(mLineWidth);
		p2.add(lwPanel);
		
		midStuff.add(p2);
		middlePanel.add(midStuff);

		// containers for the non-advanced options
		JPanel nonAdvOptions = new JPanel();
		nonAdvOptions.setLayout(new BorderLayout(5, 0));

		JPanel ctrNonAdvOptions = new JPanel();
		ctrNonAdvOptions.setLayout(new GridLayout(1, 2, 2, 2));

		// plot axes goes in ctrNonAdvOptions
		JPanel axesOptions = new JPanel();
		axesOptions.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kAxes"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		axesOptions.setBorder(tb);
		JPanel line1 = new JPanel();
		line1.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel line2 = new JPanel();
		line2.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel line3 = new JPanel();
		line3.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mPlotAxes = new JOAJCheckBox(b.getString("kPlotAxes"), true);
		mPlotAxes.addItemListener(this);
		mPlotXGrid = new JOAJCheckBox(b.getString("kXGrid"));
		mPlotYGrid = new JOAJCheckBox(b.getString("kYGrid"));
		line2.add(new JOAJLabel("    "));
		line3.add(new JOAJLabel("    "));
		line1.add(mPlotAxes);
		line2.add(mPlotXGrid);
		line3.add(mPlotYGrid);
		axesOptions.add(line1);
		axesOptions.add(line2);
		axesOptions.add(line3);

		// other options
		JPanel otherOptions = new JPanel();
		otherOptions.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kOther"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		otherOptions.setBorder(tb);

		// plot symbols
		JPanel line4 = new JPanel();
		line4.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		line4.add(new JOAJLabel(b.getString("kSymbol")));
		line4.add(mSymbolPopup);
		mSymbolPopup.addItemListener(this);
		line4.add(new JOAJLabel(b.getString("kSize")));

		SpinnerNumberModel model2 = new SpinnerNumberModel(6, 1, 100, 1);
		mSizeField = new JSpinner(model2);
		mSizeField.addChangeListener(this);
		line4.add(mSizeField);
		otherOptions.add(line4);

		JPanel line5a = new JPanel();
		line5a.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mIncludeObsPanel = new JOAJCheckBox(b.getString("kIncludeBrowser"));
		line5a.add(mIncludeObsPanel);
		otherOptions.add(line5a);

		JPanel line6a = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ignoreMissing = new JOAJCheckBox("Ignore missing observations", true);
		line6a.add(ignoreMissing);
		otherOptions.add(line6a);

		// add the axes and other panels to the gridlayout
		ctrNonAdvOptions.add(axesOptions);
		ctrNonAdvOptions.add(otherOptions);

		// add this panel to the north of the borderlayout
		nonAdvOptions.add("Center", ctrNonAdvOptions);

		JPanel colorNameContPanel = new JPanel();
		colorNameContPanel.setLayout(new BorderLayout(0, 0));

		JPanel colorNamePanel = new JPanel();
		colorNamePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));

		// swatches
		JPanel line7 = new JPanel();
		line7.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		line7.add(new JOAJLabel(b.getString("kBackgroundColor")));
		plotBg = new Swatch(JOAConstants.DEFAULT_CONTENTS_COLOR, 12, 12);
		line7.add(new JOAJLabel(" "));
		line7.add(plotBg);
		JPanel line8 = new JPanel();
		line8.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		line8.add(new JOAJLabel(b.getString("kGridColor")));
		axesColor = new Swatch(Color.black, 12, 12);
		line8.add(new JOAJLabel(" "));
		line8.add(axesColor);
		JPanel line9 = new JPanel();
		line9.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		line9.add(new JOAJLabel(b.getString("kColorSchemes")));
		Vector<String> presetSchemes = new Vector<String>();
		presetSchemes.addElement(b.getString("kDefault"));
		presetSchemes.addElement(b.getString("kWhiteBackground"));
		presetSchemes.addElement(b.getString("kBlackBackground"));
		presetColorSchemes = new JOAJComboBox(presetSchemes);
		presetColorSchemes.setSelectedItem(b.getString("kDefault"));
		presetColorSchemes.addItemListener(this);
		line9.add(presetColorSchemes);

		JPanel swatchCont = new JPanel();
		swatchCont.setLayout(new GridLayout(2, 1, 0, 5));
		swatchCont.add(line7);
		swatchCont.add(line8);
		JPanel swatchContCont = new JPanel();
		swatchContCont.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		swatchContCont.add(swatchCont);
		swatchContCont.add(line9);
		tb = BorderFactory.createTitledBorder(b.getString("kColors"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		swatchContCont.setBorder(tb);

		// window name
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));
		namePanel.add(new JOAJLabel(b.getString("kWindowName")));
		mNameField = new JOAJTextField(20);
		mNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		namePanel.add(mNameField);

		// add the color panel
		colorNameContPanel.add("North", swatchContCont);
		colorNamePanel.add(colorNameContPanel);

		// add the name panel
		colorNamePanel.add(namePanel);

		// add these to the south of the borderlayout
		nonAdvOptions.add("South", colorNamePanel);

		// add all of this to the middle panel
		middlePanel.add(nonAdvOptions);

		// advanced options panel
		mPlotSpec = new LinePlotSpecification();
		mAdvOpt = new AdvLinePlotOptions(mFileViewer, mPlotSpec);
		mAdvOpt.setVisible(true);

		JPanel advPanel = new JPanel();
		advPanel.setLayout(new BorderLayout(0, 0));
		advPanel.add("Center", mAdvOpt);

		// Isopycnals
		JPanel isoPycContPanel = new JPanel();
		isoPycContPanel.setLayout(new BorderLayout(0, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kIsopycnals"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		isoPycContPanel.setBorder(tb);
		mPlotIsopycnals = new JOAJCheckBox(b.getString("kIsopycnals"));
		JPanel line10 = new JPanel();
		line10.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		line10.add(mPlotIsopycnals);
		line10.add(new JOAJLabel(b.getString("kPressure")));
		// mRefPressField = new JOAJTextField("0", 2);
		// mRefPressField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mPressSlider = new JOAJSlider(JOAJSlider.HORIZONTAL, 0, 5000, 0);
		mPressSlider.setPaintTicks(true);
		mPressSlider.setMajorTickSpacing(1000);
		mPressSlider.setMinorTickSpacing(200);
		mPressSlider.setSnapToTicks(true);
		mPressSlider.setPaintLabels(true);
		mPressSlider.setPreferredSize(new Dimension(300, 50));
		// mPressSlider.addChangeListener(this);
		// line10.add(mRefPressField);
		line10.add(mPressSlider);
		isoPycContPanel.add("Center", line10);
		disableIsopycnals();
		advPanel.add("South", isoPycContPanel);

		upperPanel.add("South", middlePanel);
		everyThingPanel.addTab(b.getString("kContourTab1"), upperPanel);
		everyThingPanel.addTab(b.getString("kContourTab2"), advPanel);

		mainPanel.add("Center", new TenPixelBorder(everyThingPanel, 10, 10, 10, 10));

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kPlot"));
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

		mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", mainPanel);
		this.pack();

		runTimer();

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);
		everyThingPanel.setEnabledAt(1, false);
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	public void enableIsopycnals() {
		mPressSlider.setEnabled(true);
		mPlotIsopycnals.setEnabled(true);
		mCanPlotIsoPycnals = true;
	}

	public void disableIsopycnals() {
		mPressSlider.setEnabled(false);
		mPlotIsopycnals.setEnabled(false);
		mPlotIsopycnals.setSelected(false);
		mCanPlotIsoPycnals = false;
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mXParamList.getJList()) {
			// get the x param
			mSelXParam = mXParamList.getJList().getSelectedIndex();
		}
		else if (evt.getSource() == mYParamList.getJList()) {
			mSelYParam = mYParamList.getJList().getSelectedIndex();
		}

		// enable/disable isopycnal button
		String selYParamText = (String)mYParamList.getJList().getSelectedValue();
		Object selXVar = mXParamList.getJList().getSelectedValue();

		boolean enableIsoStuff = false;
		String selXParamText = (String)selXVar;
		if (selYParamText != null
		    && selXParamText != null
		    && ((selYParamText.equalsIgnoreCase("TEMP") || selYParamText.equalsIgnoreCase("THTA")
		        || selYParamText.equalsIgnoreCase("THETA") || selYParamText.equalsIgnoreCase("CTDTMP")) && (selXParamText
		        .equalsIgnoreCase("CTDS")
		        || selXParamText.equalsIgnoreCase("SALT") || selXParamText.equalsIgnoreCase("CTDSAL") || selXParamText
		        .equalsIgnoreCase("SALNTY")))) {
			enableIsoStuff = true;
			mSaltAxis = mXParamList.getJList().getSelectedIndex();
		}

		if (enableIsoStuff) {
			enableIsopycnals();
		}
		else {
			disableIsopycnals();
		}
		generatePlotName();

		if (mSelXParam >= 0 && mSelYParam >= 0) {
			UVCoordinate retVal = setAdvancedValues();
			int xerr = (int)retVal.u;
			int yerr = (int)retVal.v;
			if (xerr == -1 && yerr == -1) {
				everyThingPanel.setEnabledAt(1, true);
			}
			else {
				if (xerr >= 0 && yerr == -1) {
					// disable the x param
					JFrame f = new JFrame("Parameter Values Missing Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "All values for " + mFileViewer.mAllProperties[xerr].getVarLabel()
					    + " are missing. " + "\n"
					    + "Select a new X parameter (or remove this parameter from a multiple selection)");
					mXParamList.clearSelection();
				}
				else if (xerr == -1 && yerr >= 0) {
					// disable the y param
					JFrame f = new JFrame("Parameter Values Missing Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "All values for " + selYParamText + " are missing. " + "\n"
					    + "Select a new Y parameter");
					mYParamList.clearSelection();
					mSelYParam = 0;
				}
				else if (xerr >= 0 && yerr >= 0) {
					// disable the y param
					mXParamList.clearSelection();
					mYParamList.clearSelection();
					JFrame f = new JFrame("Parameter Values Missing Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "All values for " + selYParamText + " are missing and \n"
					    + "all values for " + mFileViewer.mAllProperties[xerr].getVarLabel() + " are missing. \n"
					    + "Select new Y and X parameters");
					mSelYParam = 0;
				}
				disableIsopycnals();
				generatePlotName();
			}
		}
		else {
			everyThingPanel.setEnabledAt(1, false);
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJCheckBox) {
			JOAJCheckBox cb = (JOAJCheckBox)evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == mPlotAxes) {
				enableGrid();
			}
			else if (cb == mPlotAxes) {
				disableGrid();
			}
		}
		else if (evt.getSource() instanceof JOAJComboBox) {
			JOAJComboBox cb = (JOAJComboBox)evt.getSource();
			if (cb == presetColorSchemes) {
				int colorScheme = cb.getSelectedIndex();
				if (colorScheme == 0) {
					// default bg
					plotBg.setColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
					axesColor.setColor(Color.black);
				}
				else if (colorScheme == 1) {
					// white bg
					plotBg.setColor(Color.white);
					axesColor.setColor(Color.black);
				}
				else {
					// color bg
					plotBg.setColor(Color.black);
					axesColor.setColor(Color.white);
				}
			}
			else if (cb == mSymbolPopup) {
				mCurrSymbol = cb.getSelectedIndex() + 1;
			}
			if (cb == mColorCombo) {
				mCurrPalette = (String)mColorPaletteList.elementAt(cb.getSelectedIndex());
			}
		}
	}

	public void enableGrid() {
		mPlotXGrid.setEnabled(true);
		mPlotYGrid.setEnabled(true);
	}

	public void disableGrid() {
		mPlotXGrid.setEnabled(false);
		mPlotYGrid.setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			LinePlotSpecification spec = createPlotSpec();
			try {
				spec.writeToLog("New XY Plot (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}

			if (spec.isAccumulateStns()) {
				// set the skip flag for all stns
				for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
					OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

					for (int sec = 0; sec < of.mNumSections; sec++) {
						Section sech = (Section)of.mSections.elementAt(sec);

						for (int stc = 0; stc < sech.mStations.size(); stc++) {
							Station sh = (Station)sech.mStations.elementAt(stc);
							sh.setSkipStn(true);
						}
					}
				}
			}

			JOALinePlotWindow plotWind = new JOALinePlotWindow(spec, mFileViewer);
			plotWind.pack();
			plotWind.setVisible(true);
			mFileViewer.addOpenWindow(plotWind);
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("options")) {
			;
		}
	}

	public UVCoordinate setAdvancedValues() {
		double xMin;
		double xMax;
		double xInc;
		double tempXMin;
		double tempXMax;
		int xtics = 1;
		String xName;

		Object selXVar = mXParamList.getJList().getSelectedValue();

		xName = (String)selXVar;

		// get pretty ranges for the current parameters
		int xerrLine = -1;
		tempXMin = mFileViewer.mAllProperties[mSelXParam].getPlotMin();
		tempXMax = mFileViewer.mAllProperties[mSelXParam].getPlotMax();
		Triplet newRange = JOAFormulas.GetPrettyRange(tempXMin, tempXMax);
		xMin = newRange.getVal1();
		xMax = newRange.getVal2();
		xInc = newRange.getVal3();
		if (Double.isNaN(xInc)) {
			xerrLine = mSelXParam;
		}

		int yerrLine = -1;
		double tempYMin = mFileViewer.mAllProperties[mSelYParam].getPlotMin();
		double tempYMax = mFileViewer.mAllProperties[mSelYParam].getPlotMax();
		newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
		double yMin = newRange.getVal1();
		double yMax = newRange.getVal2();
		double yInc = newRange.getVal3();
		if (Double.isNaN(yInc)) {
			yerrLine = mSelYParam;
		}

		if (xerrLine >= 0 || yerrLine >= 0) {
			// parameter is completely missing
			return new UVCoordinate((double)xerrLine, (double)yerrLine);
		}

		mAdvOpt.setParameters(mSelXParam, xName, mSelYParam, (String)mYParamList.getJList().getSelectedValue());
		mAdvOpt.setValues(xMin, xMax, xInc, yMin, yMax, yInc, xtics, 1, tempXMin, tempXMax, tempYMin, tempYMax);
		return new UVCoordinate((double)xerrLine, (double)yerrLine);
	}

	public void maintainButtons() {
		if (mSelXParam >= 0 && mSelYParam >= 0) {
			mOKBtn.setEnabled(true);
		}
		else {
			mOKBtn.setEnabled(false);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println("got propertyChanged event");
	}

	public void generatePlotName() {
		Object selXVar = mXParamList.getJList().getSelectedValue();

		String xVarText;
		xVarText = (String)mXParamList.getJList().getSelectedValue();
		if (xVarText == null || xVarText.length() == 0) {
			xVarText = "?";
		}

		String yVarText = (String)mYParamList.getJList().getSelectedValue();
		if (yVarText == null || yVarText.length() == 0) {
			yVarText = "?";
		}

		String nameString = new String(yVarText + "-" + xVarText + " (" + mFileViewer.mFileViewerName + ")");
		mNameField.setText(nameString);
	}

	public void stateChanged(ChangeEvent e) {
	}
}

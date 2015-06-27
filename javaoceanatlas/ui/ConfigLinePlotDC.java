/*
 * $Id: ConfigurePvPPlotDC.java,v 1.5 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;

@SuppressWarnings("serial")
public class ConfigLinePlotDC extends JOAJDialog implements ActionListener, /*
																																						 * ButtonMaintainer,
																																						 */ItemListener, ChangeListener {
	protected FileViewer mFileViewer;
	protected Component mComp;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mApplyButton = null;
	protected JOAJButton mCancelButton = null;
	protected JSpinner mSizeField = null;
	protected boolean mIncludeObsPanel = false;
	protected boolean mOriginalIncludeObsPanel = false;
	protected JOAJCheckBox includeObsPanel = null;
	protected JOAJCheckBox connectObservations = null;
	protected JOAJCheckBox ignoreMissing = null;
	protected JOAJCheckBox mPlotAxes = null;
	protected JOAJCheckBox mPlotXGrid = null;
	protected JOAJCheckBox mPlotYGrid = null;
	protected AdvLinePlotOptions mAdvOpt = null;
	protected LinePlotSpecification mPlotSpec = null;
	protected LinePlotSpecification mOriginalPlotSpec = null;
	protected JOAJComboBox mSymbolPopup = null;
	protected int mCurrSymbol = JOAConstants.SYMBOL_SQUAREFILLED;
	protected Icon[] symbolData = null;
	protected DialogClient mClient = null;
	protected int mSelXParam;
	protected int mSelYParam = -1;
	protected SimpleFGBGColorPicker mColorPicker = null;
	protected boolean mLayoutChanged = false;
	protected JOAJSlider mPressSlider = null;
	protected JOAJCheckBox mPlotIsopycnals = null;
	protected JDialog mFrame = null;
	// MaintenanceTimer mMaintain = null;
	protected JOAJRadioButton mPlotOnlyCurrStn = null;
	protected JOAJRadioButton mPlotAllStns = null;
	protected JOAJRadioButton mAccumulateStns = null;
	protected Vector<String> mColorPaletteList = null;
	protected String mCurrPalette = null;
	protected JOAJComboBox mColorCombo = null;
	protected JSpinner mLineWidth = null;

	public ConfigLinePlotDC(JOAWindow par, FileViewer fv, DialogClient client, LinePlotSpecification spec) {
		super(par, "Line Plot", false);
		mFileViewer = fv;
		mPlotSpec = new LinePlotSpecification(spec);
		mOriginalPlotSpec = new LinePlotSpecification(mPlotSpec);
		mClient = client;
		mSelXParam = mPlotSpec.getXVarCode();
		mSelYParam = mPlotSpec.getYVarCode();
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

		mSymbolPopup = new JOAJComboBox();
		for (int i = 0; i < symbolData.length; i++)
			mSymbolPopup.addItem(symbolData[i]);
		mCurrSymbol = mPlotSpec.getSymbol();
		mSymbolPopup.setSelectedIndex(mCurrSymbol - 1);

		JOAJTabbedPane everyThingPanel = new JOAJTabbedPane();
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 0));
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout(5, 0));

		// Options
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 1));

		JPanel midStuff = new JPanel();
		midStuff.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));

		JPanel p1 = new JPanel();
		p1.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.LEFT, 0));
		boolean allState = !(mPlotSpec.isPlotOnlyCurrStn() || mPlotSpec.isAccumulateStns());
		mPlotAllStns = new JOAJRadioButton(b.getString("kPlotAllStns"), allState);
		mPlotOnlyCurrStn = new JOAJRadioButton(b.getString("kPlotOlnyCurrStn"), mPlotSpec.isPlotOnlyCurrStn());
		mAccumulateStns = new JOAJRadioButton(b.getString("kAccumulateStns"), mPlotSpec.isAccumulateStns());
		p1.add(mPlotAllStns);
		p1.add(mPlotOnlyCurrStn);
		p1.add(mAccumulateStns);
		midStuff.add(p1);

		ButtonGroup bg0 = new ButtonGroup();
		bg0.add(mPlotAllStns);
		bg0.add(mPlotOnlyCurrStn);
		bg0.add(mAccumulateStns);

		mColorPaletteList = JOAFormulas.getColorPalList();
		mCurrPalette = mPlotSpec.getStnCycleColorPalette();
		mColorCombo = new JOAJComboBox(mColorPaletteList);
		mColorCombo.setSelectedItem(mCurrPalette);
		mColorCombo.addItemListener(this);

		JPanel p2 = new JPanel();
		p2.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.LEFT, 0));
		JPanel stnColor = new JPanel(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));
		stnColor.add(new JLabel("Cycle station colors using:"));
		stnColor.add(mColorCombo);
		p2.add(stnColor);
		
		JPanel lwPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		lwPanel.add(new JLabel("Line width:"));
		SpinnerNumberModel model3 = new SpinnerNumberModel(mPlotSpec.getLineWidth(), 1, 15, 1);
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
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kAxes"));
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
		mPlotAxes = new JOAJCheckBox(b.getString("kPlotAxes"), mPlotSpec.isPlotAxes());
		mPlotAxes.addItemListener(this);
		mPlotXGrid = new JOAJCheckBox(b.getString("kXGrid"), mPlotSpec.isXGrid());
		mPlotYGrid = new JOAJCheckBox(b.getString("kYGrid"), mPlotSpec.isYGrid());
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

		SpinnerNumberModel model = new SpinnerNumberModel(mPlotSpec.getSymbolSize(), 1, 100, 1);
		mSizeField = new JSpinner(model);
		mSizeField.addChangeListener(this);
		line4.add(mSizeField);
		otherOptions.add(line4);

		// include NewColorBar
		JPanel line5a = new JPanel();
		line5a.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		includeObsPanel = new JOAJCheckBox(b.getString("kIncludeBrowser"), mPlotSpec.isIncludeObsPanel());
		line5a.add(includeObsPanel);
		mOriginalIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
		mIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
		includeObsPanel.addItemListener(this);
		otherOptions.add(line5a);

		JPanel line6a = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ignoreMissing = new JOAJCheckBox("Ignore missing observations", mPlotSpec.isIgnoreMissingObs());
		line6a.add(ignoreMissing);
		otherOptions.add(line6a);

		// add the axes and other panels to the gridlayout
		ctrNonAdvOptions.add(axesOptions);
		ctrNonAdvOptions.add(otherOptions);

		// add this panel to the north of the borderlayout
		nonAdvOptions.add("Center", ctrNonAdvOptions);

		// add a color picker to the south of the borderlayout
		mColorPicker = new SimpleFGBGColorPicker(mPlotSpec.getBGColor(), mPlotSpec.getFGColor());
		nonAdvOptions.add("South", mColorPicker);

		// add all of this to the middle panel
		middlePanel.add(nonAdvOptions);

		// advanced options panel
		JPanel advOptionsCont = new JPanel();
		advOptionsCont.setLayout(new BorderLayout(5, 0));
		mAdvOpt = new AdvLinePlotOptions(mFileViewer, mPlotSpec);
		setAdvancedValues();
		advOptionsCont.add("North", mAdvOpt);

		// Isopycnals
		JPanel isoPycContPanel = new JPanel();
		isoPycContPanel.setLayout(new BorderLayout(0, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kIsopycnals"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		isoPycContPanel.setBorder(tb);
		mPlotIsopycnals = new JOAJCheckBox(b.getString("kIsopycnals"), mPlotSpec.isPlotIsopycnals());
		mPlotIsopycnals.addItemListener(this);
		JPanel line10 = new JPanel();
		line10.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		line10.add(mPlotIsopycnals);
		line10.add(new JOAJLabel(b.getString("kPressure")));
		// mRefPressField = new JOAJTextField("0", 2);
		// mRefPressField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mPressSlider = new JOAJSlider(JOAJSlider.HORIZONTAL, 0, 5000, (int)mPlotSpec.getRefPress());
		mPressSlider.setPaintTicks(true);
		mPressSlider.setMajorTickSpacing(1000);
		mPressSlider.setMinorTickSpacing(200);
		mPressSlider.setSnapToTicks(true);
		mPressSlider.setPaintLabels(true);
		mPressSlider.addChangeListener(this);
		mPressSlider.setPreferredSize(new Dimension(300, 50));
		// line10.add(mRefPressField);
		line10.add(mPressSlider);
		isoPycContPanel.add("Center", line10);
		if (mPlotSpec.isCanPlotIsoPycnals())
			enableIsopycnals();
		else
			disableIsopycnals();
		advOptionsCont.add("South", isoPycContPanel);

		upperPanel.add("North", middlePanel);

		everyThingPanel.addTab(b.getString("kContourTab1"), upperPanel);
		everyThingPanel.addTab(b.getString("kContourTab2"), advOptionsCont);
		mainPanel.add("Center", new TenPixelBorder(everyThingPanel, 10, 10, 10, 10));

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		mApplyButton = new JOAJButton(b.getString("kApply"));
		mApplyButton.setActionCommand("apply");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));

		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mApplyButton);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mApplyButton);
			dlgBtnsPanel.add(mCancelButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mOKBtn.addActionListener(this);
		mApplyButton.addActionListener(this);
		mCancelButton.addActionListener(this);

		mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", mainPanel);

		// mMaintain = new MaintenanceTimer(this, 100);
		// mMaintain.startMaintainer();
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJCheckBox) {
			JOAJCheckBox cb = (JOAJCheckBox)evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == includeObsPanel) {
				mIncludeObsPanel = true;
			}
			else if (cb == includeObsPanel) {
				mIncludeObsPanel = false;
			}

			if (evt.getStateChange() == ItemEvent.SELECTED && cb == mPlotAxes) {
				enableGrid();
			}
			else if (cb == mPlotAxes) {
				disableGrid();
			}
		}
		else if (evt.getSource() instanceof JOAJComboBox) {
			JOAJComboBox cb = (JOAJComboBox)evt.getSource();
			if (cb == mSymbolPopup) {
				mCurrSymbol = cb.getSelectedIndex() + 1;
			}
			if (cb == mColorCombo) {
				mCurrPalette = (String)mColorPaletteList.elementAt(cb.getSelectedIndex());
			}
		}
	}

	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() == mSizeField)
			mClient.dialogApply(this);
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
			// mClient.dialogCancelled(this);
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			mClient.dialogDismissed(this);
			this.dispose();
		}
		else if (cmd.equals("apply")) {
			mClient.dialogApply(this);

			mOriginalPlotSpec = null;
			mOriginalPlotSpec = createPlotSpec();
			mLayoutChanged = false;
		}
	}

	public void setAdvancedValues() {
		double tempXMin;
		double tempXMax;
		String xNames;

		tempXMin = mFileViewer.mAllProperties[mSelXParam].getPlotMin();
		tempXMax = mFileViewer.mAllProperties[mSelXParam].getPlotMax();
		xNames = new String(mFileViewer.mAllProperties[mSelXParam].getVarLabel());

		mAdvOpt.setParameters(mSelXParam, xNames, mSelYParam, mFileViewer.mAllProperties[mSelYParam].getVarLabel());

		double tempYMin = mFileViewer.mAllProperties[mSelYParam].getPlotMin();
		double tempYMax = mFileViewer.mAllProperties[mSelYParam].getPlotMax();

		mAdvOpt.setValues(mPlotSpec.getXMinValue(), mPlotSpec.getXMaxValue(), mPlotSpec.getXInc(), mPlotSpec
		    .getWinYPlotMin(), mPlotSpec.getWinYPlotMax(), mPlotSpec.getYInc(), mPlotSpec.getXTics(), mPlotSpec.getYTics(),
		    tempXMin, tempXMax, tempYMin, tempYMax);
	}

	public LinePlotSpecification getOrigPlotSpec() {
		return mOriginalPlotSpec;
	}

	public LinePlotSpecification createPlotSpec() {
		LinePlotSpecification ps = new LinePlotSpecification();
		// get the colors
		ps.setFGColor(mColorPicker.getFGColor());
		ps.setBGColor(mColorPicker.getBGColor());
		// if (mIncludeColorBar && !mIncludeColorBar) {
		// ps.mWidth(mPlotSpec.mWidth + 100;
		// }

		ps.setFileViewer(mFileViewer);

		ps.setYVarCode(mSelYParam);
		ps.setWinTitle(mPlotSpec.getWinTitle());
		ps.setIncludeObsPanel(includeObsPanel.isSelected());
		ps.setIgnoreMissingObs(ignoreMissing.isSelected());
		ps.setXGrid(mPlotXGrid.isSelected());
		ps.setYGrid(mPlotYGrid.isSelected());
		ps.setPlotAxes(mPlotAxes.isSelected());
		ps.setSaltAxis(mPlotSpec.isSaltAxis());

		mAdvOpt.upDateSettings();
		ps.setXVarCode(mSelXParam);
		ps.setWinXPlotMin(mAdvOpt.getXMin());
		ps.setWinXPlotMax(mAdvOpt.getXMax());
		ps.setXInc(mAdvOpt.getXInc());
		ps.setXTics(mAdvOpt.getXTics());

		ps.setWinYPlotMin(mAdvOpt.getYMin());
		ps.setWinYPlotMax(mAdvOpt.getYMax());
		ps.setYInc(mAdvOpt.getYInc());
		ps.setYTics(mAdvOpt.getYTics());
		ps.setRefPress(mPressSlider.getValue());
		ps.setPlotIsopycnals(mPlotIsopycnals.isSelected());
		ps.setCanPlotIsoPycnals(mPlotSpec.isCanPlotIsoPycnals());
		ps.setPlotOnlyCurrStn(mPlotOnlyCurrStn.isSelected());
		ps.setAccumulateStns(mAccumulateStns.isSelected());
		ps.setOverrideLabel(mPlotSpec.getOverrideLabel());
		ps.setStnCycleColorPalette(mCurrPalette);
		ps.setSymbol(mCurrSymbol);
		ps.setSymbolSize(((Integer)mSizeField.getValue()).intValue());
		return ps;
	}

	public void enableIsopycnals() {
		mPressSlider.setEnabled(true);
		mPlotIsopycnals.setEnabled(true);
	}

	public void disableIsopycnals() {
		mPressSlider.setEnabled(false);
		mPlotIsopycnals.setEnabled(false);

	}

	public boolean removingObsBrowser() {
		return mOriginalIncludeObsPanel && !mIncludeObsPanel;
	}

	public boolean addingObsBrowser() {
		return !mOriginalIncludeObsPanel && mIncludeObsPanel;
	}

	public void addedObsBrowser() {
		mOriginalIncludeObsPanel = true;
		mIncludeObsPanel = true;
		mLayoutChanged = true;
	}

	public void removedObsBrowser() {
		mOriginalIncludeObsPanel = false;
		mIncludeObsPanel = false;
		mLayoutChanged = true;
	}

	private int getSymbolSize() {
		return (((Integer)mSizeField.getValue()).intValue());
	}
}

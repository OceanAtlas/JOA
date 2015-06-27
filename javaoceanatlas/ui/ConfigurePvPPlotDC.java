/*
 * $Id: ConfigurePvPPlotDC.java,v 1.5 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.border.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;

@SuppressWarnings("serial")
public class ConfigurePvPPlotDC extends JOAJDialog implements ActionListener, /* ButtonMaintainer, */ItemListener,
    ChangeListener {
	protected FileViewer mFileViewer;
	protected Component mComp;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mApplyButton = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJButton mExportButton = null;
	protected JSpinner mSizeField = null;
	protected boolean mIncludeColorBar = false;
	protected boolean mIncludeObsPanel = false;
	protected boolean mOriginalIncludeColorBar = false;
	protected boolean mOriginalIncludeObsPanel = false;
	protected JOAJCheckBox includeColorBar = null;
	protected JOAJCheckBox includeObsPanel = null;
	protected JOAJCheckBox connectObservations = null;
  protected JOAJCheckBox ignoreMissing = null;
	protected JOAJCheckBox mPlotAxes = null;
	protected JOAJCheckBox mPlotXGrid = null;
	protected JOAJCheckBox mPlotYGrid = null;
	protected AdvXYPlotOptions mAdvOpt = null;
	protected XYPlotSpecification mPlotSpec = null;
	protected XYPlotSpecification mOriginalPlotSpec = null;
	protected JOAJComboBox mSymbolPopup = null;
	protected int mCurrSymbol = JOAConstants.SYMBOL_SQUAREFILLED;
	protected Icon[] symbolData = null;
	protected DialogClient mClient = null;
	protected int[] mSelXParam = new int[10];
	protected int mSelYParam = -1;
	protected SimpleFGBGColorPicker mColorPicker = null;
	protected boolean mRemovingColorLegend = false;
	protected boolean mAddingColorLegend = false;
	protected boolean mLayoutChanged = false;
	protected JOAJSlider mPressSlider = null;
	protected JOAJCheckBox mPlotIsopycnals = null;
	protected JDialog mFrame = null;
	// MaintenanceTimer mMaintain = null;
  protected JOAJRadioButton mPlotOnlyCurrStn = null;
  protected JOAJRadioButton mPlotAllStns = null;
  protected JOAJRadioButton mAccumulateStns = null;
	protected JOAJRadioButton mColorByCBParam = null;
	protected JOAJRadioButton mColorByCLC = null;
	protected JOAJRadioButton mColorByNothing = null;

	public ConfigurePvPPlotDC(JOAWindow par, FileViewer fv, DialogClient client, XYPlotSpecification spec) {
		super(par, "Property-Property Plot", false);
		mFileViewer = fv;
		mPlotSpec = new XYPlotSpecification(spec);
		mOriginalPlotSpec = new XYPlotSpecification(mPlotSpec);
		mClient = client;
		for (int i = 0; i < mPlotSpec.getNumXAxes(); i++)
			mSelXParam[i] = mPlotSpec.getXVarCode(i);
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
		mCurrSymbol = mPlotSpec.getSymbol(0);
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

		JPanel p1s = new JPanel();
		p1s.setLayout(new GridLayout(3, 1, 0, 0));
		mColorByCBParam = new JOAJRadioButton(b.getString("kPlotUsingCB"), mPlotSpec.isColorByCBParam());
		mColorByCLC = new JOAJRadioButton(b.getString("kPlotUsingCLC"), mPlotSpec.isColorByConnectLineColor());
		mColorByNothing = new JOAJRadioButton(b.getString("kPlotUsingNothing"), !mPlotSpec.isColorByCBParam()
		    && !mPlotSpec.isColorByConnectLineColor());
		ButtonGroup bg = new ButtonGroup();
		bg.add(mColorByCBParam);
		bg.add(mColorByCLC);
		bg.add(mColorByNothing);
		p1s.add(mColorByCBParam);
		p1s.add(mColorByCLC);
		p1s.add(mColorByNothing);
    midStuff.add(p1s);
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

		SpinnerNumberModel model = new SpinnerNumberModel(mPlotSpec.getSymbolSize(0), 1, 100, 1);
		mSizeField = new JSpinner(model);

		mSizeField.addChangeListener(this);
		line4.add(mSizeField);
		otherOptions.add(line4);

		// include NewColorBar
		JPanel line5 = new JPanel();
		line5.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		includeColorBar = new JOAJCheckBox(b.getString("kColorLegend"), mPlotSpec.isIncludeCBAR());
		mIncludeColorBar = mPlotSpec.isIncludeCBAR();
		mOriginalIncludeColorBar = mPlotSpec.isIncludeCBAR();
		includeColorBar.addItemListener(this);
		line5.add(includeColorBar);
		otherOptions.add(line5);

		// include NewColorBar
		JPanel line5a = new JPanel();
		line5a.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		includeObsPanel = new JOAJCheckBox(b.getString("kIncludeBrowser"), mPlotSpec.isIncludeObsPanel());
		line5a.add(includeObsPanel);
		mOriginalIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
		mIncludeObsPanel = mPlotSpec.isIncludeObsPanel();
		includeObsPanel.addItemListener(this);
		otherOptions.add(line5a);

		// connect observations
		JPanel line6 = new JPanel();
		line6.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		connectObservations = new JOAJCheckBox(b.getString("kConnectObservations"), mPlotSpec.isConnectObs());
		line6.add(connectObservations);
		otherOptions.add(line6);
    
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
		mAdvOpt = new AdvXYPlotOptions(mFileViewer, mPlotSpec);
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
		mPressSlider = new JOAJSlider(JOAJSlider.HORIZONTAL, 0, 5000, (int) mPlotSpec.getRefPress());
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
		mExportButton = new JOAJButton("Export Spec...");
		mExportButton.setActionCommand("export");
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
//			dlgBtnsPanel.add(mExportButton);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
//		dlgBtnsPanel.add(mExportButton);
			dlgBtnsPanel.add(mApplyButton);
			dlgBtnsPanel.add(mCancelButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mOKBtn.addActionListener(this);
		mApplyButton.addActionListener(this);
		mCancelButton.addActionListener(this);
		mExportButton.addActionListener(this);

		mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", mainPanel);

		// mMaintain = new MaintenanceTimer(this, 100);
		// mMaintain.startMaintainer();
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJCheckBox) {
			JOAJCheckBox cb = (JOAJCheckBox) evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == includeColorBar) {
				mIncludeColorBar = true;
			}
			else if (cb == includeColorBar) {
				mIncludeColorBar = false;
			}

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
			JOAJComboBox cb = (JOAJComboBox) evt.getSource();
			if (cb == mSymbolPopup) {
				mCurrSymbol = cb.getSelectedIndex() + 1;
				mAdvOpt.setSymbol(mCurrSymbol - 1);
				// mAdvOpt.setSymbolSize(0, int ss);
			}
		}
	}

	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() == mSizeField)
			mAdvOpt.setSymbolSize(getSymbolSize());
		else
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
			mAdvOpt.closeMe();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			mClient.dialogDismissed(this);
			mAdvOpt.closeMe();
			this.dispose();
		}
		else if (cmd.equals("apply")) {
			mClient.dialogApply(this);

			mOriginalPlotSpec = null;
			mOriginalPlotSpec = createPlotSpec();
			mOriginalIncludeColorBar = includeColorBar.isSelected();
			mRemovingColorLegend = false;
			mAddingColorLegend = false;
			mLayoutChanged = false;
		}
		else if (cmd.equals("export")) {
    	Frame fr = new Frame();
    	String directory = System.getProperty("user.dir");
    	FileDialog f = new FileDialog(fr, "Export JSON data as:", FileDialog.SAVE);
    	f.setDirectory(directory);
    	if (mFileViewer.mCurrOutFileName != null) {
    		f.setFile(mFileViewer.mCurrOutFileName + "_JOAXYSpec.jsn");
    	}
    	else {
    		f.setFile(mFileViewer.getTitle() + "_JOAXYSpec.jsn");
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
    		mOriginalPlotSpec.exportJSON(outFile);

    		try {
    			JOAConstants.LogFileStream.writeBytes("Exported spreadsheet file: " + outFile.getCanonicalPath() + "\n");
    			JOAConstants.LogFileStream.flush();
    		}
    		catch (Exception ex) {}
    	}
		}	
	}

	public void setAdvancedValues() {
		double[] tempXMin = new double[10];
		double[] tempXMax = new double[10];
		String[] xNames = new String[10];

		for (int i = 0; i < mPlotSpec.getNumXAxes(); i++) {
			tempXMin[i] = mFileViewer.mAllProperties[mSelXParam[i]].getPlotMin();
			tempXMax[i] = mFileViewer.mAllProperties[mSelXParam[i]].getPlotMax();
			xNames[i] = new String(mFileViewer.mAllProperties[mSelXParam[i]].getVarLabel());
		}

		mAdvOpt.setParameters(mPlotSpec.getNumXAxes(), mSelXParam, xNames, mSelYParam,
		    mFileViewer.mAllProperties[mSelYParam].getVarLabel());

		double tempYMin = mFileViewer.mAllProperties[mSelYParam].getPlotMin();
		double tempYMax = mFileViewer.mAllProperties[mSelYParam].getPlotMax();

		mAdvOpt.setValues(mPlotSpec.getNumXAxes(), mPlotSpec.getXMinValues(), mPlotSpec.getXMaxValues(), mPlotSpec
		    .getXIncs(), mPlotSpec.getWinYPlotMin(), mPlotSpec.getWinYPlotMax(), mPlotSpec.getYInc(), mPlotSpec.getXTics(),
		    mPlotSpec.getYTics(), tempXMin, tempXMax, tempYMin, tempYMax, mPlotSpec.getConnectStnColors(), mPlotSpec
		        .getSymbols(), mPlotSpec.getSymbolSizes());
	}

	public XYPlotSpecification getOrigPlotSpec() {
		return mOriginalPlotSpec;
	}

	public XYPlotSpecification createPlotSpec() {
		XYPlotSpecification ps = new XYPlotSpecification();
		// get the colors
		ps.setFGColor(mColorPicker.getFGColor());
		ps.setBGColor(mColorPicker.getBGColor());
		// if (mIncludeColorBar && !mIncludeColorBar) {
		// ps.mWidth(mPlotSpec.mWidth + 100;
		// }

		ps.setFileViewer(mFileViewer);
		
		ps.setYVarCode(mSelYParam);
		ps.setWinTitle(mPlotSpec.getWinTitle());
		ps.setIncludeCBAR(mIncludeColorBar);
		ps.setIncludeObsPanel(includeObsPanel.isSelected());
    ps.setIgnoreMissingObs(ignoreMissing.isSelected());
		ps.setConnectObs(connectObservations.isSelected());
		ps.setXGrid(mPlotXGrid.isSelected());
		ps.setYGrid(mPlotYGrid.isSelected());
		ps.setPlotAxes(mPlotAxes.isSelected());
		ps.setSaltAxis(mPlotSpec.isSaltAxis());

		mAdvOpt.upDateCurrSettings();
		ps.setNumXAxes(mPlotSpec.getNumXAxes());
		for (int i = 0; i < ps.getNumXAxes(); i++) {
			ps.setXVarCode(i, mSelXParam[i]);
			ps.setWinXPlotMin(i, mAdvOpt.getXMin(i));
			ps.setWinXPlotMax(i, mAdvOpt.getXMax(i));
			ps.setXInc(i, mAdvOpt.getXInc(i));
			ps.setXTics(i, mAdvOpt.getXTics(i));
			ps.setSymbol(i, mAdvOpt.getSymbol(i));
			try {
				ps.setSymbolSize(i, mAdvOpt.getSymbolSize(i));
			}
			catch (NumberFormatException ex) {
				ps.setSymbolSize(i, 4);
			}
			ps.setConnectStnColors(i, mAdvOpt.getColor(i));
		}
		ps.setXVarCodes(mAdvOpt.getXAxesOrder());
		ps.setWinYPlotMin(mAdvOpt.getYMin());
		ps.setWinYPlotMax(mAdvOpt.getYMax());
		ps.setYInc(mAdvOpt.getYInc());
		ps.setYTics(mAdvOpt.getYTics());
		ps.setRefPress(mPressSlider.getValue());
		ps.setPlotIsopycnals(mPlotIsopycnals.isSelected());
		ps.setCanPlotIsoPycnals(mPlotSpec.isCanPlotIsoPycnals());
		ps.setPlotOnlyCurrStn(mPlotOnlyCurrStn.isSelected());
    ps.setAccumulateStns(mAccumulateStns.isSelected());  
		ps.setColorByCBParam(mColorByCBParam.isSelected());
		ps.setColorByConnectLineColor(mColorByCLC.isSelected());
		
		ps.setFilteredOutColor(mPlotSpec.getFilteredOutColor());
		ps.setTimeFilters(mPlotSpec.getTimeFilters());
		ps.setTSModelTerms(mPlotSpec.getTSModelTerms());
		ps.setZFilter(mPlotSpec.getZFilter());
		ps.setTFilter(mPlotSpec.getTFilter());
		ps.setOverrideLabel(mPlotSpec.getOverrideLabel());
		ps.setModelIntercept(mPlotSpec.getModelIntercept());
		ps.setTMinOfModel(mPlotSpec.getTMinOfModel());
		ps.setTMaxOfModel(mPlotSpec.getTMaxOfModel());
		ps.setModelErrTerms(mPlotSpec.getModelErrTerms());
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

	public boolean removingColorBar() {
		return mOriginalIncludeColorBar && !mIncludeColorBar;
	}

	public boolean addingColorBar() {
		return !mOriginalIncludeColorBar && mIncludeColorBar;
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

	public void addedColorLegend() {
		mOriginalIncludeColorBar = true;
		mAddingColorLegend = true;
		mLayoutChanged = true;
	}

	public void removedColorLegend() {
		mOriginalIncludeColorBar = false;
		mRemovingColorLegend = false;
		mLayoutChanged = true;
	}

	private int getSymbolSize() {
		return (((Integer) mSizeField.getValue()).intValue());
	}
}

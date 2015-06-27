/*
 * $Id: ConfigureStationValuePlot.java,v 1.6 2005/09/07 18:49:30 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import gov.noaa.pmel.swing.*;
import gov.noaa.pmel.util.GeoDate;

@SuppressWarnings("serial")
public class ConfigureStationValuePlot extends JOAJDialog implements ListSelectionListener, ActionListener,
    ButtonMaintainer, ItemListener {
  ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
  protected FileViewer mFileViewer;
  protected String mTitle;
  protected Component mComp;
  protected StnParameterChooser mYParamList;
  protected int mSelYParam = -1;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mNameField = null;
  protected JOAJRadioButton b1 = null;
  protected JOAJRadioButton b2 = null;
  protected JOAJRadioButton b3 = null;
  protected int mOffset = JOAConstants.PROFDISTANCE;
  protected Swatch plotBg = null;
  protected Swatch axesColor = null;
	protected Swatch mLineColorSwatch = null;
	protected Swatch mSymbolColorSwatch = null;
  protected JOAJComboBox presetColorSchemes = null;
  protected JOAJCheckBox mReverseY = null;
  protected JOAJCheckBox mPlotYGrid = null;
  protected JOAJCheckBox mPlotXGrid = null;
  protected JOAJComboBox mSymbolPopup = null;
  protected int mCurrSymbol = JOAConstants.SYMBOL_CROSS1;
  protected Icon[] symbolData = null;
  protected JSpinner mSizeField = null;
  protected JOAJLabel mEnableSymbols = null;
  protected JOAJLabel mSizeLabel = null;
	private Timer timer = new Timer();
  protected JOAJTextField yMin, yMax, yInc;
  protected JSpinner yTics, xTics;
  protected JOAJTextField xMin, xMax, xInc;
  protected JPanel plotScaleCont = null;
  protected int pPos = -1;
  protected int yTicsVal = 1;
  protected int xTicsVal = 1;
  private JOAJLabel mMinXLabel = new JOAJLabel(b.getString("kMinimum"));
  private JOAJLabel mMaxXLabel = new JOAJLabel(b.getString("kMaximum"));
  private JOAJLabel mIncLabel = new JOAJLabel(b.getString("kIncrement"));
  private JOAJLabel mMinXTLabel = new JOAJLabel(b.getString("kMinimum"));
  private JOAJLabel mMaxXTLabel = new JOAJLabel(b.getString("kMaximum"));
  private JOAJLabel mXIncLabel = new JOAJLabel(b.getString("kNoMinorTicks"));
  private JSpinner mStartSpinner;
  private JSpinner mEndSpinner;
  private JCheckBox mConnectObs;

  public ConfigureStationValuePlot(JFrame par, FileViewer fv) {
    super(par, "Station Value Plot", false);
    mFileViewer = fv;
    this.init();
  }

  public void init() {
    symbolData = new Icon[] {
    		new ImageIcon(getClass().getResource("images/sym_square.gif")),
        new ImageIcon(getClass().getResource("images/sym_squarefilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_circle.gif")),
        new ImageIcon(getClass().getResource("images/sym_circlefilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_diamond.gif")),
        new ImageIcon(getClass().getResource("images/sym_diamondfilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_triangle.gif")),
        new ImageIcon(getClass().getResource("images/sym_trianglefilled.gif")),
        new ImageIcon(getClass().getResource("images/sym_cross1.gif")),
        new ImageIcon(getClass().getResource("images/sym_cross2.gif"))
    };

    mSymbolPopup = new JOAJComboBox();
    for (int i = 0; i < symbolData.length; i++) {
      mSymbolPopup.addItem(symbolData[i]);
    }
    mSymbolPopup.setSelectedIndex(mCurrSymbol - 1);
    
    JPanel everyThingPanel = new JPanel(new BorderLayout(5, 5));

    // create the two parameter chooser lists
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));
    JPanel paramPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    JPanel upperPanel = new JPanel(new BorderLayout(5, 5));
    mYParamList = new StnParameterChooser(mFileViewer, new String("Stations Parameters:"), this, 5, "SALT");
    OffsetPanel ofp = new OffsetPanel(this);
    mYParamList.init();
    paramPanel.add(mYParamList);
    paramPanel.add(ofp);
    upperPanel.add("Center", paramPanel);
    everyThingPanel.add(BorderLayout.NORTH, upperPanel);

    // Options
    JPanel middlePanel = new JPanel();
    middlePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 3));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kOptions"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    middlePanel.setBorder(tb);

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
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    axesOptions.setBorder(tb);
    JPanel line0 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    JPanel line1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    JPanel line3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    JPanel line33 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    mConnectObs = new JOAJCheckBox(b.getString("kConnectObservations"), true);
    mReverseY = new JOAJCheckBox(b.getString("kReverseYAxis"), false);
    mReverseY.addItemListener(this);
    mPlotYGrid = new JOAJCheckBox(b.getString("kYGrid"));
    mPlotXGrid = new JOAJCheckBox(b.getString("kXGrid"));
    line0.add(mConnectObs);
    line1.add(mReverseY);
    line3.add(mPlotYGrid);
    line33.add(mPlotXGrid);
    axesOptions.add(line1);
    axesOptions.add(line3);
    axesOptions.add(line33);

    // other options
    JPanel otherOptions = new JPanel();
    otherOptions.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
    tb = BorderFactory.createTitledBorder(b.getString("kOther"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    otherOptions.setBorder(tb);

    // plot symbols
    JPanel line4 = new JPanel();
    line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    mEnableSymbols = new JOAJLabel(b.getString("kSymbol"));
    line4.add(mEnableSymbols);
    line4.add(mSymbolPopup);
    mSymbolPopup.addItemListener(this);
    mSizeLabel = new JOAJLabel(b.getString("kSize"));
    line4.add(mSizeLabel);

    SpinnerNumberModel model = new SpinnerNumberModel(4, 1, 100, 1);
    mSizeField = new JSpinner(model);

    line4.add(mSizeField);
    otherOptions.add(line0);
    otherOptions.add(line4);

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
    JPanel lineLCS = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
    lineLCS.add(new JOAJLabel(b.getString("kLineColor")));
    mLineColorSwatch = new Swatch(Color.black, 12, 12);
    lineLCS.add(new JOAJLabel(" "));
    lineLCS.add(mLineColorSwatch);

    JPanel lineSCS = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
    lineSCS.add(new JOAJLabel(b.getString("kSymbolColor")));
    mSymbolColorSwatch = new Swatch(Color.black, 12, 12);
    lineSCS.add(new JOAJLabel(" "));
    lineSCS.add(mSymbolColorSwatch);
    
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
    swatchCont.setLayout(new GridLayout(4, 1, 0, 5));
    swatchCont.add(lineLCS);
    swatchCont.add(lineSCS);
    swatchCont.add(line7);
    swatchCont.add(line8);
    JPanel swatchContCont = new JPanel();
    swatchContCont.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    swatchContCont.add(swatchCont);
    swatchContCont.add(line9);
    tb = BorderFactory.createTitledBorder(b.getString("kColors"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    swatchContCont.setBorder(tb);

    // window name
    JPanel namePanel = new JPanel();
    namePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));
    namePanel.add(new JOAJLabel(b.getString("kWindowName")));
    mNameField = new JOAJTextField(30);
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
    // axis container
    // y axis detail
    // container for the axes stuff
    plotScaleCont = new JPanel(new GridLayout(1, 2, 5, 5));

    // y axis container
    JPanel yAxis = new JPanel(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
    tb = BorderFactory.createTitledBorder(b.getString("kYAxis"));
    yAxis.setBorder(tb);
    StnPlotSpecification mPlotSpec = new StnPlotSpecification();

    JPanel line5y = new JPanel();
    line5y.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line5y.add(new JOAJLabel(b.getString("kMinimum")));
    yMin = new JOAJTextField(6);
    yMin.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinYPlotMin()), 3, false));
    yMin.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line5y.add(yMin);

    JPanel line6y = new JPanel();
    line6y.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line6y.add(new JOAJLabel(b.getString("kMaximum")));
    yMax = new JOAJTextField(6);
    yMax.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinYPlotMax()), 3, false));
    yMax.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line6y.add(yMax);

    JPanel line7y = new JPanel();
    line7y.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line7y.add(new JOAJLabel(b.getString("kIncrement")));
    yInc = new JOAJTextField(6);
    yInc.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getYInc()), 3, false));
    yInc.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line7y.add(yInc);

    JPanel line8y = new JPanel();
    line8y.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line8y.add(new JOAJLabel(b.getString("kNoMinorTicks")));

    SpinnerNumberModel model3 = new SpinnerNumberModel(mPlotSpec.getYTics(), 0, 100, 1);
    yTics = new JSpinner(model3);
    line8y.add(yTics);

    yAxis.add(line5y);
    yAxis.add(line6y);
    yAxis.add(line7y);
    yAxis.add(line8y);
    yAxis.add(new JLabel("   "));
    yAxis.add(new JLabel("    "));
    yAxis.add(new JLabel("    "));
    plotScaleCont.add(yAxis);

    // x axis container
    JPanel xAxis = new JPanel(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
    tb = BorderFactory.createTitledBorder(b.getString("kXAxis"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    xAxis.setBorder(tb);
    
    JPanel line5x = new JPanel();
    line5x.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line5x.add(mMinXLabel);
    xMin = new JOAJTextField(6);
    xMin.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinXPlotMin()), 3, false));
    xMin.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line5x.add(xMin);
    
    JPanel minTime = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 1));
    minTime.add(mMinXTLabel);
    JPanel maxTime = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 1));
    maxTime.add(mMaxXTLabel);

		GeoDate minDate = new GeoDate(mFileViewer.getMinDate());
		minDate.decrement(1.0, GeoDate.YEARS);
		GeoDate maxDate = new GeoDate(mFileViewer.getMaxDate());
		maxDate.increment(1.0, GeoDate.YEARS);
		//value, start,end
		SpinnerDateModel mStartDateModel = new SpinnerDateModel(new GeoDate(mFileViewer.getMinDate()), minDate, maxDate, Calendar.HOUR);
		 mStartSpinner = new JSpinner(mStartDateModel);
		JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mStartSpinner, "yyyy-MM-dd HH:mm");
		mStartSpinner.setEditor(dateEditor);
		
		SpinnerDateModel mEndDateModel = new SpinnerDateModel(new GeoDate(mFileViewer.getMaxDate()), minDate, maxDate, Calendar.HOUR);
		 mEndSpinner = new JSpinner(mEndDateModel);
		JSpinner.DateEditor dateEditor2 = new JSpinner.DateEditor(mEndSpinner, "yyyy-MM-dd HH:mm");
		minTime.add(mStartSpinner);
		maxTime.add(mEndSpinner);
		mEndSpinner.setEditor(dateEditor2);

    JPanel line6x = new JPanel();
    line6x.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line6x.add(mMaxXLabel);
    xMax = new JOAJTextField(6);
    xMax.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinXPlotMax()), 3, false));
    xMax.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line6x.add(xMax);

    JPanel line7x = new JPanel();
    line7x.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line7x.add(mIncLabel);
    xInc = new JOAJTextField(6);
    xInc.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getXInc()), 3, false));
    xInc.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line7x.add(xInc);

    JPanel line8x = new JPanel();
    line8x.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
    line8x.add(mXIncLabel);

    SpinnerNumberModel model2 = new SpinnerNumberModel(mPlotSpec.getXTics(), 0, 100, 1);
    xTics = new JSpinner(model2);
    line8x.add(xTics);

    xAxis.add(minTime);
    xAxis.add(maxTime);
    xAxis.add(line5x);
    xAxis.add(line6x);
    xAxis.add(line7x);
    xAxis.add(line8x);
    plotScaleCont.add(xAxis);

    everyThingPanel.add(BorderLayout.CENTER, plotScaleCont);
    everyThingPanel.add(BorderLayout.SOUTH, middlePanel);

    mainPanel.add(BorderLayout.CENTER, new TenPixelBorder(everyThingPanel, 10, 10, 10, 10));

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
    setXRangeToDistance();

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

  private class OffsetPanel extends JPanel {
    private OffsetPanel(Component comp) {
      this.setLayout(new BorderLayout(0, 0));
      TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kOffsetBy"));
      if (JOAConstants.ISMAC) {
        //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
      }
      this.setBorder(tb);
      JPanel controls = new JPanel();
      controls.setLayout(new GridLayout(3, 1, 5, 0));
      b1 = new JOAJRadioButton(b.getString("kSequence"));
      b2 = new JOAJRadioButton(b.getString("kDistance"), true);
      b3 = new JOAJRadioButton(b.getString("kTime"));
      controls.add(b2);
      controls.add(b1);
      controls.add(b3);
      controls.add(new JOAJLabel("       "));
      ButtonGroup bg = new ButtonGroup();
      bg.add(b1);
      bg.add(b2);
      bg.add(b3);
      this.add("Center", controls);
      b1.addItemListener((ItemListener)comp);
      b2.addItemListener((ItemListener)comp);
      b3.addItemListener((ItemListener)comp);
    }
  }

  public void valueChanged(ListSelectionEvent evt) {
    if (evt.getSource() == mYParamList.getJList()) {
      mSelYParam = mYParamList.getJList().getSelectedIndex();
      setAdvancedValues();
    }

    generatePlotName();
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      this.dispose();
      timer.cancel();
    }
    else if (cmd.equals("ok")) {
      StnPlotSpecification spec = createPlotSpec();
      try {
        spec.writeToLog("New Station Value Plot (" + mFileViewer.getTitle() + "):");
      }
      catch (Exception ex) {}

      JOAStnValPlotWindow plotWind = new JOAStnValPlotWindow(spec, mFileViewer);
      plotWind.pack();
      plotWind.setVisible(true);
      mFileViewer.addOpenWindow(plotWind);
      timer.cancel();
      this.dispose();
    }
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJRadioButton) {
      JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
      if (rb == b1 && evt.getStateChange() == ItemEvent.SELECTED) {
        mOffset = JOAConstants.PROFSEQUENCE;
        setXRangeToSequence();
      }
      else if (rb == b2 && evt.getStateChange() == ItemEvent.SELECTED) {
        mOffset = JOAConstants.PROFDISTANCE;
        setXRangeToDistance();
      }
      else if (rb == b3 && evt.getStateChange() == ItemEvent.SELECTED) {
        mOffset = JOAConstants.PROFTIME;
        setXRangeToTime();
      }
    }
    else if (evt.getSource() instanceof JOAJCheckBox) {
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
    }
    generatePlotName();
  }

  public void maintainButtons() {
    if (mSelYParam >= 0) {
      mOKBtn.setEnabled(true);
    }
    else {
      mOKBtn.setEnabled(false);
    }
  }

  public StnPlotSpecification createPlotSpec() {
    StnPlotSpecification ps = new StnPlotSpecification();
    // get the colors
    ps.setFGColor(axesColor.getColor());
    ps.setBGColor(plotBg.getColor());

    ps.setSectionType(mOffset);
    ps.setFileViewer(mFileViewer);
    ps.setXStnVarCode(mSelYParam);
    ps.setYStnVarName(new String((String)mYParamList.getJList().getSelectedValue()));
    ps.setWinTitle(mNameField.getText());
    ps.setYGrid(mPlotYGrid.isSelected());
    ps.setXGrid(mPlotXGrid.isSelected());
    ps.setReverseY(mReverseY.isSelected());
    ps.setSymbol(mCurrSymbol);
    ps.setSymbolSize(((Integer)mSizeField.getValue()).intValue());
    ps.setConnectObs(mConnectObs.isSelected());    
    ps.setLineColor(mLineColorSwatch.getColor());
    ps.setSymbolColor(mSymbolColorSwatch.getColor());

    boolean error = false;
    try {
      ps.setWinYPlotMin(Double.valueOf(yMin.getText()).doubleValue());
    }
    catch (NumberFormatException ex) {
      error = true;
    }

    try {
      ps.setWinYPlotMax(Double.valueOf(yMax.getText()).doubleValue());
    }
    catch (NumberFormatException ex) {
      error = true;
    }

    try {
      ps.setYInc(Double.valueOf(yInc.getText()).doubleValue());
    }
    catch (NumberFormatException ex) {
      error = true;
    }

    ps.setYTics(((Integer)yTics.getValue()).intValue());

    if (mOffset == JOAConstants.PROFSEQUENCE || mOffset == JOAConstants.PROFDISTANCE) {
    	try {
    		ps.setWinXPlotMin(Double.valueOf(xMin.getText()).doubleValue());
    	}
    	catch (NumberFormatException ex) {
    		error = true;
    	}

    	try {
    		ps.setWinXPlotMax(Double.valueOf(xMax.getText()).doubleValue());
    	}
    	catch (NumberFormatException ex) {
    		error = true;
    	}

    	try {
    		ps.setXInc(Double.valueOf(xInc.getText()).doubleValue());
    	}
    	catch (NumberFormatException ex) {
    		error = true;
    	}
    }
    else {

			long startTime = ((Date) mStartSpinner.getValue()).getTime();
			long endTime = ((Date) mEndSpinner.getValue()).getTime();
			
  		ps.setWinXPlotMin(startTime);
  		ps.setWinXPlotMax(endTime);
    }

    ps.setXTics(((Integer)xTics.getValue()).intValue());

    if (error) {
      // post alert
    }
    return ps;
  }

  public void setAdvancedValues() {
    // get pretty ranges for the current parameters
    //get the range for the station value
    double min = 10000;
    double max = 0;
    for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

      for (int sec = 0; sec < of.mNumSections; sec++) {
        Section sech = (Section)of.mSections.elementAt(sec);

        if (sech.mNumCasts == 0) {
          continue;
        }

        for (int stc = 0; stc < sech.mStations.size(); stc++) {
          Station sh = (Station)sech.mStations.elementAt(stc);
          if (!sh.mUseStn) {
            continue;
          }

          // get the station value
          double y = sh.getStnValue(mSelYParam);
          if (y == JOAConstants.MISSINGVALUE || y >= JOAConstants.EPICMISSINGVALUE) {
          	continue;
          }
          else {
            min = y < min ? y : min;
            max = y > max ? y : max;
          }
        }
      }
    }

    Triplet newRange = JOAFormulas.GetPrettyRange(min, max);
    double yMinv = newRange.getVal1();
    double yMaxv = newRange.getVal2();
    double yIncv = newRange.getVal3();
    yMin.setText(JOAFormulas.formatDouble(String.valueOf(yMinv), 3, false));
    yMax.setText(JOAFormulas.formatDouble(String.valueOf(yMaxv), 3, false));
    yInc.setText(JOAFormulas.formatDouble(String.valueOf(yIncv), 3, false));
    yTics.setValue(new Integer(yTicsVal));
    if (b1.isSelected()) {
      mOffset = JOAConstants.PROFSEQUENCE;
      setXRangeToSequence();
    }
    else if (b2.isSelected()) {
      mOffset = JOAConstants.PROFDISTANCE;
      setXRangeToDistance();
    }
    else if (b3.isSelected()) {
      mOffset = JOAConstants.PROFTIME;
      setXRangeToTime();
    }
  }

  public void setXRangeToDistance() {
  	hideTime();
    //set the x axis from the total mercator distance
    double tempXMin = 0;
    double tempXMax = mFileViewer.mTotMercDist * 1.852;
    Triplet newRange = JOAFormulas.GetPrettyRange(tempXMin, tempXMax);
    double xMinv = newRange.getVal1();
    double xMaxv = newRange.getVal2();
    double xIncv = newRange.getVal3();
    xMin.setText(JOAFormulas.formatDouble(String.valueOf(xMinv), 3, false));
    xMax.setText(JOAFormulas.formatDouble(String.valueOf(xMaxv), 3, false));
    xInc.setText(JOAFormulas.formatDouble(String.valueOf(xIncv), 3, false));
    xTics.setValue(new Integer(xTicsVal));
  }

  public void setXRangeToTime() {
  	showTime();
    //set the x axis from the time range of the data
  	GeoDate minDate = mFileViewer.getMinDate();
  	GeoDate maxDate = mFileViewer.getMaxDate();
  	
    xTics.setValue(new Integer(xTicsVal));
  }

  public void setXRangeToSequence() {
  	hideTime();
    //set the x axis from the total mercator distance
    double tempXMin = 0;
    double tempXMax = mFileViewer.mTotalStations;
    Triplet newRange = JOAFormulas.GetPrettyRange(tempXMin, tempXMax);
    double xMinv = newRange.getVal1();
    double xMaxv = newRange.getVal2();
    double xIncv = newRange.getVal3();
    xMin.setText(String.valueOf((int)xMinv));
    xMax.setText(String.valueOf((int)xMaxv));
    xInc.setText(String.valueOf((int)xIncv));
    xTics.setValue(new Integer(xTicsVal));
  }
  
  public void hideTime() {
  	mMaxXTLabel.setEnabled(false);  
  	mMinXTLabel.setEnabled(false);   	
  	mMaxXLabel.setEnabled(true);  
  	mMinXLabel.setEnabled(true);
  	mIncLabel.setEnabled(true);    
    xMin.setEnabled(true);	  
    xMax.setEnabled(true);	
    xInc.setEnabled(true);	
    mStartSpinner.setEnabled(false);
    mEndSpinner.setEnabled(false);
    xTics.setEnabled(true);	
    mPlotXGrid.setEnabled(true);	
  	mXIncLabel.setEnabled(true);  
  }
  
  public void showTime() {
  	mMaxXTLabel.setEnabled(true);  
  	mMinXTLabel.setEnabled(true);  	
  	mMaxXLabel.setEnabled(false);  
  	mMinXLabel.setEnabled(false); 
  	mIncLabel.setEnabled(false);   
    xMin.setText("");	  
    xMax.setText("");		
    xInc.setText("");		
    xMin.setEnabled(false);	  
    xMax.setEnabled(false);	
    xInc.setEnabled(false);	
    mStartSpinner.setEnabled(true);
    mEndSpinner.setEnabled(true);
    xTics.setEnabled(false);	
    mPlotXGrid.setEnabled(false);	
  	mXIncLabel.setEnabled(false);  
  }

  public void generatePlotName() {
    String yVarText = (String)mYParamList.getJList().getSelectedValue();
    if (yVarText == null || yVarText.length() == 0) {
      yVarText = "?";
    }

    String offsetStr = null;
    if (b1.isSelected()) {
      offsetStr = "Seq";
    }
    else if (b2.isSelected()) {
      offsetStr = "Dist";
    }
    else {
      offsetStr = "Time";
    }

    String nameString = new String(yVarText + "-" + offsetStr + " (" + mFileViewer.mFileViewerName + ")");
    mNameField.setText(nameString);
  }
}

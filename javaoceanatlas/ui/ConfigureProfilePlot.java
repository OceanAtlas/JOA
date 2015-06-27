/*
 * $Id: ConfigureProfilePlot.java,v 1.6 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
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
public class ConfigureProfilePlot extends JOAJDialog implements ListSelectionListener, ActionListener,
    ButtonMaintainer, ItemListener {
	protected FileViewer mFileViewer;
	protected String mTitle;
	protected Component mComp;
	protected ParameterChooser mXParamList;
	protected ParameterChooser mYParamList;
	protected int mSelXParam = -1;
	protected int mSelYParam = -1;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJTextField mNameField = null;
	protected ResourceBundle b = null;
	protected JOAJRadioButton b1 = null;
	protected JOAJRadioButton b2 = null;
	protected JOAJRadioButton b3 = null;
	protected int mOffset = JOAConstants.PROFSEQUENCE;
	protected JOAJCheckBox mIncludeColorBarCB = null;
	protected Swatch plotBg = null;
	protected Swatch axesColor = null;
	protected JOAJComboBox presetColorSchemes = null;
	protected boolean mIncludeColorBar = false, mIncludeObsPanel = false;
	protected JOAJCheckBox mPlotAxes = null;
	protected JOAJCheckBox mPlotYGrid = null;
	protected DisclosureButton mDiscloseOptions = null;
	protected AdvProfPlotOptions mAdvOpt = null;
	protected ProfilePlotSpecification mPlotSpec = null;
	protected JOAJComboBox mSymbolPopup = null;
	protected int mCurrSymbol = JOAConstants.SYMBOL_SQUAREFILLED;
	protected Icon[] symbolData = null;
	protected JSpinner mSizeField = null;
	protected boolean mPlotSymbols = false;
	protected JOAJCheckBox mEnableSymbols = null;
	protected JOAJLabel mSizeLabel = null;
	protected double mTraceOffset = 5.0;
	private Timer timer = new Timer();
	protected JOAJTabbedPane everyThingPanel;
  protected JOAJRadioButton mPlotAllStns = null;
  protected JOAJRadioButton mAccumulateStns = null;

	public ConfigureProfilePlot(JFrame par, FileViewer fv) {
		super(par, "Profile Plot", false);
		mFileViewer = fv;
		this.init();
	}

	public void init() {
		b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

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
		mSymbolPopup.setSelectedIndex(mCurrSymbol - 1);

		// create the tabbed panel and the container panels
		everyThingPanel = new JOAJTabbedPane();
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 5));
		JPanel paramPanel = new JPanel();
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout(5, 5));
		paramPanel.setLayout(new GridLayout(1, 2, 5, 5));

		// create the two parameter chooser lists
		mXParamList = new ParameterChooser(mFileViewer, new String("X-Axis Parameter:"), this, 5, "SALT");
		mYParamList = new ParameterChooser(mFileViewer, new String("Y-Axis Parameter:"), this, 5, "SALT");
		OffsetPanel ofp = new OffsetPanel(this);
		mXParamList.init();
		mYParamList.init();
		paramPanel.add(mXParamList);
		paramPanel.add(mYParamList);
		paramPanel.add(ofp);
		upperPanel.add("Center", paramPanel);

		// Options
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 3));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kOptions"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
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
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		axesOptions.setBorder(tb);
		JPanel line1 = new JPanel();
		line1.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel line3 = new JPanel();
		line3.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mPlotAxes = new JOAJCheckBox(b.getString("kPlotAxes"), true);
		mPlotAxes.addItemListener(this);
		mPlotYGrid = new JOAJCheckBox(b.getString("kYGrid"));
		line3.add(new JOAJLabel("    "));
		line1.add(mPlotAxes);
		line3.add(mPlotYGrid);
		axesOptions.add(line1);
		axesOptions.add(line3);

		// other options
		JPanel otherOptions = new JPanel();
		otherOptions.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
		tb = BorderFactory.createTitledBorder(b.getString("kOther"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		otherOptions.setBorder(tb);

    JPanel p1 = new JPanel();
    p1.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.LEFT, 0));
    mPlotAllStns = new JOAJRadioButton(b.getString("kPlotAllStns"), true);
    mAccumulateStns = new JOAJRadioButton(b.getString("kAccumulateStns"), false);
    p1.add(mPlotAllStns);
    p1.add(mAccumulateStns);
    otherOptions.add(p1);
    
    ButtonGroup bg0 = new ButtonGroup();
    bg0.add(mPlotAllStns);
    bg0.add(mAccumulateStns);

		// plot symbols
		JPanel line4 = new JPanel();
		line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		mEnableSymbols = new JOAJCheckBox(b.getString("kSymbol"), false);
		mEnableSymbols.addItemListener(this);
		line4.add(mEnableSymbols);
		line4.add(mSymbolPopup);
		mSymbolPopup.addItemListener(this);
		mSizeLabel = new JOAJLabel(b.getString("kSize"));
		line4.add(mSizeLabel);

		SpinnerNumberModel model2 = new SpinnerNumberModel(3, 1, 100, 1);
		mSizeField = new JSpinner(model2);

		line4.add(mSizeField);
		otherOptions.add(line4);
		disableSymbolStuff();

		// include NewColorBar
		JPanel line5 = new JPanel();
		line5.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		mIncludeColorBarCB = new JOAJCheckBox(b.getString("kColorLegend"));
		mIncludeColorBarCB.addItemListener(this);
		line5.add(mIncludeColorBarCB);
		otherOptions.add(line5);

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

		upperPanel.add("South", middlePanel);

		// advanced options panel
		mPlotSpec = new ProfilePlotSpecification();
		mPlotSpec.setAmplitude(1.0);
		mPlotSpec.setLineWidth(2);
		mPlotSpec.setTraceOffset(5.0);

		mAdvOpt = new AdvProfPlotOptions(mFileViewer, mPlotSpec);
		mAdvOpt.setVisible(true);
		JPanel advCont = new JPanel();
		advCont.setLayout(new BorderLayout(0, 0));
		advCont.add(mAdvOpt, "North");

		everyThingPanel.addTab(b.getString("kContourTab1"), upperPanel);
		everyThingPanel.addTab(b.getString("kContourTab2"), advCont);

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

	private class OffsetPanel extends JPanel {
		private OffsetPanel(Component comp) {
			this.setLayout(new BorderLayout(0, 0));
			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kOffsetBy"));
			if (JOAConstants.ISMAC) {
				// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
			}
			this.setBorder(tb);
			JPanel controls = new JPanel();
			controls.setLayout(new GridLayout(3, 1, 5, 0));
			b1 = new JOAJRadioButton(b.getString("kSequence"), true);
			b2 = new JOAJRadioButton(b.getString("kDistance"));
			b3 = new JOAJRadioButton(b.getString("kTime"));
			controls.add(b1);
			controls.add(b2);
			controls.add(b3);
			// controls.add(new JOAJLabel(" "));
			ButtonGroup bg = new ButtonGroup();
			bg.add(b1);
			bg.add(b2);
			bg.add(b3);
			b1.setActionCommand("seq");
			b2.setActionCommand("dis");
			b3.setActionCommand("time");
			this.add("Center", controls);
			b1.addItemListener((ItemListener) comp);
			b2.addItemListener((ItemListener) comp);
			b3.addItemListener((ItemListener) comp);
		}
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mXParamList.getJList()) {
			// get the x param
			mSelXParam = mXParamList.getJList().getSelectedIndex();
		}
		else if (evt.getSource() == mYParamList.getJList()) {
			mSelYParam = mYParamList.getJList().getSelectedIndex();
		}

		if (mSelYParam >= 0) {
			UVCoordinate retVal = setAdvancedValues();
			int xerr = (int) retVal.u;
			int yerr = (int) retVal.v;
			if (xerr == -1 && yerr == -1)
				everyThingPanel.setEnabledAt(1, true);
			else {
				if (xerr >= 0 && yerr == -1) {
					// disable the x param
					JFrame f = new JFrame("Parameter Values Missing Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "All values for " + mFileViewer.mAllProperties[xerr].getVarLabel()
					    + " are missing. " + "\n" + "Select a new X parameter.");
					mXParamList.clearSelection();
					mSelXParam = 0;
				}
				else if (xerr == -1 && yerr >= 0) {
					// disable the y param
					JFrame f = new JFrame("Parameter Values Missing Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "All values for " + mFileViewer.mAllProperties[yerr].getVarLabel()
					    + " are missing. " + "\n" + "Select a new Y parameter.");
					mYParamList.clearSelection();
					mSelYParam = 0;
				}
				else if (xerr >= 0 && yerr >= 0) {
					// disable the y param
					mXParamList.clearSelection();
					mYParamList.clearSelection();
					JFrame f = new JFrame("Parameter Values Missing Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "All values for " + mFileViewer.mAllProperties[yerr].getVarLabel()
					    + " are missing and \n" + "all values for " + mFileViewer.mAllProperties[xerr].getVarLabel()
					    + " are missing. \n" + "Select new Y and X parameters.");
					mSelYParam = 0;
					mSelXParam = 0;
				}
				generatePlotName();
			}
		}
		else
			everyThingPanel.setEnabledAt(1, false);

		generatePlotName();
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			this.dispose();
			timer.cancel();
		}
		else if (cmd.equals("ok")) {
			if (mIncludeColorBar) {
			}
			else {
			}

			ProfilePlotSpecification spec = createPlotSpec();
			try {
				spec.writeToLog("New Profile Plot (" + mFileViewer.getTitle() + ")");
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

			JOAProfilePlotWindow plotWind = new JOAProfilePlotWindow(spec, mFileViewer);
			plotWind.pack();
			plotWind.setVisible(true);
			mFileViewer.addOpenWindow(plotWind);
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("options")) {
			if (mDiscloseOptions.isSelected()) {
				mAdvOpt.setVisible(true);
				setAdvancedValues();
			}
			else {
				mAdvOpt.setVisible(false);
			}
			this.pack();
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJRadioButton) {
			JOAJRadioButton rb = (JOAJRadioButton) evt.getSource();
			if (rb == b1 && evt.getStateChange() == ItemEvent.SELECTED) {
				mOffset = JOAConstants.PROFSEQUENCE;
				mTraceOffset = 5.0;
			}
			else if (rb == b2 && evt.getStateChange() == ItemEvent.SELECTED) {
				mOffset = JOAConstants.PROFDISTANCE;
				mTraceOffset = 800 / mFileViewer.mTotMercDist;
			}
			else if (rb == b3 && evt.getStateChange() == ItemEvent.SELECTED) {
				mOffset = JOAConstants.PROFTIME;
				// time offset is stored as days rather than milliseconds
				mTraceOffset = 800 / mFileViewer.getTimeLengthDays();
			}
		}
		else if (evt.getSource() instanceof JOAJCheckBox) {
			JOAJCheckBox cb = (JOAJCheckBox) evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && cb == mIncludeColorBarCB) {
				mIncludeColorBar = true;
			}
			else if (cb == mIncludeColorBarCB) {
				mIncludeColorBar = false;
			}

			if (evt.getStateChange() == ItemEvent.SELECTED && cb == mPlotAxes) {
				enableGrid();
			}
			else if (cb == mPlotAxes) {
				disableGrid();
			}

			if (evt.getStateChange() == ItemEvent.SELECTED && cb == mEnableSymbols) {
				enableSymbolStuff();
				mPlotSymbols = true;
			}
			else if (cb == mEnableSymbols) {
				disableSymbolStuff();
				mPlotSymbols = false;
			}
		}
		else if (evt.getSource() instanceof JOAJComboBox) {
			JOAJComboBox cb = (JOAJComboBox) evt.getSource();
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

	public void enableGrid() {
		mPlotYGrid.setEnabled(true);
	}

	public void disableGrid() {
		mPlotYGrid.setEnabled(false);
	}

	public void enableSymbolStuff() {
		mSymbolPopup.setEnabled(true);
		mSizeField.setEnabled(true);
		// mSizeLabel.setEnabled(true);
		mSizeLabel.invalidate();
		mSymbolPopup.invalidate();
		mSizeField.invalidate();
		this.validate();
	}

	public void disableSymbolStuff() {
		mSymbolPopup.setEnabled(false);
		mSizeField.setEnabled(false);
		// mSizeLabel.setEnabled(false);
		mSizeLabel.invalidate();
		mSymbolPopup.invalidate();
		mSizeField.invalidate();
		this.validate();
	}

	public void maintainButtons() {
		if (mSelXParam >= 0 && mSelYParam >= 0) {
			mOKBtn.setEnabled(true);
		}
		else {
			mOKBtn.setEnabled(false);
		}
	}

	public ProfilePlotSpecification createPlotSpec() {
		ProfilePlotSpecification ps = new ProfilePlotSpecification();
		// get the colors
		ps.setFGColor(axesColor.getColor());
		ps.setBGColor(plotBg.getColor());
		if (mIncludeColorBar) {
			ps.setWidth(850);
			ps.setHeight(450);
		}
		else {
			ps.setWidth(750);
			ps.setHeight(450);
		}

		ps.setSectionType(mOffset);
		ps.setFileViewer(mFileViewer);
		ps.setXVarCode(mSelXParam);
		ps.setYVarCode(mSelYParam);
		ps.setWinTitle(mNameField.getText());
		ps.setIncludeCBAR(mIncludeColorBar);
		ps.setIncludeObsPanel(mIncludeObsPanel);
		ps.setYGrid(mPlotYGrid.isSelected());
		ps.setPlotAxes(mPlotAxes.isSelected());
		ps.setSymbolSize(3);
		ps.setSymbol(mCurrSymbol);
		ps.setPlotSymbols(mPlotSymbols);
		ps.setSymbolSize(((Integer) mSizeField.getValue()).intValue());
    ps.setAccumulateStns(mAccumulateStns.isSelected());  

		ps.setWinXPlotMin(mFileViewer.mAllProperties[mSelXParam].getPlotMin());
		ps.setWinXPlotMax(mFileViewer.mAllProperties[mSelXParam].getPlotMax());
		/*
		 * Triplet newRange = JOAFormulas.GetPrettyRange(tempMin, tempMax);
		 * ps.mWinXPlotMin = tempMin;//newRange.getVal1(); ps.mWinXPlotMax =
		 * tempMax;//newRange.getVal2();
		 */
		ps.setTraceOffset(mTraceOffset);

		if (mAdvOpt.isChanged()) {
			ps.setWinYPlotMin(mAdvOpt.getYMin());
			ps.setWinYPlotMax(mAdvOpt.getYMax());
			ps.setYInc(mAdvOpt.getYInc());
			ps.setYTics(mAdvOpt.getYTics());
			ps.setAmplitude(1.0);

			try {
				ps.setLineWidth(mAdvOpt.getLineWidth());
			}
			catch (NumberFormatException ex) {
				ps.setLineWidth(1);
			}
		}
		else {
			// create pretty axes ranges really get these from the text fields if
			// needed
			ps.setYTics(1);
			double tempMin = mFileViewer.mAllProperties[mSelYParam].getPlotMin();
			double tempMax = mFileViewer.mAllProperties[mSelYParam].getPlotMax();
			Triplet newRange = JOAFormulas.GetPrettyRange(tempMin, tempMax);
			ps.setWinYPlotMin(newRange.getVal1());
			ps.setWinYPlotMax(newRange.getVal2());
			ps.setYInc(newRange.getVal3());
			ps.setAmplitude(1.0);
			// need to test whether seq or dist
			ps.setLineWidth(1);
		}

		return ps;
	}

	public UVCoordinate setAdvancedValues() {
		int xerrLine = -1;
		double xInc = Double.NaN;
		if (mSelXParam >= 0) {
			double tempXMin = mFileViewer.mAllProperties[mSelXParam].getPlotMin();
			double tempXMax = mFileViewer.mAllProperties[mSelXParam].getPlotMax();
			Triplet newRange = JOAFormulas.GetPrettyRange(tempXMin, tempXMax);
			newRange.getVal2();
			xInc = newRange.getVal3();
			if (Double.isNaN(xInc)) {
				xerrLine = mSelXParam;
			}
		}

		// get pretty ranges for the current parameters
		int yerrLine = -1;
		double tempYMin = Double.NaN;
		double tempYMax = Double.NaN;
		double yMin = Double.NaN;
		double yMax = Double.NaN;
		double yInc = Double.NaN;
		if (mSelYParam >= 0) {
			tempYMin = mFileViewer.mAllProperties[mSelYParam].getPlotMin();
			tempYMax = mFileViewer.mAllProperties[mSelYParam].getPlotMax();
			Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
			yMin = newRange.getVal1();
			yMax = newRange.getVal2();
			yInc = newRange.getVal3();
			if (Double.isNaN(yInc)) {
				yerrLine = mSelYParam;
			}
		}

		if (xerrLine >= 0 || yerrLine >= 0) {
			// parameter is completely missing
			return new UVCoordinate((double) xerrLine, (double) yerrLine);
		}
		if (mSelYParam >= 0 && mSelXParam >= 0) {
			mAdvOpt.setParameters(mSelYParam, mSelXParam);
			mAdvOpt.setValues(yMin, yMax, yInc, 1, tempYMin, tempYMax);
		}
		return new UVCoordinate((double) xerrLine, (double) yerrLine);
	}

	public void generatePlotName() {
		String xVarText = (String) mXParamList.getJList().getSelectedValue();
		if (xVarText == null || xVarText.length() == 0)
			xVarText = "?";

		String yVarText = (String) mYParamList.getJList().getSelectedValue();
		if (yVarText == null || yVarText.length() == 0)
			yVarText = "?";

		String offsetStr = null;
		if (b1.isSelected())
			offsetStr = "Seq";
		else if (b2.isSelected())
			offsetStr = "Dist";
		else if (b3.isSelected())
			offsetStr = "Time";

		String nameString = new String(yVarText + "-" + xVarText + "-" + offsetStr + " (" + mFileViewer.mFileViewerName
		    + ")");
		mNameField.setText(nameString);
	}
}

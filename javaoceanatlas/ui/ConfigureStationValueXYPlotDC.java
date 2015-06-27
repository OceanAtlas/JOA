
/*
 * $Id: ConfigureStationValuePlotDC.java,v 1.3 2005/06/17 18:08:53 oz Exp $
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
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ConfigureStationValueXYPlotDC extends JOAJDialog implements ListSelectionListener, ActionListener,
    ButtonMaintainer, ItemListener {
	protected FileViewer mFileViewer;
	protected String mTitle;
	protected Component mComp;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJButton mApplyButton = null;
	protected JOAJTextField mNameField = null;
	protected ResourceBundle b = null;
	protected JOAJRadioButton b1 = null;
	protected JOAJRadioButton b2 = null;
	protected int mOffset = JOAConstants.PROFDISTANCE;
	protected Swatch plotBg = null;
	protected Swatch axesColor = null;
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
	protected JOAJTextField xMin, xMax, xInc;
	protected JSpinner yTics, xTics;
	protected JPanel stuffCont = null;
	protected int pPos = -1;
	protected int yTicsVal = 1;
	protected int xTicsVal = 1;
	protected StnPlotSpecification mPlotSpec;
	protected StnPlotSpecification mOriginalPlotSpec = null;
	protected DialogClient mClient;
	protected JDialog mFrame = null;
	protected int mStnVarXCode;
	protected int mStnVarYCode;
	protected JOAJTabbedPane everyThingPanel;

	public ConfigureStationValueXYPlotDC(JOAWindow par, FileViewer fv, DialogClient client, StnPlotSpecification spec) {
		super(par, "Station Value XY Plot", false);
		mFileViewer = fv;
		mStnVarXCode = spec.getXStnVarCode();
		mStnVarYCode = spec.getYStnVarCode();
		mPlotSpec = spec;
		mOriginalPlotSpec = new StnPlotSpecification(spec);
		mClient = client;
		mFrame = this;
		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				mClient.dialogCancelled(mFrame);
			}
		};
		this.addWindowListener(windowListener);
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
		mCurrSymbol = mPlotSpec.getSymbol();
		mSymbolPopup.setSelectedIndex(mCurrSymbol - 1);

		// create the tabbed panel and the container panels
		everyThingPanel = new JOAJTabbedPane();

		// create the two parameter chooser lists
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 5));
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout(5, 5));
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
		JPanel line33 = new JPanel();
		line33.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mReverseY = new JOAJCheckBox(b.getString("kReverseYAxis"), mPlotSpec.isReverseY());
		mReverseY.addItemListener(this);
		mPlotYGrid = new JOAJCheckBox(b.getString("kYGrid"), mPlotSpec.isYGrid());
		mPlotXGrid = new JOAJCheckBox(b.getString("kXGrid"), mPlotSpec.isXGrid());
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
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
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

		SpinnerNumberModel model = new SpinnerNumberModel(mPlotSpec.getSymbolSize(), 1, 100, 1);
		mSizeField = new JSpinner(model);
		line4.add(mSizeField);
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
		JPanel line7 = new JPanel();
		line7.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		line7.add(new JOAJLabel(b.getString("kBackgroundColor")));
		plotBg = new Swatch(JOAConstants.DEFAULT_CONTENTS_COLOR, 12, 12);
		line7.add(new JOAJLabel(" "));
		line7.add(plotBg);
		JPanel line8 = new JPanel();
		line8.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		line8.add(new JOAJLabel(b.getString("kGridColor")));
		axesColor = new Swatch(mPlotSpec.getFGColor(), 12, 12);
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

		// add the color panel
		colorNameContPanel.add("North", swatchContCont);
		colorNamePanel.add(colorNameContPanel);

		// add these to the south of the borderlayout
		nonAdvOptions.add("South", colorNamePanel);

		// add all of this to the middle panel
		middlePanel.add(nonAdvOptions);

		// advanced options panel
		// axis container
		// y axis detail
		// container for the axes stuff
		stuffCont = new JPanel();
		stuffCont.setLayout(new GridLayout(1, 2, 5, 5));

		// y axis container
		JPanel yAxis = new JPanel();
		yAxis.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
		tb = BorderFactory.createTitledBorder(b.getString("kYAxis"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		yAxis.setBorder(tb);

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
		stuffCont.add(yAxis);

		// x axis container
		JPanel xAxis = new JPanel();
		xAxis.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 2));
		tb = BorderFactory.createTitledBorder(b.getString("kXAxis"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		xAxis.setBorder(tb);

		JPanel line5x = new JPanel();
		line5x.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
		line5x.add(new JOAJLabel(b.getString("kMinimum")));
		xMin = new JOAJTextField(6);
		xMin.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinXPlotMin()), 3, false));
		xMin.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line5x.add(xMin);

		JPanel line6x = new JPanel();
		line6x.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
		line6x.add(new JOAJLabel(b.getString("kMaximum")));
		xMax = new JOAJTextField(6);
		xMax.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getWinXPlotMax()), 3, false));
		xMax.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line6x.add(xMax);

		JPanel line7x = new JPanel();
		line7x.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
		line7x.add(new JOAJLabel(b.getString("kIncrement")));
		xInc = new JOAJTextField(6);
		xInc.setText(JOAFormulas.formatDouble(String.valueOf(mPlotSpec.getXInc()), 3, false));
		xInc.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line7x.add(xInc);

		JPanel line8x = new JPanel();
		line8x.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 1));
		line8x.add(new JOAJLabel(b.getString("kNoMinorTicks")));

		SpinnerNumberModel model2 = new SpinnerNumberModel(mPlotSpec.getXTics(), 0, 100, 1);
		xTics = new JSpinner(model2);
		line8x.add(xTics);

		xAxis.add(line5x);
		xAxis.add(line6x);
		xAxis.add(line7x);
		xAxis.add(line8x);
		stuffCont.add(xAxis);

		upperPanel.add("South", middlePanel);

		everyThingPanel.addTab(b.getString("kContourTab1"), upperPanel);
		JPanel np = new JPanel();
		np.setLayout(new BorderLayout(0, 0));
		np.add("North", stuffCont);
		everyThingPanel.addTab(b.getString("kContourTab2"), np);
		mainPanel.add("Center", new TenPixelBorder(everyThingPanel, 10, 10, 10, 10));

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mApplyButton = new JOAJButton(b.getString("kApply"));
		mApplyButton.setActionCommand("apply");
		mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
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
		mCancelButton.addActionListener(this);
		mApplyButton.addActionListener(this);

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
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	@SuppressWarnings("unused")
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
			b1 = new JOAJRadioButton(b.getString("kSequence"), mPlotSpec.getSectionType() == JOAConstants.PROFSEQUENCE);
			b2 = new JOAJRadioButton(b.getString("kDistance"), mPlotSpec.getSectionType() == JOAConstants.PROFDISTANCE);
			mOffset = mPlotSpec.getSectionType();
			b1.setActionCommand("seq");
			b2.setActionCommand("dis");
			controls.add(b2);
			controls.add(b1);
			controls.add(new JOAJLabel("       "));
			ButtonGroup bg = new ButtonGroup();
			bg.add(b1);
			bg.add(b2);
			this.add("Center", controls);
			b1.addItemListener((ItemListener) comp);
			b2.addItemListener((ItemListener) comp);
		}
	}

	public void valueChanged(ListSelectionEvent evt) {
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			this.dispose();
			timer.cancel();
		}
		else if (cmd.equals("ok")) {
			mClient.dialogDismissed(this);
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("apply")) {
			mClient.dialogApply(this);
			mOriginalPlotSpec = null;
			mOriginalPlotSpec = createPlotSpec();
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJRadioButton) {
			JOAJRadioButton rb = (JOAJRadioButton) evt.getSource();
			if (rb == b1 && evt.getStateChange() == ItemEvent.SELECTED) {
				mOffset = JOAConstants.PROFSEQUENCE;
			}
			else if (rb == b2 && evt.getStateChange() == ItemEvent.SELECTED) {
				mOffset = JOAConstants.PROFDISTANCE;
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
	}

	public void maintainButtons() {
	}

	public StnPlotSpecification createPlotSpec() {
		StnPlotSpecification ps = new StnPlotSpecification();
		// get the colors
		ps.setFGColor(axesColor.getColor());
		ps.setBGColor(plotBg.getColor());
		ps.setSectionType(mOffset);
		ps.setFileViewer(mFileViewer);
		ps.setYGrid(mPlotYGrid.isSelected());
		ps.setXGrid(mPlotXGrid.isSelected());
		ps.setReverseY(mReverseY.isSelected());
		ps.setSymbol(mCurrSymbol);
		ps.setXStnVarCode(mStnVarXCode);
		ps.setYStnVarCode(mStnVarYCode);
		ps.setSymbolSize(((Integer) mSizeField.getValue()).intValue());

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

		ps.setYTics(((Integer) yTics.getValue()).intValue());

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

		ps.setXTics(((Integer) xTics.getValue()).intValue());

		if (error) {
			// post alert
		}
		return ps;
	}

	public StnPlotSpecification getOrigPlotSpec() {
		return mOriginalPlotSpec;
	}
}

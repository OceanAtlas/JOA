/*
 * $Id: ConfigureProfilePlotDC.java,v 1.3 2005/06/17 18:08:53 oz Exp $
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

@SuppressWarnings("serial")
public class ConfigureProfilePlotDC extends JOAJDialog implements ListSelectionListener, ActionListener,
    ButtonMaintainer, ItemListener, DialogClient {
	protected FileViewer mFileViewer;
	protected Component mComp;
	protected int mSelXParam = -1;
	protected int mSelYParam = -1;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJButton mApplyButton = null;
	protected ResourceBundle b = null;
	protected int mOffset = JOAConstants.PROFSEQUENCE;
	protected JOAJCheckBox mIncludeColorBarCB = null;
	protected boolean mIncludeColorBar = false;
	protected boolean mOriginalIncludeColorBar;
	protected JOAJCheckBox mPlotAxes = null;
	protected JOAJCheckBox mPlotYGrid = null;
	protected AdvProfPlotOptionsTwo mAdvOpt = null;
	protected ProfilePlotSpecification mPlotSpec = null;
	protected ProfilePlotSpecification mOriginalPlotSpec = null;
	protected JOAJComboBox mSymbolPopup = null;
	protected int mCurrSymbol = JOAConstants.SYMBOL_CROSS1;
	protected Icon[] symbolData = null;
	protected JSpinner mSizeField = null;
	protected boolean mPlotSymbols = false;
	protected JOAJCheckBox mEnableSymbols = null;
	protected SimpleFGBGColorPicker mColorPicker = null;
	protected JOAJLabel mSizeLabel = null;
	protected double mTraceOffset;
	protected DialogClient mClient = null;
	protected boolean mRemovingColorLegend = false;
	protected boolean mAddingColorLegend = false;
	protected boolean mLayoutChanged = false;
	protected JDialog mFrame = null;
	private Timer timer = new Timer();
  protected JOAJRadioButton mPlotAllStns = null;
  protected JOAJRadioButton mAccumulateStns = null;

	public ConfigureProfilePlotDC(JOAWindow par, FileViewer fv, DialogClient client, ProfilePlotSpecification spec) {
		super(par, "Profile Plot", false);
		mFileViewer = fv;
		mPlotSpec = spec;
		mOriginalPlotSpec = new ProfilePlotSpecification(mPlotSpec);
		mClient = client;
		mSelXParam = mPlotSpec.getXVarCode();
		mSelYParam = mPlotSpec.getYVarCode();
		mOffset = spec.getSectionType();
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

		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 5));
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout(5, 5));

		// Options
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 3));

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
		JPanel line3 = new JPanel();
		line3.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mPlotAxes = new JOAJCheckBox(b.getString("kPlotAxes"), mPlotSpec.isPlotAxes());
		mPlotAxes.addItemListener(this);
		mPlotYGrid = new JOAJCheckBox(b.getString("kYGrid"), mPlotSpec.isYGrid());
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
    mPlotAllStns = new JOAJRadioButton(b.getString("kPlotAllStns"), mPlotSpec.isAccumulateStns());
    mAccumulateStns = new JOAJRadioButton(b.getString("kAccumulateStns"), !mPlotSpec.isAccumulateStns());
    p1.add(mPlotAllStns);
    p1.add(mAccumulateStns);
    otherOptions.add(p1);
    
    ButtonGroup bg0 = new ButtonGroup();
    bg0.add(mPlotAllStns);
    bg0.add(mAccumulateStns);

		// plot symbols
		JPanel line4 = new JPanel();
		line4.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		mEnableSymbols = new JOAJCheckBox(b.getString("kSymbol"), mPlotSpec.isPlotSymbols());
		mPlotSymbols = mPlotSpec.isPlotSymbols();

		mEnableSymbols.addItemListener(this);
		line4.add(mEnableSymbols);
		line4.add(mSymbolPopup);
		mSymbolPopup.addItemListener(this);
		mSizeLabel = new JOAJLabel(b.getString("kSize"));
		line4.add(mSizeLabel);

		SpinnerNumberModel model2 = new SpinnerNumberModel(mPlotSpec.getSymbolSize(), 1, 100, 1);
		mSizeField = new JSpinner(model2);

		line4.add(mSizeField);
		otherOptions.add(line4);
		if (!mPlotSpec.isPlotSymbols())
			disableSymbolStuff();

		// include NewColorBar
		JPanel line5 = new JPanel();
		line5.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		mIncludeColorBarCB = new JOAJCheckBox(b.getString("kColorLegend"), mPlotSpec.isIncludeCBAR());
		// mIncludeObsPanel = new JOAJCheckBox(b.getString("kIncludeBrowser"),
		// mPlotSpec.mIncludeObsPanel);
		mIncludeColorBar = mPlotSpec.isIncludeCBAR();
		// mIncludeObsPanel = mPlotSpec.mIncludeObsPanel;
		mOriginalIncludeColorBar = mPlotSpec.isIncludeCBAR();
		// mOriginalIncludeObsPanel = mPlotSpec.mIncludeObsPanel;
		mIncludeColorBarCB.addItemListener(this);
		line5.add(mIncludeColorBarCB);
		otherOptions.add(line5);

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

		JPanel advOptionsCont = new JPanel();
		advOptionsCont.setLayout(new BorderLayout(5, 0));
		mAdvOpt = new AdvProfPlotOptionsTwo(mFileViewer, mPlotSpec, this);
		setAdvancedValues();
		advOptionsCont.add("North", mAdvOpt);
		tb = BorderFactory.createTitledBorder(b.getString("kAdvancedOptions"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		advOptionsCont.setBorder(tb);
		middlePanel.add(advOptionsCont);

		upperPanel.add("South", middlePanel);
		mainPanel.add("Center", new TenPixelBorder(upperPanel, 10, 10, 10, 10));

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
		mApplyButton.addActionListener(this);
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
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (mSelYParam >= 0)
			setAdvancedValues();
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
		else if (cmd.equals("apply")) {
			mClient.dialogApply(this);
			mOriginalPlotSpec = null;
			mOriginalPlotSpec = createPlotSpec();
			mOriginalIncludeColorBar = mIncludeColorBarCB.isSelected();
			mRemovingColorLegend = false;
			mAddingColorLegend = false;
			mLayoutChanged = false;
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJRadioButton) {
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
			if (cb == mSymbolPopup) {
				mCurrSymbol = cb.getSelectedIndex() + 1;
			}
		}
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

	public ProfilePlotSpecification getOrigPlotSpec() {
		return mOriginalPlotSpec;
	}

	public ProfilePlotSpecification createPlotSpec() {
		ProfilePlotSpecification ps = new ProfilePlotSpecification();
		// get the colors
		ps.setFGColor(mColorPicker.getFGColor());
		ps.setBGColor(mColorPicker.getBGColor());
		// if (mIncludeColorBar && !mIncludeColorBar) {
		// ps.mWidth = mPlotSpec.mWidth + 100;
		// }

		ps.setSectionType(mOffset);
		ps.setFileViewer(mFileViewer);
		ps.setXVarCode(mSelXParam);
		ps.setYVarCode(mSelYParam);
		ps.setWinTitle(mPlotSpec.getWinTitle());
		ps.setIncludeCBAR(mIncludeColorBar);
		// ps.setIncludeObsPanel(mIncludeObsPanel);
		ps.setYGrid(mPlotYGrid.isSelected());
		ps.setPlotAxes(mPlotAxes.isSelected());
		ps.setSymbol(mCurrSymbol);
		ps.setPlotSymbols(mPlotSymbols);
		ps.setSymbolSize(((Integer) mSizeField.getValue()).intValue());

		ps.setWinYPlotMin(mAdvOpt.getYMin());
		ps.setWinYPlotMax(mAdvOpt.getYMax());
		ps.setYInc(mAdvOpt.getYInc());
		ps.setYTics(mAdvOpt.getYTics());

		ps.setWinXPlotMin(mPlotSpec.getWinXPlotMin());
		ps.setWinXPlotMax(mPlotSpec.getWinXPlotMax());

		ps.setLineWidth((int) mAdvOpt.getLineWidth());
		ps.setAmplitude(mAdvOpt.getAmplitude());
		ps.setTraceOffset(mAdvOpt.getLineSpacing());
		ps.setSecOrigin(mAdvOpt.getTranslation());
		ps.setWidth(mPlotSpec.getWidth());
		ps.setHeight(mPlotSpec.getHeight());
    ps.setAccumulateStns(mAccumulateStns.isSelected());  

		return ps;
	}

	public boolean removingColorBar() {
		return mOriginalIncludeColorBar && !mIncludeColorBar;
	}

	public boolean addingColorBar() {
		return !mOriginalIncludeColorBar && mIncludeColorBar;
	}

	public void setAdvancedValues() {
		mAdvOpt.setParameters(mSelYParam, mSelXParam);
		double tempYMin = mFileViewer.mAllProperties[mSelYParam].getPlotMin();
		double tempYMax = mFileViewer.mAllProperties[mSelYParam].getPlotMax();

		mAdvOpt.setValues(mPlotSpec.getWinYPlotMin(), mPlotSpec.getWinYPlotMax(), mPlotSpec.getYInc(),
		    mPlotSpec.getYTics(), tempYMin, tempYMax);
	}

	public void addedColorLegend() {
		mOriginalIncludeColorBar = true;
		mAddingColorLegend = false;
		mLayoutChanged = true;
	}

	public void removedColorLegend() {
		mOriginalIncludeColorBar = false;
		mRemovingColorLegend = false;
		mLayoutChanged = true;
	}

	// OK Button
	public void dialogDismissed(JDialog d) {
		;
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
		mClient.dialogApply(this);
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogApplyTwo(Object d) {
		mClient.dialogApply(this);
	}
}

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.calculations.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;

/*
 * $Id: ConfigNeutralSurfaceCalc.java,v 1.4 2005/09/07 18:49:31 oz Exp $
 *
 */

@SuppressWarnings("serial")
public class ConfigNeutralSurfaceCalc extends JOAJDialog implements ListSelectionListener, ActionListener,
    ButtonMaintainer, ItemListener {
	protected FileViewer mFileViewer;
	// Neutral Surface Controls
	protected JOAJLabel mGammaLbl = null;
	int mMethod = JOAConstants.MIXED_LAYER_DIFFERENCE;
	protected JOAJCheckBox mSalinityOnNS = null;
	protected JOAJCheckBox mTemperatureOnNS = null;
	protected JOAJCheckBox mPressureOnNS = null;
	protected JOAJCheckBox mNeutralSurfaceErrors = null;
	protected JOAJTextField mGammaOfNS = null;
	private double gamma = -99;

	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJTextField mNewParamName = null;
	protected JOAJTextField mNewParamUnits = null;
	private Timer timer = new Timer();

	public ConfigNeutralSurfaceCalc(JFrame par, FileViewer fv) {
		super(par, "Station Neutral Surface Calculations", false);
		mFileViewer = fv;
		this.init();
	}

	/*
	 * int pPos = mFileViewer.getPropertyPos("PRES", false); int sPos =
	 * mFileViewer.getPropertyPos("SALT", true); if (sPos == -1) sPos =
	 * mFileViewer.getPropertyPos("CTDS", true); int tPos =
	 * mFileViewer.getPropertyPos("TEMP", true);
	 * 
	 * if (pPos == -1 || sPos == -1 || tPos == -1) { mCantDoNS = true; }
	 * 
	 */

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		// create the two parameter chooser lists
		this.getContentPane().setLayout(new BorderLayout(5, 5));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 5));
		// Neutral Surface UI
		JPanel neutralSurfacePanel = new JPanel();
		neutralSurfacePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JPanel neutralDensCont2 = new JPanel();
		neutralDensCont2.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		JPanel ndLine1 = new JPanel();
		JPanel ndLine2 = new JPanel();
		JPanel ndLine3 = new JPanel();
		JPanel ndLine4 = new JPanel();
		JPanel ndLine5 = new JPanel();
		ndLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ndLine2.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ndLine3.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ndLine4.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ndLine5.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		mGammaLbl = new JOAJLabel(b.getString("kNeutralSurfaceGamma"));
		ndLine1.add(mGammaLbl);
		mGammaOfNS = new JOAJTextField(4);
		ndLine1.add(mGammaOfNS);
		neutralDensCont2.add(ndLine1);

		mSalinityOnNS = new JOAJCheckBox(b.getString("kSalinityOnNS"));
		ndLine2.add(new JOAJLabel("     "));
		ndLine2.add(mSalinityOnNS);
		neutralDensCont2.add(ndLine2);

		mTemperatureOnNS = new JOAJCheckBox(b.getString("kTemperatureOnNS"));
		ndLine3.add(new JOAJLabel("     "));
		ndLine3.add(mTemperatureOnNS);
		neutralDensCont2.add(ndLine3);

		mPressureOnNS = new JOAJCheckBox(b.getString("kPressureOnNS"));
		ndLine4.add(new JOAJLabel("     "));
		ndLine4.add(mPressureOnNS);
		neutralDensCont2.add(ndLine4);
		;

		mNeutralSurfaceErrors = new JOAJCheckBox(b.getString("kIncludeErrorEstimates"));
		ndLine5.add(new JOAJLabel("     "));
		ndLine5.add(mNeutralSurfaceErrors);
		neutralDensCont2.add(ndLine5);
		neutralSurfacePanel.add(neutralDensCont2);

		// construct the dialog
		JPanel mainPanelContents = new JPanel();
		mainPanelContents.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		mainPanelContents.add(new TenPixelBorder(neutralSurfacePanel, 5, 5, 5, 5));
		mainPanel.add("Center", mainPanelContents);
		this.getContentPane().add("Center", new TenPixelBorder(mainPanel, 10, 10, 10, 10));

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kOK"));
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

		this.getContentPane().add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
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
	}

	public void itemStateChanged(ItemEvent evt) {
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			// transfer pending calculations to the fileviewer for processing
			boolean error = false;
			if (!JOAFormulas.paramExists(mFileViewer, "GAMMA")) {
				if (JOAFormulas.isCalculatable("GAMMA")) {
					// make a new calculation
					Calculation calc = JOAFormulas.createCalcFromName(mFileViewer, "GAMMA");

					if (calc != null) {
						// do calculation
						mFileViewer.addCalculation(calc);
					}
				}
				else {
					JFrame f = new JFrame("Station Calculation Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "Gamma does not exist in this file" + "\n" + "and couldn't be calculated");
					error = true;
				}
			}

			if (!error) {
				// configure an neutral surface calculation
				gamma = -99;
				try {
					gamma = Double.valueOf(mGammaOfNS.getText()).doubleValue();
				}
				catch (NumberFormatException ex) {
					gamma = 0;
				}

				Calculation calc = new Calculation("NS_PLACEHOLDER", new NeutralSurfaceSpecification(gamma, mSalinityOnNS
				    .isSelected(), mTemperatureOnNS.isSelected(), mPressureOnNS.isSelected(), mNeutralSurfaceErrors
				    .isSelected()), JOAConstants.STN_CALC_TYPE);

				mFileViewer.addCalculation(calc);

				try {
					calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
				}
				catch (Exception ex) {
				}
			}

			mFileViewer.doCalcs();
			timer.cancel();
			this.dispose();
		}
	}

	public void maintainButtons() {
		// value of gamma
		gamma = -99;
		try {
			gamma = Double.valueOf(mGammaOfNS.getText()).doubleValue();
		}
		catch (NumberFormatException ex) {
		}

		boolean NSOK = (gamma != -99)
		    && ((mSalinityOnNS.isSelected() || mTemperatureOnNS.isSelected() || mPressureOnNS.isSelected()));

		if (!NSOK)
			mOKBtn.setEnabled(false);
		else
			mOKBtn.setEnabled(true);
	}
}

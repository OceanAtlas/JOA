/*
 * $Id: ConfigCalculations.java,v 1.7 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javaoceanatlas.calculations.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.utility.*;

@SuppressWarnings("serial")
public class ConfigCalculations extends JOAJDialog implements ActionListener {
	protected FileViewer mFileViewer = null;
	protected JOAJCheckBox theta = null;
	protected JOAJCheckBox sigma0 = null;
	protected JOAJCheckBox sigma1 = null;
	protected JOAJCheckBox sigma2 = null;
	protected JOAJCheckBox sigma3 = null;
	protected JOAJCheckBox sigma4 = null;
	protected JOAJCheckBox sigmax = null;
	protected JOAJCheckBox spice = null;
	protected JOAJCheckBox spice2 = null;
	protected JOAJCheckBox sva = null;
	protected JOAJCheckBox svel = null;
	protected JOAJCheckBox o2sat = null;
	protected JOAJCheckBox aou = null;
	protected JOAJCheckBox no = null;
	protected JOAJCheckBox po = null;
	protected JOAJCheckBox htso = null;
	protected JOAJCheckBox alpha = null;
	protected JOAJCheckBox alphaderiv = null;
	protected JOAJCheckBox beta = null;
	protected JOAJCheckBox betaderiv = null;
	protected JOAJCheckBox n = null;
	protected JOAJCheckBox nsqrd = null;
	protected JOAJCheckBox fnsqrdg = null;
	protected JOAJCheckBox actt = null;
	protected JOAJCheckBox nhct = null;
	protected JOAJCheckBox gpea = null;
	protected JOAJCheckBox gpan = null;
	protected JOAJCheckBox volToMass = null;
    protected JOAJButton mOKBtn = null;
    protected JOAJButton mCancelButton = null;
    protected JOAJButton mOptionsBtn = null;
    protected JOAJTextField mRefPress = null;
    protected JOAJTextField mEFolding = null;
    protected ResourceBundle b = null;
	protected JOAJCheckBox neutralDensity = null;
	protected JOAJCheckBox ndIncludeErrors = null;
	protected JOAJCheckBox cStar = null;

    public ConfigCalculations(JFrame par, FileViewer fv) {
    	super(par, "Parameter Calculations", false);
    	mFileViewer = fv;
    	init();
	}

    public void init() {
    	b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
    	Container contents = this.getContentPane();
    	this.getContentPane().setLayout(new BorderLayout(5, 5));
    	JPanel mainPanel = new JPanel();
    	mainPanel.setLayout(new BorderLayout(5, 5));
		JPanel upperCalcs = new JPanel();				// for observation calc
		upperCalcs.setLayout(new BorderLayout(5, 5));
    	TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kObservationCalculations"));
		if (JOAConstants.ISMAC) {
			//tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
    	upperCalcs.setBorder(tb);

		JPanel lowerCalcs = new JPanel();				// for integral calcs
		lowerCalcs.setLayout(new BorderLayout(5, 5));
    	tb = BorderFactory.createTitledBorder(b.getString("kIntegralCalculations"));
		if (JOAConstants.ISMAC) {
			//tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
    	lowerCalcs.setBorder(tb);

		// observation calcs (stored as individual lines in a gridlayout);
		JPanel obsContent = new JPanel();
		obsContent.setLayout(new GridLayout(9, 2, 0 ,0));

	    theta = new JOAJCheckBox(b.getString("kTheta"));
    	obsContent.add(theta);
	    spice = new JOAJCheckBox(b.getString("kSpiciness"));
    	obsContent.add(spice);
	    sigma0 = new JOAJCheckBox(b.getString("kSigma0"));
    	obsContent.add(sigma0);
	    spice2 = new JOAJCheckBox(b.getString("kSpiciness2"));
    	obsContent.add(spice2);
	    sigma1 = new JOAJCheckBox(b.getString("kSigma1"));
    	obsContent.add(sigma1);
	    svel = new JOAJCheckBox(b.getString("kSoundVelocity"));
    	obsContent.add(svel);
	    sigma2 = new JOAJCheckBox(b.getString("kSigma2"));
    	obsContent.add(sigma2);
	    o2sat = new JOAJCheckBox(b.getString("kO2Sat"));
    	obsContent.add(o2sat);
	    sigma3 = new JOAJCheckBox(b.getString("kSigma3"));
    	obsContent.add(sigma3);
	    aou = new JOAJCheckBox(b.getString("kAOU"));
    	obsContent.add(aou);
	    sigma4 = new JOAJCheckBox(b.getString("kSigma4"));
    	obsContent.add(sigma4);
	    no = new JOAJCheckBox(b.getString("kNO"));
    	obsContent.add(no);
    	JPanel line6 = new JPanel();
	    line6.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    sigmax = new JOAJCheckBox(b.getString("kSigmaRef"));
    	line6.add(sigmax);
    	mRefPress = new JOAJTextField(4);
		mRefPress.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	line6.add(mRefPress);
    	obsContent.add(line6);
	    po = new JOAJCheckBox(b.getString("kPO"));
    	obsContent.add(po);
	    sva = new JOAJCheckBox(b.getString("kSpecificVolumeAnomaly"));
    	obsContent.add(sva);
	    cStar = new JOAJCheckBox(b.getString("kcStar"));
    	obsContent.add(cStar);
	    htso = new JOAJCheckBox(b.getString("kHeatStorage"));
    	obsContent.add(htso);
    	JPanel line7 = new JPanel();
	    line7.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
	    volToMass = new JOAJCheckBox(b.getString("kVolumeToMass"), true);
    	line7.add(volToMass);
    	obsContent.add(line7);

    	JPanel lowerObsContent = new JPanel();
		lowerObsContent.setLayout(new GridLayout(1, 2, 0 ,0));

    	// buoyancy frequency
    	JPanel freq = new JPanel();
		freq.setLayout(new GridLayout(4, 1, 0 ,0));
    	tb = BorderFactory.createTitledBorder(b.getString("kBuoyancyFrequency"));
		if (JOAConstants.ISMAC) {
			//tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
    	freq.setBorder(tb);
    	JPanel line8 = new JPanel();
	    line8.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    line8.add(new JOAJLabel(b.getString("kEFolding")));
    	mEFolding = new JOAJTextField(4);
    	mEFolding.setText("3");
		mRefPress.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	line8.add(mEFolding);
	    line8.add(new JOAJLabel(b.getString("kMeters")));
    	freq.add(line8);
	    n = new JOAJCheckBox(b.getString("kN"));
    	freq.add(n);
	    nsqrd = new JOAJCheckBox(b.getString("kNSq"));
    	freq.add(nsqrd);
	    fnsqrdg = new JOAJCheckBox(b.getString("kFNSqG"));
    	freq.add(fnsqrdg);
    	JPanel freqCont = new JPanel();
		freqCont.setLayout(new BorderLayout(0,0));
		freqCont.add("Center", new TenPixelBorder(freq, 0, 0, 0, 0));
    	lowerObsContent.add(freqCont);

    	//alpha/beta
    	JPanel alphaBeta = new JPanel();
		alphaBeta.setLayout(new GridLayout(4, 1, 0 ,0));
    	tb = BorderFactory.createTitledBorder(b.getString("kAlphaBeta"));
		if (JOAConstants.ISMAC) {
			//tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
    	alphaBeta.setBorder(tb);
	    alpha = new JOAJCheckBox(b.getString("kAlpha"));
    	alphaBeta.add(alpha);
	    alphaderiv = new JOAJCheckBox(b.getString("kAlphaDeriv"));
    	alphaBeta.add(alphaderiv);
	    beta = new JOAJCheckBox(b.getString("kBeta"));
    	alphaBeta.add(beta);
	    betaderiv = new JOAJCheckBox(b.getString("kBetaDeriv"));
    	alphaBeta.add(betaderiv);
    	JPanel alphaBetaCont = new JPanel();
		alphaBetaCont.setLayout(new BorderLayout(0,0));
		alphaBetaCont.add("Center", new TenPixelBorder(alphaBeta, 0, 0, 0, 0));
    	lowerObsContent.add(alphaBetaCont);

    	upperCalcs.add("North", new TenPixelBorder(obsContent, 0, 5, 0, 5));
    	upperCalcs.add("Center", new TenPixelBorder(lowerObsContent, 0, 0, 0, 0));
		mainPanel.add("North", new TenPixelBorder(upperCalcs, 5, 5, 0, 5));

		JPanel lowerContCont = new JPanel();
		lowerContCont.setLayout(new GridLayout(1, 2, 0 ,0));

		// integral calcs
		JPanel integralCont = new JPanel();
		integralCont.setLayout(new GridLayout(4, 1, 0 ,0));
	    actt = new JOAJCheckBox(b.getString("kAcousticTravelTime"));
    	integralCont.add(actt);
	    nhct = new JOAJCheckBox(b.getString("kNetHeatContent"));
    	integralCont.add(nhct);
	    gpea = new JOAJCheckBox(b.getString("kPotentialEnergyAnomaly"));
    	integralCont.add(gpea);
	    gpan = new JOAJCheckBox(b.getString("kGeopotentialAnomaly"));
    	integralCont.add(gpan);
		lowerContCont.add(new TenPixelBorder(integralCont, 0, 5, 0, 5));

		//neutral Density calcs
		JPanel neutralDensCont = new JPanel();
		neutralDensCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
		JPanel ndLine1 = new JPanel();
		ndLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

	    neutralDensity = new JOAJCheckBox(b.getString("kNeutralDensityWUnits"));
    	neutralDensCont.add(neutralDensity);

	    ndIncludeErrors = new JOAJCheckBox(b.getString("kIncludeErrorEstimates"));
    	ndLine1.add(new JOAJLabel("     "));
    	ndLine1.add(ndIncludeErrors);
    	neutralDensCont.add(ndLine1);

    	tb = BorderFactory.createTitledBorder(b.getString("kNeutralDensity"));
		if (JOAConstants.ISMAC) {
			//tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
    	neutralDensCont.setBorder(tb);
		upperCalcs.add("South", new TenPixelBorder(neutralDensCont, 0, 0, 0, 0));

		lowerCalcs.add("Center", new TenPixelBorder(lowerContCont, 0, 5, 0, 5));
		mainPanel.add("Center", new TenPixelBorder(lowerCalcs, 0, 5, 5, 5));

		// lower panel
    	mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
    	this.getRootPane().setDefaultButton(mOKBtn);
    	mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
        dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
        dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
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

        // init state of check boxes
        initBtns();

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
		this.setLocation(x, y);
    }

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			int calcCnt = 0;
			Double dArg = null;
			// create the calculation objects
			if (theta.isSelected()) {
				dArg = new Double(0.0);
				Calculation calc = new Calculation("THTA", dArg, JOAFormulas.paramNameToJOAUnits(false, "THTA"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (sigma0.isSelected()) {
				dArg = new Double(0.0);
				Calculation calc = new Calculation("SIG0", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG0"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (sigma1.isSelected()) {
				dArg = new Double(1000.0);
				Calculation calc = new Calculation("SIG1", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG1"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (sigma2.isSelected()) {
				dArg = new Double(2000.0);
				Calculation calc = new Calculation("SIG2", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG2"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (sigma3.isSelected()) {
				dArg = new Double(3000.0);
				Calculation calc = new Calculation("SIG3", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG3"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (sigma4.isSelected()) {
				dArg = new Double(4000.0);
				Calculation calc = new Calculation("SIG4", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG4"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (sigmax.isSelected()) {
				String newVar = null;
				try {
					dArg = new Double(mRefPress.getText());
					newVar = new String("S" + mRefPress.getText().substring(0, 3));
				}
				catch (NumberFormatException ex) {
					dArg = new Double(0.0);
					newVar = new String("SIG0");
				}
				// make a new variable from the reference pressure
				Calculation calc = new Calculation(newVar, dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG4"));
				calc.setIsCustomDensity();
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (spice.isSelected()) {
				Calculation calc = new Calculation("SPCY(J&M)", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "SPCY(J&M)"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (spice2.isSelected()) {
				Calculation calc = new Calculation("SPCY(F)", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "SPCY(F)"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (sva.isSelected()) {
				Calculation calc = new Calculation("SVAN", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "SVAN"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (svel.isSelected()) {
				Calculation calc = new Calculation("SVEL", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "SVEL"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (o2sat.isSelected()) {
				Calculation calc = new Calculation("O2% ", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "O2% "));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			Boolean boolArg = new Boolean(volToMass.isSelected());
			if (aou.isSelected()) {
				Calculation calc = new Calculation("AOU ", boolArg, JOAFormulas.paramNameToJOAUnits(false, "AOU "));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (no.isSelected()) {
				Calculation calc = new Calculation("NO", boolArg, JOAFormulas.paramNameToJOAUnits(false, "NO"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (po.isSelected()) {
				Calculation calc = new Calculation("PO", boolArg, JOAFormulas.paramNameToJOAUnits(false, "PO"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (cStar.isSelected()) {
				Calculation calc = new Calculation("C*  ", boolArg, JOAFormulas.paramNameToJOAUnits(false, "C*  "));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (htso.isSelected()) {
				Calculation calc = new Calculation("HTST", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "HTST"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (alpha.isSelected()) {
				Calculation calc = new Calculation("ALPH", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "ALPH"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (beta.isSelected()) {
				Calculation calc = new Calculation("BETA", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "BETA"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (alphaderiv.isSelected()) {
				Calculation calc = new Calculation("ADRV", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "ADRV"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (betaderiv.isSelected()) {
				Calculation calc = new Calculation("BDRV", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "BDRV"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (gpan.isSelected()) {
				Calculation calc = new Calculation("GPOT", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "GPOT"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (gpea.isSelected()) {
				Calculation calc = new Calculation("PE", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "PE"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (nhct.isSelected()) {
				Calculation calc = new Calculation("HEAT", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "HEAT"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (actt.isSelected()) {
				Calculation calc = new Calculation("ACTT", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "ACTT"));
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
				calcCnt++;
			}
			if (fnsqrdg.isSelected()) {
				String newVar = null;
				try {
					String efText = mEFolding.getText();
					dArg = new Double(efText);
					newVar = new String("VT" + efText);
				}
				catch (NumberFormatException ex) {
					dArg = new Double(3.0);
					newVar = new String("VT3");
				}
				Calculation calc = new Calculation(newVar, dArg, JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "VT3"));
				calc.setIsBuoyanceFrequency();
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (nsqrd.isSelected()) {
				String newVar = null;
				try {
					String efText = mEFolding.getText();
					dArg = new Double(efText);
					newVar = new String("SB" + efText);
				}
				catch (NumberFormatException ex) {
					dArg = new Double(3.0);
					newVar = new String("SB3");
				}
				Calculation calc = new Calculation(newVar, dArg, JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "SB3"));
				calc.setIsBuoyanceFrequency();
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (n.isSelected()) {
				String newVar = null;
				try {
					String efText = mEFolding.getText();
					dArg = new Double(efText);
					newVar = new String("BV" + efText);
				}
				catch (NumberFormatException ex) {
					dArg = new Double(3.0);
					newVar = new String("BV3");
				}
				Calculation calc = new Calculation(newVar, dArg, JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "BV3"));
				calc.setIsBuoyanceFrequency();
				mFileViewer.addCalculation(calc);
				calcCnt++;
		    	try {
		    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
		    	}
		    	catch (Exception ex) {}
			}
			if (neutralDensity.isSelected()) {
				if (ndIncludeErrors.isSelected()) {
					Calculation calc = new Calculation("GAMMAWERRS", JOAConstants.INT_CALC_TYPE, null);
					mFileViewer.addCalculation(calc);
			    	try {
			    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			    	}
			    	catch (Exception ex) {}
				}
				else {
					Calculation calc = new Calculation("GAMMA", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "GAMMA"));
					mFileViewer.addCalculation(calc);
			    	try {
			    		calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			    	}
			    	catch (Exception ex) {}
				}
				calcCnt++;
			}

			// perform the calculations
			if (calcCnt > 0) {
				mFileViewer.doCalcs();
			}
			this.dispose();
		}
		else if (cmd.equals("options")) {
			;
		}
	}

	public void initBtns() {
		int pPos = mFileViewer.getPRESPropertyPos();
		int sPos = mFileViewer.getPropertyPos("SALT", true);
		if (sPos == -1)
			sPos = mFileViewer.getPropertyPos("CTDS", true);
		int tPos = mFileViewer.getPropertyPos("TEMP", true);
		if (tPos == -1)
			tPos = mFileViewer.getPropertyPos("TREV", true);
		int doPos = mFileViewer.getPropertyPos("O2", true);
		if (doPos == -1)
			doPos = mFileViewer.getPropertyPos("OXY", true);
		int nPos = mFileViewer.getPropertyPos("NO3", true);
		int poPos = mFileViewer.getPropertyPos("PO4", true);
		int tco2Pos = mFileViewer.getPropertyPos("TCO2", true);
		int talkPos = mFileViewer.getPropertyPos("TALK", true);
		
		if (tco2Pos == -1 || talkPos == -1 || doPos == -1) {
			cStar.setEnabled(false);
		}

		if (pPos == -1 || sPos == -1 || tPos == -1) {
			// disable theta and sigmasigma0 = null;
			sigma0.setEnabled(false);
			sigma1.setEnabled(false);
			sigma2.setEnabled(false);
			sigma3.setEnabled(false);
			sigma4 .setEnabled(false);
			sigmax.setEnabled(false);
			theta.setEnabled(false);
			spice.setEnabled(false);
			spice2.setEnabled(false);
			sva.setEnabled(false);
			svel.setEnabled(false);
			htso.setEnabled(false);
			alpha.setEnabled(false);
			alphaderiv.setEnabled(false);
			beta.setEnabled(false);
			betaderiv.setEnabled(false);
			n.setEnabled(false);
			nsqrd.setEnabled(false);
			fnsqrdg.setEnabled(false);
			actt.setEnabled(false);
			nhct.setEnabled(false);
			gpea.setEnabled(false);
			gpan.setEnabled(false);
			neutralDensity.setEnabled(false);
			ndIncludeErrors.setEnabled(false);
		}

		if (sPos == -1 || doPos == -1 || tPos == -1)
			aou.setEnabled(false);

		if (sPos == -1 || tPos == -1)
			o2sat.setEnabled(false);

		if (nPos == -1 || doPos == -1)
			no.setEnabled(false);

		if (poPos == -1 || doPos == -1)
			po.setEnabled(false);
		
		if (!aou.isEnabled() && !no.isEnabled() && !po.isEnabled() && !cStar.isEnabled()) {
			volToMass.setEnabled(false);
		}
	}
}

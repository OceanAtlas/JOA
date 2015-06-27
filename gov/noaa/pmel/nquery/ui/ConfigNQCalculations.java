/*
 * $Id: ConfigCalculations.java,v 1.12 2005/11/01 21:48:21 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.ui;

import javax.swing.JDialog;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.util.ResourceBundle;
import java.util.ArrayList;
import javaoceanatlas.utility.DialogClient;
import javax.swing.JFrame;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Cursor;
import javax.swing.JLabel;
import javaoceanatlas.utility.TenPixelBorder;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.Orientation;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import gov.noaa.pmel.nquery.utility.NQueryCalculation;
import javaoceanatlas.ui.widgets.JOAJDialog;

/**
 * <code>ConfigCalculations</code> UI for selecting a wide variety of Profile data claculations.
 *
 * @author oz
 * @version 1.0
 */

public class ConfigNQCalculations extends JOAJDialog implements ActionListener, ConfigNQCalcDialog {
  protected JCheckBox theta = null;
  protected JCheckBox sigma0 = null;
  protected JCheckBox sigma1 = null;
  protected JCheckBox sigma2 = null;
  protected JCheckBox sigma3 = null;
  protected JCheckBox sigma4 = null;
  protected JCheckBox sigmax = null;
  protected JCheckBox spice = null;
  protected JCheckBox sva = null;
  protected JCheckBox svel = null;
  protected JCheckBox o2sat = null;
  protected JCheckBox aou = null;
  protected JCheckBox no = null;
  protected JCheckBox po = null;
  protected JCheckBox htso = null;
  protected JCheckBox alpha = null;
  protected JCheckBox alphaderiv = null;
  protected JCheckBox beta = null;
  protected JCheckBox betaderiv = null;
  protected JCheckBox n = null;
  protected JCheckBox nsqrd = null;
  protected JCheckBox fnsqrdg = null;
  protected JCheckBox actt = null;
  protected JCheckBox nhct = null;
  protected JCheckBox gpea = null;
  protected JCheckBox gpan = null;
  protected JCheckBox volToMass = null;
  protected JButton mOKBtn = null;
  protected JButton mCancelButton = null;
  protected JButton mOptionsBtn = null;
  protected JTextField mRefPress = null;
  protected JTextField mEFolding = null;
  protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");
  protected JCheckBox neutralDensity = null;
  protected JCheckBox ndIncludeErrors = null;
  ArrayList mVarList;
  ArrayList mCalcList = new ArrayList();
  DialogClient mClient;

  public ConfigNQCalculations(JFrame par, ArrayList varList, DialogClient client) {
    super(par, "", false);
    this.setTitle(b.getString("kParameterCalculations"));
    mVarList = varList;
    mClient = client;
    init();
  }

  public void init() {
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));
    JPanel upperCalcs = new JPanel(); // for observation calc
    upperCalcs.setLayout(new BorderLayout(5, 5));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kObservationCalculations"));
    if (NQueryConstants.ISMAC) {
      tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    upperCalcs.setBorder(tb);

    JPanel lowerCalcs = new JPanel(); // for integral calcs
    lowerCalcs.setLayout(new BorderLayout(5, 5));
    tb = BorderFactory.createTitledBorder(b.getString("kIntegralCalculations"));
    if (NQueryConstants.ISMAC) {
      tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    lowerCalcs.setBorder(tb);

    // observation calcs (stored as individual lines in a gridlayout);
    JPanel obsContent = new JPanel();
    obsContent.setLayout(new GridLayout(8, 2, 0, 0));

    theta = new JCheckBox(b.getString("kTheta"));
    obsContent.add(theta);
    sva = new JCheckBox(b.getString("kSpecificVolumeAnomaly"));
    obsContent.add(sva);
    sigma0 = new JCheckBox(b.getString("kSigma0"));
    obsContent.add(sigma0);
    spice = new JCheckBox(b.getString("kSpiciness"));
    obsContent.add(spice);
    sigma1 = new JCheckBox(b.getString("kSigma1"));
    obsContent.add(sigma1);
    svel = new JCheckBox(b.getString("kSoundVelocity"));
    obsContent.add(svel);
    sigma2 = new JCheckBox(b.getString("kSigma2"));
    obsContent.add(sigma2);
    o2sat = new JCheckBox(b.getString("kO2Sat"));
    obsContent.add(o2sat);
    sigma3 = new JCheckBox(b.getString("kSigma3"));
    obsContent.add(sigma3);
    aou = new JCheckBox(b.getString("kAOU"));
    obsContent.add(aou);
    sigma4 = new JCheckBox(b.getString("kSigma4"));
    obsContent.add(sigma4);
    no = new JCheckBox(b.getString("kNO"));
    obsContent.add(no);
    JPanel line6 = new JPanel();
    line6.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    sigmax = new JCheckBox(b.getString("kSigmaRef"));
    line6.add(sigmax);
    mRefPress = new JTextField(4);
    mRefPress.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line6.add(mRefPress);
    obsContent.add(line6);
    po = new JCheckBox(b.getString("kPO"));
    obsContent.add(po);
    htso = new JCheckBox(b.getString("kHeatStorage"));
    obsContent.add(htso);
    JPanel line7 = new JPanel();
    line7.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
    volToMass = new JCheckBox(b.getString("kVolumeToMass"), true);
    line7.add(volToMass);
    obsContent.add(line7);

    JPanel lowerObsContent = new JPanel();
    lowerObsContent.setLayout(new GridLayout(1, 2, 0, 0));

    // buoyancy frequency
    JPanel freq = new JPanel();
    freq.setLayout(new GridLayout(4, 1, 0, 0));
    tb = BorderFactory.createTitledBorder(b.getString("kBuoyancyFrequency"));
    if (NQueryConstants.ISMAC) {
      tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    freq.setBorder(tb);
    JPanel line8 = new JPanel();
    line8.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    line8.add(new JLabel(b.getString("kEFolding")));
    mEFolding = new JTextField(4);
    mEFolding.setText("3");
    mRefPress.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line8.add(mEFolding);
    line8.add(new JLabel(b.getString("kMeters")));
    freq.add(line8);
    n = new JCheckBox(b.getString("kN"));
    freq.add(n);
    nsqrd = new JCheckBox(b.getString("kNSq"));
    freq.add(nsqrd);
    fnsqrdg = new JCheckBox(b.getString("kFNSqG"));
    freq.add(fnsqrdg);
    JPanel freqCont = new JPanel();
    freqCont.setLayout(new BorderLayout(0, 0));
    freqCont.add("Center", new TenPixelBorder(freq, 0, 0, 0, 0));
    lowerObsContent.add(freqCont);

    //alpha/beta
    JPanel alphaBeta = new JPanel();
    alphaBeta.setLayout(new GridLayout(4, 1, 0, 0));
    tb = BorderFactory.createTitledBorder(b.getString("kAlphaBeta"));
    if (NQueryConstants.ISMAC) {
      tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    alphaBeta.setBorder(tb);
    alpha = new JCheckBox(b.getString("kAlpha"));
    alphaBeta.add(alpha);
    alphaderiv = new JCheckBox(b.getString("kAlphaDeriv"));
    alphaBeta.add(alphaderiv);
    beta = new JCheckBox(b.getString("kBeta"));
    alphaBeta.add(beta);
    betaderiv = new JCheckBox(b.getString("kBetaDeriv"));
    alphaBeta.add(betaderiv);
    JPanel alphaBetaCont = new JPanel();
    alphaBetaCont.setLayout(new BorderLayout(0, 0));
    alphaBetaCont.add("Center", new TenPixelBorder(alphaBeta, 0, 0, 0, 0));
    lowerObsContent.add(alphaBetaCont);

    upperCalcs.add("North", new TenPixelBorder(obsContent, 0, 5, 0, 5));
    upperCalcs.add("Center", new TenPixelBorder(lowerObsContent, 0, 0, 0, 0));
    mainPanel.add("North", new TenPixelBorder(upperCalcs, 5, 5, 0, 5));

    JPanel lowerContCont = new JPanel();
    lowerContCont.setLayout(new GridLayout(1, 2, 0, 0));

    // integral calcs
    JPanel integralCont = new JPanel();
    integralCont.setLayout(new GridLayout(4, 1, 0, 0));
    actt = new JCheckBox(b.getString("kAcousticTravelTime"));
    integralCont.add(actt);
    nhct = new JCheckBox(b.getString("kNetHeatContent"));
    integralCont.add(nhct);
    gpea = new JCheckBox(b.getString("kPotentialEnergyAnomaly"));
    integralCont.add(gpea);
    gpan = new JCheckBox(b.getString("kGeopotentialAnomaly"));
    integralCont.add(gpan);
    lowerContCont.add(new TenPixelBorder(integralCont, 0, 5, 0, 5));

    //neutral Density calcs
    JPanel neutralDensCont = new JPanel();
    neutralDensCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
    JPanel ndLine1 = new JPanel();
    ndLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

    neutralDensity = new JCheckBox(b.getString("kNeutralDensityWUnits"));
    neutralDensCont.add(neutralDensity);

    ndIncludeErrors = new JCheckBox(b.getString("kIncludeErrorEstimates"));
    ndLine1.add(new JLabel("     "));
    ndLine1.add(ndIncludeErrors);
    neutralDensCont.add(ndLine1);

    tb = BorderFactory.createTitledBorder(b.getString("kNeutralDensity"));
    if (NQueryConstants.ISMAC) {
      tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    neutralDensCont.setBorder(tb);
    //upperCalcs.add("South", new TenPixelBorder(neutralDensCont, 0, 0, 0, 0));

    lowerCalcs.add("Center", new TenPixelBorder(lowerContCont, 0, 5, 0, 5));
    mainPanel.add("Center", new TenPixelBorder(lowerCalcs, 0, 5, 5, 5));

    // lower panel
    mOKBtn = new JButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JButton(b.getString("kCancel"));
    mCancelButton.setActionCommand("cancel");
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
    if (NQueryConstants.ISMAC) {
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
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
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
        mCalcList.add(new NQueryCalculation("THTA", "THTA", dArg,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "THTA"), false, false));
        calcCnt++;
      }
      if (sigma0.isSelected()) {
        dArg = new Double(0.0);
        mCalcList.add(new NQueryCalculation("SIG0", "SIG0", dArg,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SIG0"), false, false));
        calcCnt++;
      }
      if (sigma1.isSelected()) {
        dArg = new Double(1000.0);
        mCalcList.add(new NQueryCalculation("SIG1", "SIG1", dArg,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SIG1"), false, false));
        calcCnt++;
      }
      if (sigma2.isSelected()) {
        dArg = new Double(2000.0);
        mCalcList.add(new NQueryCalculation("SIG2", "SIG2", dArg,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SIG2"), false, false));
        calcCnt++;
      }
      if (sigma3.isSelected()) {
        dArg = new Double(3000.0);
        mCalcList.add(new NQueryCalculation("SIG3", "SIG3", dArg,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SIG3"), false, false));
        calcCnt++;
      }
      if (sigma4.isSelected()) {
        dArg = new Double(4000.0);
        mCalcList.add(new NQueryCalculation("SIG4", "SIG4", dArg,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SIG4"), false, false));
        calcCnt++;
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
        NQueryCalculation calc = new NQueryCalculation(newVar, "SIGWCUSTRP", dArg,
            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SIG4"), true, false);
        calc.setCustomPrompt("Reference pressure:");
        calc.setIsCustomDensity();
        mCalcList.add(calc);
        calcCnt++;
      }
      if (spice.isSelected()) {
        mCalcList.add(new NQueryCalculation("SPCY", "SPCY", NQueryConstants.OBS_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SPCY"), false, false));
        calcCnt++;
      }
      if (sva.isSelected()) {
        mCalcList.add(new NQueryCalculation("SVAN", "SVAN", NQueryConstants.OBS_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SVAN"), false, false));
        calcCnt++;
      }
      if (svel.isSelected()) {
        mCalcList.add(new NQueryCalculation("SVEL", "SVEL", NQueryConstants.OBS_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SVEL"), false, false));
        calcCnt++;
      }
      if (o2sat.isSelected()) {
        mCalcList.add(new NQueryCalculation("O2pct", "O2pct", NQueryConstants.OBS_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "O2% "), false, false));
        calcCnt++;
      }
      Boolean boolArg = new Boolean(volToMass.isSelected());
      if (aou.isSelected()) {
        mCalcList.add(new NQueryCalculation("AOU", "AOU", boolArg,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "AOU "), false, false));
        calcCnt++;
      }
      if (no.isSelected()) {
        mCalcList.add(new NQueryCalculation("NO", "NO", boolArg,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "NO"), false, false));
        calcCnt++;
      }
      if (po.isSelected()) {
        mCalcList.add(new NQueryCalculation("PO", "PO", boolArg,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "PO"), false, false));
        calcCnt++;
      }
      if (htso.isSelected()) {
        mCalcList.add(new NQueryCalculation("HTST", "HTST", NQueryConstants.OBS_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "HTST"), false, false));
        calcCnt++;
      }
      if (alpha.isSelected()) {
        mCalcList.add(new NQueryCalculation("ALPH", "ALPH", NQueryConstants.INT_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "ALPH"), false, false));
        calcCnt++;
      }
      if (beta.isSelected()) {
        mCalcList.add(new NQueryCalculation("BETA", "BETA", NQueryConstants.INT_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "BETA"), false, false));
        calcCnt++;
      }
      if (alphaderiv.isSelected()) {
        mCalcList.add(new NQueryCalculation("ADRV", "ADRV", NQueryConstants.INT_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "ADRV"), false, false));
        calcCnt++;
      }
      if (betaderiv.isSelected()) {
        mCalcList.add(new NQueryCalculation("BDRV", "BDRV", NQueryConstants.INT_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "BDRV"), false, false));
        calcCnt++;
      }
      if (gpan.isSelected()) {
        mCalcList.add(new NQueryCalculation("GPOT", "GPOT", NQueryConstants.INT_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "GPOT"), false, false));
        calcCnt++;
      }
      if (gpea.isSelected()) {
        mCalcList.add(new NQueryCalculation("PE", "PE", NQueryConstants.INT_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "PE"), false, false));
        calcCnt++;
      }
      if (nhct.isSelected()) {
        mCalcList.add(new NQueryCalculation("HEAT", "HEAT", NQueryConstants.INT_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "HEAT"), false, false));
        calcCnt++;
      }
      if (actt.isSelected()) {
        mCalcList.add(new NQueryCalculation("ACTT", "ACTT", NQueryConstants.INT_CALC_TYPE,
                                            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "ACTT"), false, false));
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
        NQueryCalculation calc = new NQueryCalculation(newVar, newVar, dArg, NQueryConstants.INT_CALC_TYPE,
            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "VT3"), true, false);
        calc.setCustomPrompt("efolding length:");
        calc.setIsBuoyanceFrequency();
        mCalcList.add(calc);
        calcCnt++;
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
        NQueryCalculation calc = new NQueryCalculation(newVar, newVar, dArg, NQueryConstants.INT_CALC_TYPE,
            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "SB3"), true, false);
        calc.setCustomPrompt("efolding length:");
        calc.setIsBuoyanceFrequency();
        mCalcList.add(calc);
        calcCnt++;
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
        NQueryCalculation calc = new NQueryCalculation(newVar, newVar, dArg, NQueryConstants.INT_CALC_TYPE,
            NQueryConstants.LEXICON.paramNameToJOAUnits(false, "BV3"), true, false);
        calc.setCustomPrompt("efolding length:");
        calc.setIsBuoyanceFrequency();
        mCalcList.add(calc);
        calcCnt++;
      }
      if (neutralDensity.isSelected()) {
        if (ndIncludeErrors.isSelected()) {
          mCalcList.add(new NQueryCalculation("GAMMAWERRS", "GAMMAWERRS", NQueryConstants.INT_CALC_TYPE, null, false, false));
        }
        else {
          mCalcList.add(new NQueryCalculation("GAMMA", "GAMMA", NQueryConstants.INT_CALC_TYPE,
                                              NQueryConstants.LEXICON.paramNameToJOAUnits(false, "GAMMA"), false, false));
        }
        calcCnt++;
      }
      mClient.dialogDismissed(this);
      this.dispose();
    }
    else if (cmd.equals("options")) {
      ;
    }
  }

  public void initBtns() {
    /*int pPos = mFileViewer.getPropertyPos("PRES", false);
       int sPos = mFileViewer.getPropertyPos("SALT", true);
       if (sPos == -1)
     sPos = mFileViewer.getPropertyPos("CTDS", true);
       int tPos = mFileViewer.getPropertyPos("TEMP", true);
       int doPos = mFileViewer.getPropertyPos("O2", true);
       if (doPos == -1)
     doPos = mFileViewer.getPropertyPos("OXY", true);
       int nPos = mFileViewer.getPropertyPos("NO3", true);
       int poPos = mFileViewer.getPropertyPos("PO4", true);

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
     po.setEnabled(false);*/
  }

  public Object getCalculation() {
    return mCalcList;
  }

  public Object getSpecification() {
    return null;
  }
}

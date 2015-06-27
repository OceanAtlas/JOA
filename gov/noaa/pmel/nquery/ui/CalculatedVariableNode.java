/*
 * $Id: CalculatedVariableNode.java,v 1.2 2004/09/14 19:11:26 oz Exp $
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

import javaoceanatlas.utility.DialogClient;
import gov.noaa.pmel.eps2.CalculatedVariable;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JDialog;
import gov.noaa.pmel.nquery.utility.NQueryCalculation;
import gov.noaa.pmel.nquery.specifications.NQIntegrationSpecification;
import gov.noaa.pmel.nquery.specifications.NQInterpolationSpecification;
import gov.noaa.pmel.nquery.specifications.NQMixedLayerCalcSpec;

public class CalculatedVariableNode extends VariableNode implements DialogClient {
  CalculatedVariable mCalcVar;
  ArrayList mGlobalVars;

  public CalculatedVariableNode(JFrame par, ArrayList vars, CalculatedVariable cvar) {
    super(par, cvar.getVarName(), null, cvar.getVarUnits(), null, cvar.isEditable(), false);
    mCalcVar = cvar;
    mGlobalVars = vars;
  }

  public CalculatedVariable getCalcVar() {
    return mCalcVar;
  }

  // OK Button
  public void dialogDismissed(JDialog d) {
    // need to store changes to the calculated variable here and specification
    NQueryCalculation exitingCalc = (NQueryCalculation)mCalcVar.getUserObject();
    NQueryCalculation newCalc = (NQueryCalculation)(((ConfigNQCalcDialog)d).getCalculation());
    Object newSpec = ((ConfigNQCalcDialog)d).getSpecification();

    newCalc.setArg(newSpec);
    mCalcVar.setUserObject(newCalc);

    // update the node's display
    mVariableName = newCalc.getCalcType();
    varUnits = newCalc.getUnits();
    mParentFrame.repaint();
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
    ;
  }

  // Apply button, OK w/o dismissing the dialog
  public void dialogApplyTwo(Object d) {
    ;
  }

  public void configureVariable() {
    NQueryCalculation calc = (NQueryCalculation)mCalcVar.getUserObject();
    if (!calc.isEditable()) {
      return;
    }

    // call the appropriate setup dialog
    try {
      double d = calc.getArgAsDouble();
      String prompt = calc.getCustomPrompt();
      ConfigNQSimpleCalculation config = new ConfigNQSimpleCalculation(mParentFrame, calc, prompt, d, this);
      config.setVisible(true);
    }
    catch (ClassCastException cce) {
      try {
        NQIntegrationSpecification spec = calc.getArgAsIntegrationSpecification();
        ConfigNQIntegrationCalc customDialog = new ConfigNQIntegrationCalc(mParentFrame, mGlobalVars, this);
        customDialog.setSpecification(calc, spec);
        customDialog.pack();
        customDialog.setVisible(true);
      }
      catch (ClassCastException cce1) {
        try {
          NQInterpolationSpecification spec = calc.getArgAsInterpolationSpecification();
          ConfigNQInterpolationCalc customDialog = new ConfigNQInterpolationCalc(mParentFrame, mGlobalVars, this);
          customDialog.setSpecification(calc, spec);
          customDialog.pack();
          customDialog.setVisible(true);
        }
        catch (ClassCastException cce2) {
          try {
            NQMixedLayerCalcSpec spec = calc.getArgAsMixedLayerCalcSpec();
            ConfigNQMLDCalc customDialog = new ConfigNQMLDCalc(mParentFrame, mGlobalVars, this);
            customDialog.setSpecification(calc, spec);
            customDialog.pack();
            customDialog.setVisible(true);
          }
          catch (Exception ex) {

          }
        }
      }
    }
  }
}

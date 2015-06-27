/**
 * 
 */
package javaoceanatlas.ui;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javaoceanatlas.classicdatamodel.Bottle;
import javaoceanatlas.resources.JOAConstants;

/**
 * @author oz
 *
 */
public class TSModelTermUI implements DocumentListener {
	private double mConstant = 0.0;
	private double mExponent = 1.0;
	TSModelTermParameter mParam = TSModelTermParameter.TEMPERATURE;
	JPanel mEditorPanel;
	JTextField mConstantField;
	JComboBox mParameterChooser;
	JComboBox mExponentChooser;
	ConfigureTSRelationship mParent;
	
	public TSModelTermUI(ConfigureTSRelationship par) {
		this(par, 0.0, 1.0, TSModelTermParameter.TEMPERATURE);
	}
	
	public TSModelTermUI(ConfigureTSRelationship par, double c, double exp, TSModelTermParameter param) {
		mParent = par;
		mConstant = c;
		mExponent = exp;
		mParam = param;

    Vector<String> mParamChoices = new Vector<String>();
    mParamChoices.add("T");
    mParamChoices.add("sqT");
    mParamChoices.add("lon");
    mParamChoices.add("lat");
    mParamChoices.add("ln*lt");
    
    Vector<String> mExpChoices = new Vector<String>();
    mExpChoices.add("1");
    mExpChoices.add("2");
    mExpChoices.add("3");
    mExpChoices.add("4");
    mExpChoices.add("5");
    mExpChoices.add("6");
    mExpChoices.add("7");
    mExpChoices.add("8");
    mExpChoices.add("9");
    mExpChoices.add("10");
    
    mParameterChooser = new JComboBox(mParamChoices);

    mParameterChooser.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
		  	mParent.updatePreview();
			}
		});
    mExponentChooser = new JComboBox(mExpChoices);
    mExponentChooser.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
		  	mParent.updatePreview();
			}
		});
    mConstantField = new JTextField(5);
    mConstantField.getDocument().addDocumentListener(this);
    mEditorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    mEditorPanel.add(new JLabel("+"));
    mEditorPanel.add(mConstantField);
    mEditorPanel.add(mParameterChooser);
    mEditorPanel.add(new JLabel("^"));
    mEditorPanel.add(mExponentChooser);
	}
	
	public TSModelTermParameter getParam() {
		if (((String)mParameterChooser.getSelectedItem()).equalsIgnoreCase("T"))
			return TSModelTermParameter.TEMPERATURE;
		else if (((String)mParameterChooser.getSelectedItem()).equalsIgnoreCase("sqT"))
			return TSModelTermParameter.SQTEMPERATURE;
		else if (((String)mParameterChooser.getSelectedItem()).equalsIgnoreCase("lon"))
			return TSModelTermParameter.LONGITUDE;
		else if (((String)mParameterChooser.getSelectedItem()).equalsIgnoreCase("lat"))
			return TSModelTermParameter.LATITUDE;
		else 
			return TSModelTermParameter.LONxLAT;
	}
	
	public void setUI() {
		mConstantField.setText(String.valueOf(mConstant));
		mConstantField.setCaretPosition(0);
		mParameterChooser.setSelectedIndex(mParam.getOrd());
		mExponentChooser.setSelectedIndex((int)mExponent-1);
	}
	
	public double evaluate(Bottle bh) {
		double termVal = bh.getIndependentValue(mParam);
		if (termVal != JOAConstants.MISSINGVALUE) {
			return mConstant * Math.pow(termVal, mExponent);
		}
		return JOAConstants.MISSINGVALUE;
	}
	
	public String toString() {
		String formulaString = " + ";
		formulaString += mConstantField.getText();
		formulaString += mParameterChooser.getSelectedItem();
		if (mExponentChooser.getSelectedIndex() >  0) {
			formulaString += "<sup>";
			formulaString += mExponentChooser.getSelectedItem();
			formulaString += "</sup>";
		}
		return formulaString;
	}
	
	public void drawFormattedTerm(Graphics g) {
		
	}
	
	public JPanel getEditor() {
		return mEditorPanel;
	}

  public void changedUpdate(DocumentEvent e) {
  	mParent.updatePreview();
  }

  public void insertUpdate(DocumentEvent e) {
  	mParent.updatePreview();
  }

  public void removeUpdate(DocumentEvent e) {
  	mParent.updatePreview();
  }

	public void setConstant(double mConstant) {
	  this.mConstant = mConstant;
  }

	public String getConstant() {
	  return mConstantField.getText();
  }

	public double getDConstant() {
		try {
			return Double.valueOf(mConstantField.getText()).doubleValue();
		}
		catch (Exception ex) {
			return Double.NaN;
		}
  }

	public void setExponent(double mExponent) {
	  this.mExponent = mExponent;
  }

	public String getExponent() {
	  return (String)mExponentChooser.getSelectedItem();
  }

	public double getDExponent() {
	  return (double)(mExponentChooser.getSelectedIndex() + 1);
  }
}

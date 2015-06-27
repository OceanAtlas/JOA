/*
 * $Id: CutPanelGUI.java,v 1.3 2005/02/15 18:31:08 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.border.*;
import gov.noaa.pmel.swing.*;
import java.util.*;


 /**
 *
 *
 * @author  Chris Windsor modified by oz
 */
public class CutPanelGUI extends JPanel implements ChangeListener, ItemListener {
	ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
   JPanel cutPanelPanel = new JPanel();
   JPanel sliderPanel = new JPanel();
   JSlider cutPanelSlider;
   JPanel selectionLeverPanel = new JPanel();
   JCheckBox independentHandles = new JCheckBox();
   JCheckBox displayAxes = new JCheckBox();
   TitledBorder cutPanelTitleBorder = new TitledBorder(b.getString("kChoosePrefs"));
   TitledBorder sliderTitleBorder = new TitledBorder(b.getString("kPanelSize"));
   EmptyBorder emptyBorder5Pixels = new EmptyBorder(5, 5, 5, 5);
   CompoundBorder sliderCompoundBorder = new CompoundBorder(emptyBorder5Pixels,sliderTitleBorder);
   GridBagLayout gbl = new GridBagLayout();
   GridBagConstraints gbc = new GridBagConstraints();

   public CutPanelGUI() {
		this.setLayout(gbl);
		setGbcCenter();
		this.add(cutPanelPanel, gbc);
		cutPanelPanel.setAlignmentX(0.498801F);
		cutPanelPanel.setBorder(cutPanelTitleBorder);
		cutPanelPanel.setLayout(new BoxLayout(cutPanelPanel,BoxLayout.Y_AXIS));
		
		sliderPanel.setBorder(sliderCompoundBorder);
		sliderPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5, 5));
		cutPanelPanel.add(sliderPanel);
		
		int val = Constants.USERSETTINGS.getCutPanelSize().width;
		val = Math.max(val, Constants.USERSETTINGS.getCutPanelMinSize());
		val = Math.min(val, Constants.USERSETTINGS.getCutPanelMaxSize());
		cutPanelSlider = new JSlider(Constants.USERSETTINGS.getCutPanelMinSize(), Constants.USERSETTINGS.getCutPanelMaxSize(), val);
		cutPanelSlider.setMinorTickSpacing(25);
		cutPanelSlider.setPaintLabels(true);
		cutPanelSlider.setSnapToTicks(true);
		cutPanelSlider.setPaintTicks(true);
		cutPanelSlider.setMajorTickSpacing(100);
		sliderPanel.add(new NPixelBorder(cutPanelSlider, 0, 0, 0, 0));
		cutPanelSlider.setForeground(java.awt.Color.darkGray);
		cutPanelSlider.setFont(BeanFonts.sliderFont);
		
		selectionLeverPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		cutPanelPanel.add(selectionLeverPanel);
		independentHandles.setText(b.getString("kIndHandles"));
		independentHandles.setActionCommand("ind");
		independentHandles.setSelected(Constants.USERSETTINGS.isIndependentHandles());
		independentHandles.addItemListener(this);
		selectionLeverPanel.add(independentHandles);
		independentHandles.setFont(BeanFonts.miFont);
		displayAxes.setText(b.getString("kDisplayAxes"));
		displayAxes.setActionCommand("Display axes");
		displayAxes.setFont(BeanFonts.miFont);
		displayAxes.setSelected(Constants.USERSETTINGS.isDisplayAxes());
		selectionLeverPanel.add(displayAxes);
		
		cutPanelTitleBorder.setTitleFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));
		cutPanelTitleBorder.setTitleColor(java.awt.Color.black);
		
		sliderTitleBorder.setTitleFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));
		sliderTitleBorder.setTitleColor(java.awt.Color.black);
		cutPanelSlider.addChangeListener(this);
		displayAxes.addItemListener(this);
   }

   public void stateChanged(ChangeEvent event) {
   }
   
   public Dimension getCutPanelSize() {
   	return new Dimension(cutPanelSlider.getValue(), cutPanelSlider.getValue());
   }
   
   public boolean isDisplayAxes() {
   	return displayAxes.isSelected();
   }
   
   public boolean isIndependentHandles() {
   	return independentHandles.isSelected();
   }
   
	public void itemStateChanged(ItemEvent event) {
	}

   private void setGbcCenter() {
      gbc.gridx = 0; 
      gbc.gridy = 0;
      gbc.gridwidth = 1; 
      gbc.gridheight = 1;
      gbc.weightx = 0.0; 
      gbc.weighty = 0.0;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.fill = GridBagConstraints.NONE;
      //gbc.insets = new Insets(0,0,0,0);
      gbc.ipadx = 0; 
      gbc.ipady = 0;
   }

   public static void main(String args[]) {

      JFrame fr = new JFrame();
      CutPanelGUI vg = new CutPanelGUI();
      fr.setVisible(true);
      fr.setSize(500, 500);
      vg.setVisible(true);

      fr.getContentPane().add(vg);
      fr.invalidate();
      fr.validate();
   }
}

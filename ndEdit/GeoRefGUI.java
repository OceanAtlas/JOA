/*
 * $Id: GeoRefGUI.java,v 1.3 2005/02/15 18:31:09 oz Exp $
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
import javax.swing.border.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class GeoRefGUI extends JPanel {

   JPanel geoRefPanel = new JPanel();
   JPanel refLonInsidePanel = new JPanel();
   JTextField refLonTextField = new JTextField(10);
   JLabel degreesLbl = new JLabel();
   EmptyBorder emptyBorder15Pixels = new EmptyBorder(15,15,15,15);
   TitledBorder geoRefTitleBorder = new TitledBorder(
		"Choose Reference Longitude:");

   TitledBorder sliderTitleBorder = new TitledBorder("Panel Size:  ");

   GridBagLayout gbl = 			new GridBagLayout();
   GridBagConstraints gbc = 		new GridBagConstraints();

   public GeoRefGUI() {
      this.setLayout(gbl);
      setGbcCenter();
      this.add(geoRefPanel, gbc);

      geoRefPanel.setAlignmentY(0.469388F);
      geoRefPanel.setBorder(geoRefTitleBorder);
      geoRefPanel.setLayout(new BoxLayout(geoRefPanel,BoxLayout.X_AXIS));
      refLonInsidePanel.setAlignmentY(0.473684F);
      refLonInsidePanel.setBorder(emptyBorder15Pixels);
      refLonInsidePanel.setLayout(new BoxLayout(refLonInsidePanel,BoxLayout.X_AXIS));
      geoRefPanel.add(refLonInsidePanel);
      refLonInsidePanel.add(refLonTextField);
      degreesLbl.setText(" degrees");
      refLonInsidePanel.add(degreesLbl);
      degreesLbl.setForeground(java.awt.Color.black);
      degreesLbl.setFont(BeanFonts.miFont);
      geoRefTitleBorder.setTitleFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));
      geoRefTitleBorder.setTitleColor(java.awt.Color.black);
       
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

   //---------------------------------------------------------------------
   //
   public static void main(String args[]) {

      JFrame fr = new JFrame();
      GeoRefGUI vg = new GeoRefGUI();
      fr.setVisible(true);
      fr.setSize(500, 500);
      vg.setVisible(true);

      fr.getContentPane().add(vg);
      fr.invalidate();
      fr.validate();
   }
}

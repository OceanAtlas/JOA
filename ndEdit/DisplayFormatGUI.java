/*
 * $Id: DisplayFormatGUI.java,v 1.3 2005/02/15 18:31:09 oz Exp $
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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

 /**
 *
 *
 * @author  Chris Windsor and oz
 * @version 1.0 01/13/00
 */

public class DisplayFormatGUI extends JPanel implements ActionListener {
   JPanel displayFormatPanel = new JPanel();
   JPanel displayFormatLblPanel = new JPanel();
   JLabel geoLbl = new JLabel();
   JLabel timeLbl = new JLabel();
   JPanel displayFormatComboPanel = new JPanel();
   JComboBox displayFormatComboBox = new JComboBox();
   JComboBox timeFormatComboBox = new JComboBox();
   TitledBorder displayFormatTitleBorder = new TitledBorder("Choose Display Format preferences:");
   EmptyBorder emptyBorder5Pixels = new EmptyBorder(5,5,5,5);
   GridBagLayout gbl = new GridBagLayout();
   GridBagConstraints gbc = new GridBagConstraints();

   public DisplayFormatGUI() {
      this.setLayout(gbl);
      setGbcCenter();
      this.add(displayFormatPanel, gbc);

      displayFormatPanel.setAlignmentY(0.484375F);
      displayFormatPanel.setBorder(displayFormatTitleBorder);
      displayFormatPanel.setLayout(new BoxLayout(displayFormatPanel,BoxLayout.X_AXIS));
      displayFormatLblPanel.setAlignmentY(0.485714F);
      displayFormatLblPanel.setAlignmentX(0.0F);
      displayFormatLblPanel.setBorder(emptyBorder5Pixels);
      displayFormatLblPanel.setLayout(new BoxLayout(displayFormatLblPanel,BoxLayout.Y_AXIS));
      displayFormatPanel.add(displayFormatLblPanel);

      geoLbl.setText("Geographic:");
      geoLbl.setBorder(emptyBorder5Pixels);
      displayFormatLblPanel.add(geoLbl);

      geoLbl.setForeground(java.awt.Color.black);
      geoLbl.setFont(BeanFonts.miFont);
      //geoLbl.setBounds(10,5,87,25);

      timeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
      timeLbl.setText("Time:         ");
      timeLbl.setBorder(emptyBorder5Pixels);

      displayFormatLblPanel.add(timeLbl);
      timeLbl.setForeground(java.awt.Color.black);
      timeLbl.setFont(BeanFonts.miFont);
      //timeLbl.setBounds(10,30,78,25);

      displayFormatComboPanel.setAlignmentY(0.485714F);
      displayFormatComboPanel.setBorder(emptyBorder5Pixels);
      displayFormatComboPanel.setLayout(new BoxLayout(displayFormatComboPanel,BoxLayout.Y_AXIS));
      displayFormatPanel.add(displayFormatComboPanel);

      //displayFormatComboPanel.setBounds(112,20,150,64);
      displayFormatComboPanel.add(displayFormatComboBox);

      displayFormatComboBox.setFont(BeanFonts.miFont);
      //displayFormatComboBox.setBounds(10,5,130,27);

      displayFormatComboPanel.add(timeFormatComboBox);

      timeFormatComboBox.setFont(BeanFonts.miFont);
      //timeFormatComboBox.setBounds(10,32,130,27);

      displayFormatTitleBorder.setTitleFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));
      displayFormatTitleBorder.setTitleColor(java.awt.Color.black);

      displayFormatComboBox.addItem("154 43' 0\" E");
      displayFormatComboBox.addItem("159.717 E");
      displayFormatComboBox.setSelectedIndex(Constants.USERSETTINGS.getGeoDisplayFormat());

      timeFormatComboBox.addItem("1998-06-07");
      timeFormatComboBox.addItem("1998-158");
      timeFormatComboBox.addItem("Months");
      timeFormatComboBox.addItem("Season");
      timeFormatComboBox.setSelectedIndex(Constants.USERSETTINGS.getTimeDisplayFormat());

      displayFormatComboBox.addActionListener(this);
      timeFormatComboBox.addActionListener(this);
   }
   
   public int getGeoDisplayFormat() {
   		return displayFormatComboBox.getSelectedIndex();
   }
   
   public int getTimeDisplayFormat() {
   		return timeFormatComboBox.getSelectedIndex();
   }

   public void actionPerformed(ActionEvent e) {
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
      DisplayFormatGUI vg = new DisplayFormatGUI();
      fr.setVisible(true);
      fr.setSize(300, 300);
      vg.setVisible(true);

      fr.getContentPane().add(vg);
      fr.invalidate();
      fr.validate();
   }
} 

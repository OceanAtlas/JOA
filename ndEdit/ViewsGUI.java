/*
 * $Id: ViewsGUI.java,v 1.3 2005/02/15 18:31:11 oz Exp $
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
import javax.swing.Box;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class ViewsGUI extends JPanel implements MouseListener, ItemListener {
   JPanel viewsPanel = new JPanel();
   JPanel viewsLeftBox = new JPanel();
   JPanel viewsRightBox = new JPanel();
   JPanel leftCheckboxes = new JPanel();
   JPanel rightCheckboxes = new JPanel();
   JCheckBoxTagged latLonCheck =   new JCheckBoxTagged(LatLonConstants.intValue);
   JCheckBoxTagged lonDepthCheck = new JCheckBoxTagged(LonDepthConstants.intValue);
   JCheckBoxTagged lonTimeCheck =  new JCheckBoxTagged(LonTimeConstants.intValue);
   JCheckBoxTagged latDepthCheck = new JCheckBoxTagged(LatDepthConstants.intValue);
   JCheckBoxTagged latTimeCheck =  new JCheckBoxTagged(LatTimeConstants.intValue);
   JCheckBoxTagged depthTimeCheck = new JCheckBoxTagged(DepthTimeConstants.intValue);
   JCheckBoxTagged[] checks = new JCheckBoxTagged[] {
			latLonCheck, 
			lonDepthCheck, 
			lonTimeCheck, 
			latDepthCheck, 
			latTimeCheck,
			depthTimeCheck };

   JPanel leftLbls = new JPanel();
   JPanel rightLbls = new JPanel();
   JPanel leftIcons = new JPanel();
   JPanel rightIcons = new JPanel();

   JLabel latLonLbl = new JLabel();
   JLabel lonDepthLbl = new JLabel();
   JLabel lonTimeLbl = new JLabel();
   JLabel latDepthLbl = new JLabel();
   JLabel latTimeLbl = new JLabel();
   JLabel depthTimeLbl = new JLabel();

   JLabel latLonIconLbl = new JLabel();
   JLabel lonDepthIconLbl = new JLabel();
   JLabel lonTimeIconLbl = new JLabel();
   JLabel latDepthIconLbl = new JLabel();
   JLabel latTimeIconLbl = new JLabel();
   JLabel depthTimeIconLbl = new JLabel();

   ImageIcon latLonIcon = new ImageIcon();
   ImageIcon lonDepthIcon = new ImageIcon();
   ImageIcon lonTimeIcon = new ImageIcon();
   ImageIcon latDepthIcon = new ImageIcon();
   ImageIcon latTimeIcon = new ImageIcon();
   ImageIcon depthTimeIcon = new ImageIcon();

   TitledBorder viewsTitleBorder = new TitledBorder("Select Visible Views:");
   EmptyBorder emptyBorder5Pixels = new EmptyBorder(5,5,5,5);

   GridBagLayout gbl = new GridBagLayout();
   GridBagConstraints gbc = new GridBagConstraints();

   //---------------------------------------------------------------------
   //
   public ViewsGUI() {
      this.setLayout(gbl);
      setGbcCenter();
      this.add(viewsPanel, gbc);
      

      viewsPanel.setAlignmentY(0.484536F);
      viewsPanel.setBorder(viewsTitleBorder);
      viewsPanel.setLayout(new BoxLayout(viewsPanel, BoxLayout.X_AXIS));
      //viewsPanel.setBackground(new java.awt.Color(204, 204, 204));
      //viewsPanel.setBounds(25, 304, 348, 122);
      viewsPanel.setSize(new Dimension(348, 122));
      viewsPanel.setPreferredSize(new Dimension(348, 122));
 
      viewsTitleBorder.setTitleFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));
      viewsTitleBorder.setTitleColor(java.awt.Color.black);

      viewsLeftBox.setAlignmentY(0.494253F);
      viewsLeftBox.setBorder(emptyBorder5Pixels);
      viewsLeftBox.setLayout(new BoxLayout(viewsLeftBox, BoxLayout.X_AXIS));

      viewsPanel.add(viewsLeftBox);

      viewsLeftBox.setBounds(5, 20, 182, 97);

      //for (int i = 0; i < checks.length; i++) {
      //    checks[i].setBackground(new java.awt.Color(204, 204, 204));
      //}

      leftCheckboxes.setAlignmentX(0.0F);
      leftCheckboxes.setLayout(new GridLayout(3, 1, 0, 0));
      viewsLeftBox.add(leftCheckboxes);
      leftCheckboxes.setBounds(10, 5, 21, 87);

      leftCheckboxes.add(latLonCheck);
      leftCheckboxes.add(lonDepthCheck);
      leftCheckboxes.add(lonTimeCheck);

      leftLbls.setAlignmentX(0.0F);
      leftLbls.setLayout(new GridLayout(3, 1, 0, 0));

      viewsLeftBox.add(leftLbls);
      leftLbls.setBounds(31, 5, 112, 87);
      latLonLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      latLonLbl.setText("Latitude / Longitude");
      leftLbls.add(latLonLbl);

      latLonLbl.setForeground(java.awt.Color.black);
      latLonLbl.setFont(BeanFonts.miFont);
      latLonLbl.setBounds(0, 0, 112, 29);
      lonDepthLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      lonDepthLbl.setText("Longitude / Depth     ");
      leftLbls.add(lonDepthLbl);

      lonDepthLbl.setForeground(java.awt.Color.black);
      lonDepthLbl.setFont(BeanFonts.miFont);
      lonDepthLbl.setBounds(0, 29, 112, 29);
      lonTimeLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      lonTimeLbl.setText("Longitude / Time     ");
      leftLbls.add(lonTimeLbl);

      lonTimeLbl.setForeground(java.awt.Color.black);
      lonTimeLbl.setFont(BeanFonts.miFont);
      lonTimeLbl.setBounds(0, 58, 112, 29);
      leftIcons.setAlignmentX(0.0F);
      leftIcons.setLayout(new GridLayout(3, 1, 0, 0));
      viewsLeftBox.add(leftIcons);

      leftIcons.setBounds(143, 5, 29, 87);
      latLonIconLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      latLonIconLbl.setVerticalAlignment(SwingConstants.BOTTOM);
      leftIcons.add(latLonIconLbl);

      latLonIconLbl.setBounds(0, 0, 29, 29);
      lonDepthIconLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      lonDepthIconLbl.setVerticalAlignment(SwingConstants.BOTTOM);
      leftIcons.add(lonDepthIconLbl);

      lonDepthIconLbl.setBounds(0, 29, 29, 29);
      lonTimeIconLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      lonTimeIconLbl.setVerticalAlignment(SwingConstants.BOTTOM);
      leftIcons.add(lonTimeIconLbl);

      lonTimeIconLbl.setBounds(0, 58, 29, 29);
      viewsRightBox.setAlignmentY(0.494253F);
      viewsRightBox.setBorder(emptyBorder5Pixels);
      viewsRightBox.setLayout(new BoxLayout(viewsRightBox, BoxLayout.X_AXIS));
      viewsPanel.add(viewsRightBox);
      viewsRightBox.setBounds(187, 20, 156, 97);

      rightCheckboxes.setAlignmentX(0.0F);
      rightCheckboxes.setLayout(new GridLayout(3, 1, 0, 0));
      viewsRightBox.add(rightCheckboxes);
      rightCheckboxes.setBounds(10, 5, 21, 87);


      rightCheckboxes.add(latDepthCheck);
      rightCheckboxes.add(latTimeCheck);
      rightCheckboxes.add(depthTimeCheck);

      rightLbls.setAlignmentX(0.0F);
      rightLbls.setLayout(new GridLayout(3, 1, 0, 0));
      viewsRightBox.add(rightLbls);

      rightLbls.setBounds(31, 5, 86, 87);
      latDepthLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      latDepthLbl.setText("Latitude / Depth");
      rightLbls.add(latDepthLbl);

      latDepthLbl.setForeground(java.awt.Color.black);
      latDepthLbl.setFont( BeanFonts.miFont);
      latDepthLbl.setBounds(0, 0, 86, 29);
      latTimeLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      latTimeLbl.setText("Latitude / Time");
      rightLbls.add(latTimeLbl);

      latTimeLbl.setForeground(java.awt.Color.black);
      latTimeLbl.setFont(BeanFonts.miFont);
      latTimeLbl.setBounds(0, 29, 86, 29);
      depthTimeLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      depthTimeLbl.setText("Depth / Time");
      rightLbls.add(depthTimeLbl);

      depthTimeLbl.setForeground(java.awt.Color.black);
      depthTimeLbl.setFont(BeanFonts.miFont);
      depthTimeLbl.setBounds(0, 58, 86, 29);
      rightIcons.setAlignmentX(0.0F);
      rightIcons.setLayout(new GridLayout(3, 1, 0, 0));
      viewsRightBox.add(rightIcons);

      rightIcons.setBounds(117, 5, 29, 87);
      latDepthIconLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      latDepthIconLbl.setVerticalAlignment(SwingConstants.BOTTOM);
      rightIcons.add(latDepthIconLbl);

      latDepthIconLbl.setBounds(0, 0, 29, 29);
      latTimeIconLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      latTimeIconLbl.setVerticalAlignment(SwingConstants.BOTTOM);
      rightIcons.add(latTimeIconLbl);

      latTimeIconLbl.setBounds(0, 29, 29, 29);
      depthTimeIconLbl.setHorizontalTextPosition(SwingConstants.LEFT);
      depthTimeIconLbl.setVerticalAlignment(SwingConstants.BOTTOM);
      rightIcons.add(depthTimeIconLbl);

      depthTimeIconLbl.setBounds(0, 58, 29, 29);

        try {
	        latLonIcon = new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/xyCyan.gif"));
	        lonDepthIcon = new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/yzCyan.gif"));
	        lonTimeIcon = new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/ytCyan.gif"));
	        latDepthIcon = new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/xzCyan.gif"));
	        latTimeIcon = new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/xtCyan.gif"));
	        depthTimeIcon = new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/ztCyan.gif"));
        }
        catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      	System.out.println("ViewsGUI:ctor");
        }

      lonTimeIconLbl.setIcon(lonTimeIcon);
      depthTimeIconLbl.setIcon(depthTimeIcon);
      latTimeIconLbl.setIcon(latTimeIcon);
      lonDepthIconLbl.setIcon(lonDepthIcon);
      latDepthIconLbl.setIcon(latDepthIcon);
      latLonIconLbl.setIcon(latLonIcon);

      //
      // Add Listeners
      //

      latLonCheck.addItemListener(this);
      lonDepthCheck.addItemListener(this);
      lonTimeCheck.addItemListener(this);
      latDepthCheck.addItemListener(this);
      latTimeCheck.addItemListener(this);
      depthTimeCheck.addItemListener(this);

      latLonLbl.addMouseListener(this);
      lonDepthLbl.addMouseListener(this);
      lonTimeLbl.addMouseListener(this);
      latDepthLbl.addMouseListener(this);
      latTimeLbl.addMouseListener(this);
      depthTimeLbl.addMouseListener(this);

      latLonIconLbl.addMouseListener(this);
      lonDepthIconLbl.addMouseListener(this);
      lonTimeIconLbl.addMouseListener(this);
      latDepthIconLbl.addMouseListener(this);
      latTimeIconLbl.addMouseListener(this);
      depthTimeIconLbl.addMouseListener(this);

	latLonCheck.setSelected(false);
	latDepthCheck.setSelected(false);
	latTimeCheck.setSelected(false);
	lonDepthCheck.setSelected(false);
	lonTimeCheck.setSelected(false);
	depthTimeCheck.setSelected(false);

     // "Check" those that are on
     //
     int [] vids = Constants.USERSETTINGS.getVisibleViewIds();
     for (int i = 0; i < vids.length; i++) {
        if (vids[i] == Constants.LAT_LON) latLonCheck.setSelected(true);
        else if (vids[i] == Constants.LAT_DEPTH) latDepthCheck.setSelected(true);
        else if (vids[i] == Constants.LAT_TIME) latTimeCheck.setSelected(true);
        else if (vids[i] == Constants.LON_DEPTH) lonDepthCheck.setSelected(true);
        else if (vids[i] == Constants.LON_TIME) lonTimeCheck.setSelected(true);
        else if (vids[i] == Constants.DEPTH_TIME) depthTimeCheck.setSelected(true);
     }
   }

   //---------------------------------------------------------------------
   //
   public void itemStateChanged(ItemEvent e) {
      JCheckBoxTagged cb = (JCheckBoxTagged) e.getItemSelectable();
      String item =
          ((JCheckBoxTagged)e.getItemSelectable()).getActionCommand();
      boolean selected = (e.getStateChange() == ItemEvent.SELECTED);

      //Constants.USERSETTINGS.setVisibleViewIds(checksToViewIds());
   }

   //---------------------------------------------------------------------
   //
   public int[] getViewIds() {
      
      int numOn = 0;
      for (int i = 0; i < checks.length; i++) {
	 if (checks[i].isSelected()) {
	    numOn++;
	 }
      }
      int[] ia = new int[numOn];
      int cnt = 0;
      for (int i = 0; i < checks.length; i++) {
	 if (checks[i].isSelected()) {
	    ia[cnt++] = checks[i].getTag();
	 }
      }
      return ia;
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
      //gbc.insets = new java.awt.Insets(0,0,0,0);
      gbc.ipadx = 0; 
      gbc.ipady = 0;
   }

   public void mouseClicked(java.awt.event.MouseEvent event) {
      Object object = event.getSource();
      if (object == latLonLbl)
	 latLonCheck.doClick();
      else if (object == lonDepthLbl)
	 lonDepthCheck.doClick();
      else if (object == lonTimeLbl)
	 lonTimeCheck.doClick();
      else if (object == latDepthLbl)
	 latDepthCheck.doClick();
      else if (object == latTimeLbl)
	 latTimeCheck.doClick();
      else if (object == depthTimeLbl)
	 depthTimeCheck.doClick();
      else if (object == latLonIconLbl)
	 latLonCheck.doClick();
      else if (object == lonDepthIconLbl)
	 lonDepthCheck.doClick();
      else if (object == lonTimeIconLbl)
	 lonTimeCheck.doClick();
      else if (object == latDepthIconLbl)
	 latDepthCheck.doClick();
      else if (object == latTimeIconLbl)
	 latTimeCheck.doClick();
      else if (object == depthTimeIconLbl)
	 depthTimeCheck.doClick();
   }
   public void mouseReleased(java.awt.event.MouseEvent event) { }
   public void mousePressed(java.awt.event.MouseEvent event) { }
   public void mouseEntered(java.awt.event.MouseEvent event) { }
   public void mouseExited(java.awt.event.MouseEvent event) { }

   //---------------------------------------------------------------------
   //
   public static void main(String args[]) {

      JFrame fr = new JFrame();
      ViewsGUI vg = new ViewsGUI();
      fr.setVisible(true);
      fr.setSize(300, 300);
      vg.setVisible(true);


      fr.getContentPane().add(vg);
      fr.invalidate();
      fr.validate();

     
   }
}

//---------------------------------------------------------------------
//
// a simple checkbox with an integer constant associated rather
//  than an action command
//

class JCheckBoxTagged extends JCheckBox {

  int tag;
  public JCheckBoxTagged(int tag) {
     super();
     this.tag = tag;
  }
  public int getTag() {
     return tag;
  }
}

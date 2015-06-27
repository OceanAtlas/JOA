/*
 * $Id: ViewToggleButn.java,v 1.3 2005/02/15 18:31:11 oz Exp $
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
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class ViewToggleButn extends SmallIconButton implements ActionListener {
   private ImageIcon icon = new ImageIcon();
   //private ImageIcon rollIcon = new ImageIcon();
   private ImageIcon selIcon = new ImageIcon();

   public ViewToggleButn(String standardGif, String toolTip) {
   		super(standardGif, toolTip);
      this.addActionListener(this);

      // --------------------------------------------
      //  begin - developer customized area
      // --------------------------------------------

      //
      // use an EmptyBorder to set insets for toggle buttons
      //
      //EmptyBorder eb = new EmptyBorder(3,3,3,3);
      //this.setBorder(eb);
   }   

   public void setVisible(boolean b) {
      if (b)
         setLocation(50, 50);
      super.setVisible(b);
   }

   public void addNotify() {
      // Record the size of the window prior to calling parents addNotify.
      Dimension size = getSize();

      super.addNotify();

      if (frameSizeAdjusted)
         return;
      frameSizeAdjusted = true;

      // Adjust size of frame according to the insets and menu bar
      Insets insets = getInsets();
      javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
      int menuBarHeight = 0;
      if (menuBar != null)
         menuBarHeight = menuBar.getPreferredSize().height;
      setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
   }

   // Used by addNotify
   boolean frameSizeAdjusted = false;


   public void actionPerformed(java.awt.event.ActionEvent event) {
      if (Debug.TRACE) System.out.println("actionPerformed entered");
      Object object = event.getSource();
      if (object == this)
         if (Debug.TRACE) System.out.println(" TogButn action performed");
   }


   static public void main(String args[]) {
      JFrame fr = new JFrame();
      fr.setSize(200, 200);
      fr.setVisible(true);
      fr.getContentPane().setLayout(null);

      ViewToggleButn viewToggleButn = new ViewToggleButn(" "," ");
      fr.getContentPane().add(viewToggleButn);
   
   }

}

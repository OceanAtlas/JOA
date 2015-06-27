/*
 * $Id: BackButn.java,v 1.3 2005/02/15 18:31:08 oz Exp $
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
 import javax.swing.*;
import java.awt.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
 public class BackButn extends JButton {
   private static final String gif = "gifs/back.gif";

   public BackButn() {
     super();

     // booleans first
     this.setRolloverEnabled(true);
     this.setFocusPainted(false);
     this.setContentAreaFilled(false);
     this.setBorderPainted(false);
     this.setSelected(false);


     this.setSize(new Dimension(61,37));
     this.setPreferredSize(new Dimension(61,37));

     //
     // quick-o-matic kludge to be able to run in Visual Cafe debugging environ:
     //
        try {
	        this.setIcon(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource(gif)));
        }
        catch (ClassNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      	System.out.println("BackButn:ctor");
        }
   }


	static public void main(String args[])
	{
	   JFrame fr = new JFrame();
	   fr.getContentPane().setLayout(new BorderLayout());
	   fr.setSize(918,570);
	   fr.setVisible(true);

           JPanel jp = new JPanel();
           jp.setBounds(10,10,100,100);
	   jp.setBackground(Color.red);

	   BackButn b = new BackButn();
	System.out.println("Before adding button to panel, rolloverEnabled: " + 
		b.isRolloverEnabled());
	   jp.add(b);
	System.out.println("After adding button to panel, rolloverEnabled: " + 
		b.isRolloverEnabled());
	   b.setBounds(10,10,61,37);
	   fr.getContentPane().add(jp, BorderLayout.NORTH);


       // Now use JToolbar
           JToolBar tb = new JToolBar();
           tb.setBounds(10,200,300,43);
	   tb.setBackground(Color.yellow);

	   BackButn b1 = new BackButn();
	   b1.setBounds(10,10,61,37);
	System.out.println("Before adding button to JToolbar, rolloverEnabled: " + 
		b1.isRolloverEnabled());
 	   tb.add(b1);
	System.out.println("After adding button to JToolbar, rolloverEnabled: " + 
		b1.isRolloverEnabled());
	   b1.setRolloverEnabled(true);

	   fr.getContentPane().add(tb, BorderLayout.SOUTH);
	   fr.invalidate();
    	   fr.validate();
	}

 }

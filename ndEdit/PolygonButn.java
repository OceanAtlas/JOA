/*
 * $Id: PolygonButn.java,v 1.3 2005/02/15 18:31:10 oz Exp $
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
 * @author  oz 
 * @version 1.0 01/13/00
 */
 public class PolygonButn extends JToggleButton {
   private static final String gif = "gifs/polygon.gif";

   public PolygonButn() {
     super();
     /*this.setContentAreaFilled(false);
     this.setBorderPainted(false);
     this.setOpaque(false);
     this.setFocusPainted(false);
     this.setSize(new Dimension(61,37));*/
     this.setPreferredSize(new Dimension(61,37));

     //this.setRolloverEnabled(true);

     //if (null != getClass().getResource(selgif)) {
     ////   this.setSelectedIcon(new ImageIcon(getClass().getResource(selgif)));
    //    this.setPressedIcon(new ImageIcon(getClass().getResource(selgif)));
     //}
        try {
	        this.setIcon(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource(gif)));
        }
        catch (ClassNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      	System.out.println("PolygonButn:ctor");

        }
	/*
     this.setRolloverIcon(new ImageIcon(getClass().getResource(rollgif)));
     this.setSelectedIcon(new ImageIcon(getClass().getResource(selgif)));
     this.setPressedIcon(new ImageIcon(getClass().getResource(selgif)));
	*/
   }
 }

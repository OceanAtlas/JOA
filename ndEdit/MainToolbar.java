/*
 * $Id: MainToolbar.java,v 1.4 2005/02/15 18:31:09 oz Exp $
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
import java.util.*;
import java.beans.*;

 /**
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class MainToolbar extends JToolBar implements PropertyChangeListener {
	// constructor
	public MainToolbar(JToggleButton[] togbutns, JButton[] butns, JToggleButton[] togbutns2) {
		// main toolbar (top)
		//this.setBackground(java.awt.Color.lightGray);
		this.setBounds(15,11,379,43);
		this.setSize(new Dimension(379,43));
		this.setPreferredSize(new Dimension(379,43));

		// use an EmptyBorder to set insets for toggle buttons
		for (int i = 0; i<togbutns.length; i++) {
			this.add(togbutns[i]);
		}
		
		this.addSeparator(new Dimension(13, 1));
		javax.swing.JSeparator sep = new javax.swing.JSeparator(SwingConstants.VERTICAL);
		sep.setPreferredSize(new Dimension(2, 30));
		sep.setSize(new Dimension(2, 30));
		sep.setMinimumSize(new Dimension(2, 30));
		sep.setMaximumSize(new Dimension(2, 30));
		this.add(sep);
		this.addSeparator(new Dimension(10, 1));
		
		for (int i = 0; i < butns.length; i++) {
			this.add(butns[i]);
			//butns[i].setRolloverEnabled(false);   // re: Swing bug # 4260485
		}
		
		if (butns.length > 0) {
			this.addSeparator(new Dimension(13, 1));
				
			sep = new javax.swing.JSeparator(SwingConstants.VERTICAL);
			sep.setPreferredSize(new Dimension(2, 30));
			sep.setSize(new Dimension(2, 30));
			sep.setMinimumSize(new Dimension(2, 30));
			sep.setMaximumSize(new Dimension(2, 30));
			this.add(sep);
			this.addSeparator(new Dimension(10, 1));
		}
		
    	ButtonGroup bg = new ButtonGroup();
    	for (int i=0; i<togbutns2.length; i++) {
			this.add(togbutns2[i]);
			//togbutns2[i].setRolloverEnabled(false);   // re: Swing bug # 4260485
			bg.add(togbutns2[i]);
		}

		ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
		
		boolean windows = false;
		Font tff = BeanFonts.textFieldFont;
		if (-1 != System.getProperty("os.name").indexOf("Windows")) 
			windows = true;
		if (windows) 
			tff = new Font("SansSerif", Font.PLAIN, 10);
		JLabel wl = new JLabel("  " + b.getString("kWidth"));
		wl.setFont(tff);
      	wl.setForeground(java.awt.Color.black);
		this.add(wl);
		SectionToolWidthTextField stw = new SectionToolWidthTextField();
		this.add(stw);
		stw.addPropertyChangeListener(this);
		JLabel kl = new JLabel(b.getString("kKM"));
		kl.setFont(tff);
      	kl.setForeground(java.awt.Color.black);
		this.add(kl);
		this.invalidate();
		this.validate();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("sectionwidth")) {
			// section width changed
			Constants.SECTION_WIDTH = ((Float)e.getNewValue()).floatValue();
		}
	}
}

/*
 * $Id: ProgressDialog.java,v 1.2 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import java.awt.*;
import javaoceanatlas.utility.*;

/**
 * A dialog that uses a bargauge to indicate progress made on a
 * task that presumably takes some time to complete.
 *
 * ProgressDialog implements the singleton pattern:  clients
 * may only access the one and only ProgressDialog through the
 * static getProgressDialog() method.<p>
 *
 * <em>Note:  The 1.0.2 version of the AWT has introduced a
 * bug that breaks the ProgressDialog under Motif - the 
 * bargauge does not function.  This worked fine in 1.0.1.<em>
 *
 * @version 1.0, Apr 1 1996
 * @author  David Geary
 * @see     GJTDialog
 * @see     Bargauge
 * @see     gjt.test.DialogTest
 */
@SuppressWarnings("serial")
public class ProgressDialog extends CloseableFrame {
    static private int     _preferredWidth  = 400;
    static private int     _preferredHeight = 50;

    private Bargauge bargauge;
    
    public ProgressDialog(Frame frame, String title, Color color, Color bgColor) {
        super(frame, title, true);
        this.getContentPane().setLayout(new BorderLayout());
        setBackground(Color.white);
        bargauge = new Bargauge(color, bgColor);
        bargauge.setOpaque(true);
        this.getContentPane().add("Center", new TenPixelBorder(bargauge, 0, 0, 0, 0, Color.white));
        pack();
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
		this.setLocation(x, y);
	}	
    
    public void setPercentComplete(double percent) {
        bargauge.setFillPercent(percent);
        bargauge.fill();
    }	
    
    public double getPercentComplete() {
        return(bargauge.getFillPercent());
    }
    
    public void reset() {
        bargauge.setFillPercent(0);
    }
    
    public void clear() {
        bargauge.clear();
    }
    
    public void setWinTitle(String title) {
    	this.setTitle(title);
    }
    
    public Dimension getPreferredSize() {
			return new Dimension(_preferredWidth, _preferredHeight);
    }
}

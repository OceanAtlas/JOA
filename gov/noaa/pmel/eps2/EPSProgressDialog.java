package gov.noaa.pmel.eps2;

import java.awt.*;

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
public class EPSProgressDialog extends Dialog {
    static private EPSProgressDialog _theProgressDialog;
    static private int     _preferredWidth  = 400;
    static private int     _preferredHeight = 50;
    static private Color   _color;
    static private boolean _dialogUp;

    private EPSBargauge bargauge;
    
    public EPSProgressDialog(Frame frame, 
                           String title, 
                           Color color) {
        super(frame, title, true);
        this.setModal(false);
        this.setLayout(new BorderLayout());
        setBackground(Color.white);
        this.add("Center", bargauge = new EPSBargauge(color));
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

        //if(percent == 100)
         //   dispose();
    }
    
    public void reset() {
        bargauge.setFillPercent(0);
    }
    
    public Dimension getPreferredSize() {
		if (System.getProperty("os.name").startsWith("Mac"))
			return new Dimension(_preferredWidth, _preferredHeight/2);
		else
			return new Dimension(_preferredWidth, _preferredHeight);
    }
}

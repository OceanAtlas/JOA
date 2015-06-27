
/*
 * $Id: ValueToolbar.java,v 1.4 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.events.ObsChangedEvent;
import javaoceanatlas.resources.JOAConstants;
import javaoceanatlas.ui.widgets.*;

 /**
 * @author  oz 
 * @version 1.0 01/13/00
 */
@SuppressWarnings("serial")
public class MetadataToolbar extends JPanel {
	protected ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	protected RubberbandPanel mComp;
  private boolean mIsLocked = false;  
  protected Station mCurrStn;
  protected Section mCurrSec;
  protected JPanel line1 = null;
  protected FileViewer mFileViewer;
  protected JOAJLabel l1 = null, l3 = null, l4 = null;
	
	// constructor
	public MetadataToolbar(RubberbandPanel comp) {
		mComp = comp;
		setStn(null, null);
	}

  public void setLocked(boolean b) {
    mIsLocked = b;
  }

  public void obsChanged(ObsChangedEvent evt) {
    // display the current station
    Station sh = evt.getFoundStation();
    Section sech = evt.getFoundSection();
    setNewStn(sech, sh);
  }
  
  public void setStn(Section sec, Station stn) {
    line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		Font tff = new Font("SansSerif", Font.PLAIN, 10);
    
    l1 = new JOAJLabel("Section: -------", JOAConstants.ISMAC);
    l3 = new JOAJLabel("Stn: -------", JOAConstants.ISMAC);
    l4 = new JOAJLabel("Cast: -------");
    l1.setFont(tff);
		l3.setFont(tff);
		l4.setFont(tff);
    line1.add(l1);
    line1.add(l3);
    line1.add(l4);
    this.add(line1, "Center");
    line1.invalidate();
  }
	
	public void setNewStn(Section sec, Station stn) {
    if (mIsLocked) {
      return;
    }
    l1.setText("Section: " + sec.mSectionDescription);
    l3.setText("Stn: " + stn.mStnNum);
    l4.setText("Cast: " + stn.mCastNum);

    l1.invalidate();
    l3.invalidate();
    l4.invalidate();
  }
}

/**
 * 
 */
package net.sourceforge.openforecast;

import java.awt.FlowLayout;
import javaoceanatlas.ui.JOATimeFilter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author oz
 *
 */
public class BlankFilterPanel extends JPanel implements JOATimeFilterPanel {
	public BlankFilterPanel() {
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		this.add(new JLabel("                                                                 "));
	}
	
	public Element getTag(Document doc) {
		return null;
	}

  public String getTagName() {
	  return "";
  }

  
  public JOATimeFilter getFilter() {
  	return null;
  }
}

/**
 * 
 */
package net.sourceforge.openforecast;

import javaoceanatlas.ui.DateRangeFilter;
import javaoceanatlas.ui.JOATimeFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author oz
 *
 */
public interface JOATimeFilterPanel {
	public Element getTag(Document doc);
	public String getTagName();
	public JOATimeFilter getFilter();
}

/*
 * $Id: NdEditTargetMonitor.java,v 1.5 2005/02/15 18:31:11 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit.ncBrowse.map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.util.List;

import ucar.nc2.Variable;
import ucar.nc2.Dimension;
/**
 * <pre>
 * Title:        Drag and Drop test application
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      NOAA/PMEL/EPIC
 * </pre>
 * @author Donald Denbo
 * @version $Revision: 1.5 $, $Date: 2005/02/15 18:31:11 $
 */
public class NdEditTargetMonitor {
	/**
	* @label instance
	*/
	static NdEditTargetMonitor monitor = null;

	JLabel[] title;
	JLabel[] item;
	JPanel[] panel;

	static Color ENABLED = new Color(186, 217, 217);
	static Color DEFAULT = new Color(102, 102, 153);

	private NdEditTargetMonitor() {
		title = new JLabel[NdEditMapModel.ELEMENT_COUNT];
		item = new JLabel[NdEditMapModel.ELEMENT_COUNT];
		panel = new JPanel[NdEditMapModel.ELEMENT_COUNT];
	}

	static public NdEditTargetMonitor getInstance() {
		if(monitor == null) {
			monitor = new NdEditTargetMonitor();
		}
		return monitor;
	}

	public void reset() {
		panel[NdEditMapModel.LATITUDE].setBackground(ENABLED);
		panel[NdEditMapModel.LONGITUDE].setBackground(ENABLED);
		panel[NdEditMapModel.Z].setBackground(ENABLED);
		panel[NdEditMapModel.TIME].setBackground(ENABLED);
		item[NdEditMapModel.LATITUDE].getDropTarget().setActive(true);
		item[NdEditMapModel.LONGITUDE].getDropTarget().setActive(true);
		item[NdEditMapModel.Z].getDropTarget().setActive(true);
		item[NdEditMapModel.TIME].getDropTarget().setActive(true);
		for(int i=0; i < NdEditMapModel.ELEMENT_COUNT; i++) {
			if (item[i] != null)
				item[i].setText("");
		}
		title[NdEditMapModel.LATITUDE].setForeground(DEFAULT);//gray);
		title[NdEditMapModel.LONGITUDE].setForeground(DEFAULT);
		title[NdEditMapModel.Z].setForeground(DEFAULT);
		title[NdEditMapModel.TIME].setForeground(DEFAULT);
	}

	public void setItem(int type, JLabel iitem, JPanel ipanel, JLabel ititle) {
		item[type] = iitem;
		panel[type] = ipanel;
		title[type] = ititle;
	}


	// may need to implement this eventually if I need to update the state
	// of the target panel based upon the results of a drag operation
	public void valueChanged(int type) {
		/* VMapModel map = VariableMapDialog.getCurrentMap();
		switch(type) {
			case VMapModel.XAXIS:
				title[VMapModel.XAXIS].setForeground(DEFAULT);
				break;
			case VMapModel.YAXIS:
				title[VMapModel.YAXIS].setForeground(DEFAULT);
				break;
			case VMapModel.UCOMPONENT:
			case VMapModel.VCOMPONENT:
				if (map.isSet(VMapModel.VCOMPONENT)) {
					title[VMapModel.VCOMPONENT].setForeground(DEFAULT);
				}
				else {
					title[VMapModel.VCOMPONENT].setForeground(gray);
				}
				if (map.isSet(VMapModel.UCOMPONENT)) {
					title[VMapModel.UCOMPONENT].setForeground(DEFAULT);
				}
				else {
					title[VMapModel.UCOMPONENT].setForeground(gray);
				}
				panel[VMapModel.ZCONTOUR].setBackground(Color.lightGray);
				item[VMapModel.ZCONTOUR].getDropTarget().setActive(false);
				break;
			case VMapModel.ZCONTOUR:
				panel[VMapModel.UCOMPONENT].setBackground(Color.lightGray);
				item[VMapModel.UCOMPONENT].getDropTarget().setActive(false);
				panel[VMapModel.VCOMPONENT].setBackground(Color.lightGray);
				item[VMapModel.VCOMPONENT].getDropTarget().setActive(false);
				break;
		}*/
	}

	public void updateAll(NdEditMapModel map) {
		for (int i=0; i<NdEditMapModel.ELEMENT_COUNT; i++) {
			if (map.isSet(i) && item[i] != null)
				updateItem(i, map);
		}
	}

	public void updateItem(int type, NdEditMapModel map) {
		String s = null;
		Object obj = map.getDimElement(type);
		s = getDimensionLabel(type, map, (Dimension)obj);
		item[type].setText("<html>" + s + "</html>");
	}

	String getVariableLabel(int type, NdEditMapModel map, Variable ncVar) {
		StringBuffer sbuf = new StringBuffer();
		List al = ncVar.getDimensions();
		if((al.size() == 1) && ((Dimension)al.get(0)).getName().equals(ncVar.getName())) {
			sbuf.append("<b>" + ncVar.getName() + "</b>   ");
		}
		else {
			sbuf.append(ncVar.getName() + "   ");
		}
		sbuf.append(getDimensionList(type, map, ncVar));
		return sbuf.toString();
	}

	String getDimensionLabel(int type, NdEditMapModel map, Dimension ncDim) {
		StringBuffer sbuf = new StringBuffer();
		//if (map.hasMatch(type, ncDim)) {
		//   sbuf.append("<font color=green>");
		//  }
		//  else {
		//   sbuf.append("<font color=red>");
		//  }
		sbuf.append(ncDim.getName() + " ");
		sbuf.append("(" + ncDim.getLength() + " points)");
		return sbuf.toString();
	}

  /**
   * Look for dimension match.  If no match is found all dimensions are shown in
   * red.  If any match is found, they are shown in green, and the rest in blue.
   */
  String getDimensionList(int type, NdEditMapModel map, Variable ncVar) {
    List al = ncVar.getDimensions();
    boolean[] match = new boolean[al.size()];
    boolean anyMatch = false;
    StringBuffer sbuf = new StringBuffer("[");
   // for(int i=0; i < al.size(); i++) {
   //   Dimension ncDim = (Dimension)al.get(i);
   //   match[i] = map.hasMatch(type, ncDim);
  //    if(match[i]) anyMatch = true;
  //  }

  //  for(int i=0; i < al.size(); i++) {
  //    String name = ((Dimension)al.get(i)).getName();
  //    if(anyMatch) {
  //      if(match[i]) {
  //        sbuf.append("<font color=green>" + name + "</font>");
  //      } else {
  //        sbuf.append(/*"<font color=blue>" + */name /* + "</font>"*/);
   //     }
  //    } else {
  //      sbuf.append("<font color=red>" + name + "</font>");
  //    }
  //    if(i < al.size()-1) {
  //      sbuf.append(",");
  //    }
 //   }
//    sbuf.append("]");

    return sbuf.toString();
  }

}

/*
 * $Id: NdEditDnDTable.java,v 1.3 2005/02/15 18:31:11 oz Exp $
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

import javax.swing.JTable;
import java.awt.dnd.*;
import java.awt.datatransfer.StringSelection;
import ndEdit.Debug;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

/**
 * <pre>
 * Title:        netCDF File Browser
 * Description:  General purpose netCDF file Browser.
 * Copyright:    Copyright (c) 2000
 * Company:      NOAA/PMEL/EPIC
 * </pre>
 * @author Donald Denbo
 * @version $Revision: 1.3 $, $Date: 2005/02/15 18:31:11 $
 */

public class NdEditDnDTable extends JTable implements DragGestureListener, DragSourceListener {
  DragSource dragSource;

  public NdEditDnDTable() {
    super();
    dragSource = new DragSource();
    dragSource.createDefaultDragGestureRecognizer(this,
                                                  DnDConstants.ACTION_COPY_OR_MOVE,
                                                  this);
	this.putClientProperty("Quaqua.Table.style", "striped");
  }

  public NdEditDnDTable(Object[][] data, Object[] titles) {
    super(data, titles);
    dragSource = new DragSource();
    dragSource.createDefaultDragGestureRecognizer(this,
                                                  DnDConstants.ACTION_COPY_OR_MOVE,
                                                  this);
	this.putClientProperty("Quaqua.Table.style", "striped");
  }

  public void dragGestureRecognized(DragGestureEvent dge) {
    int index = getSelectedRow();
    try {
		NdEditTableModel model = (NdEditTableModel)getModel();
		Object selected = model.getDimAt(index);
		if (selected != null) {
			StringBuffer str = new StringBuffer("<html>");
			NcTransferable nct = new NcTransferable(new Integer(index));
			dragSource.startDrag (dge, DragSource.DefaultCopyDrop, nct, this);
		} 
		else {
			if (Debug.DEBUG) System.out.println( "nothing was selected");
		}
	}
	catch (ClassCastException ex) {}
  }

  public void dragEnter(DragSourceDragEvent dsde) {
    if(Debug.DEBUG) System.out.println("DragSource Enter");
  }

  public void dragOver(DragSourceDragEvent dsde) {
    if(Debug.DEBUG) System.out.println("DragSource Over");
  }

  public void dropActionChanged(DragSourceDragEvent dsde) {
    if(Debug.DEBUG) System.out.println("DragSource: Drop Action Changed");
  }

  public void dragExit(DragSourceEvent dse) {
    //    this.setCursor(DragSource.DefaultLinkNoDrop);
    if(Debug.DEBUG) System.out.println("DragSource Exit");
  }

  public void dragDropEnd(DragSourceDropEvent dsde) {
    if(dsde.getDropSuccess()) {
      if(Debug.DEBUG) System.out.println("DragSource Success!");
    }
  }
}


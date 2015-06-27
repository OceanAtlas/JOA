/*
 * $Id: NdEditLabelTarget.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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

import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import ndEdit.Debug;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import java.util.Vector;
import ndEdit.ncBrowse.NdEditView;

/**
 * <pre>
 * Title:        netCDF File Browser
 * Description:  General purpose netCDF file Browser.
 * Copyright:    Copyright (c) 2000
 * Company:      NOAA/PMEL/EPIC
 * </pre>
 * @author Donald Denbo
 * @version $Revision: 1.2 $, $Date: 2005/02/15 18:31:11 $
 */
public class NdEditLabelTarget implements DropTargetListener {
  JLabel label;
  int type;
  
  public NdEditLabelTarget(JLabel tf, int itype) {
    label = tf;
    type = itype;
  }

  public void dragEnter(DropTargetDragEvent dtde) {
    if(Debug.DEBUG) System.out.println("DropTarget Enter");
  }

  public void dragOver(DropTargetDragEvent dtde) {
    if(Debug.DEBUG) System.out.println("DropTarget Over");
  }

  public void dropActionChanged(DropTargetDragEvent dtde) {
    if(Debug.DEBUG) System.out.println("DropTarget Action Changed");
  }

  public void dragExit(DropTargetEvent dte) {
    if(Debug.DEBUG) System.out.println("DropTarget Exit");
  }

  public void drop(DropTargetDropEvent dtde) {
    if(Debug.DEBUG) System.out.println("DropTarget drop");

    try {
      Transferable transferable = dtde.getTransferable();

      // we accept only NcIndex flavor
      if (transferable.isDataFlavorSupported(NcTransferable.NcIndexFlavor)) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        if (transferable.isDataFlavorSupported(NcTransferable.NcIndexFlavor)) {  // redundent test
          int index = ((Integer)transferable.getTransferData(NcTransferable.NcIndexFlavor)).intValue();
          NdEditMapModel map = NdEditView.getCurrentMap();
          Object obj = NdEditView.getCurrentModel().getDimAt(index);
          map.setElement(obj, type);
          NdEditView.updateAll(map);
        }
        dtde.getDropTargetContext().dropComplete(true);
        
        NdEditView.valueChanged(type);
      } 
      else {
        dtde.rejectDrop();
      }
    }
    catch (IOException exception) {
      exception.printStackTrace();
      System.err.println( "Exception" + exception.getMessage());
      dtde.rejectDrop();
    }
    catch (UnsupportedFlavorException ufException ) {
      ufException.printStackTrace();
      System.err.println( "Exception" + ufException.getMessage());
      dtde.rejectDrop();
    }
  }
}

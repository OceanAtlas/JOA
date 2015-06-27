/*
 * $Id: NcTransferable.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;

import ucar.nc2.Dimension;
import ucar.nc2.Variable;

import ndEdit.Debug;

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

public class NcTransferable implements Transferable {

  public static DataFlavor NcIndexFlavor;

  static {
    try {
      NcIndexFlavor = new DataFlavor(Class.forName("java.lang.Integer"),
                                      "netCDF Index");
    } catch (ClassNotFoundException cnfe) {
          cnfe.printStackTrace();
    }
  }

  private DataFlavor flavor_ = null;
  private Object data_ = null;

  public NcTransferable(Integer obj) {
    data_ = obj;
    flavor_ = NcIndexFlavor;
    if(Debug.DEBUG) System.out.println("NcTransferable: " + flavor_.toString());
  }

  public DataFlavor[] getTransferDataFlavors() {
    if(Debug.DEBUG) System.out.println("NcTransferable.getTransferDataFlavors() called");
    DataFlavor[] df = new DataFlavor[1];
    df[0] = flavor_;
    return df;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    if(Debug.DEBUG) System.out.println("NcTransferable.isDataFlavorSupported() called");
    return flavor.equals(flavor_);
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if(Debug.DEBUG) System.out.println("NcTransferable.getTransferData() called");
    if(flavor != flavor_) throw new UnsupportedFlavorException(flavor);
    return data_;
  }
}

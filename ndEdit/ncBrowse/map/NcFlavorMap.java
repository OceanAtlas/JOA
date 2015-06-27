/*
 * $Id: NcFlavorMap.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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

import java.util.Map;
import java.util.HashMap;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorMap;

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

public class NcFlavorMap implements FlavorMap {

  private HashMap mapFlavors_ = null;
  private HashMap mapNatives_ = null;
  private static NcFlavorMap instance_ = null;

  public NcFlavorMap() {
    mapNatives_ = new HashMap();
    mapNatives_.put("NetCDF Index",NcTransferable.NcIndexFlavor);
    mapFlavors_ = new HashMap();
    mapFlavors_.put(NcTransferable.NcIndexFlavor,"NetCDF Index");
    instance_ = this;
  }

  public static NcFlavorMap getInstance() {
    if(instance_ == null) {
      instance_ = new NcFlavorMap();
    }
    return instance_;
  }

  public Map getNativesForFlavors(DataFlavor[] flavors) {
    if(flavors == null) return mapFlavors_;
    HashMap map = new HashMap();
    for(int i=0; i < flavors.length; i++) {
      if(mapFlavors_.containsKey(flavors[i])) {
        map.put(flavors[i],mapFlavors_.get(flavors[i]));
      }
    }
    return map;
  }

  public Map getFlavorsForNatives(String[] natives) {
    if(natives == null) return mapNatives_;
    HashMap map = new HashMap();
    for(int i=0; i < natives.length; i++) {
      if(mapNatives_.containsKey(natives[i])) {
        map.put(natives[i],mapNatives_.get(natives[i]));
      }
    }
    return map;
  }

  public String toString() {
    return "ncFlavorMap: " + mapNatives_.toString();
  }
}

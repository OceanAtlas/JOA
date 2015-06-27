/*
 * $Id: CIMFacade.java,v 1.2 2005/02/15 18:31:08 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

/**
 * Interface for classes that register changeable listerners.
 *
 */

public interface CIMFacade {
 /**
 *
 *
 * @author  oz 
 * @version 1.0 10/25/00
 */

    public void pushChangeableInfo(ChangeableInfo ci);
    public void pushChangeableInfo(String id, Object oldValue, Object newValue, boolean undoable);
	public void addChangeableInfoListener(ChangeableInfoListener obj);
}

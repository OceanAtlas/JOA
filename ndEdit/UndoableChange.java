/*
 * $Id: UndoableChange.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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

import java.util.Vector;
import java.awt.event.*;
import javax.swing.undo.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class UndoableChange extends AbstractUndoableEdit {

   ChangeableInfo ci;
   NdEdit p;

   //
   public UndoableChange(ChangeableInfo ci,
			   NdEdit p) {
      this.ci = ci;
      this.p = p;
   }

   //
   public String getPresentationName() {
System.out.println(" getPresentationName : " + ci.getId() + "\n " + ci.toString());
      return ci.getId();
   }

   //
   // (forward)
   //
   public void redo() throws CannotRedoException {
      super.redo();
      ChangeableInfo ciNew = new ChangeableInfo(
		ci.getId(),
		ci.getOldValue(), 
		ci.getNewValue(), 
		false);
      p.pushChangeableInfo(ciNew);
   }

   //
   // (back)
   //
   public void undo() throws CannotUndoException {
      super.undo();

      ChangeableInfo ciNew = new ChangeableInfo(
		ci.getId(),
		ci.getNewValue(), 
		ci.getOldValue(), 
		false);
      p.pushChangeableInfo(ciNew);
   }
}

/*
 * $Id: BackForwardManager.java,v 1.4 2005/02/15 18:31:08 oz Exp $
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
import javax.swing.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 *
  * @note The backward-forward manager will record all actions and will 
  * be able to re-issue previous state.  It will register for all 
  * changes ... I'm not sure at this point, perhaps it will be 
  * aggregated to the Change Manager.  It is merely a collector of 
  * commands that are issued by other objects in the system.  Its 
  * functionality is very simple: to store, push and pop commands 
  * in response to user back/forward requests.
  * @stereotype Singleton
  */


public class BackForwardManager implements ActionListener, 
					ChangeableInfoListener, 
					MouseListener {

   private BackButn backButn;
   private ForwardButn forwardButn;
   UndoManagerX undoManager;
   JPopupMenu popupMenu;
   private Object parentObject;
   NdEdit rootObject;

   // ---------------------------------------------------------------------
   //
   public BackForwardManager(Object parentObject) {
      undoManager = new UndoManagerX();
      this.setParent(parentObject);
      rootObject = (NdEdit) getAncestor();
      if (rootObject != null) rootObject.addChangeableInfoListener(this);
      
   }

   // ---------------------------------------------------------------------
   //
   public void popChangeableInfo(ChangeableInfo ci) {
      if (ci.undoable) {
			undoManager.addEdit((new UndoableChange(ci, rootObject)));
      }
      else {
      }
   }

   // ---------------------------------------------------------------------
   //
   public void setBackButn(BackButn backButn) {
      this.backButn = backButn;
      this.backButn.addActionListener(this);
      this.backButn.addMouseListener(this);
   }

   // ---------------------------------------------------------------------
   //
   public void setForwardButn(ForwardButn forwardButn) {
      this.forwardButn = forwardButn;
      this.forwardButn.addActionListener(this);
   }

   //--------------------------------------------------------------------------
   // 
   public void setParent(Object parentObject) {
      this.parentObject = parentObject;
   }

   //-----------------------------------------------------------------------
   //
   public Object getAncestor() {
      if (parentObject != null) {
         return ((Lineage)parentObject).getAncestor();
      }
      return this;
   }

   //
   public void actionPerformed(ActionEvent e) {
      UndoableChange uc;
      if (e.getSource() == backButn) {
	if (undoManager.canUndo()) {
	   undoManager.undo();
	}
      }
      if (e.getSource() == forwardButn) {
	if (undoManager.canRedo()) {
	   undoManager.redo();
	}
      }
   }

   //
   void createPopupMenu() {
      popupMenu = new JPopupMenu();
      UndoableEdit[] lst = undoManager.getEdits();
System.out.println("Number of undoable edits: " + lst.length);
      for (int i = 0; i < lst.length; i++) {
	((UndoableChange)lst[i]).getPresentationName();
      }
      popupMenu.add(new JMenuItem(" "));
   }



   //
   public void mousePressed(java.awt.event.MouseEvent event) {
      //
      // unix Trigger
      //
      if (event.isPopupTrigger()) {    
        //System.out.println("Trigger");
	if (popupMenu == null) createPopupMenu();
        popupMenu.show(event.getComponent(), event.getX(), event.getY());
      }
   }
    
   //
   public void mouseEntered(java.awt.event.MouseEvent event) { 
   }
   //
   public void mouseExited(java.awt.event.MouseEvent event) { 
   }
   //
   public void mouseReleased(java.awt.event.MouseEvent event) { 
      //
      // windows Trigger
      //
      if (event.isPopupTrigger()) {    
	if (popupMenu == null) createPopupMenu();
        popupMenu.show(event.getComponent(), event.getX(), event.getY());
      }
   }

   //
   public void mouseClicked(java.awt.event.MouseEvent event) {
      Object object = event.getSource();
   } 

}

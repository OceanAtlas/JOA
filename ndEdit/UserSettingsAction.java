/*
 * $Id: UserSettingsAction.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.border.*;
import ndEdit.*;

 public class UserSettingsAction extends NdEditAction {
   public JMenuBar menuBar;
   public JToolBar toolBar;
   UserSettingsGUI usg;
   protected transient ChangeEvent changeEvent = null;
   protected EventListenerList listenerList = new EventListenerList();
   UserSettingsManager mgr;

    // This is our sample action. It must have an actionPerformed() method,
    // which is called when the action should be invoked.
    public UserSettingsAction(String menu, String text, Icon icon, UserSettingsManager usMgr) {
        super(menu, text, icon);
        mgr = usMgr;
    }

	public void actionPerformed(ActionEvent e) {
		doAction();
	}
	
	public void doAction() {
		usg = new UserSettingsGUI(true, true, true, false, true);
		if (usg.getOkay()) {
			usg.updateSettings();
	  		Constants.USERSETTINGS.save("ndeditsettings.txt");
			fireChange();
		}
	}

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireChange() {
    	mgr.publishAll();
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }          
        }
    }   


    public static void main(String s[]) {

        UserSettingsAction userSettingsAction = new UserSettingsAction("edit", "User Settings", new ImageIcon("gifs/action.gif"), null);

        // Create a menu bar and give it a bevel border
        userSettingsAction.menuBar = new JMenuBar();
        userSettingsAction.menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));

        // Create a menu and add it to the menu bar
        JMenu menu = new JMenu("The Menu");
        userSettingsAction.menuBar.add(menu);

        // Create a toolbar and give it an etched border
        userSettingsAction.toolBar = new JToolBar();
        userSettingsAction.toolBar.setBorder(new EtchedBorder());

        // Finally, add the sample action to the menu and the toolbar.
        menu.add(userSettingsAction);
        userSettingsAction.toolBar.add(userSettingsAction);

        JFrame frame = new JFrame("User Settings Action");
        frame.addWindowListener(new BasicWindowMonitor());
        frame.setJMenuBar(userSettingsAction.menuBar);
        frame.getContentPane().add(userSettingsAction.toolBar, BorderLayout.NORTH);
        frame.setSize(200,200);
        frame.setVisible(true);
    }
}

class BasicWindowMonitor extends WindowAdapter {

  public void windowClosing(WindowEvent e) {
    Window w = e.getWindow();
    w.setVisible(false);
    w.dispose();
    System.exit(0);
  }
}

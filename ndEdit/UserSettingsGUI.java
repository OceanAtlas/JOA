/*
 * $Id: UserSettingsGUI.java,v 1.3 2005/02/15 18:31:11 oz Exp $
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
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class UserSettingsGUI {
   private JDialog dialog;
   private JTabbedPane userSettingsTabs = new JTabbedPane();
   private ViewsGUI v = null;
   private DisplayFormatGUI df;
   private CutPanelGUI cp;
   private GeoRefGUI gr;
   private UserSettings usNew = new UserSettings();
   private boolean okay = false;

   public UserSettingsGUI() {
      this(true, true, true, true, false);
   }

   public UserSettingsGUI(boolean addViewsGUI, boolean addDisplayGUI,
					      boolean addCutPanelGUI, boolean addGeoRefGUI, boolean addApplyButton) {
      init(addViewsGUI, addDisplayGUI, addCutPanelGUI, addGeoRefGUI, addApplyButton);
   }


   public UserSettingsGUI(boolean addViewsGUI, boolean addDisplayGUI,
						  boolean addCutPanelGUI, boolean addGeoRefGUI) {

      init(addViewsGUI, addDisplayGUI, addCutPanelGUI, addGeoRefGUI, false);
   }


	private void init(boolean addViewsGUI, boolean addDisplayGUI, boolean addCutPanelGUI,  boolean addGeoRefGUI, boolean addApplyButton) {
		usNew = (UserSettings)Constants.USERSETTINGS.clone();
		int numAdded = 0;
		if (addViewsGUI) {
			v = new ViewsGUI();
			userSettingsTabs.add(v);
			userSettingsTabs.setTitleAt(numAdded++,"Visible Views");
		}
		if (addDisplayGUI) {
			df = new DisplayFormatGUI();
			userSettingsTabs.add(df);
			userSettingsTabs.setTitleAt(numAdded++,"Field Formatting");
		}
		
		if (addCutPanelGUI) {
			cp = new CutPanelGUI();
			userSettingsTabs.add(cp);
			userSettingsTabs.setTitleAt(numAdded++,"Cut-Panel Geometry");
		}
		if (addGeoRefGUI) {
			gr = new GeoRefGUI();
			userSettingsTabs.add(gr);
			userSettingsTabs.setTitleAt(numAdded++,"Geographic References");
		}
		
		userSettingsTabs.setOpaque(false);
		userSettingsTabs.setFont(BeanFonts.miFont);
		
		Object[] optSet1 = {"OK","Cancel"};
		Object[] options;
		
		options = optSet1;
		
		JOptionPane pane = new JOptionPane(userSettingsTabs, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
		                            	   null, options, options[0]);
		//pane.setBackground(new Color(200, 200, 200));
		dialog = pane.createDialog(null, "Options");
		dialog.pack();
		dialog.show();
		Object selectedValue = pane.getValue();
		if (options[0].equals(selectedValue)) {
			setOkay(true);
		}
		else if (options[1].equals(selectedValue)) {
			setOkay(false);
		}
   }


   public UserSettingsGUI(String sTitle) {
      this();
      //setTitle(sTitle);
   }


	public void updateSettings() {
		// update settings based upon state of UI
		if (v != null) {
			Constants.USERSETTINGS.setVisibleViewIds(v.getViewIds());
		}

		if (df != null) {
			Constants.USERSETTINGS.setGeoDisplayFormat(df.getGeoDisplayFormat());
			Constants.USERSETTINGS.setTimeDisplayFormat(df.getTimeDisplayFormat());
		}

		if (cp != null) {
			Constants.USERSETTINGS.setCutPanelSize(cp.getCutPanelSize());
			Constants.USERSETTINGS.setIndependentHandle(cp.isIndependentHandles());
			Constants.USERSETTINGS.setDisplayAxes(cp.isDisplayAxes());
		}
	}

   private void setOkay(boolean ans) {
      okay = ans;
   }
   
   public boolean getOkay() {
      return okay;
   }
      
   static public void main(String args[]) {
      JFrame jf = new JFrame();
      jf.setVisible(true);
      jf.setSize(500, 500);

      UserSettingsGUI usg = new UserSettingsGUI(true, true, true, false, true);
   }
}

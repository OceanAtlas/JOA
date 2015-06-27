/**
 * 
 */
package javaoceanatlas.utility;

import java.util.HashMap;
import javaoceanatlas.resources.JOAConstants;
import javax.swing.JCheckBox;

/**
 * @author oz
 *
 */
public class FeatureGroup {
	private String mID;
	private String mDisplayName;
	private HashMap<String, ManagedFeature> mFeatures = new HashMap<String, ManagedFeature>();
	private boolean mEnabled;
	private JCheckBox mMyJCheckBox;
	
	public FeatureGroup(String id, String name, boolean enabled) {
		mID = id;
		mDisplayName = name;
		mEnabled = enabled;
	}

	public void setID(String id) {
	  mID = id;
  }

	public String getID() {
	  return mID;
  }

	public void setDisplayName(String displayName) {
	  mDisplayName = displayName;
  }

	public String getDisplayName() {
	  return mDisplayName;
  }
	
	public HashMap<String, ManagedFeature> getFeatures() {
		return mFeatures;
	}
	
	public void addFeature(String id, ManagedFeature mf) {
		mFeatures.put(id, mf);
	}
	
	public boolean hasFeature(String featureID) {
		return mFeatures.get(featureID) != null;
	}
	
	public boolean isFeatureEnabled(String id) {
		// short circuit if the group is disabled
		if (!mEnabled) {
			return false;
		}
		
		if (mFeatures.get(id) != null) {
			return mFeatures.get(id).isEnabled();
		}
		return false;
	}

	public void setEnabled(boolean b) {
	  mEnabled = b;
	  mMyJCheckBox.setEnabled(b);
	  
	  // enable/disable the features in this group	private HashMap<String, ManagedFeature> mFeatures;
	  for (ManagedFeature mf : mFeatures.values()) {
	  	mf.setEnabled(b);
	  }
  }

	public boolean isEnabled() {
	  return mEnabled;
  }

	public void setMyJCheckBox(JCheckBox mMyJCheckBox) {
	  this.mMyJCheckBox = mMyJCheckBox;
  }

	public JCheckBox getMyJCheckBox() {
	  return mMyJCheckBox;
  }
	
	public void setSelected(boolean b) {
		mMyJCheckBox.setSelected(b);
	}
}

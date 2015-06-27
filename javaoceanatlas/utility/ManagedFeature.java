/**
 * 
 */
package javaoceanatlas.utility;

import javax.swing.JCheckBox;

/**
 * @author oz
 */
public class ManagedFeature {
	private String mID;
	private String mDisplayName;
	private String mVersion;
	private String mDevelopmentStatus;
	private boolean mEnabled;
	private boolean mVisible;
	private FeatureGroup mFeatureGroup;
	
	public ManagedFeature(FeatureGroup group, String id, String dname, String vers, String devStat, boolean enabled, boolean viz) {
		mFeatureGroup = group;
		mID = id;
		mDisplayName = dname;
		mVersion = vers;
		mDevelopmentStatus = devStat;
		mEnabled = enabled;
		mVisible = viz;
	}
	
	public void setID(String mID) {
	  this.mID = mID;
  }
	
	public String getID() {
	  return mID;
  }
	
	public void setDisplayName(String mDisplayName) {
	  this.mDisplayName = mDisplayName;
  }
	
	public String getDisplayName() {
	  return mDisplayName;
  }
	
	public void setVersion(String mVersion) {
	  this.mVersion = mVersion;
  }
	
	public String getVersion() {
	  return mVersion;
  }
	
	public void setDevelopmentStatus(String mDevelopmentStatus) {
	  this.mDevelopmentStatus = mDevelopmentStatus;
  }
	
	public String getDevelopmentStatus() {
	  return mDevelopmentStatus;
  }
	
	public void setEnabled(boolean mEnabled) {
	  this.mEnabled = mEnabled;
  }
	
	public boolean isEnabled() {
	  return mEnabled;
  }
	
	public void setVisible(boolean mVisible) {
	  this.mVisible = mVisible;
  }
	
	public boolean isVisible() {
	  return mVisible;
  }

	public void setFeatureGroup(FeatureGroup mFeatureGroup) {
	  this.mFeatureGroup = mFeatureGroup;
  }

	public FeatureGroup getFeatureGroup() {
	  return mFeatureGroup;
  }
}

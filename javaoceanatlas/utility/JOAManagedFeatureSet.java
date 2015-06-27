/**
 * 
 */
package javaoceanatlas.utility;

import java.util.HashMap;

/**
 * @author oz
 *
 */
public class JOAManagedFeatureSet {
	HashMap<String, FeatureGroup> mAllfeatureGroups;
	
	public JOAManagedFeatureSet() {
		mAllfeatureGroups = new HashMap<String, FeatureGroup>();
	}
	
	public void addFeatureGroup(String id, FeatureGroup fg) {
		mAllfeatureGroups.put(id, fg);
	}

	public boolean isFeatureEnabled(ManagedFeature mf) {
		if (mAllfeatureGroups.get(mf.getFeatureGroup().getID()) != null) {
			
		}
		return false;
	}
}

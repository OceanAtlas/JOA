package gov.noaa.pmel.eps2;

import java.util.*;

public class CompositeDbase extends Dbase {
	ArrayList mChildren = new ArrayList(100);
	
	public CompositeDbase() {
		super();
	}
	
	public void addChild(Dbase child) {
		mChildren.add(child);
		child.mParent = this;
	}
	
	public void removeChild(Dbase child) {
		if (this == child.mParent)
			child.mParent = null;
		mChildren.remove(child);
	}
	
	public Dbase getChild(int i) {
		return (Dbase)mChildren.get(i);
	}
	
	public int getNumChildren() {
		return mChildren.size();
	}
}
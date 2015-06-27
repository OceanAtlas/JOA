/**
 * 
 */
package javaoceanatlas.events;

import java.awt.Component;
import javaoceanatlas.ui.IsoBrowsingMode;
import javaoceanatlas.utility.NewInterpolationSurface;

/**
 * @author oz
 *
 */
public class LevelChange {
	private int mLevel;
	private double mLevelValue;
	private double mValueOnLevel;
	private NewInterpolationSurface mSurface = null;
	private Component mSource = null;
	private IsoBrowsingMode mBrowsingMode;
	
	public LevelChange(IsoBrowsingMode mode, int level, double levelval, double valonlevel, NewInterpolationSurface surf) {
		mBrowsingMode = mode;
		mLevel = level;
		mLevelValue = levelval;
		mValueOnLevel = valonlevel;
		mSurface = surf;
	}
		
	public LevelChange(IsoBrowsingMode mode, int level, double levelval, double valonlevel, NewInterpolationSurface surf, Component c) {
		mBrowsingMode = mode;
		mLevel = level;
		mLevelValue = levelval;
		mValueOnLevel = valonlevel;
		mSurface = surf;
		mSource = c;
	}
	
	public Component getEvtSource() {
		return mSource;
	}
	
	public NewInterpolationSurface getSurface() {
		return mSurface;
	}
	
	public double getLevelValue() {
		return mLevelValue;
	}
	
	public double getValueOnLevel() {
		return mValueOnLevel;
	}
	
	public int getLevel() {
		return mLevel;
	}

	/**
   * @param mStnOrContourLevel the mStnOrContourLevel to set
   */
  public void setBrowsingMode(IsoBrowsingMode isbm) {
	  this.mBrowsingMode = isbm;
  }

	/**
   * @return the mStnOrContourLevel
   */
  public IsoBrowsingMode getBrowsingMode() {
	  return mBrowsingMode;
  }

}

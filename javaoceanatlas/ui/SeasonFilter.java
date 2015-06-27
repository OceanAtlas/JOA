
package javaoceanatlas.ui;

import javaoceanatlas.classicdatamodel.Station;

/**
 * @author oz
 *
 */
public class SeasonFilter implements JOATimeFilter {
	private Seasons mSeason;
	
	public SeasonFilter(Seasons inSeason) {
		mSeason = inSeason;
	}

	public boolean test(Station sh) {
		Seasons tstSeason = Seasons.getSeason(sh.getDate().getYearday());
		if (tstSeason == mSeason)
			return true;
		else
			return false;
	}

	public void setSeason(Seasons mSeason) {
	  this.mSeason = mSeason;
  }

	public Seasons getSeason() {
	  return mSeason;
  }
}

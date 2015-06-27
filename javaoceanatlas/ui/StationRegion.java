/**
 * 
 */
package javaoceanatlas.ui;

import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.resources.JOAConstants;
import javaoceanatlas.specifications.ContourPlotSpecification;
import javaoceanatlas.ui.JOAContourPlotWindow.ContourPlotPanel;
import javaoceanatlas.utility.JOAVariable;

/**
 * @author oz
 *
 */
public class StationRegion {
	JOAVariable mInterpVar;
	int mSurfVarNum;
	private Station mStartStation;
	private Station mEndStation;
	private double mDeltaOrd;
	private double mDeltaDistanceKM;
	private double mDeltaLat;
	private double mDeltaLon;
	private double mDeltaTime;
	private double mXOffset;
	double mPixelsPerUnit = 0;
	private boolean mIsZoomed = false;
	ContourPlotSpecification mPlotSpec;
	FileViewer mFileViewer;
	int mOffset;

	public StationRegion(FileViewer fv, ContourPlotSpecification ps, JOAVariable interpVar, int surfVarNum, Station fs, Station ls) {
		mFileViewer = fv;
		mPlotSpec = ps;
		mSurfVarNum = surfVarNum;
		mInterpVar = interpVar;
		mStartStation = fs;
		mEndStation = ls;
		mOffset = mPlotSpec.getOffset();
	}
	
	public void setPlotSpec(ContourPlotSpecification ps) {
		mPlotSpec = ps;
		mOffset = ps.getOffset();
	}

	public void update(ContourPlotPanel contPlot) {
		computeDeltas();
		rescale(contPlot);
	}
	
	public boolean isZoomed(){ 
		return mIsZoomed;
	}
	
	public void setZoomed(boolean b){ 
		mIsZoomed = b;
	}

	public void computeDeltas() {
		// need to compute distance (kms), lat, etc from firstStation to last station
		if (mStartStation != null && mEndStation != null) {
			mDeltaOrd = mEndStation.mOrdinal - mStartStation.mOrdinal;
			mDeltaDistanceKM = mEndStation.mCumDist - mStartStation.mCumDist;
			mDeltaDistanceKM *= 1.852;
			mDeltaLat = Math.abs(mEndStation.getLat() - mStartStation.getLat());
			if (mEndStation.getLon() < 0 && mStartStation.getLon() > 0) {
				mDeltaLon = (mEndStation.getLon() + 360) - mStartStation.getLon();
			}
			else {
				mDeltaLon = Math.abs(mEndStation.getLon() - mStartStation.getLon());
			}
			if (mEndStation.getDate() != null && mStartStation.getDate() != null) {
				mDeltaTime = Math.abs(mEndStation.getDate().getTime() - mStartStation.getDate().getTime());
			}
			else {
				mDeltaTime = -99;
			}
		}
	}

	public boolean isStnInRegion(Station sh) {
		if (mOffset == JOAConstants.OFFSET_SEQUENCE) {
			if (sh.mOrdinal >= mStartStation.mOrdinal && sh.mOrdinal <= mEndStation.mOrdinal) { return true; }
		}
		else if (mOffset == JOAConstants.OFFSET_DISTANCE) {
			if (sh.mCumDist >= mStartStation.mCumDist && sh.mCumDist <= mEndStation.mCumDist) { return true; }
		}
		else if (mOffset == JOAConstants.OFFSET_LATITUDE) {
			if (sh.getLat() >= mStartStation.getLat() && sh.getLat() <= mEndStation.getLat()) { return true; }
		}
		else if (mOffset == JOAConstants.OFFSET_LONGITUDE) {
			if (mStartStation.getLon() > 0 && mEndStation.getLon() < 0) {
				double leftLon = mStartStation.getLon();
				double rightLon = mEndStation.getLon() + 360.0;
				double testLon = sh.getLon();
				if (testLon < 0) {
					testLon += 360.0;
				}
				if (testLon >= leftLon && testLon <= rightLon) {
					return true; 
				}
			}
			else {
				if (sh.getLon() >= mStartStation.getLon() && sh.getLon() <= mEndStation.getLon()) { return true; }
			}
		}
		else if (mOffset == JOAConstants.OFFSET_TIME && mStartStation.getDate() != null && mEndStation.getDate() != null && sh.getDate() != null) {
			if (sh.getDate().getTime() >= mStartStation.getDate().getTime()
			    && sh.getLat() <= mEndStation.getDate().getTime()) { return true; }
		}
		return false;
	}

	public double getXScale() {
		// pixels/km
		return mPixelsPerUnit;
	}

	public double getXOffset() {
		return 0.0;//mXOffset;
	}

	public void rescale(ContourPlotPanel mPlotPanel) {
		if (mOffset == JOAConstants.OFFSET_SEQUENCE) {
			mPixelsPerUnit = (double) (mPlotPanel.getSize().width - mPlotPanel.getLeftMargin() - mPlotPanel.getRightMargin())
			    / ((double) mDeltaOrd);
			mXOffset = mStartStation.mOrdinal;
		}
		else if (mOffset == JOAConstants.OFFSET_DISTANCE) {
			if (mPlotSpec.isAutoScaleXAxis()) {
				double w = mPlotPanel.getSize().width;
				double lm = mPlotPanel.getLeftMargin();
				double rm = mPlotPanel.getRightMargin();
				mPixelsPerUnit = (w - lm - rm) / mDeltaDistanceKM;
			}
			else {
				mPixelsPerUnit = 1.0 / (mPlotSpec.getXAxisScale()/* / 1.852*/);
			}
			// in kms 
			mXOffset = mStartStation.mCumDist * 1.852;
		}
		else if (mOffset == JOAConstants.OFFSET_LATITUDE) {
			if (mPlotSpec.isAutoScaleXAxis()) {
				mPixelsPerUnit = (mPlotPanel.getSize().width - mPlotPanel.getLeftMargin() - mPlotPanel.getRightMargin())
				    / mDeltaLat;
			}
			else {
				mPixelsPerUnit = 1 / (mPlotSpec.getXAxisScale());
			}
			mXOffset = mStartStation.getLat();
		}
		else if (mOffset == JOAConstants.OFFSET_LONGITUDE) {
			if (mPlotSpec.isAutoScaleXAxis()) {
				mPixelsPerUnit = (mPlotPanel.getSize().width - mPlotPanel.getLeftMargin() - mPlotPanel.getRightMargin())
				    / mDeltaLon;
			}
			else {
				mPixelsPerUnit = 1 / (mPlotSpec.getXAxisScale());
			}
			mXOffset = mStartStation.getLon();
		}
		else if (mOffset == JOAConstants.OFFSET_TIME) {
			if (mPlotSpec.isAutoScaleXAxis() && mDeltaTime > 0) {
				mPixelsPerUnit = (mPlotPanel.getSize().width - mPlotPanel.getLeftMargin() - mPlotPanel.getRightMargin())
				    / mDeltaTime;
			}
			else {
				mPixelsPerUnit = 1 / (mPlotSpec.getXAxisScale());
			}
			mXOffset = mStartStation.getDate().getTime();
		}
	}

	public int getNumStns() {
		int stnCnt = 0;
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section) of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				int iPos = sech.getVarPos(mInterpVar.getVarName(), false);
				int sPos = sech.getVarPos(mFileViewer.mAllProperties[mSurfVarNum].getVarLabel(), false);

				if (sPos < 0 || iPos < 0) {
					continue;
				}

				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					if (!isStnInRegion(sh)) {
						continue;
					}
					stnCnt++;

				}
			}
		}
		return stnCnt;
	}

	public void setStartStation(Station mStartStation) {
		this.mStartStation = mStartStation;
	}

	public Station getStartStation() {
		return mStartStation;
	}

	public void setEndStation(Station mEndStation) {
		this.mEndStation = mEndStation;
	}

	public Station getEndStation() {
		return mEndStation;
	}
	
	public double getMinLat() {
		return mStartStation.getLat();
	}
	
	public double getMaxLat() {
		return mEndStation.getLat();
	}

	public double getLeftLon() {
		return mStartStation.getLon();
	}

	public double getMinTime() {
		if (mStartStation.getDate() != null) {
			return mStartStation.getDate().getTime();
		}
		else {
			return -99;
		}
	}

	public int getMinOrd() {
		return mStartStation.mOrdinal;
	}

	public double getDeltaDistance() {
		return mDeltaDistanceKM;
	}

	public double getDeltaLat() {
		return mDeltaLat;
	}

	public double getDeltaLon() {
		return mDeltaLon;
	}

	public double getDeltaTime() {
		return mDeltaTime;
	}
}
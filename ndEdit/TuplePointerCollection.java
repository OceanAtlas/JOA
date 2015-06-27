/*
 * $Id: TuplePointerCollection.java,v 1.33 2005/10/18 23:44:53 oz Exp $
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

import java.util.*;
import gov.noaa.pmel.util.*;
import java.beans.*;

 /**
 * @author  OZ 
 * @version 1.0 01/13/00
 */
public class TuplePointerCollection implements PointerCollection {
  private String title;
  private String additionalField_Ids;
  private double[] latArr1;
  private double[] latArr2;
  private double[] lonArr1;
  private double[] lonArr2;
  private double[] depthArr1;
  private double[] depthArr2;
  private double[] timeArr1;
  private double[] timeArr2;
  private String[] URD;
  private String[] fileNameArr;
  private String[] pathArr;
  private boolean[] pathIsRelativeArr;
  private String[] dataTypeArr;
  private String[] cruiseArr;
  private String[] castArr;
  private double[] deltaArr;
  private String[] additionalFields;
  private double[] minMaxDepth;
  private double[] minMaxLat = null;
  private double[] minMaxLon = null;
  private double[] minMaxELon = null;
  private double[] minMaxWLon = null;
  private double[] minMaxTime;
  private long[] minMaxMonth;
  // Arrays can be sorted if needed
  private double[] latArr1Sorted;
  private double[] lonArr1Sorted;
  private double[] depthArr1Sorted;
  private double[] timeArr1Sorted;
  private int[] latArr1SortedIndices;
  private int[] lonArr1SortedIndices;
  private int[] depthArr1SortedIndices;
  private int[] timeArr1SortedIndices;
  private Boolean mCrossed180 = null;
  private int[] isDeleted;
  private boolean[] isSelected;
  private int currDeletionLevel = 0;
  private Object[] mReferences;
  private Class mRefType;
  private boolean mBatchMode = false;
  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private int mVisible = 0;
  private int mSelected = 0;
  private boolean mIsArgo = false;
  private String[] mExtraArr1;
  private String[] mExtraArr2;
  private String[] mExtraArr3;
  private String[] mExtraArr4;
  private String[] mExtraArr5;
  private String[] mExtraArr6;
  private String[] mExtraArr7;
  private String[] mExtraArr8;
  private String[] mExtraArr9;
  private String[] mExtraArr10;

	public TuplePointerCollection(double[] latArr1, double[] latArr2, double[] lonArr1,
						   	 double[] lonArr2, double[] depthArr1, double[] depthArr2,
						     double[] timeArr1, double[] timeArr2, String[] URD,
						     String[] fileName, String[] path, boolean[] pathisrel, String[] dType, String[] cruises,
						     String[] casts, double[] deltas, Object[] refs) {
						     
		this(latArr1, latArr2, lonArr1, lonArr2, depthArr1, depthArr2, timeArr1, timeArr2, URD,
		     fileName, path, pathisrel, dType, cruises, casts, deltas);
		this.mReferences = refs;
		if (refs != null)
			mRefType = refs.getClass();
		isDeleted = new int[latArr1.length];
		isSelected = new boolean[latArr1.length];
	}

	public TuplePointerCollection(double[] latArr1, double[] latArr2, double[] lonArr1,
						   	 double[] lonArr2, double[] depthArr1, double[] depthArr2,
						   	 double[] timeArr1, double[] timeArr2, Object[] refs) {
		this(latArr1, latArr2, lonArr1, lonArr2, depthArr1, depthArr2, timeArr1, timeArr2, null,
		     null, null, null, null, null, null, null);  

		this.mReferences = refs;
		mRefType = refs.getClass();
		isDeleted = new int[latArr1.length];
		isSelected = new boolean[latArr1.length];
	}
											
	public TuplePointerCollection(double[] latArr1, double[] latArr2, double[] lonArr1,
						   	 double[] lonArr2, double[] depthArr1, double[] depthArr2,
						   	 double[] timeArr1, double[] timeArr2, String[] cruises, String[] casts, String[] filenames,
						   	 String[] paths, boolean[] pathisrel, String[] extras1, String[] extras2, String[] extras3,
						   	 String[] extras4, String[] extras5, String[] extras6, String[] extras7,
						   	 String[] extras8, String[] extras9, String[] extras10) {
		this(latArr1, latArr2, lonArr1, lonArr2, depthArr1, depthArr2, timeArr1, timeArr2, null,
		     filenames, paths, pathisrel, null, cruises, casts, null);
		isDeleted = new int[latArr1.length];
		isSelected = new boolean[latArr1.length];
        this.mExtraArr1 = extras1;
        this.mExtraArr2 = extras2;
        this.mExtraArr3 = extras3;
        this.mExtraArr4 = extras4;
        this.mExtraArr5 = extras5;
        this.mExtraArr6 = extras6;
        this.mExtraArr7 = extras7;
        this.mExtraArr8 = extras8;
        this.mExtraArr9 = extras9;
        this.mExtraArr10 = extras10;
	}

	public TuplePointerCollection() {}

	public TuplePointerCollection(double[] latArr1, double[] latArr2, double[] lonArr1,
						     double[] lonArr2, double[] depthArr1, double[] depthArr2,
						     double[] timeArr1, double[] timeArr2, String[] URD,
						     String[] fileName, String[] path, boolean[] pathIsRel, String[] dType,
						     String[] cruises, String casts[], double[] deltas) {

     // check array sizes
     int s = latArr1.length;
     if (((latArr2 != null) && (s != latArr2.length)) ||
         ((lonArr1 != null) && (s != lonArr1.length)) ||
         ((lonArr2 != null) && (s != lonArr2.length)) ||
         ((depthArr1 != null) && (s != depthArr1.length)) ||
         ((depthArr2 != null) && (s != depthArr2.length)) ||
         ((timeArr1 != null) && (s != timeArr1.length)) ||
         ((timeArr2 != null) && (s != timeArr2.length))) {
			System.out.println("Exception: Pointer Collection: arrays must all be same size");
     }
     else {
        this.latArr1 = latArr1;
        this.latArr2 = latArr2;
        this.lonArr1 = lonArr1;
        this.lonArr2 = lonArr2;
        this.depthArr1 = depthArr1;
        this.depthArr2 = depthArr2;
        this.timeArr1 = timeArr1;
        this.timeArr2 = timeArr2;
        this.URD = URD;
        this.fileNameArr = fileName;
        this.pathArr = path;
		this.pathIsRelativeArr = pathIsRel;
        this.dataTypeArr = dType;
        this.cruiseArr = cruises;
        this.castArr = casts;
        this.deltaArr = deltas;
		isDeleted = new int[s];
		isSelected = new boolean[s];
     }
  }
  
  public TuplePointerCollection(double latMin, double latMax, double lonMin, double lonMax,
  								double zMin,   double zMax,   GeoDate tMin,   GeoDate tMax) {
		latArr1 = new double[1];
		latArr2 = new double[1];
		lonArr1 = new double[1];
		lonArr2 = new double[1];
		depthArr1 = new double[1];
		depthArr2 = new double[1];
		timeArr1 = new double[1];
		timeArr2 = new double[1];
  		isDeleted = new int[1];
  		isSelected = new boolean[1];
		
		latArr1[0] = latMin;
		latArr2[0] = latMax;
		lonArr1[0] = lonMin;
		lonArr2[0] = lonMax;
		depthArr1[0] = zMin;
		depthArr2[0] = zMax;
		timeArr1[0] = (double)tMin.getTime();
		timeArr2[0] = (double)tMax.getTime();
  }

  	public TuplePointerCollection(TuplePointerCollection pc) {
  		// needs to deal with references
		int size = pc.getSize();
		latArr1 = new double[size];
		latArr2 = new double[size];
		lonArr1 = new double[size];
		lonArr2 = new double[size];
		depthArr1 = new double[size];
		depthArr2 = new double[size];
		timeArr1 = new double[size];
		timeArr2 = new double[size];
		fileNameArr = new String[size];
		pathArr = new String[size];
		pathIsRelativeArr = new boolean[size];
		dataTypeArr = new String[size];
		cruiseArr = new String[size];
		castArr = new String[size];
		deltaArr = new double[size];
		isDeleted = new int[size];
		isSelected = new boolean[size];

		for (int i=0; i<size; i++) {
			latArr1[i] = pc.latArr1[i];
			latArr2[i] = pc.latArr2[i];
			lonArr1[i] = pc.lonArr1[i];
			lonArr2[i] = pc.lonArr2[i];
			depthArr1[i] = pc.depthArr1[i];
			depthArr2[i] = pc.depthArr2[i];
			timeArr1[i] = pc.timeArr1[i];
			timeArr2[i] = pc.timeArr2[i];
			fileNameArr[i] = pc.fileNameArr[i];
			pathArr[i] = pc.pathArr[i];
			pathIsRelativeArr[i] = pc.pathIsRelativeArr[i];
			dataTypeArr[i] = pc.dataTypeArr[i];
			cruiseArr[i] = pc.cruiseArr[i];
			castArr[i] = pc.castArr[i];
			deltaArr[i] = pc.deltaArr[i];
			isDeleted[i] = pc.isDeleted[i];
			isSelected[i] = pc.isSelected[i];
		}

  		minMaxDepth = null;
  		minMaxLat = null;
  		minMaxLon = null;
  		minMaxTime = null;
  		minMaxMonth = null;
  		minMaxWLon = null;
  		minMaxELon = null;
  		mCrossed180 = null;
	}

	public TuplePointerCollection getFilteredPointerCollection() {
		TuplePointerCollection pc = new TuplePointerCollection();
		int size = this.getSize();
		int c = 0;
		for (int i=0; i<size; i++) {
			if (this.isDeleted[i] == 0)
				c++;
		}

		if (c == 0)
			return pc;

		pc.latArr1 = new double[c];
		pc.latArr2 = new double[c];
		pc.lonArr1 = new double[c];
		pc.lonArr2 = new double[c];
		pc.depthArr1 = new double[c];
		pc.depthArr2 = new double[c];
		pc.timeArr1 = new double[c];
		pc.timeArr2 = new double[c];
		pc.fileNameArr = new String[c];
		pc.pathArr = new String[c];
		pc.pathIsRelativeArr = new boolean[c];
		pc.dataTypeArr = new String[c];
		pc.cruiseArr = new String[c];
		pc.castArr = new String[c];
		pc.deltaArr = new double[c];
		pc.isDeleted = new int[c];
		pc.isSelected = new boolean[c];
		pc.mReferences = new Object[c];

		c = 0;
		for (int i=0; i<size; i++) {
			if (this.isDeleted[i] == 0) {
				pc.latArr1[c] = this.latArr1[i];
				if(latArr2 != null) pc.latArr2[c] = this.latArr2[i];
				pc.lonArr1[c] = this.lonArr1[i];
				if(lonArr2 != null) pc.lonArr2[c] = this.lonArr2[i];
				pc.depthArr1[c] = this.depthArr1[i];
				if(depthArr2 != null) pc.depthArr2[c] = this.depthArr2[i];
				pc.timeArr1[c] = this.timeArr1[i];
				if(timeArr2 != null) pc.timeArr2[c] = this.timeArr2[i];
//				if(monthArr1 != null) pc.monthArr1[c] = this.monthArr1[i];
//				if(monthArr2 != null) pc.monthArr2[c] = this.monthArr2[i];
				if (this.fileNameArr != null)
					pc.fileNameArr[c] = this.fileNameArr[i];
				if (this.pathArr != null)
					pc.pathArr[c] = this.pathArr[i];
				if (this.pathIsRelativeArr != null)
					pc.pathIsRelativeArr[c] = this.pathIsRelativeArr[i];
				if (this.dataTypeArr != null)
					pc.dataTypeArr[c] = this.dataTypeArr[i];
				if (this.cruiseArr != null)
					pc.cruiseArr[c] = this.cruiseArr[i];
				if (this.castArr != null)
					pc.castArr[c] = this.castArr[i];
				if (this.deltaArr != null)
					pc.deltaArr[c] = this.deltaArr[i];
				pc.isDeleted[c] = this.isDeleted[i];
				pc.isSelected[c] = this.isSelected[i];
				pc.mReferences[c] = this.mReferences[i];
				c++;
			}
		}
		pc.setPCTitle(this.getPCTitle());
		return pc;
	}

	public TuplePointerCollection getSelectedPointerCollection() {
		TuplePointerCollection pc = new TuplePointerCollection();
		int size = this.getSize();
		int c = 0;
		for (int i=0; i<size; i++) {
			if (this.isDeleted[i] == 0 && isSelected[i])
				c++;
		}

		if (c == 0)
			return pc;

		pc.latArr1 = new double[c];
		pc.latArr2 = new double[c];
		pc.lonArr1 = new double[c];
		pc.lonArr2 = new double[c];
		pc.depthArr1 = new double[c];
		pc.depthArr2 = new double[c];
		pc.timeArr1 = new double[c];
		pc.timeArr2 = new double[c];
		pc.fileNameArr = new String[c];
		pc.pathArr = new String[c];
		pc.pathIsRelativeArr = new boolean[c];
		pc.dataTypeArr = new String[c];
		pc.cruiseArr = new String[c];
		pc.castArr = new String[c];
		pc.deltaArr = new double[c];
		pc.isDeleted = new int[c];
		pc.isSelected = new boolean[c];
		pc.mReferences = new Object[c];

		c = 0;
		for (int i=0; i<size; i++) {
			if (this.isDeleted[i] == 0 && isSelected[i]) {
				pc.latArr1[c] = this.latArr1[i];
				if(latArr2 != null) pc.latArr2[c] = this.latArr2[i];
				pc.lonArr1[c] = this.lonArr1[i];
				if(lonArr2 != null) pc.lonArr2[c] = this.lonArr2[i];
				pc.depthArr1[c] = this.depthArr1[i];
				if(depthArr2 != null) pc.depthArr2[c] = this.depthArr2[i];
				pc.timeArr1[c] = this.timeArr1[i];
				if(timeArr2 != null) pc.timeArr2[c] = this.timeArr2[i];
//				if(this.monthArr1 != null) pc.monthArr1[c] = this.monthArr1[i];
//				if(this.monthArr2 != null) pc.monthArr2[c] = this.monthArr2[i];
				if (this.fileNameArr != null)
					pc.fileNameArr[c] = this.fileNameArr[i];
				if (this.pathArr != null)
					pc.pathArr[c] = this.pathArr[i];
				if (this.pathIsRelativeArr != null)
					pc.pathIsRelativeArr[c] = this.pathIsRelativeArr[i];
				if (this.dataTypeArr != null)
					pc.dataTypeArr[c] = this.dataTypeArr[i];
				if (this.cruiseArr != null)
					pc.cruiseArr[c] = this.cruiseArr[i];
				if (this.castArr != null)
					pc.castArr[c] = this.castArr[i];
				if (this.deltaArr != null)
					pc.deltaArr[c] = this.deltaArr[i];
				pc.isDeleted[c] = this.isDeleted[i];
				pc.isSelected[c] = this.isSelected[i];
				pc.mReferences[c] = this.mReferences[i];
				c++;
			}
		}
		pc.setPCTitle(this.getPCTitle());
		return pc;
	}

	public void addPointers(PointerCollection pc) {
		int oldSize = this.getSize();
		int newSize = this.getSize() + pc.getSize();
		double[] tlatArr1 = null;
		double[] tlatArr2 = null;
		double[] tlonArr1 = null;
		double[] tlonArr2 = null;
		double[] tdepthArr1 = null;
		double[] tdepthArr2 = null;
		double[] ttimeArr1 = null;
		double[] ttimeArr2 = null;
		String[] tfileNameArr = null;
		String[] tpathArr = null;
		boolean[] tpathIsRelativeArr = null;
		String[] tdataTypeArr = null;
		String[] tcruiseArr = null;
		String[] tcastArr = null;
		double[] tdeltaArr = null;
		int[] tisdeleted = new int[newSize];
		boolean[] tisselected = new boolean[newSize];

		tlatArr1 = new double[newSize];
		tlatArr2 = new double[newSize];
		tlonArr1 = new double[newSize];
		tlonArr2 = new double[newSize];
		tdepthArr1 = new double[newSize];
		tdepthArr2 = new double[newSize];
		ttimeArr1 = new double[newSize];
		ttimeArr2 = new double[newSize];
		if (fileNameArr != null)
			tfileNameArr = new String[newSize];
		if (pathArr != null)
			tpathArr = new String[newSize];
		if (pathIsRelativeArr != null)
			tpathIsRelativeArr = new boolean[newSize];
		if (dataTypeArr != null)
			tdataTypeArr = new String[newSize];
		if (cruiseArr != null)
			tcruiseArr = new String[newSize];
		if (castArr != null)
			tcastArr = new String[newSize];
		if (deltaArr != null)
			tdeltaArr = new double[newSize];

		for (int i=0; i<oldSize; i++) {
			tlatArr1[i] = latArr1[i];
			tlatArr2[i] = latArr2[i];
			tlonArr1[i] = lonArr1[i];
			tlonArr2[i] = lonArr2[i];
			tdepthArr1[i] = depthArr1[i];
			tdepthArr2[i] = depthArr2[i];
			ttimeArr1[i] = timeArr1[i];
			ttimeArr2[i] = timeArr2[i];
			if (fileNameArr != null)
				tfileNameArr[i] = fileNameArr[i];

			if (pathArr != null)
				tpathArr[i] = pathArr[i];
				
			if (pathIsRelativeArr != null)
				tpathIsRelativeArr[i] = pathIsRelativeArr[i];

			if (dataTypeArr != null)
				tdataTypeArr[i] = dataTypeArr[i];

			if (cruiseArr != null)
				tcruiseArr[i] = cruiseArr[i];

			if (deltaArr != null)
				tdeltaArr[i] = deltaArr[i];

			if (castArr != null)
				tcastArr[i] = castArr[i];

			tisdeleted[i] = isDeleted[i];
			tisselected[i] = isSelected[i];
		}

		double[] ttlatArr1 = null;
		double[] ttlatArr2 = null;
		double[] ttlonArr1 = null;
		double[] ttlonArr2 = null;
		double[] ttdepthArr1 = null;
		double[] ttdepthArr2 = null;
		double[] tttimeArr1 = null;
		double[] tttimeArr2 = null;
		String[] ttfileNameArr = null;
		String[] ttpathArr = null;
		String[] ttdataTypeArr = null;
		String[] ttcruiseArr = null;
		String[] ttcastArr = null;
		double[] ttdeltaArr = null;
		int[] tisdeleted2 = pc.getIsDeletedArr();
		boolean[] tisselected2 = pc.getIsSelectedArr();

		ttlatArr1 = pc.getLatArr1();
		ttlatArr2 = pc.getLatArr2();
		ttlonArr1 = pc.getLonArr1();
		ttlonArr2 = pc.getLonArr2();
		ttdepthArr1 = pc.getDepthArr1();
		ttdepthArr2 = pc.getDepthArr2();
		tttimeArr1 = pc.getTimeArr1();
		tttimeArr2 = pc.getTimeArr2();
		if (pc.getFileNameArr() != null)
			ttfileNameArr = pc.getFileNameArr();
		if (pc.getPathArr() != null)
			ttpathArr = pc.getPathArr();
		if (pc.getDataTypeArr() != null)
			ttdataTypeArr = pc.getDataTypeArr();
		if (pc.getCruiseArr() != null)
			ttcruiseArr = pc.getCruiseArr();
		if (pc.getCastArr() != null)
			ttcastArr = pc.getCastArr();
		if (pc.getDeltaArr() != null)
			ttdeltaArr = pc.getDeltaArr();

		for (int i=0; i<pc.getSize(); i++) {
			tlatArr1[oldSize + i] = ttlatArr1[i];
			tlatArr2[oldSize + i] = ttlatArr2[i];
			tlonArr1[oldSize + i] = ttlonArr1[i];
			tlonArr2[oldSize + i] = ttlonArr2[i];
			tdepthArr1[oldSize + i] = ttdepthArr1[i];
			tdepthArr2[oldSize + i] = ttdepthArr2[i];
			ttimeArr1[oldSize + i] = tttimeArr1[i];
			ttimeArr2[oldSize + i] = tttimeArr2[i];
			if (ttfileNameArr != null)
				tfileNameArr[oldSize + i] = ttfileNameArr[i];
			if (tpathArr != null)
				tpathArr[oldSize + i] = ttpathArr[i];
			if (ttdataTypeArr != null)
				tdataTypeArr[oldSize + i] = ttdataTypeArr[i];
			if (ttdeltaArr != null)
				tdeltaArr[oldSize + i] = ttdeltaArr[i];
			if (ttcruiseArr != null)
				tcruiseArr[oldSize + i] = ttcruiseArr[i];
			if (ttcastArr != null)
				tcastArr[oldSize + i] = ttcastArr[i];
			tisdeleted[oldSize + i] = tisdeleted2[i];
			tisselected[oldSize + i] = tisselected2[i];
		}

		latArr1 = null;
		latArr2 = null;
		lonArr1 = null;
		lonArr2 = null;
		depthArr1 = null;
		depthArr2 = null;
		timeArr1 = null;
		timeArr2 = null;
		fileNameArr = null;
		pathArr = null;
		pathIsRelativeArr = null;
		dataTypeArr = null;
		cruiseArr = null;
		castArr = null;
		deltaArr = null;
		isDeleted = null;
		isSelected = null;

		latArr1 = tlatArr1;
		latArr2 = tlatArr2;
		lonArr1 = tlonArr1;
		lonArr2 = tlonArr2;
		depthArr1 = tdepthArr1;
		depthArr2 = tdepthArr2;
		timeArr1 = ttimeArr1;
		timeArr2 = ttimeArr2;
		if (tfileNameArr != null)
			fileNameArr = tfileNameArr;
		if (tpathArr != null)
			pathArr = tpathArr;
		if (tpathIsRelativeArr != null)
			pathIsRelativeArr = tpathIsRelativeArr;
		if (tdataTypeArr != null)
			dataTypeArr = tdataTypeArr;
		if (tcruiseArr != null)
			cruiseArr = tcruiseArr;
		if (tcastArr != null)
			castArr = tcastArr;
		if (tdeltaArr != null)
			deltaArr = tdeltaArr;
		isDeleted = tisdeleted;
		isSelected = tisselected;
  		minMaxDepth = null;
  		minMaxLat = null;
  		minMaxLon = null;
  		minMaxTime = null;
  		minMaxMonth = null;
  		minMaxWLon = null;
  		minMaxELon = null;
  		mCrossed180 = null;
	}

	public String getPCTitle() {
		return title;
	}

	public void setPCTitle(String t) {
		title = new String(t);
	}

	public int getSize() {
		if (latArr1 != null)
			return latArr1.length;
		else
			return 0;
	}

	public void resetSizes() {
		minMaxDepth = null;
		minMaxLat = null;
		minMaxLon = null;
		minMaxTime = null;
		minMaxMonth = null;
		minMaxWLon = null;
		minMaxELon = null;
		mCrossed180 = null;
  		mVisible = 0;
  		mSelected = 0;
	}

	public String toString() {
		return("Num Pointers: " + getSize());
	}

	public String getString(int i) {
  		String outStr = new String("");
	 	if (this.isArgo()) {
			String crsID = cruiseArr[i];
			String cast = castArr[i];
			String url = pathArr[i];
			
			String file = "";
            if (mExtraArr1 != null)
            	file = mExtraArr1[i];
            	
            String ocean = "";
            if (mExtraArr2 != null)
            	ocean = mExtraArr2[i];
			
			String posqc = "";
            if (mExtraArr3 != null)
            	posqc = mExtraArr3[i];
			
			String timeqc = "";
            if (mExtraArr4 != null)
            	timeqc = mExtraArr4[i];
           
			String datactr = "";
            if (mExtraArr5 != null)
            	 datactr = mExtraArr5[i];
            
			String datamode = "";
            if (mExtraArr6 != null)
            	datamode = mExtraArr6[i];
            
			String numlevels = "";
            if (mExtraArr7 != null)
            	numlevels = mExtraArr7[i];
            
			String numparams = "";
            if (mExtraArr8 != null)
            	numparams = mExtraArr8[i];
            
			String params = "";
            if (mExtraArr9 != null)
            	params = mExtraArr9[i];
            	
            if (crsID != null)
            	outStr += crsID + "\t";
            if (cast != null)
            	outStr += cast + "\t";
            if (url != null)
            	outStr += url + "\t";
            if (file != null)
            	outStr += file + "\t";
            if (ocean != null)
            	outStr += ocean + "\t";
            if (posqc != null)
            	outStr += posqc + "\t";
            if (timeqc != null)
            	outStr += timeqc + "\t";
            if (datactr != null)
            	outStr += datactr + "\t";
            if (datamode != null)
            	outStr += datamode + "\t";
            if (numlevels != null)
            	outStr += numlevels + "\t";
            if (params != null)
            	outStr += params;
            outStr += "\n";
	 	}
	 	else {
	 		String urd = null;
	 		if (URD != null)
	  			urd = URD[i];
	  			
	 		String fn = null;
	 		if (fileNameArr != null)
	 			fn = fileNameArr[i];
	 		
	 		String pth = null;
	 		if (pathArr != null)
	 			pth = pathArr[i];
	 		
	 		String dt = null;
	 		if (dataTypeArr != null)
	 			dt = dataTypeArr[i];
	 			
	  		String crs = null;
	  		if (cruiseArr != null)
	  			crs = cruiseArr[i];
	  			
	  		String cst = null;
	  		if (castArr != null)
	  			cst = castArr[i];
	  			
            if (fn != null)
            	outStr += fn + "\t";
            if (dt != null)
            	outStr += dt + "\t";
            if (crs != null)
            	outStr += crs + "\t";
            if (cst != null)
            	outStr += cst + "\t";
            if (urd != null)
            	outStr += urd;
            outStr += "\n";
	 	}
		return outStr;
	}

	public double[] getDeltaArr() {
		return deltaArr;
	}

	public String[] getCastArr() {
		return castArr;
	}

	public String[] getCruiseArr() {
	return cruiseArr;
	}

	public String[] getDataTypeArr() {
		return dataTypeArr;
	}

	public String[] getPathArr() {
		return pathArr;
	}

	public String[] getFileNameArr() {
		return fileNameArr;
	}

	public double[] getLatArr1() {
		return latArr1;
	}

	public double[] getLatArr2() {
		return latArr2;
	}

	public int getNumLats() {
	return latArr1.length;
	}

	public int getNumLons() {
	return lonArr1.length;
	}

	public int getNumDepths() {
	return depthArr1.length;
	}

	public int getNumTimes() {
	return timeArr1.length;
	}


	public double getLat1(int i) {
		return latArr1[i];
	}

	public double getLat2(int i) {
		return latArr2[i];
	}

	public double getLon1(int i) {
		return lonArr1[i];
	}

	public double getLon2(int i) {
		return lonArr2[i];
	}

	public double getZ1(int i) {
		return depthArr1[i];
	}

	public double getZ2(int i) {
		return depthArr2[i];
	}

	public double getT1(int i) {
		return timeArr1[i];
	}

	public double getT2(int i) {
		return timeArr2[i];
	}


  // ---------------------------------------------------------
  //
  public double[] getLonArr1() {
    return lonArr1;
  }
  // ---------------------------------------------------------
  //
  public double[] getLonArr2() {
    return lonArr2;
  }

  public double[] getDepthArr1() {
    return depthArr1;
  }

  public double[] getDepthArr2() {
    return depthArr2;
  }

	public double[] getTimeArr1() {
		return timeArr1;
	}

	public double[] getTimeArr2() {
		return timeArr2;
	}

	public int[] getIsDeletedArr() {
		return isDeleted;
	}

	public boolean[] getIsSelectedArr() {
		return isSelected;
	}
	
	public boolean isLatScaler() {
		if (getLatArr2() != null)
			return false;
		return true;
	}
	
	public boolean isLonScaler() {
		if (getLonArr2() != null)
			return false;
		return true;
	}
	
	public boolean isDepthScaler() {
		if (getDepthArr2() != null)
			return false;
		return true;
	}
	
	public boolean isTimeScaler() {
		if (getTimeArr2() != null)
			return false;
		return true;
	}

//-----------------------------------------------------------
// MIN / MAX Section
//-----------------------------------------------------------

	public double getMinLat() {
		double[] m = getMinMaxLat();
		return m[0];
	}

  public double getMaxLat() {
     double[] m = getMinMaxLat();
     return m[1];
  }

  public double[] getMinMaxLat() {
     double mn = 90;
     double mx = -90;
     if (minMaxLat == null) {
		minMaxLat = new double[2];
		if (latArr1 != null) {
			int len = latArr1.length;
		   for (int i = 0; i < len; i++) {
		      mx = Math.max(latArr1[i], mx);
		      mn = Math.min(latArr1[i], mn);
		   }
		}
		if (latArr2 != null) {
			int len = latArr1.length;
		   for (int i = 0; i < len; i++) {
		      mx = Math.max(latArr2[i], mx);
		      mn = Math.min(latArr2[i], mn);
		   }
		}
		minMaxLat[0] = mn;
		minMaxLat[1] = mx;
     }
     return minMaxLat;
  }

  public double getMinLon() {
     double[] m = getMinMaxLon();
     return m[0];
  }

  public double getMaxLon() {
     double[] m = getMinMaxLon();
     return m[1];
  }

  public double getMinWLon() {
     double[] m = getMinMaxWLon();
     return m[0];
  }

  public double getMaxWLon() {
     double[] m = getMinMaxWLon();
     return m[1];
  }

  public double getMinELon() {
     double[] m = getMinMaxELon();
     return m[0];
  }

  public double getMaxELon() {
     double[] m = getMinMaxELon();
     return m[1];
  }

	public double[] getMinMaxLon() {
		double mn = 360;
		double mx = -360;
		if (minMaxLon == null) {
			//minMaxLon = null;
			minMaxLon = new double[2];
			if (lonArr1 != null) {
				for (int i = 0; i < lonArr1.length; i++) {
					mx = Math.max(lonArr1[i], mx);
					mn = Math.min(lonArr1[i], mn);
				}
			}
			if (lonArr2 != null) {
				for (int i = 0; i < lonArr2.length; i++) {
					mx = Math.max(lonArr2[i], mx);
					mn = Math.min(lonArr2[i], mn);
				}
			}
			minMaxLon[0] = mn;
			minMaxLon[1] = mx;
		}
		return minMaxLon;
	}

	public double[] getMinMaxWLon() {
		double mn = 0;
		double mx = -180;
		if (minMaxWLon == null) {
			minMaxWLon = null;
			minMaxWLon = new double[2];
			if (lonArr1 != null) {
				for (int i = 0; i < lonArr1.length; i++) {
					if (lonArr1[i] < 0) {
						mx = Math.max(lonArr1[i], mx);
						mn = Math.min(lonArr1[i], mn);
					}
				}
			}
			if (lonArr2 != null) {
				for (int i = 0; i < lonArr2.length; i++) {
					if (lonArr2[i] < 0) {
						mx = Math.max(lonArr2[i], mx);
						mn = Math.min(lonArr2[i], mn);
					}
				}
			}
			minMaxWLon[0] = mn;
			minMaxWLon[1] = mx;
		}
		return minMaxWLon;
	}

	public double[] getMinMaxELon() {
		double mn = 360;
		double mx = 0;
		if (minMaxELon == null) {
			minMaxELon = null;
			minMaxELon = new double[2];
			if (lonArr1 != null) {
				for (int i = 0; i < lonArr1.length; i++) {
					if (lonArr1[i] >= 0) {
						mx = Math.max(lonArr1[i], mx);
						mn = Math.min(lonArr1[i], mn);
					}
				}
			}
			if (lonArr2 != null) {
				for (int i = 0; i < lonArr2.length; i++) {
					if (lonArr2[i] >= 0) {
						mx = Math.max(lonArr2[i], mx);
						mn = Math.min(lonArr2[i], mn);
					}
				}
			}
			minMaxELon[0] = mn;
			minMaxELon[1] = mx;
		}
		return minMaxELon;
	}

	public boolean crosses180() {
		if (mCrossed180 != null)
			return mCrossed180.booleanValue();
		if (lonArr1 != null) {
			for (int i = 1; i < lonArr1.length-1; i++) {
				if ((lonArr1[i] >= -180 && lonArr1[i] < -178) && (lonArr1[i-1] >= 178 && lonArr1[i-1] <= 180)) {
					mCrossed180 = new Boolean(true);
					return true;
				}
				else if ((lonArr1[i] >= 178 && lonArr1[i] <= 180) && (lonArr1[i-1] >= -180 && lonArr1[i-1] < -178)) {
					mCrossed180 = new Boolean(true);
					return true;
				}
			}
		}

		if (lonArr2 != null) {
			for (int i = 0; i < lonArr2.length; i++) {
				if ((lonArr2[i] >= -180 && lonArr2[i] < -178) && (lonArr2[i-1] >= 178 && lonArr2[i-1] <= 180)) {
					mCrossed180 = new Boolean(true);
					return true;
				}
				else if ((lonArr2[i] >= 178 && lonArr2[i] <= 180) && (lonArr2[i-1] >= -180 && lonArr2[i-1] < -178)) {
					mCrossed180 = new Boolean(true);
					return true;
				}
			}
		}
		mCrossed180 = new Boolean(false);
		return false;
	}

  // ---------------------------------------------------------
  //
  public double getMinDepth() {
     double[] m = getMinMaxDepth();
     return m[0];
  }
  // ---------------------------------------------------------
  //
  public double getMaxDepth() {
     double[] m = getMinMaxDepth();
     return m[1];
  }
  // ---------------------------------------------------------
  //
  public double[] getMinMaxDepth() {
     double mn = 10000;
     double mx = -10000;
     if (minMaxDepth == null) {
	minMaxDepth = new double[2];
	if (depthArr1 != null) {
	   for (int i = 0; i < depthArr1.length; i++) {
	      mx = Math.max(depthArr1[i], mx);
	      mn = Math.min(depthArr1[i], mn);
	   }
	}
	if (depthArr2 != null) {
	   for (int i = 0; i < depthArr2.length; i++) {
	      mx = Math.max(depthArr2[i], mx);
	      mn = Math.min(depthArr2[i], mn);
	   }
	}
	minMaxDepth[0] = mn;
	minMaxDepth[1] = mx;
     }
     return minMaxDepth;
  }
  // ---------------------------------------------------------
  //
  public double getMinTime() {
     double[] m = getMinMaxTime();
     return m[0];
  }
  // ---------------------------------------------------------
  //
  public double getMaxTime() {
     double[] m = getMinMaxTime();
     return m[1];
  }
  // ---------------------------------------------------------
  //
  public double[] getMinMaxTime() {
  	 double mn = 0;
     double mx = 0;
     if (minMaxTime == null) {
		minMaxTime = new double[2];
		if (timeArr1 != null) {
	   		mn = timeArr1[0];
	   		mx = timeArr1[0];
	   		for (int i = 0; i < timeArr1.length; i++) {
	      		mx = Math.max(timeArr1[i], mx);
	      		mn = Math.min(timeArr1[i], mn);
	   		}
		}
		if (timeArr2 != null) {
	   		for (int i = 0; i < timeArr2.length; i++) {
	      		mx = Math.max(timeArr2[i], mx);
	      		mn = Math.min(timeArr2[i], mn);
	   		}
		}
		minMaxTime[0] = mn;
		minMaxTime[1] = mx;
     }
     return minMaxTime;
  }

  public double[] getLatArr1Sorted() {
     if (latArr1Sorted == null)
     	sortLat();
     return latArr1Sorted;
  }

  public int[] getLatArr1SortedIndices() {
     if (latArr1Sorted == null)
     	sortLat();
     return latArr1SortedIndices;
  }

	public void sortLat() {
		latArr1Sorted = new double[latArr1.length];
			latArr1SortedIndices = new int[latArr1.length];
			for (int i = 0; i < latArr1.length; i++) {
				latArr1Sorted[i] = latArr1[i];
				latArr1SortedIndices[i] = i;
			}
			try {
				QSortAlgorithm.sort(latArr1Sorted, latArr1SortedIndices);
			} catch (Exception e) {
				System.out.println("ERROR: QSort failure");
		}
	}

	public double[] getLonArr1Sorted() {
		if (lonArr1Sorted == null)
			sortLon();
		return lonArr1Sorted;
	}

	public int[] getLonArr1SortedIndices() {
		if (lonArr1Sorted == null)
			sortLon();
		return lonArr1SortedIndices;
	}

	public void sortLon() {
		lonArr1Sorted = new double[lonArr1.length];
		lonArr1SortedIndices = new int[lonArr1.length];
		for (int i = 0; i < lonArr1.length; i++) {
			lonArr1Sorted[i] = lonArr1[i];
			lonArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(lonArr1Sorted, lonArr1SortedIndices);
		} catch (Exception e) {
			System.out.println("ERROR: QSort failure");
		}
	}

  public double[] getDepthArr1Sorted() {
     if (depthArr1Sorted == null) sortDepth();
     return depthArr1Sorted;
  }

  public int[] getDepthArr1SortedIndices() {
     if (depthArr1Sorted == null) sortDepth();
     return depthArr1SortedIndices;
  }

  public void sortDepth() {
     depthArr1Sorted = new double[depthArr1.length];
     depthArr1SortedIndices = new int[depthArr1.length];
     for (int i = 0; i < depthArr1.length; i++) {
	depthArr1Sorted[i] = depthArr1[i];
	depthArr1SortedIndices[i] = i;
     }
     try {
     QSortAlgorithm.sort(depthArr1Sorted,
			depthArr1SortedIndices);
     } catch (Exception e) {
	 System.out.println("ERROR: QSort failure");
     }
  }

  public double[] getTimeArr1Sorted() {
     if (timeArr1Sorted == null) sortTime();
     return timeArr1Sorted;
  }

  public int[] getTimeArr1SortedIndices() {
     if (timeArr1Sorted == null) sortTime();
     return timeArr1SortedIndices;
  }

	public void sortTime() {
		timeArr1Sorted = new double[timeArr1.length];
		timeArr1SortedIndices = new int[timeArr1.length];
		for (int i = 0; i < timeArr1.length; i++) {
			timeArr1Sorted[i] = timeArr1[i];
			timeArr1SortedIndices[i] = i;
		}
		try {
			QSortAlgorithm.sort(timeArr1Sorted,
			timeArr1SortedIndices);
		}
		catch (Exception e) {
			System.out.println("ERROR: QSort failure");
		}
	}

	public int getCurrDeletionIndex() {
		return currDeletionLevel;
	}

	public void setCurrDeletionIndex(int cd) {
		currDeletionLevel = cd;
	}

	public void delete(int i, int ord) {
		isDeleted[i] = ord;
	}

	public void unDelete(int i, int ord) {
		isDeleted[i] = ord;
	}

	public void unDeleteAll() {	
		for (int i=0; i<this.getSize(); i++) {
			isDeleted[i] = 0;
		}
		mVisible = this.getSize();
	}

	public boolean isDeleted(int i) {
		return isDeleted[i] != 0;
	}

	public void select(int i) {
		isSelected[i] = true;
	}

	public void unselect(int i) {
		isSelected[i] = false;
	}

	public void unselectAll() {
		for (int i=0; i<this.getSize(); i++) {
			isSelected[i] = false;
		}
		if (!mBatchMode)
			fireSelectionChange();
		mSelected = 0;
	}

	public boolean isSelected(int i) {
		return isSelected[i];
	}

	public boolean isSomethingSelected() {
		for (int i=0; i<this.getSize(); i++) {
			if (isSelected(i))
				return true;
		}
		return false;
	}

	public void setReferences(Object[] objs) {
		mReferences = objs;
	}

	public void setReferences(int i, Object obj) throws ArrayIndexOutOfBoundsException {
		mReferences[i] = obj;
	}

	public Object[] getReferences() {
		return mReferences;
	}

	public Object getReference(int i) throws ArrayIndexOutOfBoundsException {
		return mReferences[i];
	}

	public void dumpPointers() {
		for (int i=0; i<this.getSize(); i++) {
			System.out.println(latArr1[i] + " " + latArr2[i] + " " + lonArr1[i] + " " + lonArr2[i] + " " + depthArr1[i] + " " + depthArr2[i] + " " + timeArr1[i] + " " + timeArr2[i]);
		}
	}

	public void setBatchModeOn() {
		mBatchMode = true;
	}

	public void setBatchModeOff() {
		mBatchMode = false;
		fireSelectionChange();
	}
	
	public boolean isBatchMode() {
		return mBatchMode;
	}
	
	public void fireSelectionChange() {
		// has the selection changed?
		pcs.firePropertyChange("selectionchange", null, new Integer(1));
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}
	
	public int getNumPtrs() {
		return this.getSize();
	}
	
	public int getNumVisiblePtrs() {
		return mVisible;
	}
	
	public int getNumSelectedPtrs() {
		return mSelected;
	}
	
	private void computeVisible() {
		int c = 0;
		for (int i=0; i<this.getSize(); i++) {
			if (this.isDeleted[i] == 0)
				c++;
		}
		mVisible = c;
	}
	
	private void computeSelected() {
		int c = 0;
		for (int i=0; i<this.getSize(); i++) {
			if (this.isSelected[i])
				c++;
		}
		mSelected = c;
	}
	
	public void updateStats() {
	//System.out.println("***updateStats");
		this.computeVisible();
		this.computeSelected();
	}
	
	public boolean isArgo() {
		return mIsArgo;
	}
	
	public void setIsArgo(boolean b) {
		mIsArgo = b;
	}
	
	public String[] getExtraArr1() {
		return mExtraArr1;
	}
	
	public String[] getExtraArr2() {
		return mExtraArr2;
	}
	
	public String[] getExtraArr3() {
		return mExtraArr3;
	}
	
	public String[] getExtraArr4() {
		return mExtraArr4;
	}
	
	public String[] getExtraArr5() {
		return mExtraArr5;
	}
	
	public String[] getExtraArr6() {
		return mExtraArr6;
	}
	
	public String[] getExtraArr7() {
		return mExtraArr7;
	}
	
	public String[] getExtraArr8() {
		return mExtraArr8;
	}
	
	public String[] getExtraArr9() {
		return mExtraArr9;
	}
	
	public String[] getExtraArr10() {
		return mExtraArr10;
	}
	
	public boolean[] getPathIsRelativeArr() {
		return pathIsRelativeArr;
	}
}



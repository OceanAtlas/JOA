/*
 * $Id: WOCEImportOptions.java,v 1.2 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

public class WOCEImportOptions {
	boolean mConvertTemps = true;					//ITS90 to IPTS68
	boolean mConvertMassToVol = false;				// mass to volume units
	boolean mUse25DegC = false;						// use 25 degree c instead of thta in o2 conversion	
	boolean mSetMissingQBeq3= false;				// 3 questionable value
	boolean mSetMissingQBeq4= false;				// 4 bad measurement
	boolean mSetMissingQBeq7= false;				// 7 manual CFC
	boolean mSetMissingQBeq8= false;				// 8 irregular CFC
	boolean mSetMissingAllBottlesBQBeq4= false;		// 4 bad bottle
	boolean mSetMissingAllGasBQBeq3BO2QBeq4= false;	// 3 questionable bottle, 4 badd O2
	boolean mNO2NO3asNO3= false;					// report no2+no3 as no3
	boolean mSortBottles = true;
}
		
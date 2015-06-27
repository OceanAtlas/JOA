package gov.noaa.pmel.eps2;

import java.util.*;
import java.io.*;
/**
 * <code>EPSDbase</code> stores a collection of parsed files.
 * Files are only parsed when using the iterator next or getIthElement.
 * Parsed files are locally cached until explicitly released.
 *
 * @author oz
 * @version 1.0
 */
public class EPSDbase implements EPSConstants {
	/**
	* Hashtable used to store individual dbase objects
	*/
	protected Hashtable mDbaseEntries = new Hashtable(100);
	/**
	* Iterator for the pointer collection database that is used to construct this database
	*/
	protected PointerDBIterator mPtrFileEntries;
	private boolean mCacheEntries = true;
	
	/**
	* zero arg constructor
	*
	*/
	public EPSDbase() {
	
	}
	
	/**
	* Create a pointer database. Most of the intelligence is actually in 
	* the iterator;
   	*
   	* @param itor PointerDBIterator
   	* @param createPtrs Flag to specify whether ptr file records are created 
   	* for simple ptr files
	*/
	public EPSDbase(PointerDBIterator itor, boolean cache) {
		mCacheEntries = cache;
		mPtrFileEntries = itor;
	}
	
	/**
	* Return an database iterator
   	*
  	* @return Iterator for database
	*/
	public EPSDBIterator iterator(boolean mCacheEntries) {
		return new EPSDBIterator(mPtrFileEntries, mDbaseEntries, mCacheEntries);
	}
	
  	/**
  	* Return number of elements in database.
   	*
  	* @return Number of elements.
   	**/
	public int getCount() {
		try {
			EPSDBIterator itor = new EPSDBIterator(mPtrFileEntries, mDbaseEntries, mCacheEntries);
			int count = 0;
			while (itor.hasNext()) {
				itor.next();
				count++;
			}
			return count;
		}
		catch (Exception ex) {
			return 0;
		}
	}
}

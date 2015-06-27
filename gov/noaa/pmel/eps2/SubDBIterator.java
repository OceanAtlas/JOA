package gov.noaa.pmel.eps2;

import java.util.*;

/**
 * <code>PointerDBIterator</code> returns an iterator to 
 * individual pointers in the pointer file database.
 *
 * @see java.util.Iterator
 * @author oz
 * @version 1.0
 */
public class SubDBIterator implements Iterator {
	private Vector mDBs;
	private int currElement = 0;
	private String mFileName;
	
    /**
     * Create a SubDBIterator.
     *
     * @param dbs Vector of Dbases to iterate through
     * @param fname The section file name that created the sub-dbase
     */
	public SubDBIterator(Vector dbs, String fname) {
		mDBs = dbs;
		mFileName = new String(fname);
	}
	
    /**
     * Return the number of elements in the sub-dbase.
     *
     * @return Number of elements
     */
	public int size() {
		return mDBs.size();
	}
	
    /**
     * Return the section file name that created this collection.
     *
     * @return Filename
     */
	public String getName() {
		return mFileName;
	}
	
    /**
     * Returns <code>true</code> if there are more elements.
     *
     * @return True if more elements
     */
	public boolean hasNext() {
		return currElement < mDBs.size();
	}
	
    /**
     * Returns the next element. Calls to this
     * method will step through successive elements.
     *
     * @return Next pointer
     *
     * @exception java.util.NoSuchElementException If no more elements exist.
     */
	public Object next() {
		Object obj = mDBs.elementAt(currElement);
		currElement++;
		return obj;
	}
	
    /**
     * Removes from the underlying collection the last element returned by the iterator.
     *
     * Note: this method not currently implemented
     */
	public void remove() {
	
	}
	
    /**
     * get the ith dbase object from the pointer list.
     *
     * @param i Index of pointer to get
     *
     * @return Ith pointer
     *
     * @exception NoSuchElementException Requested element not in the collection
     */
	public Object getElement(int i) throws NoSuchElementException {
		return mDBs.elementAt(i);
		
	}
}

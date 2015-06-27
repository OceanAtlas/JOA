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
public class PointerDBIterator implements Iterator {
	private ArrayList mPtrs;
	private int currElement = 0;
	private String mFileName;
	
    /**
     * Construct a PointerDBIterator.
     *
     * @param ptrs An ArrayList of parsed pointers
     * @param fname Name of pointer file
     */
	public PointerDBIterator(ArrayList ptrs, String fname) {
		mPtrs = ptrs;
		if (fname != null)
			mFileName = new String(fname);
	}
	
    /**
     * Get the number of elements in the iterator.
     *
     * @return Number of elements
     */
	public int size() {
		return mPtrs.size();
	}
	
    /**
     * Get the filename that was used to created the pointer database.
     *
     * @return Source filename
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
		return currElement < mPtrs.size();
	}
	
    /**
     * Resets the iterator to the first element.
     *
     */
	public void reset() {
		currElement = 0;
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
		Object obj = mPtrs.get(currElement);
		currElement++;
		return obj;
	}
	
    /**
     * Removes from the underlying collection the last element returned by the iterator.
     */
	public void remove() {
	
	}
	
    /**
     * get the ith dbase object from the pointer list.
     *
     * @param i Index of pointer to get
     *
     * @return ith pointer
     *
     * @exception NoSuchElementException Requested element not in the collection
     */
	public Object getElement(int i) throws NoSuchElementException {
		return mPtrs.get(i);
	}
}

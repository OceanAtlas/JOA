package gov.noaa.pmel.eps2;
import java.util.*;
import java.io.*;
import java.net.URL;

/**
 * <code>EPSDBIterator</code> returns an iterator to
 * individual pointers in the pointer file database
 *
 * @see java.util.Iterator
 * @author oz
 * @version 1.1
 */
public class EPSDBIterator implements EPSConstants {
	private PointerDBIterator mPtrRecs;
	private Hashtable mDbaseRecs;
	private int mCurrElement = 0;
	private int mFileFormat; 
	private boolean mCacheEntries;

	public EPSDBIterator(PointerDBIterator ptrRecs, Hashtable recs, boolean cacheEntries) {
		mPtrRecs = ptrRecs;
		mDbaseRecs = recs;
		mCacheEntries = cacheEntries;
	}

  	/**
  	* Number of elements in the EPSDbase.
   	*
  	* @return Number of elements.
   	**/
	public int size() {
		return mPtrRecs.size();
	}

  	/**
  	* Name attached to this iterator--usually from the pointer file.
   	*
  	* @return Number of elements.
   	**/
	public String getName() {
		return mPtrRecs.getName();
	}

  	/**
  	* Reset the iterator to the first pointer file entry.
   	*
   	**/
	public void reset() {
		mCurrElement = 0;
		mPtrRecs.reset();
	}

  	/**
  	* Database has more elements. Reset the itor if we are at the end
  	* of the pointer list.
   	*
  	* @return Returns true if there are more Ptr elements.
   	**/
	public boolean hasNext() {
		boolean hasMore = mPtrRecs.hasNext();
		if (!hasMore)
			reset();
		return hasMore;
	}

    /**
     * Returns the next element in the database. Calls to this
     * method will step through successive elements.
   	 *
  	 * @return Returns an DBase.
  	 *
  	 * @see Dbase
     *
     * @exception NoSuchElementException Thrown if no more elements exist.
     */
	public Object next() throws Exception {
		EpicPtr ep = (EpicPtr)mPtrRecs.next();
		mCurrElement++;

		// see if this file has been parsed and if so return an object from the
		// mDbaseRecs collection. If not parsed, then parse it, store object and return object
		// construct a key from filename and path
		String key = ep.getPath() + ep.getFileName();

	
		Dbase db = null;
		if (mCacheEntries) {
			db = (Dbase)mDbaseRecs.get(key);
		}

		if (db != null) {
			// dbase found in cache
			return db;
		}
		else {
			// parse file and store in cache
			String fn = ep.getFileName().trim();
			String path = ep.getPath().trim();
			mFileFormat = ep.getFormat();
			try {
				if (ep.isRelativePath()) {
					if (path.indexOf(".") == 0) {
						// UNIX releative path
						path = path.substring(1, path.length());
					}
					path = System.getProperty("user.dir") + path;
				}
				
				if (path != null && path.indexOf("http") >= 0)
					db = this.setFile(new URL(path), mFileFormat, ep);
				else {
					int fnPos = path.lastIndexOf(fn);
					if (fnPos < 0) {
						File f = EPS_Util.getFile(path, fn);
						db = this.setFile(f, mFileFormat, ep);
					}
					else {
						path = path.substring(0, fnPos);
						File f = EPS_Util.getFile(path, fn);
						db = this.setFile(f, mFileFormat, ep);
					}
				}
				
				if (mCacheEntries) {
					mDbaseRecs.put(key, db);
				}
				return db;
			}
			catch (Exception ex) {
				throw ex;
			}
		}
	}

  	/**
  	* Removes from the underlying collection the last element returned by the iterator.
  	* This routine is not currently implemented
   	*
   	**/
	public void remove() {

	}

    /**
     * Removes a dBase from the underlying collection the specified element
     *
     * @param obj The element to remove from the database
     */
	public void remove(Object obj) {
		mDbaseRecs.remove(obj);
	}

    /**
     * Get the ith dbase object form the database.
     *
     * @param i The number of the element to be selected
     *
     * @exception NoSuchElementException The specified index was not found in the database
     * @exception IOException An IO error occurred parsing the file specified by the ith element in the database
     */
	public Object getElement(int i) throws NoSuchElementException, Exception {
		EpicPtr ep = (EpicPtr)mPtrRecs.getElement(i);

		// see if this file has been parsed and if so return an object from the
		// mDbaseRecs collection. If not parsed, then parse it, store object and return object
		// construct a key from filename and path
		String key = ep.getPath() + ep.getFileName();
		Dbase db = (Dbase)mDbaseRecs.get(key);

		if (db != null) {
			// dbase found in cache
			return db;
		}
		else {
			// parse file and store in cache
			String fn = ep.getFileName().trim();
			String path = ep.getPath().trim();
			mFileFormat = ep.getFormat();
			
			try {
				if (ep.isRelativePath()) {
					if (path.indexOf(".") == 0) {
						// UNIX relative path
						path = path.substring(1, path.length());
					}
					path = System.getProperty("user.dir") + path;
				}
				
				if (path != null && path.indexOf("http") >= 0)
					db = this.setFile(new URL(path), mFileFormat, ep);
				else {
					File f = EPS_Util.getFile(path, fn);
					db = this.setFile(f, mFileFormat, ep);
				}
				
				mDbaseRecs.put(key, db); 
				return db;
			}
			catch (Exception ex) {
				throw ex;
			}
		}
	}

	/**
	* Parse file and make a new dBase record.
	*
	* @param infile Disk file to parse with it's installed reader
	*
	* @exception IOException Something happened creating a netCDFFile object, setting it's reader, or parsing the file
	*/
	public Dbase setFile(File infile, int format, EpicPtr ep) throws Exception {
		Dbase db = new Dbase();
		//CompositeDbase profSection = new CompositeDbase();
		try {
			// this has to be format specific
			if (format == EPSConstants.NETCDFFORMAT || format == EPSConstants.NETCDFXBTFORMAT) {
				EPSNetCDFFile nc = new EPSNetCDFFile(infile, NC_NOWRITE);
				db.setFileReader(new netCDFReader(db, nc, ep));
				db.getFileReader().parse();
				nc.close();
			}
			else if (format == EPSConstants.ARGONODCNETCDFFORMAT) {
				EPSNetCDFFile nc = new EPSNetCDFFile(infile, NC_NOWRITE);
				db.setFileReader(new ArgoNODCProfilenetCDFReader(db, nc, ep));
				db.getFileReader().parse();
				nc.close();
			}
			else if (format == EPSConstants.ARGOGDACNETCDFFORMAT) {
				EPSNetCDFFile nc = new EPSNetCDFFile(infile, NC_NOWRITE);
				db.setFileReader(new ArgoGDACProfilenetCDFReader(db, nc, ep));
				db.getFileReader().parse();
				nc.close();
			}
			else if (format == EPSConstants.POAFORMAT) {
				db.setFileReader(new POASectionReader(db, infile, ep));
				db.getFileReader().parse();
			}
			else if (format == EPSConstants.JOAFORMAT) {
				db.setFileReader(new JOASectionReader(db, infile, ep));
				db.getFileReader().parse();
			}
			else if (format == EPSConstants.SSFORMAT) {
				db.setFileReader(new TSVSectionReader(db, infile, ep));
				db.getFileReader().parse();
			}
			else if (format == EPSConstants.XYZFORMAT) {
				db.setFileReader(new XYZSectionReader(db, infile, ep));
				db.getFileReader().parse();
			}
			else if (format == EPSConstants.SD2FORMAT) {
				db.setFileReader(new SD2SectionReader(db, infile, ep));
				db.getFileReader().parse();
			}
			else if (format == EPSConstants.WOCEHYDFORMAT) {
				db.setFileReader(new WOCEBottleSectionReader(db, infile, ep, null));
				db.getFileReader().parse();
			}
			else if (format == EPSConstants.WOCECTDFORMAT) {
				db.setFileReader(new WOCECTDProfileReader(db, infile, ep));
				db.getFileReader().parse();
			}
			else if (format == EPSConstants.DODSNETCDFFORMAT) {
				db.setFileReader(new DODSnetCDFReader(db, ep.getURL(), ep));
				db.getFileReader().parse();
			}
		}
		catch (Exception ex) {
			System.out.println("An error occurred opening: " + infile.getName());
			ex.printStackTrace();
			throw ex;
		}
		return db;
	}
				
	public Dbase setFile(URL url, int format, EpicPtr ep) throws Exception {
		Dbase db = new Dbase();
		try {
			// this has to be format specific
			if (format == EPSConstants.NETCDFFORMAT || format == EPSConstants.NETCDFXBTFORMAT) {
				EPSNetCDFFile nc = new EPSNetCDFFile(url, NC_NOWRITE);
				db.setFileReader(new netCDFReader(db, nc, ep));
				db.getFileReader().parse();
			}
			else if (format == EPSConstants.ARGONODCNETCDFFORMAT) {
				EPSNetCDFFile nc = new EPSNetCDFFile(url, NC_NOWRITE);
				db.setFileReader(new ArgoNODCProfilenetCDFReader(db, nc, ep));
				db.getFileReader().parse();
			}
		}
		catch (Exception ex) {
			System.out.println("An error occurred opening: " + url);
			throw ex;
		}
		return db;
	}
}

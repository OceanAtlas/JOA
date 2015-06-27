package gov.noaa.pmel.eps2;
import java.io.*;

/**
 * <code>EpicPtrFactoryIF</code> defines the createEpicPtr method for the EpicPtrFactory.
 *
 * @see EpicPtrFactory
 *
 * @author oz
 * @version 1.0
 */
 
public interface EpicPtrFactoryIF {
	/**
	* Create an EPIC pointers database. Determine format of file by opening it. \n
	* Set the reader class for the EPIC pointers database.
	*
 	* @see EpicPtrs
	*
	* @param inFile Input pointer file
	* @param sortFlags Passed to appropriate pointer file reader class used by the pointer file database 
	* @param createPtrs Flag to tell SimplePtrReader whether to open files to create full pointer records 
	*
	* @return new EpicPtrs database
	*
  	* @exception UnkEpicPtrFileException Pointer File is an unknown format.
  	*/
	public EpicPtrs createEpicPtrs(File infile, int[] sortFlags, boolean createPtrs, boolean ignoreArgoFlag) throws UnkEpicPtrFileException;
}

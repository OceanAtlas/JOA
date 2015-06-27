package gov.noaa.pmel.eps2;

/**
 * <code>EPSProperties</code> 
 * Stores changeable runtime constants
 *
 * @author oz
 * @version 1.0
 */
public class EPSProperties implements EPSConstants {
	/**
	* Current character item delimiter when reading a delimited text file.
	*/
 	public static char DELIMITER = TAB_DELIMITER;
 	
	/**
	* Current string item delimiter when reading a delimited text file.
	*/
 	public static String SDELIMITER = STAB_DELIMITER;
 	
	/**
	* Current string item delimiter when reading a delimited text file.
	*/
 	public static String DOUBLEDELIM = SDELIMITER + SDELIMITER;
  	
  	public static String epicKeySubDir = null;
  	public static EPIC_Key_DB epicKeyDB = null;//new EPIC_Key_DB("epic.key");
}

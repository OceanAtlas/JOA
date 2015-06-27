package gov.noaa.pmel.eps2;

/**
 * <code>TimeToSecond</code> 
 * Stores the conversion between a literal time unit (e.g. year) and the 
 * equivalent number of seconds.
 *
 * @author oz
 * @version 1.0
 */
public class TimeToSecond {
	/**
 	* Name of the time units to convert to seconds (e.g. year) 
 	*/
	protected String name;
	/**
 	* Multiplier named units to convert to seconds (e.g. year, scale = 3.1536e+7) 
 	*/
	protected double scale;
	/**
 	* Flag is true when named units have only one character (e.g., "a") 
 	*/
	protected boolean single;
   
	/**
 	* Construct a conversion object 
 	*
 	* @param nm Literal name of units
 	* @param sc Scale value to convert literal units to seconds
 	* @param sing Flag to indicate literal units is a single character
 	*/
	public TimeToSecond(String nm, double sc, boolean sing) {
		name = new String(nm);
		scale = sc;
		single = sing;	
	}
	
	/**
 	* Get the literal name of the units being converted 
 	*
 	* @return Literal name of units
 	*/
	public String getName() {
		return name;
	}
	
	/**
 	* Get the scale value to convert the literal units to seconds 
 	*
 	* @return Scale value
 	*/
	public double getScale() {
		return scale;
	}
	
	/**
 	* Get flag whether literal units is a single character 
 	*
 	* @return true = single character
 	*/
	public boolean getSingle() {
		return single;
	}
}

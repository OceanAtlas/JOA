package gov.noaa.pmel.eps2;

/**
 * <code>Key</code> 
 * Object that stores an EPIC key file entry
 *
 * @author oz
 * @version 1.0
 */
public class Key {
	/**
	* Variable ID
	*/
	protected int id;
	/**
	* Short name
	*/
	protected String sname;
	/**
	* Long name
	*/
	protected String lname;
	/**
	* Generic name
	*/
	protected String gname;
	/**
	* Units
	*/
	protected String units;
	/**
	* FORTRAN Format
	*/
	protected String frmt;
	/**
	* Type
	*/
	protected int type;
	
	
  	/**
   	* Construct a new <code>Key</code>.
   	*/
	public Key() {
	
	}
	
  	/**
   	* Construct a new <code>Key</code> with values from the EPIC key file.
   	*
   	* @param inId Key ID
   	* @param sn Short name
   	* @param ln Long name
   	* @param gn Generic name
   	* @param un Units
   	* @param fr FORTRAN format
   	* @param ty Key type
   	*/
	public Key(int inId, String sn, String ln, String gn, String un, String fr, int ty) {
		id = inId;
		if (sn != null)
			sname = new String(sn);
		if (ln != null)
			lname = new String(ln);
		if (gn != null)
			gname = new String(gn);
		if (un != null)
			units = new String(un);
		if (fr != null)
			frmt = new String(fr);
		type = ty;
	}
	
  	/**
   	* Get the Key ID.
   	*
   	* @return ID
   	*/
	public int getID() {
		return id;
	}
	
  	/**
   	* Set the Key ID.
   	*
   	* @param sid ID
   	*/
	public void setID(int sid) {
		id = sid;
	}
	
  	/**
   	* Get the generic name.
   	*
   	* @return Generic name
   	*/
	public String getGname() {
		return gname;
	}
	
  	/**
   	* Set the generic name.
   	*
   	* @param s Generic name
   	*/
	public void setGname(String s) {
		gname = null;
		gname = new String(s);
	}
	
  	/**
   	* Get the short name.
   	*
   	* @return Short name
   	*/
	public String getSname() {
		return sname;
	}
	
  	/**
   	* Set the short name.
   	*
   	* @param s Short name
   	*/
	public void setSname(String s) {
		sname = null;
		sname = new String(s);
	}
	
  	/**
   	* Get the long name.
   	*
   	* @return Long name
   	*/
	public String getLname() {
		return lname;
	}
	
  	/**
   	* Set the long name.
   	*
   	* @param s long name
   	*/
	public void setLname(String s) {
		lname = null;
		lname = new String(s);
	}
	
  	/**
   	* Get the Units.
   	*
   	* @return Units
   	*/
	public String getUnits() {
		return units;
	}
	
  	/**
   	* Set the units.
   	*
   	* @param u units
   	*/
	public void setUnits(String u) {
		units = null;
		units = new String(u);
	}
	
  	/**
   	* Get the FORTRAN format.
   	*
   	* @return FORTRAN format
   	*/
	public String getFrmt() {
		return frmt;
	}
	
  	/**
   	* Set the FORTRAN format.
   	*
   	* @param f FORTRAN format
   	*/
	public void setFrmt(String f) {
		frmt = null;
		frmt = new String(f);
	}
	
  	/**
   	* Get the Key Type.
   	*
   	* @return Type
   	*/
	public int getType() {
		return type;
	}
	
  	/**
   	* Set the Key Type.
   	*
   	* @param t Type
   	*/
	public void setType(int t) {
		type = t;
	}
	
	public String toString() {
		String outStr = new String();
		outStr += id;
		if (sname != null)
			outStr += "," + sname;
		if (lname != null)
			outStr += "," + lname;
		if (gname != null)
			outStr += "," + gname;
		if (units != null)
			outStr += "," + units;
		if (frmt != null)
			outStr += "," + frmt;
			
		return outStr; 
	}
}

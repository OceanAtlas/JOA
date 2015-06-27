package gov.noaa.pmel.eps2;

import java.lang.*;

/**
 * Can't sort pointers because an iterator is active for the EpicPtrs DB
 *
 *
 * @author oz
 * @version 1.0
 */
 
public class CantSortItorActiveExcept extends Exception {
	public CantSortItorActiveExcept() {
		this("t sort because an iterator is active for the Epic Ptrs DB");
	}
	
	public CantSortItorActiveExcept(String s) {
		super(s);
	}
}
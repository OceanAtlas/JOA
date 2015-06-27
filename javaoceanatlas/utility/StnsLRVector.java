/*
 * $Id: StnsLRVector.java,v 1.3 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.util.*;
import javaoceanatlas.classicdatamodel.*;

@SuppressWarnings("serial")
public class StnsLRVector extends Vector<Station> {
    int mCurrElement;
    
    public StnsLRVector() {
    	super(); 
   		mCurrElement = 0;
    }
    
    public synchronized Station nextElement() {
    	// override: set to next used station
    	while (true) {
	    	if (mCurrElement + 1 < this.size()) {
	    		Station sh = (Station)this.elementAt(++mCurrElement);
	    		if (sh.mUseStn)
	    			return sh;
	    	}
	    	else
	    		return null;
    	}
    }
    
    public synchronized Station prevElement() {
    	// override: set to prev used station
    	while (true) {
	    	if (mCurrElement - 1 >= 0) {
	    		Station sh = (Station)this.elementAt(--mCurrElement);
	    		if (sh.mUseStn)
	    			return sh;
	    	}
	    	else
	    		return null;
    	}
    }
    
    public void setCurrElementToFirst() {
    	// override: set to first used station
    	if (this.size() == 1)
    		return;
    	mCurrElement = 0;
    	while (true) {
    		Station sh = this.elementAt(mCurrElement);
    		if (!sh.mUseStn)
    			mCurrElement++;
    		else
    			break;
    			
    		if (mCurrElement == this.size() - 1)
    			break;
    	}
    }
    
    public void setCurrElementToLast() {
    	//override; set to last used stn;
    	if (this.size() == 1)
    		return;
    	mCurrElement = this.size() - 1;
    	while (true) {
    		Station sh = this.elementAt(mCurrElement);
    		if (!sh.mUseStn)
    			mCurrElement--;
    		else
    			break;
    			
    		if (mCurrElement == 0)
    			break;
    	}
    }
    
    public synchronized Station currElement() {
    	return this.elementAt(mCurrElement);
    }
    
    public synchronized Station getCurrElement() {
    	return this.elementAt(mCurrElement);
    }
    
    public void setCurrElement(Station obj) {
    	mCurrElement = this.indexOf(obj);
    }
}
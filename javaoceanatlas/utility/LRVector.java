/*
 * $Id: LRVector.java,v 1.3 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.util.*;
import javaoceanatlas.classicdatamodel.*;

@SuppressWarnings("serial")
public class LRVector extends Vector<Object> {
  int mCurrElement;

  public LRVector() {
    super();
    mCurrElement = 0;
  }

  public synchronized Object nextElement() {
    if (mCurrElement + 1 < this.size()) {
      if (this.elementAt(mCurrElement + 1) instanceof Section) {
        boolean hasVisData = false;
        for (int ce = mCurrElement + 1; ce < this.size(); ce++) {
          // need to check whether this section has any visible stns
          Section ns = (Section)this.elementAt(ce);
          for (int i = 0; i < ns.mStations.size(); i++) {
            Station sh = (Station)ns.mStations.elementAt(i);
            if (sh.mUseStn) {
              hasVisData = true;
              break;
            }
          }
          if (hasVisData) {
            mCurrElement = ce;
            return this.elementAt(mCurrElement);
          }
        }
        return null;
      }
      return this.elementAt(++mCurrElement);
    }
    else {
      return null;
    }
  }

  public synchronized Object prevElement() {
    if (mCurrElement - 1 >= 0) {
      if (this.elementAt(mCurrElement - 1) instanceof Section) {
        boolean hasVisData = false;
        for (int ce = mCurrElement - 1; ce >= 0; ce--) {
          // need to check whether this section has any visible stns
          Section ns = (Section)this.elementAt(ce);
          for (int i = 0; i < ns.mStations.size(); i++) {
            Station sh = (Station)ns.mStations.elementAt(i);
            if (sh.mUseStn) {
              hasVisData = true;
              break;
            }
          }
          if (hasVisData) {
            mCurrElement = ce;
            return this.elementAt(mCurrElement);
          }
        }
        return null;
      }
      return this.elementAt(--mCurrElement);
    }
    else {
      return null;
    }
  }

  public synchronized Object currElement() {
    return this.elementAt(mCurrElement >= 0 ? mCurrElement : 0);
  }

  public synchronized Object getCurrElement() {
    return this.elementAt(mCurrElement >= 0 ? mCurrElement : 0);
  }

  public void setCurrElement(Object obj) {
  	int place = this.indexOf(obj);
    mCurrElement = place >= 0 ? place : 0;
  }

  public void setCurrElementToFirst() {
    mCurrElement = 0;
  }

  public void setCurrElementToLast() {
    mCurrElement = this.size() - 1;
    if (mCurrElement < 0) {
    	mCurrElement = 0;
    }
  }

  public Object getFirstElement() {
    return this.elementAt(0);
  }
}

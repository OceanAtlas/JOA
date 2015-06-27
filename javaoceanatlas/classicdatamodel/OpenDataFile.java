/*
 * $Id: OpenDataFile.java,v 1.4 2005/06/17 18:02:04 oz Exp $
 *
 */

package javaoceanatlas.classicdatamodel;

import javaoceanatlas.utility.*;

public class OpenDataFile {
  public int mOrdinal = 0;
  public String mName;
  public LRVector mSections = new LRVector();
  public int mNumSections;
  public String mFileComments = "";

  public OpenDataFile(String filename) {
    mOrdinal++;
    mName = new String(filename);
  }
  
  public OpenDataFile(OpenDataFile of) {
    mOrdinal = of.mOrdinal;
    mName = new String(of.getName());
    mNumSections = of.getNumSections();
    mFileComments = new String(of.getComments());


    for (int sec = 0; sec < of.mNumSections; sec++) {
      Section sech = (Section)of.mSections.elementAt(sec);
      if (sech.mNumCasts == 0) {
        continue;
      }
      
      // copy section
      Section newSec = new Section((Section)of.mSections.elementAt(sec));
      mSections.add(newSec);
    }
  }
  
  public void resetSectionVector() {
		this.mSections.setCurrElementToFirst();
  }
  
  public void setCurrentSection(Section sech) {
		this.mSections.setCurrElement(sech);
  }
  
  public void addSection(Section sech) {
  	mSections.add(sech);
  }
  
  public Section getSection(int sec) {
  	return (Section)this.mSections.elementAt(sec);
  }
  
  public void setSection(Section sech, int sec) {
    this.mSections.setElementAt(sech, sec);
  }
  
  public Section getLastSection() {
  	return (Section)this.mSections.lastElement();
  }

  public String getName() {
    return mName;
  }

  public void setName(String s) {
    mName = new String(s);
  }

  public Section getCurrSection() {
    return (Section)mSections.getCurrElement();
  }

  public String getComments() {
    return mFileComments;
  }

  public void addComment(String s) {
    if (!s.startsWith("#"))
      mFileComments += "#" + s + "\n";
    else
      mFileComments += s + "\n";
  }

  public int getNumSections() {
    return mNumSections;
  }
}

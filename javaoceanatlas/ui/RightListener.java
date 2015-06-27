/*
 * $Id: RightListener.java,v 1.2 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class RightListener extends AbstractAction {
	Object mComp;
	@SuppressWarnings("unchecked")
  Class mCompType;
	
	@SuppressWarnings("unchecked")
  public RightListener(Object comp, Class compType) {
		mComp = comp;
		mCompType = compType;
	}
	
	public void actionPerformed(ActionEvent ae) {
		Method m = null;
		try {
			m = mCompType.getMethod(new String("findByArrowKey"), new Class[] {Integer.class});
		}
		catch (NoSuchMethodException ex) {ex.printStackTrace();}
		catch (SecurityException ex) {ex.printStackTrace();}
		
		try {
			m.invoke(mComp, new Object[] {new Integer(JOAConstants.NEXTSTN)});
		}
		catch (IllegalAccessException ex) {ex.printStackTrace();}
		catch (IllegalArgumentException ex) {ex.printStackTrace();}
		catch (InvocationTargetException ex) {ex.printStackTrace();}
	}
}

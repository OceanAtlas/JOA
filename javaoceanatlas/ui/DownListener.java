/*
 * $Id: DownListener.java,v 1.2 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class DownListener extends AbstractAction {
	Object mComp;
	@SuppressWarnings("unchecked")
  Class mCompType;
	
	@SuppressWarnings("unchecked")
  public DownListener(Object comp, Class compType) {
		mComp = comp;
		mCompType = compType;
	}
	
	public void actionPerformed(ActionEvent ae) {
		Method m = null;
		try {
			m = mCompType.getMethod(new String("findByArrowKey"), new Class[] {Integer.class});
		}
		catch (NoSuchMethodException ex) {;}
		catch (SecurityException ex) {;}
		
		try {
			m.invoke(mComp, new Object[] {new Integer(JOAConstants.NEXTOBS)});
		}
		catch (IllegalAccessException ex) {;}
		catch (IllegalArgumentException ex) {;}
		catch (InvocationTargetException ex) {;}
	}
}

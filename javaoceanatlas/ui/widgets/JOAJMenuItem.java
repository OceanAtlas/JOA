/**
 * 
 */
package javaoceanatlas.ui.widgets;


import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

/**
 * @author oz
 *
 */
//System.out.println(e.getSource());

public class JOAJMenuItem extends JMenuItem {
	private long mItemID;

	public JOAJMenuItem(long id) {
		mItemID = id;
	}

	/**
	 * @param icon
	 */
	public JOAJMenuItem(Icon icon, long id) {
		super(icon);
		mItemID = id;
	}

	/**
	 * @param text
	 */
	public JOAJMenuItem(String text, long id) {
		super(text);
		mItemID = id;
	}

	/**
	 * @param a
	 */
	public JOAJMenuItem(Action a, long id) {
		super(a);
		mItemID = id;
	}

	/**
	 * @param text
	 * @param icon
	 */
	public JOAJMenuItem(String text, Icon icon, long id) {
		super(text, icon);
		mItemID = id;
	}

	/**
	 * @param text
	 * @param mnemonic
	 */
	public JOAJMenuItem(String text, int mnemonic, long id) {
		super(text, mnemonic);
		mItemID = id;
	}
	
	public void setMenuID(long id) {
		mItemID = id;
	}
	
	public long getMenuID() {
		return mItemID;
	}
	
	public boolean isEqual(JOAJMenuItem mi) {
		return mi.getMenuID() == mItemID;
	}

}

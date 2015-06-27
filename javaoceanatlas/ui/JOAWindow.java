/*
 * $Id: JOAWindow.java,v 1.6 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.specifications.*;
import java.util.Vector;
import javaoceanatlas.events.WindowsMenuChangedEvent;
import javaoceanatlas.PowerOceanAtlas;
import javaoceanatlas.utility.JOAMenuBarHolder;

public abstract class JOAWindow extends JFrame {
	protected boolean mSpotable, mResizeable, mExtractable, mDrawAxes, mVisible;
	protected JOAJToolBar mToolBar = null;
	protected PlotSpecification mPlotSpec;
	protected boolean mWindowIsLocked = false;
	protected JOAMenuBarHolder mMenuBar = null;
	protected long mWindowID = System.currentTimeMillis();

	public JOAWindow(boolean spot, boolean resize, boolean extract, boolean drawAxes, boolean viz,
	    PlotSpecification plotspec) {
		mSpotable = spot;
		mResizeable = resize;
		mExtractable = extract;
		mDrawAxes = drawAxes;
		mVisible = viz;
		mPlotSpec = plotspec;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public JOAJToolBar getToolbar() {
		return mToolBar;
	}

	public Dimension getShape() {
		return new Dimension(this.getSize().width, this.getSize().height);
	}

	public Point getWindowLocation() {
		return this.getLocation();
	}

	public PlotSpecification getPlotSpecification() {
		return mPlotSpec;
	}

	public void forceRedraw() {
	}

	public abstract RubberbandPanel getPanel();

	public void windowsMenuChanged(WindowsMenuChangedEvent evt) {
//		System.out.println("windowsMenuChanged");
		try {
			if (mMenuBar == null)
				return;
			
			JMenu menu = mMenuBar.getSwingWindowsMenu();		
			menu.removeAll();

//			JOAJMenuItem cycleItem = new JOAJMenuItem("Cycle Through Windows", -99);
//			cycleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
//			    false));
//			menu.add(cycleItem);					
//			cycleItem.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					System.out.println("foobv");
//				}
//			});
//
//			menu.addSeparator();

			for (int f = 0; f < PowerOceanAtlas.getInstance().getOpenFileViewers().size(); f++) {
				try {
					// make a menu item for an open FileViewer
					final FileViewer fv = (FileViewer) (PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(f));
					final Vector<JOAWindow> windowList = fv.getOpenWindowList();
					String title = fv.getTitle();
					JOAJMenuItem item = new JOAJMenuItem(title, fv.getWindowID());
					item.setIcon(JOAMenuBar.blank);
					item.setActionCommand(title);

					// attach a actionlistener to the menu
					item.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							for (int ff = 0; ff < PowerOceanAtlas.getInstance().getOpenFileViewers().size(); ff++) {
								try {
									FileViewer ffv = (FileViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);

									Vector<JOAWindow> windowList4 = ffv.getOpenWindowList();
									for (int ii = 0; ii < windowList4.size(); ii++) {
										JOAWindow theWin = (JOAWindow) windowList4.elementAt(ii);
										String wtitle = theWin.getTitle();
										long winID = theWin.getWindowID();
										
										if (((JOAJMenuItem) e.getSource()).getMenuID() == winID) {
									    // workaround for mantis #1309
									    if (System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0) {
									      double x = theWin.getLocation().getX();
									      double y = theWin.getLocation().getY();
									      theWin.setVisible(false);
									      theWin.setLocation(0, 0); // if don't first do this, the next setLocation
									      // won't have any affect
									      theWin.setLocation((int) x, (int) y);
									    }

									    theWin.setVisible(true);
									    theWin.toFront();
										}
										else if (wtitle.equals(e.getActionCommand())) {
											theWin.toFront();
											break;
										}
									}
								}
								catch (ClassCastException cce) {
									// Some other kind of JOAViewer window was selected
									JOAViewer joavv = (JOAViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);
									String wtitle = joavv.getTitle();
									if (wtitle.equals(e.getActionCommand())) {
										((JOAWindow) joavv).toFront();
										break;
									}
								}
							}
						}
					});
					menu.add(item);

					// deal with the windows created by this FileViewer
					for (int i = 1; i < windowList.size(); i++) {
						JOAWindow joaWin = (JOAWindow) windowList.elementAt(i);
						title = joaWin.getTitle();
						item = new JOAJMenuItem("  " + title, joaWin.getWindowID());
						item.setActionCommand(title);
						item.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								for (int ff = 0; ff < PowerOceanAtlas.getInstance().getOpenFileViewers().size(); ff++) {
									try {
										FileViewer ffv = (FileViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);
										Vector<JOAWindow> windowList5 = ffv.getOpenWindowList();
										for (int ii = 0; ii < windowList5.size(); ii++) {
											JOAWindow theWin = (JOAWindow) windowList5.elementAt(ii);
											String wtitle = theWin.getTitle();
											long winID = theWin.getWindowID();
											if (((JOAJMenuItem) e.getSource()).getMenuID() == winID) {
										    // workaround for mantis #1309
										    if (System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0) {
										      double x = theWin.getLocation().getX();
										      double y = theWin.getLocation().getY();
										      theWin.setVisible(false);
										      theWin.setLocation(0, 0); // if don't first do this, the next setLocation
										      // won't have any affect
										      theWin.setLocation((int) x, (int) y);
										    }

										    theWin.setVisible(true);
										    theWin.toFront();
											}
											else if (wtitle.equals(e.getActionCommand())) {
												theWin.toFront();
												break;
											}
										}
									}
									catch (ClassCastException cce) {
										// Some other kind of JOAViewer window was selected
										JOAViewer joavv = (JOAViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);
										String wtitle = joavv.getTitle();
										if (wtitle.equals(e.getActionCommand())) {
											((JOAWindow) joavv).toFront();
											break;
										}
									}
								}
							}
						});
						menu.add(item);
					}
				}
				catch (ClassCastException ex) {
					// Make some other kind of JOAViewer window
					JOAViewer joavv = (JOAViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(f);
					String title = joavv.getTitle();
					JMenuItem item = new JMenuItem(title);
					item.setActionCommand(title);

					// attach a actionlistener to the menu
					item.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							for (int ff = 0; ff < PowerOceanAtlas.getInstance().getOpenFileViewers().size(); ff++) {
								try {
									FileViewer ffv = (FileViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);

									Vector<JOAWindow> windowList4 = ffv.getOpenWindowList();
									for (int ii = 0; ii < windowList4.size(); ii++) {
										JOAWindow theWin = (JOAWindow) windowList4.elementAt(ii);
										String wtitle = theWin.getTitle();
										if (wtitle.equals(e.getActionCommand())) {
											theWin.toFront();
											break;
										}
									}
								}
								catch (ClassCastException cce) {
									// Some other kind of JOAViewer window was selected
									JOAViewer joavv = (JOAViewer) PowerOceanAtlas.getInstance().getOpenFileViewers().elementAt(ff);
									String wtitle = joavv.getTitle();
									if (wtitle.equals(e.getActionCommand())) {
										((JOAWindow) joavv).toFront();
										break;
									}
								}
							}
						}
					});
					menu.add(item);
				}
			}
		}
		catch (Exception exx) {
			exx.printStackTrace();
		}
	}

	public long getWindowID() {
		return mWindowID;
	}
}

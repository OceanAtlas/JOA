/*
 * $Id: NdEdit.java,v 1.54 2005/10/18 23:44:53 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import javaoceanatlas.ui.ProgressDialog;
import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.beans.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.text.*;
import ndEdit.filters.*;
import ndEdit.filters.semaphore.*;
import ndEdit.filters.simple.*;
import gov.noaa.pmel.swing.*;
import gov.noaa.pmel.eps2.*;
import gov.noaa.pmel.util.*;
import ch.randelshofer.quaqua.*;
import ucar.multiarray.ArrayMultiArray;
import ucar.multiarray.MultiArray;
import java.net.URL;

 /**
 *
 *
 * @author  Chris Windsor
 * @version 1.0 01/13/00
 */
// -------------------------------------------------------------------
//
// Development note:  don't use static objects; just pass the top level
// object reference to the child it creates.  This way, there won't be a
// problem with having several of these objects running in the same VM.
//
/**
* NdEdit is a JavaBean, a JPanel containing a toolbar and a set of Cut Panels
* for viewing and filtering in-situ data.
*
* In-situ data is added to a constructed bean by calling setPointerCollection.
*
* NdEdit is comprised of several manager objects, each responsible for
* a distinct set of functionalities.  These Managers include: ViewManager,
* ChangeableInfoManager, BackForwardManager, and FilterConstraintsManager.
*/
public class NdEdit extends JPanel implements Lineage, ComponentListener, ActionListener, CIMFacade, PropertyChangeListener, ButtonMaintainer  {
	private int maximumVisibleViews;
	private int[] visibleViews = new int[Constants.MAX_CUT_PANEL_TYPES];
	private PointerCollectionGroup pointerCollectionGroup = new PointerCollectionGroup();
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private MainToolbar mainToolbar;
	private LocationToolbar locToolBar;
	static public NdEdit nd_edit = null;
	private ChangeableInfoMgr cim;
	private ViewManager viewManager;
	private BackForwardManager backForwardMgr;
	private FilterConstraintsManager filterConstraintsMgr;
	static private UserSettingsManager userSettingsManager;
	private Object parentObject;
	private SemaphoreFilter sfilter;
	//private SimpleFilter dfilter;
	private FilteredDataManager fdm;
	//private FilteredDataManager fdm1;
	boolean TRACE = false;
	boolean DEBUGSIZE = false;
	boolean mQuit = false;
	private Vector mListeners = new Vector();

	private Container menuParent;
    private JFrame parentFrame = null;
	NdEditActionList mActions;
	private String mDataTitle;
	private ProgressTimer mProgress;
	private static NdEdit ndeditinst = null;
	private int mNumPtrs = 0;
	private int mNumVisPtrs = 0;
	private int mNumSelPtrs = 0;
    protected MaintenanceTimer mMaintain = null;
    private SelectionInspector mSelectionInspector;

  /**
  * Creates managers, filters, and gui.
  */

  public NdEdit(Container jf, boolean installMenuBar) {
  	this(jf, installMenuBar, false, null);
  }

  public NdEdit(Container jf, boolean installMenuBar, Properties props) {
  	this(jf, installMenuBar, false, props);
  }

  public NdEdit(Container jf, boolean installMenuBar, boolean quitMode) {
  	this(jf, installMenuBar, quitMode, null);
  }

  public NdEdit(Container jf, boolean installMenuBar, boolean quitMode, Properties props) {
     super();

     
	if (props != null) {
		// path argument
		String supDir = props.getProperty("ndedit.support.dir");
		if (supDir != null) {
			Constants.SUPPORT_DIR = new String(supDir);
		}

		// path argument
		String useMetal = props.getProperty("ndedit.use.metal");
		if (useMetal != null && useMetal.equalsIgnoreCase("true")) {
			Constants.USE_METAL = true;
		}
	}

	String opSys = System.getProperty("os.name");
    Constants.ISMAC = opSys.startsWith("Mac");
     menuParent = jf;
     if(menuParent instanceof JFrame) parentFrame = (JFrame)menuParent;

     mQuit = quitMode;
     // please ensure that ChangeableInfoMgr is created first.
     cim = new ChangeableInfoMgr();
     
     // now attempt to set the properties from the default properties file
     Constants.USERSETTINGS = new UserSettings("ndeditsettings.txt");
     
     if (props != null) {
     	// look for any overridden properties in the properties object sent to the ctor
     	Constants.USERSETTINGS.propertiesToInternal(props);
     }
     
     userSettingsManager = new UserSettingsManager(this);
     backForwardMgr = new BackForwardManager(this);
     filterConstraintsMgr =	new FilterConstraintsManager(this);
     sfilter = new SemaphoreFilter(this);
     fdm = sfilter;
     viewManager = new ViewManager(this);

     this.setLayout(new BorderLayout());

     // create the actions associated with nededit menus
     mActions = new NdEditActionList(this, viewManager, userSettingsManager);

	 if (installMenuBar) {
     	// Create a menu bar
    	 NdEditMenuBar mMenuBar = new NdEditMenuBar(menuParent, this, mActions, false, mQuit);
     }

     createMainToolbar(viewManager.getToolbarButtons());
     locToolBar = new LocationToolbar();
     locToolBar.setFloatable(false);
     
     // create a selection inspector
     mSelectionInspector = new SelectionInspector();
     mSelectionInspector.hide();

     // create the progress thread
     //mProgress = new ProgressTimer(locToolBar, 500);
     //mProgress.startProgressPresenter();
     //mProgress.endProgressPresenter();

     JPanel tbPanel = new JPanel();
     tbPanel.setLayout(new BorderLayout(0, 0));
     tbPanel.add(mainToolbar, BorderLayout.NORTH);
     tbPanel.add(locToolBar, BorderLayout.SOUTH);
     //this.setBackground(new Color(200, 200, 200));
     //tbPanel.setBackground(new Color(200, 200, 200));

     this.add(tbPanel, BorderLayout.NORTH);
     this.add(viewManager.getCutPanelContainer(), BorderLayout.CENTER);
     viewManager.showAllViews();
     viewManager.setToolMode(Constants.SELECT_MODE);

     mainToolbar.invalidate();
     mainToolbar.validate();
     this.invalidate();
     this.validate();

     setPreferredSize();

     wireUpListeners();
     viewManager.setUserSettingsManager(userSettingsManager);
     userSettingsManager.publishAll();
		
	mMaintain = new MaintenanceTimer(this, 500);
	mMaintain.startMaintainer();
  }

  public ProgressTimer getProgressTimer() {
  	return mProgress;
  }

  public boolean getQuitMode() {
  	return mQuit;
  }
  
  public void setUserSettings(UserSettings uset) {
  	Constants.USERSETTINGS = uset;
  }


  /**
  * Encapsulates the wiring up all listeners to respective objects.
  */
  public void wireUpListeners() {
     // Connect up objects listenening for changes in UserSettings.
     userSettingsManager.addPropertyChangeListener(viewManager);
     userSettingsManager.addPropertyChangeListener(locToolBar);
     // This object listens for resizes of the CutPanelContainer
     //   so it can resize its preferredSize.
     viewManager.getCutPanelContainer().addComponentListener(this);
  }

  /**
  * Sets preferredSize to size of toolbar plus JScrollPane
  */
  public void setPreferredSize() {
     Dimension sz1 = mainToolbar.getPreferredSize();
     Dimension sz2 = viewManager.getCutPanelContainer().getPreferredSize();
     setPreferredSize(new Dimension(	sz1.width + sz2.width,
					sz1.height + sz2.height));
     if (DEBUGSIZE) {
     System.out.println(" -------------------------------------------------- ");
     System.out.println(" toolbar size: " + mainToolbar.getSize());
     System.out.println(" CutPanelContainer size: " + viewManager.getCutPanelContainer().getSize());
     System.out.println(" toolbar size: " + mainToolbar.getPreferredSize());
     System.out.println(" CutPanelContainer size: " + viewManager.getCutPanelContainer().getPreferredSize());
     System.out.println(" this preferred size: " + this.getPreferredSize());
     System.out.println(" -------------------------------------------------- ");
     }
  }

  /**
  * Sets preferredSize to size of toolbar plus JScrollPane
  */
  public Dimension getPreferredSize() {
     Dimension sz1 = mainToolbar.getPreferredSize();
     Dimension sz2 = viewManager.getCutPanelContainer().getPreferredSize();
     return (new Dimension(	sz1.width + sz2.width,
					sz1.height + sz2.height));
  }

  /**
  *
  */
  static public NdEdit getInstance() {
     return nd_edit;
  }

  /**
  * Creates toolbar from a dynamic set of toggle buttons (each representing a
  * single Cut Panel) and a static set of buttons such as "back", "forward", etc.
  *
  * @param cutPanelButtons
  */
  public void createMainToolbar(JToggleButton[] cutPanelButtons) {
     // these could be actions
     SmallIconButton zoomButn = null;
    try {
	    zoomButn = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/zoom.gif")));
    }
    catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    	System.out.println("NdEdit:createMainToolbar:zoomButn");
    }
     SmallIconButton selectButn = null;
    try {
	    selectButn = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/rectangle.gif")));
    }
    catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    	System.out.println("NdEdit:createMainToolbar:selectButn");
    }
     selectButn.setSelected(true);
     SmallIconButton sectionButn = null;
    try {
	    sectionButn = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/section.gif")));
    }
    catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    	System.out.println("NdEdit:createMainToolbar:sectionButn");
    }
     SmallIconButton polygonButn = null;
    try {
	    polygonButn = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/polygon.gif")));
    }
    catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    	System.out.println("NdEdit:createMainToolbar:polygonButn");
    }

     zoomButn.addActionListener(this);
     zoomButn.setActionCommand("setZoomMode");
     zoomButn.setToolTipText("Zoom Tool: Drag to zoom to region, Click to zoom in, Shift-Click to zoom out");
     selectButn.addActionListener(this);
     selectButn.setActionCommand("setSelectMode");
     selectButn.setToolTipText("Selection Tool: Drag to select observations");
     sectionButn.addActionListener(this);
     sectionButn.setActionCommand("setSectionMode");
     sectionButn.setToolTipText("Section Tool: Click to start/end a segment, Double-Click to end section");
     polygonButn.addActionListener(this);
     polygonButn.setActionCommand("setPolygonMode");
     polygonButn.setToolTipText("Polygon Tool: Click to start/end a segment, Double-Click to close polygon");

	 ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
     //SelectionToolAction selAction = new SelectionToolAction(b.getString("kSelectAll"), new ImageIcon("/gifs/action.gif"),
     //   														        viewManager, this);
     //ZoomToolAction zoomAction = new ZoomToolAction(b.getString("kSelectAll"), new ImageIcon("/gifs/action.gif"),
     //   														        viewManager, this);
    // SectionToolAction sectAction = new SectionToolAction(b.getString("kSelectAll"), new ImageIcon("/gifs/action.gif"),
     //   														        viewManager, this);

     mainToolbar = new MainToolbar(cutPanelButtons,
			new JButton[] {}, new JToggleButton[] {zoomButn, selectButn, polygonButn, sectionButn});
	mainToolbar.setFloatable(false);
  }

  /**
  *
  */
	public ViewManager getViewManager() {
		//System.out.println(" getViewManager ViewManager viewManager: " + viewManager);
    	return viewManager;
	}

  /**@tgGet*/
  /**
  *
  */
	public int getMaximumVisibleViews(){
    	return maximumVisibleViews;
	}
  /**@tgSet*/
  /**
  *
  * @param maximumVisibleViews restriction on number of Visible Views allowed
  */
  public void setMaximumVisibleViews(int maximumVisibleViews){
    this.maximumVisibleViews = maximumVisibleViews;
  }

  /**@tgGet*/
  /**
  *
  */
  public int[] getVisibleViews(){
    return visibleViews;
  }
  /**@tgSet*/

  /**
  *
  * @param viewEnums int array of static constants describing cut panel views
  *                  @see Constants
  */
  public void setVisibleViews(int[] viewEnums){
    for (int i=0; i < viewEnums.length; i++) {
       viewManager.showView(viewEnums[i]);
    }
    this.visibleViews = viewEnums;
  }


  /**
  * Pushes ChangeableInfo through to the ChangeableInfoManager for dissemination
  * to all interested objects; this method provides a bridge to the
  * ChangeableInfoManager for lower level objects scattered throughout the bean
  * that lack access to the ChangeableInfoManager object.
  *
  * @param ci
  */
  public void pushChangeableInfo(ChangeableInfo ci) {
     cim.pushChangeableInfo(ci);
  }

  /**
  * Pushes ChangeableInfo through to the ChangeableInfoManager for dissemination
  * to all interested objects; this method provides a bridge to the
  * ChangeableInfoManager for lower level objects scattered throughout the bean
  * that lack access to the ChangeableInfoManager object.
  *
  * @param id string identifier describing information that's changing.  This
  *                  identifier will be used as a tag by other objects interested
  *                  in this type of change. For instance, "LongitudeStart"
  *                  says the start longitude has changed.
  * @param oldValue object containing old value, if available
  * @param newValue object containing new value
  * @param undoable boolean flag indicating whether this change should be kept
  *                 by the back/forward manager for "undoing"
  */
	public void pushChangeableInfo(String id, Object oldValue, Object newValue, boolean undoable) {
		cim.pushChangeableInfo(new ChangeableInfo(id, oldValue, newValue, undoable));
  	}

  /**
  * Adds a changeableInfoListener object to the ChangeableInfoManager's listener
  * list; this method provides a bridge to the ChangeableInfoManager for lower
  * level objects scattered throughout the bean that lack access to the
  * ChangeableInfoManager object.
  *
  * @param obj object interested in listening for ChangeableInfo
  */
  public void addChangeableInfoListener(ChangeableInfoListener obj) {
     cim.addChangeableInfoListener(obj);
  }


  /**@tgGet*/
  /**
  *
  */
  public PointerCollectionGroup getPointerCollection() {
    return pointerCollectionGroup;
  }


  /**@tgSet*/
  /**
  * Resets the current pointer collection, notifying the filters, and
  * optionally resetting the current set of filtering constraints (extents).
  *
  * @param pc new pointer collection to be displayed and presented for filtering
  * @param resetFilteringConstraints boolean flag, if true, then set all
  *        filtering constaints (extents) to match the extremes of the data in
  *        the pointer collection
  *
  */
	public void setPointerCollection(PointerCollection pc, boolean resetFilteringConstraints) {
		if (this.pointerCollectionGroup != null) {
			this.pointerCollectionGroup.addElement(pc);
			pc.addPropertyChangeListener(this);

			// reset the individual panels so they redraw properly
			if (parentFrame != null)
				parentFrame.setTitle("NdEdit 2.0");
		}

	    Constants.LONGITUDE_CONV_FACTOR = 360.0f;
		fdm.newPointerCollection(pointerCollectionGroup);

		if (resetFilteringConstraints) {
			filterConstraintsMgr.resetFilterConstraints(new FilterConstraints(NdEditFormulas.GetPrettyRange(pointerCollectionGroup.getMinMaxLat(), -90.0, 90.0),
								NdEditFormulas.GetPrettyRange(pointerCollectionGroup.getMinMaxLon(), -180.0, 180.0), pointerCollectionGroup.getMinMaxDepth(), pointerCollectionGroup.getMinMaxTime(), null, null));
		}
		viewManager.setPointerCollection(pointerCollectionGroup);
		
		if (!resetFilteringConstraints)
			viewManager.invalidateAllViews();
		viewManager.getCutPanelContainer().resetPopupMenu();
		//resetAllViews();
	}

	public void resetAllViews() {
		viewManager.resetAllViews();
	}

	public void addToPointerCollection(PointerCollection pc, boolean resetFilteringConstraints) {
		this.pointerCollectionGroup.addElement(pc);
		fdm.newPointerCollection(pointerCollectionGroup);
		if (resetFilteringConstraints) {
			filterConstraintsMgr.resetFilterConstraints(new FilterConstraints(NdEditFormulas.GetPrettyRange(pointerCollectionGroup.getMinMaxLat(), -90.0, 90.0),
								NdEditFormulas.GetPrettyRange(pointerCollectionGroup.getMinMaxLon(), -180.0, 180.0), pointerCollectionGroup.getMinMaxDepth(), pointerCollectionGroup.getMinMaxTime(), null, null));
		}
		viewManager.setPointerCollection(pointerCollectionGroup);
		if (!resetFilteringConstraints)
			viewManager.invalidateAllViews();
	}




  /*
  * This is a courtesy routine provided to the application, and is not intended
  * to be called from anywhere within the bean.
  */
  /**
  *
  * Loads back from a stream a set of user settings (that were previously saved
  * by calling saveUserSettings), replacing the currently active user settings.
  *
  * @param inputStream
  */

  public void loadUserSettings(InputStream inputStream) {  }
  /* Save the current configuration of user settings to the
  * outputStream supplied.  This routine is intended to be called
  * not from within this bean, but from the application that
  * instantiates the bean.*/
  /**
  * Saves the current configuration of user settings to the outputStream
  * supplied.
  *
  * @param outputStream
  */
  public void saveUserSettings(OutputStream outputStream) {
  }

  //
  // gets the current set of filter constraints.
  //
  /**
  * Gets the current set of filter constraints as narrowed by user.
  */
  public FilterConstraints getFilterContraints(){
     return filterConstraintsMgr.getFilterConstraints();
  }

  /**
  * Sets the actual Cut Panel size.
  *
  * @param cutPanelSize
  */
  public void setCutPanelSize(Dimension cutPanelSize){
    Dimension oldCutPanelSize = viewManager.getCutPanelSize();
    pcs.firePropertyChange("cutPanelSize", oldCutPanelSize, cutPanelSize);
  }
  /**
  * Traverses the construction hierarchy, returning the object at
  * the topmost class of the current bean.
  */
	public Object getAncestor() {
		if (parentObject != null) {
			return ((Lineage)parentObject).getAncestor();
		}
		return this;
	}
  //-----------------------------------------------------------
  // Component Listener Routines

  public void componentHidden(ComponentEvent e) {}
  public void componentMoved(ComponentEvent e) {}
  public void componentResized(ComponentEvent e) {
     this.invalidate();
     this.validate();
     //System.out.println(" Component Resized \n" + e.getComponent() + "\n");
  }
  public void componentShown(ComponentEvent e) {}

	static public void main(String[] args) {
		if (args.length > 0) {
			for (int i=0; i<args.length; i++) {
				String arg = args[i];
				if (arg.length() > 0) {
					if (arg.startsWith("-P")) {
						// path argument
						Constants.SUPPORT_DIR = new String(arg.substring(2, arg.length()));
					}
					else if (arg.toUpperCase().startsWith("-METAL")) {
						// path argument
						Constants.USE_METAL = true;
					}
				}
			}
		}

	    if (!Constants.USE_METAL) {
			try {
				UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
			}
			catch (Exception e) {
				try {
			 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			 	}
			 	catch (Exception ex) {
			 		try {
	          UIManager.setLookAndFeel(new MetalLookAndFeel());
          }
          catch (UnsupportedLookAndFeelException e1) {
	          // TODO Auto-generated catch block
	          e1.printStackTrace();
          }
			 	}
			}
		}

     	final JFrame fr = new JFrame();
		ndeditinst = new NdEdit(fr, true, true);
		//ndeditinst.setPointerCollection(Constants.DEFAULT_PC, true);
       	fr.setSize(900, 700);
		fr.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				fr.setVisible(false);
				fr.dispose();
				System.exit(0);
			}
		});

		//UserSettingsAction userSettingsAction = new UserSettingsAction("edit", "User Settings ...", new ImageIcon("/gifs/action.gif"), uset);
		//userSettingsAction.addChangeListener(this);

		fr.setTitle("NdEdit 2.0");

		fr.getContentPane().add(ndeditinst);
		fr.validate();
		fr.invalidate();
		fr.setLocation(10, 10);
		fr.setVisible(true);
		//fr.addComponentListener(new CompList(fr));
		fr.addWindowListener(new WindowAdapter() {

		public void windowClosing(WindowEvent e) {
		Window w = e.getWindow();
			w.setVisible(false);
			w.dispose();
			System.exit(0);
			}
		});
	}


  // Filter constraints are set here in an indirect way ... via the same channel
  // for making individual filter changes.  Each filter parameter
  // will be sent through the ChangeableInfo event queue to permeate the
  // change through the system, that is out to all gui components and through
  // the active filters.
  //
  /**
  * Sets filtering constraints by passing new constraints to the FilterConstraints
  * Manager.
  *
  * @param fc
  */
  public void setFilterConstraints(FilterConstraints fc){
     filterConstraintsMgr.resetFilterConstraints(fc);
  }

/*
  public void setFilterConstraints(URL fcFile) {
  }
*/

   	public void processEvent(AWTEvent evt) {
		if (evt instanceof DataChangedEvent) {
			if (mListeners != null) {
				for (int i=0; i<mListeners.size(); i++) {
					((DataChangedListener)mListeners.elementAt(i)).dataChanged((DataChangedEvent)evt);
				}
			}
		}
	}

	public void addDataChangedListener(DataChangedListener l) {
		if (mListeners.indexOf(l) < 0)
			mListeners.addElement(l);
	}

	public void removeDataChangedListener(DataChangedListener l) {
		mListeners.removeElement(l);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("setZoomMode")) {
			viewManager.setToolMode(Constants.ZOOM_MODE);
		}
		else if (cmd.equals("setSelectMode")) {
			viewManager.setToolMode(Constants.SELECT_MODE);
		}
		else if (cmd.equals("setSectionMode")) {
			viewManager.setToolMode(Constants.SECTION_MODE);
		}
		else if (cmd.equals("setPolygonMode")) {
			viewManager.setToolMode(Constants.POLYGON_MODE);
		}
	}

	/**
	* Creates and shows a dialog sporting a table with key/value system properties.
	*/
	public void showSystemProperties() {
		try {
			JSystemPropertiesDialog propDisplay = new JSystemPropertiesDialog();
			propDisplay.setModal(true);

			// show dialog at center of screen
			Rectangle dBounds = propDisplay.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width/2 - dBounds.width/2;
			int y = sd.height/2 - dBounds.height/2;
			propDisplay.setLocation(x, y);
			propDisplay.show();
		}
		catch (Exception e) {}
	}

	public FilterConstraintsManager getFilterConstraintsManager() {
    	return filterConstraintsMgr;
	}

	public FilteredDataManager getFilteredDataManager() {
		return fdm;
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("sectionwidth")) {
			// section width changed
			Constants.SECTION_WIDTH = Float.valueOf((String)e.getNewValue()).floatValue();
		}
	}

    public void setParentFrame(JFrame parent) {
      parentFrame = parent;
    }

	public JFrame getFrame() {
		return parentFrame;
	}

	public NdEditActionList getActions() {
		return mActions;
	}
	
	public void writeInvFile(File invFile, File locFile, int[] mSortKeys) {
		// write the inventory file directly instead of going the EpicPtrs
    	try {
	    	FileOutputStream fos = new FileOutputStream(invFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
	    	DataOutputStream out = new DataOutputStream(bos);
	    	
	    	FileOutputStream fos2 = null;
	    	BufferedOutputStream bos2 = null;
	    	DataOutputStream out2 = null;
		    if (locFile != null) {
		    	fos2 = new FileOutputStream(locFile);
				bos2 = new BufferedOutputStream(fos2, 1000000);
		    	out2 = new DataOutputStream(bos2);
		    }
	    	
			try {
				for (int p=0; p<this.getPointerCollection().size(); p++) {
					// get a pointer collection
					PointerCollection pc = (PointerCollection)pointerCollectionGroup.elementAt(p);
					// get the arrays
					double[] lats = pc.getLatArr1();
					double[] lons = pc.getLonArr1();
					double[] times = pc.getTimeArr1();
					double[] depths = pc.getDepthArr1();
					double[] depths2 = pc.getDepthArr2();
								
					// need arrays for the other information
					String[] cruiseIDs = pc.getCruiseArr();
					String[] URLs = pc.getPathArr();
		            String[] files = pc.getExtraArr1();
		            String[] ocean = pc.getExtraArr2();
					String[] posQCs = pc.getExtraArr3();
					String[] timeQCs = pc.getExtraArr4();
		            String[] dataCtrs = pc.getExtraArr5();
		            String[] dataModes = pc.getExtraArr6();
		            String[] numLevels = pc.getExtraArr7();
		            String[] numParams = pc.getExtraArr8();
		            String[] paramLists = pc.getExtraArr9();
		            
		            // write the header line
		            out.writeBytes("callSign,data_URL,file,ocean,date,time,time_qc,latitude,longitude,position_qc,data_center,data_mode,num_of_levels,min_D_P,max_D_P,num_of_param,param1,param2,param3,param4,param5");
		            out.writeBytes("\n");
		            
					GeoDate gDate;
		            for (int d=0; d<pc.getSize(); d++) {
						if (!pc.isSelected(d))
							continue;
							
						gDate = new GeoDate((long)times[d]);
						String dateTimeStr = gDate.toString("yyyy-MM-dd,HH:mm");
							
			            // write an inventory detail line 
			            
			            if (cruiseIDs != null)
			            	out.writeBytes(cruiseIDs[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (URLs != null)
			            	out.writeBytes(URLs[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (files != null)
			            	out.writeBytes(files[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (ocean != null)
			            	out.writeBytes(ocean[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (dateTimeStr != null)
			            	out.writeBytes(dateTimeStr + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (timeQCs != null)
			            	out.writeBytes(timeQCs[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (timeQCs != null)
			            	out.writeBytes(timeQCs[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (lats != null)
			            	out.writeBytes(NdEditFormulas.formatDouble(lats[d], 3, false) + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (lons != null)
			            	out.writeBytes(NdEditFormulas.formatDouble(lons[d], 3, false) + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (posQCs != null)
			            	out.writeBytes(posQCs[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (dataCtrs != null)
			            	out.writeBytes(dataCtrs[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (dataModes != null)
			            	out.writeBytes(dataModes[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (numLevels != null)
			            	out.writeBytes(numLevels[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (depths != null)
			            	out.writeBytes(NdEditFormulas.formatDouble(depths[d], 3, false) + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (depths2 != null)
			            	out.writeBytes(NdEditFormulas.formatDouble(depths2[d], 3, false) + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (numParams != null)
			            	out.writeBytes(numParams[d] + ",");
			            else 
			            	out.writeBytes("na,");
			            
			            if (paramLists != null)
			            	out.writeBytes(paramLists[d] + "\n");
			            else 
			            	out.writeBytes("na,");
			            	
			            if (out2 != null && files != null) {
			            	out2.writeBytes(files[d] + "\n");
			            }
			            else
			            	out2.writeBytes("na\n");
			        }
			        out.close();
			        if (out2 != null)
			        	out2.close();
					
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
				out.close();
		        if (out2 != null)
		        	out2.close();
			}
		}
		catch (Exception exx) {
		}
	}
	
	public String padCycleNum(String c) {
		String loc = new String(c);
		while (loc.length() < 3)
			loc = "0" + loc;
		return loc;
	}
	
	public void writePtrFile(File outFile, int[] mSortKeys) {
		ProgressDialog progress = new ProgressDialog(parentFrame, "Extracting Profiles...", Color.blue, Color.white);
		progress.setVisible(true);
		
		try {
			// create a ptr database
			EpicPtrs ptrDB = new EpicPtrs(outFile);
			String ptrFileName = outFile.getName();
			String lcName = ptrFileName.toLowerCase();
			if (lcName.indexOf(".xml") > 0)
				ptrDB.setWriter(new XMLPtrFileWriter(outFile));
			else
				ptrDB.setWriter(new EpicPtrFileWriter(outFile));

			ArrayList filePtrs = new ArrayList();

			for (int p=0; p<this.getPointerCollection().size(); p++) {
				// get a pointer collection
				PointerCollection pc = (PointerCollection)pointerCollectionGroup.elementAt(p);

				// get the arrays
				double[] lats = pc.getLatArr1();
				double[] lons = pc.getLonArr1();
				double[] lats2 = pc.getLatArr2();
				double[] lons2 = pc.getLonArr2();
				double[] times = pc.getTimeArr1();
				double[] times2 = pc.getTimeArr2();
				double[] depths = pc.getDepthArr1();
				double[] depths2 = pc.getDepthArr2();

				if (pc instanceof TuplePointerCollection) {
					// these will only come from a tuple pointer collection
					String[] fileNames = pc.getFileNameArr();
					String[] paths = pc.getPathArr();
					String[] types = pc.getDataTypeArr();
					String[] casts = pc.getCastArr();
					String[] cruises = pc.getCruiseArr();
					double[] deltas = pc.getDeltaArr();
					boolean[] pathsRelative = pc.getPathIsRelativeArr();
					EpicPtr epPtr = null;

					// loop through the pointer collection
					int c = 0;
					for (int i=0; i<pc.getSize(); i++) {
						if (!pc.isSelected(i))
							continue;

						progress.setPercentComplete(100.0 * ((double) ++c / (double) pc.getNumSelectedPtrs()));
						
						double lat = lats[i];
						double lon = lons[i];
						double lat2 = Float.NaN;
						double lon2 = Float.NaN;
						double tmin = times[i];
						double tmax = Float.NaN;
						double zmin = depths[i];
						double zmax = Float.NaN;

						if (lon > 180)
							lon -= 360.0;

						if (lats2 != null)
							lat2 = lats2[i];

						if (lons2 != null) {
							lon2 = lons2[i];
							if (lon2 > 180)
								lon2 -= 360.0;
						}

						if (times2 != null)
							tmax = times2[i];

						if (depths2 != null)
							zmax = depths2[i];

						String fileName = "na";
						String dir = "na";
						String dType = "profile";
            if(depths2 == null) dType = "time series";
						String theCast = "na";
						String theCruise = "na";
						double delta = 360;

						if (fileNames != null)
							fileName = fileNames[i];
						if (paths != null)
							dir = paths[i];
						if (types != null)
							dType = types[i];
						if (casts != null)
							theCast = casts[i];
						if (cruises != null)
							theCruise = cruises[i];
						if (deltas != null)
							delta = deltas[i];

						if (dType.equals("profile")) {
							// profile
							epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "NdEdit Export", dType, theCruise, theCast, lat, lon,
				                   new GeoDate((long)tmin), zmin, zmax, null, fileName, dir, null);
				       		epPtr.setIsRelativePath(pathsRelative[i]);
				        }
				       	else if (dType.equals("time series"))
							epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "NdEdit Export", dType, zmin, lat, lon,
				                   new GeoDate((long)tmin), new GeoDate((long)tmax), delta, null, fileName, dir);
				       filePtrs.add(epPtr);
					}
					ptrDB.setData(filePtrs);
				}
				else if (pc instanceof GridPointerCollection) {
					// make a profile for GridPointerCollection
					// build an array to keep track of whether we have already found a particular profile
					int numLats = ((GridPointerCollection)pc).getNumLats();
					int numLons = ((GridPointerCollection)pc).getNumLons();
					int numTs = ((GridPointerCollection)pc).getNumTimes();
					double[] zaxis = ((GridPointerCollection)pc).getDepthAxis();
					int numZs = zaxis.length;
					int numVars = ((GridPointerCollection)pc).getNumMeasuredVars();
					int numSelected = ((GridPointerCollection)pc).getNumSelected();
					int numVirtProfiles = 0;
					String section = ((GridPointerCollection)pc).getPCTitle();
					Vector attrs = ((GridPointerCollection)pc).getAttributes();

					// create a hastable to store virtual profiles in
					Hashtable virtProfilesTable = new Hashtable();

					// create the array of parameter names and units
					String[] params = new String[numVars];
					String[] units = new String[numVars];
					for (int i=0; i<numVars; i++) {
						ucar.nc2.Variable v = (ucar.nc2.Variable)(((GridPointerCollection)pc).getMeasuredVar(i));
						 params[i] = new String(v.getName());
						ucar.nc2.Attribute attr = v.findAttribute("units");
    					if (attr != null)
     						units[i] = new String(attr.getStringValue());
     					else
     						units[i] = "";
					}

					// now loop on the pointer collection again to build the virtual profiles
					int vpc = 0;
					for (int i=0; i<pc.getSize(); i++) {
						if (!pc.isSelected(i))
							continue;

						progress.setPercentComplete(100.0 * ((double) vpc / (double) numSelected));
							
						// need to get all the values of the variables at each depth 
						double lat = ((GridPointerCollection)pc).getLat1(i);
						double lon = ((GridPointerCollection)pc).getLon1(i);
						double t = ((GridPointerCollection)pc).getT1(i); // what if time is a vector?
						double t2 = 0;
						if (pc.getTimeArr2() != null)
							t2 = ((GridPointerCollection)pc).getT2(i); // what if time is a vector?
						
						// z is ignored--we want all the z values
						double z = ((GridPointerCollection)pc).getZ1(i);
								
						Double key = ((GridPointerCollection)pc).getKey(i);
						if (!virtProfilesTable.containsKey(key)) {
							// found new virtual profile
							double[][] vals;
							vals = ((GridPointerCollection)pc).getMeasuredVals(lat, lon, t);
							if (lon > 180)
								lon -= 360.0;
							VirtualProfile vp = new VirtualProfile(vpc, section, lat, lon, t, zaxis, params, units, vals, attrs);
							
							virtProfilesTable.put(key, new Boolean(true));
							
							// write the profile to disk so I'm not keeping the Virtual Profile around in the hashtable

							// generate a filename for this virtual pointer
							String fileName = vp.getSection() + "_" + vp.getStation() + ".nc";

							// write the vp to an independent file
							if (!vp.isBlank()) {
								vp.truncate();
								
								if (vp.getNumLevels() > 0) {
									vp.writeAsFile(EPSConstants.NETCDFFORMAT, fileName, outFile.getParent());

									// get an EPIC Pointer and add to database
							       filePtrs.add(vp.getEpicPointer(EPSConstants.NETCDFFORMAT, fileName, outFile.getParent()));
									vpc++;
								}
							}
							vp = null;
						}
						else {
							System.out.println(key + " IGNORED lat = " + lat + " lon = " + lon + " t = " + t);
						}
					}
					ptrDB.setData(filePtrs);
				}
			}

			try {
				if (mSortKeys != null) {
					// set the sort keys
					ptrDB.setSort(mSortKeys);

					// sort
					ptrDB.doSort();
				}

				// write
				ptrDB.writePtrs();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			progress.dispose();
		}
	}
	
	public PointerFileAttributes getSelAttributes() {
		// turn the selected stations into PointerFileAttributes (spatial/temporal domain)
		double mLonMin = 360.0;		
		double mLatMin = 90.0;		
		double mLonMax = -360.0;
		double mLatMax = -90.0;
		double mTMin = 99999999;
		double mTMax = -99999999;
		double mZMinMin = 9999.0;
		double mZMinMax = 0.0;
		double mZMaxMin = 9999.0;
		double mZMaxMax = 0.0;
	    GeoDate mMinDate = new GeoDate();
	    GeoDate mMaxDate = new GeoDate();
	    ArrayList masterVarList = new ArrayList();
		        
		NameItDialog nameIt = new NameItDialog("Name New Data Window", "Dataset name:", "");
		nameIt.setModal(true);
		nameIt.setVisible(true);
		String dataset = nameIt.getName();
		EPSProperties.SDELIMITER = EPSProperties.SCOMMA_DELIMITER;
				
		for (int p=0; p<this.getPointerCollection().size(); p++) {
			// get a pointer collection
			PointerCollection pc = (PointerCollection)pointerCollectionGroup.elementAt(p);

			// get the arrays
			double[] lats = pc.getLatArr1();
			double[] lons = pc.getLonArr1();
			double[] times = pc.getTimeArr1();
			double[] depths = pc.getDepthArr1();
			double[] depths2 = pc.getDepthArr2();
			String params[] = pc.getExtraArr9();
			if (pc instanceof TuplePointerCollection) {
				// loop through the pointer collection
				for (int i=0; i<pc.getSize(); i++) {
					if (!pc.isSelected(i))
						continue;
					double lat = lats[i];
					double lon = lons[i];
					double tmin = times[i];
					double zmin = depths[i];
					double zmax = Float.NaN;
						
					mLatMin = lat < mLatMin ? lat : mLatMin;
					mLatMax = lat > mLatMax ? lat : mLatMax;

					if (lon > 180)
						lon -= 360.0;
						
					mLonMin = lon < mLonMin ? lon : mLonMin;
					mLonMax = lon > mLonMax ? lon : mLonMax;
					
					mZMaxMin = zmin < mZMaxMin ? zmin : mZMaxMin;
					mZMinMax = zmin > mZMinMax ? zmin : mZMinMax;

					if (depths2 != null) {
						zmax = depths2[i];
						
						mZMinMin = zmax < mZMinMin ? zmax : mZMinMin;
						mZMaxMax = zmax > mZMaxMax ? zmax : mZMaxMax;
					}
						
					mTMin = tmin < mTMin ? tmin : mTMin;
					mTMax = tmin > mTMax ? tmin : mTMax; 
				
					//get the gloabl param list
					if (params != null) {
						EPS_Util.CollectUniqueItems(params[i], masterVarList);
					}
				}
			}
		}
		
		// make an arraylist of lats
		ExportLatitude expLat1 = new ExportLatitude(mLatMin, "south");
		ExportLatitude expLat2 = new ExportLatitude(mLatMax, "north");
		ArrayList glats = new ArrayList();
		glats.add(expLat1);
		glats.add(expLat2);
		
		ExportLongitude expLon1 = new ExportLongitude(mLonMin, "west");
		ExportLongitude expLon2 = new ExportLongitude(mLonMax, "east");
		ArrayList glons = new ArrayList();
		glons.add(expLon1);
		glons.add(expLon2);

		// create depth range
		ArrayList verts = new ArrayList();
		verts.add(new ExportVertical(mZMinMin, "top", "db", "down"));
		verts.add(new ExportVertical(mZMaxMax, "bottom", "db", "down"));
		
		// create the time range
		ArrayList dates = new ArrayList();
		dates.add(new ExportDate(new GeoDate((long)mTMin), "start"));
		dates.add(new ExportDate(new GeoDate((long)mTMax), "end"));
		
		ArrayList globalVars = new ArrayList();
		 		
    	for (int i=0; i<masterVarList.size(); i++) {
    		// don't know lexicon or units
    		ExportVariable exp = new ExportVariable((String)masterVarList.get(i), null, "unk");
    		globalVars.add(exp);
		}
		
		return new PointerFileAttributes(dataset, null, "profile", glats, glons, verts, dates, globalVars);
	}

	public ArrayList getSelFileSets(PointerFileAttributes attribs) {
		// turn the selected stations into PointerFileAttributes
		ArrayList stns = new ArrayList();
		ArrayList filesets = new ArrayList();
		boolean hasMasterPath = true;
		String masterPath = null;
		
		for (int p=0; p<this.getPointerCollection().size(); p++) {
			// get a pointer collection
			PointerCollection pc = (PointerCollection)pointerCollectionGroup.elementAt(p);

			// get the arrays
			double[] lats = pc.getLatArr1();
			double[] lons = pc.getLonArr1();
			double[] lats2 = pc.getLatArr2();
			double[] lons2 = pc.getLonArr2();
			double[] times = pc.getTimeArr1();
			double[] times2 = pc.getTimeArr2();
			double[] depths = pc.getDepthArr1();
			double[] depths2 = pc.getDepthArr2();

			if (pc instanceof TuplePointerCollection) {
				// these will only come from a tuple pointer collection
				String[] fileNames = pc.getFileNameArr();
				String[] paths = pc.getPathArr();
				String[] types = pc.getDataTypeArr();
				String[] casts = pc.getCastArr();
				String[] cruises = pc.getCruiseArr();
				double[] deltas = pc.getDeltaArr();
				boolean[] pathsRelative = pc.getPathIsRelativeArr();

				// loop through the pointer collection
				for (int i=0; i<pc.getSize(); i++) {
					if (!pc.isSelected(i))
						continue;
					double lat = lats[i];
					double lon = lons[i];
					double tmin = times[i];
					double zmin = depths[i];
					double zmax = Float.NaN;

					if (lon > 180)
						lon -= 360.0;

					if (depths2 != null)
						zmax = depths2[i];

					String fileName = "na";
					String dir = "na";
					String dType = "profile";
					String theCast = "na";
					String theCruise = "na";

					if (fileNames != null)
						fileName = fileNames[i];
					if (paths != null) {
						dir = paths[i];
						if (masterPath == null)
							masterPath = new String(dir);
						if (masterPath != null) {
							if (!dir.equalsIgnoreCase(masterPath))
								hasMasterPath = false;
						}
					}
					if (types != null)
						dType = types[i];
					if (casts != null)
						theCast = casts[i];
					if (cruises != null)
						theCruise = cruises[i];

					if (dType.equals("profile")) {
						// profile
						GeoDate theDate = new GeoDate((long)tmin);
						ArrayList verts = new ArrayList();
				    	verts.add(new ExportVertical(zmin, "top", "db"));
				    	if (depths2 != null)
				    		verts.add(new ExportVertical(zmax, "bottom", "db"));
					    ExportStation epPtr = new ExportStation(EPSConstants.NETCDFFORMAT, "JOA Export", "profile", theCruise, 
					    	theCast, "1", new ExportLatitude(lat), new ExportLongitude(lon), 
					    	new ExportDate(theDate, "point"), verts, null, fileName, dir, null, -99);
			            stns.add(epPtr);
			        }
				}
				String mCurrFSURI = null;
				if (hasMasterPath)
					mCurrFSURI = masterPath;
				
				filesets.add(new ExportFileSet(attribs.getOrigFilename(), mCurrFSURI, attribs.getVarList(), stns));
			}
		}	
			
		return filesets;
	}

	private class VirtualProfile {
		private int mStn, mNumZs, mNumParams;
		private double[] mZAxis;
		private double mLat, mLon;
		private double mTime;
		private String[] mParams, mUnits;
		private double[][] mLevelValues;
		private String mSection;
		private Vector mGlobalAttributes = null;
		boolean mIsBlank = true;

		public VirtualProfile(int stn, String section, double lat, double lon, double t, double[] zaxis,
			String[] params, String[] units, double[][] paramVals, Vector gattrs) {
			mStn = stn;
			mSection = new String(section);
			mLat = lat;
			mLon = lon;
			mTime = t;
			mNumZs = zaxis.length;
			mZAxis = new double[mNumZs];
			for (int i=0; i<mNumZs; i++) {
				mZAxis[i] = zaxis[i];
			}
			mNumParams = params.length;
			mParams = params;
			mUnits = units;

			mLevelValues = new double[mNumZs][mNumParams];
			double oldVal =  paramVals[0][0];
			for (int l=0; l<mNumZs; l++) {
				for (int p=0; p<mNumParams; p++) {
					mLevelValues[l][p] = paramVals[l][p];
					if (mIsBlank && paramVals[l][p] != oldVal)
						mIsBlank = false;
					oldVal = paramVals[l][p];
				}
			}

			// add global attributes (if any)
			if (gattrs != null && gattrs.size() > 0) {
				mGlobalAttributes = new Vector();
				for (int i=0; i<gattrs.size(); i++) {
					mGlobalAttributes.addElement(gattrs.elementAt(i));
				}
			}
		}
		
		public boolean isBlank() {
			return mIsBlank;
		}
		
		public void truncate() {
			double[][] retProf = null;
			
			
			/*System.out.println("Before ");
			for (int i=0; i<mNumZs; i++) {
				System.out.print(i + "\t");
				for (int p=0; p<mNumParams; p++) {
					double val = mLevelValues[i][p];
					System.out.print(val + "\t");
				}
				System.out.println();
			}*/
			
			int lastLevel = mNumZs;
			for (int z=mNumZs-1; z>0; z--) {
				boolean allValsMissing = true;
				for (int p=0; p<mNumParams; p++) {
					double val = mLevelValues[z][p];
					if (val > -99 && val < 1e10) {
						allValsMissing = false;
						break;
					}
				}
				if (allValsMissing) {
					lastLevel = z;
				}
			}
			retProf = new double[lastLevel][mNumParams];
			for (int z=0; z<lastLevel; z++) {
				for (int p=0; p<mNumParams; p++) {
					retProf[z][p] = mLevelValues[z][p];
				}
			}
			mNumZs = lastLevel;
			mLevelValues = null;
			mLevelValues = retProf;
			
			/*System.out.println("After ");
			for (int i=0; i<mNumZs; i++) {
				System.out.print(i + "\t");
				for (int p=0; p<mNumParams; p++) {
					double val = mLevelValues[i][p];
					System.out.print(val + "\t");
				}
				System.out.println();
			}*/
		}
		
		public int getNumLevels() {
			return mNumZs;
		}

		public void addLevel(double z, double[] paramVals) {
			for (int i=0; i<mNumZs; i++) {
				double tstVal = mZAxis[i];
				if (z == tstVal) {
					// belong to ith level
					for (int p=0; p<mNumParams; p++) {
						mLevelValues[i][p] = paramVals[p];
						if (paramVals[p] != 1e20)
							mIsBlank = false;
					}
					return;
				}
			}

			// first level in profile
			for (int p=0; p<mNumParams; p++) {
				mLevelValues[0][p] = paramVals[p];
				if (paramVals[p] != -99)
					mIsBlank = false;
			}
		}

		public void sort() {
			// sort the levels in ascending or descending levels
		}

		public void dumpLevels() {
			GeoDate gd = new GeoDate((long)mTime);
			System.out.println("stn = " + mStn);
			System.out.println("lat = " + mLat + " lon = " + mLon + " time = " + mTime + " " + gd);
			System.out.print("z" + "\t");
			for (int p=0; p<mNumParams; p++) {
				System.out.print(mParams[p] + "\t");
			}
			System.out.println();
			System.out.println(" " + "\t");
			for (int p=0; p<mNumParams; p++) {
				System.out.print(mUnits[p] + "\t");
			}
			for (int i=0; i<mNumZs; i++) {
				System.out.print(mZAxis[i] + "\t");
				for (int p=0; p<mNumParams; p++) {
					System.out.print(mLevelValues[i][p] + "\t");
				}
				System.out.println();
			}
		}

		public EpicPtr getEpicPointer(int format, String filename, String dir) {
			EpicPtr epPtr = new EpicPtr(format, "NdEdit Export", "profile", mSection, String.valueOf(mStn), mLat, mLon,
                   new GeoDate((long)mTime), mZAxis[0], mZAxis[mNumZs-1], null, filename, dir, null);
			return epPtr;
		}

		public String getSection() {
			return mSection;
		}

		public String getStation() {
			return String.valueOf(mStn);
		}

		public void writeAsFile(int format, String fname, String dir) {
			if (format == EPSConstants.NETCDFFORMAT) {
			 // make a DBase object
			    Dbase db = new Dbase();

			    // add the global attributes
			    short[] sarray = new short[1];

				String sStn = String.valueOf(mStn);
			    //db.addEPSAttribute("CREATION_DATE", EPCHAR, 8, "Today");
			    db.addEPSAttribute("CRUISE", EPSConstants.EPCHAR, mSection.length(), mSection);
			    db.addEPSAttribute("CAST", EPSConstants.EPCHAR, sStn.length(), sStn);
			    //db.addEPSAttribute("DATA_ORIGIN", EPSConstants.EPCHAR, sech.mShipCode.length(), sech.mShipCode);
			    String dType = "BOTTLE";
			    db.addEPSAttribute("DATA_TYPE", EPSConstants.EPCHAR, dType.length(), dType);

			    //add global attributes from the pointer collection
			    if (mGlobalAttributes != null) {
		            int[] 		iarray;			//nclong
		            double[] 	rarray;
		            double[]  	darray;

			    	for (int a=0; a<mGlobalAttributes.size(); a++) {
			    		ucar.nc2.Attribute at = (ucar.nc2.Attribute)mGlobalAttributes.elementAt(a);

		                String name = at.getName();
		                // get the type
		                String type = at.getValueType().getName();

		                // get the length
		                int atlen = at.getLength();

		                // install epic attributes
		                if (type.equalsIgnoreCase("char")) {
		                    String valc = at.getStringValue();
		                    db.addEPSAttribute(name, EPSConstants.EPCHAR, atlen, (Object)valc);
		                }
		                else if (type.equalsIgnoreCase("short")) {
		                    // array of shorts
		                    sarray = new short[atlen];
		                    for (int i=0; i<atlen; i++) {
		                        sarray[i] = ((Short)at.getNumericValue(i)).shortValue();
		                    }
		                    db.addEPSAttribute(name, EPSConstants.EPSHORT, atlen, (Object)sarray);

		                }
		                else if (type.equalsIgnoreCase("int")) {
		                    // array of ints
		                    iarray = new int[atlen];
		                    for (int i=0; i<atlen; i++) {
		                        iarray[i] = ((Integer)at.getNumericValue(i)).intValue();
		                    }
		                    db.addEPSAttribute(name, EPSConstants.EPINT, atlen, (Object)iarray);
		                }
		                else if (type.equalsIgnoreCase("long")) {
		                    // array of longs
		                    iarray = new int[atlen];
		                    for (int i=0; i<atlen; i++) {
		                        iarray[i] = ((Integer)at.getNumericValue(i)).intValue();
		                    }
		                    db.addEPSAttribute(name, EPSConstants.EPINT, atlen, (Object)iarray);
		                }
		                else if (type.equalsIgnoreCase("double")) {
		                    // array of floats
		                    rarray = new double[atlen];
		                    for (int i=0; i<atlen; i++) {
		                        rarray[i] = ((Float)at.getNumericValue(i)).floatValue();
		                    }
		                    db.addEPSAttribute(name, EPSConstants.EPREAL, atlen, (Object)rarray);
		                }
		                else if (type.equalsIgnoreCase("double")) {
		                    // array of doubles
		                    darray = new double[atlen];
		                    for (int i=0; i<atlen; i++) {
		                        darray[i] = ((Double)at.getNumericValue(i)).doubleValue();
		                    }
		                    db.addEPSAttribute(name, EPSConstants.EPDOUBLE, atlen, (Object)darray);
		                }
			    	}
			    }

			    // create the axes time = 0, depth = 1, lat = 2, lon = 3
			    Axis timeAxis = new Axis();
			    Axis zAxis = new Axis();
			    Axis latAxis = new Axis();
			    Axis lonAxis = new Axis();

			    // make a GoDate from the time value
			    GeoDate gd = new GeoDate((long)mTime);
			    int hour = gd.getHours();
			    int mins = gd.getMinutes();
			    int secs = gd.getSeconds();
			    int month = gd.getMonth();
			    int year = gd.getYear();
			    int day = gd.getDay();

			    // time axis
			    timeAxis.setName("time");
			    timeAxis.setTime(true);
			    timeAxis.setUnlimited(false);
			    timeAxis.setAxisType(EPSConstants.EPTAXIS);
				timeAxis.setLen(1);

				// make the time axis units
				String date = "days since ";

		    	//sprintf(time_string,"%04d-%02d-%02d %02d:%02d:%02d.%03d",yr,mon,day,hr,min,sec,f);
				String frmt = new String("{0,number,####}-{1,number,00}-{2,number,00} {3,number,00}:{4,number,00}:{5,number,00}.{6,number,000}");
				MessageFormat msgf = new MessageFormat(frmt);

				Object[] objs = {new Integer(year), new Integer(month), new Integer(day), new Integer(hour),
								 new Integer(mins), new Integer(secs), new Integer(0)};
				StringBuffer out = new StringBuffer();
				msgf.format(objs, out, null);
				String time_string = new String(out);
				date = date + time_string;
			    timeAxis.addAttribute(0, "units", EPSConstants.EPCHAR, date.length(), date);
			    timeAxis.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
			    double[] ta = {0.0};
			    ArrayMultiArray tma = new ArrayMultiArray(ta);
			    timeAxis.setData(tma);
			    db.setAxis(timeAxis);

			    // add the time axes variable
			    EPSVariable var = new EPSVariable();
			    var.setOname("time");
			    var.setDtype(EPSConstants.EPDOUBLE);
			    var.setVclass(Double.TYPE);
			    var.addAttribute(0, "units", EPSConstants.EPCHAR, date.length(), date);
			    var.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
			    double[] vta = {0.0};
			    ucar.multiarray.MultiArray vtma = new ArrayMultiArray(vta);
			    try {
			    	var.setData(vtma);
			    }
			    catch (Exception ex) {ex.printStackTrace();}
			    db.addEPSVariable(var);

			    // z axis
			    zAxis.setName("depth");
			    zAxis.setTime(false);
			    zAxis.setUnlimited(false);
				zAxis.setLen(mNumZs);
			    zAxis.setAxisType(EPSConstants.EPZAXIS);
			    zAxis.addAttribute(0, "units", EPSConstants.EPCHAR, 2, "db");
			    zAxis.addAttribute(1, "type", EPSConstants.EPCHAR, 6, "UNEVEN");
			    sarray[0] = 1;
			    zAxis.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);

			    double[] za = new double[mNumZs];
			    for (int b=0; b<mNumZs; b++) {
	    			za[b] = (double)mZAxis[b];
	    		}
			    MultiArray zma = new ArrayMultiArray(za);
			    zAxis.setData(zma);
			    db.setAxis(zAxis);

			    // add the z axes variable
			    var = new EPSVariable();
			    var.setOname("depth");
			    var.setDtype(EPSConstants.EPDOUBLE);
			    var.setVclass(Double.TYPE);
			    var.addAttribute(0, "units", EPSConstants.EPCHAR, 4, "dbar");
			    var.addAttribute(1, "type", EPSConstants.EPCHAR, 6, "UNEVEN");
			    sarray[0] = 1;
			    var.addAttribute(2, "epic_code", EPSConstants.EPSHORT, 1, sarray);

			    MultiArray zvma = new ArrayMultiArray(za);
			    try {
			    	var.setData(zvma);
			    }
			    catch (Exception ex) {ex.printStackTrace();}
			    db.addEPSVariable(var);

			    // lat axis
			    latAxis.setName("latitude");
			    latAxis.setTime(false);
			    latAxis.setUnlimited(false);
				latAxis.setLen(1);
			    latAxis.setAxisType(EPSConstants.EPYAXIS);
			    latAxis.addAttribute(0, "units", EPSConstants.EPCHAR, 7, "degrees");
			    latAxis.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
			    sarray[0] = 500;
			    latAxis.addAttribute(2, "epic_code", EPSConstants.EPSHORT, 1, sarray);
			    double[] la = {mLat};
			    MultiArray lma = new ArrayMultiArray(la);
			    latAxis.setData(lma);
			    db.setAxis(latAxis);

			    // add the y axes variable
			    var = new EPSVariable();
			    var.setOname("latitude");
			    var.setDtype(EPSConstants.EPDOUBLE);
			    var.setVclass(Double.TYPE);
			    var.addAttribute(0, "units", EPSConstants.EPCHAR, 7, "degrees");
			    var.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
			    sarray[0] = 500;
			    var.addAttribute(2, "epic_code", EPSConstants.EPSHORT, 1, sarray);
			    MultiArray yvma = new ArrayMultiArray(la);
			    try {
			    	var.setData(yvma);
			    }
			    catch (Exception ex) {ex.printStackTrace();}
			    db.addEPSVariable(var);

			    // lon axis
			    lonAxis.setName("longitude");
			    lonAxis.setTime(false);
			    lonAxis.setUnlimited(false);
				lonAxis.setLen(1);
			    lonAxis.setAxisType(EPSConstants.EPXAXIS);
			    lonAxis.addAttribute(0, "units", EPSConstants.EPCHAR, 7, "degrees");
			    lonAxis.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
			    sarray[0] = 502;
			    lonAxis.addAttribute(2, "epic_code", EPSConstants.EPSHORT, 1, sarray);
			    double[] lla = {mLon};
			    lma = new ArrayMultiArray(lla);
			    lonAxis.setData(lma);
			    db.setAxis(lonAxis);

			    // add the x axes variable
			    var = new EPSVariable();
			    var.setOname("longitude");
			    var.setDtype(EPSConstants.EPDOUBLE);
			    var.setVclass(Double.TYPE);
			    var.addAttribute(0, "units", EPSConstants.EPCHAR, 7, "degrees");
			    var.addAttribute(1, "type", EPSConstants.EPCHAR, 1, " ");
			    sarray[0] = 502;
			    var.addAttribute(2, "epic_code", EPSConstants.EPSHORT, 1, sarray);
			    MultiArray xvma = new ArrayMultiArray(lla);
			    try {
			    	var.setData(xvma);
			    }
			    catch (Exception ex) {ex.printStackTrace();}

			    db.addEPSVariable(var);

    			EPIC_Key_DB mEpicKeyDB = new EPIC_Key_DB("joa_epic.key");
    			EPIC_Key_DB mOrigEpicKeyDB = new EPIC_Key_DB("epic.key");

			    // add the measured variables;
				for (int i=0; i<mNumParams; i++) {
				    double[][][][] va = new double[1][mNumZs][1][1];
				    for (int b=0; b<mNumZs; b++) {
		    			va[0][b][0][0] = mLevelValues[b][i];
		    				//System.out.println("va =" + b + " " + va[0][b][0][0]);
		    		}

		    		// look this variable up in JOA EPIC_Key. find matching entry in original EPIC Key
		    		String oname = mParams[i];
		    		String sname = null;
		    		String lname = null;
		    		String gname = null;
		    		String units = null;
		    		String ffrmt = null;
		    		int keyID = -99;
		    		int type = -99;
		    		try {
			    		keyID = mEpicKeyDB.findKeyIDByCode(mParams[i]);
			    		Key key = mOrigEpicKeyDB.findKey(keyID);
			    		gname = key.getGname();
			    		sname = key.getSname();
			    		lname = key.getLname();
			    		units = key.getUnits();
			    		ffrmt = key.getFrmt();
			    		type = key.getType();
			    	}
			    	catch (Exception e) {
			    		lname = mParams[i];
			    		gname = mParams[i];
			    		sname = mParams[i];
			    		units = mUnits[i];
			    	}

		    		// make a new variable
				    var = new EPSVariable();

				    var.setOname(oname);
				    var.setSname(sname);
				    var.setLname(lname);
				    var.setGname(gname);
				    var.setDtype(EPSConstants.EPDOUBLE);
				    var.setVclass(Double.TYPE);
				    int numAttributes = 0;
				    if (ffrmt != null)
				    	var.addAttribute(numAttributes++, "FORTRAN_format", EPSConstants.EPCHAR, ffrmt.length(), ffrmt);
				    if (units != null && units.length() > 0)
				    	var.addAttribute(numAttributes++, "units", EPSConstants.EPCHAR, units.length(), units);
				    if (keyID >= 0)	{
				    	sarray[0] = (short)type;
				    	//var.addAttribute(numAttributes++, "type", EPSConstants.EPSHORT, 1, sarray);
				    }
				    if (keyID >= 0) {
				    	sarray[0] = (short)keyID;
				    	var.addAttribute(numAttributes++, "epic_code", EPSConstants.EPSHORT, 1, sarray);
				    }

				    // connect variable to axis
				    boolean[] dimUsed = {true, true, true, true};
				    var.setDimorder(0, 0);
				    var.setDimorder(1, 1);
				    var.setDimorder(2, 2);
				    var.setDimorder(3, 3);
				    var.setT(timeAxis);
				    var.setZ(zAxis);
				    var.setY(latAxis);
				    var.setX(lonAxis);

				    // store the data
				    MultiArray mdma = new ArrayMultiArray(va);
				    try {
				    	var.setData(mdma);
				    }
				    catch (Exception ex) {ex.printStackTrace();}

			    	// add the variable to the database
		    		db.addEPSVariable(var);
		    	}

			    // write the output file
			    try {
			    	db.writeNetCDF(new File(dir, fname));
			    }
			    catch (Exception ex) {
			    	ex.printStackTrace();
			    	System.out.println("an error occurred writing a netCDF file");
			    }

			}
		}
	}

	public void setDataTitle(String name){
		if (mDataTitle == null)
			mDataTitle = new String(name);
		else
			mDataTitle += mDataTitle + " " + new String(name);
	}

	public String getDataTitle() {
		return mDataTitle;
	}

	public void setLocation(double x, double y, double z, double t) {
		locToolBar.setLocation(x, y, z, t);
	}

	public void setCtrLocation(double x, double y, double z, double t) {
		locToolBar.setCtrLocation(x, y, z, t);
	}
	
	public void maintainButtons() {
		int np = pointerCollectionGroup.getNumPtrs();
		int nsp = pointerCollectionGroup.getNumSelectedPtrs();
		int nvp = pointerCollectionGroup.getNumVisiblePtrs();
		boolean update = false;
			
		if (np != mNumPtrs) {
			this.mNumPtrs = np;
			update = true;
		}
		if (nsp != mNumSelPtrs) {
			this.mNumSelPtrs = nsp;
			update = true;
		}
		if (nvp != mNumVisPtrs) {
			this.mNumVisPtrs = nvp;
			update = true;
		}
	
		if (update) {
			locToolBar.setInventory(np, nvp, nsp);
			if (mSelectionInspector.isVisible())
				mSelectionInspector.setContents(pointerCollectionGroup);
		}
	}
	
	public boolean isInspectorVisible () {
		return mSelectionInspector.isVisible();
	}
	
	public void toggleInspectorView() {
		if (mSelectionInspector.isVisible())
			mSelectionInspector.reset();
		else
			mSelectionInspector.setContents(pointerCollectionGroup);
		mSelectionInspector.setVisible(!mSelectionInspector.isVisible());
	}
	

		/**
		 * Title:        NameItDialog
		 * Description:  General purpose netCDF file Browser.
		 * Copyright:    Copyright (c) 2000
		 * Company:      NOAA/PMEL/EPIC
		 * @author Donald Denbo and oz
		 * @version 1.0
		 */

		private class NameItDialog extends JDialog {
		  JPanel panel1 = new JPanel();
		  BorderLayout borderLayout1 = new BorderLayout();
		  JPanel buttonPanel = new JPanel();
		  JPanel navigationPanel = new JPanel();
		  JButton acceptButton = new JButton();
		  JButton cancelButton = new JButton();
		  JPanel fileURLPanel = new JPanel();
		  JLabel jLabel2 = new JLabel();
		  JTextField nameField = new JTextField();
		  ImageIcon backImage_;
		  URL currentURL_ = null;
		  String prompt, name, defVal;

		  GridBagLayout gridBagLayout2 = new GridBagLayout();
		  JLabel jLabel1 = new JLabel();
		  GridBagLayout gridBagLayout1 = new GridBagLayout();

		  public NameItDialog(Frame frame, String title, boolean modal, String prmpt, String dv) {
		    super(frame, title, modal);
		    prompt = prmpt;
		    defVal = dv;
		    try {
		      jbInit();
		    }
		    catch(Exception ex) {
		      ex.printStackTrace();
		    }
		  }

		  public NameItDialog(String title, String prmpt, String dv) {
		    this(null, title, true, prmpt, dv);
		  }
		  
		  void jbInit() throws Exception {
			ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
		    panel1.setLayout(borderLayout1);
		    acceptButton.setText(b.getString("kOK"));
		    acceptButton.addActionListener(new NameItDialog_acceptButton_actionAdapter(this));
		    cancelButton.setText(b.getString("kCancel"));
		    cancelButton.addActionListener(new NameItDialog_cancelButton_actionAdapter(this));
		    navigationPanel.setLayout(gridBagLayout1);
		    jLabel2.setText(prompt);
		    nameField.setColumns(20);
		    if (defVal != null || defVal.length() > 0) {
		    	nameField.setText(defVal);
		    }
		    fileURLPanel.setLayout(gridBagLayout2);
		    jLabel1.setFont(new java.awt.Font("Dialog", 0, 14));
		    jLabel1.setText(prompt);
		    getContentPane().add(panel1);
		    panel1.add(buttonPanel, BorderLayout.SOUTH);

			if (Constants.ISMAC) {
		    	buttonPanel.add(cancelButton);
		    	buttonPanel.add(acceptButton);
			}
			else {
		    	buttonPanel.add(acceptButton);
		    	buttonPanel.add(cancelButton);
			}
		    
		    panel1.add(navigationPanel, BorderLayout.CENTER);
		    GridBagConstraints gbc1 = new GridBagConstraints();//(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, 
		    	//GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
		    gbc1.gridx = 0;
		    gbc1.gridy = 1;
		    gbc1.gridwidth = 1;
		    gbc1.gridheight = 1;
		    gbc1.weightx = 1.0;
		    gbc1.weighty = 1.0;
		    gbc1.anchor = GridBagConstraints.CENTER;
		    gbc1.fill = GridBagConstraints.BOTH;
		    gbc1.insets =  new Insets(0, 0, 0, 0);
		    gbc1.ipadx = 0;
		    gbc1.ipady = 0;
		    navigationPanel.add(fileURLPanel, gbc1);
		    
		    GridBagConstraints gbc2 = new GridBagConstraints();//0, 0, 1, 1, 0.0, 0.0
		            //,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 5, 0), 0, 0);
		    gbc2.gridx = 0;
		    gbc2.gridy = 0;
		    gbc2.gridwidth = 1;
		    gbc2.gridheight = 1;
		    gbc2.weightx = 0.0;
		    gbc2.weighty = 0.0;
		    gbc2.anchor = GridBagConstraints.WEST;
		    gbc2.fill = GridBagConstraints.NONE;
		    gbc2.insets =  new Insets(5, 15, 5, 0);
		    gbc2.ipadx = 0;
		    gbc2.ipady = 0;
		    fileURLPanel.add(jLabel2, gbc2);
		    
		    GridBagConstraints gbc3 = new GridBagConstraints();//1, 0, 1, 1, 1.0, 0.0
		            //,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 15), 0, 0));
		    gbc3.gridx = 1;
		    gbc3.gridy = 0;
		    gbc3.gridwidth = 1;
		    gbc3.gridheight = 1;
		    gbc3.weightx = 1.0;
		    gbc3.weighty = 0.0;
		    gbc3.anchor = GridBagConstraints.WEST;
		    gbc3.fill = GridBagConstraints.HORIZONTAL;
		    gbc3.insets =  new Insets(5, 0, 5, 15);
		    gbc3.ipadx = 0;
		    gbc3.ipady = 0;
		    fileURLPanel.add(nameField, gbc3);
		    nameField.selectAll();

		    this.getRootPane().setDefaultButton(acceptButton);
		    	
			// show dialog at center of screen
		    pack();
			Rectangle dBounds = this.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width/2 - dBounds.width/2;
			int y = sd.height/2 - dBounds.height/2;
			this.setLocation(x, y);
		  }

		  public String getName() {
		    return name;
		  }

		  void acceptButton_actionPerformed(ActionEvent e) {
		    name = nameField.getText();
		    setVisible(false);
		  }

		  void cancelButton_actionPerformed(ActionEvent e) {
		    name = null;
		    setVisible(false);
		  }

		private class NameItDialog_acceptButton_actionAdapter implements java.awt.event.ActionListener {
		  NameItDialog adaptee;

		  NameItDialog_acceptButton_actionAdapter(NameItDialog adaptee) {
		    this.adaptee = adaptee;
		  }
		  public void actionPerformed(ActionEvent e) {
		    adaptee.acceptButton_actionPerformed(e);
		  }
		}

		private class NameItDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener {
		  NameItDialog adaptee;

		  NameItDialog_cancelButton_actionAdapter(NameItDialog adaptee) {
		    this.adaptee = adaptee;
		  }
		  public void actionPerformed(ActionEvent e) {
		    adaptee.cancelButton_actionPerformed(e);
		  }
		}
	}
}

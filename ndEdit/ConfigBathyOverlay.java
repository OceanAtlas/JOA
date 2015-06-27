/*
 * $Id: ConfigBathyOverlay.java,v 1.13 2005/06/22 23:30:12 oz Exp $
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.border.*;
import gov.noaa.pmel.eps2.*;
import gov.noaa.pmel.swing.*;

public class ConfigBathyOverlay extends JDialog implements ActionListener, ItemListener {
	protected JButton mOKBtn = null;
	protected JButton mApplyButton = null;
	protected JButton mCancelButton = null;
	protected Swatch[] swatches = new Swatch[120];
	protected JCheckBox[] checks = new JCheckBox[120];
	protected int[] bathyColorRamp = new int[150];
	protected int[] grayBathyColorRamp = new int[150];
	protected int[] rainbowBathyColorRamp = new int[150];
	protected DialogClient mClient = null;
	protected BathyOverlaySpec mBathySpec;
	protected SmallIconButton checkAll = null;
	protected SmallIconButton checkNone = null;
	protected SmallIconButton ramp = null;
	protected SmallIconButton ramp2 = null;
	protected SmallIconButton ramp3 = null;

	protected JPanel custBathyCont = null;
	protected int numIsobaths = 12;
	protected int numCustomIsos = 0;
	protected int numCustomCoasts = 0;
	protected String[] allIsobathValues = null;
	protected String[] allIsobathDescrips = null;
	protected String[] allIsobathUnits = null;
	protected String[] allIsobathPaths = null;
	protected JComboBox customCoastsList = null;
	protected JRadioButton builtinCoast = null;
	protected JRadioButton customCoast = null;
	protected String[] custCoastPaths = null;
	protected String initalCoastPath;
	protected Swatch mCoastColor;
	protected int mCoastWight;
	protected JSpinner mWeightSpinner;
	protected int initialWeight;
	protected Color initialColor;

	public ConfigBathyOverlay(JFrame par, BathyOverlaySpec spec, String custCoastPath, DialogClient client) {
		super(par, "Map Settings", true);
		mBathySpec = spec;
		initalCoastPath = custCoastPath;
		if (mBathySpec == null)
			initBathySpec();
		mClient = client;
		try {
			checkAll = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/checkall.gif")));
			checkNone = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/checknone.gif")));
			ramp = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/colorramp.gif")));
			ramp2 = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/grayramp.gif")));
			ramp3 = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/rainbowramp.gif")));
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("ConfigBathyOverlay:ctor");
		}
		init();
		initialWeight = spec.getCoastWeight();
		initialColor = spec.getCoastColor();
	}

	public void initBathySpec() {
		mBathySpec = new BathyOverlaySpec();
		mBathySpec.mNumIsobaths = 0;
	}

	public boolean isCoastWeightChanged() {
		return initialWeight != ((SpinnerNumberModel) (mWeightSpinner.getModel())).getNumber().intValue();
	}

	public boolean isCoastColorChanged() {
		Color c = mCoastColor.getColor();
		return initialColor.getRGB() != mCoastColor.getColor().getRGB();
	}

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
		
		// build the color ramps
		float hue = 120, sat=1, light=1, startSat=120, satAngleDelta=1;
		for (int i=0; i<150; i++) {
			hue = (startSat + ((float)i * satAngleDelta))/360.f;
			bathyColorRamp[i] = Color.HSBtoRGB(hue, sat, light);
		}
		
		hue = 0;
		sat = 0;
		light = 1;
		float startLight = 288.f;
		float lightAngleDelta = 1;
		for (int i=0; i<150; i++) {
			light = (startLight - ((float)i * lightAngleDelta))/360.f;
			grayBathyColorRamp[i] = Color.HSBtoRGB(hue, sat, light);
		}
		
		hue = 0;
		sat = 1;
		light = 1;
		float startHue = 0;
		float hueAngleDelta = 1.8f;
		for (int i=0; i<150; i++) {
			hue = (startHue + ((float)i * hueAngleDelta))/360.f;
			rainbowBathyColorRamp[i] = Color.HSBtoRGB(hue, sat, light);
		}
		
		// the main panel
    	Container contents = this.getContentPane();
    	this.getContentPane().setLayout(new BorderLayout(5, 5));
    	JPanel mainPanelholder = new JPanel();
    	mainPanelholder.setLayout(new BorderLayout(5, 5));
    	
    	JPanel bathyCont = new JPanel();
        bathyCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
        
        JPanel bathyContCont = new JPanel();
        bathyContCont.setLayout(new BorderLayout(5, 5));
    	TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kBathymetry"));
    	bathyContCont.setBorder(tb);
    	
        JPanel coastlineContCont = new JPanel();
        coastlineContCont.setLayout(new BorderLayout(5, 5));
    	tb = BorderFactory.createTitledBorder(b.getString("kCoastline"));
    	coastlineContCont.setBorder(tb);
        
        // arrays for the built-in bathymetry
        String defValues[] = {" 500", "1000", "1500", "2000", "2500", "3000", "3500", "4000", "4500", "5000", "5500", "6000"};
        String defDescriptions[] = {"Built in", "Built in", "Built in", "Built in", "Built in", "Built in", "Built in", "Built in", "Built in", "Built in", "Built in", "Built in"};
        String defUnits[] = {"m", "m", "m", "m", "m", "m", "m", "m", "m", "m", "m", "m"};
        String defPaths[] = {"", "", "", "", "", "", "", "", "", "", "", ""};

        // look for custom isobaths in the "custom isobaths" folder
	    String[] custValues = null;
	    String[] custDescrips = null;
	    String[] custUnits = null;
	    String[] custPaths = null;
        File customIsobathDir = null;
        File customCoastDir = null;
	    String[] custCoastDescrips = null;
	    
    	FilenameFilter filter = new FilenameFilter() {
    		public boolean accept(File dir, String name) {
    			if (name.startsWith("."))
    				return false;
    			else
    				return true;
    		}
    	};
	    
		if (initalCoastPath == null) {
			try {
				String directory = NdEditFormulas.getCustomCoastlinePath();
				customCoastDir = new File(directory);
			}
			catch (Exception ex) {
			}
		}
		else
			customCoastDir = new File(initalCoastPath);
						
		// look for custom bathy
		try {
			String directory = NdEditFormulas.getCustomBathymetryPath();
			customIsobathDir = new File(directory);
		}
		catch (Exception exx) {
		}
		
		String[] customIsos = null;
		if (customIsobathDir != null && customIsobathDir.isDirectory())
			customIsos = customIsobathDir.list(filter);
			
		if (customIsos != null && customIsos.length > 0) {
	    	numCustomIsos = customIsos.length/2;
	    	custValues = new String[numCustomIsos];
	    	custDescrips = new String[numCustomIsos];
	    	custUnits = new String[numCustomIsos];
	    	custPaths = new String[numCustomIsos];
	    	
	    	// sort the list
	    	ArrayList al = new ArrayList();
			for (int i=0; i<customIsos.length; i++) {
				al.add(customIsos[i]);
			}
	    	Collections.sort(al);
	    	
	    	int cnt = 0;
			for (int i=0; i<customIsos.length; i++) {
				String nme = ((String)al.get(i)).toLowerCase();
				String inLine = null;
				
				if (nme.indexOf(".txt") > 0) {
					// open the file and get the description
					try {
						File mFile = new File(customIsobathDir + File.separator + nme);
		
						FileReader fr = new FileReader(mFile);
					    LineNumberReader in = new LineNumberReader(fr, 10000);
					    inLine = in.readLine();
					    in.close();
					}
					catch (Exception ex) {}
					// isolate the values
					StringTokenizer st = new StringTokenizer(inLine, ":");
					custValues[cnt] = (String)st.nextElement();
					custUnits[cnt] = (String)st.nextElement();
					custDescrips[cnt] = (String)st.nextElement();
				}
				else
					custPaths[cnt] = customIsobathDir + File.separator + nme;
					
				if (custDescrips[cnt] != null && custValues[cnt] != null)
					cnt++;
			}
		
			// merge the default values and custom values into final array
			allIsobathValues = new String[defValues.length + numCustomIsos];
        	allIsobathDescrips = new String[defValues.length + numCustomIsos];
        	allIsobathUnits = new String[defValues.length + numCustomIsos];
        	allIsobathPaths = new String[defValues.length + numCustomIsos];
        	for (int i=0; i<defValues.length; i++) {
        		allIsobathValues[i] = defValues[i];
        		allIsobathDescrips[i] = defDescriptions[i];
        		allIsobathUnits[i] = defUnits[i];
        		allIsobathPaths[i] = defPaths[i];
        	}
        	for (int j=0; j<numCustomIsos; j++) {
        		allIsobathValues[defValues.length + j] = custValues[j];
        		allIsobathDescrips[defValues.length + j] = custDescrips[j];
        		allIsobathUnits[defValues.length + j] = custUnits[j];
        		allIsobathPaths[defValues.length + j] = custPaths[j];
        	}
		}
		else {
			// just copy the default values into final array
			allIsobathValues = new String[defValues.length];
        	allIsobathDescrips = new String[defValues.length];
        	allIsobathUnits = new String[defValues.length];
        	allIsobathPaths = new String[defValues.length];
        	for (int i=0; i<defValues.length; i++) {
        		allIsobathValues[i] = defValues[i];
        		allIsobathDescrips[i] = defDescriptions[i];
        		allIsobathUnits[i] = defUnits[i];
        		allIsobathPaths[i] = defPaths[i];
        	}
		}
			
		// sort by the value of the isobath:
		for (int i=0; i<allIsobathValues.length-1; i++) {
			for (int j=i+1; j<allIsobathValues.length; j++) {
				double val1 = Float.valueOf(allIsobathValues[i]).floatValue();
				double val2 = Float.valueOf(allIsobathValues[j]).floatValue();
				if (val2 < val1) {
					// swap everything
					String t1 = allIsobathValues[i];
					allIsobathValues[i] = allIsobathValues[j];
					allIsobathValues[j] = t1;
					String t2 = allIsobathDescrips[i];
					allIsobathDescrips[i] = allIsobathDescrips[j];
					allIsobathDescrips[j] = t2;
					String t3 = allIsobathUnits[i];
					allIsobathUnits[i] = allIsobathUnits[j];
					allIsobathUnits[j] = t3;
					String t4 = allIsobathPaths[i];
					allIsobathPaths[i] = allIsobathPaths[j];
					allIsobathPaths[j] = t4;
				}
			}
		}

        // the bathy detail panel
        for (int i=0; i<allIsobathValues.length; i++) {
	    	JPanel detailCont = new JPanel();
	    	detailCont.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
	    	checks[i] = new JCheckBox("");
	    	checks[i].addItemListener(this);
	    	swatches[i] = new Swatch(Color.black, 12, 12);
	    	JLabel label = new JLabel(allIsobathValues[i] + allIsobathUnits[i] + ":" + allIsobathDescrips[i]);
			label.setFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));
	        detailCont.add(checks[i]);
	        detailCont.add(swatches[i]);
	        detailCont.add(label);
	        bathyCont.add(detailCont);
        }
        
        // set the initial values of the widgets from the mBathySpec
        if (mBathySpec != null) {
        	for (int i=0; i<mBathySpec.getNumIsobaths(); i++) {
        		// find a matching detail line
        		for (int j=0; j<allIsobathValues.length; j++) {
        			boolean valMatch =  (Double.valueOf(allIsobathValues[j]).doubleValue()) == mBathySpec.getValue(i);
        			boolean descripMatch = allIsobathDescrips[j].equals(mBathySpec.getDescrip(i));
        			if (valMatch && descripMatch) {
        				checks[j].setSelected(true);
        				swatches[j].setColor(mBathySpec.getColor(i));
        			}
        		}
        	}
        }
        
    	JScrollPane listScroller = new JScrollPane(bathyCont);
	    bathyContCont.add("Center", listScroller);
        
        // convenience buttons
    	JPanel bathyBtnCont = new JPanel();
	    bathyBtnCont.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
	    try {
	      checkAll = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/checkall.gif")));
      }
      catch (ClassNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      	System.out.println("ConfigBathyOverlay:init checkAll");
      }
	   	bathyBtnCont.add(checkAll);
	    try {
	      checkNone = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/checknone.gif")));
      }
      catch (ClassNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      	System.out.println("ConfigBathyOverlay:init checkNone");
      }
	   	bathyBtnCont.add(checkNone);
	    try {
	      ramp = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/colorramp.gif")));
      }
      catch (ClassNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      	System.out.println("ConfigBathyOverlay:init ramp");
      }
	   	bathyBtnCont.add(ramp);
	    try {
	      ramp2 = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/grayramp.gif")));
      }
      catch (ClassNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      	System.out.println("ConfigBathyOverlay:init ramp2");
      }
	   	bathyBtnCont.add(ramp2);
	    try {
	      ramp3 = new SmallIconButton(new ImageIcon(Class.forName("ndEdit.NdEdit").getResource("gifs/rainbowramp.gif")));
      }
      catch (ClassNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      	System.out.println("ConfigBathyOverlay:init ramp3");
      }
	    bathyBtnCont.add(ramp3);
	    bathyContCont.add("South", bathyBtnCont);
	    checkAll.addActionListener(this);
	    checkNone.addActionListener(this);
	    ramp.addActionListener(this);
	    ramp2.addActionListener(this);
	    ramp3.addActionListener(this);
		checkAll.setActionCommand("all");
		checkNone.setActionCommand("none");
		ramp.setActionCommand("ramp");
		ramp2.setActionCommand("ramp2");
		ramp3.setActionCommand("ramp3");
	    checkAll.setToolTipText("Select all isobaths");
	    checkNone.setToolTipText("Deselect selected isobaths");
	    ramp.setToolTipText("Auto assign green-purple color ramp to selected isobaths");
	    ramp2.setToolTipText("Auto assign gray scale color ramp to selected isobaths");
	    ramp3.setToolTipText("Auto assign rainbow color ramp to selected isobaths");
				
		// custom coastlines
		String[] customCoasts = null;
		if (customCoastDir != null && customCoastDir.isDirectory())
			customCoasts = customCoastDir.list(filter);
		
		if (customCoasts != null && customCoasts.length > 0 && customCoastDir.isDirectory()) {	
	    	custCoastDescrips = new String[customCoasts.length/2];
	    	custCoastPaths = new String[customCoasts.length/2];
	    	numCustomCoasts = customCoasts.length/2;

	    	// sort the list
	    	ArrayList al = new ArrayList();
			for (int i=0; i<customCoasts.length; i++) {
				al.add(customCoasts[i]);
			}
	    	Collections.sort(al);
	    	
	    	int cnt = 0;
			for (int i=0; i<customCoasts.length; i++) {
				String nme = ((String)al.get(i)).toLowerCase();
				String inLine = null;
				if (nme.indexOf(".txt") > 0) {
					// open the file and get the description
					try {
						File mFile = new File(customCoastDir + File.separator + nme);
						FileReader fr = new FileReader(mFile);
					    LineNumberReader in = new LineNumberReader(fr, 10000);
					    inLine = in.readLine();
					    in.close();
					}
					catch (Exception ex) {}
					// isolate the values
					StringTokenizer st = new StringTokenizer(inLine, ":");
					custCoastDescrips[cnt] = (String)st.nextElement();
				}
				else
					custCoastPaths[cnt] = customCoastDir + File.separator + nme;
					
				if (custCoastDescrips[cnt] != null)
					cnt++;
			}
		}
		else if (customCoastDir != null && customCoastDir.isFile()) {
			// custom coast was set in pervious invocation of dialog
			customCoasts = customCoastDir.getParentFile().list(filter);
	    	custCoastDescrips = new String[customCoasts.length/2];
	    	custCoastPaths = new String[customCoasts.length/2];
	    	numCustomCoasts = customCoasts.length/2;

	    	// sort the list
	    	ArrayList al = new ArrayList();
			for (int i=0; i<customCoasts.length; i++) {
				al.add(customCoasts[i]);
			}
	    	Collections.sort(al);
	    	
	    	int cnt = 0;
			for (int i=0; i<customCoasts.length; i++) {
				String nme = ((String)al.get(i)).toLowerCase();
				String inLine = null;
				if (nme.indexOf(".txt") > 0) {
					// open the file and get the description
					try {
						File mFile = new File(customCoastDir.getParentFile() + File.separator + nme);
						FileReader fr = new FileReader(mFile);
					    LineNumberReader in = new LineNumberReader(fr, 10000);
					    inLine = in.readLine();
					    in.close();
					}
					catch (Exception ex) {}
					// isolate the values
					StringTokenizer st = new StringTokenizer(inLine, ":");
					custCoastDescrips[cnt] = (String)st.nextElement();
				}
				else
					custCoastPaths[cnt] = customCoastDir.getParentFile() + File.separator + nme;
					
				if (custCoastDescrips[cnt] != null)
					cnt++;
			}
		
		}
		
		// coastline panel
		JPanel coastPanel = new JPanel();
		coastPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
		builtinCoast = new JRadioButton(b.getString("kBuiltin"));
		customCoast = new JRadioButton(b.getString("kCustom"));
		
		if (customCoastDir != null && customCoastDir.isDirectory()) {
			builtinCoast.setSelected(true);
		}
		else if (customCoastDir == null) {
			builtinCoast.setSelected(true);
			builtinCoast.setEnabled(false);
		}
		
		if (customCoastDir != null && customCoastDir.isFile()) {
			customCoast.setSelected(true);
		}
		else if (customCoastDir == null) {
			customCoast.setEnabled(false);
		}
		
		builtinCoast.setActionCommand("builtin");
		customCoast.setActionCommand("custom");
        builtinCoast.addActionListener(this);
        customCoast.addActionListener(this);
    	ButtonGroup bg1 = new ButtonGroup();
    	bg1.add(builtinCoast);
    	bg1.add(customCoast);
		coastPanel.add(builtinCoast);
		coastPanel.add(customCoast);
	    builtinCoast.setToolTipText("Click to use the built in coastline (medium resolution)");
	    customCoast.setToolTipText("Click to select a custom coastline from popup menu");
	    
		if (numCustomCoasts == 0) {
			customCoast.setEnabled(false);
		}
		else {
			// add a popup menu for the custom coastlines
	    	Vector coasts = new Vector();
	    	for (int i=0; i<numCustomCoasts; i++) {
				coasts.addElement(custCoastDescrips[i]);
			}
    		customCoastsList = new JComboBox(coasts);
			coastPanel.add(customCoastsList);
			
			customCoastsList.setEnabled(false);
			if (customCoastDir.isFile()) {
		    	for (int i=0; i<numCustomCoasts; i++) {
					if (custCoastDescrips[i].equals(initalCoastPath)) {
						customCoastsList.setSelectedIndex(i);
						break;
					}
				}
				customCoastsList.setEnabled(true);
			}
		}
		coastlineContCont.add("North", coastPanel);
		mCoastColor = new Swatch(mBathySpec.mCoastColor);
		JPanel colPanel = new JPanel();
		colPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		colPanel.add(new JLabel(b.getString("kCoastColor")));
		colPanel.add(mCoastColor);
		
		Integer value = new Integer(mBathySpec.getCoastWeight()); 
		Integer min = new Integer(1);
		Integer max = new Integer(3); 
		Integer step = new Integer(1); 
		SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step); 
		mWeightSpinner = new JSpinner(model);

		colPanel.add(new JLabel("  " + b.getString("kCoastWeight")));
		colPanel.add(mWeightSpinner);
		
		coastlineContCont.add("South", colPanel);

		// lower panel
    	mOKBtn = new JButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
    	mCancelButton = new JButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
        dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
        dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
    	if (Constants.ISMAC) {
	    	dlgBtnsPanel.add(mCancelButton);
	    	dlgBtnsPanel.add(mOKBtn);
		}
		else {
	    	dlgBtnsPanel.add(mOKBtn);
	    	dlgBtnsPanel.add(mCancelButton);
		}    
        dlgBtnsInset.add(dlgBtnsPanel);
        
        mOKBtn.addActionListener(this);
        this.getRootPane().setDefaultButton(mOKBtn);
        mCancelButton.addActionListener(this);
        mainPanelholder.add(new NPixelBorder(bathyContCont, 0, 0, 0, 0), "Center");
        mainPanelholder.add(new NPixelBorder(coastlineContCont, 0, 0, 0, 0), "South");
        contents.add(new NPixelBorder(mainPanelholder, 10, 10, 10, 10), "Center");
        contents.add(new NPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
        this.pack();
		
		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
		this.setLocation(x, y);
    }

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel") && mClient != null) {
			// mClient.dialogCancelled(this);
			this.dispose();
		}
		else if (cmd.equals("builtin") && customCoastsList != null) {
			customCoastsList.setEnabled(false);
		}
		else if (cmd.equals("custom") && customCoastsList != null) {
			customCoastsList.setEnabled(true);
		}
		else if (cmd.equals("ok") && mClient != null) {
			this.setVisible(false);
			mClient.dialogDismissed(this);
			this.dispose();
		}
		else if (cmd.equals("cancel")) {
			this.dispose();
		}
		else if (cmd.equals("apply") && mClient != null) {
			mClient.dialogApply(this);
		}
		else if (cmd.equals("ok")) {
			// get the state of the swatches
			// for (int i=0; i<numIsobaths; i++) {
			// if (mBathySpec.mUseIsobath[i]) {
			// mBathySpec.mIsobathColors[i] = swatches[i].getColor();
			// }
			// }
			this.dispose();
		}
		else if (cmd.equals("all")) {
			checkAll.setSelected(false);
			for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
				checks[i].setSelected(true);
				swatches[i].setColor(Color.black);
			}

		}
		else if (cmd.equals("none")) {
			checkNone.setSelected(false);
			for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
				checks[i].setSelected(false);
				swatches[i].setColor(Color.black);
			}

		}
		else if (cmd.equals("ramp")) {
			ramp.setSelected(false);
			int numChecked = 0;
			for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
				if (checks[i].isSelected())
					numChecked++;
			}
			int step;
			if (numChecked == 1)
				step = 150;
			else
				step = 150 / (numChecked - 1);
			int nc = 0;
			for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
				if (checks[i].isSelected()) {
					int index = (nc * step) < 150 ? nc * step : 150 - 1;
					nc++;
					swatches[i].setColor(new Color(bathyColorRamp[index]));
				}
				else
					swatches[i].setColor(Color.black);
			}
		}
		else if (cmd.equals("ramp2")) {
			ramp2.setSelected(false);
			int numChecked = 0;
			for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
				if (checks[i].isSelected())
					numChecked++;
			}
			int step;
			if (numChecked == 1)
				step = 150;
			else
				step = 150 / (numChecked - 1);
			int nc = 0;
			for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
				if (checks[i].isSelected()) {
					int index = (nc * step) < 150 ? nc * step : 150 - 1;
					nc++;
					swatches[i].setColor(new Color(grayBathyColorRamp[index]));
				}
				else
					swatches[i].setColor(Color.black);
			}
		}
		else if (cmd.equals("ramp3")) {
			ramp3.setSelected(false);
			int numChecked = 0;
			for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
				if (checks[i].isSelected())
					numChecked++;
			}
			int step;
			if (numChecked == 1)
				step = 150;
			else
				step = 150 / (numChecked - 1);
			int nc = 0;
			for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
				if (checks[i].isSelected()) {
					int index = (nc * step) < 150 ? nc * step : 150 - 1;
					nc++;
					swatches[i].setColor(new Color(rainbowBathyColorRamp[index]));
				}
				else
					swatches[i].setColor(Color.black);
			}
		}
	}

	public boolean isBathyNeeded() {
		for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
			if (checks[i].isSelected())
				return true;
		}
		return false;
	}

	public boolean isCustomCoastNeeded() {
		if (customCoast.isSelected())
			return true;
		return false;
	}

	public String getCustomCoastPath() {
		return custCoastPaths[customCoastsList.getSelectedIndex()];
	}

	public BathyOverlaySpec getBathySpec() {
		// get the state of the swatches
		int cnt = 0;
		for (int i = 0; i < numIsobaths + numCustomIsos; i++) {
			if (checks[i].isSelected()) {
				mBathySpec.mIsobathValues[cnt] = Float.valueOf(allIsobathValues[i]).floatValue();
				mBathySpec.mIsobathColors[cnt] = swatches[i].getColor();
				mBathySpec.mIsobathDescrips[cnt] = allIsobathDescrips[i];
				mBathySpec.mIsobathPaths[cnt] = allIsobathPaths[i];
				mBathySpec.mNumIsobaths = ++cnt;
			}
		}
		mBathySpec.mCoastColor = mCoastColor.getColor();
		mBathySpec.mCoastWeight = ((SpinnerNumberModel) (mWeightSpinner.getModel())).getNumber().intValue();
		return mBathySpec;
	}

	public void itemStateChanged(ItemEvent evt) {
	}
}

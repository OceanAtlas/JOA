/*
 * $Id: AboutDialog.java,v 1.10 2005/09/07 18:49:29 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import gov.noaa.pmel.swing.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class AboutDialog extends JOAJDialog implements ActionListener {
	JOAJLabel label1;
	JOAJLabel label2;
	JOAJLabel label3;
	JOAJLabel label4;
	JOAJLabel label5;
	JOAJLabel label6;
	JOAJLabel label7;
	JOAJLabel label8;
	JOAJLabel label9;
	JOAJLabel label10;
	JOAJButton mOKBtn;
	JOAJButton mAboutNdEditBtn;
	JOAJButton mSysPropsBtn;
	ImageIcon icon1;
	ImageIcon icon2;
	ImageIcon icon3;

	public AboutDialog(JFrame parent, boolean modal) {
		super(parent, "", modal);
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		setLayout(new BorderLayout(5, 5));

		try {
			icon1 = new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource("images/Nsf4.gif"));
			icon2 = new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource("images/noaalogobig.gif"));
			icon3 = new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource("images/sio_color.gif"));
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("AboutDialog:ctor");
		}

		// top panel
		String topPanelLabel = "Java OceanAtlas " + VersionInfo.getVersion() + " " + 
		VersionInfo.getVersionShortDate() + " (" + VersionInfo.getRevision() + ")";
		
		label1 = new JOAJLabel(topPanelLabel);
		label1.setFont(new Font("sansserif", Font.ITALIC + Font.BOLD, 24));
		this.add(label1, "North");

		// middle panel
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new javaoceanatlas.utility.ColumnLayout(javaoceanatlas.utility.Orientation.CENTER,
		    javaoceanatlas.utility.Orientation.CENTER, 2));
		label2 = new JOAJLabel("by");
		label2.setFont(new Font("sansserif", Font.ITALIC, 12));

		label3 = new JOAJLabel("John \"oz\" Osborne");
		label3.setFont(new Font("sansserif", Font.PLAIN, 18));

		label4 = new JOAJLabel("OceanAtlas Software and NOAA/PMEL");
		label4.setFont(new Font("sansserif", Font.PLAIN, 18));

		label4 = new JOAJLabel("tooz@oceanatlas.com");
		label4.setFont(new Font("sansserif", Font.PLAIN, 18));

		label5 = new JOAJLabel("");

		label6 = new JOAJLabel("Jim Swift");
		label6.setFont(new Font("sansserif", Font.PLAIN, 18));

		label7 = new JOAJLabel("SIO/ODF");
		label7.setFont(new Font("sansserif", Font.PLAIN, 18));

		label8 = new JOAJLabel("jswift@ucsd.edu");
		label8.setFont(new Font("sansserif", Font.PLAIN, 18));

		label9 = new JOAJLabel("Contouring and ZGrid from SGT by Donald Denbo NOAA/PMEL www.epic.noaa.gov/java/sgt/");
		label9.setFont(new Font("sansserif", Font.PLAIN, 12));

		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		String javaClassVersion = System.getProperty("java.class.version");
		String mrjVersion = System.getProperty("mrj.version");
		if (mrjVersion == null || mrjVersion.length() == 0) {
			mrjVersion = "";
		}
		else {
			mrjVersion = "MRJ Version = " + mrjVersion;
		}

		label10 = new JOAJLabel(System.getProperty("os.name") + " " + javaVendor + ":" + javaVersion + "/"
		    + javaClassVersion + " " + mrjVersion);
		label10.setFont(new Font("sansserif", Font.PLAIN, 10));

		JPanel logoPanel = new JPanel();
		logoPanel.setLayout(new GridLayout(1, 3, 0, 0));
		logoPanel.add(new JLabel(icon1));
		logoPanel.add(new JLabel(icon2));
		logoPanel.add(new JLabel(icon3));

		middlePanel.add(label1);
		middlePanel.add(label2);
		middlePanel.add(label3);
		middlePanel.add(label4);
		middlePanel.add(label5);
		middlePanel.add(label6);
		middlePanel.add(label7);
		middlePanel.add(label8);
		middlePanel.add(label9);
		middlePanel.add(label10);
		middlePanel.add(logoPanel);
		this.add(new javaoceanatlas.utility.TenPixelBorder(middlePanel, 5, 5, 5, 5), "Center");

		mAboutNdEditBtn = new JOAJButton(b.getString("kAboutNdEdit"));
		mAboutNdEditBtn.setActionCommand("aboutndedit");
    mAboutNdEditBtn.setFocusable(false);

		mSysPropsBtn = new JOAJButton(b.getString("kSystemProperties"));
		mSysPropsBtn.setActionCommand("sysprop");
		mSysPropsBtn.setFocusable(false);

		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mAboutNdEditBtn.setFocusable(false);
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mAboutNdEditBtn);
			dlgBtnsPanel.add(mSysPropsBtn);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mAboutNdEditBtn);
			dlgBtnsPanel.add(mSysPropsBtn);
		}
		dlgBtnsInset.add(dlgBtnsPanel);
		this.add("South", new javaoceanatlas.utility.TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5));

		mOKBtn.addActionListener(this);
		mAboutNdEditBtn.addActionListener(this);
		mSysPropsBtn.addActionListener(this);
		setTitle("About Java OceanAtlas");

		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				e.getWindow().dispose();
			}
		};
		this.addWindowListener(windowListener);
	}

	public AboutDialog(JFrame parent, String title, boolean modal) {
		this(parent, modal);
		setTitle(title);
	}

	public void actionPerformed(ActionEvent event) {
		Object object = event.getSource();
		if (object == mOKBtn) {
			dispose();
		}
		else if (object == mAboutNdEditBtn) {
			ndEdit.AboutDialog ff = new ndEdit.AboutDialog(new JFrame(), true);
			// ff.init();
			ff.pack();

			// show dialog at center of screen
			Rectangle dBounds = ff.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			ff.setLocation(x, y);
			ff.setVisible(true);
		}
		else if (object == mSysPropsBtn) {
			try {
				JSystemPropertiesDialog propDisplay = new JSystemPropertiesDialog();
				propDisplay.setModal(true);

				// show dialog at center of screen
				Rectangle dBounds = propDisplay.getBounds();
				Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				int x = sd.width / 2 - dBounds.width / 2;
				int y = sd.height / 2 - dBounds.height / 2;
				propDisplay.setLocation(x, y);
				propDisplay.setVisible(true);
			}
			catch (Exception e) {
			}
		}
	}

	public static class XMLUtilities {
		private static XMLUtilities mInstance = null;
		public static int MAX_INDEX_HITS = 100;
		// Meta
		// Params
		public static String XBMessage_Version = "Version"; /* Protocol version */
		public static String XBMessage_ID = "MessageID";; /*
																											 * Session-unique message
																											 * ID for the local
																											 * service manager
																											 */
		public static String XBMessage_RequestID = "RequestID";; /*
																															 * Copy of request
																															 * message ID used
																															 * in reply to a
																															 * request
																															 */
		public static String XBMessage_RequestObserver = "RequestObserver"; /*
																																				 * The
																																				 * service
																																				 * making
																																				 * the
																																				 * request
																																				 */
		public static String XBMessage_RequestTarget = "RequestTarget"; /*
																																		 * The
																																		 * service
																																		 * intended
																																		 * to answer
																																		 * the
																																		 * request
																																		 */
		public static String XBMessage_MessageTarget = "MessageTarget"; /*
																																		 * The
																																		 * service
																																		 * intended
																																		 * to
																																		 * receive
																																		 * the
																																		 * message
																																		 */
		public static String XBMessage_MessageSource = "MessageSource"; /*
																																		 * The
																																		 * service
																																		 * sending
																																		 * the
																																		 * message
																																		 */
		public static String XBMessage_Type = "MessageType"; /* The "method call" */

		// Generic Services
		// In
		public static String XBMessage_Type_Heartbeat = "Heartbeat"; /*
																																	 * MessageType:
																																	 * Basic
																																	 * connection
																																	 * test
																																	 */
		public static String XBMessage_Type_WillBeTerminated = "WillBeTerminated"; /*
																																								 * MessageType:
																																								 * The
																																								 * service
																																								 * is
																																								 * about
																																								 * to
																																								 * be
																																								 * stopped
																																								 */
		public static String XBMessage_Type_EnsureServiceReady = "EnsureServiceReady"; /*
																																										 * MessageType:
																																										 * Request
																																										 * "boot"
																																										 * confirmation
																																										 * from
																																										 * service
																																										 */
		public static String XBMessage_Type_Notification = "Notification"; /*
																																				 * MessageType:
																																				 * Generic
																																				 * notification
																																				 */
		public static String XBMessage_Type_ErrorNotification = "ErrorNotification"; /*
																																									 * MessageType:
																																									 * Generic
																																									 * error
																																									 * notification
																																									 */
		// Out
		public static String XBMessage_Type_ServiceReady = "ServiceReady"; /*
																																				 * MessageType:
																																				 * Service
																																				 * has
																																				 * "booted"
																																				 */
		public static String XBMessage_Type_ServiceNotHealthy = "ServiceNotHealthy"; /*
																																									 * MessageType:
																																									 * Service
																																									 * failed
																																									 * to
																																									 * "boot"
																																									 */
		public static String XBMessage_Type_Progress = "Progress"; /*
																																 * MessageType:
																																 * Generic
																																 * percentual
																																 * progress
																																 * indication
																																 */
		public static String XBMessage_Type_ServiceReadyToQuit = "ServiceReadyToQuit"; /*
																																										 * MessageType:
																																										 * Termination
																																										 * accepted
																																										 */
		// Keys
		public static String XBMessage_Key_EID = "EID"; /* 128-bit entity ID */
		public static String XBMessage_Key_UUID = "UUID"; /* Capture identifier */
		public static String XBMessage_Key_ProgressTotal = "ProgressTotal"; /*
																																				 * Total
																																				 * items
																																				 * to do
																																				 * as a
																																				 * double
																																				 */
		public static String XBMessage_Key_ProgressDone = "ProgressDone"; /*
																																			 * Total
																																			 * items
																																			 * done as
																																			 * a
																																			 * double
																																			 */
		public static String XBMessage_Key_CaptureText = "CaptureText"; /*
																																		 * Text
																																		 * aspect of
																																		 * a capture
																																		 */
		public static String XBMessage_Key_Notification = "Notification"; /*
																																			 * Generic
																																			 * notification
																																			 */
		public static String XBMessage_Key_ErrorNotification = "ErrorNotification"; /*
																																								 * Generic
																																								 * error
																																								 * notification
																																								 */
		public static String XBMessage_Key_ElementState = "ElementState"; /*
																																			 * State:
																																			 * e.g.
																																			 * Good,
																																			 * Deleted
																																			 */
		// Values
		public static String XBMessage_Value_ElementStateGood = "Good"; /*  */
		public static String XBMessage_Value_ElementStateDeleted = "Deleted"; /*  */
		public static String XBMessage_Value_MessageWithoutType = "MessageWithoutType"; /*
																																										 * A
																																										 * message
																																										 * was
																																										 * received
																																										 * with
																																										 * no
																																										 * type
																																										 */

		// Capture
		// Keys
		public static String XBMessage_Key_Capture = "Capture"; /*
																														 * Capture
																														 * dictionary
																														 */
		// Capture Keys
		public static String XBMessage_Key_TagList = "TagList"; /*
																														 * A dictionary of
																														 * strings to counts
																														 * of captures using
																														 * them
																														 */
		public static String XBMessage_Key_CreationTime = "CreationTime"; /*  */
		public static String XBMessage_Key_ModificationTime = "ModificationTime"; /*  */
		public static String XBMessage_Key_DocumentHitList = "DocumentHitList"; /*
																																						 * Array
																																						 * of
																																						 * DocumentHit
																																						 * sub-dictionaries
																																						 */
		public static String XBMessage_Key_DocumentHit = "DocumentHit"; /*
																																		 * Sub-dictionary
																																		 * with a
																																		 * search
																																		 * result
																																		 * location.
																																		 * Can
																																		 * appear at
																																		 * top
																																		 * level, or
																																		 * in hit
																																		 * list.
																																		 */
		// </>
		// Document Hit Keys
		public static String XBMessage_Key_CaptureContext = "CaptureContext"; /*
																																					 * Text
																																					 * surrounding
																																					 * a
																																					 * located
																																					 * capture
																																					 */
		public static String XBMessage_Key_DocumentURI = "DocumentURI"; /*
																																		 * URI of
																																		 * text
																																		 * document.
																																		 * Only for
																																		 * read-only
																																		 * use.
																																		 * Could be
																																		 * in local
																																		 * index, or
																																		 * external.
																																		 */
		public static String XBMessage_Key_DocumentID = "DocumentID"; /*
																																	 * ID of text
																																	 * document
																																	 */
		public static String XBMessage_Key_CaptureOffsetInDocument = "CaptureOffsetInDocument"; /*
																																														 * Character
																																														 * offset
																																														 * to
																																														 * the
																																														 * capture
																																														 * text
																																														 * in
																																														 * the
																																														 * text-only
																																														 * document
																																														 */
		public static String XBMessage_Key_CaptureOffsetInContext = "CaptureOffsetInContext"; /*
																																													 * Character
																																													 * offset
																																													 * to
																																													 * the
																																													 * capture
																																													 * text
																																													 * in
																																													 * the
																																													 * CaptureContext
																																													 */
		public static String XBMessage_Key_CaptureLengthInContext = "CaptureLengthInContext"; /*
																																													 * Character
																																													 * count
																																													 * of
																																													 * the
																																													 * capture
																																													 * text
																																													 * in
																																													 * the
																																													 * CaptureContext
																																													 */
		// </>
		public static String XBMessage_Type_LookUpCapture = "LookUpCapture"; /*
																																					 * MessageType:
																																					 * Find
																																					 * the
																																					 * source
																																					 * location
																																					 * of a
																																					 * capture
																																					 * (see
																																					 * CaptureText)
																																					 */
		// Out
		public static String XBMessage_Type_CaptureHitsFound = "CaptureHitsFound"; /*
																																								 * MessageType:
																																								 * Source
																																								 * locations
																																								 * have
																																								 * or
																																								 * have
																																								 * not
																																								 * been
																																								 * found
																																								 */
		// Keys
		public static String XBMessage_Key_CaptureContextWordsBefore = "CaptureContextWordsBefore"; /*
																																																 * Words
																																																 * to
																																																 * get
																																																 * before
																																																 * the
																																																 * CaptureText
																																																 */
		public static String XBMessage_Key_CaptureContextWordsAfter = "CaptureContextWordsAfter"; /*
																																															 * Words
																																															 * to
																																															 * get
																																															 * after
																																															 * the
																																															 * CaptureText
																																															 */
		// Values
		public static String XBMessage_Value_NewIndexAvailable = "NewIndexAvailable"; /*
																																									 * The
																																									 * search
																																									 * service
																																									 * should
																																									 * load
																																									 * a
																																									 * fresh
																																									 * index
																																									 */
		public static String XBMessage_Value_BadSearchRequest = "BadSearchRequest"; /*
																																								 * Malformed
																																								 * search
																																								 * request
																																								 */

		public XMLUtilities() {

		}

		public Node ChildElementOfType(Node node, String childType) {
			NodeList nl = node.getChildNodes();
			int numNodes = nl.getLength();
			for (int i = 0; i < numNodes; i++) {
				Node eachNode = nl.item(i);
				if (eachNode.getNodeType() == Node.ELEMENT_NODE && eachNode.getNodeName() != null
				    && (eachNode.getNodeName().equalsIgnoreCase(childType))) { return eachNode; }
			}

			return null;
		}

		public Node PListNodeForChildNamed(Node node, String childName) {
			boolean getNext = false;

			NodeList nl = node.getChildNodes();
			int numNodes = nl.getLength();
			for (int i = 0; i < numNodes; i++) {
				Node eachNode = nl.item(i);
				if (eachNode.getNodeType() == Node.ELEMENT_NODE) {
					if (getNext) { return eachNode; }

					if (eachNode.getNodeName().equalsIgnoreCase("key")) {
						// Get the content of the child text node
						if ((eachNode.getChildNodes() != null) && (eachNode.getNodeValue() != null)
						    && (eachNode.getNodeValue().equalsIgnoreCase(childName))) {
							getNext = true;
						}
					}
				}
			}

			return null;
		}

		public String PListStringValueForChildNamed(Node node, String childName) {
			Node childNode = PListNodeForChildNamed(node, childName);
			if ((childNode == null) || (childNode.getChildNodes() == null)) {
				System.out.format("Child named %s not found. ", childName);
				return null;
			}

			// childNode->children->content;
			return childNode.getChildNodes().item(0).getNodeValue();
		}

		public int PListINTValueForChildNamed(Node node, String childName) {
			Node childNode = PListNodeForChildNamed(node, childName);
			if (childNode == null || childNode.getChildNodes() == null) {
				System.out.format("Child named &s not found. ", childName);
				return 0;
			}

			// return atoi((char *)childNode->children->content);
			return Integer.valueOf(childNode.getChildNodes().item(0).getNodeValue());
		}

		public long PListLongSValueForChildNamed(Node node, String childName) {
			Node childNode = PListNodeForChildNamed(node, childName);
			if (childNode == null || childNode.getChildNodes() == null) {
				System.out.format("Child named % s not found. ", childName);
				return 0;
			}

			// return atoll((char *)childNode->children->content);
			return Long.valueOf(childNode.getChildNodes().item(0).getNodeValue());
		}

		public boolean PListBooleanValueForChildNamed(Node node, String childName) {
			Node childNode = PListNodeForChildNamed(node, childName);
			if (childNode == null || childNode.getChildNodes() == null) {
				System.out.format("Child named %s not found. ", childName);
				return false;
			}

			return Boolean.valueOf(childNode.getChildNodes().item(0).getNodeValue());
			// return atoi((char *)childNode->children->content);
		}

		public double PListFloatValueForChildNamed(Node node, String childName) {
			Node childNode = PListNodeForChildNamed(node, childName);
			if (childNode == null || childNode.getChildNodes() == null) {
				System.out.format("Child named %s not found. ", childName);
				return 0.0; // Probably should return NaN
			}

			return Double.valueOf(childNode.getChildNodes().item(0).getNodeValue());
			// return atof((char *)childNode->children->content);
		}

		public Node CreatePListElement(Document doc, Node parent, String name, String type, String value) {
			// Node key = new Node(parent, null, "key", name);
			Node key = (Node) doc.createElement("key");
			if (null == key) {
				System.out.format("Unable to create plist key %s.", name);
				return null;
			}
			key.setNodeValue(name);
			parent.appendChild(key);

			// Node result = new Node(parent, null, type, value);
			Node result = (Node) doc.createElement(type);
			if (result == null) {
				System.out.format("Unable to create plist value with key %s and type %s.", name, type);
				return null;
			}
			result.setNodeValue(value);
			parent.appendChild(result);
			return result;
		}

		public Node SetPListElement(Document doc, Node parent, String name, String type, String value) {
			Node result = CreatePListElement(doc, parent, name, type, value);
			if (result == null) {
				System.out.println("Unable to create plist value %s for key %s of type %s." + value + name + type);
				return null;
			}

			// TODO: Escape content?
			// result.setNodeValue(value);

			return result;
		}

		public void ProcessXML(Document doc) {
			// getDocumentElement returns the root element of the document
			Node root = doc.getDocumentElement();
			if (root == null) {
				System.out.println("Unable to get root XML element.");
				return;
			}
			else {
				int nn = root.getChildNodes().getLength();
				NodeList nl = root.getChildNodes();
				System.out.println(root.toString() + " has " + nn + " children");
				System.out.println(root.getNodeValue());
				for (int i = 0; i < nn; i++) {
					Node n = nl.item(i);
					System.out.println(i + " " + n.toString() + " " + n.getNodeValue());
				}
			}

			// Get the top dict node
			// this appears to work
			Node messageRoot = ChildElementOfType(root, "dict");
			if (messageRoot == null) {
				System.out.println("Unable to get message root XML element.");
				return;
			}
			else {
				System.out.println(messageRoot.toString());
				int nn = messageRoot.getChildNodes().getLength();
				NodeList nl = messageRoot.getChildNodes();
				System.out.println(messageRoot.toString() + " has " + nn + " children");
				System.out.println(messageRoot.getNodeValue());
				for (int i = 0; i < nn; i++) {
					Node n = nl.item(i);
					System.out.println(i + " " + n.toString() + " " + n.getNodeValue());
				}
			}

			HashMap parsedPList = processDict("plist", messageRoot);

			//  		
			// Node messageType = PListNodeForChildNamed(messageRoot, XBMessage_Type);
			// if (messageType == null) {
			// System.out.println("Unable to get request type.");
			// return;
			// }
			//  		
			// // Start assuming it's bad
			// messageType.setNodeValue(XBMessage_Value_BadSearchRequest);
			//  		
			// Node captureDict = PListNodeForChildNamed(messageRoot,
			// XBMessage_Key_Capture);
			// if (captureDict == null) {
			// System.out.println("Unable to get capture XML element.");
			// return;
			// }
			//  		
			// int wordsBefore, wordsAfter;
			// String searchString;
			//  		
			// wordsBefore = PListINTValueForChildNamed(messageRoot,
			// XBMessage_Key_CaptureContextWordsBefore);
			// wordsAfter = PListINTValueForChildNamed(messageRoot,
			// XBMessage_Key_CaptureContextWordsAfter);
			// searchString = PListStringValueForChildNamed(captureDict,
			// XBMessage_Key_CaptureText);
			//  		
			// System.out.format("Search for %d words before and %d words after
			// '%s'.", wordsBefore, wordsAfter, searchString);
			//  		
			// //LookUpCaptureString(searchString, captureDict, wordsBefore,
			// wordsAfter);
			//  		
			// messageType.setNodeValue(XBMessage_Type_CaptureHitsFound);
			//  		
			// Node targetService = PListNodeForChildNamed(messageRoot,
			// XBMessage_MessageTarget);
			// if (targetService == null) {
			// targetService.setNodeValue("0");
			// }
		}

		public PListDict processDict(String key, Node dict) {
			// make a new dict collection
			PListDict pld = new PListDict(key);

			// process children
			org.w3c.dom.NodeList nodeList = dict.getChildNodes();
			String s;
			for (int i = 0; i < nodeList.getLength(); i++) {
				org.w3c.dom.Node node = nodeList.item(i);
				int type = node.getNodeType();

				if (type == Node.ELEMENT_NODE) {
					// next node should be a key
					if (node.getNodeName() != null && node.getNodeName().equalsIgnoreCase("key")) {
						String keyval = node.getNodeValue();

						// get the next node and process
						org.w3c.dom.Node relatedNode = nodeList.item(i+1);
						
						if (relatedNode.getNodeName().equalsIgnoreCase("dict")) {
							PListDict childDict = processDict(keyval, relatedNode);
							pld.addElement(keyval, childDict);
						}
						else if (relatedNode.getNodeName().equalsIgnoreCase("array")) {
							PListArray childArray = processArrayNode(keyval, relatedNode);
							pld.addElement(keyval, childArray);		
						}
						else {
							// some other kind of node
						}
						continue;
					}
//					s += ("<" + node.getNodeName() + ">");
//					s += adpNode.content();
//					s += ("</" + node.getNodeName() + ">");
				}
				else if (type == Node.TEXT_NODE) {
//					s += node.getNodeValue();
				}
				else if (type == Node.ENTITY_NODE) {
					// The content is in the TEXT node under it
//					s += adpNode.content();
				}
				else if (type == Node.CDATA_SECTION_NODE) {
					// The "value" has the text, same as a text node.
					// while EntityRef has it in a text node underneath.
					// (because EntityRef can contain multiple subelements)
					// Convert angle brackets and ampersands for display
//					StringBuffer sb = new StringBuffer(node.getNodeValue());
//
//					for (int j = 0; j < sb.length(); j++) {
//						if (sb.charAt(j) == '<') {
//							sb.setCharAt(j, '&');
//							sb.insert(j + 1, "lt;");
//							j += 3;
//						}
//						else if (sb.charAt(j) == '&') {
//							sb.setCharAt(j, '&');
//							sb.insert(j + 1, "amp;");
//							j += 4;
//						}
//					}
//
//					s += ("<pre>" + sb + "\n</pre>");
				}
			}
			return null;
		}

		public PListArray processArrayNode(String keyVal, Node array) {
			// make a new dict collection
			PListArray pla = new PListArray(keyVal);

			// process children
			org.w3c.dom.NodeList nodeList = array.getChildNodes();
			String s;
			for (int i = 0; i < nodeList.getLength(); i++) {
				org.w3c.dom.Node node = nodeList.item(i);
				int type = node.getNodeType();

				if (type == Node.ELEMENT_NODE) {
					// next node should be a key
					if (node.getNodeName().equalsIgnoreCase("key")) {
						String keyVal1 = node.getNodeValue();
						// get the next node and process
						org.w3c.dom.Node relatedNode = nodeList.item(i+1);
						
						if (relatedNode.getNodeName().equalsIgnoreCase("dict")) {
							PListDict childDict = processDict(keyVal1, relatedNode);
							pla.addElement(keyVal1, childDict);
						}
						else if (relatedNode.getNodeName().equalsIgnoreCase("array")) {
							PListArray childArray = processArrayNode(keyVal1, relatedNode);
							pla.addElement(keyVal1, childArray);		
						}
						else {
							// some other kind of node
						}
						continue;
					}
//					s += ("<" + node.getNodeName() + ">");
//					s += adpNode.content();
//					s += ("</" + node.getNodeName() + ">");
				}
				else if (type == Node.TEXT_NODE) {
//					s += node.getNodeValue();
				}
				else if (type == Node.ENTITY_NODE) {
					// The content is in the TEXT node under it
//					s += adpNode.content();
				}
				else if (type == Node.CDATA_SECTION_NODE) {
					// The "value" has the text, same as a text node.
					// while EntityRef has it in a text node underneath.
					// (because EntityRef can contain multiple subelements)
					// Convert angle brackets and ampersands for display
//					StringBuffer sb = new StringBuffer(node.getNodeValue());
//
//					for (int j = 0; j < sb.length(); j++) {
//						if (sb.charAt(j) == '<') {
//							sb.setCharAt(j, '&');
//							sb.insert(j + 1, "lt;");
//							j += 3;
//						}
//						else if (sb.charAt(j) == '&') {
//							sb.setCharAt(j, '&');
//							sb.insert(j + 1, "amp;");
//							j += 4;
//						}
//					}
//
//					s += ("<pre>" + sb + "\n</pre>");
				}
			}
			return null;
		}

		// boolean LookUpCaptureString(String capture, Node captureDict, int
		// wordsBefore, int wordsAfter) {
		// String line = null;
		// int length = 0;
		// String line = PickUTF8CaptureLine(String capture, length);
		//  		
		// XBTrieDocumentID *filterDocs = NULL;
		// XBTrieDocumentID hitDocs[MAX_INDEX_HITS];
		// INT32U hitCharOffsets[MAX_INDEX_HITS];
		// INT32U hitInCaptureCharOffsets[MAX_INDEX_HITS];
		// INT32U hitCaptureLengths[MAX_INDEX_HITS];
		// INT32U nHits = searchAndFix((char *)line, filterDocs, 0 //filterDocs
		// count
		// MAX_INDEX_HITS, hitDocs, hitInCaptureCharOffsets, hitCharOffsets,
		// hitCaptureLengths);
		//  		
		// XBLogC(XBLOG_INFO, "Got %d hit(s) for capture line '%s'.", nHits, line);
		//  		
		// BOOL result = FALSE;
		// INT16U i = 0;
		// for (i = 0; i < nHits; i++)
		// {
		// if (AddDocumentHit(captureDict, hitDocs[i], hitCharOffsets[i],
		// hitCaptureLengths[i], wordsBefore, wordsAfter))
		// {
		// result = TRUE;
		// }
		// }
		//  		
		// return result;
		// }*/

		private class PListDict extends HashMap {
			String mDictName;
			Object mLastAdded;

			public PListDict(String name) {
				mDictName = name;
			}

			public void addElement(String key, Object o) {
				this.put(key, o);
			}
		}

		private class PListArray extends ArrayList {
			String mArrayName;

			public PListArray(String name) {
				mArrayName = name;
			}

			public void addElement(String key, Object o) {
				this.add(o);
			}
		}

		public static XMLUtilities getInstance() {
			if (mInstance == null) {
				mInstance = new XMLUtilities();
			}
			return mInstance;
		}

		public static void processTest() {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				File testFile = new File("/Users/oz/Desktop/plist_wo_tabs_wo_nl.xml");
				Document document = builder.parse(testFile);
				getInstance().ProcessXML(document);
			}
			catch (Exception sxe) {
				sxe.printStackTrace();
			}
		}

		/**
		 * @param args
		 */
		public static void main(String[] args) {
			System.out.println("main");
			processTest();
		}
	}
}

/*
 * $Id: SiteBrowserPanel.java 1009 2006-07-21 22:49:22Z dwd $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.swing.dapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

public class SiteBrowserPanel extends JComponent implements ItemListener {

	private URL siteURL_ = null;
	private String[] tokens_ = { "<TITLE>", "<td id='name'>", "<a href", "<tr><td><a href" };
	// private String title_ = null;
	private String dirName_ = null;
	private Vector name_ = new Vector();
	private Vector url_ = new Vector();
	private Vector dir_ = new Vector();
	private String rootURL_ = null;
	private String currentURL_ = null;
	private String rawSiteURL_ = null;
	private int selected_ = -1;
	private Stack previousURL_ = new Stack();
	JPanel mainPanel = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	JScrollPane scrollPanel = new JScrollPane();
	JList selectionList = new JList();
	DefaultListModel model_ = new DefaultListModel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JPanel urlPanel = new JPanel();
	JLabel jLabel1 = new JLabel();
	JTextField urlTF = new JTextField(30);
	JComboBox urlCB;
	JButton openURLButton = new JButton();
	GridBagLayout gridBagLayout2 = new GridBagLayout();
	private transient Vector actionListeners;
	Dapper parent_;
	private Vector servers_;

	public SiteBrowserPanel() {
		this(null, null);
	}

	public SiteBrowserPanel(Vector ds, Dapper parent) {
		super();
		servers_ = ds;
		parent_ = parent;
		try {
			jbInit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		selectionList.setCellRenderer(new SelectionListRenderer());
	}

	public void setSiteURL(String url) throws MalformedURLException {
		rawSiteURL_ = url;
		currentURL_ = rawSiteURL_.replaceFirst("dods:", "http:");
		urlTF.setText(currentURL_);
		siteURL_ = new URL(currentURL_);
		if (siteURL_.getPort() == -1) {
			rootURL_ = "dods://" + siteURL_.getHost() + siteURL_.getFile() + "/";

		}
		else {
			rootURL_ = "dods://" + siteURL_.getHost() + ":" + siteURL_.getPort() + siteURL_.getFile() + "/";
		}
		// System.out.println("root = " + rootURL_);
		parseURL();
	}

	public String getSiteURL() {
		return rawSiteURL_;
	}

	private void jbInit() {
		setPreferredSize(new Dimension(450, 300));
		setLayout(borderLayout1);
		selectionList.setModel(model_);
		selectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				selectionList_valueChanged(e);
			}
		});
		selectionList.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				selectionList_mouseClicked(e);
			}
		});
		mainPanel.setLayout(gridBagLayout1);
		jLabel1.setText("URL:");
		urlTF.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				urlTF_actionPerformed(e);
			}
		});
		openURLButton.setText("Open");
		openURLButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openURLButton_actionPerformed(e);
			}
		});
		urlPanel.setBorder(BorderFactory.createEtchedBorder());
		urlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(scrollPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
		    GridBagConstraints.BOTH, new Insets(15, 10, 15, 10), 0, 0));
		add(urlPanel, BorderLayout.NORTH);

		urlPanel.add(jLabel1);
		urlPanel.add(urlTF);

		if (servers_ != null) {
			urlCB = new JComboBox(servers_);
			urlCB.addItemListener(this);
			urlPanel.add(urlCB);
			urlPanel.add(openURLButton);
		}
		else {
			urlPanel.add(openURLButton);
		}

		scrollPanel.getViewport().add(selectionList, null);

	}

	private void parseURL() {
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				parseTheURL();
				return null;
			}
		};
		worker.start();
	}

	private void parseTheURL() {
		int open = -1;
		int close = -1;
		model_.clear();
		// title_ = null;
		name_.clear();
		url_.clear();
		dir_.clear();
		if (!previousURL_.empty()) {
			String pURL = (String) previousURL_.peek();
			name_.add("..");
			url_.add(pURL);
			dir_.add(Boolean.TRUE);
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(siteURL_.openStream()));
			String line = br.readLine();
			while (line != null) {
				String token = getToken(line);
				if (!token.equals("")) {
					// System.out.println(count++ +": " + line);
					// if(token.equals(tokens_[0])) { // title
					// open = line.indexOf(">") + 1;
					// close = line.lastIndexOf("<");
					// // title_ = line.substring(open, close);
					// // this.setTitle(title_);
					// // System.out.println(" title = " + title_);
					// } else if(token.equals(tokens_[1])) { // name
					// open = line.indexOf(">") + 1;
					// close = line.lastIndexOf("<");
					// name_.add(line.substring(open, close));
					// }

					// else
					if (token.equals(tokens_[2]) && line.lastIndexOf(".das") > 0) { // .das
						open = line.indexOf("'") + 1;
						close = line.lastIndexOf(".das");
						if (close > 0) {
							url_.add(rootURL_ + line.substring(open, close));
							name_.add(line.substring(open, close));
							dir_.add(Boolean.FALSE);
						}
					}
					else if (token.equals(tokens_[2])) { // url
						try {
							open = line.indexOf("'") + 1;
							close = line.lastIndexOf("'");
							url_.add(rootURL_ + line.substring(open, close));
							name_.add(line.substring(open, close));
							dir_.add(Boolean.TRUE);
						}
						catch (Exception e0) {
							//silent catch
						}
					}
				}
				line = br.readLine();
			}
		}
		catch (Exception ex1) {
		}
		synchronized (getTreeLock()) {
			for (int i = 0; i < name_.size(); i++) {
				model_.add(i, new ListItem((String) name_.get(i), ((Boolean) dir_.get(i)).booleanValue()));
				// model_.add(i, name_.get(i));
				// System.out.println("dir = " + dir_.get(i) + ", name = " +
				// name_.get(i) + ", url = " + url_.get(i));
			}
		}
	}

	private String getToken(String line) {
		for (int i = 0; i < tokens_.length; i++) {
			if (line.indexOf(tokens_[i]) >= 0)
				return tokens_[i];
		}
		return "";
	}

	public String getSelectedURL() {
		if (selected_ < 0)
			return null;
		return (String) url_.get(selected_);
	}

	public String getSelectedDataset() {
		int dirsel = selectionList.getSelectedIndex();
		return (String) name_.get(dirsel);
	}

	public String getSelectedDirectory() {
		return dirName_;
	}

	void selectionList_mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1) {
			// double click
			int dirsel = selectionList.getSelectedIndex();
			if (((Boolean) dir_.get(dirsel)).booleanValue()) {
				// directory open next level!
				try {
					if (!((String) name_.get(dirsel)).equals("..")) {
						previousURL_.push(currentURL_);
					}
					else {
						dirName_ = (String) name_.get(dirsel);
						previousURL_.pop();
					}
					currentURL_ = ((String) url_.get(dirsel)).replaceFirst("dods:", "http:");
					siteURL_ = new URL(currentURL_);
				}
				catch (MalformedURLException ex) {
					ex.printStackTrace();
				}
				parseURL();
				selectionList.invalidate();
				this.validate();
				fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Selection Cleared"));
			}
			else {
				// double click in a dataset: bring up the map panel
				dirName_ = (String) name_.get(dirsel);
				parent_.setToMap();
			}
		}
	}

	void openURLButton_actionPerformed(ActionEvent e) {
		try {
			siteURL_ = new URL(urlTF.getText());
			if (siteURL_.getPort() == -1) {
				rootURL_ = "dods://" + siteURL_.getHost() + siteURL_.getFile() + "/";

			}
			else {
				rootURL_ = "dods://" + siteURL_.getHost() + ":" + siteURL_.getPort() + siteURL_.getFile() + "/";
			}
			// rootURL_ = "dods://" + siteURL_.getHost() + ":" + siteURL_.getPort();
			// System.out.println("root = " + rootURL_);
			previousURL_.clear();
			parseURL();
		}
		catch (MalformedURLException ex) {
			JOptionPane.showMessageDialog(this,
			    "URL could not be parsed.\nA legal example is 'dods://www.epic.noaa.gov:10100/dods'", "Malformed URL",
			    JOptionPane.ERROR_MESSAGE);
		}
	}

	void urlTF_actionPerformed(ActionEvent e) {
		openURLButton_actionPerformed(e);
	}

	class ListItem {
		public String text;
		public boolean dir;

		public ListItem(String text, boolean dir) {
			this.text = text;
			this.dir = dir;
		}

		public String toString() {
			return text;
		}
	}

	class SelectionListRenderer extends JLabel implements ListCellRenderer {
		ImageIcon dirIcon = null;
		ImageIcon fileIcon = null;

		public SelectionListRenderer() {
			super();
			try {
				dirIcon = new ImageIcon(Class.forName("gov.noaa.pmel.swing.ThreeDotsIcon").getResource("images/Open24.gif"));
				fileIcon = new ImageIcon(Class.forName("gov.noaa.pmel.swing.ThreeDotsIcon").getResource("images/File24.gif"));
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}

		}

		/**
		 * getListCellRendererComponent
		 * 
		 * @param jList
		 *          JList
		 * @param object
		 *          Object
		 * @param int2
		 *          int
		 * @param boolean3
		 *          boolean
		 * @param boolean4
		 *          boolean
		 * @return Component
		 * @todo Implement this javax.swing.ListCellRenderer method
		 */
		public Component getListCellRendererComponent(JList list, Object object, int index, boolean isSelected,
		    boolean cellHasFocus) {
			String s = object.toString();
			setText(s);
			if (object instanceof ListItem) {
				if (((ListItem) object).dir) {
					setIcon(dirIcon);
				}
				else {
					setIcon(fileIcon);
				}
			}
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

	void selectionList_valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;

		selected_ = selectionList.getSelectedIndex();
		Object item = selectionList.getSelectedValue();
		String action = null;

		if (item == null || !(item instanceof ListItem) || ((ListItem) item).dir) {
			selected_ = -1;
			action = "Selection Cleared";
		}
		else {
			action = item.toString();
		}

		fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, action));
	}

	public synchronized void removeActionListener(ActionListener l) {
		if (actionListeners != null && actionListeners.contains(l)) {
			Vector v = (Vector) actionListeners.clone();
			v.removeElement(l);
			actionListeners = v;
		}
	}

	public synchronized void addActionListener(ActionListener l) {
		Vector v = actionListeners == null ? new Vector(2) : (Vector) actionListeners.clone();
		if (!v.contains(l)) {
			v.addElement(l);
			actionListeners = v;
		}
	}

	protected void fireActionPerformed(ActionEvent e) {
		if (actionListeners != null) {
			Vector listeners = actionListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++) {
				((ActionListener) listeners.elementAt(i)).actionPerformed(e);
			}
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			if (cb == urlCB && evt.getStateChange() == ItemEvent.SELECTED) {
				urlTF.setText((String) cb.getSelectedItem());
				try {
					this.setSiteURL(urlTF.getText());
				}
				catch (MalformedURLException ex) {
				}
			}
		}
	}

}

/*
 * $Id: OpenFileAction.java,v 1.23 2005/10/18 23:44:53 oz Exp $
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

import java.awt.event.*;
import javax.swing.*;
import gov.noaa.pmel.util.GeoDate;
import ucar.netcdf.Netcdf;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;
import gov.noaa.pmel.eps2.*;
import ucar.multiarray.*;
import ndEdit.ncBrowse.*;

@SuppressWarnings("serial")
public class OpenFileAction extends NdEditAction {
	int[] savedSelection = null;
	Vector<String> paths;
	Vector<String> units;
	String mFileName;
	Netcdf nc = null;
	boolean isPointer = false;
	private double[] latArr1;
	private double[] latArr2;
	private double[] lonArr1;
	private double[] lonArr2;
	private double[] depthArr1;
	private double[] depthArr2;
	private double[] timeArr1;
	private double[] timeArr2;
	private String[] fileNameArr;
	private String[] pathArr;
	private boolean[] pathIsRelArr;
	private String[] dataTypeArr;
	private String[] cruiseArr;
	private String[] castArr;
	private double[] deltaArr;
	private String[] URD;
	private String[] extraField1;
	private String[] extraField2;
	private String[] extraField3;
	private String[] extraField4;
	private String[] extraField5;
	private String[] extraField6;
	private String[] extraField7;
	private String[] extraField8;
	private String[] extraField9;
	private String[] extraField10;
	Vector<String> pointers = new Vector<String>();
	TuplePointerCollection pointerCollection;
	String ptrFile = null;
	URL theURL;
	private File mInputFile = null;
	private boolean DEBUG = false;

	public OpenFileAction(String menu, String text, Icon icon, ViewManager vm, NdEdit parent) {
		super(menu, text, icon, vm, parent);
	}

	public OpenFileAction(String menu, String text, Icon icon, KeyStroke ks, ViewManager vm, NdEdit parent) {
		super(menu, text, icon, ks, vm, parent);
	}

	public void actionPerformed(ActionEvent e) {
		doAction();
	}

	private class MyFrame extends JFrame {
		public MyFrame() {
			super();
		}

		public Dimension getMinimumSize() {
			return new Dimension(400, 120);
		}

		public Dimension getPreferredSize() {
			return new Dimension(400, 120);
		}
	}

	public void doAction() {
		// get a filename
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".nc") || name.endsWith(".cdf") || name.endsWith(".ptr") || name.endsWith("argoinv.txt")
				    || name.endsWith("gtsppinv.txt"))
					return true;
				else
					return false;
			}
		};

		// ask for file
		boolean keepAsking = true;
		FileDialog f = null;
		Frame fr = new Frame();
		String directory;
		while (keepAsking) {
			// present a file dialog
			f = new FileDialog(fr, "Open Browsable File", FileDialog.LOAD);
			f.setFilenameFilter(filter);
			f.setVisible(true);
			directory = f.getDirectory();
			mFileName = f.getFile();
			if (directory != null && mFileName != null) {
				mInputFile = new File(directory, mFileName);
				// get the file format
				try {
					int fileFormat = EPS_Util.getFileFormat(directory, mFileName);
					if (fileFormat == EPSConstants.ARGOINVENTORYFORMAT || fileFormat == EPSConstants.GTSPPINVENTORYFORMAT) {
						final int[] sortKeys = { 0, 0, 0, 0 };

						try {
							final SwingWorker worker = new SwingWorker() {
								public Object construct() {
									JProgressBar progressBar = new JProgressBar(0, 150);
									progressBar.setString("Parsing inventory file...");
									progressBar.setValue(0);
									progressBar.setStringPainted(true);
									progressBar.setIndeterminate(true);

									JDialog jf = new JDialog();
									JPanel jp = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
									jp.setPreferredSize(new Dimension(300, 60));
									jf.setLayout(new BorderLayout(5, 5));
									jp.add(progressBar);
									jf.add(BorderLayout.CENTER, jp);
									jf.setPreferredSize(new Dimension(300, 60));
									jf.setMinimumSize(new Dimension(300, 60));
									jf.pack();
									Rectangle dBounds = jf.getBounds();
									Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
									int x = sd.width / 2 - dBounds.width / 2;
									int y = sd.height / 2 - dBounds.height / 2;
									jf.setLocation(x, y);
									jf.setTitle("Reading inventory file...");
									jf.setVisible(true);

									try {
										EpicPtrFactory ptrfact = new EpicPtrFactory();
										EpicPtrs ptrDB = ptrfact.createEpicPtrs(mInputFile, sortKeys, false, false);
										convertPtrDBToNdEditPC(ptrDB);

										pointerCollection = new TuplePointerCollection(latArr1, latArr2, lonArr1, lonArr2, depthArr1,
										    depthArr2, timeArr1, timeArr2, cruiseArr, castArr, fileNameArr, pathArr, pathIsRelArr,
										    extraField1, extraField2, extraField3, extraField4, extraField5, extraField6, extraField7,
										    extraField8, extraField9, extraField10);
										pointerCollection.setPCTitle(mFileName);
										pointerCollection.setIsArgo(true);
									}
									catch (Exception ex) {
										System.out.println(ex.getMessage());
										ex.printStackTrace();
									}

									if (pointerCollection != null) {
										mParent.setDataTitle(mFileName);
										mParent.setPointerCollection(pointerCollection, true);
										mParent.getFrame().setTitle(mParent.getFrame().getTitle() + ": " + mFileName);
										mParent.getViewManager().invalidateAllViews();
									}

									// done with the progress bar
									jf.setVisible(false);
									jf.dispose();

									return null;
								}
							};
							worker.start();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
						keepAsking = false;
					}
					else if (fileFormat == EPSConstants.PTRFILEFORMAT) {
						// create a pointer file database
						final int[] sortKeys = { EPSConstants.T_DSC, 0, 0, 0 };
						try {
							final SwingWorker worker = new SwingWorker() {
								public Object construct() {
									JProgressBar progressBar = new JProgressBar(0, 150);
									progressBar.setString("Parsing pointer or inventory file...");
									progressBar.setValue(0);
									progressBar.setStringPainted(true);
									progressBar.setIndeterminate(true);

									JFrame jf = new JFrame();
									JPanel jp = new JPanel();
									jp.add("South", progressBar);
									jf.getContentPane().add("Center", jp);
									jf.pack();
									Rectangle dBounds = jf.getBounds();
									Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
									int x = sd.width / 2 - dBounds.width / 2;
									int y = sd.height / 2 - dBounds.height / 2;
									jf.setLocation(x, y);
									jf.setTitle("Parsing Pointer file...");
									jf.setVisible(false);

									try {
										EpicPtrFactory ptrfact = new EpicPtrFactory();
										EpicPtrs ptrDB = ptrfact.createEpicPtrs(mInputFile, sortKeys, false, false);

										// turn this dbase into a ndedits's internal data structures
										if (ptrDB == null && DEBUG) {
											System.out.println("the Ptr DB is null!!");
											System.out.println("ptrfact = " + ptrfact);
											System.out.println("mInputFile = " + mInputFile);
											System.out.println("sortKeys = " + sortKeys);
										}
										convertPtrDBToNdEditPC(ptrDB);

										pointerCollection = new TuplePointerCollection(latArr1, latArr2, lonArr1, lonArr2, depthArr1,
										    depthArr2, timeArr1, timeArr2, URD, fileNameArr, pathArr, pathIsRelArr, dataTypeArr, cruiseArr,
										    castArr, deltaArr, null);
										pointerCollection.setPCTitle(mFileName);
										pointerCollection.setIsArgo(ptrDB.isArgo());
									}
									catch (Exception ex) {
										System.out.println(ex.getMessage());
										ex.printStackTrace();
									}

									if (pointerCollection != null) {
										mParent.setDataTitle(mFileName);
										mParent.setPointerCollection(pointerCollection, true);
										mParent.getFrame().setTitle(mParent.getFrame().getTitle() + ": " + mFileName);
										mParent.getViewManager().invalidateAllViews();
									}

									// done with the progress bar
									jf.setVisible(false);
									jf.dispose();

									return null;
								}
							};
							worker.start();
						}
						catch (Exception exx) {
							exx.printStackTrace();
						}
						keepAsking = false;
					}
					else if (fileFormat == EPSConstants.XMLPTRFILEFORMAT) {

					}
					else if (fileFormat == EPSConstants.POAFORMAT || fileFormat == EPSConstants.JOAFORMAT
					    || fileFormat == EPSConstants.SSFORMAT || fileFormat == EPSConstants.SD2FORMAT
					    || fileFormat == EPSConstants.WOCEHYDFORMAT) {
						EpicPtrs ptrDB = new EpicPtrs();

						// create a pointer
						EpicPtr epPtr = new EpicPtr(fileFormat, "Bottle Import", "BOTTLE", "na", "na", -99, -99, new GeoDate(),
						    -99, -99, mFileName, directory);

						// set the data of ptrDB to this one entry
						ptrDB.setFile(mInputFile);
						ptrDB.setData(epPtr);

						// create a database
						// PointerDBIterator pdbi = ptrDB.iterator();
						// EPSDbase epsDB = new EPSDbase(pdbi);

						// turn this dbase into a NdEdit's internal data structures
						convertPtrDBToNdEditPC(ptrDB);

						keepAsking = false;
					}
					else if (fileFormat == EPSConstants.WOCECTDFORMAT || fileFormat == EPSConstants.NETCDFFORMAT) {
						ndEdit.ncBrowse.NcFile ncFile;
						File file = new File(directory, mFileName);
						try {
							ncFile = new LocalNcFile(file);
						}
						catch (IOException e) {
							e.printStackTrace();
							System.out.println(e + ": new NcFile");
							return;
						}

						try {
							NdEditView tblView = new NdEditView(mParent);
							tblView.setNcFile(ncFile);
							tblView.setVisible(true);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
						keepAsking = false;
					}
					else {
						JFrame ff = new JFrame("Unknown File Format Error");
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(ff, "The file " + mFileName + " is not recognized by NdEdit.");
					}
				}
				catch (Exception ex) {
					JFrame ff = new JFrame("File Import Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(ff, "An error occurred trying to import " + mFileName);
				}
			}
			else {
				// cancelled
				break;
			}
		} // while keepAsking
		f.dispose();
	}

	public void convertPtrDBToNdEditPC(EpicPtrs ptrDB) {
		// get an iterator
		PointerDBIterator itor = ptrDB.iterator();
		int numPtrs = itor.size();

		// allocate the common arrays
		latArr1 = new double[numPtrs];
		lonArr1 = new double[numPtrs];
		depthArr1 = new double[numPtrs];
		timeArr1 = new double[numPtrs];
		timeArr2 = new double[numPtrs];
		latArr2 = null; // by definition for EPIC pointer files
		lonArr2 = null; // by definition for EPIC pointer files

		try {
			if (ptrDB.getFormat() == EPSConstants.TSPTRS)
				depthArr2 = null;
			else
				depthArr2 = new double[numPtrs];
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			// dimension arrays for non-scaler lats and lons
			if (ptrDB.isArgo() || ptrDB.getFormat() == EPSConstants.ARGOPTRS) {
				latArr2 = new double[numPtrs];
				lonArr2 = new double[numPtrs];
				depthArr2 = new double[numPtrs];
			}

			if (ptrDB.getFormat() == EPSConstants.ARGOPTRS || ptrDB.getFormat() == EPSConstants.GTSPPPTRS) {
				extraField1 = new String[numPtrs];
				extraField2 = new String[numPtrs];
				extraField3 = new String[numPtrs];
				extraField4 = new String[numPtrs];
				extraField5 = new String[numPtrs];
				extraField6 = new String[numPtrs];
				extraField7 = new String[numPtrs];
				extraField8 = new String[numPtrs];
				extraField9 = new String[numPtrs];
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		URD = new String[numPtrs];
		fileNameArr = new String[numPtrs];
		pathArr = new String[numPtrs];
		pathIsRelArr = new boolean[numPtrs];
		dataTypeArr = new String[numPtrs];

		int ptrCount = 0;
		boolean zRangeFound = false;
		boolean tRangeFound = false;
		while (itor.hasNext()) {
			EpicPtr ptr = (EpicPtr) itor.next();
			// by definition for EPIC pointer files
			latArr1[ptrCount] = (double) ptr.getLat();
			lonArr1[ptrCount] = (double) ptr.getLon();
			depthArr1[ptrCount] = (double) ptr.getZMin();
			if (depthArr2 != null)
				depthArr2[ptrCount] = (double) ptr.getZMax();
			timeArr1[ptrCount] = (double) ptr.getStartTime().getTime();
			timeArr2[ptrCount] = (double) ptr.getEndTime().getTime();

			if (latArr2 != null)
				latArr2[ptrCount] = (double) ptr.getMaxLat();

			if (lonArr2 != null)
				lonArr2[ptrCount] = (double) ptr.getMaxLon();

			if (extraField1 != null)
				extraField1[ptrCount] = ptr.getExtraField1();

			if (extraField2 != null)
				extraField2[ptrCount] = ptr.getExtraField2();

			if (extraField3 != null)
				extraField3[ptrCount] = ptr.getExtraField3();

			if (extraField4 != null)
				extraField4[ptrCount] = ptr.getExtraField4();

			if (extraField5 != null)
				extraField5[ptrCount] = ptr.getExtraField5();

			if (extraField6 != null)
				extraField6[ptrCount] = ptr.getExtraField6();

			if (extraField7 != null)
				extraField7[ptrCount] = ptr.getExtraField7();

			if (extraField8 != null)
				extraField8[ptrCount] = ptr.getExtraField8();

			if (extraField9 != null)
				extraField9[ptrCount] = ptr.getExtraField9();

			if (extraField10 != null)
				extraField10[ptrCount] = ptr.getExtraField10();

			/*
			 * System.out.println("extraField1[ptrCount] = " + extraField1[ptrCount]);
			 * System.out.println("extraField2[ptrCount] = " + extraField2[ptrCount]);
			 * System.out.println("extraField3[ptrCount] = " + extraField3[ptrCount]);
			 * System.out.println("extraField4[ptrCount] = " + extraField4[ptrCount]);
			 * System.out.println("extraField5[ptrCount] = " + extraField5[ptrCount]);
			 * System.out.println("extraField6[ptrCount] = " + extraField6[ptrCount]);
			 * System.out.println("extraField7[ptrCount] = " + extraField7[ptrCount]);
			 * System.out.println("extraField8[ptrCount] = " + extraField8[ptrCount]);
			 * System.out.println("extraField8[ptrCount] = " + extraField8[ptrCount]);
			 * System.out.println("extraField9[ptrCount] = " + extraField9[ptrCount]);
			 */

			// need to test whether the axes are scaler or vector
			if (!zRangeFound && !ptr.isZScaler())
				zRangeFound = true;
			if (!tRangeFound && !ptr.isTimeScaler())
				tRangeFound = true;

			fileNameArr[ptrCount] = ptr.getFileName();
			pathArr[ptrCount] = ptr.getPath();
			pathIsRelArr[ptrCount] = ptr.isRelativePath();

			if (ptr.isProfile() && ptrCount == 0) {
				cruiseArr = new String[numPtrs];
				castArr = new String[numPtrs];
			}
			else if (ptr.isTimeSeries() && ptrCount == 0) {
				deltaArr = new double[numPtrs];
			}

			if (ptr.isProfile()) {
				cruiseArr[ptrCount] = ptr.getFileSet();
				castArr[ptrCount] = ptr.getID();
				dataTypeArr[ptrCount] = "profile";
			}
			else if (ptr.isTimeSeries()) {
				deltaArr[ptrCount] = (double) ptr.getDeltaT();
				dataTypeArr[ptrCount] = "time series";
			}

			ptrCount++;
		}

		if (!zRangeFound)
			depthArr2 = null;

		if (!tRangeFound)
			timeArr2 = null;

		// determine whether data crosses 180 or 0 meridians
		// hack-o-rama
		boolean b1 = false, b2 = false;
		for (int i = 0; i < lonArr1.length - 1; i++) {
			double lon1 = lonArr1[i];
			if (lon1 < -170)
				b1 = true;
			if (lon1 > 170)
				b2 = true;
			if (b1 && b2) {
				Constants.LONGITUDE_CONV_FACTOR = 360.0f;
				break;
			}
		}

		if (Constants.LONGITUDE_CONV_FACTOR > 0) {
			for (int i = 0; i < lonArr1.length; i++) {
				if (lonArr1[i] < 0) {
					lonArr1[i] += 360.0;
				}
				if (lonArr2 != null && lonArr2[i] < 0) {
					lonArr2[i] += 360.0;
				}
			}
		}
	}

	// this routine does not have all the PC arrays represented
	public void convertDbaseToNdEditPC(EPSDbase epsDB) {
		// get a database iterator
		EPSDBIterator dbItor = epsDB.iterator(false);
		SubDBIterator sdbItor = null;

		// get the type of database (ptr or section)
		boolean isSectionDB = false;
		try {
			Dbase db = (Dbase) dbItor.getElement(0);

			// test whether a subdatabase exists
			isSectionDB = db.isSectionDB();
			if (isSectionDB) {
				sdbItor = db.iterator();
				dbItor = null;
			}
		}
		catch (Exception ex) {
			System.out.println("threw testing for subdatabase");
		}

		int ptrCount = 0;
		if (sdbItor != null)
			ptrCount = sdbItor.size();
		else
			ptrCount = dbItor.size();

		// allocate the arrays
		latArr1 = new double[ptrCount];
		lonArr1 = new double[ptrCount];
		depthArr1 = new double[ptrCount];
		timeArr1 = new double[ptrCount];
		latArr2 = new double[ptrCount];
		lonArr2 = new double[ptrCount];
		depthArr2 = new double[ptrCount];
		timeArr2 = new double[ptrCount];
		URD = new String[ptrCount];

		ptrCount = 0;
		while ((dbItor != null && dbItor.hasNext()) || (sdbItor != null && sdbItor.hasNext())) {
			Dbase db = null;
			try {
				if (isSectionDB)
					db = (Dbase) sdbItor.next();
				else
					db = (Dbase) dbItor.next();
			}
			catch (Exception ex) {
				System.out.println("at P1");
			}

			long minT = Calendar.getInstance().getTime().getTime();
			long maxT = Calendar.getInstance().getTime().getTime();
			double minZ = 50000f;
			double maxZ = -50000f;
			double minLat = 90.0f;
			double maxLat = -90.0f;
			double minLon = 180.0f;
			double maxLon = -180.0f;

			// use the axes to gather the basic metainformation

			// get the longitude convention first get the longitude axis
			Axis lonAxis = db.getAxis("lon");
			if (lonAxis == null)
				;// throw something

			int lonMutiplier = 1;
			int epicCode = lonAxis.getIntegerAttributeValue("epic_code");
			if (epicCode == 501) { // epic code for west longitude convention
				// convert to JOA's internal East longitude convention
				lonMutiplier = -1;
			}

			int numLons = lonAxis.getLen();
			MultiArray lma = lonAxis.getData();

			for (int i = 0; i < numLons; i++) {
				double myLon = 0.0f;
				try {
					myLon = (double) lma.getDouble(new int[] { i });
				}
				catch (Exception ex) {
					// throw something ?
					System.out.println("at P3");
				}

				// apply the longitude convention (it west longitude convert to east
				// longitude)
				myLon *= lonMutiplier;

				minLon = myLon < minLon ? myLon : minLon;
				maxLon = myLon > maxLon ? myLon : maxLon;
			}

			// get the Z axis
			Axis depAxis = db.getAxis("depth");
			if (depAxis == null)
				depAxis = db.getAxis("pres");
			else if (depAxis == null)
				depAxis = db.getAxis("p");

			int numDepths = depAxis.getLen();
			MultiArray zma = depAxis.getData();

			for (int i = 0; i < numDepths; i++) {
				double z = 0;
				try {
					z = (double) zma.getDouble(new int[] { i });
				}
				catch (Exception ex) {
				}

				minZ = z < minZ ? z : minZ;
				maxZ = z > maxZ ? z : maxZ;
			}

			// latitude axis
			Axis latAxis = db.getAxis("lat");
			if (latAxis == null)
				;// throw something

			int numLats = latAxis.getLen();
			lma = latAxis.getData();

			for (int i = 0; i < numLats; i++) {
				double myLat = 0.0f;
				try {
					myLat = (double) lma.getDouble(new int[] { i });
				}
				catch (Exception ex) {
					// throw something ?
					System.out.println("at P2");
				}

				minLat = myLat < minLat ? myLat : minLat;
				maxLat = myLat > maxLat ? myLat : maxLat;
			}

			Axis timeAxis = db.getAxis("time");
			if (timeAxis == null)
				;// throw something ?

			int numTimes = timeAxis.getLen();
			MultiArray tma = timeAxis.getData();

			for (int i = 0; i < numTimes; i++) {
				GeoDate date = null;
				try {
					date = (GeoDate) tma.get(new int[] { i });
				}
				catch (Exception ex) {
					// throw something ?
				}

				// convert to long
				long t = date.getTime();
				minT = t < minT ? t : minT;
				maxT = t > maxT ? t : maxT;
			}

			// make an entry into the pointercollection arrays
			latArr1[ptrCount] = minLat;
			lonArr1[ptrCount] = minLon;
			depthArr1[ptrCount] = minZ;
			depthArr2[ptrCount] = maxZ;
			latArr2[ptrCount] = maxLat;
			lonArr2[ptrCount] = maxLon;
			timeArr1[ptrCount] = minT;
			timeArr2[ptrCount] = maxT;
			ptrCount++;
		}
	}
}

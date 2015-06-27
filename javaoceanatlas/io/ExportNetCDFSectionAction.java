/* * $Id: ExportNetCDFSectionAction.java,v 1.1 2005/09/07 18:43:19 oz Exp $ * */package javaoceanatlas.io;import java.awt.*;import java.io.*;import java.awt.event.*;import javax.swing.*;import javaoceanatlas.ui.*;import javaoceanatlas.utility.JOAFormulas;import javaoceanatlas.resources.*;public class ExportNetCDFSectionAction extends AbstractAction {  protected String mText;  protected FileViewer mFV;  protected String mTitle;  public ExportNetCDFSectionAction(FileViewer fv, String text) {    super(text, null);    mFV = fv;    mText = text;  }  public String getText() {    return mText;  }  public void actionPerformed(ActionEvent e) {    // export a netcdf section    Frame fr = new Frame();    String directory = System.getProperty("user.dir");    FileDialog f = new FileDialog(fr, "Name of Pointer File:", FileDialog.SAVE);    f.setDirectory(directory);    String fileExtension = new String(".ptr");        String strippedFileName = JOAFormulas.stripExtensions(mFV.getTitle());    f.setFile(strippedFileName + fileExtension);    f.setVisible(true);    directory = f.getDirectory();    f.dispose();    if (directory != null && f.getFile() != null) {      File outFile = new File(directory, f.getFile());      mFV.setCurrOutFile(outFile);      String outFileName = outFile.getName();      String ptrFileName = outFile.getName().toLowerCase();      try {        if (ptrFileName.indexOf(".xml") > 0) {          NetCDFSupport.writeNetCDF_XML(outFile, mFV);        }        else {          NetCDFSupport.writeNetCDF_EPIC(outFile, mFV);        }      }      catch (Exception ex) {        ex.printStackTrace();      }      try {        JOAConstants.LogFileStream.writeBytes("Exported netCDF file: " + outFile.getCanonicalPath() + "\n");        JOAConstants.LogFileStream.flush();      }      catch (Exception ex) {}    }  }  public boolean isEnabled() {    return true;  }}
/*
 * $Id: PlotSpecification.java,v 1.4 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

//import java.io.*;
//import java.awt.*;
import org.w3c.dom.*;
//import com.ibm.xml.parser.*;
//import org.xml.sax.*;
import javaoceanatlas.ui.*;
import java.io.File;
import java.io.IOException;

public interface PlotSpecification {
    public void saveAsXML(FileViewer fv, Document doc, Element root);
    public void writeToLog(String preamble) throws IOException;
}
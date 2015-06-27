package gov.noaa.pmel.eps2;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
//import javax.xml.parsers.*;
/**
 * <code>XMLPtrFileWriter</code> defines the write method for writing pointers in XML format.
 *
 * @author oz
 * @version 1.0
 */

public class XMLPtrFileWriter implements PtrFileWriter {
	File mFile;

	public XMLPtrFileWriter(File inFile) {
		mFile = inFile;
	}

	public void write(ArrayList thePtrs) throws IOException {
	}

	/*
	* Write a pointer file.
	*
	* @exception IOException An IO error occurred writing the pointer file
	*/
	public void write(ArrayList thePtrs, PointerFileAttributes ptrAttributes) throws IOException {
		// need to determine where to place the varlist tag
		ArrayList globalVars = ptrAttributes.getVarList();
		boolean globalVarList = globalVars != null;

		// need to determine where to place the URI attribute
		boolean globalURI = false;

		String type = ptrAttributes.getType();

		try {

			// DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
			// DocumentBuilder docBuild = docFact.newDocumentBuilder();
			// Document doc = docBuild.newDocument();

    		// create a document object
    		Document doc = (Document)Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

    		// make joapreferences the root element
    		Element root = doc.createElement("epicxml");
		    root.setAttribute("version", "1.0");

		    // set the required type
		    root.setAttribute("type", type);

			// path
			if (ptrAttributes.getPath() != null) {
	   			root.setAttribute("uri", ptrAttributes.getPath()); // path includes the protocol
	   			globalURI = true;
			}

			// domain (required)
			Element domain = doc.createElement("domain");

			// get the latitude domain
			ArrayList lats = ptrAttributes.getLats();
			Iterator ditor = lats.iterator();
			while (ditor.hasNext()) {
				ExportLatitude lat = (ExportLatitude)ditor.next();
    			Element lItem = doc.createElement("latitude");
    			lItem.appendChild(doc.createTextNode(Double.toString(lat.getLat())));
		   		lItem.setAttribute("location", lat.getLocation());
		   		lItem.setAttribute("units", lat.getLatUnits());
    			domain.appendChild(lItem);
			}

			// get the longitude domain
			ArrayList lons = ptrAttributes.getLons();
			ditor = lons.iterator();
			while (ditor.hasNext()) {
				ExportLongitude lon = (ExportLongitude)ditor.next();
    			Element lItem = doc.createElement("longitude");
    			lItem.appendChild(doc.createTextNode(Double.toString(lon.getLon())));
		   		lItem.setAttribute("location", lon.getLocation());
		   		lItem.setAttribute("units", lon.getLonUnits());
    			domain.appendChild(lItem);
			}

			// get the vertical domain
			ArrayList verts = ptrAttributes.getVerticals();
			ditor = verts.iterator();
			while (ditor.hasNext()) {
				ExportVertical z = (ExportVertical)ditor.next();
    			Element lItem = doc.createElement("vertical");
    			lItem.appendChild(doc.createTextNode(Double.toString(z.getZ())));
		   		lItem.setAttribute("location", z.getLocation());
		   		lItem.setAttribute("units", z.getVerticalUnits());
		   		lItem.setAttribute("positive", z.getPositive());
    			domain.appendChild(lItem);
			}

			// get the time domain
			ArrayList timesordates = ptrAttributes.getTimesOrDates();
			ditor = timesordates.iterator();
			while (ditor.hasNext()) {
				Object tord = ditor.next();
				if (tord instanceof ExportTime) {
    				Element lItem = doc.createElement("time");
    				lItem.appendChild(doc.createTextNode(Long.toString(((ExportTime)tord).getT())));
		   			lItem.setAttribute("location", ((ExportTime)tord).getLocation());
		   			lItem.setAttribute("units", ((ExportTime)tord).getTimeUnits());
    				domain.appendChild(lItem);
				}
				else if (tord instanceof ExportDate) {
    				Element lItem = doc.createElement("date");
		   			lItem.setAttribute("location", ((ExportDate)tord).getLocation());
		   			lItem.setAttribute("year",  Integer.toString(((ExportDate)tord).getYear()));
		   			lItem.setAttribute("month", Integer.toString(((ExportDate)tord).getMonth()));
		   			lItem.setAttribute("day",   Integer.toString(((ExportDate)tord).getDay()));
		   			lItem.setAttribute("hour",  Integer.toString(((ExportDate)tord).getHour()));
		   			lItem.setAttribute("min",   Integer.toString(((ExportDate)tord).getMinutes()));
		   			lItem.setAttribute("secs",  Double.toString(((ExportDate)tord).getSecs()));
    				domain.appendChild(lItem);
				}
			}
    		root.appendChild(domain);

    		// global varlist goes here if needed
    		if (globalVarList) {
    			Element vlitem = doc.createElement("varlist");

    			// write the individual variable items
				Iterator vitor = globalVars.iterator();
				while (vitor.hasNext()) {
		        	ExportVariable ev = (ExportVariable)vitor.next();

		    		Element vitem = doc.createElement("variable");
		    		vitem.setAttribute("name", ev.getVarName());
		    		if (ev.getVarUnits() != null)
		    			vitem.setAttribute("units", ev.getVarUnits());
		    		else
		    			vitem.setAttribute("units", "n.a.");
		    		if (ev.getLexicon() != null)
		    			vitem.setAttribute("lexicon", ev.getLexicon());
		    		if (ev.getAlgorithmRef() != null)
		    			vitem.setAttribute("algorithm", ev.getAlgorithmRef());
		    		vlitem.appendChild(vitem);

		    		// write the variable-level attributes and comments
		    		if (ev.getNumAttributes() > 0) {
		    			Hashtable attribs = ev.getAttributes();
		    			Enumeration iter = attribs.keys();
		    			while (iter.hasMoreElements()) {
		    				String key = (String)iter.nextElement();
		    				String val = (String)attribs.get(key);

		    				Element attribItem = doc.createElement("attribute");
		    				attribItem.setAttribute("name", key);
		    				attribItem.setAttribute("value", val);
		    				vlitem.appendChild(attribItem);
		    			}
		    		}

		    		if (ev.getComments().size() > 0) {
		    			ArrayList comments = ev.getComments();
		    			Iterator citor = comments.iterator();
		    			while (citor.hasNext()) {
		    				String aComment = (String)citor.next();

		    				Element commentItem = doc.createElement("comment");
							commentItem.appendChild(doc.createTextNode(aComment));
		    				vlitem.appendChild(commentItem);
		    			}
		    		}
		        }
    			root.appendChild(vlitem);
    		}

			// Iterate on the filesets
			Iterator itor = thePtrs.iterator();
			while (itor.hasNext()) {
	        	ExportFileSet efs = (ExportFileSet)itor.next();
		        ArrayList fsVars = efs.getVariables();

		        // begin the fileset tag
				Element fileSetItem = doc.createElement("fileset");
    			fileSetItem.setAttribute("id", efs.getID());
	    		if (efs.getURI() != null)
	    			fileSetItem.setAttribute("uri", String.valueOf(efs.getURI()));

		        // if varlist is present
		        if (fsVars != null) {
    				Element fsvlitem = doc.createElement("varlist");
		        	// write variable list for this fileset
		        	// if global vars are present write variables as references
					Iterator vitor = fsVars.iterator();
					while (vitor.hasNext()) {
			        	ExportVariable ev = (ExportVariable)vitor.next();

			        	if (globalVarList) {
			        		// try to make this a variable ID
			        		String refName = null;
							Iterator gvitor = globalVars.iterator();
							while (gvitor.hasNext()) {
					        	ExportVariable gev = (ExportVariable)gvitor.next();
					        	if (ev.getVarName().equalsIgnoreCase(gev.getVarName())) {
					        		refName = new String(ev.getVarName());
					        		break;
					        	}
					        }

			        		if (refName != null) {
					    		Element vritem = doc.createElement("variableref");
	    						vritem.setAttribute("name", ev.getVarName());
					    		fsvlitem.appendChild(vritem);
					    	}
					    	else {
					    		// add this as a regular variable item
					    		Element vitem = doc.createElement("variable");
					    		vitem.setAttribute("name", ev.getVarName());
					    		if (ev.getVarUnits() != null)
					    			vitem.setAttribute("units", ev.getVarUnits());
					    		else
					    			vitem.setAttribute("units", "n.a.");
					    		if (ev.getLexicon() != null)
					    			vitem.setAttribute("lexicon", ev.getLexicon());
					    		if (ev.getAlgorithmRef() != null)
					    			vitem.setAttribute("algorithm", ev.getAlgorithmRef());
					    		fsvlitem.appendChild(vitem);
					    	}
			        	}
			        	else {
				    		Element vitem = doc.createElement("variable");
				    		vitem.setAttribute("name", ev.getVarName());
				    		if (ev.getVarUnits() != null)
				    			vitem.setAttribute("units", ev.getVarUnits());
				    		else
				    			vitem.setAttribute("units", "n.a.");
				    		if (ev.getLexicon() != null)
				    			vitem.setAttribute("lexicon", ev.getLexicon());
				    		if (ev.getAlgorithmRef() != null)
				    			vitem.setAttribute("algorithm", ev.getAlgorithmRef());
				    		fsvlitem.appendChild(vitem);
				    	}
			        }
			        fileSetItem.appendChild(fsvlitem);
		        }

		        // get an iterator for the stations
		        Iterator stnItor = efs.getStations().iterator();
		        while (stnItor.hasNext()) {
	        		ExportStation stn = (ExportStation)stnItor.next();

			        // begin the station tag
		    		Element stnItem = doc.createElement("station");
		    		stnItem.setAttribute("id", stn.getID());
		    		stnItem.setAttribute("cast", stn.getCast().toString());
		    		stnItem.setAttribute("bottom", String.valueOf(stn.getBottomDepth()));
		    		if (!globalURI && stn.getURI() != null)
		    			stnItem.setAttribute("uri", stn.getURI());
		    		if (stn.getFileName() != null)
		    			stnItem.setAttribute("reference", stn.getFileName());

		        	ArrayList stnVars = stn.getParameters();
			        // if stn varlist is present
			        if (stnVars != null) {
			        	// write variable list for this fileset
		        		// if global vars are present write variables as references
						Iterator vitor = stnVars.iterator();
						while (vitor.hasNext()) {
				        	ExportVariable ev = (ExportVariable)vitor.next();

				    		Element vitem = doc.createElement("variable");
				    		vitem.setAttribute("name", ev.getVarName());
				    		if (ev.getVarUnits() != null)
				    			vitem.setAttribute("units", ev.getVarUnits());
				    		else
				    			vitem.setAttribute("units", "n.a.");
				    		if (ev.getLexicon() != null)
				    			vitem.setAttribute("lexicon", ev.getLexicon());
				    		if (ev.getAlgorithmRef() != null)
				    			vitem.setAttribute("algorithm", ev.getAlgorithmRef());
				    		stnItem.appendChild(vitem);
				        }
			        }

			        // time or date tag
					ArrayList stntimesordates = stn.getTimesOrDates();
					ditor = stntimesordates.iterator();
					while (ditor.hasNext()) {
						Object tord = ditor.next();
						if (tord instanceof ExportTime) {
		    				Element lItem = doc.createElement("time");
		    				lItem.appendChild(doc.createTextNode(Long.toString(((ExportTime)tord).getT())));
				   			lItem.setAttribute("location", ((ExportTime)tord).getLocation());
				   			lItem.setAttribute("units", ((ExportTime)tord).getTimeUnits());
		    				stnItem.appendChild(lItem);
						}
						else if (tord instanceof ExportDate) {
		    				Element lItem = doc.createElement("date");
				   			lItem.setAttribute("location", ((ExportDate)tord).getLocation());
				   			lItem.setAttribute("year",  Integer.toString(((ExportDate)tord).getYear()));
				   			lItem.setAttribute("month", Integer.toString(((ExportDate)tord).getMonth()));
				   			lItem.setAttribute("day",   Integer.toString(((ExportDate)tord).getDay()));
				   			lItem.setAttribute("hour",  Integer.toString(((ExportDate)tord).getHour()));
				   			lItem.setAttribute("min",   Integer.toString(((ExportDate)tord).getMinutes()));
				   			lItem.setAttribute("secs",  Double.toString(((ExportDate)tord).getSecs()));
		    				stnItem.appendChild(lItem);
						}
					}

			        // latitude tag
					ExportLatitude lat = stn.getLat();
	    			Element lItem = doc.createElement("latitude");
	    			lItem.appendChild(doc.createTextNode(Double.toString(lat.getLat())));
			   		lItem.setAttribute("location", lat.getLocation());
			   		lItem.setAttribute("units", lat.getLatUnits());
	    			stnItem.appendChild(lItem);

			        // longitude tag
					ExportLongitude lon = stn.getLon();
	    			lItem = doc.createElement("longitude");
	    			lItem.appendChild(doc.createTextNode(Double.toString(lon.getLon())));
			   		lItem.setAttribute("location", lon.getLocation());
			   		lItem.setAttribute("units", lon.getLonUnits());
	    			stnItem.appendChild(lItem);

			        // vertical tags
					ArrayList stnverts = stn.getVerticals();
					ditor = stnverts.iterator();
					while (ditor.hasNext()) {
						ExportVertical z = (ExportVertical)ditor.next();
		    			lItem = doc.createElement("vertical");
		    			lItem.appendChild(doc.createTextNode(Double.toString(z.getZ())));
				   		lItem.setAttribute("location", z.getLocation());
				   		lItem.setAttribute("units", z.getVerticalUnits());
				   		lItem.setAttribute("positive", z.getPositive());
		    			stnItem.appendChild(lItem);
					}

		    		if (stn.getDeltaT() > 0) {
			    		lItem = doc.createElement("deltat");
			    		lItem.appendChild(doc.createTextNode(Double.toString(stn.getDeltaT())));
			    		if (stn.getDeltaTUnits() != null)
				    		lItem.setAttribute("units", stn.getDeltaTUnits());
			    		stnItem.appendChild(lItem);
		    		}

			       	// station variables go here if they exists
		    		if (stn.getNumStnCalcs() > 0) {
		    			ArrayList scalcs = stn.getStnCalcs();

		    			Iterator scitor = scalcs.iterator();
		    			while (scitor.hasNext()) {
		    				// get a station calc object
		    				StationCalculation sc = (StationCalculation)scitor.next();

		    				if (sc.getVarName() != null) {
		    					Element stnCalcItem = doc.createElement("stationvalue");
		    					stnCalcItem.setAttribute("name", sc.getVarName());
		    					if (sc.getVarUnits() != null)
		    						stnCalcItem.setAttribute("units", sc.getVarUnits());
		    					else
		    						stnCalcItem.setAttribute("units", "n.a.");

		    					if (sc.getCalcMethod() != null)
		    						stnCalcItem.setAttribute("method", sc.getCalcMethod());

		    					if (sc.getLexicon() != null)
		    						stnCalcItem.setAttribute("lexicon", sc.getLexicon());

		    					stnCalcItem.setAttribute("value", Double.toString(sc.getValue()));
		    					stnCalcItem.setAttribute("missingflag", new Boolean(!sc.isMissing()).toString());

		    					//TODO: stn calc might have additional context like whether it outcrops, interpolated, etc...
		    					// stationcalc-level attributes and comments
					    		if (sc.getNumAttributes() > 0) {
					    			Hashtable attribs = sc.getAttributes();
					    			Enumeration iter = attribs.keys();
					    			while (iter.hasMoreElements()) {
					    				String key = (String)iter.nextElement();
					    				String val = (String)attribs.get(key);

					    				Element attribItem = doc.createElement("attribute");
					    				attribItem.setAttribute("name", key);
					    				attribItem.setAttribute("value", val);
					    				stnCalcItem.appendChild(attribItem);
					    			}
					    		}

					    		if (sc.getComments().size() > 0) {
					    			ArrayList comments = sc.getComments();
					    			Iterator citor = comments.iterator();
					    			while (citor.hasNext()) {
					    				String aComment = (String)citor.next();

					    				Element commentItem = doc.createElement("comment");
				    					commentItem.appendChild(doc.createTextNode(aComment));
					    				stnCalcItem.appendChild(commentItem);
					    			}
					    		}
		    					stnItem.appendChild(stnCalcItem);
		    				}
		    			}
		    		}

		    		// station-level attributes and comments
		    		if (stn.getNumAttributes() > 0) {
		    			Hashtable attribs = stn.getAttributes();
		    			Enumeration iter = attribs.keys();
		    			while (iter.hasMoreElements()) {
		    				String key = (String)iter.nextElement();
		    				String val = (String)attribs.get(key);

		    				Element attribItem = doc.createElement("attribute");
		    				attribItem.setAttribute("name", key);
		    				attribItem.setAttribute("value", val);
		    				stnItem.appendChild(attribItem);
		    			}
		    		}

		    		if (stn.getComments().size() > 0) {
		    			ArrayList comments = stn.getComments();
		    			Iterator citor = comments.iterator();
		    			while (citor.hasNext()) {
		    				String aComment = (String)citor.next();

		    				Element commentItem = doc.createElement("comment");
	    					commentItem.appendChild(doc.createTextNode(aComment));
		    				stnItem.appendChild(commentItem);
		    			}
		    		}

			        // end the station tag
					fileSetItem.appendChild(stnItem);

		        }
		        // end the fileset tag
				root.appendChild(fileSetItem);
			}

			// global attributes and comments
    		if (ptrAttributes.getNumAttributes() > 0) {
    			Hashtable attribs = ptrAttributes.getAttributes();
    			Enumeration iter = attribs.keys();
    			while (iter.hasMoreElements()) {
    				String key = (String)iter.nextElement();
    				String val = (String)attribs.get(key);

    				Element attribItem = doc.createElement("attribute");
    				attribItem.setAttribute("name", key);
    				attribItem.setAttribute("value", val);
    				root.appendChild(attribItem);
    			}
    		}

    		if (ptrAttributes.getComments() != null && ptrAttributes.getComments().size() > 0) {
    			ArrayList comments = ptrAttributes.getComments();
    			Iterator citor = comments.iterator();
    			while (citor.hasNext()) {
    				String aComment = (String)citor.next();

    				Element commentItem = doc.createElement("comment");
					commentItem.appendChild(doc.createTextNode(aComment));
    				root.appendChild(commentItem);
    			}
    		}

    		// write the XML file
    		doc.appendChild(root);
    		((TXDocument)doc).setVersion("1.0");
    		((TXDocument)doc).printWithFormat(new FileWriter(mFile));
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

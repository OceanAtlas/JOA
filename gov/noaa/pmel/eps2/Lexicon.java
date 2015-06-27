package gov.noaa.pmel.eps2;

import java.util.*;
import java.io.*;

/**
 * <code>Lexicon</code> 
 * Object that stores translations between various lexicons
 *
 * @author oz
 * @version 1.0
 */
public class Lexicon {
	public String mDir = null;
	public static int EPIC_LEXICON = 0;
	public static int JOA_LEXICON = 1;
	public static int WOCE_LEXICON = 2;
	public static int ARGO_LEXICON = 3;
	public static int BOTTLE_SALT = 0;
	public static int CTD_SALT = 1;
	public static int BOTTLE_O2 = 0;
	public static int CTD_O2 = 1;
	Hashtable mJOAToEPIC = new Hashtable();
	Hashtable mJOAToWOCE = new Hashtable();
	Hashtable mJOAToEPICCode = new Hashtable();
	Hashtable mJOAToArgo = new Hashtable();
	
	Hashtable mWOCEToEPIC = new Hashtable();
	Hashtable mWOCEToEPICCode = new Hashtable();
	Hashtable mWOCEToJOA = new Hashtable();
	Hashtable mWOCEToArgo = new Hashtable();
	
	Hashtable mArgoToJOA = new Hashtable();
	Hashtable mArgoToEPIC = new Hashtable();
	Hashtable mArgoToWOCE = new Hashtable();
	Hashtable mArgoToEPICCode = new Hashtable();
	
	Hashtable mEPICToJOA = new Hashtable();
	Hashtable mEPICToWOCE = new Hashtable();
	Hashtable mEPICToArgo = new Hashtable();
	Hashtable mEPICCodeToWOCE = new Hashtable();
	Hashtable mEPICCodeToJOA = new Hashtable();
	Hashtable mEPICCodeToEPIC = new Hashtable();
	Hashtable mEPICCodeToArgo = new Hashtable();
	
	// The Canonical Variables for JOA
	public static String mJOACanonicalDepth = "PRES";
	public static String mJOACanonicalBottleSalinity = "SALT";
	public static String mJOACanonicalCTDSalinity = "CTDS";
	public static String mJOACanonicalTemperature = "TEMP";
	public static String mJOACanonicalBottleOxygen = "O2";
	public static String mJOACanonicalCTDOxygen = "CTDO";
	public static String mJOACanonicalNitrate = "NO3";
	public static String mJOACanonicalPhosphate = "PO4";
	
	// The Canonical Variables for WOCE
	public static String mWOCECanonicalDepth = "CTDPRS";
	public static String mWOCECanonicalBottleSalinity = "SALNTY";
	public static String mWOCECanonicalCTDSalinity = "CTDSAL";
	public static String mWOCECanonicalTemperature = "CTDTMP";
	public static String mWOCECanonicalBottleOxygen = "OXYGEN";
	public static String mWOCECanonicalCTDOxygen = "CTDOXY";
	public static String mWOCECanonicalNitrate = "NITRAT";
	public static String mWOCECanonicalPhosphate = "PHSPHT";
	
	// canonical variables for EPIC
	public static String mEPICCanonicalDepth = "P";
	public static String mEPICCanonicalBottleSalinity = "S0";
	public static String mEPICCanonicalCTDSalinity = "S";
	public static String mEPICCanonicalTemperature = "T";
	public static String mEPICCanonicalBottleOxygen = "BO";
	public static String mEPICCanonicalCTDOxygen = "O";
	public static String mEPICCanonicalNitrate = "NO3";
	public static String mEPICCanonicalPhosphate = "PO4";
	
	public static int mEPICCodeCanonicalDepth = 1;
	public static int mEPICCodeCanonicalBottleSalinity = 43;
	public static int mEPICCodeCanonicalCTDSalinity = 41;
	public static int mEPICCodeCanonicalTemperature = 28;
	public static int mEPICCodeCanonicalBottleOxygen = 66;
	public static int mEPICCodeCanonicalCTDOxygen = 65;
	public static int mEPICCodeCanonicalNitrate = 282;
	public static int mEPICCodeCanonicalPhosphate = 286;
	
	// The Canonical Variables for Argo
	public static String mArgoCanonicalDepth = "PRES";
	public static String mArgoCanonicalCTDSalinity = "PSAL";
	public static String mArgoCanonicalTemperature = "TEMP";
	public static String mArgoCanonicalCTDOxygen = "DOXY";
	public static String mArgoCanonicalDepth2 = "pressure";
	public static String mArgoCanonicalCTDSalinity2 = "salinity";
	public static String mArgoCanonicalTemperature2 = "temperature";
	public static String mArgoCanonicalCTDOxygen2 = "dissolved_oxygen";
	
	private static boolean DEBUG = false;
	
	public Lexicon(String dir) {
		if (dir != null)
			mDir = new String(dir);
		try {
			init();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Lexicon() {
		this(null);
	}
	
	public void init() throws Exception{
		// init the lexicon from an external file
		try {
			String lexDir;
			if (mDir != null)
				lexDir = mDir + File.pathSeparator + "lexicon.txt";
			else
				lexDir = "lexicon.txt";
				
			if (DEBUG)
				System.out.println(lexDir);
			File f = EPS_Util.getSupportFile(lexDir);
			FileReader lex = new FileReader(f);
			readLexicon(lex);
		}
		catch (IOException ex) {
			throw ex;
		}
	}

    /**
    * Read the lexicon
    *
    * @file lexicon tab-delimited file
    *
    * @exception IOException An IO error occured reading the lexicon file.
    */
    public void readLexicon(Reader file) throws IOException {
        try {
        	parse(file);
        }
        catch (IOException ex) {
        	throw(ex);
        }
    }
    
    
    /**
    * Determine the Lexicon of an individual Dbase (profile or time series)
    *
    * @inVar EPSVariable
    *
    */
    public int getLexicon(Dbase db) {
    	// look first for a lexicon global attribute
    	// this attribute added to files created by JOA 3.5 and above
    	// Other formats nay eventually choose to support this attribute
		EPSAttribute lexiconAttr = db.getAttributeByFuzzyName("lexicon");
		
		if (lexiconAttr != null) {
			if (lexiconAttr.getStringVal().equalsIgnoreCase("JOA"))
				return JOA_LEXICON;
			if (lexiconAttr.getStringVal().equalsIgnoreCase("EPIC"))
				return EPIC_LEXICON;
			if (lexiconAttr.getStringVal().equalsIgnoreCase("WOCE"))
				return WOCE_LEXICON;
		}
		
		lexiconAttr = db.getAttributeByFuzzyName("Conventions");
		
		if (lexiconAttr != null) {
			if (lexiconAttr.getStringVal().equalsIgnoreCase("COARDS, US_NODC"))
				return ARGO_LEXICON;
			else if (lexiconAttr.getStringVal().equalsIgnoreCase("COARDS, WOCE, GTSPP"))
				return ARGO_LEXICON;
		}
		
    	if (db.getDataType().indexOf("JOA") >= 0 || db.getDataType().indexOf("POA") >= 0 || db.getDataType().indexOf("SD2") >= 0)
    		return JOA_LEXICON;
    	else if (db.getDataType().indexOf("WOCE") >= 0)
    		return WOCE_LEXICON;
    	//else if (db.getDataType().indexOf("BOTTLE") >= 0)
    	//	return JOA_LEXICON;
    	//else if (db.getDataType().indexOf("Argo") >= 0)
    	//	return ARGO_LEXICON;
    	else {
    		// try looking for an epic_code attribute in this Dbase's variable list
    		// the prescence of an epic code attribute doesn't insure that the actual variables are in
    		// epic lexicon but rather their equivalent in epic
    		Vector tvars = db.getMeasuredVariables(false);
    		for (int p=0; p<tvars.size(); p++) {
				Object obj = tvars.elementAt(p);
				EPSVariable epv = (EPSVariable)obj;
				EPSAttribute epicCodeAttr = epv.getAttribute("epic_code");
				if (epicCodeAttr != null)
					return EPIC_LEXICON;
					
				// look for the epic code embedded in the variable name
				/*if (epv.getGname().indexOf('_') > 0) {
					// check and see if epic code is embedded to var title
					String tempVar = epv.getGname().trim();
					
					if (tempVar.length() == 0)
						tempVar = epv.getOname();
						
					try {
						String ss = tempVar.substring(tempVar.indexOf('_') + 1, tempVar.length());
						int epicVarCode = Integer.valueOf(ss).intValue();
						return EPIC_LEXICON;
					}
					catch (Exception ex) {
						// just eat the exception
					}
				}*/
			}
    	}
    		
    	// couldn't determine a lexicon
    	return -99;
    }
    public String getLexiconString(Dbase db) {
    	// look first for a lexicon global attribute
    	// this attribute added to files created by JOA 3.5 and above
    	// Other formats nay eventually choose to support this attribute
		EPSAttribute lexiconAttr = db.getAttributeByFuzzyName("lexicon");
		
		if (lexiconAttr != null) {
			if (lexiconAttr.getStringVal().equalsIgnoreCase("JOA"))
				return "JOA";
			if (lexiconAttr.getStringVal().equalsIgnoreCase("EPIC"))
				return "EPIC";
			if (lexiconAttr.getStringVal().equalsIgnoreCase("WOCE"))
				return "WOCE";
		}
		
    	if (db.getDataType().indexOf("JOA") >= 0 || db.getDataType().indexOf("POA") >= 0 || db.getDataType().indexOf("SD2") >= 0)
    		return "JOA";
    	else if (db.getDataType().indexOf("WOCE") >= 0)
    		return "WOCE";
    	else if (db.getDataType().indexOf("BOTTLE") >= 0)
    		return "JOA";
    	else if (db.getDataType().indexOf("Argo") >= 0)
    		return "Argo";
    	else {
    		// try looking for an epic_code attribute in this Dbase's variable list
    		// the prescence of an epic code attribute doesn't insure that the actual variables are in
    		// epic lexicon but rather their equivalent in epic
    		Vector tvars = db.getMeasuredVariables(false);
    		for (int p=0; p<tvars.size(); p++) {
				Object obj = tvars.elementAt(p);
				EPSVariable epv = (EPSVariable)obj;
				EPSAttribute epicCodeAttr = epv.getAttribute("epic_code");
				if (epicCodeAttr != null)
					return "EPIC";
			}
    	}
    		
    	// couldn't determine a lexicon
    	return "unk";
    }
    
    /**
    * Parse the Lexicon.txt file
    *
    * @file fr Lexicon.txt Reader
    *
    * @exception IOException An IO error occured parsing the Lexicon.txt file.
    */
    private void parse(Reader fr) throws IOException {
        try {
            String inLine = new String();
            char oldDelim = EPSProperties.DELIMITER;
        	LineNumberReader in = new LineNumberReader(fr, 10000);
        	
        	// skip the first 2 lines
            inLine = in.readLine();
            inLine = in.readLine();
            
            // read the rest of the lines
            while (true) {
            	EPSProperties.DELIMITER = EPSProperties.TAB_DELIMITER;
            	
                inLine = in.readLine();
                if (inLine == null)
                    break;
                    
                inLine = EPS_Util.expandNullItems(inLine);

                // parse the line
                // id is epic code
                String temp = EPS_Util.getItem(inLine, 1).trim();
                //temp = EPS_Util.trimPreceedingWhiteSpace(temp);

                int id = 0;
                try {
                	id = Integer.valueOf(temp).intValue();
                }
                catch (NumberFormatException ex) {
                	ex.printStackTrace();
                    continue;
                }

                // sname
                String shortEPICname = EPS_Util.getItem(inLine, 2).trim().toLowerCase();
                
                // JOA
                String JOAnames = EPS_Util.getItem(inLine, 3);
                if (JOAnames != null)
                	 JOAnames = JOAnames.toLowerCase();
                
                // WOCE
                String WOCEnames = EPS_Util.getItem(inLine, 4);
                if (WOCEnames != null)
                	 WOCEnames = WOCEnames.toLowerCase();
                
                // Argo
                String Argonames = EPS_Util.getItem(inLine, 5);
                if (Argonames != null)
                	 Argonames = Argonames.toLowerCase();

                // lname
                String longEPICname = EPS_Util.getItem(inLine, 6);
                if (longEPICname != null)
                	 longEPICname = longEPICname.toLowerCase();

                // gname
                String genericEPICname = EPS_Util.getItem(inLine, 7);
                if (genericEPICname != null)
                	 genericEPICname = genericEPICname.toLowerCase();

                // units
                String units = EPS_Util.getItem(inLine, 8);

                // frmt
                String frmt = EPS_Util.getItem(inLine, 9);
                
                int numJOAnames = 0;
                if (JOAnames.length() > 1) {
                	// count the number of commas
                	numJOAnames = this.numItems(JOAnames);
                }
                	
                int numWOCEnames = 0;
                if (WOCEnames.length() > 1) {
                	numWOCEnames = this.numItems(WOCEnames);
                }
                	
                int numArgonames = 0;
                if (Argonames.length() > 1) {
                	numArgonames = this.numItems(Argonames);
                }
                
                int type = EPSConstants.EPREAL;

                Key key = new Key(id, shortEPICname, longEPICname, genericEPICname, units, frmt, type);
                
                //System.out.println(id + "\tj=" + JOAnames + "\tw=" + WOCEnames + "\ta=" + Argonames + "\t" + units);
                //System.out.println(id + "\t" + numJOAnames + "\t" + numWOCEnames + "\t" + numArgonames + "\t");
                
                // EPIC and JOA names are defined for all entries
				mEPICCodeToEPIC.put(new Integer(id), shortEPICname);
				
                // Argo and WOCE are only defined for a subset
                for (int i=0; i<numJOAnames; i++) {
                	String JOAname = getItem(JOAnames, i+1).trim();
                
	                mJOAToEPIC.put(JOAname, key);
	                mJOAToEPICCode.put(JOAname, new Integer(id));
					mEPICToJOA.put(shortEPICname, JOAname);
					mEPICCodeToJOA.put(new Integer(id), JOAname);
					
                	if (numWOCEnames > 0) {
                		for (int w=0; w<numWOCEnames; w++) {
                			String WOCEname = getItem(WOCEnames, w+1).trim();
	                		//System.out.println("mJOAToWOCE***** " + JOAname +"*" + "\t" + WOCEname);
	                		mJOAToWOCE.put(JOAname, WOCEname);
	               		}
                	}
                	
	                if (numArgonames > 0) {
                		for (int a=0; a<numArgonames; a++) {
                			String Argoname = getItem(Argonames, a+1).trim();
	                		//System.out.println("mJOAToArgo***** " + JOAname + "\t" + Argoname);
		                	mJOAToArgo.put(JOAname, Argoname);
			             }  	
	               	}
                }
                
                for (int i=0; i<numWOCEnames; i++) {
                	String WOCEname = getItem(WOCEnames, i+1).trim();
                
               		mWOCEToEPIC.put(WOCEname, key);
               		mWOCEToEPICCode.put(WOCEname, new Integer(id));
					mEPICToWOCE.put(shortEPICname, WOCEname);
					mEPICCodeToWOCE.put(new Integer(id), WOCEname);
					
                	if (numJOAnames > 0) {
                		for (int j=0; j<numJOAnames; j++) {
                			String JOAname = getItem(JOAnames, j+1).trim();
	                		//System.out.println("mWOCEToJOA***** " + WOCEname + "\t" + JOAname);
	               			mWOCEToJOA.put(WOCEname, JOAname);
	               		}
                	}
                	
	                if (numArgonames > 0) {
                		for (int a=0; a<numArgonames; a++) {
                			String Argoname = getItem(Argonames, a+1).trim();
	                		//System.out.println("mWOCEToArgo***** " + WOCEname + "\t" + Argoname);
		                	mWOCEToArgo.put(WOCEname, Argoname);
			             }  	
	               	}
                }
                
                for (int i=0; i<numArgonames; i++) {
                	String Argoname = getItem(Argonames, i+1).trim();
                	
               		mArgoToEPIC.put(Argoname, key);
	                mArgoToEPICCode.put(Argoname, new Integer(id));
					mEPICToArgo.put(shortEPICname, Argoname);
					mEPICCodeToArgo.put(new Integer(id), Argoname);
					
                	if (numJOAnames > 0) {
                		for (int j=0; j<numJOAnames; j++) {
                			String JOAname = getItem(JOAnames, j+1).trim();
	               			mArgoToJOA.put(Argoname, JOAname);
	                		//System.out.println("mArgoToJOA***** " + Argoname + "\t" + JOAname);
	               		}
                	}
					
                	if (numWOCEnames > 0) {
                		for (int w=0; w<numWOCEnames; w++) {
                			String WOCEname = getItem(WOCEnames, w+1).trim();
	                		//System.out.println("mArgoToWOCE***** " + Argoname + "\t" + WOCEname);
	                		mArgoToWOCE.put(Argoname, WOCEname);
	               		}
                	}
                }
            }
            EPSProperties.DELIMITER = oldDelim;
        }
        catch (IOException ex) {
        	throw ex;
        }
		/*Hashtable mJOAToEPIC = new Hashtable();
		Hashtable mJOAToWOCE = new Hashtable();
		Hashtable mJOAToEPICCode = new Hashtable();
		Hashtable mJOAToArgo = new Hashtable();
		
		Hashtable mWOCEToEPIC = new Hashtable();
		Hashtable mWOCEToEPICCode = new Hashtable();
		Hashtable mWOCEToJOA = new Hashtable();
		Hashtable mWOCEToArgo = new Hashtable();
		
		Hashtable mArgoToJOA = new Hashtable();
		Hashtable mArgoToEPIC = new Hashtable();
		Hashtable mArgoToWOCE = new Hashtable();
		Hashtable mArgoToEPICCode = new Hashtable();
		
		Hashtable mEPICToJOA = new Hashtable();
		Hashtable mEPICToWOCE = new Hashtable();
		Hashtable mEPICToArgo = new Hashtable();
		Hashtable mEPICCodeToWOCE = new Hashtable();
		Hashtable mEPICCodeToJOA = new Hashtable();
		Hashtable mEPICCodeToEPIC = new Hashtable();
		Hashtable mEPICCodeToArgo = new Hashtable();*/
	
        //dumpHashtable("mArgoToJOA", mArgoToJOA);
        //dumpHashtable("mJOAToEPIC", mJOAToEPIC);
        //dumpHashtable("mEPICToJOA", mEPICToJOA);
        //dumpHashtable("mEPICCodeToWOCE", mEPICCodeToWOCE);
    }
    
    public int numItems(String inString) {
    	int startPos = 0;
    	int delimPos = 0;
    	int lastPos = 0;
    	int itemCnt = 1;
    	for (int i=0;; i++) {
    		delimPos = inString.indexOf(',', startPos);
    		
    		if (delimPos == -1) {
    			// hit end of string
    			break;
    		}
    		
    		lastPos = startPos;
    		startPos = delimPos + 1;
    		itemCnt++;
    	}
    	return itemCnt;
    }
    
    public String getItem(String inString, int item) {
    	if (inString == null || inString.length() == 0)
    		return null;
    	if (item-1 > numItems(inString))
    		return null;
    	if (inString.indexOf(',') < 0)
    		return inString;
    	
    	int startPos = 0;
    	int delimPos = 0;
    	int lastPos = 0;
    	for (int i=0; i<item; i++) {
    		delimPos = inString.indexOf(',', startPos);
    		
    		if (delimPos == -1) {
    			// hit end of string
    			lastPos = startPos;
    			delimPos = inString.length();
    			break;
    		}
    		
    		lastPos = startPos;
    		startPos = delimPos + 1;
    	}
    		
    	String outStr = inString.substring(lastPos, delimPos);
    	return outStr;
    }
    
	public String translate(String fromLexicon, int toLexicon, String param) {
	 	return this.translate(fromLexicon, toLexicon, param, -99);
	}
    
	public String translate(int toLexicon, String param) {
	 	return this.translate(toLexicon, param, -99);
	}
	 
    public String translate(String fromLexicon, int toLexicon, String param, int ec) {
    	int fromLex;
    	if (fromLexicon.equalsIgnoreCase("JOA"))
    		fromLex = JOA_LEXICON;
    	else if (fromLexicon.equalsIgnoreCase("EPIC"))
    		fromLex = EPIC_LEXICON;
    	else if (fromLexicon.equalsIgnoreCase("WOCE"))
    		fromLex = WOCE_LEXICON;
    	else if (fromLexicon.equalsIgnoreCase("Argo"))
    		fromLex = ARGO_LEXICON;
    	else
    		return param;
    	return this.translate(fromLex, toLexicon, param, ec);
    
    }
    
    public String translate(int toLexicon, String iparam, int ec) {
    	String tstString = iparam.toLowerCase();
    	if (DEBUG) {
    		System.out.println("translating " + tstString + " missing lex to lex = "+ toLexicon + " epic code = " + ec);
    	}
    	// unknown input lexicon
    	// attempt a match in the JOA lexicon
    	Key  key = (Key)mJOAToEPIC.get(tstString.trim());
    	if (key != null) {
	    	if (DEBUG) {
	    		System.out.println("found " + key + " in JOA Lexicon");
	    	}
    		return this.translate(JOA_LEXICON, toLexicon, tstString, ec);
    	}
    		
    	// attempt a match in the EPIC lexicon
    	String result = (String)mEPICToJOA.get(tstString);
    	if (result != null) {
	    	if (DEBUG) {
	    		System.out.println("found " + result + " in EPIC Lexicon");
	    	}
    		return this.translate(EPIC_LEXICON, toLexicon, tstString, ec);
    	}
    		
    	// attempt a match in the WOCE lexicon
    	result = (String)mWOCEToJOA.get(tstString);					
    	if (result != null) {
	    	if (DEBUG) {
	    		System.out.println("found " + result + " in WOCE Lexicon");
	    	}
    		return this.translate(WOCE_LEXICON, toLexicon, tstString, ec);
    	}
    		
    	// attempt a match in the Argo lexicon
    	result = (String)mArgoToJOA.get(tstString);
    	if (result != null) {
	    	if (DEBUG) {
	    		System.out.println("found " + result + " in ARGO Lexicon");
	    	}
    		return this.translate(ARGO_LEXICON, toLexicon, tstString, ec);
    	}
    	
    	// check to see in an EPIC code is embedded in the title (like Dapper CDP database)
		if (tstString.indexOf('_') > 0) {
			// check and see if epic code is embedded to var title
			String tempVar = tstString.trim();
			try {
				String ss = tempVar.substring(tempVar.indexOf('_') + 1, tempVar.length());
				Integer epicVarCode = Integer.valueOf(ss);
		    	if (DEBUG) {
		    		System.out.println("found embedded EPIC code = " + epicVarCode);
		    	}
				
    			return this.translate(EPIC_LEXICON, toLexicon, (String)mEPICCodeToEPIC.get(epicVarCode), ec);
			}
			catch (Exception ex) {
				// just eat the exception
			}
		}
    		
    	// couldn't find a match--try to find some JOA equivalent
    	if (DEBUG) {
    		System.out.println("No Match found in Lexicon " + result + " try to find a JOA equivalent");
    	}
    	result = paramNameToJOAName(tstString.toUpperCase());
    	if (result != null) {
	    	if (DEBUG) {
	    		System.out.println("found JOA equiv = " + result);
	    	}
    		if (toLexicon == JOA_LEXICON) {
    			return result;
    		}
    		return this.translate(JOA_LEXICON, toLexicon, tstString, ec);
    	}
    	
    	if (DEBUG) {
    		System.out.println("Translation failed!");
    	}
    	// failed--just return the original parameter
    	return tstString;
    }
    
    public String translate(int fromLexicon, int toLexicon, String iparam, int epicCode) {
    	String tstString = iparam.toLowerCase();
    	String translatedName = null;
    	
    	if (fromLexicon == toLexicon) {
    		translatedName =  tstString;
    		return translatedName;
    	}
    	if (DEBUG) {
    		System.out.println("translating " + tstString + " lex = " + fromLexicon + " to lex = "+ toLexicon + " epic code = " + epicCode);
    	}
    		
		if (fromLexicon == JOA_LEXICON && toLexicon == EPIC_LEXICON) {
			Key  key = (Key)mJOAToEPIC.get(tstString.trim());
			
			if (key != null)
				translatedName = key.getSname();
		}
		else if (fromLexicon == JOA_LEXICON && toLexicon == WOCE_LEXICON) {
			translatedName = (String)mJOAToWOCE.get(tstString);
		}
		else if (fromLexicon == JOA_LEXICON && toLexicon == ARGO_LEXICON) {
			translatedName = (String)mJOAToArgo.get(tstString);
		}
		else if (fromLexicon == WOCE_LEXICON && toLexicon == JOA_LEXICON) {
			translatedName = (String)mWOCEToJOA.get(tstString);
		}
		else if (fromLexicon == WOCE_LEXICON && toLexicon == EPIC_LEXICON) {
			Key key = (Key)mWOCEToEPIC.get(tstString);
			if (key != null)
				translatedName = key.getSname();
		}
		else if (fromLexicon == WOCE_LEXICON && toLexicon == ARGO_LEXICON) {
			translatedName = (String)mWOCEToArgo.get(tstString);
		}
		else if (fromLexicon == EPIC_LEXICON && toLexicon == JOA_LEXICON) {
			if (epicCode >= 0) {
				//epic code trumps the hashtable lookup
				String  name = (String)mEPICCodeToJOA.get(new Integer(epicCode));
				if (name != null)
					translatedName = name;
			}
			else {
				translatedName = (String)mEPICToJOA.get(tstString);
	    	}
		}
		else if (fromLexicon == EPIC_LEXICON && toLexicon == WOCE_LEXICON) {
			if (epicCode >= 0) {
				//epic code trumps the hashtable lookup
				String name = (String)mEPICCodeToWOCE.get(new Integer(epicCode));
				if (name != null)
					translatedName = name;
			}
			else {
				translatedName = (String)mEPICToWOCE.get(tstString);
	    	}
		}
		else if (fromLexicon == EPIC_LEXICON && toLexicon == ARGO_LEXICON) {
			if (epicCode >= 0) {
				//epic code trumps the hashtable lookup
				String name = (String)mEPICCodeToArgo.get(new Integer(epicCode));
				if (name != null)
					translatedName = name;
			}
			else {
				translatedName = (String)mEPICToArgo.get(tstString);
			}
		}
		else if (fromLexicon == ARGO_LEXICON && toLexicon == JOA_LEXICON) {
			translatedName = (String)mArgoToJOA.get(tstString);
		}
		else if (fromLexicon == ARGO_LEXICON && toLexicon == WOCE_LEXICON) {
			translatedName = (String)mArgoToWOCE.get(tstString);
		}
		else if (fromLexicon == ARGO_LEXICON && toLexicon == EPIC_LEXICON) {
			Key  key = (Key)mArgoToEPIC.get(tstString);
			if (key != null)
				translatedName = key.getSname();
			else
				translatedName = tstString;
		}
		return translatedName;
    }
    
    public int getEPICCode(int fromLexicon, String iparam) {
    	String tstString = iparam.toLowerCase();
		if (fromLexicon == JOA_LEXICON) {
			Key  key = (Key)mJOAToEPIC.get(tstString);
			return key.getID();
		}
		else if (fromLexicon == WOCE_LEXICON) {
			Key  key = (Key)mWOCEToEPIC.get(tstString);
			return key.getID();
		}
		else if (fromLexicon == ARGO_LEXICON) {
			Key  key = (Key)mArgoToEPIC.get(tstString);
			return key.getID();
		}
		return 0;
    }
	
	public static int getCanonicalDepth(int lexicon, String[] varNames, boolean translateOnFailure) {
		// returns the position in the input array of variable names of
		// the canonical depth in the chosen lexicon
		String testVar;
		for (int i=0; i<varNames.length; i++) {
			testVar = varNames[i].trim();
			if (lexicon == EPIC_LEXICON && testVar.equalsIgnoreCase(mEPICCanonicalDepth))
				return i;
			else if (lexicon == JOA_LEXICON && testVar.equalsIgnoreCase(mJOACanonicalDepth))
				return i;
			else if (lexicon == WOCE_LEXICON && testVar.equalsIgnoreCase(mWOCECanonicalDepth))
				return i;
			else if (lexicon == ARGO_LEXICON && testVar.equalsIgnoreCase(mArgoCanonicalDepth))
				return i;
			else if (lexicon == ARGO_LEXICON && testVar.equalsIgnoreCase(mArgoCanonicalDepth2))
				return i;
		}
		
		// get here if the none of the variable names matched a canonical depth
		if (translateOnFailure) {
			for (int i=0; i<varNames.length; i++) {
				testVar = varNames[i].trim();
				
				if (testVar.indexOf("_") > 0)
					testVar = testVar.substring(0, testVar.indexOf("_"));
					
				// try to translate this to a JOA name
				String joaVar = paramNameToJOAName(testVar);
				if (joaVar != null) {
					if (joaVar.trim().equalsIgnoreCase(mJOACanonicalDepth))
						return i;
				}
			}
		}
		return -99;
	}
	
	public static EPSVariable getCanonicalTemperature(int lexicon, ArrayList varNames, boolean translateOnFailure) {
		// returns the position in the array of variable names of
		// the canonical temperature in the chosen lexicon
		Iterator itor = varNames.iterator();
		while (itor.hasNext()) {
			EPSVariable epv = (EPSVariable)itor.next();
			String testVar = epv.getOname().trim();
			if (lexicon == EPIC_LEXICON) {
				// get the epic_code attribute
				int epicVarCode = epv.getIntegerAttributeValue("epic_code");
				if (epicVarCode != 0) {
					if (epicVarCode == mEPICCodeCanonicalTemperature) {
						return epv;
					}
				}
				else if (testVar.equalsIgnoreCase(mEPICCanonicalTemperature)) {
					return epv;
				}
			}
			else if (lexicon == JOA_LEXICON && testVar.equalsIgnoreCase(mJOACanonicalTemperature)) {
				return epv;
			}
			else if (lexicon == WOCE_LEXICON && testVar.equalsIgnoreCase(mWOCECanonicalTemperature)) {
				return epv;
			}
			else if (lexicon == ARGO_LEXICON && testVar.equalsIgnoreCase(mArgoCanonicalTemperature)) {
				return epv;
			}
			else if (lexicon == ARGO_LEXICON && testVar.equalsIgnoreCase(mArgoCanonicalTemperature2)) {
				return epv;
			}	
		}

		// get here if the none of the variable names matched a canonical Temperature
		if (translateOnFailure) {
			itor = varNames.iterator();
			while (itor.hasNext()) {
				EPSVariable epv = (EPSVariable)itor.next();
				String testVar = epv.getOname().trim();
				
				if (testVar.indexOf("_") > 0) {
					// does this contain an embedded epic code?
			        try {
						int epicVarCode = Integer.valueOf(testVar.substring(testVar.lastIndexOf("_") + 1, testVar.length())).intValue();
						testVar = testVar.substring(0, testVar.lastIndexOf("_"));

						if (DEBUG) {
							System.out.println("isolated testVar = " + testVar + " has epic code = " + epicVarCode);
						}
			          
						if (epicVarCode != 0) {
							if (epicVarCode == mEPICCodeCanonicalTemperature)
								return epv;
						}
						else if (testVar.equalsIgnoreCase(mEPICCanonicalTemperature)) {
							return epv;
				        }
				    }
			        catch (Exception ex) {
			          	// eat exception...no epic code on this variable name--just a variable with an underscore in it
						// try to translate this to a JOA name
						String joaVar = paramNameToJOAName(testVar);
						if (joaVar != null) {
							if (joaVar.trim().equalsIgnoreCase(mJOACanonicalTemperature))
								return epv;
						}
			        }
				}
				else {
					// try to translate this to a JOA name
					String joaVar = paramNameToJOAName(testVar);
					if (joaVar != null) {
						if (joaVar.trim().equalsIgnoreCase(mJOACanonicalTemperature)) {
					          if (DEBUG) {
					            System.out.println("found = " + joaVar + " as canonical temperature");
					          }
							return epv;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static EPSVariable getCanonicalSalinity(int lexicon, ArrayList varNames, boolean bottleSalt, boolean translateOnFailure) {
		// returns the position in the array of variable names of
		// the canonical salinity in the chosen lexicon
		if (lexicon >= 0) {
			String testVar;
			Iterator itor = varNames.iterator();
			while (itor.hasNext()) {
				EPSVariable epv = (EPSVariable)itor.next();
				testVar = epv.getOname().trim();
				if (lexicon == EPIC_LEXICON && bottleSalt) {
					// get the epic_code attribute
					int epicVarCode = epv.getIntegerAttributeValue("epic_code");
					if (epicVarCode != 0) {
						if (epicVarCode == mEPICCodeCanonicalBottleSalinity)
							return epv;
					}
					else if (testVar.equalsIgnoreCase(mEPICCanonicalBottleSalinity)) {
						return epv;
					}
				}
				else if (lexicon == EPIC_LEXICON && !bottleSalt) {
					// get the epic_code attribute
					int epicVarCode = epv.getIntegerAttributeValue("epic_code");
					if (epicVarCode != 0) {
						if (epicVarCode == mEPICCodeCanonicalCTDSalinity)
							return epv;
					}
					else if (testVar.equalsIgnoreCase(mEPICCanonicalCTDSalinity)) {
						return epv;
					}
				}
				else if (lexicon == JOA_LEXICON && bottleSalt && testVar.equalsIgnoreCase(mJOACanonicalBottleSalinity))
					return epv;
				else if (lexicon == JOA_LEXICON && !bottleSalt && testVar.equalsIgnoreCase(mJOACanonicalCTDSalinity))
					return epv;
				else if (lexicon == WOCE_LEXICON && bottleSalt && testVar.equalsIgnoreCase(mWOCECanonicalBottleSalinity))
					return epv;
				else if (lexicon == WOCE_LEXICON && !bottleSalt && testVar.equalsIgnoreCase(mWOCECanonicalCTDSalinity))
					return epv;
				else if (lexicon == ARGO_LEXICON && testVar.equalsIgnoreCase(mArgoCanonicalCTDSalinity))
					return epv;
				else if (lexicon == ARGO_LEXICON && testVar.equalsIgnoreCase(mArgoCanonicalCTDSalinity2))
					return epv;
			}
		}
		
		// get here if the none of the variable names matched a canonical salinity
		if (translateOnFailure) {
			Iterator itor = varNames.iterator();
			while (itor.hasNext()) {
				EPSVariable epv = (EPSVariable)itor.next();
				String testVar = epv.getOname().trim();
				
				if (testVar.indexOf("_") > 0) {
					// does this contain an embedded epic code?
			        try {
						int epicVarCode = Integer.valueOf(testVar.substring(testVar.lastIndexOf("_") + 1, testVar.length())).intValue();
						testVar = testVar.substring(0, testVar.lastIndexOf("_"));

						if (DEBUG) {
							System.out.println("isolated testVar = " + testVar + " has epic code = " + epicVarCode);
						}
			          
						if (epicVarCode != 0) {
							if (epicVarCode == mEPICCodeCanonicalCTDSalinity)
								return epv;
						}
						else if (testVar.equalsIgnoreCase(mEPICCanonicalCTDSalinity)) {
							return epv;
				        }
				    }
			        catch (Exception ex) {
			          	// eat exception...no epic code on this variable name--just a variable with an underscore in it
						// try to translate this to a JOA name
						String joaVar = paramNameToJOAName(testVar);
						if (joaVar != null) {
							if (joaVar.trim().equalsIgnoreCase(mJOACanonicalBottleSalinity) && bottleSalt)
								return epv;
							if (joaVar.trim().equalsIgnoreCase(mJOACanonicalCTDSalinity) && !bottleSalt)
								return epv;
						}
			        }
				}
				else {
					// try to translate this to a JOA name
					String joaVar = paramNameToJOAName(testVar);
					if (joaVar != null) {
						if (joaVar.trim().equalsIgnoreCase(mJOACanonicalBottleSalinity) && bottleSalt) {
					          if (DEBUG) {
					            System.out.println("found = " + joaVar + " as canonical bottle salinity");
					          }
							return epv;
						}
						if (joaVar.trim().equalsIgnoreCase(mJOACanonicalCTDSalinity) && !bottleSalt) {
					          if (DEBUG) {
					            System.out.println("found = " + joaVar + " as canonical CTD salinity");
					          }
							return epv;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static EPSVariable getCanonicalOxygen(int lexicon, ArrayList varNames, boolean bottleO2, boolean translateOnFailure) {
		// returns the position in the array of variable names of
		// the canonical Oxygen in the chosen lexicon
		Iterator itor = varNames.iterator();
		while (itor.hasNext()) {
			EPSVariable epv = (EPSVariable)itor.next();
			String testVar = epv.getOname().trim();
			if (lexicon == EPIC_LEXICON && bottleO2) {
				// get the epic_code attribute
				int epicVarCode = epv.getIntegerAttributeValue("epic_code");
				if (epicVarCode != 0) {
					if (epicVarCode == mEPICCodeCanonicalBottleOxygen)
						return epv;
				}
				else if (testVar.equalsIgnoreCase(mEPICCanonicalBottleOxygen)) {
					return epv;
				}
			}
			else if (lexicon == EPIC_LEXICON && !bottleO2) {
				// get the epic_code attribute
				int epicVarCode = epv.getIntegerAttributeValue("epic_code");
				if (epicVarCode != 0) {
					if (epicVarCode == mEPICCodeCanonicalCTDOxygen)
						return epv;
				}
				else if (testVar.equalsIgnoreCase(mEPICCanonicalCTDOxygen)) {
					return epv;
				}
			}
			else if (lexicon == JOA_LEXICON && bottleO2 && testVar.equalsIgnoreCase(mJOACanonicalBottleOxygen))
				return epv;
			else if (lexicon == JOA_LEXICON && !bottleO2 && testVar.equalsIgnoreCase(mJOACanonicalCTDOxygen))
				return epv;
			else if (lexicon == WOCE_LEXICON && bottleO2 && testVar.equalsIgnoreCase(mWOCECanonicalBottleOxygen))
				return epv;
			else if (lexicon == WOCE_LEXICON && !bottleO2 && testVar.equalsIgnoreCase(mWOCECanonicalCTDOxygen))
				return epv;
			else if (lexicon == ARGO_LEXICON && testVar.equalsIgnoreCase(mArgoCanonicalCTDOxygen))
				return epv;
			else if (lexicon == ARGO_LEXICON && testVar.equalsIgnoreCase(mArgoCanonicalCTDOxygen2))
				return epv;
		}
		
		// get here if the none of the variable names matched a canonical alinity
		if (translateOnFailure) {
			itor = varNames.iterator();
			while (itor.hasNext()) {
				EPSVariable epv = (EPSVariable)itor.next();
				 String testVar = epv.getOname().trim();
				
				if (testVar.indexOf("_") > 0) {
					// does this contain an embedded epic code?
			        try {
						int epicVarCode = Integer.valueOf(testVar.substring(testVar.lastIndexOf("_") + 1, testVar.length())).intValue();
						testVar = testVar.substring(0, testVar.lastIndexOf("_"));

						if (DEBUG) {
							System.out.println("isolated testVar = " + testVar + " has epic code = " + epicVarCode);
						}
			          
						if (epicVarCode != 0) {
							if (epicVarCode == mEPICCodeCanonicalBottleOxygen)
								return epv;
						}
						else if (testVar.equalsIgnoreCase(mEPICCanonicalBottleOxygen)) {
							return epv;
				        }
				    }
			        catch (Exception ex) {
			          	// eat exception...no epic code on this variable name--just a variable with an underscore in it
						// try to translate this to a JOA name
						String joaVar = paramNameToJOAName(testVar);
						if (joaVar != null) {
							if (joaVar.trim().equalsIgnoreCase(mJOACanonicalBottleOxygen) && bottleO2)
								return epv;
							if (joaVar.trim().equalsIgnoreCase(mJOACanonicalCTDOxygen) && !bottleO2)
								return epv;
						}
			        }
				}
				else {
					// try to translate this to a JOA name
					String joaVar = paramNameToJOAName(testVar);
					if (joaVar != null) {
						if (joaVar.trim().equalsIgnoreCase(mJOACanonicalBottleOxygen) && bottleO2) {
					          if (DEBUG) {
					            System.out.println("found = " + joaVar + " as canonical bottle oxygen");
					          }
							return epv;
						}
						if (joaVar.trim().equalsIgnoreCase(mJOACanonicalCTDOxygen) && !bottleO2) {
					          if (DEBUG) {
					            System.out.println("found = " + joaVar + " as canonical CTD oxygen");
					          }
							return epv;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static EPSVariable getCanonicalNitrate(int lexicon, ArrayList varNames, boolean translateOnFailure) {
		// returns the position in the array of variable names of
		// the canonical nitrate in the chosen lexicon
		Iterator itor = varNames.iterator();
		while (itor.hasNext()) {
			EPSVariable epv = (EPSVariable)itor.next();
			String testVar = epv.getOname().trim();
			if (lexicon == EPIC_LEXICON) {
				// get the epic_code attribute
				int epicVarCode = epv.getIntegerAttributeValue("epic_code");
				if (epicVarCode != 0) {
					if (epicVarCode == mEPICCodeCanonicalNitrate)
						return epv;
				}
				else if (testVar.equalsIgnoreCase(mEPICCanonicalNitrate)) {
					return epv;
				}
			}
			else if (lexicon == JOA_LEXICON && testVar.equalsIgnoreCase(mJOACanonicalNitrate))
				return epv;
			else if (lexicon == WOCE_LEXICON && testVar.equalsIgnoreCase(mWOCECanonicalNitrate))
				return epv;
		}
		
		// get here if the none of the variable names matched a canonical alinity
		if (translateOnFailure) {
			itor = varNames.iterator();
			while (itor.hasNext()) {
				EPSVariable epv = (EPSVariable)itor.next();
				String testVar = epv.getOname().trim();
				
				if (testVar.indexOf("_") > 0) {
					// does this contain an embedded epic code?
			        try {
						int epicVarCode = Integer.valueOf(testVar.substring(testVar.lastIndexOf("_") + 1, testVar.length())).intValue();
						testVar = testVar.substring(0, testVar.lastIndexOf("_"));

						if (DEBUG) {
							System.out.println("isolated testVar = " + testVar + " has epic code = " + epicVarCode);
						}
			          
						if (epicVarCode != 0) {
							if (epicVarCode == mEPICCodeCanonicalNitrate)
								return epv;
						}
						else if (testVar.equalsIgnoreCase(mEPICCanonicalNitrate)) {
							return epv;
				        }
				    }
			        catch (Exception ex) {
			          	// eat exception...no epic code on this variable name--just a variable with an underscore in it
						// try to translate this to a JOA name
						String joaVar = paramNameToJOAName(testVar);
						if (joaVar != null) {
							if (joaVar.trim().equalsIgnoreCase(mJOACanonicalNitrate))
								return epv;
						}
			        }
				}
				else {
					// try to translate this to a JOA name
					String joaVar = paramNameToJOAName(testVar);
					if (joaVar != null) {
						if (joaVar.trim().equalsIgnoreCase(mJOACanonicalNitrate)) {
					          if (DEBUG) {
					            System.out.println("found = " + joaVar + " as canonical nitrate");
					          }
							return epv;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static EPSVariable getCanonicalPhosphate(int lexicon, ArrayList varNames, boolean translateOnFailure) {
		// returns the position in the array of variable names of
		// the canonical phosphate in the chosen lexicon
		Iterator itor = varNames.iterator();
		while (itor.hasNext()) {
			EPSVariable epv = (EPSVariable)itor.next();
			String testVar = epv.getOname().trim();
			if (lexicon == EPIC_LEXICON) {
				// get the epic_code attribute
				int epicVarCode = epv.getIntegerAttributeValue("epic_code");
				if (epicVarCode != 0) {
					if (epicVarCode == mEPICCodeCanonicalPhosphate)
						return epv;
				}
				else if (testVar.equalsIgnoreCase(mEPICCanonicalPhosphate)) {
					return epv;
				}
			}
			else if (lexicon == JOA_LEXICON && testVar.equalsIgnoreCase(mJOACanonicalPhosphate))
				return epv;
			else if (lexicon == WOCE_LEXICON && testVar.equalsIgnoreCase(mWOCECanonicalPhosphate))
				return epv;
		}
		
		// get here if the none of the variable names matched a canonical alinity
		if (translateOnFailure) {
			itor = varNames.iterator();
			while (itor.hasNext()) {
				EPSVariable epv = (EPSVariable)itor.next();
				String testVar = epv.getOname().trim();
				
				if (testVar.indexOf("_") > 0) {
					// does this contain an embedded epic code?
			        try {
						int epicVarCode = Integer.valueOf(testVar.substring(testVar.lastIndexOf("_") + 1, testVar.length())).intValue();
						testVar = testVar.substring(0, testVar.lastIndexOf("_"));

						if (DEBUG) {
							System.out.println("isolated testVar = " + testVar + " has epic code = " + epicVarCode);
						}
			          
						if (epicVarCode != 0) {
							if (epicVarCode == mEPICCodeCanonicalPhosphate)
								return epv;
						}
						else if (testVar.equalsIgnoreCase(mEPICCanonicalPhosphate)) {
							return epv;
				        }
				    }
			        catch (Exception ex) {
			          	// eat exception...no epic code on this variable name--just a variable with an underscore in it
						// try to translate this to a JOA name
						String joaVar = paramNameToJOAName(testVar);
						if (joaVar != null) {
							if (joaVar.trim().equalsIgnoreCase(mJOACanonicalPhosphate))
								return epv;
						}
			        }
				}
				else {
					// try to translate this to a JOA name
					String joaVar = paramNameToJOAName(testVar);
					if (joaVar != null) {
						if (joaVar.trim().equalsIgnoreCase(mJOACanonicalPhosphate)) {  
					          if (DEBUG) {
					            System.out.println("found = " + joaVar + " as canonical phosphate");
					          }
							return epv;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static boolean isVarCanonicalPres(String srcLexicon, int epicCode, EPSVariable epv) {
		return false;
	}
	
	public static boolean isVarCanonicalTemp(String srcLexicon, int epicCode, EPSVariable epv) {
		return false;
	}
	
	public static boolean isVarCanonicalSalt(String srcLexicon, int epicCode, EPSVariable epv) {
		return false;
	}
	
	public static String paramNameToJOAName(String inParam) {
		String retVal = inParam;

		String testParam = inParam.toUpperCase();
		
		// always translate pressure
		if (testParam.equalsIgnoreCase("PRES") || testParam.equalsIgnoreCase("CTDPRS") || testParam.equalsIgnoreCase("PRESSURE")) {
			retVal = new String("PRES");
		}
		if (testParam.startsWith("SIG")) {
			retVal = new String(testParam);
		}
		
		// always translate depth
		if (testParam.startsWith("DEPTH") || testParam.equalsIgnoreCase("DEPM")  || testParam.startsWith("DEP")) {
			retVal = new String("DEPTH");
		}
		
		if (testParam.startsWith("SIG")) {
			retVal = new String(testParam);
		}
		else if (testParam.equalsIgnoreCase("PO")) {
			retVal = new String(testParam);
		}
		else if (testParam.equalsIgnoreCase("NO")) {
			retVal = new String(testParam);
		}
		else if (testParam.equalsIgnoreCase("BTLNBR")) {
			retVal = new String("BTLN");
		}
		else if (testParam.equalsIgnoreCase("TEM") || testParam.equalsIgnoreCase("TEMP") || testParam.equalsIgnoreCase("TEMPERATURE") ||
			testParam.equalsIgnoreCase("T") || testParam.equalsIgnoreCase("TE") || testParam.equalsIgnoreCase("CTDTMP")) {
			retVal = new String("TEMP");
		}
		else if (testParam.equalsIgnoreCase("SAMPNO")) {
			retVal = new String("SAMP");
		}
		else if (testParam.equalsIgnoreCase("CTDSAL")) {
			retVal = new String("CTDS");
		}
		else if (testParam.equalsIgnoreCase("CTDOXY")) {
			retVal = new String("CTDO");
		}
		else if (testParam.equalsIgnoreCase("THETA")) {
			retVal = new String("WTHT");
		}
		else if (testParam.equalsIgnoreCase("NO2NO3")) {
			retVal = new String("NO2NO3");
		}
		else if (testParam.equalsIgnoreCase("Bedfort")) {
			retVal = new String("BEDFORT");
		}
		else if (testParam.startsWith("SPPT") || testParam.startsWith("BSAL") || testParam.equalsIgnoreCase("SAL") || 
			testParam.equalsIgnoreCase("S") || testParam.equalsIgnoreCase("SA") || testParam.equalsIgnoreCase("SALNTY") || testParam.equalsIgnoreCase("S0")
			|| testParam.equalsIgnoreCase("SALNITY") || testParam.equalsIgnoreCase("PSAL") || testParam.equalsIgnoreCase("SALINITY")) {
			retVal = new String("SALT");
		}
		else if (testParam.equalsIgnoreCase("OXYGEN") || testParam.equalsIgnoreCase("OXY") || testParam.equalsIgnoreCase("DOXY") ||
			testParam.startsWith("O2") || testParam.equalsIgnoreCase("BO") || testParam.equalsIgnoreCase("O") || testParam.startsWith("BOTTLE_OXYGEN")) {
			retVal = new String("O2");
		}
		else if (testParam.equalsIgnoreCase("SILICATE") || testParam.equalsIgnoreCase("SILICAT") || testParam.equalsIgnoreCase("SILI") || testParam.startsWith("SIL") || testParam.equalsIgnoreCase("SI") ||
			testParam.equalsIgnoreCase("SIO3") || testParam.equalsIgnoreCase("SIO4") || testParam.equalsIgnoreCase("SLCA")|| testParam.equalsIgnoreCase("SILCAT")) {
			retVal = new String("SIO3");
		}
		else if (testParam.equalsIgnoreCase("NO2+NO3") || testParam.equalsIgnoreCase("NO2_NO3") || testParam.equalsIgnoreCase("nitrate_plus_nitrite")) {
			retVal = new String("NO2+NO3");
		}
		else if (testParam.equalsIgnoreCase("NITRATE") || testParam.equalsIgnoreCase("NITRAT") || testParam.equalsIgnoreCase("NTRA") ||
			testParam.startsWith("NO3")) {
			retVal = new String("NO3");
		}
		else if (testParam.equalsIgnoreCase("NITRITE") || testParam.equalsIgnoreCase("NITRIT") || testParam.equalsIgnoreCase("NTRI") ||
			testParam.startsWith("NO2")) {
			retVal = new String("NO2");
		}
		else if (testParam.equalsIgnoreCase("PHOSPHATE") || testParam.equalsIgnoreCase("PHOS") || testParam.equalsIgnoreCase("PHSPHT") || testParam.startsWith("PO4")) {
			retVal = new String("PO4");
		}
		else if (testParam.equalsIgnoreCase("CFC-11") || testParam.equalsIgnoreCase("Freon_11")) {
			retVal = new String("F11");
		}
		else if (testParam.equalsIgnoreCase("CFC-12") || testParam.equalsIgnoreCase("Freon_12")) {
			retVal = new String("F12");
		}
		else if (testParam.equalsIgnoreCase("CFC-113") || testParam.equalsIgnoreCase("Freon_113")) {
			retVal = new String("F113");
		}
		else if (testParam.equalsIgnoreCase("CFC113")) {
			retVal = new String("F113");
		}
		else if (testParam.equalsIgnoreCase("CF113ER") || testParam.equalsIgnoreCase("freon_113_error") || testParam.equalsIgnoreCase("C113ER")) {
			retVal = new String("F113ER");
		}
		else if (testParam.equalsIgnoreCase("CF11ER") || testParam.equalsIgnoreCase("freon_11_error")) {
			retVal = new String("F11ER");
		}
		else if (testParam.equalsIgnoreCase("CF12ER") || testParam.equalsIgnoreCase("freon_12_error")) {
			retVal = new String("F12ER");
		}
		else if (testParam.equalsIgnoreCase("TRITIUM") || testParam.equalsIgnoreCase("TRITUM")) {
			retVal = new String("TRIT");
		}
		else if (testParam.equalsIgnoreCase("TRITER") || testParam.equalsIgnoreCase("TRITIUM_ERROR")) {
			retVal = new String("TRER");
		}
		else if (testParam.equalsIgnoreCase("HELIUM")) {
			retVal = new String("HELI");
		}
		else if (testParam.equalsIgnoreCase("HELIER") || testParam.equalsIgnoreCase("HELIUM_ERROR")) {
			retVal = new String("HEER");
		}
		else if (testParam.equalsIgnoreCase("DELHE") || testParam.equalsIgnoreCase("DELTA_HELIUM")) {
			retVal = new String("DELH");
		}
		else if (testParam.equalsIgnoreCase("DELHE3") || testParam.equalsIgnoreCase("DELTA_HELIUM_3")) {
			retVal = new String("DELH3");
		}
		else if (testParam.equalsIgnoreCase("DELHER") || testParam.equalsIgnoreCase("DELTA_HELIUM_ERROR")) {
			retVal = new String("DH3E");
		}
		else if (testParam.equalsIgnoreCase("O18O16")) {
			retVal = new String("O18O16");
		}
		else if (testParam.equalsIgnoreCase("DELC14") || testParam.equalsIgnoreCase("carbon_14")) {
			retVal = new String("C14");
		}
		else if (testParam.equalsIgnoreCase("FCO2") || testParam.equalsIgnoreCase("FCO2TMP") || testParam.equalsIgnoreCase("fugacity_co2_temperature")) {
			retVal = new String("FCO2");
		}
		else if (testParam.equalsIgnoreCase("CCL4") || testParam.equalsIgnoreCase("CARBON_TETRACHLORIDE")) {
			retVal = new String("CCL4");
		}
		else if (testParam.equalsIgnoreCase("CCL4ER") || testParam.equalsIgnoreCase("CARBON_TETRACHLORIDE_ERROR")) {
			retVal = new String("CCL4ER");
		}
		else if (testParam.equalsIgnoreCase("C14ERR")) {
			retVal = new String("C14E");
		}
		else if (testParam.equalsIgnoreCase("TCARBN") || testParam.equalsIgnoreCase("total_carbon") || testParam.equalsIgnoreCase("total_co2")) {
			retVal = new String("TCO2");
		}
		else if (testParam.equalsIgnoreCase("TCO2TMP") || testParam.equalsIgnoreCase("total_co2_temperature")) {
			retVal = new String("TCO2TMP");
		}
		else if (testParam.equalsIgnoreCase("ALKALI") || testParam.equalsIgnoreCase("TOTAL_ALKALINITY")) {
			retVal = new String("ALKI");
		}
		else if (testParam.equalsIgnoreCase("PCO2TMP")|| testParam.equalsIgnoreCase("partial_co2_temperature")) {
			retVal = new String("PCOT");
		}
		else if (testParam.equalsIgnoreCase("PCO2") || testParam.equalsIgnoreCase("partial_pressure_of_co2")) {
			retVal = new String("PCO2");
		}
		else if (testParam.equalsIgnoreCase("PH") || testParam.equalsIgnoreCase("PHPH")) {
			retVal = new String("PH");
		}
		else if (testParam.equalsIgnoreCase("PHTEMP")) {
			retVal = new String("PHTEMP");
		}
		else if (testParam.equalsIgnoreCase("NH4") || testParam.equalsIgnoreCase("ammonium") || testParam.equalsIgnoreCase("AMON")) {
			retVal = new String("NH4");
		}
		else if (testParam.equalsIgnoreCase("BARIUM")) {
			retVal = new String("BARI");
		}
		else if (testParam.equalsIgnoreCase("DELC13") || testParam.equalsIgnoreCase("carbon_13")) {
			retVal = new String("C13");
		}
		else if (testParam.equalsIgnoreCase("C113ERR") || testParam.equalsIgnoreCase("FREON_113_ERROR")) {
			retVal = new String("C113E");
		}
		else if (testParam.equalsIgnoreCase("C13ERR") || testParam.equalsIgnoreCase("CARBON_13_ERROR")) {
			retVal = new String("C13E");
		}
		else if (testParam.equalsIgnoreCase("KR-85")|| testParam.equalsIgnoreCase("85_krypton")) {
			retVal = new String("KR85");
		}
		else if (testParam.equalsIgnoreCase("KR85ERR")) {
			retVal = new String("KRER");
		}
		else if (testParam.equalsIgnoreCase("ARGON")) {
			retVal = new String("ARGO");
		}
		else if (testParam.equalsIgnoreCase("ARGERR") || testParam.equalsIgnoreCase("ARGON_ERROR")) {
			retVal = new String("ARGE");
		}
		else if (testParam.equalsIgnoreCase("AR-39") || testParam.equalsIgnoreCase("39_Argon")) {
			retVal = new String("AR39");
		}
		else if (testParam.equalsIgnoreCase("AR39ER")) {
			retVal = new String("ARER");
		}
		else if (testParam.equalsIgnoreCase("NEON")) {
			retVal = new String("NEON");
		}
		else if (testParam.equalsIgnoreCase("NEONER") || testParam.equalsIgnoreCase("NEON_ERROR")) {
			retVal = new String("NEONEER");
		}
		else if (testParam.equalsIgnoreCase("RA-228") || testParam.equalsIgnoreCase("228_radium")) {
			retVal = new String("R228");
		}
		else if (testParam.equalsIgnoreCase("RA228")) {
			retVal = new String("R228");
		}
		else if (testParam.equalsIgnoreCase("R228ER")) {
			retVal = new String("R8ER");
		}
		else if (testParam.equalsIgnoreCase("RA-226") || testParam.equalsIgnoreCase("226_radium")) {
			retVal = new String("R226");
		}
		else if (testParam.equalsIgnoreCase("RA226")) {
			retVal = new String("R226");
		}
		else if (testParam.equalsIgnoreCase("R226ER")) {
			retVal = new String("R6ER");
		}
		else if (testParam.equalsIgnoreCase("O18/16") || testParam.equalsIgnoreCase("O18/O16") || testParam.equalsIgnoreCase("oxy18_oxy16")) {
			retVal = new String("O18/O16");
		}
		else if (testParam.equalsIgnoreCase("O16/O16") || testParam.equalsIgnoreCase("o16_o16")) {
			retVal = new String("O16/O16");
		}
		else if (testParam.equalsIgnoreCase("OXYNIT")) {
			retVal = new String("OXYNIT");
		}
		else if (testParam.equalsIgnoreCase("REVPRS") || testParam.equalsIgnoreCase("REVERSING_THERMOMETER_PRESSURE")) {
			retVal = new String("PREV");
		}
		else if (testParam.equalsIgnoreCase("REVTMP") || testParam.equalsIgnoreCase("REVERSING_THERMOMETER_TEMPERATURE")) {
			retVal = new String("TREV");
		}
		else if (testParam.equalsIgnoreCase("SR-90")) {
			retVal = new String("SR90");
		}
		else if (testParam.equalsIgnoreCase("SR90")) {
			retVal = new String("SR90");
		}
		else if (testParam.equalsIgnoreCase("CS-137") || testParam.equalsIgnoreCase("137_cesium")) {
			retVal = new String("C137");
		}
		else if (testParam.equalsIgnoreCase("CS137")) {
			retVal = new String("C137");
		}
		else if (testParam.equalsIgnoreCase("IODATE")) {
			retVal = new String("IDAT");
		}
		else if (testParam.equalsIgnoreCase("IODIDE")) {
			retVal = new String("IDID");
		}
		else if (testParam.equalsIgnoreCase("CH4") || testParam.equalsIgnoreCase("METHANE")|| testParam.equalsIgnoreCase("METHAN")) {
			retVal = new String("CH4");
		}
		else if (testParam.equalsIgnoreCase("DON")|| testParam.equalsIgnoreCase("nitrogen_dissolved_organic")) {
			retVal = new String("DON");
		}
		else if (testParam.equalsIgnoreCase("N20")) {
			retVal = new String("N20");
		}
		else if (testParam.equalsIgnoreCase("CHLORA") || testParam.equalsIgnoreCase("CHLA") || testParam.equalsIgnoreCase("chlorophyl_a")  || testParam.equalsIgnoreCase("CHPL")) {
			retVal = new String("CHLA");
		}
		else if (testParam.equalsIgnoreCase("CHLOROPHYL") || testParam.equalsIgnoreCase("CHLOROPHYLL")) {
			retVal = new String("CHPL");
		}
		else if (testParam.equalsIgnoreCase("PPHYTN") || testParam.equalsIgnoreCase("phaeophytin") || testParam.equalsIgnoreCase("PHAE")) {
			retVal = new String("PPHYTN");
		}
		else if (testParam.equalsIgnoreCase("POC") || testParam.equalsIgnoreCase("patriculate_organic_carbon") || testParam.equalsIgnoreCase("POCA")) {
			retVal = new String("POC");
		}
		else if (testParam.equalsIgnoreCase("PON") || testParam.equalsIgnoreCase("patriculate_organic_nitrogen")) {
			retVal = new String("PON");
		}
		else if (testParam.equalsIgnoreCase("BACT")) {
			retVal = new String("BACT");
		}
		else if (testParam.equalsIgnoreCase("DOC") || testParam.equalsIgnoreCase("dissolved_organic_carbon") || testParam.equalsIgnoreCase("DOCA")) {
			retVal = new String("DOC");
		}
		else if (testParam.equalsIgnoreCase("COMON") || testParam.equalsIgnoreCase("carbon_monoxide")) {
			retVal = new String("CO");
		}
		else if (testParam.equalsIgnoreCase("CH3CCL3")) {
			retVal = new String("CHCL");
		}
		else if (testParam.equalsIgnoreCase("CALCITE_SATURATION")) {
			retVal = new String("CALC_SAT");
		}
		else if (testParam.equalsIgnoreCase("ARAGONITE_SATURATION")) {
			retVal = new String("ARAG_SAT");
		}
		else if (testParam.equalsIgnoreCase("CTDRAW")|| testParam.equalsIgnoreCase("ctd_raw")) {
			retVal = new String("CTDRAW");
		}
		else if (testParam.equalsIgnoreCase("AZOTE")) {
			retVal = new String("AZOTE");
		}
		else if (testParam.equalsIgnoreCase("F113ER")) {
			retVal = new String("Freon 113 Error");
		}
		else if (testParam.equalsIgnoreCase("F12ER")) {
			retVal = new String("Freon 12 Error");
		}
		else if (testParam.equalsIgnoreCase("F11ER")) {
			retVal = new String("Freon 11 Error");
		}
		else if (testParam.equalsIgnoreCase("N2O")|| testParam.equalsIgnoreCase("nitrous_oxide")) {
			retVal = new String("N2O");
		}
		else if (testParam.equalsIgnoreCase("PHAEO")) {
			retVal = new String("PHAEO");
		}
		if (DEBUG) {
			System.out.println("paramNameToJOAName translated " + testParam + " to " + retVal);
		}
		return retVal;
	}
	
	public static String paramNameToJOAUnits(boolean isWoce, String inParam) {
		String testParam = inParam.toUpperCase();
		
		if (testParam.equalsIgnoreCase("PRES")) {
			return new String("db");
		}
		else if (testParam.equalsIgnoreCase("TEMP")) {
			return new String("deg C");
		}
		else if (testParam.equalsIgnoreCase("SALT")) {
			return new String("psu");
		}
		else if (testParam.equalsIgnoreCase("CTDS")) {
			return new String("psu");
		}
		else if (testParam.equalsIgnoreCase("CTDO") && isWoce) {
			return new String("mol/kg");
		}
		else if (testParam.equalsIgnoreCase("CTDO")) {
			return new String("um/kg");
		}
		else if (testParam.equalsIgnoreCase("O2") && isWoce) {
			return new String("mol/kg");
		}
		else if (testParam.equalsIgnoreCase("O2")) {
			return new String("ml/l");
		}
		else if (testParam.equalsIgnoreCase("THTA")) {
			return new String("deg C");
		}
		else if (testParam.equalsIgnoreCase("SIO3")) {
			return new String("um/l");
		}
		else if (testParam.equalsIgnoreCase("NO3")) {
			return new String("um/l");
		}
		else if (testParam.equalsIgnoreCase("NO2")) {
			return new String("um/l");
		}
		else if (testParam.equalsIgnoreCase("PO4")) {
			return new String("um/l");
		}
		else if (testParam.equalsIgnoreCase("SIG0")) {
			return new String("kg/m^3");
		}
		else if (testParam.equalsIgnoreCase("SIG1")) {
			return new String("kg/m^3");
		}
		else if (testParam.equalsIgnoreCase("SIG2")) {
			return new String("kg/m^3");
		}
		else if (testParam.equalsIgnoreCase("SIG3")) {
			return new String("kg/m^3");
		}
		else if (testParam.equalsIgnoreCase("SIG4")) {
			return new String("kg/m^3");
		}
		else if (testParam.equalsIgnoreCase("GAMMA")) {
			return new String("kg/m^3");
		}
		else if (testParam.equalsIgnoreCase("AOU")) {
			return new String("um/kg");
		}
		else if (testParam.equalsIgnoreCase("O2% ")) {
			return new String("none");
		}
		else if (testParam.equalsIgnoreCase("NO")) {
			return new String("um/kg");
		}
		else if (testParam.equalsIgnoreCase("PO")) {
			return new String("um/kg");
		}
		else if (testParam.equalsIgnoreCase("SPCY")) {
			return new String("none");
		}
		else if (testParam.equalsIgnoreCase("SVAN")) {
			return new String("m^3/kg");
		}
		else if (testParam.equalsIgnoreCase("SVEL")) {
			return new String("m/s");
		}
		else if (testParam.equalsIgnoreCase("GPOT")) {
			return new String("J/m");
		}
		else if (testParam.equalsIgnoreCase("ACTT")) {
			return new String("sec");
		}
		else if (testParam.equalsIgnoreCase("PE")) {
			return new String("10^6 J/m^2");
		}
		else if (testParam.equalsIgnoreCase("HEAT")) {
			return new String("10^9 J/m^2");
		}
		else if (testParam.equalsIgnoreCase("HTST")) {
			return new String("10^6 J/kg");
		}
		else if (testParam.startsWith("BV")) {
			return new String("Hz");
		}
		else if (testParam.startsWith("SB")) {
			return new String("Hz");
		}
		else if (testParam.startsWith("VT")) {
			return new String("Hz");
		}
		else if (testParam.equalsIgnoreCase("ALPH")) {
			return new String("1/degC*10^2");
		}
		else if (testParam.equalsIgnoreCase("ADRV")) {
			return new String("1/db*10^3");
		}
		else if (testParam.equalsIgnoreCase("BETA")) {
			return new String("none");
		}
		else if (testParam.equalsIgnoreCase("BDRV")) {
			return new String("1/db*10^3");
		}
		else if (testParam.equalsIgnoreCase("GAMMA")) {
			return new String("kg/m^3");
		}
		return new String("na");
	}
	
	public int getLexiconCode(String lex) {
		if (lex.equalsIgnoreCase("EPIC"))
			return EPIC_LEXICON;
		else if (lex.equalsIgnoreCase("JOA"))
			return JOA_LEXICON;
		else if (lex.equalsIgnoreCase("WOCE"))
			return WOCE_LEXICON;
		else if (lex.equalsIgnoreCase("ARGO"))
			return ARGO_LEXICON;
		return -99;
	}
	
	private void dumpHashtable(String title, Hashtable ht) {
		System.out.println(title);
		System.out.println("*************");
		Enumeration keys = ht.keys();
		while (keys.hasMoreElements() ) {
			Object key = keys.nextElement();
			Object val = ht.get(key);
			if (val instanceof Key) {
				Key ky = (Key)val;
				System.out.println(key + "* = " + ky.toString());
			}
			else {
				String s = (String)val;
				System.out.println(key + "* = " + s);
			}
		}
		System.out.println("*************");
	}
}

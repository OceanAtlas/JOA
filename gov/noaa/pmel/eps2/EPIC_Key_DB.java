package gov.noaa.pmel.eps2;

import java.util.*;
import java.io.*;

/**
 * <code>EPIC_Key_DB</code>
 * Parses and stores key information from the EPIC Key File
 *
 * @see Key
 *
 * @author oz
 * @version 1.0
 */
public class EPIC_Key_DB {
        /**
        * Storage for individual key entries
        */
        private Vector mKeys;
        /**
        * Name of the EPIC key file to use. This way, EPS can open custom (e.g., JOA) epic key files
        */
        private String mFileName;

        /**
        * Construct a new <code>EPIC_Key_DB</code>
        *
        */
        public EPIC_Key_DB(String filename) {
        	if (EPSProperties.epicKeySubDir != null)
                mFileName = new String(EPSProperties.epicKeySubDir + File.separator + filename);
            else
            	mFileName = filename;
            init();
        }

        /**
        * Initialize the key storage and start the reading process
        *
        */
        public void init() {
            try {
                mKeys = new Vector();
                initKeyDB();
            }
            catch (IOException ex) {

            }
        }

        /**
        * Install a key into the key database
        *
        * @param key Key to install
        */
        public void installKey(Key key) {
            mKeys.addElement(key);
        }

        /**
        * Read the Key database
        *
        * @file key EPIC key file
        *
        * @exception IOException An IO error occured reading the EPIC Key file.
        */
        public void readKeyDB(Reader file) throws IOException {
            try {
                parse(file);
            }
            catch (IOException ex) {
                throw(ex);
            }
        }

        /**
        * Parse the EPIC Key file
        *
        * @file key EPIC key file
        *
        * @exception IOException An IO error occured parsing the EPIC Key file.
        */
        private void parse(Reader fr) throws IOException {
                try {
                    String inLine = new String();
                    String oldDelim = EPSProperties.SDELIMITER;
                    EPSProperties.SDELIMITER = EPSProperties.SCOLON_DELIMITER;
//                      FileReader fr = new FileReader(file);
                	LineNumberReader in = new LineNumberReader(fr, 10000);
                    while (true) {
                        inLine = in.readLine();
                        if (inLine == null)
                            break;

                            // parse the line
                            // id
                            String temp = EPS_Util.getItem(inLine, 1);
                            temp = EPS_Util.trimPreceedingWhiteSpace(temp);

                            int id = 0;
                            try {
                            	id = Integer.valueOf(temp).intValue();
                            }
                            catch (NumberFormatException ex) {
                                    continue;
                            }

                            // sname
                            String sname = EPS_Util.getItem(inLine, 2);

                            // lname
                            String lname = EPS_Util.getItem(inLine, 3);

                            // gname
                            String gname = EPS_Util.getItem(inLine, 4);

                            // units
                            String units = EPS_Util.getItem(inLine, 5);

                            // frmt
                            String frmt = EPS_Util.getItem(inLine, 6);

                            int type	= EPSConstants.EPREAL;

                            Key key = new Key(id, sname, lname, gname, units, frmt, type);
                            installKey(key);

                        }
                        EPSProperties.SDELIMITER = oldDelim;
                    }
                    catch (IOException ex) {
                            throw ex;
                    }
        }

        /**
        * Initialize the Key database
        *
        * @exception IOException An IO error occured reading the EPIC Key file.
        */
        public void initKeyDB() throws IOException {
            try {
        		File f = EPS_Util.getSupportFile(mFileName);
                FileReader epkf = new FileReader(f);
                readKeyDB(epkf);
            }
            catch (IOException ex) {
              try {
                Reader epkr = new InputStreamReader(getClass().getResourceAsStream(mFileName));
                readKeyDB(epkr);
              } catch (IOException eex) {
                    throw eex;
              }
            }
        }

        /**
        * Search Key database for key matching a varcode
        *
        * @param varcde variable id
        *
        * @return Key if found in database
        *
        * @exception EpicKeyNotFoundException EPIC Key not found in database.
        */
        public Key findKey(int varcde) throws EpicKeyNotFoundException {
            for (int i=0; i<mKeys.size(); i++) {
                Key key = (Key)mKeys.elementAt(i);
                if (key.getID() == varcde) {
                        return key;
                }
            }
            throw new EpicKeyNotFoundException();
        }

        /**
        * Search Key database for ID matching a particular EPIC Key name
        *
        * @param code name of Key
        *
        * @return EPIC key code
        *
        * @exception EpicKeyNotFoundException EPIC Key not found in database.
        */
        public int findKey(String code) throws EpicKeyNotFoundException {
            for (int i=0; i<mKeys.size(); i++) {
                Key key = (Key)mKeys.elementAt(i);
                if (code.equalsIgnoreCase(key.getGname()))
                        return key.getID();
            }
            throw new EpicKeyNotFoundException();
        }

        /**
        * Search Key database for ID matching a particular EPIC Key name
        *
        * @deprecated
        * @see findKey
        *
        * @param code name of Key
        *
        * @return EPIC key code
        *
        * @exception EpicKeyNotFoundException EPIC Key not found in database.
        */
        public int findKeyIDByCode(String code) throws EpicKeyNotFoundException {
            for (int i=0; i<mKeys.size(); i++) {
                Key key = (Key)mKeys.elementAt(i);
                if (code.equalsIgnoreCase(key.getGname()))
                        return key.getID();
            }
            throw new EpicKeyNotFoundException();
        }

        public void dumpKeys() {
            for (int i=0; i<mKeys.size(); i++) {
                Key key = (Key)mKeys.elementAt(i);
                System.out.println(key.getID() + ":" + key.getGname() + ":" + key.getSname() + ":" + key.getLname() + ":" + key.getUnits() + ":" + key.getFrmt() + ":" + key.getType());
            }
    	}
}

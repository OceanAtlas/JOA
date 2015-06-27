package gov.noaa.pmel.eps2;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

public class EPS implements EPSConstants {
	public EPS() {
	}

	// Main entry point
	static public void main(String[] args)  {
		File mFile = new File("c:\\PeeCee Share\\CreatedWZipit.zip");
		ZipInputStream zis = null;
		boolean DEBUG = false;
		
	    try {
			if (DEBUG)
				System.out.println("test file = " + mFile);
				
	    	// get the number of entries in zip file
	    	//ZipFile zippo = new ZipFile(mFile);
	    	//int fcount = zippo.size();
	    	//zippo.close();
	    	
			//if (DEBUG)
			//	System.out.println("num files in zip = " + fcount);
	    	
			FileInputStream fis = new FileInputStream(mFile);
		    BufferedInputStream bis = new BufferedInputStream(fis, 10000);
		    zis = new ZipInputStream(bis);
			
			// get or create (if first time) the temporary directory
			String fpath = System.getProperty("user.dir") + File.separator + "temp" + File.separator;
			
			if (DEBUG)
				System.out.println("fpath = " + fpath);
		
			// test to see if it exists
			File tdir = new File(fpath);
			if (!tdir.exists()) {
				tdir.mkdir();
				if (DEBUG)
					System.out.println("made temp directory");
			}
			else {
				if (DEBUG)
					System.out.println("temp directory already exists");
			}

			// loop on zip entries;
		    ZipEntry zipEntry = null;
			int entryCount = 0;
			while ((zipEntry = zis.getNextEntry()) != null) {
				if (zipEntry.isDirectory())
					continue;
					
				String filename = zipEntry.getName();
				entryCount++;
				
				if (DEBUG)
					System.out.println("inflating = " + filename);
					
				// inflate a zip entry to the temporary directory
				long size = zipEntry.getSize();
				long csize = zipEntry.getCompressedSize();
				
				if (DEBUG)
					System.out.println("sizes = " + size + "," + csize);
					
				byte[] zContents = new byte[(int)size];
				int rb = 0;
				int chunk = 0;
				while (((int)size - rb) > 0) {
					chunk = zis.read(zContents, rb, (int)size - rb);
					if (chunk == -1) 
						break;
					rb += chunk;
				}
				File tFile = new File(fpath, filename);	
	    		FileOutputStream fos = new FileOutputStream(tFile);
				BufferedOutputStream out = new BufferedOutputStream(fos, 1000000);
	    		out.write(zContents);
	    		out.flush();
	    		out.close();
			}  //while
			zis.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
			try {
				zis.close();
			}
			catch (IOException ex2) {
			
			}
		}
	}
		
}



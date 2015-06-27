/*
 * $Id: MapDataInputStream.java,v 1.2 2005/06/17 18:03:35 oz Exp $
 *
 */

package javaoceanatlas.io;

import java.io.*;

public class MapDataInputStream extends DataInputStream {
	public MapDataInputStream(InputStream in) {
		super(in);
	}
	
	public double readTextDouble(int nc) throws IOException {
		byte buf[] = new byte[nc];
		this.read(buf, 0, nc);
		String bufStr = new String(buf);
		return Double.valueOf(bufStr).doubleValue();
	}
}

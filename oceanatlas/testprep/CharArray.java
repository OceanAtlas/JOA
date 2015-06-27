/**
 * 
 */
package com.oceanatlas.testprep;

import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * @author oz
 *
 */
public class CharArray {
		private char[] cstore;
		
		public CharArray(String s) {
			s.trim();
			cstore = new char[s.length()];
			
			for (int i=0; i< s.length(); i++) {
				cstore[i] = s.charAt(i);
			}
		}
		
		public boolean hasUniqueChars() {
			HashMap<Character, Character> map = new HashMap<Character, Character>();
			for (int i=0; i<cstore.length; i++) {
				map.put(cstore[i], cstore[i]);
			}
			return map.size() == cstore.length;
		}

		
		public String replaceCharWithSB(char c, String rs) {
			StringBuffer sb = new StringBuffer(new String(cstore));
			
			for (int i=0; i<sb.length(); i++) {
				if (sb.charAt(i) == c) {
					sb.replace(i, i, rs);
				}
			}
			 return new String(sb);                     
		}
		
		public String replaceCharWithStr(char c, String rs) {
			char[] rsa = new char[rs.length()];
			for (int i=0; i< rs.length(); i++) {
				rsa[i] = rs.charAt(i);
			}
			                      
			while (true) {
				if (!replace(c, rsa)) {
					break;
				}
			}
			return new String(cstore);
		}
		
		private boolean replace(char c, char[] rs) {
			for (int i=0; i<cstore.length; i++) {
				if (cstore[i] == c) {
					insertAt(i, rs);
					return true;
				}
			}
			return false;
		}
		
		private void insertAt(int i, char[] rs) {
			char[] newStore = new char[cstore.length + rs.length];
    	System.arraycopy(cstore, 0, newStore, 0, i);
    	System.arraycopy(rs, 0, newStore, i, rs.length);
    	System.arraycopy(cstore, i+1, newStore, i + rs.length-1, cstore.length - i-1);
    	cstore = newStore;
		}
		
		public String toString() {
			return new String(cstore);
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String testStr = "John R._Osb0rNe";
		CharArray ca = new CharArray(testStr);
		ca.replaceCharWithStr(' ', "%20");
		System.out.println(ca.toString());
		
		System.out.println("Replace Test = " + testStr.replace(" ", "%20"));
		
		StringTokenizer st = new StringTokenizer(testStr, " ", false);
		String outStr = new String();
		int c = 0;
		while (st.hasMoreElements()) {
			String t = st.nextToken();
			outStr += t;
			if (c++ < st.countTokens()+1)
				outStr += "%20";
		}
		System.out.println("Tokenizer Test = " + outStr);
		
		CharArray cac = new CharArray(testStr);
		System.out.println("Has Unique Test = " + cac.hasUniqueChars());
		
//		System.out.println("StringBUffer Test = " + cac.replaceCharWithSB(' ', "%20"));

	}
}

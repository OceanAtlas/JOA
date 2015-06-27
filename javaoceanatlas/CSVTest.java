/**
 * 
 */
package javaoceanatlas;

/**
 * @author oz
 *
 */

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
Copyright 2005 Bytecode Pty Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
public class CSVTest {
	private static final String TEST_FILE="/Users/oz/Desktop/NODC Data Select Examples/OSD (Ocean Station Data)/ocldb1328632067.19950.OSD.csv";
	
	public static void main(String[] args) throws IOException {
		
		CSVReader reader = new CSVReader(new FileReader(TEST_FILE));
		String [] nextLine;
		long startTime = System.currentTimeMillis();
		while ((nextLine = reader.readNext()) != null) {
//			for (int i=0; i<nextLine.length; i++) {
//				System.out.print("[" + nextLine[i] + "]");
//			}
//			System.out.println();
		}
		System.out.println(System.currentTimeMillis() - startTime);
		
		// Try writing it back out as CSV to the console
//		CSVReader reader2 = new CSVReader(new FileReader(TEST_FILE));
//		List<String[]> allElements = reader2.readAll();
//		System.out.println(System.currentTimeMillis() - startTime);
//		StringWriter sw = new StringWriter();
//		CSVWriter writer = new CSVWriter(sw);
//		writer.writeAll(allElements);
//		
//		System.out.println("\n\nGenerated CSV File:\n\n");
//		System.out.println(sw.toString());
		
		
	}
}

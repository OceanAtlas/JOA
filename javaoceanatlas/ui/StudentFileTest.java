package javaoceanatlas.ui;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JFrame;

public class StudentFileTest {
	public static void main(String[] args) {
		Integer myInt = new Integer(5);
		int ival = myInt.intValue();
		FileDialog f = new FileDialog(new JFrame(), "Add File", FileDialog.LOAD);
		f.setDirectory(System.getProperty("user.dir"));
		f.setVisible(true);
		String dir = f.getDirectory();
		f.dispose();
		
		ArrayList<Student> myStudentList = new ArrayList<Student>();
		
		if (dir != null && f.getFile() != null) {
			File inFile = new File(f.getDirectory() + f.getFile());
			long bytesInFile = inFile.length();
			try {
				long bytesRead = 0;
				LineNumberReader in = new LineNumberReader(new FileReader(inFile), 10000);
				
				do {
					// read a line
					String inLine = in.readLine();
					if (inLine == null) {
						break;
					}
					bytesRead += inLine.length();
					
					// P{arse each line of the file
					// This code has been adapted from code provided by Daniel Ortyn
					String[] stuInfo = inLine.split("\t");
					String Name = stuInfo[0]+" "+stuInfo[1];
					int id = Integer.valueOf(stuInfo[2]);
					
					// Create a new Student
					Student aNewStudent = new Student(Name, id);				
					
					int classNumber = Integer.parseInt(stuInfo[4]);
					
					// Puts the class names in an array
					String[] classInfoTemp = new String[classNumber];
					String[] classNames = stuInfo[5].split(",");				
					
					// Puts the class credits in an array
					int[] classCredits = new int[classNumber];
					classInfoTemp = stuInfo[6].split(",");							
					
					// Converts the class credits information into ints
					for (int i = 0; i<classInfoTemp.length; i++) {
						classCredits[i]=Integer.parseInt(classInfoTemp[i]);
					}
					
					// Puts the class grades in an array
					double[] classGrades = new double[classNumber];
					classInfoTemp=stuInfo[7].split(",");
					
					// Converts the class credits information into doubles
					for	(int i = 0; i<classInfoTemp.length; i++) {	
						classGrades[i]=Double.parseDouble(classInfoTemp[i]);
					}
					
					// Add class data to Student object
					aNewStudent.addClasses(classNames,classCredits,classGrades);
					
					// Your new work will go here
					myStudentList.add(aNewStudent);
					
					
				} while (bytesRead < bytesInFile);
				in.close();
				
				long startTime = System.currentTimeMillis();
				
				// Now let's sort it
				Collections.sort(myStudentList);
				
				long endTime = System.currentTimeMillis();
				System.out.println("Elapsed Time is: "  + (startTime - endTime)/1000 + " seconds");
				
				// And more code goes here
				for (Student s : myStudentList) {
					printReportCard(s);
				}

			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static double formatGPA(double gpa) {
		return ((int) (gpa * 100)) / 100.0;
	}

	public static void printReportCard(Student aStudent) {
		System.out.println("Quarter Report Card for: \t" + aStudent.getName() + ": " + aStudent.getId());
		System.out.println();
		System.out.println("Class\tCredits\tGrade");
		System.out.println("-----\t-------\t-----");
		String[] classNames = aStudent.getClasses();
		int[] classCredits = aStudent.getCredits();
		double[] classGrades = aStudent.getGrades();
		for (int i = 0; i < aStudent.getNumClasses(); i++) {
			printLineDetail(classNames[i], classCredits[i], classGrades[i]);
		}
		System.out.println("-----\t-------\t-----");
		double sumCredits = 0;
		for (int i = 0; i < classNames.length; i++) {
			sumCredits += classCredits[i];
		}
		System.out.println("Total\t" + sumCredits);
		System.out.println("GPA:\t" + formatGPA(aStudent.getGPA()));
		if (aStudent.getGPA() >= 3.75) {
			System.out.println("!!Congratulations you made Dean's List!!");
		} else if (aStudent.getGPA() >= 3.5 && aStudent.getGPA() < 3.75) {
			System.out.println("!!Congratulations you made Honor Roll!!");
		}
	}

	public static void printLineDetail(String className, int credits, double grade) {
		System.out.println(className + "\t" + credits + "\t" + grade);
	}
}


//public class StudentTest2 {
//	public static void main(String[] args) {
//		// create the data for the Student object
//		String studentName = new String("Bugsy Malone");
//		String studentID = "123456D";
//		String[] classNames = { "CHM101", "CPS141", "ENG202", "PHY301" };
//		double[] classGrades = { 3.5, 4.0, 3.5, 3.9 };
//		int[] classCredits = { 5, 5, 3, 4 };
//
//		// create a new Student object--test toString
//		Student aTestStudent = new Student(studentName, studentID);
//
//		// add Classes
//		aTestStudent.addClasses(classNames, classCredits, classGrades);
//
//		// test GPA calculation
//		printReportCard(aTestStudent);
//	}
	
	
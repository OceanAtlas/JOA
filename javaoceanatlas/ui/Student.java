package javaoceanatlas.ui;

public class Student implements Comparable<Student> {
	// Declare State Variables
	private String mName = null;
	private int mID;
	private String mGradingPeriod = null;
	private int mNumClasses;
	private String[] mClasses = null;
	private int[] mCredits = null;
	private double[] mGrades = null;
	private double mGPA = -99.0;

	// Declare the constructor Don't add a return type!!
	public Student(String name, int id) {
		mName = name;
		mID = id;
	}
	
	public Student(String fname, String lname, int id) {
		mName = fname + " " + lname;
		mID = id;
	}

	// declare public behavior
	public void addClasses(String[] crsNames, int[] crsCredits,
			double[] crsGrades) {
		mClasses = crsNames;
		mCredits = crsCredits;
		mGrades = crsGrades;
		mNumClasses = mClasses.length;
	}

	public double getGPA() {
		if (mGPA == -99) {
			computeGPA();
		}
		return mGPA;
	}

	public String getName() {
		return mName;
	}

	public int getId() {
		return mID;
	}

	public int getNumClasses() {
		return mNumClasses;
	}

	public String toString() {
		return mName + " (" + mID + ")";
	}
	
	public String[] getClasses() {
		return mClasses;
	}
	
	public int[] getCredits() {
		return mCredits;
	}
	
	public double[] getGrades() {
		return mGrades;
	}

	// private behavior
	public void computeGPA() {
		double sumCredits = 0;
		double weightedGrades = 0;
		for (int i = 0; i < mCredits.length; i++) {
			sumCredits += mCredits[i];
			weightedGrades += mGrades[i] * mCredits[i];
		}
		mGPA = weightedGrades / sumCredits;
	}
	/* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Student anotherStudent) {
  	return this.getName().compareTo(anotherStudent.getName());
  }
}
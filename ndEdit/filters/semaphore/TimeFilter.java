package ndEdit.filters.semaphore;

 import ndEdit.*;
 import ndEdit.filters.*;


/** @stereotype Strategy*/
public class TimeFilter {
  // The filtering technique depends on whether the times are each a
  // scalar, in which case a binary search on a sorted array is used, or
  // a set of ranges, which uses another technique.

  PointerCollectionGroup pc;
  double begValue;
  double endValue;
  private double[] sortedArr1;
  private int begIndex;
  private int endIndex;
  private int[] indicesArr1;
  private byte[] results;
  private double[] arr1;
  private double[] arr2;
  private int[] indicesIncluded;  // if range
  private int indCnt;
  private boolean scalar;

   public TimeFilter(double begValue, double endValue) {
      this.begValue = begValue;
      this.endValue = endValue;
   }

	public void newPointerCollection(PointerCollectionGroup pcg, byte[] results) {
		this.pc = pcg;
		int sz = pc.getSize();
		scalar = true;
		this.results = null;
		this.results = results;

		// If arr2 is null, times are a single scalar.
		arr2 = pc.getTimeArr2();

		if (arr2 != null)
			scalar = false;

		if (scalar) {
			// times are a scalar
			if (Debug.DEBUG_FILTER)
				System.out.println("Times: Scalar");
			sortedArr1 = pc.getTimeArr1Sorted();
			indicesArr1 = pc.getTimeArr1SortedIndices();

			// Initialize indices
			int i, k;
			for (i=0; i<sortedArr1.length && sortedArr1[i]<begValue; i++) {
			
				;//System.out.println(" sortedArr1[i]: " + sortedArr1[i]);
			}
			begIndex = i;

			for (i=sortedArr1.length-1; i>=0&& sortedArr1[i]>endValue; i--) {
			
				;//foobarSystem.out.println(i + " sortedArr1[i]: " + sortedArr1[i]);
			}
			endIndex = i;
			for (int j=begIndex; j<=endIndex; j++) {
				k = indicesArr1[j];
				results[k]++;
			}
			if (Debug.DEBUG_FILTER) 
				System.out.println(" Beg & End Indices are: " + begIndex + " " + endIndex);
		}
		else {
			// times are each a range
			if (Debug.DEBUG_FILTER)
				System.out.println("Time: Range of Times");
			indicesIncluded = new int[sz];
			indCnt = 0;
			arr1 = pc.getTimeArr1();
			indCnt = SearchMethods.sweepArrays(begValue, endValue, arr1, arr2, indCnt, indicesIncluded, results);
			if (Debug.DEBUG_FILTER)
				System.out.println(" indCnt is: " + indCnt);
		}

	}

   public double getBegValue() {
      return begValue;
   }

   public double getEndValue() {
      return endValue;
   }

   public void startChanged(double oldValue, double newValue) {
      begValue = newValue;
      if (scalar) {
		 // times are a single scalar.
		 begIndex = SearchMethods.bSearchStart(newValue,
						sortedArr1,
						indicesArr1,
						begIndex,
						endIndex,
						results);
      }
      else {
	 	// each latitude is a range of times.
		 indCnt = SearchMethods.sweepArrays(begValue,
						endValue,
						arr1,
						arr2,
						indCnt,
						indicesIncluded,
						results);
      }
   }

   public void stopChanged(double oldValue, double newValue) {
      endValue = newValue;
      if (scalar) {
		 // times are a single scalar.
		 endIndex = SearchMethods.bSearchEnd(newValue,
						sortedArr1,
						indicesArr1,
						endIndex,
						begIndex,
						results);
      }
      else {
		 // each latitude is a range of times.
		 indCnt = SearchMethods.sweepArrays(begValue,
						endValue,
						arr1,
						arr2,
						indCnt,
						indicesIncluded,
						results);
      }
   }
   
	public void reset() {
		for (int i=0; i<results.length; i++) {
			results[i] = 1;
		}
	}
}
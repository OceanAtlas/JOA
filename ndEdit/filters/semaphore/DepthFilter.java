package ndEdit.filters.semaphore;
 import ndEdit.*;
 import ndEdit.filters.*;


/** @stereotype Strategy*/
public class DepthFilter {

  //
  // The filtering technique depends on whether the depths are each a
  // scalar, in which case a binary search on a sorted array is used, or
  // a set of ranges, which uses another technique.

  PointerCollectionGroup pc;
  double begValue;
  double endValue;
  private double[] sortedArr1;
  private double[] sortedArr2;
  private int begIndex1;
  private int endIndex1;
  private int begIndex2;
  private int endIndex2;
  private int[] indicesArr1;
  private int[] indicesArr2;
  private byte[] results;
  private double[] arr1;
  private double[] arr2;

  private int[] indicesIncluded;  // if range
  private int indCnt;
  private boolean scalar;

   public DepthFilter(double begValue, double endValue) {
      this.begValue = begValue;
      this.endValue = endValue;
   }

	public void newPointerCollection(PointerCollectionGroup pcg, byte[] results) {
		this.pc = pcg;
		int sz = pc.getSize();
		scalar = true;
		this.results = null;
		this.results = results;

		// If arr2 is null, depths are a single scalar.
		arr2 = pc.getDepthArr2();

		if (arr2 != null)
			scalar = false;

		if (scalar) {
			// depths are a scalar
			if (Debug.DEBUG_FILTER)
				System.out.println("Depth: Scalar");
			sortedArr1 = pc.getDepthArr1Sorted();
			indicesArr1 = pc.getDepthArr1SortedIndices();

			// Initialize indices
			int i, k;
			for (i = 0; i < sortedArr1.length && sortedArr1[i] < begValue; i++);
			begIndex1 = i;

			for (i = sortedArr1.length-1; i >=0 && sortedArr1[i] > endValue; i--);
			endIndex1 = i;

			for (int j = begIndex1; j <= endIndex1; j++) {
				k = indicesArr1[j];
				results[k]++;
			}
			if (Debug.DEBUG_FILTER)
				System.out.println(" Beg & End Indices are: " + begIndex1 + " " + endIndex1);
		}
		else {
			// depths are each a range
			if (Debug.DEBUG_FILTER)
				System.out.println("Depth: Range of Depths");
			indicesIncluded = new int[sz];
			indCnt = 0;
			arr1 = pc.getDepthArr1();

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
		 // depths are a single scalar.
		 begIndex1 = SearchMethods.bSearchStart(newValue,
							sortedArr1,
							indicesArr1,
							begIndex1,
							endIndex1,
							results);
      }
      else {
		 // each depth is a range of depths.
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
		 // depths are a single scalar.
		 endIndex1 = SearchMethods.bSearchEnd(newValue,
							sortedArr1,
							indicesArr1,
							endIndex1,
							begIndex1,
							results);
      }
      else {
		 // each depth is a range of depths.
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

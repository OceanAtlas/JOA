package ndEdit.filters;

import ndEdit.*;

public class SearchMethods {
//
// Includes both Double Version and a Long Version.  Any changes made
//  need to be make in both areas.
//

// --------------------------------------------------------------
// Double Version
// --------------------------------------------------------------
   // ------------------------------------------------------------
   //
   public static int binarySearch(double[] v, double a) {
      int from = 0;
      int to = v.length - 1;
      while (from < to) {
         int mid = (from + to) / 2;
         double diff = v[mid] - a;
         if (diff == 0) /* v[mid] == a */
            return mid;
         else if (diff > 0) /* v[mid] > a */
            to = mid - 1;
         else
            from = mid + 1;
      }
      return from;
   }

   public static int binarySearch(int[] v, int a) {
      int from = 0;
      int to = v.length - 1;
      while (from < to) {
         int mid = (from + to) / 2;
         int diff = v[mid] - a;
         if (diff == 0) /* v[mid] == a */
            return mid;
         else if (diff > 0) /* v[mid] > a */
            to = mid - 1;
         else
            from = mid + 1;
      }
      return from;
   }

	public static int binarySearchEqOrGt(double[] v, double a) {
		int from = 0;
		int to = v.length - 1;
		while (from < to) {
			int mid = (from + to) / 2;
			double diff = v[mid] - a;
			if (diff == 0) { /* v[mid] == a */
				while ((mid != 0) && (v[mid] == v[mid-1]))
					mid--;  // position backwards to *first* "equals"
				return mid;
			}
			else if (diff > 0) /* v[mid] > a */
				to = mid - 1;
			else
				from = mid + 1;
		}
		while ((from != v.length) && (v[from] < a))
			from++;  // position forward to first value greater
		return from;
	}

	public static int binarySearchEqOrGt(int[] v, int a) {
		int from = 0;
		int to = v.length - 1;
		while (from < to) {
			int mid = (from + to) / 2;
			int diff = v[mid] - a;
			if (diff == 0) { /* v[mid] == a */
			while ((mid != 0) && (v[mid] == v[mid-1]))
				mid--;  // position backwards to *first* "equals"
				return mid;
			}
			else if (diff > 0) /* v[mid] > a */
				to = mid - 1;
			else
				from = mid + 1;
		}
		while ((from != v.length) && (v[from] < a))
			from++;  // position forward to first value greater
		return from;
	}


	public static int binarySearchEqOrLt(double[] v, double a) {
		int from = 0;
		int to = v.length - 1;
		while (from < to) {
			int mid = (from + to) / 2;
			double diff = v[mid] - a;
			if (diff == 0) { /* v[mid] == a */
			while ((mid != v.length-1) && (v[mid] == v[mid+1]))
				mid++; // position forward to *last* "equals"
				return mid;
			}
			else if (diff > 0) /* v[mid] > a */
				to = mid - 1;
			else
				from = mid + 1;
		}
		while ((from != -1) && (v[from] > a))
			from--;  // position backward to first value lesser
		return from;
	}

	public static int binarySearchEqOrLt(int[] v, int a) {
		int from = 0;
		int to = v.length - 1;
		while (from < to) {
			int mid = (from + to) / 2;
			int diff = v[mid] - a;
			if (diff == 0) { /* v[mid] == a */
			while ((mid != v.length-1) && (v[mid] == v[mid+1]))
				mid++; // position forward to *last* "equals"
				return mid;
			}
			else if (diff > 0) /* v[mid] > a */
				to = mid - 1;
			else
				from = mid + 1;
		}
		while ((from != -1) && (v[from] > a))
			from--;  // position backward to first value lesser
		return from;
	}

   // ------------------------------------------------------------
   //
   //  This method is used by the semaphore searches with starting values.
   //
   //   newValue = the new value to compare against.
   //   sortedValues = values, sorted.  Used in conjunction with indices that
   //      point back into the unsorted array.
   //   indicesOfSortedValues = the indices of the sorted values pointing back into
   //      the unsorted array.
   //   idxLastPass = from last filter pass, the beginning point of
   //      values included in the filter.  Needed to reverse these values
   //       where necessary.
   //   idxOfEnd = index where end values start.  Don't want to go
   //      above this or will float decrement the "results".
   //   results = increment, if this passes the filter; decrement turns
   //       of a previously incremented item.
   //       
	  
   public static int bSearchStart(double newValue,
					double[] sortedValues,
					int[] indicesOfSortedValues, 
					int idxLastPass,
					int idxOfEnd,
					byte[] results) {

		//
		// Need the binary search index to be positioned at the first (start) of a
		// group of items of equal values or at the first item of greater value.
		//
		int idxThisPass = binarySearchEqOrGt(sortedValues, newValue);


		int jj;
		if (idxThisPass > idxLastPass) {
			for (int i = idxLastPass; i < idxThisPass && i <= idxOfEnd; i++) {
				jj = indicesOfSortedValues[i];
				results[jj] -= 1;

				if (Debug.DEBUG_FILTER)
					System.out.println(" -1  from item at : " + jj);
			}
		}
		else {
			for (int i = idxThisPass; i < idxLastPass; i++) {
				if (i <= idxOfEnd) {
					jj = indicesOfSortedValues[i];
					results[jj] += 1;
					if (Debug.DEBUG_FILTER)
						System.out.println("#1 +1  to item at : " + jj);
				}
			}
		}
		return idxThisPass;
	}


    public static int bSearchEnd(double newValue,
                     double[] sortedValues,
                     int[] indicesOfSortedValues,
                     int idxLastPass,
                     int idxOfBeg,
                     byte[] results) {

         //
         // Need the binary search index to be positioned at the last (end) of a
         // group of items of equal values or at the first item of lesser value.
         //
         int idxThisPass = binarySearchEqOrLt(sortedValues, newValue + 0.01);
         if (Debug.DEBUG_FILTER)
             System.out.println(" BSearch index is: " + idxThisPass);


         if (Debug.DEBUG_FILTER)
             System.out.println(" IdxLastPass, IdxThisPass: " + idxLastPass + " " + idxThisPass);
         int jj;
         if (idxThisPass > idxLastPass) {
             for (int i = idxLastPass+1; i <= idxThisPass; i++) {
                 if (i >= idxOfBeg) {  // don't trample on the begin side
                     jj = indicesOfSortedValues[i];
                     results[jj] += 1;
                     if (Debug.DEBUG_FILTER)
                         System.out.println("#4 +1  to item at : " + jj);
                 }
                 else {
                     jj = indicesOfSortedValues[i];
                     if (Debug.DEBUG_FILTER)
                         System.out.println(" (+1)  to item at : " + jj + " DONT TRAMPLE");
                 }
                 }
             }
         else {
             for (int i = idxThisPass+1; i <= idxLastPass; i++) {
                 if (i >= idxOfBeg) {  // don't trample on the begin side
                     jj = indicesOfSortedValues[i];
                     results[jj] -= 1;							

                     if (Debug.DEBUG_FILTER)
                         System.out.println(" -1  from item at : " + jj);
                 }
             }
         }
         return idxThisPass;
     }
   // ------------------------------------------------------------
   //
   //  This method is used by the semaphore searches.
   //
   //   newValue = the new value to compare against.
   //   startValues = lower bounds of set of ranges.
   //   endValues = upper bounds of set of ranges.
   //   indCntLastPass = count of indices in indicesIncluded array from last pass.
   //   indicesIncluded = indices included in last filter.  First
   //       these are all switched back off (decremented) in the results array.
   //       Then, the array is refilled with new indices and switched
   //       on (incremented) in the results array.
   //   results = increment, if this passes the filter; decrement turns
   //       of a previously incremented item.
   //
   // To speed this up, could use the idea is that using sorted startValues,
   //  you can definately exclude all ranges where the start of the range exceeds
   //  the end of the selected range.
   //
   static public int sweepArrays(double begValue,
		double endValue,
		double[] startValues, 
		double[] endValues,
		int indCntLastPass, 
		int[] indicesIncluded, 
		byte[] results) {
		
		for (int i = 0; i < indCntLastPass; i++) {
			int jidx = indicesIncluded[i];
			results[jidx] -= 1;
		}
		int indCnt = 0;
		if (begValue > endValue)
			return indCnt;
		for (int i = 0; i < startValues.length; i++) {
			if (!(begValue > endValues[i] || endValue < startValues[i])) {
				results[i] += 1;
				indicesIncluded[indCnt++] = i;
			}
		}
		return indCnt;
	}

	static public int sweepArrays(int begValue,
		int endValue,
		int[] startValues,
		int[] endValues,
		int indCntLastPass,
		int[] indicesIncluded,
		byte[] results) {

		for (int i = 0; i < indCntLastPass; i++) {
			int jidx = indicesIncluded[i];

			results[jidx] -= 1;
		}
		int indCnt = 0;
		if (begValue > endValue)
			return indCnt;
		for (int i = 0; i < startValues.length; i++) {
			if (!(begValue > endValues[i] || endValue < startValues[i])) {
				results[i] += 1;
				indicesIncluded[indCnt++] = i;
			}
		}
		return indCnt;
	}

   public static void main(String[] args) {
      double[] aa = {0f, 10f, 20f, 30f, 40f, 50f};

      for (int i = 0; i < aa.length; i++)
	System.out.println(i + " : " + aa[i]);

      System.out.println("Search for -1: " + binarySearch(aa, -1.0f));
      System.out.println("Search for 10: " + binarySearch(aa, 10f));
      System.out.println("Search for 25: " + binarySearch(aa, 25f));
      System.out.println("Search for 50: " + binarySearch(aa, 50f));
      System.out.println("Search for 55: " + binarySearch(aa, 55f));

   }

	// Long Version
	public static int binarySearch(long[] v, long a) {
		int from = 0;
		int to = v.length - 1;
		while (from < to) {
			int mid = (from + to) / 2;
			long diff = v[mid] - a;
			if (diff == 0) /* v[mid] == a */
				return mid;
			else if (diff > 0) /* v[mid] > a */
				to = mid - 1;
			else
				from = mid + 1;
		}
		return from;
	}

	public static int binarySearchEqOrGt(long[] v, long a) {
		int from = 0;
		int to = v.length - 1;
		while (from < to) {
			int mid = (from + to) / 2;
			long diff = v[mid] - a;
			if (diff == 0) { /* v[mid] == a */
			while ((mid != 0) && (v[mid] == v[mid-1]))
				mid--;  // position backwards to *first* "equals"
				return mid;
			}
			else if (diff > 0) /* v[mid] > a */
				to = mid - 1;
			else
				from = mid + 1;
		}
		while ((from != v.length) && (v[from] < a))
			from++;  // position forward to first value greater
		return from;
	}


	public static int binarySearchEqOrLt(long[] v, long a) {
		int from = 0;
		int to = v.length - 1;
		while (from < to) {
			int mid = (from + to) / 2;
			long diff = v[mid] - a;
			if (diff == 0) { /* v[mid] == a */
				while ((mid != v.length-1) && (v[mid] == v[mid+1]))
					mid++; // position forward to *last* "equals"
				return mid;
			}
			else if (diff > 0) /* v[mid] > a */
				to = mid - 1;
			else
				from = mid + 1;
		}
		while ((from != -1) && (v[from] > a))
			from--;  // position backward to first value lesser
		return from;
	}

   // ------------------------------------------------------------
   //
   //  This method is used by the semaphore searches with starting values.
   //
   //   newValue = the new value to compare against.
   //   sortedValues = values, sorted.  Used in conjunction with indices that
   //      point back into the unsorted array.
   //   indicesOfSortedValues = the indices of the sorted values pointing back into
   //      the unsorted array.
   //   idxLastPass = from last filter pass, the beginning point of
   //      values included in the filter.  Needed to reverse these values
   //       where necessary.
   //   idxOfEnd = index where end values start.  Don't want to go 
   //      above this or will double decrement the "results".
   //   results = increment, if this passes the filter; decrement turns
   //       of a previously incremented item.
   //
   public static int bSearchStart(long newValue,
					long[] sortedValues,
					int[] indicesOfSortedValues,
					int idxLastPass,
					int idxOfEnd,
					byte[] results) {

		// Need the binary search index to be positioned at the first (start) of a
		// group of items of equal values or at the first item of greater value.
		//
		int idxThisPass = binarySearchEqOrGt(sortedValues, newValue);

		int jj;
		if (idxThisPass > idxLastPass) {
			for (int i = idxLastPass; i < idxThisPass && i <= idxOfEnd; i++) {
				jj = indicesOfSortedValues[i];
				results[jj] -= 1;

				if (Debug.DEBUG_FILTER)
					System.out.println(" -1  from item at : " + jj);
			}
		}
		else {
			for (int i = idxThisPass; i < idxLastPass; i++) {
				if (i <= idxOfEnd) {
					jj = indicesOfSortedValues[i];
					results[jj] += 1;
					if (Debug.DEBUG_FILTER)
						System.out.println("#5 +1  to item at : " + jj);
				}
			}
		}
		return idxThisPass;
	}


   public static int bSearchStart(int newValue, int[] sortedValues,
					int[] indicesOfSortedValues,
					int idxLastPass,
					int idxOfEnd,
					byte[] results) {

		// Need the binary search index to be positioned at the first (start) of a
		// group of items of equal values or at the first item of greater value.
		int idxThisPass = binarySearchEqOrGt(sortedValues, newValue);


		int jj;
		if (idxThisPass > idxLastPass) {
			for (int i = idxLastPass; i < idxThisPass && i <= idxOfEnd; i++) {
				jj = indicesOfSortedValues[i];
				results[jj] -= 1;
				if (Debug.DEBUG_FILTER)
					System.out.println(" -1  from item at : " + jj);
			}
		}
		else {
			for (int i = idxThisPass; i < idxLastPass; i++) {
				if (i <= idxOfEnd) {
					jj = indicesOfSortedValues[i];
					results[jj] += 1;
					if (Debug.DEBUG_FILTER)
						System.out.println("#6 +1  to item at : " + jj);
				}
			}
		}
		return idxThisPass;
	}


   public static int bSearchEnd(long newValue,
					long[] sortedValues,
					int[] indicesOfSortedValues,
					int idxLastPass,
					int idxOfBeg,
					byte[] results) {

      // Need the binary search index to be positioned at the last (end) of a
      // group of items of equal values or at the first item of lesser value.
		int idxThisPass = binarySearchEqOrLt(sortedValues, newValue);
		if (Debug.DEBUG_FILTER)
			System.out.println(" BSearch index is: " + idxThisPass);


		if (Debug.DEBUG_FILTER)
			System.out.println(" IdxLastPass, IdxThisPass: " + idxLastPass + " " + idxThisPass);
		int jj;
		if (idxThisPass > idxLastPass) {
			for (int i = idxLastPass+1; i <= idxThisPass; i++) {
				if (i >= idxOfBeg) {  // don't trample on the begin side
					jj = indicesOfSortedValues[i];
					results[jj] += 1;
					if (Debug.DEBUG_FILTER)
						System.out.println("#7 +1  to item at : " + jj);
				}
				else {
					jj = indicesOfSortedValues[i];
					if (Debug.DEBUG_FILTER)
						System.out.println(" (+1)  to item at : " + jj + " DONT TRAMPLE");
				}
			}
		}
		else {
			for (int i = idxThisPass+1; i <= idxLastPass; i++) {
				if (i >= idxOfBeg) {  // don't trample on the begin side
					jj = indicesOfSortedValues[i];
					results[jj] -= 1;
					if (Debug.DEBUG_FILTER)
						System.out.println(" -1  from item at : " + jj);
				}
			}
		}
		return idxThisPass;
	}

   public static int bSearchEnd(int newValue,
					int[] sortedValues,
					int[] indicesOfSortedValues,
					int idxLastPass,
					int idxOfBeg,
					byte[] results) {

		// Need the binary search index to be positioned at the last (end) of a
		// group of items of equal values or at the first item of lesser value.
		int idxThisPass = binarySearchEqOrLt(sortedValues, newValue);
		if (Debug.DEBUG_FILTER)
			System.out.println(" BSearch index is: " + idxThisPass);

		if (Debug.DEBUG_FILTER)
			System.out.println(" IdxLastPass, IdxThisPass: " + idxLastPass + " " + idxThisPass);
		int jj;
		if (idxThisPass > idxLastPass) {
			for (int i = idxLastPass+1; i <= idxThisPass; i++) {
				if (i >= idxOfBeg) {  // don't trample on the begin side
					jj = indicesOfSortedValues[i];
					results[jj] += 1;
					if (Debug.DEBUG_FILTER)
						System.out.println("#8 +1  to item at : " + jj);
				}
				else {
					jj = indicesOfSortedValues[i];
					if (Debug.DEBUG_FILTER)
						System.out.println(" (+1)  to item at : " + jj + " DONT TRAMPLE");
				}
			}
		}
		else {
			for (int i = idxThisPass+1; i <= idxLastPass; i++) {
				if (i >= idxOfBeg) {  // don't trample on the begin side
					jj = indicesOfSortedValues[i];
					results[jj] -= 1;

					if (Debug.DEBUG_FILTER)
						System.out.println(" -1  from item at : " + jj);
				}
			}
		}
		return idxThisPass;
	}


   static public int sweepArrays(long begValue,
					long endValue,
					long[] startValues,
					long[] endValues,
					int indCntLastPass,
					int[] indicesIncluded,
					byte[] results) {

		for (int i = 0; i < indCntLastPass; i++) {
			int jidx = indicesIncluded[i];
			results[jidx] -= 1;
		}
		int indCnt = 0;
		if (begValue > endValue)
			return indCnt;
		for (int i = 0; i < startValues.length; i++) {
			if (!(begValue > endValues[i] || endValue < startValues[i])) {
				results[i] += 1;
				indicesIncluded[indCnt++] = i;
			}
		}
		return indCnt;
	}
}

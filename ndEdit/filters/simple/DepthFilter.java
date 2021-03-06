package ndEdit.filters.simple;

 import ndEdit.*;
 import ndEdit.filters.*;


/** @stereotype Strategy*/
public class DepthFilter {

  //private SemaphoreResults_Event lnkUnnamed;
  PointerCollection pc;
  double begValue, endValue;
  double[] arr1;
  double[] arr2;
  boolean scalar;
  boolean[] results;
  int sz;


   // ---------------------------------------------------------------------
   //
   public DepthFilter(double begValue, double endValue) {
      this.begValue = begValue;
      this.endValue = endValue;
   }


   // ---------------------------------------------------------------------
   //
	public void newPointerCollection(PointerCollection pc, boolean[] results) {
		this.pc = pc;
		this.sz = pc.getSize();
		scalar = true;
		this.results = results;
		arr1 = pc.getDepthArr1();

		// If arr2 is null, depths are a single scalar.
		arr2 = pc.getDepthArr2();

		if (arr2 != null)
			scalar = false;

		if (scalar) {
			// depths are a scalar
			if (Debug.DEBUG_FILTER)
				System.out.println("depths: Scalar");
		}
		else {
			// depths are each a range
			if (Debug.DEBUG_FILTER)
				System.out.println("depths: Range of depths");
		}
	}

   // ---------------------------------------------------------------------
   //
   // very brute force
   //
   public void startChanged(double oldValue, double newValue) {
      begValue = newValue;
      valueChange();
   }
   // ---------------------------------------------------------------------
   //
   public void stopChanged(double oldValue, double newValue) {
      endValue = newValue;
      valueChange();
   }
   // ---------------------------------------------------------------------
   //
   public void valueChange() {
      if (begValue > endValue) {
	for (int i = 0; i < sz; i++) results[i] = false;
	return;
      }
      if (scalar) {
	for (int i = 0; i < sz; i++) {
	   if ((arr1[i] >= begValue) && (arr1[i] <= endValue))
	     results[i] = true;
	   else
	     results[i] = false;
	}
      }
      else {
	for (int i = 0; i < sz; i++) {
	   if (!(begValue > arr2[i] || endValue < arr1[i]))
	     results[i] = true;
	   else
	     results[i] = false;
	}
      }
   }
   
	public void reset() {
	}
}

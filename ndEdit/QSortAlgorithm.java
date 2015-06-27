/*
 * $Id: QSortAlgorithm.java,v 1.5 2005/02/15 18:31:10 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;
/*
 * @(#)QSortAlgorithm.java	1.3   29 Feb 1996 James Gosling
 *
 * Copyright (c) 1994-1996 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
 * without fee is hereby granted. 
 * Please refer to the file http://www.javasoft.com/copy_trademarks.html
 * for further important copyright and trademark information and to
 * http://www.javasoft.com/licensing.html for further important
 * licensing information for the Java (tm) Technology.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
 * CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
 * PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
 * NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
 * SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
 * SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
 * PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES").  SUN
 * SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
 * HIGH RISK ACTIVITIES.
 */

/**
 * A quick sort demonstration algorithm
 * SortAlgorithm.java
 *
 * @author James Gosling
 * @author Kevin A. Smith
 * @version 	@(#)QSortAlgorithm.java	1.3, 29 Feb 1996
 */
public class QSortAlgorithm 
//extends SortAlgorithm 
{
   /** This is a generic version of C.A.R Hoare's Quick Sort 
    * algorithm.  This will handle arrays that are already
    * sorted, and arrays with duplicate keys.<BR>
    *
    * If you think of a one dimensional array as going from
    * the lowest index on the left to the highest index on the right
    * then the parameters to this function are lowest index or
    * left and highest index or right.  The first time you call
    * this function it will be with the parameters 0, a.length - 1.
    *
    * @param a       an integer array
    * @param lo0     left boundary of array partition
    * @param hi0     right boundary of array partition
    */
   static void QuickSortLong(long arr[], int indices[], int lo0, int hi0) throws Exception {
      int lo = lo0;
      int hi = hi0;
      long mid;

      // pause for redraw
      //pause(lo, hi);
      if (hi0 > lo0)
      {

         /* Arbitrarily establishing partition element as the midpoint of
          * the array.
          */
         mid = arr[(lo0 + hi0) / 2];

         // loop through the array until indices cross
         while(lo <= hi)
         {
            /* find the first element that is greater than or equal to 
             * the partition element starting from the left Index.
             */
            while((lo < hi0) && (arr[lo] < mid))
               ++lo;

            /* find an element that is smaller than or equal to 
             * the partition element starting from the right Index.
             */
            while((hi > lo0) && (arr[hi] > mid))
               --hi;

            // if the indexes have not crossed, swap
            if(lo <= hi) 
            {
               swap(arr, indices, lo, hi);
               // pause
               //pause();

               ++lo;
               --hi;
            }
         }

         /* If the right index has not reached the left side of array
          * must now sort the left partition.
          */
         if(lo0 < hi)
            QuickSortLong(arr, indices, lo0, hi);

         /* If the left index has not reached the right side of array
          * must now sort the right partition.
          */
         if(lo < hi0)
            QuickSortLong(arr, indices, lo, hi0);

      }
   }
   
  static void QuickSortInt(int arr[], int indices[], int lo0, int hi0) throws Exception {
      int lo = lo0;
      int hi = hi0;
      int mid;

      // pause for redraw
      //pause(lo, hi);
      if (hi0 > lo0)
      {

         /* Arbitrarily establishing partition element as the midpoint of
          * the array.
          */
         mid = arr[(lo0 + hi0) / 2];

         // loop through the array until indices cross
         while(lo <= hi)
         {
            /* find the first element that is greater than or equal to 
             * the partition element starting from the left Index.
             */
            while((lo < hi0) && (arr[lo] < mid))
               ++lo;

            /* find an element that is smaller than or equal to 
             * the partition element starting from the right Index.
             */
            while((hi > lo0) && (arr[hi] > mid))
               --hi;

            // if the indexes have not crossed, swap
            if(lo <= hi) 
            {
               swap(arr, indices, lo, hi);
               // pause
               //pause();

               ++lo;
               --hi;
            }
         }

         /* If the right index has not reached the left side of array
          * must now sort the left partition.
          */
         if(lo0 < hi)
            QuickSortInt(arr, indices, lo0, hi);

         /* If the left index has not reached the right side of array
          * must now sort the right partition.
          */
         if(lo < hi0)
            QuickSortInt(arr, indices, lo, hi0);

      }
   }

   static private void swap(long arr[], int indices[], int i, int j)
   {
      long T;
      T = arr[i]; 
      arr[i] = arr[j];
      arr[j] = T;

      int U;
      U = indices[i]; 
      indices[i] = indices[j];
      indices[j] = U;
   }

   static private void swap(int arr[], int indices[], int i, int j)
   {
      int T;
      T = arr[i]; 
      arr[i] = arr[j];
      arr[j] = T;

      int U;
      U = indices[i]; 
      indices[i] = indices[j];
      indices[j] = U;
   }

   static public void sort(long arr[], int indices[]) throws Exception
   {
      QuickSortLong(arr, indices, 0, arr.length - 1);
   }

   static public void sort(int arr[], int indices[]) throws Exception
   {
      QuickSortInt(arr, indices, 0, arr.length - 1);
   }

   // --------------------------------------------------------------------------

   static void QuickSortFloat(double arr[], int indices[], int lo0, int hi0) throws Exception
   {
      int lo = lo0;
      int hi = hi0;
      double mid;

      // pause for redraw
      //pause(lo, hi);
      if (hi0 > lo0)
      {

         /* Arbitrarily establishing partition element as the midpoint of
          * the array.
          */
         mid = arr[(lo0 + hi0) / 2];

         // loop through the array until indices cross
         while(lo <= hi)
         {
            /* find the first element that is greater than or equal to 
             * the partition element starting from the left Index.
             */
            while((lo < hi0) && (arr[lo] < mid))
               ++lo;

            /* find an element that is smaller than or equal to 
             * the partition element starting from the right Index.
             */
            while((hi > lo0) && (arr[hi] > mid))
               --hi;

            // if the indexes have not crossed, swap
            if(lo <= hi) 
            {
               swap(arr, indices, lo, hi);
               // pause
               //pause();

               ++lo;
               --hi;
            }
         }

         /* If the right index has not reached the left side of array
          * must now sort the left partition.
          */
         if(lo0 < hi)
            QuickSortFloat(arr, indices, lo0, hi);

         /* If the left index has not reached the right side of array
          * must now sort the right partition.
          */
         if(lo < hi0)
            QuickSortFloat(arr, indices, lo, hi0);

      }
   }


   static void QuickSortDouble(double arr[], int indices[], int lo0, int hi0) throws Exception
   {
      int lo = lo0;
      int hi = hi0;
      double mid;

      // pause for redraw
      //pause(lo, hi);
      if (hi0 > lo0)
      {

         /* Arbitrarily establishing partition element as the midpoint of
          * the array.
          */
         mid = arr[(lo0 + hi0) / 2];

         // loop through the array until indices cross
         while(lo <= hi)
         {
            /* find the first element that is greater than or equal to 
             * the partition element starting from the left Index.
             */
            while((lo < hi0) && (arr[lo] < mid))
               ++lo;

            /* find an element that is smaller than or equal to 
             * the partition element starting from the right Index.
             */
            while((hi > lo0) && (arr[hi] > mid))
               --hi;

            // if the indexes have not crossed, swap
            if(lo <= hi) 
            {
               swap(arr, indices, lo, hi);
               // pause
               //pause();

               ++lo;
               --hi;
            }
         }

         /* If the right index has not reached the left side of array
          * must now sort the left partition.
          */
         if(lo0 < hi)
            QuickSortDouble(arr, indices, lo0, hi);

         /* If the left index has not reached the right side of array
          * must now sort the right partition.
          */
         if(lo < hi0)
            QuickSortDouble(arr, indices, lo, hi0);

      }
   }
   static private void swap(double arr[], int indices[], int i, int j)
   {
      double T;
      T = arr[i]; 
      arr[i] = arr[j];
      arr[j] = T;

      int U;
      U = indices[i]; 
      indices[i] = indices[j];
      indices[j] = U;
   }

   static public void sort(double arr[], int indices[]) throws Exception
   {
      QuickSortFloat(arr, indices, 0, arr.length - 1);
   }
}

/*
 * $Id: Assert.java,v 1.2 2005/06/17 18:10:58 oz Exp $
 *
 */

package javaoceanatlas.utility;

public class Assert {
  static public boolean mAssertionsEnabled = false; // false to short circuit
  static public boolean mThrowOnFail = false; // true to throw exceptions
  static public boolean mMessageOnFail = true; // true to display message on failure
  static public boolean mDumpStackOnFail = false; // true to dump call stack on failure

  static String mTrueMsg = "Assert.isTrue failed";
  static String mFalseMsg = "Assert.isFalse failed";
  static String mNotNullMsg = "Assert.isNotNull failed";

  static public void isTrue(boolean inBoolean) {
    if (mAssertionsEnabled && !inBoolean) {
      if (mMessageOnFail) {
        System.out.println(mTrueMsg);
      }

      if (mDumpStackOnFail) {
        Thread.dumpStack();
      }

      //if( mThrowOnFail )
      //	throw new AssertionException( mTrueMsg );
    }
  }

  static public void isTrue(boolean inBoolean, String inMessage) {
    if (mAssertionsEnabled && !inBoolean) {
      if (mMessageOnFail) {
        System.out.println(mTrueMsg + ": " + inMessage);
      }

      if (mDumpStackOnFail) {
        Thread.dumpStack();
      }

      //if( mThrowOnFail )
      //	throw new AssertionException( mTrueMsg + ": " + inMessage );
    }
  }

  static public void isFalse(boolean inBoolean) {
    if (mAssertionsEnabled && inBoolean) {
      if (mMessageOnFail) {
        System.out.println(mFalseMsg);
      }

      if (mDumpStackOnFail) {
        Thread.dumpStack();
      }

      //if( mThrowOnFail )
      //	throw new AssertionException( mFalseMsg );
    }
  }

  static public void isFalse(boolean inBoolean, String inMessage) {
    if (mAssertionsEnabled && inBoolean) {
      if (mMessageOnFail) {
        System.out.println(mFalseMsg + ": " + inMessage);
      }

      if (mDumpStackOnFail) {
        Thread.dumpStack();
      }

      //if( mThrowOnFail )
      //	throw new AssertionException( mFalseMsg + ": " + inMessage );
    }
  }

  static public void isNotNull(Object inObject) {
    if (mAssertionsEnabled && inObject == null) {
      if (mMessageOnFail) {
        System.out.println(mNotNullMsg);
      }

      if (mDumpStackOnFail) {
        Thread.dumpStack();
      }

      //if( mThrowOnFail )
      //	throw new AssertionException( mNotNullMsg );
    }
  }

  static public void isNotNull(Object inObject, String inMessage) {
    if (mAssertionsEnabled && inObject == null) {
      if (mMessageOnFail) {
        System.out.println(mNotNullMsg + ": " + inMessage);
      }

      if (mDumpStackOnFail) {
        Thread.dumpStack();
      }

      //if( mThrowOnFail )
      //	throw new AssertionException( mNotNullMsg + ": " + inMessage );
    }
  }
}

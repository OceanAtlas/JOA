/*
 * $Id: ProgressTimer.java,v 1.3 2005/02/15 18:31:10 oz Exp $
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

public class ProgressTimer extends Thread {
	ProgressPresenter mPresenter;
	long mDelay;
	
	public ProgressTimer(ProgressPresenter presenter, long delay) {
		mPresenter = presenter;
		mDelay = delay;
	}
	
	public void startProgressPresenter() {
		if (isAlive())
			super.resume();
		else
			start();	
	}
	
	public void endProgressPresenter() {
		if (isAlive())
			suspend();	
	}
	
	public void run() {
		for (;;) {
			mPresenter.updateProgress();
			try {
				Thread.sleep(mDelay);
			}
			catch (InterruptedException e) {
				;
			}
		}
	}
}



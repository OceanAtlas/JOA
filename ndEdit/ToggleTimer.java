/*
 * $Id: ToggleTimer.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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

public class ToggleTimer extends Thread {
	long mWaitTime;
	long mDelay;
	long mStartTime;
	ToggleAction mClient;
	
	public ToggleTimer(ToggleAction client, long wait, long delay) {
		mWaitTime = wait;
		mDelay = delay;
		mClient = client;
	}
	
	public void startTimer() {
		mStartTime = System.currentTimeMillis();
		mClient.toggleOn();
		if (isAlive())
			super.resume();
		else
			start();	
	}
	
	public void endTimer() {
		mClient.toggleOff();
		if (isAlive())
			stop();	
	}
	
	public void run() {
		for (;;) {
			try {
				Thread.sleep(100);
				if (System.currentTimeMillis() - mStartTime >= mWaitTime)
					this.endTimer();
			}
			catch (InterruptedException e) {
				;
			}
		}
	}
}



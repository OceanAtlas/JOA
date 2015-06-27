/*
 * $Id: MaintenanceTimer.java,v 1.2 2005/02/15 18:31:09 oz Exp $
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

public class MaintenanceTimer extends Thread {
	ButtonMaintainer mMaintainer;
	long mDelay;
	
	public MaintenanceTimer(ButtonMaintainer maintainer, long delay) {
		mMaintainer = maintainer;
		mDelay = delay;
	}
	
	public void startMaintainer() {
		if (isAlive())
			super.resume();
		else
			start();	
	}
	
	public void endMaintainer() {
		if (isAlive())
			stop();	
	}
	
	public void run() {
		for (;;) {
			mMaintainer.maintainButtons();
			try {
				Thread.sleep(mDelay);
			}
			catch (InterruptedException e) {
				;
			}
		}
	}
}



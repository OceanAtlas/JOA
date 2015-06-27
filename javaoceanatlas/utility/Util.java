/*
 * $Id: Util.java,v 1.2 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.applet.Applet;
import java.awt.*;

/**
 * A handy collection of methods for getting a component's frame, getting a
 * component's applet, waiting for a component's image, and wallpapering a
 * components background.
 * <p>
 * 
 * @version 1.0, Apr 1 1996
 * @author David Geary
 */
public class Util {
	public static Dialog getDialog(Component c) {
		if (c instanceof Dialog)
			return (Dialog) c;

		while ((c = c.getParent()) != null) {
			if (c instanceof Dialog)
				return (Dialog) c;
		}
		return null;
	}

	public static Frame getFrame(Component c) {
		if (c instanceof Frame)
			return (Frame) c;

		while ((c = c.getParent()) != null) {
			if (c instanceof Frame)
				return (Frame) c;
		}
		return null;
	}

	public static Applet getApplet(Component c) {
		if (c instanceof Applet)
			return (Applet) c;

		while ((c = c.getParent()) != null) {
			if (c instanceof Applet)
				return (Applet) c;
		}
		return null;
	}

	public static void waitForImage(Component component, Image image) {
		MediaTracker tracker = new MediaTracker(component);
		try {
			tracker.addImage(image, 0);
			tracker.waitForID(0);
		}
		catch (InterruptedException e) {
			Assert.isNotNull(null);
		}
	}

	public static void wallPaper(Component component, Graphics g, Image image) {
		Dimension compsize = component.getSize();
		Util.waitForImage(component, image);

		int patchW = image.getWidth(component);
		int patchH = image.getHeight(component);

		Assert.isTrue(patchW != -1 && patchH != -1);

		for (int r = 0; r < compsize.width; r += patchW) {
			for (int c = 0; c < compsize.height; c += patchH)
				g.drawImage(image, r, c, component);
		}
	}

	public static void translateToScreen(Event event) {
		Assert.isTrue(event.target instanceof Component);

		Frame frame = getFrame((Component) event.target);
		event.translate(frame.getLocation().x + frame.getInsets().left, frame.getLocation().y + frame.getInsets().top);
	}

	public static void beep() {
		System.out.write(7);
		System.out.flush();
	}
}

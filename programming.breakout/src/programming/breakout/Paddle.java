package programming.breakout;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;

import acm.graphics.GRoundRect;

public class Paddle extends GRoundRect implements MouseMotionListener, MouseListener {
	
	private boolean mouseIsOnCanvas = false;	
	private boolean mouseIsActive = false;
	
	/** The last x position of the mouse. */
	private double lastX;

	public Paddle(double x, double y, double length) {
		super(x, y, length, 10);
		this.setFilled(true);
	}

	public void mouseClicked(MouseEvent e) {
		if (mouseIsOnCanvas) {
			// toggle boolean
			mouseIsActive = !mouseIsActive;
		}
	}

	public void mouseEntered(MouseEvent e) {
		mouseIsOnCanvas = true;
		lastX = e.getX();
	}

	public void mouseExited(MouseEvent e) {
		mouseIsOnCanvas = false;
	}

	public void mouseMoved(MouseEvent e) {
		if (mouseIsActive && mouseIsOnCanvas) {
			this.move(e.getX() - lastX, 0);
			lastX = e.getX();
		}
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mousePressed(MouseEvent e) {
		// nothing happens
	}

	public void mouseReleased(MouseEvent e) {
		// nothing happens
	}
}

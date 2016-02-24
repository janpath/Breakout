package programming.breakout;

import acm.graphics.GOval;
import acm.graphics.GRectangle;
import acm.graphics.GObject;

public class Ball extends GOval {

	private double direction = -0.123456;
	private double size;

	/** Primary Constructor */
	public Ball(double x, double y, double size) {

		super(x, y, size, size);
		this.setFilled(true);
		this.size = size;
	}

	/** Simple constructor */
	public Ball(double size) {
		this(0, 0, size);
	}

	/**
	 * Calls the {@code movePolar} method (which is forbidden to override), but
	 * saves the direction, which is an angle from 0 (to the right) counterclockwise to 360
	 */
	public void moveInDirection(double step, double direction) {
		this.direction = direction;
		super.movePolar(step, direction);
	}

	/** Returns the direction this Ball is moving */
	public double getDirection() {
		assert (direction != -0.123456) : "Direction has not been initialized";
		return direction;
	}

	/**
	 * Returns true for a Rectangle, which is defined by the bounds of the given
	 * GObject, intersect the bounds of this Ball.
	 */
	public boolean intersects(GObject obj) {
		return obj.getBounds().intersects(this.getBounds());
	}

	/**
	 * Returns true if this Ball intersects the border(!) of the given
	 * GRectangle.
	 */
	public boolean intersectsBorder(GRectangle outerRect) {

		/*
		 * Create an inner Rectangle that is smaller by the size of the ball in
		 * each direction
		 */
		GRectangle innerRect = new GRectangle(outerRect);
		innerRect.grow(-size, -size);

		// ball is inside the borders
		if (this.getBounds().intersects(innerRect)) {
			return false;
		}
		// there is an intersection with the border
		else if (this.getBounds().intersects(outerRect)) {
			return true;
		}
		// ball exceeds the borders
		else {
			return false;
		}

	}

	/**
	 * Returns a String for the Direction this Ball exceeds or intersects the
	 * border(!) of the given GRectangle. There are only four directions: right
	 * and left are prioritized over top and bottom.
	 */
	public String getExceedingDirection(GRectangle rect) {

		double x = rect.getX();
		double y = rect.getY();
		double width = rect.getWidth();
		double height = rect.getHeight();

		// right
		if (this.getX() + size >= width) {
			return "right";
		}
		// left
		else if (this.getX() <= x) {
			return "left";
		}
		// bottom
		else if (this.getY() + size >= height) {
			return "bottom";
		}
		// top
		else if (this.getY() <= y) {
			return "top";
		}
		// no intersection
		else {
			return "no intersection";
		}
	}

	public double getNewDirection() {

		double oldDirection = getDirection();

		// right
		if (getX() + size >= Interface.getFieldWidth()) {
			return 180 - oldDirection;
		}
		// left
		else if (getX() <= 0) {
			return 180 - oldDirection;
		}
		// top
		else if (getY() <= 0) {
			return 360 - oldDirection;
		}
//		// bottom
//		else if (getY() + size >= HEIGHT)
//		{
//			return 360 - oldDirection;
//		}
		// other
		else {

			return oldDirection;

		}

	}

}

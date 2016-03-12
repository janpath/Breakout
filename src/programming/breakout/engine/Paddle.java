package programming.breakout.engine;

/**
 * A paddle is the intersection of a rectangle and a circle
 */
public class Paddle extends Rectangle {

	/**
	 * Creates a paddle in the given rectangle.
	 * @param position Upper left corner of the bounding rectangle
	 * @param width Width of the bounding rectangle
	 * @param height Height of the bounding rectangle
	 */
	public Paddle(Vector2D position, double width, double height) {
		super(position, width, height);
	}

	/**
	 * Get the radius of the underlying arc
	 */
	public double getRadius() {
		return ((getHeight() * getHeight()) + (getWidth() * getWidth() / 4))
			/ (2 * getHeight());
	}

	/**
	 * Get the center of the underlying arc
	 */
	public Vector2D getArcCenter() {
		return getPosition().add(new Vector2D(getWidth()/2, getRadius()));
	}

	/**
	 * Get the angle of the underlying arc
	 */
	public double getAngle() {
		return Math.asin((getWidth()/2d) / getRadius()) * 2d;
	}
}

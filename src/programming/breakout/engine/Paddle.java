package programming.breakout.engine;

public class Paddle extends Rectangle {

	public Paddle(Vector2D position, double width, double height) {
		super(position, width, height);
	}

	public double getRadius() {
		return ((getHeight() * getHeight()) + (getWidth() * getWidth() / 4)) / (2 * getHeight());
	}

	public Vector2D getArcCenter() {
		return getPosition().add(new Vector2D(getWidth()/2, getRadius()));
	}

	public double getAngle() {
		return Math.asin((getWidth()/2d) / getRadius()) * 2d;
	}
}

package programming.breakout.engine;

public class Vector2D {
	private final double x0, x1;

	/**
	 * @return the x0
	 */
	public double getX0() {
		return x0;
	}

	/**
	 * @return the x1
	 */
	public double getX1() {
		return x1;
	}

	/**
	 * Create new Vector
	 * @param x0 first compounent
	 * @param x1 second compounent
	 */
	public Vector2D(double x0, double x1) {
		this.x0 = x0;
		this.x1 = x1;
	}
}

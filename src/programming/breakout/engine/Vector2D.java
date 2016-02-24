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

	/**
	 * Add two vectors
	 * @param otherVector vector to add
	 * @return resulting vector from addition
	 */
	public Vector2D add(Vector2D otherVector) {
		return new Vector2D(getX0() + otherVector.getX0(), getX1() + otherVector.getX1());
	}
}

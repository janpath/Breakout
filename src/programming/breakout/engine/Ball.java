package programming.breakout.engine;

/**
 * A ball
 */
public class Ball extends Entity {
	private double radius;
			
	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	void setRadius(double radius) {
		this.radius = radius;
	}
}

package programming.breakout.engine;

import programming.breakout.engine.Vector2D;

public abstract class Entity {
	private Vector2D acceleration = new Vector2D(0, 0);
	private Vector2D velocity = new Vector2D(0, 0);
	private Vector2D position = new Vector2D(0, 0);

	/**
	 * @return the acceleration
	 */
	public Vector2D getAcceleration() {
		return acceleration;
	}

	/**
	 * @param acceleration the acceleration to set
	 */
	void setAcceleration(Vector2D acceleration) {
		this.acceleration = acceleration;
	}

	/**
	 * @return the velocity
	 */
	public Vector2D getVelocity() {
		return velocity;
	}

	/**
	 * @param velocity the velocity to set
	 */
	void setVelocity(Vector2D velocity) {
		this.velocity = velocity;
	}

	/**
	 * @return the position
	 */
	public Vector2D getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	void setPosition(Vector2D position) {
		this.position = position;
	}
}

/**
 * Copyright: 2016 Jan Path
 *            2016 Felix von der Heide
 *
 * This file is part of Breakout.
 *
 * Breakout is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Breakout is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Breakout.  If not, see <http://www.gnu.org/licenses/>.
 */

package programming.breakout.engine;

import programming.breakout.engine.Vector2D;

public abstract class Entity {
	private Vector2D acceleration = new Vector2D(0, 0);
	private Vector2D velocity = new Vector2D(0, 0);
	private Vector2D position = new Vector2D(0, 0);

	private boolean visible;

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

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the x coordinate of the object
	 */
	public double getX() {
		return position.getX0();
	}

	/**
	 * @return the y coordinate of the object
	 */
	public double getY() {
		return position.getX1();
	}
}

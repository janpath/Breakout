/*
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

import programming.breakout.engine.Rectangle;
import programming.breakout.engine.Vector2D;

/**
 * A ball
 */
public class Ball extends Entity {
	private double radius;

	/**
	 * @param position the initial position of the upper left corner of the ball
	 * @param radius the radius of the ball
	 */
	public Ball(Vector2D position, double radius) {
		setPosition(position);
		this.radius = radius;
	}

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

	/**
	 * @return bounding rectangle
	 */
	@Override
	public Rectangle getBounds() {
		return new Rectangle(new Vector2D(getX() - radius, getY() - radius),
		                     2 * radius, 2 * radius);
	}

	/**
	 * @return the position of the center of the ball.
	 */
	public Vector2D getCenter() {
		return new Vector2D(this.getX() + this.getRadius(),
		                    this.getY() + this.getRadius());
	}
}

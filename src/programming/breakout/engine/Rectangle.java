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

import programming.breakout.engine.Entity;

public class Rectangle extends Entity {

	/**
	 * instance variables
	 */
	private double height = 0;
	private double width = 0;

	/**
	 * primary constructor
	 */
	Rectangle(Vector2D position, double width, double height) {
		this.setPosition(position);
		this.height = height;
		this.width = width;
	}

	/**
	 * Returns the height of the Rectangle
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Sets the height of the Rectangle
	 */
	void setHeight(double height) {
		this.height = height;
	}

	/**
	 * Returns the width of the Rectangle
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Sets the width of the Rectangle
	 */
	void setWidth(double width) {
		this.width = width;
	}

	/**
	 * @return bounding rectangle
	 */
	@Override
	public Rectangle getBounds() {
		return this;
	}
}

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
	
	/**
	 * Calculate the scalar product
	 */
	public double scalarProduct (Vector2D otherVector) {
		return getX0() * otherVector.getX0() + getX1() * otherVector.getX1();
	}
}

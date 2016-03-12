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

/**
 * Immutable Object to represent a two dimensional vector.
 */
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
	 *
	 * @param x0
	 *            first compounent
	 * @param x1
	 *            second compounent
	 */
	public Vector2D(double x0, double x1) {
		this.x0 = x0;
		this.x1 = x1;
	}

	/**
	 * Add two vectors
	 *
	 * @param otherVector
	 *            vector to add
	 * @return resulting vector from addition
	 */
	public Vector2D add(Vector2D otherVector) {
		return new Vector2D(getX0() + otherVector.getX0(), getX1() + otherVector.getX1());
	}

	/**
	 * Subtract another vector from this one
	 *
	 * @param otherVector
	 *            vector to subtract
	 * @return resulting vector from subtraction
	 */
	public Vector2D sub(Vector2D otherVector) {
		return new Vector2D(getX0() - otherVector.getX0(), getX1() - otherVector.getX1());
	}

	/**
	 * Calculate the dot product
	 */
	public double dotProduct(Vector2D otherVector) {
		return getX0() * otherVector.getX0() + getX1() * otherVector.getX1();
	}

	/**
	 * Get the magnitude of the vector
	 */
	public double getMagnitude() {
		return Math.sqrt(getX0() * getX0() + getX1() * getX1());
	}

	/**
	 * Multiply the vector with a scalar
	 */
	public Vector2D scale(double scale) {
		return new Vector2D(getX0() * scale, getX1() * scale);
	}

	/**
	 * Rotate the vector
	 * @param theta angle in radians around which to rotate the vector
	 */
	public Vector2D rotate(double theta) {
		Vector2D result
			= new Vector2D(Math.cos(theta)*getX0() - Math.sin(theta)*getX1(),
                     Math.sin(theta)*getX0() + Math.cos(theta)*getX1());
    assert result.getMagnitude() == getMagnitude():
		"Vector rotation should not change the length";
		return result;
	}

	/**
	 * @return {@code true} if both components are the same.
	 */
	@Override
	public boolean equals(Object o) {
		return o instanceof Vector2D &&
			((Vector2D) o).getX0() == x0 && ((Vector2D) o).getX1() == x1;
	}


	/**
	 * @returns Returns string of format Classname[x1, x2]
	 */
	@Override
	public String toString() {
		return String.format("%s[%f, %f]", getClass(), getX0(), getX1());
	}

	/**
	 * Hashcodes are equal if both components are the same.
	 */
	@Override
	public int hashCode() {
		return Double.hashCode(getX0()) + Double.hashCode(getX1());
	}
}

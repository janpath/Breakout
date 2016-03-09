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
 * An Immutable pair
 */
public class Pair<S, T> {
	/**
	 * Left object
	 */
	public S left;

	/**
	 * Right object
	 */
	public T right;

	public Pair(S left, T right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Create pair inferring type
	 */
	public static <S, T> Pair<S,T> of(S left, T right) {
		return new Pair<S,T>(left, right);
	}

	/**
	 * @return the left object
	 */
	public S getLeft() {
		return left;
	}

	/**
	 * @return the right object
	 */
	public T getRight() {
		return right;
	}

}

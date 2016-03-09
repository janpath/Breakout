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

import java.util.ArrayList;

import programming.breakout.engine.Ball;
import programming.breakout.engine.Rectangle;

public class DummyEngine implements Runnable {
	private GameState state;

	public DummyEngine(GameState state) {
		this.state = state;
		state.setWidth(80);
		state.setHeight(100);
		ArrayList<Entity> entities = state.getEntityList();
		entities.add(new Ball(new Vector2D(5, 80), 1));
		entities.add(new Rectangle(new Vector2D(3, 98), 5, 1));
	}

	@Override
	public void run() {

	}
}

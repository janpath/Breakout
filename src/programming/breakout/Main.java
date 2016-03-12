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

package programming.breakout;

import programming.breakout.engine.GameState;
import programming.breakout.engine.Controller;
import programming.breakout.engine.Engine;
import programming.breakout.view.View;


/*
 * 
 */
public class Main {
	public static void main(String[] args) {
		GameState game = new GameState();
		Engine engine = new Engine(game);
		View view = new View(game);
		Controller controller = new Controller(game, engine.getPaddle(), true, false, view.getGCanvas());

		Thread engineThread = new Thread(engine);
		engineThread.start();

		view.start();
	}
}

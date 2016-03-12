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


/***************************************
 * 
 * This program is a variation of the video game classic called “Breakout”, it
 * is ready to run as-is. It is implemented using four main Classes which are:
 * the Engine, the View, the Controller and the GameState. Where the latter one
 * is an Observable. It gets its information from the Engine class and is
 * observed by the View, which is an Observer. The controller implements a
 * number of Listener-interfaces to provide user input. The four main classes
 * are implemented in a Way that matches the Model-view-controller (MVC)
 * pattern, where the Model is represented by the Engine plus the GameState
 * class. The user interact via the Controller and gets to see what the View
 * provides. Besides those, there are classes needed to represent the objects on
 * the field, namely the Paddle, the Ball and the Bricks (Rectangle class).
 * Those (directly or indirectly) inherit from the Entity class which cannot be
 * instantiated itself but provides methods to the Objects to control basic
 * attributes like position and velocity. The last two classes (Vector2D and
 * Pair) are helper classes, needed to implement the Physics and Object-control
 * in this Program.
 * 
 * Each class has a number of methods, instance-variables, and sometimes
 * class-variables. The visibility of those are set to private by default, and
 * only changed to public for “getter” and “setter”-methods, constructors and
 * the run() methods, as well as the main() method, which have to be public, all
 * of those are needed by other classes to make use of the Object. There are
 * some methods, that are package-private as well. Those are not needed to make
 * use of the class but by this program to function properly. Instance- and
 * class-variables only occur in private visibility. If the values of those are
 * needed in other classes, there are public “getter”-methods for them. In this
 * context class variables are only needed as constants (finals) they are used
 * for initialization, and can not be changed in in run time but manually in
 * between program execution processes to alternate user experience. On the other
 * hand instance variables are used to store values that must have class-wide
 * scope.
 ***************************************/

public class Main {
	public static void main(String[] args) {
		GameState game = new GameState();
		Engine engine = new Engine(game);
		View view = new View(game);
		new Controller(game, engine.getPaddle(), true, false, view.getGCanvas());

		Thread engineThread = new Thread(engine);
		engineThread.start();

		view.start();
	}
}

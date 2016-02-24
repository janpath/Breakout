package programming.breakout;

import programming.breakout.engine.GameState;
import programming.breakout.engine.Engine;
import programming.breakout.view.View;

public class Main {
	public static void main(String[] args) {
		GameState game = new GameState();
		Engine engine = new Engine(game);
		View view = new View(game);

		Thread engineThread = new Thread(engine);
		engineThread.start();

		view.main(args);
	}
}

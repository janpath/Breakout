package programming.breakout;

import acm.graphics.*;
import acm.program.GraphicsProgram;

public class Breakout extends GraphicsProgram {

	/** The height and width of the ball in pixels */
	static final double BALL_SIZE = 10;

	/** The number of milliseconds to pause after each cycle */
	static final int PAUSE_TIME = 10;

	/**
	 * The number of pixels to shift the ball after each cycle
	 */
	static final double SHIFT = 2.0;

	/** The starting coordinate in x direction */
	static final double START_X = WIDTH / 2;

	/** The starting coordinate in y direction */
	static final double START_Y = HEIGHT / 2;

	public void run() {

		// initialize Interface
		Interface field = new Interface();
		add(field);

		// Set canvas size
		setSize(field.getSize().toDimension());

		// Initialize Bricks
		Bricks bricks = new Bricks(5, 10);
		add(bricks, 0, 0);

		// Initialize ball
		Ball ball = new Ball(START_X, START_Y, BALL_SIZE);
		add(ball);

		// Initialize paddle
		Paddle paddle = new Paddle( WIDTH/2, HEIGHT - 20, 70);
		add(paddle);
		addMouseListeners(paddle);

		double direction = 270;

		while (isOngoing(ball)) {

			ball.moveInDirection(SHIFT, direction);
			direction = getNewDirection(ball);

			pause(PAUSE_TIME);
		}

		GLabel end = new GLabel("Good game, well played.", START_X, START_Y);
		add(end);


	}

	private double getNewDirection(Ball ball) {

		double oldDirection = ball.getDirection();

		// right
		if (ball.getX() + BALL_SIZE >= WIDTH) {
			return 180 - oldDirection;
		}
		// left
		else if (ball.getX() <= 0) {
			return 180 - oldDirection;
		}
		// top
		else if (ball.getY() <= 0) {
			return 360 - oldDirection;
		}
//		// bottom
//		else if (ball.getY() + BALL_SIZE >= HEIGHT)
//		{
//			return 360 - oldDirection;
//		}
		// other
		else {

			return oldDirection;

		}

	}

	/** returns true as long as the ball is still on the field */
	private boolean isOngoing(Ball ball) {
		return ball.getY() < HEIGHT;
	}

}

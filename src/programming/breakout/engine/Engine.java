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

public class Engine implements Runnable {

	/**
	 * game state
	 */
	private boolean isPaused = false;

	/**
	 * Playing field
	 */
	private static final double PLAYING_FIELD_HEIGHT = 130;
	private static final double PLAYING_FIELD_WIDTH = 80;

	/**
	 * Paddle
	 */
	private Rectangle paddle;
	private double paddleLength = 0.2 * PLAYING_FIELD_WIDTH;
	private double paddleHeight = 0.1 * paddleLength;
	private double paddleParabolaFactor = 0.01;

	/**
	 * Bricks
	 */
	private ArrayList<Rectangle> bricks;
	private int numberOfBrickRows = 6;
	private int numberOfBrickCols = 7;
	private int brickWidth = 8;
	private int brickHeight = 4;

	/**
	 * Ball
	 */
	private static final Vector2D START_POS = new Vector2D(PLAYING_FIELD_WIDTH / 2, PLAYING_FIELD_HEIGHT / 2);
	private static final double RADIUS = 2;
	/* Velocity in units per frame */
	private Vector2D velocity = new Vector2D(1.0, 0.0);
	private Ball ball;

	/**
	 * How much to wait between each frame
	 */
	private static final int REFRESH_RATE = 20;

	private GameState state;

	public Engine(GameState state) {
		this.state = state;
		this.paddle = createPaddle();
		this.bricks = createBricks();
		this.ball = createBall();
	}

	@Override
	public void run() {

		setGameState();

		while (isRunning()) {
			long start = System.currentTimeMillis();
			moveBall();

			state.setChanged();
			state.notifyObservers();

			long elapsed = start - System.currentTimeMillis();

			try {
				Thread.sleep(REFRESH_RATE - elapsed);
			} catch (InterruptedException ex) {
			}
		}

		this.ball = createBall();
		run();
		state.setGameOver(true);

	}

	/**
	 * this method moves the ball
	 */
	private void moveBall() {
		do {
			Vector2D newPosition = ball.getPosition().add(ball.getVelocity());
			ball.setPosition(newPosition);
		} while (noCollisionPossible());

	}

	/**
	 * checks whether the ball is inside a "save" part of the playing field
	 */
	private boolean noCollisionPossible() {
		if (ball.getY() + (3 * ball.getRadius()) > getLowestBrickY()
				&& ball.getX() + (3 * ball.getRadius()) < PLAYING_FIELD_HEIGHT
				&& ball.getX() - (2 * ball.getRadius()) > 0 && ball.getY() + (3 * ball.getRadius()) < paddle.getY()) {
			return true;
		}
		return false;
	}

	/**
	 * this method handles a possible collision with a wall. If no Wall is hit,
	 * nothing happens
	 */
	private void handleWallCollision() {
		double x = ball.getVelocity().getX0();
		double y = ball.getVelocity().getX1();

		switch (whichWall()) {
		// right
		case 1:
			ball.setVelocity(new Vector2D(-x, y));
			break;
		// left
		case 2:
			ball.setVelocity(new Vector2D(-x, y));
			break;
		// top
		case 3:
			ball.setVelocity(new Vector2D(x, -y));
			break;
		// other
		case 4:
			handleBrickCollision();
			handlePaddleCollision();
			// no break
			// no collision;
		default:
			moveBall();
		}

	}

	/**
	 * this method handles the collision with a rectangle, which is the paddle
	 * or a brick.
	 * 
	 * @param rect
	 *            the rectangle that is hit.
	 */
	private void handleRectCollision(Rectangle rect) {

		// a vector representing the center of the brick which is hit
		Vector2D brickCenter = new Vector2D(rect.getX() + rect.getHeight() / 2, rect.getY() + rect.getWidth() / 2);
		// a vector representing the center of the ball
		Vector2D ballCenter = new Vector2D(ball.getX() + ball.getRadius(), ball.getY() + ball.getRadius());
		// a vector which points from the ball to the brick
		Vector2D referenceVector = ballCenter.add(brickCenter);

		// get x and y values of the reference vector
		double x = rect.getWidth() / 2 - referenceVector.getX0();
		double y = rect.getHeight() / 2 - referenceVector.getX1();

		double xVel = ball.getVelocity().getX0();
		double yVel = ball.getVelocity().getX1();

		// compare x and y to set velocity (direction)
		if (x < y) {
			ball.setVelocity(new Vector2D(-xVel, yVel));
		} else {
			ball.setVelocity(new Vector2D(xVel, -yVel));
		}
	}

	/**
	 * this method handles a possible collision with a brick
	 */
	private void handleBrickCollision() {
		if (whichBrick() != null) {
			Rectangle brick = whichBrick();
			handleRectCollision(brick);
			bricks.remove(brick);
			state.getEntityList().remove(brick);
		}
	}

	/**
	 * this method handles a possible collision with the paddle
	 */
	private void handlePaddleCollision() {

		// a vector representing the point on the paddle where the ball hits
		Vector2D paddlePoint = new Vector2D(paddle.getX() + ball.getX() + ball.getRadius(), paddle.getY());

		// the slope of the parabola
		double paddleParabolaSlope = getParabolaY(paddlePoint.getX0()) / (2 * paddlePoint.getX0());

		Vector2D normalizedVector = new Vector2D(-1, paddleParabolaSlope);

		double dotProduct = normalizedVector.dotProduct(ball.getVelocity());

		Vector2D normalizedVectorScaled = new Vector2D(-1, paddleParabolaSlope);

		Vector2D mirroredVelocity = ball.getVelocity().sub(normalizedVectorScaled).sub(normalizedVectorScaled);

		ball.setVelocity(mirroredVelocity);
		System.out.println("afga");
	}

	private double getParabolaY(double x) {
		x = getParabolaX(x);
		return paddleParabolaFactor * x * x;
	}

	private double getParabolaX(double x) {
		double offset = paddle.getX() + paddle.getWidth();
		offset = (x < PLAYING_FIELD_WIDTH) ? -offset : offset;
		return offset + (paddle.getWidth() * x / PLAYING_FIELD_WIDTH) / 2.0;
	}

	private boolean rectangleIsHit(Rectangle r) {
		if (ball.getY() + (2 * ball.getRadius()) >= r.getY() && ball.getY() <= r.getY() + r.getHeight()) {
			if (ball.getX() + (2 * ball.getRadius()) >= r.getX() && ball.getX() <= r.getX() + r.getWidth()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * checks which brick is hit and returns it. Returns {@code null} if no
	 * brick is hit
	 */
	private Rectangle whichBrick() {

		for (Rectangle r : bricks) {

			if (rectangleIsHit(r)) {
				return r;
			}
		}

		return null;
	}

	/**
	 * checks which wall is hit and returns a number representing either top,
	 * left , right, or no wall hit
	 */
	private int whichWall() {
		// right
		if (ball.getX() + (2 * ball.getRadius()) >= PLAYING_FIELD_WIDTH) {
			return 1;
		}
		// left
		else if (ball.getX() <= 0) {
			return 2;
		}
		// top
		else if (ball.getY() <= 0) {
			return 3;
		}
		// other
		else {
			return 4;
		}
	}

	/**
	 * returns the y coordinate of the lowest brick on the screen (the one with
	 * the highest y)
	 */
	private double getLowestBrickY() {
		double minY = Double.POSITIVE_INFINITY;
		for (Rectangle r : bricks) {
			minY = (r.getY() < minY) ? r.getY() : minY;
		}
		return minY;
	}

	/**
	 * returns the paddle
	 * 
	 * @return a Rectangle representing the paddle
	 */
	public Rectangle getPaddle() {
		return this.paddle;
	}

	/**
	 * creates the paddle, which is a rectangle
	 */
	private Rectangle createPaddle() {
		Vector2D startingPosition = new Vector2D(PLAYING_FIELD_WIDTH / 2, PLAYING_FIELD_HEIGHT - paddleHeight);
		Rectangle paddle = new Rectangle(startingPosition, paddleLength, paddleHeight);
		return paddle;
	}

	/**
	 * creates the ball
	 */
	private Ball createBall() {
		Ball ball = new Ball(START_POS, RADIUS);
		ball.setVelocity(velocity);
		return ball;
	}

	/**
	 * creates the bricks and stores them in an array
	 */
	private ArrayList<Rectangle> createBricks() {
		ArrayList<Rectangle> bricks = new ArrayList<Rectangle>();
		double brickSpacePerCol = PLAYING_FIELD_WIDTH - (numberOfBrickCols * brickWidth);
		double colPadding = brickSpacePerCol / (numberOfBrickCols + 1);
		double brickSpacePerRow = PLAYING_FIELD_HEIGHT * 0.33 - (numberOfBrickRows * brickHeight);
		double rowPadding = brickSpacePerRow / (numberOfBrickRows + 1);
		double x = colPadding;
		double y = rowPadding;

		for (int i = 0; i < numberOfBrickRows; i++) {
			for (int j = 0; j < numberOfBrickCols; j++) {

				Vector2D position = new Vector2D(x, y);
				Rectangle brick = new Rectangle(position, brickWidth, brickHeight);
				bricks.add(brick);

				x += brickWidth + colPadding;

			}
			y += brickHeight + rowPadding;
			x = colPadding;
		}
		return bricks;
	}

	/**
	 * initializes the GameState
	 */
	private void setGameState() {
		state.setGameOver(false);
		state.setHeight(PLAYING_FIELD_HEIGHT);
		state.setWidth(PLAYING_FIELD_WIDTH);
		ArrayList<Entity> list = state.getEntityList();
		list.addAll(bricks);
		list.add(ball);
		list.add(paddle);
	}

	/**
	 * checks whether the game is still running
	 */
	private boolean isRunning() {
		return ball.getY() < PLAYING_FIELD_HEIGHT && !isPaused;
	}

}

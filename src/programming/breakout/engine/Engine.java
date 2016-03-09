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
	private Paddle paddle;
	private double paddleLength = 0.2 * PLAYING_FIELD_WIDTH;
	private double paddleHeight = 0.1 * paddleLength;

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
	private Vector2D velocity = new Vector2D(0.0, -1.0);
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
			handleCollisions();
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

	private void handleCollisions() {
		handleBrickCollision();
		handlePaddleCollision();
		handleWallCollision();
	}

	/**
	 * checks whether the ball is inside a "save" part of the playing field
	 */
	private boolean noCollisionPossible() {
		if (ball.getY() - ball.getRadius() > getLowestBrickY()
				&& ball.getX() + (3 * ball.getRadius()) < PLAYING_FIELD_WIDTH && ball.getX() - ball.getRadius() > 0
				&& ball.getY() + (3 * ball.getRadius()) < paddle.getY()) {
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
		default:
			// nothing happens
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
			state.remove(brick);
		}
	}

	/**
	 * this method handles a possible collision with the paddle
	 */
	private void handlePaddleCollision() {
		Ball arc = paddle.getPaddleArc();
		if (rectangleIsHit(paddle) && ballIsHit(arc)) {
			Vector2D distance = ball.getCenter().add(arc.getCenter());
			Vector2D norm = distance.divide(distance.getLength());
			double scalar = norm.dotProduct(ball.getVelocity());
			Vector2D dotVector = new Vector2D(scalar, scalar);
			Vector2D mirroredVelocity = ball.getVelocity().sub(dotVector).sub(dotVector);
			ball.setVelocity(mirroredVelocity);
		}

	}

	private boolean ballIsHit(Ball b) {
		// a vector representing the center of the ball
		Vector2D ballCenter = new Vector2D(ball.getX() + ball.getRadius(), ball.getY() + ball.getRadius());
		// a vector representing the center of the ball
		Vector2D collisionCenter = new Vector2D(b.getX() + b.getRadius(), b.getY() + b.getRadius());

		double collisionDistance = ball.getRadius() + b.getRadius();
		Vector2D distance = ballCenter.add(collisionCenter);

		if (distance.getLength() < collisionDistance) {
			return true;
		}
		return false;
	}

	private boolean rectangleIsHit(Rectangle r) {

		// a vector representing the center of the ball
		Vector2D ballCenter = ball.getCenter();
		Vector2D rectCenter = r.getPosition().add(new Vector2D(r.getWidth() / 2.0, r.getHeight() / 2.0));
		Vector2D centerDistance = rectCenter.add(ballCenter);
		double alpha = ballCenter.dotProduct(rectCenter) / ballCenter.getLength() * rectCenter.getLength();
		
		// get x and y values of the reference vector
		double x = r.getWidth() / 2 - centerDistance.getX0();
		double y = r.getHeight() / 2 - centerDistance.getX1();
		
		// ball is to the right
		if (true) {
		double oppositeSite = rectCenter.add(new Vector2D(r.getHeight() / 2.0, 0.0)).getLength();
		} 
		// ball is to the left
		else if (false) {
			
		}
		// ball is on top
		else if (false) {
			
		}
		// ball is on the bottom
		else if (false) {
		}
		else
		{}	
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
		if (ball.getX() + (2 * ball.getRadius()) >= state.getWidth()) {
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
		double maxY = Double.NEGATIVE_INFINITY;
		for (Rectangle r : bricks) {
			maxY = (r.getY() + r.getHeight() > maxY) ? r.getY() + r.getHeight() : maxY;
		}
		return maxY;
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
	private Paddle createPaddle() {
		Vector2D startingPosition = new Vector2D(state.getWidth() / 2, state.getHeight() - paddleHeight);
		Paddle paddle = new Paddle(startingPosition, paddleLength, paddleHeight, createPaddleArc());
		return paddle;
	}

	/**
	 * creates the paddle collision arc
	 */
	private Ball createPaddleArc() {
		double arcRadius = ((paddleHeight * paddleHeight) + (paddleLength * paddleLength / 4)) / (2 * paddleHeight);
		double x = (paddleLength / 2) - arcRadius;
		double y = paddleHeight;
		Vector2D position = new Vector2D(x, y);
		return new Ball(position, arcRadius);
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
		double brickSpacePerCol = state.getWidth() - (numberOfBrickCols * brickWidth);
		double colPadding = brickSpacePerCol / (numberOfBrickCols + 1);
		double brickSpacePerRow = state.getHeight() * 0.33 - (numberOfBrickRows * brickHeight);
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
		return ball.getY() < state.getHeight() && !isPaused;
	}

}

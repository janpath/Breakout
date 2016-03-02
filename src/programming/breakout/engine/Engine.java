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
	private static final double PLAYING_FIELD_HEIGHT = 100;
	private static final double PLAYING_FIELD_WIDTH = 40;

	/**
	 * Paddle
	 */
	private Rectangle paddle = createPaddle();
	private double paddleLength = 0.2 * PLAYING_FIELD_WIDTH;
	private double paddleHeight = 0.1 * paddleLength;

	/**
	 * Bricks
	 */
	private ArrayList<Rectangle> bricks;
	private int numberOfBrickRows = 12;
	private int numberOfBrickCols = 6;
	private double brickPaddingX = 0;
	private double brickPaddingY = 0;

	/**
	 * Ball
	 */
	private static final Vector2D START_POS = new Vector2D(PLAYING_FIELD_WIDTH / 2, PLAYING_FIELD_HEIGHT / 2);
	private static final double RADIUS = 2;
	/* Velocity in units per frame */
	private Vector2D velocity = new Vector2D(0.0, -2.0);
	private Ball ball = createBall();

	/**
	 * How much to wait between each frame
	 */
	private static final int REFRESH_RATE = 20;

	private GameState state;

	public Engine(GameState state) {
		this.state = state;
	}

	@Override
	public void run() {
		setGameState();
		createBall();
		createPaddle();
		this.bricks = createBricks();
		
		while (isRunning()) {
			long start = System.currentTimeMillis();
			moveBallIf();
			updateEntityList();
			
			System.out.println("tick");
			
			state.setChanged();
			state.notifyObservers();
			
			long elapsed = start - System.currentTimeMillis();

			try {
				Thread.sleep(REFRESH_RATE - elapsed);
			} catch (InterruptedException ex) {
			}
		}

	}

	
	/**
	 * this method updates the entity list from GameState about changes 	
	 */
	private void updateEntityList() {
		ArrayList<Entity> list = state.getEntityList();
		list.clear();
		list.addAll(bricks);
		list.add(ball);
		list.add(paddle);
	}
	
	/**
	 * this method moves the ball while adding the velocity to the position, if no collision is possible
	 */
	private void moveBallIf() {
		
		// the ball is far enough away from everything 
		if (noCollisionPossible()) {
			moveBall();
		} 
		// is a wall hit?
		else if (whichWall() < 4) {
			handleWallCollision();
		}
		// is a brick hit?
		else if (whichBrick() != null) {
			handleBrickCollision();
		}
		// nothing is hit
		else {
			moveBall();
		}
	}
	
	/**
	 * this method moves the ball, not asking for collisions 
	 */
	private void moveBall() {
		Vector2D newPosition = ball.getPosition().add(ball.getVelocity());
		ball.setPosition(newPosition);
	}
	
	/**
	 * this method handles a possible collision with a wall. If no Wall is hit, nothing happens
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
			// no break
		// no collision;	
		default:
			moveBall();
		}		

	}
	
	/**
	 * this method handles a possible collision with a brick 
	 */
	private void handleBrickCollision() {
		if (whichBrick() != null) {
			Rectangle brick = whichBrick();

			Vector2D brickCenter = new Vector2D(brick.getX() + brick.getHeight() / 2,
					brick.getY() + brick.getWidth() / 2);
			Vector2D ballCenter = new Vector2D(ball.getX() + ball.getRadius(), ball.getY() + ball.getRadius());
			Vector2D referenceVector = ballCenter.add(brickCenter);

			double x = brick.getWidth() / 2 - referenceVector.getX0();
			double y = brick.getHeight() / 2 - referenceVector.getX1();

			double xVel = ball.getVelocity().getX0();
			double yVel = ball.getVelocity().getX1();

			if (x < y) {
				ball.setVelocity(new Vector2D(-xVel, yVel));
			} else {
				ball.setVelocity(new Vector2D(xVel, -yVel));
			}

			bricks.remove(brick);
		}

	}
	
	/**
	 * checks which brick is hit and returns it
	 */
	private Rectangle whichBrick() {

		for (Rectangle r : bricks) {

			if (ball.getY() + (2 * ball.getRadius()) >= r.getY() && ball.getY() <= r.getY() + r.getHeight()) {
				if (ball.getX() + (2 * ball.getRadius()) >= r.getX() && ball.getX() <= r.getX() + r.getWidth()) {
					return r;
				}
			}
		}
		return null;
	}
	
	/**
	 * checks which wall is hit and returns a number representing either top, left , right, or no wall hit
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
	 * checks whether the ball is far enough away from anything which it can collide with
	 */
	private boolean noCollisionPossible() {
		// no collision possible
		if (ball.getY() < getLowestBrickY() && ball.getX() + (2 * ball.getRadius()) < PLAYING_FIELD_WIDTH
				&& ball.getX() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 *  returns the y coordinate of the lowest brick on the screen (the one with the highest y)
	 */
	private double getLowestBrickY() {
		double minY = Double.POSITIVE_INFINITY;
		for (Rectangle r : bricks) {
			minY = (r.getY() < minY) ? r.getY() : minY;
		}
		return minY;
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
		double brickSpacePerCol = PLAYING_FIELD_WIDTH - (brickPaddingX * numberOfBrickCols + 1);
		double brickWidth = brickSpacePerCol / (numberOfBrickCols);
		double brickSpacePerRow = PLAYING_FIELD_HEIGHT * 0.33 - (brickPaddingY * numberOfBrickRows + 1);
		double brickHeight = brickSpacePerRow / (numberOfBrickRows);

		for (int i = 0; i < numberOfBrickRows; i++) {

			double x = (i * brickWidth) + brickPaddingX;

			for (int j = 0; j < numberOfBrickCols; j++) {

				double y = (j * brickHeight) + brickPaddingY;
				Vector2D position = new Vector2D(x, y);
				Rectangle brick = new Rectangle(position, brickHeight, brickWidth);
				bricks.add(brick);
			}

		}
		return bricks;
	}
	
	/**
	 * initializes the GameState
	 */
	private void setGameState() {
		state.setHeight(PLAYING_FIELD_HEIGHT);
		state.setWidth(PLAYING_FIELD_WIDTH);
	}

	/**
	 * checks whether the game is still running
	 */
	private boolean isRunning() {
		return ball.getY() < PLAYING_FIELD_HEIGHT && !isPaused;
	}

}

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
	private static final double RADIUS = 1;
	private static final Vector2D START_POS = new Vector2D(PLAYING_FIELD_WIDTH/2 - RADIUS, PLAYING_FIELD_HEIGHT/2 - RADIUS);
	/* Velocity in units per frame */
	private Vector2D velocity = new Vector2D(0.0, 2);
	private Ball ball;

	/**
	 * How much to wait between each frame
	 */
	private static final int REFRESH_RATE = 20;

	private GameState state;

	public Engine(GameState state) {
		this.state = state;
		state.setEngine(this);
		state.setHeight(PLAYING_FIELD_HEIGHT);
		state.setWidth(PLAYING_FIELD_WIDTH);
		this.paddle = createPaddle();
	}

	@Override
	public void run() {

		while (true) {
			paddle.setPosition(new Vector2D(( state.getWidth()-paddle.getWidth() )/2, state.getHeight() - paddle.getHeight()*2));
			setGameState();

			for(int i=0; i < 1000; i += 20) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException ex) {
				}
				state.endTick();
			}



			while (isRunning()) {
				long start = System.currentTimeMillis();
				if (!state.isPaused()) {
					moveBall();
				}
				state.endTick();

				long elapsed = start - System.currentTimeMillis();

				try {
					Thread.sleep(REFRESH_RATE - elapsed);
				} catch (InterruptedException ex) {
				}
			}

			state.remove(paddle);
			state.endTick();

			try {
				Thread.sleep(2000);
			} catch (InterruptedException ex) {
			}
		}
	}

	/**
	 * this method moves the ball
	 */
	private void moveBall() {
		Vector2D newPosition = ball.getPosition().add(ball.getVelocity().scale(state.getTimeFactor()));
		ball.setPosition(newPosition);
		state.addMoved(ball);

    handleCollisions();
	}

	private void handleCollisions() {
		// See if we collide with anything and get the vector that would move the
		// ball out of collision
		Vector2D outOfCollisonVector;
		if((outOfCollisonVector = getWallCollision()) != null ||
		   (outOfCollisonVector = getPaddleCollision()) != null ||
		   (outOfCollisonVector = getBrickCollison()) != null) {
			collisionResponse(outOfCollisonVector);
		}
	}

	private void collisionResponse(Vector2D outOfCollisonVector) {
	// First move the ball out of collison
	ball.setPosition(ball.getPosition().add(outOfCollisonVector));

	Vector2D norm = outOfCollisonVector.scale(1/outOfCollisonVector.getLength());
	double scalar = norm.dotProduct(ball.getVelocity());
	Vector2D dotVector = norm.scale(scalar*2);
	Vector2D mirroredVelocity = ball.getVelocity().sub(dotVector);
	ball.setVelocity(mirroredVelocity);
}

/**
 * this method handles a possible collision with a wall. If no Wall is hit,
 * nothing happens
 */
private Vector2D getWallCollision() {
	Vector2D outOfCollisonVector = new Vector2D(Math.max(0, -ball.getX()) + Math.min(0, state.getWidth() - ( ball.getX() + 2*ball.getRadius())) ,
	                                            Math.max(0, -ball.getY()));

	return outOfCollisonVector.equals(new Vector2D(0, 0)) ? null : outOfCollisonVector;
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
private Vector2D getBrickCollison() {
	if (ball.getY() > getLowestBrickY()) {
		return null;
	}

	Pair<Rectangle, Vector2D> collisionInfo = whichBrick();
	if (collisionInfo != null) {
		bricks.remove(collisionInfo.getLeft());
		state.remove(collisionInfo.getLeft());
		return collisionInfo.getRight();
	}
	return null;
}

/**
 * this method handles a possible collision with the paddle
 */
private Vector2D getPaddleCollision() {
	if(rectangleIsHit(paddle) != null) {
		Vector2D outOfCollisionVector =
			ballIsHit(new Ball(paddle.getArcCenter()
			                   .sub(new Vector2D(paddle.getRadius(),
			                                     paddle.getRadius())),
			                   paddle.getRadius()));

		if(outOfCollisionVector != null) {
			if(outOfCollisionVector.getX1()/outOfCollisionVector.getLength()*paddle.getRadius()
			   > paddle.getHeight() - paddle.getRadius()) {
				//The ball actually collided with the corner of the paddle
				Vector2D rectBottomMiddle = paddle.getPosition()
					.add(new Vector2D(paddle.getWidth()/2, paddle.getHeight()));
				Vector2D diff = ball.getCenter().sub(rectBottomMiddle);
				Vector2D cornerDiff =
					diff.sub(new Vector2D(Math.copySign(paddle.getWidth()/2,
					                                    diff.getX0()),
					                      0));
				if(cornerDiff.getLength() > ball.getRadius()) {
					//Actually it's not colliding at all
					return null;
				} else {
					return cornerDiff.scale(ball.getRadius()/cornerDiff.getLength() - 1);
				}
			}

			return outOfCollisionVector;
		}
	}

	return null;
}

/**
 * Checks if the playing ball overlaps with the given ball.
 * @return the shortest vector that moves the playing ball out of collision.
 */
private Vector2D ballIsHit(Ball b) {
	// a vector representing the center of the ball
	Vector2D ballCenter = new Vector2D(ball.getX() + ball.getRadius(), ball.getY() + ball.getRadius());
	// a vector representing the center of the ball
	Vector2D collisionCenter = new Vector2D(b.getX() + b.getRadius(), b.getY() + b.getRadius());

	double collisionDistance = ball.getRadius() + b.getRadius();
	Vector2D distance = ballCenter.sub(collisionCenter);

	double overlapLength;
	if (( overlapLength = distance.getLength() - collisionDistance ) < 0) {
		return distance.scale(-overlapLength/distance.getLength());
	}
	return null;
}

/**
 * Checks if the ball hit the given rectangle.
 * @return the shortest vector, that moves the ball out of the overlap
 */
private Vector2D rectangleIsHit(Rectangle r) {
	// a vector representing the center of the ball
	Vector2D ballCenter = ball.getCenter();
	Vector2D rectCenter = r.getPosition().add(new Vector2D(r.getWidth() / 2.0, r.getHeight() / 2.0));
	Vector2D centerDistance = ballCenter.sub(rectCenter);
	Vector2D absDistance = new Vector2D(Math.abs(centerDistance.getX0()),
	                                    Math.abs(centerDistance.getX1()));

	double overlapLength;
	if (( absDistance.getX0() >= r.getWidth()/2 + ball.getRadius() ) ||
	    ( absDistance.getX1() >= r.getHeight()/2 + ball.getRadius() )) {
		//The x or y coordinate difference is already bigger than the balls radius
		return null;
	} else if (absDistance.getX0() < r.getWidth()/2) {
		//Ball overlaps the top or bottom
		return new Vector2D(0, Math.copySign(absDistance.getX1() - (r.getHeight()/2 + ball.getRadius()),
		                                     centerDistance.getX1()));
	} else if (absDistance.getX1() < r.getHeight()/2) {
		//Ball overlaps the left or right edge
		return new Vector2D(Math.copySign(absDistance.getX0() - (r.getWidth()/2 +  ball.getRadius() ),
		                                  centerDistance.getX0()), 0);
	} else {
		Vector2D cornerDistance = absDistance.sub(new Vector2D(r.getWidth(), r.getHeight()).scale(.5));
		if ((overlapLength = cornerDistance.getLength() - ball.getRadius()) < 0) {
			//Ball overlaps the corner
			return new Vector2D(Math.copySign(cornerDistance.getX0(), centerDistance.getX0()),
			                    Math.copySign(cornerDistance.getX1(), centerDistance.getX1()))
				.scale(-overlapLength/cornerDistance.getLength());
		} else {
			return null;
		}
	}
}

/**
 * checks which brick is hit and returns it.
 * @return Pair of entity hit and the unit normal to the surface the ball hit pointing outwards.
 */
private Pair<Rectangle, Vector2D> whichBrick() {
	for (Rectangle r : bricks) {
		Vector2D axis;
		if ((axis = rectangleIsHit(r)) != null) {
			return new Pair<Rectangle, Vector2D>(r, axis);
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
	Vector2D startingPosition = new Vector2D((state.getWidth()-paddleLength)/2, state.getHeight() - paddleHeight*2);
	return new Paddle(startingPosition, paddleLength, paddleHeight);
}

/**
 * creates the ball
 */
private Ball createBall() {
	Ball ball = new Ball(START_POS, RADIUS);
	ball.setVelocity(velocity.rotate(Math.random()*Math.PI/2 - Math.PI/4));
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
	ArrayList<Entity> list = state.getEntityList();
	this.bricks = createBricks();
	this.ball = createBall();
	list.clear();
	list.addAll(bricks);
	list.add(ball);
	list.add(paddle);
	state.setChanged();
	state.notifyObservers();
}

/**
 * checks whether the game is still running
 */
private boolean isRunning() {
	return ball.getY() < state.getHeight()*1.1 && !isPaused;
}
}

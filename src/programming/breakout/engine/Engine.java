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
   * Playing field
   */
  private static final double PLAYING_FIELD_HEIGHT = 130;
  private static final double PLAYING_FIELD_WIDTH = 80;

  /**
   * Paddle
   */
  private Paddle paddle;
  private static final double PADDLE_LENGTH = 0.2 * PLAYING_FIELD_WIDTH;
  private static final double PADDLE_HEIGHT = 0.1 * PADDLE_LENGTH;

  /**
   * Bricks
   */
  private ArrayList<Rectangle> bricks;
  private static final int NUMBER_OF_BRICK_ROWS = 6;
  private static final int NUMBER_OF_BRICK_COLS = 7;
  private static final int BRICK_WIDTH = 8;
  private static final int BRICK_HEIGHT = 4;

  /**
   * Ball
   */
  private static final double RADIUS = 1;
  private static final Vector2D START_POS =
    new Vector2D(PLAYING_FIELD_WIDTH / 2 - RADIUS,
                 PLAYING_FIELD_HEIGHT / 2 - RADIUS);
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

    // Restart the game until the player managed too destroy all the pour
    // little
    // bricks
    while (!state.isGameOver()) {
      // Initialise everything
      ArrayList<Entity> list = state.getEntityList();
      this.bricks = createBricks();
      this.ball = createBall();
      list.clear();
      list.addAll(bricks);
      list.add(ball);
      list.add(paddle);

      // Center paddle
      paddle.setPosition
	      (new Vector2D((state.getWidth() - paddle.getWidth()) / 2,
                      state.getHeight() - paddle.getHeight() * 2));

      // Notify observers of state without delta
      state.endTick(false);

      // Wait a few ticks before starting the game
      for (int i = 0; i < 1000; i += 20) {
        try {
          Thread.sleep(20);
        } catch (InterruptedException ex) {
        }
        state.endTick();
      }

      while (ballInField() && !state.isGameOver()) {
        long start = System.currentTimeMillis();

        if (!state.isPaused()) {
          moveBall();
        }
        state.endTick();

        if (gameOver()) {
          state.setGameOver(true);
        }

        long elapsed = start - System.currentTimeMillis();

        try {
          Thread.sleep(REFRESH_RATE - elapsed);
        } catch (InterruptedException ex) {
        }
      }

      // When the ball fall out of the playing field the paddle is
      // temporarily
      // destroyed
      state.remove(paddle);
      state.endTick();

      // Wait two seconds before restarting the game
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
    // Move ball, then handle collisions, so to not have frames, where the
    // ball
    // is overlapping something.
    Vector2D newPosition =
	    ball.getPosition().add(ball.getVelocity().scale(state.getTimeFactor()));

    ball.setPosition(newPosition);
    state.addMoved(ball);

    handleCollisions();
  }

  private void handleCollisions() {
    // See if we collide with anything and get the vector that would move
    // the
    // ball out of collision
    Vector2D outOfCollisonVector;
    if ((outOfCollisonVector = getWallCollision()) != null ||
        (outOfCollisonVector = getPaddleCollision()) != null ||
        (outOfCollisonVector = getBrickCollison()) != null) {
      collisionResponse(outOfCollisonVector);
    }
  }

  private void collisionResponse(Vector2D outOfCollisonVector) {
    // First move the ball out of collison
    ball.setPosition(ball.getPosition().add(outOfCollisonVector));

    // Mirror the velocity of the ball over the axis orthogonal to the
    // outOfCollisonVector
    Vector2D norm = outOfCollisonVector // Normalise vector
      .scale(1 / outOfCollisonVector.getMagnitude());
    // Get the velocity in the direction of the normal vector
    double scalar = norm.dotProduct(ball.getVelocity());

    // Scale the normal vector by that velocity
    Vector2D mirroredDiff = norm.scale(scalar * 2);
    Vector2D mirroredVelocity = ball.getVelocity().sub(mirroredDiff);
    ball.setVelocity(mirroredVelocity);
  }

  /**
   * this method handles a possible collision with a wall. If no Wall is hit,
   * nothing happens
   */
  private Vector2D getWallCollision() {
    Vector2D outOfCollisonVector =
	    new Vector2D(Math.max(0, -ball.getX()) +
	                 Math.min(0, state.getWidth()
	                          - (ball.getX() + 2 * ball.getRadius())),
                   Math.max(0, -ball.getY()));

    // If the ball is already in the playing field, return null instead of
    // the
    // 0 vector
    return outOfCollisonVector.equals(new Vector2D(0, 0))
	    ? null : outOfCollisonVector;
  }

  /**
   * this method handles a possible collision with a brick
   *
   * @return the shortest vector that moves the ball out of collision or
   *         {@code null} if no collision was detected.
   */
  private Vector2D getBrickCollison() {
    // First make a broad collision check
    if (ball.getY() > getLowestBrickY()) {
      return null;
    }

    // Test all bricks for collision. Only handle first.
    Pair<Rectangle, Vector2D> collisionInfo = null;
    for (Rectangle r : bricks) {
      Vector2D axis;
      if ((axis = rectangleIsHit(r)) != null) {
        collisionInfo = new Pair<Rectangle, Vector2D>(r, axis);
        break;
      }
    }

    // If a collision was detected, remove that brick
    if (collisionInfo != null) {
      bricks.remove(collisionInfo.getLeft());
      state.remove(collisionInfo.getLeft());
      return collisionInfo.getRight();
    } else {
      return null;
    }
  }

  /**
   * this method handles a possible collision with the paddle
   */
  private Vector2D getPaddleCollision() {
    // First check if the bounding rectangle was hit
    if (rectangleIsHit(paddle) != null) {
      // Check if the circle was hit
      Vector2D outOfCollisionVector =
	      ballIsHit(new Ball(paddle.getArcCenter()
	                         .sub(new Vector2D(paddle.getRadius(),
	                                           paddle.getRadius())),
                           paddle.getRadius()));

      if (outOfCollisionVector != null) {
        if (outOfCollisionVector.getX1() / outOfCollisionVector.getMagnitude()
            * paddle.getRadius() > paddle.getHeight() - paddle.getRadius()) {
          // The ball actually collided with the corner of the paddle
          Vector2D rectBottomMiddle = paddle.getPosition()
            .add(new Vector2D(paddle.getWidth() / 2, paddle.getHeight()));
          Vector2D diff = ball.getCenter().sub(rectBottomMiddle);
          Vector2D cornerDiff =
	          diff.sub(new Vector2D(Math.copySign(paddle.getWidth() / 2,
	                                              diff.getX0()), 0));
          if (cornerDiff.getMagnitude() > ball.getRadius()) {
            // Actually it didn't collide at all
            return null;
          } else {
            return
	            cornerDiff.scale(ball.getRadius()/cornerDiff.getMagnitude() - 1);
          }
        }

        return outOfCollisionVector;
      }
    }

    return null;
  }

  /**
   * Checks if the playing ball overlaps with the given ball.
   * 
   * @return the shortest vector that moves the playing ball out of collision.
   */
  private Vector2D ballIsHit(Ball b) {
    // a vector representing the center of the ball
    Vector2D ballCenter = new Vector2D(ball.getX() + ball.getRadius(),
                                       ball.getY() + ball.getRadius());
    // a vector representing the center of the ball
    Vector2D collisionCenter = new Vector2D(b.getX() + b.getRadius(),
                                            b.getY() + b.getRadius());

    Vector2D centerDistance = ballCenter.sub(collisionCenter);

    double overlapLength =
	    ball.getRadius() + b.getRadius() - centerDistance.getMagnitude();
    if (overlapLength > 0) {
      return
	      centerDistance.scale(overlapLength / centerDistance.getMagnitude());
    }
    return null;
  }

  /**
   * Checks if the ball hit the given rectangle.
   * 
   * @return the shortest vector, that moves the ball out of the overlap or
   *         {@code null} if no collision was detected
   */
  private Vector2D rectangleIsHit(Rectangle r) {
    // a vector representing the center of the ball
    Vector2D ballCenter = ball.getCenter();
    Vector2D rectCenter =
	    r.getPosition().add(new Vector2D(r.getWidth() / 2d, r.getHeight() / 2d));
    Vector2D centerDistance = ballCenter.sub(rectCenter);
    Vector2D absDistance = new Vector2D(Math.abs(centerDistance.getX0()),
                                        Math.abs(centerDistance.getX1()));

    if ((absDistance.getX0() >= r.getWidth() / 2 + ball.getRadius())
        || (absDistance.getX1() >= r.getHeight() / 2 + ball.getRadius())) {
      // The x or y coordinate difference is already bigger than the balls
      // radius
      return null;

    } else if (absDistance.getX0() < r.getWidth() / 2) {
      // Ball overlaps the top or bottom
      return
	      new Vector2D(0, Math.copySign(absDistance.getX1()
	                                    - (r.getHeight() / 2 + ball.getRadius()),
                                           centerDistance.getX1()));

    } else if (absDistance.getX1() < r.getHeight() / 2) {
      // Ball overlaps the left or right edge
      return
	      new Vector2D(Math.copySign(absDistance.getX0()
	                                 - (r.getWidth() / 2 + ball.getRadius()),
	                                 centerDistance.getX0()), 0);

    } else {
      Vector2D cornerDistance =
	      absDistance.sub(new Vector2D(r.getWidth(),
                                     r.getHeight()).scale(.5));
      double overlapLength = ball.getRadius() - cornerDistance.getMagnitude();
      if (overlapLength > 0) {
        // Ball overlaps the corner
        return new Vector2D(Math.copySign(cornerDistance.getX0(),
                                          centerDistance.getX0()),
                            Math.copySign(cornerDistance.getX1(),
                                          centerDistance.getX1()))
          .scale(overlapLength / cornerDistance.getMagnitude());
      } else {
        return null;
      }
    }
  }

  /**
   * returns the y coordinate of the lowest brick on the screen (the one with
   * the highest y)
   */
  private double getLowestBrickY() {
    double maxY = Double.NEGATIVE_INFINITY;
    for (Rectangle r : bricks) {
      maxY = (r.getY() + r.getHeight() > maxY) ?
	      r.getY() + r.getHeight() : maxY;
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
    Vector2D startingPosition =
	    new Vector2D((state.getWidth() - PADDLE_LENGTH) / 2,
                   state.getHeight() - PADDLE_HEIGHT * 2);
    return new Paddle(startingPosition, PADDLE_LENGTH, PADDLE_HEIGHT);
  }

  /**
   * creates the ball
   */
  private Ball createBall() {
    Ball ball = new Ball(START_POS, RADIUS);
    ball.setVelocity(velocity.rotate(Math.random() * Math.PI/2 - Math.PI/4));
    return ball;
  }

  /**
   * creates the bricks and stores them in an array
   */
  private ArrayList<Rectangle> createBricks() {
    ArrayList<Rectangle> bricks = new ArrayList<Rectangle>();
    double brickSpacePerCol =
	    state.getWidth() - (NUMBER_OF_BRICK_COLS * BRICK_WIDTH);
    double colPadding = brickSpacePerCol / (NUMBER_OF_BRICK_COLS + 1);
    double brickSpacePerRow =
	    state.getHeight()/3d - (NUMBER_OF_BRICK_ROWS * BRICK_HEIGHT);
    double rowPadding = brickSpacePerRow / (NUMBER_OF_BRICK_ROWS + 1);
    double x = colPadding;
    double y = rowPadding;

    for (int i = 0; i < NUMBER_OF_BRICK_ROWS; i++) {
      for (int j = 0; j < NUMBER_OF_BRICK_COLS; j++) {

        Vector2D position = new Vector2D(x, y);
        Rectangle brick = new Rectangle(position, BRICK_WIDTH, BRICK_HEIGHT);
        bricks.add(brick);

        x += BRICK_WIDTH + colPadding;

      }
      y += BRICK_HEIGHT + rowPadding;
      x = colPadding;
		}
		return bricks;
	}

	/**
	 * checkes whether the ball is still in the playing field or only slightly
	 * out of it.
	 */
	private boolean ballInField() {
		return ball.getY() < state.getHeight() * 1.1;
	}

	/**
	 * Check if the game is over
	 */
	private boolean gameOver() {
		return bricks.size() == 0;
	}
}

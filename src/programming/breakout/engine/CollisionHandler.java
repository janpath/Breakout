package programming.breakout.engine;

public class CollisionHandler {

	private double getParabolaY(double x) {
		x = getParabolaX(x);
		return paddleParabolaFactor * x * x;
	}
	
	private double getParabolaX(double x) {
		double offset = paddle.getX() + paddle.getWidth();
		offset = (x < PLAYING_FIELD_WIDTH) ? -offset : offset;
		return offset + (paddle.getWidth() * x / PLAYING_FIELD_WIDTH) /2.0;
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
	 * this method handles the collision with a rectangle, which is the paddle or
	 * a brick.
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
	
}

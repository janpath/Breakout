package programming.breakout.engine;

public class Paddle extends Rectangle {

	private Ball paddleArc;

	public Paddle(Vector2D position, double width, double height, Ball arc) {
		super(position, width, height);
		this.paddleArc = arc;
	}

	public void setPaddleArc(Ball paddleArc) {
		this.paddleArc = paddleArc;
	}

	public Ball getPaddleArc() {
		return paddleArc;
	}

	public double getRadius() {
		return paddleArc.getRadius();
	}


	public double getAngle() {
		return Math.asin((getWidth()/2) / (getRadius() - getHeight())) * 2;
	}
}

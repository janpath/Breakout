package programming.breakout.engine;

public class Paddle extends Rectangle {
	
	private Ball paddleArc; 
	
	private void setPaddleArc(Ball paddleArc) {
		this.paddleArc = paddleArc;
	}
	
	private Ball getPaddleArc() {
		return paddleArc;
	}
	
}

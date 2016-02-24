package programming.breakout.engine;

import programming.breakout.engine.Entity;

public class Rectangle extends Entity {

	/**
	 * instance variables
	 */
	private double height = 0;
	private double width = 0;

	/**
	 * primary constructor
	 */
	Rectangle(Vector2D position, double height, double width) {
		this.setPosition(position);
		this.height = height;
		this.width = width;
	}

	/**
	 * Returns the height of the Rectangle
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Sets the height of the Rectangle
	 */
	void setHeight(double height) {
		this.height = height;
	}

	/**
	 * Returns the width of the Rectangle
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Sets the width of the Rectangle
	 */
	void setWidth(double width) {
		this.width = width;
	}

}

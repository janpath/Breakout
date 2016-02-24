package programming.breakout;

import java.awt.Color;

import acm.graphics.GCompound;
import acm.graphics.GRect;

public class Bricks extends GCompound {

	private Color color = Color.BLUE;
	private double brickWidth = 60;
	private double brickHeight = 20;
	private final double WALL_HEIGHT;
	private final double WALL_WIDTH;
	private double margin = 20;
	private double padding = 10;

	public Bricks(int rows, int cols) {

		WALL_HEIGHT = (brickHeight + padding) * rows;
		WALL_WIDTH = Breakout.WIDTH - margin;

		this.add( createWallOfBricks(cols, rows) );
	}

	private GCompound createWallOfBricks(int cols, int rows) {

		GCompound wall = new GCompound();
		double y = (padding + margin) / 2;
		for (int i = 0; i < rows; i++) {
			y += brickHeight + padding;
			wall.add( createRowOfBricks(cols, y) );
		}

		return wall;
	}

	private GCompound createRowOfBricks(int numberOfBricks, double y) {

		GCompound row = new GCompound();
		double x = (padding + margin) / 2;
		for (int i = 0; i < numberOfBricks; i++) {
			x += brickWidth + padding ;
			row.add( createBrick(x, y, color) );
		}

		return row;
	}

	private GRect createBrick(double x, double y, Color color) {

		GRect brick = new GRect(x, y, brickWidth, brickHeight);
		brick.setFilled(true);
		brick.setColor(color);
		return brick;
	}


	public void setBrickWidth (double brickWidth) {
		this.brickWidth = brickWidth;
	}

	public void setBrickHeight (double brickHeight) {
		this.brickHeight = brickHeight;
	}

	public void setPadding (double padding) {
		this.padding = padding;
	}

	public void setMargin(double margin) {
		this.margin = margin;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

}

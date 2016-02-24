package programming.breakout;

import acm.graphics.GCompound;
import acm.graphics.GRect;
import acm.graphics.GLabel;

public class Interface extends GCompound {

	
	/**
	 * Instance variables that specify the margin size
	 */
	private double margin_top = 10;
	private double margin_bot = 10;
	private double margin_left = 10;
	private double margin_right = 100;
	
	/**
	 * Instance variables that specify the playingfield size
	 */
	private double fieldHeight = 800;
	private double fieldWidth = 500;
	private double fieldX = margin_left;
	private double fieldY = margin_top;

	/**
	 * Instance variables for every Object in the Interface
	 */

	
	public Interface () {
		this.add(createMargin());
		this.add(createField());
//		this.add(scores);
	}
	
//	private void createScores() {
//		GLabel scores = new GLabel("Scores");
//		scores.setLocation();
//		this.scores = scores;
//	}
	
	private GRect createMargin() {
		GRect margin = new GRect(0, 0, fieldWidth + margin_right + margin_left, fieldHeight + margin_top + margin_bot);
		margin.setVisible(false);
		return margin;
	}
	
	private GRect createField() {
		GRect field = new GRect(fieldX, fieldY, fieldWidth, fieldHeight);		
		return field;
	}
	
	public void setFieldX (double x) {
		this.fieldX = x;
	}
	
	public void setFieldY (double y) {
		this.fieldY = y;
	}
	
	public double getFieldX () {
		return fieldX;
	}
	
	public double getFieldY () {
		return fieldY;
	}
	
	public void setFieldWidth(double width) {
		this.fieldWidth = width;
	}
	
	public void setFieldHeight(double height) {
		this.fieldHeight = height;
	}
	
	public double getFieldWidth() {
		return fieldWidth;
	}
	
	public double getFieldHeight() {
		return fieldHeight;
	}
	
	
}

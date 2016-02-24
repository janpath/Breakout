package programming.breakout;

import acm.graphics.GCompound;
import acm.graphics.GRect;
import acm.graphics.GLabel;

public class Interface extends GCompound {

	/**
	 * Instance variables that specify the margin size
	 */
	private static double margin_top = 10;
	private static double margin_bot = 10;
	private static double margin_left = 10;
	private static double margin_right = 100;

	/**
	 * Instance variables that specify the playingfield size
	 */
	private static double fieldHeight = 800;
	private static double fieldWidth = 500;
	private static double fieldX = margin_left;
	private static double fieldY = margin_top;

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

	public static void setFieldX (double x) {
		fieldX = x;
	}

	public static void setFieldY (double y) {
		fieldY = y;
	}

	public static double getFieldX () {
		return fieldX;
	}

	public static double getFieldY () {
		return fieldY;
	}

	public static void setFieldWidth(double width) {
		fieldWidth = width;
	}

	public static void setFieldHeight(double height) {
		fieldHeight = height;
	}

	public static double getFieldWidth() {
		return fieldWidth;
	}

	public static double getFieldHeight() {
		return fieldHeight;
	}

}

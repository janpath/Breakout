package programming.breakout.engine;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Save all the GameState relevant to the view.
 */
public class GameState extends Observable {

	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private boolean paused = false;
	private int score = 0;

	private double width, height;

	/**
	 * Get a list of all the objects on the playing field
	 * @return List of objects on playing field
	 */
	public ArrayList<Entity> getEntityList() {
		return entities;
	}

	/**
	 * Whether the game is paused or running
	 * @return {@code true} if the game is paused, {@false} if it is running
	 */
	public boolean getPaused() {
		return paused;
	}

	/**
	 * Set whether the game is paused or running.
	 * @param paused {@code true} will pause the game, {@code false} unpause it.
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}

	/**
	 * @return the width of the playing field
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	void setWidth(double width) {
		this.width = width;
	}

	/**
	 * @return the height of the playing field
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	void setHeight(double height) {
		this.height = height;
	}
}

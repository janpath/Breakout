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
import java.util.Observable;
import java.util.HashSet;

import programming.breakout.engine.Pair;

/**
 * Save all the GameState relevant to the view.
 */
public class GameState extends Observable {

	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private boolean paused = true, gameOver = false;
	private int score = 0;
	private double width, height;
	private GameDelta delta = new GameDelta();
	private double timeFactor = 1;
	private Controller controller;
	private Engine engine;

	/**
	 * Contains information about what changed since last time
	 */
	public static class GameDelta {
		public ArrayList<Entity> entitiesDestroyed;
		public ArrayList<Entity> entitiesAdded;
		public HashSet<Entity> entitiesMoved;
		public int scoreDelta;
		public boolean pausedToggled, gameOverToggled;

		GameDelta(ArrayList<Entity> entitiesDestroyed,
		          ArrayList<Entity> entitiesAdded,
		          HashSet<Entity> entitiesMoved,
		          int scoreDelta,
		          boolean pausedToggled,
		          boolean gameOverToggled) {
			this.entitiesDestroyed = entitiesDestroyed;
			this.entitiesMoved = entitiesMoved;
			this.scoreDelta = scoreDelta;
			this.pausedToggled = pausedToggled;
			this.gameOverToggled = gameOverToggled;
		}

		GameDelta() {
			entitiesDestroyed = new ArrayList<Entity>();
			entitiesAdded = new ArrayList<Entity>();
			entitiesMoved = new HashSet<Entity>();
		}
	}

	/**
	 * Add an entity to the list of moved entities for the next game delta
	 */
	protected void addMoved(Entity e) {
		delta.entitiesMoved.add(e);
		setChanged();
	}


	/**
	 * Add an item to the playing field
	 */
	protected void add(Entity e) {
		entities.add(e);
		delta.entitiesAdded.add(e);
		setChanged();
	}

	/**
	 * Remove an entity from the playing field
	 */
	protected void remove(Entity e) {
		entities.remove(e);
		delta.entitiesDestroyed.add(e);
		setChanged();
	}

	/**
	 * Notify observers with the accumulated GameDelta and set up a new GameDelta.
	 * @param useDelta whether to use the accumulated GameDelta
	 */
	void endTick(boolean useDelta) {
		if (useDelta) {
			notifyObservers(delta);
		} else {
			setChanged();
			notifyObservers();
		}

		delta = new GameDelta();
	}

	/**
	 * End tick using delta
	 */
	void endTick() {
		endTick(true);
  }

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
	public boolean isPaused() {
		return paused || gameOver;
	}

	/**
	 * Set whether the game is paused or running.
	 * @param paused {@code true} will pause the game, {@code false} unpause it.
	 */
	public void setPaused(boolean paused) {
		if (this.paused == paused) {
			return;
		}

		delta.pausedToggled = !delta.pausedToggled;
		setChanged();

		this.paused = paused;

		if (paused && !gameOver) {
			endTick();
		}
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
		delta.scoreDelta += score - this.score;
		this.score = score;
		setChanged();
	}

	/**
	 * @return the gameOver
	 */
	public boolean isGameOver() {
		return gameOver;
	}

	/**
	 * Set whether the game is over or not
	 */
	void setGameOver(boolean gameOver) {
		if (this.gameOver == gameOver) {
			return;
		}

		delta.gameOverToggled = !delta.gameOverToggled;
		setChanged();

		this.gameOver = gameOver;

		if(gameOver) {
			endTick();
		}
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

	/**
	 * @return get the current time factor.
	 */
	public double getTimeFactor() {
		return timeFactor;
	}

	/**
	 * Set the time factor.
	 * @param timeFactor The timeFactor to be set. A timeFactor of 2 makes the
	 * game run twice as fast as a time factor of 1.
	 */
	void setTimeFactor(double timeFactor) {
		this.timeFactor = timeFactor;
	}

	/**
	 * Get Controller
	 */
	public Controller getController() {
		return controller;
	}

	/**
	 * Set Controller
	 */
	void setController(Controller controller) {
		this.controller = controller;
	}

	/**
	 * Get Engine
	 */
	public Engine getEngine() {
		return engine;
	}

	/**
	 * Set Engine
	 */
	void setEngine(Engine engine) {
		this.engine = engine;
	}
}

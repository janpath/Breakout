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
	private boolean paused = false, gameOver = false;
	private int score = 0;
	private double width, height;
	private GameDelta delta = new GameDelta();
	private double timeFactor = 1;

	/**
	 * Contains information about what changed since last time
	 */
	public static class GameDelta {
		public ArrayList<Entity> entitiesDestroyed;
		public ArrayList<Entity> entitiesAdded;
		public HashSet<Entity> entitiesMoved;
		public ArrayList<Pair<Entity, Entity>> entitiesCollided;
		public int scoreDelta;
		public boolean pausedToggled, gameOverToggled;

		GameDelta(ArrayList<Entity> entitiesDestroyed,
		          ArrayList<Entity> entitiesAdded,
		          HashSet<Entity> entitiesMoved,
		          ArrayList<Pair<Entity, Entity>> entitiesCollided,
		          int scoreDelta,
		          boolean pausedToggled,
		          boolean gameOverToggled) {
			this.entitiesDestroyed = entitiesDestroyed;
			this.entitiesMoved = entitiesMoved;
			this.entitiesCollided = entitiesCollided;
			this.scoreDelta = scoreDelta;
			this.pausedToggled = pausedToggled;
			this.gameOverToggled = gameOverToggled;
		}

		GameDelta() {
			entitiesDestroyed = new ArrayList<Entity>();
			entitiesAdded = new ArrayList<Entity>();
			entitiesMoved = new HashSet<Entity>();
			entitiesCollided = new ArrayList<Pair<Entity, Entity>>();
		}

		/**
		 * Get the union of two deltas.
		 */
		public GameDelta union(GameDelta other) {
			ArrayList<Entity> entitiesDestroyed = new ArrayList<Entity>();
			entitiesDestroyed.addAll(this.entitiesDestroyed);
			entitiesDestroyed.addAll(other.entitiesDestroyed);

			ArrayList<Entity> entitiesAdded = new ArrayList<Entity>();
			entitiesAdded.addAll(this.entitiesAdded);
			entitiesAdded.addAll(other.entitiesAdded);

			HashSet<Entity> entitiesMoved = new HashSet<Entity>();
			entitiesMoved.addAll(this.entitiesMoved);
			entitiesMoved.addAll(other.entitiesMoved);

			ArrayList<Pair<Entity, Entity>> entitiesCollided = new ArrayList<Pair<Entity, Entity>>();
			entitiesCollided.addAll(this.entitiesCollided);
			entitiesCollided.addAll(other.entitiesCollided);

			int scoreDelta = this.scoreDelta + other.scoreDelta;
			boolean pausedToggled = this.pausedToggled ^ other.pausedToggled;
			boolean gameOverToggled = this.gameOverToggled ^ other.gameOverToggled;

			return new GameDelta(entitiesDestroyed,
			                     entitiesAdded,
			                     entitiesMoved,
			                     entitiesCollided,
			                     scoreDelta,
			                     pausedToggled,
			                     gameOverToggled);
		}
	}

	protected void setChanged() {
		super.setChanged();
	}

	protected void addMoved(Entity e) {
		delta.entitiesMoved.add(e);
		setChanged();
	}

	protected void addCollided(Entity e1, Entity e2) {
		delta.entitiesCollided.add(new Pair<Entity, Entity>(e1, e2));
		setChanged();
	}

	protected void add(Entity e) {
		entities.add(e);
		delta.entitiesAdded.add(e);
		setChanged();
	}

	protected void remove(Entity e) {
		entities.remove(e);
		delta.entitiesDestroyed.add(e);
		setChanged();
	}

	void endTick() {
		notifyObservers(delta);
		delta = new GameDelta();
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
		return paused;
	}

	/**
	 * Set whether the game is paused or running.
	 * @param paused {@code true} will pause the game, {@code false} unpause it.
	 */
	public void setPaused(boolean paused) {
		if (this.paused != paused) {
			delta.pausedToggled = !delta.pausedToggled;
			setChanged();
		}

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
	 * @param gameOver the gameOver to set
	 */
	void setGameOver(boolean gameOver) {
		if (this.gameOver != gameOver) {
			delta.gameOverToggled = !delta.gameOverToggled;
			setChanged();
		}

		this.gameOver = gameOver;
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

	void setTimeFactor(double timeFactor) {
		this.timeFactor = timeFactor;
	}
}

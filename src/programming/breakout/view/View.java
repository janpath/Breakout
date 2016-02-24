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

package programming.breakout.view;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Observable;
import java.util.Observer;

import acm.graphics.GCompound;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.program.GraphicsProgram;

import programming.breakout.engine.Ball;
import programming.breakout.engine.Rectangle;
import programming.breakout.engine.Entity;
import programming.breakout.engine.GameState;

/**
 * A simple view for the breakout program
 */
public class View extends GraphicsProgram implements Observer {
	/**
	 * The game state
	 */
	private GameState state;
	private double scale;

	public View(GameState state) {
		this.state = state;
		state.addObserver(this);
	}

	/**
	 * Initialize the window, by drawing the background.
	 */
	@Override
	public void init() {
		//Resize things when window is resized
		addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					draw();
				}
			});
	}

	/**
	 * Redraw everything
	 */
	private void draw() {
		scale = Math.min(getWidth()/state.getWidth(),
										 getHeight()/state.getHeight());
		drawBackground();
		drawEntities();
	}

	/**
	 * Update us when there is a new game state.
	 */
	@Override
	public void update(Observable observable, Object arg) {
		drawEntities();
	}

	private GCompound playingField = new GCompound();
	/**
	 * Draw entities
	 */
	private void drawEntities() {
		GCompound buffer = new GCompound();

		state.getEntityList().stream()
			.map(this::entity2GObject)
			.forEach(buffer::add);

		add(buffer);
		remove(playingField);
		playingField = buffer;
	}

	/**
	 * Convert an entity to a GObject
	 */
	private GObject entity2GObject(Entity entity) {
		GObject obj;

		if(entity instanceof Ball) {
			//Draw a ball
			Ball ball = (Ball) entity;
			GOval gball = new GOval(ball.getX() * scale,
															ball.getY() * scale,
															ball.getRadius() * scale,
															ball.getRadius() * scale);
			gball.setFilled(true);
			obj = gball;

		} else if (entity instanceof Rectangle) {
			//Draw a rectangle
			Rectangle rect = (Rectangle) entity;
			GRect grect = new GRect(rect.getX() * scale,
															rect.getY() * scale,
															rect.getWidth() * scale,
															rect.getHeight() * scale);
			grect.setFilled(true);
			obj = grect;

		} else {
			throw new IllegalArgumentException("I don't know how to display a "
																				 + entity.getClass());
		}

		double offsetX = ( getWidth() - state.getWidth() * scale )/2;
		double offsetY = ( getHeight() - state.getHeight() * scale )/2;

		obj.setLocation(obj.getX() + offsetX, obj.getY() + offsetY);

		return obj;
	}

	private GCompound background = new GCompound();
	/**
	 * Draw background
	 */
	private void drawBackground() {
		GCompound buffer = new GCompound();

		//Background colour
		GRect background = new GRect(0, 0, getWidth(), getHeight());
		background.setColor(Color.GRAY);
		background.setFilled(true);
		buffer.add(background);

		//Playing field
		GRect field = new GRect((getWidth() - state.getWidth() * scale)/2,
														(getHeight() - state.getHeight() * scale)/2,
														state.getWidth() * scale,
														state.getHeight() * scale);
		field.setFilled(true);
		buffer.add(field);

		buffer.markAsComplete();

		//Replace old background
		add(buffer);
		remove(background);
		this.background = buffer;
	}
}

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
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.HashMap;

import acm.graphics.GCompound;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.graphics.GArc;
import acm.program.GraphicsProgram;

import programming.breakout.engine.Ball;
import programming.breakout.engine.Entity;
import programming.breakout.engine.GameState;
import programming.breakout.engine.Rectangle;
import programming.breakout.engine.Paddle;

import static programming.breakout.engine.GameState.GameDelta;

/**
 * A simple view for the breakout program
 */
@SuppressWarnings("serial")
public class View extends GraphicsProgram implements Observer {
	/**
	 * The game state
	 */
	private GameState state;
	private double scale;

	private double fieldOffsetX, fieldOffsetY;

	private HashMap<Entity, GObject> entities = new HashMap<Entity, GObject>();

	public View(GameState state) {
		this.state = state;
		state.addObserver(this);
	}

	/**
	 * Initialize the window, by drawing the background.
	 */
	@Override
	public void init() {
		setBackground(Color.GRAY);

		// Resize things when window is resized
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
		redrawAll();
		drawBackground();
	}

	/**
	 * Update us when there is a new game state.
	 */
	@Override
	public void update(Observable observable, Object arg) {
		if(arg instanceof GameDelta) {
			GameDelta delta = (GameDelta) arg;

			for(Entity entity : delta.entitiesChanged) {
				updateEntity(entity);
			}

			for(Entity entity : delta.entitiesDestroyed) {
				removeEntity(entity);
			}

			for(Entity entity : delta. entitiesAdded) {
				addEntity(entity);
			}

		}
		else  {
			redrawAll();
		}
		add(background);
	}

	private void addEntity(Entity entity) {
		GObject obj = entity2GObject(entity);
		entities.put(entity, obj);
		playingField.add(obj);
	}

	private void removeEntity(Entity entity) {
		playingField.remove(entities.get(entity));
		entities.remove(entity);
	}

	private void updateEntity(Entity entity) {
		entities.get(entity).setLocation(entity.getX()*scale, entity.getY()*scale);
	}

	private GCompound playingField = new GCompound();
	/**
	 * Draw entities
	 */
	private void redrawAll() {
		entities.clear();
		GCompound oldField = playingField;
		playingField = new GCompound();

		for (int i = 0; i < state.getEntityList().size(); i += 1) {
			addEntity(state.getEntityList().get(i));
		}

		fieldOffsetX = ( getWidth() - state.getWidth() * scale )/2;
		fieldOffsetY = ( getHeight() - state.getHeight() * scale )/2;

		playingField.setLocation(fieldOffsetX, fieldOffsetY);

		add(playingField);
		remove(oldField);
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

		} else if(entity instanceof Paddle) {
			//Draw paddle
			Paddle paddle = (Paddle) entity;

			double arcStart = Math.toDegrees((Math.PI - paddle.getAngle())/2);

			GCompound paddleComp = new GCompound();
			paddleComp.setLocation((paddle.getX() - paddle.getRadius() + paddle.getWidth()/2)*scale, paddle.getY()*scale);

			GArc paddleArc = new GArc(0, 0, paddle.getRadius()*2*scale, paddle.getRadius()*2*scale,
			                          arcStart, Math.toDegrees(paddle.getAngle()));
			paddleArc.setFilled(true);

			double hideOffset = paddle.getHeight()/2*scale;
			GArc paddleHide = new GArc(hideOffset/2, hideOffset,
			                           paddle.getRadius()*2*scale - hideOffset, paddle.getRadius()*2*scale - hideOffset,
			                           arcStart, Math.toDegrees(paddle.getAngle()));
			paddleHide.setColor(Color.GRAY);
			paddleHide.setFilled(true);

			paddleComp.add(paddleArc);
			paddleComp.add(paddleHide);
			paddleComp.markAsComplete();

			obj = paddleComp;
		} else if (entity instanceof Rectangle) {
			//Draw a rectangle
			Rectangle rect = (Rectangle) entity;
			GRect grect = new GRect(rect.getX() * scale,
			                        rect.getY() * scale,
			                        rect.getWidth() * scale,
			                        rect.getHeight() * scale);
			grect.setFilled(true);
			obj = grect;

		}  else {
			throw new IllegalArgumentException("I don't know how to display a "
			                                   + entity.getClass());
		}

		return obj;
	}

	private GCompound background = new GCompound();
	/**
	 * Draw background
	 */
	private void drawBackground() {
		GCompound buffer = new GCompound();

		GRect left = new GRect(0, 0, fieldOffsetX, getHeight());
		GRect right = new GRect(getWidth() - fieldOffsetX, 0,
		                        fieldOffsetX, getHeight());
		GRect top = new GRect(0, 0, getWidth(), fieldOffsetY);
		GRect bottom = new GRect(0, getHeight()-fieldOffsetY, getWidth(), fieldOffsetY);

		left.setFilled(true);
		right.setFilled(true);
		top.setFilled(true);
		bottom.setFilled(true);

		buffer.add(left);
		buffer.add(right);
		buffer.add(top);
		buffer.add(bottom);

		buffer.markAsComplete();

		//Replace old background
		add(buffer);
		remove(background);
		this.background = buffer;
	}
}

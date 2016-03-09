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

	private static final Color bgColor = Color.BLACK;
	private static final Color objColor = Color.WHITE;

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
		setBackground(bgColor);

		// Resize things when window is resized
		addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					rescale();
				}
			});
	}

	/**
	 * Redraw everything
	 */
	private void rescale() {
		scale = Math.min(getWidth()/state.getWidth(),
		                 getHeight()/state.getHeight());
		redrawAll();
	}

	/**
	 * Update us when there is a new game state.
	 */
	@Override
	public void update(Observable observable, Object arg) {
		if(arg instanceof GameDelta) {
			GameDelta delta = (GameDelta) arg;

			for(Entity entity : delta.entitiesMoved) {
				updateMoved(entity);
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
	}

	private void addEntity(Entity entity) {
		GObject obj = entity2GObject(entity);
		entities.put(entity, obj);
		playingField.add(obj);
	}

	private void removeEntity(Entity entity) {
		playingField.remove(entities.get(entity));
		// spawnParticles(entities.get(entity));
		entities.remove(entity);
	}

	private void updateMoved(Entity entity) {
		GObject obj = entities.get(entity);
		if(obj != null) {
			obj.setLocation(entity.getX()*scale, entity.getY()*scale);
		}
	}

	// /**
	//  * Make fancy particles when something is destroyed.
	//  */
	// private void spawnParticles(GRectangle rect) {

	// }

	private GCompound playingField = new GCompound();
	/**
	 * Draw entities
	 */
	private int n = 0;
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

		removeAll();
		add(playingField);

		drawBackground();
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
			gball.setColor(objColor);
			obj = gball;

		} else if(entity instanceof Paddle) {
			//Draw paddle
			Paddle paddle = (Paddle) entity;

			double arcStart = Math.toDegrees((Math.PI - paddle.getAngle())/2);

			GCompound paddleComp = new GCompound();
			paddleComp.setLocation(paddle.getX()*scale, paddle.getY()*scale);

			GArc paddleArc = new GArc((-paddle.getRadius() + paddle.getWidth()/2 )*scale, 0,
			                          paddle.getRadius()*2*scale, paddle.getRadius()*2*scale,
			                          arcStart, Math.toDegrees(paddle.getAngle()));
			paddleArc.setFilled(true);
			paddleArc.setColor(objColor);

			double hideOffset = paddle.getHeight()/2*scale;
			GArc paddleHide = new GArc(( -paddle.getRadius() + paddle.getWidth()/2 )*scale + hideOffset/2, hideOffset,
			                           paddle.getRadius()*2*scale - hideOffset, paddle.getRadius()*2*scale - hideOffset,
			                           arcStart, Math.toDegrees(paddle.getAngle()));
			paddleHide.setColor(bgColor);
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
			grect.setColor(objColor);
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
		GRect border = new GRect(fieldOffsetX, fieldOffsetY,
		                         state.getWidth()*scale, state.getHeight()*scale);

		left.setFilled(true);
		right.setFilled(true);
		top.setFilled(true);
		bottom.setFilled(true);

		left.setColor(bgColor);
		right.setColor(bgColor);
		top.setColor(bgColor);
		bottom.setColor(bgColor);
		border.setColor(objColor);

		buffer.add(left);
		buffer.add(right);
		buffer.add(top);
		buffer.add(bottom);
		buffer.add(border);

		buffer.markAsComplete();

		//Replace old background
		add(buffer);
		remove(background);
		this.background = buffer;
	}
}

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
import java.util.ArrayDeque;
import java.util.ArrayList;

import acm.graphics.GCompound;
import acm.graphics.GPolygon;
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
import programming.breakout.engine.Vector2D;

import static programming.breakout.engine.GameState.GameDelta;

/**
 * A simple view for the breakout program
 */
@SuppressWarnings("serial")
public class View extends GraphicsProgram implements Observer {

	private class Particle {
		Vector2D velocity;
		Vector2D acceleration;
		double torque;
		GPolygon shape;

		Particle(Vector2D velocity,
		         Vector2D acceleration,
		         double torque,
		         GPolygon shape) {
			this.velocity = velocity;
			this.acceleration = acceleration;
			this.torque = torque;
			this.shape = shape;
		}

		void tick() {
			shape.move(velocity.getX0()*state.getTimeFactor(),
			           velocity.getX1()*state.getTimeFactor());
			shape.rotate(Math.toDegrees(torque));
			velocity = velocity.add(acceleration);
		}
	}

	/**
	 * The game state
	 */
	private GameState state;
	private double scale;

	private static final Color bgColor = Color.BLACK;
	private static final Color objColor = Color.WHITE;
	private static final Color SHADOW_COLOR = Color.RED;
	private static final double SHADOW_DELTA = 3;
	private static final double SHADOW_FADE = .9;
	private static final int PARTICLE_MIN_COUNT = 50;
	private static final int PARTICLE_MAX_COUNT = 65;
	private static final double PARTICLE_SPEED = 2;
	private static final double PARTICLE_TORQUE = Math.PI/10;
	private static final double PARTICLE_MIN_VERTICES = 3;
	private static final double PARTICLE_MAX_VERTICES = 5;
	private static final double PARTICLE_MIN_SIZE = .2;
	private static final double PARTICLE_MAX_SIZE = .5;
	private static final Vector2D PARTICLE_GRAVITY = new Vector2D(0, .3);

	private static final int REFRESH_RATE = 20;

	private double fieldOffsetX, fieldOffsetY;

	private HashMap<Entity, GObject> entities = new HashMap<Entity, GObject>();

	private GCompound particlesComp = new GCompound();
	private ArrayList<Particle> particles = new ArrayList<Particle>();
	private GCompound playingField = new GCompound();
	private GCompound background = new GCompound();

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
		rescale();
		redrawAll();

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
		double oldscale = scale;
		scale = Math.min(getWidth()/state.getWidth(),
		                 getHeight()/state.getHeight());

		fieldOffsetX = ( getWidth() - state.getWidth() * scale )/2;
		fieldOffsetY = ( getHeight() - state.getHeight() * scale )/2;

		drawBackground();

		particlesComp.scale(scale/oldscale);
		particlesComp.setLocation(fieldOffsetX, fieldOffsetY);

		playingField.scale(scale/oldscale);
		playingField.setLocation(fieldOffsetX, fieldOffsetY);

		shadowComp.scale(scale/oldscale);
		shadowComp.setLocation(fieldOffsetX, fieldOffsetY);
	}

	ArrayDeque<GameDelta> deltas = new ArrayDeque<GameDelta>();
	boolean needsRedraw = false;
	/**
	 * Update us when there is a new game state.
	 */
	@Override
	public void update(Observable observable, Object arg) {
		if(arg instanceof GameDelta) {
			deltas.add((GameDelta) arg);
		}
		else {
			needsRedraw = true;
		}
	}

	private void addEntity(Entity entity) {
		GObject obj = entity2GObject(entity);
		entities.put(entity, obj);
		playingField.add(obj);
	}

	private void removeEntity(Entity entity) {
		playingField.remove(entities.get(entity));
		spawnParticles(entity.getBounds(),
		               (int) Math.random()*(PARTICLE_MAX_COUNT - PARTICLE_MIN_COUNT) + PARTICLE_MIN_COUNT,
		               entity instanceof Paddle ? PARTICLE_SPEED*5 : PARTICLE_SPEED);
		entities.remove(entity);
	}

	private void updateMoved(Entity entity) {
		GObject obj = entities.get(entity);
		if(obj != null) {
			obj.setLocation(entity.getX()*scale, entity.getY()*scale);

			if(entity instanceof Ball) {
				createBallShadow((Ball) entity);
			}
		}
	}

	private GCompound shadowComp = new GCompound();
	private HashMap<Ball, Vector2D> lastLocations = new HashMap<Ball, Vector2D>();
	/**
	 * Make a fancy trail for balls
	 */
	private void createBallShadow(Ball ball) {
		Vector2D lastLocation = lastLocations.get(ball);
		if(lastLocation == null ||
		   lastLocation.sub(ball.getPosition()).getLength() > SHADOW_DELTA) {
			lastLocations.put(ball, ball.getPosition());
			GOval oval = new GOval(ball.getX()*scale, ball.getY()*scale,
			                       ball.getRadius()*2*scale, ball.getRadius()*2*scale);
			oval.setFilled(true);
			oval.setColor(SHADOW_COLOR);
			shadowComp.add(oval);
		}
	}

	private void fadeShadows() {
		shadowComp.iterator()
			.forEachRemaining(obj -> {
					if(obj instanceof GOval) {
						GOval oval = (GOval) obj;
						oval.move(oval.getWidth()*(1 - SHADOW_FADE )/2*state.getTimeFactor(),
						          oval.getHeight()*(1 - SHADOW_FADE )/2*state.getTimeFactor());
						oval.scale(1 - state.getTimeFactor() + SHADOW_FADE*state.getTimeFactor());

						if(oval.getHeight() < 1) {
							// shadowComp.remove(oval);
						} else {
							Color prevColor = oval.getColor();
							oval.setColor(new Color(prevColor.getRed(),
							                        prevColor.getGreen(),
							                        prevColor.getBlue(),
							                        (int) ( prevColor.getAlpha()*(1 - state.getTimeFactor() + SHADOW_FADE*state.getTimeFactor() ) )));
						}
					}
				});
	}

	/**
	 * Make fancy particles when something is destroyed.
	 */
	private void spawnParticles(Rectangle rect, int count, double speed) {
		for (int i = 0; i < count; i += 1) {
			Particle particle =
				new Particle(new Vector2D(Math.random()*speed*2 - speed,
				                          Math.random()*speed*2 - speed),
				             PARTICLE_GRAVITY,
				             Math.random()*PARTICLE_TORQUE*2 - PARTICLE_TORQUE,
				             getRandomPolygon(( Math.random()*rect.getWidth() + rect.getX() )*scale,
				                              ( Math.random()*rect.getHeight() + rect.getY() )*scale,
				                              ( Math.random()*(PARTICLE_MAX_SIZE - PARTICLE_MIN_SIZE)
				                                + PARTICLE_MIN_SIZE ) * scale));
			particles.add(particle);
			particlesComp.add(particle.shape);
		}
	}

	GPolygon getRandomPolygon(double x, double y, double size) {
		GPolygon poly = new GPolygon();
		int vertices =
			(int) ( Math.random()*( PARTICLE_MAX_VERTICES - PARTICLE_MIN_VERTICES)
			        + PARTICLE_MIN_VERTICES );

		for (int i = 0; i < vertices; i += 1) {
			poly.addVertex(Math.random()*size, Math.random()*size);
		}

		poly.setFilled(true);
		poly.setColor(objColor);
		poly.recenter();
		poly.setLocation(x, y);

		return poly;
	}

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

		playingField.setLocation(fieldOffsetX, fieldOffsetY);

		removeAll();
		add(shadowComp);
		add(playingField);
		drawBackground();
		add(particlesComp);
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
			                        2*ball.getRadius() * scale,
			                        2*ball.getRadius() * scale);
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
		                         state.getWidth()*scale - 1, state.getHeight()*scale - 1);
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

	@Override
	public void run() {
		while(true) {
			long start = System.currentTimeMillis();

			tick();

			long elapsed = System.currentTimeMillis() - start;

			try {
				Thread.sleep(Math.max(0, REFRESH_RATE - elapsed ));
			}
			catch (InterruptedException ex) {
			}
		}
	}

	private void tick() {
		ArrayDeque<GameDelta> deltasThisFrame = deltas;
		deltas = new ArrayDeque<GameDelta>();
		if (needsRedraw) {
			redrawAll();
			needsRedraw = false;
		} else {
			for (GameDelta delta : deltasThisFrame) {
				processDelta(delta);
			}
		}

		if (!state.isPaused()) {
			particles.stream().forEach(Particle::tick);
			fadeShadows();
		}
	}

	private void processDelta(GameDelta delta) {
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
}

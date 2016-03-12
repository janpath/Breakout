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
import java.awt.Font;
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
import acm.graphics.GLabel;
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

	/**
	 * Represents a particle used for visual effects
	 */
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

		/**
		 * Calculate new state of particle
		 */
		void tick() {
			//Move shape by velocity
			shape.move(velocity.getX0()*state.getTimeFactor(),
			           velocity.getX1()*state.getTimeFactor());

			//Rotate shape by torque
			shape.rotate(Math.toDegrees(torque*state.getTimeFactor()));

			//Increase velocity by acceleration
			velocity = velocity.add(acceleration.scale(state.getTimeFactor()));
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
	private static final double PARTICLE_TORQUE = Math.PI/5;
	private static final double PARTICLE_MIN_VERTICES = 3;
	private static final double PARTICLE_MAX_VERTICES = 5;
	private static final double PARTICLE_MIN_SIZE = .5;
	private static final double PARTICLE_MAX_SIZE = 2;
	private static final Vector2D PARTICLE_GRAVITY = new Vector2D(0, .4);

	private static final int REFRESH_RATE = 20;

	private double fieldOffsetX, fieldOffsetY;

	private HashMap<Entity, GObject> entities = new HashMap<Entity, GObject>();
	private GCompound particlesComp = new GCompound();
	private ArrayList<Particle> particles = new ArrayList<Particle>();
	private GCompound playingField = new GCompound();
	private GCompound background = new GCompound();
	private GCompound shadowComp = new GCompound();
	private GCompound instructions = new GCompound();
	private GCompound gameOver = new GCompound();
	private HashMap<Ball, Vector2D> lastLocations = new HashMap<Ball, Vector2D>();
	private ArrayDeque<GameDelta> deltas = new ArrayDeque<GameDelta>();
	private boolean needsRedraw = false;

	/**
	 * Create new View and register it with the given GameState
	 */
	public View(GameState state) {
		this.state = state;
		state.addObserver(this);
	}

	/**
	 * Initialize the window and draw everything.
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
	 * Initialize instructions compound
	 */
	private void drawInstructions() {
		GCompound instructions = new GCompound();

		GLabel pause = new GLabel("SPACE to (un)pause");
		GLabel speed = new GLabel("SHIFT to slow down, CTRL to speed up");

		pause.setFont(new Font(GLabel.DEFAULT_FONT.getFontName(),
		                       GLabel.DEFAULT_FONT.getStyle(), 50));

		pause.setColor(objColor);
		speed.setColor(objColor);

		//Align instructions in the center
		instructions.add(pause, (getWidth() - pause.getWidth())/2,
		                 (getHeight()*1.3 + pause.getAscent())/2);
		instructions.add(speed, (getWidth() - speed.getWidth())/2,
		                 (getHeight()*1.3 + pause.getAscent())/2
		                 + speed.getHeight() + pause.getDescent());

		//Make instructions visible, only if the game is paused
		instructions.setVisible(state.isPaused() && !state.isGameOver());

		//Replace old instructions compound
		remove(this.instructions);
		add(instructions);
		this.instructions = instructions;
	}

	/**
	 * Initialize instructions compound
	 */
	private void drawGameOver() {
		GCompound gameOver= new GCompound();

		GLabel gameOverLabel = new GLabel("Game Over");
		GLabel explanation = new GLabel("You ran out of bricks :(");

		gameOverLabel.setFont(new Font(GLabel.DEFAULT_FONT.getFontName(),
		                               GLabel.DEFAULT_FONT.getStyle(), 50));

		gameOverLabel.setColor(objColor);
		explanation.setColor(objColor);

		gameOver.add(gameOverLabel, (getWidth() - gameOverLabel.getWidth())/2,
		             (getHeight()*1.3 + gameOverLabel.getAscent())/2);
		gameOver.add(explanation, (getWidth() - explanation.getWidth())/2,
		             (getHeight()*1.3 + gameOverLabel.getAscent())/2
		             + explanation.getHeight() + gameOverLabel.getDescent());

		//Make game over message visibile only if the game is over
		gameOver.setVisible(state.isGameOver());

		remove(this.gameOver);
		add(gameOver);
		this.gameOver= gameOver;
	}

	/**
	 * Rescale window if window size has changed
	 */
	private void rescale() {
		double oldScale = scale;
		scale = Math.min(getWidth()/state.getWidth(),
		                 getHeight()/state.getHeight());

		fieldOffsetX = ( getWidth() - state.getWidth() * scale )/2;
		fieldOffsetY = ( getHeight() - state.getHeight() * scale )/2;

		particlesComp.scale(scale/oldScale);
		particlesComp.setLocation(fieldOffsetX, fieldOffsetY);

		playingField.scale(scale/oldScale);
		playingField.setLocation(fieldOffsetX, fieldOffsetY);

		shadowComp.scale(scale/oldScale);
		shadowComp.setLocation(fieldOffsetX, fieldOffsetY);

		drawBackground();

		drawInstructions();
		drawGameOver();
	}

	/**
	 * Update us when there is a new game state.
	 */
	@Override
	public void update(Observable observable, Object arg) {
		if(arg instanceof GameDelta) {
			// If we were supplied with information about what changed, we can just
			// change that
			deltas.add((GameDelta) arg);
		}
		else {
			// Otherwise we have to redraw everything
			needsRedraw = true;
		}
	}

	/**
	 * Add an entity to the canvas
	 */
	private void addEntity(Entity entity) {
		GObject obj = entity2GObject(entity);
		entities.put(entity, obj);
		playingField.add(obj);
	}

	/**
	 * Remove an entity from the canvas
	 */
	private void removeEntity(Entity entity) {
		playingField.remove(entities.get(entity));

		//Spawn particles for the destroyed entity
		spawnParticles(entity.getBounds(),
		               (int) Math.random()*(PARTICLE_MAX_COUNT - PARTICLE_MIN_COUNT)
		               + PARTICLE_MIN_COUNT,
		               //Make initial particle velocity higher if paddle was
		               //destroyed
		               entity instanceof Paddle
		               ? PARTICLE_SPEED*5 : PARTICLE_SPEED);

		//Remove entity from entities to GObjects mapping
		entities.remove(entity);
	}

	/**
	 * Move an entity on the canvas
	 */
	private void updateMoved(Entity entity) {
		GObject obj = entities.get(entity);
		if(obj != null) {
			obj.setLocation(entity.getX()*scale, entity.getY()*scale);

			if(entity instanceof Ball) {
				createBallShadow((Ball) entity);
			}
		}
	}

	/**
	 * Make a fancy trail for balls
	 */
	private void createBallShadow(Ball ball) {
		Vector2D lastLocation = lastLocations.get(ball);
		if(lastLocation == null ||
		   lastLocation.sub(ball.getPosition()).getMagnitude() > SHADOW_DELTA) {
			lastLocations.put(ball, ball.getPosition());
			GOval oval = new GOval(ball.getX()*scale, ball.getY()*scale,
			                       ball.getRadius()*2*scale,
			                       ball.getRadius()*2*scale);
			oval.setFilled(true);
			oval.setColor(SHADOW_COLOR);
			shadowComp.add(oval);
		}
	}

	private void fadeShadows() {
		ArrayList<GOval> toBeRemoved = new ArrayList<GOval>();
		shadowComp.iterator()
			.forEachRemaining(obj -> {
					if(obj instanceof GOval) {
						GOval oval = (GOval) obj;
						// Move oval so that after shrinking the center stays in place
						oval.move(oval.getWidth()*(1 - SHADOW_FADE )/2
						          * state.getTimeFactor(),
						          oval.getHeight()*(1 - SHADOW_FADE )/2
						          * state.getTimeFactor());

						// Shrink oval
						oval.scale(1 - state.getTimeFactor()
						           + SHADOW_FADE*state.getTimeFactor());

						if(oval.getHeight() < 1) {
							// If oval is smaller than one pixel, remove it.
							toBeRemoved.add(oval);
						} else {
							// Fade color of oval
							Color prevColor = oval.getColor();
							oval.setColor(new Color(prevColor.getRed(),
							                        prevColor.getGreen(),
							                        prevColor.getBlue(),
							                        (int)
							                        (prevColor.getAlpha() *
							                         (1 - state.getTimeFactor() +
                                          SHADOW_FADE*state.getTimeFactor()))));
            }
					}
				});
		toBeRemoved.forEach(oval -> shadowComp.remove(oval));
	}

	/**
	 * Make fancy particles when something is destroyed.
	 */
	private void spawnParticles(Rectangle rect, int count, double speed) {
		for (int i = 0; i < count; i += 1) {
			//Create particle with random velocity, torque, and shape
			Particle particle =
				new Particle(new Vector2D(Math.random()*speed*2 - speed,
				                          Math.random()*speed*2 - speed),
				             PARTICLE_GRAVITY,
				             Math.random()*PARTICLE_TORQUE*2 - PARTICLE_TORQUE,
				             getRandomPolygon((Math.random()*rect.getWidth()
				                                + rect.getX() )*scale,
				                              (Math.random()*rect.getHeight()
				                                + rect.getY() )*scale,
				                              (Math.random()*(PARTICLE_MAX_SIZE
				                                               - PARTICLE_MIN_SIZE)
				                                + PARTICLE_MIN_SIZE ) * scale));
			particles.add(particle);
			particlesComp.add(particle.shape);
		}
	}

	/**
	 * Create a random polygon within the given square
	 */
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
	 * Redraw everything
	 */
	private void redrawAll() {
		entities.clear();
		playingField = new GCompound();

		for (int i = 0; i < state.getEntityList().size(); i += 1) {
			addEntity(state.getEntityList().get(i));
		}

		playingField.setLocation(fieldOffsetX, fieldOffsetY);

		removeAll();
		add(shadowComp);
		add(playingField);
		add(particlesComp);

		//Background is drawn over the playing field, to clip it's contents
		drawBackground();

		drawInstructions();
		drawGameOver();
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
			//Draw paddle as two arcs, the second hiding the bottom of the first
			Paddle paddle = (Paddle) entity;

			double arcStart = Math.toDegrees((Math.PI - paddle.getAngle())/2);

			GCompound paddleComp = new GCompound();
			paddleComp.setLocation(paddle.getX()*scale, paddle.getY()*scale);

			// Create the visible arc
			GArc paddleArc = new GArc((paddle.getWidth()/2-paddle.getRadius())*scale,
			                          0,
			                          paddle.getRadius()*2*scale,
			                          paddle.getRadius()*2*scale,
			                          arcStart, Math.toDegrees(paddle.getAngle()));
			paddleArc.setFilled(true);
			paddleArc.setColor(objColor);

			// Create the hiding arc
			double hideOffset = paddle.getHeight()/2*scale;
			GArc paddleHide = new GArc((paddle.getWidth()/2-paddle.getRadius())*scale
			                           + hideOffset/2,
			                           hideOffset,
			                           paddle.getRadius()*2*scale - hideOffset,
			                           paddle.getRadius()*2*scale - hideOffset,
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
	 * Draw the sides and the border
	 */
	private void drawBackground() {
		GCompound buffer = new GCompound();

		GRect left = new GRect(0, 0, fieldOffsetX, getHeight());
		GRect right = new GRect(getWidth() - fieldOffsetX, 0,
		                        fieldOffsetX, getHeight());
		GRect top = new GRect(0, 0, getWidth(), fieldOffsetY);
		GRect bottom = new GRect(0, getHeight()-fieldOffsetY,
		                         getWidth(), fieldOffsetY);
		GRect border = new GRect(fieldOffsetX, fieldOffsetY,
		                         state.getWidth()*scale - 1,
		                         state.getHeight()*scale - 1);
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
		//Register all accumulated deltas
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

		//Animate particles and ball trail
		if (!state.isPaused()) {
			ArrayList<Particle> toBeRemoved = new ArrayList<Particle>();

			particles.stream().peek(Particle::tick)
				.forEach(particle -> {
						if(particle.shape.getY() > getHeight()) {
							toBeRemoved.add(particle);
						}
					});

			//Remove particles that are out of the window
			toBeRemoved.forEach(particle -> {
					particles.remove(particle);
					particlesComp.remove(particle.shape);
				});
			fadeShadows();
		}
	}

	/**
	 * Process a game delta
	 */
	private void processDelta(GameDelta delta) {
		if(delta.pausedToggled) {
			instructions.setVisible(state.isPaused() && !state.isGameOver());
		}

		if (delta.gameOverToggled && state.isGameOver()) {
			instructions.setVisible(false);
			gameOver.setVisible(true);
		}

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

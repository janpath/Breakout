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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.Component;
import java.awt.Robot;
import java.awt.AWTException;

import programming.breakout.engine.Vector2D;

public class Controller implements MouseMotionListener, KeyListener {
	private GameState state;
	private Entity controlledObject;
	private boolean freeX, freeY;
	private Component component;

	/**
	 * @param state the GameState object
	 * @param controlledObject the object to be controlled
	 * @param x whether the object can be moved in the x direction
	 * @param y whether the object can be moved in the y direction
	 */
	public Controller(GameState state, Entity controlledObject,
										boolean x, boolean y,
										Component component) {
		this.state = state;
		this.controlledObject = controlledObject;
		this.freeX = x;
		this.freeY = y;
		this.component = component;

		//Add listeners
		component.addKeyListener(this);
		component.addMouseMotionListener(this);

		//Create Robot for mouse catching
		try {
			robot = new Robot();
		} catch(AWTException ex) {
			ex.printStackTrace();
		}
	}

	private Robot robot;
	/**
	 * Move controlled object and keep mouse in window
	 */
	@Override
	public void mouseMoved(MouseEvent event) {
		// How much the mouse moved on screen
		double xMoved = event.getX() - component.getWidth()/2;
		double yMoved = event.getY() - component.getHeight()/2;

		// Translate that to the playing field
		double dx = xMoved/component.getWidth()*state.getWidth();
		double dy = yMoved/component.getHeight()*state.getHeight();

		// Make sure we don't move the object out of the playing field
		Rectangle bounds = controlledObject.getBounds();
		dx += Math.max(0, -(bounds.getX() + dx))
			- Math.max(0, bounds.getX() + bounds.getWidth() + dx - state.getWidth());
		dy += Math.max(0, -(bounds.getY() + dy))
			- Math.max(0, bounds.getY() + bounds.getHeight() + dy - state.getHeight());

		//Move controlledObject
		double newX = controlledObject.getX() + ((freeX) ? dx : 0);
		double newY = controlledObject.getY() + ((freeY) ? dy : 0);
		controlledObject.setPosition(new Vector2D(newX, newY));

		//Keep mouse in component if game is not paused
		if(!state.isPaused() && !state.isGameOver() && Math.max(Math.abs(xMoved), Math.abs(yMoved)) > 5) {
			robot.mouseMove((int) (component.getLocationOnScreen().getX()
														 + component.getWidth()/2),
											(int) (component.getLocationOnScreen().getY()
														 + component.getHeight()/2));
		}
	}

	@Override
	public void mouseDragged(MouseEvent ev) {}


	/**
	 * Pause Game
	 */
	@Override
	public void keyTyped(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.VK_SPACE) {
			state.setPaused(!state.isPaused());
		}
	}

	@Override
	public void keyReleased(KeyEvent ev) {
	}

	@Override
	public void keyPressed(KeyEvent ev) {}
}

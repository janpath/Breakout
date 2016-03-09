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
import java.awt.event.MouseListener;
import java.awt.Component;
import java.awt.Robot;
import java.awt.AWTException;

import programming.breakout.engine.Vector2D;

public class Controller implements MouseListener, MouseMotionListener, KeyListener {
	private GameState state;
	private Entity controlledObject;
	private boolean freeX, freeY;
	private Component component;
	private Robot robot;

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
		component.addMouseListener(this);
		component.addMouseMotionListener(this);

		//Create Robot for mouse catching
		try {
			robot = new Robot();
		} catch(AWTException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Move controlled object and keep mouse in window
	 */
	@Override
	public void mouseMoved(MouseEvent event) {
		if(state.isPaused()) {
			return;
		}

		component.requestFocusInWindow();
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

		assert isInField(): "Controlled object moved out of playing field.";

		//Keep mouse in component if game is not paused
		if(!state.isGameOver() && Math.max(Math.abs(xMoved), Math.abs(yMoved)) > 5) {
			alignMouse();
		}
	}

	/**
	 * Grab the focus to the GCanvas as soon as the mouse enters, for keyboard input
	 */
	@Override
	public void mouseDragged(MouseEvent ev) {}

	@Override
	public void mouseEntered(MouseEvent ev) {
		component.requestFocusInWindow();
	}

	@Override
	public void mouseExited(MouseEvent ev) {}

	@Override
	public void mouseClicked(MouseEvent ev) {}

	@Override
	public void mousePressed(MouseEvent ev) {}

	@Override
	public void mouseReleased(MouseEvent ev) {}

	/**
	 * Test if the controlled object is still inside the playing field
	 */
	private boolean isInField() {
		Rectangle bounds = controlledObject.getBounds();
		return
			bounds.getX() >= 0 && bounds.getY() >= 0 &&
			bounds.getX() + bounds.getWidth() <= state.getWidth() &&
			bounds.getY() + bounds.getHeight() <= state.getHeight();
	}

	/**
	 * Align mouse so that it doesn't move out of the window
	 */
	private void alignMouse() {
		robot.mouseMove((int) (component.getLocationOnScreen().getX()
													 + component.getWidth()/2),
										(int) (component.getLocationOnScreen().getY()
													 + component.getHeight()/2));
	}

	/**
	 * Pause Game
	 */
	@Override
	public void keyTyped(KeyEvent event) {
		if(event.getKeyChar() == ' ') {
			state.setPaused(!state.isPaused());
		}
	}

	@Override
	public void keyReleased(KeyEvent ev) {
	}

	@Override
	public void keyPressed(KeyEvent event) {
	}
}

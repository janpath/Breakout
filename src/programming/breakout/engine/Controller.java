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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Container;
import java.awt.Robot;
import java.awt.AWTException;

import programming.breakout.engine.Vector2D;

public class Controller implements MouseListener, MouseMotionListener, KeyListener {
	private GameState state;
	private Entity controlledObject;
	private boolean freeX, freeY;
	private Container container;

	/**
	 * @param state the GameState object
	 * @param controlledObject the object to be controlled
	 * @param x whether the object can be moved in the x direction
	 * @param y whether the object can be moved in the y direction
	 */
	public Controller(GameState state, Entity controlledObject,
										boolean x, boolean y,
										Container container) {
		this.state = state;
		this.controlledObject = controlledObject;
		this.freeX = x;
		this.freeY = y;
		this.container = container;
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		double newX = controlledObject.getX() + ((freeX) ? event.getX() - container.getWidth()/2 : 0);
		double newY = controlledObject.getY() + ((freeY) ? event.getY() - container.getWidth()/2 : 0);
		controlledObject.setPosition(new Vector2D(newX, newY));

		if(!state.getPaused()) {
			try {
				new Robot().mouseMove((int) (container.getLocationOnScreen().getX()
																		 + container.getWidth()/2),
															(int) (container.getLocationOnScreen().getY()
																		 + container.getHeight()/2));
			} catch(AWTException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent ev) {}


	@Override
	public void keyTyped(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.VK_SPACE) {
			state.setPaused(!state.getPaused());
		}
	}

	@Override
	public void keyPressed(KeyEvent ev) {}

	@Override
	public void keyReleased(KeyEvent ev) {}

	@Override
	public void mouseExited(MouseEvent ev) {}

	@Override
	public void mouseEntered(MouseEvent ev) {}

	@Override
	public void mousePressed(MouseEvent ev) {}

	@Override
	public void mouseReleased(MouseEvent ev) {}

	@Override
	public void mouseClicked(MouseEvent ev) {}

}

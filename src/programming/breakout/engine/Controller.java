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

public class Controller implements MouseListener, KeyListener {
	private Entity controlledObject;

	/**
	 * @param state the GameState object
	 * @param controlledObject the object to be controlled
	 * @param x whether the object can be moved in the x direction
	 * @param y whether the object can be moved in the y direction
	 */
	public Controller(GameState state, Entity controlledObject,
										boolean x, boolean y,
										Container container) {
		this.controlledObject = controlledObject;
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		controlledObject.setX(controlledObject.getX() + event.getX() - container.getWidth/2);
		if(!state.getPaused()) {
			new Robot().mouseMove((int) (container.getLocationOnScreen().getX()
																	 + container.getWidth()/2),
														(int) (container.getLocationOnScreen().getY()
																	 + container.getHeight()/2));
		}
	} catch(AWTException ex) {
		ex.printStackTrace();
	}

	@Override
	public void keyTyped(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.VK_SPACE) {
			state.setPaused(!state.getPaused());
		}
	}
}

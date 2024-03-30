/*
 * Copyright (C) 2007 cryptosms.org
 *
 * This file is part of cryptosms.org.
 *
 * cryptosms.org is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * cryptosms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cryptosms.org; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.cryptosms.test;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class TestSimple extends MIDlet implements CommandListener {
	protected Display display;

	protected Form form;

	protected static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);

	public void commandAction(Command c, Displayable d) {
		if (c == CMD_EXIT) {
			destroyApp(false);
			notifyDestroyed();
		}
	}

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		form = new Form("Test");
		form.append("Hello World");
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);
		if (display != null)
			display.setCurrent(form);
	}

	protected void destroyApp(boolean arg0) {
	}

	protected void pauseApp() {
	}


}

/*
 * Copyright (C) 2007 Crypto Messaging with Elliptic Curves Cryptography (CRYMES)
 *
 * This file is part of Crypto Messaging with Elliptic Curves Cryptography (CRYMES).
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Crypto Messaging with Elliptic Curves Cryptography (CRYMES); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jwhisper.a2p.gui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

/**
 * @author sd
 */
public class ErrorScreen extends Alert /* implements CommandListener */ {

	/**
	 * Creates a new ErrorScreen object.
	 */
	// private final Command cancel;
	private ErrorScreen() {
		super("Message:");
		this.setTimeout(5000);
		this.setCommandListener(null); // use the default
		//TODO i18n
		/*
		this.cancel = new Command("cancel", Command.CANCEL, 0);
		this.addCommand(this.cancel);
		*/
	}

	public static void showE(Display d, String e, Displayable nextScreen) {
		show(d, e, nextScreen, AlertType.ERROR);
	}

	public static void showW(Display d, String e, Displayable nextScreen) {
		show(d, e, nextScreen, AlertType.WARNING);
	}

	public static void showI(Display d, String e, Displayable nextScreen) {
		show(d, e, nextScreen, AlertType.INFO);
	}

	// generic function for alert box
	// TODO if running in debugmode: print to system.out
	private static synchronized void show(Display d, String e, Displayable nextScreen, AlertType type) {
		
		ErrorScreen es = new ErrorScreen();
		es.setType(type);
		es.setString(e);
		d.setCurrent(es, nextScreen);
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	/*
	public void commandAction(Command co, Displayable arg1) {
		if (co == this.cancel) {
			this.setTimeout(1);
			
	}
	
	}
	*/

}

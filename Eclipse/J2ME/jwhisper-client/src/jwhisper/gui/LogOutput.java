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
package jwhisper.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import jwhisper.helpers.Logger;


/** 
 * Produces a view of the log messages in the Helpers.Logger ringbuffer. It's a simple
 * and forward thing, no 'tail -f' or other nice stuff.
 * 
 * @author malte
 */
public class LogOutput extends Form implements CommandListener {
	
	protected static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);

	private JWhisperMidlet midlet;
	
	public LogOutput(JWhisperMidlet midlet) {
		super("Log");
		this.midlet=midlet;
		
		String[] 	lines=Logger.getInstance().readout();
		
		for (int i=0; i<lines.length; i++) 
			this.append(lines[i]);
		
		lines=null;
		
		this.addCommand(CMD_EXIT);
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}
	
	public void commandAction(Command c, Displayable d) {
		if (c == CMD_EXIT) {
			midlet.showMenu();
		}
	}
}

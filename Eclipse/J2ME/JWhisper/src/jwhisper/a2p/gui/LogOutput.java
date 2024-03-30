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

import gr.bluevibe.fire.components.Component;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.util.CommandListener;

import javax.microedition.lcdui.Command;

import jwhisper.utils.Logger;

/** 
 * Produces a view of the log messages in the Helpers.Logger ringbuffer. It's a simple
 * and forward thing, no 'tail -f' or other nice stuff.
 * 
 * @author malte
 */
public class LogOutput extends Panel implements CommandListener {
	
	protected static final Command CMD_EXIT = new Command("Esci", Command.EXIT, 1);

	private JWhisperA2PMidlet midlet;
	
	public LogOutput(JWhisperA2PMidlet midlet) {
		
		this.setLabel("Log window");
		this.midlet=midlet;
		
		String[] lines=Logger.getInstance().readout();
		
		for (int i=0; i<lines.length; i++) {
			this.add(new Row(lines[i]));
			this.validate();
		}
			
		
		lines=null;
		
		this.addCommand(CMD_EXIT);
		this.setCommandListener(this);
        this.setTicker(midlet.getTicker());
	}

	public void commandAction(Command cmd, Component c) {
		if (cmd == CMD_EXIT) {
			midlet.showMenu();
		}	
	}
}

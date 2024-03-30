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


import javax.microedition.lcdui.Command;

import jwhisper.Settings;
import jwhisper.SettingsA2P;
import jwhisper.utils.ResourceManager;
import jwhisper.utils.ResourceTokens;

import gr.bluevibe.fire.components.Component;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.util.CommandListener;

/**
 * 	@author sd
 *	@date 02.05.2007
 */
public class InfoScreen extends Panel implements CommandListener {

	
	private final Command ok;
	private final Command alterNumber;

	private final JWhisperA2PMidlet midlet;
	
	public InfoScreen(JWhisperA2PMidlet midlet, String title) {

		this.setLabel(title);
		this.midlet=midlet;
		ResourceManager rm = midlet.getResourceManager();
		Settings s=midlet.getSettings();
		
		this.add(new Row("Dip. di Informatica ed Applicazioni \"R.M. Capocelli\""));
		this.add(new Row("Universita' degli Studi di Salerno"));
		this.add(new Row(rm.getString(ResourceTokens.STRING_NAME)+s.getName()));
		this.add(new Row(rm.getString(ResourceTokens.STRING_NUMBER)+s.getNumber()));
		this.add(new Row(rm.getString(ResourceTokens.STRING_VERSION)+s.getVersion()));
		this.add(new Row( ((SettingsA2P)midlet.getSettings()).getSMSCNumber()));
		
		this.ok = new Command(rm.getString(ResourceTokens.STRING_BACK), Command.OK, 1);
		this.alterNumber = new Command(rm.getString(ResourceTokens.STRING_EDIT_ENTRY), Command.BACK, 1);
		
		this.addCommand(this.ok);
		this.addCommand(this.alterNumber);
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}

	public void commandAction(Command cmd, Component c) {
		if (cmd == this.ok) {
			midlet.setTicketText("");
			midlet.showMenu();
		}
		if(cmd==alterNumber){
			midlet.setTicketText("TODO");
		}
	}
		
}

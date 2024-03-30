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
import javax.microedition.lcdui.Font;


import jwhisper.SettingsA2P;
import jwhisper.utils.ResourceTokens;
import jwhisper.utils.ResourceManager;

import gr.bluevibe.fire.components.Component;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.displayables.FireScreen;
import gr.bluevibe.fire.util.CommandListener;
import gr.bluevibe.fire.util.Lang;


/**
 * @version $Revision: 1.7 $
 */
public class Menu extends Panel implements CommandListener {

	private final Command exit, registrazione, unreadbox, readbox, info, log;

	private final JWhisperA2PMidlet _midlet;

	private boolean toBeRegistered = false;
	
	// se true inserire anche registrazione, altrimenti dipende dal valore di settings
	private boolean _isSetup;

	public Menu(JWhisperA2PMidlet parent, boolean isSetup) {
		this.setLabel(parent.getResourceManager().getString(ResourceTokens.STRING_MENU));
		ResourceManager rm = parent.getResourceManager();
		
		this.toBeRegistered = false;
		this._midlet = parent;
		this._isSetup = isSetup;

		this.exit = new Command(rm.getString(ResourceTokens.STRING_EXIT), Command.EXIT, 1);
		
		registrazione = new Command("",Command.OK,1);
		unreadbox = new Command("",Command.OK,1);
		readbox = new Command("",Command.OK,1);
		info = new Command("",Command.OK,1);
		log = new Command("",Command.OK,1);
		
		this.addCommand(this.exit);
		this.setCommandListener(this);
		
		this.setTicker(parent.getTicker());
		
		buildList();
	}

	private void buildList() {

		toBeRegistered = false;
		if (_isSetup){
			toBeRegistered = true;
		} else if (!((SettingsA2P)(_midlet.getSettings())).isRegistered()){
			toBeRegistered = true;
		}
		
		if (toBeRegistered) {
			Row regRow = new Row(Lang.get("Registrazione"));
			regRow.setFont(Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_BOLD,Font.SIZE_LARGE)); // change the font
			regRow.setAlignment(FireScreen.CENTRE);
			regRow.addCommand(registrazione);
			regRow.setCommandListener(this);
			this.add(regRow);
		} else {
			Row unreadRow = new Row(_midlet.getResourceManager().getString(ResourceTokens.STRING_UNREAD_INBOX));
			unreadRow.setFont(Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_BOLD,Font.SIZE_LARGE)); // change the font
			unreadRow.setAlignment(FireScreen.CENTRE);
			unreadRow.addCommand(unreadbox);
			unreadRow.setCommandListener(this);
			
			Row readRow = new Row(_midlet.getResourceManager().getString(ResourceTokens.STRING_READ_INBOX));
			readRow.setFont(Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_BOLD,Font.SIZE_LARGE)); // change the font
			readRow.setAlignment(FireScreen.CENTRE);
			readRow.addCommand(readbox);
			readRow.setCommandListener(this);
			this.add(unreadRow);
			this.add(readRow);
		}
		
		// Info Log
		
		Row infoRow = new Row("Info");
		infoRow.setFont(Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_BOLD,Font.SIZE_LARGE)); // change the font
		infoRow.setAlignment(FireScreen.CENTRE);
		infoRow.addCommand(info);
		infoRow.setCommandListener(this);
		
		Row logRow = new Row("Log");
		logRow.setFont(Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_BOLD,Font.SIZE_LARGE)); // change the font
		logRow.setAlignment(FireScreen.CENTRE);
		logRow.addCommand(log);
		logRow.setCommandListener(this);
		this.add(infoRow);
		this.add(logRow);
	}

	public void destroyApp() {
		this._midlet.notifyDestroyed();
	}

	public void commandAction(Command cmd, Component c) {
		if(cmd==log) {
			_midlet.dumpLog();
		}
		else if(cmd==info) {
			_midlet.openInfoScreen();
		}
		else if(cmd==readbox) {
			_midlet.openReadMessageList();
		}
		else if(cmd==unreadbox) {
			_midlet.openUnreadMessageList();
		}
		else if(cmd==registrazione) {
			_midlet.Registrazione();
		}
		else if (cmd == this.exit) {
			_midlet.openExitScreen();
		}
	}
}
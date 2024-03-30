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

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import jwhisper.book.Address;
import jwhisper.book.AddressService;
import jwhisper.book.CMessage;
import jwhisper.book.MessageType;
import jwhisper.helpers.ResourceTokens;



/**
 * @author sd
 */
public class KeyList extends List implements CommandListener {
	JWhisperMidlet gui;

	CMessage[] messages;

	private final Command back;


	private final Command delete;

	private final Command importKey;
	
	//private final Command createAddress4Key;

	public KeyList(JWhisperMidlet gui) {
		super(gui.getResourceManager().getString(ResourceTokens.STRING_PUBLIC_KEYS), Choice.IMPLICIT);
		
		this.gui = gui;
		this.back = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.BACK, 1);
		this.importKey = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_IMPORT_KEY), Command.OK, 0);
		this.delete = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_DELETE_KEY), Command.OK, 0);

		this.addCommand(this.back);
		this.addCommand(importKey);
		this.addCommand(this.delete);

		this.setCommandListener(this);
		this.setTicker(gui.getTicker());
		buildList();
	}

	private void setMessages() {
		try {
			messages = AddressService.getPublicKeys();
		} catch (Exception e) {
			gui.showError(e.getMessage());
		}
	}

	private void buildList() {
		setMessages();
		this.deleteAll();
		String number;
		Address address=null;
		if (messages != null) {
			for (int i = 0; i < messages.length; i++) {
				number=messages[i].getSender();
				
				try {
					address=AddressService.findAdress4Url(number);
				} catch (Exception e) {
					// TODO i18n
					//e.printStackTrace();
				}
				if (address==null)
					this.append(number,null);
				else
					this.append(address.getPerson().getName(),null);	
			}
		}
	}

	public void commandAction(Command co, Displayable disp) {


		if (co == this.importKey) {
			if (this.getSelectedIndex()>=0){
				CMessage m = messages[this.getSelectedIndex()];
	
				if (m.getType() == MessageType.KEY) {
					try {
						AddressService.importKey4Url(m.getSender(), m.getData());
						gui.setTicketText(gui.getResourceManager().getString(ResourceTokens.STRING_ADDED_KEY_TO_PERSON) + m.getSender());
						
					} catch (Exception e) {
						gui.showError(e.getMessage());
					}
				} else {
					gui.showError(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_WRONG_TYPE3));
				}
			}
		}

		if (co == this.back) {
			gui.showMenu();
		}

		if (co == this.delete) {
			if (this.getSelectedIndex()>=0){
				CMessage m = messages[this.getSelectedIndex()];
				try {
					AddressService.deleteMessage(m);
					buildList();
				} catch (Exception e) {
					gui.showError(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DELETE_MESSAGE));
				}
			}
		}
	}


}

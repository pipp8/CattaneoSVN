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

package crymes.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;


import crymes.book.Address;
import crymes.book.AddressService;
import crymes.helpers.ResourceTokens;

/**
 * 	@author sd
 *	@date 29.03.2007
 */
public class AddressEntry extends Form implements CommandListener {
	
	private final Command ok;
	private final Command cancel;
	
	CrymesMidlet gui;

	TextField nameField;

	TextField addressField;

	Displayable caller=null;

	Address address; // address==null is  marker for new Address

	public AddressEntry(AddressList nl, Address a,CrymesMidlet g) {
		super(g.getResourceManager().getString(ResourceTokens.STRING_CHANGE_NAME));
		this.gui=g;
		this.caller = nl;
		this.address = a;
		nameField = new TextField(gui.getResourceManager().getString(ResourceTokens.STRING_NAME), a.getPerson().getName(), 20, TextField.ANY);
		addressField = new TextField(gui.getResourceManager().getString(ResourceTokens.STRING_NUMBER), a.getUrl(), 20, TextField.PHONENUMBER);
		this.append(nameField);
		this.append(addressField);
		this.ok = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
		this.cancel = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.CANCEL, 0);
		this.addCommand(this.ok);
		this.addCommand(this.cancel);
		this.setCommandListener(this);
		this.setTicker(gui.getTicker());
	}

	public AddressEntry(AddressList nl,CrymesMidlet g) {
		super(g.getResourceManager().getString(ResourceTokens.STRING_NEW_ENTRY));
		this.gui=g;
		this.caller = nl;
		this.address = null;
		nameField = new TextField(gui.getResourceManager().getString(ResourceTokens.STRING_NAME), "", 20, TextField.ANY);
		addressField = new TextField(gui.getResourceManager().getString(ResourceTokens.STRING_NUMBER), "", 20, TextField.PHONENUMBER);
		this.append(nameField);
		this.append(addressField);
		this.ok = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
		this.cancel = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.CANCEL, 0);
		this.addCommand(this.ok);
		this.addCommand(this.cancel);
		this.setCommandListener(this);
		this.setTicker(gui.getTicker());
	}

	public void commandAction(Command co, Displayable disp) {
		if (co == this.ok) {
			if(this.nameField.getString().length()<2 || this.addressField.getString().length()<7){
				gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_FORM_INCOMPLETE));
			}else{
			try {
				if (address == null) {
					AddressService.createPerson(this.nameField.getString(), this.addressField.getString());
					gui.setTicketText("added "+this.nameField.getString());
				} else {
					AddressService.alterName(address.getPerson(), this.nameField.getString());
					address.setUrl(addressField.getString());
					AddressService.updateAdress(address);
					gui.setTicketText("updated "+this.nameField.getString());
				}
				if(caller instanceof AddressList)
					((AddressList)caller).buildList();
				Display.getDisplay(gui).setCurrent(caller);
			} catch (Exception e) {
				gui.showError(e.getMessage());
			}
		}
		}
		if (co == this.cancel) {
				Display.getDisplay(gui).setCurrent(caller);	
		}
	}
}

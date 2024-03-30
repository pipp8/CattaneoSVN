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


import crymes.book.AddressService;
import crymes.book.CMessage;
import crymes.helpers.ResourceTokens;

/**
 * @author sd
 */
public class OutBoxList extends MessageList {

	public OutBoxList(CrymesMidlet gui,String title) {
		super(gui,title);
		
	}
	protected void deleteMessage(CMessage m){
		try {
			AddressService.deleteMessage(m);
		} catch (Exception e) {
			gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DELETE_MESSAGE + "\n"+ e.getMessage()));
		}
	}
	protected String getMessageText(CMessage m){
		String text=null;
		try {
			 text = new String(m.getData());
		} catch (Exception e) {
			gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n"+ e.getMessage()));
		}
		return text;
	}

	protected String getNumber(CMessage message) {
		
		return message.getReceiver();
	}

	protected void setMessages() {
		try {
			messages = AddressService.getOutboxMessages();
		} catch (Exception e) {
			gui.showError(e.getMessage());
		}
	}


}
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
package org.cryptosms.gui;

import org.cryptosms.book.AddressService;
import org.cryptosms.book.CMessage;
import org.cryptosms.helpers.ResourceTokens;

/**
 * @author sd
 */
public class OutBoxList extends MessageList {

	public OutBoxList(CryptoSMSMidlet gui,String title) {
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
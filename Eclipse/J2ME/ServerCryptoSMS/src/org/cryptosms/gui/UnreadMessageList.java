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




import javax.microedition.lcdui.Display;

import org.cryptosms.book.AddressService;
import org.cryptosms.book.MessageType;

import org.cryptosms.book.CMessage;

import org.cryptosms.helpers.CryptoHelper;
import org.cryptosms.helpers.Logger;

import org.cryptosms.helpers.ResourceTokens;

/**
 * @author sd
 */
public class UnreadMessageList extends MessageList {
	String text1;

	/**
	 * @param gui
	 */
	public UnreadMessageList(CryptoSMSMidlet gui,String title) {
		super(gui,title);
		
	}
	protected void deleteMessage(CMessage m){
		try {
			AddressService.deleteMessage(m);
		} catch (Exception e) {
			gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DELETE_MESSAGE + "\n"+ e.getMessage()));
		}
	}
	protected String getNumber(CMessage message) {
		
		return message.getSender();
	}
	protected String getMessageText(final CMessage m){
		
		try {
			 text1="hmpf-look in readBox";
			final MessageText mt;
			Display d = Display.getDisplay(gui);
			mt=new MessageText(this,text1,AddressService.findAdress4Url(m.getSender()));
			d.setCurrent( new ProgressScreen(mt , gui.getResourceManager().getString(ResourceTokens.STRING_DECRYPT_MESSAGE),d ){
			
			  public void run(){
				  this.setStatus("progressbar opens");
					try {
						 this.setStatus("decrypt");
						 text1 = new String(CryptoHelper
									.decrypt(m.getData(), gui.getSettings().getPair().getKeyPair()));
							 m.setType(MessageType.SYM_ENCRYPTED_TEXT_INBOX);
							 m.setData(text1.getBytes());
							 mt.setString(text1);
							 AddressService.deleteMessage(m);
							 
							 AddressService.saveMessage(m);
				this.setStatus("progessbar closes");
					} catch (Exception e) {
						Logger.getInstance().write((gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n"+ e.getMessage())));
						//gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n"+ e.getMessage()));
					}finally{
						 stopThread();	
					}
					
			   
			  }
			} );
			
			//Display.getDisplay(gui).setCurrent(new MessageText((MessageList)this.getNext(), text1));
			
			

		} catch (Exception e) {
			gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n"+ e.getMessage()));
		}
		return null;
	}
	protected void setMessages() {
		try {
			messages = AddressService.getInboxUnreadMessages();
		} catch (Exception e) {
			gui.showError(e.getMessage());
		}
	}

}

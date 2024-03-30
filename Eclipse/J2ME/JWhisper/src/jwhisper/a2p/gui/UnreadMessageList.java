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

import gr.bluevibe.fire.displayables.FireScreen;

import jwhisper.crypto.Cipher;
import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;
import jwhisper.crypto.Utils;
import jwhisper.modules.addressBook.AddressService;

import jwhisper.modules.message.CMessage;
import jwhisper.modules.message.MessageType;

import jwhisper.utils.Logger;
import jwhisper.utils.ResourceTokens;


/**
 * @author sd
 */
public class UnreadMessageList extends MessageList {
	String text1;

	/**
	 * @param gui
	 */
	public UnreadMessageList(JWhisperA2PMidlet gui,String title) {
		super(gui,title);
		
	}
	protected void deleteMessage(CMessage m){
		try {
			AddressService.deleteMessage(m);
		} catch (Exception e) {
			_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_DELETE_MESSAGE + "\n"+ e.getMessage()));
		}
	}
	
	protected String getNumber(CMessage message) {
		
		return message.getSender();
	}
	
	protected String getMessageText(final CMessage m){
		
		try {
			text1="Decrypting";
			final MessageText mt;
			FireScreen d = _midlet.getFireScr();
			mt =new MessageText(this,text1,AddressService.findAdress4Url(m.getSender()));
			d.setCurrent( new ProgressScreen(mt , _midlet.getResourceManager().getString(ResourceTokens.STRING_DECRYPT_MESSAGE),d){
			
			public void run(){
				  this.setStatus("progressbar opens");
					try {
						 this.setStatus("Decrypt");
						 
						 
						 Cipher _cipher = Utils.GetCipher(_midlet.getSettings().getAlghoritm());
						 IKeyPairGenerator _keyPairGenerator = Utils.GetKeyPairGenerator(_midlet.getSettings().getAlghoritm());
							
						 KeyPair kp = _keyPairGenerator.getKeyPairFromABytePair( _midlet.getSettings().getPublicKey(), _midlet.getSettings().getPrivateKey());
						 
						 byte [] data = _cipher.decrypt(m.getData(), kp);
						 text1 = new String(data);
						 //text1 = new String(CryptoHelper.decrypt(m.getData(), gui.getSettings().getPair().getKeyPair()));
							 
						 m.setType(MessageType.SYM_ENCRYPTED_TEXT_INBOX);	 
						 m.setData(text1.getBytes());
						 mt.setString(text1);
						 AddressService.deleteMessage(m);
					     AddressService.saveMessage(m);
					     this.setStatus("progessbar closes");
					} 
					catch (Exception e) {
						Logger.getInstance().write((_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n"+ e.getMessage())));
						//gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n"+ e.getMessage()));
					}
					finally{
						 stop();	
					}
			  }
			} );
		} catch (Exception e) {
			_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n"+ e.getMessage()));
		}
		return null;
	}
	
	protected void setMessages() {
		try {
			CMessage [] messagesInbox = AddressService.getInboxUnreadMessages();
			CMessage [] messagesWelcome = AddressService.getInboxWelcomeMessages();
			
			messages = new CMessage[messagesInbox.length+messagesWelcome.length];
			
			System.arraycopy(messagesInbox, 0, messages, 0, messagesInbox.length);
			System.arraycopy(messagesWelcome, 0, messages, messagesInbox.length, messagesWelcome.length);
						
		} catch (Exception e) {
			_midlet.showError(e.getMessage());
		}
	}

}

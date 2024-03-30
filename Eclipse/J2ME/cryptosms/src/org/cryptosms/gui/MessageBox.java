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

import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.cryptosms.book.Address;
import org.cryptosms.book.AddressService;
import org.cryptosms.book.CMessage;
import org.cryptosms.book.MessageType;
import org.cryptosms.connection.MessageSender;
import org.cryptosms.helpers.CryptoHelper;
import org.cryptosms.helpers.Logger;
import org.cryptosms.helpers.ResourceTokens;

class MessageBox extends TextBox implements CommandListener {
	private final Command ok;

	private final Command cancel;

	Address _address=null;

	CryptoSMSMidlet midlet;

	Displayable _parent;

	public MessageBox(Address a, CryptoSMSMidlet gui, Displayable parent) {
		super(gui.getResourceManager().getString(ResourceTokens.STRING_TEXT), "", 100, TextField.ANY);
		midlet = gui;
		_address = a;
		_parent = parent;
		this.ok = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
		this.cancel = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_CANCEL), Command.CANCEL, 0);
		this.addCommand(this.ok);
		this.addCommand(this.cancel);
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}
	public MessageBox( CryptoSMSMidlet gui, Displayable parent) {
		super(gui.getResourceManager().getString(ResourceTokens.STRING_TEXT), "", 100, TextField.ANY);
		midlet = gui;
	
		_parent = parent;
		this.ok = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
		this.cancel = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_CANCEL), Command.CANCEL, 0);
		this.addCommand(this.ok);
		this.addCommand(this.cancel);
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}

	public void commandAction(Command co, Displayable disp) {
		if (co == this.ok) {
			if (_address==null){
				//elect Address and send from AddressList
				Display.getDisplay(midlet).setCurrent(new AddressList( midlet,this.getString()));
			}else{
				//send Message to given Address
				//TODO duplicated Code in AddressList send solve via Command/Executor 
	
				Display d = Display.getDisplay(midlet);
				d.setCurrent( new ProgressScreen(_parent , midlet.getResourceManager().getString(ResourceTokens.STRING_ENCRYPT_MESSAGE),d){
				
				  public void run(){
					  this.setStatus("progressbar opens");
						try {
							 this.setStatus("encrypt");
							 byte[] data = CryptoHelper.encrypt(getString().getBytes(), _address.getPublicKey());

							 if (data == null) {
								 throw new Exception("Aieeh ... encyption failed");
							 }
							
							 //Message to send
							 CMessage mess = new CMessage(midlet.getSettings().getNumber(), _address.getUrl(), MessageType.ENCRYPTED_TEXT_INBOX, data,new Date());
							 // Connection.create(_address.getType().getTypeCode()).send(_address, mess.prepareForTransport());
							
							 if (MessageSender.getInstance() != null)
								 MessageSender.getInstance().send(_address.getUrl(), mess.prepareForTransport());
							
							
//							 AddressService.saveMessage(new CMessage(midlet.getSettings().getNumber(), _address.getUrl(), MessageType.SYM_ENCRYPTED_TEXT_OUTBOX,
//									getString().getBytes(),new Date()));
							 //make the message Object to an outboxMessage
							 mess.setType(MessageType.SYM_ENCRYPTED_TEXT_OUTBOX);
							 mess.setData(getString().getBytes());
							 AddressService.saveMessage(mess);
							 //AddressService.saveMessage(mess);
							 midlet.setTicketText(midlet.getResourceManager().getString(ResourceTokens.STRING_SENDING_MESSAGE));
							
					
							 this.setStatus("progressbar closes");
						
						} 
						catch (Exception e) {
							Logger.getInstance().write((midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_THREAD  + "\n"+ e.getMessage())));
						}
						finally{
							stop();
						}
				  }
				} );
			}
		}
		if (co == this.cancel) {
			Display.getDisplay(midlet).setCurrent(_parent);
		}
		
	}
}

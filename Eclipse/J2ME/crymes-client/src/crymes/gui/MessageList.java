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

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;


import crymes.book.Address;
import crymes.book.AddressService;
import crymes.book.CMessage;
import crymes.helpers.Helpers;
import crymes.helpers.ResourceTokens;

/**
 * @author sd
 */
public abstract class MessageList extends List implements CommandListener {
	CrymesMidlet gui;
	
	CMessage[] messages;

	private final Command back;

	private final Command showText;

	private final Command delete;



	public MessageList(CrymesMidlet gui,String title) {
		super(title, Choice.IMPLICIT);
		this.gui = gui;

		this.back = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.BACK, 1);
		this.showText = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_VIEW_MESSAGE), Command.OK, 0);
		this.delete = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_DELETE_MESSAGE), Command.OK, 0);

		this.addCommand(this.back);
		this.addCommand(this.showText);
		this.addCommand(this.delete);

		this.setCommandListener(this);
		this.setTicker(gui.getTicker());
		buildList();
	}

	protected abstract void setMessages(); 	 
	protected abstract void deleteMessage(CMessage m);
	protected abstract String getMessageText(CMessage m);
	protected abstract String getNumber(CMessage message) ;

	private void buildList() {
		setMessages();
		this.deleteAll();
		String number;
		String time;
		Address address=null;
		for (int i = 0; i < messages.length; i++) {
			number=getNumber(messages[i]);//.getSender();
			time=Helpers.dateToStringFull(messages[i].getTime());
			
			try {
				address=AddressService.findAdress4Url(number);
			} catch (Exception e) {
				// TODO i18n
				//e.printStackTrace();
			}
			if (address==null)
				this.append(number+" "+time,null);
			else
				this.append(address.getPerson().getName()+" "+time,null);
				
		}
	}



	public void commandAction(Command co, Displayable disp) {

		if (co == this.showText) {
			if (this.getSelectedIndex()>=0){
				CMessage m = messages[this.getSelectedIndex()];				
						String text =null;
						text=getMessageText(m);
						if(text!=null)
							try {
								Display.getDisplay(gui).setCurrent(new MessageText(this, text,AddressService.findAdress4Url(m.getSender())));
							} catch (Exception e) {
								gui.setTicketText(ResourceTokens.STRING_ERROR_NO_PERSON_WITH_NUMBER);
							}
			}
		}



		if (co == this.back) {
			gui.showMenu();
		}

		if (co == this.delete) {
			if (this.getSelectedIndex()>=0){
				CMessage m = messages[this.getSelectedIndex()];
				deleteMessage(m);
				buildList();
			}
		}
	}


	class MessageText extends TextBox implements CommandListener {
		private final Command ok;
//		private final Command reply;
		MessageList nl;
		Address a;

		//Person ; // person==null marker for new person

		public MessageText(MessageList nl, String text,Address a) {
			super(gui.getResourceManager().getString(ResourceTokens.STRING_MESSAGE), text, 255, TextField.UNEDITABLE);
			this.nl = nl;
			this.a=a;
			this.ok = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
//			this.reply = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_REPLY), Command.OK, 0);
			this.addCommand(this.ok);
//			this.addCommand(this.reply);
			this.setCommandListener(this);
			this.setTicker(gui.getTicker());
		}

		public void commandAction(Command co, Displayable disp) {
			if (co == this.ok) {
				try {
					nl.buildList();
					Display.getDisplay(gui).setCurrent(nl);
				} catch (Exception e) {
					gui.showError(e.getMessage());
				}
			}
//			if (co == this.reply) {
//				try {
//					Display.getDisplay(gui).setCurrent(new MessageBox(a, gui, gui.getMenu()));
//				} catch (Exception e) {
//					gui.showError(e.getMessage());
//				}
//			}
		}
	}
}
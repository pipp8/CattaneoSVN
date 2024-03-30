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

import gr.bluevibe.fire.components.Component;
import gr.bluevibe.fire.components.ListBox;
import gr.bluevibe.fire.components.ListElement;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Popup;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.util.CommandListener;
import gr.bluevibe.fire.util.FireIO;

import jwhisper.modules.message.CMessage;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.addressBook.Address;
import jwhisper.utils.Helpers;
import jwhisper.utils.ResourceTokens;

/**
 * @author sd
 */
public abstract class MessageList extends Panel implements CommandListener {
	JWhisperA2PMidlet _midlet;
	
	CMessage[] messages;

	private Command back;

	private Command showText;

	private Command delete;
	
	private Command menuCmd;
	
	private ListBox choice;
	
	private Popup menu = new Popup();

	public MessageList(JWhisperA2PMidlet gui,String title) {
		this.setLabel(title);
		this._midlet = gui;
		
		buildList();
		
		back = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.EXIT,1);
		this.addCommand(back); // add the command to the panel, 

		showText = new Command("",Command.OK,1);
		delete = new Command("",Command.OK,1);

		// in a popup panel you can add everything you add on a normal panel.
		// it even shown a scrollbar if you put too much elements in it.
		Row rowView= new Row(_midlet.getResourceManager().getString(ResourceTokens.STRING_VIEW_MESSAGE));		
		rowView.addCommand(showText);
		rowView.setCommandListener(this);
		Row rowDelete= new Row(_midlet.getResourceManager().getString(ResourceTokens.STRING_DELETE_MESSAGE));		
		rowDelete.addCommand(delete);
		rowDelete.setCommandListener(this);
		
		this.menu.add(rowView);
		this.menu.add(rowDelete);

		// There are two ways to display a popup. 
		// The first is by directly calling the FireScreen.getScreen(null).showPopup(myPopup);
		// The second is indirectly by associating it with a softkey command on a panel using 
		// the panel.addCommand(popup,command) method like below:
		menuCmd = new Command("Menu",Command.BACK,1); // set the Command  as BACK in order to be assigned on the left softkey
		this.addCommand(menuCmd, this.menu);
		this.setCommandListener(this);		
		this.setTicker(_midlet.getTicker());
	}

	protected abstract void setMessages(); 	 
	protected abstract void deleteMessage(CMessage m);
	protected abstract String getMessageText(CMessage m);
	protected abstract String getNumber(CMessage message) ;

	private void clearList() {
		for (int i=this.countRows()-1; i >= 0; --i) {
			this.remove(i); // remove last component.
			this.resetPointer(); // reset the pointer position.
		}
		this.validate();
		_midlet.getFireScr().repaint();
	}
	
	private void buildList() {
		setMessages();

		clearList();
		
		choice = new ListBox();
		choice.setMultiple(false);
		choice.setBullet(FireIO.getLocalImage("box"));  // if not set, a filled circle is drawn as a bullet..
		choice.setSelectedBullet(FireIO.getLocalImage("checkedbox"));  // comment these two lines to see the difference.
		this.add(choice);
		
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
			if (address==null) {
				choice.add(new ListElement(number+" "+time, String.valueOf(i), false));
			}
			else {
				choice.add(new ListElement(address.getPerson().getName()+" "+time, String.valueOf(i), false));
			}
		}
		
		_midlet.getFireScr().repaint();
	}

	public void commandAction(Command co, Component disp) {
		
		if (co == this.showText) {
			_midlet.getFireScr().closePopup(); // first close the popup.
			
			if (choice.getCheckedElements().size() > 0) {
				ListElement selected = (ListElement)(choice.getCheckedElements().elementAt(0));
				CMessage m = messages[Integer.parseInt((String)selected.getId())];				
						String text =null;
						text=getMessageText(m);
						if(text!=null)
							try {
								_midlet.getFireScr().setCurrent(new MessageText(this, text, AddressService.findAdress4Url(m.getSender())));
							} catch (Exception e) {
								_midlet.setTicketText(ResourceTokens.STRING_ERROR_NO_PERSON_WITH_NUMBER);
							}
			}
			else {
				_midlet.getFireScr().getCurrentPanel().showAlert("Per favore marca il messaggio da leggere", null);
			}
		}

		if (co == this.back) {
			_midlet.getFireScr().closePopup(); // first close the popup.
			_midlet.showMenu();
		}

		if (co == this.delete) {
			_midlet.getFireScr().closePopup(); // first close the popup.
			
			if (choice.getCheckedElements().size() > 0) {
				ListElement selected = (ListElement)(choice.getCheckedElements().elementAt(0));
				CMessage m = messages[Integer.parseInt((String)selected.getId())];
				deleteMessage(m);
				buildList();
				_midlet.getFireScr().setCurrent(this);
			}
			else {
				_midlet.getFireScr().getCurrentPanel().showAlert("Per favore marca il messaggio da cancellare", null);
			}
		}
	}


	class MessageText extends Panel implements CommandListener {
		private final Command ok;
		MessageList nl;
		Address a;
		Row textMessage;

		public MessageText(MessageList nl, String text, Address a) {
			if (a==null) {
				
			}
			else {
				this.setLabel(a.getUrl());
			}
			this.nl = nl;
			this.a=a;
			textMessage = new Row(text);
			this.add(textMessage);
			
			this.ok = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
			this.addCommand(this.ok);
			this.setCommandListener(this);
			this.setTicker(_midlet.getTicker());
		}

		public void commandAction(Command cmd, Component c) {
			if (cmd == this.ok) {
				try {
					nl.buildList();
					_midlet.getFireScr().setCurrent(nl);
				} catch (Exception e) {
					_midlet.showError(e.getMessage());
				}
			}
//			if (co == this.reply) {
//				try {
//					Display.getDisplay(_midlet).setCurrent(new MessageBox(a, _midlet, _midlet.getMenu()));
//				} catch (Exception e) {
//					_midlet.showError(e.getMessage());
//				}
//			}
			
		}

		public void setString(String text1) {
			textMessage.setText(text1);
			// Validate nameRow.
			textMessage.validate();
			_midlet.getFireScr().repaint();
			
		}
	}
}
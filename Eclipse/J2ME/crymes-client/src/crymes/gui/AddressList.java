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

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;


import crymes.book.Address;
import crymes.book.AddressService;
import crymes.book.CMessage;
import crymes.book.MessageType;
import crymes.book.Person;
import crymes.connection.MessageSender;
import crymes.helpers.CryptoHelper;
import crymes.helpers.Logger;
import crymes.helpers.ResourceTokens;

/**
 * @author sd
 */
public class AddressList extends List implements CommandListener {
	CrymesMidlet gui;
	Vector adresses = new Vector();

	private  Command back=null;
	private  Command delete=null;

	private  Command add=null;

	private  Command edit=null;
	
	private  Command send=null;

	private  Command newMessage=null;

	private  Command sendPublikKey=null;
	
	private  Command importKey=null;
	private String _messageText=null;
	
	Image _key=null;
	
	Image _noKey=null;

	/**
	 * Creates a new AddressList object. 
	 * @throws IOException 
	 */
	public AddressList(CrymesMidlet gui)  {
		super(gui.getResourceManager().getString(ResourceTokens.STRING_ADDRESSBOOK), Choice.IMPLICIT);
		this.gui = gui;

		this.back = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.BACK, 1);

		this.add = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_NEW_ENTRY), Command.OK, 0);
		this.edit = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_EDIT_ENTRY), Command.OK, 0);
		this.newMessage = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_NEW_MESSAGE), Command.OK, 0);
		this.sendPublikKey = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_SEND_PUBKEY), Command.OK, 0);
		this.importKey = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_IMPORT_KEY_FROM_RECEIVED), Command.OK, 0);
		this.delete = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_DELETE_ADDRESS), Command.OK, 0);

		this.addCommand(this.back);

		this.addCommand(this.add);
		this.addCommand(this.edit);
		this.addCommand(this.newMessage);
		this.addCommand(this.sendPublikKey);
		this.addCommand(this.importKey);
		this.addCommand(this.delete);
		this.setCommandListener(this);
		
		this.setTicker(gui.getTicker());
		
		try {
			_noKey=Image.createImage("/global/icons/nokey.png");
			_key=Image.createImage("/global/icons/key.png");
		} catch (IOException e) {
			gui.showError(e.getMessage());
		}
		buildList();
		
	}

	/**
	 * @param midlet
	 * @param string
	 */
	public AddressList(CrymesMidlet midlet, String string) {
		super(midlet.getResourceManager().getString(ResourceTokens.STRING_ADDRESS_CHOICE), Choice.IMPLICIT);
		this.gui = midlet;
		this._messageText=string;
		
		this.back = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.BACK, 1);

		this.send = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_SEND_MESSAGE), Command.OK, 0);
		this.add = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_NEW_ENTRY), Command.OK, 0);
		this.edit = new Command(gui.getResourceManager().getString(ResourceTokens.STRING_EDIT_ENTRY), Command.OK, 0);
		
		this.addCommand(this.back);
		this.addCommand(this.send);
		this.addCommand(this.add);
		this.addCommand(this.edit);
		

		this.setCommandListener(this);
		this.setTicker(gui.getTicker());
		
		try {
			_noKey=Image.createImage("/global/icons/nokey.png");
			_key=Image.createImage("/global/icons/key.png");
		} catch (IOException e) {
			gui.showError(e.getMessage());
		}
		buildList();
	}

	void buildList() {
		Person[] pers;
		adresses.removeAllElements();

		try {
			pers = AddressService.getPersons();

			this.deleteAll();

			for (int i = 0; i < pers.length; i++) {
				if (pers[i].getConns().length > 0) {
					for (int j = 0; j < pers[i].getConns().length; j++) {
						adresses.addElement(pers[i].getConns()[j]);
						this.append(pers[i].getName() + gui.getResourceManager().getString(ResourceTokens.STRING_NUMBER_WITH_SPACES) + pers[i].getConns()[j].getUrl() + gui.getResourceManager().getString(ResourceTokens.STRING_KEY_1)
								+ pers[i].getConns()[j].hasKey(), listImage(pers[i].getConns()[j].hasKey()));
					}
				} else {
					this.append(pers[i].getName(), null);
				}
			}
		} catch (Exception e) {
			gui.showError(e.getMessage());
		}
	}
	private Image listImage(boolean hasKey){
		if (hasKey)
			return _key;
		else
			return _noKey;
	}

	public void commandAction(Command co, Displayable disp) {
		if(co==this.send){
			if(this.getSelectedIndex()>=0){
			Address a = (Address) adresses.elementAt(this.getSelectedIndex());
			if (a.hasKey()){
				if(_messageText!=null){
					//send Message to given Address
					//TODO duplicated Code in MessageBox  solve via Command/Executor 
		
					Display d = Display.getDisplay(gui);
					d.setCurrent( new ProgressScreen(gui.getMenu() , gui.getResourceManager().getString(ResourceTokens.STRING_ENCRYPT_MESSAGE),d){
					
					  public void run(){
						  Address ad = (Address) adresses.elementAt(getSelectedIndex());
						  this.setStatus("progressbar opens");
							try {
								 this.setStatus("encrypt");
							  byte[] data = CryptoHelper.encrypt(_messageText.getBytes(), ad.getPublicKey());

							  if (data == null) {
									throw new Exception("Aieeh ... encyption failed");
								}
								//Message to send
								CMessage mess = new CMessage(gui.getSettings().getNumber(), ad.getUrl(), MessageType.ENCRYPTED_TEXT_INBOX,
										data,new Date());
								
									if (MessageSender.getInstance() != null)
										MessageSender.getInstance().send(ad.getUrl(), mess.prepareForTransport());
								
//								AddressService.saveMessage(new CMessage(gui.getSettings().getNumber(), ad.getUrl(), MessageType.SYM_ENCRYPTED_TEXT_OUTBOX,
//										_message.getBytes(),new Date()));
								mess.setType(MessageType.SYM_ENCRYPTED_TEXT_OUTBOX);
								mess.setData(_messageText.getBytes());
								
								AddressService.saveMessage(mess);
						this.setStatus("progressbar closes");
							} catch (Exception e) {
								System.out.println("mau qui 1");
								Logger.getInstance().write((gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_THREAD  + "\n"+ e.getMessage())));
								
							}finally{
								 stop();
							}
					   
					  }
					} );
					
					

					
				}else{
					gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_NO_MESSAGEBOX));
				}
			}else{
				gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_NO_KEY_FOR_SENDER));
			}
			}else
			{
				gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_SELECT_PLEASE));
			}
			
		}
		if (co == this.back) {
			if (_messageText==null){
				gui.showMenu();
			}else{
				gui.showMenu();
			}
		}

		if (co == this.add) {
			Display.getDisplay(gui).setCurrent(new AddressEntry(this,gui));
			
		}

		if (co == this.edit) {
			if (this.getSelectedIndex()>=0){
			Display.getDisplay(gui).setCurrent(
					new AddressEntry(this, (Address) adresses.elementAt(this.getSelectedIndex()),gui));
			}
		}
		if (co == this.delete) {
			if (this.getSelectedIndex()>=0){
				try{
				Address a = (Address) adresses.elementAt(this.getSelectedIndex());
				AddressService.deleteAddress(a);
				}catch(Exception e){
					// System.out.println("aaaa"+e.getMessage());
					gui.showError(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_COULD_NOT_DELETE_ADDRESS));
				}
			buildList();
			}
		}

		if (co == this.newMessage) {
			if (this.getSelectedIndex()>=0){
			Address a = (Address) adresses.elementAt(this.getSelectedIndex());

			if (a.hasKey()) {
				Display.getDisplay(gui).setCurrent(new MessageBox(a, gui, this));
			} else {
				
				gui.showError(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_ONLY_ENCRYPTED_SENDING));
			}
			}
		}
		
		if (co == this.sendPublikKey) {
			if (this.getSelectedIndex()>=0){
				try {
					
					Address a = (Address) adresses.elementAt(this.getSelectedIndex());
					byte[] data = gui.getSettings().getPublicKey();
					
					CMessage n = new CMessage(gui.getSettings().getNumber(), a.getUrl(), MessageType.KEY, data,new Date());
					// Connection.create(a.getType().getTypeCode()).send(a, n.prepareForTransport());
					if (MessageSender.getInstance() != null)
						MessageSender.getInstance().send(a.getUrl(), n.prepareForTransport());
					
				} catch (Exception e) {
					// System.out.println(e.toString());
					gui.showError(e.getMessage());
				}
			}
		}
		if (co == this.importKey) {
			if (this.getSelectedIndex()>=0){
			try {
				Address a = (Address) adresses.elementAt(this.getSelectedIndex());
				byte[] d=AddressService.findPublicKey4Address(a);
				if (d==null){
					gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_NO_PERSON_WITH_NUMBER)+a.getUrl());
					
				}else{
					AddressService.importKey4Url(a.getUrl(), d);
					gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ADDED_KEY_TO_PERSON)+a.getUrl());
					buildList();
				}
			} catch (Exception e) {
				gui.showError(e.getMessage());
			}
			}
		}
	}

}

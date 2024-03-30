/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.gui;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import jwhisper.modules.addressBook.Address;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.message.CMessage;
import jwhisper.modules.message.MessageType;
import jwhisper.utils.ResourceTokens;




public class KeyList extends List implements CommandListener {
	JWhisperMidlet _midlet;

	CMessage[] messages;

	private final Command back;


	private final Command delete;

	private final Command importKey;
	
	//private final Command createAddress4Key;

	public KeyList(JWhisperMidlet midlet) {
		super(midlet.getResourceManager().getString(ResourceTokens.STRING_PUBLIC_KEYS), Choice.IMPLICIT);
		
		this._midlet = midlet;
		this.back = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.BACK, 1);
		this.importKey = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_IMPORT_KEY), Command.OK, 0);
		this.delete = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_DELETE_KEY), Command.OK, 0);

		this.addCommand(this.back);
		this.addCommand(importKey);
		this.addCommand(this.delete);

		this.setCommandListener(this);
		this.setTicker(_midlet.getTicker());
		buildList();
	}

	private void setMessages() {
		try {
			messages = AddressService.getPublicKeys();
		} catch (Exception e) {
			_midlet.showError(e.getMessage());
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
						_midlet.setTicketText(_midlet.getResourceManager().getString(ResourceTokens.STRING_ADDED_KEY_TO_PERSON) + m.getSender());
						
					} catch (Exception e) {
						_midlet.showError(e.getMessage());
					}
				} else {
					_midlet.showError(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_WRONG_TYPE3));
				}
			}
		}

		if (co == this.back) {
			_midlet.showMenu();
		}

		if (co == this.delete) {
			if (this.getSelectedIndex()>=0){
				CMessage m = messages[this.getSelectedIndex()];
				try {
					AddressService.deleteMessage(m);
					buildList();
				} catch (Exception e) {
					_midlet.showError(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_DELETE_MESSAGE));
				}
			}
		}
	}


}

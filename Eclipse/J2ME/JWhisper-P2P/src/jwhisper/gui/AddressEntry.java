/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import jwhisper.modules.addressBook.Address;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.utils.ResourceTokens;



public class AddressEntry extends Form implements CommandListener {
	
	private final Command ok;
	private final Command cancel;
	
	JWhisperMidlet _midlet;

	TextField nameField;

	TextField addressField;

	Displayable caller=null;

	Address address; // address==null is  marker for new Address

	public AddressEntry(AddressList nl, Address a,JWhisperMidlet g) {
		super(g.getResourceManager().getString(ResourceTokens.STRING_CHANGE_NAME));
		this._midlet=g;
		this.caller = nl;
		this.address = a;
		nameField = new TextField(_midlet.getResourceManager().getString(ResourceTokens.STRING_NAME), a.getPerson().getName(), 20, TextField.ANY);
		addressField = new TextField(_midlet.getResourceManager().getString(ResourceTokens.STRING_NUMBER), a.getUrl(), 20, TextField.PHONENUMBER);
		this.append(nameField);
		this.append(addressField);
		this.ok = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
		this.cancel = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.CANCEL, 0);
		this.addCommand(this.ok);
		this.addCommand(this.cancel);
		this.setCommandListener(this);
		this.setTicker(_midlet.getTicker());
	}

	public AddressEntry(AddressList nl,JWhisperMidlet g) {
		super(g.getResourceManager().getString(ResourceTokens.STRING_NEW_ENTRY));
		this._midlet=g;
		this.caller = nl;
		this.address = null;
		nameField = new TextField(_midlet.getResourceManager().getString(ResourceTokens.STRING_NAME), "", 20, TextField.ANY);
		addressField = new TextField(_midlet.getResourceManager().getString(ResourceTokens.STRING_NUMBER), "", 20, TextField.PHONENUMBER);
		this.append(nameField);
		this.append(addressField);
		this.ok = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
		this.cancel = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.CANCEL, 0);
		this.addCommand(this.ok);
		this.addCommand(this.cancel);
		this.setCommandListener(this);
		this.setTicker(_midlet.getTicker());
	}

	public void commandAction(Command co, Displayable disp) {
		if (co == this.ok) {
			if(this.nameField.getString().length()<2 || this.addressField.getString().length()<7){
				_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_INCOMPLETE));
			}else{
			try {
				if (address == null) {
					AddressService.createPerson(this.nameField.getString(), this.addressField.getString());
					_midlet.setTicketText("added "+this.nameField.getString());
				} else {
					AddressService.alterName(address.getPerson(), this.nameField.getString());
					address.setUrl(addressField.getString());
					AddressService.updateAdress(address);
					_midlet.setTicketText("updated "+this.nameField.getString());
				}
				if(caller instanceof AddressList)
					((AddressList)caller).buildList();
				Display.getDisplay(_midlet).setCurrent(caller);
			} catch (Exception e) {
				_midlet.showError(e.getMessage());
			}
		}
		}
		if (co == this.cancel) {
				Display.getDisplay(_midlet).setCurrent(caller);	
		}
	}
}

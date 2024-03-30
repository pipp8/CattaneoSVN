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
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import jwhisper.modules.addressBook.Address;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.message.CMessage;
import jwhisper.utils.Helpers;
import jwhisper.utils.ResourceTokens;



public abstract class MessageList extends List implements CommandListener {
	JWhisperMidlet _midlet;
	
	CMessage[] messages;

	private final Command back;

	private final Command showText;

	private final Command delete;



	public MessageList(JWhisperMidlet midlet,String title) {
		super(title, Choice.IMPLICIT);
		this._midlet = midlet;

		this.back = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.BACK, 1);
		this.showText = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_VIEW_MESSAGE), Command.OK, 0);
		this.delete = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_DELETE_MESSAGE), Command.OK, 0);

		this.addCommand(this.back);
		this.addCommand(this.showText);
		this.addCommand(this.delete);

		this.setCommandListener(this);
		this.setTicker(_midlet.getTicker());
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
								Display.getDisplay(_midlet).setCurrent(new MessageText(this, text,AddressService.findAdress4Url(m.getSender())));
							} catch (Exception e) {
								_midlet.setTicketText(ResourceTokens.STRING_ERROR_NO_PERSON_WITH_NUMBER);
							}
			}
		}



		if (co == this.back) {
			_midlet.showMenu();
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
			super(_midlet.getResourceManager().getString(ResourceTokens.STRING_MESSAGE), text, 255, TextField.UNEDITABLE);
			this.nl = nl;
			this.a=a;
			this.ok = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
//			this.reply = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_REPLY), Command.OK, 0);
			this.addCommand(this.ok);
//			this.addCommand(this.reply);
			this.setCommandListener(this);
			this.setTicker(_midlet.getTicker());
		}

		public void commandAction(Command co, Displayable disp) {
			if (co == this.ok) {
				try {
					nl.buildList();
					Display.getDisplay(_midlet).setCurrent(nl);
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
	}
}
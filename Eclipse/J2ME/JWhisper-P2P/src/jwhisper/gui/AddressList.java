/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.gui;

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


import jwhisper.crypto.Cipher;
import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;
import jwhisper.crypto.Utils;



import jwhisper.modules.addressBook.Address;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.addressBook.Person;
import jwhisper.modules.connection.MessageSender;
import jwhisper.modules.message.CMessage;
import jwhisper.modules.message.MessageType;
import jwhisper.utils.Logger;
import jwhisper.utils.ResourceTokens;




public class AddressList extends List implements CommandListener {
	private JWhisperMidlet _midlet = null;
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

	/*
	 * Creates a new AddressList object. 
	 * @throws IOException 
	 */
	public AddressList(JWhisperMidlet midlet)  {
		super(midlet.getResourceManager().getString(ResourceTokens.STRING_ADDRESSBOOK), Choice.IMPLICIT);
		this._midlet = midlet;
		

		this.back = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.BACK, 1);

		this.add = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_NEW_ENTRY), Command.OK, 0);
		this.edit = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_EDIT_ENTRY), Command.OK, 0);
		this.newMessage = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_NEW_MESSAGE), Command.OK, 0);
		this.sendPublikKey = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_SEND_PUBKEY), Command.OK, 0);
		this.importKey = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_IMPORT_KEY_FROM_RECEIVED), Command.OK, 0);
		this.delete = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_DELETE_ADDRESS), Command.OK, 0);

		this.addCommand(this.back);

		this.addCommand(this.add);
		this.addCommand(this.edit);
		this.addCommand(this.newMessage);
		this.addCommand(this.sendPublikKey);
		this.addCommand(this.importKey);
		this.addCommand(this.delete);
		this.setCommandListener(this);
		
		this.setTicker(_midlet.getTicker());
		
		try {
			_noKey=Image.createImage("/global/icons/nokey.png");
			_key=Image.createImage("/global/icons/key.png");
		} catch (IOException e) {
			_midlet.showError(e.getMessage());
		}
		buildList();
		
	}

	/*
	 * @param midlet
	 * @param string
	 */
	public AddressList(JWhisperMidlet midlet, String string) {
		super(midlet.getResourceManager().getString(ResourceTokens.STRING_ADDRESS_CHOICE), Choice.IMPLICIT);
		this._midlet = midlet;
		this._messageText=string;
		
		this.back = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_BACK), Command.BACK, 1);

		this.send = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_SEND_MESSAGE), Command.OK, 0);
		this.add = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_NEW_ENTRY), Command.OK, 0);
		this.edit = new Command(_midlet.getResourceManager().getString(ResourceTokens.STRING_EDIT_ENTRY), Command.OK, 0);
		
		this.addCommand(this.back);
		this.addCommand(this.send);
		this.addCommand(this.add);
		this.addCommand(this.edit);
		

		this.setCommandListener(this);
		this.setTicker(_midlet.getTicker());
		
		try {
			_noKey=Image.createImage("/global/icons/nokey.png");
			_key=Image.createImage("/global/icons/key.png");
		} catch (IOException e) {
			_midlet.showError(e.getMessage());
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
				if (pers[i].getAddresses().length > 0) {
					for (int j = 0; j < pers[i].getAddresses().length; j++) {
						adresses.addElement(pers[i].getAddresses()[j]);
						this.append(pers[i].getName() + _midlet.getResourceManager().getString(ResourceTokens.STRING_NUMBER_WITH_SPACES) + pers[i].getAddresses()[j].getUrl() + _midlet.getResourceManager().getString(ResourceTokens.STRING_KEY_1)
								+ pers[i].getAddresses()[j].hasKey(), listImage(pers[i].getAddresses()[j].hasKey()));
					}
				} else {
					this.append(pers[i].getName(), null);
				}
			}
		} catch (Exception e) {
			_midlet.showError(e.getMessage());
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
		
					Display d = Display.getDisplay(_midlet);
					d.setCurrent( new ProgressScreen(_midlet.getMenu() , _midlet.getResourceManager().getString(ResourceTokens.STRING_ENCRYPT_MESSAGE),d){
					
					  public void run(){
						  Address ad = (Address) adresses.elementAt(getSelectedIndex());
						  this.setStatus("progressbar opens");
							try {
								 this.setStatus("encrypt");
								
								 
									Cipher _cipher = Utils.GetCipher(_midlet.getSettings().getAlghoritm());
									IKeyPairGenerator _keyPairGenerator = Utils.GetKeyPairGenerator(_midlet.getSettings().getAlghoritm());
									
									KeyPair kp = _keyPairGenerator.getKeyPairFromABytePair(ad.getPublicKey(), null);
									
									
									byte[] data = _cipher.encrypt(_messageText.getBytes(), kp);
								 
							  

							  if (data == null) {
									throw new Exception("Aieeh ... encyption failed");
								}
								//Message to send
								CMessage mess = new CMessage(_midlet.getSettings().getNumber(), ad.getUrl(), MessageType.ENCRYPTED_TEXT_INBOX,
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
								Logger.getInstance().write((_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_THREAD  + "\n"+ e.getMessage())));
								
							}finally{
								 stop();
							}
					   
					  }
					} );
					
					

					
				}else{
					_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_NO_MESSAGEBOX));
				}
			}else{
				_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_NO_KEY_FOR_SENDER));
			}
			}else
			{
				_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_SELECT_PLEASE));
			}
			
		}
		if (co == this.back) {
			if (_messageText==null){
				_midlet.showMenu();
			}else{
				_midlet.showMenu();
			}
		}

		if (co == this.add) {
			Display.getDisplay(_midlet).setCurrent(new AddressEntry(this,_midlet));
			
		}

		if (co == this.edit) {
			if (this.getSelectedIndex()>=0){
			Display.getDisplay(_midlet).setCurrent(
					new AddressEntry(this, (Address) adresses.elementAt(this.getSelectedIndex()),_midlet));
			}
		}
		if (co == this.delete) {
			if (this.getSelectedIndex()>=0){
				try{
				Address a = (Address) adresses.elementAt(this.getSelectedIndex());
				AddressService.deleteAddress(a);
				}catch(Exception e){
					// System.out.println("aaaa"+e.getMessage());
					_midlet.showError(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_COULD_NOT_DELETE_ADDRESS));
				}
			buildList();
			}
		}

		if (co == this.newMessage) {
			if (this.getSelectedIndex()>=0){
			Address a = (Address) adresses.elementAt(this.getSelectedIndex());

			if (a.hasKey()) {
				Display.getDisplay(_midlet).setCurrent(new MessageBox(a, _midlet, this));
			} else {
				
				_midlet.showError(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_ONLY_ENCRYPTED_SENDING));
			}
			}
		}
		
		if (co == this.sendPublikKey) {
			if (this.getSelectedIndex()>=0){
				try {
					
					Address a = (Address) adresses.elementAt(this.getSelectedIndex());
					byte[] data = _midlet.getSettings().getPublicKey();
					CMessage n = new CMessage(_midlet.getSettings().getNumber(), a.getUrl(), MessageType.KEY, data,new Date());
					// Connection.create(a.getType().getTypeCode()).send(a, n.prepareForTransport());
					if (MessageSender.getInstance() != null)
						MessageSender.getInstance().send(a.getUrl(), n.prepareForTransport());
					
				} catch (Exception e) {
					// System.out.println(e.toString());
					_midlet.showError(e.getMessage());
				}
			}
		}
		if (co == this.importKey) {
			if (this.getSelectedIndex()>=0){
			try {
				Address a = (Address) adresses.elementAt(this.getSelectedIndex());
				byte[] d=AddressService.findPublicKey4Address(a);
				if (d==null){
					_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_NO_PERSON_WITH_NUMBER)+a.getUrl());
					
				}else{
					AddressService.importKey4Url(a.getUrl(), d);
					_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_ADDED_KEY_TO_PERSON)+a.getUrl());
					buildList();
				}
			} catch (Exception e) {
				_midlet.showError(e.getMessage());
			}
			}
		}
	}

}

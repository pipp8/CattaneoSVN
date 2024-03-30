/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.p2p.gui;

import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import jwhisper.crypto.Cipher;
import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;
import jwhisper.crypto.Utils;

import jwhisper.modules.addressBook.Address;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.connection.MessageSender;
import jwhisper.modules.message.CMessage;
import jwhisper.modules.message.MessageType;
import jwhisper.utils.Logger;
import jwhisper.utils.ResourceTokens;



class MessageBox extends TextBox implements CommandListener {
	private final Command ok;

	private final Command cancel;

	Address _address=null;

	JWhisperP2PMidlet midlet;

	Displayable _parent;

	public MessageBox(Address a, JWhisperP2PMidlet gui, Displayable parent) {
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
	public MessageBox( JWhisperP2PMidlet gui, Displayable parent) {
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
							 
								Cipher _cipher = Utils.GetCipher(midlet.getSettings().getAlghoritm());
								IKeyPairGenerator _keyPairGenerator = Utils.GetKeyPairGenerator(midlet.getSettings().getAlghoritm());
								
								KeyPair kp = _keyPairGenerator.getKeyPairFromABytePair(_address.getPublicKey(), null);
								
								
							 
							 byte[] data = _cipher.encrypt(getString().getBytes(), kp);
							 

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
							e.printStackTrace();
							//TODO remove stacktrace
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

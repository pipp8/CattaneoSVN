/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.gui;




import javax.microedition.lcdui.Display;

import jwhisper.crypto.Cipher;
import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;
import jwhisper.crypto.Utils;

import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.message.CMessage;
import jwhisper.modules.message.MessageType;
import jwhisper.utils.Logger;
import jwhisper.utils.ResourceTokens;






public class UnreadMessageList extends MessageList {
	String text1;

	/**
	 * @param gui
	 */
	public UnreadMessageList(JWhisperMidlet midlet,String title) {
		super(midlet,title);
		
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
			text1="hmpf-look in readBox";
			final MessageText mt;
			Display d = Display.getDisplay(_midlet);
			mt =new MessageText(this,text1,AddressService.findAdress4Url(m.getSender()));
			d.setCurrent( new ProgressScreen(mt , _midlet.getResourceManager().getString(ResourceTokens.STRING_DECRYPT_MESSAGE),d){
			
			public void run(){
				  this.setStatus("progressbar opens");
					try {
						 this.setStatus("decrypt");
						 
						 Cipher _cipher = Utils.GetCipher(_midlet.getSettings().getAlghoritm());
							IKeyPairGenerator _keyPairGenerator = Utils.GetKeyPairGenerator(_midlet.getSettings().getAlghoritm());
							
							KeyPair kp = _keyPairGenerator.getKeyPairFromABytePair( _midlet.getSettings().getPublicKey(), _midlet.getSettings().getPrivateKey());
							
							
						 
						 byte[] data = _cipher.decrypt(m.getData(), kp);
						 text1 = new String(data);
						 
							 
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
			
			//Display.getDisplay(gui).setCurrent(new MessageText((MessageList)this.getNext(), text1));
			
			

		} catch (Exception e) {
			_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n"+ e.getMessage()));
		}
		return null;
	}
	
	protected void setMessages() {
		try {
			messages = AddressService.getInboxUnreadMessages();
		} catch (Exception e) {
			_midlet.showError(e.getMessage());
		}
	}

}

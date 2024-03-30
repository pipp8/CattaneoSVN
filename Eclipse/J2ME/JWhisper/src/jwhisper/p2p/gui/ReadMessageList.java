/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.p2p.gui;



import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.message.CMessage;
import jwhisper.utils.ResourceTokens;


public class ReadMessageList extends MessageList {


	
	public ReadMessageList(JWhisperP2PMidlet midlet,String title) {
		super(midlet,title);
		
	}
	protected void deleteMessage(CMessage m){
		try {
			AddressService.deleteMessage(m);
		} catch (Exception e) {
			_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_DELETE_MESSAGE + "\n"+ e.getMessage()));
		}
	}
	protected String getMessageText(CMessage m){
		String text=null;
		try {
			 text = new String(m.getData());
		} catch (Exception e) {
			_midlet.showInfo(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n"+ e.getMessage()));
		}
		return text;
	}
	protected void setMessages() {
		try {
			messages = AddressService.getInboxReadMessages();
		} catch (Exception e) {
			_midlet.showError(e.getMessage());
		}
	}
	protected String getNumber(CMessage message) {
		
		return message.getSender();
	}

}
/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.cgss.utils;


import jwhisper.cgss.CSettings;

import org.smslib.CIncomingMessage;
import org.smslib.COutgoingBinaryMessage;
import org.smslib.COutgoingMessage;
import org.smslib.CMessage.MessageEncoding;

import com.sun.midp.io.j2me.sms.*;


import com.sun.kvem.midp.io.j2se.wma.WMAMessage;
import jwhisper.utils.Helpers;

public class MessageUtils {
	
	// Messaggi in ingresso da GSM a BinaryMessage
	public static jwhisper.cgss.modules.wma.BinaryMessage CIncomingMessage2BynaryMessage(CIncomingMessage m){
	
		String smspdu = m.getPDUUserData();

		String portsPDU = smspdu.substring(0, 4*2);
		String binaryMsgPDU = smspdu.substring(4*2); 
		
		int destinationPort = Integer.parseInt(portsPDU.substring(0,4), 16);
		@SuppressWarnings("unused")
		int sourcePort = Integer.parseInt(portsPDU.substring(4,8), 16);
		
		
		if ( (m.getMessageEncoding() ==  MessageEncoding.Enc8Bit ) &&
		     (destinationPort == CSettings.getDefaultSMSPort()) ){
			
			byte [] binaryMsg = Helpers.hexStringToBytes(binaryMsgPDU);
			
			jwhisper.cgss.modules.wma.BinaryMessage bm = new jwhisper.cgss.modules.wma.sms.BinaryObject(m.getOriginator());
			bm.setPayloadData(binaryMsg);

			return bm;	
		}
		
		CGSSLogger.getInstance().write("SMS format sent from "+m.getOriginator()+" isn't correct");
		return null;
		
	}


	// Messaggi in uscita da BinaryMessage a formato per il GSM 
	public static COutgoingMessage BinaryMessage2COutgoingMessage (jwhisper.cgss.modules.wma.BinaryMessage msg){
		
		String address = Helpers.numberFromUrl(msg.getAddress());	
		
		byte []binaryData = msg.getPayloadData();
		
				
		COutgoingBinaryMessage msgBOut = new COutgoingBinaryMessage();
		
		msgBOut.setRecipient(address);
		
		msgBOut.setSourcePort(CSettings.getDefaultSMSPort());
		msgBOut.setDestinationPort(CSettings.getDefaultSMSPort());
		
		
		
		msgBOut.setPayload(binaryData);
		
		return msgBOut;
		
	}
    

	// Messaggi in ingresso su Emulatore a BinayMessage
	public static jwhisper.cgss.modules.wma.BinaryMessage WMAMessage2BinaryMessage(WMAMessage m){
		com.sun.kvem.midp.io.j2se.wma.Message msgWma = (com.sun.kvem.midp.io.j2se.wma.Message)m;

		
		if ( (msgWma.isText() ==  false ) &&
			 (msgWma.getToPort() == CSettings.getDefaultSMSPort()) ){
			
			jwhisper.cgss.modules.wma.BinaryMessage bm = new jwhisper.cgss.modules.wma.sms.BinaryObject(msgWma.getFromAddress());
			//bm.setAddress(msgWma.getToAddress());
			bm.setPayloadData(msgWma.getData());

			return bm;
		}
		
		
		CGSSLogger.getInstance().write("SMS format sent from "+msgWma.getFromAddress()+" isn't correct");
		return null;
	}

	// Messaggi in uscita da BinaryMessage a formato per l'Emulatore 
	public static WMAMessage BinaryMessage2WMAMessage (jwhisper.cgss.modules.wma.BinaryMessage msg){
		
		com.sun.kvem.midp.io.j2se.wma.Message msgWma = new com.sun.kvem.midp.io.j2se.wma.Message(msg.getPayloadData());
		msgWma.setFromAddress(CSettings.getNumberSMSC_WTK());
		msgWma.setToAddress(msg.getAddress());
			
		return msgWma;
		
	}
	
	public static javax.wireless.messaging.BinaryMessage JWhisperBinaryMessage2J2MEBinaryMessage(jwhisper.cgss.modules.wma.Message msg){
		
		BinaryObject bm = new BinaryObject(msg.getAddress());
		//bm.setAddress(msg.getAddress());
		bm.setPayloadData(((jwhisper.cgss.modules.wma.BinaryMessage)msg).getPayloadData());
		
		return bm;
	}
	
	
	
	

	
	
}

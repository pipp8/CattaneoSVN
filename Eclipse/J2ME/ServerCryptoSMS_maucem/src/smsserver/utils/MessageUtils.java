package smsserver.utils;

import org.cryptosms.helpers.Logger;
import org.smslib.CIncomingMessage;
import org.smslib.COutgoingBinaryMessage;
import org.smslib.COutgoingMessage;
import org.smslib.CMessage.MessageEncoding;


import smsserver.CSettings;


import com.sun.kvem.midp.io.j2se.wma.WMAMessage;

public class MessageUtils {
	
	// Messaggi in ingresso da GSM a BinaryMessage
	public static util.wma.BinaryMessage CIncomingMessage2BynaryMessage(CIncomingMessage m){
	
		String smspdu = m.getPDUUserData();

		String portsPDU = smspdu.substring(0, 4*2);
		String binaryMsgPDU = smspdu.substring(4*2); 
		
		int destinationPort = Integer.parseInt(portsPDU.substring(0,4), 16);
		@SuppressWarnings("unused")
		int sourcePort = Integer.parseInt(portsPDU.substring(4,8), 16);
		
		if ( (m.getMessageEncoding() ==  MessageEncoding.Enc8Bit ) &&
		     (destinationPort == CSettings.getDefaultSMSPort()) ){
			
			byte [] binaryMsg = Utils.hexStringToBytes(binaryMsgPDU);
			
			util.wma.BinaryMessage bm = new util.wma.sms.BinaryObject(m.getOriginator());
			bm.setPayloadData(binaryMsg);

			return bm;	
		}
		
		Logger.getInstance().write("SMS format sent from "+m.getOriginator()+" isn't correct");
		return null;
		
	}


	// Messaggi in uscita da BinaryMessage a formato per il GSM 
	public static COutgoingMessage BinaryMessage2COutgoingMessage (util.wma.BinaryMessage msg){
		
		String address = Utils.numberFromUrl(msg.getAddress());		
		byte []binaryData = msg.getPayloadData();
		
				
		COutgoingBinaryMessage msgBOut = new COutgoingBinaryMessage();
		
		msgBOut.setRecipient(address);
		
		msgBOut.setSourcePort(CSettings.getDefaultSMSPort());
		msgBOut.setDestinationPort(CSettings.getDefaultSMSPort());
		
		msgBOut.setPayload(binaryData);
		
		return msgBOut;
		
	}
    

	// Messaggi in ingresso su Emulatore a BinayMessage
	public static util.wma.BinaryMessage WMAMessage2BinaryMessage(WMAMessage m){
		com.sun.kvem.midp.io.j2se.wma.Message msgWma = (com.sun.kvem.midp.io.j2se.wma.Message)m;

		if ( (msgWma.isText() ==  false ) &&
			 (msgWma.getToPort() == CSettings.getDefaultSMSPort()) ){
			
			util.wma.BinaryMessage bm = new util.wma.sms.BinaryObject(msgWma.getFromAddress());
			//bm.setAddress(msgWma.getToAddress());
			bm.setPayloadData(msgWma.getData());

			return bm;
		}
		
		
		Logger.getInstance().write("SMS format sent from "+msgWma.getFromAddress()+" isn't correct");
		return null;
	}

	// Messaggi in uscita da BinaryMessage a formato per l'Emulatore 
	public static WMAMessage BinaryMessage2WMAMessage (util.wma.BinaryMessage msg){
		
		com.sun.kvem.midp.io.j2se.wma.Message msgWma = new com.sun.kvem.midp.io.j2se.wma.Message(msg.getPayloadData());
		msgWma.setFromAddress(CSettings.getNumberSMSC_WTK());
		msgWma.setToAddress(msg.getAddress());
			
		return msgWma;
		
	}
	

	
	

	
	
}

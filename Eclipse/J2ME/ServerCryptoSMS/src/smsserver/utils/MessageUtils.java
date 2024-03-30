package smsserver.utils;

import java.util.Date;

import org.smslib.CIncomingMessage;
import com.sun.kvem.midp.io.j2se.wma.WMAMessage;
import smsserver.MyMessage;

public class MessageUtils {

	// Converte i messaggi utilizzati dall'emulatore nella classe Message comune 
	public static MyMessage WMAMessage2Message(WMAMessage m){
		com.sun.kvem.midp.io.j2se.wma.Message inMsg = (com.sun.kvem.midp.io.j2se.wma.Message) m; 
		
		Date time = new Date(inMsg.getSendTime());		
		String str = inMsg.toString();
		String sender = inMsg.getFromAddress();
		String receiver = inMsg.getToAddress();
				
		if (inMsg.getFromPort() != -1)
			sender += ":" + inMsg.getFromPort();
		if (inMsg.getToPort() != -1)
			receiver += ":" + inMsg.getToPort();
		
		MyMessage msg = new MyMessage(sender, receiver, str, time);
		return msg;
	}
	
	
	// Converte i messaggi comuni nel formato utilizzato dall'emulatore
	public static WMAMessage Message2WMAMessage(MyMessage inMsg){
		com.sun.kvem.midp.io.j2se.wma.Message msg = null;
		if (inMsg.isText())
			msg = new com.sun.kvem.midp.io.j2se.wma.Message(inMsg.getText());
		else
			msg = new com.sun.kvem.midp.io.j2se.wma.Message(inMsg.getData());

		msg.setToAddress(inMsg.getReceiverFullQualified());
		msg.setFromAddress(inMsg.getSenderFullQualified());
		
		return msg;
	}

	// Converte il formato del messaggio utilizzato da smslib nel formato comune
	public static MyMessage CIncomingMessage2Message(CIncomingMessage m){
		
		Date time = m.getDate();
		String sender = m.getOriginator();
		String str = m.getText();

		String receiver = null;

		MyMessage msg = new MyMessage(sender, receiver, str, time);
		
		return msg;
	}
	
	
}

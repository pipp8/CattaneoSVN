/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.server.communication;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.smslib.CIncomingMessage;
import org.smslib.CService;
import org.smslib.SMSLibException;



import com.sun.kvem.midp.io.j2se.wma.WMAMessage;
import com.sun.kvem.midp.io.j2se.wma.client.WMAClient;

import crymes.book.CMessage;
import crymes.util.helpers.Logger;
import crymes.util.helpers.MessageUtils;
import crymes.util.helpers.NullEncoder;

/*
 * Implementa i listener per le callback chiamate dal wtk e da smslib. 
 * 
 * Implementa il gestore del listener ISmsMessageListener. Ogni volta
 * che arriva un messaggio, o dal wtk o da smslib, viene sollevato l'evento
 * nuovo sms e verranno invocate tutte le callback delle classi che implementano
 * il listener ISmsMessaggeListener e si sono registrate.
 */
@SuppressWarnings("unchecked")
public class CIncomingMessageListener 
					implements WMAClient.MessageListener,
							  	org.smslib.ISmsMessageListener {
	

	/* Implementazione dell'ISmsMessageListener */
	/* **************************************** */
	static boolean readed = false;
	
	private Vector<ISmsMessageListener> iSmsMessageListeners =  new Vector(3);

	public CIncomingMessageListener(ISmsMessageListener listener) {
		addSMSMessageListener(listener);
	}

	public void addSMSMessageListener(ISmsMessageListener listener) {
		iSmsMessageListeners.add(listener);
	}

	public void removeSMSMessageListener(ISmsMessageListener listener) {
		iSmsMessageListeners.remove(listener);
	}

	protected void fireSmsMessage(crymes.util.wma.BinaryMessage msg, boolean isEmulator) {
		Object[] listeners = iSmsMessageListeners.toArray();
				
		// loop through each listener and pass on the event if needed
		int numListeners = listeners.length;
		for (int i = 0; i < numListeners; i++) {
			if (listeners[i] instanceof ISmsMessageListener) {
				// pass the event to the listeners event dispatch method
				((ISmsMessageListener) listeners[i]).notifyIncomingMessage(msg,
						isEmulator);
			}
		}
	}

	
	/* WTK Client CallBack */
	public void notifyIncomingMessage(WMAClient arg0) {
		
		WMAMessage wmaMsg = null;
		try {
			wmaMsg = arg0.receive();
		} catch (IOException e) {
			Logger.getInstance().write("Error: impossible to receive the message");
		}
		

		crymes.util.wma.BinaryMessage bm = MessageUtils.WMAMessage2BinaryMessage(wmaMsg);
		
		// Se il formato del messaggio è corretto lo propago
		if (bm != null) {
			fireSmsMessage(bm, true);
		}

		
	}

	// GSM Client callback offered by smslib
	public boolean received(CService service, CIncomingMessage message)
			throws SMSLibException {

		crymes.util.wma.BinaryMessage msg = MessageUtils.CIncomingMessage2BynaryMessage(message);
		
		// Se il formato del messaggio è corretto lo propago
		if (msg != null) {
			fireSmsMessage(msg, false);
		}
		
		// Return false to leave the message in memory - otherwise return
		// true to delete it.
		return true;		
	}
	
	private synchronized byte[] hextoBytes(String hex) {

		byte[] bts = new byte[hex.length() / 2];
		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
		}
		return bts;
	} 
	
	
	@SuppressWarnings("unused")
	private void StampaMSG(CIncomingMessage message){
		if (readed == false ){
			if (message.getMemIndex()>=5){
				readed = true;
			
				String smspdu = message.getPDUUserData();

				String portsPDU = smspdu.substring(0, 4*2);
				String binaryMsgPDU = smspdu.substring(4*2); 
				
				
				/*
				 * Send Binary Message Other" form, specify a user 
				 * data header (UDH) of: 060504xxxxyyyy Where xxxx 
				 * is the destination port in hex, 
				 * and yyyy is the source port in hex.
				 */
				int sp = Integer.parseInt(portsPDU.substring(0,4), 16);
				int dp = Integer.parseInt(portsPDU.substring(4,8), 16);
				

				
				try {
					byte [] binaryMsg = hextoBytes(binaryMsgPDU);
					
					byte [] data = NullEncoder.decode(binaryMsg);
				
					byte	version;
					byte	type;
					long	timestamp;
				
								
					ByteArrayInputStream Bstrm = new ByteArrayInputStream(data);
					DataInputStream Dstrm = new DataInputStream(Bstrm);
				
					version = Dstrm.readByte();
					if (version != CMessage.supportedVersion){ 						
						Logger.getInstance().write("Version diffrent!!! Maybe sms corrupt???");
						return;
					}
						
					type=Dstrm.readByte();
					timestamp=Dstrm.readLong();
					
					Date dd = new Date(timestamp);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
					//System.out.println(sdf.format(dd));
					

					int len=Dstrm.available();
					byte[] payload=new byte[len];

					Dstrm.read(payload);
					//Utils.dump("Payload", payload);											
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
				
		}
		
		
		
	}
	
	
	
}

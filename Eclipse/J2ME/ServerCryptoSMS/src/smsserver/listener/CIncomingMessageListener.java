package smsserver.listener;

import java.io.IOException;
import java.util.Vector;

import org.smslib.CIncomingMessage;
import org.smslib.CService;
import org.smslib.SMSLibException;

import smsserver.MyMessage;
import smsserver.utils.MessageUtils;

import com.sun.kvem.midp.io.j2se.wma.WMAMessage;
import com.sun.kvem.midp.io.j2se.wma.client.WMAClient;

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

	protected void fireSmsMessage(MyMessage msg, boolean isEmulator) {
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
			System.out.println("Error: impossible to receive the message");
			//e.printStackTrace();
		}
		MyMessage msg = MessageUtils.WMAMessage2Message(wmaMsg);

		// Notify the event
		fireSmsMessage(msg, true);
	}

	// GSM Client callback offered by smslib
	public boolean received(CService service, CIncomingMessage message)
			throws SMSLibException {

		MyMessage msg = MessageUtils.CIncomingMessage2Message(message);

		fireSmsMessage(msg, false);

		// Return false to leave the message in memory - otherwise return
		// true to delete it.
		return false;		
	}
}

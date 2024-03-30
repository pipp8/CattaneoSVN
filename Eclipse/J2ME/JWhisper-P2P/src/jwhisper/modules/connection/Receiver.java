/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.modules.connection;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.rms.RecordStore;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;

import jwhisper.modules.message.CMessage;
import jwhisper.utils.Logger;



/*
 * Ricezione degli SMS e salvataggio nel RecordStore
 * 
 * Questa classe parte automaticamente e non ha bisogna di interazione 
 * con l'utente se l'SMS arriva (PushRegistry)
 * 
 */

public class Receiver implements MessageListener {
	public static final String PORT = "16002";

	public static final String SERVER_URL = "sms://:" + PORT;

	public static MessageConnection connection;

	private static Logger log = null;

	public static final String INCOMMING_STORE_NAME = "incomming";

	private static RecordStore incommingStore = null;

	private static Receiver receiver = null;

	private Receiver() {
		log = Logger.getInstance();
		try {
			connection = (MessageConnection) Connector.open(SERVER_URL);
			log.write("MessageConnection Register " + SERVER_URL);
			connection.setMessageListener(this);
		} catch (IOException e) {
			log.write("Error Starting Receiver: " + e.toString());
			connection = null;
		}
	}

	public static Receiver getInstance() {
		if (receiver == null) {
			receiver = new Receiver();
		}
		return receiver;
	}

	/*
	 * Hack - La classe è attiva ma non agganciata con il push registry
	 */
	public static Receiver dirtyInit() {
		if (receiver != null) {
			if (receiver.getConnection() != null) {
				// we are there and have a connection
				try {
					MessageConnection connection = receiver.getConnection();
					connection.setMessageListener(null);
					connection.close();
					connection = (MessageConnection) Connector.open(SERVER_URL);
					receiver.setConnection(connection);
					connection.setMessageListener(receiver);

					return receiver;
				} catch (IOException e) {
					log.write("Error Cannon Register the Receiver with PushRegistry" + e.toString());
					e.printStackTrace();
				}
			} else
				return Receiver.getInstance();
		}
		return null;
	}

	
	//use this only if you check for a null value afterwards
	public static Receiver getReceiver() {
		return receiver;
	}

	public MessageConnection getConnection() {
		return connection;
	}

	public void setConnection(MessageConnection conn) {
		Receiver.connection = conn;
	}

	public void notifyIncomingMessage(MessageConnection mc) {
		if (mc == connection) {
			try {
				Message message = mc.receive();
				
				// Salve in messaggio binario decodificato in CMessage nello store "incommingStore"
				if (message != null) {
					saveMessage(message);
				}
			} catch (Exception e) { // catch everything
				log.write("Error in Receiver while receiving a message: " + e.toString());
			}
		} else {
			log.write("Error in Receiver. Called with wrong connection");
		}
	}

	public synchronized void saveMessage(Message msg) {
		if (msg != null) {
			if (msg instanceof BinaryMessage) {
				try {
					if (incommingStore == null) {
						incommingStore = RecordStore.openRecordStore(INCOMMING_STORE_NAME, true);
					}
		
					//La createFromTransport prende il messaggio binario lo decodifica e mi
					//restituisce un bel CMessage che poi aggiungo allo store
					CMessage cmsg = CMessage.createFromTransport((BinaryMessage)msg);
	
					if (cmsg != null) {
						byte[] data = cmsg.getBytes();
						incommingStore.addRecord(data, 0, data.length);
					}
				} catch (Exception e) {
					log.write("Error in Receiver - incoming message of wrong type: "+msg.getClass().getName());
				}
			} else {
				log.write("Error in Receiver - incoming message of wrong type: "+msg.getClass().getName());
			}
		}
	}

}

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
import javax.wireless.messaging.MessageConnection;

import jwhisper.Constant;
import jwhisper.utils.Logger;



/*
 * Ricezione degli SMS e salvataggio nel RecordStore
 * 
 * Questa classe parte automaticamente e non ha bisogna di interazione 
 * con l'utente se l'SMS arriva (PushRegistry)
 * 
 */

public class ReceiverP2P extends Receiver {

	public static final String SERVER_URL_P2P = "sms://:" + Constant.SMS_PORT_P2P;


	private ReceiverP2P() {
		super(SERVER_URL_P2P);
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
			receiver = new ReceiverP2P();
		}
		return receiver;
	}

	/*
	 * Hack - La classe e' attiva ma non agganciata con il push registry
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
				return ReceiverP2P.getInstance();
		}
		return null;
	}

	
	

}

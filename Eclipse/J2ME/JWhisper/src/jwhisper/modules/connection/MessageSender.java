/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */



package jwhisper.modules.connection;

import java.util.Vector;

import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;

import jwhisper.Constant;
import jwhisper.JWhisperMidlet;
import jwhisper.utils.Logger;



/*
 * MessageSender effettua l'invio.
 * 
 * Il messaggio (byte[]) e' accodato in una Queue
 * Il thread lo invia in maniera asincrona
 *  
 */
public class MessageSender extends Thread {

	private static Thread sender = null;

	private static MessageSender instance = null;

	private static Vector queue = null;

	private static final String PROTOCOL = "sms://";

	private static JWhisperMidlet midlet;
	
	private static Logger log=null;

	private MessageSender(JWhisperMidlet midlet) {
		MessageSender.midlet = midlet;
		MessageSender.queue = new Vector();
		MessageSender.sender = new Thread(this);
		MessageSender.log=Logger.getInstance();
	}

	public static MessageSender getInstance() {
		return instance;
	}

	public static void init(JWhisperMidlet midlet) {
		if (getInstance() == null) {
			instance = new MessageSender(midlet);
		}
	}

	public void send(String target, byte[] message) {
		if (message != null) {
			accessQueue(true, target, message);
	
			if (MessageSender.sender != null) {
				if (!MessageSender.sender.isAlive()) {
					log.write("MessageSender starting new thread");
					MessageSender.sender = new Thread(this);
					MessageSender.sender.start();
				}
			} else {
				MessageSender.sender = new Thread(this);
				if (MessageSender.sender != null)
					MessageSender.sender.start();
				else {
					midlet.showError("MessageSender cannot start");
					log.write("Error MessageSender cannot start");
				}
			}
		}
	}



	public void run() {
		log.write("MessageSender thread starting");
		QueuedMessage msg = accessQueue(false, null, null);
		while (msg != null) {
			try {
				String address = numberToUrl(sanitizeNumber(msg.getUrl()));
				MessageConnection connection;
				Receiver receiver= null;
				if (midlet.getApplicationType() == Constant.APPLICATION_TYPE_A2P){
					receiver=ReceiverA2P.getInstance();
				}
				else if (midlet.getApplicationType() == Constant.APPLICATION_TYPE_P2P){
					receiver=ReceiverP2P.getInstance();
				}
				
				if (receiver != null) {
					connection = receiver.getConnection();
					if (connection != null) {
						BinaryMessage binary = (BinaryMessage) connection.newMessage(MessageConnection.BINARY_MESSAGE);
	
						binary.setAddress(address);
						binary.setPayloadData(msg.getPayload());
	
						log.write("New outcoming message to: " + address+" Length: "+msg.getPayload().length);
						midlet.setTicketText("Messaggio inviato a "+address);
	
						connection.send(binary);
					}
					else {
						midlet.showError("Message Sender Thread - lost connection");
					}
				} else {
					midlet.showError("Message Sender Thread - no connection available");
				}
			} catch (Exception e) {
				midlet.showError("Error Message Sender Thread "+e.toString());
			} 

			msg = dequeue();
		}
	}


	public static String numberToUrl(String number) {
		String port = "";
		if (midlet.getApplicationType() == Constant.APPLICATION_TYPE_A2P){
			port = ":"+Constant.SMS_PORT_A2P;
		}
		else if (midlet.getApplicationType() == Constant.APPLICATION_TYPE_P2P){
			port = ":"+Constant.SMS_PORT_P2P;
		}
		
		return PROTOCOL + number + port;
	}
	
	/*
	 * Il numero e' dialable?
	 */
	public static String sanitizeNumber(String number) {
		if (number != null) {
			if (number.charAt(0)=='0') return number;
			else if (number.charAt(0) == '+') return number;
			else return "+"+number;
		}
		else return null;
	}

	// queue handling

	/*
	 * access the queue
	 * mode == true -> write access
	 * mode == false -> read access  
	 */
	
	public synchronized QueuedMessage accessQueue(boolean mode, String url, byte[] payload) {
		if (mode) { // write
			enqueue(url, payload);
			return null;
		} else {
			return dequeue();
		}
	}
	
	private void enqueue(String url, byte[] payload) {
		if (MessageSender.queue != null) {
			MessageSender.queue.addElement(new QueuedMessage(url, payload));
		}
	}

	private QueuedMessage dequeue() {
		if (MessageSender.queue != null) {
			if (!MessageSender.queue.isEmpty()) {
				QueuedMessage msg = (QueuedMessage) MessageSender.queue
						.firstElement();
				MessageSender.queue.removeElementAt(0);
				return msg;
			}
		}
		return null;
	}

	public class QueuedMessage {
		String url;

		byte[] payload;

		public QueuedMessage(String url, byte[] payload) {
			this.url = url;
			this.payload = payload;
		}

		public String getUrl() {
			return url;
		}

		public byte[] getPayload() {
			return payload;
		}
	}
}
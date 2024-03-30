/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.server.communication;

import crymes.book.AddressService;
import crymes.book.CMessage;
import crymes.util.helpers.Logger;
import crymes.util.recordstore.RecordEnumeration;
import crymes.util.recordstore.RecordStore;
import crymes.util.recordstore.RecordStoreException;
import crymes.util.recordstore.RecordStoreFullException;


/**
 * The MessageReader is the counterpart of the Receiver. The Receiver receives the sms
 * ans stores it in a RecordStore, the MessageReader fetches it from there and does the
 * rest of processing.
 * 
 * MessageReader and Receiver exist, so that one of them (Receiver) can be headless 
 * (producing no graphical output) and without password/crypto support, while the other
 * runs in an environment with passwords, crypted storage and a display.
 * 
 * @author sd, malte
 */
public class MessageReader extends Thread {
	//private CryptoSMSMidlet midlet;

	private static MessageReader _mr = null;

	private Logger log = null;

	private RecordStore incommingStore = null;

	private MessageReader worker;

	private MessageReader() {
		log = Logger.getInstance();
	}

	public synchronized static MessageReader getInstance() {
		if (_mr == null) {
			_mr = new MessageReader();
		}
		return _mr;
	}

	// public static void init(CryptoSMSMidlet g) {
		public static void init() {

		MessageReader reader = MessageReader.getInstance();
		try {
		//	reader.midlet = g;
			reader.worker = new MessageReader();
			//reader.worker.setMidlet(g);
			reader.worker.start();
		} catch (Exception e) {
			reader.log.write("MessageReader init: " + e.toString());
		}
	}

//	public void setMidlet(CryptoSMSMidlet midlet) {
//		this.midlet=midlet;
//	}
	
	public synchronized CMessage loadMessage() {
		try {
			if (incommingStore == null) {
				incommingStore = RecordStore.openRecordStore(Receiver.INCOMMING_STORE_NAME, true);
			}

			RecordEnumeration renum = incommingStore.enumerateRecords(null, null, false);
			if (renum.hasNextElement()) {
				int nextId = renum.nextRecordId();
				renum.destroy();

				// System.out.println("nextId" + nextId);

				byte[] data = incommingStore.getRecord(nextId);

				incommingStore.deleteRecord(nextId);

				return new CMessage(data, -1);
			}

		} catch (Exception e) {
			log.write("in reader thread:" + e.toString());
			e.printStackTrace();
		}
		return null;
	}

	public void run() {
		CMessage 	msg;
		String 		senders="";
		//boolean 	newMessage;

		try {
			log.write("reader thread starting");
			while (true) { // bail out thru interrupt
				//newMessage = false;
				msg = loadMessage();
				while (msg != null) {
					log.write("message found in the incomingStore: "+msg.getSender());
					try {
						AddressService.saveMessage(msg);
					//	newMessage=true;
						senders+=" "+msg.getSender();
					} catch (RecordStoreFullException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RecordStoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
// Test invio dello stesso messaggio per provare la comunicazione					
//					sleep(1000);
//					//String a = CConstants.NUMBER_SIM_SERVER;				
//					String a = "+393928191659";
//					byte[] data = msg.getData();
//					CMessage n = new CMessage( CSettings.getEmulatorPhoneNumber(),a, MessageType.KEY, data,new Date());
//					MessageSender ms = MessageSender.getInstance();
//					ms.send(a, n.prepareForTransport());
					
					msg = loadMessage();
				}
				//if (newMessage &&(midlet != null)) midlet.setTicketText(midlet.getResourceManager().getString(ResourceTokens.STRING_NEW_MESSAGES_RECEIVED+senders));
//				if (newMessage) {
//					System.out.println("NEW_MESSAGES_RECEIVED"+senders);
//				}
				Thread.sleep(1000);
				
			}
		} catch (Exception e) {
			log.write("finish reader thread");
			e.printStackTrace();
		}
	}

		// public static void stop() {
		public void stopThread() {
		MessageReader reader = MessageReader.getInstance();

		if ((reader.worker != null) && (reader.worker.isAlive())) {
			reader.worker.interrupt(); // stop the reader thread
			reader.worker = null;
		}
	}

}
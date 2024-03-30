/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.modules.connection;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

import jwhisper.gui.JWhisperMidlet;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.message.CMessage;
import jwhisper.utils.Logger;
import jwhisper.utils.ResourceTokens;



/*
 * Receiver riceve gli sms e li memorizza nel RecordStore
 * MessageReader preleva gli sms dal record store  eli processa
 * 
 * Il Receiver viene eseguito anche senza pin inserito. Questo assicura la ricezione di 
 * SMS anche ad applicazione non avviata.
 * 
 */

public class MessageReader extends Thread {
	private JWhisperMidlet midlet;

	private static MessageReader _mr = null;

	private Logger log = null;

	private RecordStore incommingStore = null;

	private MessageReader worker;

	
	/* Singleton Pattern */
	private MessageReader() {
		log = Logger.getInstance();
	}

	public synchronized static MessageReader getInstance() {
		if (_mr == null) {
			_mr = new MessageReader();
		}
		return _mr;
	}

	
	public static void init(JWhisperMidlet g) {

		MessageReader reader = MessageReader.getInstance();
		try {
			reader.midlet = g;
			reader.worker = new MessageReader();
			reader.worker.setMidlet(g);
			reader.worker.start();
		} catch (Exception e) {
			reader.log.write("Error MessageReader initialize: " + e.toString());
		}
	}

	public void setMidlet(JWhisperMidlet midlet) {
		this.midlet=midlet;
	}
	
	public synchronized CMessage loadMessage() {
		try {
			if (incommingStore == null) {
				incommingStore = RecordStore.openRecordStore(Receiver.INCOMMING_STORE_NAME, true);
			}

			RecordEnumeration renum = incommingStore.enumerateRecords(null, null, false);
			if (renum.hasNextElement()) {
				int nextId = renum.nextRecordId();
				renum.destroy();

				byte[] data = incommingStore.getRecord(nextId);
				incommingStore.deleteRecord(nextId);
				return new CMessage(data, -1);
			}

		} catch (Exception e) {
			log.write("Error MessageReader thread:" + e.toString());
			e.printStackTrace();
		}
		return null;
	}

	public void run() {
		CMessage 	msg;
		String 		senders="";
		boolean 	newMessage;

		try {
			log.write("MessageReader thread starting");
			while (true) { 
				// bail out thru interrupt
				newMessage = false;
				msg = loadMessage();
				while (msg != null) {
					log.write("MessageReader - new message found from: " + msg.getSender() +"Message type: "+ msg.getType());
					try {
						AddressService.saveMessage(msg);
						newMessage=true;
						senders+=" "+msg.getSender();
					} catch (RecordStoreFullException e) {
						log.write("Error MessageReader thread:" + e.toString());
						e.printStackTrace();
					} catch (RecordStoreException e) {
						log.write("Error MessageReader thread:" + e.toString());
						e.printStackTrace();
					} catch (Exception e) {
						log.write("Error MessageReader thread:" + e.toString());
						e.printStackTrace();
					}
					msg = loadMessage();
				}
				if (newMessage &&(midlet != null)) 
					midlet.setTicketText(midlet.getResourceManager().getString(ResourceTokens.STRING_NEW_MESSAGES_RECEIVED+senders));
				
				sleep(1000);
			}
		} catch (Exception e) {
			log.write("Error MessageReader thread finish: " + e.toString());
		}
	}

	public static void stop() {
		MessageReader reader = MessageReader.getInstance();

		if ((reader.worker != null) && (reader.worker.isAlive())) {
			// stop the reader thread
			reader.worker.interrupt(); 
			reader.worker = null;
		}
	}

}
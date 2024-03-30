/*
 * Copyright (C) 2007 Crypto Messaging with Elliptic Curves Cryptography (CRYMES)
 *
 * This file is part of Crypto Messaging with Elliptic Curves Cryptography (CRYMES).
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Crypto Messaging with Elliptic Curves Cryptography (CRYMES); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jwhisper.connection;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

import jwhisper.book.AddressService;
import jwhisper.book.CMessage;
import jwhisper.gui.JWhisperMidlet;
import jwhisper.helpers.Logger;
import jwhisper.helpers.ResourceTokens;



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
	private JWhisperMidlet midlet;

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

	
	public static void init(JWhisperMidlet g) {

		MessageReader reader = MessageReader.getInstance();
		try {
			reader.midlet = g;
			reader.worker = new MessageReader();
			reader.worker.setMidlet(g);
			reader.worker.start();
		} catch (Exception e) {
			reader.log.write("MessageReader init: " + e.toString());
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
		boolean 	newMessage;

		try {
			log.write("reader thread starting");
			while (true) { // bail out thru interrupt
				newMessage = false;
				msg = loadMessage();
				while (msg != null) {
					log.write("message found: " + msg.getSender() +"Message type: "+ msg.getType());
					try {
						AddressService.saveMessage(msg);
						newMessage=true;
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
					msg = loadMessage();
				}
				if (newMessage &&(midlet != null)) midlet.setTicketText(midlet.getResourceManager().getString(ResourceTokens.STRING_NEW_MESSAGES_RECEIVED+senders));
				sleep(1000);
			}
		} catch (Exception e) {
			log.write("finish reader thread");
		}
	}

	public static void stop() {
		MessageReader reader = MessageReader.getInstance();

		if ((reader.worker != null) && (reader.worker.isAlive())) {
			reader.worker.interrupt(); // stop the reader thread
			reader.worker = null;
		}
	}

}
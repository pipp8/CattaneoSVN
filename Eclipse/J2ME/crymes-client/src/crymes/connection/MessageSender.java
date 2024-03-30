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

package crymes.connection;

import java.util.Vector;

import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;


import crymes.gui.CrymesMidlet;
import crymes.helpers.Logger;

/**
 * The MessageSender does the actual sending. The message (byte[]) together with the target
 * address is piped to a thread, so that the actual sending is asynchronous. The sending thread
 * runs as long as there are messages in the pipe. The MessageSender takes care that a sending
 * thread is running if there are new messages to send (it starts one if not).
 *  
 * Note: This is the transport layer - it assumes the message to be ready for sending, no 
 * encryption or encoding takes place here.
 * 
 * @author malte
 */
public class MessageSender extends Thread {

	private static Thread sender = null;

	private static MessageSender instance = null;

	private static Vector queue = null;

	private static final String PROTOCOL = "sms://";

	private static CrymesMidlet midlet;
	
	private static Logger log=null;

	private MessageSender(CrymesMidlet midlet) {
		MessageSender.midlet = midlet;
		MessageSender.queue = new Vector();
		MessageSender.sender = new Thread(this);
		MessageSender.log=Logger.getInstance();
	}

	public static MessageSender getInstance() {
		return instance;
	}

	public static void init(CrymesMidlet midlet) {
		if (getInstance() == null) {
			instance = new MessageSender(midlet);
		}
	}

	public void send(String target, byte[] message) {
		if (message != null) {
			accessQueue(true, target, message);
	
			if (MessageSender.sender != null) {
				if (!MessageSender.sender.isAlive()) {
					log.write("start new thread");
					MessageSender.sender = new Thread(this);
					MessageSender.sender.start();
				}
			} else {
				MessageSender.sender = new Thread(this);
				if (MessageSender.sender != null)
					MessageSender.sender.start();
				else {
					midlet.showError("Sender cannot start");
					log.write("Sender cannot start");
				}
			}
		}
	}

	// --- sending thread

	public void run() {
		log.write("in thread");
		QueuedMessage msg = accessQueue(false, null, null);
		while (msg != null) {
			try {
				String address = numberToUrl(sanitizeNumber(msg.getUrl()));
				MessageConnection connection;
				Receiver receiver=Receiver.getInstance();
				if (receiver != null) {
					connection = receiver.getConnection();
					if (connection != null) {
						BinaryMessage binary = (BinaryMessage) connection.newMessage(MessageConnection.BINARY_MESSAGE);
	
						binary.setAddress(address);
						binary.setPayloadData(msg.getPayload());
	
						log.write("message: " + address+" Length: "+msg.getPayload().length);
						midlet.setTicketText("message sent to "+address);
	
						connection.send(binary);
					}
					else {
						midlet.showError("lost connection");
					}
				} else {
					midlet.showError("no connection available");
				}
			} catch (Exception e) {
				midlet.showError(e.toString());
			} 

			msg = dequeue();
		}
	}

	// --- helpers

	public static String numberToUrl(String number) {
		return PROTOCOL + number + ":" + Receiver.PORT;
	}
	
	/**
	 * make sure it's a dialable number: check if it starts with '0'
	 * or prepend a '+' to it.
	 * @param number
	 * @return
	 */
	public static String sanitizeNumber(String number) {
		if (number != null) {
			if (number.charAt(0)=='0') return number;
			else if (number.charAt(0) == '+') return number;
			else return "+"+number;
		}
		else return null;
	}

	// --- queue handling

	/**
	 * access the queue
	 * if mode == true it's write (return null)
	 * else read access (return the QueuedMessage, or null)
	 * 
	 * this code implements a 'semaphore' to protect the queue thru
	 * a simple 'synchronized'
	 */
	public synchronized QueuedMessage accessQueue(boolean mode,String url, byte[] payload) {
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
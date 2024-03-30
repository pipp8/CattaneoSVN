/*
 * Copyright (C) 2007 cryptosms.org
 *
 * This file is part of cryptosms.org.
 *
 * cryptosms.org is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * cryptosms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cryptosms.org; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.cryptosms.connection;





import smsserver.CSettings;
import util.recordstore.RecordStore;
import util.wma.BinaryMessage;
import util.wma.Message;
import util.wma.MessageConnection;


import org.cryptosms.book.CMessage;
import org.cryptosms.helpers.Logger;

/**
 * Receiver class, receiving sms and storing them into a RecordStore.
 * 
 * this is somewhat redundant to the MessageReader class, the difference is that
 * this class works without a provided password - so there is also no encryption
 * of the incomming sms.
 * 
 * This is for starting the Receiver automatically without user interaction if a
 * sms arrives (PushRegistry).
 * 
 * @author malte
 * 
 */
public class Receiver  {

	public static final String SERVER_URL = "sms://:" + CSettings.getDefaultSMSPort();

	public static MessageConnection connection;

	private static Logger log = null;

	public static final String INCOMMING_STORE_NAME = "incomming";

	private static RecordStore incommingStore = null;

	private static Receiver receiver = null;

	private Receiver() {
		log = Logger.getInstance();
//		try {
//			connection = (MessageConnection) Connector.open(SERVER_URL);
//			log.write("register " + SERVER_URL);
//			connection.setMessageListener(this);
//		} catch (IOException e) {
//			log.write("Exception while starting receiver: " + e.toString());
//			connection = null;
//		}
	}

	public static Receiver getInstance() {
		if (receiver == null) {
			receiver = new Receiver();
		}
		return receiver;
	}

	/**
	 * this is a dirty hack - we are existing, but are not enlisted with the
	 * push registry (at least it says so). We try to do some cleanup and then
	 * setup the Connection. 
	 * 
	 * @return Receiver
	 */
	

	/**
	 * use this only if you check for a null value afterwards
	 * 
	 * @return
	 */
	

	

	public void notifyIncomingMessage(util.wma.Message mc) {
		if (mc != null) {
			saveMessage(mc);
		} else {
			log.write("message = null");
		}
	}

	public synchronized void saveMessage(Message msg) {
		if (msg != null) {
			
			if (msg instanceof BinaryMessage) {
				try {
					if (incommingStore == null) {
						incommingStore = RecordStore.openRecordStore(INCOMMING_STORE_NAME, true);
					}
					
					CMessage cmsg = CMessage.createFromTransport((BinaryMessage)msg);
	
					
					
					if (cmsg != null) {
						byte[] data = cmsg.getBytes();
						incommingStore.addRecord(data, 0, data.length);
						//System.out.println("TYPE: "+cmsg.getType());
					}
				} catch (Exception e) {
					// TODO: what now?
					e.printStackTrace();
				}
			} else {
				log.write("incoming message of wrong type: "+msg.getClass().getName());
			}
		}
	}

}

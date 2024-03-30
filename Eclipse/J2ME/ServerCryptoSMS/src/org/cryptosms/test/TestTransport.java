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

package org.cryptosms.test;

import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;

import org.cryptosms.book.CMessage;
import org.cryptosms.helpers.Helpers;
import org.cryptosms.helpers.ResourceManager;
import org.cryptosms.helpers.ResourceTokens;

public class TestTransport extends TestFrame {

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		form = new Form("Test");
		form.append("start: " + Helpers.dateToStringFull(new Date()));
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);
		if (display != null)
			display.setCurrent(form);

		try {
			byte[]			message={ 'a', 'b', 'c', 0, 'a', 'b', 'c' };
			CMessage	cmsg1=new CMessage("sender","receiver",(byte)0x01,message,new Date());
			
			byte[]			transport=cmsg1.prepareForTransport();
			
			displayString("transport: "+transport.length);
			
			MessageConnection connection=(MessageConnection)Connector.open("sms://:9000");
			BinaryMessage binary = (BinaryMessage) connection.newMessage(MessageConnection.BINARY_MESSAGE);
			
			displayString("got message");
			
			binary.setPayloadData(transport);
			binary.setAddress("receiver");
			
			displayString("decode");
			
			CMessage	cmsg2=CMessage.createFromTransport(binary);
			
			byte[]			payload=cmsg2.getData();
			
			displayString("message: "+message.length);
			displayString("after: "+payload.length);
			
		} catch (Exception exp) {
			System.out.println("Exception: " + exp.toString());
		}
		displayString("finish: " + Helpers.dateToStringFull(new Date()));

	}

}

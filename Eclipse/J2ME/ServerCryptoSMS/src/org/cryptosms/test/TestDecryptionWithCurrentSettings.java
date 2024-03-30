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

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDletStateChangeException;

import org.cryptosms.book.AddressService;
import org.cryptosms.book.CMessage;
import org.cryptosms.book.MessageType;
import org.cryptosms.book.Settings;
import org.cryptosms.helpers.CryptedRecordStore;
import org.cryptosms.helpers.CryptoHelper;
import org.cryptosms.helpers.Helpers;
import org.cryptosms.helpers.KeyPairHelper;

public class TestDecryptionWithCurrentSettings extends TestFrame {

	protected void startApp() throws MIDletStateChangeException {

		display = Display.getDisplay(this);
		form = new Form("Test");
		form.append("start: " + Helpers.dateToStringFull(new Date()));
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);
		if (display != null)
			display.setCurrent(form);

		try {
//			 open recordstores

			CryptedRecordStore.init("zunder");
			AddressService.init(null);


			
			// get settings to recordstore
			Settings settings0=AddressService.getSetting();
			// get settings from record store- in this case the saved settings object is returned
		
			
			String message="hallojulia";
			// encrypt messsage
			byte[]encrypted=CryptoHelper.encrypt(message.getBytes(), settings0.getPublicKey());
			CMessage mess = new CMessage(settings0.getNumber(), "5550000", MessageType.ENCRYPTED_TEXT_INBOX,
					encrypted,new Date());
			
			System.out.println( "encrypted Message: "+ new String(encrypted));
			byte[] trans=mess.prepareForTransport();
			CMessage messIn=CMessage.createFromTransport(trans);
				new CMessage("5550000", "5550000", MessageType.SYM_ENCRYPTED_TEXT_OUTBOX,
					trans,new Date());
			System.out.println( "Public key byte [] from settings object: "+ new String(settings0.getPublicKey()));
			// save message in record store
			// get message from store
			// decrypt
			byte[] decrypted=CryptoHelper.decrypt(messIn.getData(), settings0.getPair().getKeyPair());
			System.out.println(new String(decrypted));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//à(=[ì151]4ˆrz{™µË”…¢%IÜêœk™þiœ¤km;ÈQ"AU©’

	}

}

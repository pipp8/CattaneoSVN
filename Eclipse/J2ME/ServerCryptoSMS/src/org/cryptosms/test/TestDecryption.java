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
import org.cryptosms.book.Settings;
import org.cryptosms.helpers.CryptedRecordStore;
import org.cryptosms.helpers.CryptoHelper;
import org.cryptosms.helpers.Helpers;
import org.cryptosms.helpers.KeyPairHelper;

public class TestDecryption extends TestFrame {

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
			//CryptedRecordStore.deleteGroup();
			CryptedRecordStore.init("Sommer2006");
			AddressService.init(null);
			// generate keys and settings object
			KeyPairHelper kph;
			kph = CryptoHelper.generateKeyPairHelper(null);
			System.out.println("created private key byte []: "+ new String(kph.getPrivateKeyBA()));
			System.out.println("created public key byte []: "+ new String(kph.getPublicKeyBA()));
			
			
			
			
			// save settings to recordstore
			Settings settings0=AddressService.saveSettings("Testdummy", "555 0000", kph);
			// get settings from record store- in this case the saved settings object is returned
			//Settings settings1 =AddressService.getSetting();
			System.out.println("created keypair equals that from settings object?: "+kph.getKeyPair().equals(settings0.getPair().getKeyPair()));
			String message="wird schon werden welch beschwerden wir auch immer haben";
			// encrypt messsage
			byte[]encrypted=CryptoHelper.encrypt(message.getBytes(), settings0.getPublicKey());
			System.out.println( "encrypted Message: "+ new String(encrypted));
			
			System.out.println( "Public key byte [] from settings object: "+ new String(settings0.getPublicKey()));
			// save message in record store
			// get message from store
			// decrypt
			byte[] decrypted=CryptoHelper.decrypt(encrypted, settings0.getPair().getKeyPair());
			System.out.println(new String(decrypted));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//à(=[ì151]4ˆrz{™µË”…¢%IÜêœk™þiœ¤km;ÈQ"AU©’

	}

}

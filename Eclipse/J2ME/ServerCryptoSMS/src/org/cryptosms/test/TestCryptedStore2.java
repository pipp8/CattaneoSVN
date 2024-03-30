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
import java.util.Enumeration;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordEnumeration;

import org.cryptosms.helpers.CacheEnumeration;
import org.cryptosms.helpers.CryptedRecordEnumeration;
import org.cryptosms.helpers.CryptedRecordStore;
import org.cryptosms.helpers.Helpers;
import org.cryptosms.helpers.Record;

public class TestCryptedStore2 extends TestFrame {
	protected void startApp() throws MIDletStateChangeException {
		
		display = Display.getDisplay(this);
		form = new Form("Testaaaa");
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);
		displayString("start: " + Helpers.dateToStringFull(new Date()));

		CryptedRecordStore store1;
		try {
			CryptedRecordStore.deleteCryptedRecordStore("magic_cookie_store2");
			CryptedRecordStore.deleteCryptedRecordStore("hello");
		}catch( Exception e){
			displayString("Noting to delete");
		}
		try {
			//CryptedRecordStore2.deleteCryptedRecordStore("hello");
			CryptedRecordStore.init( "zunder");
			store1 = CryptedRecordStore.open("hello", true);
			int n=0;
			for(int i=0;i<20;i++){
				if(i==14){
					n=store1.getNextRecordId();
				}
				store1.addRecord(("This is noooot a teeest"+i).getBytes());
			}
			
			store1.setRecord(n, "auch schön".getBytes());
			store1.setRecord(17, "noch schöner".getBytes());
			store1.deleteRecord(16);
			store1.closeCryptedRecordStore();
			store1=null;
			CryptedRecordStore.init( "zunder");
			store1 = CryptedRecordStore.open("hello", true);
			displayString("Records in storeeeeeeeeeeeeeeeee: " + store1.getNumRecords() + " - "
					+ Helpers.dateToStringFull(new Date()));
			
			//getRecords
			displayString("Records from store");
			byte[] data;
			CryptedRecordEnumeration e=store1.enumerateCryptedRecords(null,null,false);
			while(e.hasNextElement()){
				
				data = e.nextRecord(); //store1.getRecord(e.nextRecordId());
				displayString("Contains: " + new String(data) + " - " + Helpers.dateToStringFull(new Date()));
			}
		

			
			displayString("Records from cache");
			
			CacheEnumeration en=store1.enumerateCache(null, null);
			while(en.hasNextElement()){
				
				data = en.nextRecord(); //store1.getRecord(e.nextRecordId());
				displayString("Contains: " + new String(data) + " - " + Helpers.dateToStringFull(new Date()));
			}
		} catch (Exception e) {
			displayString("Exception: " + e.toString() + " - " + Helpers.dateToStringFull(new Date()));
			}
		

			
	}

}

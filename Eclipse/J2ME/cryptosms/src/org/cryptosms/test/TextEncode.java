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

import org.cryptosms.helpers.Helpers;
import org.cryptosms.helpers.NullEncoder;
import org.cryptosms.helpers.SevenBitEncoder;

public class TextEncode extends TestFrame {
	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		form = new Form("Test");
		form.append("start: " + Helpers.dateToStringFull(new Date()));
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);
		if (display != null)
			display.setCurrent(form);

		try {

			byte[] message={ 'a', 'b', 'c', 0, 'a', 'b', 'c', 0, 'a' };
			displayString("message length: "+message.length);
			byte[] encoded=NullEncoder.encode(message);
			displayString("encoded length: "+encoded.length);
			byte[] decoded=NullEncoder.decode(encoded);
			displayString("decoded length: "+decoded.length);
			
			if (Helpers.compareByteArray(message, decoded)) 
				displayString("equal");
			else {
				displayString("not equal");
				for (int i=0; i<decoded.length; i++) {
					displayString(i+" :"+decoded[i]+" != "+message[i]);
				}
			}
			
		} catch (Exception exp) {
			System.out.println("Exception: " + exp.toString());
		}
		
		try {
			byte[] message=new byte[255];
			for (int i=0; i< message.length; i++) {
				message[i]=(byte)i;
			}
			
			byte[] encoded=SevenBitEncoder.encode(message);
			displayString("7Bit - length:"+encoded.length);
			
			for (int i=0; i<encoded.length; i++) {
				if ((encoded[i] & 0x80) != 0) {
					displayString("8Bit at "+i+": "+encoded[i]);
				}
			}
			
		} catch (Exception exp) {
			System.out.println("Exception: " + exp.toString());
			exp.printStackTrace();
		}
		
		displayString("finish: " + Helpers.dateToStringFull(new Date()));

	}

}

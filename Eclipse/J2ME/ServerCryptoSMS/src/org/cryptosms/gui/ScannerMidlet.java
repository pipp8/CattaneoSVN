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

package org.cryptosms.gui;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.wireless.messaging.MessageConnection;

/**
 * Naively go and try to open every port for receiving sms.
 * Produce a graphical output while doing so. It shows all port
 * which can be opend (without causing an exception). 
 * 
 * This is not a garanty that this ports will be usable ... ports
 * causing an exception are surely not usable however. 
 * 
 * @author malte
 *
 */
public class ScannerMidlet extends MIDlet implements CommandListener {
	protected Display display;

	protected Form form;

	protected static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);

	public void commandAction(Command c, Displayable d) {
		if (c == CMD_EXIT) {
			destroyApp(false);
			notifyDestroyed();
		}
	}

	protected void destroyApp(boolean arg0) {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		form = new Form("PortScanner");
		form.append("scanning usable sms ports\n");
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);

		TextField progress=new TextField("", "starting", 10, TextField.UNEDITABLE);
		form.append(progress);
		
		if (display != null)
			display.setCurrent(form);
		
		
		String serverUrl="sms://:";
		int range=-1;
		int i=0;
		for (i=0; i<65536; i++) {
			if ((i % 100) == 0) progress.setString(""+i);
			try {
				MessageConnection connection=(MessageConnection)Connector.open(serverUrl+i);
				if (connection != null) {
					if (range <0) range=i;
					connection.close();
				}
			} catch (Exception e) {
				if (range>=0) {
					form.append(range+"-"+(i-1));
					if (display != null)
						display.setCurrent(form);

					range=-1;
				}
			}
		}
		if (range>=0) {
			form.append(range+"-"+(i-1));
		}
		form.append("fin");
		if (display != null)
			display.setCurrent(form);
		
	}

}

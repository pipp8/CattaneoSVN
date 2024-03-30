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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.cryptosms.helpers.ResourceManager;
import org.cryptosms.helpers.ResourceTokens;

/**
 * @version $Revision: 1.1 $
 */
public class Login extends TextBox implements CommandListener {
	private final Command ok;

	private final Command exit;

	private final CryptoSMSMidlet midlet;
	private int counter=0;

	/**
	 * Creates a new Login object. 
	 */
	public Login(CryptoSMSMidlet midlet) {
		super(midlet.getResourceManager().getString(ResourceTokens.STRING_ENTER_PASSPHRASE), "", 30, TextField.PASSWORD);
		ResourceManager rm = midlet.getResourceManager();

		this.midlet = midlet;
		this.ok = new Command(rm.getString(ResourceTokens.STRING_OK), Command.OK, 1);
		this.exit = new Command(rm.getString(ResourceTokens.STRING_EXIT), Command.EXIT, 1);
		this.addCommand(this.ok);
		this.addCommand(this.exit);
		setCommandListener(this);
		// this.setTicker(midlet.getTicker());
	}

	public void commandAction(Command c, Displayable disp) {
		if (c == this.ok) {
			try{
				midlet.initAddressService(this.getString());
				
				midlet.finishInit();
				midlet.getMenu();
				
			}catch(Exception e){
				counter++;
				this.setTitle(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_PASS_NOT_MATCH));
				this.setString("");
			}
		}
		if (c == this.exit||counter>3) {
			midlet.notifyDestroyed();
		}
	}
}

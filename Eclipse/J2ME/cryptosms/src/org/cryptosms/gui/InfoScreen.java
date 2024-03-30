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
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import org.cryptosms.book.Settings;
import org.cryptosms.helpers.ResourceManager;
import org.cryptosms.helpers.ResourceTokens;


/**
 * 	@author sd
 *	@date 02.05.2007
 */
public class InfoScreen extends Form implements CommandListener {

	
	private final Command ok;
	private final Command alterNumber;

	private final CryptoSMSMidlet midlet;
	
	public InfoScreen(CryptoSMSMidlet midlet, String title) {
		super(title);
		this.midlet=midlet;
		ResourceManager rm = midlet.getResourceManager();
		Settings s=midlet.getSettings();
		this.append("Dip. di Informatica ed Applicazioni\n\"R.M. Capocelli\"\nUniv. Salerno\n");
		this.append(rm.getString(ResourceTokens.STRING_NAME)+s.getName()+"\n");
		this.append(rm.getString(ResourceTokens.STRING_NUMBER)+s.getNumber()+"\n");
		this.append(rm.getString(ResourceTokens.STRING_VERSION)+s.getVersion()+"\n");
//		this.append(rm.getString(ResourceTokens.STRING_BUILD)+s.getBuild()+"\n");
		this.append("SMSC: " + System.getProperty("wireless.messaging.sms.smsc")+"\n");
		
		this.ok = new Command(rm.getString(ResourceTokens.STRING_BACK), Command.OK, 1);
		this.alterNumber = new Command(rm.getString(ResourceTokens.STRING_EDIT_ENTRY), Command.OK, 1);
		
		this.addCommand(this.ok);
		this.addCommand(this.alterNumber);
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
		
	}


	public void commandAction(Command co, Displayable arg1) {
		if (co == this.ok) {
			midlet.showMenu();
		}
		if(co==alterNumber){
			midlet.setTicketText("todo");
		}
		}
		
	

}

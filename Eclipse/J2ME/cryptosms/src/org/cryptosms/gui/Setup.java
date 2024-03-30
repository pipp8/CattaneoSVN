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
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.cryptosms.book.AddressService;
import org.cryptosms.book.Settings;
import org.cryptosms.helpers.CryptoHelper;
import org.cryptosms.helpers.Logger;
import org.cryptosms.helpers.ResourceManager;
import org.cryptosms.helpers.ResourceTokens;


/**
 * @author sd
 */
public class Setup extends Form implements CommandListener {
	private final Command ok;

	TextField name;

	TextField number;

	TextField pass;
	TextField repeatPass;
	
	CryptoSMSMidlet midlet;

	public Setup(CryptoSMSMidlet midlet) {
		super(midlet.getResourceManager().getString(ResourceTokens.STRING_INITIALIZATION));
		ResourceManager rm = midlet.getResourceManager();
		this.midlet = midlet;
		name = new TextField(rm.getString(ResourceTokens.STRING_YOUR_NAME), "", 20, TextField.ANY);
		number = new TextField(rm.getString(ResourceTokens.STRING_YOUR_NUMBER), "", 20, TextField.PHONENUMBER);
		pass = new TextField(rm.getString(ResourceTokens.STRING_ENTER_PASSPHRASE), "", 20, TextField.PASSWORD);
		repeatPass = new TextField(rm.getString(ResourceTokens.STRING_FORM_REPEAT_PASSPHRASE), "", 20, TextField.PASSWORD);
		this.append(name);
		this.append(number);
		this.append(pass);
		this.append(repeatPass);
		this.ok = new Command(midlet.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
		this.addCommand(this.ok);
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}

	public void commandAction(Command co, Displayable disp) {
		if (co == this.ok) {
			
			// Controlla se tutti i valori inseriti sono validi
			
			if(this.name.getString().length()<2 || this.number.getString().length()<7|| this.pass.getString().length()<5||!pass.getString().equals(repeatPass.getString())){
				StringBuffer e=new StringBuffer();
				if(this.name.getString().length()<2){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_NAME_MIN_2_CHARS));
					e.append("\n");
				}
				if(this.number.getString().length()<7 ){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_NUMBER_MIN_7_CHARS));
					e.append("\n");
				}
				if(this.pass.getString().length()<5){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_PASS_MIN_5_CHARS));
					e.append("\n");
				}
				if(!pass.getString().equals(repeatPass.getString())){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_PASS_NOT_MATCH_REPEAT));
					e.append("\n");
				}
				midlet.showInfo(e.toString());
			} 
			else {	
				try {				
				midlet.initAddressService(pass.getString());
				Display d = Display.getDisplay(midlet);
				d.setCurrent( new ProgressScreen(midlet.getMenu() , midlet.getResourceManager().getString(ResourceTokens.STRING_GENERATING_KEYPAIR),d){
				
				  public void run(){
					  this.setStatus("progressbar opens");
					  try 
					  {
						  Settings s=AddressService.saveSettings(name.getString(), number.getString(), CryptoHelper.generateKeyPairHelper(this));
						  midlet.setSettings(s);
						  this.setStatus("progressbar closes");				    
						  midlet.finishInit();
					  } 
					  
					  catch (Exception e) 
					  {
						  Logger.getInstance().write((midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_THREAD  + "\n"+ e.getMessage())));					
					  }
					  
					  finally
					  {
						  stop();
					  }
				  }
				} );
				//AddressService.initSettings(name.getString(), number.getString(), CryptoHelper.generateKeyPairHelper());
				//midlet.showMenu();
			} catch (Exception e) {
				midlet.showError(e.getMessage());
			}
			}
		}
	}
}
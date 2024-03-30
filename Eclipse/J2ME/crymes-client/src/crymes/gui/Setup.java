/*
 * Copyright (C) 2007 Crypto Messaging with Elliptic Curves Cryptography (CRYMES)
 *
 * This file is part of Crypto Messaging with Elliptic Curves Cryptography (CRYMES).
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Crypto Messaging with Elliptic Curves Cryptography (CRYMES); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package crymes.gui;


import javax.microedition.lcdui.*;

import crymes.book.AddressService;
import crymes.book.Settings;
import crymes.helpers.CryptoHelper;
import crymes.helpers.Logger;
import crymes.helpers.ResourceManager;
import crymes.helpers.ResourceTokens;


/**
 * @author sd
 */
public class Setup extends Form implements CommandListener {
	private final Command ok;

	TextField name;

	TextField number;

	TextField pass;
	TextField repeatPass;
	ChoiceGroup cgSmsc;
	CrymesMidlet midlet;
	private int smscIndex;       // Index of "reply" in choice group
	
	public Setup(CrymesMidlet midlet) {
		super(midlet.getResourceManager().getString(ResourceTokens.STRING_INITIALIZATION));
		ResourceManager rm = midlet.getResourceManager();
		this.midlet = midlet;
		name = new TextField(rm.getString(ResourceTokens.STRING_YOUR_NAME), "", 20, TextField.ANY);
		number = new TextField(rm.getString(ResourceTokens.STRING_YOUR_NUMBER), "", 20, TextField.PHONENUMBER);
		pass = new TextField(rm.getString(ResourceTokens.STRING_ENTER_PASSPHRASE), "", 20, TextField.PASSWORD);
		repeatPass = new TextField(rm.getString(ResourceTokens.STRING_FORM_REPEAT_PASSPHRASE), "", 20, TextField.PASSWORD);
		
		if (!isEmulator()){
			// Create an exclusive (radio) choice group
		    cgSmsc = new ChoiceGroup("CGSS address", Choice.EXCLUSIVE);
		    // Append options, with no associated images
		    smscIndex = cgSmsc.append("WIND", null);
		    cgSmsc.append("TIM", null);
		    cgSmsc.append("Vodafone", null);    
		    cgSmsc.append("3", null);   
		    cgSmsc.setSelectedIndex(smscIndex, true);
		}
		   
		this.append(name);
		this.append(number);
		this.append(pass);
		this.append(repeatPass);
		
		if (!isEmulator())
			this.append(cgSmsc);
		
		this.ok = new Command(midlet.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
		this.addCommand(this.ok);
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}
	
	private boolean isEmulator () {
		Logger.getInstance().write(System.getProperty("microedition.platform"));
		
		String platform = System.getProperty("microedition.platform");
		boolean isEmulated = (platform != null && (platform.endsWith("JAVASDK") || platform.equals("j2me") || platform.equals("SunMicrosystems_wtk")));
		
		return isEmulated;
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
						  String smscNumber ="";
					
							if (isEmulator()){
								smscNumber = "+5554827";
							} else {
								
							
							
						  switch (cgSmsc.getSelectedIndex()) {
						case 0:
							// WIND
							smscNumber = "+393804351160";
							break;
						case 1:
							// TIM
							smscNumber = "+393338966733";
							break;
						case 2:
							// Vodafone
							smscNumber = "+393290904025";
							
							break;
						case 3:
							// H3G
							smscNumber = "+393928191659";
							break;							
						default:
							break;
						}
							}
						  
						  Settings s=AddressService.saveSettings(name.getString(), number.getString(), CryptoHelper.generateKeyPairHelper(this), smscNumber);
						  midlet.setSettings(s);
						  this.setStatus("progressbar closes");				    
						  midlet.finishInit();
					  } 
					  
					  catch (Exception e) 
					  {
						  System.out.println("mau qui 5");
						  
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
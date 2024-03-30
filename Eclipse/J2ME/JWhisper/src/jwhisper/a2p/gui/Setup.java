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

package jwhisper.a2p.gui;


//import javax.microedition.lcdui.Choice;
//import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextField;

import jwhisper.Settings;
import jwhisper.a2p.ConstantA2P;
import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;
import jwhisper.crypto.Utils;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.utils.Logger;
import jwhisper.utils.ResourceManager;
import jwhisper.utils.ResourceTokens;

import gr.bluevibe.fire.components.Component;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.displayables.FireScreen;
import gr.bluevibe.fire.util.CommandListener;


/**
 * @author sd
 */
public class Setup extends Panel implements CommandListener {
	private final Command ok;
	private IKeyPairGenerator _keyPairGenerator;
	private String _algorithm = "ECIES";
	
	Row name;
	Row number;
	Row pass;
	Row repeatPass;
	Row smscNumber;

	JWhisperA2PMidlet midlet;
	
	public Setup(JWhisperA2PMidlet midlet) {

		this.setLabel(midlet.getResourceManager().getString(ResourceTokens.STRING_INITIALIZATION));
		ResourceManager rm = midlet.getResourceManager();
		_keyPairGenerator = Utils.GetKeyPairGenerator(_algorithm);
		this.midlet = midlet;
		
		// now lets add a text area
		name = new Row();
		name.setEditable(true); 
		name.setLabel(rm.getString(ResourceTokens.STRING_YOUR_NAME),
									  FireScreen.defaultLabelFont,
									  new Integer(midlet.getFireScr().getWidth()/2), 
									  FireScreen.CENTRE); 
		name.setTextBoxConstrains(TextField.INITIAL_CAPS_WORD); 
		name.setTextBoxSize(20); 
		name.setCommandListener(this);
		this.add(name); 
		
		number = new Row();
		number.setEditable(true); 
		number.setLabel(rm.getString(ResourceTokens.STRING_YOUR_NUMBER),
									  FireScreen.defaultLabelFont,
									  new Integer(midlet.getFireScr().getWidth()/2), 
									  FireScreen.CENTRE); 
		number.setTextBoxConstrains(TextField.PHONENUMBER ); 
		number.setTextBoxSize(20); 
		this.add(number); 
		
		smscNumber = new Row();		
		smscNumber.setLabel(rm.getString(ResourceTokens.STRING_SMSC_GSM_NUMBER),
				  FireScreen.defaultLabelFont,
				  new Integer(midlet.getFireScr().getWidth()/2), 
				  FireScreen.CENTRE); 
		smscNumber.setTextBoxConstrains(TextField.PHONENUMBER ); 
		smscNumber.setTextBoxSize(20); 
		if (!isEmulator()){
			smscNumber.setEditable(true);
		} else {
			smscNumber.setText(ConstantA2P.CGSS_PHONE_NUMBER);
			smscNumber.setEditable(false);
		}
		this.add(smscNumber); 
		
		
		
		pass = new Row();
		pass.setEditable(true); 
		pass.setLabel(rm.getString(ResourceTokens.STRING_ENTER_PASSPHRASE),
									  FireScreen.defaultLabelFont,
									  new Integer(midlet.getFireScr().getWidth()/2), 
									  FireScreen.CENTRE); 
		pass.setTextBoxConstrains(TextField.INITIAL_CAPS_WORD); 
		pass.setTextBoxConstrains(TextField.PASSWORD); 
		pass.setTextBoxSize(20); 
		this.add(pass); 
		
		repeatPass = new Row();
		repeatPass.setEditable(true); 
		repeatPass.setLabel(rm.getString(ResourceTokens.STRING_FORM_REPEAT_PASSPHRASE),
									  FireScreen.defaultLabelFont,
									  new Integer(midlet.getFireScr().getWidth()/2), 
									  FireScreen.CENTRE);
		repeatPass.setTextBoxConstrains(TextField.INITIAL_CAPS_WORD);
		repeatPass.setTextBoxConstrains(TextField.PASSWORD); 
		repeatPass.setTextBoxSize(20); 	
		//repeatPass.addCommand(new Command("", 12, 0));
		this.add(repeatPass); 	
		

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

	public void commandAction(Command co, Component c) {

		if (co == this.ok) {
			// Controlla se tutti i valori inseriti sono validi
			
			if(this.name.getText().length()<2 || this.number.getText().length()<7|| this.pass.getText().length()<5||!pass.getText().equals(repeatPass.getText())){
				StringBuffer e=new StringBuffer();
				if(this.name.getText().length()<2){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_NAME_MIN_2_CHARS));
					e.append("\n");
				}
				if(this.number.getText().length()<7 ){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_NUMBER_MIN_7_CHARS));
					e.append("\n");
				}
				if(this.smscNumber.getText().length()<7 ){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_NUMBER_MIN_7_CHARS));
					e.append("\n");
				}
				if(this.pass.getText().length()<5){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_PASS_MIN_5_CHARS));
					e.append("\n");
				}
				if(!pass.getText().equals(repeatPass.getText())){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_PASS_NOT_MATCH_REPEAT));
					e.append("\n");
				}
				midlet.getFireScr().getCurrentPanel().showAlert(e.toString(), null);
			} 
			else {	
				try {				
				midlet.initAddressService(pass.getText());
				FireScreen d = midlet.getFireScr();
				d.setCurrent( new ProgressScreen(midlet.getMenu() , "Generating Keypair"/*midlet.getResourceManager().getString(ResourceTokens.STRING_GENERATING_KEYPAIR)*/, midlet.getFireScr()){
				
				  public void run(){
					  this.setStatus("progressbar opens");
					  try 
					  {
						  String smscNumberStr = Setup.this.smscNumber.getText();
		  
							  System.out.println("smscNumber = " + smscNumberStr);
						
						KeyPair k = _keyPairGenerator.generateKeyPair();
						Settings s=AddressService.saveSettingsA2P(_algorithm, name.getText(), number.getText(), k, smscNumberStr);
						  
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
				
			} catch (Exception e) {
				midlet.showError(e.getMessage());
			}
			}
		}
	}
}
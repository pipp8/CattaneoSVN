/*
 * Copyright (C) 2007 Crypto Messaging with Elliptic Curves Cryptography
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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextField;
import jwhisper.utils.ResourceTokens;
import jwhisper.utils.ResourceManager;

import gr.bluevibe.fire.components.Component;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.displayables.FireScreen;
import gr.bluevibe.fire.util.CommandListener;


/**
 * @version $Revision: 1.7 $
 */
public class Login extends Panel implements CommandListener {
	private final Command ok;

	private final Command exit;

	private final JWhisperA2PMidlet midlet;
	private int   counter=0;
	
	private Row textField;

	/**
	 * Creates a new Login object. 
	 */
	public Login(JWhisperA2PMidlet midlet) {
		this.midlet = midlet;
		
		setLabel("Login"); // set label (displayed on the top of the screen)
		ResourceManager rm = midlet.getResourceManager();
		
		// now lets add a text area
		textField = new Row();
		textField.setEditable(true); // when a row is set as editable 
     								 // it gets a min height equal to the Fonts height.
		
		// you can even add a label on a row, so lets add one to our textField example
		textField.setLabel(midlet.getResourceManager().getString(ResourceTokens.STRING_ENTER_PASSPHRASE),
						   FireScreen.defaultLabelFont,
						   new Integer(midlet.getFireScr().getWidth()/2), // set the width of the label to be half the screen width.
							                                        // this helps with the alligment of multiple textfields.
									  						        // If you dont care about the label width, you can leave this null
						   FireScreen.CENTRE);                      // The alligment of the label in the row (applicable if row.height>labeltext.heigh)
		
		textField.setTextBoxConstrains(TextField.INITIAL_CAPS_WORD); // if a row is editable it gets the same constratints as a TextBox
		textField.setTextBoxConstrains(TextField.PASSWORD);
		textField.setTextBoxSize(20); // max name size=20
		// when a user enters text on a text field you can get it back using the getText() method. 
		this.add(textField); // finally add it to the panel.
		
		this.exit = new Command(rm.getString(ResourceTokens.STRING_EXIT), Command.BACK, 1);
		this.addCommand(exit); // add the command to the panel, 
		// it will be displayed on the bottom bar on the right, and assigned to the righ softkey.
		// The Panel checks the type of the command added. If it is Command.BACK 
		// it is assigned to the left softkey, otherwise it goes to the right.
		this.ok   = new Command(rm.getString(ResourceTokens.STRING_OK),   Command.EXIT, 1);
		this.addCommand(ok); // add the command to the panel,  
		
		// now set the CommandListener of the panel.
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}

	public void commandAction(Command cmd, Component c) {
		if (cmd == this.ok) {
			try{
				midlet.initAddressService(textField.getText());
				midlet.finishInit();
			}
			catch(Exception e){
				counter++;
				midlet.getFireScr().closePopup(); // first close the popup.
				// The panel has an alertUser method, witch displays an alert-like message to the user. 
				// This can be very usefull for reporting errors,warnings etc in order to display an alert 
				// you can call the showAlert method on the current panel.
				
				midlet.getFireScr().getCurrentPanel().showAlert(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_PASS_NOT_MATCH), null);
			}
		}
		if (cmd == this.exit||counter>3) {
			midlet.notifyDestroyed();
		}
	}
}

/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.p2p.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import jwhisper.utils.ResourceManager;
import jwhisper.utils.ResourceTokens;


/*
 * @version $Revision: 1.3 $
 */
public class Login extends TextBox implements CommandListener {
	private final Command ok;

	private final Command exit;

	private final JWhisperP2PMidlet midlet;
	private int counter=0;

	/*
	 * Creates a new Login object. 
	 */
	public Login(JWhisperP2PMidlet midlet) {
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

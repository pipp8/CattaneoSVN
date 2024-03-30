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
import javax.microedition.lcdui.Form;

import jwhisper.Settings;
import jwhisper.utils.ResourceManager;
import jwhisper.utils.ResourceTokens;





public class InfoScreen extends Form implements CommandListener {

	
	private final Command ok;
	private final Command alterNumber;

	private final JWhisperP2PMidlet midlet;
	
	public InfoScreen(JWhisperP2PMidlet midlet, String title) {
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

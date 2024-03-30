/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import jwhisper.utils.Logger;


/*
 * Produces a view of the log messages in the Helpers.Logger ringbuffer. It's a simple
 * and forward thing, no 'tail -f' or other nice stuff.
 * 
 * @author malte
 */
public class LogOutput extends Form implements CommandListener {
	
	protected static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);

	private JWhisperMidlet midlet;
	
	public LogOutput(JWhisperMidlet midlet) {
		super("Log");
		this.midlet=midlet;
		
		String[] 	lines=Logger.getInstance().readout();
		
		for (int i=0; i<lines.length; i++) 
			this.append(lines[i]);
		
		lines=null;
		
		this.addCommand(CMD_EXIT);
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}
	
	public void commandAction(Command c, Displayable d) {
		if (c == CMD_EXIT) {
			midlet.showMenu();
		}
	}
}

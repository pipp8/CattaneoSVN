package jwhisper.a2p.gui;

import gr.bluevibe.fire.components.Component;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.util.CommandListener;

import javax.microedition.lcdui.Command;


/**
 * catching accidential 'exits' by asking if one wants to really exit.
 * 
 * This is especially neccessary on old phones where keypressing can cause
 * two events (instead of the intended one).
 * 
 * @author malte
 *
 */
public class ExitScreen extends Panel implements CommandListener{

	protected static final Command CMD_YES = new Command("Si", Command.BACK, 0);
	protected static final Command CMD_NO =  new Command("No", Command.EXIT, 1);
	
	private JWhisperA2PMidlet midlet;
	
	public ExitScreen(JWhisperA2PMidlet midlet) {

		this.setLabel("Uscita");
		
		this.midlet=midlet;
		
		this.add(new Row("Vuoi veramente terminare l'applicazione?"));
		this.add(new Row()); // empty line.
		
		this.addCommand(CMD_YES);
		this.addCommand(CMD_NO);
		
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}

	public void commandAction(Command cmd, Component c) {
		if (cmd == CMD_YES) {
			midlet.notifyDestroyed();
		}
		else {
			midlet.showMenu();
		}
		
	}

}

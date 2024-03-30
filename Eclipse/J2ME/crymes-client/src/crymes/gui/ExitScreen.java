package crymes.gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 * catching accidential 'exits' by asking if one wants to really exit.
 * 
 * This is especially neccessary on old phones where keypressing can cause
 * two events (instead of the intended one).
 * 
 * @author malte
 *
 */
public class ExitScreen extends Form implements CommandListener{

	protected static final Command CMD_YES = new Command("Yes", Command.OK, 0);
	protected static final Command CMD_NO = new Command("No", Command.EXIT, 1);

	private CrymesMidlet midlet;
	
	public ExitScreen(CrymesMidlet midlet) {
		super("Exit");
		
		this.midlet=midlet;
		
		this.append("Do you really want to quit?");
		
		this.addCommand(CMD_YES);
		this.addCommand(CMD_NO);
		
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
	}
	
	public void commandAction(Command c, Displayable d) {
		if (c == CMD_YES) {
			midlet.notifyDestroyed();
		}
		else {
			midlet.showMenu();
		}
	}

}

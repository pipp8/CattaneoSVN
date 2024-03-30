/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.gui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;


public class ErrorScreen extends Alert /* implements CommandListener */ {

	/*
	 * Creates a new ErrorScreen object.
	 */
	// private final Command cancel;
	private ErrorScreen() {
		super("Message:");
		this.setTimeout(5000);
		this.setCommandListener(null); // use the default
		//TODO i18n
		/*
		this.cancel = new Command("cancel", Command.CANCEL, 0);
		this.addCommand(this.cancel);
		*/
	}

	public static void showE(Display d, String e, Displayable nextScreen) {
		show(d, e, nextScreen, AlertType.ERROR);
	}

	public static void showW(Display d, String e, Displayable nextScreen) {
		show(d, e, nextScreen, AlertType.WARNING);
	}

	public static void showI(Display d, String e, Displayable nextScreen) {
		show(d, e, nextScreen, AlertType.INFO);
	}

	// generic function for alert box
	// TODO if running in debugmode: print to system.out
	private static synchronized void show(Display d, String e, Displayable nextScreen, AlertType type) {
		
		ErrorScreen es = new ErrorScreen();
		es.setType(type);
		es.setString(e);
		d.setCurrent(es, nextScreen);
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	/*
	public void commandAction(Command co, Displayable arg1) {
		if (co == this.cancel) {
			this.setTimeout(1);
			
	}
	
	}
	*/

}

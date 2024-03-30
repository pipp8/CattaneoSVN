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
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;

/*
 * EraserMidlet: reset/erase everything.
 * 
 * TODO also reset the PushRegistry
 * 
 * @author malte
 */
public class JWhisperCleanerMidlet extends MIDlet implements CommandListener {
	protected Display display;

	protected Form form;

	protected static final Command CMD_OK = new Command("OK", Command.OK, 0);
	protected static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);

	public void commandAction(Command c, Displayable d) {
		if (c == CMD_EXIT) {
			destroyApp(false);
			notifyDestroyed();
		}
		if (c == CMD_OK) {
			try {
				String[] stores=RecordStore.listRecordStores();
				if (stores != null) {
					for (int i=0;i<stores.length;i++) {
						try {
							RecordStore.deleteRecordStore(stores[i]);
							form.append(stores[i]+" - ok");
						} catch (Exception e) {
							form.append(stores[i]+" - "+e.toString());
						}
					}
					form.append("erased!");
				}
				else {
					form.append("no stores to delete");
				}
			} catch (Exception e2) {
				form.append(e2.toString());
			}
			if (display != null)
				display.setCurrent(form);
		}
	}

	protected void destroyApp(boolean arg0){
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		// TODO Auto-generated method stub
		form=new Form("Eraser");
		form.append("erase all stored data?");
		form.addCommand(CMD_OK);
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);
		
		String[] stores=RecordStore.listRecordStores();
		if (stores != null) 
			for (int i=0;i<stores.length;i++) 
				form.append(stores[i]);
		
		display=Display.getDisplay(this);
		if (display != null)
			display.setCurrent(form);
	}

}

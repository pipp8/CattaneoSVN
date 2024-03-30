/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.gui;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import jwhisper.utils.ResourceManager;
import jwhisper.utils.ResourceTokens;


/*
 * @version $Revision: 1.3 $
 */
public class Menu extends List implements CommandListener {

	private final Command exit;

	private final Command ok;

	private final JWhisperMidlet _midlet;

	private final String[] mainMenu = { ResourceTokens.STRING_NEW_SMS, 
										ResourceTokens.STRING_UNREAD_INBOX,
										ResourceTokens.STRING_READ_INBOX,
										ResourceTokens.STRING_OUTBOX, 
										ResourceTokens.STRING_ADDRESSBOOK, 
										ResourceTokens.STRING_KEY_LIST,
										ResourceTokens.STRING_INFO
										};
	
//	private final String[] mainMenu = { ResourceTokens.STRING_UNREAD_INBOX,
//										ResourceTokens.STRING_READ_INBOX,
//										ResourceTokens.STRING_INFO
//										};

	public Menu(JWhisperMidlet midlet) {
		super(midlet.getResourceManager().getString(ResourceTokens.STRING_MENU), Choice.IMPLICIT);
		ResourceManager rm = midlet.getResourceManager();
		this._midlet = midlet;

		this.ok = new Command(rm.getString(ResourceTokens.STRING_OK), Command.OK, 1);
		this.exit = new Command(rm.getString(ResourceTokens.STRING_EXIT), Command.EXIT, 1);

		this.addCommand(this.ok);
		this.addCommand(this.exit);
		this.setCommandListener(this);
		
		this.setTicker(_midlet.getTicker());
		
		buildList();
	}

	private void buildList() {
		this.deleteAll();

		ResourceManager rm = _midlet.getResourceManager();
		for (int i = 0; i < mainMenu.length; i++) {
			this.append(rm.getString(mainMenu[i]), null);
		}
		
		this.append("Log", null);
//		this.append("Registrazione", null);
	}

	// TODO i18n
	public void commandAction(Command co, Displayable disp) {
		if (co == this.ok) {
			int i = this.getSelectedIndex();
			switch (i) {
			case 0:
				_midlet.openNewMessage();
				break;
			case 1:
				_midlet.openUnreadMessageList();
				break;
			case 2:
				_midlet.openReadMessageList();
				break;
			case 3:
				_midlet.openOutBox();
				break;
			case 4:
				_midlet.openAdressbook();
				break;
			case 5:
				_midlet.openKeyList();
				break;
			case 6:
				_midlet.openInfoScreen();
				break;
			case 7: // Logger
				_midlet.dumpLog();
				break;
//			case 8: 
//				gui.Registrazione();
//				break;
			default:
				break;

//			case 0:
//				gui.openUnreadMessageList();
//				break;
//			case 1:
//				gui.openReadMessageList();
//				break;
//			case 2:
//				gui.openInfoScreen();
//				break;
//			case 3: // Logger
//				gui.dumpLog();
//				break;
//			case 4: 
//				gui.Registrazione();
//				break;
//			default:
//				break;
			}
		}
		if (co == this.exit) {
			_midlet.openExitScreen();
		}
	}

	public void destroyApp() {
		this._midlet.notifyDestroyed();
	}
}
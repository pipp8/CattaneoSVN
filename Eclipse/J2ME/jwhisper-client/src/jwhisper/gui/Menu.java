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
package jwhisper.gui;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import jwhisper.helpers.ResourceManager;
import jwhisper.helpers.ResourceTokens;


/**
 * @version $Revision: 1.1 $
 */
public class Menu extends List implements CommandListener {

	private final Command exit;

	private final Command ok;

	private final JWhisperMidlet gui;

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

	public Menu(JWhisperMidlet parent) {
		super(parent.getResourceManager().getString(ResourceTokens.STRING_MENU), Choice.IMPLICIT);
		ResourceManager rm = parent.getResourceManager();
		this.gui = parent;

		this.ok = new Command(rm.getString(ResourceTokens.STRING_OK), Command.OK, 1);
		this.exit = new Command(rm.getString(ResourceTokens.STRING_EXIT), Command.EXIT, 1);

		this.addCommand(this.ok);
		this.addCommand(this.exit);
		this.setCommandListener(this);
		
		this.setTicker(parent.getTicker());
		
		buildList();
	}

	private void buildList() {
		this.deleteAll();

		ResourceManager rm = gui.getResourceManager();
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
				gui.openNewMessage();
				break;
			case 1:
				gui.openUnreadMessageList();
				break;
			case 2:
				gui.openReadMessageList();
				break;
			case 3:
				gui.openOutBox();
				break;
			case 4:
				gui.openAdressbook();
				break;
			case 5:
				gui.openKeyList();
				break;
			case 6:
				gui.openInfoScreen();
				break;
			case 7: // Logger
				gui.dumpLog();
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
			gui.openExitScreen();
		}
	}

	public void destroyApp() {
		this.gui.notifyDestroyed();
	}
}
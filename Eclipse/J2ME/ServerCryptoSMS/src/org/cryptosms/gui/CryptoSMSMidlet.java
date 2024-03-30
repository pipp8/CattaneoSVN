/*
 * Copyright (C) 2007 cryptosms.org
 *
 * This file is part of cryptosms.org.
 *
 * cryptosms.org is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * cryptosms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cryptosms.org; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.cryptosms.gui;

import java.io.IOException;

import javax.microedition.io.PushRegistry;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.cryptosms.book.AddressService;
import org.cryptosms.book.Settings;
import org.cryptosms.connection.MessageReader;
import org.cryptosms.connection.MessageSender;
import org.cryptosms.connection.Receiver;
import org.cryptosms.helpers.CryptedRecordStore;
import org.cryptosms.helpers.Logger;
import org.cryptosms.helpers.PassphraseTimer;
import org.cryptosms.helpers.ResourceManager;
import org.cryptosms.helpers.ResourceTokens;
import org.cryptosms.helpers.TickerTask;

/**
 * @version $Revision: 1.1 $
 */
public class CryptoSMSMidlet extends MIDlet {
	private static final String DEFAULT_LOCALE = "en";

	private Displayable menu;

	private PassphraseTimer timer;

	private Settings _settings;

	private ResourceManager res = null;

	private Form splashScreen;

	private Logger log=null;
	
	private Receiver 	receiver=null;
	
	private Ticker		ticker=null;
	
	private TickerTask	tickerTask=null;
	
	/**
	 * Creates a new Midlet object.
	 * 
	 * @throws
	 */
	public CryptoSMSMidlet() {

	}
	public void initAddressService(String pass) throws Exception {
			CryptedRecordStore.init(pass);
			AddressService.init(this);
			_settings = AddressService.getSetting();

		startPPTimer();
		showMenu();
		
	}
	public void notify(Object o) {
		if (((Boolean) o).booleanValue() == false) {
			Display.getDisplay(this).setCurrent(new Login(this));
		}
	}

	public Displayable getMenu() {
		return menu;
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		MessageReader.stopThread();
	}

	public void openAdressbook() {
		Display.getDisplay(this).setCurrent(new AddressList(this));
	}

	/**
	 * starts Process new Message (Textbox->AddressList)
	 */
	public void openNewMessage() {
		Display.getDisplay(this).setCurrent(new MessageBox(this, menu));

	}

	public void openUnreadMessageList() {
		Display.getDisplay(this).setCurrent(new UnreadMessageList(this,res.getString(ResourceTokens.STRING_UNREAD_INBOX)));
	}
	public void openReadMessageList() {
		Display.getDisplay(this).setCurrent(new ReadMessageList(this,res.getString(ResourceTokens.STRING_READ_INBOX)));
			
	}

	public void openOutBox() {
		Display.getDisplay(this).setCurrent(new OutBoxList(this,res.getString(ResourceTokens.STRING_OUTBOX)));

	}

	public void openKeyList() {
		Display.getDisplay(this).setCurrent(new KeyList(this));
	}
	
	public void openExitScreen() {
		Display.getDisplay(this).setCurrent(new ExitScreen(this));
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
	}

	public void showError(String error) {
		ErrorScreen.showE(Display.getDisplay(this), error, Display.getDisplay(
				this).getCurrent());
	}

	public void showInfo(String info) {
		ErrorScreen.showI(Display.getDisplay(this), info, Display.getDisplay(
				this).getCurrent());
	}

	public void showWarning(String warning) {
		ErrorScreen.showW(Display.getDisplay(this), warning, Display
				.getDisplay(this).getCurrent());
	}

	public void showMenu() {
		Display.getDisplay(this).setCurrent(this.menu);
	}

	protected void startApp() throws MIDletStateChangeException {
		String	startupMessage="starting";
		boolean	validStartup=true;
		log=Logger.getInstance();

		// kickoff receiver
		this.receiver=null;
		String[]	connections=PushRegistry.listConnections(true);
		if (connections.length > 0) {
			for (int i=0;i<connections.length;i++) {
				if (Receiver.SERVER_URL.equals(connections[i])) {
					// we are a registerd push client - start receiver
					this.receiver=Receiver.getInstance(); 
					break;
				}
			}
		} 
		if (this.receiver == null) {
			if (Receiver.getReceiver() == null) {
				// we are not yet living - so do that
				
				try {
					PushRegistry.registerConnection(Receiver.SERVER_URL, this.getClass().getName(), "*");
					this.receiver=Receiver.getInstance();
				} catch (ClassNotFoundException e) {
					// AIEEE - MIDP 1.0 - we shouldn't be here, because of our .jad
					// just start the receiver
					log.write("MIDP1.0 detected");
					this.receiver=Receiver.getInstance();
				} catch (IOException e) {
					log.write("cannot register connection: "+e.toString());
					startupMessage="Cannot send or receive SMS!";
					validStartup=false;
				} 
			} else { 
				// bug in kvm - the PushRegistry tells us nothing about us, even
				// if we are there (are we)?
				this.receiver=Receiver.dirtyInit();
			}
		}
		
		showSplash(startupMessage); // we need a form so that errors during startup can use showError()

		if (!validStartup) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) { }
		}
		
		// load the language
		String locale = System.getProperty("microedition.locale");
		try {
			res = new ResourceManager(locale);
		} catch (IOException e) {
			try {
				res = new ResourceManager(DEFAULT_LOCALE);
			} catch (IOException e1) {
				showError("IO error: "+e.toString());
			}
		}

		this.ticker=new Ticker("");
		this.tickerTask=new TickerTask(this);
		this.menu = new Menu(this);
		this.timer = new PassphraseTimer();
		this.timer.register(this);
	
		try {
			if (CryptedRecordStore.hasPass()) {
				Display.getDisplay(this).setCurrent(new Login(this));
			} else {
				Display.getDisplay(this).setCurrent(new Setup(this));
			}
		} catch (Exception e) {
			this.showError(e.getMessage());
		}
	}

	public void startPPTimer() {
		this.timer.start();
	}

	public Settings getSettings() {
		return _settings;
	}

	public void setSettings(Settings s) {
		this._settings = s;
	}

	public ResourceManager getResourceManager() {
		return res;
	}

	public void setRes(ResourceManager reso) {
		res = reso;
	}

	public void showSplash(String msg) {
		// the initial (first) screen
		// TODO: i18n
		Display display;

		splashScreen = new Form("CryptoSMS");
		splashScreen.append(msg);

		display = Display.getDisplay(this);
		if (display != null)
			display.setCurrent(splashScreen);

	}
	/**
	 * 
	 */
	public void openInfoScreen() {
		Display.getDisplay(this).setCurrent(new InfoScreen(this,res.getString(ResourceTokens.STRING_INFO)));
		
	}

	public void dumpLog() {
		Display.getDisplay(this).setCurrent(new LogOutput(this));
	}

	/**
	 * do the final steps to initialization, meaning, start the services after our
	 * settings object is in place.
	 *
	 */
	public void finishInit() {
		MessageReader.init(this);
		MessageSender.init(this);		
	}
	
	// ticker related methods
	
	public void setTicketText(String text) {
		if (ticker != null) {
			tickerTask.resetCounter();
			ticker.setString(text);
		}
	}
	
	public Ticker getTicker() {
		return this.ticker;
	}
}
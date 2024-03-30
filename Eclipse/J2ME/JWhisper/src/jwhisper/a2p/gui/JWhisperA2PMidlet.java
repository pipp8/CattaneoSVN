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
package jwhisper.a2p.gui;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.PushRegistry;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.wireless.messaging.MessageConnection;

import jwhisper.Constant;
import jwhisper.JWhisperMidlet;
import jwhisper.Settings;
import jwhisper.SettingsA2P;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.connection.MessageReader;
import jwhisper.modules.connection.MessageSender;
import jwhisper.modules.connection.Receiver;
import jwhisper.modules.connection.ReceiverA2P;
import jwhisper.modules.recordStore.CryptedRecordStore;
import jwhisper.utils.Logger;
import jwhisper.utils.PassphraseTimer;
import jwhisper.utils.ResourceManager;
import jwhisper.utils.ResourceTokens;
import jwhisper.utils.TickerTask;

import gr.bluevibe.fire.components.FTicker;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.displayables.FireScreen;
import gr.bluevibe.fire.displayables.SplashScreen;
import gr.bluevibe.fire.util.FireIO;
import gr.bluevibe.fire.util.Lang;

/**
 * @version $Revision: 1.9 $
 */
public class JWhisperA2PMidlet extends JWhisperMidlet {
	private static final String DEFAULT_LOCALE = "en";

	private Panel menu;

	private PassphraseTimer timer;

	private Settings _settings;

	private ResourceManager res = null;

	private Form splashScreen;

	private Logger log=null;
	
	private Receiver 	receiver=null;
	
	private FTicker Ticker;
	
	private TickerTask	tickerTask=null;
	
	private  FireScreen  screen;
	
	/**
	 * Creates a new Midlet object.
	 * 
	 * @throws
	 */
	public JWhisperA2PMidlet() {
		super(Constant.APPLICATION_TYPE_A2P);
	}
	
	public void initAddressService(String pass) throws Exception {
			CryptedRecordStore.init(pass);
			AddressService.init(this);
			_settings = AddressService.getSetting();

		startPPTimer();
	}
	
	public void notify(Object o) {
		if (((Boolean) o).booleanValue() == false) {
			screen.setCurrent(new Login(this)); // set the current panel on the FireScreen.
		}
	}
	
	public Receiver getReceiver() {
		return receiver;
	}

	public Panel getMenu() {
		return menu;
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		MessageReader.stop();
		
		if (ReceiverA2P.getInstance() != null) {
			if (ReceiverA2P.getInstance().getConnection() != null)
				try {
					MessageConnection connection = ReceiverA2P.getInstance().getConnection();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		// In order to stop the clock thread, and clean up you must 
		// call the destroy method on the FireScreen singleton, 
		// when you exit. 
		screen.destroy();
	}


	public void openUnreadMessageList() {
		screen.setCurrent(new UnreadMessageList(this,res.getString(ResourceTokens.STRING_UNREAD_INBOX)));
	}
	
	public void openReadMessageList() {
		screen.setCurrent(new ReadMessageList(this,res.getString(ResourceTokens.STRING_READ_INBOX)));	
	}


	public void Registrazione() {
		screen.setCurrent(new menuRegistrazione(this));
	}
	
	public void openExitScreen() {
		screen.setCurrent(new ExitScreen(this));
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
	}

	public void showError(String error) {
		ErrorScreen.showE(Display.getDisplay(this), error, Display.getDisplay(this).getCurrent());
	}

	public void showInfo(String info) {
		ErrorScreen.showI(Display.getDisplay(this), info, Display.getDisplay(this).getCurrent());
	}

	public void showWarning(String warning) {
		ErrorScreen.showW(Display.getDisplay(this), warning, Display
				.getDisplay(this).getCurrent());
	}

	public void showMenu() {
		screen.setCurrent(this.menu, FireScreen.RIGHT);
	}

	protected void startApp() throws MIDletStateChangeException {
		
		final String SERVER_URL = ReceiverA2P.SERVER_URL_A2P;
		boolean	validStartup=true;
		
		log=Logger.getInstance();
		
		// kickoff receiver
		this.receiver=null;
		
		// PushRegistry.listConnections(true); serve a controllare se l'applicazione � partita
		// normalmente oppure � stata invocata dal push registry.
		// Il parametro true, serve proprio ad indicare che vogliamo la lista delle connessioni
		// che hanno gi� un input disponibile
		String[] connections=PushRegistry.listConnections(true);
		
		//Se ci sono connessioni ...
		if (connections.length > 0) {
			
			log.write("There are " + connections.length + " connections");
			for (int i=0;i<connections.length;i++) {
				
				//... e ce ne sta una connessa proprio sull'url che serve a noi ... 
				if (SERVER_URL.equals(connections[i])) {
					
					// ...allora "we are a registerd push client - start receiver"
					log.write("Started by an incoming SMS");
					this.receiver=ReceiverA2P.getInstance(); 
					break;
				}
			}
		} 
		
		if (this.receiver == null) {
			
			log.write("this.receiver is null");
			
			if (Receiver.isReceiverNull()) {
				// we are not yet living - so do that
				log.write("receiver is still not alive");
				boolean bAlreadyRegistered = false;
				
				try {
					// Normal startup after the first execution
					connections=PushRegistry.listConnections(false);
					if (connections.length > 0) {
						for (int i=0;i<connections.length;i++) {
							if (SERVER_URL.equals(connections[i])) {
								log.write("Our url is allready registered");
								bAlreadyRegistered = true;
								break;
							}
						}
					} 
					
					if (bAlreadyRegistered == false){
						PushRegistry.registerConnection(SERVER_URL, this.getClass().getName(), "*");
					}
					
					this.receiver=ReceiverA2P.getInstance();
				} catch (ClassNotFoundException e) {
					// AIEEE - MIDP 1.0 - we shouldn't be here, because of our .jad
					// just start the receiver
					log.write("MIDP1.0 detected");
					this.receiver=ReceiverA2P.getInstance();
				} catch (IOException e) {
					log.write("cannot register connection: "+e.toString());
					
					validStartup=false;
				} 
			} else { 
				// bug in kvm - the PushRegistry tells us nothing about us, even
				// if we are there (are we)?
				this.receiver=ReceiverA2P.dirtyInit();
			}
		}
		
		initFireScreen();
		openSplashScreen();

		// If there were errors, show them and take some seconds to let it read
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

		this.Ticker = new FTicker("");
		this.tickerTask=new TickerTask(this);
		
		//Menu' creation... we need to cut here
		this.menu = new Menu(this,true);
		this.timer = new PassphraseTimer();
		this.timer.register(this);
	
	}


	public void rebuildMenu(){
		this.menu = new Menu(this,false);
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
	
	public void updateSettings(SettingsA2P s){
		this.setSettings(s);
		try {
			AddressService.saveSettingsA2P(s);
		} catch (RecordStoreFullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		splashScreen = new Form("Crymes");
		splashScreen.append(msg);

		display = Display.getDisplay(this);
		if (display != null)
			display.setCurrent(splashScreen);

	}
	/**
	 * 
	 */
	public void openInfoScreen() {
		screen.setCurrent(new InfoScreen(this,res.getString(ResourceTokens.STRING_INFO)));
	}

	public void dumpLog() {
		screen.setCurrent(new LogOutput(this));
	}

	/**
	 * do the final steps to initialization, meaning, start the services after our
	 * settings object is in place.
	 *
	 */
	public void finishInit() {
		this.menu = new Menu(this, false);
		MessageReader.init(this);
		MessageSender.init(this);
		showMenu();
	}
	
	// ticker related methods
	public void setTicketText(String text) {
		if (Ticker != null) {
			tickerTask.resetCounter();
			Ticker.setText(text);
		}
	}
	
	public FTicker getTicker() {
		return this.Ticker;
	}
	
	public void openSplashScreen() {
		screen.setCurrent(new JWhisperA2PSplash(this, "")); // set the current panel on the FireScreen.
	}
	
	public void initFireScreen() {
		// The first step is to initialize the FireIO. 
		// The FireScreen class uses the FireIO in order to load the 
		// theme image so we really need to do this first.
		// Most of the lines here could be ommited, if the default values are ok.
		
		Hashtable mediaMap = new Hashtable(); // The hashtable maps key string to file 
																		  // name that are located either in the jar or at a web location.
		mediaMap.put(FireScreen.THEME_FILE,"theme-t.png"); // THEME_FILE is the default key for the theme image
		mediaMap.put("logo","logo2.png"); // some other media we are going to use later on the tutorial.
		mediaMap.put("box","box.png");  
		mediaMap.put("checkedbox","checkedbox.png");
		mediaMap.put("fire","Logo_A2P.png");  
		mediaMap.put("water","water.jpg");  
		
		// we setup the FireIO with the mediaMap we just created, and a cache size=10.
		// This means that up to 10 images will be cached in the FireIO class. 
		// The downloadLocation is null because all our images will be local.   
		FireIO.setup(mediaMap,10,null); 
		
		
		// I want to have a nice splash screen while my application initializes 
		// so i will use the SplashScreen class before doing anything else. 
        SplashScreen loading = new SplashScreen(); 
        loading.setTitle("JWhisper A2P"); // set the title of the splash screen
        loading.setLogo(FireIO.getLocalImage("logo")); // show a nice logo.
        // Note that i used the FireIO.getLocalImage in order to load the logo image. 
        // The getLocalImage method will first look in the cache for the image, then in the record store, 
        // thirdly in the jar and finally (if a url was provided on setup()) it will try to download the image and 
        // store it locally. If the image is found on a location it is cached, in case of a later request.
        Display.getDisplay(this).setCurrent(loading); // show the load screen.
        
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // now we continue with the start up proccess:
        
        // First we load the default language bundle:
        Lang.loadBundle(); // this is optional if you dont want to use the Lang class for i18n
        
        // Secondly set the location of the theme logo, on the top border of each panel. 
        // This is optional, the default value is FireScreen.RIGHT, other possible values are FireScreen.CENTER and FireScreen.LEFT.
        FireScreen.setLogoPossition(FireScreen.LEFT);
        
        
        // Thirdly we initialize the FireScreen.
        screen = FireScreen.getScreen(Display.getDisplay(this));
        // A request to the FireScreen.getSctreen() with null parameter, returns the singleton if initialized
        // ie : screen = FireScreen.getScreen(null); returns the same screen object, thus there is no need for keeping the pointer.
        // For the scope of this demo, we will use the screen variable instead of calling the FireScreen.getScreen(null) each time.
        screen.setFullScreenMode(true); // set the FireScreen to full screen mode. 
        
        // You can set the orientation of the screen, normal, or landscape (right handed or left handed)
        screen.setOrientation(FireScreen.NORMAL); // normal is the default orientation
        screen.setOrientationChangeKey(new Integer(Canvas.KEY_STAR));  // no preset key is the default. You can reset that by setting null key.
        
        // set the screen to a non interractive busy mode. That means that no user action is allowed while the screen is in busy mode.
        screen.setInteractiveBusyMode(false); // false is the default value.
	}
	
	public FireScreen getFireScr() {
		return screen;
	}
}
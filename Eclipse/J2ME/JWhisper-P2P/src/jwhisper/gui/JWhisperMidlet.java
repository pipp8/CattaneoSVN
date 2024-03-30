/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.gui;

import java.io.IOException;

import javax.microedition.io.PushRegistry;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.wireless.messaging.MessageConnection;

import jwhisper.modules.Settings;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.modules.connection.MessageReader;
import jwhisper.modules.connection.MessageSender;
import jwhisper.modules.connection.Receiver;
import jwhisper.modules.recordStore.CryptedRecordStore;
import jwhisper.utils.Logger;
import jwhisper.utils.PassphraseTimer;
import jwhisper.utils.ResourceManager;
import jwhisper.utils.ResourceTokens;
import jwhisper.utils.TickerTask;




/*
 * @version $Revision: 1.3 $
 */
public class JWhisperMidlet extends MIDlet {
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
	
//	private Display __display = Display.getDisplay(this);
	
	/*
	 * Creates a new Midlet object.
	 * 
	 * @throws
	 */
	public JWhisperMidlet() {

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
	
	public Receiver getReceiver() {
		return receiver;
	}

	public Displayable getMenu() {
		return menu;
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		MessageReader.stop();
		
		// By Fabio
		if (Receiver.getInstance() != null)
			if (Receiver.getInstance().getConnection() != null)
				try {
					MessageConnection connection = Receiver.getInstance().getConnection();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	public void openAdressbook() {
		Display.getDisplay(this).setCurrent(new AddressList(this));
	}

	/*
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
				if (Receiver.SERVER_URL.equals(connections[i])) {
					
					// ...allora "we are a registerd push client - start receiver"
					log.write("Started by an incoming SMS");
					this.receiver=Receiver.getInstance(); 
					break;
				}
			}
		} 
		
		if (this.receiver == null) {
			
			log.write("this.receiver is null");
			
			if (Receiver.getReceiver() == null) {
				// we are not yet living - so do that
				log.write("receiver is still not alive");
				boolean bAlreadyRegistered = false;
				
				try {
					// Normal startup after the first execution
					connections=PushRegistry.listConnections(false);
					if (connections.length > 0) {
						for (int i=0;i<connections.length;i++) {
							if (Receiver.SERVER_URL.equals(connections[i])) {
								log.write("Our url is allready registered");
								bAlreadyRegistered = true;
								break;
							}
						}
					} 
					
					if (bAlreadyRegistered == false)
						PushRegistry.registerConnection(Receiver.SERVER_URL, this.getClass().getName(), "*");
					
					this.receiver=Receiver.getInstance();
				} catch (ClassNotFoundException e) {
					// AIEEE - MIDP 1.0 - we shouldn't be here, because of our .jad
					// just start the receiver
					log.write("MIDP1.0 detected");
					this.receiver=Receiver.getInstance();
				} catch (IOException e) {
					log.write("cannot register connection: "+e.toString());
					validStartup=false;
				} 
			} else { 
				// bug in kvm - the PushRegistry tells us nothing about us, even
				// if we are there (are we)?
				this.receiver=Receiver.dirtyInit();
			}
		}
		
		openSplashScreen();
		
		// we need a form so that errors during startup can use showError()
		//showSplash(startupMessage); 

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

		this.ticker=new Ticker("");
		this.tickerTask=new TickerTask(this);
		
		//Menu' creation... we need to cut here
		this.menu = new Menu(this);
		this.timer = new PassphraseTimer();
		this.timer.register(this);
		
//		try 
//		{
//			//If the crypted record store exists, we already have done the setup
//			if (CryptedRecordStore.hasPass()) {
//				Display.getDisplay(this).setCurrent(new Login(this));
//			} else {
//				//From here we need to do the setup and the handshake.
//				Display.getDisplay(this).setCurrent(new Setup(this));
//			}
//		} catch (Exception e) {
//			this.showError(e.getMessage());
//		}
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

		splashScreen = new Form("Crymes");
		splashScreen.append(msg);

		display = Display.getDisplay(this);
		if (display != null)
			display.setCurrent(splashScreen);
		
//		Image logo = null;
//		try
//		{
//			logo = Image.createImage("/Logo_png.png");
//			Alert splash = new Alert("Secure Messaging System with ECC",
//				"Dipartimento di Informatica ed Applicazioni \"R.M.Capocelli\"", 
//				logo, null);
//			splash.setTimeout(8000);
//			Display.getDisplay(this).setCurrent(splash);
//		
//			try {
//				Thread.sleep(8000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		catch( IOException e )
//		{
//			System.out.println(e);
//		}
	}
	/**
	 * 
	 */
	public void openInfoScreen() {
		Display.getDisplay(this).setCurrent(new InfoScreen(this,res.getString(ResourceTokens.STRING_INFO)));
		
	}
	
	public void openSplashScreen() {
		Display.getDisplay(this).setCurrent(new JWhisperSplash(this, "Secure Messaging System with ECC"));
		
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
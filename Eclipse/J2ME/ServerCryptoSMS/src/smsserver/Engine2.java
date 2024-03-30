package smsserver;


import java.io.*;
import java.util.Date;

import org.cryptosms.gui.AddressEntry;
import org.cryptosms.gui.AddressList;
import org.cryptosms.gui.MessageBox;
import org.cryptosms.gui.ProgressScreen;
import org.cryptosms.gui.MessageList.MessageText;
import org.cryptosms.helpers.CryptedRecordStore;
import org.cryptosms.helpers.Logger;
import org.cryptosms.helpers.PassphraseTimer;
import org.cryptosms.helpers.ResourceManager;
import org.cryptosms.helpers.ResourceTokens;
import org.cryptosms.helpers.TickerTask;
import org.cryptosms.book.Address;
import org.cryptosms.book.AddressService;
import org.cryptosms.book.CMessage;
import org.cryptosms.book.MessageType;
import org.cryptosms.book.Person;
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

import com.sun.kvem.midp.io.j2se.wma.client.PhoneNumberNotAvailableException;

import smsserver.client.GSMClient;
import smsserver.client.IGenericClient;
import smsserver.client.WtkClient;
import smsserver.listener.CIncomingMessageListener;
import smsserver.listener.ISmsMessageListener;

/*
 * implementa tutte le funzioni invocate dalla GUI
 * atraverso metodi di alto livello.
 * 
 */
@SuppressWarnings("serial")
public class Engine2  implements ISmsMessageListener{

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
	

	private GUIControl _guiControl;
	
	CIncomingMessageListener _listener;

	private WtkClient wtkClient;
    private GSMClient gsmClient;
    
    
	public Engine2(GUIControl gc) {
		
			_guiControl = gc;
			
			_listener = new CIncomingMessageListener(this);
			_guiControl.addMessageLog("CIncomingMessageListener create");
	}
	

	// Invia un sms utilizzando il msg in input e il client specificato
	public void sendSms(MyMessage msg, IGenericClient gClient) {
        try {
            gClient.send(msg);
        } catch (Exception e) {            
            _guiControl.addMessageLog("Exception sending sms.");
        }
	}
	
	// Callback dell'ISmsMessageListener
	public void notifyIncomingMessage(MyMessage msg, boolean isEmulator)  {

		
		IGenericClient gClient = null;
	    if (isEmulator){
	    	gClient = wtkClient;
	    } else {
	    	gClient = gsmClient;
	    }
	
		_guiControl.addSMS(msg);
	
		// Test risposta automatica
		// Risposta al mittente dopo 10 sec se emulatore
		if (isEmulator) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		MyMessage message = new MyMessage();

		message.setSender(gClient.getPhoneNumber());
		String destAddress = "sms://" + msg.getSender() + ":" + CConstants.DEFAULT_SMS_PORT;		
		message.setReceiver(destAddress);
		message.setText("Thank You!");
		
		sendSms(message, gClient);
	}


	// Tenta la connessione al client wireless
	public boolean createWTKConnection(){
		boolean b = true;
		try {
			wtkClient = new WtkClient(_listener);
			_guiControl.addMessageLog("Emulator connected");
		} catch (IOException e) {
			_guiControl.addMessageLog("Impossibile connettersi all'emulatore");
			wtkClient =  null;
			b = false;
		} catch (PhoneNumberNotAvailableException e) {
			_guiControl.addMessageLog("Impossibile specificare il numero telefonico");
			wtkClient =  null;
			b = false;
		}
		return b;
	}

	// Tenta la connessione al modem gsm
	public boolean createGSMModemConnection(){
		boolean b = true;
		String str = "GSMModem connected on " + CConstants.GSM_MODEM_COM_PORT;
	
		gsmClient = new GSMClient(_listener, _guiControl); //cell
		try {
			gsmClient.connect();
			_guiControl.addMessageLog(str);	
		} catch (javax.comm.NoSuchPortException e) {
			str = "GSMClient: NoSuchPortException. Insert the modem. ";
			b = false;
		} catch (javax.comm.PortInUseException e){
			str = "GSMClient: Port currently owned by another process. ";
			b = false;
		} catch (Exception e) {
			str = "GSMClient: Connection refused. ";
			b = false;
		}

		if (b)
			gsmClient.start();
		else
			gsmClient = null;

		//_guiControl.addMessageLog(str);
	
		return b;
	}

	public WtkClient getWtkClient(){
		return wtkClient;
	}
	
	public GSMClient getGSMClient(){
		return gsmClient;
	}


	public void doSetup( String name, String number, String passphrase, String repeatPass){
		if(name.length() < 2 || number.length() < 7 || passphrase.length() < 5 || !passphrase.equals(repeatPass)) {
			StringBuffer e=new StringBuffer();
			if (name.length() < 2){
				e.append("Name must be at least 2 characters!");
				e.append("\n");
			}
			if (number.length() < 7) {
				e.append("Phonenumber must be at least 7 characters!");
				e.append("\n");
			}
			if (passphrase.length() < 5){
				e.append("Passphrase must be at least 5 characters!");
				e.append("\n");
			}
			if(!passphrase.equals(repeatPass)){
				e.append("1. and 2. passphrase entry do not match!");
				e.append("\n");
			}
			showInfo(e.toString());
		} else {	
			try {
				initAddressService(passphrase);
			
			// progress bar con il messaggio "Generating keypair..."
			// d.setCurrent( new ProgressScreen(midlet.getMenu(), midlet.getResourceManager().getString(ResourceTokens.STRING_GENERATING_KEYPAIR),d ){
			
				Runnable p = new Runnable() {
					public void run(){
						// this.setStatus("progressbar opens");
						try {
							Settings s=AddressService.saveSettings(name, number, CryptoHelper.generateKeyPairHelper(this));
							setSettings(s);
							// this.setStatus("progressbar closes");				    
							finishInit();
						} catch (Exception e) {						
							Logger.getInstance().write("Error while starting thread: "  + "\n"+ e.getMessage());					
						} finally{
							stop();
						}
					}
				};
			new Thread(p).start();
			//AddressService.initSettings(name.getString(), number.getString(), CryptoHelper.generateKeyPairHelper());
			//midlet.showMenu();
		} catch (Exception e) {
			showError(e.getMessage());
		}
		}
	}
	
	public void initAddressService(String pass) throws Exception {
		CryptedRecordStore.init(pass);
		AddressService.init(this);
		_settings = AddressService.getSetting();

		startPPTimer();
	}
	
	public void notify(Object o) {
		if (((Boolean) o).booleanValue() == false) {
			// richiede la password perche' e' scaduto il timeout
			// Display.getDisplay(this).setCurrent(new Login(this));
		}
	}

	protected void destroyApp(boolean arg0)  {
		MessageReader.stopThread();
	}

	public void openAdressbook() {
	// passa la li sta degli indirizzi disponibili
	// Display.getDisplay(this).setCurrent(new AddressList(this));
	}

	/**
	 * starts Process new Message (Textbox->AddressList)
	 */
	public void openNewMessage() {
	// 	Display.getDisplay(this).setCurrent(new MessageBox(this, menu));

	}

	public void openUnreadMessageList() {
		// 	new UnreadMessageList(this,"New Messages");
	}
	public void openReadMessageList() {
		// new ReadMessageList(this,"Old Messages");	
	}

	public void openOutBox() {
		// new OutBoxList(this,"Sent messages");

	}

	public void openKeyList() {
		// new KeyList(this);
	}
	
	public void openExitScreen() {
		// new ExitScreen(this);
	}

	public void showError(String error) {
		// visualizza il messaggio di errore (log ???)
		//  error
	}

	public void showInfo(String info) {
		// visualizza informazioni aboutBox
		// info
	}

	public void showWarning(String warning) {
		// visualizza eventuali warning
		// warning
	}

	protected void startApplication() {
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

		// this.ticker=new Ticker("");
		// this.tickerTask=new TickerTask(this);
		this.timer = new PassphraseTimer();
		this.timer.register(this);
	
		try {
			if (CryptedRecordStore.hasPass()) {
				doLogin(); // chiede solo la password e chiama doLogin(passwd)
			} else {
				doSetup(); // chiede nome, numero e password --- e chiama doSetup(n.p.p.)
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
		// splashScreen = new Form("CryptoSMS");
		// splashScreen.append(msg);
	}
	/**
	 * 
	 */
	public void openInfoScreen() {
		// mostra l'about Box
		// "Info";
		
	}

	public void dumpLog() {
		// show log textBox
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
	
	public void ShowMessage() {
		if (this.getSelectedIndex()>=0){
			CMessage m = messages[this.getSelectedIndex()];				
					String text =null;
					text=getMessageText(m);
					if(text!=null)
						try {
							Display.getDisplay(gui).setCurrent(new MessageText(this, text,AddressService.findAdress4Url(m.getSender())));
						} catch (Exception e) {
							gui.setTicketText(ResourceTokens.STRING_ERROR_NO_PERSON_WITH_NUMBER);
						}
		}
	}

	private void setMessages() {
		try {
			messages = AddressService.getPublicKeys();
		} catch (Exception e) {
			gui.showError(e.getMessage());
		}
	}

	private void buildList() {
		setMessages();
		this.deleteAll();
		String number;
		Address address=null;
		if (messages != null) {
			for (int i = 0; i < messages.length; i++) {
				number=messages[i].getSender();
				
				try {
					address=AddressService.findAdress4Url(number);
				} catch (Exception e) {
					// TODO i18n
					//e.printStackTrace();
				}
				if (address==null)
					this.append(number,null);
				else
					this.append(address.getPerson().getName(),null);	
			}
		}
	}

	public void importKey() {

		if (this.getSelectedIndex()>=0){
			CMessage m = messages[this.getSelectedIndex()];

			if (m.getType() == MessageType.KEY) {
				try {
					AddressService.importKey4Url(m.getSender(), m.getData());
					gui.setTicketText(gui.getResourceManager().getString(ResourceTokens.STRING_ADDED_KEY_TO_PERSON) + m.getSender());
					
				} catch (Exception e) {
					gui.showError(e.getMessage());
				}
			} else {
				gui.showError(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_WRONG_TYPE3));
			}
		}
		/*
	public void importKey() {
		if (this.getSelectedIndex()>=0){
			try {
				Address a = (Address) adresses.elementAt(this.getSelectedIndex());
				byte[] d=AddressService.findPublicKey4Address(a);
				if (d==null){
					gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_NO_PERSON_WITH_NUMBER)+a.getUrl());
					
				}else{
					AddressService.importKey4Url(a.getUrl(), d);
					gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_ADDED_KEY_TO_PERSON)+a.getUrl());
					buildList();
				}
			} catch (Exception e) {
				gui.showError(e.getMessage());
			}
		}
	}
	*/
	}

	public void deleteMessage() {
		if (this.getSelectedIndex()>=0){
			CMessage m = messages[this.getSelectedIndex()];
			try {
				AddressService.deleteMessage(m);
				buildList();
			} catch (Exception e) {
				gui.showError(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DELETE_MESSAGE));
			}
		}
	}

	void buildList() {
		Person[] pers;
		adresses.removeAllElements();

		try {
			pers = AddressService.getPersons();

			this.deleteAll();

			for (int i = 0; i < pers.length; i++) {
				if (pers[i].getConns().length > 0) {
					for (int j = 0; j < pers[i].getConns().length; j++) {
						adresses.addElement(pers[i].getConns()[j]);
						this.append(pers[i].getName() + gui.getResourceManager().getString(ResourceTokens.STRING_NUMBER_WITH_SPACES) + pers[i].getConns()[j].getUrl() + gui.getResourceManager().getString(ResourceTokens.STRING_KEY_1)
								+ pers[i].getConns()[j].hasKey(), listImage(pers[i].getConns()[j].hasKey()));
					}
				} else {
					this.append(pers[i].getName(), null);
				}
			}
		} catch (Exception e) {
			gui.showError(e.getMessage());
		}
	}
	private Image listImage(boolean hasKey){
		if (hasKey)
			return _key;
		else
			return _noKey;
	}

	public void sendSMS(Address a, Object messageText) {
		if (a.hasKey()){
			if(_messageText!=null){
				//send Message to given Address
				//TODO duplicated Code in MessageBox  solve via Command/Executor 
	
				Display d = Display.getDisplay(gui);
				d.setCurrent( new ProgressScreen(gui.getMenu() , gui.getResourceManager().getString(ResourceTokens.STRING_ENCRYPT_MESSAGE),d ){
				
				  public void run(){
					  Address ad = (Address) adresses.elementAt(getSelectedIndex());
					  this.setStatus("progressbar opens");
						try {
							 this.setStatus("encrypt");
						  byte[] data = CryptoHelper.encrypt(_messageText.getBytes(), ad.getPublicKey());

						  if (data == null) {
								throw new Exception("Aieeh ... encyption failed");
							}
							//Message to send
							CMessage mess = new CMessage(gui.getSettings().getNumber(), ad.getUrl(), MessageType.ENCRYPTED_TEXT_INBOX,
									data,new Date());
							
								if (MessageSender.getInstance() != null)
									MessageSender.getInstance().send(ad.getUrl(), mess.prepareForTransport());
							
//								AddressService.saveMessage(new CMessage(gui.getSettings().getNumber(), ad.getUrl(), MessageType.SYM_ENCRYPTED_TEXT_OUTBOX,
//										_message.getBytes(),new Date()));
							mess.setType(MessageType.SYM_ENCRYPTED_TEXT_OUTBOX);
							mess.setData(_messageText.getBytes());
							
							AddressService.saveMessage(mess);
					this.setStatus("progressbar closes");
						} catch (Exception e) {
							Logger.getInstance().write((gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_THREAD  + "\n"+ e.getMessage())));
							
						}finally{
							 stopThread();
						}
				   
				  }
				} );
				
				

				
			}else{
				gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_NO_MESSAGEBOX));
			}
		}else{
			gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_NO_KEY_FOR_SENDER));
		}
		}else
		{
			gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_SELECT_PLEASE));
		}
		
	}

	public void addEntry() {
		if (co == this.ok) {
			if(this.nameField.getString().length()<2 || this.addressField.getString().length()<7){
				gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_FORM_INCOMPLETE));
			}else{
			try {
				if (address == null) {
					AddressService.createPerson(this.nameField.getString(), this.addressField.getString());
					gui.setTicketText("added "+this.nameField.getString());
				} else {
					AddressService.alterName(address.getPerson(), this.nameField.getString());
					address.setUrl(addressField.getString());
					AddressService.updateAdress(address);
					gui.setTicketText("updated "+this.nameField.getString());
				}
				if(caller instanceof AddressList)
					((AddressList)caller).buildList();
				Display.getDisplay(gui).setCurrent(caller);
			} catch (Exception e) {
				gui.showError(e.getMessage());
			}
		}
			
	}

	public void editEntry() {
		if (this.getSelectedIndex()>=0){
		Display.getDisplay(gui).setCurrent(
				new AddressEntry(this, (Address) adresses.elementAt(this.getSelectedIndex()),gui));
		}
		if(this.nameField.getString().length()<2 || this.addressField.getString().length()<7){
			gui.showInfo(gui.getResourceManager().getString(ResourceTokens.STRING_FORM_INCOMPLETE));
		}else{
			try {
				if (address == null) {
					AddressService.createPerson(this.nameField.getString(), this.addressField.getString());
					gui.setTicketText("added "+this.nameField.getString());
				} else {
					AddressService.alterName(address.getPerson(), this.nameField.getString());
					address.setUrl(addressField.getString());
					AddressService.updateAdress(address);
					gui.setTicketText("updated "+this.nameField.getString());
				}
				if(caller instanceof AddressList)
					((AddressList)caller).buildList();
				Display.getDisplay(gui).setCurrent(caller);
			} catch (Exception e) {
				gui.showError(e.getMessage());
			}
		}
	}

	public void deleteEntry() {
		if (this.getSelectedIndex()>=0){
			try{
			Address a = (Address) adresses.elementAt(this.getSelectedIndex());
			AddressService.deleteAddress(a);
			}catch(Exception e){
				// System.out.println("aaaa"+e.getMessage());
				gui.showError(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_COULD_NOT_DELETE_ADDRESS));
			}
		buildList();
		}
	}

	public void readNewMessage() {
		if (this.getSelectedIndex()>=0){
		Address a = (Address) adresses.elementAt(this.getSelectedIndex());

		if (a.hasKey()) {
			Display.getDisplay(gui).setCurrent(new MessageBox(a, gui, this));
		} else {
			
			gui.showError(gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_ONLY_ENCRYPTED_SENDING));
		}
		}
	}

	public void sendPublikKey() {
		if (this.getSelectedIndex()>=0){
			try {
				
				Address a = (Address) adresses.elementAt(this.getSelectedIndex());
				byte[] data = gui.getSettings().getPublicKey();
				CMessage n = new CMessage(gui.getSettings().getNumber(), a.getUrl(), MessageType.KEY, data,new Date());
				// Connection.create(a.getType().getTypeCode()).send(a, n.prepareForTransport());
				if (MessageSender.getInstance() != null)
					MessageSender.getInstance().send(a.getUrl(), n.prepareForTransport());
				
			} catch (Exception e) {
				// System.out.println(e.toString());
				gui.showError(e.getMessage());
			}
		}
	}

}


/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.server;


import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;



import com.sun.kvem.midp.io.j2se.wma.client.PhoneNumberNotAvailableException;

import crymes.book.Address;
import crymes.book.AddressService;
import crymes.book.CMessage;
import crymes.book.MessageType;
import crymes.book.Person;
import crymes.book.Settings;
import crymes.crypto.Cipher;
import crymes.crypto.IKeyPairGenerator;
import crymes.crypto.KeyPair;
import crymes.crypto.Utils;
import crymes.server.communication.CIncomingMessageListener;
import crymes.server.communication.ISmsMessageListener;
import crymes.server.communication.MessageReader;
import crymes.server.communication.MessageSender;
import crymes.server.communication.Receiver;
import crymes.server.gui.MainFrame;
import crymes.server.handlers.GSMClient;
import crymes.server.handlers.IGenericClient;
import crymes.server.handlers.WtkClient;
import crymes.util.helpers.CryptedRecordStore;


import crymes.util.helpers.Logger;



 


@SuppressWarnings("unchecked")
public class Engine  implements ISmsMessageListener{
	
	@SuppressWarnings("serial")	
	
	private Cipher _cipher;
	private IKeyPairGenerator _keyPairGenerator;
	
	// key Sender, value Cookie
	private Hashtable<String, String> cookies = new Hashtable<String, String>();
	
	//private GUIControl _guiControl;
	private Receiver _receiver;
	private String _algorithm = "ECIES";
	private Logger log=null;
	
	CIncomingMessageListener _listener;

	private WtkClient wtkClient;
    private GSMClient gsmClient;
    
    private Vector<Address> addressesEnabled = new Vector<Address>();
    
	
	Vector adresses = new Vector();
    private Settings _settings;
    private MainFrame _mainFrame; 
    
    private boolean useGSMModem = true;
    
    //public Engine(GUIControl gc, MainFrame mf) {
	public Engine(MainFrame mf) {
		log = Logger.getInstance();
		
		_mainFrame = mf;
		//_guiControl = gc;
		_receiver = Receiver.getInstance();
		
		
		_cipher = Utils.GetCipher(_algorithm);
		_keyPairGenerator = Utils.GetKeyPairGenerator(_algorithm);
		
		
		_listener = new CIncomingMessageListener(this);
		log.write("CIncomingMessageListener create");
	}
	
	public void sendSms (crymes.util.wma.BinaryMessage m){
		String internationPrefix = m.getAddress().substring(6,9);
		
		IGenericClient gClient = null;
	    if (internationPrefix.compareTo("+55") == 0){
	    	gClient = wtkClient;	    	
	    } else {
	    	gClient = gsmClient;
	    }
		
	    boolean sent = false;
	    try {
	    	//Helpers.dump("MAU MSG", m.getPayloadData());
	    	 Logger.getInstance().write("Payload size: " + m.getPayloadData().length);
			sent = gClient.send(m);
		} catch (IOException e) {
			 Logger.getInstance().write("Exception sending sms.");
		}
		
		if (sent){			
			try {
				
				CMessage m2 = CMessage.createFromTransport(m);
				AddressService.saveOutgoingMessage(m2);
				addRowToMessageList(m2);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getInstance().write("Cannot show the sms sent in the table");
				//e.printStackTrace();
			}
			
		}
	}
	
	
	
	// Callback dell'ISmsMessageListener
	public void notifyIncomingMessage(crymes.util.wma.BinaryMessage msg, boolean isEmulator)  {
		
//		IGenericClient gClient = null;
//	    if (isEmulator){
//	    	gClient = wtkClient;
//	    	
//	    } else {
//	    	gClient = gsmClient;
//	    }
	
	    _receiver.notifyIncomingMessage(msg);
	}


	// Tenta la connessione al client wireless
	public boolean createWTKConnection(){
		boolean b = true;
		try {
			wtkClient = new WtkClient(_listener);
			//CSettings.setEmulatorPhoneNumber(wtkClient.getPhoneNumber());
			log.write("Emulator connected");
		} catch (IOException e) {
			log.write("Impossibile connettersi all'emulatore");
			wtkClient =  null;
			b = false;
		} catch (PhoneNumberNotAvailableException e) {
			log.write("Impossibile specificare il numero telefonico");
			wtkClient =  null;
			b = false;
		} catch (Exception e) {
			log.write("Impossibile connettersi all'emulatore");
			wtkClient =  null;
			b = false;
		}
		return b;
	}

	// Tenta la connessione al modem gsm
	public boolean createGSMModemConnection(){
		boolean b = true;
		String str = "GSMModem connected on " + CSettings.getGsmModemComPort();
	
		if (!useGSMModem)
			return false;
		gsmClient = new GSMClient(_listener); //cell
		try {
			gsmClient.connect();
			//throw new Exception("simulated");
			//CSettings.setGsmModemPhoneNumber(gsmClient.getPhoneNumber());
			log.write(str);	
		} catch (javax.comm.NoSuchPortException e) {
			str = "GSMClient: NoSuchPortException. Insert the modem. ";
			int r = JOptionPane.showConfirmDialog(null,"GSM Modem isn't inserted. If you want use the GSM close and restart the server after inserting the modem.\n\nDo you want use the GSM modem?","SMS Server", JOptionPane.YES_NO_OPTION );
			if (r==JOptionPane.YES_OPTION){
				System.exit(0);
			} else {
				useGSMModem = false;
			}
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
		else {
			gsmClient = null;
			log.write(str);
		}
		
	
		return b;
	}

	public WtkClient getWtkClient(){
		return wtkClient;
	}
	
	public GSMClient getGSMClient(){
		return gsmClient;
	}
	
	
	public void initAddressService(String pass) throws Exception {
		CryptedRecordStore.init(pass);
		AddressService.init(this);
		_settings = AddressService.getSetting();
		if (_settings!=null){
			CSettings.setNumberSMSC_GSM(_settings.getSMSCNumber());
		}
		//startPPTimer();
	}
	
	public Settings getSettings() {
		return _settings;
	}

	public void setSettings(Settings s) {
		this._settings = s;
	}
	
	/**
	 * do the final steps to initialization, meaning, start the services after our
	 * settings object is in place.
	 *
	 */
	public void finishInit() {
		MessageReader.init();
		MessageSender.init(this);	
		
	}
	
	/* PROBLEMA: noi abbiamo due numeri attivi sul server... */
	public void doSetup( String name, String gsmNumber, String emuNumber, String passphrase, String repeatPass){
		
		try {
			initAddressService(passphrase);
		
		// progress bar con il messaggio "Generating keypair..."
		// d.setCurrent( new ProgressScreen(midlet.getMenu(), midlet.getResourceManager().getString(ResourceTokens.STRING_GENERATING_KEYPAIR),d ){
		final String n = name;
		final String gsm = gsmNumber;
		
			Runnable p = new Runnable() {
				public void run(){
					// this.setStatus("progressbar opens");
					try {
						
						
						
						KeyPair k = _keyPairGenerator.generateKeyPair();
						Settings s=AddressService.saveSettings(_algorithm, n, gsm, k, gsm);
						setSettings(s);
						// this.setStatus("progressbar closes");				    
						finishInit();
						_mainFrame.setActiveTabs(false);
						_mainFrame.startPassphraseTimer();
					} catch (Exception e) {						
						Logger.getInstance().write("Error while starting thread: "  + "\n"+ e.getMessage());					
					} finally{
		//				stop();
					}
				}
			};
		new Thread(p).start();
		MainFrame.startGUIControl();
		
		//AddressService.initSettings(name.getString(), number.getString(), CryptoHelper.generateKeyPairHelper());
		//midlet.showMenu();
	} catch (Exception e) {
		e.printStackTrace();
	}
	
}
	
	/* metodi relativi all'addresslist */
	
	@SuppressWarnings("unchecked")
	public Vector buildList() {
		Person[] pers;
		adresses.removeAllElements();

		try {
			pers = AddressService.getPersons();

			for (int i = 0; i < pers.length; i++) {
				if (pers[i].getConns().length > 0) {
					for (int j = 0; j < pers[i].getConns().length; j++) {
						adresses.addElement(pers[i].getConns()[j]);
//						this.append(pers[i].getName() + gui.getResourceManager().getString(ResourceTokens.STRING_NUMBER_WITH_SPACES) + pers[i].getConns()[j].getUrl() + gui.getResourceManager().getString(ResourceTokens.STRING_KEY_1)
//								+ pers[i].getConns()[j].hasKey(), listImage(pers[i].getConns()[j].hasKey()));
					}
				} else {
					//this.append(pers[i].getName(), null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return adresses;
	}
	
	public Vector buildListAddressesEnabled() {
		Person[] pers;
		addressesEnabled.removeAllElements();

		try {
			pers = AddressService.getPersons();

			for (int i = 0; i < pers.length; i++) {
				if (pers[i].getConns().length > 0) {
					for (int j = 0; j < pers[i].getConns().length; j++) {
						if (pers[i].getConns()[j].isKeyIsValid()){
							addressesEnabled.addElement(pers[i].getConns()[j]);	
						}
					}
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return addressesEnabled;
	}
	
	public void addEntry(String name, String address) {
		try {
			AddressService.createPerson(name, address);
			Logger.getInstance().write("Added " + name + " to address book");
		} catch (Exception e) {
			Logger.getInstance().write("Error: cannot add " + name + "to address book");
		}
		//MAU_mainFrame.updateRecipientList();
		_mainFrame.updateAddressListTable();
	} // end addEntry
	
	
	public void deleteEntry(int row) {

		String name = "";
		try {
			
			Address a = (Address) adresses.elementAt(row);
			name = a.getPerson().getName();
			AddressService.deleteAddress(a);
			Logger.getInstance().write("Deleted address entry of " + name);
		} catch (Exception e) {
			Logger.getInstance().write("Error: cannot delete " + name + "from address book");
		}
		//MAU_mainFrame.updateRecipientList();
		_mainFrame.updateAddressListTable();

	}
	
	
	public void editEntry(int row, String name, String addressStr) {

		try {
			Address address = (Address) adresses.elementAt(row);
			AddressService.alterName(address.getPerson(), name);
			address.setUrl(addressStr);
			AddressService.updateAdress(address);
			Logger.getInstance().write("Updated address entry of " + name);
		} catch (Exception e) {
			Logger.getInstance().write("Error: cannot update " + name + "in address book");
		}
		//MAU_mainFrame.updateRecipientList();
		_mainFrame.updateAddressListTable();

	}
	
	public void PK_Register(CMessage m) {

		if (m.getType() == MessageType.PK_REGISTER_REQUEST ) {


			try {

				AddressService.importKey4Url(m.getSender(), m.getData());
				

				Logger.getInstance().write("key (not verified) added to "+m.getSender());
			} catch (Exception e) {
				Logger.getInstance().write("key added error to "+m.getSender());
				return;
			}


			try { 
				Address a = AddressService.findAdress4Url(m.getSender());
				byte [] pk = AddressService.findPublicKey4Address(a);

				String cookie = makeCookie(a.getUrl());

				//byte[] data = CryptoHelper.encrypt(cookie.getBytes(), pk);
				KeyPair kp = _keyPairGenerator.getKeyPairFromABytePair(pk, null);
				
				byte[] data = _cipher.encrypt(cookie.getBytes(), kp);
				if (data == null) {	
					throw new Exception("Aieeh ... encyption failed");
				}

				String SMSC;
				if (a.getUrl().substring(0, 3).compareTo("+55") == 0)
					SMSC = CSettings.getNumberSMSC_WTK();
				else
					SMSC = CSettings.getNumberSMSC_GSM();

				
				CMessage challenge = new CMessage(SMSC, a.getUrl(), MessageType.PK_SERVER_CHALLENGE, data, new Date());


				if (MessageSender.getInstance() != null)
					MessageSender.getInstance().send(a.getUrl(), challenge.prepareForTransport());

			} 
			catch (Exception e) {	
				Logger.getInstance().write(e.getMessage());				
			}



			_mainFrame.updateAddressListTable();
			

		} else {
			log.write("CMessage is not a PK_Register");
		}
	}
	
	
	
	public void completeRegistration(CMessage m){
		if (m.getType() == MessageType.PK_CHALLENGE_RESPONSE ) {

			Address a;
			try {
				a = AddressService.findAdress4Url(m.getSender());


				String cookie = getCookie(a.getUrl());

				HMac mac = new HMac(new SHA1Digest());
				mac.init(new KeyParameter(cookie.getBytes()));

				String challengeResp = new String("Decriptato con successo");
				mac.update(challengeResp.getBytes(), 0, challengeResp.getBytes().length);
				byte[] HMACResp = new byte[mac.getUnderlyingDigest().getDigestSize()];
				mac.doFinal(HMACResp, 0);



				byte[] aaaa = challengeResp.getBytes();
				byte[] lastMess = new byte[aaaa.length + HMACResp.length];
				System.arraycopy(aaaa, 0, lastMess, 0, aaaa.length);
				System.arraycopy(HMACResp, 0, lastMess, aaaa.length, HMACResp.length);


				boolean equal = Arrays.equals(m.getData(), lastMess);


				if (equal){
					AddressService.setKeyValid4Url(a.getUrl(), true);
					String str = "Il processo di registrazione è avvenuto con successo. Da questo momento in poi tutte le comunicazioni saranno cifrate. JWhisper A2P";
					addressesEnabled.add(a);
					
					log.write("Public Key verified for "+ a.getUrl());
					sendAMessage(a, str, MessageType.A2P_WELCOME_MSG);
				} else {
					AddressService.setKeyValid4Url(m.getSender(), false);	
					//addressesEnabled.remove(a);
					log.write("Challenge lose");
				}
			} catch (Exception e) {
				Logger.getInstance().write("Error: " + e.getMessage());
			}


		} else {

			log.write("CMessage is not a PK_CHALLENGE_RESPONSE");
		}

		_mainFrame.updateAddressListTable();
	}


	
	public void sendAMessage(String recipient, String msg) {

		Address a;
		try {
			a = AddressService.findAdress4Url(recipient);
		} catch (Exception e) {
			log.write("Address with url '"+recipient+"' is not registered");
			return;
		}
		sendAMessage(a, msg, MessageType.ENCRYPTED_TEXT_INBOX);
	}
	
	
	public void sendAMessage(Address a, String msg, byte messageType) {
		String SMSC;
		if (a.getUrl().substring(0, 3).compareTo("+55") == 0)
			SMSC = CSettings.getNumberSMSC_WTK();
		else
			SMSC = CSettings.getNumberSMSC_GSM();
		
		try {
		//byte[] pk = AddressService.findPublicKey4Address(a);
		
		KeyPair kp = _keyPairGenerator.getKeyPairFromABytePair(a.getPublicKey(), null);
			
		
		byte[] data = _cipher.encrypt(msg.getBytes(), kp);
		if (data == null) {	
			Logger.getInstance().write("Aieeh ... encyption failed");
			return;
		}
		
		
		CMessage mess = new CMessage(SMSC, a.getUrl(), messageType , data , new Date());
		
		
		if (MessageSender.getInstance() != null)
				MessageSender.getInstance().send(a.getUrl(), mess.prepareForTransport());

//		mess.setType(MessageType.SYM_ENCRYPTED_TEXT_OUTBOX);
//		mess.setData(msg.getBytes());
//		
//		AddressService.saveMessage(mess);
		
		} catch (IOException e) {
			Logger.getInstance().write(e.getMessage());
		} catch (Exception e1) {
			Logger.getInstance().write(e1.getMessage());
		}
	}
	
	private String makeCookie(String key){
		String str = "";
		StringBuffer sb = new StringBuffer();
		java.util.Random r = new java.util.Random();

		for(char c='A'; c<='Z'; c++) 
			sb.append(c);
//		for(char c='a'; c<='z'; c++) 
//			sb.append(c);
		for(int j=0; j<=10; j++) 
			sb.append(j);
		for(int j=0; j<48; j++)
			str +=sb.charAt(r.nextInt(sb.length()) );
	
	
		cookies.put(key, str);
		return str;
	}
	
	private String getCookie(String key){
		
		return cookies.remove(key);
	}
	
	public void addRowToMessageList(CMessage m){
		_mainFrame.addRowToMessageList(m);
	}
	
}


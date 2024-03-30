/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.modules.addressBook;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotOpenException;

import jwhisper.Constant;
import jwhisper.JWhisperMidlet;
import jwhisper.Settings;
import jwhisper.SettingsA2P;
import jwhisper.SettingsP2P;
import jwhisper.crypto.KeyPair;
import jwhisper.modules.message.CMessage;
import jwhisper.modules.message.MessageType;
import jwhisper.modules.message.MessageTypeFilter;
import jwhisper.modules.recordStore.CacheEnumeration;
import jwhisper.modules.recordStore.CryptedRecordEnumeration;
import jwhisper.modules.recordStore.CryptedRecordStore;
import jwhisper.utils.CountryCodeStore;
import jwhisper.utils.ResourceTokens;


/*
 * AddressService gestisce gli accessi ai RecordStore attraverso una 
 * classe Singleton.
 * 
 */

public class AddressService {
	private static JWhisperMidlet _midlet;

	private static final String STORE_PERSON_A2P = "jwhisper-A2P-adressbook";
	private static final String STORE_NUMBER_A2P = "jwhisper-A2P-numbers";
	private static final String STORE_MESSAGE_A2P = "jwhisper-A2P-messages";
	private static final String STORE_SETTING_A2P = "jwhisper-A2P-settings";
	
	private static final String STORE_PERSON_P2P = "jwhisper-P2P-adressbook";
	private static final String STORE_NUMBER_P2P = "jwhisper-P2P-numbers";
	private static final String STORE_MESSAGE_P2P = "jwhisper-P2P-messages";
	private static final String STORE_SETTING_P2P = "jwhisper-P2P-settings";

	
	private static String STORE_PERSON; 
	private static String STORE_NUMBER;
	private static String STORE_MESSAGE;
	private static String STORE_SETTING;
	
	private static AddressService _addressService = null;
	private static CryptedRecordStore _persons;

	private static CryptedRecordStore _numbers;

	private static CryptedRecordStore _messages;

	private static CryptedRecordStore _settings;

	/*
	 * Singleton Pattern
	 */
	private AddressService() {
	}

	/*
	 * I RecordStore vengono aperti e creati
	 */
	public static void init(JWhisperMidlet midlet) throws Exception {
		if (_addressService == null) {

			if (midlet.getApplicationType() == Constant.APPLICATION_TYPE_A2P){
				STORE_PERSON = STORE_PERSON_A2P;
				STORE_SETTING = STORE_SETTING_A2P;
				STORE_MESSAGE = STORE_MESSAGE_A2P;
				STORE_NUMBER = STORE_NUMBER_A2P;
			} else if (midlet.getApplicationType() == Constant.APPLICATION_TYPE_P2P){
				STORE_PERSON = STORE_PERSON_P2P;
				STORE_SETTING = STORE_SETTING_P2P;
				STORE_MESSAGE = STORE_MESSAGE_P2P;
				STORE_NUMBER = STORE_NUMBER_P2P;
			}
			_settings = CryptedRecordStore.open(STORE_SETTING, true);
			_persons  = CryptedRecordStore.open(STORE_PERSON, true);
			_numbers  = CryptedRecordStore.open(STORE_NUMBER, true);
			_messages = CryptedRecordStore.open(STORE_MESSAGE, true);

			_midlet = midlet;

			_addressService = new AddressService();

		}
	}



	/* Operazioni su Settings 
	 * ***************************************** */


	public static Settings getSetting() throws Exception, RecordStoreNotOpenException, RecordStoreException {
		Settings sett = null;
		try {
			CryptedRecordEnumeration e = _settings.enumerateCryptedRecords(null, null, false);

			while (e.hasNextElement()) {
				
				sett = Settings.init(e.nextRecord());
			}
		} catch (Exception e) {
			RecordStore.deleteRecordStore(_settings.getName());
			_settings = null;
		}
		
		
		return sett;
	}

	public static boolean existSettings() throws Exception {
		boolean out = false;
		if (CryptedRecordStore.isEmpty(AddressService.STORE_SETTING)) {
			out = false;
		} else {
			out = true;
		}
		return out;
	}

	public static Settings saveSettingsP2P(String alg, String name, String number, KeyPair pair) throws Exception,
	RecordStoreFullException, RecordStoreException, Exception {
		Settings s = SettingsP2P.init(alg, name, number, pair);
		
		_settings.addRecord(s.toBytes());
		return s;
	}

	public static Settings saveSettingsA2P(String alg, String name, String number, KeyPair pair, String smscNumber)throws Exception,
	RecordStoreFullException, RecordStoreException, Exception {
		Settings s = SettingsA2P.init(alg, name, number, pair, smscNumber);
		
		_settings.addRecord(((SettingsA2P)s).toBytes());
		return s;
	}
	
	public static void saveSettingsA2P(SettingsA2P s)throws Exception,
	RecordStoreFullException, RecordStoreException, Exception {

		int [] r = _settings.getRecordIds();
		for (int i=0; i < r.length ; i++)
			_settings.deleteRecord(r[i]);
		
		_settings.addRecord(((SettingsA2P)s).toBytes());
		
	}
	
	public static void destroySettings() throws Exception, RecordStoreException {
		if (Settings.getSettings() != null)
			Settings.getSettings().destroy();
	}

	/* Operazioni su Person 
	 * ***************************************** */


	/*
	 * Create a person and first Address 
	 */
	public static void createPerson(String name, String url) throws Exception {
		if (existsPerson(name)) {
			throw (new
					Exception(_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_PERSON_ALREADY_EXISTS)));
		} else {
			if ((name != null) && (!"".equals(name)) && (url != null) && (!"".equals(url))) {
				byte[] ba = name.getBytes();
				int rmdId = _persons.getNextRecordId();
				_persons.addRecord(ba);
				Person p = new Person(name, rmdId);
				Address[] a = { createAdress4Person(p, url) };
				p.setAddresses(a);
			}
		}
	}

	private static boolean existsPerson(String name) throws Exception {
		boolean exists = false;
		Enumeration e = getPersonV().elements();

		while (e.hasMoreElements()) {
			Person p = (Person) e.nextElement();

			if (p.getName().equals(name)) {
				exists = true;
				break;
			}
		}
		return exists;
	}


	public static Person[] getPersons() throws Exception {
		Vector p = getPersonV();
		Person[] ps = new Person[p.size()];
		p.copyInto(ps);

		return ps;
	}


	private static Vector getPersonV() throws Exception {
		Vector p = new Vector();

		CacheEnumeration re = _persons.enumerateCache(null, new PersonComparator());

		while (re.hasNextElement()) {
			Person _person = getPerson(re.nextRecordId());
			_person.setAddresses(getAdresses4Person(_person));
			p.addElement(_person);
		}
		return p;
	}


	public static void alterName(Person p, String newName) throws Exception {
		if (!p.getName().equals(newName)) {
			if (existsPerson(newName)) {
				throw (new Exception("Person with name '"+newName+"' already exists"));
			}
			_persons.setRecord(p.getRmsID(), newName.getBytes());
		}
	}

	public static Person getPerson(int id) throws Exception, InvalidRecordIDException, RecordStoreException {
		Person p = null;
		p = new Person(new String(_persons.getRecord(id)), id);
		return p;
	}

	private static void deletePerson(Person p) throws Exception {
		_persons.deleteRecord(p.getRmsID());
	}

	/* Operazioni su Number 
	 * ***************************************** */

	/*
	 * create an Adress for a Person
	 */
	public static Address createAdress4Person(Person p, String n) throws Exception {
		int rmsID = _numbers.getNextRecordId();

		Address a = new Address(rmsID, n, p);
		byte[] ba = a.getBytes();
		_numbers.addRecord(ba);

		return a;
	}

	/*
	 * Ricerca tutti gli address che appartengono a person
	 */
	public static Address[] getAdresses4Person(Person p) throws Exception {
		Vector as = new Vector();
		RecordEnumeration e = _numbers.enumerateCache(new AddressPersonFilter(p), null); 

		Address[] adresses = new Address[e.numRecords()];

		while (e.hasNextElement()) {
			as.addElement(new Address(e.nextRecord()));
		}

		as.copyInto(adresses);

		return adresses;
	}


	public static void updateAdress(Address a) throws Exception, InvalidRecordIDException, RecordStoreFullException,
	RecordStoreException, Exception {
		_numbers.setRecord(a.getRmsID(), a.getBytes());
	}


	public static Address findAdress4Url(String url) throws Exception {
		Vector as = new Vector();
		RecordEnumeration e = _numbers.enumerateCache(new AddressUrlFilter(url), null);

		while (e.hasNextElement()) {
			as.addElement(new Address(e.nextRecord()));
		}

		if (as.size() > 1)
			throw new Exception("Persons with number "+url+"  in your address book are more than one (" + as.size() + ")");

		if (as.isEmpty())
			return null;
		else
			return (Address) as.firstElement();
	}

	public static void deleteAddress(Address a) throws Exception {
		Person p = a.getPerson();
		if (getAdresses4Person(p).length == 1) {
			deletePerson(p);
		}
		_numbers.deleteRecord(a.getRmsID());
	}





	/* Operazioni su Message 
	 * ***************************************** */

	public static synchronized void saveMessage(CMessage m) throws Exception, RecordStoreFullException, RecordStoreException {
		if (m != null) { 
			if (m.getTime() == null)
				m.setTime(new Date());

			byte[] encodedMessage = m.getBytes();

			_messages.addRecord(encodedMessage);
		}
	}


	public static void deleteMessage(CMessage m) throws Exception, RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
		_messages.deleteRecord(m.getRmsID());
	}

	public static CMessage[] getInboxUnreadMessages() throws Exception {
		RecordEnumeration e = _messages.enumerateCache(new MessageTypeFilter(MessageType.ENCRYPTED_TEXT_INBOX), null);
		return getMessagesFromEnumeration(e);
	}
	
	public static CMessage[] getInboxWelcomeMessages() throws Exception {
		RecordEnumeration e = _messages.enumerateCache(new MessageTypeFilter(MessageType.A2P_WELCOME_MSG), null);
		return getMessagesFromEnumeration(e);
	}

	public static CMessage[] getInboxReadMessages() throws Exception {
		RecordEnumeration e = _messages.enumerateCache(new MessageTypeFilter(MessageType.SYM_ENCRYPTED_TEXT_INBOX), null);
		return getMessagesFromEnumeration(e);
	}

	public static CMessage[] getPKServerChallenge() throws Exception {
		RecordEnumeration e = _messages.enumerateCache(new MessageTypeFilter(MessageType.PK_SERVER_CHALLENGE), null);
		return getMessagesFromEnumeration(e);
	}

	public static CMessage[] getOutboxMessages() throws Exception {
		RecordEnumeration e = _messages.enumerateCache(new MessageTypeFilter(MessageType.SYM_ENCRYPTED_TEXT_OUTBOX), null);
		return getMessagesFromEnumeration(e);
	}

	public static CMessage[] getPKRegisterRequest() throws Exception {
		RecordEnumeration e = _messages.enumerateCache(new MessageTypeFilter(MessageType.PK_REGISTER_REQUEST), null);
		return getMessagesFromEnumeration(e);
	}

	public static CMessage[] getAllMessages() throws Exception {
		RecordEnumeration e = _messages.enumerateCache(null, null);
		return getMessagesFromEnumeration(e);
	}

	public static CMessage[] getPublicKeys() throws Exception {
		RecordEnumeration e = _messages.enumerateCache(new MessageTypeFilter(MessageType.KEY), null);
		return getMessagesFromEnumeration(e);
	}

	private static CMessage[] getMessagesFromEnumeration(RecordEnumeration e) throws Exception {
		Vector v = new Vector();
		CMessage[] n = new CMessage[e.numRecords()];
		int rmsID;
		while (e.hasNextElement()) {
			rmsID = e.nextRecordId();
			try {
				v.addElement(new CMessage(_messages.getRecord(rmsID), rmsID));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
			}
		}
		v.copyInto(n);
		return n;
	}




	/*
	 * Deletes all recordstores
	 */
	public static void deleteStores() throws Exception, RecordStoreException {
		if (_numbers != null)
			_numbers.closeCryptedRecordStore();
		if (_persons != null)
			_persons.closeCryptedRecordStore();
		if (_settings != null)
			_settings.closeCryptedRecordStore();
		if (_messages != null)
			_messages.closeCryptedRecordStore();
		RecordStore.deleteRecordStore(STORE_NUMBER);
		RecordStore.deleteRecordStore(STORE_PERSON);
		RecordStore.deleteRecordStore(STORE_MESSAGE);
		RecordStore.deleteRecordStore(STORE_SETTING);
	}


	public static void importKey4Url(String url, byte[] data) throws Exception, RecordStoreFullException,
	RecordStoreException {
		Address a = findAdress4Url(url);
		if (a == null) {
			throw new Exception(_midlet.getResourceManager().getString(
					ResourceTokens.STRING_ERROR_NO_PERSON_WITH_NUMBER)
					+ url);
		}
		a.setPublicKey(data);
		updateAdress(a);
	}

	public static byte[] findPublicKey4Address(Address a) throws Exception {
		CMessage[] m = getPublicKeys();
		byte[] d = null;
		for (int i = 0; i < m.length; i++) {
			// if (a.getUrl().equals(m[i].getSender())){
			if (CountryCodeStore.compareNumbers(a.getUrl(), (m[i].getSender()))) {
				d = m[i].getData();
				break;
			}
		}
		return d;

	}



}
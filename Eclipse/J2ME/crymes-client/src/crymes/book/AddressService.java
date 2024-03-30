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
package crymes.book;

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


import crymes.gui.CrymesMidlet;
import crymes.helpers.CacheEnumeration;
import crymes.helpers.CountryCodeStore;
import crymes.helpers.CryptedRecordEnumeration;
import crymes.helpers.CryptedRecordStore;
import crymes.helpers.KeyPairHelper;
import crymes.helpers.ResourceTokens;

/**
 * TODO better exception handling
 * 
 * Singleton that synchronizes domain objects and record stores. Updates and
 * fetches of domain objects.
 * 
 * @author sd
 */
public class AddressService {
	private static CrymesMidlet _midlet;

	private static final String STORE_PERSON = "jwhisper-a2p-adressbook";

	private static final String STORE_NUMBER = "jwhisper-a2p-numbers";

	private static final String STORE_MESSAGE = "jwhisper-a2p-messages";

	private static final String STORE_SETTING = "jwhisper-a2p-settings";

	private static AddressService _adressService = null;

	private static CryptedRecordStore _persons;

	private static CryptedRecordStore _numbers;

	private static CryptedRecordStore _messages;

	private static CryptedRecordStore _settings;

	/**
	 * Private Constructor of the singleton
	 */
	private AddressService() {
	}

	/**
	 * Inits the singleton AdressService. All Recordstores are opened or
	 * created. The intialised field gui is used for showing Exceptions in the
	 * User Interface
	 * 
	 * @param gui
	 *            the main class of the user interface
	 * 
	 * @throws Exception
	 *             for RecordStores
	 */

	public static void init(CrymesMidlet midlet) throws Exception {
		if (_adressService == null) {

			_settings = CryptedRecordStore.open(AddressService.STORE_SETTING, true);
			_persons  = CryptedRecordStore.open(AddressService.STORE_PERSON, true);
			_numbers  = CryptedRecordStore.open(AddressService.STORE_NUMBER, true);
			_messages = CryptedRecordStore.open(AddressService.STORE_MESSAGE, true);

			_midlet = midlet;

			_adressService = new AddressService();

		}
	}

	/**
	 * Deletes the Settings recordstore.
	 * 
	 * @throws RecordStoreException
	 * @throws Exception
	 */
	public static void destroySettings() throws Exception, RecordStoreException {
		if (Settings.findSettings() != null)
			Settings.findSettings().destroy();
	}

	/**
	 * Deletes all recordstores
	 * 
	 * @throws RecordStoreException
	 * @throws Exception
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
		RecordStore.deleteRecordStore(AddressService.STORE_NUMBER);
		RecordStore.deleteRecordStore(AddressService.STORE_PERSON);
		RecordStore.deleteRecordStore(AddressService.STORE_MESSAGE);
		RecordStore.deleteRecordStore(AddressService.STORE_SETTING);
	}

	/**
	 * Creates a person and his/her first Adress for a name and phonenumber
	 * 
	 * @param name
	 *            name of the person
	 * @param url
	 *            phonenumber
	 * 
	 * @throws Exception
	 *             recordstore - throws an Exception if a Person with the name
	 *             already exists
	 * 
	 * TODO: after Exception, return to Adressbook gui
	 */
	public static void createPerson(String name, String url) throws Exception {
		if (existsPerson(name)) {
			// TODO ResourceMAnager static?!
			// throw (new
			// Exception(CrymesMidlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_PERSON_ALREADY_EXISTS)));
		} else {
			if ((name != null) && (!"".equals(name)) && (url != null) && (!"".equals(url))) {
				byte[] ba = name.getBytes();
				int rmdId = _persons.getNextRecordId();
				_persons.addRecord(ba);// .addRecord(ba, 0, ba.length);

				Person p = new Person(name, rmdId);
				Address[] a = { createAdress4Person(p, url) };
				p.setConns(a);
			}
			// else: called with insufficient arguments - what to do?
		}
	}

	/**
	 * creates an Adress for a Person
	 * 
	 * @param p
	 *            person
	 * @param n
	 *            phonenumber
	 * 
	 * @return the created Adress
	 * 
	 * @throws Exception
	 *             recordstore
	 */
	public static Address createAdress4Person(Person p, String n) throws Exception {
		int rmsID = _numbers.getNextRecordId();
		// Address a = new Address(rmsID, n,
		// Connection.create(ConnectionType.SMS), p);
		Address a = new Address(rmsID, n, p);
		byte[] ba = a.getBytes();
		_numbers.addRecord(ba);

		return a;
	}

	/**
	 * Looks up all Adress objects that belong to a person
	 * 
	 * @param p
	 *            person
	 * 
	 * @return Array Adress
	 * 
	 * @throws Exception
	 *             recordstore
	 */
	public static Address[] getAdresses4Person(Person p) throws Exception {
		Vector as = new Vector();
		RecordEnumeration e = _numbers.enumerateCache(new AddressPersonFilter(p), null); // new
		// AdressPersonFilter(p)
		Address[] adresses = new Address[e.numRecords()];

		while (e.hasNextElement()) {
			as.addElement(new Address(e.nextRecord()));
		}

		as.copyInto(adresses);

		return adresses;
	}

	/**
	 * Checks if a person with the given name exists already
	 * 
	 * @param name
	 * 
	 * @return boolean
	 * @throws Exception
	 */
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

	/**
	 * Returns all persons in the Person recordstore
	 * 
	 * @return Array Persons
	 * @throws Exception
	 */
	public static Person[] getPersons() throws Exception {
		Vector p = getPersonV();
		Person[] ps = new Person[p.size()];
		p.copyInto(ps);

		return ps;
	}

	/**
	 * Private Method that returns a Vector of all Persons in the recordstore
	 * 
	 * @return Vector
	 * @throws Exception
	 * 
	 * @exception if
	 *                the recordstore does not work
	 */
	private static Vector getPersonV() throws Exception {
		Vector p = new Vector();

		// RecordEnumeration re = _persons.enumerateCache(null, null );
		//
		// while (re.hasNextElement()) {
		// Person _person = getPerson(re.nextRecordId());
		// _person.setConns(getAdresses4Person(_person));
		// p.addElement(_person);
		// }
		CacheEnumeration re = _persons.enumerateCache(null, new PersonComparator());

		while (re.hasNextElement()) {
			Person _person = getPerson(re.nextRecordId());
			_person.setConns(getAdresses4Person(_person));
			p.addElement(_person);
		}
		return p;
	}

	/**
	 * Alters the name of a person
	 * 
	 * @param p
	 *            Person
	 * @param newName
	 * 
	 * @throws Exception
	 *             if a Person with the name exists already
	 */
	public static void alterName(Person p, String newName) throws Exception {
		if (!p.getName().equals(newName)) {
			if (existsPerson(newName)) {
				throw (new Exception("A person by this name already exists"));
			}
			_persons.setRecord(p.getRmsID(), newName.getBytes());
		}
	}

	/**
	 * Saves a Message to the recordstore
	 * 
	 * @param data
	 *            the Message TODO i18n
	 * @throws RecordStoreException
	 * @throws RecordStoreFullException
	 * @throws Exception
	 */
	// public static void saveMessage(byte[] data) throws Exception,
	// RecordStoreFullException, RecordStoreException {
	// _messages.addRecord(data, 0, data.length);
	// }
	public static synchronized void saveMessage(CMessage m) throws Exception, RecordStoreFullException, RecordStoreException {
		if (m != null) { // only act if message is there
			if (m.getTime() == null)
				m.setTime(new Date());
			byte[] encodedMessage = m.getBytes();
			// decided to store all messages in the crypted store to decure
			// address etc.
			// if(m.getType()==MessageType.ENCRYPTED_TEXT_INBOX){
			// //encryptedmessages are stored in a 'normal' Recordstore
			// _cryptedInbox.addRecord(encodedMessage, 0,
			// encodedMessage.length);
			// }else{
			// not encrytpted keys and messages are stored in a
			// CryptedRecordstore
			_messages.addRecord(encodedMessage);
		}
		// }
	}

	/**
	 * returns the Settings of the application
	 * 
	 * @return Settings object
	 * @throws RecordStoreException
	 * @throws RecordStoreNotOpenException
	 * @throws Exception
	 * 
	 * @exception recordstore
	 */
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
			// TODO ResourceMAnager static?!
			// throw new
			// Exception(CryptoSMSMidlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_LOADING_SETTINGS_FAILED));
		}

		return sett;
	}

	/**
	 * Checks if the Setting object of application exists
	 * 
	 * @return Setting object
	 * @throws Exception
	 * 
	 * @exception recordstore
	 */
	public static boolean existSettings() throws Exception {
		boolean out = false;
		if (CryptedRecordStore.isEmpty(AddressService.STORE_SETTING)) {
			out = false;
		} else {
			out = true;
		}
		return out;
	}

	/**
	 * saves the initial Settings
	 * 
	 * @param name
	 *            username
	 * @param number
	 *            phonenumber
	 * @param pair
	 *            KeyPair
	 * @throws Exception
	 * @throws RecordStoreException
	 * @throws RecordStoreFullException
	 * @throws Exception
	 * 
	 * @exception recordstore
	 */
	public static Settings saveSettings(String name, String number, KeyPairHelper pair, String smscNumber) throws Exception,
			RecordStoreFullException, RecordStoreException, Exception {
		Settings s = Settings.init(name, number, pair, smscNumber);
		_settings.addRecord(s.toBytes());
		return s;
	}

	public static void deleteMessage(CMessage m) throws Exception, RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
		_messages.deleteRecord(m.getRmsID());
	}

	/**
	 * returns all Messages of the inbox
	 * 
	 * @return Array Message
	 * 
	 * @throws Exception
	 *             recordstore
	 */
	public static CMessage[] getInboxUnreadMessages() throws Exception {

		RecordEnumeration e = _messages.enumerateCache(new MessageTypeFilter(MessageType.ENCRYPTED_TEXT_INBOX), null);

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

	/**
	 * returns the person refering to a recordstore ID
	 * 
	 * @param id
	 * 
	 * @return Person
	 * @throws RecordStoreException
	 * @throws InvalidRecordIDException
	 * @throws Exception
	 */
	public static Person getPerson(int id) throws Exception, InvalidRecordIDException, RecordStoreException {
		Person p = null;
		p = new Person(new String(_persons.getRecord(id)), id);
		return p;
	}

	/**
	 * Updates an Adress
	 * 
	 * @param a
	 *            Adress
	 * @throws Exception
	 * @throws RecordStoreException
	 * @throws RecordStoreFullException
	 * @throws InvalidRecordIDException
	 * @throws Exception
	 * 
	 * @exception recordstore
	 */
	public static void updateAdress(Address a) throws Exception, InvalidRecordIDException, RecordStoreFullException,
			RecordStoreException, Exception {
		_numbers.setRecord(a.getRmsID(), a.getBytes());
	}

	/**
	 * Returns collection of Adress objects containing the phonenumber
	 * 
	 * @param url
	 *            phonenumber
	 * 
	 * @return Vector Adress
	 * @throws Exception
	 * 
	 * @exception recordstore
	 */
	public static Address findAdress4Url(String url) throws Exception {
		Vector as = new Vector();
		RecordEnumeration e = _numbers.enumerateCache(new AddressUrlFilter(url), null);

		while (e.hasNextElement()) {
			as.addElement(new Address(e.nextRecord()));
		}
		// TODO i18n
		if (as.size() > 1)
			throw new Exception("There are " + as.size() + "persons with number " + url + " in your addressbook");

		if (as.isEmpty())
			return null;
		else
			return (Address) as.firstElement();
	}

	/**
	 * Saves the public key for a phonenumber
	 * 
	 * @param url
	 *            phonenumber
	 * @param data
	 *            public key
	 * @throws Exception
	 * @throws RecordStoreException
	 * @throws RecordStoreFullException
	 * @throws Exception
	 * 
	 * @exception recordstore
	 * @exception no
	 *                phonenumber matches
	 */
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

	/**
	 * @param a
	 * @throws Exception
	 */
	public static void deleteAddress(Address a) throws Exception {
		Person p = a.getPerson();
		if (getAdresses4Person(p).length == 1) {
			deletePerson(p);
		}
		_numbers.deleteRecord(a.getRmsID());

	}

	/**
	 * @param p
	 * @throws Exception
	 */
	private static void deletePerson(Person p) throws Exception {
		_persons.deleteRecord(p.getRmsID());

	}

}
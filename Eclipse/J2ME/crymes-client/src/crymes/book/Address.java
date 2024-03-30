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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.bouncycastle.crypto.engines.IESEngine;

/**
 * domain object which holds one adress of one person. adress includes URL/Phone
 * number, public key, flag for key.
 * 
 * @author sd
 */
public class Address {
	private int _rmsID;

	private Person _person;

	private String _url;

	// private Connection _urlType;

	private byte[] _publicKey;

	private int _publicKeyLen;

	private boolean _hasKey;
	
	private IESEngine engine=null;

	/**
	 * Constructor for an adress object that refers to a person. Url and pubkey
	 * are parameters.
	 * 
	 * @param rmsID
	 *            ID of person's record
	 * @param url
	 *            phonenumber/url of adress
	 * @param type
	 *            connection type (sms, ...)
	 * @param p
	 *            person
	 * 
	 */
	public Address(int rmsID, String url, Person p) {
		this._rmsID = rmsID;
		// if (url.charAt(0)!='0')
		// url="0"+url;
		this._url = url;
		// this._urlType = type;
		this._person = p;
		this._publicKey = new byte[0];
		this._publicKeyLen = 0;
		this._hasKey = false;
	}

	/**
	 * Contructor creates a new adress object out of the record store.
	 * 
	 * @param record
	 *            byte array, of the record store
	 */
	public Address(byte[] record) {
		ByteArrayInputStream bStrm = null;
		DataInputStream dStrm = null;
		try {
			bStrm = new ByteArrayInputStream(record);
			dStrm = new DataInputStream(bStrm);

			_publicKeyLen = dStrm.readInt();

			_person = AddressService.getPerson(dStrm.readInt());
			/*
			_urlType = Connection.create(dStrm.readShort());
			*/
			_url = dStrm.readUTF();
			_rmsID = dStrm.readInt();
			_hasKey = dStrm.readBoolean();

			if (_hasKey) {
				byte[] b = new byte[_publicKeyLen];
				dStrm.read(b);
				_publicKey = b;
			} else {
				_publicKey = new byte[0];
			}
			dStrm.close();
			bStrm.close();
		} catch (Exception e) {
			// TODO handle Exception
		}
	}

	/**
	 * Serializes adress object into byte array
	 * 
	 * @return byte array serialized adress object
	 * @throws Exception
	 *             IO-Exception
	 */
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream bStrm = new ByteArrayOutputStream();
		DataOutputStream dStrm = new DataOutputStream(bStrm);

		bStrm = new ByteArrayOutputStream();
		dStrm = new DataOutputStream(bStrm);

		dStrm.writeInt(_publicKeyLen);

		dStrm.writeInt(_person.getRmsID());
		/*
		dStrm.writeShort(_urlType.getTypeCode());
		*/
		dStrm.writeUTF(_url);
		dStrm.writeInt(_rmsID);
		dStrm.writeBoolean(_hasKey);
		dStrm.write(_publicKey);

		return bStrm.toByteArray();
	}

	/**
	 * Returns connection type
	 * 
	 * @return connection type of adress
	 */
	/*
	public Connection getType() {
		return this._urlType;
	}
	*/
	
	/**
	 * Returns phonenumber/url
	 * 
	 * @return _url phonenumber
	 */
	public String getUrl() {
		return this._url;
	}

	/**
	 * Sets phonenumber/url
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		this._url = url;
	}

	/**
	 * Returns the person on which the adress object refers to
	 * 
	 * @return person
	 */
	public Person getPerson() {
		return _person;
	}

	/**
	 * Returns Record ID of adress
	 * 
	 * @return rmsID
	 */
	public int getRmsID() {
		return _rmsID;
	}

	/**
	 * Returns public key of adress
	 * 
	 * @returns publicKey
	 */
	public byte[] getPublicKey() {
		return _publicKey;
	}

	/**
	 * Sets public key refering to url
	 * 
	 * @param data
	 *            Byte Array containing public key
	 */
	public void setPublicKey(byte[] data) {
		_publicKey = data;
		_publicKeyLen = data.length;
		_hasKey = true;
	}

	/**
	 * Flag showing if adress has pubkey
	 * 
	 * @return hasKey
	 */
	public boolean hasKey() {
		return _hasKey;
	}
	
	/**
	 * provide/register an encryption engine for this address
	 */
	public synchronized void registerEngine(IESEngine engine) {
		this.engine=engine;
	}
	
	/**
	 * consume/use provided engine - may return null
	 */
	public synchronized IESEngine consumeEngine() {
		IESEngine tmp=this.engine;
		registerEngine(null);
		return tmp;
	}
	 
	 
}

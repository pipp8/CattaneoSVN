/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.modules.addressBook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import jwhisper.utils.Logger;

/*
 * Address info of one person:
 * Phone number, public key, flag for key
 */

public class Address {
	
	// rms ID 
	private int _rmsID;

	// Person associated
	private Person _person;

	// URL/Phone number
	private String _url;

	// Public key
	private byte[] _publicKey;
	private int _publicKeyLen;

	// Flag key
	private boolean _hasKey;
	
	
	/*
	 * Constructors
	 */
	public Address(int rmsID, String url, Person p) {
		this._rmsID = rmsID;
	
		this._url = url;
	
		this._person = p;
		this._publicKey = new byte[0];
		this._publicKeyLen = 0;
		this._hasKey = false;
	}

	
	public Address(byte[] record) {
		ByteArrayInputStream bStrm = null;
		DataInputStream dStrm = null;
		try {
			bStrm = new ByteArrayInputStream(record);
			dStrm = new DataInputStream(bStrm);

			_publicKeyLen = dStrm.readInt();

			_person = AddressService.getPerson(dStrm.readInt());
			
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
			Logger.getInstance().write("Error: parsing address record fail");
		}
	}

	/*
	 * Address to Bytes
	 */
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream bStrm = new ByteArrayOutputStream();
		DataOutputStream dStrm = new DataOutputStream(bStrm);

		bStrm = new ByteArrayOutputStream();
		dStrm = new DataOutputStream(bStrm);

		dStrm.writeInt(_publicKeyLen);
		dStrm.writeInt(_person.getRmsID());
		
		dStrm.writeUTF(_url);
		dStrm.writeInt(_rmsID);
		dStrm.writeBoolean(_hasKey);
		dStrm.write(_publicKey);
		return bStrm.toByteArray();
	}

	
	
	/*
	 * Getter/Setter methods
	 */
	public String getUrl() {
		return this._url;
	}

	
	public void setUrl(String url) {
		this._url = url;
	}

	public Person getPerson() {
		return _person;
	}


	public int getRmsID() {
		return _rmsID;
	}


	public byte[] getPublicKey() {
		return _publicKey;
	}

	public void setPublicKey(byte[] data) {
		_publicKey = data;
		_publicKeyLen = data.length;
		_hasKey = true;
	}


	public boolean hasKey() {
		return _hasKey;
	}
 
}

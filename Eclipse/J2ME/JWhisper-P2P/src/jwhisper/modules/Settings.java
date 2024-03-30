/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.modules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;



public class Settings {
	
	private static Settings _settings = null;

	// Name utente
	private String _name;
	
	// Number utente
	private String _number;
	
	// versione protocollo utilizzato
	private final byte _version = 1;

	// coppia chiavi
	private KeyPair _keyPair=null;
	
	// algoritimo di cifratura SMS utilizzato
	private String _alghoritm;
	

	/*
	 * Costruttori
	 */
	
	private Settings(String alg, String name, String number, KeyPair pair) {
		_alghoritm = alg;
		
		_name = name;
		_number = number;
		_keyPair = pair;
	}
	
	private Settings(byte[] record) throws IOException {
		ByteArrayInputStream Bstrm = new ByteArrayInputStream(record);
		DataInputStream Dstrm = new DataInputStream(Bstrm);

		int privLen = Dstrm.readShort();
		int pubLen = Dstrm.readShort();

		byte[] buffer = new byte[privLen + pubLen];
		byte[] workPriv = new byte[privLen];
		byte[] workPub = new byte[pubLen];

		_alghoritm = Dstrm.readUTF();
		
		Dstrm.read(buffer, 0, privLen + pubLen);

		System.arraycopy(buffer, 0, workPriv, 0, privLen);
		System.arraycopy(buffer, privLen, workPub, 0, pubLen);
		
		IKeyPairGenerator c = jwhisper.crypto.Utils.GetKeyPairGenerator(_alghoritm);
		_keyPair=c.getKeyPairFromABytePair(workPub, workPriv);
		
		_name = Dstrm.readUTF();
		_number = Dstrm.readUTF();
	}


	/*
	 *  Metodi di init 
	 */
	
	public static Settings init(byte[] record) throws Exception {
		try {
			if (_settings == null) {
				_settings = new Settings(record);
			}
		} catch (Exception e) {
			// TODO notify user
			_settings=null; // do not return a half initialized object
		}
		return _settings;
	}

	
	public static Settings init(String alg, String name, String number, KeyPair pair) throws Exception {
		if (_settings == null) {
			_settings = new Settings(alg, name, number, pair);
		} else {
			throw new Exception("Settings initialized");
		}

		return _settings;
	}
	
	
	
	/*
	 * Settings to Byte
	 */
	public byte[] toBytes() throws Exception {
		ByteArrayOutputStream Bstrm = new ByteArrayOutputStream();
		DataOutputStream Dstrm = new DataOutputStream(Bstrm);

		Bstrm = new ByteArrayOutputStream();
		Dstrm = new DataOutputStream(Bstrm);

		Dstrm.writeUTF(_alghoritm);
		
		Dstrm.writeShort(_keyPair.getPrivateKey().length);
		Dstrm.writeShort(_keyPair.getPublicKey().length);
		Dstrm.write(_keyPair.getPrivateKey());
		Dstrm.write(_keyPair.getPublicKey());
		
		Dstrm.writeUTF(_name);
		Dstrm.writeUTF(_number);
		
		return Bstrm.toByteArray();
	}

	/* 
	 * Distruttore
	 */
	
	public  void destroy() {
		_settings=null;
		_name=null;
		_keyPair=null;
	}

	
	/*
	 * Getter methods
	 */
	
	public String getAlghoritm() {
		return _alghoritm;
	}
	
	public static Settings getSettings() {
		return _settings;
	}

	public String getName() {
		return _name;
	}

	public String getNumber() {
		return _number;
	}

	public byte[] getPublicKey() {
		return _keyPair.getPublicKey();
	}
	
	public byte[] getPrivateKey() {
		return _keyPair.getPrivateKey();
	}

	public int getVersion() {
		return _version;
	}

	
}

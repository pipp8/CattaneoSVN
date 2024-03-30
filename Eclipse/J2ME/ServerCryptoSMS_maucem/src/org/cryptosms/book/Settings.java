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
package org.cryptosms.book;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.cryptosms.helpers.KeyPairHelper;
import org.cryptosms.helpers.Logger;



/**
 * @author sd
 */
public class Settings {
	private static Settings _settings = null;

	private String _name;

	private String _number;

	private final byte _version = 1;

	private KeyPairHelper _keyPair=null;

	private String build="20070523-1";
	
	private Logger logger;

	private Settings(String name, String number, KeyPairHelper pair) {
		_name = name;
		_number = number;
		_keyPair = pair;
		
		logger=Logger.getInstance();
		logger.write("Build: "+this.build);
	}

	private Settings(byte[] record) throws IOException {
		ByteArrayInputStream Bstrm = new ByteArrayInputStream(record);
		DataInputStream Dstrm = new DataInputStream(Bstrm);

		

		int privLen = Dstrm.readShort();
		int pubLen = Dstrm.readShort();

		byte[] buffer = new byte[privLen + pubLen];
		byte[] workPriv = new byte[privLen];
		byte[] workPub = new byte[pubLen];

		Dstrm.read(buffer, 0, privLen + pubLen);

		System.arraycopy(buffer, 0, workPriv, 0, privLen);
		System.arraycopy(buffer, privLen, workPub, 0, pubLen);
		
		_keyPair=new KeyPairHelper(workPub,workPriv);
		_name = Dstrm.readUTF();
		_number = Dstrm.readUTF();
	}

	static Settings init(byte[] record) throws Exception {
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

	/**
	 * @param name of mobile user
	 * @param number of user's mobile of phone
	 * @param pair keypair
	 */
	public static Settings init(String name, String number, KeyPairHelper pair) throws Exception {
		if (_settings == null) {
			_settings = new Settings(name, number, pair);
		} else {
			throw new Exception("Settings initialized twice");
		}

		return _settings;
	}

	public static Settings findSettings() {
		return _settings;
	}

	public String getName() {
		return _name;
	}

	public String getNumber() {
		return _number;
	}

	public byte[] getPublicKey() {
//		ECPublicKeyParameters pub = (ECPublicKeyParameters) _pair.getPublic();
//
//		return pub.getQ().getEncoded();
		return _keyPair.getPublicKeyBA();
	}

	public KeyPairHelper getPair() {
		return _keyPair;
	}

	public int getVersion() {
		return _version;
	}

	public byte[] toBytes() throws Exception {
		ByteArrayOutputStream Bstrm = new ByteArrayOutputStream();
		DataOutputStream Dstrm = new DataOutputStream(Bstrm);

		Bstrm = new ByteArrayOutputStream();
		Dstrm = new DataOutputStream(Bstrm);

//		ECPrivateKeyParameters priv = (ECPrivateKeyParameters) _pair.getPrivate();
//		BigInteger dd = priv.getD();
//		byte[] data = dd.toByteArray();
//		ECPublicKeyParameters pub = (ECPublicKeyParameters) _pair.getPublic();		
//		Dstrm.writeShort(data.length);
//		Dstrm.writeShort(pub.getQ().getEncoded().length);
//		Dstrm.write(data);
//		Dstrm.write(pub.getQ().getEncoded());

		Dstrm.writeShort(_keyPair.getPrivateKeyBA().length);
		Dstrm.writeShort(_keyPair.getPublicKeyBA().length);
		Dstrm.write(_keyPair.getPrivateKeyBA());
		Dstrm.write(_keyPair.getPublicKeyBA());
		
		Dstrm.writeUTF(_name);
		Dstrm.writeUTF(_number);

		return Bstrm.toByteArray();
	}

	/**
	 * 
	 */
	public  void destroy() {
		_settings=null;
		_name=null;
		_keyPair=null;
	}


	public String getBuild() {	
		return build;
	}
}

package jwhisper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;

public class SettingsP2P extends Settings {

	
	private SettingsP2P(String alg, String name, String number, KeyPair pair) {
		_alghoritm = alg;
		
		_name = name;
		_number = number;
		_keyPair = pair;
		_applicationType = Constant.APPLICATION_TYPE_P2P;
	}
	
	private SettingsP2P(byte[] record) throws IOException{
		ByteArrayInputStream Bstrm = new ByteArrayInputStream(record);
		DataInputStream Dstrm = new DataInputStream(Bstrm);

		
		_applicationType = Dstrm.readByte();
		_alghoritm = Dstrm.readUTF();
		
		int privLen = Dstrm.readShort();
		int pubLen = Dstrm.readShort();

		byte[] buffer = new byte[privLen + pubLen];
		byte[] workPriv = new byte[privLen];
		byte[] workPub = new byte[pubLen];

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
	
	protected static Settings initP2P(byte[] record) throws Exception
	{
		try {
			if (_settings == null) {
				_settings = new SettingsP2P(record);
			}
		} catch (Exception e) {
			// TODO notify user
			_settings=null; // do not return a half initialized object
		}
		return _settings;
	}

	
	public static Settings init(String alg, String name, String number, KeyPair pair) throws Exception {
		if (_settings == null) {
			_settings = new SettingsP2P(alg, name, number, pair);
		} else {
			throw new Exception("Settings initialized");
		}

		return _settings;
	}
	
	
	public byte[] toBytes() throws Exception {
			ByteArrayOutputStream Bstrm = new ByteArrayOutputStream();
			DataOutputStream Dstrm = new DataOutputStream(Bstrm);

			Bstrm = new ByteArrayOutputStream();
			Dstrm = new DataOutputStream(Bstrm);

			Dstrm.writeByte(_applicationType);
			Dstrm.writeUTF(_alghoritm);
			
			Dstrm.writeShort(_keyPair.getPrivateKey().length);
			Dstrm.writeShort(_keyPair.getPublicKey().length);
			Dstrm.write(_keyPair.getPrivateKey());
			Dstrm.write(_keyPair.getPublicKey());
			
			Dstrm.writeUTF(_name);
			Dstrm.writeUTF(_number);
			
			return Bstrm.toByteArray();
		}

}

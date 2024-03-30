/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.book;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import crymes.crypto.IKeyPairGenerator;
import crymes.crypto.KeyPair;
import crymes.util.helpers.Logger;




/**
 * @author sd
 */
public class Settings {
	private static Settings _settings = null;

	private String _name;

	private String _number;

	private final byte _version = 1;

	//private KeyPairHelper _keyPair=null;
	private KeyPair _keyPair=null;

	private String build="20070523-1";
	
	private Logger logger;

	private String _smscNumber;
	
	private String _alghoritm;
	
//	private Settings(String name, String number, KeyPairHelper pair, String smscNumber) {
//		_name = name;
//		_number = number;
//		_keyPair = pair;
//		_smscNumber = smscNumber;
//		
//		logger=Logger.getInstance();
//		logger.write("Build: "+this.build);
//	}

	private Settings(String alg, String name, String number, KeyPair pair, String smscNumber) {
		_alghoritm = alg;
		
		_name = name;
		_number = number;
		_keyPair = pair;
		_smscNumber = smscNumber;
		
		
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

		_alghoritm = Dstrm.readUTF();
		
		Dstrm.read(buffer, 0, privLen + pubLen);

		System.arraycopy(buffer, 0, workPriv, 0, privLen);
		System.arraycopy(buffer, privLen, workPub, 0, pubLen);
		
		//KeyPair kp = 
		IKeyPairGenerator c = crymes.crypto.Utils.GetKeyPairGenerator(_alghoritm);
		_keyPair=c.getKeyPairFromABytePair(workPub, workPriv);
		
		_name = Dstrm.readUTF();
		_number = Dstrm.readUTF();
		_smscNumber = Dstrm.readUTF();
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
//	public static Settings init(String name, String number, KeyPairHelper pair, String smscNumber) throws Exception {
//		if (_settings == null) {
//			_settings = new Settings(name, number, pair, smscNumber);
//		} else {
//			throw new Exception("Settings initialized twice");
//		}
//
//		return _settings;
//	}

	public static Settings init(String alg, String name, String number, KeyPair pair, String smscNumber) throws Exception {
		if (_settings == null) {
			_settings = new Settings(alg, name, number, pair, smscNumber);
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
		return _keyPair.getPublicKey();
	}

//	public KeyPairHelper getPair() {
//		return _keyPair;
//	}

	public int getVersion() {
		return _version;
	}
	public String getSMSCNumber(){
		return _smscNumber;
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

		Dstrm.writeUTF(_alghoritm);
		
		Dstrm.writeShort(_keyPair.getPrivateKey().length);
		Dstrm.writeShort(_keyPair.getPublicKey().length);
		Dstrm.write(_keyPair.getPrivateKey());
		Dstrm.write(_keyPair.getPublicKey());
		
		Dstrm.writeUTF(_name);
		Dstrm.writeUTF(_number);
		Dstrm.writeUTF(_smscNumber);
		
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

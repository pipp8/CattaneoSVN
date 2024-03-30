/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import jwhisper.crypto.KeyPair;



abstract public class Settings {
	
	protected static Settings _settings = null;
	
	protected byte _applicationType;
	// Name utente
	protected String _name;
	
	// Number utente
	protected String _number;
	
	// versione protocollo utilizzato
	protected final byte _version = 1;

	// coppia chiavi
	protected KeyPair _keyPair=null;
	
	// algoritimo di cifratura SMS utilizzato
	protected String _alghoritm;
	

	/*
	 * Costruttori
	 */
	
	public Settings(){
		
	}

	/*
	 * Settings to Byte
	 */
	abstract public byte[] toBytes() throws Exception;
	/*
	
	
*/
	/* 
	 * Distruttore
	 */
	
	public void destroy() {
		_settings=null;
		_name=null;
		_number=null;
		_alghoritm=null;
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

	public static Settings init(byte[] nextRecord) throws Exception{
		Settings s = null;
		try {
			
			byte b = whichApplicationType(nextRecord);
			
			if ( b == Constant.APPLICATION_TYPE_A2P ){
			
				s = SettingsA2P.initA2P(nextRecord);
			
			} 
			else if ( b ==  Constant.APPLICATION_TYPE_P2P){
			
				s = SettingsP2P.initP2P(nextRecord);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
		
	}

	public byte getApplicationType() {
		return _applicationType;
	}
	
	private static byte whichApplicationType(byte[] record) throws IOException{
		ByteArrayInputStream Bstrm = new ByteArrayInputStream(record);
		DataInputStream Dstrm = new DataInputStream(Bstrm);

		//int privLen = Dstrm.readShort();
		//int pubLen = Dstrm.readShort();
		
		byte applicationType = Dstrm.readByte();
		
		return applicationType;
	}
	
}

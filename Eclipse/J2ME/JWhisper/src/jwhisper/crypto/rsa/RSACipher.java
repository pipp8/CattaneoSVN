/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.crypto.rsa;


import org.bouncycastle.crypto.InvalidCipherTextException;

import jwhisper.crypto.Cipher;
import jwhisper.crypto.KeyPair;

public class RSACipher extends Cipher {
	
	public static final String ALG_ID = "RSA";
	
	public RSACipher(){
		
		_cipher = this;	
	}

	public byte[] decrypt(byte[] data, KeyPair p)
			throws InvalidCipherTextException, Exception {
		if (p  instanceof RSAKeyPair) {
			//@SuppressWarnings("unused")
			RSAKeyPair pair = (RSAKeyPair) p ;
		} else {
			throw new Exception("type Key pair incorrect");
		}
		
		return null;
	}

	public byte[] encrypt(byte[] data, KeyPair p) 
			throws Exception {
		if (p  instanceof RSAKeyPair) {
			//@SuppressWarnings("unused")
			RSAKeyPair pair = (RSAKeyPair) p ;
		} else {
			throw new Exception("type Key pair incorrect");
		}
		
		return null;
	}
}

/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.crypto;


import org.bouncycastle.crypto.InvalidCipherTextException;

public abstract class Cipher{
	protected static String ALG_ID;
	protected Cipher _cipher;
	
	public abstract byte[] decrypt(byte[] data, KeyPair pair)
			throws InvalidCipherTextException, Exception;
	
	public abstract byte[] encrypt(byte[] data, KeyPair pair)
			throws Exception;

	
	
}

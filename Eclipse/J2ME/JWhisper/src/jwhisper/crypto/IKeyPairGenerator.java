/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.crypto;

public interface IKeyPairGenerator {
	KeyPair generateKeyPair() throws Exception;
	KeyPair getKeyPairFromABytePair(byte[] pubKey,byte[] priKey);
}

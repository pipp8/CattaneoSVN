/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.crypto;



public abstract class KeyPair {
	
	
	protected byte[] _privateKey;
	protected byte[] _publicKey;
	
	public byte[] getPrivateKey(){
		return _privateKey;
	}
	
	public byte[] getPublicKey(){
		return _publicKey;
	}
	
}

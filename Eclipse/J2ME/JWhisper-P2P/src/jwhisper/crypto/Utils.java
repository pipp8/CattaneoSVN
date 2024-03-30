/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.crypto;

import jwhisper.crypto.ecies.ECIESCipher;
import jwhisper.crypto.ecies.ECIESKeyPairGenerator;
import jwhisper.crypto.rsa.RSACipher;
import jwhisper.crypto.rsa.RSAKeyPairGenerator;

public class Utils {

	public static Cipher GetCipher(String alg){
		Cipher c = null;
		
		if (alg.compareTo(ECIESCipher.ALG_ID) == 0){
			c = new ECIESCipher();
		} else if (alg.compareTo(RSACipher.ALG_ID) == 0){
			c = new RSACipher();
		}
		return c;
	}
	
	public static IKeyPairGenerator GetKeyPairGenerator(String alg){
		IKeyPairGenerator c = null;
		
		if (alg.compareTo(ECIESCipher.ALG_ID) == 0){
			c = new ECIESKeyPairGenerator();
		} else if (alg.compareTo(RSACipher.ALG_ID) == 0){
			c = new RSAKeyPairGenerator();
		}
		
		return c;
	}
	
}

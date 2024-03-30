package crymes.crypto;

import crymes.crypto.ecies.ECIESCipher;
import crymes.crypto.ecies.ECIESKeyPairGenerator;
import crymes.crypto.rsa.RSACipher;
import crymes.crypto.rsa.RSAKeyPairGenerator;

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

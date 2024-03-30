package crypto;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import org.bouncycastle.util.encoders.Base64;



public class CifraCF {

	public static void main(String[] args) throws Exception {
		String		inputStr = "CTTGPP60A11A662B";
		byte[]		input = inputStr.getBytes();
	    Cipher		cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
	    Base64		encoder = new Base64();    
	    SecureRandom     random = null;
	    
    	KeyPair pair;
    	Key privKey = null;
    	Key pubKey = null;
 
    	// create the keys
	    try {

	    	KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
	    	random = SecureRandom.getInstance("SHA1PRNG", "SUN");
	    	keyGen.initialize(1024, random);

	    	pair = keyGen.generateKeyPair();
	    	privKey = pair.getPrivate();
	    	pubKey = pair.getPublic();
	    }
	    catch(Exception e) {
	    	System.out.println("Error Generating Keys: " + e.getMessage());
	    }

	    System.out.println("input : " + new String(input));

	    // encryption step
	    cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);

	    byte[] cipherText = cipher.doFinal(input);
	    String ct = new String(encoder.encode(cipherText));
	    System.out.println("cipher text (len = " + ct.length()+") : " + ct);
	        
	    // decryption step

	    cipher.init(Cipher.DECRYPT_MODE, privKey);
	    
	    byte[] plainText = cipher.doFinal(cipherText);
	        
	    System.out.println("plain : " + new String(plainText));
	}
}

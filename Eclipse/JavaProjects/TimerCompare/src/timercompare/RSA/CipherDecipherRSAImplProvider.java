/*
 * $Revision: 1.2 $
 * $Date: 2008-11-13 16:27:44 $
 */

package timercompare.RSA;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;

import timercompare.CipherDecipherEngine;
import timercompare.utility.TCUtility;

public class CipherDecipherRSAImplProvider implements CipherDecipherEngine {

	private int keySize;
	private PublicKey pubKey;
	private PrivateKey privKey;
	private Cipher cipher;
	private Base64 encoder;
    private SecureRandom securRand = null;
    private String cryptingProvider;

    
	public CipherDecipherRSAImplProvider( int pKeySize, String provider){
		provider = provider.toUpperCase();
		//keySize can be 1024, 2048, 3072, 7680, 15369
		keySize = pKeySize;
		//initEngine();
		if(!provider.equals("SUN") || !provider.equals("BC"))
			cryptingProvider = "";
		else
			cryptingProvider = provider;
	}
	
	public void initEngine() {
		// inizializzazione motore
		// DOVE E' ??????
		
		// generatore di chiavi
		// System.out.println("generatore di chiavi RSA");
		KeyPairGenerator keyGenRSA = null;
		try {
			keyGenRSA = KeyPairGenerator.getInstance("RSA",cryptingProvider);
			securRand = SecureRandom.getInstance("SHA1PRNG","SUN");
			keyGenRSA.initialize(keySize,securRand);
			// inizializzazione chiavi
			KeyPair pair= keyGenRSA.generateKeyPair();
			// System.out.println("inizializzazione chiavi");
			// prendiamo le chiavi
			// System.out.println("prendiamo le chiavi");
			pubKey = (PublicKey)pair.getPublic();
			privKey = (PrivateKey)pair.getPrivate();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}
	}

	public byte[] encrypt(byte[] plainText)
	{
		if (pubKey == null) {		
			return null;
		}
		try {
			// encryption step
			System.out.println((Cipher.ENCRYPT_MODE) + " "+pubKey+ " "+securRand);
		    cipher.init(Cipher.ENCRYPT_MODE, pubKey, securRand);	
		    return encoder.encode( cipher.doFinal(plainText));
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] decrypt(byte[] cypherText) 
	{
		if (privKey == null) {		
			return null;
		}
		try {
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			return cipher.doFinal(encoder.decode(cypherText));
		}
		catch(Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
}

/*
 * $Revision: 1.2 $
 * $Date: 2008-11-13 16:27:44 $
 */

package timercompare.ECC;

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
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import org.bouncycastle.util.encoders.Base64;
import timercompare.CipherDecipherEngine;
import timercompare.utility.TCUtility;

public class CipherDecipherECCImplProvider implements CipherDecipherEngine {

	private byte[] dataOriginale;
	private byte[] dataCifrato;
	private int keySize;
	private PublicKey pubKey;
	private PrivateKey privKey;
	private Cipher cipher;
	private Base64 encoder;
    private SecureRandom securRand = null;
    private String cryptingProvider;

    public CipherDecipherECCImplProvider(byte[] dataInput, String provider){
		this(dataInput, 128, provider);
	}
    
	public CipherDecipherECCImplProvider(byte[] dataInput, int pKeySize, String provider){
		//keySize can be 128, 160, 224, 256, 384, 512
		provider = provider.toUpperCase();
		dataOriginale = dataInput;
		keySize = pKeySize;
		pubKey = null;
		privKey = null;	
		if(!provider.equals("SUN") || !provider.equals("BC"))
			cryptingProvider = "";
		else
			cryptingProvider = provider;
	}
	
	public CipherDecipherECCImplProvider(File fileInput, int pKeySize, String provider){
		try {
			dataOriginale = TCUtility.getBytesFromFile(fileInput);
		} catch (IOException e) {
			dataOriginale = new byte[0];
			System.err.println("CipherDecipherRSAImplBC: file not exists");
		}
		keySize = pKeySize;
		if(!provider.equals("SUN") || !provider.equals("BC"))
			cryptingProvider = "SUN";
		else
			cryptingProvider = provider;
	}

	public void initEngine() {
		// inizializzazione motore
		// generatore di chiavi
		// System.out.println("generatore di chiavi ECDSA");
		KeyPairGenerator keyGenECC = null;
		try {
			keyGenECC = KeyPairGenerator.getInstance("ECC",cryptingProvider);
			securRand = SecureRandom.getInstance("SHA1PRNG","SUN");
			keyGenECC.initialize(keySize,securRand);
			// inizializzazione chiavi
			KeyPair pair= keyGenECC.generateKeyPair();
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
		    cipher.init(Cipher.ENCRYPT_MODE, pubKey, securRand);
		    return encoder.encode( cipher.doFinal(dataOriginale));
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
			return cipher.doFinal(encoder.decode(dataCifrato));
		}
		catch(Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
}

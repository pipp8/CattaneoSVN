/*
 * $Revision: 1.4 $
 * $Date: 2009-07-08 16:55:32 $
 */

package timercompare.RSA;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.Base64;

import timercompare.CipherDecipherEngine;
import timercompare.KeyParameters;
import timercompare.utility.TCUtility;
import java.io.*;

public class CipherDecipherRSAImplBC implements CipherDecipherEngine {
	
	private RSAEngine rsa_engine1;
	private AsymmetricBlockCipher cipher1;
 	@SuppressWarnings("unused")
	private PKCS1Encoding cipherPKCS;
	private RSAKeyPairGenerator pGen;
	private RSAKeyGenerationParameters genParam;
	private AsymmetricCipherKeyPair keyPair;
	private RSAKeyParameters publicKey;
	private RSAKeyParameters privateKey;
	private int keySize;
	private int maxBlockSize;
	private boolean paddingOn = false;
	
	private Logger logger = null;
	private Base64 encoder = null;

	public CipherDecipherRSAImplBC(){
		//keySize can be 1024, 2048, 3072, 7680, 15369
		keySize = 1024;
		maxBlockSize = 0;
		initEngine();
	}
	
	public CipherDecipherRSAImplBC( int index){
		//keySize can be 1024, 2048, 3072, 7680, 15369
		KeyParameters[] kl = KeyParameters.values();
		keySize = kl[index].getRSAKeyLen();
		maxBlockSize = 0;
		initEngine();
	}


	public void initEngine(){
		
		logger = Logger.getLogger("RSAvsEC");
		logger.setLevel(Level.WARNING);
		logger.log(Level.WARNING, "Inizializzazione ambiente RSA -> KeyLen: " + keySize);

		// inizializzazione motore
		rsa_engine1 = new RSAEngine();
		if (paddingOn == true)
		{
			cipher1 = new PKCS1Encoding(rsa_engine1);
			logger.log(Level.WARNING, "RSA Engine initialized WITH padding PKCS 1 v5");
		}
		else {
			cipher1 = rsa_engine1;
			logger.log(Level.WARNING, "RSA Engine initialized WITHOUT PADDING");
		}
		
		pGen = new RSAKeyPairGenerator();
		genParam = new RSAKeyGenerationParameters(BigInteger.valueOf(0x11),
				new SecureRandom(),keySize, 25); // ????
		// inizializzazione chiavi
		// System.out.println("inizializzazione chiavi");
		pGen.init(genParam);

		
		logger.log(Level.INFO, "Starting Key Generation");
		keyPair = pGen.generateKeyPair();
		// prendiamo le chiavi
		// System.out.println("prendiamo le chiavi");
		publicKey = (RSAKeyParameters)keyPair.getPublic();
		logger.log(Level.INFO, "Key Generated");
		
		logger.log(Level.INFO, "Public Key Lenght Lenght: " +
				publicKey.getModulus().bitLength() + " (exp: " +
				publicKey.getExponent().bitLength() + ")");

		privateKey = (RSAKeyParameters)keyPair.getPrivate();
		logger.log(Level.INFO, "Private Key Lenght Lenght: " +
				privateKey.getModulus().bitLength() + " (exp: " +
				privateKey.getExponent().bitLength() + ")");
}
	
	public byte[] encrypt(byte[] plainText) {
		
		if (publicKey == null) {
			logger.log(Level.SEVERE, "RSA Encryption engine not initialized. Returning");
			return null;
		}
		// cifratura
		cipher1.init(true, publicKey);
		
		maxBlockSize = cipher1.getInputBlockSize();
		logger.log(Level.INFO, "RSA EncryptionEngine initialized maxblocksize: " + maxBlockSize);

		byte[] cypherText = null;
		try {
			cypherText = cipher1.processBlock(plainText, 0, maxBlockSize);  //dataOriginale.length);
			logger.log(Level.INFO, "processBlock output (Len: " + cypherText.length+")");

		} catch (InvalidCipherTextException e) {
			//e.printStackTrace();
			System.err.println(e);
		} catch (ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		return cypherText;
	}
	
	public byte[] decrypt(byte[] cypherText) {

		if (keyPair == null) {
			logger.log(Level.SEVERE, "RSA Encryption engine not initialized. Returning");
			return null;
		}
		// decifratura
		//		System.out.println("decifratura");
		byte[] plainText = null;
		cipher1.init(false, privateKey );
		try {
			plainText = cipher1.processBlock(cypherText, 0, cypherText.length);
		} catch (InvalidCipherTextException e) {
			// e.printStackTrace();
			System.err.println(e);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		// (data byte[], 0, data.length)
		// System.out.println("decifrato: "+dataOriginale);
		return plainText;
	}

	public int getMaxBlockSize() {
		return maxBlockSize;
	}
}

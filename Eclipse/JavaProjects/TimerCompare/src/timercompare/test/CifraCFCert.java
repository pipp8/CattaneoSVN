package timercompare.test;

/*
 * $Revision: 1.3 $
 * $Date: 2009-07-08 16:55:32 $
 */
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.Base64;

import timercompare.utility.*;

public class CifraCFCert {
	
//    private Cipher			cipherRSA = null;
    private Cipher			cipher = null;
    private Cipher			cipherECC = null;
    private Base64			encoder = null;    
    private SecureRandom	random = null;
//    private PublicKey		pubKey = null;            
//    private PrivateKey		privKey = null; 
    private PublicKey		pubKey = null;            
    private PrivateKey		privKey = null; 
    private PublicKey		pubKeyECC = null;            
    private PrivateKey		privKeyECC = null; 
    private int				keySize = 512;
    
    public String toString(){
    	return cipher.toString(); // + " " + encoder.toString()+ " " +
    	// random.toString() + " " + pubKey.toString()+ " " + privKey.toString();
    }
    
	public CifraCFCert (int pKS) {
		keySize = pKS;
		try {
			pubKey = null;
			privKey = null;
		    cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
//		    cipherECC = Cipher.getInstance("ECC/None/NoPadding", "BC");
		    
		    encoder = new Base64();
		}
		catch(java.security.NoSuchProviderException e) {
			System.out.println(e.getMessage());
			System.out.println("Occorre installare il provider Crittografico BouncyCastle");
			System.out.println("prima di poter eseguire il programma.");
			System.out.println("Exiting");
			System.exit(-128);
		}
		catch(Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
	}
	
	private void initRSAKeys() {
		// inizializzazione motore
		// generatore di chiavi
		// System.out.println("generatore di chiavi RSA");
		KeyPairGenerator keyGenRSA = null;
		KeyPairGenerator keyGenECC = null;
		try {
			keyGenRSA = KeyPairGenerator.getInstance("RSA","BC");
//			keyGenECC = KeyPairGenerator.getInstance("ECC","BC");
			SecureRandom securRand = SecureRandom.getInstance("SHA1PRNG","SUN");
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

	/**
	 * @deprecated
	 * @param pubKeyFile
	 * @return
	 */
	public X509Certificate initPubKey (String pubKeyFile) {
		try {
			// File certFile = new File(pubKeyFile);
			// read public key DER file
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(new FileInputStream(pubKeyFile));
			pubKey = (java.security.interfaces.RSAPublicKey) cert.getPublicKey();
			// System.out.println(cert.toString() + "\n" + pubKey.toString());
			RSAPublicKey rpk = (RSAPublicKey) pubKey;
			BigInteger mod = rpk.getModulus();
			// System.out.println("modulus:\n" + mod.toString(16));

			return 	(X509Certificate) cert;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
			
	/**
	 * @deprecated
	 * @param keyStore
	 * @param kspasswd
	 * @param certAlias
	 * @param pvkpasswd
	 * @return
	 */
	public boolean initPrivKey(String keyStore, String kspasswd, String certAlias, String pvkpasswd) {
		try 
		{
			
			// to decrypt I need private key from keystore.
		    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	
		    // get user password and file input stream
		    char[] password = kspasswd.toCharArray();
		    java.io.FileInputStream fis = new java.io.FileInputStream(keyStore);
		    ks.load(fis, password);
		    fis.close();
	
		    // get the private key
		    char[] pvkpass = pvkpasswd.toCharArray();
		    KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)
		    				ks.getEntry(certAlias, new KeyStore.PasswordProtection(pvkpass));
		    
		    if (pkEntry != null) { 
		    	privKey = pkEntry.getPrivateKey();
		    	return true;
		    }
		    else
		    	return false;
		    
		}
		catch(Exception e) {
			e.getMessage();
			return false;
		}
	}
	
	public byte[] encrypt(byte[] input) throws Exception
	{
		if (pubKey == null) {		
			System.out.println("cipher: " + cipher);
			System.out.println("pubkey: " + pubKey);
			throw new Exception("Public Key MUST be initialized before calling encrypt");
		}
		try {
			// encryption step
		    cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);	
		    return encoder.encode( cipher.doFinal(input));
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] decrypt(byte[] cipherText) throws Exception
	{
		
		if (privKey == null) {		
			System.out.println("cipher: " + cipher);
			System.out.println("privkey: " + privKey);
			throw new Exception("Private Key MUST be initialized before calling dencrypt");
		}
		try {
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			return cipher.doFinal(encoder.decode(cipherText));
		}
		catch(Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args){
		CifraCFCert ccf = new CifraCFCert(1024);
		System.out.println(ccf);
//		ccf.initPrivKey("keystore", "armando", "catalogo", "armando");
//		X509Certificate x509 = ccf.initPubKey("keystore");
		ccf.initRSAKeys();
		System.out.println("privKey "+ccf.privKey);
		System.out.println("pubKey "+ccf.pubKey);
		File f = new File("prova2.txt");
		File fc = new File("decifrato2RSA.txt");
		byte bb[] = null;
		try {
			bb = ccf.encrypt(TCUtility.getBytesFromFile(f));
			TCUtility.saveFileFromBytes(ccf.decrypt(bb), fc); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}


/* Valori assegnati alla keystore
 * keystore password:  armando
 * first and last name: armando.prova.it
 * name of your organizational unit:  aa.pp
 * name of your organization:  unisa
 * name of your City or Locality:  Salerno
 * name of your State or Province:  Italy
 * two-letter country code for this unit:  IT
 * <CN=armando.prova.it, OU=aa.pp, O=unisa, L=Salerno, ST=Italy, C=IT> correct?
 * */

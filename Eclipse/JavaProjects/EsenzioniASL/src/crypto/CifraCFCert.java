package crypto;
/*
 * $Revision: 1.3 $
 * $Date: 2008-05-07 09:45:00 $
 */
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;
import org.bouncycastle.util.encoders.Base64;



public class CifraCFCert {
    Cipher			cipher = null;
    Base64			encoder = null;    
    SecureRandom	random = null;
	PublicKey		pubKey = null;            
    PrivateKey		privKey = null; 


	public CifraCFCert () {
		try 
		{
			pubKey = null;
			privKey = null;
		    cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
		    
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
	
	public X509Certificate initPubKey (String pubKeyFile) {
		try 
		{
			File certFile = new File(pubKeyFile);
			// read public key DER file
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(new FileInputStream(pubKeyFile));
			pubKey = (java.security.interfaces.RSAPublicKey) cert.getPublicKey();
			// System.out.println(cert.toString() + "\n" + pubKey.toString());
			// RSAPublicKey rpk = (RSAPublicKey) pubKey;
			// BigInteger mod = rpk.getModulus();
			// System.out.println("modulus:\n" + mod.toString(16));

			return 	(X509Certificate) cert;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
			
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
}

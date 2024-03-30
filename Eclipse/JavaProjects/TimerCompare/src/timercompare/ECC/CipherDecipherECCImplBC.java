/*
 * $Revision: 1.5 $
 * $Date: 2009-07-08 16:55:32 $
 */

package timercompare.ECC;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import java.security.SecureRandom;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.IESEngine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.IESParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import timercompare.CipherDecipherEngine;
import timercompare.KeyParameters;
import timercompare.KeyParameters;


public class CipherDecipherECCImplBC implements CipherDecipherEngine {

	private int keySize;
	private KeyParameters[] keyParams;
	private IESEngine engineIES;
	private ECCurve.Fp ecCurve;
	private ECDomainParameters ecParam;
	private ECKeyPairGenerator ecGen;
	private AsymmetricCipherKeyPair keyPair;
	private ECPrivateKeyParameters privateKey;
	private ECPublicKeyParameters publicKey;
	private SecureRandom PRNG = null;
	private IESParameters IESParam = null;
	
	private int NONCE_SIZE = 16;
	private int PRIV_KEY_SIZE = 24;  // 32 -> 256, 24 -> 192, 16 -> 128 bit key lenght ;
	
	private final byte[] d = new byte[]{1,2,3,4,5,6,7,8};
	private final byte[] e = new byte[]{8,7,6,5,4,3,2,1};
	
	private Logger logger = null;
	private Base64 encoder = null;
	
	public CipherDecipherECCImplBC(){
		KeyParameters param = KeyParameters.secp160r2;
		keySize = param.getP().bitLength();
		initEngine( param);
	}
	
	public CipherDecipherECCImplBC(int index){
		keyParams = KeyParameters.values();
		
		keySize = keyParams[index].getP().bitLength();
		initEngine(keyParams[index]);
	}
		
	public void initEngine(KeyParameters param){
		
		logger = Logger.getLogger("RSAvsEC");
		logger.setLevel(Level.WARNING);
		logger.log(Level.WARNING, "Inizializzazione ambiente EC -> KeyLen: " + keySize);

		// Creazione motore di cifratura
		engineIES = new IESEngine(new ECDHBasicAgreement(),
				new KDF2BytesGenerator(new SHA1Digest()), 
				new HMac(new SHA1Digest()));

		logger.log(Level.INFO, "IES Engine initialized");
		
		try {
			// secure random number generator
			PRNG = SecureRandom.getInstance("SHA1PRNG");

			// copy from bouncy castle
			ecCurve = new ECCurve.Fp(
					param.getP(),
					param.getA(),
					param.getB());

			logger.log(Level.INFO, "Curve created (param P, a, b)");
			

			// Creazione parametri di dominio (da fare FINAL)
			// copy from bouncy castle
			ecParam= new ECDomainParameters(
					ecCurve,
	/* G */			ecCurve.decodePoint( param.getG()),
	/* n */			param.getN(),
	/* h */			param.getH(),
	/* seed */		param.getSeed());
	
			logger.log(Level.INFO, "Domain set (param G, n, h)");
	
			// Generazione chiavi
			keyPair = generateKeyPairHelper();
			
			// prendiamo le chiavi
			publicKey = (ECPublicKeyParameters)keyPair.getPublic();
	
			privateKey = (ECPrivateKeyParameters)keyPair.getPrivate();

			IESParam = new IESParameters(d,e,keySize);
			
			encoder = new Base64();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	

//	public byte[] encrypt(byte[] plainText) {
//		// cifratura
//
//		if (PubKey == null) {
//			logger.log(Level.SEVERE, "Encryption engine not initialized. Returning");
//			return null;
//		}
//
//		// perche' c'e' la chiave privata ???
//		enginC.init(true, PrivKey, PubKey, IESParam);
//		logger.log(Level.INFO, "Encrypt initialized");
//	
//		byte[] cypherText = null;
//		try {
//			cypherText = enginC.processBlock(plainText, 0, plainText.length);
//			logger.log(Level.WARNING, "processBlock output (Len: " + cypherText.length+")");
//			logger.log(Level.INFO, new String(encoder.encode(cypherText)));
//		}
//		catch (InvalidCipherTextException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		return cypherText;
//	}
	
//	public byte[] decrypt(byte[] cypherText) {
//		// decifratura
//		if (privateKey == null) {
//			logger.log(Level.SEVERE, "Engine not initialized. Returning");
//			return null;
//		}
//		engineIES.init(false, privateKey, publicKey, IESParam);
//		logger.log(Level.INFO, "Dencrypt initialized");
//
//		byte[] plainText = null;
//		try {
//			plainText = engineIES.processBlock(cypherText, 0, cypherText.length);
//			logger.log(Level.WARNING, "processBlock output (Len: " + plainText.length+")");
//			logger.log(Level.INFO, new String(plainText));
//		} catch (InvalidCipherTextException e1) {
//			e1.printStackTrace();
//		}
//		return plainText;
//	}

//
//	// TODO PRNG must be protected against concurrent access
//	public static SecureRandom PRNG = SecureRandom.getInstance("SHA256PRNG");
//
//	public static int NONCE_SIZE = 16;
//
//	public static int PRIV_KEY_SIZE = 16;
//
//	public static ECDomainParameters ecparms = CryptoHelper.prime192v1;
//
//
	public void initPRNG()
	{
		if (PRNG != null) {
			PRNG.setSeed(Runtime.getRuntime().freeMemory());
			PRNG.setSeed(System.currentTimeMillis());
			// TODO we cannot use this - it clashes with the preverifier
			// PRNG.setSeed(CryptoHelper.class.hashCode());
			// PRNG.setSeed(CryptoSMSMidlet.class.hashCode());
		}
	}
//
//	/**
//	 * encrypt a byte array with a public key using ECIES.
//	 * 
//	 * After the algorithm published in 'Guide to elliptic curve encryption'
//	 * (Hankerson, Menezes, Vanstone) Page 189ff
//	 * 
//	 * @param data
//	 * @return byte array with crypted triple (R,C,t)
//	 */
	public byte[] encrypt(byte[] data) {

		if (publicKey == null) {
			logger.log(Level.SEVERE, "Encryption engine not initialized. Returning");
			return null;
		}
		
		try {
			boolean failure = true;

			ECPoint publicPoint = ecParam.getCurve().decodePoint(
					publicKey.getQ().getEncoded());
			ECPublicKeyParameters pubKey = new ECPublicKeyParameters(
					publicPoint, ecParam);
			ECPrivateKeyParameters k = null;
			ECPublicKeyParameters R = null;
			ECPoint Z = null;

			initPRNG();

			while (failure) {

				BigInteger privNum = new BigInteger(PRNG.generateSeed(NONCE_SIZE));
				
				// select k
				k = new ECPrivateKeyParameters(privNum, ecParam); // this is just a container
				R = generatePublicKey(k); // R = kP

				// calculate Z=hkQ
				BigInteger z = new BigInteger(ecParam.getH().toByteArray()); // z=h
				z = z.multiply(privNum); // z=hk

				Z = pubKey.getQ(); // Z=Q
				if (Z instanceof ECPoint.F2m) {
					ECPoint.F2m Z2 = new ECPoint.F2m(Z.getCurve(), Z.getX(), Z.getY()); // clone
					Z = Z2.multiply(z); // Z=zQ <=> Z=hkQ
				}
				if (Z instanceof ECPoint.Fp) {
					ECPoint.Fp Z2 = new ECPoint.Fp(Z.getCurve(), Z.getX(), Z.getY());	// clone
					Z = Z2.multiply(z); // Z=zQ <=> Z=hkQ
				}
				if (!Z.isInfinity())
					failure = false; // see step 2
			}

			// step 3 and 4:
			// setup KDF(xz,R) - see new IESParameters(...)
			// pass the parameters for step4
			engineIES.init(true, (CipherParameters) k, (CipherParameters) pubKey,
					new IESParameters(Z.getX().toBigInteger().toByteArray(), R.getQ().getEncoded(), 64));

			// step 4 generate C and t
			byte[] cAndT = engineIES.processBlock(data, 0, data.length);

			logger.log(Level.INFO, "processBlock output (Len: " + cAndT.length+")");

			// collect the result in format
			// byte[0] = length of R (encoded)
			// R in encoded form
			// C and t as comming from the engine
			byte[] publicBytes = R.getQ().getEncoded();
			byte[] out = new byte[1 + publicBytes.length + cAndT.length];

			out[0] = (byte) publicBytes.length; // WARN this will crash if
			// key.length > 255
			System.arraycopy(publicBytes, 0, out, 1, publicBytes.length);
			System.arraycopy(cAndT, 0, out, 1 + publicBytes.length,
					cAndT.length);

			logger.log(Level.INFO, "Output cAndT + R (Len: " + out.length+")");

			return out;
		} catch (Exception excep) {
			logger.log(Level.SEVERE, "Encrypt Failed: " + excep.getMessage());
			excep.printStackTrace();
			return null;
		} catch (Throwable t) {
			return null;
		}

	}

	/**
	 * decrypt a byte array with a public key using ECIES.
	 * 
	 * After the algorithm published in 'Guide to elliptic curve encryption'
	 * (Hankerson, Menezes, Vanstone) Page 190ff
	 * 
	 * @param data
	 *            the encrypted data
	 * @param pair
	 *            the keypair of the receiver
	 * @return byte[] the decoded data
	 */
	public byte[] decrypt(byte[] data)	{ // throws InvalidCipherTextException {
		byte[] plainText = null;

		if (keyPair == null) {
			logger.log(Level.SEVERE, "Encryption engine not initialized. Returning");
			return null;
		}

		ECPrivateKeyParameters p = (ECPrivateKeyParameters) keyPair.getPrivate();

		// deserialize data into (R,C,t)
		int lengthOfR = data[0];
		if (lengthOfR > data.length)
		{
			logger.log(Level.SEVERE, "Currupted message: invalid lenght. Returning");
			return null;
		}
		byte[] RasBytes = new byte[lengthOfR];

		System.arraycopy(data, 1, RasBytes, 0, lengthOfR);
		ECPoint RPoint = ecParam.getCurve().decodePoint(RasBytes); // decode into R
		ECPublicKeyParameters R = new ECPublicKeyParameters(RPoint, ecParam); // this
		// is just a container

		byte[] cAndT = new byte[data.length - lengthOfR - 1];
		System.arraycopy(data, 1 + lengthOfR, cAndT, 0, data.length - lengthOfR
				- 1);

		// compute Z=hdR (step 2)
		BigInteger z = new BigInteger(ecParam.getH().toByteArray()); // z=h
		z = z.multiply(p.getD()); // z=hd

		ECPoint Z = R.getQ(); // Z=Q
		if (Z instanceof ECPoint.F2m) {
			ECPoint.F2m Z2 = new ECPoint.F2m(Z.getCurve(), Z.getX(), Z.getY()); // clone
			Z = Z2.multiply(z); // Z=zQ <=> Z=hdQ
		}
		if (Z instanceof ECPoint.Fp) {
			ECPoint.Fp Z2 = new ECPoint.Fp(Z.getCurve(), Z.getX(), Z.getY()); // clone
			Z = Z2.multiply(z); // Z=zQ <=> Z=hdQ
		}
		try {
			if (Z.isInfinity()) {
				throw new InvalidCipherTextException("Z is infinite"); // reject if
				// Z is infinite (see step 2)
			}
	
			// step 3
			// setup KDF(xz,R) - see new IESParameters(...)
			// pass the parameters for step4
			engineIES.init(false, (CipherParameters) p, (CipherParameters) R,
					new IESParameters(Z.getX().toBigInteger().toByteArray(), R
							.getQ().getEncoded(), 64));
	
			// step 4 and 5
			
			plainText = engineIES.processBlock(cAndT, 0, cAndT.length);

		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
		}
		return plainText;
	}

	/**
	 * generate a new keypair and wrap it in a KeyPairHelper Object show status
	 * messages if progess screen is not null.
	 * 
	 * @return KeyPairHelper
	 */
	public AsymmetricCipherKeyPair generateKeyPairHelper() throws Exception {

		initPRNG();
		BigInteger privNum = new BigInteger(PRNG.generateSeed(
				(int) Math.ceil((double) keySize / 8))).abs();

		ECPrivateKeyParameters priv = new ECPrivateKeyParameters(privNum, ecParam);
		logger.log(Level.INFO, "Private Key Lenght: " + priv.getD().bitLength());

		ECPublicKeyParameters pub = generatePublicKey(priv);

		AsymmetricCipherKeyPair acKeyPair = new AsymmetricCipherKeyPair(pub, priv);

		return acKeyPair;
	}

	/**
	 * generate public key for a private key/domain combination throws an
	 * exception if the point in the domain is of the wrong class
	 * 
	 * @param priv
	 *            ECPrivateKeyParameters private key
	 * @return ECPublicKeyParameters
	 */
	public ECPublicKeyParameters generatePublicKey(
			ECPrivateKeyParameters priv) throws Exception {
		ECDomainParameters domain = priv.getParameters();
		ECPoint dG = domain.getG();

		if (dG instanceof ECPoint.F2m) {
			ECPoint.F2m G = new ECPoint.F2m(dG.getCurve(), dG.getX(), dG.getY());
			return new ECPublicKeyParameters(G.multiply(priv.getD()), domain);
		}
		if (dG instanceof ECPoint.Fp) {
			ECPoint.Fp G = new ECPoint.Fp(dG.getCurve(), dG.getX(), dG.getY());
			ECPublicKeyParameters pub = new ECPublicKeyParameters(G.multiply(priv.getD()), domain);
			logger.log(Level.INFO, "Public Key Lenght (byte array lenght) : " + pub.getQ().getEncoded().length);
			return pub;
		}

		// we should not reach this point
		throw new Exception(
				"generate public key from unknown point type - check your domain");

	}
//
//	static byte[] privateKeyArrayFromACKeyPair(AsymmetricCipherKeyPair pair) {
//		ECPrivateKeyParameters priv = (ECPrivateKeyParameters) pair
//				.getPrivate();
//		BigInteger dd = priv.getD();
//		return dd.toByteArray();
//	}
//
//	static byte[] publicKeyArrayFromACKeyPair(AsymmetricCipherKeyPair pair) {
//		ECPublicKeyParameters pub = (ECPublicKeyParameters) pair.getPublic();
//		return (pub.getQ().getEncoded());
//	}
//
//	static AsymmetricCipherKeyPair ACKeyPairFromByteArrays(byte[] privKey,
//			byte[] pubKey) {
//		ECDomainParameters ecparms = CryptoHelper.prime192v1;
//		BigInteger privNum = new BigInteger(privKey);
//		ECPrivateKeyParameters privateKey = new ECPrivateKeyParameters(privNum,
//				ecparms);
//		ECPoint pubPoint = ecparms.getCurve().decodePoint(pubKey);
//		ECPublicKeyParameters publicKey = new ECPublicKeyParameters(pubPoint,
//				ecparms);
//		return new AsymmetricCipherKeyPair(publicKey, privateKey);
//
//	}
}

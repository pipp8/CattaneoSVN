package crymes.crypto.ecies;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.IESEngine;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.IESParameters;
import org.bouncycastle.math.BigInteger;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.security.SecureRandom;
import org.bouncycastle.util.encoders.Hex;

import crymes.crypto.Cipher;
import crymes.crypto.KeyPair;


public class ECIESCipher extends Cipher{

	public static final String ALG_ID = "ECIES";
	
	// copy from bouncy castle
	public static final ECCurve cFp192v1 = new ECCurve.Fp(new BigInteger(
	"6277101735386680763835789423207666416083908700390324961279"),
	new BigInteger("fffffffffffffffffffffffffffffffefffffffffffffffc",
			16), new BigInteger(
					"64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1", 16));

	// copy from bouncy castle
	public static final ECDomainParameters prime192v1 = new ECDomainParameters(
			cFp192v1,
			cFp192v1
			.decodePoint(Hex
					.decode("03188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012")),
					new BigInteger("ffffffffffffffffffffffff99def836146bc9b1b4d22831",
							16), BigInteger.valueOf(1), Hex
							.decode("3045AE6FC8422f64ED579528D38120EAE12196D5"));

	// 
	// TODO PRNG must be protected against concurrent access
	protected static SecureRandom PRNG = SecureRandom.getInstance("SHA256PRNG");

	public static int NONCE_SIZE = 16;

	public static int PRIV_KEY_SIZE = 16;

	public static ECDomainParameters ecparms = prime192v1;

	public ECIESCipher(){
		
		_cipher = this;	
	}



	public byte[] encrypt(byte[] data, KeyPair p) 
	throws Exception {
		ECIESKeyPair pair = null;
		if (p  instanceof ECIESKeyPair) {
			pair = (ECIESKeyPair) p ;
		} else {
			throw new Exception("type Key pair incorrect");
		}

		try {
		boolean failure = true;

		IESEngine engine = new IESEngine(new ECDHBasicAgreement(),
				new KDF2BytesGenerator(new SHA1Digest()), new HMac(
						new SHA1Digest()));

		ECPoint publicPoint = ecparms.getCurve().decodePoint(pair.getPublicKey());
		ECPublicKeyParameters pubKey = new ECPublicKeyParameters(
				publicPoint, ecparms);
		ECPrivateKeyParameters k = null;
		ECPublicKeyParameters R = null;
		ECPoint Z = null;

		initPRNG();

		while (failure) {

			BigInteger privNum = new BigInteger(PRNG
					.generateSeed(NONCE_SIZE)); // select k

			k = new ECPrivateKeyParameters(privNum, ecparms); // this is
			// just a container
			R = ECIESKeyPairGenerator.generatePublicKey(k); // R = kP

			// calculate Z=hkQ
			BigInteger z = new BigInteger(ecparms.getH().toByteArray()); // z=h
			z = z.multiply(privNum); // z=hk

			Z = pubKey.getQ(); // Z=Q
			if (Z instanceof ECPoint.F2m) {
				ECPoint.F2m Z2 = new ECPoint.F2m(Z.getCurve(), Z.getX(), Z
						.getY()); // clone
				Z = Z2.multiply(z); // Z=zQ <=> Z=hkQ
			}
			if (Z instanceof ECPoint.Fp) {
				ECPoint.Fp Z2 = new ECPoint.Fp(Z.getCurve(), Z.getX(), Z
						.getY()); // clone
				Z = Z2.multiply(z); // Z=zQ <=> Z=hkQ
			}
			if (!Z.isInfinity())
				failure = false; // see step 2
		}

		// step 3 and 4:
		// setup KDF(xz,R) - see new IESParameters(...)
		// pass the parameters for step4
		engine.init(true, (CipherParameters) k, (CipherParameters) pubKey,
				new IESParameters(Z.getX().toBigInteger().toByteArray(), R
						.getQ().getEncoded(), 64));

		// step 4 generate C and t
		byte[] cAndT = engine.processBlock(data, 0, data.length);			

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



		return out;
	} catch (Exception excep) {
		return null;
	} catch (Throwable t) {
		return null;
	}


	
}


public byte[] decrypt(byte[] data, KeyPair keyPair)
throws InvalidCipherTextException, Exception {
	ECIESKeyPair pair;
	if (keyPair  instanceof ECIESKeyPair) {
		pair = (ECIESKeyPair) keyPair ;
	} else {
		throw new Exception("type Key pair incorrect");
	}

	
	IESEngine engine = new IESEngine(new ECDHBasicAgreement(),
			new KDF2BytesGenerator(new SHA1Digest()), new HMac(
					new SHA1Digest()));

	
	ECPrivateKeyParameters p = (ECPrivateKeyParameters) pair.getKeyPair().getPrivate();

	// deserialize data into (R,C,t)
	int lengthOfR = data[0];
	if (lengthOfR > data.length)
		throw new InvalidCipherTextException("Lengthfield invalid");

	byte[] RasBytes = new byte[lengthOfR];

	System.arraycopy(data, 1, RasBytes, 0, lengthOfR);
	ECPoint RPoint = ecparms.getCurve().decodePoint(RasBytes); // decode into R
	ECPublicKeyParameters R = new ECPublicKeyParameters(RPoint, ecparms); // this
	// is just a container

	byte[] cAndT = new byte[data.length - lengthOfR - 1];
	System.arraycopy(data, 1 + lengthOfR, cAndT, 0, data.length - lengthOfR
			- 1);

	// compute Z=hdR (step 2)
	BigInteger z = new BigInteger(ecparms.getH().toByteArray()); // z=h
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
	if (Z.isInfinity()) {
		throw new InvalidCipherTextException("Z is infinite"); // reject if
		// Z is infinite (see step 2)
	}

	// step 3
	// setup KDF(xz,R) - see new IESParameters(...)
	// pass the parameters for step4
	engine.init(false, (CipherParameters) p, (CipherParameters) R,
			new IESParameters(Z.getX().toBigInteger().toByteArray(), R
					.getQ().getEncoded(), 64));

	// step 4 and 5
	byte[] clear = engine.processBlock(cAndT, 0, cAndT.length);
	return clear;
	
	
	
}

/**
 * put some randomness into the PRNG
 * 
 * TODO the randomness is questionable
 * 
 */
protected static void initPRNG() {

	if (PRNG != null) {
		PRNG.setSeed(Runtime.getRuntime().freeMemory());
		PRNG.setSeed(System.currentTimeMillis());
		// TODO we cannot use this - it clashes with the preverifier
		// PRNG.setSeed(CryptoHelper.class.hashCode());
		// PRNG.setSeed(CryptoSMSMidlet.class.hashCode());
	}
}
}

/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.crypto.ecies;

import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.BigInteger;
import org.bouncycastle.math.ec.ECPoint;





public class ECIESKeyPairGenerator implements IKeyPairGenerator {


	
	public KeyPair getKeyPairFromABytePair(byte[] pubKey,byte[] priKey){
		ECIESKeyPair c = new ECIESKeyPair(pubKey, priKey);
		return c;
	}

	public KeyPair generateKeyPair() throws Exception {

		ECIESCipher.initPRNG();
		
		BigInteger privNum = new BigInteger(ECIESCipher.PRNG.generateSeed(ECIESCipher.PRIV_KEY_SIZE))
				.abs();

		
		ECPrivateKeyParameters priv = new ECPrivateKeyParameters(privNum,
				ECIESCipher.ecparms);

		ECPublicKeyParameters pub = generatePublicKey(priv);

		AsymmetricCipherKeyPair acKeyPair = new AsymmetricCipherKeyPair(pub,
				priv);


		ECIESKeyPair eph = new ECIESKeyPair(acKeyPair);
		return eph;
	}

	/**
	 * generate public key for a private key/domain combination throws an
	 * exception if the point in the domain is of the wrong class
	 * 
	 * @param priv
	 *            ECPrivateKeyParameters private key
	 * @return ECPublicKeyParameters
	 */
	protected static ECPublicKeyParameters generatePublicKey(
			ECPrivateKeyParameters priv) throws Exception {
		ECDomainParameters domain = priv.getParameters();
		ECPoint dG = domain.getG();

		if (dG instanceof ECPoint.F2m) {
			ECPoint.F2m G = new ECPoint.F2m(dG.getCurve(), dG.getX(), dG.getY());
			return new ECPublicKeyParameters(G.multiply(priv.getD()), domain);
		}
		if (dG instanceof ECPoint.Fp) {
			ECPoint.Fp G = new ECPoint.Fp(dG.getCurve(), dG.getX(), dG.getY());
			return new ECPublicKeyParameters(G.multiply(priv.getD()), domain);
		}

		// we should not reach this point
		throw new Exception(
				"generate public key from unknown point type - check your domain");

	}



	

	
}

/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.crypto.ecies;

import jwhisper.crypto.KeyPair;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.BigInteger;
import org.bouncycastle.math.ec.ECPoint;



public class ECIESKeyPair extends KeyPair{
	
	private AsymmetricCipherKeyPair _keyPair;
	
	public ECIESKeyPair(AsymmetricCipherKeyPair pair) {
		_keyPair=pair;

		_privateKey= privateKeyArrayFromACKeyPair(pair);
		_publicKey= publicKeyArrayFromACKeyPair(pair);
	}
	
	public ECIESKeyPair(byte[] pubKey,byte[] priKey) {
		_privateKey=priKey;
		_publicKey=pubKey;

		if ( (pubKey != null) && (priKey != null) )
			_keyPair= ACKeyPairFromByteArrays(priKey,pubKey);
	}
	public AsymmetricCipherKeyPair getKeyPair() {
		return _keyPair;
	}
	
	
	private static byte[] privateKeyArrayFromACKeyPair(AsymmetricCipherKeyPair pair) {
		ECPrivateKeyParameters priv = (ECPrivateKeyParameters) pair
				.getPrivate();
		BigInteger dd = priv.getD();
		return dd.toByteArray();
	}
	
	private static byte[] publicKeyArrayFromACKeyPair(AsymmetricCipherKeyPair pair) {
		ECPublicKeyParameters pub = (ECPublicKeyParameters) pair.getPublic();
		return (pub.getQ().getEncoded());
	}

	private static AsymmetricCipherKeyPair ACKeyPairFromByteArrays(byte[] privKey,
			byte[] pubKey) {
		ECDomainParameters ecparms =ECIESCipher.prime192v1;
		BigInteger privNum = new BigInteger(privKey);
		ECPrivateKeyParameters privateKey = new ECPrivateKeyParameters(privNum,
				ecparms);
		ECPoint pubPoint = ecparms.getCurve().decodePoint(pubKey);
		ECPublicKeyParameters publicKey = new ECPublicKeyParameters(pubPoint,
				ecparms);
		return new AsymmetricCipherKeyPair(publicKey, privateKey);

	}
	
}

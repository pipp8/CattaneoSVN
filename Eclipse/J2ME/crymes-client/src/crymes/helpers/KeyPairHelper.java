/*
 * Copyright (C) 2007 Crypto Messaging with Elliptic Curves Cryptography (CRYMES)
 *
 * This file is part of Crypto Messaging with Elliptic Curves Cryptography (CRYMES).
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Crypto Messaging with Elliptic Curves Cryptography (CRYMES); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package crymes.helpers;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

public class KeyPairHelper {
	private byte[] _privateKeyBA;
	private byte[] _publicKeyBA;
	private AsymmetricCipherKeyPair _keyPair;
	
	public KeyPairHelper(AsymmetricCipherKeyPair pair) {
		_keyPair=pair;
//		ECPrivateKeyParameters priv = (ECPrivateKeyParameters) pair.getPrivate();
//		BigInteger dd = priv.getD();
//		_privateKeyBA = dd.toByteArray();
//		ECPublicKeyParameters pub = (ECPublicKeyParameters) pair.getPublic();
//		_publicKeyBA=(pub.getQ().getEncoded());
		_privateKeyBA=CryptoHelper.privateKeyArrayFromACKeyPair(pair);
		_publicKeyBA=CryptoHelper.publicKeyArrayFromACKeyPair(pair);
	}
	public KeyPairHelper(byte[] pubKey,byte[] priKey) {
		_privateKeyBA=priKey;
		_publicKeyBA=pubKey;
//		ECDomainParameters ecparms = CryptoHelper.prime192v1;
//		BigInteger privNum = new BigInteger(priKey);
//		ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privNum, ecparms);
//		ECPoint pubPoint = ecparms.getCurve().decodePoint(pubKey);
//		ECPublicKeyParameters pubKey1 = new ECPublicKeyParameters(pubPoint, ecparms);
//		_keyPair = new AsymmetricCipherKeyPair(pubKey1, privKey);
		_keyPair=CryptoHelper.ACKeyPairFromByteArrays(priKey,pubKey);
	}
	public AsymmetricCipherKeyPair getKeyPair() {
		return _keyPair;
	}
	public byte[] getPrivateKeyBA() {
		return _privateKeyBA;
	}
	public byte[] getPublicKeyBA() {
		return _publicKeyBA;
	}
	

}

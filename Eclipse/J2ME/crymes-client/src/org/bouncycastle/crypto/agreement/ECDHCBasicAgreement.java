package org.bouncycastle.crypto.agreement;


import org.bouncycastle.math.BigInteger;
import org.bouncycastle.math.ec.ECPoint;

import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;

/**
 * P1363 7.2.2 ECSVDP-DHC
 *
 * ECSVDP-DHC is Elliptic Curve Secret Value Derivation Primitive,
 * Diffie-Hellman version with cofactor multiplication. It is based on
 * the work of [DH76], [Mil86], [Kob87], [LMQ98] and [Kal98a]. This
 * primitive derives a shared secret value from one party's private key
 * and another party's public key, where both have the same set of EC
 * domain parameters. If two parties correctly execute this primitive,
 * they will produce the same output. This primitive can be invoked by a
 * scheme to derive a shared secret key; specifically, it may be used
 * with the schemes ECKAS-DH1 and DL/ECKAS-DH2. It does not assume the
 * validity of the input public key (see also Section 7.2.1).
 * <p>
 * Note: As stated P1363 compatability mode with ECDH can be preset, and
 * in this case the implementation doesn't have a ECDH compatability mode
 * (if you want that just use ECDHBasicAgreement and note they both implement
 * BasicAgreement!).
 */
public class ECDHCBasicAgreement
    implements BasicAgreement
{
    ECPrivateKeyParameters key;

    public void init(
        CipherParameters key)
    {
        this.key = (ECPrivateKeyParameters)key;
    }

    public BigInteger calculateAgreement(
        CipherParameters pubKey)
    {
        ECPublicKeyParameters   pub = (ECPublicKeyParameters)pubKey;
        ECDomainParameters      params = pub.getParameters();
        ECPoint P = pub.getQ().multiply(params.getH().multiply(key.getD()));

        // if (p.isInfinity()) throw new RuntimeException("Invalid public key");

        return P.getX().toBigInteger();
    }
}

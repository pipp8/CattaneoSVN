package org.bouncycastle.crypto.signers;


import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.security.SecureRandom;

/**
 * RSA-PSS as described in PKCS# 1 v 2.1.
 * <p>
 * Note: the usual value for the salt length is the number of
 * bytes in the hash function.
 */
public class PSSSigner
    implements Signer
{
    static final public byte   TRAILER_IMPLICIT    = (byte)0xBC;

    private Digest                      digest;
    private AsymmetricBlockCipher       cipher;
    private SecureRandom                random;

    private int                         hLen;
    private int                         sLen;
    private int                         emBits;
    private byte[]                      salt;
    private byte[]                      mDash;
    private byte[]                      block;
    private byte                        trailer;

    /**
     * basic constructor
     *
     * @param cipher the assymetric cipher to use.
     * @param digest the digest to use.
     * @param sLen the length of the salt to use (in bytes).
     */
    public PSSSigner(
        AsymmetricBlockCipher   cipher,
        Digest                  digest,
        int                     sLen)
    {
        this(cipher, digest, sLen, TRAILER_IMPLICIT);
    }
    
    public PSSSigner(
        AsymmetricBlockCipher   cipher,
        Digest                  digest,
        int                     sLen,
        byte                    trailer)
    {
        this.cipher = cipher;
        this.digest = digest;
        this.hLen = digest.getDigestSize();
        this.sLen = sLen;
        this.salt = new byte[sLen];
        this.mDash = new byte[8 + sLen + hLen];
        this.trailer = trailer;
    }

    public void init(
        boolean                 forSigning,
        CipherParameters        param)
    {
        RSAKeyParameters  kParam = null;

        if (param instanceof ParametersWithRandom)
        {
            ParametersWithRandom    p = (ParametersWithRandom)param;

            kParam = (RSAKeyParameters)p.getParameters();
            random = p.getRandom();
        }
        else
        {
            kParam = (RSAKeyParameters)param;
            if (forSigning)
            {
                random = new SecureRandom();
            }
        }

        cipher.init(forSigning, kParam);

        emBits = kParam.getModulus().bitLength() - 1;

        block = new byte[(emBits + 7) / 8];

        reset();
    }

    /**
     * clear possible sensitive data
     */
    private void clearBlock(
        byte[]  block)
    {
        for (int i = 0; i != block.length; i++)
        {
            block[i] = 0;
        }
    }

    /**
     * update the internal digest with the byte b
     */
    public void update(
        byte    b)
    {
        digest.update(b);
    }

    /**
     * update the internal digest with the byte array in
     */
    public void update(
        byte[]  in,
        int     off,
        int     len)
    {
        digest.update(in, off, len);
    }

    /**
     * reset the internal state
     */
    public void reset()
    {
        digest.reset();
    }

    /**
     * generate a signature for the message we've been loaded with using
     * the key we were initialised with.
     */
    public byte[] generateSignature()
        throws CryptoException, DataLengthException
    {
        if (emBits < (8 * hLen + 8 * sLen + 9))
        {
            throw new DataLengthException("encoding error");
        }

        digest.doFinal(mDash, mDash.length - hLen - sLen);

        if (sLen != 0)
        {
            random.nextBytes(salt);

            System.arraycopy(salt, 0, mDash, mDash.length - sLen, sLen);
        }

        byte[]  h = new byte[hLen];

        digest.update(mDash, 0, mDash.length);

        digest.doFinal(h, 0);

        block[block.length - sLen - 1 - hLen - 1] = 0x01;
        System.arraycopy(salt, 0, block, block.length - sLen - hLen - 1, sLen);

        byte[] dbMask = maskGeneratorFunction1(h, 0, h.length, block.length - hLen - 1);
        for (int i = 0; i != dbMask.length; i++)
        {
            block[i] ^= dbMask[i];
        }

        block[0] &= (0xff >> ((block.length * 8) - emBits));

        System.arraycopy(h, 0, block, block.length - hLen - 1, hLen);

        block[block.length - 1] = trailer;

        byte[]  b = cipher.processBlock(block, 0, block.length);

        clearBlock(block);

        return b;
    }

    /**
     * return true if the internal state represents the signature described
     * in the passed in array.
     */
    public boolean verifySignature(
        byte[]      signature)
    {
        if (emBits < (8 * hLen + 8 * sLen + 9))
        {
            return false;
        }

        digest.doFinal(mDash, mDash.length - hLen - sLen);

        try
        {
            byte[] b = cipher.processBlock(signature, 0, signature.length);
            System.arraycopy(b, 0, block, block.length - b.length, b.length);
        }
        catch (Exception e)
        {
            return false;
        }

        if (block[block.length - 1] != trailer)
        {
            clearBlock(block);
            return false;
        }

        byte[] dbMask = maskGeneratorFunction1(block, block.length - hLen - 1, hLen, block.length - hLen - 1);

        for (int i = 0; i != dbMask.length; i++)
        {
            block[i] ^= dbMask[i];
        }

        block[0] &= (0xff >> ((block.length * 8) - emBits));

        for (int i = 0; i != block.length - hLen - sLen - 2; i++)
        {
            if (block[i] != 0)
            {
                clearBlock(block);
                return false;
            }
        }

        if (block[block.length - hLen - sLen - 2] != 0x01)
        {
            clearBlock(block);
            return false;
        }

        System.arraycopy(block, block.length - sLen - hLen - 1, mDash, mDash.length - sLen, sLen);

        digest.update(mDash, 0, mDash.length);
        digest.doFinal(mDash, mDash.length - hLen);

        for (int i = block.length - hLen - 1, j = mDash.length - hLen;
                                                 j != mDash.length; i++, j++)
        {
            if ((block[i] ^ mDash[j]) != 0)
            {
                clearBlock(mDash);
                clearBlock(block);
                return false;
            }
        }

        clearBlock(mDash);
        clearBlock(block);

        return true;
    }

    /**
     * int to octet string.
     */
    private void ItoOSP(
        int     i,
        byte[]  sp)
    {
        sp[0] = (byte)(i >>> 24);
        sp[1] = (byte)(i >>> 16);
        sp[2] = (byte)(i >>> 8);
        sp[3] = (byte)(i >>> 0);
    }

    /**
     * mask generator function, as described in PKCS1v2.
     */
    private byte[] maskGeneratorFunction1(
        byte[]  Z,
        int     zOff,
        int     zLen,
        int     length)
    {
        byte[]  mask = new byte[length];
        byte[]  hashBuf = new byte[hLen];
        byte[]  C = new byte[4];
        int     counter = 0;

        digest.reset();

        while (counter < (length / hLen))
        {
            ItoOSP(counter, C);

            digest.update(Z, zOff, zLen);
            digest.update(C, 0, C.length);
            digest.doFinal(hashBuf, 0);

            System.arraycopy(hashBuf, 0, mask, counter * hLen, hLen);
            
            counter++;
        }

        if ((counter * hLen) < length)
        {
            ItoOSP(counter, C);

            digest.update(Z, zOff, zLen);
            digest.update(C, 0, C.length);
            digest.doFinal(hashBuf, 0);

            System.arraycopy(hashBuf, 0, mask, counter * hLen, mask.length - (counter * hLen));
        }

        return mask;
    }
}

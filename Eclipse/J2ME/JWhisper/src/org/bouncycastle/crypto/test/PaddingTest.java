package org.bouncycastle.crypto.test;


import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.ISO10126d2Padding;
import org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.TBCPadding;
import org.bouncycastle.crypto.paddings.X923Padding;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.security.SecureRandom;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.test.SimpleTest;

/**
 * General Padding tests.
 */
public class PaddingTest
    extends SimpleTest
{
    public PaddingTest()
    {
    }

    private void blockCheck(
        PaddedBufferedBlockCipher   cipher,
        BlockCipherPadding          padding,
        KeyParameter                key,
        byte[]                      data)
    {
        byte[]  out = new byte[data.length + 8];
        byte[]  dec = new byte[data.length];
        
        try
        {                
            cipher.init(true, key);
            
            int    len = cipher.processBytes(data, 0, data.length, out, 0);
            
            len += cipher.doFinal(out, len);
            
            cipher.init(false, key);
            
            int    decLen = cipher.processBytes(out, 0, len, dec, 0);
            
            decLen += cipher.doFinal(dec, decLen);
            
            if (!areEqual(data, dec))
            {
                fail("failed to decrypt - i = " + data.length + ", padding = " + padding.getPaddingName());
            }
        }
        catch (Exception e)
        {
            fail("Exception - " + e.toString(), e);
        }
    }
    
    public void testPadding(
        BlockCipherPadding  padding,
        SecureRandom        rand,
        byte[]              ffVector,
        byte[]              ZeroVector)
    {
        PaddedBufferedBlockCipher    cipher = new PaddedBufferedBlockCipher(new DESEngine(), padding);
        KeyParameter                 key = new KeyParameter(Hex.decode("0011223344556677"));
        
        //
        // ff test
        //
        byte[]    data = { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0 };
        
        if (ffVector != null)
        {
            padding.addPadding(data, 3);
            
            if (!areEqual(data, ffVector))
            {
                fail("failed ff test for " + padding.getPaddingName());
            }
        }
        
        //
        // zero test
        //
        if (ZeroVector != null)
        {
            data = new byte[8];
            padding.addPadding(data, 4);
            
            if (!areEqual(data, ZeroVector))
            {
                fail("failed zero test for " + padding.getPaddingName());
            }
        }
        
        for (int i = 1; i != 200; i++)
        {
            data = new byte[i];
            
            rand.nextBytes(data);

            blockCheck(cipher, padding, key, data);
        }
    }
    
    public void performTest()
    {
        SecureRandom    rand = new SecureRandom(new byte[20]);
        
        rand.setSeed(System.currentTimeMillis());
        
        testPadding(new PKCS7Padding(), rand,
                                    Hex.decode("ffffff0505050505"),
                                    Hex.decode("0000000004040404"));

        testPadding(new ISO10126d2Padding(), rand,
                                    null,
                                    null);
        
        testPadding(new X923Padding(), rand,
                                    null,
                                    null);

        testPadding(new TBCPadding(), rand,
                                    Hex.decode("ffffff0000000000"),
                                    Hex.decode("00000000ffffffff"));

        testPadding(new ZeroBytePadding(), rand,
                                    Hex.decode("ffffff0000000000"),
                                    null);
        
        testPadding(new ISO7816d4Padding(), rand,
                                    Hex.decode("ffffff8000000000"),
                                    Hex.decode("0000000080000000"));
    }

    public String getName()
    {
        return "PaddingTest";
    }

    public static void main(
        String[]    args)
    {
        runTest(new PaddingTest());
    }
}

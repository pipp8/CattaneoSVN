package org.bouncycastle.crypto.test;


import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.DHAgreement;
import org.bouncycastle.crypto.agreement.DHBasicAgreement;
import org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator;
import org.bouncycastle.crypto.generators.DHKeyPairGenerator;
import org.bouncycastle.crypto.generators.DHParametersGenerator;
import org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.math.BigInteger;
import org.bouncycastle.security.SecureRandom;
import org.bouncycastle.util.test.SimpleTest;

public class DHTest
    extends SimpleTest
{
    private BigInteger g512 = new BigInteger("153d5d6172adb43045b68ae8e1de1070b6137005686d29d3d73a7749199681ee5b212c9b96bfdcfa5b20cd5e3fd2044895d609cf9b410b7a0f12ca1cb9a428cc", 16);
    private BigInteger p512 = new BigInteger("9494fec095f3b85ee286542b3836fc81a5dd0a0349b4c239dd38744d488cf8e31db8bcb7d33b41abb9e5a33cca9144b1cef332c94bf0573bf047a3aca98cdf3b", 16);

    private BigInteger g768 = new BigInteger("7c240073c1316c621df461b71ebb0cdcc90a6e5527e5e126633d131f87461c4dc4afc60c2cb0f053b6758871489a69613e2a8b4c8acde23954c08c81cbd36132cfd64d69e4ed9f8e51ed6e516297206672d5c0a69135df0a5dcf010d289a9ca1", 16);
    private BigInteger p768 = new BigInteger("8c9dd223debed1b80103b8b309715be009d48860ed5ae9b9d5d8159508efd802e3ad4501a7f7e1cfec78844489148cd72da24b21eddd01aa624291c48393e277cfc529e37075eccef957f3616f962d15b44aeab4039d01b817fde9eaa12fd73f", 16);

    private BigInteger  g1024 = new BigInteger("1db17639cdf96bc4eabba19454f0b7e5bd4e14862889a725c96eb61048dcd676ceb303d586e30f060dbafd8a571a39c4d823982117da5cc4e0f89c77388b7a08896362429b94a18a327604eb7ff227bffbc83459ade299e57b5f77b50fb045250934938efa145511166e3197373e1b5b1e52de713eb49792bedde722c6717abf", 16);
    private BigInteger  p1024 = new BigInteger("a00e283b3c624e5b2b4d9fbc2653b5185d99499b00fd1bf244c6f0bb817b4d1c451b2958d62a0f8a38caef059fb5ecd25d75ed9af403f5b5bdab97a642902f824e3c13789fed95fa106ddfe0ff4a707c85e2eb77d49e68f2808bcea18ce128b178cd287c6bc00efa9a1ad2a673fe0dceace53166f75b81d6709d5f8af7c66bb7", 16);

    public String getName()
    {
        return "DH";
    }

    private void testDH(
        int         size,
        BigInteger  g,
        BigInteger  p)
    {
        DHKeyPairGenerator kpGen = getDHKeyPairGenerator(g, p);

        //
        // generate first pair
        //
        AsymmetricCipherKeyPair     pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu1 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv1 = (DHPrivateKeyParameters)pair.getPrivate();
        //
        // generate second pair
        //
        pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu2 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv2 = (DHPrivateKeyParameters)pair.getPrivate();

        //
        // two way
        //
        DHAgreement    e1 = new DHAgreement();
        DHAgreement    e2 = new DHAgreement();

        e1.init(pv1);
        e2.init(pv2);

        BigInteger  m1 = e1.calculateMessage();
        BigInteger  m2 = e2.calculateMessage();

        BigInteger   k1 = e1.calculateAgreement(pu2, m2);
        BigInteger   k2 = e2.calculateAgreement(pu1, m1);

        if (!k1.equals(k2))
        {
            fail(size + " bit 2-way test failed");
        }
    }

    private void testDHBasic(
        int         size,
        int         privateValueSize,
        BigInteger  g,
        BigInteger  p)
    {
        DHBasicKeyPairGenerator kpGen = getDHBasicKeyPairGenerator(g, p, privateValueSize);

        //
        // generate first pair
        //
        AsymmetricCipherKeyPair     pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu1 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv1 = (DHPrivateKeyParameters)pair.getPrivate();

        checkKeySize(privateValueSize, pv1);
        //
        // generate second pair
        //
        pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu2 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv2 = (DHPrivateKeyParameters)pair.getPrivate();

        checkKeySize(privateValueSize, pv2);
        //
        // two way
        //
        DHBasicAgreement    e1 = new DHBasicAgreement();
        DHBasicAgreement    e2 = new DHBasicAgreement();

        e1.init(pv1);
        e2.init(pv2);

        BigInteger   k1 = e1.calculateAgreement(pu2);
        BigInteger   k2 = e2.calculateAgreement(pu1);

        if (!k1.equals(k2))
        {
            fail("basic " + size + " bit 2-way test failed");
        }
    }

    private void checkKeySize(
        int privateValueSize,
        DHPrivateKeyParameters priv)
    {
        if (privateValueSize != 0)
        {
            if (priv.getX().bitLength() != privateValueSize)
            {
                fail("limited key check failed for key size " + privateValueSize);
            }
        }
    }

    private void testGPWithRandom(
        DHKeyPairGenerator kpGen)
    {
        //
        // generate first pair
        //
        AsymmetricCipherKeyPair     pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu1 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv1 = (DHPrivateKeyParameters)pair.getPrivate();
        //
        // generate second pair
        //
        pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu2 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv2 = (DHPrivateKeyParameters)pair.getPrivate();

        //
        // two way
        //
        DHAgreement    e1 = new DHAgreement();
        DHAgreement    e2 = new DHAgreement();

        e1.init(new ParametersWithRandom(pv1, new SecureRandom()));
        e2.init(new ParametersWithRandom(pv2, new SecureRandom()));

        BigInteger   m1 = e1.calculateMessage();
        BigInteger   m2 = e2.calculateMessage();

        BigInteger   k1 = e1.calculateAgreement(pu2, m2);
        BigInteger   k2 = e2.calculateAgreement(pu1, m1);
        
        if (!k1.equals(k2))
        {
            fail("basic with random 2-way test failed");
        }
    }
    
    private void testSimpleWithRandom(
        DHBasicKeyPairGenerator kpGen)
    {
        //
        // generate first pair
        //
        AsymmetricCipherKeyPair     pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu1 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv1 = (DHPrivateKeyParameters)pair.getPrivate();
        //
        // generate second pair
        //
        pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu2 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv2 = (DHPrivateKeyParameters)pair.getPrivate();

        //
        // two way
        //
        DHBasicAgreement    e1 = new DHBasicAgreement();
        DHBasicAgreement    e2 = new DHBasicAgreement();

        e1.init(new ParametersWithRandom(pv1, new SecureRandom()));
        e2.init(new ParametersWithRandom(pv2, new SecureRandom()));

        BigInteger   k1 = e1.calculateAgreement(pu2);
        BigInteger   k2 = e2.calculateAgreement(pu1);

        if (!k1.equals(k2))
        {
            fail("basic with random 2-way test failed");
        }
    }

    private DHBasicKeyPairGenerator getDHBasicKeyPairGenerator(
        BigInteger g,
        BigInteger p,
        int        privateValueSize)
    {
        DHParameters                dhParams = new DHParameters(p, g, null, privateValueSize);
        DHKeyGenerationParameters   params = new DHKeyGenerationParameters(new SecureRandom(), dhParams);
        DHBasicKeyPairGenerator     kpGen = new DHBasicKeyPairGenerator();

        kpGen.init(params);
        
        return kpGen;
    }
    
    private DHKeyPairGenerator getDHKeyPairGenerator(
        BigInteger g,
        BigInteger p)
    {
        DHParameters                dhParams = new DHParameters(p, g);
        DHKeyGenerationParameters   params = new DHKeyGenerationParameters(new SecureRandom(), dhParams);
        DHKeyPairGenerator          kpGen = new DHKeyPairGenerator();

        kpGen.init(params);
        
        return kpGen;
    }
    
    /**
     * this test is can take quiet a while
     */
    private void testGeneration(
        int         size)
    {
        DHParametersGenerator       pGen = new DHParametersGenerator();

        pGen.init(size, 10, new SecureRandom());

        DHParameters                dhParams = pGen.generateParameters();

        DHKeyGenerationParameters   params = new DHKeyGenerationParameters(new SecureRandom(), dhParams);

        DHBasicKeyPairGenerator     kpGen = new DHBasicKeyPairGenerator();

        kpGen.init(params);

        //
        // generate first pair
        //
        AsymmetricCipherKeyPair     pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu1 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv1 = (DHPrivateKeyParameters)pair.getPrivate();
        
        //
        // generate second pair
        //
        params = new DHKeyGenerationParameters(new SecureRandom(), pu1.getParameters());

        kpGen.init(params);

        pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu2 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv2 = (DHPrivateKeyParameters)pair.getPrivate();

        //
        // two way
        //
        DHBasicAgreement    e1 = new DHBasicAgreement();
        DHBasicAgreement    e2 = new DHBasicAgreement();

        e1.init(new ParametersWithRandom(pv1, new SecureRandom()));
        e2.init(new ParametersWithRandom(pv2, new SecureRandom()));

        BigInteger   k1 = e1.calculateAgreement(pu2);
        BigInteger   k2 = e2.calculateAgreement(pu1);

        if (!k1.equals(k2))
        {
            fail("basic with " + size + " bit 2-way test failed");
        }
    }

    public void performTest()
    {
        testDHBasic(512, 0, g512, p512);
        testDHBasic(768, 0, g768, p768);
        testDHBasic(1024, 0, g1024, p1024);

        testDHBasic(512, 64, g512, p512);
        testDHBasic(768, 128, g768, p768);
        testDHBasic(1024, 256, g1024, p1024);

        testDH(512, g512, p512);
        testDH(768, g768, p768);
        testDH(1024, g1024, p1024);

        //
        // generation test.
        //
        testGeneration(256);
        
        //
        // with random test
        //
        DHBasicKeyPairGenerator     kpBasicGen = getDHBasicKeyPairGenerator(g512, p512, 0);
        
        testSimpleWithRandom(kpBasicGen);
        
        DHKeyPairGenerator          kpGen = getDHKeyPairGenerator(g512, p512);
        
        testGPWithRandom(kpGen);
        
        //
        // parameter tests
        //
        DHAgreement             dh = new DHAgreement();
        AsymmetricCipherKeyPair dhPair = kpGen.generateKeyPair();
        
        try
        {
            dh.init(dhPair.getPublic());
            fail("DHAgreement key check failed");
        }
        catch (IllegalArgumentException e)
        {
            // ignore
        }
        
        DHKeyPairGenerator      kpGen768 = getDHKeyPairGenerator(g768, p768);
        
        try
        {
            dh.init(dhPair.getPrivate());
            
            dh.calculateAgreement((DHPublicKeyParameters)kpGen768.generateKeyPair().getPublic(), BigInteger.valueOf(100));
            
            fail("DHAgreement agreement check failed");
        }
        catch (IllegalArgumentException e)
        {
            // ignore
        }
        
        DHBasicAgreement        dhBasic = new DHBasicAgreement();
        AsymmetricCipherKeyPair dhBasicPair = kpBasicGen.generateKeyPair();
 
        try
        {
            dhBasic.init(dhBasicPair.getPublic());
            fail("DHBasicAgreement key check failed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        
        DHBasicKeyPairGenerator      kpBasicGen768 = getDHBasicKeyPairGenerator(g768, p768, 0);
        
        try
        {
            dhBasic.init(dhPair.getPrivate());
            
            dhBasic.calculateAgreement((DHPublicKeyParameters)kpBasicGen768.generateKeyPair().getPublic());
            
            fail("DHBasicAgreement agreement check failed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    public static void main(
        String[]    args)
    {
        runTest(new DHTest());
    }
}

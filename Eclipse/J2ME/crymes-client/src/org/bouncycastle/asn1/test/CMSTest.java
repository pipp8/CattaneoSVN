package org.bouncycastle.asn1.test;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.CompressedData;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.EnvelopedData;
import org.bouncycastle.asn1.cms.KEKRecipientInfo;
import org.bouncycastle.asn1.cms.KeyTransRecipientInfo;
import org.bouncycastle.asn1.cms.RecipientInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.test.SimpleTestResult;
import org.bouncycastle.util.test.Test;
import org.bouncycastle.util.test.TestResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class CMSTest
    implements Test
{
    //
    // compressed data object
    //
    byte[]  compData = Base64.decode(
            "MIAGCyqGSIb3DQEJEAEJoIAwgAIBADANBgsqhkiG9w0BCRADCDCABgkqhkiG9w0BBwGggCSABIIC"
          + "Hnic7ZRdb9owFIbvK/k/5PqVYPFXGK12YYyboVFASSp1vQtZGiLRACZE49/XHoUW7S/0tXP8Efux"
          + "fU5ivWnasml72XFb3gb5druui7ytN803M570nii7C5r8tfwR281hy/p/KSM3+jzH5s3+pbQ90xSb"
          + "P3VT3QbLusnt8WPIuN5vN/vaA2+DulnXTXkXvNTr8j8ouZmkCmGI/UW+ZS/C8zP0bz2dz0zwLt+1"
          + "UEk2M8mlaxjRMByAhZTj0RGYg4TvogiRASROsZgjpVcJCb1KV6QzQeDJ1XkoQ5Jm+C5PbOHZZGRi"
          + "v+ORAcshOGeCcdFJyfgFxdtCdEcmOrbinc/+BBMzRThEYpwl+jEBpciSGWQkI0TSlREmD/eOHb2D"
          + "SGLuESm/iKUFt1y4XHBO2a5oq0IKJKWLS9kUZTA7vC5LSxYmgVL46SIWxIfWBQd6AdrnjLmH94UT"
          + "vGxVibLqRCtIpp4g2qpdtqK1LiOeolpVK5wVQ5P7+QjZAlrh0cePYTx/gNZuB9Vhndtgujl9T/tg"
          + "W9ogK+3rnmg3YWygnTuF5GDS+Q/jIVLnCcYZFc6Kk/+c80wKwZjwdZIqDYWRH68MuBQSXLgXYXj2"
          + "3CAaYOBNJMliTl0X7eV5DnoKIFSKYdj3cRpD/cK/JWTHJRe76MUXnfBW8m7Hd5zhQ4ri2NrVF/WL"
          + "+kV1/3AGSlJ32bFPd2BsQD8uSzIx6lObkjdz95c0AAAAAAAAAAAAAAAA");
    
    //
    // enveloepd data
    //
    byte[]   envDataKeyTrns = Base64.decode(
            "MIAGCSqGSIb3DQEHA6CAMIACAQAxgcQwgcECAQAwKjAlMRYwFAYDVQQKEw1Cb3Vu"
          + "Y3kgQ2FzdGxlMQswCQYDVQQGEwJBVQIBCjANBgkqhkiG9w0BAQEFAASBgC5vdGrB"
          + "itQSGwifLf3KwPILjaB4WEXgT/IIO1KDzrsbItCJsMA0Smq2y0zptxT0pSRL6JRg"
          + "NMxLk1ySnrIrvGiEPLMR1zjxlT8yQ6VLX+kEoK43ztd1aaLw0oBfrcXcLN7BEpZ1"
          + "TIdjlBfXIOx1S88WY1MiYqJJFc3LMwRUaTEDMIAGCSqGSIb3DQEHATAdBglghkgB"
          + "ZQMEARYEEAfxLMWeaBOTTZQwUq0Y5FuggAQgwOJhL04rjSZCBCSOv5i5XpFfGsOd"
          + "YSHSqwntGpFqCx4AAAAAAAAAAAAA");
    
    byte[]   envDataKEK = Base64.decode(
            "MIAGCSqGSIb3DQEHA6CAMIACAQIxUqJQAgEEMAcEBQECAwQFMBAGCyqGSIb3DQEJE"
          + "AMHAgE6BDC7G/HyUPilIrin2Yeajqmj795VoLWETRnZAAFcAiQdoQWyz+oCh6WY/H"
          + "jHHi+0y+cwgAYJKoZIhvcNAQcBMBQGCCqGSIb3DQMHBAiY3eDBBbF6naCABBiNdzJb"
          + "/v6+UZB3XXKipxFDUpz9GyjzB+gAAAAAAAAAAAAA");
    
    //
    // signed data
    //
    byte[]   signedData = Base64.decode(
            "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"
          + "a+fEv4J8wQ8Xwvk0C8iMAAAAAAAA");

    private boolean isSameAs(
        byte[]  a,
        byte[]  b)
    {
        if (a.length != b.length)
        {
            return false;
        }
        
        for (int i = 0; i != a.length; i++)
        {
            if (a[i] != b[i])
            {
                return false;
            }
        }
        
        return true;
    }
    
    private TestResult compressionTest()
    {
        try
        {
            ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(compData));
            
            ContentInfo     info = ContentInfo.getInstance(aIn.readObject());
            CompressedData  data = CompressedData.getInstance(info.getContent());
            
            data = new CompressedData(data.getCompressionAlgorithmIdentifier(), data.getEncapContentInfo());
            info = new ContentInfo(CMSObjectIdentifiers.compressedData, data);
            
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
            
            aOut.writeObject(info);
            
            if (!isSameAs(bOut.toByteArray(), compData))
            {
                return new SimpleTestResult(false, getName() + ": CMS compression failed to re-encode");
            }
            
            return new SimpleTestResult(true, getName() + ": Okay");
        }
        catch (Exception e)
        {
            return new SimpleTestResult(false, getName() + ": CMS compression failed - " + e.toString(), e);
        }
    }
    
    private TestResult envelopedTest()
    {
        try
        {
            //
            // Key trans
            //
            ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(envDataKeyTrns));
            
            ContentInfo     info = ContentInfo.getInstance(aIn.readObject());
            EnvelopedData   envData = EnvelopedData.getInstance(info.getContent());
            ASN1Set         s = envData.getRecipientInfos();
            
            if (s.size() != 1)
            {
                return new SimpleTestResult(false, getName() + ": CMS KeyTrans enveloped, wrong number of recipients");
            }
            
            RecipientInfo   recip = RecipientInfo.getInstance(s.getObjectAt(0));

            if (recip.getInfo() instanceof KeyTransRecipientInfo)
            {
                KeyTransRecipientInfo   inf = KeyTransRecipientInfo.getInstance(recip.getInfo());
                
                inf = new KeyTransRecipientInfo(inf.getRecipientIdentifier(), inf.getKeyEncryptionAlgorithm(), inf.getEncryptedKey());
                
                s = new DERSet(new RecipientInfo(inf));
            }
            else
            {
                return new SimpleTestResult(false, getName() + ": CMS KeyTrans enveloped, wrong recipient type");
            }

            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
            
            envData = new EnvelopedData(envData.getOriginatorInfo(), s, envData.getEncryptedContentInfo(), envData.getUnprotectedAttrs());
            info = new ContentInfo(CMSObjectIdentifiers.envelopedData, envData);
            
            aOut.writeObject(info);
            
            if (!isSameAs(bOut.toByteArray(), envDataKeyTrns))
            {
                return new SimpleTestResult(false, getName() + ": CMS KeyTrans enveloped failed to re-encode");
            }
            
            //
            // KEK
            //
            aIn = new ASN1InputStream(new ByteArrayInputStream(envDataKEK));
            
            info = ContentInfo.getInstance(aIn.readObject());
            envData = EnvelopedData.getInstance(info.getContent());
            s = envData.getRecipientInfos();
            
            if (s.size() != 1)
            {
                return new SimpleTestResult(false, getName() + ": CMS KEK enveloped, wrong number of recipients");
            }
            
            recip = RecipientInfo.getInstance(s.getObjectAt(0));

            if (recip.getInfo() instanceof KEKRecipientInfo)
            {
                KEKRecipientInfo   inf = KEKRecipientInfo.getInstance(recip.getInfo());
                
                inf = new KEKRecipientInfo(inf.getKekid(), inf.getKeyEncryptionAlgorithm(), inf.getEncryptedKey());
                
                s = new DERSet(new RecipientInfo(inf));
            }
            else
            {
                return new SimpleTestResult(false, getName() + ": CMS KEK enveloped, wrong recipient type");
            }
            
            bOut = new ByteArrayOutputStream();
            aOut = new ASN1OutputStream(bOut);
            
            envData = new EnvelopedData(envData.getOriginatorInfo(), s, envData.getEncryptedContentInfo(), envData.getUnprotectedAttrs());
            info = new ContentInfo(CMSObjectIdentifiers.envelopedData, envData);
            
            aOut.writeObject(info);
            
            if (!isSameAs(bOut.toByteArray(), envDataKEK))
            {                                                         System.out.println(new String(Base64.encode(bOut.toByteArray())));
                return new SimpleTestResult(false, getName() + ": CMS KEK enveloped failed to re-encode");
            }
            
            return new SimpleTestResult(true, getName() + ": Okay");
        }
        catch (Exception e)
        {
            return new SimpleTestResult(false, getName() + ": CMS enveloped failed - " + e.toString(), e);
        }
    }
    
    private TestResult signedTest()
    {
        try
        {
            ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(signedData));
            
            ContentInfo     info = ContentInfo.getInstance(aIn.readObject());
            SignedData      sData = SignedData.getInstance(info.getContent());
            
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
            
            sData = new SignedData(sData.getDigestAlgorithms(), sData.getEncapContentInfo(), sData.getCertificates(), sData.getCRLs(), sData.getSignerInfos());
            info = new ContentInfo(CMSObjectIdentifiers.signedData, sData);
            
            aOut.writeObject(info);
            
            if (!isSameAs(bOut.toByteArray(), signedData))
            {
                return new SimpleTestResult(false, getName() + ": CMS signed failed to re-encode");
            }
            
            return new SimpleTestResult(true, getName() + ": Okay");
        }
        catch (Exception e)
        {
            return new SimpleTestResult(false, getName() + ": CMS signed failed - " + e.toString(), e);
        }
    }
    
    public TestResult perform()
    {
        TestResult  res = compressionTest();
        
        if (!res.isSuccessful())
        {
            return res;
        }
        
        res = envelopedTest();
        if (!res.isSuccessful())
        {
            return res;
        }
        
        return signedTest();
    }

    public String getName()
    {
        return "CMS";
    }

    public static void main(
        String[] args)
    {
        CMSTest    test = new CMSTest();
        TestResult result = test.perform();

        System.out.println(result);
    }
}

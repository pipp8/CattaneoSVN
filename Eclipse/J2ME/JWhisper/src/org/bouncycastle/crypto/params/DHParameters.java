package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.math.BigInteger;


public class DHParameters
    implements CipherParameters
{
    private BigInteger              g;
    private BigInteger              p;
    private BigInteger              q;
    private int                     j;
    private DHValidationParameters  validation;

    public DHParameters(
        BigInteger  p,
        BigInteger  g)
    {
        this.g = g;
        this.p = p;
    }

    public DHParameters(
        BigInteger  p,
        BigInteger  g,
        BigInteger  q,
        int         j)
    {
        this.g = g;
        this.p = p;
        this.q = q;
        this.j = j;
    }   

    public DHParameters(
        BigInteger              p,
        BigInteger              g,
        BigInteger              q,
        int                     j,
        DHValidationParameters  validation)
    {
        this.g = g;
        this.p = p;
        this.q = q;
        this.j = j;
        this.validation = validation;
    }

    public BigInteger getP()
    {
        return p;
    }

    public BigInteger getG()
    {
        return g;
    }

    public BigInteger getQ()
    {
        return q;
    }

    /**
     * Return the private value length in bits - if set, zero otherwise (use bitLength(P) - 1).
     * 
     * @return the private value length in bits, zero otherwise.
     */
    public int getJ()
    {
        return j;
    }

    public DHValidationParameters getValidationParameters()
    {
        return validation;
    }

    public boolean equals(
        Object  obj)
    {
        if (!(obj instanceof DHParameters))
        {
            return false;
        }

        DHParameters    pm = (DHParameters)obj;

        if (this.getQ() != null)
        {
            if (!this.getQ().equals(pm.getQ()))
            {
                return false;
            }
        }
        else
        {
            if (pm.getQ() != null)
            {
                return false;
            }
        }
        
        return (j == pm.getJ()) && pm.getP().equals(p) && pm.getG().equals(g);
    }
    
    public int hashCode()
    {
        return getJ() ^ getP().hashCode() ^ getG().hashCode() ^ (getQ() != null ? getQ().hashCode() : 0);
    }
}

package org.bouncycastle.crypto.generators;

import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.math.BigInteger;
import org.bouncycastle.security.SecureRandom;


public class DHParametersGenerator
{
    private int             size;
    private int             certainty;
    private SecureRandom    random;

    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);

    /**
     * Initialise the parameters generator.
     * 
     * @param size bit length for the prime p
     * @param certainty level of certainty for the prime number tests
     * @param random  a source of randomness
     */
    public void init(
        int             size,
        int             certainty,
        SecureRandom    random)
    {
        this.size = size;
        this.certainty = certainty;
        this.random = random;
    }

    /**
     * which generates the p and g values from the given parameters,
     * returning the DHParameters object.
     * <p>
     * Note: can take a while...
     */
    public DHParameters generateParameters()
    {
        BigInteger      g, p, q;
        int             qLength = size - 1;

        //
        // find a safe prime p where p = 2*q + 1, where p and q are prime.
        //
        for (;;)
        {
            q = new BigInteger(qLength, certainty, random);
            p = q.multiply(TWO).add(ONE);
            if (p.isProbablePrime(certainty))
            {
                break;
            }
        }

        //
        // calculate the generator g - the advantage of using the 2q+1 
        // approach is that we know the prime factorisation of (p - 1)...
        //
        for (;;)
        {
            g = new BigInteger(qLength, random);

            if (g.modPow(TWO, p).equals(ONE))
            {
                continue;
            }

            if (g.modPow(q, p).equals(ONE))
            {
                continue;
            }

            break;
        }

        return new DHParameters(p, g, q, 2);
    }
}

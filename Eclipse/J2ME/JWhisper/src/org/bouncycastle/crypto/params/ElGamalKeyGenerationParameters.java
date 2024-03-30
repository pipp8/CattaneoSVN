package org.bouncycastle.crypto.params;


import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.security.SecureRandom;

public class ElGamalKeyGenerationParameters
    extends KeyGenerationParameters
{
    private ElGamalParameters    params;

    public ElGamalKeyGenerationParameters(
        SecureRandom        random,
        ElGamalParameters   params)
    {
        super(random, params.getP().bitLength());

        this.params = params;
    }

    public ElGamalParameters getParameters()
    {
        return params;
    }
}

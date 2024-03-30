package crymes.crypto;


import org.bouncycastle.crypto.InvalidCipherTextException;

public abstract class Cipher{
	protected static String ALG_ID;
	protected Cipher _cipher;
	
	public abstract byte[] decrypt(byte[] data, KeyPair pair)
			throws InvalidCipherTextException, Exception;
	
	public abstract byte[] encrypt(byte[] data, KeyPair pair)
			throws Exception;

	
	
}

package crymes.crypto;

public interface IKeyPairGenerator {
	KeyPair generateKeyPair() throws Exception;
	KeyPair getKeyPairFromABytePair(byte[] pubKey,byte[] priKey);
}

package crymes.crypto;



public abstract class KeyPair {
	
	
	protected byte[] _privateKey;
	protected byte[] _publicKey;
	
	public byte[] getPrivateKey(){
		return _privateKey;
	}
	
	public byte[] getPublicKey(){
		return _publicKey;
	}
	
}

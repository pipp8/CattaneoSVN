/*
 * $Revision: 1.3 $
 * $Date: 2008-11-14 14:38:23 $
 */

package timercompare;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

public interface CipherDecipherEngine {

	AsymmetricCipherKeyPair keyPair = null;
	
	public byte[] encrypt(byte[] plainText);
	public byte[] decrypt(byte[] cypherText);
}

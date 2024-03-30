package crypto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.crypto.Cipher;
import org.bouncycastle.util.encoders.Base64;



public class InvioEsenzioni {

	public static void main(String[] args) throws Exception {

		String inputFileName = "ESEUNI01.TXT";
		String outputFileName = "ESEOUT.TXT";
		
		CifraCFCert cf = new CifraCFCert();
		cf.initPubKey("mefpp.cer");
		
		cf.initPrivKey("keyStore", "123456", "mefpp2", "123456");
		
		DataInputStream dis = new DataInputStream(
				new FileInputStream( new File( inputFileName)));
		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream( new File( outputFileName)));
		
	    final int recLen = 40;
	    final int CFLen = 16;
	    final int startCF = 0;
	    final int fineCF = 16;
	    
		byte[] buf = new byte[recLen];
		byte[] CFBuf;
		int letti;
		for(int i = 0; true; i++) {
			try {
				letti = dis.read(buf);
				if (letti < recLen) {
					System.out.println("End of file");
					System.out.printf("Processati %d record input\n", i);
					break;
				}
				else if ((buf[recLen-2] != 13) || (buf[recLen-1] != 10)) {
					System.out.println("Formato record non riconosciuto:");
					System.out.println("buf[recLen-2] = " + buf[recLen-2] + " buf[recLen-1] = " + buf[recLen-1]);
					System.out.println("Processing aborted");
					break;
				}
				
				CFBuf = estrai(buf, startCF, fineCF);
				System.out.println("PlainText : " + new String(CFBuf));

			    // encryption step
			    byte[] cipherText = cf.encrypt(CFBuf);
			    String ct = new String(cipherText);
			    System.out.printf("cipherText (len = %d):\n%s\n", ct.length(), ct);
			    
			    // scrittura record output
			    dos.writeBytes("1150110"); // prologo ... da dove vengono questi bvalori ?
			    dos.write(cipherText);
			    dos.write(buf, fineCF, recLen-fineCF); // scrive tutto il resto del record compreso terminatore \r\n
			    dos.flush();
			}
			catch(EOFException e) {
				System.out.println("Letti " + i + " record(s) input");
			}
		}
		dis.close();
		dos.close();
//	        
//	    byte[] plainText = cf.decrypt(cipherText);
//	    System.out.println("plain : " + new String(plainText));
	}

	// estrae una sottostringa come substring dal byte "da" incluso al byte "a" escluso 
	static public byte[] estrai(byte[] buf, int da, int a) {
		byte[] ret = new byte[a - da];
		for(int i = da; i < a; i++)
			ret[i - da] = buf[i];
		
		return ret;
	}
}

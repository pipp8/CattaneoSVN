/*
 * $Revision: 1.3 $
 * $Date: 2008-11-14 14:38:23 $
 */

package timercompare.utility;

import java.io.*;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.asymmetric.ec.ECUtil;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

public class TCUtility {

	// Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

	// Returns the contents of the file in a byte array.
    public static void saveFileFromBytes(byte[] byteArray, File toWrite) throws IOException {
    	FileOutputStream fos = new FileOutputStream(toWrite);
    	try {
			fos.write(byteArray);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			System.err.println(e);
		}
    	fos.flush();
    	fos.close();
    }
    
    public static boolean fileCompare(File fCompare1,File fCompare2) throws IOException {
     	byte[] file1 = getBytesFromFile(fCompare1);
     	byte[] file2 = getBytesFromFile(fCompare2);
     	boolean comp = true; 
     	int numCicli = 0;
//     	if (offset>0){
//     		numCicli = offset;
//     	} else {
 		if (file1.length<file2.length){
 			numCicli = file1.length;
 		}else{
 			numCicli = file2.length;
 		}
//     	}
 		for (int i=0;i<numCicli;i++){
 			if(file1[i]!=file2[i]) {
 				comp = false;
 				break;
 			}
 		}
     	return comp;
    }
    
    public static void printDomainParameters( ECDomainParameters param){

		System.out.println( "param: P = " + param.getCurve().getFieldSize());
		System.out.println( "param: A = " + param.getCurve().getA().toBigInteger().toString(16));
		System.out.println( "param: B = " + param.getCurve().getB().toBigInteger().toString(16));
		System.out.println("Parameters:");
		System.out.print  ( "param: G = ");  printPoint( param.getG());
		System.out.println( "param: H = " + param.getH());
		System.out.println( "param: N = " + param.getN().toString(16));
		System.out.print("param: Seed = ");
		try {
			Hex.encode(param.getSeed(), System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
    }
    
    public static void printParameterSpec( ECParameterSpec ecSpec){
		System.out.println( "ecSpec: P = " + ecSpec.getCurve().getFieldSize());
		System.out.println( "ecSpec: A = " + ecSpec.getCurve().getA().toBigInteger().toString(16));
		System.out.println( "ecSpec: B = " + ecSpec.getCurve().getB().toBigInteger().toString(16));
		System.out.println("Parameters:");
		System.out.print  ( "ecSpec: G = ");  TCUtility.printPoint( ecSpec.getG());
		System.out.println( "ecSpec: H = " + ecSpec.getH());
		System.out.println( "ecSpec: N = " + ecSpec.getN().toString(16));
		System.out.print("ecSpec: Seed = ");
		try {
			Hex.encode(ecSpec.getSeed(), System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
    }
    
    
    public static void printPoint( ECPoint p) {
    	ECPointUtil t  = new ECPointUtil();
    	byte[] pa = p.getEncoded();
    	if (p.isCompressed())
    		System.out.print (" Compressed format: ");
    	else
    		System.out.print (" Uncompressed format: ");
    	
    	try {
			Hex.encode(pa, System.out);
			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

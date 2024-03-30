/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.util.helpers;

/**
 * this is stupid - but it seems that at least in some cases, binary messages may not contain
 * 0x00 bytes. so make sure we have no 0x00 bytes in our data.
 * 
 * @author malte
 *
 */
public class NullEncoder {

	public static byte[] encode(byte[] data) {
		byte[]  zeros=new byte[data.length]; // it wont be more
		byte[]	buffer=new byte[data.length];
		
		int zerocount=0;
		int buffercount=0;
		for (int i=0;i<data.length;i++) {
			if (data[i] == 0) {
				zeros[zerocount]=(byte)(i+1); // make sure we have no '0'
				zerocount++;
			}
			else {
				buffer[buffercount]=data[i];
				buffercount++;
			}
		}
		
		byte[] result=new byte[data.length+1];
		result[0]=(byte)zerocount;
		for (int i=0; i<zerocount;i++) 
			result[i+1]=zeros[i];
		for (int i=0; i<buffercount;i++) 
			result[i+1+zerocount]=buffer[i];
		
		return result;
	}
	
	public static byte[] decode(byte[] data) {
		if ((data != null) && (data.length>1)) {
			byte[]	result=new byte[data.length-1];
			int 	offset=data[0];
			int 	amount=data.length-1;
			int		zerocount=0;
			for (int i=0; i<amount; i++) {
				if (data[zerocount+1]==(i+1)) {
					result[i]=0;
					zerocount++;
				}
				else {
					result[i]=(byte)(data[offset+i-zerocount+1]);
				}
			}
		return result;
		}
		return null;
	}
	
}

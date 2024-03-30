/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.modules.encoders;

import jwhisper.utils.Logger;

/*
 * 8bit to 7bit encoder/ decoder
 */
public class SevenBitEncoder {
	
	public static byte[] encode(byte[] data) {
		int len=data.length;
		byte[] 	target=new byte[((len+6)/7)*8];

		Logger.getInstance().write("SevenBitEncoder - Encode - Calculated result length: "+ target.length);
		
		int targetOff=0;
		int i;
		int	store=0;
		int bytesToGo;
		
		for (i=0; i<(len-7); i+=7) {
			Logger.getInstance().write("SevenBitEncoder - Encode - encoding loop: "+i+" targetOff: "+targetOff);
			targetOff=encodeBlock(data, i, target, targetOff);
		}
		if (i>(len-7)) {
			Logger.getInstance().write("SevenBitEncoder - Encode - encoding tail: "+i+" targetOff: "+targetOff);
			bytesToGo=len-i; // bytes to go < 7
			Logger.getInstance().write("SevenBitEncoder - Encode - len: "+len+" bytes to go: "+bytesToGo);
			
			while (bytesToGo>0) { // still data to process?
				target[targetOff++]=(byte)(data[i] & 0x7F);
				store+=	(byte)((data[i] & 0x80)>>bytesToGo);
				bytesToGo--;
				i++;
			}
			target[targetOff]=(byte)store;
			Logger.getInstance().write("SevenBitEncoder - Encode - store: "+store);
		}
		return target;
	}
	
	public static byte[] decode(byte[] data) {
		int len=data.length;
		byte[] 	target=new byte[((len+7)/8)*7];
		int targetOff=0;
		int i;
		
		for (i=0; i<(len-8);i+=8) {
			targetOff=decodeBlock(data,i,target,targetOff);
		}
		return target; 
	}
	
	/*
	 * unrolled encoding loop - WARN: no bounds checks 
	 */
	private static int encodeBlock(byte[] block,int blockOff,byte[] target,int targetOff) {
		
		target[targetOff]=(byte)(block[blockOff] & 0x7F);
		target[targetOff+1]=(byte)(block[blockOff+1] & 0x7F);
		target[targetOff+2]=(byte)(block[blockOff+2] & 0x7F);
		target[targetOff+3]=(byte)(block[blockOff+3] & 0x7F);
		target[targetOff+4]=(byte)(block[blockOff+4] & 0x7F);
		target[targetOff+5]=(byte)(block[blockOff+5] & 0x7F);
		target[targetOff+6]=(byte)(block[blockOff+6] & 0x7F);
		
		target[targetOff+7]=(byte)(
				((block[blockOff] & 0x80) != 0 ? 1 : 0) +
				((block[blockOff+1] & 0x80) != 0 ? 2 : 0) +
				((block[blockOff+2] & 0x80) != 0 ? 4 : 0) +
				((block[blockOff+3] & 0x80) != 0 ? 8 : 0) +
				((block[blockOff+4] & 0x80) != 0 ? 16 : 0) +
				((block[blockOff+5] & 0x80) != 0 ? 32 : 0) +
				((block[blockOff+6] & 0x80) != 0 ? 64 : 0) );
		
		return targetOff+8;
	}
	
	/*
	 * unrolled decoding loop - WARN: no bounds checks
	 * 
	 */
	private static int decodeBlock(byte[] block,int blockOff,byte[] target,int targetOff) {

		byte		bits=block[blockOff+7];
		
		target[targetOff]=(byte)(block[blockOff]+((bits & 1) != 0 ? 0x80 : 0)); 
		target[targetOff+1]=(byte)(block[blockOff+1]+((bits & 2) != 0 ? 0x80 : 0)); 
		target[targetOff+2]=(byte)(block[blockOff+2]+((bits & 4) != 0 ? 0x80 : 0)); 
		target[targetOff+3]=(byte)(block[blockOff+3]+((bits & 8) != 0 ? 0x80 : 0)); 
		target[targetOff+4]=(byte)(block[blockOff+4]+((bits & 16) != 0 ? 0x80 : 0)); 
		target[targetOff+5]=(byte)(block[blockOff+5]+((bits & 32) != 0 ? 0x80 : 0)); 
		target[targetOff+6]=(byte)(block[blockOff+6]+((bits & 64) != 0 ? 0x80 : 0)); 
		
		return targetOff+7;
	}
	
}

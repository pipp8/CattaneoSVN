/*
 * Copyright (C) 2007 Crypto Messaging with Elliptic Curves Cryptography (CRYMES)
 *
 * This file is part of Crypto Messaging with Elliptic Curves Cryptography (CRYMES).
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Crypto Messaging with Elliptic Curves Cryptography (CRYMES); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package crymes.helpers;

/**
 * simple straight forward 8bit to 7bit encoder/ decoder
 * 
 * @author malte
 *
 */
public class SevenBitEncoder {
	
	public static byte[] encode(byte[] data) {
		int len=data.length;
		byte[] 	target=new byte[((len+6)/7)*8];
		
		System.out.println("calulated result length: "+target.length);
		
		int targetOff=0;
		int i;
		int	store=0;
		int bytesToGo;
		
		for (i=0; i<(len-7); i+=7) {
			System.out.println("encoding loop: "+i+" targetOff: "+targetOff);
			targetOff=encodeBlock(data, i, target, targetOff);
		}
		if (i>(len-7)) {
			System.out.println("encoding tail: "+i+" targetOff: "+targetOff);
			bytesToGo=len-i; // bytes to go < 7
			System.out.println("len: "+len+" bytes to go: "+bytesToGo);
			while (bytesToGo>0) { // still data to process?
				target[targetOff++]=(byte)(data[i] & 0x7F);
				store+=	(byte)((data[i] & 0x80)>>bytesToGo);
				bytesToGo--;
				i++;
			}
			target[targetOff]=(byte)store;
			System.out.println("store: "+store);
		}
		// TODO trim down target to the actual used size
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
		// TODO decode the tail
		return target; 
	}
	
	/**
	 * unrolled encoding loop - WARN: no bounds checks 
	 *
	 * @param block	- byte[] of source
	 * @param blockOff - offset into source
	 * @param target - byte[] of target
	 * @param targetOff - offset into target
	 * @return new target index
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
	
	/**
	 * unrolled decoding loop - WARN: no bounds checks
	 * 
	 * @param block	- byte[] of source
	 * @param blockOff - offset into source
	 * @param target - byte[] of target
	 * @param targetOff - offset into target
	 * @return new target index
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

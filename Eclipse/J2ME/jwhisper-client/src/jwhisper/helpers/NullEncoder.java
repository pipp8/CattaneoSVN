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

package jwhisper.helpers;

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

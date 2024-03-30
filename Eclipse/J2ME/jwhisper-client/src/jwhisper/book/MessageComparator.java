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

package jwhisper.book;



import javax.microedition.rms.RecordComparator;

/**
 * 	@author sd
 *	@date 29.04.2007
 */
public class MessageComparator implements RecordComparator {

	/* (non-Javadoc)
	 * @see javax.microedition.rms.RecordComparator#compare(byte[], byte[])
	 */
	public int compare(byte[] a, byte[] b)  {
		try{
		CMessage m1=new CMessage(a,0);
		CMessage m2=new CMessage(b,0);
		long l1=m1.getTime().getTime();
		long l2=m2.getTime().getTime();
		if(l1>l2){
			return FOLLOWS;
		}else{
			return PRECEDES;
		}
		
		}catch (Exception e) {
			return EQUIVALENT;
		}
		
	}

}
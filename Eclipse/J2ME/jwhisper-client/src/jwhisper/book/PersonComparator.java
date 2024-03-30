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
public class PersonComparator implements RecordComparator {

	/* (non-Javadoc)
	 * @see javax.microedition.rms.RecordComparator#compare(byte[], byte[])
	 */
	public int compare(byte[] a, byte[] b) {
		String name1=new String(a);
		String name2=new String(b);
		if(name1.compareTo(name2)<0){
			return PRECEDES;
		}else 
			return FOLLOWS;
	}

}

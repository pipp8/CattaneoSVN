/*
 * Copyright (C) 2007 cryptosms.org
 *
 * This file is part of cryptosms.org.
 *
 * cryptosms.org is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * cryptosms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cryptosms.org; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.cryptosms.book;

import util.recordstore.RecordComparator;

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

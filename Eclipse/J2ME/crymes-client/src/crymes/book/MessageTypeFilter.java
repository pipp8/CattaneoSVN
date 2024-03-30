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
package crymes.book;

import java.io.IOException;

import javax.microedition.rms.RecordFilter;

/**
 * 
 * 
 * @author sd
 */
public class MessageTypeFilter implements RecordFilter {
	private byte _mType = 0;

	/**
	 * Creates a new MessageTypeFilter object.
	 */
	public MessageTypeFilter(byte type) {
		_mType = type;
	}

	public boolean matches(byte[] record) {
		//Dummy rmsID -99
		try {
			CMessage m = new CMessage(record,-99);

			return  m.getType()==_mType;
		} catch (IOException e) {
			return false;
		}
	}
}

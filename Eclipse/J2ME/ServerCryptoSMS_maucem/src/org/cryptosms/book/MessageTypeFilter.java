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

import java.io.IOException;

import util.recordstore.RecordFilter;

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

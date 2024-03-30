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

/**
 * @author sd
 */
public class MessageType {
	public static final byte ENCRYPTED_TEXT_INBOX = 1;

	public static final byte ENCRYPTED_KEY = 2;

	public static final byte KEY = 3;
	
	public static final byte SYM_ENCRYPTED_TEXT_OUTBOX = 4;
	
	public static final byte SYM_ENCRYPTED_TEXT_INBOX = 5;
	
	public static final byte PK_REGISTER_REQUEST = 6;
	
	public static final byte PK_SERVER_CHALLENGE  = 7;
	
    public static final byte PK_CHALLENGE_RESPONSE = 8;
	
	public static final byte MAX_MESSAGE_TYPE  = 9;
}

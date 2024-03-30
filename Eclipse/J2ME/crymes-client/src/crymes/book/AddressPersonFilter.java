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

import javax.microedition.rms.RecordFilter;

/**
 * implements the Interface Record Filter, which filters adress records if they
 * belong to a person
 * 
 * @author sd
 */
class AddressPersonFilter implements RecordFilter {
	private Person _person = null;

	/**
	 * Creates a new AdressPersonFilter object.
	 * 
	 * @param p
	 *            person
	 */
	public AddressPersonFilter(Person p) {
		_person = p;
	}

	/**
	 * Checks if person from the adress record store matches with person from
	 * constructor
	 * 
	 * @param record
	 *            one record from record store
	 * @return true if person matches, else false
	 */
	public boolean matches(byte[] record) {
		Address a = new Address(record);
		Person p2 = a.getPerson();

		return _person.equals(p2);
	}
}

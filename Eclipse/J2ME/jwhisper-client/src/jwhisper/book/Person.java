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

/**
 * @author sd
 */
public class Person {
	private int _rmsID;

	private String _name;

	private Address[] _conns;

	/**
	 * @param name
	 */
	public Person(String name, int id) {
		this._name = name;
		this._rmsID = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this._name;
	}

	/**
	 * @return the conns
	 */
	public Address[] getConns() {
		return _conns;
	}

	/**
	 * @return the rmsID
	 */
	public int getRmsID() {
		return _rmsID;
	}

	/**
	 * @param conns
	 *            the conns to set
	 */
	public void setConns(Address[] conns) {
		this._conns = conns;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this._name = name;
	}

	public boolean equals(Person p) {
		if (p==null) return false;
		return this._rmsID == p.getRmsID();
	}
}

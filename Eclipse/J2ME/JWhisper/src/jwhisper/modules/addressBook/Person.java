/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.modules.addressBook;


public class Person {
	private int _rmsID;

	private String _name;

	private Address[] _addresses;

	public Person(String name, int id) {
		this._name = name;
		this._rmsID = id;
	}

	public String getName() {
		return this._name;
	}

	public Address[] getAddresses() {
		return _addresses;
	}

	public int getRmsID() {
		return _rmsID;
	}

	public void setAddresses(Address[] addresses) {
		this._addresses = addresses;
	}

	public void setName(String name) {
		this._name = name;
	}

	public boolean equals(Person p) {
		if (p==null) return false;
		return this._rmsID == p.getRmsID();
	}
}

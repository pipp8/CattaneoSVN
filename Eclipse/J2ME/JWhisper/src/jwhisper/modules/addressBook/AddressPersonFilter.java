/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.modules.addressBook;

import javax.microedition.rms.RecordFilter;

/*
 * Interface Record Filter
 * Filtra tutti gli Address appartenenti ad una Person
 * 
 */

public class AddressPersonFilter implements RecordFilter {
	private Person _person = null;

	public AddressPersonFilter(Person p) {
		_person = p;
	}

	public boolean matches(byte[] record) {
		Address a = new Address(record);
		Person p2 = a.getPerson();

		return _person.equals(p2);
	}
}

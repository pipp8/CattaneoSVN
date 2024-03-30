/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.book;

import jwhisper.cgss.modules.recordstore.RecordFilter;

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

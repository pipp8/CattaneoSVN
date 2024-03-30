/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.book;

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

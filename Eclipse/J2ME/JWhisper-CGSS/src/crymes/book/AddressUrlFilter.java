/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.book;

import jwhisper.cgss.modules.recordstore.RecordFilter;
import jwhisper.utils.CountryCodeStore;


/**
 * @author sd
 */
public class AddressUrlFilter implements RecordFilter {
	private String _url = null;

	/**
	 * Creates a new AdressUrlFilter object.
	 */
	public AddressUrlFilter(String url) {
		_url = url;
	}

	public boolean matches(byte[] record) {
		Address a = new Address(record);
//return (_url.equals(a.getUrl()));
		return (CountryCodeStore.compareNumbers(_url,a.getUrl()));
	}
}

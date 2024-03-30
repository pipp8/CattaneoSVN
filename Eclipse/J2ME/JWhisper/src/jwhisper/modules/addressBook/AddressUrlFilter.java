/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.modules.addressBook;

import javax.microedition.rms.RecordFilter;

import jwhisper.utils.CountryCodeStore;



public class AddressUrlFilter implements RecordFilter {
	private String _url = null;

	public AddressUrlFilter(String url) {
		_url = url;
	}

	public boolean matches(byte[] record) {
		Address a = new Address(record);
		return (CountryCodeStore.compareNumbers(_url,a.getUrl()));
	}
}

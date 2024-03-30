/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.utils;


public class CountryCode {
	private String country;
	private String code;
	private String replacement;

	public CountryCode(String country,String code,String replacement ) {
		this.code=code;
		this.country=country;
		this.replacement=replacement;
	}
	public String getCode() {
		return code;
	}

	public String getCountry() {
		return country;
	}
	public String getReplacement() {
		return replacement;
	}

}

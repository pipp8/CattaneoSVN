/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.util.helpers;


import java.util.Enumeration;
import java.util.Vector;

/**
 * 	@author sd
 *	@date 04.05.2007
 */
@SuppressWarnings("unchecked")
public class CountryCodeStore {
	
	private static Vector CountryCodes=new Vector();

	static{
		CountryCodes.addElement(new CountryCode("germany","+49","0"));
		CountryCodes.addElement(new CountryCode("netherlands","+31","0"));
		CountryCodes.addElement(new CountryCode("emulator","+55","55"));
	}
	public static boolean compareNumbers(String number1, String number2){
		boolean out=false;
		Enumeration e=CountryCodes.elements();
		CountryCode cc;
		String number1repl="";
		String number2repl="";
		while( e.hasMoreElements()){
			cc= (CountryCode)e.nextElement();
			if(number1.startsWith(cc.getCode()))
				number1repl=cc.getReplacement()+number1.substring(cc.getCode().length(), number1.length());
		
			if(number2.startsWith(cc.getCode()))
				number2repl=cc.getReplacement()+number2.substring(cc.getCode().length(), number2.length());

		}
		if(number1.equals(number2)||number1.equals(number2repl)||number1repl.equals(number2)){
			out=true;
		}
		return out;
	}
}

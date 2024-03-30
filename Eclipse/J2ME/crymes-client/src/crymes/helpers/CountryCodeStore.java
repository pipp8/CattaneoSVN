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
package crymes.helpers;


import java.util.Enumeration;
import java.util.Vector;

/**
 * 	@author sd
 *	@date 04.05.2007
 */
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

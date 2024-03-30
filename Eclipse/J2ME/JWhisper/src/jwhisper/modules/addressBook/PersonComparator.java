/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.modules.addressBook;

import javax.microedition.rms.RecordComparator;

public class PersonComparator implements RecordComparator {

	public int compare(byte[] a, byte[] b) {
		String name1=new String(a);
		String name2=new String(b);
		if(name1.compareTo(name2)<0){
			return PRECEDES;
		}else 
			return FOLLOWS;
	}
}

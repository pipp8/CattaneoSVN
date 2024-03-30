/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.book;

import jwhisper.cgss.modules.recordstore.RecordComparator;

/**
 * 	@author sd
 *	@date 29.04.2007
 */
public class PersonComparator implements RecordComparator {

	/* (non-Javadoc)
	 * @see javax.microedition.rms.RecordComparator#compare(byte[], byte[])
	 */
	public int compare(byte[] a, byte[] b) {
		String name1=new String(a);
		String name2=new String(b);
		if(name1.compareTo(name2)<0){
			return PRECEDES;
		}else 
			return FOLLOWS;
	}

}

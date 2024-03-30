/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.book;


import crymes.util.recordstore.RecordComparator;

/**
 * 	@author sd
 *	@date 29.04.2007
 */
public class MessageComparator implements RecordComparator {

	/* (non-Javadoc)
	 * @see javax.microedition.rms.RecordComparator#compare(byte[], byte[])
	 */
	public int compare(byte[] a, byte[] b)  {
		try{
		CMessage m1=new CMessage(a,0);
		CMessage m2=new CMessage(b,0);
		long l1=m1.getTime().getTime();
		long l2=m2.getTime().getTime();
		if(l1>l2){
			return FOLLOWS;
		}else{
			return PRECEDES;
		}
		
		}catch (Exception e) {
			return EQUIVALENT;
		}
		
	}

}
/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.modules.message;



import javax.microedition.rms.RecordComparator;

public class MessageComparator implements RecordComparator {

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
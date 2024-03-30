/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.util.recordstore;


@SuppressWarnings("serial")
public class RecordStoreNotOpenException extends RecordStoreException
{

	public RecordStoreNotOpenException(String message)
	{
		super(message);
	}
	
	
	public RecordStoreNotOpenException()
	{
		super();
	}
	
}


/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */
 
package crymes.util.recordstore;


@SuppressWarnings("serial")
public class RecordStoreFullException extends RecordStoreException
{

	public RecordStoreFullException(String message)
	{
		super(message);
	}
	
	
	public RecordStoreFullException()
	{
		super();
	}
	
}


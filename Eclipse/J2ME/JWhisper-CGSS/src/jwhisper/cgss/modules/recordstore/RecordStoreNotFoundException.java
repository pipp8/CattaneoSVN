/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.cgss.modules.recordstore;


@SuppressWarnings("serial")
public class RecordStoreNotFoundException extends RecordStoreException
{

	public RecordStoreNotFoundException(String message)
	{
		super(message);
	}
	
	
	public RecordStoreNotFoundException()
	{
		super();
	}
	
}


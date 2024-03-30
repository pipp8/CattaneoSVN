/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */
 
package jwhisper.cgss.modules.recordstore;


@SuppressWarnings("serial")
public class InvalidRecordIDException extends RecordStoreException
{

	public InvalidRecordIDException(String message)
	{
		super(message);
	}
	
	
	public InvalidRecordIDException()
	{
		super();
	}
	
}


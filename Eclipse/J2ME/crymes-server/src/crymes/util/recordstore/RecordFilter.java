/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */
 
package crymes.util.recordstore;


public interface RecordFilter
{

  boolean matches(byte[] candidate);
	
}


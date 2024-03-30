/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */
 
package jwhisper.cgss.modules.recordstore;


public interface RecordFilter
{

  boolean matches(byte[] candidate);
	
}


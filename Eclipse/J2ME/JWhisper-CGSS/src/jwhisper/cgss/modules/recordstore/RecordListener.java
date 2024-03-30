/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */
 
package jwhisper.cgss.modules.recordstore;


public interface RecordListener
{

  void recordAdded(RecordStore recordStore, int recordId);

  void recordChanged(RecordStore recordStore, int recordId);
  
  void recordDeleted(RecordStore recordStore, int recordId);
	
}


/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.util.helpers;



import jwhisper.cgss.modules.recordstore.InvalidRecordIDException;
import jwhisper.cgss.modules.recordstore.RecordComparator;
import jwhisper.cgss.modules.recordstore.RecordFilter;



/**
 * 	@author sd
 *	@date 13.04.2007
 */
public class CryptedRecordEnumeration   {

	
	CryptedRecordStore _store;
	int []_recIds ;
	RecordFilter _rf;
	RecordComparator _rc;
	int _currentRec;
	

	 CryptedRecordEnumeration(CryptedRecordStore store, RecordFilter rf, RecordComparator rc, boolean keepUpdated) throws Exception {
			 _store=store;
			 _rf=rf;
			 _rc=rc;
			 intialize();
			 _currentRec=0;
	}
	 


	public void destroy() {
		 _store=null;
		 _recIds =null;
		 _rf=null;
		 _rc=null;
		// _records=null;
		
	}
	private void intialize() throws Exception{
		_recIds=_store.getRecordIds();
		
	}
	private void destroyed() {
		if (_store==null)
			throw new IllegalStateException();	
	}


	public boolean hasNextElement() {
		destroyed();
		if (numRecords()>_currentRec)
			return true;
		else
			return false;
	}


	public boolean hasPreviousElement() {
		destroyed();
		if (_currentRec>0)
			return true;
		else
			return false;
	}


	public boolean isKeptUpdated() throws Exception {
		throw new Exception("not supported");
		
	}


	public void keepUpdated(boolean b) throws Exception {
		throw new Exception("not supported");
		
	}



	public byte[] nextRecord() throws  Exception {
			return _store.getRecord(nextRecordId());	
	}


	public int nextRecordId() throws InvalidRecordIDException {
		destroyed();
		if (hasNextElement()){
			return _recIds[_currentRec++];
		}else{
			throw new InvalidRecordIDException();
		}
	}


	public int numRecords() {
		destroyed();
		// return _recIds.length-1;
		return _recIds.length;
	}


	public byte[] previousRecord() throws Exception {
		return _store.getRecord(_recIds[previousRecordId()]);	
	}


	public int previousRecordId() throws InvalidRecordIDException {
		destroyed();
		if (hasPreviousElement()){
			return --_currentRec;
		}else{
			throw new InvalidRecordIDException();
		}
	}



}
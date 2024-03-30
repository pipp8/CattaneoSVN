/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */



package jwhisper.modules.recordStore;



import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;

import javax.microedition.rms.RecordFilter;

public class CacheEnumeration  implements RecordEnumeration {


	RSCache _cache;
	int []_recIds ;
	RecordFilter _rf;
	RecordComparator _rc;
	int _currentRecIndex;


	CacheEnumeration(RSCache cache, RecordFilter rf, RecordComparator rc) throws Exception {
		_cache=cache;
		_rf=rf;
		_rc=rc;
		intialize();
		_currentRecIndex=0;
	}



	public void destroy() {
		_cache=null;
		_recIds =null;
		_rf=null;
		_rc=null;
		// _records=null;

	}
	private void intialize(){

		_recIds=_cache.getRecordIds(_rf);
		if(_rc!=null&&_recIds.length>1){
			sort(_recIds,0,_recIds.length-1);
		}

	}
	private void destroyed() {
		if (_cache==null)
			throw new IllegalStateException();	
	}


	public boolean hasNextElement() {
		destroyed();
		if (numRecords()>_currentRecIndex)
			return true;
		else
			return false;
	}


	public boolean hasPreviousElement() {
		destroyed();
		if (_currentRecIndex>0)
			return true;
		else
			return false;
	}


	public boolean isKeptUpdated() {
		return false;
		//throw new Exception("not supported");

	}


	public void keepUpdated(boolean b)  {
		//throw new Exception("not supported");

	}


	public byte[] nextRecord() throws InvalidRecordIDException {

		byte[] ba=null;

		ba = _cache.getData(nextRecordId());

		return 	ba;
	}


	public int nextRecordId() throws InvalidRecordIDException {
		destroyed();
		if (hasNextElement()){
			return _recIds[_currentRecIndex++];
		}else{
			throw new InvalidRecordIDException();
		}
	}


	public int numRecords() {
		destroyed();

		return _recIds.length;
	}


	public byte[] previousRecord() throws InvalidRecordIDException {
		byte[] ba=null;

		ba=_cache.getData(_recIds[previousRecordId()]);

		return ba;	
	}


	public int previousRecordId() throws InvalidRecordIDException {
		destroyed();
		if (hasPreviousElement()){
			return --_currentRecIndex;
		}else{
			throw new InvalidRecordIDException();
		}
	}

	private void sort(int[] ids,int left,int right) {
		int tempID;
		if (right > left)
		{
			int id1 = ids[right];
			int i = left - 1;
			int j = right;
			while (true)
			{
				while (_rc.compare(_cache.getData(ids[++i]), _cache.getData(id1)) < 0);
				while (j > 0)
					if (_rc.compare(_cache.getData(ids[--j]),_cache.getData( id1)) <= 0)
						break;
				if (i >= j)
					break;
				//swap
				tempID=ids[i];
				ids[i]=ids[j];
				ids[j]=tempID;

			}
			tempID=ids[i];
			ids[i]=ids[right];
			ids[right]=tempID;

			sort(ids, left, i - 1);
			sort(ids, i + 1, right);
		}
	}



	
	public void rebuild() {
		intialize();

	}



	
	public void reset() {
		intialize();

	}


}
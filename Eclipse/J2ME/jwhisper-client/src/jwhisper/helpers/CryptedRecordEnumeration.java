/*
 * Copyright (C) 2007 Crypto Messaging with Elliptic Curves Cryptography (CRYMES)
 *
 * This file is part of Crypto Messaging with Elliptic Curves Cryptography (CRYMES).
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Crypto Messaging with Elliptic Curves Cryptography (CRYMES); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jwhisper.helpers;



import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordComparator;

import javax.microedition.rms.RecordFilter;


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
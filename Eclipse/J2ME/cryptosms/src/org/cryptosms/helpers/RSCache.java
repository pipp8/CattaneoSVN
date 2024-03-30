/*
 * Copyright (C) 2007 cryptosms.org
 *
 * This file is part of cryptosms.org.
 *
 * cryptosms.org is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * cryptosms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cryptosms.org; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.cryptosms.helpers;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordFilter;

/**
 * 	@author sd
 *	@date 27.04.2007
 */
public class RSCache {
	private String name;
	private Vector records;
	public RSCache(String name){
		this.name=name;
		records=new Vector();
	}
	public void add(Record rec){
		records.addElement(rec);
	}
	public byte[] getData(int rid) {
		int cid=getCacheID4RecordID(rid);
		if(cid!=-1){
			return ((Record)records.elementAt(cid)).getData();
		}else return null;
		
	}
	public String getName() {
		return name;
	}
	private int getCacheID4RecordID(int id){
		int out=-1;
		Record rec;
		int i=0;
		Enumeration e=records.elements();
		while(e.hasMoreElements()){
			rec=(Record)e.nextElement();
			if(rec.getId()==id){
				out=i;
				break;
			}
			i++;
		}
		return out;
	}
	public void setData(int rid,byte[] ba) throws Exception {
		int cid=getCacheID4RecordID(rid);
		if (cid!=-1){
				Record r=(Record)records.elementAt(cid);
				r.setRecord(ba);
		}else throw new Exception("Record not found");
	}
	public void deleteRecord(int id) throws Exception {
		boolean found=false;
		Record rec;
		int i=0;
		Enumeration e=records.elements();
		while(e.hasMoreElements()){
			rec=(Record)e.nextElement();
			if(rec.getId()==id){
				records.removeElementAt(i);
				found=true;
				break;
			}
			i++;
		}
		if(found==false)throw new Exception("Record not found");
	}
	public CacheEnumeration getEnumeration(RecordFilter rf, RecordComparator rc) throws Exception{
//		Enumeration e=records.elements();
//		return e;
		return new CacheEnumeration(this,rf,rc);
	}
	public void destroy() {
		records.removeAllElements();
		
	}
	/**
	 * @return
	 */
	 int[] getRecordIds(RecordFilter rf) {
		 int[] tmp=new int[records.size()];
		 Record rec;
		 int i=0;
			Enumeration e=records.elements();
			while(e.hasMoreElements()){
				rec=(Record)e.nextElement();
				if (rf==null||rf.matches(rec.getData())){
					tmp[i]=rec.getId();
					i++;
				}
			}
		int[] out=new int[i];

		System.arraycopy(tmp,0,out,0,i);
		return out;
	}

}
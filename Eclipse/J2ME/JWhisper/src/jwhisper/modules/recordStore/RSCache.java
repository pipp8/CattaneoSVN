/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.modules.recordStore;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordFilter;


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
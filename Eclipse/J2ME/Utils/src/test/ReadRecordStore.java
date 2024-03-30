package test;

import java.math.BigInteger;
import java.util.Random;

import util.recordstore.RecordComparator;
import util.recordstore.RecordEnumeration;
import util.recordstore.RecordFilter;
import util.recordstore.RecordStore;

public class ReadRecordStore {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RecordStore[] rs = new RecordStore[10];
		
		for(int s = 1; s < 6; s++) {
			try {
				 rs[s] = RecordStore.openRecordStore( "test" + String.valueOf(s), true);
					System.out.println("RecordStore: " + rs[s].getName() + " opened");
					System.out.println("# record presenti: " + rs[s].getNumRecords());
			}
			catch(Exception e) {
				System.out.println("Error opening store");
				System.out.println(e.getMessage());
			}
	
	//		RecordFilter rf = new RecordFilter() {
	//			public boolean matches(byte[] can) {
	//				return true;
	//			}
	//		};
	//		RecordComparator rc = new RecordComparator() {
	//			public int compare(byte[] a, byte[] b) {
	//				return EQUIVALENT;
	//			}
	//		};
	//		try {
	//			RecordEnumeration re = rs.enumerateRecords(rf, rc, true);
	//			for( ; re.hasNextElement(); ) {
	//				byte[] r = re.nextRecord();
	//				BigInteger bi = new BigInteger( r);
	//				System.out.println(" -> " + bi.toString());
	//			}
	//		}
	//		catch(Exception e) {
	//			System.out.println("Errore di lettura");
	//			System.out.println(e.getMessage());
	//		}
	
			try {
				for(int i = 0;i < rs[s].getNumRecords() ; i++ ) {
					int id = i+1;
					byte[] r = rs[s].getRecord(id);
					BigInteger bi = new BigInteger( r);
					System.out.println(" -> " + bi.toString());
				}
			}
			catch(Exception e) {
				System.out.println("Errore di lettura");
				System.out.println(e.getMessage() + e.toString());
				e.printStackTrace();
			}
		}	
		
		
		for(int s = 1; s < 6; s++) {
			try {
				rs[s].closeRecordStore();
			}
			catch(Exception e) {
				System.out.println("Error closing");
				System.out.println(e.getMessage());
			}

		}
	}
}

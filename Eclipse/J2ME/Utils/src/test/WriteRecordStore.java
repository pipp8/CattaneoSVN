package test;

import java.math.BigInteger;
import java.util.Random;

import util.recordstore.RecordStore;

public class WriteRecordStore {

	static public void main(String[] arg) {
		
		RecordStore[] rs = new RecordStore[10];
		for(int s = 1; s < 6; s++) {
			try {
				 rs[s] = RecordStore.openRecordStore( "test"+String.valueOf(s), true);
			}
			catch(Exception e) {
				System.out.println("Error opening store");
				System.out.println(e.getMessage());
			}
			Random rnd = new Random();
			rnd.setSeed(System.currentTimeMillis());
			for(int i = 0; i < 100; i++) {
				try {				
					BigInteger bi = new BigInteger( 512, rnd);
					byte[] data = bi.toByteArray();
					System.out.print("record: " + rs[s].getNextRecordID());
					rs[s].addRecord( data, 0, data.length);
					System.out.println(" -> " + bi.toString());
				}
				catch( Exception e) {
					System.out.println("Errore writing records");
					System.out.println(e.getMessage());
				}
			}
		}

		for(int s = 1; s < 6; s++) {
			try {
				rs[s].closeRecordStore();
			}
			catch(Exception e) {
				System.out.println("Error closing");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}

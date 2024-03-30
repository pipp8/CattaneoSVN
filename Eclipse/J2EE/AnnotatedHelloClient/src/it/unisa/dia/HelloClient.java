package it.unisa.dia;

import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.Random;

import org.apache.axis.AxisFault;


public class HelloClient {

		
	public static void main(String[] args) {
		
		for(int i = 0; i < 100; i++) {
			try {
				HelloProxy stub = new HelloProxy();
				System.out.println("Calling greet method: " + stub.greet("Pippo"));
			} catch (AxisFault e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}

package wtp;

import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.Random;

import org.apache.axis.AxisFault;


public class ConverterClient {

		
	public static void main(String[] args) {
		Random rng = new Random();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(3);
		nf.setMaximumIntegerDigits(3);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		
		for(int i = 0; i < 100; i++) {
			try {
				float celsiusValue = rng.nextFloat() * 100;
				ConverterProxy stub = new ConverterProxy();
				System.out.println("Celsius : " + nf.format(celsiusValue) + " = Farenheit : " + 
						nf.format(stub.celsiusToFarenheit(celsiusValue)));
			} catch (AxisFault e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}

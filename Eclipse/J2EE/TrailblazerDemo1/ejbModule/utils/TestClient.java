package utils;

import javax.naming.InitialContext;

import sessionstateless.Calculator;

public class TestClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Calculator cal = null;

		try {
		      InitialContext ctx = new InitialContext();
		      Object obj = ctx.lookup( "TrailblazerEar/CalculatorBean/remote");
		      System.out.println("get Session reference, JNDI.lookup got: " + obj.getClass().getSimpleName());
		      cal = (Calculator) obj;
	    } catch (Exception e) {
		      e.printStackTrace ();
	    }

	    double res = cal.calculate( 3, 4, 3.4, 4.5);
	    System.out.println("result: " + res);
	}
}

package statelesssession;

import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class StatelessClient {

   public static void main(String[] args) throws Exception
   {
			
	   
		try {
			Hashtable props = new Hashtable (); 
	        props.put ( InitialContext . INITIAL_CONTEXT_FACTORY , "org.jnp.interfaces.NamingContextFactory" ); 
	        props.put ( InitialContext . PROVIDER_URL , "jnp://127.0.0.1:1099" ); 
	        InitialContext ctx = new InitialContext ( props ); 

	        Calculator calculator = (Calculator) ctx.lookup( "calculator/remote");
			
			
			System.out.println("1 + 1 = " + calculator.add(1, 1));
			System.out.println("1 - 1 = " + calculator.subtract(1, 1));
		}
		catch (NamingException ex) {
			System.err.println("Naming Exception: " + ex.getMessage());
			ex.printStackTrace();
		}
   }
}

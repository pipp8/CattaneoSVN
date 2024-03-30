package statefullsession;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.ejb.EJBAccessException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class StatefullClient {

   public static void main(String[] args) throws Exception
   {
	try { 
		Hashtable props = new Hashtable (); 
        props.put ( InitialContext . INITIAL_CONTEXT_FACTORY , "org.jnp.interfaces.NamingContextFactory" ); 
        props.put ( InitialContext . PROVIDER_URL , "jnp://127.0.0.1:1099" ); 
        InitialContext ctx = new InitialContext ( props ); 

        ShoppingCart cart = (ShoppingCart) ctx.lookup("ShoppingCartBean/remote");

        System.out.println("Buying 1 memory stick");
        cart.buy("Memory stick", 1);
        System.out.println("Buying another memory stick");
        cart.buy("Memory stick", 1);

        System.out.println("Buying a laptop");
        cart.buy("Laptop", 1);

        System.out.println("Print cart:");
        HashMap<String, Integer> fullCart = cart.getCartContents();
        for (String product : fullCart.keySet())
        {
           System.out.println(fullCart.get(product) + "     " + product);
        }

        System.out.println("Checkout");
        cart.checkout();

        System.out.println("Should throw an object not found exception by invoking on cart after @Remove method");
        try
        {
           cart.getCartContents();
        }
        catch (javax.ejb.EJBAccessException e)
        {
           System.out.println("Successfully caught no such object exception.");
        }
     }
	 catch(Exception e) {
		 System.out.println("General failure: " + e.getMessage());
	 }
   }
}

package entity;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.ejb.EJBAccessException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class OrderClient {

	public static void main(String[] args) throws Exception
	{
		try { 
	      InitialContext ctx = new InitialContext();
	      CMPShoppingCart cart = (CMPShoppingCart) ctx.lookup("CMPShoppingCartBean/remote");

	      System.out.println("Buying 2 memory sticks");
	      cart.buy("Memory stick", 2, 500.00);
	      System.out.println("Buying a laptop");
	      cart.buy("Laptop", 1, 2000.00);

	      System.out.println("Print cart:");
	      Order order = cart.getOrder();
	      System.out.println("Total: $" + order.getTotal());
	      for (LineItem item : order.getLineItems())
	      {
	         System.out.println(item.getQuantity() + "     " + item.getProduct() + "     " + item.getSubtotal());
	      }

	      System.out.println("Checkout");
	      cart.checkout();
		}
		catch (Exception ex) {
			System.out.println("General failure: " + ex.getMessage());
		}
	}
}

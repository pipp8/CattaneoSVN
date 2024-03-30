package transaction;

import java.util.Properties;
import javax.ejb.EJBAccessException;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * @version $Revision: 1.1 $
 */
public class ClientTrans
{
   public static void main(String[] args) throws Exception
   {
      // Establish the proxy with an incorrect security identity
      Properties env = new Properties();
      env.setProperty(Context.SECURITY_PRINCIPAL, "kabir");
      env.setProperty(Context.SECURITY_CREDENTIALS, "invalidpassword");
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      InitialContext ctx = new InitialContext(env);
      CalculatorTrans calculator = (CalculatorTrans) ctx.lookup("CalculatorTransBean/remote");

      System.out.println("Kabir is a student.");
      System.out.println("Kabir types in the wrong password");
      try
      {
         System.out.println("1 + 1 = " + calculator.add(1, 1));
      }
      catch (EJBAccessException ex)
      {
         System.out.println("Saw expected SecurityException: " + ex.getMessage());
      }

      System.out.println("Kabir types in correct password.");
      System.out.println("Kabir does unchecked addition.");

      // Re-establish the proxy with the correct security identity
      env.setProperty(Context.SECURITY_CREDENTIALS, "validpassword");
      ctx = new InitialContext(env);
      calculator = (CalculatorTrans) ctx.lookup("CalculatorTransBean/remote");

      try
      {
    	  System.out.println("1 + 1 = " + calculator.add(1, 1));
      }
      catch (javax.ejb.EJBAccessException  ex)
      {
         System.out.println(ex.getMessage());
      }
      
      try
      {
    	  System.out.println("Kabir is not a teacher so he cannot do division");
    	  calculator.divide(16, 4);
      }
      catch (javax.ejb.EJBAccessException  ex)
      {
         System.out.println(ex.getMessage());
      }

      try
      {
	      System.out.println("Students are allowed to do subtraction");
	      System.out.println("1 - 1 = " + calculator.subtract(1, 1));
      }
      catch (javax.ejb.EJBAccessException  ex)
      {
         System.out.println(ex.getMessage());
      }
   }
}

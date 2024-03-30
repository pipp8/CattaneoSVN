package timer;

import java.util.Properties;
import javax.ejb.EJBAccessException;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * @version $Revision: 1.1 $
 */
public class ClientTimer
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      ExampleTimer timer = (ExampleTimer) ctx.lookup("ExampleTimerBean/remote");
      long delay = 2000;
      try
      {
    	  timer.scheduleTimer(delay);
    	  System.out.println("timer armed (" + delay + ") sec.");
      }
      catch (EJBAccessException ex)
      {
         System.out.println("Timer error: " + ex.getMessage());
      }
   }
}

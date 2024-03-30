package timer;

import javax.ejb.Stateless;
import timer.ExampleTimer;
import java.util.Date;
import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;


@Stateless
@Remote(ExampleTimer.class)
public class ExampleTimerBean implements ExampleTimer
{
   private @Resource SessionContext ctx;

   public void scheduleTimer(long milliseconds)
   {
      ctx.getTimerService().createTimer(new Date(new Date().getTime() + milliseconds), "Hello World");
   }

   @Timeout
   public void timeoutHandler(Timer timer)
   {
      System.out.println("---------------------");
      System.out.println("* Received Timer event: " + timer.getInfo());
      System.out.println("---------------------");

      timer.cancel();
   }
}

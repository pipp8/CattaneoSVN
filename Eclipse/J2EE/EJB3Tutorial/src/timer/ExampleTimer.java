package timer;

import java.util.Date;

import javax.ejb.Remote;
import javax.ejb.Timeout;
import javax.ejb.Timer;

@Remote
public interface ExampleTimer {
		public void scheduleTimer(long milliseconds);
		
		public void timeoutHandler(Timer timer);
}

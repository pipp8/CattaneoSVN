package beans;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestClient {

	  private ScheduleSessionHome scheduleSessionHome;

	  private ScheduleSession getScheduleSession() {
			if (null == scheduleSessionHome) {
				Hashtable props = new Hashtable (); 
		        props.put ( InitialContext . INITIAL_CONTEXT_FACTORY , "org.jnp.interfaces.NamingContextFactory" ); 
		        props.put ( InitialContext . PROVIDER_URL , "jnp://127.0.0.1:1099" ); 
				try {
					Context context = new InitialContext(props);
					Object obj = context.lookup(ScheduleSessionHome.JNDI_NAME);
					System.out.println("getScheduleSession, JNDI.lookup got: " + obj.getClass().getSimpleName());
					scheduleSessionHome = (ScheduleSessionHome) PortableRemoteObject.narrow(obj, ScheduleSessionHome.class);
					// scheduleSessionHome = (ScheduleSessionHome) obj;
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (ClassCastException e) {
					System.err.println("Cast failed");
					e.printStackTrace();
				}
			}
			try {
				return scheduleSessionHome.create();
			} catch (CreateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		   
		protected void test() throws RemoteException {
			PrintWriter out = new PrintWriter(System.out);

			ScheduleSession scheduleSession = getScheduleSession();

			String scheduleid = "Pluto";
			if (scheduleid != null) {
				String title = "LP1";
				int starttime = 8;
				int endtime = 9;
				String[] days = { "MON", "WED" };

				if (days != null) {
					for (int i = 0; i < days.length; i++) {
						String dayString = days[i];
						int day;
						if (dayString.equalsIgnoreCase("MON"))
							day = 0;
						else if (dayString.equalsIgnoreCase("TUE"))
							day = 1;
						else if (dayString.equalsIgnoreCase("WED"))
							day = 2;
						else if (dayString.equalsIgnoreCase("THU"))
							day = 3;
						else if (dayString.equalsIgnoreCase("FRI"))
							day = 4;
						else if (dayString.equalsIgnoreCase("SAT"))
							day = 5;
						else
							day = 6;

						scheduleSession.addScheduleItem(title, starttime, endtime,day, scheduleid);
					}
					// List<ScheduleItemData> items =	scheduleSession.getScheduleItemesForScheduleID(scheduleid);
				}
			}
			String name = "Pippo";
			if (name != null) {
				List items = scheduleSession.getScheduleItem(name);
				out.println("Schedule Item retrieved:");
				printTable(out, items);
			}
			String scheduleID = "Pippo";
			if (scheduleID != null) {
				List items = scheduleSession.getScheduleItemesForScheduleID(scheduleID);
				out.println("ScheduleIDs matching " + scheduleID + ":");
				printTable(out, items);
			}
			List items = scheduleSession.getAllScheduleItems();
			out.println("All Schedule Items:");
			printTable(out, items);
			out.flush();
		}   
		
		private void printTable(PrintWriter out, List items) {
			out.println("<TABLE border=\"1\" cellspacing=\"0\"><TBODY><TR>");
			out.println("<TH align=\"center\" valign=\"middle\">Name</TH>");
			out.println("<TH align=\"center\" valign=\"middle\">StartTime</TH>");
			out.println("<TH align=\"center\" valign=\"middle\">EndTime</TH>");
			out.println("<TH align=\"center\" valign=\"middle\">Day</TH>");
			out.println("<TH align=\"center\" valign=\"middle\">ScheduleID</TH></TR>");
			for (int i = 0; i < items.size(); i++) {
				ScheduleItemData item = (ScheduleItemData) items.get(i);
				out.println("<TR>");
				out.println("<TD>" + item.getName() + "</TD>");
				out.println("<TD>" + item.getStartTime() + "</TD>");
				out.println("<TD>" + item.getEndTime() + "</TD>");
				out.println("<TD>" + item.getDay() + "</TD>");
				out.println("<TD>" + item.getScheduleID() + "</TD>");
				out.println("</TR>");
			}
			out.println("</TBODY></TABLE>");
		}
		
		public static void main(String[] args) {

			TestClient run = new TestClient();
			try {
				run.test();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
}

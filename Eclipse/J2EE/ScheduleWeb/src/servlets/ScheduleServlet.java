package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ejbs.ScheduleItemData;
import ejbs.ScheduleSessionLocal;
import ejbs.ScheduleSessionLocalHome;

/**
 * Servlet implementation class for Servlet: ScheduleServlet
 *
 * @web.servlet
 *   name="ScheduleServlet"
 *   display-name="ScheduleServlet" 
 *
 * @web.servlet-mapping
 *   url-pattern="/ScheduleServlet"
 *  
 * @web.ejb-local-ref	name="ScheduleSession"
 * 						type="Session"
 * 						local="ejbs.ScheduleSessionLocal"
 * 						home="ejbs.ScheduleSessionLocalHome"
 * 						link="ScheduleSession"
 * 
 * @jboss.ejb-local-ref	ref-name="ScheduleSession"
 *                      jndi-name="ScheduleSessionLocal"
 *  
 */
 public class ScheduleServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
   static final long serialVersionUID = 1L;
   String[] weekDays;
   private ScheduleSessionLocalHome scheduleSessionLocalHome;

   private ScheduleSessionLocal getScheduleSession() {
		if (null == scheduleSessionLocalHome) {
			try {
//				scheduleSessionHome = ScheduleSessionUtil.getHome();
				Context context = new InitialContext();
				Object obj = context.lookup("java:comp/env/ScheduleSession");
				System.out.println("getScheduleSession, JNDI.lookup got: " + obj.getClass().getSimpleName());
				scheduleSessionLocalHome = (ScheduleSessionLocalHome) PortableRemoteObject.narrow(obj, ScheduleSessionLocalHome.class);
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
			return scheduleSessionLocalHome.create();
		} catch (CreateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}  
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public ScheduleServlet() {
		super();
		weekDays = new String[7];
		weekDays[0] = "MON";
		weekDays[1] = "TUE";
		weekDays[2] = "WED";
		weekDays[3] = "THU";
		weekDays[4] = "FRI";
		weekDays[5] = "SAT";
		weekDays[6] = "SUN";
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/* (non-Java-doc)
		 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
		 */
		// TODO Auto-generated method stub
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();

		ScheduleSessionLocal scheduleSessionLocal = getScheduleSession();

		String scheduleid = request.getParameter("multidayscheduleid");
		if (scheduleid != null) {
			String title = request.getParameter("title");
			int starttime = Integer.parseInt(request.getParameter("starttime"));
			int endtime = Integer.parseInt(request.getParameter("endtime"));
			String[] days = request.getParameterValues("day");

			if (days != null) {
				for (int i = 0; i < days.length; i++) {
					String dayString = days[i];
					int day;
					for(day = 0; day < weekDays.length; day++)
						if (dayString.equalsIgnoreCase(weekDays[day]))
							break;

					scheduleSessionLocal.addScheduleItem(title, starttime, endtime,
							day, scheduleid);
				}
				request.getSession().setAttribute( "schoolschedule",
						scheduleSessionLocal.getScheduleItemesForScheduleID(scheduleid));
				getServletContext().getRequestDispatcher("/Schedule.jsp")
						.forward(request, response);
			}
		}

		String name = request.getParameter("nameGet");
		if (name != null) {
			List items = scheduleSessionLocal.getScheduleItem(name);
			out.println("Schedule Item retrieved:");
			printTable(out, items);
			return;
		}
		String scheduleID = request.getParameter("scheduleID");
		if (scheduleID != null) {
			List items = scheduleSessionLocal.getScheduleItemesForScheduleID(scheduleID);
			out.println("ScheduleIDs matching " + scheduleID + ":");
			printTable(out, items);
			return;
		}
		List items = scheduleSessionLocal.getAllScheduleItems();
		out.println("All Schedule Items:");
		printTable(out, items);
		return;
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
			out.println("<TD>" + weekDays[item.getDay().intValue()] + "</TD>");
			out.println("<TD>" + item.getScheduleId() + "</TD>");
			out.println("</TR>");
		}
		out.println("</TBODY></TABLE>");
	}  	  
	  	    
}
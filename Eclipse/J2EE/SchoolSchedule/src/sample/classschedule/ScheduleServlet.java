package sample.classschedule;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class for Servlet: ScheduleServlet
 *
 */
 public class ScheduleServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
   static final long serialVersionUID = 1L;
   
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public ScheduleServlet() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.err.println("Servlet Starting");
		String title = request.getParameter("title");
		int starttime = Integer.parseInt(request.getParameter("starttime"));
		int endtime = Integer.parseInt(request.getParameter("endtime"));
		String[] days = request.getParameterValues("day");
		
		SchoolSchedule schedule = (SchoolSchedule) request.getSession(true).getAttribute("schoolschedule");
		if (schedule == null)
		{
			System.err.println("Creating new schedule");
			schedule = new SchoolSchedule();
		}
		if (days != null)
		{
			for(int i = 0; i < days.length; i++) 
			{
				String dayStr = days[i];
				int day = -1;
				if (dayStr.equalsIgnoreCase("MON"))	day = 0;
				else if (dayStr.equalsIgnoreCase("TUE"))	day = 1;
				else if (dayStr.equalsIgnoreCase("WED"))	day = 2;
				else if (dayStr.equalsIgnoreCase("THU"))	day = 3;
				else if (dayStr.equalsIgnoreCase("FRI"))	day = 4;
				else if (dayStr.equalsIgnoreCase("SAT"))	day = 5;
				else 										day = 6;
			
				System.err.println("Adding class: " + title + " for day: " + day);
				SchoolClass clazz = new SchoolClass( title, starttime, endtime, day);
				schedule.addClass(clazz);
			}
			
			request.getSession().setAttribute("schoolschedule", schedule);
			getServletContext().getRequestDispatcher("/Schedule.jsp").forward(request, response);
		}
	}   	  	    
}
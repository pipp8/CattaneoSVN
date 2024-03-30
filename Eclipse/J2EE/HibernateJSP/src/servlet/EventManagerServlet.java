package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.HibernateUtil;
import events.*;
/**
 * Servlet implementation class for Servlet: EventManagerServlet
 *
 */
 public class EventManagerServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
   static final long serialVersionUID = 1L;
   
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public EventManagerServlet() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
													throws ServletException, IOException {

		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
		
		try {
		   // Begin unit of work
		   HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
		
		   // Process request and render page...
		   
		   // Write HTML header
		   PrintWriter out = response.getWriter();
		   out.println("<html><head><title>Event Manager</title></head><body>");

		   // Handle actions
		   if ( "store".equals(request.getParameter("action")) ) {

		       String eventTitle = request.getParameter("eventTitle");
		       String eventDate = request.getParameter("eventDate");

		       if ( "".equals(eventTitle) || "".equals(eventDate) ) {
		           out.println("<b><i>Please enter event title and date.</i></b>");
		       } else {
		           createAndStoreEvent(eventTitle, dateFormatter.parse(eventDate));
		           out.println("<b><i>Added event.</i></b>");
		       }
		   }

		   // Print page
		   printEventForm(out);
		   listEvents(out, dateFormatter);

		   // Write HTML footer
		   out.println("</body></html>");
		   out.flush();
		   out.close();
	
		   // End unit of work
		   HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
		
		} catch (Exception ex) {
		   HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
		   throw new ServletException(ex);
		}
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}   	
	
	private void printEventForm(PrintWriter out) {
	    out.println("<h2>Add new event:</h2>");
	    out.println("<form>");
	    out.println("Title: <input name='eventTitle' length='50'/><br/>");
	    out.println("Date (e.g. 24.12.2009): <input name='eventDate' length='10'/><br/>");
	    out.println("<input type='submit' name='action' value='store'/>");
	    out.println("</form>");
	}

	private void listEvents(PrintWriter out, SimpleDateFormat dateFormatter) {

	    List result = HibernateUtil.getSessionFactory()
	                    .getCurrentSession().createCriteria(Event.class).list();
	    if (result.size() > 0) {
	        out.println("<h2>Events in database:</h2>");
	        out.println("<table border='1'>");
	        out.println("<tr>");
	        out.println("<th>Event title</th>");
	        out.println("<th>Event date</th>");
	        out.println("</tr>");
	        for (Iterator it = result.iterator(); it.hasNext();) {
	            Event event = (Event) it.next();
	            out.println("<tr>");
	            out.println("<td>" + event.getTitle() + "</td>");
	            out.println("<td>" + dateFormatter.format(event.getDate()) + "</td>");
	            out.println("</tr>");
	        }
	        out.println("</table>");
	    }
	}

	protected void createAndStoreEvent(String title, Date theDate) {
	    Event theEvent = new Event();
	    theEvent.setTitle(title);
	    theEvent.setDate(theDate);

	    HibernateUtil.getSessionFactory().getCurrentSession().save(theEvent);
	}

}
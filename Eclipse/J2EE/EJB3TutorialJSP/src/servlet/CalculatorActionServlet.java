package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import statelesssession.*;

/**
 * Servlet implementation class for Servlet: CalculatorActionServlet
 *
 */
 public class CalculatorActionServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
   static final long serialVersionUID = 1L;
   
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public CalculatorActionServlet() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doit(request, response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doit(request, response);
	} 
	
	/**
	 * Simulate injection of Calculator EJB
	 *
	 * @throws ServletException
	 */
	public void init() throws ServletException
	{
		super.init();
		try
		{
			InitialContext ctx = new InitialContext();

			// J2EE 1.5 has not yet defined exact XML <ejb-ref> syntax for EJB3
			CalculatorLocal calculator = (CalculatorLocal) ctx.lookup("calculator/local");
			setCalculator(calculator);
		}
		catch (NamingException e)
		{
			throw new RuntimeException(e);
	    }

	}

	private CalculatorLocal calculator;
	
	/**
	 * The @EJB annotation is similated.  Tomcat does not yet understand injection annotations.
	 * @param calculator
	 */
	@EJB(name = "calculator")
	public void setCalculator(CalculatorLocal calculator)
	{
		this.calculator = calculator;
	}	

	protected void doit(HttpServletRequest req, HttpServletResponse resp)
						throws ServletException, IOException
	{
		int op1 = Integer.parseInt(req.getParameter("op1"));
		int op2 = Integer.parseInt(req.getParameter("op2"));
		
		String action = req.getParameter("action");
		
		int result = 0;
		if (action.equals("Add"))
			result = calculator.add(op1, op2);
		else if (action.equals("Subtract")) result = calculator.subtract(op1, op2);

		resp.setContentType("text/html");
		PrintWriter writer = resp.getWriter();
		writer.println("<html>");
		writer.println("<body>");
		writer.println("<h1>Answer: " + result + "</h1>");
		writer.println("</body");
		writer.println("</html>");
	}
}
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Stateless Calculator Test Page</title>
<%@ page import="sessionstateless.*, javax.naming.*, java.text.*"%>
</head>
<%!
  private Calculator cal = null;
  public void jspInit () {
    try {
      InitialContext ctx = new InitialContext();
      cal = (Calculator) ctx.lookup(
                  "TrailblazerEar/CalculatorBean/local");
    } catch (Exception e) {
      e.printStackTrace ();
    }
  }
%>
 
<%
  String result;
  int start = 25;
  int end = 65;
  double growthrate = 0.08;
  double saving = 300.0;
  try {
    start = Integer.parseInt(request.getParameter ("start"));
    end = Integer.parseInt(request.getParameter ("end"));
    growthrate = Double.parseDouble(request.getParameter ("growthrate"));
    saving = Double.parseDouble(request.getParameter ("saving"));

    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(2);
    result = nf.format(cal.calculate(start, end, growthrate, saving));
  } catch (Exception e) {
    // e.printStackTrace ();
    result = "Not valid";
  }
%>
 <html>
 <body>

<p>
 <h4>
	 Investment calculator
 </h4>
 <br/>
<form action="calculator.jsp" method="POST">
 Start age = <input type="text" name="start" value="<%=start%>"><br/>
 End age   = <input type="text" name="end" value="<%=end%>"><br/>
 Annual Growth Rate = <input type="text" name="growthrate" value="<%=growthrate%>"><br/>
 Montly Saving = <input type="text" name="saving" value="<%=saving%>"><br/>
 <input type="submit" value="Calculate">
 <INPUT type="button" value="Close Window" onClick="window.close()">
</form>
</p>

<p>The result from the last calculation: The balance at the End age is
<b><%=result%></b></p>

</body>
</html> 

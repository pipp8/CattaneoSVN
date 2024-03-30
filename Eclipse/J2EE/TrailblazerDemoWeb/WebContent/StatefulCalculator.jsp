<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Stateful Calculator Test Page</title>

<%@ page import="stateful.*,
                 javax.naming.*,
                 java.text.*,
                 java.util.ArrayList"%>

<%!
  private NumberFormat nf = null;

  public void jspInit () {
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(2);
  }
%>

<%
  Calculator cal =
      (Calculator) session.getAttribute("sfsb_cal");
  if (cal == null) {
    try {
      InitialContext ctx = new InitialContext();
      cal = (Calculator) ctx.lookup(
                  "TrailblazerEar/StatefulCalculatorBean/local");
      session.setAttribute ("sfsb_cal", cal);
    } catch (Exception e) {
      e.printStackTrace ();
    }
  }

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

    double res = cal.calculate(start, end, growthrate, saving);
    result = nf.format(res);

  } catch (Exception e) {
     // e.printStackTrace ();
     result = "Not valid";
  }
%>

<html>
<body>
 
<p>Investment calculator with session history<br/>
<form action="StatefulCalculator.jsp" method="POST">
  Start age = <input type="text" name="start" value="<%=start%>"><br/>
  End age   = <input type="text" name="end" value="<%=end%>"><br/>
  Annual Growth Rate = <input type="text" name="growthrate" value="<%=growthrate%>"><br/>
  Montly Saving = <input type="text" name="saving" value="<%=saving%>"><br/>
  <input type="submit" value="Calculate">
  <INPUT type="button" value="Close Window" onClick="window.close()">
</form>
</p>

<p>The result from the last calculation: The total investment at end age is
<b><%=result%></b></p>

<p><i>Past results</i><br/>
<%
  int entries = cal.getStarts().size();
%>
<table>
<tr>
<td>Start Age</td>
<td>Edn Age</td>
<td>Annual Growth rate</td>
<td>Monthly savings</td>
<td><b>Total investment</b></td>
</tr>

<%
  for (int i = 0; i < entries; i++) {
%>

<tr>
<td><%=cal.getStarts().get(i)%></td>
<td><%=cal.getEnds().get(i)%></td>
<td><%=nf.format(cal.getGrowthrates().get(i))%></td>
<td><%=nf.format(cal.getSavings().get(i))%></td>
<td><%=nf.format(cal.getResults().get(i))%></td>
</tr>
 
<%
  }
%>
</table></p>
</body>
</html> 

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Remote Calculator Test Page</title>
<form action="CalculatorActionServlet" method="post">
Operand 1:  <input type="text" name="op1" value="0" size="35">
<br>
Operand 2:  <input type="text" name="op2" value="0" size="35">
<br>
<input type="submit" name="action" value="Add">
<input type="submit" name="action" value="Subtract">
</form>
</head>
<body>

</body>
</html>
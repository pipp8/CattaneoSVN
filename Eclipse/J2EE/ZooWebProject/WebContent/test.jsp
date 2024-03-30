<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Pagina di test del servizio ZooBeans</title>
</head>
<body>
Calling ...
<% zoo.Tiger tigre = null;
	try {
		zoo.TigerHome home = zoo.TigerUtil.getHome();
		tigre = home.create();
	}
	catch( Exception ex) {
	
	}
%>
<b><font size=7>
<%= tigre.leone() %>
</font></b><p>
<b><font size=3>
<%= tigre.cane() %>
</font></b><p>
 ... called
</body>
</html>
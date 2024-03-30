<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="demo.Hello"%>
<%@page import="demo.HelloUtil"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Demo page for hello beans project</title>
</head>
<body>
<%Hello hello = HelloUtil.getHome().create(); %>
<%=hello.hello() %>
</body>
</html>
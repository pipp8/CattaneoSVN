<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Esempio Orario Scolastico</title>
</head>
<body>
<form action="/SchoolSchedule/ScheduleServlet" method="POST">
Nome del Corso: <input type="text" name="title" size="35"><br>
Orario del corso:
Lun<input type="checkbox" name="day" value="mon">
Mar<input type="checkbox" name="day" value="tue">
Mer<input type="checkbox" name="day" value="wed">
Gio<input type="checkbox" name="day" value="thu">
Ven<input type="checkbox" name="day" value="fri">
Sab<input type="checkbox" name="day" value="sat">
Dom<input type="checkbox" name="day" value="sun">

<select name="starttime">
<option value="8">8:00</option>
<option value="9">9:00</option>
<option value="10">10:00</option>
<option value="11">11:00</option>
<option value="12">12:00</option>
<option value="13">13:00</option>
<option value="14">14:00</option>
<option value="15">15:00</option>
<option value="16">16:00</option>
<option value="17">17:00</option>
<option value="18">18:00</option>
<option value="19">19:00</option>
<option value="20">20:00</option>
<option value="21">21:00</option>
</select>
to
<select name="endtime">
<option value="8">8:00</option>
<option value="9">9:00</option>
<option value="10">10:00</option>
<option value="11">11:00</option>
<option value="12">12:00</option>
<option value="13">13:00</option>
<option value="14">14:00</option>
<option value="15">15:00</option>
<option value="16">16:00</option>
<option value="17">17:00</option>
<option value="18">18:00</option>
<option value="19">19:00</option>
<option value="20">20:00</option>
<option value="21">21:00</option>
</select>
<BR>
<BR>
<input type="submit" name="Submit" value="Add Course">
</form>

<table border="1" cellspacing="0">
<tbody>
<tr>
<th align="center" valign="middle" width="80"></th>
<th align="center" valign="middle" width="100">Lunedi'</th>
<th align="center" valign="middle">Martedi'</th>
<th align="center" valign="middle">Mercoledi'</th>
<th align="center" valign="middle">Giovedi'</th>
<th align="center" valign="middle">Venerdi'</th>
<th align="center" valign="middle">Sabato</th>
<th align="center" valign="middle">Domenica</th>
</tr>

<c:forEach begin="8" end="21" step="1" var="time">
<TR>
<TD align="center" valign="middle" width="80">
<c:out value="${time}" />
</TD>
<c:forEach begin="0" end="6" step="1" var="day">
<TD align="center" valign="middle" width="100">
<c:forEach items="${schoolschedule.classes}" var="clazz">
<c:if test="${clazz.startTime <= time && clazz.endTime > time && clazz.day == day}">
<c:out value="${clazz.title}"/>
</c:if>
</c:forEach>
</TD>
</c:forEach>
</TR>
</c:forEach>
</TBODY>
</TABLE>
</body>
</html>
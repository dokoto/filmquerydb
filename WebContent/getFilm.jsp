<%@page import="services.FilmGetTitle"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html;charset=UTF-8"%>


<%
	try {
		out.print(FilmGetTitle.ManageQueryBy(request));

	} catch (Exception e) {
		e.printStackTrace();
	}
%>
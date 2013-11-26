<%@page import="services.FilmGetImage"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html;charset=UTF-8"%>

<%
	try {
		out.print(FilmGetImage.ManageImageQuery(request));

	} catch (Exception e) {
		e.printStackTrace();
	}
%>
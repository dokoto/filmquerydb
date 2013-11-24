<%@page import="services.FilmGetTitle"%>
<%@page import="java.util.ArrayList"%>


<%
	try {
		out.print(FilmGetTitle.ManageQueryBy(request));

	} catch (Exception e) {
		e.printStackTrace();
	}
%>
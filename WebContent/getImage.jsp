<%@page import="services.FilmGetImage"%>
<%@page import="java.util.ArrayList"%>


<%
	try {
		out.print(FilmGetImage.ManageImageQuery(request));

	} catch (Exception e) {
		e.printStackTrace();
	}
%>
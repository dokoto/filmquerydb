<%@page import="services.FilmsAutoComplete"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html;charset=UTF-8"%>


<%
	try {
		out.print(FilmsAutoComplete.ManageAutoCompleteQueryBy(request));

	} catch (Exception e) {
		e.printStackTrace();
	}
%>
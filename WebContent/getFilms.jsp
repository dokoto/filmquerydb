<%@page import="services.FilmsAutoComplete"%>
<%@page import="java.util.ArrayList"%>


<%
	try {
		out.print(FilmsAutoComplete.ManageAutoCompleteQueryBy(request));

	} catch (Exception e) {
		e.printStackTrace();
	}
%>
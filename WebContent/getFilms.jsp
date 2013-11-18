<%@page import="services.FilmsAutoComplete"%>
<%@page import="java.util.ArrayList"%>


<%	
	try 
{		
		String query = request.getParameter("film");
		String json = FilmsAutoComplete.GetTitle(query);	
		out.print(json);
	
	} catch (Exception e) {
		e.printStackTrace();
	}
%>
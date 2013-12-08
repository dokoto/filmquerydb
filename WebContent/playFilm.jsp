<%@page session="false" contentType="text/html; charset=utf-8"
	import="java.io.IOException,
            java.io.InputStream,
            services.FilmPlay,
            java.util.ArrayList,
		    java.io.OutputStream,
        	javax.servlet.ServletContext,
        	javax.servlet.http.HttpServlet,
        	javax.servlet.http.HttpServletRequest,
        	javax.servlet.http.HttpServletResponse,
        	java.io.File,
        	java.io.FileInputStream"%>

<%
	try {
		String FileContent = FilmPlay.ManageQueryBy(request);
		out.print(FileContent);

	} catch (Exception e) {
		e.printStackTrace();
	}
%>
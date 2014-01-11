<%@page import="conf.ConfigBuilder"%>
<%@page contentType="text/html;charset=UTF-8"%>

<%
	try {
		ConfigBuilder GlobConf = new ConfigBuilder();
		GlobConf.Log().info("Init service from : "  + request.getRemoteAddr());
		session.setAttribute("GlobConf" , GlobConf);
	} catch (Exception e) {
		e.printStackTrace();
	}
%>
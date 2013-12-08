package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FilmPlay
{
	private static final String link = "http://mansierra.homelinux.net:8080/FilmQueryDb/streamLink.m3u";
	
	public static String ManageQueryBy(HttpServletRequest request)
	{
		String path = request.getParameter("path");
		try
		{
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("sffserver \"" + path + "\"");
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";

			while ((line = b.readLine()) != null)
			{
				System.out.println(line);		
			}

			Map<String, Object> root = new HashMap<String, Object>();
			root.put("url", link);
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(root);
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		
	}

}

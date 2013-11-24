package services;

import java.util.ArrayList;

import utils.filters;
import utils.filters.efilterQuery;
import utils.filters.efilterRgx;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import conf.ConfigBuilder;
import ddbb.DDBB;

public class FilmsAutoComplete
{	
	public static String ManageAutoCompleteQueryBy(HttpServletRequest request)
	{
		String query = request.getParameter("film");
		String filter_query = request.getParameter("filter_query");
		String filter_rgx = request.getParameter("filter_rgx");
		
		return GetAutoCompleteResutls(query, filters.GetQueryType(filter_query), filters.GetQueryRgx(filter_rgx));
				
	}
		
	
	private static String GetAutoCompleteResutls(String beginWith, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
	{
		DDBB db = null;
		ArrayList<String> titles = null;
		try
		{
			ConfigBuilder Conf = new ConfigBuilder();
			db = Conf.CreateRefToMongoDB();
			db.connect();
			titles = db.QueryAutoCompleteBy(beginWith, type_filter_query, type_filter_rgx);
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> mapObject = new HashMap<String, Object>();
			mapObject.put("values", titles);
			return objectMapper.writeValueAsString(mapObject);
		} catch (Exception e)
		{
			e.printStackTrace();
			return new String();
		} finally
		{
			if (null != db)
				db.close();
		}
	}
}

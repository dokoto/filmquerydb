package services;

import javax.servlet.http.HttpServletRequest;

import utils.filters;
import utils.filters.efilterQuery;
import utils.filters.efilterRgx;
import conf.ConfigBuilder;
import ddbb.DDBB;

public class FilmGetTitle
{
	
	public static String ManageQueryBy(HttpServletRequest request)
	{
		String query = request.getParameter("film");
		String filter_query = request.getParameter("filter_query");
		String filter_rgx = request.getParameter("filter_rgx");
		
		return GetResutls(query, filters.GetQueryType(filter_query), filters.GetQueryRgx(filter_rgx));
				
	}
	
	private static String GetResutls(String query, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
	{
		DDBB db = null;

		try
		{
			ConfigBuilder Conf = new ConfigBuilder();
			db = Conf.CreateRefToMongoDB();
			db.connect();
			return db.QueryGetData(query, type_filter_query, type_filter_rgx);
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

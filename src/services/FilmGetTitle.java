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
		ConfigBuilder GlobConf = (ConfigBuilder)request.getSession().getAttribute("GlobConf");
		String query = request.getParameter("film");
		String filter_query = request.getParameter("filter_query");
		String filter_rgx = request.getParameter("filter_rgx");
		
		return GetResutls(GlobConf, query, filters.GetQueryType(filter_query), filters.GetQueryRgx(filter_rgx));
				
	}
	
	private static String GetResutls(ConfigBuilder Conf, String query, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
	{
		DDBB db = null;

		try
		{
			db = Conf.CreateRefToMongoDB();
			db.connect();
			return db.QueryBy(query, type_filter_query, type_filter_rgx);
		} catch (Exception e)
		{
			e.printStackTrace();
			Conf.Log().severe(e.toString());
			return new String();
		} finally
		{
			if (null != db)
				db.close();
		}
	}
}

package services;

import javax.servlet.http.HttpServletRequest;

import conf.ConfigBuilder;
import ddbb.DDBB;

public class FilmGetImage
{
	public static String ManageImageQuery(HttpServletRequest request)
	{
		String image = request.getParameter("image");
		ConfigBuilder GlobConf = (ConfigBuilder)request.getSession().getAttribute("GlobConf");
		
		return GetImage(GlobConf, image);						
	}
	
	private static String GetImage(ConfigBuilder Conf, String image)
	{
		DDBB db = null;

		try
		{
			db = Conf.CreateRefToMongoDB();
			db.connect();
			return db.GetImageQuery(image);
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

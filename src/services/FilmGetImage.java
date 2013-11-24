package services;

import javax.servlet.http.HttpServletRequest;

import conf.ConfigBuilder;
import ddbb.DDBB;

public class FilmGetImage
{
	public static String ManageImageQuery(HttpServletRequest request)
	{
		String image = request.getParameter("image");
		
		return GetImage(image);
						
	}
	
	private static String GetImage(String image)
	{
		DDBB db = null;

		try
		{
			ConfigBuilder Conf = new ConfigBuilder();
			db = Conf.CreateRefToMongoDB();
			db.connect();
			return db.GetImageQuery(image);
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

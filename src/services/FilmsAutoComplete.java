package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import conf.ConfigBuilder;
import ddbb.DDBB;

public class FilmsAutoComplete
{

	public static String GetTitle(String beginWith)
	{
		DDBB db = null;
		ArrayList<String> titles = null;
		try
		{
			ConfigBuilder Conf = new ConfigBuilder();
			db = Conf.CreateRefToMongoDB();
			db.connect();
			titles = db.QueryTitle(beginWith);
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> mapObject = new HashMap<String, Object>();
			mapObject.put("titulos", titles);
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

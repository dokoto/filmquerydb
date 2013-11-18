package conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import ddbb.DDBB;


public class ConfigBuilder
{
	private Properties prop = null;
	private ClassLoader loader = null;
	private InputStream stream = null;
	private DDBB db = null;

	public ConfigBuilder() throws IOException
	{
		prop = new Properties();
		loader = Thread.currentThread().getContextClassLoader();
		stream = loader.getResourceAsStream("config.properties");
		prop.load(stream);
	}


	public DDBB GetRefToMongoDB() throws Exception
	{
		return db;
	}

	public DDBB CreateRefToMongoDB() throws Exception
	{
		return new DDBB(Get("db_host"), Get("db_user"), Get("db_password"), Get("db_name"), Integer.valueOf(Get("db_port")));
	}
	
	public String Get(String key) throws Exception
	{
		return prop.getProperty(key);
	}
}

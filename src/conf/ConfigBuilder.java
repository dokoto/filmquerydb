package conf;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import ddbb.DDBB;

public class ConfigBuilder
{
	private static final long serialVersionUID = 1L;

	private static final String TAG = "FilmQueryDb";
	private Properties prop = null;
	private ClassLoader loader = null;
	private InputStream stream = null;
	private DDBB db = null;
	private Logger log = null;
	private FileHandler LogHandler = null;
	static private final String RUTA_LOG_PROP = "log.properties";

	public ConfigBuilder() throws Exception
	{
		prop = new Properties();
		loader = Thread.currentThread().getContextClassLoader();
		stream = loader.getResourceAsStream("config.properties");
		prop.load(stream);
		InitLogger();
	}

	public Logger Log()
	{
		return log;
	}

	public String LogPath() throws Exception
	{
		return Get("log_path");
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

	private void InitLogger() throws Exception
	{
		log = Logger.getLogger(TAG);
		InputStream in = null;
		String methodName = new Exception().getStackTrace()[0].getMethodName();

		// LOG.PROPERTIES
		if (log == null)
			throw new Exception("[" + methodName + "] logger('log) object loaded with errors. It value is null.");
		try
		{
			in = this.getClass().getClassLoader().getResourceAsStream(RUTA_LOG_PROP);
		} catch (Exception e)
		{
			log = null;
			throw new Exception("[" + methodName + "] It couldn't possible reading log.propoerties file and init log.");
		}

		if (in == null)
			throw new Exception("[" + methodName + "] It couldn't to read path of  :  " + RUTA_LOG_PROP);
		try
		{
			LogManager.getLogManager().readConfiguration(in);
			LogHandler = new FileHandler();
			LogHandler.setFormatter(new LogFormat());
			log.addHandler(LogHandler);
		} catch (Exception e)
		{
			log = null;
			throw new Exception("[" + methodName + "] It happened an error when logger was in configure phase.");
		} finally
		{
			if (in != null)
			{
				in.close();
				in = null;
			}
		}
	}

}

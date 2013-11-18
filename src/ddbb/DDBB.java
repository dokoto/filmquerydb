package ddbb;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class DDBB
{

	private String host;
	private String user;
	private String password;
	private String db_name;
	private int db_port;
	private DB db_handle;
	MongoClient mongoClient;

	public DDBB(String host, String user, String password, String db_name, int db_port)
	{
		this.host = host;
		this.user = user;
		this.password = password;
		this.db_name = db_name;
		this.db_port = db_port;
		db_handle = null;
		mongoClient = null;
	}

	public void close()
	{
		if (null != mongoClient)
			mongoClient.close();
	}

	public boolean connect()
	{
		try
		{
			mongoClient = new MongoClient(host, db_port);
			db_handle = mongoClient.getDB(db_name);
			boolean auth = db_handle.authenticate(user, password.toCharArray());
			if (!auth)
				return false;
			else
				return true;

		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public ArrayList<String> QueryTitle(String Query)
	{
		ArrayList<String> result = new ArrayList<String>();
		DBCursor cursor = null;
		try
		{
			final String collection_name = "films";
			DBCollection coll = db_handle.getCollection(collection_name);
			db_handle.setWriteConcern(WriteConcern.SAFE);
			BasicDBObject query = new BasicDBObject("titulo", java.util.regex.Pattern.compile("^" + Query + ".*", Pattern.CASE_INSENSITIVE));
			cursor = coll.find(query);
			while(cursor.hasNext())
			{
				result.add(cursor.next().get("titulo").toString());
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != cursor)
				cursor.close();
		}

		return result;
	}


}

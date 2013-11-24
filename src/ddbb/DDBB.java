package ddbb;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import sun.misc.BASE64Encoder;
import utils.filters.efilterQuery;
import utils.filters.efilterRgx;

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

	public ArrayList<String> QueryAutoCompleteBy(String Query, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
	{
		switch (type_filter_query)
		{
		case actores:
			return QueryAutoCompleteByTitleArray(Query, type_filter_query, type_filter_rgx);
		case directores:
			return QueryAutoCompleteByTitleArray(Query, type_filter_query, type_filter_rgx);
		case titulo:
			return QueryAutoCompleteByTitle(Query, type_filter_query, type_filter_rgx);
		case generos:
			return QueryAutoCompleteByTitleArray(Query, type_filter_query, type_filter_rgx);
		case release_date:
			return QueryAutoCompleteByTitle(Query, type_filter_query, type_filter_rgx);
		default:
			return new ArrayList<String>();

		}
	}

	private String mk_rgx(String Query, efilterRgx type_filter_rgx)
	{
		switch (type_filter_rgx)
		{
		case AL_PRINCIPIO:
			return "^" + Query + ".*";
		case EN_CUALQUIER_SITIO:
			return Query + ".*";
		default:
			return new String();
		}
	}

	public String GetImageQuery(String image)
	{
		GridFS gfsPhoto = null;
		GridFSDBFile imageForOutput = null;
		try
		{
			gfsPhoto = new GridFS(db_handle, "photo");
			imageForOutput = gfsPhoto.findOne(image);				
			Map<String, Object> root = new HashMap<String, Object>();
			if (imageForOutput == null)			
			{
				root.put("value", "img/ico_film.png");
				ObjectMapper objectMapper = new ObjectMapper();
				return objectMapper.writeValueAsString(root);
			}
			BufferedImage image_byte = ImageIO.read(imageForOutput.getInputStream());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(image_byte, "jpeg", bos);
			BASE64Encoder encoder = new BASE64Encoder();
			root.put("value", encoder.encode(bos.toByteArray()));
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(root);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	
	public String QueryGetData(String queryTx, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
	{
		DBCursor cursor = null;
		try
		{
			final String collection_name = "films";
			DBCollection coll = db_handle.getCollection(collection_name);
			db_handle.setWriteConcern(WriteConcern.SAFE);
			BasicDBObject query = new BasicDBObject(type_filter_query.toString(), java.util.regex.Pattern.compile(mk_rgx(queryTx, type_filter_rgx), Pattern.CASE_INSENSITIVE));
			cursor = coll.find(query);
			ArrayList<Map<String, Object>> values = new ArrayList<Map<String, Object>>();

			while (cursor.hasNext())
			{
				DBObject rootObj = cursor.next();
				Map<String, Object> value = new HashMap<String, Object>();

				// TIUTULO
				value.put("titulo", rootObj.get("titulo").toString());

				// GENEROS
				value.put("generos", mgdbGetSubArray(rootObj, "generos", 3));

				// DIRECTORES
				value.put("directores", mgdbGetSubArray(rootObj, "directores", 2));

				// ACTORES
				value.put("actores", mgdbGetSubArray(rootObj, "actores", 3));

				// FECHA DE ESTRENO
				value.put("release_date", rootObj.get("release_date").toString());

				// SINOPSIS
				value.put("sinopsis", rootObj.get("sinopsis").toString());

				// CLAVE IMAGENES
				value.put("foto_mini", rootObj.get("foto_mini").toString());
				value.put("foto_maxi", rootObj.get("foto_maxi").toString());

				values.add(value);
			}
			Map<String, Object> root = new HashMap<String, Object>();
			root.put("values", values);

			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(root);

		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != cursor)
				cursor.close();
		}

		return null;
	}

	private ArrayList<String> mgdbGetSubArray(DBObject rootObj, String key, int limite)
	{
		ArrayList<String> arrayStr = new ArrayList<String>();
		BasicDBList subCursor = (BasicDBList) rootObj.get(key);
		limite = (limite == 0) ? subCursor.size() : limite;
		for (int i = 0; i < subCursor.size(); i++)
		{
			if (i == limite)
				return arrayStr;
			arrayStr.add(subCursor.get(i).toString());
		}

		return arrayStr;
	}

	private ArrayList<String> QueryAutoCompleteByTitle(String Query, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
	{
		ArrayList<String> result = new ArrayList<String>();
		DBCursor cursor = null;
		try
		{
			final String collection_name = "films";
			DBCollection coll = db_handle.getCollection(collection_name);
			db_handle.setWriteConcern(WriteConcern.SAFE);

			BasicDBObject query = new BasicDBObject(type_filter_query.toString(), java.util.regex.Pattern.compile(mk_rgx(Query, type_filter_rgx), Pattern.CASE_INSENSITIVE));
			cursor = coll.find(query);
			while (cursor.hasNext())
			{
				result.add(cursor.next().get(type_filter_query.toString()).toString());
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
	
	private ArrayList<String> QueryAutoCompleteByTitleArray(String Query, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
	{
		ArrayList<String> result = new ArrayList<String>();
		DBCursor cursor = null;
		try
		{
			final String collection_name = "films";
			DBCollection coll = db_handle.getCollection(collection_name);
			db_handle.setWriteConcern(WriteConcern.SAFE);
			BasicDBObject query = new BasicDBObject(type_filter_query.toString(), java.util.regex.Pattern.compile(mk_rgx(Query, type_filter_rgx), Pattern.CASE_INSENSITIVE));
			cursor = coll.find(query);
			while (cursor.hasNext())
			{
				result.addAll(mgdbGetSubArray(cursor.next(), type_filter_query.toString(), 0));
				//result.add(cursor.next().get(type_filter_query.toString()).toString());
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

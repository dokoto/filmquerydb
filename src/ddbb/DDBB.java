package ddbb;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.JsonNode;
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
			return QueryAutoCompleteByTitle(Query, type_filter_rgx);
		case generos:
			return QueryAutoCompleteByTitleArray(Query, type_filter_query, type_filter_rgx);
		case release_date:
			return QueryAutoCompleteByAny(Query, type_filter_query, type_filter_rgx);
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

	
	public String QueryBy(String Query, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
	{
		switch (type_filter_query)
		{
		case actores:
			return QueryGetDataAny(Query, type_filter_query, type_filter_rgx);
		case directores:
			return QueryGetDataAny(Query, type_filter_query, type_filter_rgx);
		case titulo:
			return QueryGetDataTitle(Query, type_filter_rgx);
		case generos:
			return QueryGetDataAny(Query, type_filter_query, type_filter_rgx);
		case release_date:
			return QueryGetDataAny(Query, type_filter_query, type_filter_rgx);
		default:
			return new String();

		}
	}
	
	public String QueryGetDataTitle(String queryTx, efilterRgx type_filter_rgx)
	{
		DBCursor cursor = null;
		try
		{
			final String collection_name = "films";
			DBCollection coll = db_handle.getCollection(collection_name);
			db_handle.setWriteConcern(WriteConcern.SAFE);

			BasicDBObject orQuery = new BasicDBObject();
			List<BasicDBObject> orQueryParams = new ArrayList<BasicDBObject>();
			Pattern regPattern = java.util.regex.Pattern.compile(mk_rgx(queryTx, type_filter_rgx), Pattern.CASE_INSENSITIVE);
			
			orQueryParams.add(new BasicDBObject("titulo", regPattern));
			
			BasicDBObject elemMatch = new BasicDBObject();
			BasicDBObject elemMatchItem = new BasicDBObject();
			elemMatchItem.put("titulo_alt", regPattern);
			elemMatch.put("$elemMatch", elemMatchItem);
			orQueryParams.add(new BasicDBObject("titulos_alternativos", elemMatch));
			
			orQuery.put("$or", orQueryParams);
			cursor = coll.find(orQuery);
			
			
			ArrayList<Map<String, Object>> values = new ArrayList<Map<String, Object>>();

			while (cursor.hasNext())
			{
				DBObject rootObj = cursor.next();
				Map<String, Object> value = new HashMap<String, Object>();

				// TIUTULO
				value.put("titulo", rootObj.get("titulo").toString());

				// GENEROS
				value.put("generos", mgdbGetSubArray(rootObj, "generos", 3, regPattern));

				// DIRECTORES
				value.put("directores", mgdbGetSubArray(rootObj, "directores", 2, regPattern));

				// ACTORES
				value.put("actores", mgdbGetSubArray(rootObj, "actores", 3, regPattern));

				// FECHA DE ESTRENO
				value.put("release_date", rootObj.get("release_date").toString());

				// SINOPSIS
				value.put("sinopsis", rootObj.get("sinopsis").toString());

				// CLAVE IMAGENES
				value.put("foto_mini", rootObj.get("foto_mini").toString());
				value.put("foto_maxi", rootObj.get("foto_maxi").toString());
				
				// ATRIBUTOS DE FICHERO
				DBObject subItem = (DBObject) rootObj.get("fileAttributes");
				value.put("file_full_path", subItem.get("file_full_path").toString());				

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
	
	public String QueryGetDataAny(String queryTx, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
	{
		DBCursor cursor = null;
		try
		{
			final String collection_name = "films";
			DBCollection coll = db_handle.getCollection(collection_name);
			db_handle.setWriteConcern(WriteConcern.SAFE);
			Pattern regPattern = java.util.regex.Pattern.compile(mk_rgx(queryTx, type_filter_rgx), Pattern.CASE_INSENSITIVE);
			Pattern regPatternAll = java.util.regex.Pattern.compile("^.*", Pattern.CASE_INSENSITIVE);
			
			BasicDBObject query = new BasicDBObject(type_filter_query.toString(), regPattern);
			cursor = coll.find(query);
			ArrayList<Map<String, Object>> values = new ArrayList<Map<String, Object>>();

			while (cursor.hasNext())
			{
				DBObject rootObj = cursor.next();
				Map<String, Object> value = new HashMap<String, Object>();

				// TIUTULO
				value.put("titulo", rootObj.get("titulo").toString());

				// GENEROS
				value.put("generos", mgdbGetSubArray(rootObj, "generos", 3, regPatternAll));

				// DIRECTORES
				value.put("directores", mgdbGetSubArray(rootObj, "directores", 2, regPatternAll));

				// ACTORES
				value.put("actores", mgdbGetSubArray(rootObj, "actores", 3, regPatternAll));

				// FECHA DE ESTRENO
				value.put("release_date", rootObj.get("release_date").toString());

				// SINOPSIS
				value.put("sinopsis", rootObj.get("sinopsis").toString());

				// CLAVE IMAGENES
				value.put("foto_mini", rootObj.get("foto_mini").toString());
				value.put("foto_maxi", rootObj.get("foto_maxi").toString());
				
				// ATRIBUTOS DE FICHERO
				DBObject subItem = (DBObject) rootObj.get("fileAttributes");
				value.put("file_full_path", subItem.get("file_full_path").toString());
				
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

	private ArrayList<String> mgdbGetSubArray(DBObject rootObj, String key, int limite, Pattern regPattern)
	{
		ArrayList<String> arrayStr = new ArrayList<String>();
		BasicDBList subCursor = (BasicDBList) rootObj.get(key);
		limite = (limite == 0) ? subCursor.size() : limite;
		for (int i = 0; i < subCursor.size(); i++)
		{
			String item = subCursor.get(i).toString();
			if (i == limite)
				return arrayStr;
			if (regPattern.matcher(item).find())
				arrayStr.add(item);
		}

		return arrayStr;	
	}
	
	private ArrayList<String> QueryAutoCompleteByTitle(String Query, efilterRgx type_filter_rgx)
	{
		ArrayList<String> result = new ArrayList<String>();
		DBCursor cursor = null;
		try
		{
			final String collection_name = "films";
			DBCollection coll = db_handle.getCollection(collection_name);
			db_handle.setWriteConcern(WriteConcern.SAFE);

			BasicDBObject orQuery = new BasicDBObject();
			List<BasicDBObject> orQueryParams = new ArrayList<BasicDBObject>();
			Pattern regPattern = java.util.regex.Pattern.compile(mk_rgx(Query, type_filter_rgx), Pattern.CASE_INSENSITIVE);
			
			orQueryParams.add(new BasicDBObject("titulo", regPattern));
			
			BasicDBObject elemMatch = new BasicDBObject();
			BasicDBObject elemMatchItem = new BasicDBObject();
			elemMatchItem.put("titulo_alt", regPattern);
			elemMatch.put("$elemMatch", elemMatchItem);
			orQueryParams.add(new BasicDBObject("titulos_alternativos", elemMatch));
			
			orQuery.put("$or", orQueryParams);
			cursor = coll.find(orQuery);
			Object o = null;
			while (cursor.hasNext())
			{					
				DBObject objDb = cursor.next(); 
				o = objDb.get("titulo");
				if (null != o)
				{
					if (regPattern.matcher(o.toString()).find())
						result.add(o.toString());	
				}
				
				ObjectMapper mapper = new ObjectMapper();
				JsonNode jsonArray = mapper.readTree(objDb.get("titulos_alternativos").toString());
				if (null != jsonArray)
				{
					for(int i = 0; i < jsonArray.size(); i++)		
					{
						if (regPattern.matcher(jsonArray.get(i).path("titulo_alt").asText()).find())
							result.add(jsonArray.get(i).path("titulo_alt").asText());
					}
				}
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


	private ArrayList<String> QueryAutoCompleteByAny(String Query, efilterQuery type_filter_query, efilterRgx type_filter_rgx)
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
			Pattern regPattern = java.util.regex.Pattern.compile(mk_rgx(Query, type_filter_rgx), Pattern.CASE_INSENSITIVE);
			BasicDBObject query = new BasicDBObject(type_filter_query.toString(), regPattern);
			cursor = coll.find(query);
			while (cursor.hasNext())
			{
				result.addAll(mgdbGetSubArray(cursor.next(), type_filter_query.toString(), 0, regPattern));
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

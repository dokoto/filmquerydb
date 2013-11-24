package utils;

public class filters
{
	
	public enum efilterQuery {titulo, actores, directores, generos, release_date};
	public enum efilterRgx {AL_PRINCIPIO, EN_CUALQUIER_SITIO};
	
	public static String toBASE64(final String text)
	{
		return javax.xml.bind.DatatypeConverter.printBase64Binary(text.getBytes());
	}
	
	public static efilterQuery GetQueryType(String check)
	{
		if (check.compareTo("rb_titulo") == 0)
		{
			return efilterQuery.titulo;
		} else if (check.compareTo("rb_director") == 0)
		{
			return efilterQuery.directores;
		} else if (check.compareTo("rb_actor") == 0)
		{
			return efilterQuery.actores;
		} else if (check.compareTo("rb_genero") == 0)
		{
			return efilterQuery.generos;
		} else if (check.compareTo("rb_fecha_estreno") == 0)
		{
			return efilterQuery.release_date;
		}
		return null;
	}

	public static efilterRgx GetQueryRgx(String check)
	{
		if (check.compareTo("rb_rx_begin") == 0)
		{
			return efilterRgx.AL_PRINCIPIO;
		} else if (check.compareTo("rb_rx_anywhere") == 0)
		{
			return efilterRgx.EN_CUALQUIER_SITIO;
		}
		return null;
	}
}

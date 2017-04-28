package renderfarm.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Auxiliary methods
 * @author Andre
 *
 */
public abstract class RenderFarmUtil{
	
	public static final int HTTP_OK = 200;
	
	/**
	 * Used to parse request parameters
	 * @param query
	 * @return Map with HttpURI <parameter,value>
	 */
	public static Map<String, String> getQueryMap(String query)
	{
	    String[] params = query.split("&");
	    Map<String, String> map = new HashMap<String, String>();
	    for (String param : params)
	    {
	        String name = param.split("=")[0];
	        String value = param.split("=")[1];
	        map.put(name, value);
	    }
	    return map;
	}

}
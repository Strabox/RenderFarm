package renderfarm.loadbalancer.handlers;

import java.io.IOException;
import java.net.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import renderfarm.loadbalancer.LoadBalancerMain;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.InvalidRenderingRequest;
import renderfarm.util.NormalizedWindow;
import renderfarm.util.RenderFarmUtil;
import renderfarm.util.SystemConfiguration;

import java.net.URL;
import java.util.Map;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Load balancer handler that receives the requests from the users and
 * redirect it to our farm.
 * @author Andre
 *
 */
public class RequestHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange http) throws IOException {
		String params = http.getRequestURI().getQuery();
		OutputStream out = http.getResponseBody();
		InputStream is = null;
		Map<String,String> paramMap = RenderFarmUtil.getQueryMap(params); 
		
		try{
			if(!paramMap.containsKey("f") || !paramMap.containsKey("sc") || !paramMap.containsKey("sr") 
	        		|| !paramMap.containsKey("wc") || !paramMap.containsKey("wr")
	        		|| !paramMap.containsKey("coff") || !paramMap.containsKey("roff")) {
			   throw new InvalidRenderingRequest();
		   }
			Long sceneHeight = Long.parseLong(paramMap.get("sr"));
			Long sceneWidth = Long.parseLong(paramMap.get("sc"));
			Long windowWidth = Long.parseLong(paramMap.get("wc"));
			Long windowHeight = Long.parseLong(paramMap.get("wr"));
			Long collumnOffset = Long.parseLong(paramMap.get("coff"));
			Long rowOffset = Long.parseLong(paramMap.get("roff"));
			
			Request request= new Request(paramMap.get("f") ,
					NormalizedWindow.BuildNormalizedWindow(sceneWidth, sceneHeight, windowWidth, windowHeight, collumnOffset, rowOffset)
					,windowWidth * windowHeight);
			
			String ip = LoadBalancerMain.instanceManager.getHandlerInstanceIP(request);
			URL url = new URL("http",ip,SystemConfiguration.RENDER_INSTANCE_PORT,"/r.html?" + params);
			System.out.println("[Handler]" + url.toString());
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();	//Connect with instance
   			is = connection.getInputStream();
    		http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, 0);
			byte[] buffer = new byte[1024]; // Adjust if you want
    		int bytesRead;
    		while ((bytesRead = is.read(buffer)) != -1)
    		{
        		out.write(buffer, 0, bytesRead);
   			}
		} catch(InvalidRenderingRequest e) {
			String response = "Bad request: " + http.getRequestURI().getQuery();
			http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
			out.write(response.getBytes());
			e.printStackTrace();
		} catch (Exception e){
			String response = "No available machines for rendering try later...";
			http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
			out.write(response.getBytes());
			e.printStackTrace();
		}
		finally {	//Set all the resources free
			out.close();
			if(is != null) {
				is.close();
			}
		}
	}

}

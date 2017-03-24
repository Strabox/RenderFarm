package webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import raytracer.Main;

public class MultiThreadedWebServerMain {

	private static final int PORT = 8000; 
	private static final int POOL_SIZE = 5;
	
	/**
	 * Method used to setup and start the webserver
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/r.html", new RenderHandler());
        server.setExecutor(Executors.newFixedThreadPool(POOL_SIZE));
        server.start();
        System.out.println("Listening on Port: " + PORT);
    }

    static class RenderHandler implements HttpHandler {
    	
    	/**
    	 * Used to parse request parameters
    	 * @param query
    	 * @return Map with HttpURI <parameter,value>
    	 */
    	public Map<String, String> getQueryMap(String query)
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
    	
        @Override
        public void handle(HttpExchange t) throws IOException {
        	System.out.println("Thread ID: " + Thread.currentThread().getId());
        	String response;
            Map<String,String> paramMap = getQueryMap(t.getRequestURI().getQuery());
            
            if(paramMap.containsKey("f") && paramMap.containsKey("sc") && paramMap.containsKey("sr") 
            		&& paramMap.containsKey("wc") && paramMap.containsKey("wr")
            		&& paramMap.containsKey("coff") && paramMap.containsKey("roff")) {
            	String[] args = { paramMap.get("f"), "out.bmp", paramMap.get("sc"), paramMap.get("sr"),
            			paramMap.get("wc"), paramMap.get("wr"), paramMap.get("coff"), paramMap.get("roff")};
            	System.out.println(1.01);
				try {
					Main.main(args);
					response = "Ok request: " + t.getRequestURI().getQuery();
					t.sendResponseHeaders(200, response.length());
				} catch (InterruptedException e) {
					response = "Problem Rendering";
					t.sendResponseHeaders(400, response.length());
					e.printStackTrace();
				}
            }
            else {	//Invalid Request - Argument Missing
            	response = "Bad request: " + t.getRequestURI().getQuery();
            	t.sendResponseHeaders(400, response.length());
            }
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}

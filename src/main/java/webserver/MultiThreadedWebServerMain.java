package webserver;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import measures.Measures;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import raytracer.Main;

public class MultiThreadedWebServerMain {

	private static final int PORT = 8000; 
	private static final int THREAD_POOL_SIZE = 5;
	public static ConcurrentHashMap<Long,Measures> hash=new ConcurrentHashMap<Long,Measures>();
	
	/**
	 * Method used to setup and start the webserver
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/r.html", new RenderHandler());
        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
        server.start();
        System.out.println("Listening on Port: " + PORT);
    }
    
    public static void initMeasure(Long threadID){
    	Measures measure= new Measures();
    	hash.put(threadID, measure);	
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
            OutputStream out = t.getResponseBody();
            Long threadID=Thread.currentThread().getId();
            if(hash.get(threadID)==null){
            	initMeasure(threadID);
            }
            
            if(paramMap.containsKey("f") && paramMap.containsKey("sc") && paramMap.containsKey("sr") 
            		&& paramMap.containsKey("wc") && paramMap.containsKey("wr")
            		&& paramMap.containsKey("coff") && paramMap.containsKey("roff")) {
            	String[] args = { paramMap.get("f"), "out" + Thread.currentThread().getId() + ".bmp", paramMap.get("sc"), paramMap.get("sr"),
            			paramMap.get("wc"), paramMap.get("wr"), paramMap.get("coff"), paramMap.get("roff")};
            	System.out.println("INfile:" + args[0] + ";OUTfile:" + args[1] + ";sc:" + args[2] + ";sr:" + 
            			args[3] + ";wc:" + args[4] + ";wr:" + args[5] + ";coff:" + args[6] + ";roff:" + args[7]
            			+ ";");
				try {
					hash.get(threadID).raytracerInput(args[0],Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]));
					Main.main(args);
					response = "Ok request: " + t.getRequestURI().getQuery();
					File image = new File("out" + Thread.currentThread().getId() + ".bmp");
					t.sendResponseHeaders(200, image.length());
					Files.copy(image.toPath(), out);
				} catch(IOException e) {
					response = "Rendering source file not found";
					t.sendResponseHeaders(400, response.length());
					out.write(response.getBytes());
					e.printStackTrace();
				} catch (InterruptedException e) {
					response = "Problem Rendering";
					t.sendResponseHeaders(400, response.length());
					out.write(response.getBytes());
					e.printStackTrace();
				}
            }
            else {	//Invalid Request - Argument Missing
            	response = "Bad request: " + t.getRequestURI().getQuery();
            	t.sendResponseHeaders(400, response.length());
            	out.write(response.getBytes());
            }
            out.close();
        }
    }

}

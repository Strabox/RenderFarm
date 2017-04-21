package webserver;


import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import measures.Measures;

import com.sun.net.httpserver.HttpServer;

public class MultiThreadedWebServerMain {

	private static final int PORT = 8000; 
	
	public static ConcurrentHashMap<Long,Measures> hash = new ConcurrentHashMap<Long,Measures>();
	
	/**
	 * Method used to setup and start the webserver
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/r.html", new RenderHandler());
        server.createContext("/HealthCheck", new HealthCheckHandler());
        server.setExecutor(Executors.newCachedThreadPool());	//Warning: Unbounded thread limit
        server.start();
        System.out.println("Listening on Port: " + PORT);
    }
    
    public static void initMeasure(Long threadID){
    	Measures measure = new Measures();
    	hash.put(threadID, measure);	
    }
    

}

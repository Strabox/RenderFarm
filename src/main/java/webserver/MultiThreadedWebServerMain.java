package webserver;


import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import metrics.Metric;

/**
 * Class to launch the webserver instance that receive requests to raytracer.
 * @author Andre
 *
 */
public class MultiThreadedWebServerMain {

	private static final int PORT = 8000; 
	
	public static ConcurrentHashMap<Long,Metric> metricsGatherer = new ConcurrentHashMap<Long,Metric>();
	
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
        System.out.println("Raytracer webserver running, listening on Port: " + PORT);
    }
    
    public static void initMeasure(Long threadID){
    	Metric metric = new Metric();
    	metricsGatherer.put(threadID, metric);	
    }
    

}

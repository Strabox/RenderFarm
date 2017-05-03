package renderfarm.instance;


import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import renderfarm.instance.handlers.HealthCheckHandler;
import renderfarm.instance.handlers.RenderHandler;
import renderfarm.util.Metric;
import renderfarm.util.SystemConfiguration;

/**
 * Class to launch the webserver instance that receive requests to raytracer.
 * @author Andre
 *
 */
public class MultiThreadedWebServerMain {
	
	public static ConcurrentHashMap<Long,Metric> metricsGatherer = new ConcurrentHashMap<Long,Metric>();
	
	/**
	 * Method used to setup and start the webserver
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(SystemConfiguration.RENDER_INSTANCE_PORT), 0);
        server.createContext("/r.html", new RenderHandler());
        server.createContext("/HealthCheck", new HealthCheckHandler());
        server.setExecutor(Executors.newCachedThreadPool());	//Warning: Unbounded thread limit
        server.start();
        System.out.println("Raytracer webserver running, listening on Port: " + SystemConfiguration.RENDER_INSTANCE_PORT);
    }
    
    public static void initMeasure(Long threadID){
    	Metric metric = new Metric();
    	metricsGatherer.put(threadID, metric);	
    }
    

}

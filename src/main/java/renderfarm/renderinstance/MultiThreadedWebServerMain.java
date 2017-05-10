package renderfarm.renderinstance;


import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import renderfarm.dynamo.AmazonDynamoDB;
import renderfarm.renderinstance.handlers.HealthCheckHandler;
import renderfarm.renderinstance.handlers.RenderHandler;
import renderfarm.util.Metric;
import renderfarm.util.SystemConfiguration;

/**
 * Class to launch the webserver instance that receive requests to raytracer.
 * @author Andre
 *
 */
public class MultiThreadedWebServerMain {
	
	public static ConcurrentHashMap<Long,Metric> metricsGatherer = new ConcurrentHashMap<Long,Metric>();
	
	public static AmazonDynamoDB dynamoDB;
	
	/**
	 * Method used to setup and start the webserver
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
    	if(args.length == 2) {
    		dynamoDB = new AmazonDynamoDB(args[0], args[1]);
    	}
    	else {
    		dynamoDB = new AmazonDynamoDB(null, null);
    	}
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

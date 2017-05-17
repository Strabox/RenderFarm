package renderfarm.loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;

import renderfarm.autoscaler.AutoScaler;
import renderfarm.dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.handlers.FarmStatusHandler;
import renderfarm.loadbalancer.handlers.RequestHandler;
import renderfarm.loadbalancer.loadbalancing.LoadBalancing;
import renderfarm.loadbalancer.loadbalancing.BestLoadBalancing;

/**
 * Main class to launch and hold our custom Load Balancer
 * @author Andre
 *
 */
public class LoadBalancerMain {
	
	/**
	 * Load balancer PORT to accept the HTTP requests.
	 */
	private static final int LOAD_BALANCER_PORT = 8000;
	
	/**
	 * Render farm instances manager
	 */
	private static RenderFarmInstanceManager instanceManager;
	
	/**
	 * Our load balancer entry point.
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("[LOADBALANCER MAIN]Set up load balancer...");
		AmazonDynamoDB dynamoDB;
		LoadBalancing loadBalacing;
		if(args.length >= 2) {
			dynamoDB = new AmazonDynamoDB(args[0],args[1]);
			loadBalacing = new BestLoadBalancing(dynamoDB);
			instanceManager = new RenderFarmInstanceManager(loadBalacing,true,args[0],args[1]);
		} else {
			dynamoDB = new AmazonDynamoDB(null,null);
			loadBalacing = new BestLoadBalancing(dynamoDB);
			instanceManager = new RenderFarmInstanceManager(loadBalacing,false,null,null);
		}
		initAutoScaler();
		initFaultDetector();
		initLoadBalancer();
	}
	
	/**
	 * Start the auto scaler component
	 */
	private static void initAutoScaler() {
		AutoScaler autoScaler = new AutoScaler(instanceManager);
		autoScaler.start();
	}
	
	/**
	 * Start the auto scaler component
	 */
	private static void initFaultDetector() {
		RenderFarmInstanceFaultDetector fd = new RenderFarmInstanceFaultDetector(instanceManager);
		fd.start();
	}
	
	/**
	 * Start the load balancer web server to start receiving requests
	 * @throws IOException
	 */
	private static void initLoadBalancer() {
		final int THREAD_POOL_SIZE = 100;
		try {
			System.out.println("Initializing load balancing algorithm");
			System.out.println("Initializing load balancer request handler");
			HttpServer server = HttpServer.create(new InetSocketAddress(LOAD_BALANCER_PORT), 0);
			server.createContext("/r.html", new RequestHandler(instanceManager));
			server.createContext("/FarmStatus", new FarmStatusHandler(instanceManager));
			server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
			server.start();
			System.out.println("[LOADBALANCER MAIN]Loadbalancer, listening on Port: " + LOAD_BALANCER_PORT);
		} catch(IOException e) {
			System.out.println("[LOADBALANCER MAIN]Problem starting Load Balancer web server, exiting...");
			e.printStackTrace();
			System.exit(-1);
		}

	}
    
}

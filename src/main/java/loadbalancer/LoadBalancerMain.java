package loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

/**
 * Main class to launch and hold our custom Load Balancer
 * @author Andre
 *
 */
public class LoadBalancerMain {
	
	private static final int LOAD_BALANCER_PORT = 8000;
	
	private static RenderFarmInstanceManager instanceManager;
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("Starting Load Balancer...");
		if(args.length >= 2) {
			instanceManager = new RenderFarmInstanceManager(true,args[0],args[1]);
		} else {
			instanceManager = new RenderFarmInstanceManager(false,null,null);
		}
		instanceManager.launchInstance();
		initLoadBalancer();
	}
	
	/**
	 * Start the load balancer webserver to start receiving requests
	 * @throws IOException
	 */
	private static void initLoadBalancer() throws IOException {
		System.out.print("Initializing load balancer request handler...");
		HttpServer server = HttpServer.create(new InetSocketAddress(LOAD_BALANCER_PORT), 0);
		server.createContext("/r.html", new RequestHandler());
		server.setExecutor(Executors.newCachedThreadPool());	//Warning: Unbounded thread limit
		server.start();
		System.out.println("Loadbalancer, listening on Port: " + LOAD_BALANCER_PORT);
	}
    
}

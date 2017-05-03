package renderfarm.loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;

import renderfarm.autoscaler.AutoScaler;
import renderfarm.loadbalancer.handlers.RequestHandler;
import renderfarm.loadbalancer.loadbalancing.FilipeStyleLoadBalancing;
import renderfarm.loadbalancer.loadbalancing.LoadBalancing;

/**
 * Main class to launch and hold our custom Load Balancer
 * @author Andre
 *
 */
public class LoadBalancerMain {
	
	private static final int LOAD_BALANCER_PORT = 8000;
	
	private static RenderFarmInstanceManager instanceManager;
	
	//To change the load balancing style, change the object below.
	private static final LoadBalancing loadBalacing = new FilipeStyleLoadBalancing();
	
	//Auto scaler object that are executing auto scaler thread
	@SuppressWarnings("unused")
	private static AutoScaler autoScaler;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("Starting Load Balancer...");
		if(args.length >= 2) {
			instanceManager = new RenderFarmInstanceManager(loadBalacing,true,args[0],args[1]);
		} else {
			instanceManager = new RenderFarmInstanceManager(loadBalacing,false,null,null);
		}
		initAutoScaler();
		initLoadBalancer();
	}
	
	/**
	 * Start the auto scaler component
	 */
	private static void initAutoScaler() {
		AutoScaler autoScaler = new AutoScaler(instanceManager);
		LoadBalancerMain.autoScaler = autoScaler;
		autoScaler.start();
	}
	
	/**
	 * Start the load balancer webserver to start receiving requests
	 * @throws IOException
	 */
	private static void initLoadBalancer() {
		try {
			System.out.print("Initializing load balancer request handler...\n");
			HttpServer server = HttpServer.create(new InetSocketAddress(LOAD_BALANCER_PORT), 0);
			server.createContext("/r.html", new RequestHandler(instanceManager));
			server.setExecutor(Executors.newCachedThreadPool());	//Warning: Unbounded thread limit
			server.start();
			System.out.println("Loadbalancer, listening on Port: " + LOAD_BALANCER_PORT);
		} catch(IOException e) {
			System.out.println("Problem starting Load Balancer web server");
			e.printStackTrace();
		}

	}
    
}

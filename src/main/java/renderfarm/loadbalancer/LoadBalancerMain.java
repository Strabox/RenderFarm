package renderfarm.loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;

import renderfarm.autoscaler.AutoScaler;
import renderfarm.loadbalancer.handlers.FarmStatusHandler;
import renderfarm.loadbalancer.handlers.RequestHandler;
import renderfarm.loadbalancer.loadbalancing.FilipeStyleLoadBalancing;
import renderfarm.loadbalancer.loadbalancing.LoadBalancing;
import dynamo.AmazonDynamoDB;
/**
 * Main class to launch and hold our custom Load Balancer
 * @author Andre
 *
 */
public class LoadBalancerMain {
	
	/**
	 * Load balancer PORT to accept the http requests.
	 */
	private static final int LOAD_BALANCER_PORT = 8000;
	
	private static RenderFarmInstanceManager instanceManager;

	private static AmazonDynamoDB dynamoDB;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("Starting Load Balancer...");
		LoadBalancing loadBalacing = new FilipeStyleLoadBalancing(dynamoDB);
		if(args.length >= 2) {
			instanceManager = new RenderFarmInstanceManager(loadBalacing,true,args[0],args[1]);
		} else {
			instanceManager = new RenderFarmInstanceManager(loadBalacing,false,null,null);
		}
		initDynamoDB();
		initAutoScaler();
		initLoadBalancer();
		/*dynamoDB.putItem("file1.txt", (float) 0.5,(float) 0.5,(float) 0.5,(float) 0.5, (long)1000000,300000000000000000L,(long)2000000,(long)222222222, 5);
		dynamoDB.putItem("file2.txt", (float) 0.5,(float) 0.5,(float) 0.5,(float) 0.5, (long)1000000,(long)300,(long)2000000,(long)222222222, 5);
		List<Metric> metric=dynamoDB.getIntersectiveItems("file1.txt", (float) 0.5, (float) 0.5, (float) 0.5, (float) 0.5);
		System.out.println(metric.get(0).toString());*/
	}
	
	/**
	 * Start the auto scaler component
	 */
	private static void initAutoScaler() {
		AutoScaler autoScaler = new AutoScaler(instanceManager);
		autoScaler.start();
	}
	
	/**
	 * Start the load balancer webserver to start receiving requests
	 * @throws IOException
	 */
	private static void initLoadBalancer() {
		try {
			System.out.println("Initializing load balancing algorithm");
			System.out.println("Initializing load balancer request handler");
			HttpServer server = HttpServer.create(new InetSocketAddress(LOAD_BALANCER_PORT), 0);
			server.createContext("/r.html", new RequestHandler(instanceManager));
			server.createContext("/FarmStatus", new FarmStatusHandler(instanceManager));
			server.setExecutor(Executors.newCachedThreadPool());	//Warning: Unbounded thread limit
			server.start();
			System.out.println("Loadbalancer, listening on Port: " + LOAD_BALANCER_PORT);
		} catch(IOException e) {
			System.out.println("Problem starting Load Balancer web server, exiting...");
			e.printStackTrace();
			System.exit(-1);
		}

	}
	private static void initDynamoDB(){
		//dynamoDB = new AmazonDynamoDB();
	}
    
}

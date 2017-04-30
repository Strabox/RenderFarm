package renderfarm.loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.amazonaws.AmazonServiceException;

import com.sun.net.httpserver.HttpServer;

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
	
	private static final int LOAD_BALANCER_PORT = 8000;
	
	private static final int INITIAL_NUMBER_OF_INSTANCES = 1;
	
	public static RenderFarmInstanceManager instanceManager;
	
	//To change the load balancing style, change the object below.
	private static final LoadBalancing loadBalacing = new FilipeStyleLoadBalancing();
	
	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("Starting Load Balancer...");
		if(args.length >= 2) {
			instanceManager = new RenderFarmInstanceManager(loadBalacing,true,args[0],args[1]);
		} else {
			instanceManager = new RenderFarmInstanceManager(loadBalacing,false,null,null);
		}
		try{
			System.out.println("Starting " + "" +INITIAL_NUMBER_OF_INSTANCES +" FarmInstances...\n");
			instanceManager.launchInstance(INITIAL_NUMBER_OF_INSTANCES);
			System.out.println("Two FarmInstances created...\n");
			System.out.println("number of instances up " + "" +instanceManager.getCurrentInstances().size());
		}
		catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }
		initLoadBalancer();
		//AmazonDynamoDB dynamo = new AmazonDynamoDB();
		//dynamo.putItem("file1.txt",(float)0.111111,(float) 0.876,(float)0.567,(float)0.678,900000000,300000,50000, 6000000,"Muito");
	}
	
	/**
	 * Start the load balancer webserver to start receiving requests
	 * @throws IOException
	 */
	private static void initLoadBalancer() {
		try {
			System.out.print("Initializing load balancer request handler...\n");
			HttpServer server = HttpServer.create(new InetSocketAddress(LOAD_BALANCER_PORT), 0);
			server.createContext("/r.html", new RequestHandler());
			server.setExecutor(Executors.newCachedThreadPool());	//Warning: Unbounded thread limit
			server.start();
			System.out.println("Loadbalancer, listening on Port: " + LOAD_BALANCER_PORT);
		} catch(IOException e) {
			System.out.println("Problem starting Load Balancer web server");
			e.printStackTrace();
		}

	}
    
}

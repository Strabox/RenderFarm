package renderfarm.loadbalancer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;

import renderfarm.autoscaler.AutoScaler;
import renderfarm.autoscaler.BestAutoScaling;
import renderfarm.dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.handlers.FarmStatusHandler;
import renderfarm.loadbalancer.handlers.RequestHandler;
import renderfarm.loadbalancer.loadbalancing.LoadBalancing;
import renderfarm.util.RenderFarmInstanceHealthCheck;
import renderfarm.loadbalancer.loadbalancing.BestLoadBalancing;

/**
 * Main class to launch and hold our custom Load Balancer
 * @author Andre
 *
 */
public class LoadBalancerMain {
	
	/**
	 * Name of the configuration properties file
	 */
	private static final String CONFIG_PROPERTIES_FILE = "config.properties";
	
	/**
	 * Configuration strings from the properties file
	 */
	private static final String CONFIG_STRING_LOADBALANCER_PORT = "loadbalancer.port";
	private static final String CONFIG_STRING_LOADBALANCER_MAX_RETRIES = "loadbalancer.maximumRetries";
	private static final String CONFIG_STRING_LOADBALANCER_RETRY_INTERVAL = "loadbalancer.retryInterval";
	private static final String CONFIG_STRING_LOADBALANCER_MAXIMUM_LOAD = "loadbalancer.maximumLoad";
	private static final String CONFIG_STRING_LOADBALANCER_MAXIMUM_TRIES_FOR_UP = "loadbalancer.maximumTriesForUp";
	private static final String CONFIG_STRING_LOADBALANCER_MAXIMUM_OVERLAPPING_AREA_WEIGHT = "loadbalancer.overlappingAreaWeight";
	private static final String CONFIG_STRING_LOADBALANCER_MAXIMUM_OVERLAPPING_AREA_IN_METRIC_WEIGHT = "loadbalancer.overlappingAreaInMetricWeight";
	private static final String CONFIG_STRING_AUTOSCALE_INIT_INST = "autoScaler.initialInstances";
	private static final String CONFIG_STRING_AUTOSCALE_INTERVAL = "autoScaler.interval";
	private static final String CONFIG_STRING_AUTOSCALE_MIN_INSTANCES_UP = "autoScaler.minimumInstancesAlwaysUp";
	private static final String CONFIG_STRING_AUTOSCALE_MAX_PERC_CLUSTER_TO_DIE = "autoScaler.maximumPercOfClusterToDie";
	private static final String CONFIG_STRING_FAULTDETECTOR_DELAY = "faultDetector.delayToStart";
	private static final String CONFIG_STRING_FAULTDETECTOR_INTERVAL = "faultDetector.interval";
	private static final String CONFIG_STRING_FAULTDETECTOR_TIMEOUT = "faultDetector.timeout";
	private static final String CONFIG_STRING_INSTANCE_SECURITY_GROUP = "instance.securityGroup";
	private static final String CONFIG_STRING_INSTANCE_IMAGE_ID = "instance.imageId";
	private static final String CONFIG_STRING_INSTANCE_KEY_PAIR_NAME = "instance.keyPairName";
	/**
	 * Load balancer PORT to accept the HTTP requests.
	 */
	private static int LOAD_BALANCER_PORT;
	
	/**
	 * Render farm instances manager
	 */
	private static RenderFarmInstanceManager instanceManager;
	
	/**
	 * Our load balancer entry point.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("[LOADBALANCER MAIN]Set up load balancer...");
		AmazonDynamoDB dynamoDB;
		LoadBalancing loadBalacing;
		initConfiguration();
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
	 * Read the configuration file and set the properties accordingly
	 */
	private static void initConfiguration() {
		System.out.println("@@@@@@@@@@@@@@@@@@ Configuration Settings @@@@@@@@@@@@@@@@@@");
		Properties prop = new Properties();
		InputStream configFileInput = null;
		try {
			configFileInput = new FileInputStream(CONFIG_PROPERTIES_FILE);
			prop.load(configFileInput);
			System.out.println(CONFIG_STRING_AUTOSCALE_INIT_INST + "=" + (AutoScaler.INITIAL_NUMBER_OF_INSTANCES = Integer.parseInt(prop.getProperty(CONFIG_STRING_AUTOSCALE_INIT_INST,"2"))));
			System.out.println(CONFIG_STRING_AUTOSCALE_INTERVAL + "=" + (AutoScaler.AUTO_SCALING_TIME_INTERVAL = Integer.parseInt(prop.getProperty(CONFIG_STRING_AUTOSCALE_INTERVAL,"90000"))));
			System.out.println(CONFIG_STRING_AUTOSCALE_MIN_INSTANCES_UP + "=" + (BestAutoScaling.MINIMUM_INSTANCE_ALWAYS_UP = Integer.parseInt(prop.getProperty(CONFIG_STRING_AUTOSCALE_MIN_INSTANCES_UP,"2"))));
			System.out.println(CONFIG_STRING_AUTOSCALE_MAX_PERC_CLUSTER_TO_DIE + "=" + (BestAutoScaling.PERCENTAGE_OF_CLUSTER_SIZE_TO_DIE = Float.parseFloat(prop.getProperty(CONFIG_STRING_AUTOSCALE_MAX_PERC_CLUSTER_TO_DIE,"0.25"))));
			System.out.println(CONFIG_STRING_LOADBALANCER_PORT + "=" + (LoadBalancerMain.LOAD_BALANCER_PORT = Integer.parseInt(prop.getProperty(CONFIG_STRING_LOADBALANCER_PORT,"8000"))));
			System.out.println(CONFIG_STRING_LOADBALANCER_MAX_RETRIES + "=" + (RequestHandler.MAXIMUM_NUMBER_OF_RETRIES = Integer.parseInt(prop.getProperty(CONFIG_STRING_LOADBALANCER_MAX_RETRIES,"3"))));
			System.out.println(CONFIG_STRING_LOADBALANCER_RETRY_INTERVAL + "=" + (RequestHandler.TIME_INTERVAL_TO_RETRY = Integer.parseInt(prop.getProperty(CONFIG_STRING_LOADBALANCER_RETRY_INTERVAL,"15000"))));
			System.out.println(CONFIG_STRING_LOADBALANCER_MAXIMUM_LOAD + "=" + (BestLoadBalancing.MAXIMUM_LOAD = Integer.parseInt(prop.getProperty(CONFIG_STRING_LOADBALANCER_MAXIMUM_LOAD,"5"))));
			System.out.println(CONFIG_STRING_LOADBALANCER_MAXIMUM_TRIES_FOR_UP + "=" + (BestLoadBalancing.MAXIMUM_TRIES_FOR_UP = Integer.parseInt(prop.getProperty(CONFIG_STRING_LOADBALANCER_MAXIMUM_TRIES_FOR_UP,"3"))));
			System.out.println(CONFIG_STRING_LOADBALANCER_MAXIMUM_OVERLAPPING_AREA_WEIGHT + "=" + (LoadBalancing.OVERLAPPING_AREA_WEIGHT = Float.parseFloat(prop.getProperty(CONFIG_STRING_LOADBALANCER_MAXIMUM_OVERLAPPING_AREA_WEIGHT,"0.7"))));
			System.out.println(CONFIG_STRING_LOADBALANCER_MAXIMUM_OVERLAPPING_AREA_IN_METRIC_WEIGHT + "=" + (LoadBalancing.OVERLAPPING_AREA_PERCENTAGE_IN_METRIC_WEIGHT = Float.parseFloat(prop.getProperty(CONFIG_STRING_LOADBALANCER_MAXIMUM_OVERLAPPING_AREA_IN_METRIC_WEIGHT,"0.3"))));
			System.out.println(CONFIG_STRING_FAULTDETECTOR_DELAY + "=" + (RenderFarmInstanceFaultDetector.DELAY_TO_START = Integer.parseInt(prop.getProperty(CONFIG_STRING_FAULTDETECTOR_DELAY,"90000"))));
			System.out.println(CONFIG_STRING_FAULTDETECTOR_INTERVAL + "=" + (RenderFarmInstanceFaultDetector.INTERVAL_OF_FAULT_DETECTION_POLLING = Integer.parseInt(prop.getProperty(CONFIG_STRING_FAULTDETECTOR_INTERVAL,"60000"))));
			System.out.println(CONFIG_STRING_FAULTDETECTOR_TIMEOUT + "=" + (RenderFarmInstanceHealthCheck.WAIT_FOR_REPLY_TIMEOUT = Integer.parseInt(prop.getProperty(CONFIG_STRING_FAULTDETECTOR_TIMEOUT,"5000"))));
			System.out.println(CONFIG_STRING_INSTANCE_SECURITY_GROUP + "=" + (RenderFarmInstanceManager.SECURITY_GROUP = prop.getProperty(CONFIG_STRING_INSTANCE_SECURITY_GROUP,"cnv-ssh+http")));
			System.out.println(CONFIG_STRING_INSTANCE_IMAGE_ID + "=" + (RenderFarmInstanceManager.RENDER_IMAGE_ID = prop.getProperty(CONFIG_STRING_INSTANCE_IMAGE_ID,"ami-44acca24")));
			System.out.println(CONFIG_STRING_INSTANCE_KEY_PAIR_NAME + "=" + (RenderFarmInstanceManager.RENDER_KEY_PAIR_NAME = prop.getProperty(CONFIG_STRING_INSTANCE_KEY_PAIR_NAME,"PROJECT_FINAL_KEY")));
		} catch(IOException e) {
			System.out.println("[Config]Config properties file not found (Must have the file at least)!");
		} finally {
			if(configFileInput != null) {
				try {
					configFileInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
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

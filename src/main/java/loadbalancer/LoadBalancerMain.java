package loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.sun.net.httpserver.HttpServer;

/**
 * Main class to launch and hold our custom Load Balancer
 * @author Andre
 *
 */
public class LoadBalancerMain {

	private static final Regions AVAILABILITY_ZONE = Regions.US_WEST_2;
	
	private static final String SECURITY_GROUP = "CNV-ssh+http";
	
	private static final String RENDER_IMAGE_ID = "ami-db73ecbb";
	private static final String RENDER_INSTANCE_TYPE = "t2.micro";
	private static final String RENDER_KEY_PAIR_NAME = "CNV-lab-AWS";
	
	private static final int LOAD_BALANCER_PORT = 80;
	
	private static AmazonEC2 ec2;
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("Starting Load Balancer...");
		initAWSCredentials();
		initAWSInfrastructure();
		initLoadBalancer();
	}
	
	 /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a  client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    private static void initAWSCredentials() {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
    	System.out.println("Initializing AWS credentials...");
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion(AVAILABILITY_ZONE).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }
    
	/**
	 * Start the first instances that will be part of our system.
	 * @throws InterruptedException 
	 */
	private static void initAWSInfrastructure() throws InterruptedException {
		System.out.println("Initializing AWS infrastructure");
		try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");

            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
            List<Reservation> reservations = describeInstancesRequest.getReservations();
            Set<Instance> instances = new HashSet<Instance>();

            for (Reservation reservation : reservations) {
                instances.addAll(reservation.getInstances());
            }

            System.out.println("You have " + instances.size() + " Amazon EC2 instance(s) running.");
            System.out.println("Starting a new instance.");
            
            startNewInstance(RENDER_IMAGE_ID, RENDER_INSTANCE_TYPE,
            		RENDER_KEY_PAIR_NAME, SECURITY_GROUP);
            		
            describeInstancesRequest = ec2.describeInstances();
            reservations = describeInstancesRequest.getReservations();
            instances = new HashSet<Instance>();

            for (Reservation reservation : reservations) {
                instances.addAll(reservation.getInstances());
            }

            System.out.println("You have " + instances.size() + " Amazon EC2 instance(s) running.");
            System.out.println("Waiting 1 minute. See your instance in the AWS console...");
            System.out.println("Terminating the instance.");                 
        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }
	}
    
	/**
	 * @param imageId
	 * @param instanceType
	 * @param keyPairName
	 * @param securityGroup
	 * @return InstanceId 
	 */
	private static String startNewInstance(String imageId,String instanceType,String keyPairName,String securityGroup){
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(imageId)
                           .withInstanceType(instanceType)
                           .withMinCount(1)
                           .withMaxCount(1)
                           .withKeyName(keyPairName)
                           .withSecurityGroups(securityGroup);
        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        return runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
	}
	
	/**
	 * 
	 * @param instanceId
	 */
	private static void terminateInstance(String instanceId) {
		TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
		termInstanceReq.withInstanceIds(instanceId);
		ec2.terminateInstances(termInstanceReq);
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

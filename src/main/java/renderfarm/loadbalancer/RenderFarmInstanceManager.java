package renderfarm.loadbalancer;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequestException;
import renderfarm.loadbalancer.exceptions.RedirectFailedException;
import renderfarm.loadbalancer.loadbalancing.LoadBalancing;

import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateChange;

import renderfarm.util.RenderFarmInstanceHealthCheck;

/**
 * Class to manage the operations of our render farm instances
 * @author Andre
 *
 */
public class RenderFarmInstanceManager {

	/**
	 * Amazon Web Services configurations.
	 */
	private static final Regions AVAILABILITY_ZONE = Regions.US_WEST_2;
	private static final String RENDER_INSTANCE_TYPE = "t2.micro";
	private static final String SECURITY_GROUP = "cnv-ssh+http";
	private static final String RENDER_IMAGE_ID = "ami-1fdebb7f";
	private static final String RENDER_KEY_PAIR_NAME = "PROJECT_FINAL_KEY";
	
	//AmazonEC2 API Object
	private AmazonEC2 ec2;	//Thread Safe
	
	//Represents all the render farm instances currently running
	private List<RenderFarmInstance> currentInstances;	//Thread Safe (EXCEPT when iterating)
	
	//Object that implements the load balancing logic
	private LoadBalancing loadBalancing;				//Thread Safe
	
	
	public RenderFarmInstanceManager(LoadBalancing loadBalacing,boolean directCredentials,
			String accessId,String accessKey) {
		AWSCredentials credentials = null;
		try {
			credentials = obtainCredentials(directCredentials, accessId, accessKey);
		} catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
		ec2 = AmazonEC2ClientBuilder.standard().withRegion(AVAILABILITY_ZONE)
        		.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
		currentInstances = Collections.synchronizedList(new ArrayList<RenderFarmInstance>());
		loadBalancing = loadBalacing;
	}
	
	/**
	 * Obtain credentials to use in AWS web service requests.
	 * @param directCredentials
	 * @param accessId
	 * @param accessKey
	 * @return
	 */
	private AWSCredentials obtainCredentials(boolean directCredentials,String accessId,String accessKey) {
		if(directCredentials) {
			return new BasicAWSCredentials(accessId,accessKey);
		} else {
			return new ProfileCredentialsProvider().getCredentials();
		}
	}
	
	/**
	 * Launch render nInstances farm instances instances in AWS
	 * @param nInstances Number of instances to launch
	 * @throws AmazonServiceException
	 */
	public void launchInstance(int nInstances) throws AmazonServiceException {
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(RENDER_IMAGE_ID)
                           .withInstanceType(RENDER_INSTANCE_TYPE)
                           .withMinCount(nInstances)
                           .withMaxCount(nInstances)
                           .withKeyName(RENDER_KEY_PAIR_NAME)
                           .withSecurityGroups(SECURITY_GROUP);
        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        List<Instance> instances = runInstancesResult.getReservation().getInstances();
        for(Instance i: instances) {
        	RenderFarmInstance instance = new RenderFarmInstance(i.getInstanceId());
        	currentInstances.add(instance);
        }
	}

	/**
	 * Launch a single render farm instance in AWS
	 * @return RendarfarmInstance local structure that was launched
	 * @throws AmazonServiceException
	 */
	public RenderFarmInstance launchInstance() throws AmazonServiceException {
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(RENDER_IMAGE_ID)
                           .withInstanceType(RENDER_INSTANCE_TYPE)
                           .withMinCount(1)
                           .withMaxCount(1)
                           .withKeyName(RENDER_KEY_PAIR_NAME)
                           .withSecurityGroups(SECURITY_GROUP);
        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        List<Instance> instances = runInstancesResult.getReservation().getInstances();
        RenderFarmInstance instance = new RenderFarmInstance(instances.get(0).getInstanceId());
        currentInstances.add(instance);
        return instance;
	}
	
	/**
	 * Terminate instances that we no longer need
	 * @param instancesIds List with all instances we want terminate
	 */
	public void terminateInstances(List<RenderFarmInstance> instancesToTerminate) {
		List<String> goingToDie = new ArrayList<String>();
		for(RenderFarmInstance instance : instancesToTerminate) {
			//if true instance now can't receive more requests and has 0 requests running!
			if(instance.readyToBeTerminated()) {
				goingToDie.add(instance.getId());
			}
		}
		TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
		termInstanceReq.withInstanceIds(goingToDie);
		TerminateInstancesResult result = ec2.terminateInstances(termInstanceReq);
		List<InstanceStateChange> instancesState = result.getTerminatingInstances();
		for(InstanceStateChange instanceState : instancesState) {
			int currentState = instanceState.getCurrentState().getCode();
			if(currentState == RenderFarmInstance.SHUTTING_DOWN ||
				currentState == RenderFarmInstance.STOPPED ||
				currentState == RenderFarmInstance.STOPPING ||
				currentState == RenderFarmInstance.TERMINATED) {
				removeRenderFarmInstance(instanceState.getInstanceId());
			}
		}
	}
	
	/**
	 * Remove all the instances from currentInstances that are dead
	 * (Not responding)
	 */
	public void removeDeadRenderFarmInstances() {
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
   	 	describeInstancesRequest.getMaxResults();
   	 	DescribeInstancesResult res = ec2.describeInstances(describeInstancesRequest);
   	 	List<Instance> instances = res.getReservations().get(0).getInstances();
   	 	for(Instance instance : instances) {
   	 		if(instance.getState().getCode() == RenderFarmInstance.SHUTTING_DOWN ||
   	 			instance.getState().getCode() == RenderFarmInstance.STOPPED ||
   	 			instance.getState().getCode() == RenderFarmInstance.TERMINATED ) {
   	 			removeRenderFarmInstance(instance.getInstanceId());
   	 		}
   	 	}
	}
	
	/**
	 * Remove the instance from our internal structure given the ID
	 * @param instanceId Instance Id
	 */
	private void removeRenderFarmInstance(String instanceId) {
		synchronized (currentInstances) {
			for(RenderFarmInstance instance : currentInstances) {
				if(instance.getId().equals(instanceId)) {
					currentInstances.remove(instance);
					return;
				}
			}
		}
	}
	
	private List<String> getCurrentInstancesId() {
		List<String> res = new ArrayList<String>();
		synchronized (currentInstances) {
			for(RenderFarmInstance instance: currentInstances) {
				res.add(instance.getId());
			}
		}
		return res;
	}
	
	/**
	 * Remove a request from a render farm instance
	 * @param instanceIP Instance IP
	 * @param request Request to be deleted
	 */
	public void removeRequestFromInstance(RenderFarmInstance instance,Request request) {
		if(currentInstances.indexOf(instance) != -1) {
			currentInstances.get(currentInstances.indexOf(instance)).removeRequest(request);
		}
		else {
			return;
		}
	}
	
	/**
	 * Verify in AWS if the instance is running or not.
	 * @param instanceId AWS instance id
	 * @return
	 */
	public boolean isInstanceRunning(RenderFarmInstance instance) {
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
   	 	describeInstancesRequest.withInstanceIds(instance.getId());
   	 	DescribeInstancesResult res = ec2.describeInstances(describeInstancesRequest);
   	 	InstanceState state = res.getReservations().get(0).getInstances().get(0).getState();
   	 	if(instance.getIp() == null) {
   	 		instance.setIp(res.getReservations().get(0).getInstances().get(0).getPublicIpAddress());
   	 	}
   	 	return state.getCode() == RenderFarmInstance.RUNNING;
	}
	
	/**
	 * Get all current instances
	 * @return List of the current running instances
	 */
	public List<RenderFarmInstance> getCurrentRunningInstances(){
		return currentInstances;
	}
	
	/**
	 * Get the IP of the "best" machine to handle the request (Load Balancing logic)
	 * @param request Request that arrived to the load balancer handler
	 * @return IP of the machine which will handle the request
	 * @throws NoInstancesToHandleRequestException 
	 * @throws RedirectFailedException 
	 */
	public RenderFarmInstance getHandlerInstanceIP(Request request) 
			throws NoInstancesToHandleRequestException, RedirectFailedException {
		return loadBalancing.getFitestMachine(this, request);
	}

	
	public RenderFarmInstance createReadyInstance(){
		//TODO Tornar o metodo indestrutivel
		final int polling_interval = 5000;
		try{
			RenderFarmInstance instance =launchInstance();
			while(!isInstanceRunning(instance)){
				Thread.sleep(polling_interval);
			}
			while(!new RenderFarmInstanceHealthCheck(instance.getIp()).isUp()){
				Thread.sleep(polling_interval);
			}
			return instance;
		}
		catch(Exception e){
			System.out.println("[createReadyInstance] Threads problems");
		}
		return null;

	}
	
	@Override
	public String toString() {
		String res = "============== ALL INSTANCES ================" + System.lineSeparator();
		synchronized (currentInstances) {
			res += "Number of Instance Running: " + currentInstances.size() + System.lineSeparator();
			for(RenderFarmInstance instance: currentInstances) {
				res += instance + System.lineSeparator();
			}
		}
		res += "=========================================";
		return res;
	}
	
}

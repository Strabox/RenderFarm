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
	 * Amazon Web Services configurations
	 */
	public static Regions AVAILABILITY_ZONE = Regions.US_WEST_2;
	public static String RENDER_INSTANCE_TYPE = "t2.micro";
	public static String SECURITY_GROUP = "cnv-ssh+http";
	public static String RENDER_IMAGE_ID = "ami-44acca24";
	public static String RENDER_KEY_PAIR_NAME = "PROJECT_FINAL_KEY";
	
	//AmazonEC2 API Object (THREAD SAFE)
	private AmazonEC2 ec2;
	
	//Represents all the render farm instances currently running (THREAD SAFE EXCEPT when iterating)
	private List<RenderFarmInstance> currentInstances;
	
	//Object that implements the load balancing logic (THREAD SAFE)
	private LoadBalancing loadBalancing;
	
	
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
	public void launchInstances(int nInstances) throws AmazonServiceException {
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
	private RenderFarmInstance launchInstance() throws AmazonServiceException {
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
        return instance;
	}
	
	/**
	 * Terminate instances that we no longer need
	 * @param instancesIds List with all instances we want terminate
	 */
	public void terminateInstances(List<RenderFarmInstance> instancesToTryTerminate) {
		List<String> instancesMarkedToTermiante = new ArrayList<String>();
		for(RenderFarmInstance instance : instancesToTryTerminate) {
			//if true instance now can't receive more requests
			if(instance.readyToBeTerminated()) {
				System.out.println("[Terminate instances]Going to be terminated: " + instance.getIp());
				instancesMarkedToTermiante.add(instance.getId());
			}
		}
		if(!instancesMarkedToTermiante.isEmpty()) {
			TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
			termInstanceReq.withInstanceIds(instancesMarkedToTermiante);
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
			for(RenderFarmInstance instance : instancesToTryTerminate) {
				if(instance.readyToBeTerminated()) {
					instance.abortAllRequest();
				}
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
					break;
				}
			}
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
   	 	return (state.getCode() == RenderFarmInstance.RUNNING) && (instance.getIp() != null);
	}
	
	
	public List<RenderFarmInstance> getCurrentInstancesUnsync() {
		List<RenderFarmInstance> instanceList = new ArrayList<RenderFarmInstance>();
		synchronized (currentInstances) {
			for(RenderFarmInstance instance : currentInstances) {
				if(instance.getIp() != null) {
					instanceList.add(instance);
				}
			}
		}
		return instanceList;
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
	public RenderFarmInstance getHandlerInstanceIP(Request request) throws RedirectFailedException {
		return loadBalancing.getFitestMachine(this, request);
	}

	/**
	 * Create a render farm instance and wait for it to be up
	 * @return RenderFarmInstance
	 */
	public RenderFarmInstance createReadyInstance() {
		final int polling_interval_running = 10 * 1000;
		final int polling_interval_ping = 30 * 1000;
		int tries = 0;
		try{
			RenderFarmInstance instance = launchInstance();
			while(!isInstanceRunning(instance)){
				if(tries == 10) {
					return null;
				}
				tries++;
				Thread.sleep(polling_interval_running);
			}
			tries = 0;
			RenderFarmInstanceHealthCheck rfihc = new RenderFarmInstanceHealthCheck(instance.getIp());
			while(!rfihc.isUp()){
				if(tries == 6) {
					return null;
				}
				tries++;
				Thread.sleep(polling_interval_ping);
			}
			return instance;
		}
		catch(Exception e){
			System.out.println("[createReadyInstance]DANGER DANGER!!!");
		}
		return null;
	}
	
	@Override
	public String toString() {
		String res = "============== ALL INSTANCES ================" + System.lineSeparator();
		res += "Number of Instance Running: " + currentInstances.size() + System.lineSeparator();
		for(RenderFarmInstance instance: currentInstances) {
			res += instance + System.lineSeparator();
		}
		res += "=========================================";
		return res;
	}
	
}

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

import renderfarm.loadbalancer.loadbalancing.LoadBalancing;

import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;

/**
 * Class to manage the operations of our render farm instances
 * @author Andre
 *
 */
public class RenderFarmInstanceManager {

	private static final Regions AVAILABILITY_ZONE = Regions.US_WEST_2;
	private static final String SECURITY_GROUP = "CNV-ssh+http";
	private static final String RENDER_IMAGE_ID = "ami-db73ecbb";
	private static final String RENDER_INSTANCE_TYPE = "t2.micro";
	private static final String RENDER_KEY_PAIR_NAME = "CNV-lab-AWS";
	
	//AmazonEC2 API Object
	private AmazonEC2 ec2;	//Thread Safe
	
	//All the instances that we think that are currently running
	private List<RenderFarmInstance> currentInstances;	//Thread Safe
	
	//Object that implements the load balancing logic
	private LoadBalancing loadBalancing;	//Thread Safe
	
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
		currentInstances = Collections.synchronizedList(new ArrayList());
		loadBalancing = loadBalacing;
	}
	
	private AWSCredentials obtainCredentials(boolean directCredentials,String accessId,String accessKey) {
		if(directCredentials) {
			return new BasicAWSCredentials(accessId,accessKey);
		} else {
			return new ProfileCredentialsProvider().getCredentials();
		}
	}
	
	/**
	 * Launch render farm instances instances in AWS
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
	 * Terminate instances
	 * @param instancesIds Array with all instances id we want terminate
	 */
	public void terminateInstances(String[] instanceIds) {
		TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
		termInstanceReq.withInstanceIds(instanceIds);
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
	 * Remove the instance from our internal structure given the ID
	 * @param instanceId Instance Id
	 */
	public void removeRenderFarmInstance(String instanceId) {
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
	 * Remove a request from a render farm instance
	 * @param instanceIP Instance IP
	 * @param request Request to be deleted
	 */
	public void removeRequestFromInstance(String instanceIP,Request request) {
		synchronized(currentInstances) {
			for(RenderFarmInstance instance : currentInstances) {
				if(instance.getIp().equals(instanceIP)) {
					instance.removeRequest(request);
					return;
				}
			}
		}
	}
	
	/**
	 * Get all current instances
	 * @return
	 */
	public List<RenderFarmInstance> getCurrentInstances(){
		return currentInstances;
	}
	
	public AmazonEC2 getAmazonEC2() {
		return ec2;
	}
	
	/**
	 * Get the IP of the "best" machine to handle the request (Load Balancing logic)
	 * @param request Request that arrived to the load balancer handler
	 * @return IP of the machine which will handle the request
	 */
	public String getHandlerInstanceIP(Request request){
		return loadBalancing.getFitestMachineIp(this, request);
	}
	
	@Override
	public String toString() {
		String res = "============== ALL INSTANCES ================" + System.lineSeparator();
		synchronized (currentInstances) {
			for(RenderFarmInstance instance: currentInstances) {
				res += instance + System.lineSeparator();
			}
		}
		res += "=========================================";
		return res;
	}
	
}

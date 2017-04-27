package loadbalancer;

import java.util.HashMap;
import java.util.Map;
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
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.InstanceState;

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
	
	private AmazonEC2 ec2;	//Thread Safe
	
	private List<RenderFarmInstance> currentInstances;
	
	public RenderFarmInstanceManager(boolean directCredentials,String accessId,String accessKey) {
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
	}
	
	private AWSCredentials obtainCredentials(boolean directCredentials,String accessId,String accessKey) {
		if(directCredentials) {
			return new BasicAWSCredentials(accessId,accessKey);
		} else {
			return new ProfileCredentialsProvider().getCredentials();
		}
	}
	
	public void launchInstance(int nInstances) throws AmazonServiceException{
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(RENDER_IMAGE_ID)
                           .withInstanceType(RENDER_INSTANCE_TYPE)
                           .withMinCount(nInstances)
                           .withMaxCount(nInstances)
                           .withKeyName(RENDER_KEY_PAIR_NAME)
                           .withSecurityGroups(SECURITY_GROUP);
        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        List<Instance> instances=runInstancesResult.getReservation().getInstances();
        for(Instance i: instances){
        	RenderFarmInstance instance= new RenderFarmInstance(i.getInstanceId());
        	currentInstances.add(instance);
        }

	}
	
	public void terminateInstance(String instanceId) {
		TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
		termInstanceReq.withInstanceIds(instanceId);
		ec2.terminateInstances(termInstanceReq);
	}

	public List<RenderFarmInstance>  getCurrentInstances(){
		return currentInstances;
	}
	public String getFirstInstanceId(){
		return currentInstances.get(0).getId();
	}
	public String getInstanceIp(String instanceId){
		/*DescribeInstanceAttributeRequest attributeRequest= new DescribeInstanceAttributeRequest(instanceId,"ip");
		DescribeInstanceAttributeResult result =ec2.describeInstanceAttribute(attributeRequest);
		return result.toString();*/
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.withInstanceIds(instanceId);
        DescribeInstancesResult res= ec2.describeInstances(describeInstancesRequest);
        return res.getReservations().get(0).getInstances().get(0).getPublicIpAddress();

	}
	public String getAvailableInstance(){
		for(RenderFarmInstance instance : currentInstances){
			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
       	 	describeInstancesRequest.withInstanceIds(instance.getId());
       	 	DescribeInstancesResult res= ec2.describeInstances(describeInstancesRequest);
       	 	InstanceState state = res.getReservations().get(0).getInstances().get(0).getState();
       	 	if(state.getCode()==16){
       	 		if(instance.getIp()==null){
       	 			instance.setIp(res.getReservations().get(0).getInstances().get(0).getPublicIpAddress());
       	 		}
       	 		return instance.getIp();
       	 	} 
		}
		return null;
	}
	
}

package loadbalancer;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

/**
 * Class to manage the operations of our render farm instances
 * @author André
 *
 */
public class RenderFarmInstanceManager {

	private static final Regions AVAILABILITY_ZONE = Regions.US_WEST_2;
	private static final String SECURITY_GROUP = "CNV-ssh+http";
	private static final String RENDER_IMAGE_ID = "ami-db73ecbb";
	private static final String RENDER_INSTANCE_TYPE = "t2.micro";
	private static final String RENDER_KEY_PAIR_NAME = "CNV-lab-AWS";
	
	private AmazonEC2 ec2;	//Thread Safe
	
	private Map<String,RenderFarmInstance> currentInstances;
	
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
		currentInstances = new HashMap<String,RenderFarmInstance>();
	}
	
	private AWSCredentials obtainCredentials(boolean directCredentials,String accessId,String accessKey) {
		if(directCredentials) {
			return new BasicAWSCredentials(accessId,accessKey);
		} else {
			return new ProfileCredentialsProvider().getCredentials();
		}
	}
	
	public void launchInstance() {
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(RENDER_IMAGE_ID)
                           .withInstanceType(RENDER_INSTANCE_TYPE)
                           .withMinCount(1)
                           .withMaxCount(1)
                           .withKeyName(RENDER_KEY_PAIR_NAME)
                           .withSecurityGroups(SECURITY_GROUP);
        ec2.runInstances(runInstancesRequest);    
	}
	
	public void terminateInstance(String instanceId) {
		TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
		termInstanceReq.withInstanceIds(instanceId);
		ec2.terminateInstances(termInstanceReq);
	}
	
}

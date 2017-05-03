package renderfarm.autoscaler;

import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;

/**
 * Thread that will wake up to check the render farm instances load and 
 * decide to shrink our enlarge the quantity of instances.
 * @author Andre
 *
 */
public class AutoScaler extends Thread {

	private static final int INITIAL_NUMBER_OF_INSTANCES = 2;
	
	private static final int WAKE_UP_TIME_INTERVAL = 20000;
	
	private RenderFarmInstanceManager instanceManager;
	
	public AutoScaler(RenderFarmInstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}
	
	@Override
	public void run() {
		try{
			instanceManager.launchInstance(INITIAL_NUMBER_OF_INSTANCES);
		} catch (AmazonServiceException ase) {
            System.out.println("[AUTOSCALER]Caught Exception: " + ase.getMessage());
            System.out.println("[AUTOSCALER]Reponse Status Code: " + ase.getStatusCode());
            System.out.println("[AUTOSCALER]Error Code: " + ase.getErrorCode());
            System.out.println("[AUTOSCALER]Request ID: " + ase.getRequestId());
		}
		System.out.println("[AUTOSCALER]Auto scaler launched. " + INITIAL_NUMBER_OF_INSTANCES + 
				" instances launched");
		while(true) {
			System.out.println("[AUTOSCALER]Auto scaler algorithm started...");
			//Remove the dead machines from our structures!!
			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
       	 	describeInstancesRequest.getMaxResults();
       	 	DescribeInstancesResult res = instanceManager.getAmazonEC2().describeInstances(describeInstancesRequest);
       	 	List<Instance> instances = res.getReservations().get(0).getInstances();
       	 	for(Instance instance : instances) {
       	 		if(instance.getState().getCode() == RenderFarmInstance.SHUTTING_DOWN ||
       	 			instance.getState().getCode() == RenderFarmInstance.STOPPED ||
       	 			instance.getState().getCode() == RenderFarmInstance.TERMINATED ) {
       	 			instanceManager.removeRenderFarmInstance(instance.getInstanceId());
       	 		}
       	 	}
       	 	//TODO Implement the auto scaler algorithm here
			System.out.println("[AUTOSCALER]Auto scaler algorithm ended.");
			try {
				Thread.sleep(WAKE_UP_TIME_INTERVAL);
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	
}

package renderfarm.loadbalancer.loadbalancing;

import java.util.List;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.InstanceState;

import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;

/**
 * Class that represents the unique style of Filipe for load balancing.
 * Good if we want test something because our main load balancing is broken.
 * @author Andre
 *
 */
public class FilipeStyleLoadBalancing extends LoadBalancing {

	@Override
	public String getFitestMachineIp(RenderFarmInstanceManager im, Request req) {
		List<RenderFarmInstance> currentInstances = im.getCurrentInstances();
		synchronized(currentInstances) {
			for(RenderFarmInstance instance : currentInstances){
				DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
	       	 	describeInstancesRequest.withInstanceIds(instance.getId());
	       	 	DescribeInstancesResult res = im.getAmazonEC2().describeInstances(describeInstancesRequest);
	       	 	InstanceState state = res.getReservations().get(0).getInstances().get(0).getState();
	       	 	if(state.getCode() == RUNNING){
	       	 		if(instance.getIp() == null){
	       	 			instance.setIp(res.getReservations().get(0).getInstances().get(0).getPublicIpAddress());
	       	 		}
	       	 		return instance.getIp();
	       	 	} 
			}
			return null;
		}
	}

}
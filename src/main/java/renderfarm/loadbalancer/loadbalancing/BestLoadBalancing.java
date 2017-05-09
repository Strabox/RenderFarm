package renderfarm.loadbalancer.loadbalancing;

import dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;

/**
 * Class that implements our best load balancing algorithm
 * @author Andre
 *
 */
public final class BestLoadBalancing extends LoadBalancing {

	private static final MAXIMUM_LOAD=8;

	public BestLoadBalancing(AmazonDynamoDB dynamoDB) {
		super(dynamoDB);
	}
	
	@Override
	public RenderFarmInstance getFitestMachineAlgorithm(RenderFarmInstanceManager im, Request req) {
		List<RenderFarmInstance> currentInstances = im.getCurrentInstances();
		RenderFarmInstance previous_instance=null;
		synchronized(currentInstances) {
			if(currentInstances.empty() || currentInstances.get(0).getLoadLevel()+req.getWeight()>MAXIMUM_LOAD){
				//CRIO INSTANCIA
				//return da nova
				return null;
			}
			else{
				for(RenderFarmInstance instance : currentInstances){
					if(instance.getLoadLevel()+req.getWeight()>MAXIMUM_LOAD){
						return previous_instance;
					}
					previous_instance=instance;	
				}
				return instance;
			}		
		}










		synchronized(currentInstances) {
			for(RenderFarmInstance instance : currentInstances){
				DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
	       	 	describeInstancesRequest.withInstanceIds(instance.getId());
	       	 	DescribeInstancesResult res = im.getAmazonEC2().describeInstances(describeInstancesRequest);
	       	 	InstanceState state = res.getReservations().get(0).getInstances().get(0).getState();
	       	 	if(state.getCode() == RenderFarmInstance.RUNNING){
	       	 		if(instance.getIp() == null){
	       	 			instance.setIp(res.getReservations().get(0).getInstances().get(0).getPublicIpAddress());
	       	 		}
	       	 		return instance;
	       	 	} 
			}
			throw new NoInstancesToHandleRequest();
		}
	}


}

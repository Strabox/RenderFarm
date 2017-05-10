package renderfarm.loadbalancer.loadbalancing;

import java.util.List;

import renderfarm.dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequestException;

/**
 * Class that represents the unique legendary style of Filipe for load balancing.
 * Good if we want test something because our main load balancing is broken.
 * @author Andre/Filipe :)
 *
 */
public class FilipeStyleLoadBalancing extends LoadBalancing {

	public FilipeStyleLoadBalancing(AmazonDynamoDB dynamoDB) {
		super(dynamoDB);
	}
	
	@Override
	public RenderFarmInstance getFitestMachineAlgorithm(RenderFarmInstanceManager im, Request req)
			throws NoInstancesToHandleRequestException {
		List<RenderFarmInstance> currentInstances = im.getCurrentInstances();
		synchronized(currentInstances) {
			for(RenderFarmInstance instance : currentInstances){
	       	 	if(im.isInstanceRunning(instance)){
	       	 		return instance;
	       	 	} 
			}
			throw new NoInstancesToHandleRequestException();
		}
	}

}

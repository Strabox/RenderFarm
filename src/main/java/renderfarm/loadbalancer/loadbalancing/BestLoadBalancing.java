package renderfarm.loadbalancer.loadbalancing;

import java.util.Collections;
import java.util.List;

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

	private static final int MAXIMUM_LOAD = 8;

	public BestLoadBalancing(AmazonDynamoDB dynamoDB) {
		super(dynamoDB);
	}
	
	@Override
	public RenderFarmInstance getFitestMachineAlgorithm(RenderFarmInstanceManager im, Request req) {
		List<RenderFarmInstance> currentInstances = im.getCurrentInstances();
		RenderFarmInstance previous_instance=null;
		synchronized(currentInstances) {
			Collections.sort(currentInstances);	//Sort the list by load level in ASCENDING order
			if(currentInstances.isEmpty() || currentInstances.get(0).getLoadLevel()+req.getWeight() > MAXIMUM_LOAD){
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
				return null;	//TODO
			}		
		}
	}


}

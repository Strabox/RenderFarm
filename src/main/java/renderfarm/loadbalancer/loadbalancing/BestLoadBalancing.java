package renderfarm.loadbalancer.loadbalancing;

import java.util.Collections;
import java.util.List;

import renderfarm.dynamo.AmazonDynamoDB;
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
		try{
			
			List<RenderFarmInstance> currentInstances = im.getCurrentInstances();
			RenderFarmInstance previous_instance=null;
			synchronized(currentInstances) {
				Collections.sort(currentInstances);	//Sort the list by load level in ASCENDING order
				if(currentInstances.isEmpty() || currentInstances.get(0).getLoadLevel()+req.getWeight() > MAXIMUM_LOAD){
					return im.createReadyInstance();
				}
				else{
					for(RenderFarmInstance instance : currentInstances){
						if(instance.getLoadLevel()+req.getWeight()>MAXIMUM_LOAD){
							while(!im.isInstanceRunning(previous_instance)){
								Thread.sleep(3000);
							}
							while(!im.isInstanceWorking(previous_instance)){
								Thread.sleep(3000);
							}
							return previous_instance;
						}
						previous_instance=instance;	
					}
					while(!im.isInstanceRunning(previous_instance)){
						Thread.sleep(3000);
					}
					while(!im.isInstanceWorking(previous_instance)){
						Thread.sleep(3000);
					}
					return previous_instance;	//TODO
				}		
			}
		}
		catch(Exception e){
			System.out.println("[getFitestMachineAlgorithm] Problem finding Instance");
			return null;
		}
	}


}

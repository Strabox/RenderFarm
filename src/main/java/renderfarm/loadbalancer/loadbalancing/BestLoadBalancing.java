package renderfarm.loadbalancer.loadbalancing;

import java.util.Collections;
import java.util.List;

import renderfarm.dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequestException;
import renderfarm.util.RenderFarmInstanceHealthCheck;

/**
 * Class that implements our best load balancing algorithm
 * @author Andre
 *
 */
public final class BestLoadBalancing extends LoadBalancing {

	/**
	 * Maximum load we want in an instance
	 */
	private static final int MAXIMUM_LOAD = 8;

	public BestLoadBalancing(AmazonDynamoDB dynamoDB) {
		super(dynamoDB);
	}
	
	@Override
	public RenderFarmInstance getFitestMachineAlgorithm(RenderFarmInstanceManager im, Request req) throws NoInstancesToHandleRequestException {
		try{
			List<RenderFarmInstance> currentInstances = im.getCurrentRunningInstances();
			RenderFarmInstance previous_instance = null;
			synchronized(currentInstances) {
				Collections.sort(currentInstances);	//Sort the list by load level in ASCENDING order
				if(currentInstances.isEmpty() || currentInstances.get(0).getLoadLevel()+req.getWeight() > MAXIMUM_LOAD){
					return im.createReadyInstance();
				}
				else {
					for(RenderFarmInstance instance : currentInstances) {
						if(instance.getLoadLevel()+req.getWeight() > MAXIMUM_LOAD) {
							while(!im.isInstanceRunning(previous_instance)){
								System.out.println("[BestLoadBalancing]1 " + previous_instance.getIp());
								Thread.sleep(3000);
							}
							while(!new RenderFarmInstanceHealthCheck(previous_instance.getIp()).isUp()) {
								System.out.println("[BestLoadBalancing]2 " + previous_instance.getIp());
								Thread.sleep(3000);
							}
							return previous_instance;
						}
						previous_instance = instance;	
					}
					while(!im.isInstanceRunning(previous_instance)) {
						System.out.println("[BestLoadBalancing]3 " + previous_instance.getIp());
						Thread.sleep(3000);
					}
					while(!new RenderFarmInstanceHealthCheck(previous_instance.getIp()).isUp()) {
						System.out.println("[BestLoadBalancing]4 " + previous_instance.getIp());
						Thread.sleep(3000);
					}
					return previous_instance;	//TODO
				}		
			}
		}
		catch(Exception e) {
			throw new NoInstancesToHandleRequestException();
		}
	}
}

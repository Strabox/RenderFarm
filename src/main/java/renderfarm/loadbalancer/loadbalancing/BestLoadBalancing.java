package renderfarm.loadbalancer.loadbalancing;

import java.util.Collections;
import java.util.List;

import renderfarm.dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequestException;

/**
 * Class that implements our best load balancing algorithm
 * @author Andre
 *
 */
public final class BestLoadBalancing extends LoadBalancing {

	/**
	 * Poll time interval to remote requests to know about instance state
	 */
	private static final int POLL_TIME_INTERVAL = 8 * 1000;
	
	/**
	 * Maximum load we want in an instance
	 */
	private static final int MAXIMUM_LOAD = 8;

	public BestLoadBalancing(AmazonDynamoDB dynamoDB) {
		super(dynamoDB);
	}
	
	@Override
	public RenderFarmInstance getFitestMachineAlgorithm(RenderFarmInstanceManager im, Request req)
			throws NoInstancesToHandleRequestException {
		System.out.println("[Load Balancing]Load balancer algorithm started!");
		RenderFarmInstance previous_instance = null;
		try{
			List<RenderFarmInstance> currentInstances = im.getCurrentRunningInstances();
			System.out.println("[Load Balancing]Waiting for lock!");
			synchronized(currentInstances) {
				Collections.sort(currentInstances);				//Sort the list by load level in ASCENDING way
				previous_instance = currentInstances.get(0);
				if(currentInstances.isEmpty() || ((currentInstances.get(0).getLoadLevel() + req.getWeight() > MAXIMUM_LOAD) 
						&& (currentInstances.get(0).getLoadLevel() > 2))) {
					previous_instance = null;		
				}
				else {
					for(RenderFarmInstance instance : currentInstances) {
						if(instance.getLoadLevel() + req.getWeight() > MAXIMUM_LOAD) {
							break;
						}
						previous_instance = instance;	
					}
					if(previous_instance.getIp() == null){
						while(!im.isInstanceRunning(previous_instance)) {
							System.out.println("[BestLoadBalancing]Waiting for UP at aws " + previous_instance.getIp());
							Thread.sleep(POLL_TIME_INTERVAL);
						}
					}
					System.out.println("[Load Balancing]Load balancer algorithm ended!");
					return previous_instance;
				}
			}
			System.out.println("[Load Balancing]Load balancer algorithm ended!");
			return im.createReadyInstance();
		}
		catch(Exception e) {
			throw new NoInstancesToHandleRequestException();
		}
	}
}

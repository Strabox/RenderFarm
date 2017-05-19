package renderfarm.loadbalancer.loadbalancing;

import java.util.Collections;
import java.util.List;

import renderfarm.dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.InstanceCantReceiveMoreRequestsException;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequestException;
import renderfarm.loadbalancer.exceptions.RedirectFailedException;

/**
 * Class that implements our best load balancing algorithm
 * @author Andre
 *
 */
public final class BestLoadBalancing extends LoadBalancing {

	/**
	 * Poll time interval to remote requests to know about instance state
	 */
	private static final int POLL_TIME_INTERVAL = 10 * 1000;
	
	/**
	 * Maximum load we want in an instance
	 */
	public static int MAXIMUM_LOAD = 5;

	public static int MAXIMUM_TRIES_FOR_UP = 3;
	
	public BestLoadBalancing(AmazonDynamoDB dynamoDB) {
		super(dynamoDB);
	}
	
	@Override
	public RenderFarmInstance getFitestMachineAlgorithm(RenderFarmInstanceManager im, Request req)
			throws RedirectFailedException {
		System.out.println("[Load Balancing]Load balancer algorithm started!");
		List<RenderFarmInstance> currentInstances = im.getCurrentRunningInstances();
		RenderFarmInstance previous_instance = null;
		try{
			System.out.println("[Load Balancing]Waiting for lock!");
			synchronized(currentInstances) {
				Collections.sort(currentInstances);				//Sort the list by load level in ASCENDING way
				if(currentInstances.isEmpty() || ((currentInstances.get(0).getLoadLevel() + req.getWeight() > MAXIMUM_LOAD) 
						&& (currentInstances.get(0).getLoadLevel() > 2))) {
					throw new NoInstancesToHandleRequestException();
				}
				else {
					previous_instance = currentInstances.get(0);
					for(RenderFarmInstance instance : currentInstances) {
						if(instance.getLoadLevel() + req.getWeight() > MAXIMUM_LOAD) {
							break;
						}
						previous_instance = instance;	
					}
					try {
						previous_instance.addRequest(req);
					} catch (InstanceCantReceiveMoreRequestsException e) {
						throw new RedirectFailedException();
					}
				}
			}
			if(previous_instance.getIp() == null){
				int tries = 0;
				while(!im.isInstanceRunning(previous_instance)) {
					if(tries == MAXIMUM_TRIES_FOR_UP) {
						throw new NoInstancesToHandleRequestException();
					}
					tries++;
					try {
						Thread.sleep(POLL_TIME_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("[Load Balancing]Load balancer algorithm ended!");
			return previous_instance;
		} catch(NoInstancesToHandleRequestException e) {
			synchronized (currentInstances) {
				//Load balancer couldn't found an instance so we create a new one.
				RenderFarmInstance newInstance = im.createReadyInstance();
				if(newInstance == null) {
					throw new RedirectFailedException();
				}
				try {
					newInstance.addRequest(req);
				} catch (InstanceCantReceiveMoreRequestsException e1) {
					throw new RedirectFailedException();
				}
				//Add the new instance to the running instances
				im.getCurrentRunningInstances().add(newInstance);
				System.out.println("[Load Balancing]Load balancer algorithm ended!");
				return newInstance;
			}
		}
	}
}

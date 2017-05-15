package renderfarm.loadbalancer.loadbalancing;

import java.util.Collections;
import java.util.List;

import renderfarm.dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequestException;
import renderfarm.util.RenderFarmInstanceHealthCheck;
import java.io.Console;

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
		RenderFarmInstance previous_instance =null;
		try{
			List<RenderFarmInstance> currentInstances = im.getCurrentRunningInstances();
			synchronized(currentInstances) {
				Collections.sort(currentInstances);
				 previous_instance = currentInstances.get(0);	//Sort the list by load level in ASCENDING order
				if(currentInstances.isEmpty() || ((currentInstances.get(0).getLoadLevel() + req.getWeight() > MAXIMUM_LOAD)&&(currentInstances.get(0).getLoadLevel()<=2))) {
					previous_instance=null;
				}
				else {
					for(RenderFarmInstance instance : currentInstances) {
						if(instance.getLoadLevel() + req.getWeight() > MAXIMUM_LOAD) {
							break ;

						}
						previous_instance = instance;	
					}
					if(previous_instance.getIp()==null){
						while(!im.isInstanceRunning(previous_instance)) {
							System.out.println("[BestLoadBalancing]3 " + previous_instance.getIp());
							Thread.sleep(3000);
						}
					}

					RenderFarmInstanceHealthCheck check= new RenderFarmInstanceHealthCheck(previous_instance.getIp());
					while(!check.isUp()) {
						System.out.println("[BestLoadBalancing]4 " + previous_instance.getIp());
						Thread.sleep(1000);
					}
					return previous_instance;	//TODO

				}
			}
			return im.createReadyInstance();
				
					
		}
		catch(Exception e) {
			throw new NoInstancesToHandleRequestException();
		}
	}
}

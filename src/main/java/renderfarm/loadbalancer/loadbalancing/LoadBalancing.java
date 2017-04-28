package renderfarm.loadbalancer.loadbalancing;

import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;

/**
 * Class that implements the interface to the load balancing algorithm.
 * (Strategy Desing Pattern)
 * @author Andre
 *
 */
public abstract class LoadBalancing {

	protected static final int RUNNING = 16;
	
	public abstract String getFitestMachineIp(RenderFarmInstanceManager im,Request req);
	
}

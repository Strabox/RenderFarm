package renderfarm.loadbalancer.loadbalancing;

import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;

/**
 * Class that implements our best load balancing algorithm
 * @author Andre
 *
 */
public final class BestLoadBalancing extends LoadBalancing {

	@Override
	public RenderFarmInstance getFitestMachine(RenderFarmInstanceManager im, Request req) {
		// TODO Our final balancing algorithm runs here
		throw new RuntimeException("Algorithm not implemented");
	}


}

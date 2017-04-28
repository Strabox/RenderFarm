package renderfarm.loadbalancer.loadbalancing;

import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Class that implements our best load balancing algorithm
 * @author Andre
 *
 */
public final class BestLoadBalancing extends LoadBalancing {

	@Override
	public String getFitestMachineIp(RenderFarmInstanceManager im, Request req) {
		// TODO The real magic is here
		throw new NotImplementedException();
	}

}

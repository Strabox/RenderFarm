package renderfarm.loadbalancer.loadbalancing;

import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequest;

/**
 * Class that implements the interface to the load balancing algorithm.
 * (Strategy + Template Method Design Pattern)
 * @author Andre
 *
 */
public abstract class LoadBalancing {

	/**
	 * Override this method in subclasses to implement the instance selection algorithm.
	 * @param im Manager of render farms instances
	 * @param req Actual request
	 * @return Render farm instance selected to handle the request
	 */
	protected abstract RenderFarmInstance getFitestMachine(RenderFarmInstanceManager im,Request req) throws NoInstancesToHandleRequest;
	
	/**
	 * Method to get the machine IP to handle the request
	 * @param im Manager of render farms instances
	 * @param req Actual request
	 * @return Render farm instance IP selected to handle the request
	 */
	public String getFitestMachineIp(RenderFarmInstanceManager im,Request req) {
		RenderFarmInstance chosenInstance;
		try {
			chosenInstance = getFitestMachine(im, req);
			chosenInstance.addRequest(req);
			return chosenInstance.getIp();
		} catch (NoInstancesToHandleRequest e) {
			// IMPORTANT: THIS BLOCK SHOULD NEVER BE EXECUTED BECAUSE IT MEANS WE ARE DENYING 
			// THE SERVICE TO THE CLIENT (WE SHOULD ALLOCATE A NEW MACHINE AND WAIT OR OTHER STUFF...)
			return null;
		}
	}
	
}

package renderfarm.autoscaler;

import renderfarm.loadbalancer.RenderFarmInstanceManager;

/**
 * Auto scaling strategy pattern algorithm
 * @author Andre
 *
 */
public abstract class AutoScaling {

	protected RenderFarmInstanceManager instanceManager;
	
	public AutoScaling(RenderFarmInstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}
	
	/**
	 * Method that implement the auto scaling algorithm. 
	 */
	public abstract void autoScale();
	
}

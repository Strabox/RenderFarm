package renderfarm.autoscaler;

import java.util.List;

import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;

/**
 * Our best auto scaling algorithm.
 * @author Andre
 *
 */
public class BestAutoScaling extends AutoScaling {

	public BestAutoScaling(RenderFarmInstanceManager im) {
		super(im);
	}

	@Override
	public void autoScale() {
   	 	List<RenderFarmInstance> currentRenderFarmInstances = instanceManager.getCurrentInstances();
   	 	synchronized (currentRenderFarmInstances) {
			for(@SuppressWarnings("unused") RenderFarmInstance instance : currentRenderFarmInstances) {
				//TODO Implement the auto scaling/descaling algorithm here
			}
		}
	}

}

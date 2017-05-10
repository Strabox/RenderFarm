package renderfarm.autoscaler;

import java.util.ArrayList;
import java.util.Collections;
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
   	 	List<String> terminateInstances = new ArrayList<String>();
   	 	synchronized (currentRenderFarmInstances) {
   	 		Collections.sort(currentRenderFarmInstances);		//Sort the instances by ASCENDING load level
			for(RenderFarmInstance instance : currentRenderFarmInstances) {
				if((currentRenderFarmInstances.size() - terminateInstances.size()) > 2  &&
						instance.isEmpty()) {
					terminateInstances.add(instance.getId());
				}
			}
			if(!terminateInstances.isEmpty()) {
				instanceManager.terminateInstances(terminateInstances.toArray(new String[terminateInstances.size()]));
			}
		}
	}

}

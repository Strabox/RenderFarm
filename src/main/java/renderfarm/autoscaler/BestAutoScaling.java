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

	/**
	 * Minimum number of render farm instance we want always up
	 */
	private final static int MINIMUM_INSTANCE_ALWAYS_UP = 2;
	
	public BestAutoScaling(RenderFarmInstanceManager im) {
		super(im);
	}

	@Override
	public void autoScale() {
   	 	List<RenderFarmInstance> currentRenderFarmInstances = instanceManager.getCurrentRunningInstances();
   	 	List<RenderFarmInstance> terminateInstances = new ArrayList<RenderFarmInstance>();
   	 	synchronized (currentRenderFarmInstances) {
   	 		Collections.sort(currentRenderFarmInstances);		//Sort the instances by ASCENDING load level
			for(RenderFarmInstance instance : currentRenderFarmInstances) {
				if((currentRenderFarmInstances.size() - terminateInstances.size()) > MINIMUM_INSTANCE_ALWAYS_UP) {
					if(instance.readyToSignToTerminate()) {
						terminateInstances.add(instance);
					}
					else {	//The list is sorted by load level and emptiness so first are the empty ones.
						break;
					}
				}
				else {		//Minimum of instances already reached we can't terminate more
					break;
				}
			}
		}
		if(!terminateInstances.isEmpty()) {
			instanceManager.terminateInstances(terminateInstances);
		}
	}

}

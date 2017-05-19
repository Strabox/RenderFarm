package renderfarm.loadbalancer;

import java.util.ArrayList;
import java.util.List;

import renderfarm.util.RenderFarmInstanceHealthCheck;

/**
 * Detect instances that have crashed and send signal to handler threads
 * that are connected to them
 * @author Andre
 *
 */
public class RenderFarmInstanceFaultDetector extends Thread {

	/**
	 * Time interval of pooling the Health Check.
	 */
	public static int INTERVAL_OF_FAULT_DETECTION_POLLING;
	
	/**
	 * Delay to start checking for faults
	 */
	public static int DELAY_TO_START;
	
	/**
	 * Render farm instance manager
	 */
	private RenderFarmInstanceManager instanceManager;
	
	public RenderFarmInstanceFaultDetector(RenderFarmInstanceManager im) {
		this.instanceManager = im;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(DELAY_TO_START);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while(true) {
			try {
				System.out.println("[FAULT DETECTOR]Fault detector started...");
				List<RenderFarmInstance> instances = instanceManager.getCurrentInstancesUnsync();
				List<RenderFarmInstance> instancesInFaultToTermiante = new ArrayList<RenderFarmInstance>();
				for(RenderFarmInstance instance: instances) {
					RenderFarmInstanceHealthCheck rfhc = new RenderFarmInstanceHealthCheck(instance.getIp());
					if(!rfhc.isUp()) {
						//Instance Crashed
						System.out.println("[FAULT DETECTOR]" + instance.getIp() + " crashed");
						instance.forceReadyToBeTerminated();
						instancesInFaultToTermiante.add(instance);
					}
				}
				instanceManager.terminateInstances(instancesInFaultToTermiante);
				System.out.println("[FAULT DETECTOR]Fault detector ended");
				Thread.sleep(INTERVAL_OF_FAULT_DETECTION_POLLING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}

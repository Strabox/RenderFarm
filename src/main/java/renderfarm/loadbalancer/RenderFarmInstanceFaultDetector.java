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
	private final static int INTERVAL_OF_POLLING = 60 * 1000;
	
	/**
	 * Delay to start checking for faults
	 */
	private final static int DELAY_TO_START = 60 * 1000;
	
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
				List<RenderFarmInstance> instances = instanceManager.getCurrentInstanceIp();
				List<RenderFarmInstance> instancesInFaultToTermiante = new ArrayList<RenderFarmInstance>();
				for(RenderFarmInstance instance: instances) {
					RenderFarmInstanceHealthCheck rfhc = new RenderFarmInstanceHealthCheck(instance.getIp());
					if(!rfhc.isUp()) {		//Instance is dead send signal to all threads waiting for it
						System.out.println("[FAULT DETECTOR]" + instance.getIp() + " crashed");
						instance.abortAllRequest();
						instancesInFaultToTermiante.add(instance);
					}
				}
				if(!instancesInFaultToTermiante.isEmpty()) {
					instanceManager.terminateInstances(instancesInFaultToTermiante);
				}
				System.out.println("[FAULT DETECTOR]Fault detector ended");
				Thread.sleep(INTERVAL_OF_POLLING);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
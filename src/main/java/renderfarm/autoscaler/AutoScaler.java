package renderfarm.autoscaler;

import com.amazonaws.AmazonServiceException;
import renderfarm.loadbalancer.RenderFarmInstanceManager;

/**
 * Thread that will wake up to check the render farm instances load and 
 * decide to shrink our enlarge the quantity of instances.
 * @author Andre
 *
 */
public class AutoScaler extends Thread {

	/**
	 * Initial number instance for our Rendering Farm.
	 */
	private static final int INITIAL_NUMBER_OF_INSTANCES = 1;
	
	/**
	 * Interval between the auto scaling of Rendering Farm (Minutes).
	 */
	private static final int AUTO_SCALING_TIME_INTERVAL = 2 * (60 * 1000);
	
	/**
	 * Implements our auto scaler logic.
	 */
	private AutoScaling autoScaling;
	
	/**
	 * Manager of render farm instances
	 */
	private RenderFarmInstanceManager instanceManager;
	
	public AutoScaler(RenderFarmInstanceManager instanceManager) {
		this.instanceManager = instanceManager;
		this.autoScaling = new BestAutoScaling(instanceManager);
	}
	
	@Override
	public void run() {
		try{
			instanceManager.launchInstances(INITIAL_NUMBER_OF_INSTANCES);
			System.out.println("[AUTOSCALER]Auto scaler launched. " + INITIAL_NUMBER_OF_INSTANCES + 
					" instances launched");
		} catch (AmazonServiceException ase) {
            System.out.println("[AUTOSCALER]Caught Exception: " + ase.getMessage());
            System.out.println("[AUTOSCALER]Reponse Status Code: " + ase.getStatusCode());
            System.out.println("[AUTOSCALER]Error Code: " + ase.getErrorCode());
            System.out.println("[AUTOSCALER]Request ID: " + ase.getRequestId());
		}
		while(true) {
			try {
				System.out.println("[AUTOSCALER]Auto scaler algorithm started...");
	       	 	autoScaling.autoScaleAlgorithm();
				System.out.println("[AUTOSCALER]Auto scaler algorithm ended.");
				try {
					Thread.sleep(AUTO_SCALING_TIME_INTERVAL);
				} catch (InterruptedException e) {
					continue;
				}
			} catch (Exception e) {
				System.out.println("[AUTOSCALER]Problems during autoscaling.");
				e.printStackTrace();
				try {
					Thread.sleep(AUTO_SCALING_TIME_INTERVAL);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
		}
	}
	
}

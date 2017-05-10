package renderfarm.loadbalancer;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

import renderfarm.loadbalancer.exceptions.InstanceIsDownException;
import renderfarm.util.RenderFarmInstanceHealthCheck;

public class KeepAliveThread extends Thread {

	/**
	 * Time interval of pooling the Health Check.
	 */
	private final static int INTERVAL_OF_POLLING = 10000;
	
	/**
	 * Handler thread connection.
	 */
	private final HttpURLConnection handlerConnection;
	
	/**
	 * Instance IP pooling
	 */
	private final RenderFarmInstanceHealthCheck instanceHealthCheck;
	
	/**
	 * Used to decide if stop the polling
	 */
	private final AtomicBoolean keepPolling;
	
	public KeepAliveThread(HttpURLConnection conn,String instanceIP) {
		this.handlerConnection = conn;
		this.keepPolling = new AtomicBoolean(true);
		instanceHealthCheck = new RenderFarmInstanceHealthCheck(instanceIP);
	}
	
	/**
	 * Used to other threads stop this thread.
	 */
	public void terminate() {
		keepPolling.set(false);
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				System.out.println("[KEEPALIVE] U there?");
				if(!instanceHealthCheck.isUp()) {
					throw new InstanceIsDownException();
				}
				System.out.println("[KEEPALIVE] I'm here bro");
				Thread.sleep(INTERVAL_OF_POLLING);
				if(!keepPolling.get()) {
					break;
				}

			}
		} catch(Exception e) {
			System.out.println("[KEEPALIVE] Instance died probably");
			handlerConnection.disconnect();		//Kill the handler connection to blow it.
		}
	}
	
}

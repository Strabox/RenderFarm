package renderfarm.util;

import java.net.HttpURLConnection;
import java.net.URL;

import renderfarm.loadbalancer.exceptions.InstanceIsDownException;

/**
 * Class used to check if an instance is working
 * @author Andre
 *
 */
public class RenderFarmInstanceHealthCheck {

	/**
	 * Timeout to establish the connection with the instance.
	 */
	private final static int CONNECTION_TIMEOUT = 10000;
	
	/**
	 * Timeout waiting for the render farm Health Check reply.
	 */
	public static int WAIT_FOR_REPLY_TIMEOUT;
	
	/**
	 * Instance endpoint for health checking
	 */
	private final static String HEALTH_CHECK_ENDPOINT = "/HealthCheck";
	
	/**
	 * Instance IP to check
	 */
	private final String instanceIP;
	
	public RenderFarmInstanceHealthCheck(String instanceIP) {
		this.instanceIP = instanceIP;
	}
	
	public boolean isUp() {
		HttpURLConnection connection = null;
		boolean up = true;
		try {
			URL url = new URL("http",instanceIP,SystemConfiguration.RENDER_INSTANCE_PORT,HEALTH_CHECK_ENDPOINT);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setReadTimeout(WAIT_FOR_REPLY_TIMEOUT);
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new InstanceIsDownException();
			}
			up = true;
		} catch(Exception e) {
			up = false;
		}
		finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
		return up;
	}
	
}

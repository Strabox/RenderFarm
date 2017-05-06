package renderfarm.loadbalancer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import renderfarm.util.SystemConfiguration;

public class KeepAliveThread extends Thread {
	/**
	 * Timeout to establish the connection with the instance.
	 */
	private final static int CONNECTION_TIMEOUT = 10000;
	
	/**
	 * Timeout waiting for the render farm Health Check reply.
	 */
	private final static int WAIT_FOR_REPLY_TIMEOUT = 5000;
	
	/**
	 * Time interval of pooling the Health Check.
	 */
	private final static int INTERVAL_OF_POOLING = 10000;
	
	/**
	 * Handler thread connection.
	 */
	private final HttpURLConnection handlerConnection;
	
	/**
	 * Instance IP pooling
	 */
	private final String instanceIP;
	
	private final AtomicBoolean keepPooling;
	
	public KeepAliveThread(HttpURLConnection conn,String instanceIP) {
		this.handlerConnection = conn;
		this.instanceIP  = instanceIP;
		this.keepPooling = new AtomicBoolean(true);
	}
	
	/**
	 * Used to other threads stop this thread.
	 */
	public void terminate() {
		keepPooling.set(false);
	}
	
	@Override
	public void run() {
		HttpURLConnection connection = null;
		try {
			while(true) {
				URL url = new URL("http",instanceIP,SystemConfiguration.RENDER_INSTANCE_PORT,"/HealthCheck");
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(CONNECTION_TIMEOUT);
				connection.setReadTimeout(WAIT_FOR_REPLY_TIMEOUT);
				System.out.println("[KEEPALIVE] U there?");
				if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new Exception("Wrong response from instance.");
				}
				System.out.println("[KEEPALIVE] I'm here bro");
				Thread.sleep(INTERVAL_OF_POOLING);
				if(!keepPooling.get()) {
					break;
				}
				if(connection != null) {
					connection.disconnect();
				}
			}
		} catch(Exception e) {
			System.out.println("[KEEPALIVE] Instance died probably");
			handlerConnection.disconnect();		//Kill the handler connection to blow it.
		}
		finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
	}
	
}

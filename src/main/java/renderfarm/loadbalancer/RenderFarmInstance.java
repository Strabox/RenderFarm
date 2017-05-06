package renderfarm.loadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import renderfarm.loadbalancer.exceptions.InstanceCantReceiveMoreRequests;

/**
 * Class represent a render farm instance of our system.
 * @author Andre
 *
 */
public class RenderFarmInstance {
	
	public static final int RUNNING = 16; 
	public static final int SHUTTING_DOWN = 32; 
	public static final int TERMINATED = 48; 
	public static final int STOPPING = 64; 
	public static final int STOPPED = 80; 
	
	/**
	 * Instance IP
	 */
	private String ip;
	
	/**
	 * Instance ID
	 */
	private final String id;
	
	/**
	 * All the request running in the instance
	 */
	private final List<Request> requestsInExecution;	//Thread Safe

	/**
	 * true if the instance should stop accepting requests
	 * false otherwise 
	 */
	private final AtomicBoolean stopReceiveRequests;
	
	/**
	 * The estimate for our instance load
	 */
	private int loadLevel;
	
	public RenderFarmInstance(String id) {
		this.ip = null;
		this.id = id;
		this.loadLevel = 0;	//TODO set the initial load level
		this.stopReceiveRequests = new AtomicBoolean(false);
		this.requestsInExecution = Collections.synchronizedList(new ArrayList<Request>());
	}
	
	public synchronized void addRequest(Request req) throws InstanceCantReceiveMoreRequests {
		if(stopReceiveRequests.get()) {
			throw new InstanceCantReceiveMoreRequests();
		}
		requestsInExecution.add(req);
		loadLevel += req.getWeight();
	}
	
	public synchronized void removeRequest(Request req) {
		requestsInExecution.remove(req);
		loadLevel -= req.getWeight();
	}
	
	public void stopReceivingRequests() {
		stopReceiveRequests.set(true);
	}
	
	public boolean stoppedFromReceivingRequests() {
		return stopReceiveRequests.get();
	}
	
	public synchronized int getLoadLevel() {
		return loadLevel;
	}
	
	public synchronized void setIp(String ip){
		this.ip = ip;
	}
	
	public synchronized String getIp() {
		return ip;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		RenderFarmInstance instance = (RenderFarmInstance) obj;
		return instance.getId().equals(instance.getId());
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public synchronized String toString() {
		String res = "$$$$$$$$$$$$$$$$$ INSTANCE $$$$$$$$$$$$$$$$$" + System.lineSeparator();
		res += "ID: " + id.toString() + System.lineSeparator();
		if(ip == null) {
			res += "IP: Not Attributted yet" + System.lineSeparator();
		} else {
			res += "IP: " + ip.toString() + System.lineSeparator();
		}
		//res += "Load Level: " + loadLevel.toString() + System.lineSeparator();
		for(Request req : requestsInExecution) {
			res += req + System.lineSeparator();
		}
		res += System.lineSeparator();
		res += "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$";
		return res;
	}
	
}

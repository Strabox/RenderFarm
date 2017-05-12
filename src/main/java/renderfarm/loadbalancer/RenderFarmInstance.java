package renderfarm.loadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import renderfarm.loadbalancer.exceptions.InstanceCantReceiveMoreRequestsException;

/**
 * Class represent a render farm instance of our system.
 * @author Andre
 *
 */
public class RenderFarmInstance implements Comparable<RenderFarmInstance> {
	
	public static final int RUNNING = 16; 
	public static final int SHUTTING_DOWN = 32; 
	public static final int TERMINATED = 48; 
	public static final int STOPPING = 64; 
	public static final int STOPPED = 80; 
	
	/**
	 * Instance's IP
	 */
	private volatile String ip;
	
	/**
	 * Instance's AWS ID
	 */
	private final String id;
	
	/**
	 * All the request running in the instance at the moment
	 */
	private final List<Request> requestsInExecution;	//Thread Safe

	/**
	 * ->True if it is signed to be terminated but can receive requests
	 * that should set this flag to false
	 * ->False can accept request normally  
	 */
	private final AtomicBoolean signalToBeTerminated;
	
	/**
	 * Going to be terminate the instance
	 * ->If true can't accept new requests or have requests pending
	 * ->Otherwise instance can accept
	 */
	private final AtomicBoolean goingToBeTerminated;
	
	/**
	 * The estimate for our instance load [0,10]
	 */
	private volatile int loadLevel;
	
	public RenderFarmInstance(String id) {
		this.ip = null;
		this.id = id;
		this.loadLevel = 0;
		this.signalToBeTerminated = new AtomicBoolean(false);
		this.goingToBeTerminated = new AtomicBoolean(false);
		this.requestsInExecution = Collections.synchronizedList(new ArrayList<Request>());
	}
	
	public synchronized void addRequest(Request req) throws InstanceCantReceiveMoreRequestsException {
		if(goingToBeTerminated.get()) {
			throw new InstanceCantReceiveMoreRequestsException();
		}
		if(signalToBeTerminated.get()) {
			signalToBeTerminated.set(false);
		}
		requestsInExecution.add(req);
		loadLevel += req.getWeight();
	}
	
	public synchronized void removeRequest(Request req) {
		if(requestsInExecution.remove(req)) {
			loadLevel -= req.getWeight();
		}
	}

	/**
	 * If it was marked to be terminated and didn't receive a request in the meantime
	 * so the instance is marked to be terminated and can't receive more requests.
	 * @return
	 */
	public synchronized boolean readyToBeTerminated() {
		if(signalToBeTerminated.get()) {
			goingToBeTerminated.set(true);
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Sign the instance to see if it is in state that can be terminated (0 requests processing).
	 * @return
	 */
	public synchronized boolean readyToSignToTerminate() {
		if(requestsInExecution.isEmpty()) {
			signalToBeTerminated.set(true);
			return true;
		}
		else {
			return false;
		}
	}
	
	public synchronized boolean isEmpty() {
		return requestsInExecution.isEmpty();
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
		if(this == obj) {
			return true;
		}
		if(obj == null || getClass() != obj.getClass()) {
			return false;
		}
		RenderFarmInstance instance = (RenderFarmInstance) obj;
		return instance.getId().equals(instance.getId());
	}
	
	@Override
	public synchronized int hashCode() {
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
		res += "Load Level: " + loadLevel + System.lineSeparator();
		for(Request req : requestsInExecution) {
			res += req + System.lineSeparator();
		}
		res += System.lineSeparator();
		res += "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$";
		return res;
	}

	@Override
	public synchronized int compareTo(RenderFarmInstance o) {
		if(this.loadLevel < o.getLoadLevel()) {
			return -1;
		}
		else if(this.loadLevel == o.getLoadLevel()) {	
			if(this.isEmpty() && !o.isEmpty()) {
				return -1;
			}
			else if(!this.isEmpty() && !o.isEmpty()) {
				return 1;
			}
			else {
				return 0;
			}
		}
		else {
			return 1;
		}
	}
	
}

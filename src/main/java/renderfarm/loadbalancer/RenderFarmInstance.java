package renderfarm.loadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
	
	private String ip;
	
	private final String id;
	
	private List<Request> requestsInExecution;	//Thread Safe

	/**
	 * The estimate for our instance load
	 */
	private int loadLevel;
	
	public RenderFarmInstance(String id) {
		this.ip = null;
		this.id = id;
		this.loadLevel = 0;	//TODO set the initial load level
		this.requestsInExecution = Collections.synchronizedList(new ArrayList<Request>());
	}
	
	public synchronized void addRequest(Request req) {
		requestsInExecution.add(req);
		loadLevel += req.getWeight();
	}
	
	public synchronized void removeRequest(Request req) {
		requestsInExecution.remove(req);
		loadLevel -= req.getWeight();
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
			res += req;
		}
		res += System.lineSeparator();
		res += "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$";
		return res;
	}
	
}

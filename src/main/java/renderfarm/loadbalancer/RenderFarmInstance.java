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

	private String ip;
	
	private String id;
	
	private List<Request> requestsInExecution;

	public RenderFarmInstance(String id) {
		this.ip = null;
		this.id = id;
		requestsInExecution = Collections.synchronizedList(new ArrayList<Request>());
	}

	public synchronized LoadLevel getLoadLevel() {
		Iterator<Request> i = requestsInExecution.iterator();
		while(i.hasNext()) {	//Look to all instances
			//TODO algorithm to return instance load level based on requests that
			//are executing in the instance
		}
		return null;
	}
	
	public void addRequest(Request req) {
		requestsInExecution.add(req);
	}
	
	public void removeRequest(Request req) {
		requestsInExecution.remove(req);
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getId() {
		return id;
	}
	
}

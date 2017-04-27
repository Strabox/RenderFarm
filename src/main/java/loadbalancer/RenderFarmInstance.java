package loadbalancer;

/**
 * Class represent a render farm instance of our system.
 * @author Andre
 *
 */
public class RenderFarmInstance {

	private String ip;
	private String id;
	private List<Request>requests;

	public RenderFarmInstance(String id) {
		this.ip = null;
		this.id=id;
		requests=Collections.synchronizedList(new ArrayList());
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

package renderfarm.loadbalancer;

import renderfarm.util.NormalizedWindow;

/**
 * Represents a request that arrived to the load balancer.
 * Thread safe.
 * @author Andre
 *
 */
public class Request {
	
	private final String file;
	
	private final NormalizedWindow normalizedWindow;
	
	private final long scenePixelsResolution;
	
	private int weight;
	
	public Request(String file,NormalizedWindow normalizedWindow,long scenePixelsResolution){
		this.file = file;
		this.normalizedWindow = normalizedWindow;
		this.scenePixelsResolution = scenePixelsResolution;
		this.weight = 0;
	}

	public String getFile() {
		return this.file;
	}
	
	public int getWeight() {
		return this.weight;
	}

	public NormalizedWindow getNormalizedWindow() {
		return this.normalizedWindow;
	}

	public long getScenePixelsResolution() {
		return this.scenePixelsResolution;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		Request r = (Request) o;
		return this.getFile().equals(r.getFile()) && 
				this.getNormalizedWindow().equals(r.getNormalizedWindow()) &&
				(this.getScenePixelsResolution() == r.getScenePixelsResolution());
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		String res = "################ REQUEST #################" + System.lineSeparator();
		res += "File: " + file + System.lineSeparator();
		res += "Total Pixels: " + scenePixelsResolution + System.lineSeparator();
		res += "Weight: " + weight + System.lineSeparator();
		res += "Normalized Window: " + normalizedWindow + System.lineSeparator();
		res += "#########################################";
		return res;
	}
	
}

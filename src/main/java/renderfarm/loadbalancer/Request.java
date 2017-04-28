package renderfarm.loadbalancer;

import renderfarm.util.NormalizedWindow;

/**
 * Represents a request that arrived to the load balancer.
 * Total thread safe.
 * @author Andre
 *
 */
public class Request {
	
	private final String file;
	
	private final NormalizedWindow normalizedWindow;
	
	private final long totalPixelsRendered;

	public Request(String file,NormalizedWindow normalizedWindow,long totalPixelsRendered){
		this.file = file;
		this.normalizedWindow = normalizedWindow;
		this.totalPixelsRendered = totalPixelsRendered;
	}

	public String getFile() {
		return file;
	}

	public NormalizedWindow getNormalizedWindow() {
		return normalizedWindow;
	}

	public long getTotalPixelsRendered() {
		return totalPixelsRendered;
	}
	
	@Override
	public boolean equals(Object o) {
		Request r = (Request) o;
		return this.getFile().equals(r.getFile()) && 
				this.getNormalizedWindow().equals(r.getNormalizedWindow()) &&
				(this.getTotalPixelsRendered() == r.getTotalPixelsRendered());
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		String res = "################ REQUEST #################" + System.lineSeparator();
		res += "File: " + file + System.lineSeparator();
		res += "Total Pixels: " + totalPixelsRendered + System.lineSeparator();
		res += "Normalized Window: " + normalizedWindow + System.lineSeparator();
		res += "#########################################";
		return res;
	}
	
}

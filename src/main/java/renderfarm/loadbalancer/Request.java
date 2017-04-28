package renderfarm.loadbalancer;

import renderfarm.util.NormalizedWindow;

/**
 * Represents a request that arrived.
 * @author Andre
 *
 */
public class Request {
	
	private String file;
	
	private NormalizedWindow normalizedWindow;
	
	private long totalPixelsRendered;

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

	public void setFile(String file) {
		this.file = file;
	}

	public void setNormalizedWindow(NormalizedWindow normalizedWindow) {
		this.normalizedWindow = normalizedWindow;
	}

	public void setTotalPixelsRendered(long totalPixelsRendered) {
		this.totalPixelsRendered = totalPixelsRendered;
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

}

package loadbalancer;

import metrics.*;

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

}

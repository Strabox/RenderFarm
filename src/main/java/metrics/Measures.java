package metrics;

/**
 * Measures in terms of coded executed for a given request.
 * @author Andre
 *
 */
public class Measures {
	
	private long basicBlockCount;
	
	private long loadCount;
	
	private long storeCount;
	
	public Measures() {
		this.setBasicBlockCount(0);
		this.setLoadcount(0);
		this.setStorecount(0);
	}

	public long getBasicBlockCount() {
		return basicBlockCount;
	}

	public void setBasicBlockCount(long basicBlockCount) {
		this.basicBlockCount = basicBlockCount;
	}

	public void incrementBasiBlockCount() {
		this.basicBlockCount++;
	}
	
	public void incrementLoadCount() {
		this.loadCount++;
	}
	
	public void incrementStoreCount() {
		this.storeCount++;
	}
	
	
	public void reset(){
		this.setBasicBlockCount(0);
		this.setLoadcount(0);
		this.setStorecount(0);	
	}

	public long getLoadcount() {
		return loadCount;
	}

	public void setLoadcount(long loadcount) {
		this.loadCount = loadcount;
	}

	public long getStorecount() {
		return storeCount;
	}

	public void setStorecount(long storecount) {
		this.storeCount = storecount;
	}
	
	@Override
	public String toString() {
		String res = "";
		res += "Basic Block Count: " + getBasicBlockCount() + System.lineSeparator();
		res += "Store Count: " + getStorecount() + System.lineSeparator();
		res += "Load Count: " + getLoadcount() + System.lineSeparator();
		return res;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}

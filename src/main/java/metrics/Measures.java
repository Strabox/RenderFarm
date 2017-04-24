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
	
	private long fieldLoadCount;
	
	private long fieldStoreCount;
	
	public Measures() {
		this.setBasicBlockCount(0);
		this.setLoadcount(0);
		this.setStorecount(0);
		this.setFieldloadcount(0);
		this.setFieldstorecount(0);
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
	
	public void incrementFieldLoadCount() {
		this.fieldLoadCount++;
	}
	
	public void incrementFieldStoreCount() {
		this.fieldStoreCount++;
	}
	
	public void reset(){
		this.setBasicBlockCount(0);
		this.setLoadcount(0);
		this.setStorecount(0);
		this.setFieldloadcount(0);
		this.setFieldstorecount(0);	
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

	public long getFieldLoadCount() {
		return fieldLoadCount;
	}

	public void setFieldloadcount(long fieldloadcount) {
		this.fieldLoadCount = fieldloadcount;
	}

	public long getFieldStoreCount() {
		return fieldStoreCount;
	}

	public void setFieldstorecount(long fieldstorecount) {
		this.fieldStoreCount = fieldstorecount;
	}
	
	@Override
	public String toString() {
		String res = "";
		res += "Basic Block Count: " + getBasicBlockCount() + System.lineSeparator();
		res += "Store Count: " + getStorecount() + System.lineSeparator();
		res += "Load Count: " + getLoadcount() + System.lineSeparator();
		res += "Field Store Count: " + getFieldStoreCount() + System.lineSeparator();
		res += "Fiel Load Count: " + getFieldLoadCount();
		return res;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
